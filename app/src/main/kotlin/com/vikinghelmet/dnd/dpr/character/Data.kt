package com.vikinghelmet.dnd.dpr.character

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/*
0. Calculate ability stats plus racial modifiers

    # cat /tmp/leif.pretty.json | jq .data.stats[]
    # cat /tmp/leif.pretty.json | jq '.data.modifiers.race[] | select(.type == "bonus")'

1. Locate Classes: Parse character.classes to find the class with the highest spellcasting capability or the one currently active.

    # cat /tmp/leif.pretty.json | jq .data.classes[] |

        "id": 216942902,
        "entityTypeId": 1446578651,
        "level": 2,
            ...
        "definition": {
            ...
          "spellCastingAbilityId": 5,   # Wisdom,             # 5

2. Get Level

    # cat /tmp/leif.pretty.json | jq .data.classes[].level

3. Calculate PB based on level (STATIC TABLE LOOKUP)

4. Identify the spellCastingAbilityId [1..6] = [STR..CHA]

    # cat /tmp/leif.pretty.json | jq .data.classes[].definition.spellCastingAbilityId

5. Get the ability score from step 0, then map the score to a modifier (STATIC TABLE LOOKUP)

6. Add modifier to PB
 */

@JsonIgnoreUnknownKeys
@Serializable
data class Data(
    val classes: List<CharacterClass>,
    val modifiers: AbilityModifiers,
    val stats: List<AbilityScore>,
)