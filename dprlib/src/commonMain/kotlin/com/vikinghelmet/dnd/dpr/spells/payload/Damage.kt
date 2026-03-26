package com.vikinghelmet.dnd.dpr.spells.payload

import com.vikinghelmet.dnd.dpr.spells.Spell
import com.vikinghelmet.dnd.dpr.util.DiceBlock
import com.vikinghelmet.dnd.dpr.util.DiceBlockHelper
import dev.shivathapaa.logger.api.LoggerFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("Damage")
data class Damage(
    private val ability: String?= null,
    private val damageType: String?= null,
    private val _bonus: Int? = null,
    private val diceCount: Int? = null, // TODO: if absent, usually a cantrip that increases based on char level ...
    private val diceSize: String? = null
) : Payload() {

    @Transient private val logger = LoggerFactory.get(Spell::class.simpleName ?: "")
    private var damageDice: DiceBlock? = null

    constructor(damageDice: DiceBlock) : this()
    {
        this.damageDice = damageDice
    }

    fun getDamageDice(): DiceBlock {
        logger.debug { "getDamageDice: this = $this" }
        return damageDice ?: DiceBlockHelper.get("${ (diceCount?: 1) }${diceSize}")
    }

}