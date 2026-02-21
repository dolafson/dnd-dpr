package com.vikinghelmet.dnd.dpr.spells

data class Save(
    val onFail: String,
    val onSucceed: String,
    val saveAbility: String
)