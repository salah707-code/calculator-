package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.model.AccentColor
import com.example.model.AppTheme
import com.example.ui.CalculatorViewModel
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CalculatorTheme

enum class CurrentDestination {
    CALCULATOR,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val accentColor by viewModel.accentColor.collectAsState()

            CalculatorTheme(appTheme = themeMode, accentColor = accentColor) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: CalculatorViewModel) {
    var currentDestination by remember { mutableStateOf(CurrentDestination.CALCULATOR) }

    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val thousandsSeparatorEnabled by viewModel.thousandsSeparatorEnabled.collectAsState()

    BackHandler(enabled = currentDestination != CurrentDestination.CALCULATOR) {
        currentDestination = CurrentDestination.CALCULATOR
    }

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            if (targetState != CurrentDestination.CALCULATOR) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width / 3 } + fadeOut()
                )
            } else {
                (slideInHorizontally { width -> -width / 3 } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut()
                )
            }
        },
        label = "screen_transition",
        modifier = Modifier.fillMaxSize()
    ) { destination ->
        when (destination) {
            CurrentDestination.CALCULATOR -> {
                CalculatorScreen(
                    state = uiState,
                    currentTheme = themeMode,
                    onKeyClick = { key -> viewModel.onKeyClicked(key) },
                    onOpenHistory = { currentDestination = CurrentDestination.HISTORY },
                    onOpenSettings = { currentDestination = CurrentDestination.SETTINGS },
                    onToggleTheme = {
                        val nextTheme = when (themeMode) {
                            AppTheme.SYSTEM -> AppTheme.LIGHT
                            AppTheme.LIGHT -> AppTheme.DARK
                            AppTheme.DARK -> AppTheme.SYSTEM
                        }
                        viewModel.setTheme(nextTheme)
                    }
                )
            }
            CurrentDestination.HISTORY -> {
                HistoryScreen(
                    historyList = historyList,
                    onBack = { currentDestination = CurrentDestination.CALCULATOR },
                    onUseResult = { result ->
                        viewModel.reuseResult(result)
                        currentDestination = CurrentDestination.CALCULATOR
                    },
                    onDeleteSingle = { item -> viewModel.deleteHistoryItem(item) },
                    onClearAll = { viewModel.clearAllHistory() }
                )
            }
            CurrentDestination.SETTINGS -> {
                SettingsScreen(
                    currentTheme = themeMode,
                    currentAccentColor = accentColor,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    thousandsSeparatorEnabled = thousandsSeparatorEnabled,
                    onThemeChange = { theme -> viewModel.setTheme(theme) },
                    onAccentColorChange = { accent -> viewModel.setAccentColor(accent) },
                    onHapticChange = { enabled -> viewModel.setHaptic(enabled) },
                    onSoundChange = { enabled -> viewModel.setSound(enabled) },
                    onThousandsSeparatorChange = { enabled -> viewModel.setThousandsSeparator(enabled) },
                    onClearAllHistory = { viewModel.clearAllHistory() },
                    onBack = { currentDestination = CurrentDestination.CALCULATOR }
                )
            }
        }
    }
}
