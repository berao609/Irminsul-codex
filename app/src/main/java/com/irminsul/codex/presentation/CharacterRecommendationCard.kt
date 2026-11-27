package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.CharacterDevelopmentRecommendationType
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.GoalAwareCharacterRecommendation

@Composable
private fun CharacterAvatarFallback(symbol: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(76.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, fontSize = 30.sp)
        }
    }
}

@Composable
fun CharacterRecommendationCard(
    item: GoalAwareCharacterRecommendation,
    character: CharacterSnapshot?,
    expanded: Boolean
) {
    val recommendation = item.recommendation
    val name = recommendation.characterName ?: "Character"
    val artifactGap = recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW &&
        recommendation.evidence.any { it.contains("Artifact collection", ignoreCase = true) }
    val title = when {
        artifactGap -> "Check $name's artifacts"
        recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW -> "Check $name's character details"
        recommendation.type == CharacterDevelopmentRecommendationType.BUILD_ERRORS -> "Fix a build issue"
        recommendation.type == CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> "Check $name's stats"
        recommendation.type == CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> "Review $name's build"
        else -> recommendation.title
    }
    val artifactCount = (character?.artifacts as? DataValue.Available)?.value?.size
        ?: character?.equipmentIntegrity?.artifactSourceEntryCount
    val explanation = when {
        artifactGap && artifactCount != null ->
            "We found $artifactCount of 5 equipped artifact entries in your account data."
        artifactGap ->
            "We couldn't confirm all five equipped artifacts from your account data."
        recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW ->
            "Some information for $name is missing, so we can't fully check this character yet."
        else -> recommendation.reason
    }
    val nextStep = when {
        artifactGap ->
            "Refresh your account data, then check whether all five artifacts appear in $name's character details."
        recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW ->
            "Refresh your account data and check that $name's character details are available."
        else -> recommendation.action
    }
    val priorityLabel = when (recommendation.priority.name) {
        "P1" -> "Urgent"
        "P2" -> "High priority"
        "P3" -> "Review"
        else -> "Information needed"
    }
    val elementLabel = character?.element.orEmpty()
    val elementSymbol = when (elementLabel.lowercase()) {
        "wind", "anemo" -> "🌪️"
        "fire", "pyro" -> "🔥"
        "water", "hydro" -> "💧"
        "ice", "cryo" -> "❄️"
        "electric", "electro" -> "⚡"
        "rock", "geo" -> "🪨"
        "grass", "dendro" -> "🌿"
        else -> name.firstOrNull()?.uppercase() ?: "?"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!character?.avatarIcon.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = character?.avatarIcon,
                    contentDescription = "$name portrait",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)),
                    loading = { CharacterAvatarFallback(elementSymbol) },
                    error = { CharacterAvatarFallback(elementSymbol) }
                )
            } else {
                CharacterAvatarFallback(elementSymbol)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            priorityLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(explanation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("What to do", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(nextStep, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (expanded) {
                    if (artifactGap || recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW) {
                        Text(
                            "This is an information check, not a rating of how strong this character is.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(recommendation.reason, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        recommendation.evidence.forEach { evidence ->
                            Text("• $evidence", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (recommendation.basedOnStaleData) {
                        Text(
                            "Your account data may be out of date. Refresh it to confirm this finding.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
