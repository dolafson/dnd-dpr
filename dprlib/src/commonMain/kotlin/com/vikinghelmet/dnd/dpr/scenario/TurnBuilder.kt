package com.vikinghelmet.dnd.dpr.scenario

import com.vikinghelmet.dnd.dpr.action.*
import com.vikinghelmet.dnd.dpr.action.enums.WeaponProperty
import com.vikinghelmet.dnd.dpr.character.PlayerCharacter
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.scenario.combat.CombatantWithStatus
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Globals

class TurnBuilder(
    val attacker: Combatant,
    val target: Combatant, // target is used for assignment only (result is otherwise target independent)
) {
    fun getPossibleTurns(actionsAvailable: ActionsAvailable, targetProximity: Int): List<Turn>
    {
        val actionList = actionsAvailable.getPrimaryAction(targetProximity)

        val bonusActionSpells = attacker.getPreparedBonusActionSpells(targetProximity)
        val turnOptions = ArrayList<Turn>()

        for (action in actionList) {
            if (action is Spell) { // generally spell attacks do not get bonus actions
                turnOptions.add(Turn(attacks = listOf(Attack(target = target, action = action))))
                continue
            }
            if (action !is Weapon) {
                Globals.debug("error: action without a spell or a weapon")
                continue
            }
            // remainder pertains to weapon attacks
            fun makeBA(bonus: Action) = listOf(Attack(target = target, action = bonus, isBonusAction = true))
            fun makeTurnStartingWithBA(bonus: Action) = Turn(attacks = makeBA(bonus) + getAttacksForTurn(action))
            fun makeTurnEndingWithBA(bonus: Action)   = Turn(attacks = getAttacksForTurn(action) + makeBA(bonus))

            // light weapon ?  see if you have a 2nd one to use in a BA
            val lightWeapons = if (action.hasWeaponProperty (WeaponProperty.Light))
                actionsAvailable.getLightWeaponsForBA (targetProximity, action) else emptyList()

            if (lightWeapons.isNotEmpty()) {
                lightWeapons.forEach { turnOptions.add (makeTurnEndingWithBA (it)) }
            }
            else {
                // if you didn't find a pair of light weapons, just use the first weapon w/out a BA
                turnOptions.add (Turn(attacks = getAttacksForTurn(action)))
            }

            // bonus action spells: mostly for ranger and paladin
            bonusActionSpells.forEach {
                turnOptions.add (makeTurnEndingWithBA (it))
                // sometimes starting with BA is better
                if (!it.takeImmediatelyAfterHitting()) { turnOptions.add (makeTurnStartingWithBA(it)) }
            }
        }
        return turnOptions
    }

    private fun getAttacksForTurn(action: Action): List<Attack> {
        val result = mutableListOf<Attack>()
        val extra = if (attacker !is PlayerCharacter) 0 else attacker.getExtraAttacks()

        repeat(1+extra) {
            result.add(Attack(target = target, action = action),)
        }

        if (action.getActionName() == "Multiattack") {
            val monster = (if (attacker is CombatantWithStatus) attacker.combatant else attacker) as Monster
            val expanded = monster.expandMultiAttack()
            for (w in expanded) {
                result.add(Attack(target = target, action = w))
            }
        }
        return result
    }
}