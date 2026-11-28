package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
                icon = { AppIcon(AppIconName.HOME, contentDescription = "Home") },
                label = { Text("Home", fontSize = 10.sp) },
                alwaysShowLabel = true
            )
            NavigationBarItem(
                selected = selectedTab == CodexTab.VISUALS,
                onClick = { onTabSelected(CodexTab.VISUALS) },
                icon = { AppIcon(AppIconName.VISUALS, contentDescription = "Visuals") },
                label = { Text("Visuals", fontSize = 10.sp) },
                alwaysShowLabel = true
            )
            Spacer(Modifier.weight(1f))
            NavigationBarItem(
                selected = selectedTab == CodexTab.TOOLS,
                onClick = { onTabSelected(CodexTab.TOOLS) },
                icon = { AppIcon(AppIconName.TOOLS, contentDescription = "Tools") },
                label = { Text("Tools", fontSize = 10.sp) },
                alwaysShowLabel = true
            )
            NavigationBarItem(
                selected = selectedTab == CodexTab.ACCOUNT,
                onClick = { onTabSelected(CodexTab.ACCOUNT) },
                icon = { AppIcon(AppIconName.ACCOUNT, contentDescription = "Account") },
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
            AppIcon(AppIconName.AI_ASSISTANT, contentDescription = "Open AI assistant")
        }
    }
}
