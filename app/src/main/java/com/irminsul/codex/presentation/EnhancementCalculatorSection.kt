package com.irminsul.codex.presentation

import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.irminsul.codex.data.catalogue.BundledCharacterProgressionCandidateRepository
import com.irminsul.codex.data.catalogue.BundledEnhancementCatalogueRepository
import com.irminsul.codex.data.catalogue.BundledWeaponAscensionCandidateRepository
import com.irminsul.codex.data.catalogue.CharacterFullUpgradePlanCandidateCalculator
import com.irminsul.codex.data.catalogue.CharacterTalentLevelRange
import com.irminsul.codex.data.catalogue.CharacterUpgradePlanCandidateCalculator
import com.irminsul.codex.data.catalogue.CharacterUpgradePlanCandidateRequest
import com.irminsul.codex.data.catalogue.CharacterUpgradePlanCandidateStatus
import com.irminsul.codex.data.catalogue.EnhancementCatalogueEntry
import com.irminsul.codex.data.catalogue.EnhancementSubjectType
import com.irminsul.codex.data.catalogue.GenshinCharacterExperienceCurveCandidate
import com.irminsul.codex.data.catalogue.GenshinWeaponExperienceCurveCandidate
import com.irminsul.codex.data.catalogue.WeaponUpgradePlanCandidateCalculator
import com.irminsul.codex.data.catalogue.WeaponUpgradePlanCandidateRequest
import com.irminsul.codex.data.catalogue.WeaponUpgradePlanCandidateStatus
import com.irminsul.codex.domain.analysis.WeaponExperienceRarity
import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.DataValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.compose.SubcomposeAsyncImage

private enum class CalculatorPage { CHOOSE_TYPE, CHOOSE_ITEM, PLAN }
private data class CalculatorMaterial(val id: String, val name: String, val required: Long, val iconUrl: String)
private data class CalculatorResult(val materials: List<CalculatorMaterial>, val issue: String? = null)
private val calculatorLevelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

@Composable
internal fun EnhancementCalculatorSection(
    characters: List<CharacterSnapshot>,
    preferences: SharedPreferences
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf(CalculatorPage.CHOOSE_TYPE) }
    var subjectType by remember { mutableStateOf(EnhancementSubjectType.CHARACTER) }
    var catalogue by remember { mutableStateOf<List<EnhancementCatalogueEntry>>(emptyList()) }
    var filterIconMetadata by remember { mutableStateOf<com.irminsul.codex.data.catalogue.EnhancementFilterIconMetadata?>(null) }
    var loading by remember { mutableStateOf(true) }
    var catalogueError by remember { mutableStateOf<String?>(null) }
    var selectedElementFilter by remember { mutableStateOf<String?>(null) }
    var selectedWeaponTypeFilter by remember { mutableStateOf<String?>(null) }
    var selectedRarityFilter by remember { mutableStateOf<Int?>(null) }
    var selected by remember { mutableStateOf<EnhancementCatalogueEntry?>(null) }
    var currentLevel by remember { mutableIntStateOf(1) }
    var targetLevel by remember { mutableIntStateOf(90) }
    var currentRank by remember { mutableIntStateOf(0) }
    var normalCurrent by remember { mutableIntStateOf(1) }
    var normalTarget by remember { mutableIntStateOf(1) }
    var skillCurrent by remember { mutableIntStateOf(1) }
    var skillTarget by remember { mutableIntStateOf(1) }
    var burstCurrent by remember { mutableIntStateOf(1) }
    var burstTarget by remember { mutableIntStateOf(1) }
    var calculating by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<CalculatorResult?>(null) }
    var owned by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    BackHandler(enabled = page != CalculatorPage.CHOOSE_TYPE && result == null) {
        when (page) {
            CalculatorPage.PLAN -> {
                page = CalculatorPage.CHOOSE_ITEM
                result = null
            }
            CalculatorPage.CHOOSE_ITEM -> page = CalculatorPage.CHOOSE_TYPE
            CalculatorPage.CHOOSE_TYPE -> Unit
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val loaded = withContext(Dispatchers.IO) {
                BundledEnhancementCatalogueRepository.loadAll(context) to
                    BundledEnhancementCatalogueRepository.loadFilterIconMetadata(context)
            }
            catalogue = loaded.first
            filterIconMetadata = loaded.second
            catalogueError = null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            catalogueError = failure.message ?: "The bundled game catalogue could not be loaded."
        } finally {
            loading = false
        }
    }

    val choices = remember(catalogue, subjectType, selectedElementFilter, selectedWeaponTypeFilter, selectedRarityFilter) {
        catalogue.filter { entry ->
            entry.subjectType == subjectType &&
                (selectedElementFilter == null || entry.element == selectedElementFilter) &&
                (selectedWeaponTypeFilter == null || entry.weaponType == selectedWeaponTypeFilter) &&
                (selectedRarityFilter == null || entry.rarity == selectedRarityFilter)
        }
    }

    fun selectItem(entry: EnhancementCatalogueEntry) {
        selected = entry
        val ownedLevel = when (entry.subjectType) {
            EnhancementSubjectType.CHARACTER ->
                characters.firstOrNull { it.id.toString() == entry.id }?.level
            EnhancementSubjectType.WEAPON ->
                characters.firstNotNullOfOrNull { character ->
                    when (val weapon = character.weapon) {
                        is DataValue.Available -> weapon.value.takeIf { it.id.toString() == entry.id }?.level
                        is DataValue.Stale -> weapon.value.takeIf { it.id.toString() == entry.id }?.level
                        else -> null
                    }
                }
        }
        currentLevel = (ownedLevel ?: 1).coerceIn(1, 90)
        targetLevel = 90
        val rankFloor = calculatorLevelCaps.indexOfFirst { currentLevel <= it }.coerceAtLeast(0)
        val rankCeiling = if (currentLevel == calculatorLevelCaps[rankFloor]) {
            (rankFloor + 1).coerceAtMost(6)
        } else rankFloor
        val accountAscensionRank = when (entry.subjectType) {
            EnhancementSubjectType.CHARACTER ->
                characters.firstOrNull { it.id.toString() == entry.id }?.ascensionLevel
            EnhancementSubjectType.WEAPON ->
                characters.firstNotNullOfOrNull { character ->
                    when (val weapon = character.weapon) {
                        is DataValue.Available -> weapon.value.takeIf { it.id.toString() == entry.id }?.ascensionLevel
                        is DataValue.Stale -> weapon.value.takeIf { it.id.toString() == entry.id }?.ascensionLevel
                        else -> null
                    }
                }
        }
        currentRank = (accountAscensionRank ?: rankFloor).coerceIn(rankFloor, rankCeiling)
        val skills = characters.firstOrNull { it.id.toString() == entry.id }?.skills.orEmpty()
        normalCurrent = skills.firstOrNull { it.type == 1 }?.level?.coerceIn(1, 10) ?: 1
        skillCurrent = skills.firstOrNull { it.type == 2 }?.level?.coerceIn(1, 10) ?: 1
        burstCurrent = skills.firstOrNull { it.type == 3 }?.level?.coerceIn(1, 10) ?: 1
        normalTarget = normalCurrent
        skillTarget = skillCurrent
        burstTarget = burstCurrent
        result = null
        owned = emptyMap()
        page = CalculatorPage.PLAN
    }

    fun calculate() {
        val entry = selected ?: return
        calculating = true
        result = null
        scope.launch {
            val calculated = try {
                withContext(Dispatchers.IO) {
                    if (entry.subjectType == EnhancementSubjectType.CHARACTER) {
                        val imported = BundledCharacterProgressionCandidateRepository.loadCandidates(context, entry.id)
                            ?: return@withContext CalculatorResult(emptyList(), "No progression data is available for this character.")
                        val levelPlan = CharacterUpgradePlanCandidateCalculator.calculate(
                            CharacterUpgradePlanCandidateRequest(
                                characterId = entry.id,
                                gameVersion = BundledCharacterProgressionCandidateRepository.GAME_VERSION,
                                currentLevel = currentLevel,
                                currentExperienceWithinLevel = 0L,
                                currentAscensionRank = currentRank,
                                targetLevel = targetLevel
                            ),
                            GenshinCharacterExperienceCurveCandidate.current.cumulativeExperienceByLevel,
                            imported.candidates
                        )
                        val fullPlan = CharacterFullUpgradePlanCandidateCalculator.calculate(
                            levelPlan,
                            entry.id,
                            BundledCharacterProgressionCandidateRepository.GAME_VERSION,
                            listOf(
                                CharacterTalentLevelRange("normal", normalCurrent, normalTarget),
                                CharacterTalentLevelRange("skill", skillCurrent, skillTarget),
                                CharacterTalentLevelRange("burst", burstCurrent, burstTarget)
                            ),
                            imported.candidates
                        )
                        if (fullPlan.status != CharacterUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA) {
                            CalculatorResult(emptyList(), fullPlan.issue ?: "Complete character cost data is unavailable.")
                        } else CalculatorResult(fullPlan.requirements.map {
                            CalculatorMaterial(it.itemId, it.itemName, it.quantity, calculatorMaterialIconUrl(it.itemId))
                        })
                    } else {
                        val rarity = if (entry.rarity == 4) WeaponExperienceRarity.FOUR_STAR
                            else WeaponExperienceRarity.FIVE_STAR
                        val candidates = BundledWeaponAscensionCandidateRepository.loadCandidates(context, entry.id)
                        val curve = if (entry.rarity == 4) GenshinWeaponExperienceCurveCandidate.fourStar
                            else GenshinWeaponExperienceCurveCandidate.fiveStar
                        val plan = WeaponUpgradePlanCandidateCalculator.calculate(
                            WeaponUpgradePlanCandidateRequest(
                                weaponId = entry.id,
                                gameVersion = BundledWeaponAscensionCandidateRepository.GAME_VERSION,
                                rarity = rarity,
                                currentLevel = currentLevel,
                                currentExperienceWithinLevel = 0L,
                                currentAscensionRank = currentRank,
                                targetLevel = targetLevel
                            ),
                            curve.cumulativeExperienceByLevel,
                            candidates
                        )
                        if (plan.status != WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA) {
                            CalculatorResult(emptyList(), plan.issue ?: "Complete weapon cost data is unavailable.")
                        } else CalculatorResult(plan.requirements.map {
                            CalculatorMaterial(it.itemId, it.itemName, it.quantity, calculatorMaterialIconUrl(it.itemId))
                        })
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                CalculatorResult(emptyList(), failure.message ?: "The calculation failed.")
            }
            result = calculated
            calculating = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        when (page) {
            CalculatorPage.CHOOSE_TYPE -> {
                Text("Enhancement Calculator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Choose a character or weapon. The catalogue includes all supported 4-star and 5-star game items, not only items on your account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CalculatorTypeCard("Character", "Character level, ascension, and talent materials") {
                    subjectType = EnhancementSubjectType.CHARACTER
                                page = CalculatorPage.CHOOSE_ITEM
                }
                CalculatorTypeCard("Weapon", "Weapon level and ascension materials") {
                    subjectType = EnhancementSubjectType.WEAPON
                                page = CalculatorPage.CHOOSE_ITEM
                }
            }
            CalculatorPage.CHOOSE_ITEM -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { page = CalculatorPage.CHOOSE_TYPE }) { Text("‹ Back") }
                    Text(if (subjectType == EnhancementSubjectType.CHARACTER) "Choose Character" else "Choose Weapon", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                CalculatorRarityFilters(selectedRarityFilter) { selectedRarityFilter = it }
                if (subjectType == EnhancementSubjectType.CHARACTER) {
                    CalculatorIconFilterRow("Element", calculatorElementFilterOptions(filterIconMetadata?.elementIcons.orEmpty()), selectedElementFilter) {
                        selectedElementFilter = it
                    }
                }
                CalculatorIconFilterRow("Weapon type", calculatorWeaponTypeFilterOptions(filterIconMetadata?.weaponTypeIcons.orEmpty()), selectedWeaponTypeFilter) {
                    selectedWeaponTypeFilter = it
                }
                Text(choices.size.toString() + " available · 4-star and 5-star", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                when {
                    loading -> CircularProgressIndicator()
                    catalogueError != null -> Text(catalogueError ?: "Catalogue unavailable", color = MaterialTheme.colorScheme.error)
                    choices.isEmpty() -> Text("No items match these filters.")
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 560.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(choices, key = { it.subjectType.name + ":" + it.id }) { entry ->
                            val isOwned = if (entry.subjectType == EnhancementSubjectType.CHARACTER) {
                                characters.any { it.id.toString() == entry.id }
                            } else {
                                characters.any { character ->
                                    when (val weapon = character.weapon) {
                                        is DataValue.Available -> weapon.value.id.toString() == entry.id
                                        is DataValue.Stale -> weapon.value.id.toString() == entry.id
                                        else -> false
                                    }
                                }
                            }
                            CalculatorChoiceCard(entry, isOwned) { selectItem(entry) }
                        }
                    }
                }
                Text(
                    "Portraits and item icons use source-attributed game asset metadata. Missing assets use a neutral placeholder.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CalculatorPage.PLAN -> {
                val entry = selected
                if (entry == null) {
                    Text("Choose a character or weapon first.")
                    TextButton(onClick = { page = CalculatorPage.CHOOSE_TYPE }) { Text("Choose item") }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        TextButton(onClick = { page = CalculatorPage.CHOOSE_ITEM; result = null }) { Text("‹ Change") }
                        if (!entry.iconUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = entry.iconUrl,
                                contentDescription = "${entry.name} icon",
                                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = if (entry.subjectType == EnhancementSubjectType.CHARACTER) ContentScale.Crop else ContentScale.Fit,
                                loading = { Text("✦") },
                                error = { Text("✦") }
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(54.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(if (entry.subjectType == EnhancementSubjectType.CHARACTER) "✦" else "⚔")
                                }
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2)
                            Text(
                                entry.rarity.toString() + "★ · " + if (entry.subjectType == EnhancementSubjectType.CHARACTER) "Character" else (entry.detail ?: "Weapon"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    CalculatorInputSection(if (entry.subjectType == EnhancementSubjectType.CHARACTER) "Character level" else "Weapon level") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            WheelNumberPicker("Current", currentLevel, 1, 90, Modifier.weight(1f)) {
                                currentLevel = it
                                if (targetLevel < it) targetLevel = it
                                val floor = calculatorLevelCaps.indexOfFirst { cap -> it <= cap }.coerceAtLeast(0)
                                val ceiling = if (it == calculatorLevelCaps[floor]) (floor + 1).coerceAtMost(6) else floor
                                currentRank = currentRank.coerceIn(floor, ceiling)
                            }
                            WheelNumberPicker("Target", targetLevel, currentLevel, 90, Modifier.weight(1f)) { targetLevel = it }
                        }
                    }
                    if (entry.subjectType == EnhancementSubjectType.CHARACTER) {
                        CalculatorInputSection("Talent levels") {
                            CalculatorTalentWheelRow("Normal Attack", normalCurrent, normalTarget,
                                { normalCurrent = it; if (normalTarget < it) normalTarget = it }, { normalTarget = it })
                            CalculatorTalentWheelRow("Elemental Skill", skillCurrent, skillTarget,
                                { skillCurrent = it; if (skillTarget < it) skillTarget = it }, { skillTarget = it })
                            CalculatorTalentWheelRow("Elemental Burst", burstCurrent, burstTarget,
                                { burstCurrent = it; if (burstTarget < it) burstTarget = it }, { burstTarget = it })
                        }
                    }
                    Button(onClick = { calculate() }, enabled = !calculating, modifier = Modifier.fillMaxWidth()) {
                        if (calculating) CircularProgressIndicator(modifier = Modifier.size(18.dp)) else Text("Calculate")
                    }
                    Text(
                        "Current EXP within the selected level is assumed to be 0. Ascension rank is retained from account data when available, otherwise inferred from the selected level. Upgrade costs remain candidate data; incomplete totals are withheld.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    result?.let { plan ->
        Dialog(onDismissRequest = { result = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Column(
                Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.92f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Material Requirements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(selected?.name.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { result = null }) { Text("Close") }
                }
                if (plan.issue != null) {
                    Text(plan.issue, color = MaterialTheme.colorScheme.error)
                } else if (plan.materials.isEmpty()) {
                    Text("No additional materials are required for these levels.")
                } else {
                    Text("Adjust owned quantities to update the remaining amount.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        Modifier.fillMaxWidth().padding(start = 43.dp, end = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text("Material / owned", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Consumed", Modifier.width(62.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Still needed", Modifier.width(68.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    plan.materials.forEachIndexed { index, material ->
                        val legacyStorageKey = "enhancement_owned_" +
                            selected?.subjectType?.name + ":" + selected?.id + ":" + material.id
                        val storageKey = "enhancement_owned_material_" + material.id
                        val storedQuantity = owned[material.id]
                            ?: preferences.getString(storageKey, null)?.toLongOrNull()?.coerceAtLeast(0L)
                            ?: preferences.getString(legacyStorageKey, null)?.toLongOrNull()?.coerceAtLeast(0L)
                        val remaining = com.irminsul.codex.data.catalogue.EnhancementMaterialInventoryCalculator
                            .remainingQuantity(material.required, storedQuantity)
                        CalculatorMaterialRow(
                            material, storedQuantity ?: 0L, remaining, storedQuantity != null,
                            { next ->
                                val safe = next.coerceAtLeast(0L)
                                owned = owned + (material.id to safe)
                                preferences.edit().putString(storageKey, safe.toString()).apply()
                            },
                            {
                                owned = owned + (material.id to 0L)
                                preferences.edit().putString(storageKey, "0").apply()
                            }
                        )
                        if (index != plan.materials.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }
                }
                Spacer(Modifier.size(4.dp))
                Text(
                    "Source: Genshin-DB " + BundledEnhancementCatalogueRepository.GAME_VERSION +
                        " and bundled EXP candidate tables. These values are not yet verified production costs.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalculatorTypeCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class CalculatorFilterOption(val key: String, val label: String, val iconUrl: String)
private fun calculatorElementFilterOptions(icons: Map<String, String>): List<CalculatorFilterOption> =
    listOf("Anemo", "Geo", "Electro", "Dendro", "Hydro", "Pyro", "Cryo").mapNotNull { key ->
        icons[key]?.let { CalculatorFilterOption(key, key, it) }
    }
private fun calculatorWeaponTypeFilterOptions(icons: Map<String, String>): List<CalculatorFilterOption> =
    listOf(
        "SWORD" to "Sword",
        "CLAYMORE" to "Claymore",
        "BOW" to "Bow",
        "POLEARM" to "Polearm",
        "CATALYST" to "Catalyst"
    ).mapNotNull { (key, label) ->
        icons[key]?.let { CalculatorFilterOption(key, label, it) }
    }
private fun calculatorMaterialIconUrl(id: String): String = "https://enka.network/ui/UI_ItemIcon_$id.png"

@Composable
private fun CalculatorIconFilterRow(title: String, options: List<CalculatorFilterOption>, selected: String?, onSelected: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            CalculatorIconFilter("All", null, selected == null) { onSelected(null) }
            options.forEach { option ->
                CalculatorIconFilter(option.label, option.iconUrl, selected == option.key) { onSelected(if (selected == option.key) null else option.key) }
            }
        }
    }
}
@Composable
private fun CalculatorIconFilter(label: String, iconUrl: String?, selected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(11.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)) else null) {
        Column(Modifier.width(58.dp).padding(horizontal = 5.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (iconUrl == null) Text("✦", style = MaterialTheme.typography.titleMedium)
            else SubcomposeAsyncImage(model = iconUrl, contentDescription = "$label filter icon", modifier = Modifier.size(25.dp), contentScale = ContentScale.Fit, loading = { Text("·") }, error = { Text("·") })
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}
@Composable
private fun CalculatorRarityFilters(selected: Int?, onSelected: (Int?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Rarity", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        FilterChip(selected == null, onClick = { onSelected(null) }, label = { Text("All") })
        FilterChip(selected == 5, onClick = { onSelected(if (selected == 5) null else 5) }, label = { Text("5★") })
        FilterChip(selected == 4, onClick = { onSelected(if (selected == 4) null else 4) }, label = { Text("4★") })
    }
}
@Composable
private fun CalculatorChoiceCard(entry: EnhancementCatalogueEntry, owned: Boolean, onClick: () -> Unit) {
    val background = if (entry.rarity == 5) Color(0xFFFFC857).copy(alpha = 0.22f) else Color(0xFFB388FF).copy(alpha = 0.22f)
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = background), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!entry.iconUrl.isNullOrBlank()) SubcomposeAsyncImage(
                model = entry.iconUrl, contentDescription = "${entry.name} game icon",
                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = if (entry.subjectType == EnhancementSubjectType.CHARACTER) ContentScale.Crop else ContentScale.Fit,
                loading = { Text("✦", style = MaterialTheme.typography.headlineMedium) },
                error = { Text("✦", style = MaterialTheme.typography.headlineMedium) }
            ) else Text(if (entry.subjectType == EnhancementSubjectType.CHARACTER) "✦" else "⚔", style = MaterialTheme.typography.headlineMedium)
            Text(entry.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, minLines = 2)
            Text(entry.rarity.toString() + "★", style = MaterialTheme.typography.labelSmall)
            Text(if (entry.subjectType == EnhancementSubjectType.CHARACTER) { if (owned) "On account" else "Not owned" } else { if (owned) "Equipped" else "Not seen equipped" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable
private fun CalculatorInputSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}
@Composable
private fun CalculatorTalentWheelRow(title: String, current: Int, target: Int, onCurrentChange: (Int) -> Unit, onTargetChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            WheelNumberPicker("Current", current, 1, 10, Modifier.weight(1f), onCurrentChange)
            WheelNumberPicker("Target", target, current, 10, Modifier.weight(1f), onTargetChange)
        }
    }
}
@Composable
private fun CalculatorMaterialRow(material: CalculatorMaterial, ownedQuantity: Long, remaining: Long?, isInventoryKnown: Boolean, onOwnedChange: (Long) -> Unit, onConfirmZero: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        SubcomposeAsyncImage(
            model = material.iconUrl,
            contentDescription = "${material.name} icon",
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit,
            loading = { Text("✦") },
            error = { Text("✦") }
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(material.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Text("Owned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isInventoryKnown) {
                CalculatorQuantityControl("", ownedQuantity, onOwnedChange)
            } else {
                TextButton(
                    onClick = onConfirmZero,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                ) { Text("Set 0 owned", style = MaterialTheme.typography.labelSmall) }
            }
        }
        Column(Modifier.width(62.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(material.required.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Column(Modifier.width(68.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(remaining?.toString() ?: "—", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        }
    }
}

@Composable
private fun CalculatorQuantityControl(label: String, value: Long, onChange: (Long) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        if (label.isNotBlank()) Text(label, style = MaterialTheme.typography.labelSmall)
        TextButton(onClick = { onChange((value - 1L).coerceAtLeast(0L)) }, enabled = value > 0L, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)) { Text("−") }
        Text(value.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = { onChange((value + 1L).coerceAtMost(Long.MAX_VALUE - 1L)) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)) { Text("+") }
    }
}
