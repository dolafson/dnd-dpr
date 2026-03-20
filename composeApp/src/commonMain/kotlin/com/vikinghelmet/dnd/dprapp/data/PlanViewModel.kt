package com.vikinghelmet.dnd.dprapp.data

import androidx.compose.ui.graphics.Color
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter
import com.vikinghelmet.dnd.dpr.editable.PlanLevel
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Globals

class PlanViewSpell(val spellLevel: Int, var selectedSpell: String, val options: List<Pair<String,Color>>){
    override fun toString(): String {
        return "($spellLevel: $selectedSpell)"
    }
}

class PlanViewLevel(
    val level: Int,

    val addFeat: Boolean,
    val addFS: Boolean, // subset of feat
    val addSpell: Boolean,

    // these fields should be an exact match with file storage
    var subclass: String? = null,
    var feat: Feat? = null,
    var asi1: AbilityType? = null,
    var asi2: AbilityType? = null,

    // spells are a superset of what's in file storage; this version includes the list of options available
    var spellsToAdd: List<PlanViewSpell> = mutableListOf()
) {
    override fun toString(): String {
        return "level=$level, flags=($addFeat,$addFS,$addSpell), feat=($feat,$asi1,$asi2) spells=$spellsToAdd"
    }
}

data class PlanViewModel(var plan: MutableList<PlanViewLevel> = mutableListOf())
{
    override fun toString(): String {
        val buf = StringBuilder()
        plan.forEach { buf.append(it.toString()+"\n") }
        return "[$buf]"
    }

    constructor(character: EditableCharacter) : this() {
        // println("planView constructor, view hash = ${this.hashCode()}, plan hash = ${plan.hashCode()}")
        val asiLevelList = character.getLevelsForAbilityIncrease()
        val fsLevelList = character.getLevelsForFightingStyle()

        //for (tmpLevel in character.from.getLevel()..20)
        for (tmpLevel in 0..20) {
            val p = PlanViewLevel (tmpLevel,
                asiLevelList.contains(tmpLevel),
                fsLevelList.contains(tmpLevel),
                character.hasNewSpellSlotsAtCharacterLevel(tmpLevel))

            val savedPlanLevel = character.editableFields.plan["$tmpLevel"]
            if (savedPlanLevel != null) {
                p.feat = savedPlanLevel.feat
                p.asi1 = savedPlanLevel.asi1
                p.asi2 = savedPlanLevel.asi2
                p.subclass = savedPlanLevel.subclass
            }

            val savedSpells = getSavedSpells(character, tmpLevel) // this list will shrink as we progress thru the loop below

            if (p.addSpell) {
                val spellsToAdd = mutableListOf<PlanViewSpell>()
                val slotList = character.getNewSpellSlotsAtCharacterLevel(tmpLevel)

                for (id in slotList.indices) for (i in 1..slotList[id]) {
                    val spellLevel = id+1
                    val spellNames = character.getSpellsForClass().filter { it.properties.Level == spellLevel }.map { it.name }

                    val spell = getFirstMatchingSpellAndPruneIt(spellLevel, savedSpells)
                    if (spell != null) {
                        println("matching spell ${spell.name}")
                    }

                    spellsToAdd.add(
                        PlanViewSpell(spellLevel, spell?.name ?: "", spellNames.map { it -> Pair(it,Color.Black) }.toList())
                    )
                }
                p.spellsToAdd = spellsToAdd
            }

            plan.add(p)
        }
    }

    fun getSavedSpells(character: EditableCharacter, level: Int): MutableList<Spell> {
        val savedSpells: MutableList<Spell> = mutableListOf()

        val savedPlanLevel = character.editableFields.plan["$level"]
        if (savedPlanLevel?.spells != null) {
            savedPlanLevel.spells.forEach { spellName ->
                try {
                    savedSpells.add(Globals.getSpell(spellName, character.is2014()))
                } catch (e: Exception) {
                    println("unable to get spell $spellName")
                }
            }
        }
        return savedSpells
    }

    fun getFirstMatchingSpellAndPruneIt(spellLevel: Int, spellList: MutableList<Spell> ): Spell? {
        val index = spellList.indexOfFirst { it.properties.Level == spellLevel }
        if (index != -1) {
            return spellList.removeAt(index)
        }
        return null
    }

    fun toPersistentFormat(): MutableMap<String,PlanLevel> {
        val result = mutableMapOf<String,PlanLevel>()
        for (p in plan) {
            val spellList: List<String> = p.spellsToAdd.map { it.selectedSpell }.toList()
            result.put( "${p.level}", PlanLevel(p.feat, p.asi1, p.asi2, p.subclass, emptyMap(), spellList))
        }
        return result
    }
}