# TODO

## Monster damage types that crash `DamageType.valueOf`

72 resistance/immunity/vulnerability entries (across 70 monsters in `monsters.json`) are not plain
damage type names, e.g.:

- `"bludgeoning, piercing, and slashing from nonmagical weapons"` (Air Elemental, Balor, ...)
- `"bludgeoning, piercing, and slashing from nonmagical weapons that aren't silvered"` (Barbed Devil)
- `"damage from spells"` (Archmage)

`Monster.getDamageImmunities/Resistances/Vulnerabilities()` (`dprlib/.../monsters/Monster.kt`) call
`DamageType.valueOf(it)` on each entry, which throws `IllegalArgumentException` the first time one of
these monsters takes damage.

Fix: parse the plain damage types out of these strings, and record the qualifier
(nonmagical / silvered / from spells) for later use.

## Condition immunities are ignored

`Monster.condition_immunities` is loaded but never read. A failed save still applies the condition
(e.g. Command -> prone, Cause Fear -> frightened) to a monster that is immune to it.
Flameskull is immune to charmed, frightened, paralyzed, poisoned and prone.

Fix: check the target's condition immunities before adding a `TargetEffect` with conditions.

## Magic Resistance is not used in combat

Only the one-sided DPR calculator accounts for it (`ActionCalculator.kt`, as the advantage/min case).
In combat, `SavingThrowGenerator.makeSavingThrow` asks `getAdvantageOnSave()`, which never checks
the "Magic Resistance" special ability, so monsters like Flameskull save against spells
without advantage.

Fix: in `getAdvantageOnSave`, give advantage when the save is against a spell and the monster has
Magic Resistance.

## Minor

- Turn option filtering (`CombatantWithStatus.kt`, "exclude spells with damageType that target is
  immune to") only checks the first damage type of a spell, and doesn't de-prioritize spells
  the target resists.
- Monster saving throws use only the ability modifier; saving throw proficiencies from
  `proficiencies` are not added.
