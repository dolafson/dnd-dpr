package com.vikinghelmet.dnd.dpr.spells

import com.vikinghelmet.dnd.dpr.character.spells.PreparedSpell
import com.vikinghelmet.dnd.dpr.spells.payload.Attack
import com.vikinghelmet.dnd.dpr.spells.payload.Damage
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect

class SavingThrowAction(
    name: String,
    description: String,
    save: Attack.Save,
    aoe: AreaOfEffect,
    damageType: String,
    diceCount: Int,
    diceSize: String,
) : PreparedSpell (true, Spell("Fake Book 2014", description, name, makeProps(), ""))
{
    init {
        properties.dataDatarecords = listOf(
            DataDatarecord("$name Attack", payload = Attack(
                name = name,
                description = description,
                save = save,
                aoe = aoe,
                range = properties.toString()
            )),
            DataDatarecord("$name Damage", parent = "$name Attack", payload = Damage(
                ability = "none",
                damageType = damageType,
                diceCount = diceCount,
                diceSize = "d$diceSize"
            ))
        )
    }

    companion object {
        fun makeProps() = Properties("Instantaneous", "Spells", 0, "")
    }
}

