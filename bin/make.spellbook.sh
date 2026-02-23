#!/bin/bash
cat ~/dnd/git/5e-database/src/2014/5e-SRD-Spells.json \
    ~/dnd/git/dnd-data/data/spells.json | 
    jq -c .[] | 
    sed -e 's+"Healing":70,+"Healing":"70",+g' |
    sed -e 's+"Higher Spell Slot Die":10,+"Higher Spell Slot Die":"10",+g' |
    bin/transform.spells.py  | 
    jq -s | 
    jq . > app/src/main/resources/spells.json

