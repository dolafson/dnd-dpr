package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import com.vikinghelmet.dnd.dpr.util.DiceBlock

// recharge is needed primarily for Dragon Breath Weapon; a few other monster attacks also use it
class Recharge(
    val dice: String ?= null,
    val min_value: Int ?= 0,
    val type: String,
    val times: Int ?= 0,
    val rest_types: List<String> = emptyList(),
) {
    fun rollForRecharge(): Boolean {
        return dice != null && min_value != null && DiceBlock(dice).roll() >= min_value
    }
}

class SavingThrowAction(
    name: String,
    description: String,
    save: Attack.Save,
    aoe: AreaOfEffect? = null,
    damageType: String? = null,
    diceString: String? = null,
    recharge: Recharge? = null,
) : PreparedSpell (true, Spell("Fake Book 2014", description, name, makeProps(), ""))
{
    var recharge: Recharge? = null
    fun rollForRecharge() =  recharge?.rollForRecharge() ?: false

    init {
        val recordList = mutableListOf(
            DataDatarecord("$name Attack", payload = Attack(
                name = name,
                description = description,
                save = save,
                aoe = aoe,
                range = properties.toString()
            )),
        )

        if (damageType != null && diceString != null) {
            val diceSplit = diceString.split("d")

            recordList.add(DataDatarecord(
                    "$name Damage", parent = "$name Attack", payload = Damage(
                        ability = "none",
                        damageType = damageType,
                        diceCount = diceSplit[0].toInt(),
                        diceSize = "d${ diceSplit[1] }"
                    )))
        }

        properties.dataDatarecords = recordList
        this.recharge = recharge
    }

    companion object {
        fun makeProps() = Properties("Instantaneous", "Spells", 0, "")
    }
}

