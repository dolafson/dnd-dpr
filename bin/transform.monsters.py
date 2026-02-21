#!/opt/homebrew/bin/python3
#
# data source
#   https://github.com/nick-aschenbach/dnd-data/blob/main/data/monsters.json
#
# In the input data set, some values - like 'AC' - will at times appear 
# as a String, and other times as an Int.  If we get an Int, replace it 
# with a String - to simplify parsing in strongly-typed languages
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
        # perform transform on problematic keys as needed
        for key in ['AC', 'HP', 'Challenge Rating', 'data-CHA-mod', 'data-CON-mod', 'data-DEX-mod', 'data-INT-mod', 'data-STR-mod', 'data-WIS-mod', 'data-XP']:
            if key in props:
                val = monster['properties'][key]
                if not isinstance(val, str):
                    monster['properties'][key] = str(val)

        # output the transformed data
        print(json.dumps(monster))

