# dnd-dpr

DND Damage Per Round Calculator - Spell Attacks

This tool reads spell and monster data originally from https://github.com/nick-aschenbach/dnd-data/tree/main/data

The bin directory includes a few python scripts to transform the origin data into a more readable format 

Spell attack hit (and damage) probabilities are calculated using formulas borrowed from Ludic
- [Ludic Documentation](https://docs.google.com/document/d/11eTMZPPxWXHY0rQEhK1msO-40BcCGrzArSl4GX4CiJE/edit?tab=t.0#heading=h.llxekwsqql6y)
- [Ludic Spreadsheet](https://docs.google.com/spreadsheets/d/14WlZE_UKwn3Vhv4i8ewVOc-f2-A7tMW_VRum_p3YNHQ/edit?gid=151780215#gid=151780215)


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
	 search:monsters:NAME 	 search for name in list of spells, and display details if found

Attacks:

	 json file should contain an array of monster/spell pairs, for example:

		 [{"monster":"Goblin","spell":"Ensnaring Strike"}]
```

## Output Format

While performing the Attack DPR calculation, several stats are calculated and displayed.  This output format is subject to change.  It currently looks like this: 

```
spell duration:      10
spell damage:        DiceBlock(four=0, six=1, eight=0, ten=0, twelve=0)
spell save result:   [SPELL_ENDS]
num effects/targets: 1

save ability = Strength
save proficiency = -1

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


