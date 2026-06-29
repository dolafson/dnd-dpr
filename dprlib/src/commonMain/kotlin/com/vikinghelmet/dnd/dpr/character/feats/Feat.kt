package com.vikinghelmet.dnd.dpr.character.feats

import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType.*
import com.vikinghelmet.dnd.dpr.scenario.TargetEffectCause
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private val strAndDex = listOf(Strength,Dexterity)
private val str = listOf(Strength)
private val dex = listOf(Dexterity)
private val con = listOf(Constitution)
private val int = listOf(Intelligence)
private val wis = listOf(Wisdom)
private val cha = listOf(Charisma)

private val spellCaster = listOf(Intelligence, Wisdom, Charisma)

@Serializable(with = FeatSerializer::class)
enum class Feat(
    val fullSupport: Boolean = false,
    val asiPrerequisite: List<AbilityType> = emptyList(),
    val asiChoices: List<AbilityType> = asiPrerequisite,
    val isFightingStyle: Boolean = false,
) : TargetEffectCause
{
    // https://www.dndbeyond.com/sources/dnd/phb-2024/feats

    AbilityScoreIncrease(true, emptyList(), AbilityType.getAllNotALL()),

    Actor (false, cha),
    Athlete (false, strAndDex),
    Charger (false, strAndDex),
    Chef (false, emptyList(), listOf(Constitution,Wisdom)),
    ColdCaster(true, emptyList(), spellCaster),     // https://www.dndbeyond.com/sources/dnd/frhof/character-options#ColdCaster

    CrossbowExpert (false, dex),
    Crusher (false, emptyList(), listOf(Strength,Constitution)),
    DefensiveDuelist (false, dex),
    DualWielder (false, strAndDex),
    Durable (false, emptyList(), con),
    ElementalAdept(true, emptyList(), spellCaster),
    FeyTouched (false, emptyList(), spellCaster),
    Grappler (false, strAndDex),
    GreatWeaponMaster(false, str),

    HeavilyArmored (false, emptyList(), str),
    HeavyArmorMaster (false, emptyList(), listOf(Strength,Constitution)),
    InspiringLeader (false, listOf(Wisdom,Charisma)),
    KeenMind (false, int),
    LightlyArmored (false, emptyList(), strAndDex),
    MageSlayer (false, emptyList(), strAndDex),
    MartialWeaponTraining (false, emptyList(), strAndDex),
    MediumArmorMaster (false, emptyList(), strAndDex),
    ModeratelyArmored (false, emptyList(), strAndDex),
    MountedCombatant (false, emptyList(), listOf(Strength,Dexterity,Wisdom)),
    Observant (false, listOf(Intelligence,Wisdom)),
    Piercer (false, emptyList(), strAndDex),
    Poisoner (false, emptyList(), listOf(Dexterity,Intelligence)),
    PolearmMaster (false, strAndDex),
    Resilient (false, emptyList(), AbilityType.getAllNotALL()),
    Ritualist (false, spellCaster),
    Sentinel (false, strAndDex),
    ShadowTouched (false, emptyList(), spellCaster),
    Sharpshooter (false, dex),
    ShieldMaster (false, emptyList(), str),
    SkillExpert (false, emptyList(), AbilityType.getAllNotALL()),
    Skulker (false, dex),
    Slasher (false, emptyList(), strAndDex),
    Speedy (false, listOf(Dexterity,Constitution)),
    SpellSniper (false, spellCaster),
    Telekinetic (false, emptyList(), spellCaster),
    Telepathic (false, emptyList(), spellCaster),
    WarCaster (false, spellCaster),
    WeaponMaster (false, emptyList(), strAndDex),

    // fighting style feats
    // https://www.dndbeyond.com/sources/dnd/phb-2024/feats#GreatWeaponFighting

    Archery(false, isFightingStyle = true), // this is only used implicitly, via Character.getRangeAttackModifiers()
    BlindFighting(false, isFightingStyle = true),
    Defense(false, isFightingStyle = true),
    Dueling(false, isFightingStyle = true),
    GreatWeaponFighting(true, isFightingStyle = true),
    Interception(false, isFightingStyle = true),
    Protection(false, isFightingStyle = true),
    ThrownWeaponFighting(false, isFightingStyle = true),
    TwoWeaponFighting(false, isFightingStyle = true),
    UnarmedFighting(false, isFightingStyle = true),
    ;

    fun getNameWithWS(): String {
        return if (this == TwoWeaponFighting) "Two-Weapon Fighting" else Globals.addWStoCamelCase(name)
    }

    companion object {
        fun fromName(input: String) = entries.find { it.name == input || it.getNameWithWS() == input }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Feat::class)
object FeatSerializer : KSerializer<Feat> {
    override fun serialize(encoder: Encoder, value: Feat) {
        encoder.encodeString (Globals.addWStoCamelCase (value.toString()))
    }

    override fun deserialize(decoder: Decoder) = Feat.fromName (decoder.decodeString())!!

}
