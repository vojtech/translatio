package com.fediim.translatio.localisation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fediim.translatio.shared.InternalLocale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * UI for creating a new string key and entering translations per locale.
 * Requirements:
 * - Enter key for the string
 * - Enter translation for each language
 * - Button to pre-fill all with default language
 * - Default language displayed at first position
 * - Translations grouped by language; country displayed as subcategory
 */
@Composable
fun LocalisationEditor(
    locales: List<InternalLocale>,
    onCreateKey: suspend (key: String, description: String?) -> Int?,
    onSubmitTranslation: suspend (stringId: Int, localeId: Int, value: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort locales to ensure default language at top, then group by language
    val grouped = remember(locales) {
        val sorted = locales.sortedWith(compareByDescending<InternalLocale> { it.isDefault }.thenBy { it.language }.thenBy { it.country ?: "" })
        sorted.groupBy { it.language }
    }

    var key by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Create a mutable map of localeId -> translation text
    val translations = remember(locales) {
        mutableStateMapOf<Int, String>().apply {
            locales.forEach { put(it.id, "") }
        }
    }

    val defaultLanguage: String? = locales.firstOrNull { it.isDefault }?.language

    val scope = rememberCoroutineScope()

    Column(modifier.padding(16.dp)) {
        Text("Create translation key", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = key,
            onValueChange = { key = it },
            label = { Text("Key") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val defaultLangText = defaultLanguage ?: "default"
            Button(onClick = {
                // Find default language values typed by user; copy to empty fields of same language group
                val defaultGroups = grouped[defaultLanguage]
                val defaultTexts = mutableMapOf<String, String>()
                // If default exists, pick base language text from any locale within default language group (countryless preferred)
                if (defaultGroups != null) {
                    val base = defaultGroups.minByOrNull { it.country?.isNotEmpty() == true }
                    base?.let { b ->
                        val text = translations[b.id] ?: ""
                        defaultTexts[b.language] = text
                    }
                }
                // Pre-fill all locales with the selected language's text if their field is empty
                grouped.forEach { (lang, items) ->
                    val fillText = if (lang == defaultLanguage) defaultTexts[lang] ?: "" else defaultTexts[defaultLanguage] ?: ""
                    if (fillText.isNotEmpty()) {
                        items.forEach { loc ->
                            if ((translations[loc.id] ?: "").isEmpty()) translations[loc.id] = fillText
                        }
                    }
                }
            }) {
                Text("Pre-fill with default")
            }

            Spacer(Modifier.width(16.dp))
            Button(
                enabled = key.isNotBlank(),
                onClick = {
                    scope.launch {
                        val stringId = onCreateKey(key.trim(), description.ifBlank { null })
                        if (stringId != null) {
                            // Submit translations
                            translations.forEach { (locId, value) ->
                                onSubmitTranslation(stringId, locId, value)
                            }
                            // Optionally clear
                            // key = ""; description = ""; translations.keys.forEach { translations[it] = "" }
                        }
                    }
                }
            ) { Text("Create") }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Show default language group first, then others
            val orderedLanguages = buildList {
                defaultLanguage?.let { add(it) }
                addAll(grouped.keys.filter { it != defaultLanguage }.sorted())
            }
            items(orderedLanguages) { lang ->
                val group = grouped[lang].orEmpty()
                LanguageGroup(lang = lang, items = group, translations = translations)
            }
        }


    }
}

@Composable
private fun LanguageGroup(
    lang: String,
    items: List<InternalLocale>,
    translations: MutableMap<Int, String>
) {
    Column {
        Text(text = lang.uppercase(), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        items.sortedBy { it.country ?: "" }.forEach { loc ->
            val label = if (loc.country.isNullOrBlank()) "(base)" else loc.country!!
            OutlinedTextField(
                value = translations[loc.id] ?: "",
                onValueChange = { translations[loc.id] = it },
                label = { Text("$lang - $label") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
