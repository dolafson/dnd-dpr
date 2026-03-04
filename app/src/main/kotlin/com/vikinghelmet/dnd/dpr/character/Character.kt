@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.vikinghelmet.dnd.dpr.character

import com.vikinghelmet.dnd.dpr.character.feats.Feat
import com.vikinghelmet.dnd.dpr.character.inventory.Weapon
import com.vikinghelmet.dnd.dpr.character.modifiers.Modifier
import com.vikinghelmet.dnd.dpr.character.race.RacialTrait
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.spells.SpellHelper
import com.vikinghelmet.dnd.dpr.turn.*
import com.vikinghelmet.dnd.dpr.util.Constants
import com.vikinghelmet.dnd.dpr.util.Globals
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@JsonIgnoreUnknownKeys
@Serializable
data class Character(
    @SerialName("data")
    val characterData: CharacterData,
    val id: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    fun dump() {
        println(Json.encodeToString(this))
    }

    // ----------------------------------------------------------------------------------------
    // TRAITS, ABILITIES, and FEATS

    fun is2014(): Boolean {
        if (characterData.characterValues == null) return false // default to 2024 rules
        for (value in characterData.characterValues) {
            if (value.notes?.endsWith(" 2014") == true) return true
        }
        return false
    }

    fun getRawAbilityScore(a: AbilityType): Int {
        for (stat in characterData.stats) {
            if (stat.id == a.ordinal) return stat.value
        }
        return 0 // should not get here
    }

    fun getBonusModifierSum(a: AbilityType, list: List<Modifier>): Int {
        var mod = 0
        for (modifier in list) {
            if (modifier.type == "bonus" && modifier.entityId == a.ordinal) {
                mod += (modifier.value?: 0)
            }
        }
        return mod
    }
    fun getModifiedAbilityScore(a: AbilityType): Int {
        return getRawAbilityScore(a) +
                getBonusModifierSum(a, characterData.modifiers.race) +
                getBonusModifierSum(a, characterData.modifiers.feat)
    }

    fun getLevel(): Int {
        return characterData.classes.first().level
    }

    fun getProficiencyBonus(): Int {
        return Constants.levelToProficiencyMap[getLevel()] ?: 0
    }

    fun isFeatEnabled(requested : Feat): Boolean {
        for (feat in characterData.feats) {
            if (feat.definition.name == requested.traitName) return true
        }
        return false
    }

    fun isRacialTraitEnabled(requested : RacialTrait): Boolean {
        for (trait in characterData.race.racialTraits) {
            if (trait.definition.name == requested.traitName) return true
        }
        return false
    }

    fun isLucky(): Boolean {
        return isRacialTraitEnabled (RacialTrait.Luck)
    }

    /**
     * Elemental Adept in D&D 5e enhances damage by treating any 1 rolled on damage dice for a chosen element
     * (acid, cold, fire, lightning, or thunder) as a 2. It also allows spells to ignore resistance to that
     * damage type, effectively doubling damage against resistant targets. This feat increases average damage
     * slightly, particularly with multi-die spells.
     */
    fun isElvenAccuracy(): Boolean {
        return isRacialTraitEnabled (RacialTrait.ElvenAccuracy)
    }

    fun isElementalAdept(): Boolean {
        return isFeatEnabled(Feat.ElementalAdept)
    }

    fun isGreatWeaponFighting(): Boolean {
        return isFeatEnabled(Feat.GreatWeaponFighting)
    }

    // ----------------------------------------------------------------------------------------
    // COMBAT MODIFIERS

    fun getSpellSaveDC(): Int {
        return 8 + getSpellBonusToHit()
    }

    fun getSpellBonusToHit(): Int {
        val abilityId = characterData.classes.first().definition.spellCastingAbilityId
        val statBonus = if (abilityId == null) 0 else {
            Constants.statToBonusMap[getModifiedAbilityScore(AbilityType.entries[abilityId])] ?: 0
        }
        return statBonus + getProficiencyBonus()
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

        val props = w.properties ?: emptyList()
        return if (props.contains("Finesse")) Math.max(strBonus, dexBonus)
        else if (w.attackType == 1) strBonus
        else dexBonus
    }

    fun getAttackBonus(w: Weapon): Int {
        val statBonus = getAbilityWeaponBonus(w)
        val weaponTypeBonus = if (w.attackType == 2) getRangeAttackModifiers() else 0  // TODO: melee attack bonuses ?
        return statBonus + getProficiencyBonus() + weaponTypeBonus // for now assume proficiency in all weapons
    }

    fun getDamageBonus(w: Weapon, isBA: Boolean): Int {
        return if (isBA) 0 else getAbilityWeaponBonus(w) // TODO: two-weapon fighting feat lets you add bonus damage even in BA
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


    fun getWeaponNames(): List<String> {
        val list = mutableListOf<String>()
        if (characterData.inventory == null) return list
        for (item in characterData.inventory) {
            if (item.definition.filterType == "Weapon") list.add(item.definition.name)
        }
        return list;
    }

    fun getWeaponList(): List<Weapon> {
        val list = mutableListOf<Weapon>()

        if (characterData.inventory == null) return list
        for (item in characterData.inventory) {
            if (item.equipped != true) continue

            val def = item.definition
            if (def.filterType == "Weapon") {
                var props = mutableListOf<String>()
                if (def.properties != null) {
                    for (prop in def.properties) props.add(prop.name)
                }
                val diceString = def.damage?.diceString ?: "0d4"

                val nickname = getWeaponNicknameMap().get(""+item.id)

                list.add(Weapon (def.name, diceString, props, def.magic, def.attackType ?: 1, def.range ?: 5, def.longRange, nickname))
            }
        }
        return list
    }

    fun getWeapon(name: String?): Weapon? {
        if (name == null) return null
        for (weapon in getWeaponList()) if (weapon.name == name) return weapon
        return null
    }

    // ----------------------------------------------------------------------------------------
    // SPELLS

    private fun transformSpellList(input: List<PreparedSpell>): List<Spell> {
        val result = mutableListOf<Spell>()
        for (preparedSpell in input) {
            val spell = Globals.getSpell(preparedSpell.definition.name, is2014())
            if (spell != null) result.add(spell)
        }
        return result
    }

    fun getPreparedSpells(): List<Spell> {
        val result = mutableListOf<Spell>()
        for (classSpellList in characterData.classSpells!!)  result.addAll (transformSpellList (classSpellList.spells))
        result.addAll (transformSpellList (characterData.spells.classSpells))
        result.addAll (transformSpellList (characterData.spells.raceSpells))
        return result
    }

    fun getPreparedAttackSpells(): List<Spell> {
        val result = mutableListOf<Spell>()
        for (spell in getPreparedSpells()) {
            if (spell.properties.filterTags?.contains("Healing") == true) continue // we only care about offensive spells for now
            if (spell.isBonusAction()) continue // this list should only contain primary attacks
            result.add(spell)
        }
        return result
    }

    fun getPreparedBonusActionSpells(melee: Boolean): List<Spell> {
        val result = mutableListOf<Spell>()
        for (spell in getPreparedSpells()) {
            if (!spell.isBonusAction()) continue

            if (!spell.takeImmediatelyAfterHitting() ||
                (spell.isMeleeBonusAction() && melee) ||
                (spell.isRangedBonusAction() && !melee))
            {
                result.add(spell)
            }
        }
        return result
    }

    fun getSpellSlots(): List<Int> {
        // TODO: support multi-class spell casters
        return characterData.classes.first().definition.spellRules?.levelSpellSlots?.get(getLevel()) ?: MutableList(20) { 0 }
    }

    // ----------------------------------------------------------------------------------------
    // TESTS

    fun test() {
        println ("")
        println (String.format("%-15s %-5s %s\n", "ability", "base", "withBonusesAdded"))

        for (ability in AbilityType.entries) {
            if (ability == AbilityType.ALL) continue
            val base = getRawAbilityScore(ability)
            val withBonuses = getModifiedAbilityScore(ability)
            //println ("$ability: base=$base, withBonuses=$mod")
            println (String.format("  %-15s %3d %8d", ability, base, withBonuses))
        }

        val abilityId = characterData.classes.first().definition.spellCastingAbilityId
        val spellAbilityType = if (abilityId == null) "n/a" else AbilityType.entries[abilityId]

        println ("")
        println ("level         = "+getLevel())
        println ("PB            = "+getProficiencyBonus())
        println ("spell ability = "+spellAbilityType)
        println ("spellSaveDC   = "+getSpellSaveDC())
        println ("")
        println ("isLucky       = "+isLucky())
        println ("isEA          = "+isElvenAccuracy())
        println ("isGWF         = "+isGreatWeaponFighting())
        println ("is2014        = "+is2014())
        println ("")
        for (item in getWeaponList()) {
            val attackHitBonus      = getAttackBonus(item)
            val attackDamageBonus   = getDamageBonus(item, false)
            //val baDamageBonus       = getDamageBonus(item, true) // for now, this is always 0
            //println("$item, attackHitBonus=$attackHitBonus, attackDamageBonus=$attackDamageBonus, baDamageBonus=$baDamageBonus")
            println("$item, attackHitBonus=$attackHitBonus, attackDamageBonus=$attackDamageBonus")
        }

        println ("")
        for (trait in characterData.race.racialTraits) {
            println("racial trait: "+trait.definition.name)
        }

        println ("")
        for (feat in characterData.feats) {
            println("feat: "+feat.definition.name)
        }
        println ("")
        println ("weapon nickname map: "+getWeaponNicknameMap())
        println ("spell slots: "+getSpellSlots())
        println ("")
    }


    fun rangeTest() {
        println()
        val meleeBonusActions = SpellHelper.getSpellNames(getPreparedBonusActionSpells(true))
        val rangedBonusActions = SpellHelper.getSpellNames(getPreparedBonusActionSpells(false))

        val weaponList = mutableListOf<Weapon>()
        val weaponListNames = mutableListOf<String>()

        for (weapon in getWeaponList()) {
            if (!weaponListNames.contains(weapon.name)) {
                weaponListNames.add(weapon.name)
                weaponList.add(weapon)
            }
            else {
                // dups are most likely from a pair of light weapons for use in dual weapon fighting
                // when this happens add it as a bonus action
                meleeBonusActions.add(weapon.name)
            }
        }

        val rangeWeaponMap = mutableMapOf<Int, MutableList<Weapon>>() // hashMapOf<Int,Weapon>()
        for (weapon in weaponList) {
            rangeWeaponMap.getOrPut(weapon.range ?: 0) { mutableListOf() }.add(weapon)
        }

        val rangePreparedSpellMap = hashMapOf<Int, MutableList<Spell>>()
        for (spell in getPreparedAttackSpells()) {
            rangePreparedSpellMap.getOrPut(spell.properties.dataRangeNum ?: 0) { mutableListOf() }.add(spell)
        }

        val rangeList = mutableListOf<Int>()
        rangeList.addAll(rangeWeaponMap.keys)
        rangeList.addAll(rangePreparedSpellMap.keys)
        rangeList.sort()

        println()

        for (range in rangeList) {
            println("range = " + range)
            if (range in rangeWeaponMap.keys) {
                for (weapon in rangeWeaponMap.get(range)!!)  println("\t " + weapon.name)
            }
            if (range in rangePreparedSpellMap.keys) {
                for (spell in rangePreparedSpellMap.get(range)!!)  println("\t " + spell.name)
            }
            println()
        }

        println("Melee  Bonus Actions = $meleeBonusActions")
        println("Ranged Bonus Actions = $rangedBonusActions")
        println()
    }

    fun possibleTurns(actionsAvailable: ActionsAvailable, isMelee: Boolean): List<Turn> {
        val actionList = actionsAvailable.getFullList(isMelee)
        val bonusActions = SpellHelper.getSpellNames(getPreparedBonusActionSpells(isMelee))

        val monster = "Goblin" // TODO
        val turnOptions = ArrayList<Turn>()

        for (action in actionList) {
            if (action.spell != null) { // generally spell attacks do not get bonus actions
                turnOptions.add(Turn(attacks = listOf(Attack(monster = monster, attack = action.getName()))))
                continue
            }
            if (action.weapon == null) {
                Globals.debug("error: action without a spell or a weapon")
                continue
            }

            // remainder pertains to weapon attacks
            val w1 = action.weapon
            var turn: Turn? = null

            // light weapon ?  see if you have a 2nd one to use in a BA
            if (w1.isLight()) {
                for (w2 in getWeaponList()) {
                    if (w1 == w2) continue // if you have 2 shortswords, hopefully they vary by nickname ...
                    if (w2.isLight()) {
                        turn = Turn(attacks = listOf(
                            /*
                            Attack(monster = monster, attack = (w1.nickname ?: w1.name)),
                            Attack(monster = monster, attack = (w2.nickname ?: w2.name), isBonusAction = true),
                             */
                            Attack(monster = monster, attack = w1.name),
                            Attack(monster = monster, attack = w2.name, isBonusAction = true),
                        ))
                        break
                    }
                }
            }

            // if you didn't find a 2nd light weapon, just use the first weapon w/out a BA
            if (turn == null) {
                turn = Turn(attacks = listOf(Attack(monster = monster, attack = action.getName())))
            }
            turnOptions.add(turn)

            // bonus action spells: mostly for ranger and paladin
            for (bonus in bonusActions) {
                turnOptions.add(Turn(attacks = listOf(
                    //Attack(monster = monster, attack = (w1.nickname ?: w1.name)),
                    Attack(monster = monster, attack = w1.name),
                    Attack(monster = monster, attack = bonus),
                )))
            }
        }
        return turnOptions
    }

    fun getActionsAvailable(): ActionsAvailable {
        val actionsAvailable = ActionsAvailable()
        val weaponListNames = mutableListOf<String>()

        for (weapon in getWeaponList()) {
            if (weaponListNames.contains(weapon.name)) continue // avoid dups
            weaponListNames.add(weapon.name)
            actionsAvailable.add(weapon.range ?: 0, weapon)
        }

        for (spell in getPreparedAttackSpells()) {
            actionsAvailable.add(spell.properties.dataRangeNum ?: 0, spell)
        }
        return actionsAvailable
    }

    fun testPossibleTurns() {
        val actionsAvailable = getActionsAvailable()

        println("\n# MELEE ATTACKS\n")
        for (turn in possibleTurns(actionsAvailable, true))  println("\t "+turn.shortString())

        println("\n# RANGED ATTACKS\n")
        for (turn in possibleTurns(actionsAvailable, false))  println("\t "+turn.shortString())
        println()
    }

    data class Scenario(val turns: List<Turn>, val spellsUsed: List<Spell>) {
        fun isSlotAvailable(character: Character, spell: Spell): Boolean {
            val level = spell.properties.Level
            if (level == 0) return true // cantrip

            val maxSlots = character.getSpellSlots()[level - 1]
            val slotsUsed = spellsUsed.count { it.properties.Level == spell.properties.Level }
            return (slotsUsed < maxSlots)
        }

        fun getLabel(): String {
            val buf = StringBuilder()
            for (turn in turns) {
                val attackNameList = mutableListOf<String>()
                turn.attacks.map { attackNameList.add(it.attack) }
                buf.append(""+attackNameList)
            }
            return buf.toString()
        }
    }

    fun buildScenarios(rounds: Int, turnOptions: List<Turn>, currentScenario: Scenario, scenarioList: ArrayList<Scenario>) {
        if (rounds == 0) {
            scenarioList.add(currentScenario)
            return
        }

        for (turn in turnOptions) {
            val spellsUsedThisTurn = ArrayList<Spell>()

            // for each spell in the proposed turn, check if it would conflict with a prior running spell,
            // due to either (a) concentration required, or (b) insufficient remaining spell slots ...
            // if either of these are true, abandon this turn option

            for (a in turn.attacks) {
                val spell = Globals.getSpell(a.attack, this.is2014())
                if (spell == null) continue // not a spell

                if (currentScenario.spellsUsed.isEmpty()) { // no spells used, no conflict, just add it
                    spellsUsedThisTurn.add(spell)
                    continue
                }

                // if the last spell is still running, and new spell requires concentration, do not cast it ... TODO: refine this logic
                val lastSpell = currentScenario.spellsUsed.last()
                if ((lastSpell.getDuration() ?: 0) > 6 && spell.properties.Concentration == "Yes") {
//                    println("last spell = "+lastSpell.name+" dur = "+lastSpell.getDuration()
//                            +"skip spell: "+spell.name+", conc = "+spell.properties.Concentration)
                    return
                }
                if (!currentScenario.isSlotAvailable(this,spell)) {
                    // println("no slots available, skip: "+spell.name)
                    return
                }
                spellsUsedThisTurn.add(spell)
            }

            val spellsUsed = if (spellsUsedThisTurn.isEmpty()) currentScenario.spellsUsed
                             else currentScenario.spellsUsed.map { it.copy() } + spellsUsedThisTurn

            val nextScenario = Scenario (currentScenario.turns.map { it.copy() } + turn, spellsUsed)

            buildScenarios(rounds - 1, turnOptions, nextScenario, scenarioList)
        }
    }

    fun testScenarios() {
        val actionsAvailable = getActionsAvailable()
        val meleeTurnOptions = possibleTurns(actionsAvailable, true)
        val rangedTurnOptions = possibleTurns(actionsAvailable, false)

        val meleeScenarioList = ArrayList<Scenario>()
        buildScenarios(6, meleeTurnOptions, Scenario(emptyList(), emptyList()), meleeScenarioList)
        println("melee: scenarioList size = "+meleeScenarioList.size)

        val rangedScenarioList = ArrayList<Scenario>()
        buildScenarios(6, rangedTurnOptions, Scenario(emptyList(), emptyList()), rangedScenarioList)
        println("ranged: scenarioList size = "+rangedScenarioList.size)
    }

    fun runScenarios() {
        val actionsAvailable = getActionsAvailable()
/*
        val meleeTurnOptions = possibleTurns(actionsAvailable, true)
        val meleeScenarioList = ArrayList<Scenario>()
        buildScenarios(6, meleeTurnOptions, Scenario(emptyList(), emptyList()), meleeScenarioList)
        // System.err.println("melee: scenarioList size = "+meleeScenarioList.size)
        for (scenario in meleeScenarioList) {
            val scenarioResult = TurnCalculator(scenario.turns, this,EffectManager(ArrayList())).calculateDPRForAllTurns()
            System.err.println(String.format("%2.2f \t%s", scenarioResult.totalDPR, scenario.getLabel()))
        }
*/
        val rangedTurnOptions = possibleTurns(actionsAvailable, false)
        val rangedScenarioList = ArrayList<Scenario>()
        buildScenarios(6, rangedTurnOptions, Scenario(emptyList(), emptyList()), rangedScenarioList)
        // System.err.println("ranged: scenarioList size = "+rangedScenarioList.size)
        for (scenario in rangedScenarioList) {
            val scenarioResult = TurnCalculator(scenario.turns, this, EffectManager(ArrayList())).calculateDPRForAllTurns()
            System.err.println(String.format("%2.2f \t%s", scenarioResult.totalDPR, scenario.getLabel()))
        }
    }

}
