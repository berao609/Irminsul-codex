package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*

object CharacterStatClassifier {
    fun classify(characters: List<CharacterSnapshot>): CharacterStatClassificationReport =
        CharacterStatClassificationReport(characters.map(::classifyCharacter))

    private fun classifyCharacter(character: CharacterSnapshot): CharacterStatClassification {
        val results = mutableListOf<StatPropertyClassification>()
        fun add(path: String, stat: StatValueSnapshot) {
            results += StatPropertyCatalog.classify(character.id, path, stat)
        }
        fun addStats(path: String, stats: List<StatValueSnapshot>) {
            stats.forEachIndexed { index, stat -> add("$path[$index]", stat) }
        }

        addStats("selectedProperties", character.selectedProperties)
        addStats("baseProperties", character.baseProperties)
        addStats("extraProperties", character.extraProperties)
        addStats("elementProperties", character.elementProperties)

        when (val weapon = character.weapon) {
            is DataValue.Available -> {
                weapon.value.mainProperty?.let { add("weapon.mainProperty", it) }
                weapon.value.subProperty?.let { add("weapon.subProperty", it) }
            }
            is DataValue.Stale -> {
                weapon.value.mainProperty?.let { add("weapon.mainProperty", it) }
                weapon.value.subProperty?.let { add("weapon.subProperty", it) }
            }
            else -> Unit
        }
        when (val artifacts = character.artifacts) {
            is DataValue.Available -> artifacts.value.forEachIndexed { index, artifact ->
                artifact.mainProperty?.let { add("artifacts[$index].mainProperty", it) }
                addStats("artifacts[$index].subProperties", artifact.subProperties)
            }
            is DataValue.Stale -> artifacts.value.forEachIndexed { index, artifact ->
                artifact.mainProperty?.let { add("artifacts[$index].mainProperty", it) }
                addStats("artifacts[$index].subProperties", artifact.subProperties)
            }
            else -> Unit
        }
        return CharacterStatClassification(character.id, results)
    }
}
