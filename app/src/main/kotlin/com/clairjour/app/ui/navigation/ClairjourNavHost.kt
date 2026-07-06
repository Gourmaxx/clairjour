package com.clairjour.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.clairjour.app.data.AppContainer
import com.clairjour.app.ui.screen.addiction.AddictionDetailScreen
import com.clairjour.app.ui.screen.addiction.AddictionEditScreen
import com.clairjour.app.ui.screen.home.HomeScreen
import com.clairjour.app.ui.screen.journal.JournalEditorScreen
import com.clairjour.app.ui.screen.journal.JournalScreen
import com.clairjour.app.ui.screen.onboarding.OnboardingScreen
import com.clairjour.app.ui.screen.settings.SettingsScreen
import com.clairjour.app.ui.screen.stats.StatsScreen

@Composable
fun ClairjourNavHost(
    navController: NavHostController,
    container: AppContainer,
    startDestination: String,
    contentPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.ONBOARDING) {
            OnboardingScreen(
                container = container,
                onDone = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Destinations.HOME) {
            HomeScreen(
                container = container,
                contentPadding = contentPadding,
                onOpenAddiction = { navController.navigate(Destinations.addictionDetail(it)) },
                onAddAddiction = { navController.navigate(Destinations.addictionEdit()) },
                onOpenJournalEditor = { navController.navigate(Destinations.JOURNAL_EDITOR) }
            )
        }
        composable(Destinations.JOURNAL) {
            JournalScreen(
                container = container,
                contentPadding = contentPadding,
                onOpenEditor = { navController.navigate(Destinations.JOURNAL_EDITOR) }
            )
        }
        composable(Destinations.JOURNAL_EDITOR) {
            JournalEditorScreen(
                container = container,
                onDone = { navController.popBackStack() }
            )
        }
        composable(Destinations.STATS) {
            StatsScreen(
                container = container,
                contentPadding = contentPadding
            )
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(
                container = container,
                contentPadding = contentPadding,
                onEditAddiction = { navController.navigate(Destinations.addictionEdit(it)) },
                onAddAddiction = { navController.navigate(Destinations.addictionEdit()) }
            )
        }
        composable(
            route = Destinations.ADDICTION_DETAIL,
            arguments = listOf(navArgument("addictionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("addictionId") ?: return@composable
            AddictionDetailScreen(
                container = container,
                addictionId = id,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Destinations.addictionEdit(id)) }
            )
        }
        composable(
            route = Destinations.ADDICTION_EDIT,
            arguments = listOf(navArgument("addictionId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("addictionId")
            AddictionEditScreen(
                container = container,
                addictionId = id,
                onDone = { navController.popBackStack() }
            )
        }
    }
}
