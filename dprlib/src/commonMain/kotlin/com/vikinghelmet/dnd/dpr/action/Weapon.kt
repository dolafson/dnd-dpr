package com.vikinghelmet.dnd.dpr.action

import com.vikinghelmet.dnd.dpr.action.enums.AttackType
import com.vikinghelmet.dnd.dpr.action.enums.WeaponProperty
import com.vikinghelmet.dnd.dpr.character.inventory.MasteryProperty
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
    override fun getNumTargetsAffected(numTargets: Int, targetSpacing: Int) = 1

    // other methods
    fun hasWeaponProperty(prop: WeaponProperty)   = propertyNames.contains(prop.name)
    fun hasMasteryProperty(prop: MasteryProperty) = propertyNames.contains(prop.name)

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Weapon

        if (range != other.range) return false
        if (longRange != other.longRange) return false
        if (bonusToHit != other.bonusToHit) return false
        if (name != other.name) return false
        if (attackType != other.attackType) return false
//        if (propertyNames != other.propertyNames) return false
//        if (_damageList != other._damageList) return false
        if (!propertyNames.equals(other.propertyNames)) return false
        //if (!_damageList.equals(other._damageList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = range
        result = 31 * result + longRange
        result = 31 * result + bonusToHit
        result = 31 * result + name.hashCode()
        result = 31 * result + attackType.hashCode()
        result = 31 * result + propertyNames.hashCode()
        result = 31 * result + _damageList.hashCode()
        return result
    }


}
