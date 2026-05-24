package com.vikinghelmet.dnd.dpr.monsters

import kotlinx.serialization.Serializable

@Serializable
data class Speed(
    val burrow: String? = null,
    val climb: String? = null,
    val hover: Boolean? = false,
    val fly: String? = null,
    val swim: String? = null,
    val walk: String? = null,
) {
    private fun format(addComma: Boolean, key: String, value: String?): String {
        if (value == null) return ""
        val prefix = if (addComma) ", " else ""
        val valueMinusFt = value.replace(" ft.".toRegex(), "") // all speeds are in ft, making it redundant
        return "${prefix}$key = $valueMinusFt"
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(format (builder.isNotEmpty(), "burrow", burrow))
        builder.append(format (builder.isNotEmpty(), "climb", climb))
        if (hover == true) builder.append(format (builder.isNotEmpty(), "hover", "$hover"))
        builder.append(format (builder.isNotEmpty(), "fly", fly))
        builder.append(format (builder.isNotEmpty(), "swim", swim))
        builder.append(format (builder.isNotEmpty(), "walk", walk))
        return builder.toString()
    }
}