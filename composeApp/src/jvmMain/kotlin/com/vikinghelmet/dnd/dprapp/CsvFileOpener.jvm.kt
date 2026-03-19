package com.vikinghelmet.dnd.dprapp

import java.awt.Desktop
import java.io.File

actual fun openCsvFile(fileName: String) {
    val file = File(fileName) // Assumes file is in root/current dir
    System.out.println("filename = $fileName, file = $file, exists = ${file.exists()}")
    if (file.exists()) {
        Desktop.getDesktop().open(file)
    }
}

actual fun shareCsv(fileName: String, csvContent: String) {
    val tempDir = System.getProperty("java.io.tmpdir")
    println("Temp Dir: " + tempDir)

    val file = File("$tempDir/$fileName")
    file.writeText(csvContent)
    Desktop.getDesktop().open(file)
}
