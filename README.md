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
Usage:  [-d] [--csv]  [file.json ...]  [character]  < dump[:opt] | search<opt> | <attacks> | turns >

Options:

    -d      debug
    --csv   CSV output

File:

     file.json   load spell or monster data; this is optional: program contains most 2014 and 2024 data

Character:

     NumericID   read character from DND Beyond API (character must have public visibility)
     file.json   read character from a local file

Dump:

     dump:spells     export all known spells
     dump:monsters   export all known monsters
     dump:attacks    export attacks from user input
     dump:character  export (minimal) character data from DND Beyond
     dump            export all of the above

Search:

     search:spells:NAME      search for name in list of spells, and display details if found
     search:monsters:NAME    search for name in list of spells, and display details if found

Attacks:

     -a  <monster spellOrWeapon> ...    (multiple pairs allowed)

Turns:

     an array of turns, each with an array of attacks, for example:

         [ { "attacks": [ { "monster": "Goblin", "attack": "Longbow" } ] } ]


     optional notes and preconditions are also supported, for example:

    [
      {
        "notes": [
            "Turn 1: Assume Mind Sliver was cast prior to this turn",
            "When MS is successful, in addition to 1d6 damage, it imposes a 1d4 penalty on the target's next saving throw"
        ],
        "preconditions": {
            "bonusDiceToSave":       { "d4": 0, "d6": 0, "d8": 0, "d10": 0, "d12": 0 },
            "penaltyDiceToSave":     { "d4": 1, "d6": 0, "d8": 0, "d10": 0, "d12": 0 },
            "bonusDamageOnFirstHit": { "d4": 0, "d6": 0, "d8": 0, "d10": 0, "d12": 0 },
            "bonusDamage": 0
        },
    
        "attacks": [
            { "monster": "Goblin", "attack": "Longbow" },
            { "monster": "Goblin", "attack": "Ensnaring Strike", "isBonusAction": true }
        ]    
      } 
    ]
```

<br>

## Output Format (TXT)

While performing the Attack DPR calculation, several stats are calculated and displayed.  This output format is subject to change.  It currently looks like this: 

```
#######################################################

	level                2
	characterName        Leif Lightfoot
	spellBonusToHit      4
	spellSaveDC          12
	monsterName          Goblin
	monsterAC            15
	scenario             goblin.longbow.hail.of.thorns

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
	damagePerHit         8.50
	duration             1.00
	damageFullEffect     6.17

	turn                 1
	action               BA
	effect               1
	attack               Hail of Thorns
	spellSaveAbility     Dexterity
	targetSaveBonus      2
	numTargets           3
	chanceToHit          0.45
	damagePerHit         3.85
	duration             0.00
	damageFullEffect     11.55

	TURN TOTAL           17.72

	SCENARIO TOTAL       17.72

```

In this sample output, the key things to note are
- the character has a high proficiency with the longbow
  - with an attack bonus of 8, the chance to hit is 70%
  - on a successful hit, the damage is 8.5 (1d8 + 4)
  - the average damage for this attack is 6.17 (%hit * DPH)
- the (bonus attack) spell requires the target to perform a Dexterity saving throw
  - Goblins have above average Dexterity, so for this spell the target save proficiency is 2
  - the spellcaster has a save DC of 12
- as a result ...
  - the monster needs to roll a 10 or higher to avoid the spell effect
  - the "chance to hit" is 45%
- the number of spell effects/targets is 3 (the Goblin has 2 friends within 5 feet)
  - Note: this number should be configurable
- spell damage per target is 3.85 (%hit * (1d10 per hit))
- full effect damage is 11.55 (across 3 targets)
- total DPR is 17.72 (longbow + spell)

  
## Future Improvements

In no particular order ...

- add unit tests
- add support for cascading conditions between attacks, for example:
  - cast Mind Sliver, which on a hit reduces the target's next save roll by 1d4
  - then cast a different spell which requires a save roll, including the above penalty
- add optional support for constraint checks
  - your character doesn't know that spell, can't cast it 100 times, etc 
- add better support for levelling up / what-if scenarios
  - for now this is done by hand-editing a character file and re-runnimg the DPR calculation
- add a web interface
  - i'll probably never use it, but someone else might
- add simulated battles
  - give the monster(s) a chance to fight back
  - calculate probability of character death
