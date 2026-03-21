package com.vikinghelmet.dnd.dprapp

/*
// commonMain/kotlin/CsvFileOpener.kt
import androidx.compose.runtime.staticCompositionLocalOf

interface FileOpener {
    fun openCsvFile(fileName: String) // e.g., "data.csv"
}

// CompositionLocal to provide it in Compose
val LocalFileOpener = staticCompositionLocalOf<FileOpener> {
    error("No FileOpener provided")
}

 */

expect fun openCsvFile(fileName: String) // e.g., file:///path/to/file.csv

expect fun shareCsv(fileName: String, csvContent: String)

expect fun isShareCsvSupported(): Boolean

expect fun isTinyCpu(): Boolean