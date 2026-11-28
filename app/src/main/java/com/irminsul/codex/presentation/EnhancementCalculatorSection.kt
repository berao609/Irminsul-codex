package com.irminsul.codex.presentation

import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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

private enum class CalculatorPage { CHOOSE_TYPE, CHOOSE_ITEM, PLAN }
private data class CalculatorMaterial(val id: String, val name: String, val required: Long)
private data class CalculatorResult(val materials: List<CalculatorMaterial>, val issue: String? = null)
private val calculatorLevelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

private fun calculatorExperienceCurve(entry: EnhancementCatalogueEntry): List<Long> =
    when (entry.subjectType) {
        EnhancementSubjectType.CHARACTER ->
            GenshinCharacterExperienceCurveCandidate.current.cumulativeExperienceByLevel
        EnhancementSubjectType.WEAPON ->
            if (entry.rarity == 4) GenshinWeaponExperienceCurveCandidate.fourStar.cumulativeExperienceByLevel
            else GenshinWeaponExperienceCurveCandidate.fiveStar.cumulativeExperienceByLevel
    }

private fun calculatorCurrentExperienceMaximum(
    currentLevel: Int,
    currentAscensionRank: Int,
    cumulativeExperienceByLevel: List<Long>
): Long {
    val unlockedCap = calculatorLevelCaps.getOrNull(currentAscensionRank) ?: return 0L
    if (currentLevel !in 1 until unlockedCap) return 0L
    val currentTotal = cumulativeExperienceByLevel.getOrNull(currentLevel - 1) ?: return 0L
    val nextTotal = cumulativeExperienceByLevel.getOrNull(currentLevel) ?: return 0L
    return (nextTotal - currentTotal - 1L).coerceAtLeast(0L)
}

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
    var loading by remember { mutableStateOf(true) }
    var catalogueError by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<EnhancementCatalogueEntry?>(null) }
    var currentLevel by remember { mutableIntStateOf(1) }
    var targetLevel by remember { mutableIntStateOf(90) }
    var currentExperience by remember { mutableStateOf(0L) }
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
            catalogue = withContext(Dispatchers.IO) {
                BundledEnhancementCatalogueRepository.loadAll(context)
            }
            catalogueError = null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            catalogueError = failure.message ?: "The bundled game catalogue could not be loaded."
        } finally {
            loading = false
        }
    }

    val choices = remember(catalogue, subjectType, search) {
        catalogue.filter { it.subjectType == subjectType &&
            (search.isBlank() || it.name.contains(search.trim(), ignoreCase = true))
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
        currentExperience = 0L
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
        search = ""
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
                                currentExperienceWithinLevel = currentExperience,
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
                            CalculatorMaterial(it.itemId, it.itemName, it.quantity)
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
                                currentExperienceWithinLevel = currentExperience,
                                currentAscensionRank = currentRank,
                                targetLevel = targetLevel
                            ),
                            curve.cumulativeExperienceByLevel,
                            candidates
                        )
                        if (plan.status != WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA) {
                            CalculatorResult(emptyList(), plan.issue ?: "Complete weapon cost data is unavailable.")
                        } else CalculatorResult(plan.requirements.map {
                            CalculatorMaterial(it.itemId, it.itemName, it.quantity)
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
                    search = ""
                    page = CalculatorPage.CHOOSE_ITEM
                }
                CalculatorTypeCard("Weapon", "Weapon level and ascension materials") {
                    subjectType = EnhancementSubjectType.WEAPON
                    search = ""
                    page = CalculatorPage.CHOOSE_ITEM
                }
            }
            CalculatorPage.CHOOSE_ITEM -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { page = CalculatorPage.CHOOSE_TYPE }) { Text("‹ Back") }
                    Text(if (subjectType == EnhancementSubjectType.CHARACTER) "Choose Character" else "Choose Weapon", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text(choices.size.toString() + " available · 4-star and 5-star", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("Search") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                when {
                    loading -> CircularProgressIndicator()
                    catalogueError != null -> Text(catalogueError ?: "Catalogue unavailable", color = MaterialTheme.colorScheme.error)
                    choices.isEmpty() -> Text("No matching items.")
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
                    "Source: Genshin-DB " + BundledEnhancementCatalogueRepository.GAME_VERSION +
                        ". The bundled selection data does not include a complete portrait/icon mapping.",
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { page = CalculatorPage.CHOOSE_ITEM; result = null }) { Text("‹ Change") }
                        Column {
                            Text(entry.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(entry.rarity.toString() + "★ · " + if (entry.subjectType == EnhancementSubjectType.CHARACTER) "Character" else (entry.detail ?: "Weapon"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text("Level range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    CalculatorNumberControl("Current level", currentLevel, 1, 90) {
                        currentLevel = it
                        if (targetLevel < it) targetLevel = it
                        val floor = calculatorLevelCaps.indexOfFirst { cap -> it <= cap }.coerceAtLeast(0)
                        val ceiling = if (it == calculatorLevelCaps[floor]) (floor + 1).coerceAtMost(6) else floor
                        val nextRank = currentRank.coerceIn(floor, ceiling)
                        currentRank = nextRank
                        val curve = calculatorExperienceCurve(entry)
                        currentExperience = currentExperience.coerceAtMost(
                            calculatorCurrentExperienceMaximum(it, nextRank, curve)
                        )
                    }
                    CalculatorNumberControl("Desired level", targetLevel, currentLevel, 90) { targetLevel = it }
                    val experienceCurve = calculatorExperienceCurve(entry)
                    val currentExperienceMaximum = calculatorCurrentExperienceMaximum(
                        currentLevel, currentRank, experienceCurve
                    )
                    CalculatorLongNumberControl(
                        "EXP already earned within current level",
                        currentExperience,
                        0L,
                        currentExperienceMaximum,
                        1L
                    ) { currentExperience = it }
                    val rankFloor = calculatorLevelCaps.indexOfFirst { currentLevel <= it }.coerceAtLeast(0)
                    val rankCeiling = if (currentLevel == calculatorLevelCaps[rankFloor]) {
                        (rankFloor + 1).coerceAtMost(6)
                    } else rankFloor
                    CalculatorNumberControl("Current ascension rank", currentRank, rankFloor, rankCeiling) {
                        currentRank = it
                        currentExperience = currentExperience.coerceAtMost(
                            calculatorCurrentExperienceMaximum(
                                currentLevel, it, calculatorExperienceCurve(entry)
                            )
                        )
                    }

                    if (entry.subjectType == EnhancementSubjectType.CHARACTER) {
                        Text("Talent levels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        CalculatorNumberControl("Normal Attack · current", normalCurrent, 1, 10) {
                            normalCurrent = it
                            if (normalTarget < it) normalTarget = it
                        }
                        CalculatorNumberControl("Normal Attack · desired", normalTarget, normalCurrent, 10) { normalTarget = it }
                        CalculatorNumberControl("Elemental Skill · current", skillCurrent, 1, 10) {
                            skillCurrent = it
                            if (skillTarget < it) skillTarget = it
                        }
                        CalculatorNumberControl("Elemental Skill · desired", skillTarget, skillCurrent, 10) { skillTarget = it }
                        CalculatorNumberControl("Elemental Burst · current", burstCurrent, 1, 10) {
                            burstCurrent = it
                            if (burstTarget < it) burstTarget = it
                        }
                        CalculatorNumberControl("Elemental Burst · desired", burstTarget, burstCurrent, 10) { burstTarget = it }
                    }
                    Button(onClick = { calculate() }, enabled = !calculating, modifier = Modifier.fillMaxWidth()) {
                        if (calculating) CircularProgressIndicator(modifier = Modifier.size(18.dp)) else Text("Calculate")
                    }
                    Text(
                        "Upgrade costs and EXP curves are source-attributed candidate data. Incomplete totals are withheld.",
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
                    Text("Change your owned quantities. Remaining requirements update immediately.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    plan.materials.forEach { material ->
                        val legacyStorageKey = "enhancement_owned_" +
                            selected?.subjectType?.name + ":" + selected?.id + ":" + material.id
                        // Material inventory belongs to the account, not to an individual plan.
                        // Read the old plan-scoped value as a compatibility fallback.
                        val storageKey = "enhancement_owned_material_" + material.id
                        val storedQuantity = owned[material.id]
                            ?: preferences.getString(storageKey, null)?.toLongOrNull()?.coerceAtLeast(0L)
                            ?: preferences.getString(legacyStorageKey, null)?.toLongOrNull()?.coerceAtLeast(0L)
                        val quantity = storedQuantity ?: 0L
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(material.name, fontWeight = FontWeight.SemiBold)
                                Text("Required: " + material.required, style = MaterialTheme.typography.bodySmall)
                                CalculatorQuantityControl("Owned", quantity) { next ->
                                    val safe = next.coerceAtLeast(0L)
                                    owned = owned + (material.id to safe)
                                    preferences.edit().putString(storageKey, safe.toString()).apply()
                                }
                                if (storedQuantity == null) {
                                    Text(
                                        "Owned quantity not entered. Confirm 0 if you own none.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = {
                                            owned = owned + (material.id to 0L)
                                            preferences.edit().putString(storageKey, "0").apply()
                                        }
                                    ) { Text("Confirm 0 owned") }
                                }
                                val remaining = com.irminsul.codex.data.catalogue.EnhancementMaterialInventoryCalculator
                                    .remainingQuantity(material.required, storedQuantity)
                                Text(
                                    remaining?.let { "Remaining: $it" }
                                        ?: "Remaining: enter owned quantity",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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

@Composable
private fun CalculatorChoiceCard(entry: EnhancementCatalogueEntry, owned: Boolean, onClick: () -> Unit) {
    val background = if (entry.rarity == 5) Color(0xFFFFC857).copy(alpha = 0.22f)
        else Color(0xFFB388FF).copy(alpha = 0.22f)
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(if (entry.subjectType == EnhancementSubjectType.CHARACTER) "✦" else "⚔", style = MaterialTheme.typography.headlineMedium)
            Text(entry.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, minLines = 2)
            Text(entry.rarity.toString() + "★", style = MaterialTheme.typography.labelSmall)
            Text(
                if (entry.subjectType == EnhancementSubjectType.CHARACTER) {
                    if (owned) "On account" else "Not owned"
                } else {
                    if (owned) "Equipped" else "Not seen equipped"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalculatorNumberControl(label: String, value: Int, minimum: Int, maximum: Int, onChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { onChange((value - 1).coerceAtLeast(minimum)) }, enabled = value > minimum) { Text("−") }
            Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { onChange((value + 1).coerceAtMost(maximum)) }, enabled = value < maximum) { Text("+") }
        }
        if (maximum > minimum) {
            Slider(
                value = value.toFloat(),
                onValueChange = { onChange(it.toInt().coerceIn(minimum, maximum)) },
                valueRange = minimum.toFloat()..maximum.toFloat(),
                steps = (maximum - minimum - 1).coerceAtLeast(0)
            )
        }
    }
}

@Composable
private fun CalculatorLongNumberControl(
    label: String,
    value: Long,
    minimum: Long,
    maximum: Long,
    increment: Long,
    onChange: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { onChange((value - increment).coerceAtLeast(minimum)) }, enabled = value > minimum) { Text("−") }
            Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { onChange((value + increment).coerceAtMost(maximum)) }, enabled = value < maximum) { Text("+") }
        }
        if (maximum > minimum) {
            Slider(
                value = value.coerceIn(minimum, maximum).toFloat(),
                onValueChange = { onChange(it.toLong().coerceIn(minimum, maximum)) },
                valueRange = minimum.toFloat()..maximum.toFloat(),
                steps = 0
            )
        }
    }
}

@Composable
private fun CalculatorQuantityControl(label: String, value: Long, onChange: (Long) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = { onChange((value - 1L).coerceAtLeast(0L)) }, enabled = value > 0L) { Text("−") }
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = { onChange((value + 1L).coerceAtMost(Long.MAX_VALUE - 1L)) }) { Text("+") }
    }
}
