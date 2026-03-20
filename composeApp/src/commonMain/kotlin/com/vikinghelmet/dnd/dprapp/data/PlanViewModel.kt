package com.vikinghelmet.dnd.dprapp.data

import androidx.compose.ui.graphics.Color
import com.vikinghelmet.dnd.dpr.editable.EditableCharacter

class PlanSpellRecord(val spellLevel: Int, var selectedSpell: String, val options: List<Pair<String,Color>>){
    override fun toString(): String {
        return "($spellLevel: $selectedSpell)"
    }
}

class PlanLevel(
    val level: Int,
    val addFeat: Boolean,
    val addFS: Boolean, // subset of feat
    val addSpell: Boolean,

    var feat: String? = "",
    var asi1: String? = "",
    var asi2: String? = "",
    var spellsToAdd: List<PlanSpellRecord> = mutableListOf()
) {
    override fun toString(): String {
        return "level=$level, flags=($addFeat,$addFS,$addSpell), feat=($feat,$asi1,$asi2) spells=$spellsToAdd"
    }
}

data class PlanViewModel(var plan: MutableList<PlanLevel> = mutableListOf())
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
            val p = PlanLevel (tmpLevel,
                asiLevelList.contains(tmpLevel),
                fsLevelList.contains(tmpLevel),
                character.hasNewSpellSlotsAtCharacterLevel(tmpLevel))

            if (p.addSpell) {
                val spellsToAdd = mutableListOf<PlanSpellRecord>()
                val slotList = character.getNewSpellSlotsAtCharacterLevel(tmpLevel)

                for (id in slotList.indices) for (i in 1..slotList[id]) {
                    val spellLevel = id+1
                    val spellNames = character.getSpellsForClass().filter { it.properties.Level == spellLevel }.map { it.name }
                    spellsToAdd.add(
                        PlanSpellRecord(spellLevel, "", spellNames.map { it -> Pair(it,Color.Black) }.toList())
                    )
                }
                p.spellsToAdd = spellsToAdd
            }

            plan.add(p)
        }
    }
}