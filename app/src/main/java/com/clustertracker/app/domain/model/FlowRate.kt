package com.clustertracker.app.domain.model

/** Utility for converting between stored flow rate strings and Int L/min values. */
object FlowRate {
    const val DEFAULT = 15
    const val MIN = 6
    const val MAX = 25

    fun fromStorageString(s: String): Int =
        s.toIntOrNull() ?: when (s) {
            "Higher" -> 25
            else -> DEFAULT
        }

    fun toStorageString(lpm: Int): String = lpm.toString()

    fun displayLabel(lpm: Int): String = "$lpm L/min"
}
