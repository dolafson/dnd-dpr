package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.Properties
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.EditableAbilityMap
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.NumericRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class EditableCharacter (
    val from: Character,
    val editableFields: EditableFields
) : Character(from.characterData, from.id, from.message, from.success)
{
    // cat leif.full.json | jq -c .data.classes[].definition.spellRules.levelSpellSlots[] |head
    //[0,0,0,0,0,0,0,0,0]
    //[2,0,0,0,0,0,0,0,0]
    //[2,0,0,0,0,0,0,0,0]
    //[3,0,0,0,0,0,0,0,0]
    //[3,0,0,0,0,0,0,0,0]
    //[4,2,0,0,0,0,0,0,0]
    //[4,2,0,0,0,0,0,0,0]
    //[4,3,0,0,0,0,0,0,0]
    //[4,3,0,0,0,0,0,0,0]
    //[4,3,2,0,0,0,0,0,0]

    fun numberOfSlotsAtSpellLevel(spellLevel: Int): Int {
        val slotList = getSpellSlots() // spellsBySpellLevel, returns different list based on current character level
        val result = slotList[spellLevel-1] // 1-based to 0-based indexing
        println("hasSpellsAtSpellLevel($spellLevel) -> list=$slotList, result=$result")
        return result ?: 0
    }

    fun getAbilityMap(): EditableAbilityMap {
        val result = mutableMapOf<AbilityType, NumericRange>()
        AbilityType.entries.forEach {
            result[it] = NumericRange(getModifiedAbilityScore(it), 20)
        }
        return EditableAbilityMap(result)
    }

    override fun getModifiedAbilityScore(a: AbilityType): Int {
        if (editableFields.plan.isEmpty()) {
            return editableFields.stats[a] ?: 0     // TODO: remove this once plan is fully realized
        }

        var increase = 0
        for (i in from.getLevel()..getLevel()) {
            increase += (editableFields.plan[i.toString()]?.asi[a] ?: 0)
        }
        return from.getModifiedAbilityScore(a) + increase
    }

    override fun getLevel(): Int {
        return editableFields.level
    }

    override fun getName(): String {
        return editableFields.name
    }

    fun getSpellSelectionsBySpellLevel(currentLevel: Int): Map<Int, SpellSelections> {
        val result = mutableMapOf<Int, SpellSelections>()
        println("currentLevel: $currentLevel")

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

            planEntry.value.spells.forEach { spellName ->
                // TODO: optimize this
                try {
                    val spell = Globals.getSpell(spellName, is2014())
                    val spellLevel = spell.properties.Level

                    if (planCharacterLevel < currentLevel) {
                        result[spellLevel]!!.readOnlyList.add(spell)
                    } else {
                        result[spellLevel]!!.editableList.add(spell)
                    }
                } catch (e: Exception) {
                    println("unable to display details for spell $spellName")
                }
            }
        }

        // final audit: if not all slots are filled, add a placeholder
        for (spellLevel in 1..9) {
            if (result[spellLevel] == null) break

            val max = numberOfSlotsAtSpellLevel(spellLevel)
            val size = result[spellLevel]!!.size()
            println("spellLevel: $spellLevel, maxSlots: $max, filled: $size")

            for (i in size..max-1) {
                // TODO: better placeholder ...
                val props = Properties("","",spellLevel,"")
                result[spellLevel]!!.editableList.add(Spell("TODO","TODO","TODO", props,"TODO"))
            }
        }

        println("all spell selections: $result")

        return result
    }

}