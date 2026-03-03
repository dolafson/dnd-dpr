package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellAttack
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import com.vikinghelmet.dnd.dpr.util.Globals

data class RunningSpell (val startTurn: Int, val spell: Spell)

object EffectManager {
    var runningSpellList = mutableListOf<RunningSpell>()

    fun add(turnId: Int, spell: Spell) {
        runningSpellList.add(RunningSpell(turnId, spell))
    }

    fun getRunningSpells(): MutableList<Spell> {
        val result = mutableListOf<Spell>()
        for (running in runningSpellList) result.add(running.spell)
        return result
    }

    fun pruneRunningSpells(turnId: Int) {
        val iterator = runningSpellList.listIterator()
        while (iterator.hasNext()) {
            val running = iterator.next()
            val deltaT = turnId - running.startTurn
            val spell = running.spell
            val duration = spell.getDuration() ?: 0
            if (deltaT >= duration) {
                Globals.debug("spell is complete, remove from running list: "+spell.name)
                iterator.remove()
            }
        }
    }

    fun pruneSpellsWaitingForNextAttack(spellAttack: SpellAttack?) {
        val iterator = runningSpellList.listIterator()
        while (iterator.hasNext()) {
            val running = iterator.next()
            val spell = running.spell

            if (spell.appliesEffectToNextTargetSaveOnly() && spellAttack != null && spellAttack.isSavingThrowAttack()) {
                Globals.debug("spell was waiting for next saving throw, removing it from running list: "+spell.name)
                iterator.remove()
            }

            if (spell.appliesToNextMeleeOrRangeAttackOnly() && (spellAttack == null || spellAttack.isMeleeOrRangeAttack())) {
                Globals.debug("spell was waiting for next melee/range attack, removing it from running list: "+spell.name)
                iterator.remove()
            }
        }
    }

    fun getPreconditions(turnId: Int, actionCount: Int, turn: Turn, currentSpell: Spell?): Preconditions? {
        if (turnId == 1 && actionCount == 1) return turn.preconditions
        val precondition = Preconditions()

        for (running in runningSpellList) {
            val effect = running.spell.getTargetEffect()

            // extra damage from old spells can be applied independently (does not depend on "currentSpell")
            for (damage in effect.attackerExtraDamageOnHit) {
                if (precondition.bonusDamageDice == null) {
                    precondition.bonusDamageDice = DiceBlockHelper.emptyBlock()
                }
                precondition.bonusDamageDice = precondition.bonusDamageDice!!.add (DiceBlockHelper.getDiceBlock (damage))
            }

            if (currentSpell != null) {
                currentSpell.preProcessEffectsOfOldSpell (running.spell, precondition)
            }
        }
        return precondition
    }

    fun attackerHasAdvantage(): Boolean {
        for (running in runningSpellList) {
            if (running.spell.getTargetEffect().attackerHasAdvantage == true) return true
        }
        return false
    }

    fun isAutoCrit(): Boolean {
        for (running in runningSpellList) {
            if (running.spell.getTargetEffect().attackerAutoCrit == true) return true
        }
        return false
    }
    override fun toString(): String {
        val buf = StringBuilder()
        for (running in runningSpellList) buf.append(running.spell.getTargetEffect())
        return buf.toString()
    }
}
