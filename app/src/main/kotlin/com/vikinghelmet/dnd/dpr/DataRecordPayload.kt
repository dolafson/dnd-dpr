@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
@JsonIgnoreUnknownKeys
@JsonClassDiscriminator("type") // Specify the field name in JSON
sealed class DataRecordPayload()


@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Action")
data class ActionPayload(val name: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Armor Class")
data class ArmorClassPayload(val calculation: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Attack")
data class AttackPayload(
    val name: String? = null,
    val description: String? = null,
    val save: DataRecordPayloadSave? = null,
    val actionType: String? = null,
    val range: String? = null
)   : DataRecordPayload()
{
    @Serializable
    data class DataRecordPayloadSave (
        val saveAbility: String,        // "Wisdom",
        val onFail: String? = null,     // "onFail": "Take 5d8 Force damage.",
        val onSucceed: String? = null,  // "onSucceed": "Half as much damage."

        // val aoe:  "aoe":{"shape":"Sphere","size":.....
    )
}

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Condition")
data class ConditionPayload(val name: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Damage")
data class DamagePayload(
    val ability: String,
    val damageType: String,
    val diceCount: Int? = null,
    val diceSize: String? = null
) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Defense")
data class DefensePayload(val defense: String, val condition: String? = null, val damage: String? = null) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Effect")
data class EffectPayload(val name: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Healing")
data class HealingPayload(val ability: String, val isTemp: Boolean) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Generic Roll")
data class GenericRollPayload(val name: String? = null, val _label: String? = null, val rollString: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Hit Points")
data class HitPointsPayload(val calculation: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Language")
data class LanguagePayload(val name: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Roll Bonus")
data class RollBonusPayload(val bonusName: List<String>, val bonusDetails: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Sense")
data class SensePayload(val name: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Speed")
data class SpeedPayload(val calculation: String) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Spell")
data class SpellPayload(val name: String, val description: String, val upcastText: String? = null) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Spell Attach")
data class SpellAttachPayload(val spells: List<String>, val alwaysPrepared: Boolean) : DataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Upcasting")
data class UpcastingPayload(
    val mode: String,
    val startingLevel: Int? = null,
    val target: String,
    val changeMode: String,
    // val level: Int, // sometimes Int, sometimes String
    // val value: String, // sometimes Int, sometimes String
) : DataRecordPayload()

