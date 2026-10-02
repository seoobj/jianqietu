package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BatchEditorScreen
import com.example.ui.screens.BatchResultScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExportPreviewScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.Screen
import com.example.viewmodel.SliceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SliceViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isDark = uiState.isDarkMode ?: isSystemInDarkTheme()

            MyApplicationTheme(
                palette = uiState.appThemePalette,
                darkTheme = isDark
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ImageSlicerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ImageSlicerApp(
    viewModel: SliceViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = uiState.currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is Screen.Home -> HomeScreen(viewModel = viewModel, uiState = uiState)
            is Screen.Editor -> EditorScreen(viewModel = viewModel, uiState = uiState)
            is Screen.ResultPreview -> ExportPreviewScreen(viewModel = viewModel, uiState = uiState)
            is Screen.BatchEditor -> BatchEditorScreen(viewModel = viewModel, uiState = uiState)
            is Screen.BatchResult -> BatchResultScreen(viewModel = viewModel, uiState = uiState)
        }
    }
}
