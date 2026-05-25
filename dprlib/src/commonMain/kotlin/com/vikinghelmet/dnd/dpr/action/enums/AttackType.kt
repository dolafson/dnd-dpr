package com.vikinghelmet.dnd.dpr.action.enums

enum class AttackType(val type: Int) {

    Melee(1),
    Range(2),
    MeleeOrRange(3);

    fun includesMelee() = (this != Range)
    fun includesRange() = (this != Melee)

    companion object {
        fun getByType(type: Int): AttackType {
            return entries.firstOrNull { it.type == type } ?: throw IllegalArgumentException("invalid attack type: $type")
        }
    }
}