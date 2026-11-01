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

## Conditions from spells without a saving throw are never applied

Spell conditions are only applied in `AttackAction.castSavingThrowSpell`, when a target fails its save.
Spell attacks go through `meleeOrRangeAttack`, which applies damage but never adds a `TargetEffect`.

Spells in `shared/resources/spells.json` that impose an ongoing condition on an enemy with no save
to avoid it:

| Spell | Version | How it lands | Condition |
|---|---|---|---|
| Ray of Sickness | 2024 | ranged spell attack hits | Poisoned until end of your next turn |
| Conjure Fey | 2024 | melee spell attack hits | Frightened until start of your next turn |
| Color Spray | 2014 | HP pool (6d10), lowest HP first | Blinded for 1 round |
| Sleep | 2014 | HP pool (5d8), lowest HP first | Unconscious (ends on damage / shaken awake) |
| Power Word Stun | 2014 | target has 150 HP or fewer | Stunned (Con save at end of each turn to end it) |
| Telekinesis | 2014 | contested spellcasting check vs Strength | Restrained until end of your next turn |
| Otiluke's Freezing Sphere | 2024 | creatures swimming on the frozen water | Restrained (niche) |

Irresistible Dance (2014) is similar: no initial save, but its effects (disadvantage on Dex saves and
attacks, advantage against the target) are not a named condition.

Fix: apply `TargetEffect` conditions on a spell attack hit; add special handling for the
HP-pool / HP-threshold / contested-check spells.

## Spell data missing saving throws

These spells require a save by the rules, but their data has no `save` in any `Attack` record,
so their conditions are never applied:

- No `Attack` records at all (save only in `properties.Save`): Blindness/Deafness (2014),
  Antipathy/Sympathy (2014)
- `Attack` record without a `save`: Earthquake, Sleet Storm, Web, Prismatic Wall, Symbol,
  Holy Aura (all 2014)

Fix: fill in the save (e.g. in `extra.spells.json`), or fall back to `properties.Save`.

## 2014 condition names not detected in spell descriptions

`Spell.getSpellFailConditions()` matches capitalized condition names (`Blinded`, `Restrained`, ...)
in the description, but 2014 descriptions use lowercase ("blinded", "restrained"). For 2014 spells,
only conditions named in the save's `onFail` text are detected.

Fix: match case-insensitively on whole words, being careful with false positives
(e.g. "invisible", "can't be charmed").

## Effects on a successful save are ignored

`castSavingThrowSpell` uses a successful save only to reduce damage; a `TargetEffect` is added only
on a failed save. No spell imposes a named condition on a successful save, but these 2024 spells
still have a condition-like effect:

| Spell | Version | Failed save | Successful save |
|---|---|---|---|
| Flesh to Stone | 2024 | Restrained (Petrified after 3 failures) | Speed 0 until start of your next turn |
| Otto's Irresistible Dance | 2024 | Charmed, dancing, disadvantage, advantage against it | dances until end of its next turn (all movement spent dancing) |
| Ray of Enfeeblement | 2024 | Disadvantage on Strength-based d20 tests | Disadvantage on its next attack roll before start of your next turn |

## Spells modelled as saves where the condition applies regardless

The save in the data only ends or tracks the condition, so in the simulation a successful save
wrongly prevents it:

- Power Word Stun (2024): a target with 150 HP or fewer is Stunned with no save; the Con save at the
  end of each of its turns only ends the stun.
- Contagion (2014): a melee spell attack hit applies the disease; the later Con saves count toward
  3 successes / 3 failures (data: Con save with `onSucceed: "other"`).

Related (not checked): repeated saves ("three successes / three failures" for Flesh to Stone and
Contagion, "repeat the save at the end of each turn") are probably not modelled beyond save-to-end.

## Minor

- Turn option filtering (`CombatantWithStatus.kt`, "exclude spells with damageType that target is
  immune to") only checks the first damage type of a spell, and doesn't de-prioritize spells
  the target resists.
- Monster saving throws use only the ability modifier; saving throw proficiencies from
  `proficiencies` are not added.
