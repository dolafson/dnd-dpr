#!/opt/homebrew/bin/python3
#
# this translates spells from character-service format to NickA format

import sys, re, json

while True:
    line = sys.stdin.readline()
    if len(line) <= 0:
        break

    inputSpell = json.loads(line)

    if inputSpell == None:
        continue

    definition = inputSpell['definition']

    name       = definition['name']
    modifiers  = definition['modifiers']

    # TODO: 'NoIdea' placeholder for activationType 2 ... 
    # is this longer than 1 action ? a function of duration (below) ?
    castingTimeArray = ['Action','NoIdea','BonusAction']
    castingTimeDescription = ""
    if 'activation' in definition:
        actType = definition['activation']['activationType']
        #print(f'actType = {actType}', file=sys.stderr)
        castingTimeDescription = castingTimeArray[actType-1]

    aoe=None
    dataRange = 0
    if 'range' in definition:
        dataRange = definition['range']['rangeValue']

        if 'aoeType' in definition['range']:
            aoeType  = definition['range']['aoeType']
            aoeValue = definition['range']['aoeValue']
            aoe = { "shape": aoeType, "size": f'{aoeValue} foot' }

    duration = ""
    if 'duration' in definition:
        interval = definition['duration']['durationInterval']
        unit     = definition['duration']['durationUnit']
        duration = f'{interval} {unit}'

    Concentration = "No"
    if 'concentration' in definition:
        conc = definition['concentration']
        if conc:
            Concentration = "Yes"

    Damage     = ''
    DamageType = ''
    diceCount = 0
    diceValue = 0
    for mod in modifiers:
        if 'friendlyTypeName' in mod and mod['friendlyTypeName'] == 'Damage' and 'die' in mod:
            diceCount  = mod['die']['diceCount']
            diceValue  = mod['die']['diceValue']
            Damage     = mod['die']['diceString'] 
            DamageType = mod['friendlySubtypeName']

    abilityNameArray = ['Strength','Dexterity','Constitution','Intelligence','Wisdom','Charisma']

    save=None
    if 'saveDcAbilityId' in definition and definition['saveDcAbilityId'] != None:
        saveDcAbilityId = definition['saveDcAbilityId']
        print(f'saveDcAbilityId = {saveDcAbilityId}', file=sys.stderr)

        abilityName = abilityNameArray[saveDcAbilityId-1]
        save={ 
            "saveAbility": abilityName, 
            "onSucceed": "unaffected"   # TODO: this may vary, but source info is not easily parsed
        }

    attackRecord = {
      "name": f'{name} Attack',
      "payload": {
        "type": "Attack",
        "name": name
      }
    }

    if save != None:
        attackRecord['payload']['save'] = save

    if aoe != None:
        attackRecord['payload']['aoe'] = aoe

    damageRecord = {
      "name": f'{name} Damage',
      "parent": f'{name} Attack',
      "payload": {
        "type": "Damage",
        "ability": "none",
        "damageType": DamageType,
        "diceCount": diceCount,
        "diceSize": f'd{diceValue}'
      }
    }

    dataRecords = [ attackRecord, damageRecord ]

    result = {
        "name": name,
        "description": definition['description'],

        "properties": {
            "Category": "Spells",
            "School": definition['school'],
            "Name": name,
            "Level": definition['level'],
            "Casting Time": castingTimeDescription,

            "filter-Duration": duration,
            "data-RangeNum": dataRange,

            "data-datarecords": dataRecords,

            "Concentration": Concentration,
            "Damage": Damage,
            "Damage Type": DamageType
        },

        "publisher": "Wizards of the Coast",
        "book": "Hand Entered, Free Basic Rules (2024)"
    }

    print(json.dumps(result))

