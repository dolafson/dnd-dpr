package com.vikinghelmet.dnd.dpr.character.api

data class ApiRequestParameters(
    val campaignId: Int? = null,
    val backgroundId: Int? = null,
    val classId: Int? = null,
    val classLevel: Int,
)
{
    fun isIncomplete(): Boolean = (campaignId == null || backgroundId == null || classId == null)
}
