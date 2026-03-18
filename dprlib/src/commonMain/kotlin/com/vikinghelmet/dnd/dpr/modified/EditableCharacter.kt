package com.vikinghelmet.dnd.dpr.modified

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.util.HasNumericRangeMap
import com.vikinghelmet.dnd.dpr.util.NumericRange
import com.vikinghelmet.dnd.dpr.util.NumericRangeMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class EditableCharacter (
    val from: Character,
    val editableFields: EditableFields
) : HasNumericRangeMap, Character(from.characterData, from.id, from.message, from.success)
{
    override fun getNumericRangeMap(): NumericRangeMap {
        val result = mutableMapOf<String, NumericRange>()
        AbilityType.entries.forEach {
            val score = getModifiedAbilityScore(it)
            result.put(it.name, NumericRange(score, 20))
        }
        result.put("level", NumericRange(getLevel(), 20))
        return NumericRangeMap(result)
    }

    override fun getModifiedAbilityScore(a: AbilityType): Int {
        return editableFields.stats[a] ?: 0
    }

    override fun getLevel(): Int {
        return editableFields.level
    }

    override fun getName(): String {
        return editableFields.name
    }

}