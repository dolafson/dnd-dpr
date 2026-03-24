@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpellRemote
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.NumericRange
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class EditableFields (
    var remoteId: Int, // cross-reference to original character from dndbeyond
    var level: Int,
    var name: String,
    var plan: MutableMap<String,PlanLevel> = mutableMapOf(),
    var alwaysPreparedSpells: List<PreparedSpellRemote> = mutableListOf()
){
    constructor(name: String, character: EditableCharacter, characterLevel: NumericRange): this(character)
    {
        this.level = characterLevel.current
        this.name = name
        // use json serialization to get a deep copy
        this.plan = Json.decodeFromString (Json.encodeToString (character.editableFields.plan))
    }

    constructor(character: Character): this(
        character.characterData.id!!,
        character.getLevel(),
        character.getName(),
        mutableMapOf(),
        character.getAlwaysPreparedSpells())
    {
        // set up iterators to populate plan with pre-selected values
        val asiFeatIterator = character.getFeatAddedList().filter { it.isASI() }.mapNotNull { Feat.fromNameWithWS(it.definition.name) }.iterator()

        val spellLevelMap = mutableMapOf<Int,Iterator<Spell>>()
        for (spellLevel in 1..9) {
            // we want to populate the plan with spells, but not the always prepared ones
            spellLevelMap[spellLevel] = character.getPreparedSpells().filter {
                !it.alwaysPrepared && !it.isRitual() && it.properties.Level == spellLevel
            }.iterator()
        }

        for (tmpLevel in 1..20) {
            val planLevel = PlanLevel()
            plan.put("$tmpLevel", planLevel)

            if (character.getLevelsForFightingStyle().contains(tmpLevel)) {
                // addFS
                planLevel.feat = character.getFeatAddedList()
                    .filter { it.isFightingStyle() }
                    .mapNotNull { Feat.fromNameWithWS(it.definition.name) }
                    .firstOrNull()
            }
            else if (character.getLevelsForAbilityIncrease().contains(tmpLevel)) {
                planLevel.feat = if (asiFeatIterator.hasNext()) asiFeatIterator.next() else null
            }

            if (character.getSubclassLevel() == tmpLevel) {
                planLevel.subclass = character.getSubclassName()
            }

            val newSlots = character.getSpellSlotsGainedAtCharacterLevel(tmpLevel)
            val spells = mutableListOf<String>()

            for (id in newSlots.indices) repeat (newSlots[id]) {
                val spellLevel = id + 1
                val iter = spellLevelMap[spellLevel]
                spells.add (if (iter != null && iter.hasNext()) iter.next().name else "")
            }

            if (tmpLevel == character.getLevel()) {
                // check for any spells from dndbeyond character not assigned to a slot ...
                // ( this happens with Kael at char level 2, they have 1 extra 1st level spell i can't account for )
                // remainder should be assigned to characters current level (not a future level!)

                for (spellLevel in 1..9) {
                    val iterator = spellLevelMap[spellLevel]!!
                    if (iterator.hasNext()) {
                        val remainder = mutableListOf<String>()
                        while (iterator.hasNext()) remainder.add(iterator.next().name)
                        println ("editableFields: assigning 'remainder' spell to level=${ character.getLevel() }, remainder=$remainder")
                        println("before rmdr, spells = ${ spells }")
                        spells += remainder
                        println("after rmdr, spells = ${ spells }")
                    }
                }
            }

            planLevel.spells = spells
        }
    }

    fun toPrettyPlan(): String {
        val buf = StringBuilder()
        for ((key, value) in plan) { buf.append("$key=$value").append("\n") }
        return "[$buf]"
    }

}
