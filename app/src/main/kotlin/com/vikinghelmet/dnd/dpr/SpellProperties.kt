package com.vikinghelmet.dnd.dpr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpellProperties (

    // all spells - required properties
    @SerialName("Casting Time") val CastingTime: String,
    val Category: String,
    val Level: Int,
    val School: String,

    // optional fields

    // PHB - both
    //      391 "Player's Handbook (2024)"
    //      361 "Player's Handbook"
    val Components: String? = null,
    @SerialName("Damage Type") val DamageType: String? = null,
    val Expansion: Int? = null,
    val Name: String? = null,
    val Save: String? = null,
    @SerialName("data-RangeAoe") val dataRangeAoe: String? = null, // not useful

    // Free Basic Rules (2024)
    val Classes: String? = null,
    val Concentration: String? = null,
    val Damage: String? = null,
    val Duration: String? = null,
    @SerialName("Higher Spell Slot Desc") val HigherSpellSlotDesc: String? = null,
    val Material: String? = null,
    val Range: String? = null,
    val Ritual: String? = null,

    @SerialName("Spell Attack") val SpellAttack: String? = null,
    val Target: String? = null,

    @SerialName("data-AttackType") val dataAttackType: String? = null,
    @SerialName("data-Cantrip Scaling") val dataCantripScaling: String? = null,
    @SerialName("data-CastNum") val dataCastNum: Int? = null,
    @SerialName("data-DurationNum") val dataDurationNum: Int? = null,
    @SerialName("data-List") val dataList: String? = null,
    @SerialName("data-RangeNum") val dataRangeNum: Int? = null,
    @SerialName("data-datarecords") val dataDatarecords: String? = null,
    @SerialName("data-description") val dataDescription: String? = null,

    @SerialName("filter-Casting Time") val filterCastingTime: String? = null,
    @SerialName("filter-Components") val filterComponents: String? = null,
    @SerialName("filter-Concentration") val filterConcentration: String? = null,
    @SerialName("filter-Duration") val filterDuration: String? = null,
    @SerialName("filter-Level") val filterLevel: Int? = null,
    @SerialName("filter-Range") val filterRange: String? = null,
    @SerialName("filter-Ritual") val filterRitual: String? = null,
    @SerialName("filter-Tags") val filterTags: String? = null,
    @SerialName("filter-Upcast") val filterUpcast: String? = null,

    // other
    @SerialName("Add Casting Modifier") val AddCastingModifier: String? = null,
    val Healing: String? = null,
    @SerialName("Higher Spell Slot Dice") val HigherSpellSlotDice: Float? = null,
    @SerialName("Higher Spell Slot Die") val HigherSpellSlotDie: String? = null,
    @SerialName("Save Success") val SaveSuccess: String? = null,
    @SerialName("Saving Throws") val SavingThrows: String? = null,
    @SerialName("Secondary Damage") val SecondaryDamage: String? = null,
    @SerialName("Secondary Damage Type") val SecondaryDamageType: String? = null,
    val Source: String? = null,
)
