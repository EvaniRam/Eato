package com.evani.homechefai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evani.homechefai.data.PreferencesManager
import com.evani.homechefai.ui.screens.baking.BakingScreen
import com.evani.homechefai.ui.screens.settings.SettingsScreen
import com.evani.homechefai.ui.screens.splash.SplashScreen
import com.evani.homechefai.ui.theme.HomeChefAITheme
import com.evani.homechefai.viewmodel.BakingViewModel
import com.evani.homechefai.viewmodel.BakingViewModelFactory

@OptIn(ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesManager = PreferencesManager(applicationContext)
        
        setContent {
            HomeChefAITheme(dynamicColor = false) {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
                val viewModel: BakingViewModel = viewModel(
                    factory = BakingViewModelFactory(preferencesManager)
                )

                AnimatedContent(targetState = currentScreen) { screen ->
                    when (screen) {
                        Screen.Splash -> SplashScreen {
                            currentScreen = Screen.Baking
                        }
                        Screen.Baking -> BakingScreen(
                            onNavigateToSettings = { currentScreen = Screen.Settings },
                            bakingViewModel = viewModel
                        )
                        Screen.Settings -> SettingsScreen(
                            onNavigateBack = { currentScreen = Screen.Baking },
                            onSaveSettings = { preferences ->
                                viewModel.updatePreferences(preferences)
                            },
                            preferencesManager = preferencesManager
                        )
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Splash : Screen()
    object Baking : Screen()
    object Settings : Screen()
}