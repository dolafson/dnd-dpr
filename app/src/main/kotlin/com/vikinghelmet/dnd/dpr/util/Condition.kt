package com.vikinghelmet.dnd.dpr.util

enum class Condition {
// https://www.dndbeyond.com/sources/dnd/br-2024/rules-glossary#Condition

    Blinded,        // Attack rolls against you have Advantage, and your attack rolls have Disadvantage.
    Charmed,        // [ignore]
    Deafened,       // You can’t hear and automatically fail any ability check that requires hearing
    Exhaustion,     // D20 rolls are reduced by 2 times your Exhaustion level; speed reduced by 5x that level
    Frightened,     // you have Disadvantage on ability checks and attack rolls while the source of fear is within line of sight.
    Grappled,       // speed=0; You have Disadvantage on attack rolls against any target other than the grappler.
    Incapacitated,  // you can’t take any action, Bonus Action, or Reaction; concentration is broken; can't speak; disadvantage on initiative
    Invisible,      // adv on initiative; Attack rolls against you have Disadvantage, and your attack rolls have Advantage
    Paralyzed,      // Incapacitated++ ; speed=0; auto-fail STR and DEX saving throws; Attack rolls against you have Advantage; auto-crit (5 ft)
    Petrified,      // Incapacitated++ ; speed=0; auto-fail STR and DEX saving throws; Attack rolls against you have Advantage; resist damage; immune to poison
    Poisoned,       // You have Disadvantage on attack rolls and ability checks.
    Prone,          // half-speed to end; You have Disadvantage on attacks. Attacks against you: Advantage w/in 5 feet, else Disadvantage.
    Restrained,     // speed=0; Attack rolls against you have Advantage, and your attack rolls have Disadvantage. You have Disadvantage on Dexterity saving throws
    Stunned,        // Incapacitated++ ; auto-fail STR and DEX saving throws; Attack rolls against you have Advantage
    Unconscious,    // Incapacitated+Prone++ ; speed=0; Attack rolls against you have Advantage; auto-fail STR and DEX saving throws;  auto-crit (5 ft)
}