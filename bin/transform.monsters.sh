#!/bin/bash

# git clone git@github.com:5e-bits/5e-database.git
# cd 5e-database

cat ~/git/dnd/5e-database/src/2014/en/5e-SRD-Monsters.json |
    sed -e 's+"Number of Heads"+1000000+g' \
        -e 's+"count": "1d4"+"count": 4+g'

