package com.vikinghelmet.dnd.dpr.action

import com.vikinghelmet.dnd.dpr.action.enums.AttackType
import com.vikinghelmet.dnd.dpr.action.enums.WeaponProperty
import com.vikinghelmet.dnd.dpr.character.inventory.MasteryProperty
import com.vikinghelmet.dnd.dpr.scenario.Scenario
import com.vikinghelmet.dnd.dpr.util.Constants
import kotlinx.serialization.Serializable

@Serializable
data class Weapon (
    val name: String,
    var attackType: AttackType = AttackType.Melee,
    var range : Int = Constants.MELEE_RANGE,
    var longRange: Int = Constants.MELEE_RANGE,
    var bonusToHit: Int = 0,
    var propertyNames: List<String> = emptyList(),
    var _damageList: MutableList<Damage> = mutableListOf(),
) : MeleeOrRangeAction, Action
{
    override fun getActionName(): String { return name }
    override fun getDamageList()  = _damageList
    override fun getAttackBonus() = bonusToHit
    override fun getNumTargetsAffected(scenario: Scenario) = 1

    // other methods
    fun hasWeaponProperty(prop: WeaponProperty)   = propertyNames.contains(prop.name)
    fun hasMasteryProperty(prop: MasteryProperty) = propertyNames.contains(prop.name)

    override fun toString(): String {
        return name
    }

}
