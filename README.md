# dnd-dpr

DND Damage Per Round Calculator 

This tool calculates the average Damage Per Round for Spell Attacks.  Support for Weapon Attacks may be added in a future release.  Until then, consider using [DPR Calc](https://dprcalc.com/), which provides an excellent interface for Weapon DPR (but does not yet support spells)

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
Usage:  [file.json ...]  [character]  < dump[:opt] | search<opt> | attacks.json >

File options:

	 file.json 	 load spell or monster data from the given file; this is optional: program is prepackaged with 2024 data

Character options:

	 NumericID 	 read character from DND Beyond API (character must have public visibility)
	 file.json 	 read character from a file (useful for what-if analysis)

Dump (debug) options:

	 dump:spells     export all known spells
	 dump:monsters   export all known monsters
	 dump:attacks    export attacks from user input
	 dump:character  export (minimal) character data from DND Beyond
	 dump            export all of the above

Search commands:

	 search:spells:NAME 	 search for name in list of spells, and display details if found
	 search:monsters:NAME 	 search for name in list of monsters, and display details if found

Attacks:

	 json file should contain an array of monster/spell pairs, for example:

		 [{"monster":"Goblin","spell":"Ensnaring Strike"}]

	 
Note: for more complex scenarios, the attack json also supports preconditions.  For example:
            
    [
        {
            "monster": "Goblin",
            "spell": "Ensnaring Strike",
            "preconditions": {
                "bonusDiceToSave":       { "d4": 1, "d6": 0, "d8": 0, "d10": 0, "d12": 0 },
                "penaltyDiceToSave":     { "d4": 0, "d6": 0, "d8": 0, "d10": 0, "d12": 0 },
                "bonusDamageOnFirstHit": { "d4": 0, "d6": 0, "d8": 0, "d10": 0, "d12": 0 },
                "bonusDamage": 0
            }
        }
    ]            
```

<br>

## Output Format

While performing the Attack DPR calculation, several stats are calculated and displayed.  This output format is subject to change.  It currently looks like this: 

```
spell duration:      10
spell damage:        DiceBlock(d4=0, d6=1, d8=0, d10=0, d12=0)
spell save result:   [SPELL_ENDS]
num effects/targets: 1

spell save ability      = Strength
target save proficiency = -1
spell caster save DC    = 12

Chance to Hit, (avg, min, max) = (0.6, 0.36, 0.84)
Full Damage, (avg, min, max) = (3.5, 1.0, 6.0)
Half Damage, (avg, min, max) = (1.5, 0.0, 3.0)

Full Damage (First Hit), (avg, min, max) = (0.0, 0.0, 0.0)
Half Damage (First Hit), (avg, min, max) = (0.0, 0.0, 0.0)
Chance of at least one hit, (avg, min, max) = (0.6, 0.36, 0.84)

Damage Per Target, (avg, min, max) = (2.7, 2.22, 3.1799998)
Damage Per Failed Save, (avg, min, max) = (3.5, 1.0, 6.0)
Damage Per Successful Save, (avg, min, max) = (0.0, 0.0, 0.0)
Damage Per Hit, (avg, min, max) = (2.1000001, 1.26, 2.9399998)

Average Damage Per Round, (avg, min, max) = (2.1000001, 1.26, 2.9399998)
Average Duration (In Rounds), (avg, min, max) = (1.4909303, 0.56247944, 4.331768)
Average Total Damage Over Time, (avg, min, max) = (5.218256, 1.968678, 15.161188)
```

In this sample output, the key things to note are
- the spell has a max duration of 1 minute (10 rounds)
- each round it may trigger 1d6 damage
- the number of effects/targets is 1 (this is a default)
  - Note: some spells may impact more than 1 target; this needs to be made configurable
- on a succesful hit, the spell requires the target to perform a Strength saving throw
  - the monster (Goblin) has a save proficiency of -1 (they need to work out more)
  - the spellcaster has a save DC of 12
- as a result ...
  - the monster needs to roll a 13 or higher to avoid the spell effect
  - the "chance to hit" is 60%
- average duration is 1.49 rounds
- average total damage (until spell ends) is 5.21

  
## Future Improvements

In no particular order ...

- add unit tests
- add support for cascading conditions between attacks, for example:
  - cast Mind Sliver, which on a hit reduces the target's next save roll by 1d4
  - then cast a different spell which requires a save roll, including the above penalty
- add optional support for constraint checks
  - your character doesn't know that spell, can't cast it 100 times, etc 
- add support for weapon attacks
- add better support for levelling up / what-if scenarios
  - for now this is done by hand-editing a character file and re-runnimg the DPR calculation
- add a web interface
  - i'll probably never use it, but someone else might
- add simulated battles
  - give the monster(s) a chance to fight back
  - calculate probability of character death
