package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.Properties
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class EditableCharacter (
    val from: Character,
    val editableFields: EditableFields
) : Character(from.characterData, from.id, from.message, from.success)
{
    fun numberOfSlotsAtSpellLevel(spellLevel: Int): Int {
        val slotList = getSpellSlotsIncludingExtraForPrepared()
        val result = slotList[spellLevel-1] // 1-based to 0-based indexing
        //println("hasSpellsAtSpellLevel($spellLevel) -> list=$slotList, result=$result")
        return result ?: 0
    }

    fun getSpellSlotsAtCharacterLevel(characterLevel: Int): List<Int> {
        val result = characterData.classes.first().definition.spellRules?.levelSpellSlots?.get(characterLevel) ?: emptyList()

        // println("getSpellSlotsAtCharacterLevel($characterLevel) -> result=$result")
        return result
    }

    fun getNewSpellSlotsAtCharacterLevel(characterLevel: Int): List<Int> {
        if (characterLevel == 0) return emptyList()

        val slotsNow = getSpellSlotsAtCharacterLevel(characterLevel)
        if (characterLevel == 1) return slotsNow

        val slotsBefore = getSpellSlotsAtCharacterLevel(characterLevel-1)
        var result: MutableList<Int> = mutableListOf()

        for (id in slotsBefore.indices.sorted()) {
            // println("get new slots, id=$id, now=${ slotsNow[id] }, before=${ slotsBefore[id] }, delta=${ slotsNow[id] - slotsBefore[id] }")
            result.add(slotsNow[id] - slotsBefore[id])
        }
        return result
    }

    fun hasNewSpellSlotsAtCharacterLevel(characterLevel: Int): Boolean {
        val newSlots = getNewSpellSlotsAtCharacterLevel(characterLevel)
        // println("hasNewSpellSlotsAtCharacterLevel($characterLevel) -> list=$newSlots")
        return newSlots.filter { it != 0 }.isNotEmpty()
    }

    override fun getModifiedAbilityScore(a: AbilityType): Int {
        var increase = 0
        for (i in from.getLevel()..getLevel()) {
            if (a == editableFields.plan[i.toString()]?.asi1) increase++
            if (a == editableFields.plan[i.toString()]?.asi2) increase++
        }
        return from.getModifiedAbilityScore(a) + increase
    }

    override fun getLevel(): Int {
        return editableFields.level
    }

    override fun getName(): String {
        return editableFields.name
    }

    override fun getSubclassName(): String? {
        val sub = from.getSubclassName()
        if (sub != null) return sub

        for (i in from.getLevel()..getLevel()) {
            val sub = editableFields.plan[i.toString()]?.subclass
            if (sub != null) { return sub }
        }
        return null
    }

    override fun getFeatList(): List<Feat> {
        val result = mutableListOf<Feat>()
        for (i in 1..getLevel()) {
            val feat = editableFields.plan["$i"]?.feat
            if (feat != null) { result.add(feat) }
        }
        return result
    }

    override fun getPreparedSpells(): List<Spell> {
        val result = mutableListOf<Spell>()
        for (i in 1..getLevel()) {
           (editableFields.plan["$i"]?.spells ?: emptyList()).forEach { spellName ->
                try { result.add (Globals.getSpell (spellName, is2014())) } catch (e: Exception) {}
            }
        }
        return result
    }
    fun getSpellSelectionsBySpellLevel(currentLevel: Int): Map<Int, SpellSelections> {
        val result = mutableMapOf<Int, SpellSelections>()
        // println("currentLevel: $currentLevel")

        if (editableFields.plan.isEmpty()) {
            return result
        }

        // initialize
        for (spellLevel in 1..9) if (numberOfSlotsAtSpellLevel(spellLevel) > 0) {
            result[spellLevel] = SpellSelections()
        }

        for (planEntry in editableFields.plan) {
            val planCharacterLevel = planEntry.key.toInt()
            if (planCharacterLevel > currentLevel) break

            planEntry.value.spells.forEach { s ->
                // TODO: optimize this
                try {
                    val spell = Globals.getSpell(s, is2014())
                    val spellLevel = spell.properties.Level

                    if (planCharacterLevel < currentLevel) {
                        result[spellLevel]!!.readOnlyList.add(spell)
                    } else {
                        result[spellLevel]!!.editableList.add(spell)
                    }
                } catch (e: Exception) {
                    println("unable to display details for spell ${s}")
                }
            }
        }

        // final audit: if not all slots are filled, add a placeholder
        for (spellLevel in 1..9) {
            if (result[spellLevel] == null) break

            val max = numberOfSlotsAtSpellLevel(spellLevel)
            val size = result[spellLevel]!!.size()
            // println("spellLevel: $spellLevel, maxSlots: $max, filled: $size")

            for (i in size..max-1) {
                // TODO: better placeholder ...
                val props = Properties("","",spellLevel,"")
                result[spellLevel]!!.editableList.add(Spell("TODO","TODO","TODO", props,"TODO"))
            }
        }

        // println("all spell selections: $result")
        return result
    }

}