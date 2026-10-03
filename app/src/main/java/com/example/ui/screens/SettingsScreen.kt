package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.NeetViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: NeetViewModel,
    onNavigate: (AppTab) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showResetTodayDialog by remember { mutableStateOf(false) }
    var showResetAllDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportJsonText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "NEET PREP OS SETTINGS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = CyanAccentGlow
            )
            Text(
                text = "Offline local configuration & data management",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // Section: Study & Routine Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ROUTINE & TIMERS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccentGlow
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsRowButton(
                        title = "Configure 10 Daily Routine Blocks",
                        subtitle = "Target: 390 Questions • 510 Mins (8h 30m)",
                        icon = Icons.Default.Checklist,
                        testTag = "settings_routine_btn"
                    ) {
                        onNavigate(AppTab.ROUTINE)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsRowButton(
                        title = "Pomodoro & Custom Study Timer",
                        subtitle = "25m Study / 5m Short Break / 15m Long Break",
                        icon = Icons.Default.Timer,
                        testTag = "settings_pomodoro_btn"
                    ) {
                        onNavigate(AppTab.POMODORO)
                    }
                }
            }
        }

        // Section: Backup & Restore
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BACKUP & RESTORE (LOCAL JSON)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                    Text(
                        text = "Export and import your routine, sessions, syllabus progress, and mistake book safely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.exportDataJson()
                                    exportJsonText = json
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("NEET Prep Backup", json))
                                    Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_backup_btn")
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_backup_btn"),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(EmeraldSuccess)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = EmeraldSuccess)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Reset Data
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RESET PROGRESS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonError
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showResetTodayDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_today_btn")
                    ) {
                        Text("Reset Today's Progress (Keeps History)", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showResetAllDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CrimsonError)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_all_btn")
                    ) {
                        Text("Reset All Data & Repopulate", color = CrimsonError, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: App info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NEET PREP OS — PRO MAX",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = CyanAccentGlow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0 (PRO MAX Native Edition)\n100% Offline • Single Source of Truth: Room Database\nFull NCERT NEET Syllabus • Android AppWidget",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    // Reset Today Dialog
    if (showResetTodayDialog) {
        AlertDialog(
            onDismissRequest = { showResetTodayDialog = false },
            title = { Text("Reset Today's Progress?") },
            text = {
                Text("This resets today's routine block checkboxes and today's logged sessions. Previous days' history remains completely safe.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetTodayProgress()
                        showResetTodayDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
                ) {
                    Text("Reset Today", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetTodayDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Reset All Dialog
    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text("RESET ALL DATA?") },
            text = {
                Text("Are you sure? This will wipe all recorded study sessions, questions, mistakes, and reset syllabus progress. NEET default syllabus and routine will be restored.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
                ) {
                    Text("Wipe & Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        var inputJson by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Backup JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste your exported JSON backup text below:")
                    OutlinedTextField(
                        value = inputJson,
                        onValueChange = { inputJson = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val ok = viewModel.importDataJson(inputJson)
                            if (ok) {
                                Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to restore backup (invalid JSON)", Toast.LENGTH_SHORT).show()
                            }
                            showImportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("Restore", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsRowButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MedicalSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CyanAccentGlow, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
