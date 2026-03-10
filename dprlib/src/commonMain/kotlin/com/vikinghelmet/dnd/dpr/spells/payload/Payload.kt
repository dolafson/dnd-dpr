@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.spells.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
@JsonIgnoreUnknownKeys
@JsonClassDiscriminator("type") // json field that specifies subtype
sealed class Payload

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Action")
data class ActionPayload(val name: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Armor Class")
data class ArmorClassPayload(val calculation: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Condition")
data class ConditionPayload(val name: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Defense")
data class DefensePayload(val defense: String, val condition: String? = null, val damage: String? = null) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Effect")
data class EffectPayload(val name: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Healing")
data class HealingPayload(val ability: String, val isTemp: Boolean) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Generic Roll")
data class GenericRollPayload(val name: String? = null, val _label: String? = null, val rollString: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Hit Points")
data class HitPointsPayload(val calculation: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Language")
data class LanguagePayload(val name: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Roll Bonus")
data class RollBonusPayload(val bonusName: List<String>, val bonusDetails: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Sense")
data class SensePayload(val name: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Speed")
data class SpeedPayload(val calculation: String) : Payload()

@Serializable
@JsonIgnoreUnknownKeys
@SerialName("Spell Attach")
data class SpellAttachPayload(val spells: List<String>, val alwaysPrepared: Boolean) : Payload()

