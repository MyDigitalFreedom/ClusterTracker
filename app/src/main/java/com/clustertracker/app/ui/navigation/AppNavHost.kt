package com.clustertracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.clustertracker.app.ui.attack.ActiveAttackScreen
import com.clustertracker.app.ui.attack.AttackDetailScreen
import com.clustertracker.app.ui.attack.EditAttackScreen
import com.clustertracker.app.ui.attack.ManualAttackFormScreen
import com.clustertracker.app.ui.cycles.CycleDetailScreen
import com.clustertracker.app.ui.cycles.CycleFormScreen
import com.clustertracker.app.ui.cycles.CycleListScreen
import com.clustertracker.app.ui.settings.SettingsScreen
import com.clustertracker.app.ui.stats.StatsScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = CycleListRoute) {
        composable<CycleListRoute> {
            CycleListScreen(
                onCycleClick = { cycleId ->
                    navController.navigate(CycleDetailRoute(cycleId))
                },
                onCreateCycle = {
                    navController.navigate(CycleFormRoute())
                },
                onActiveAttackClick = { attackId, cycleId ->
                    navController.navigate(ActiveAttackRoute(attackId, cycleId))
                },
                onSettingsClick = {
                    navController.navigate(SettingsRoute)
                },
                onStatsClick = {
                    navController.navigate(StatsRoute)
                }
            )
        }

        composable<CycleDetailRoute> {
            CycleDetailScreen(
                onBack = { navController.popBackStack() },
                onEditCycle = { cycleId ->
                    navController.navigate(CycleFormRoute(cycleId))
                },
                onAttackClick = { attackId ->
                    navController.navigate(AttackDetailRoute(attackId))
                },
                onStartAttack = { attackId, cycleId ->
                    navController.navigate(ActiveAttackRoute(attackId, cycleId))
                },
                onManualAttack = { cycleId ->
                    navController.navigate(ManualAttackRoute(cycleId))
                }
            )
        }

        composable<CycleFormRoute> {
            CycleFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<ActiveAttackRoute> {
            ActiveAttackScreen(
                onAttackEnded = { attackId ->
                    navController.popBackStack()
                    navController.navigate(AttackDetailRoute(attackId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<ManualAttackRoute> {
            ManualAttackFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<AttackDetailRoute> {
            AttackDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { attackId ->
                    navController.navigate(EditAttackRoute(attackId))
                }
            )
        }

        composable<EditAttackRoute> {
            EditAttackScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<StatsRoute> {
            StatsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
