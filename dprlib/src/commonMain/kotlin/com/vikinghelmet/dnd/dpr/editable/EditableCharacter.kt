package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpellRemote
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.Properties
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Constants
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
    override fun getAlwaysPreparedSpells(): List<PreparedSpellRemote> = editableFields.alwaysPreparedSpells

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

    override fun getPreparedSpells(): List<PreparedSpell> {
        val result = mutableListOf<PreparedSpell>()

        result.addAll(from.getPreparedSpells())
        // add plan-based prepared spells to the ones on the original character sheet

        // loop across plan up to current selected level
        for (i in 1..getLevel()) {
            // get the planned spells at the given level
           (editableFields.plan["$i"]?.spells ?: emptyList()).forEach { spellName ->
               // if spell name is not already in the prepared list, add it
               if (! result.map {it.name}.contains(spellName)) {
                   try { result.add (PreparedSpell(spellName, is2014())) } catch (e: Exception) {}
               }
            }
        }

        return result
    }

    fun getSpellSelectionsBySpellLevel(currentCharacterLevel: Int): Map<Int, List<Spell>> {
        // val result = mutableMapOf<Int, SpellToPlanLevelMap>()
        val result = mutableMapOf<Int, MutableList<Spell>>()
        // println("currentLevel: $currentLevel")

        if (editableFields.plan.isEmpty()) {
            return result
        }

        result[0] = mutableListOf()

        // initialize
        for (spellLevel in Constants.SPELL_LEVELS) if (getNumberOfSlotsAtSpellLevel(spellLevel) > 0) {
            result[spellLevel] = mutableListOf()
        }

        for (planEntry in editableFields.plan) {
            val planCharacterLevel = planEntry.key.toInt()
            if (planCharacterLevel > currentCharacterLevel) break

            planEntry.value.spells.forEach { s ->
                // TODO: optimize this
                try {
                    val spell = Globals.getSpell(s, is2014())
                    result[spell.properties.Level]!!.add(spell)

                } catch (e: Exception) {
                    println("unable to display details for spell ${s}")
                }
            }
        }

        // final audit: if not all slots are filled, add a placeholder
        for (spellLevel in Constants.SPELL_LEVELS) {
            if (result[spellLevel] == null) break

            val max = getNumberOfSlotsAtSpellLevel(spellLevel)
            val size = result[spellLevel]!!.size
            // println("spellLevel: $spellLevel, maxSlots: $max, filled: $size")

            repeat (max-size) {
                // TODO: better placeholder ...
                val props = Properties("","",spellLevel,"")
                result[spellLevel]!!.add(Spell("TODO","TODO","TODO", props,"TODO")) // hack
            }
        }

        // always prepped spells are usually not tracked in the plan; they get added here

        //println ("getSpellSelectionsBySpellLevel, prepared = ${ getPreparedSpells() } ")
        for (prep in getPreparedSpells()) {
            val spellLevel = prep.properties.Level
            if (result[spellLevel] == null) continue

            val spellNames = result[spellLevel]!!.map { it.name }

            if (! spellNames.contains(prep.name)) {
                // println("result[$spellLevel] = ${result[spellLevel]!!} , adding prepped spell ${prep.name}")
                result[spellLevel]!!.add(prep)
            }
        }

        // println("all spell selections: $result")
        return result
    }

}