package com.fediim.translatio

import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

object Importers {
    fun parseAndroidStringsXml(bytes: ByteArray): List<Pair<String, String>> {
        return runCatching {
            val dbf = DocumentBuilderFactory.newInstance()
            val doc = dbf.newDocumentBuilder().parse(ByteArrayInputStream(bytes))
            val list = doc.getElementsByTagName("string")
            (0 until list.length).mapNotNull { idx ->
                val node = list.item(idx)
                if (node is Element) {
                    val key = node.getAttribute("name")
                    val value = node.textContent
                    key to value
                } else null
            }
        }.getOrElse { emptyList() }
    }

    fun parseFlutterArb(bytes: ByteArray): List<Pair<String, String>> {
        return runCatching {
            val string = bytes.toString(Charsets.UTF_8)
            val json = Json.parseToJsonElement(string).jsonObject
            json.entries.filter { !it.key.startsWith("@") }.mapNotNull { e ->
                val value = e.value.toString().trim('"')
                e.key to value
            }
        }.getOrElse { emptyList() }
    }

    fun parseIosStrings(bytes: ByteArray): List<Pair<String, String>> {
        // .strings is a simple key = "value"; format, we'll do a basic regex parse
        val text = bytes.toString(Charsets.UTF_8)
        val regex = Regex("""\"([^\"]+)\"\s*=\s*\"([^\"]*)\";""")
        return regex.findAll(text).map { m -> m.groupValues[1] to m.groupValues[2] }.toList()
    }
}
