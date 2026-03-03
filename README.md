# dnd-dpr

DND Damage Per Round Calculator 

This tool calculates the average Damage Per Round for Spell and Weapon Attacks.  This program currently runs from a linux/mac commandline.  For a user-friendly web interface, consider using [DPR Calc](https://dprcalc.com/), which provides great support for Weapon DPR (but does not yet support spells)

This tool reads spell and monster data originally from [Nick Aschenbach](https://github.com/nick-aschenbach/dnd-data/tree/main/data).  
```
Spells:
 319 "Free Basic Rules (2014)"
 333 "Free Basic Rules (2024)"

Monsters:
 324 "Free Basic Rules (2014)"
  55 "Free Basic Rules (2024)"
```

Nick has spell data from many books - including Free Rules for 2014 and 2024 - with caveats:
- the 2024 data includes spell save info - what monster ability is targeted, and what happens on success (half/no damage)
- the 2014 data does NOT include spell save info

To fill in the blanks in the 2014 spells, we also pull from [5e-bits](https://github.com/5e-bits/5e-database/blob/main/src/2014/5e-SRD-Spells.json) .  This data is in a different format.  The [bin](https://github.com/dolafson/dnd-dpr/tree/main/bin) directory contains a set of python scripts to transform the data, and clean up fields as needed

Spell attack hit (and damage) probabilities are calculated using formulas borrowed from Ludic
- [Ludic Documentation](https://docs.google.com/document/d/11eTMZPPxWXHY0rQEhK1msO-40BcCGrzArSl4GX4CiJE/edit?tab=t.0#heading=h.llxekwsqql6y)
- [Ludic Spreadsheet](https://docs.google.com/spreadsheets/d/14WlZE_UKwn3Vhv4i8ewVOc-f2-A7tMW_VRum_p3YNHQ/edit?gid=151780215#gid=151780215)    
  
<br>
  
## Build
gradle build 

## Run
java -jar ./app/build/libs/app-standalone.jar 

## Usage
```
Usage:  [-d] [--csv]  [file.json ...]  [character]  < search<opt> | dump[:opt] | <attacks> | turns >

Options:

    -d      debug
    --csv   CSV output

File:

     file.json   load spell or monster data; this is optional: program contains most 2014 and 2024 data

Character:

     NumericID   read character from DND Beyond API (character must have public visibility)
     file.json   read character from a local file

Search:

     search:NAME     search for NAME in list of spells/monsters, and display details if found

Dump:

     dump:spells     export all known spells
     dump:monsters   export all known monsters
     dump:attacks    export attacks from user input
     dump:character  export (minimal) character data from DND Beyond
     dump            export all of the above

Attacks:

     -a  <monster spellOrWeapon> ...    (multiple pairs allowed)

Turns:

     an array of turns, each with an array of attacks, for example:

         [ { "attacks": [ { "monster": "Goblin", "attack": "Longbow" } ] } ]


     optional notes and preconditions are also supported, for example:

    [
      {
        "notes": [
            "Assume Mind Sliver and HM were cast prior to this turn (following 2024 rules):",
            "- MS adds a 1d4 penalty to the target's next saving throw",
            "- HM adds 1d6 to each subsequent attack roll for 1 hour (including melee/range attacks)"
        ],
        "preconditions": {
            "bonusDiceToSave":   { "d4": 0, "d6": 0, "d8": 0, "d10": 0, "d12": 0 },
            "penaltyDiceToSave": { "d4": 1, "d6": 0, "d8": 0, "d10": 0, "d12": 0 },
            "bonusDamageDice":   { "d4": 0, "d6": 1, "d8": 0, "d10": 0, "d12": 0 },
            "bonusDamage": 0
        },
    
        "attacks": [
            { "monster": "Goblin", "attack": "Longbow" },
            { "monster": "Goblin", "attack": "Hail of Thorns", "isBonusAction": true, "numTargets": 3 }
        ]    
      } 
    ]            
```

<br>

## Output Format (TXT)

While performing the Attack DPR calculation, several stats are calculated and displayed.  The output for the above example (with preconditions) looks like this: 

```
#######################################################

	level                2
	characterName        Leif Lightfoot
	spellBonusToHit      4
	spellSaveDC          12
	monsterName          Goblin
	monsterAC            15
	scenario             goblin.longbow.hail.of.thorns.with.MS.and.HM

#######################################################

	turn                 1
	action               1
	effect               1
	attack               Longbow
	weaponDamage         1d8
	weaponDamageBonus    4
	weaponAttackBonus    8
	numTargets           1
	chanceToHit          0.70
	damagePerHit         12.00
	duration             1.00
	damageFullEffect     8.80

	turn                 1
	action               BA
	effect               1
	attack               Hail of Thorns
	spellSaveAbility     Dexterity
	targetSaveBonus      2
	numTargets           3
	chanceToHit          0.57
	damagePerHit         4.22
	duration             0.00
	damageFullEffect     12.67

	TURN TOTAL           21.47

	SCENARIO TOTAL       21.47

```

In this sample output, the key things to note are
- this [character](example/character.json) has a high proficiency with the [Longbow](https://www.dndbeyond.com/equipment/37-longbow)
  - with an attack bonus of 8, the chance to hit is 70%
  - on a successful hit, the average damage is 12 
    - 1d8 (weapon) + 1d6 ([Hunter's Mark](https://www.dndbeyond.com/spells/2619166-hunters-mark)) + 4 (proficiency)
  - the average damage for this action is 8.8 (%hit * DPH)
- the [Hail of Thorns](https://www.dndbeyond.com/spells/2618975-hail-of-thorns) (bonus attack spell) triggers a Dexterity saving throw
  - the [Goblin](https://www.dndbeyond.com/monsters/16907-goblin) has above average Dexterity, so their save bonus is 2
  - the spell caster has a save DC of 12
- as a result ...
  - the Goblin would need to roll a 10 or higher to avoid the spell effect
  - but in this case, [Mind Sliver](https://www.dndbeyond.com/spells/2619037-mind-sliver) causes the Goblin to add a 1d4 penalty
  - MindSliver improves the "chance to hit" from 45% to 57%
- the number of spell effects/targets is 3 (assume 2 friends within 5 feet)
  - Note 1: this number should be configurable
  - Note 2: Mind Sliver should only apply to one of those 3 targets
- spell damage per hit is 4.22
  - 1d10 on a failed save, or half that on a successful save 
  - Note: Hunter's Mark damage does not apply here 
  - (Hail of Thorns has a **save** roll, not an **attack** roll)
- spell full effect damage is 12.67 (across 3 targets)
- total DPR is 21.47 (longbow + spell)
- sample output in [TXT](example/attackResult.txt) and [CSV](example/attackResult.csv)

  
## Future Improvements

In no particular order ...

- add unit tests
- add optional support for constraint checks
  - your character doesn't know that spell, can't cast it 100 times, etc 
- add support for "show me all the attack combinations i can make at this range"
- add support for weapon effects, similar to spell effects (eg [Vex](https://www.dndbeyond.com/sources/dnd/phb-2024/equipment#Vex) -> advantage on next attack)
- when cascading spell effects, most effects only apply to a single target
  - take this into account when computing DPR for multi-target spell save (like Hail of Thorns, Ice Knife, ...)
- add better support for levelling up / what-if scenarios
  - for now this is done by hand-editing a character file and re-runnimg the DPR calculation
- add a web interface
  - i'll probably never use it, but someone else might
- add simulated battles
  - give the monster(s) a chance to fight back
  - calculate probability of character death
