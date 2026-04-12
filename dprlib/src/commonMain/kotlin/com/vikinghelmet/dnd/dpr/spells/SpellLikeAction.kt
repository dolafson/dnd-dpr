package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.character.actions.ActionAdded
import com.vikinghelmet.dnd.dpr.character.actions.ActionModifier
import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.character.stats.AbilityType
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffectShape

class SpellLikeAction(
    actionModifier: ActionModifier,
    actionAdded: ActionAdded,
    props: Properties
) : PreparedSpell (true, Spell("Fake Book 2014", actionAdded.description ?: "", actionAdded.name, props, ""))
{
    init {
        props.Damage = actionAdded.dice?.diceString // may not get used, but might as well

        val dataRecords: MutableList<DataDatarecord> = mutableListOf()
        var save: Attack.Save? = null
        var aoe: AreaOfEffect? = null
        var damageType: String? = null
        var range: String? = null

        if (actionAdded.saveStatId != null) {
            val abilityType = AbilityType.entries[actionAdded.saveStatId]
            save = Attack.Save(abilityType, actionAdded.saveFailDescription, actionAdded.saveSuccessDescription)
        }

        if (actionModifier == ActionModifier.BreathWeapon) {
            // in this case, AOE is too difficult to extract from character data ...
            // and we aren't really using AOE shape yet ...
            aoe = AreaOfEffect(AreaOfEffectShape.Cone, "15")
            damageType = actionAdded.name.substringAfterLast('(').substringBeforeLast(')')
            range = "30"
            props.dataRangeNum = 30
        }

        dataRecords.add(DataDatarecord("$name Attack", payload = Attack(
            name = actionAdded.name,
            description = actionAdded.description,
            save = save,
            aoe = aoe,
            range = range
        )))

        dataRecords.add(DataDatarecord("$name Damage", parent = "$name Attack", payload = Damage(
            ability = "none",
            damageType = damageType,
            diceCount = actionAdded.dice?.diceCount ?: 1,
            diceSize = "d${actionAdded.dice?.diceValue}"
        )))

        props.dataDatarecords = dataRecords
    }
}

/*
data.actions.race.0.limitedUse.useProficiencyBonus = true
data.actions.race.0.limitedUse.proficiencyBonusOperator = 1
data.actions.race.0.name = "Breath Weapon (Fire)"

data.actions.race.0.abilityModifierStatId = 3
data.actions.race.0.onMissDescription = ""
data.actions.race.0.saveFailDescription = ""

data.actions.race.0.saveSuccessDescription = "Half Damage"
data.actions.race.0.saveStatId = 2
data.actions.race.0.actionType = 3

data.actions.race.0.dice.diceCount = 1
data.actions.race.0.dice.diceValue = 10
data.actions.race.0.dice.diceString = "1d10"
 */
