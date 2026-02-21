package com.vikinghelmet.dnd.dpr.spells.payload

import com.vikinghelmet.dnd.dpr.spells.SaveResult
import com.vikinghelmet.dnd.dpr.spells.payload.fields.AreaOfEffect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Attack")
data class Attack(
    val name: String? = null,
    val description: String? = null,
    val save: Save? = null,
    val aoe: AreaOfEffect? = null,    //  "aoe":{"shape":"Sphere","size":.....
    val attack: Attack? = null,
    val actionType: String? = null,
    val autoHit: Boolean? = null,
    val onHitDisplay: String? = null,
    val range: String? = null,
    val repeat: Int? = null
)   : Payload()
{
    @Serializable
    data class Save (
        val saveAbility: String,        // "Wisdom",
        val onFail: String? = null,     // "onFail": "Take 5d8 Force damage.",
        val onSucceed: String? = null,  // "onSucceed": "Half as much damage."
    ) {
        fun getSaveResult(): SaveResult {
            return if (onSucceed == null) {
                SaveResult.NOT_APPLICABLE
            }
            else if (".*three times.*".toRegex().matches(onSucceed)) {
                SaveResult.NOT_APPLICABLE  // TODO: accumulated saves
            }
            else if (".*[Hh]alf.*amage.*".toRegex().matches(onSucceed)) {
                SaveResult.HALF_DAMAGE
            }
            else if (".*([Ss]pell.*[Ee]nds|[Ee]nds.*[Ss]pell).*".toRegex().matches(onSucceed)) {
                SaveResult.SPELL_ENDS
            }
            else if (".*([Cc]ondition.*[Ee]nd|[Ee]nd.*[Ss]pell|no longer).*".toRegex().matches(onSucceed)) {
                SaveResult.CONDITION_ENDS
            }
            else if (".*([Nn]o effect|unaffected|isn.t Restrained|resists your efforts|isn.t affected).*".toRegex().matches(onSucceed)) {
                SaveResult.NO_EFFECT
            }
            else {
                SaveResult.NOT_APPLICABLE
            }
        }
    }

    @Serializable
    data class Attack(
        val proficiencyLevel: String? = null,
        val type: String
    )
}