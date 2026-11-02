package com.vikinghelmet.dnd.dpr.spells

class NoNameSpell(val level: Int)
    : Spell ("book", "noDescription", "noName",
            Properties("Instantaneous", "category", level, "school" ),
            "publisher")
{
}