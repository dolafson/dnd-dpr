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

    fun getSpellSelectionsBySpellLevel(currentCharacterLevel: Int): Map<Int, SpellToPlanLevelMap> {
        val result = mutableMapOf<Int, SpellToPlanLevelMap>()
        // println("currentLevel: $currentLevel")

        if (editableFields.plan.isEmpty()) {
            return result
        }

        // initialize
        for (spellLevel in 1..9) if (getNumberOfSlotsAtSpellLevel(spellLevel) > 0) {
            result[spellLevel] = SpellToPlanLevelMap()
        }

        for (planEntry in editableFields.plan) {
            val planCharacterLevel = planEntry.key.toInt()
            if (planCharacterLevel > currentCharacterLevel) break

            planEntry.value.spells.forEach { s ->
                // TODO: optimize this
                try {
                    val spell = Globals.getSpell(s, is2014())
                    val spellLevel = spell.properties.Level
                    result[spellLevel]!!.spellToPlanLevelMap.put(spell,planCharacterLevel)

                } catch (e: Exception) {
                    println("unable to display details for spell ${s}")
                }
            }
        }

        // final audit: if not all slots are filled, add a placeholder
        for (spellLevel in 1..9) {
            if (result[spellLevel] == null) break

            val max = getNumberOfSlotsAtSpellLevel(spellLevel)
            val size = result[spellLevel]!!.spellToPlanLevelMap.size
            // println("spellLevel: $spellLevel, maxSlots: $max, filled: $size")

            repeat (max-size) {
                // TODO: better placeholder ...
                val props = Properties("","",spellLevel,"")
                result[spellLevel]!!.spellToPlanLevelMap.put(Spell("TODO","TODO","TODO", props,"TODO"), 100) // hack
            }
        }

        // println("all spell selections: $result")
        return result
    }

}