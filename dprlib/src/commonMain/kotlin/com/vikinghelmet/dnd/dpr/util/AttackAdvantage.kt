package com.vikinghelmet.dnd.dpr.util

enum class AttackAdvantage {
    normal, advantage, disadvantage;

    companion object {
        fun fromList(list: List<AttackAdvantage>): AttackAdvantage {
            return if (list.isEmpty() ||
                (list.contains(advantage) && list.contains(disadvantage)))  normal
            else if (list.contains(advantage))  advantage
            else if (list.contains(disadvantage))  disadvantage
            else normal
        }
    }
}