@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
@JsonIgnoreUnknownKeys
@JsonClassDiscriminator("type") // Specify the field name in JSON
sealed class SpellDataRecordPayload


@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Action")
data class ActionPayload(val name: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Armor Class")
data class ArmorClassPayload(val calculation: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Attack")
data class AttackPayload(
    val name: String? = null,
    val description: String? = null,
    val save: DataRecordPayloadSave? = null,
    val aoe: AreaOfEffect? = null,    //  "aoe":{"shape":"Sphere","size":.....
    val actionType: String? = null,
    val range: String? = null
)   : SpellDataRecordPayload()
{
    @Serializable
    data class DataRecordPayloadSave (
        val saveAbility: String,        // "Wisdom",
        val onFail: String? = null,     // "onFail": "Take 5d8 Force damage.",
        val onSucceed: String? = null,  // "onSucceed": "Half as much damage."
    )

}

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Condition")
data class ConditionPayload(val name: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Damage")
data class DamagePayload(
    val ability: String,
    val damageType: String,
    val diceCount: Int? = null,
    val diceSize: String? = null
) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Defense")
data class DefensePayload(val defense: String, val condition: String? = null, val damage: String? = null) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Effect")
data class EffectPayload(val name: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Healing")
data class HealingPayload(val ability: String, val isTemp: Boolean) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Generic Roll")
data class GenericRollPayload(val name: String? = null, val _label: String? = null, val rollString: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Hit Points")
data class HitPointsPayload(val calculation: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Language")
data class LanguagePayload(val name: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Roll Bonus")
data class RollBonusPayload(val bonusName: List<String>, val bonusDetails: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Sense")
data class SensePayload(val name: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Speed")
data class SpeedPayload(val calculation: String) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Spell")
data class SpellPayload(val name: String, val description: String, val upcastText: String? = null) : SpellDataRecordPayload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Spell Attach")
data class SpellAttachPayload(val spells: List<String>, val alwaysPrepared: Boolean) : SpellDataRecordPayload()

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
) : SpellDataRecordPayload()

