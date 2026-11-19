package com.booksearch.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.booksearch.app.navigation.AppNavHost
import com.booksearch.app.navigation.HomeRoute
import com.booksearch.app.navigation.OnboardingRoute
import com.booksearch.app.ui.theme.BookSearchTheme
import com.booksearch.app.ui.theme.isDarkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep the splash until preferences are known, so we never flash the wrong theme or screen.
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val ready = uiState as? MainUiState.Ready ?: return@setContent

            // Decided once: later preference changes must not rebuild the navigation graph.
            val startDestination = remember {
                if (ready.preferences.onboardingCompleted) HomeRoute else OnboardingRoute
            }

            // System bar icons must follow the app theme, which may differ from the system one.
            val darkTheme = ready.preferences.themeMode.isDarkTheme()
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                )
                onDispose {}
            }

            BookSearchTheme(
                palette = ready.preferences.colorPalette,
                themeMode = ready.preferences.themeMode,
            ) {
                AppNavHost(startDestination = startDestination)
            }
        }
    }
}
