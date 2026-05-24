package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.character.actions.ActionAdded
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.monsters.actions.MonsterAction
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffectShape

class SpellLikeAction(
    name: String,
    description: String,
) : PreparedSpell (true, Spell("Fake Book 2014", description, name, makeProps(), ""))
{
    constructor(actionModifier: ActionModifier, actionAdded: ActionAdded) : this(actionAdded.name, actionAdded.description ?: "")
    {
        if (actionAdded.saveStatId == null || actionModifier != ActionModifier.BreathWeapon) {
            throw IllegalArgumentException("currently only BreathWeapon is supported for SpellLikeAction")
        }

        properties.Damage = actionAdded.dice?.diceString // may not get used, but might as well

        val abilityType = AbilityType.entries[actionAdded.saveStatId]
        val damageType  = actionAdded.name.substringAfterLast('(').substringBeforeLast(')')

        properties.dataDatarecords = listOf(
            getBreathWeaponAttack (abilityType, actionAdded.saveFailDescription!!, actionAdded.saveSuccessDescription!!),
            getBreathWeaponDamage (damageType,  actionAdded.dice?.diceCount ?: 1,  actionAdded.dice?.diceValue.toString())
        )
    }

    constructor(monsterAction: MonsterAction) : this(monsterAction.name, monsterAction.desc)
    {
        val dc = monsterAction.dc
        if (dc == null || ! name.contains("Breath")) { // hack for dragon breath weapon
            throw IllegalArgumentException("currently only BreathWeapon is supported for SpellLikeAction")
        }

        val abilityType = AbilityType.fromShortName(dc.dc_type.name)!!
        val damageType  = monsterAction.damage?.get(0)?.damage_type?.name ?: "unknown"
        val diceSplit   = monsterAction.damage?.get(0)?.damage_dice!!.split("d")

        properties.dataDatarecords = listOf(
            getBreathWeaponAttack (abilityType, monsterAction.desc, dc.success_type),
            getBreathWeaponDamage (damageType, diceSplit[0].toInt(), diceSplit[1])
        )
    }

    private fun getBreathWeaponAttack(abilityType: AbilityType, onFail: String, onSucceed: String): DataDatarecord {
        // in this case, AOE is too difficult to extract from character data ...
        // and we aren't really using AOE shape yet anyway ...
        properties.dataRangeNum = 30
        return DataDatarecord("$name Attack", payload = Attack(
            name = name,
            description = description,
            save = Attack.Save(abilityType, onFail, onSucceed),
            aoe = AreaOfEffect(AreaOfEffectShape.Cone, "15"),
            range = properties.toString()
        ))
    }

    private fun getBreathWeaponDamage(damageType: String, diceCount: Int, diceSize: String): DataDatarecord {
        return DataDatarecord("$name Damage", parent = "$name Attack", payload = Damage(
            ability = "none",
            damageType = damageType,
            diceCount = diceCount,
            diceSize = "d$diceSize"
        ))
    }

    companion object {
        fun makeProps() = Properties("Instantaneous", "Spells", 0, "")
    }
}

