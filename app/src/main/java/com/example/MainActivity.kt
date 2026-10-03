package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.NeetBottomBar
import com.example.ui.components.NeetTopBar
import com.example.ui.screens.*
import com.example.ui.theme.NeetPrepTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.NeetViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NeetPrepTheme {
                val viewModel: NeetViewModel = viewModel()
                val currentTab by viewModel.currentTab.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Request Notification permission on Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Handle result gracefully */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Show Snackbar for events
                LaunchedEffect(Unit) {
                    viewModel.messageEvent.collectLatest { msg ->
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                // Back button handling
                BackHandler(enabled = currentTab != AppTab.HOME) {
                    viewModel.navigateTo(AppTab.HOME)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        NeetTopBar(
                            currentTab = currentTab,
                            onNavigate = { viewModel.navigateTo(it) },
                            onOpenSettings = { viewModel.navigateTo(AppTab.SETTINGS) }
                        )
                    },
                    bottomBar = {
                        NeetBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.navigateTo(it) }
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            AppTab.ROUTINE -> RoutineScreen(
                                viewModel = viewModel
                            )
                            AppTab.POMODORO -> PomodoroScreen(
                                viewModel = viewModel
                            )
                            AppTab.TRACKER -> TrackerScreen(
                                viewModel = viewModel
                            )
                            AppTab.SYLLABUS -> SyllabusScreen(
                                viewModel = viewModel,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            AppTab.REVISION -> RevisionScreen(
                                viewModel = viewModel
                            )
                            AppTab.MISTAKE_BOOK -> MistakeScreen(
                                viewModel = viewModel
                            )
                            AppTab.ANALYTICS -> AnalyticsScreen(
                                viewModel = viewModel
                            )
                            AppTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
