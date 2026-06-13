package com.vikinghelmet.dnd.dpr.character.actions

import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.SavingThrowAction
import com.vikinghelmet.dnd.dpr.spells.SavingThrowAction.Companion.makeProps
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffectShape
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Transient private val logger = LoggerFactory.get(ActionAdded::class.simpleName ?: "")

@JsonIgnoreUnknownKeys
@Serializable
data class ActionAdded(
    val name: String,
    val description: String? = null,
    val snippet: String,

    var saveStatId: Int? = null,
    val saveFailDescription: String? = null,
    val saveSuccessDescription: String? = null,

    val limitedUse: com.vikinghelmet.dnd.dpr.character.actions.LimitedUse? = null,
    val activation: com.vikinghelmet.dnd.dpr.character.actions.Activation? = null,
    val dice: com.vikinghelmet.dnd.dpr.character.actions.Dice? = null,
    val range: com.vikinghelmet.dnd.dpr.character.actions.Range? = null,

    /*
    val abilityModifierStatId: Any,
    val actionType: Int,
    val ammunition: Any,
    val attackSubtype: Any,
    val attackTypeRange: Any,
    val componentId: Int,
    val componentTypeId: Int,
    val damageTypeId: Int,
    val description: String,
    val displayAsAttack: Boolean,
    val entityTypeId: String,
    val fixedSaveDc: Any,
    val fixedToHit: Any,
    val id: String,
    val isMartialArts: Boolean,
    val isProficient: Boolean,

    val numberOfTargets: Any,
    val onMissDescription: String,
    val saveFailDescription: String,
    val saveStatId: Any,
    val saveSuccessDescription: String,
    val spellRangeType: Any,
    val value: Any
 */
) {

    fun toSavingThrowAction(): SavingThrowAction {
        // val mod = ActionModifier.partialMatch(name)
        val abilityType = AbilityType.entries[saveStatId!!]
        val damageType  = name.substringAfterLast('(').substringBeforeLast(')')
        val damageString = if (dice == null) null
        else "${ dice.diceCount }d${ dice.diceValue }"

        val spellAction = SavingThrowAction(
            name, description ?: "",
            Attack.Save (abilityType, saveFailDescription, saveSuccessDescription),
            AreaOfEffect (AreaOfEffectShape.Line, "30"),
            damageType, damageString
        )
        logger.debug { "getSpellLikeActionList, spell = $spellAction" }
        return spellAction
    }

    fun toHealingSpell(healingAmount: String, range: Int): PreparedSpell {
        val description = if (description.isNullOrEmpty()) snippet else description
        val spell = PreparedSpell (true, Spell("Fake Book 2014", description, name, makeProps(), ""))
        spell.properties.filterTags = "Healing"
        spell.properties.Healing = healingAmount
        spell.properties.dataRangeNum ?: range

        return spell
    }

}