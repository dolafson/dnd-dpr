package com.vikinghelmet.dnd.dpr.turn

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.monsters.Monster
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlinx.serialization.json.Json

object TurnCalculator {

    val spells = ArrayList<Spell>()
    val monsters = ArrayList<Monster>()
    val turns = ArrayList<Turn>()
    var character: Character? = null

    fun calculateDPRForAllTurns() {
        var turnId = 1
        var scenarioTotalDamage = 0f

        for (turn in turns) {
            var dpr = 0f
            var actionCount = 1
            AttackResultFormatter.header()

            for (attack in turn.attacks) {
                val resultList = calculateDPR(turnId, actionCount, turn, attack)
                for (result in resultList) {
                    dpr += result.damagePerRound.select (result.getAvgMinMaxSelection())
                }
                actionCount++
            }

            AttackResultFormatter.footer(turnId, "TURN TOTAL", dpr)

            EffectManager.pruneRunningSpells(turnId)
            turnId++
            scenarioTotalDamage += dpr
        }

        AttackResultFormatter.footer("", "SCENARIO TOTAL", scenarioTotalDamage)
        System.err.println()
    }

    fun calculateDPR(turnId: Int, actionId: Int, turn: Turn, attack: Attack): List<AttackResult>
    {
        val monster = getMonster(attack.monster)
        if (monster == null) {
            println("monster not found: "+attack.monster)
            return emptyList()
        }

        val weapon = getWeapon(attack.attack)
        val spell  = getSpell(attack.attack)

        if (weapon == null && spell == null) {
            System.err.println()
            System.err.println("spell or weapon not found: "+attack.attack)
            System.err.println()
            System.err.println("character weapons: "+ character!!.getWeaponNames())
            System.err.println()
            return emptyList()
        }

        attack.preconditions = EffectManager.getPreconditions(turnId, actionId, turn, spell)

        val dpr = DamagePerRound(character!!)

        if (weapon != null) {
            val meleeOrRangeAttack = MeleeOrRangeAttack(character!!, null, weapon)
            val attackResult = dpr.getMeleeOrRangeDPR (meleeOrRangeAttack, attack, monster)

            attackResult.output(character!!, monster, attack, turnId, actionId, weapon)

            EffectManager.pruneSpellsWaitingForNextAttack(null)
            return listOf(attackResult)
        }

        if (spell == null) return emptyList() // should not get here due to if(w/s) above; this is just to make the compiler happy

        val resultList = mutableListOf<AttackResult>()
        var effectCount = 1
        for (spellAttack in spell.getSpellAttacks()) {
            val attackResult = dpr.getSpellDPR(spellAttack, spell, attack, monster)

            spell.postProcessEffectsOfOldSpells(EffectManager.getRunningSpells(), attackResult)

            attackResult.output(character!!, monster, attack, turnId, actionId, effectCount++, spellAttack)

            EffectManager.pruneSpellsWaitingForNextAttack(spellAttack) // do this pruning before adding current spell to the manager (below)
            if (attackResult.targetHadDisadvantageOnSave == true) {

            }

            resultList.add(attackResult)
        }

        if (!spell.getTargetEffect().isEmpty()) { // we only track spells with a non-empty effect
            EffectManager.add(turnId, spell)
            Globals.debug("adding to running list: "+spell.name)
        }

        return resultList
    }

    fun getMonster(name: String): Monster? {
        if (monsters.isEmpty()) return null

        for (monster in monsters) {
            if (monster.name == name) {
                return monster
            }
        }
        return null
    }

    fun getSpell(name: String?): Spell? {
        if (name == null || spells.isEmpty()) return null

        for (spell in spells) {
            if (spell.name == name) {
                if (!spell.isSameIn2014And2024() && character!!.is2014() != spell.is2014()) {
                    continue
                }

                return spell
            }
        }
        return null
    }

    fun getWeapon(name: String?): Weapon? {
        if (name == null || character == null || character!!.getWeaponList().isEmpty()) return null
        for (weapon in character!!.getWeaponList()) if (weapon.name == name) return weapon
        return null
    }

    fun dump(arg: String) {
        if (!arg.contains(":")) {
            for (item in spells)    println(Json.encodeToString(item))
            for (item in monsters)  println(Json.encodeToString(item))
            for (item in turns)   println(Json.encodeToString(item))
            println(Json.encodeToString(character))
            return
        }

        val dumpType = arg.split(":")[1]
        when (dumpType) {
            "spells" -> {
                for (item in spells)  println(Json.encodeToString(item))
            }
            "monsters" -> {
                for (item in monsters)  println(Json.encodeToString(item))
            }
            "attacks" -> {
                for (item in turns)  println(Json.encodeToString(item))
            }
            "character" -> {
                println(Json.encodeToString(character))
            }
        }
    }

    fun search(arg: String) {
        val searchValue = arg.split(":")[1]
        for (item in spells) if (item.name.contains(searchValue))  println(Json.encodeToString(item))
        for (item in monsters) if (item.name.contains(searchValue))  println(Json.encodeToString(item))
    }

}
