package com.booksearch.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.booksearch.app.feature.home.HomeScreen
import com.booksearch.app.feature.onboarding.OnboardingScreen

@Composable
fun AppNavHost(startDestination: Any) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(HomeRoute) {
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                },
            )
        }
        composable<HomeRoute> {
            HomeScreen(
                onBookClick = { workId -> navController.navigate(DetailRoute(workId)) },
                onSettingsClick = { navController.navigate(SettingsRoute) },
            )
        }
        // Detail and Settings destinations are registered in the next steps.
        composable<DetailRoute> {}
        composable<SettingsRoute> {}
    }
}
