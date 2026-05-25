package com.vikinghelmet.dnd.dpr.monsters.actions

import com.vikinghelmet.dnd.dpr.action.Weapon
import com.vikinghelmet.dnd.dpr.action.enums.AttackType
import com.vikinghelmet.dnd.dpr.monsters.Attack
import com.vikinghelmet.dnd.dpr.monsters.Dc
import com.vikinghelmet.dnd.dpr.monsters.Usage
import com.vikinghelmet.dnd.dpr.monsters.damage.Damage
import com.vikinghelmet.dnd.dpr.util.Constants
import kotlinx.serialization.Serializable

@Serializable
data class ActionX(
    val action_name: String,
    val count: Int,
    val type: String
)

@Serializable
data class MonsterAction(
    val name: String,
    val desc: String,

    // sadly, everything in here is optional except for name and desc; for multiattack all other fields are missing
    val attacks: List<Attack> ?= emptyList(),
    val actions: List<ActionX> ?= emptyList(),
    val attack_bonus: Int ?= 0,
    val damage: List<Damage> ?= emptyList(),
    val dc: Dc?= null,
    val multiattack_type: String ?= null,
    val options: ActionOptions ?= null,
    val action_options: ActionOptions ?= null,
    val usage: Usage?= null
) {
    fun toWeapon(): Weapon
    {
        var attackType: AttackType
        var range: Int
        var longRange: Int
        var bonusToHit = 0

        if (desc.isEmpty() || !desc.contains("range")) {
            attackType = AttackType.Melee
            range = Constants.MELEE_RANGE
            longRange = range
        }
        else {
            attackType = if (desc.contains("Melee or Range")) AttackType.MeleeOrRange else AttackType.Range

            val reach      = desc.replace(".* range ".toRegex(), "").replace(" .*".toRegex(),"")   //  "... 30/120 ft., ...",
            val reachSplit = reach.split("/")
            range     = reachSplit[0].toInt()
            longRange = reachSplit[1].toInt()
        }

        if (attack_bonus != null) {
            bonusToHit = attack_bonus
        }

        val damageList = mutableListOf<com.vikinghelmet.dnd.dpr.action.Damage>()

        if (! damage.isNullOrEmpty()) {
            damage.forEach { d ->
                damageList.add(com.vikinghelmet.dnd.dpr.action.Damage.fromStringPair(d.damage_dice!!, d.damage_type!!.name)) // TODO: values are sometimes null?
            }
        }

        return Weapon (name, attackType, range, longRange, bonusToHit, emptyList(), damageList)
    }


}