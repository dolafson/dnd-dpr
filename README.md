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
- First, build the KMP library
```
(cd ..; git clone git@github.com:dolafson/dnd-dprlib.git ; cd dnd-dprlib; ./build.sh )
```
- Once the library is ready, you can now build the app
```
gradle build 
```

## Run
java -jar ./dprcmd/build/libs/dprcmd-standalone.jar 

## Usage
```
Usage:  [-d] [--csv] [+aaa=N]  [file.json ...]  [character]  < dump[:opt] | search<opt> | <attacks> >

Options:

    -d              debug logging
    --csv           CSV output
    
    --maxTurns=N    number of turns per scenario (default = 5)
    --maxResults=N  number of results in final output (default = 30), sorted by totalDamage (descending)
    
    +feat=name      add feat
    +aaa=N          increase ability (3-letter shorthand = str, dex, ...) by N = [1-9]

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
     dump:features   export supported features: racialTraits, actionModifiers, feats
     dump            export all of the above

Search:

     search:NAME     search for NAME in list of spells/monsters, and display details if found

Attacks:

     -a  <monster turn[;turn...] >          one/more turns, each a comma-separated list of spell or weapon name
     -z  <monster <"melee" or "range">>     run all possible 5-turn scenarios, then sort by total damage

```

<br>

## Output Format (TXT)

While performing the Attack DPR calculation, several stats are calculated and displayed.  

To demonstrate, run the following command

`java -jar ./dprcmd/build/libs/dprcmd-standalone.jar ./example/character.json  -a Goblin "Mind Sliver;Longbow,Hail of Thorns"
`

Output for this scenario can be found here in [TXT](example/attackResult/MindSliver.then.HailOfThorns.txt) and [CSV](example/attackResult/MindSliver.then.HailOfThorns.csv).  Key things to note in the output are:
- The first attack is [Mind Sliver](https://www.dndbeyond.com/spells/2619037-mind-sliver)
  - this has a small initial impact, due to a low chanceToHit and damagePerHit (1d6)
  - since Mind Sliver is a cantrip, no bonus actions occur in this first turn
  - however, the spell does produce a savePenalty that is carried forward to the end of the next round
  - DPR for this first round is only 1.93
- The second turn starts with [Longbow](https://www.dndbeyond.com/equipment/37-longbow) 
  - this [character](example/character.json) has a high proficiency with Longbow
  - with an attack bonus of 8, the chance to hit is 70%
  - on a successful hit, the average damage is 8.5 
    - this includes the average of a 1d8 (= 4.5) plus a proficiency bonus (4)
  - the average damage for this action is 6.17 (%hit * DPH)
- The second turn includes a bonus action: [Hail of Thorns](https://www.dndbeyond.com/spells/2618975-hail-of-thorns) 
  - This spell triggers a Dexterity saving throw
  - the [Goblin](https://www.dndbeyond.com/monsters/16907-goblin) has above average Dexterity, so their save bonus is 2
  - the spell caster has a save DC of 12
- As a result ...
  - the Goblin would need to roll a 10 or higher to avoid the spell effect
  - but in this case, Mind Sliver causes the Goblin to add a 1d4 penalty
  - MindSliver improves the "chance to hit" from 45% to 57%
  - the additional damage is 4.22 
- Hail of Thorns may also impact other targets within 5 feet of the first
  - Note: the number of additional targets should be configurable (for now = 2)
  - Mind Sliver does not apply to these targets; "chance to hit" is 45%
  - the average damage for these targets is 3.85 each
- DPR for the second round is 18.10
- total damage for the entire scenario is 20.02

While [Mind Sliver](https://www.dndbeyond.com/spells/2619037-mind-sliver) is 
an interesting spell mechanic, it is overall a poor choice for this character. A 
better choice would have been to start with Longbow and a bonus action of 
[Hunter's Mark](https://www.dndbeyond.com/spells/2619166-hunters-mark).  That 
 ends with total damage = 26.52, which is 6.5 higher than the first scenario 
(on average).  The full results for this second scenario can be found here 
in [TXT](example/attackResult/HuntersMark.then.HailOfThorns.txt) 
and [CSV](example/attackResult/HuntersMark.then.HailOfThorns.csv)

  
## Future Improvements

In no particular order ...

- add unit tests
- add support for damage types, and Resistance / Immunity
- add support for more class features / feats
- add support for weapon effects, similar to spell effects
  - (eg Vex [weapon mastery](http://dnd2024.wikidot.com/equipment:weapon) -> advantage on next attack)
- add support for spell damage upcasting (by character level, or spell level) 
- add a web interface
  - i'll probably never use it, but someone else might
- add simulated battles
  - give the monster(s) a chance to fight back
  - calculate probability of character death
