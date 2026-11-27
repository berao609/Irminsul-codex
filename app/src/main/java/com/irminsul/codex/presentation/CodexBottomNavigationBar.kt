package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CodexTab(val label: String) {
    HOME("Home"),
    VISUALS("Visuals"),
    TOOLS("Tools"),
    ACCOUNT("Account")
}

@Composable
fun CodexBottomNavigationBar(
    selectedTab: CodexTab,
    onTabSelected: (CodexTab) -> Unit,
    onOpenAi: () -> Unit
) {
    Box {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            NavigationBarItem(
                selected = selectedTab == CodexTab.HOME,
                onClick = { onTabSelected(CodexTab.HOME) },
                icon = { Icon(Icons.Outlined.DoorFront, contentDescription = "Home") },
                label = { Text("Home", fontSize = 10.sp) },
                alwaysShowLabel = true
            )
            NavigationBarItem(
                selected = selectedTab == CodexTab.VISUALS,
                onClick = { onTabSelected(CodexTab.VISUALS) },
                icon = { Icon(Icons.Outlined.InsertChart, contentDescription = "Visuals") },
                label = { Text("Visuals", fontSize = 10.sp) },
                alwaysShowLabel = true
            )
            Spacer(Modifier.weight(1f))
            NavigationBarItem(
                selected = selectedTab == CodexTab.TOOLS,
                onClick = { onTabSelected(CodexTab.TOOLS) },
                icon = { Icon(Icons.Outlined.Build, contentDescription = "Tools") },
                label = { Text("Tools", fontSize = 10.sp) },
                alwaysShowLabel = true
            )
            NavigationBarItem(
                selected = selectedTab == CodexTab.ACCOUNT,
                onClick = { onTabSelected(CodexTab.ACCOUNT) },
                icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Account") },
                label = { Text("Account", fontSize = 10.sp) },
                alwaysShowLabel = true
            )
        }
        FloatingActionButton(
            onClick = onOpenAi,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-18).dp),
            shape = RoundedCornerShape(50),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = "Open AI assistant")
        }
    }
}
