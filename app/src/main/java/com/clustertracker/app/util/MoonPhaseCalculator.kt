package com.clustertracker.app.util

import org.shredzone.commons.suncalc.MoonIllumination
import java.time.ZonedDateTime

data class MoonPhaseInfo(
    val phaseName: String,
    val phaseAngle: Double,
    val illuminationFraction: Double
)

object MoonPhaseCalculator {
    fun getMoonPhase(dateTime: ZonedDateTime, lat: Double, lon: Double): MoonPhaseInfo {
        val illumination = MoonIllumination.compute()
            .on(dateTime)
            .execute()

        val phase = illumination.phase
        val phaseName = when {
            phase >= 170 || phase < -170 -> "Full Moon"
            phase >= 80 -> "Waxing Gibbous"
            phase >= 10 -> "First Quarter"
            phase >= -10 -> "New Moon"
            phase >= -80 -> "Waxing Crescent"
            phase >= -170 -> "Waning Crescent"
            phase < -80 -> "Third Quarter"
            else -> "Waning Gibbous"
        }

        return MoonPhaseInfo(
            phaseName = phaseName,
            phaseAngle = phase,
            illuminationFraction = illumination.fraction
        )
    }
}
