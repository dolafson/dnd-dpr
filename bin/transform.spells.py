#!/opt/homebrew/bin/python3
#
# data source
#   https://github.com/nick-aschenbach/dnd-data/blob/main/data/spells.json
#
# In the input data set, some values like 'data-datarecords' will appear
# as "stringified" json - an object flatted into a string.  Replace these
# with a parsed object to simplify parsing in strongly-typed languages
#

import sys, re, json

while True:
    line = sys.stdin.readline()
    if len(line) <= 0:
        break

    # spell "filter-Level" is usually an Int, except for "Cantrip"; make that a zero
    if '"Cantrip",' in line:
        line = line.replace('"Cantrip",','0,')

    spell = json.loads(line)
    props = spell['properties']

    # spell format varies by book; for now we only care about "free basic"
    if not spell['book'].startswith('Free Basic Rules'):
        continue

    # perform transform on data records
    drKey='data-datarecords'

    if drKey not in props:
        continue

    oldDataRecordString = spell['properties'][drKey]
    if not isinstance(oldDataRecordString, str):
        continue

    newDataRecordArray = json.loads(oldDataRecordString)
    spell['properties'][drKey] = newDataRecordArray

    for dr in newDataRecordArray:
        if not 'payload' in dr:
            continue

        oldPayload = dr['payload']
        newPayload = json.loads(oldPayload)
        dr['payload'] = newPayload

        if newPayload['type'] == "Upcasting":
            for key in ['level', 'value']:
                if key in newPayload:
                    val = newPayload[key]
                    if not isinstance(val, str):
                        newPayload[key] = str(val)

    # output the transformed data
    print(json.dumps(spell))

