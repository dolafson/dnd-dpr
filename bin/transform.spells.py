#!/opt/homebrew/bin/python3
#
# data sources
#   1. https://github.com/nick-aschenbach/dnd-data/blob/main/data/spells.json
#   2. https://github.com/5e-bits/5e-database/blob/main/src/2014/5e-SRD-Spells.json
#
# src 1 has spell data from many books, including Free Rules for 2014 and 2024
#   - the 2024 data allows multiple attacks per spell, with potentially multiple saves
#   - the 2014 data has no attack/save info
#
# src 2 only has spell data for 2014
#   - for spells with a single attack/save, structured damage/save info is included
#   - for spells with multiple attacks (eg Earthquake), damage/save info is NOT included
#
# Solution
#   - cat src2 src1 | thisScript
#   - when reading src2, extract save info, and store in a map
#   - when reading src1, consult the map as needed
#
# In addition, we need to reformat 2024 records in src1:
#
#     In the input data set, some values like 'data-datarecords' will appear
#     as "stringified" json - an object flatted into a string.  Replace these
#     with a parsed object to simplify parsing in strongly-typed languages
#

import sys, re, json

saveMap = {}

#########################################################

def expandAbility(ability):
    match ability:
        case 'str':
            return 'Strength'
        case 'dex':
            return 'Dexterity'
        case 'con':
            return 'Constitution'
        case 'int':
            return 'Intelligence'
        case 'wis':
            return 'Wisdom'
        case 'cha':
            return 'Charisma'
        case _:
            return 'Unknown'

def translate(line):
    srdSpell = json.loads(line)
    name = srdSpell['name']
    save2  = None
    aoe2 = None
    if 'dc' in srdSpell:
        dc = srdSpell['dc']
        save2 = {
            "saveAbility": expandAbility(dc['dc_type']['index'])
        }
        if 'dc_success' in dc:
            saveResult = dc['dc_success']
            match saveResult:
                case 'half': 
                    saveResult='half damage'
                case 'none': 
                    saveResult='unaffected'

            save2['onSucceed'] = saveResult

        # note: src data does not have 'dc_fail' ...

    if 'area_of_effect' in srdSpell:
        aoe = srdSpell['area_of_effect']
        shape = aoe['type'].capitalize()
        size  = str(aoe['size'])
        aoe2 = { 'shape': shape, 'size':  size }

    if (save2 != None or aoe2 != None):
        payload = { 'type': 'Attack', 'name': name } 

        if save2 != None:
            payload['save'] = save2

        if aoe2 != None:
            payload['aoe'] = aoe2

        saveMap[name] = [ { 'name': name+' Attack', 'payload': payload } ]
        # print(f'name = {name} , DR = {saveMap[name]}')


#########################################################

while True:
    line = sys.stdin.readline()
    if len(line) <= 0:
        break

    # check for the 5e-SRD format
    if "casting_time" in line:
        translate(line)
        continue

    # spell "filter-Level" is usually an Int, except for "Cantrip"; make that a zero
    if '"Cantrip",' in line:
        line = line.replace('"Cantrip",','0,')

    spell = json.loads(line)
    name  = spell['name']
    props = spell['properties']

    # spell format varies by book; for now we only care about "free basic"
    if not spell['book'].startswith('Free Basic Rules'):
        continue

    drKey='data-datarecords'

    if drKey not in props:
        # 2014 spell: check for save data
        if name in saveMap:
            props['data-datarecords'] = saveMap[name]
        print(json.dumps(spell))
        continue

    
    # 2024 spell: DR is a string of embedded json; expand it

    oldDataRecordString = props[drKey]
    if not isinstance(oldDataRecordString, str):
        continue

    newDataRecordArray = json.loads(oldDataRecordString)
    props[drKey] = newDataRecordArray

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

