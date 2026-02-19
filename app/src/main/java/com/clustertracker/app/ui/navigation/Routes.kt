package com.clustertracker.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object CycleListRoute

@Serializable
data class CycleDetailRoute(val cycleId: Long)

@Serializable
data class CycleFormRoute(val cycleId: Long = 0L) // 0 = create new

@Serializable
data class ActiveAttackRoute(val attackId: Long, val cycleId: Long)

@Serializable
data class AttackDetailRoute(val attackId: Long)

@Serializable
data class EditAttackRoute(val attackId: Long)

@Serializable
data class ManualAttackRoute(val cycleId: Long)

@Serializable
object StatsRoute

@Serializable
object SettingsRoute
