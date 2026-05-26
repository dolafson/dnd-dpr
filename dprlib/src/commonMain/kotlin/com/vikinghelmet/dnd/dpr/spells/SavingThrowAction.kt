package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect

class SavingThrowAction(
    name: String,
    description: String,
    save: Attack.Save,
    aoe: AreaOfEffect? = null,
    damageType: String? = null,
    diceString: String? = null,
) : PreparedSpell (true, Spell("Fake Book 2014", description, name, makeProps(), ""))
{
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
    }

    companion object {
        fun makeProps() = Properties("Instantaneous", "Spells", 0, "")
    }
}

