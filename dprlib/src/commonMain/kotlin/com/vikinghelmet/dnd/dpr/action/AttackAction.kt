package com.vikinghelmet.dnd.dpr.action

interface AttackAction { // marker interface for Weapon and Spell
    fun getActionName(): String
}