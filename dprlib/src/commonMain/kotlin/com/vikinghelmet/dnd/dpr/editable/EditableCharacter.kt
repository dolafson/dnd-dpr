package com.vikinghelmet.dnd.dpr.editable

import com.vikinghelmet.dnd.dpr.character.Character
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.util.EditableAbilityMap
import com.vikinghelmet.dnd.dpr.util.NumericRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class EditableCharacter (
    val from: Character,
    val editableFields: EditableFields
) : Character(from.characterData, from.id, from.message, from.success)
{
    fun getAbilityMap(): EditableAbilityMap {
        val result = mutableMapOf<AbilityType, NumericRange>()
        AbilityType.entries.forEach {
            result[it] = NumericRange(getModifiedAbilityScore(it), 20)
        }
        return EditableAbilityMap(result)
    }

    override fun getModifiedAbilityScore(a: AbilityType): Int {
        if (editableFields.plan.isEmpty()) {
            return editableFields.stats[a] ?: 0     // TODO: remove this once plan is fully realized
        }

        var increase = 0
        for (i in from.getLevel()..getLevel()) {
            increase += (editableFields.plan[i.toString()]?.asi[a] ?: 0)
        }
        return from.getModifiedAbilityScore(a) + increase
    }

    override fun getLevel(): Int {
        return editableFields.level
    }

    override fun getName(): String {
        return editableFields.name
    }

}