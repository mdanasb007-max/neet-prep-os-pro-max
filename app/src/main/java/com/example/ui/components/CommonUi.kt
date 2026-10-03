package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeetTopBar(
    currentTab: AppTab,
    onNavigate: (AppTab) -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = MedicalSurface,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentTab != AppTab.HOME) {
                    IconButton(
                        onClick = { onNavigate(AppTab.HOME) },
                        modifier = Modifier.testTag("nav_back_home_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = CyanAccentGlow
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
                    Text(
                        text = "NEET PREP OS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = CyanAccentGlow,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "PRO MAX — MEDICAL ASPIRANT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("open_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun NeetBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = MedicalSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val navItems = listOf(
            Triple(AppTab.HOME, "Home", Icons.Default.Home),
            Triple(AppTab.ROUTINE, "Routine", Icons.Default.Checklist),
            Triple(AppTab.SYLLABUS, "Syllabus", Icons.Default.MenuBook),
            Triple(AppTab.TRACKER, "Tracker", Icons.Default.EditNote),
            Triple(AppTab.REVISION, "Revision", Icons.Default.Autorenew),
            Triple(AppTab.ANALYTICS, "Analytics", Icons.Default.BarChart)
        )

        navItems.forEach { (tab, label, icon) ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) CyanAccentGlow else TextMuted
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) CyanAccentGlow else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF0E3A53)
                ),
                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun SubjectBadge(
    subject: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (subject.lowercase()) {
        "physics" -> Pair(Color(0xFF1E3A8A), Color(0xFF93C5FD))
        "chemistry" -> Pair(Color(0xFF78350F), Color(0xFFFDE68A))
        "biology" -> Pair(Color(0xFF064E3B), Color(0xFFA7F3D0))
        "zoology" -> Pair(Color(0xFF831843), Color(0xFFFBCFE8))
        "botany" -> Pair(Color(0xFF134E4A), Color(0xFF99F6E4))
        else -> Pair(MedicalSurfaceVariant, TextSecondary)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = subject.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
