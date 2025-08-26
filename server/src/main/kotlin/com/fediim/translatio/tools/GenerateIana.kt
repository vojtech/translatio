package com.fediim.translatio.tools

import com.fediim.translatio.Exporters
import java.io.File

/**
 * Usage (from project root):
 *   ./gradlew :server:exportAvailableLocale
 */
fun main() {
    try {
        val json = Exporters.exportAvailableLocale()
        val outFile = File("resources/locales.json")
        outFile.writeText(json)
        println("[GenerateLocale] Wrote ${outFile.absolutePath} with ${json.length} bytes")
    } catch (e: Exception) {
        System.err.println("[GenerateLocale] Failed: ${e.message}")
        e.printStackTrace()
        kotlin.system.exitProcess(1)
    }
}
