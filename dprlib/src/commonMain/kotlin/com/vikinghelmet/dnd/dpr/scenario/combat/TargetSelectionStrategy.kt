package com.vikinghelmet.dnd.dpr.scenario.combat

enum class TargetSelectionStrategy {
    targetAttackingFriendWhoIsAlmostDead,
    targetWithHighDamageToAttacker,
    targetWithHighDamageToTeam,
    biggestTarget,
    easiestTarget,
    noTarget,
    closestTarget,
    ;
}