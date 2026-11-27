package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.irminsul.codex.api.AccountProfile

@Composable
fun HomeAccountOverviewSection(
    uid: String,
    serverName: String,
    accountProfile: AccountProfile?,
    rosterCount: Int?,
    exportedAt: String?,
    hasSnapshot: Boolean,
    onOpenAnalysis: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CodexSpacing.md)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(CodexSpacing.xl),
                horizontalArrangement = Arrangement.spacedBy(CodexSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatarUrl = accountProfile?.avatarUrl
                if (!avatarUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = avatarUrl,
                        contentDescription = "HoYoLAB profile avatar",
                        modifier = Modifier.size(72.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = { ProfileAvatarFallback(accountProfile.nickname) },
                        error = { ProfileAvatarFallback(accountProfile.nickname) }
                    )
                } else {
                    ProfileAvatarFallback(accountProfile?.nickname)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CodexSpacing.xs)
                ) {
                    Text(
                        text = "ADVENTURER PROFILE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = accountProfile?.nickname?.takeIf { it.isNotBlank() }
                            ?: if (uid.isNotBlank()) "Genshin Impact" else "No account connected",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (uid.isNotBlank()) {
                        Text(
                            text = "UID ${uid.trim()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formatHomeServerName(accountProfile?.serverName ?: serverName),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Text(
                            text = "Connect your HoYoLAB account from Account to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (hasSnapshot) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CodexSpacing.sm)
            ) {
                Text(
                    text = "ACCOUNT SNAPSHOT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(CodexSpacing.xxs)
                    ) {
                        Text(
                            text = rosterCount?.toString() ?: "—",
                            style = MaterialTheme.typography.headlineSmall.merge(CodexTabularNumericStyle),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (rosterCount == null) "Roster unavailable" else "Characters retrieved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onOpenAnalysis) { Text("View analysis") }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last retrieved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = exportedAt?.takeIf { it.isNotBlank() } ?: "Time unavailable",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Text(
                text = "No account snapshot yet. Retrieve your account data from Visuals to build your profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileAvatarFallback(nickname: String?) {
    Surface(
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = nickname?.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatHomeServerName(value: String): String {
    return when (value.trim().lowercase()) {
        "os_asia", "asia", "cn_gf01" -> "Asia Server"
        "os_usa", "america", "na" -> "America Server"
        "os_euro", "europe" -> "Europe Server"
        "os_cht", "tw", "hk", "mo" -> "TW, HK, MO Server"
        else -> if (value.isBlank()) "Server unavailable" else "$value Server"
    }
}
