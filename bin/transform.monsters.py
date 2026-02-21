#!/opt/homebrew/bin/python3
#
# data source
#   https://github.com/nick-aschenbach/dnd-data/blob/main/data/monsters.json
#
# In the input data set, two issues:
#
# 1) some values - like 'AC' - will at times appear as a String, and other 
#    times as an Int.  If we get an Int, replace it with a String
#
# 2) some values - like 'data-Actions' will appear as "stringified" json - 
#    an object flatted into a string.  Replace these with a parsed object 
#

import sys, re, json

while True:
    line = sys.stdin.readline()
    if len(line) <= 0:
        break

    monster = json.loads(line)
    props = monster['properties']

    # monster format varies by book; for now we only care about "free basic"

    if monster['book'].startswith('Free Basic Rules'):
        # perform int-to-string transform as needed
        for key in ['AC', 'HP', 'Challenge Rating', 'data-CHA-mod', 'data-CON-mod', 'data-DEX-mod', 'data-INT-mod', 'data-STR-mod', 'data-WIS-mod', 'data-XP']:
            if key in props:
                val = monster['properties'][key]
                if not isinstance(val, str):
                    monster['properties'][key] = str(val)

        # perform string-to-object transform as needed
        for key in ['data-Traits', 'data-Actions', 'data-Legendary Actions']:
            if key in props:
                oldPayload = props[key]
                props[key] = json.loads(oldPayload)

        # output the transformed data
        print(json.dumps(monster))

