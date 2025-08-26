package com.fediim.translatio

import com.fediim.translatio.db.*
import com.fediim.translatio.model.toExport
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlinx.serialization.json.*
import org.jetbrains.exposed.v1.core.eq
import java.util.Locale

object Exporters {
    fun exportLocalesJson(): String {
        val items = transaction {
            LocaleDb.selectAll().orderBy(LocaleDb.language).map { row ->
                val code = listOfNotNull(row[LocaleDb.language], row[LocaleDb.country]).joinToString("-")
                JsonObject(
                    mapOf(
                        "id" to JsonPrimitive(row[LocaleDb.id].value),
                        "language" to JsonPrimitive(row[LocaleDb.language]),
                        "country" to (row[LocaleDb.country]?.let { JsonPrimitive(it) } ?: JsonNull),
                        "code" to JsonPrimitive(code)
                    ))
            }
        }
        return Json.encodeToString(JsonArray.serializer(), JsonArray(items))
    }

    private val json = Json {
        prettyPrint = true
    }

    fun exportAvailableLocale(): String {
        val items = Locale.getAvailableLocales().map { it.toExport() }.filter {
            !it.language.isNullOrEmpty()
        }

        // Sort by subtag for stable output
        val sorted = items.sortedBy { it.language }
        return json.encodeToString(sorted)
    }

    fun exportAndroidXml(): String {
        val data = transaction {
            StringKeyDb.selectAll().associate { keyRow ->
                val keyId = keyRow[StringKeyDb.id].value
                val translations = TranslationDb.selectAll().where { TranslationDb.stringId eq keyId }
                    .associate { t -> t[TranslationDb.localeId].value to t[TranslationDb.value] }
                keyRow[StringKeyDb.key] to translations
            }
        }
        // For Android, export default language (first available) into strings.xml
        val builder = StringBuilder()
        builder.append(
            """<?xml version='1.0' encoding='utf-8'?>
<resources>
"""
        )
        data.forEach { (k, translations) ->
            val value = translations.values.firstOrNull() ?: ""
            builder.append("    <string name=\"${k}\">${escapeXml(value)}</string>\n")
        }
        builder.append("</resources>\n")
        return builder.toString()
    }

    fun exportFlutterArb(): String {
        val data = transaction {
            StringKeyDb.selectAll().associate { keyRow ->
                val keyId = keyRow[StringKeyDb.id].value
                val translations = TranslationDb.selectAll().where { TranslationDb.stringId eq keyId }
                    .associate { t -> t[TranslationDb.localeId].value to t[TranslationDb.value] }
                keyRow[StringKeyDb.key] to translations
            }
        }
        val defaultMap = buildMap<String, String> {
            data.forEach { (k, translations) ->
                put(k, translations.values.firstOrNull() ?: "")
            }
        }
        return Json.encodeToString(
            JsonObject.serializer(),
            JsonObject(defaultMap.mapValues { JsonPrimitive(it.value) })
        )
    }

    fun exportIosStrings(): String {
        val data = transaction {
            StringKeyDb.selectAll().associate { keyRow ->
                val keyId = keyRow[StringKeyDb.id].value
                val translations = TranslationDb.selectAll().where { TranslationDb.stringId eq keyId }
                    .associate { t -> t[TranslationDb.localeId].value to t[TranslationDb.value] }
                keyRow[StringKeyDb.key] to translations
            }
        }
        val sb = StringBuilder()
        data.forEach { (k, translations) ->
            val value = translations.values.firstOrNull() ?: ""
            sb.append("\"$k\" = \"${value.replace("\"", "\\\"")}\";\n")
        }
        return sb.toString()
    }

    fun exportWebJson(): String {
        // Export all translations structured as { key: { locale: value } }
        val data = transaction {
            val locales = LocaleDb.selectAll().associateBy({ it[LocaleDb.id].value }) { row ->
                listOfNotNull(row[LocaleDb.language], row[LocaleDb.country]).joinToString("-")
            }
            StringKeyDb.selectAll().associate { keyRow ->
                val keyId = keyRow[StringKeyDb.id].value
                val translations = TranslationDb.selectAll().where { TranslationDb.stringId eq keyId }
                    .associate { t -> locales[t[TranslationDb.localeId].value]!! to t[TranslationDb.value] }
                keyRow[StringKeyDb.key] to translations
            }
        }
        val jsonObj = JsonObject(
            data.mapValues { (_, m) -> JsonObject(m.mapValues { JsonPrimitive(it.value) }) }
        )
        return Json.encodeToString(JsonObject.serializer(), jsonObj)
    }

    private fun escapeXml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
