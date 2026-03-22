package com.vikinghelmet.dnd.dpr.character.spells

import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.Globals

class PreparedSpell(val alwaysPrepared: Boolean = false,
                    val from: Spell) : Spell(from.book, from.description, from.name, from.properties, from.publisher)
{
    constructor(name: String, is2014: Boolean) : this(false, Globals.getSpell(name, is2014))
}
