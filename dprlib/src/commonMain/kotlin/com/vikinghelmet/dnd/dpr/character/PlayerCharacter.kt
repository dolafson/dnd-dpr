@file:OptIn(ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.action.Combatant
import com.vikinghelmet.dnd.dpr.action.Damage
import com.vikinghelmet.dnd.dpr.action.Weapon
import com.vikinghelmet.dnd.dpr.action.enums.AttackType
import com.vikinghelmet.dnd.dpr.action.enums.DamageType
import com.vikinghelmet.dnd.dpr.action.enums.WeaponProperty
import com.vikinghelmet.dnd.dpr.character.actions.ActionAdded
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.api.ApiRequestParameters
import com.vikinghelmet.dnd.dpr.character.classes.ClassFeature
import com.vikinghelmet.dnd.dpr.character.classes.ClassName
import com.vikinghelmet.dnd.dpr.character.feats.Definition
import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.feats.FeatAdded
import com.vikinghelmet.dnd.dpr.character.inventory.ArmorType
import com.vikinghelmet.dnd.dpr.character.modifiers.Modifier
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpellRemote
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.scenario.onesided.ActionsAvailable
import com.vikinghelmet.dnd.dpr.spells.SavingThrowAction
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.Globals
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
open class PlayerCharacter(
    @SerialName("data")
    val characterData: CharacterData,
    val id: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) : Combatant {
    @Transient private val logger = LoggerFactory.get(PlayerCharacter::class.simpleName ?: "")

    var alwaysPrepared: List<PreparedSpellRemote> = mutableListOf()

    open fun getAlwaysPreparedSpells(): List<PreparedSpellRemote> = alwaysPrepared

    override fun getName(): String {
        return characterData.name
    }

    override fun getWalkingSpeed() = 30 // TODO: data.race.weightSpeeds.normal.{walk,fly,burrow,climb,swim}

    open fun getLevel(): Int {
        return characterData.classes.first().level
    }

    // ----------------------------------------------------------------------------------------
    // TRAITS, ABILITIES, and FEATS

    override fun is2014(): Boolean {
        if (characterData.characterValues == null) return false // default to 2024 rules
        for (value in characterData.characterValues) {
            if (value.notes?.endsWith(" 2014") == true) return true
        }
        return false
    }

    private fun getRawAbilityScore(a: AbilityType): Int {
        for (stat in characterData.stats) {
            if (stat.id == a.ordinal) return stat.value
        }
        return 0 // should not get here
    }

    override fun getAbilityModifier(abilityType: AbilityType): Int {
        return Constants.statToBonusMap[getModifiedAbilityScore(abilityType)] ?: 0
    }

    private fun getBonusModifierSum(a: AbilityType, list: List<Modifier>): Int {
        var mod = 0
        for (modifier in list) {
            if (modifier.type == "bonus" && modifier.entityId == a.ordinal) {
                mod += (modifier.value?: 0)
            }
        }
        return mod
    }
    open fun getModifiedAbilityScore(a: AbilityType): Int {
        return getRawAbilityScore(a) +
                getBonusModifierSum(a, characterData.modifiers.race) +
                getBonusModifierSum(a, characterData.modifiers.feat)
    }

    fun getRawAbilityScoreMap()      = AbilityType.getAllNotALL().associateWith { getRawAbilityScore(it) }
    fun getModifiedAbilityScoreMap() = AbilityType.getAllNotALL().associateWith { getModifiedAbilityScore(it) }

    fun getProficiencyBonus(): Int {
        return Constants.levelToProficiencyMap[getLevel()] ?: 0
    }

    override fun isFeatEnabled(requested : Feat): Boolean {
        return getFeatList().any { it == requested }
    }

    fun addFeat(requested : Feat) {
        characterData.feats.add(FeatAdded(definition = Definition(name = requested.getNameWithWS())))
    }

    override fun isRacialTraitEnabled(requested : RacialTrait): Boolean {
        for (trait in getRacialTraitList()) {
            if (trait.definition.name == requested.getNameWithWS()) return true
        }
        return false
    }

    override fun isEvasive(): Boolean {
        return isClassFeatureEnabled(ClassFeature.Evasive)
    }

    fun getRacialTraitList() = characterData.race.racialTraits
    fun getRacialTraitNameList() = characterData.race.racialTraits.map { it.definition.name }

    fun getFeatAddedList(): List<FeatAdded> {
        return characterData.feats.filter { f ->
            f.definition.name != "Dark Bargain" &&
            !f.definition.categories!!.any { it2 -> it2.tagName == "__DISGUISE_FEAT"} &&
            !f.definition.categories.any { it2 -> it2.tagName == "__DISPLAY_WITH_DATA_ORIGIN"}
        }
    }

    open fun getFeatList(): List<Feat> {
        return getFeatAddedList().mapNotNull { it.getFeat() }
    }

    // ----------------------------------------------------------------------------------------
    // COMBAT MODIFIERS

    override fun getAC(): Int { // TODO: support for magic items that improve AC ?
        var armorClassSum = 0
        var armorType = ArmorType.Unarmored

        characterData.inventory?.filter { it.equipped == true }?.forEach { item ->
            val def = item.definition
            if (def.filterType == "Armor") {
                armorClassSum += (def.armorClass ?: 0)
                if (def.armorTypeId != null) {
                    val thisArmorType = ArmorType.entries[def.armorTypeId]
                    if (thisArmorType != ArmorType.Shield) {
                        armorType = thisArmorType
                    }
                }
            }
        }

        val dexBonus = Constants.statToBonusMap[getModifiedAbilityScore(AbilityType.Dexterity)] ?: 0
        val conBonus = Constants.statToBonusMap[getModifiedAbilityScore(AbilityType.Constitution)] ?: 0

        when (armorType) {
            ArmorType.Unarmored -> {
                armorClassSum = 10 + dexBonus
                if (isClassFeatureEnabled(ClassFeature.UnarmoredDefense)) armorClassSum += conBonus
            }
            ArmorType.LightArmor -> armorClassSum += dexBonus
            ArmorType.MediumArmor -> armorClassSum += kotlin.math.max(dexBonus, 2)
            ArmorType.HeavyArmor -> {} // no dex bonus
            else -> {}
        }
        return armorClassSum
    }

    override fun getHP(): Int = characterData.baseHitPoints

    override fun getInitiativeBonus(): Int {
        val dexBonus = getAbilityModifier(AbilityType.Dexterity)
        val wisBonus = getAbilityModifier(AbilityType.Wisdom)
        return if (!isClassFeatureEnabled(ClassFeature.DreadAmbusher)) dexBonus else dexBonus +  wisBonus
    }

    fun getSpellAbilityType(): AbilityType? {
        val abilityId: Int = characterData.classes.first().definition.spellCastingAbilityId ?: return null
        return AbilityType.entries[abilityId]
    }

    fun getSpellAbilityBonusWithoutPB(): Int {
        val abilityId = characterData.classes.first().definition.spellCastingAbilityId ?: return 0
        return getAbilityModifier(AbilityType.entries[abilityId])
    }
    override fun getSpellBonusToHit(): Int {
        return getSpellAbilityBonusWithoutPB() + getProficiencyBonus()
    }
    override fun getSpellSaveDC(): Int {
        return 8 + getSpellBonusToHit()
    }

    fun getRangeAttackModifiers(): Int {
        var mod = 0
        for (modifier in characterData.modifiers.feat) {
            if (modifier.type == "bonus" && modifier.subType == "ranged-weapon-attacks") {
                mod += (modifier.value?: 0)
            }
        }
        return mod
    }

    // this can potentially be used as part both attack and damage bonus calculation
    fun getAbilityWeaponBonus(w: Weapon): Int {
        // first calculate ability bonus, based on weapon type
        val strBonus = Constants.statToBonusMap[getModifiedAbilityScore(AbilityType.Strength)] ?: 0
        val dexBonus = Constants.statToBonusMap[getModifiedAbilityScore(AbilityType.Dexterity)] ?: 0

        return if (w.hasWeaponProperty(WeaponProperty.Finesse)) kotlin.math.max(strBonus, dexBonus)
        else if (w.attackType.includesMelee()) strBonus
        else dexBonus
    }

    // TODO: use of "includesMelee / includesRange" (above, below) should really be based on scenario
    //  - you get the melee bonus only if you are using the weapon in a melee attack, etc

    fun getAttackBonus(w: Weapon): Int {
        val statBonus = getAbilityWeaponBonus(w)
        val weaponTypeBonus = if (w.attackType.includesRange()) getRangeAttackModifiers() else 0  // TODO: melee attack bonuses ?
        return statBonus + getProficiencyBonus() + weaponTypeBonus // for now assume proficiency in all weapons
    }

    fun getDamageBonus(w: Weapon, isBA: Boolean): Int {
        if (isBA && !isFeatEnabled(Feat.TwoWeaponFighting)) {
            logger.debug { "isBA, and TWF is not enabled" }
            return 0
        }

        var bonus = getAbilityWeaponBonus(w)

        if (isFeatEnabled(Feat.GreatWeaponMaster) && w.hasWeaponProperty(WeaponProperty.Heavy)) {
            bonus += getProficiencyBonus()
            logger.debug { "GWM, adding PB, full damage bonus = $bonus" }
        }
        return bonus
    }

    // ----------------------------------------------------------------------------------------
    // WEAPONS

    fun getWeaponNicknameMap(): Map<String,String> {
        val result = mutableMapOf<String,String>()
        if (characterData.characterValues == null) return result // default to 2024 rules
        for (value in characterData.characterValues) {
            if (value.typeId == 8 && value.valueId != null && value.value != null) result.put(value.valueId, ""+value.value)
        }
        return result
    }

    override fun getWeaponList(): List<Weapon> {
        val list = mutableListOf<Weapon>()

        if (characterData.inventory == null) return list
        for (item in characterData.inventory) {
            //if (item.equipped != true) continue // TODO: more flexible ways to limit selected weapons

            val def = item.definition
            if (def.filterType == "Weapon") {
                val name     = def.name.replace(",.*".toRegex(),"")
                // val nickname = getWeaponNicknameMap().get(""+item.id)

                val attackType = AttackType.getByType(item.definition.attackType ?: 1)
                val range      = item.definition.range ?: Constants.MELEE_RANGE
                val longRange  = item.definition.longRange ?: range
                val propertyNames = item.definition.properties?.map {it.name} ?: emptyList()
                val magicBonus = item.definition.grantedModifiers?.firstOrNull {
                    it.type == "bonus" && it.subType == "magic" && it.modifierTypeId == 1 && it.modifierSubTypeId == 312
                }?.value ?: 0

                val weapon = Weapon (name, attackType, range, longRange, 0, propertyNames, mutableListOf())

                // weapon attack/damage bonus are easier to modify post-constructor
                weapon.bonusToHit = magicBonus + this.getAttackBonus(weapon)

                // PC weapons almost always do a single form of damage
                weapon._damageList = mutableListOf(Damage(
                    DiceBlock(item.definition.damage?.diceString ?: "0d4"),
                    magicBonus,
                    this.getDamageBonus(weapon, false),
                    DamageType.valueOf(item.definition.damageType!!.lowercase())))

                list.add(weapon)
            }
        }
        return list
    }

    fun getWeapon(name: String): Weapon {
        for (weapon in getWeaponList()) if (weapon.name == name) return weapon
        throw IllegalArgumentException("weapon not found: $name")
    }

    // ----------------------------------------------------------------------------------------
    // SPELLS

    private fun transformSpellList(origin: String, input: List<PreparedSpellRemote>): List<PreparedSpell>
    {
        val originNameList = input.map { it.definition.name }
        logger.debug { "transformSpellList($origin), before transform = ${ originNameList  }" }

        val result = mutableListOf<PreparedSpell>()
        for (psRemote in input) {
            try {
                val spell =  Globals.getSpell(psRemote.definition.name, is2014())
                result.add (PreparedSpell(psRemote.alwaysPrepared, spell))
            }
            catch (e: Exception) {
                logger.error { "unable to add preparedSpell ${psRemote.definition.name}: $e" }
            }
        }
        return result
    }

    open fun getPreparedSpells(): List<PreparedSpell> {
        val result = mutableListOf<PreparedSpell>()
        for (classSpellList in characterData.classSpells!!) {
            result.addAll (transformSpellList ("topClass", classSpellList.spells))
        }
        result.addAll (transformSpellList ("class", characterData.spells.classSpells))
        result.addAll (transformSpellList ("race", characterData.spells.raceSpells))
        result.addAll (transformSpellList ("feat", characterData.spells.featSpells))
        result.addAll (transformSpellList ("always", getAlwaysPreparedSpells()))
        result.addAll (getSavingThrowActionList())
        return result
    }

    fun getPreparedAttackSpells(): List<PreparedSpell> {
        val result = mutableListOf<PreparedSpell>()
        for (spell in getPreparedSpells()) {
            // when attacking, ignore healing spells
            if (spell.properties.filterTags?.contains("Healing") == true) continue

            // ritual spells are not normally used in combat; they also do not consume a spell slot when read from a book
            if (spell.isRitual()) {
                logger.debug { "excluding ritual spell from prepared list" }
                continue
            }

            // this list should only contain primary attacks
            if (spell.isBonusAction()) continue
            result.add(spell)
        }
        return result
    }

    override fun getPreparedBonusActionSpells(targetProximity: Int): List<PreparedSpell> {
        val result = mutableListOf<PreparedSpell>()
        for (spell in getPreparedSpells()) {
            if (!spell.isBonusAction()) continue

            // special cases ... these spells don't specify range, as it is implied by the weapon type
            if (spell.takeImmediatelyAfterHitting()) {
                if ( (spell.isMeleeWeaponBonusAction()  && (targetProximity <= Constants.MELEE_RANGE)) ||
                     (spell.isRangedWeaponBonusAction() && (targetProximity > Constants.MELEE_RANGE)) )
                {
                    result.add(spell)
                }
            }
            else if (targetProximity <= spell.getRange()) { // all other spells, include on the basis of range
                result.add(spell)
            }
        }
        return result
    }

    // ----------------------------------------------------------------------------------------
    // SPELL SLOTS

    override fun getSpellSlots(): List<Int> {
        // TODO: support multi-class spell casters
        return characterData.classes.first().definition.spellRules?.levelSpellSlots?.get(getLevel()) ?: MutableList(20) { 0 }
    }

    private fun getMaxPreparedSpells(characterLevel: Int): Int {
        var maxList = characterData.classes.first().definition.spellRules?.levelSpellKnownMaxes ?: emptyList()
        if (maxList.isNotEmpty()) return maxList[characterLevel]

        val maxList2 = characterData.classes.first().definition.spellRules?.levelPreparedSpellMaxes ?: emptyList()
        return if (maxList2.isEmpty()) 0 else maxList2[characterLevel] ?: 0
    }

    private fun getSpellSlotsAtCharacterLevel(characterLevel: Int): List<Int> {
        //return characterData.classes.first().definition.spellRules?.levelSpellSlots?.get(characterLevel) ?: emptyList()
        val result = getSpellSlotsIncludingExtraForPrepared(characterLevel)
        //println("getSpellSlotsIncludingExtraForPrepared(level=$characterLevel) = $result")
        return result
    }

    fun getSpellSlotsGainedAtCharacterLevel(characterLevel: Int): List<Int> {
        if (characterLevel == 0) return emptyList()

        val slotsNow = getSpellSlotsAtCharacterLevel(characterLevel)
        if (characterLevel == 1) return slotsNow

        val slotsBefore = getSpellSlotsAtCharacterLevel(characterLevel-1)
        var result: MutableList<Int> = mutableListOf()

        for (id in slotsBefore.indices.sorted()) {
            // println("get new slots, id=$id, now=${ slotsNow[id] }, before=${ slotsBefore[id] }, delta=${ slotsNow[id] - slotsBefore[id] }")
            result.add(slotsNow[id] - slotsBefore[id])
        }
        return result
    }

    fun getNumberOfSlotsAtSpellLevel(spellLevel: Int): Int {
        val slotList = getSpellSlotsIncludingExtraForPrepared(getLevel())
        return slotList[spellLevel-1] // 1-based to 0-based indexing
    }

    fun getSpellSlotsIncludingExtraForPrepared(characterLevel: Int): List<Int> {
        // TODO: support multi-class spell casters
        val result = mutableListOf<Int>()
        if (characterLevel == 0) return result
        result.addAll(characterData.classes.first().definition.spellRules?.levelSpellSlots?.get(characterLevel) ?: MutableList(9) { 0 })

        //println("extra($characterLevel): before adjustment, result = $result")
        val delta = getMaxPreparedSpells(characterLevel) - result.sum()
        if (delta <= 0) return result

        // otherwise, find the highest non-zero slot, and add the delta there ...
        val index = result.indexOfLast { it > 0 }
        result[index] += delta

        return result;
    }

    // ----------------------------------------------------------------------------------------
    // ACTIONS (spells or weapons)

    override fun getActionsAvailable(): ActionsAvailable {
        val actionsAvailable = ActionsAvailable()

        for (weapon in getWeaponList()) {
            actionsAvailable.add(weapon.range, weapon)

            if (weapon.hasWeaponProperty(WeaponProperty.Thrown)) {
                actionsAvailable.add(Constants.MELEE_RANGE, weapon) // this ensures it will appear in both melee and range selection
            }
        }

        for (spell in getPreparedAttackSpells()) {
            logger.debug { "getActionsAvailable, spell = $spell" }

            if (spell.isRangedSpellAttack()) {
                actionsAvailable.add(spell.getRange(), spell)
            }
            else {
                // all spells - except for "ranged spell attack" - can be used in melee
                actionsAvailable.add(Constants.MELEE_RANGE, spell)

                if (spell.getRange() > Constants.MELEE_RANGE) {
                    actionsAvailable.add(spell.getRange(), spell)
                }
                else {
                    // "range = self, but aoe is 60 ft cone ..."
                    // this may be inaccurate for some AOE shapes, but close enough for now
                    if (spell.getSpellAttacks(0).isNotEmpty()) {
                        val aoeSize = spell.getSpellAttacks(0).first().getAoeSize()
                        if (aoeSize > 0) {
                            actionsAvailable.add(aoeSize, spell)
                        }
                    }
                }
            }
        }
        return actionsAvailable
    }

    override fun getActionModifiersAvailable(): List<ActionModifier> {
        val result = mutableListOf<ActionModifier>()
        val nameList = (
            characterData.actions.race.map { it.name } +
            characterData.actions.feat.map { it.name } +
            characterData.actions.classActions.map { it.name }
        ).filter { s -> !s.contains("Circle Spell") } // circle spell is garbage data, not really usable

        for (name in nameList) {
            try {
                val mod = ActionModifier.fromName(name)
                if (mod != null) result.add(mod)
            }
            catch (e: IllegalArgumentException) {
                Globals.debug("action is unsupported: " + name)
            }
        }
        return result
    }

    override fun getActionList(): List<ActionAdded> {
        return (characterData.actions.race +
                characterData.actions.feat +
                characterData.actions.classActions
        ).filter { a -> !a.name.contains("Circle Spell") }
    }

    private fun getSavingThrowActionList(): List<SavingThrowAction>
    {
        val result = mutableListOf<SavingThrowAction>()
        for (action in getActionList()) {
            if (action.saveStatId != null) {
                result.add (action.toSavingThrowAction())
            }
        }
        return result
    }

        // ----------------------------------------------------------------------------------------
    // CLASS INFO

    fun getSpellsForClass(): List<Spell> {
        return Globals.getSpellsForClass(getClassName(), is2014 = is2014())
    }

    fun getSubclassOptions(): List<String> {
        return when (getClass()) {
            ClassName.Barbarian -> listOf("Path of the Berserker","Path of the Wild Heart","Path of the World Tree","Path of the Zealot")
            ClassName.Bard      -> listOf("College of Dance","College of Glamour","College of Lore","College of Valor","College of the Moon")
            ClassName.Cleric    -> listOf("Life Domain","Light Domain","Trickery Domain","War Domain","Knowledge Domain")
            ClassName.Druid     -> listOf("Circle of the Land","Circle of the Moon","Circle of the Sea","Circle of the Stars")
            ClassName.Fighter   -> listOf("Battle Master","Champion","Eldritch Knight","Psi Warrior","Benneret")
            ClassName.Monk      -> listOf("Warrior of Mercy","Warrior of Shadow","Warrior of the Elements","Warrior of the Open Hand")
            ClassName.Paladin   -> listOf("Oath of Devotion","Oath of Glory","Oath of the Ancients","Oath of Vengeance","Oath ofthe Noble Genies")
            ClassName.Ranger    -> listOf("Beast Master","Fey Wanderer","Gloom Stalker","Hunter","Winter Walker")
            ClassName.Rogue     -> listOf("Arcane Trickster","Assassin","Soulknife","Thief","Scion of the Three")
            ClassName.Sorcerer  -> listOf("Aberrant Sorcery","Clockwork Sorcery","Draconic Sorcery","Wild Magic Sorcery","Spellfire Sorcery")
            ClassName.Warlock   -> listOf("Archfey Patron","Celestial Patron","Fiend Patron","Great Old One Patron")
            ClassName.Wizard    -> listOf("Abjurer","Diviner","Evoker","Illusionist","Bladesinger")
            else -> listOf()
        }
    }

    fun getClass(): ClassName {
        return ClassName.valueOf(characterData.classes.first().definition.name)
    }

    fun getClassName(): String {
        return characterData.classes.first().definition.name
    }

    open fun getSubclassName(): String? {
        return characterData.classes.first().subclassDefinition?.name
    }

    fun getSubclassLevel(): Int? {
        return getClassFeaturesByLevel().filter { it.key.contains("Subclass") || it.key.contains("Divine Domain")}.map { it.value}.firstOrNull()
    }

    fun getLevelsForAbilityIncrease(): List<Int> {
        return getClassFeaturesByLevel().filter { it.key.contains("Ability Score Improvement")}.map { it.value}
    }

    fun getExtraAttacks(): Int {
        // most martial classes get one extra at level 5, and thats it
        // fighters get an extra at L5, another at L11, and a third at L20
        return getClassFeaturesByLevel().filter { it.key.contains("Extra Attack") && it.value <= getLevel() }.count()
    }

    fun isClassFeatureEnabled(feature: ClassFeature): Boolean {
        return getClassFeaturesByLevel().any { it.key == feature.getSerialName() }
    }

    fun getLevelsForFightingStyle(): List<Int> {
        return getClassFeaturesByLevel().filter { it.key.contains("Fighting Style")}.map { it.value}
    }

    fun getClassFeaturesByLevel(): Map<String, Int> {
        return characterData.classes.first().definition.classFeatures.map { it -> Pair(it.name, it.requiredLevel) }.toMap()
    }

    fun getClassFeatureNames(): List<String> {
        return characterData.classes.first().definition.classFeatures.map { it.name }
    }

    fun getSubclassFeatureNames(): List<String> {
        val sub = characterData.classes.first().subclassDefinition ?: return emptyList()
        return sub.classFeatures.map { it.name }
    }

    fun getApiRequestParameters(): ApiRequestParameters {
        val classId = characterData.classes.first().subclassDefinition?.id ?:
            characterData.classes.first().classFeatures.mapNotNull { it.definition }.mapNotNull { it.classId }.firstOrNull()

        return ApiRequestParameters(
            characterData.campaign?.id,
            characterData.background?.definition?.id,
            classId,
            getLevel()
        )
    }

}
