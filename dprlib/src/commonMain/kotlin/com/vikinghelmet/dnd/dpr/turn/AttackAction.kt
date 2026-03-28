package com.vikinghelmet.dnd.dpr.turn

interface AttackAction { // marker interface for Weapon and Spell
    fun getActionName(): String
}