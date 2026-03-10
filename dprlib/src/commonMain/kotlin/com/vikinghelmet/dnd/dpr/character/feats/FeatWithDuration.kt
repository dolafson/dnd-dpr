package com.vikinghelmet.dnd.dpr.character.feats

import com.vikinghelmet.dnd.dpr.scenario.EffectWithDuration
import com.vikinghelmet.dnd.dpr.util.TargetEffect

data class FeatWithDuration(val feat: com.vikinghelmet.dnd.dpr.character.feats.Feat, val featDuration: Int, val effect: TargetEffect) : EffectWithDuration {
    override fun getDuration(): Int? {
        return featDuration
    }

    override fun getTargetEffect(): TargetEffect {
        return effect
    }

    override fun appliesEffectToNextTargetSaveOnly(): Boolean {
        return true // TODO: in the future may vary by feat
    }

    override fun appliesToNextMeleeOrRangeAttackOnly(): Boolean {
        return false // TODO: in the future may vary by feat
    }
}
