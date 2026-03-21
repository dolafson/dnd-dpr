package com.vikinghelmet.dnd.dprapp

//import platform.UIKit.UIApplication
//import platform.UIKit.UIDocumentPickerViewController

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.temporaryDirectory
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
actual fun openCsvFile(fileName: String) {
    /*
    val url = NSURL.fileURLWithPath(fileName)

    println("Opening CSV file: $url")
    //UIApplication.sharedApplication.openURL(url)
    UIApplication.sharedApplication.openURL()

     */
    /*
    val url = NSURL.fileURLWithPath(fileName)
    val documentPicker = UIDocumentPickerViewController(
        /*
        forOpeningContentTypes = listOf(UTTypeCommaSeparatedText),
        fileName = fileName,
        asCopy = true

         */
        url, UIDocumentPickerMode.UIDocumentPickerModeExportToService
    )
    // Delegate must be implemented to handle dismissal
    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
        documentPicker, animated = true, completion = null
    )

 */
}

actual fun shareCsv(fileName: String, csvContent: String) {

    val fileManager = NSFileManager.defaultManager
    val tempDir = fileManager.temporaryDirectory
    val fileUrl = tempDir.URLByAppendingPathComponent(fileName)

    // Write CSV content to file
    //(csvContent as NSString).writeToURL(fileUrl!!, true, NSUTF8StringEncoding, null)

    // Present share sheet
    val activityViewController = UIActivityViewController(
        activityItems = listOf(fileUrl),
        applicationActivities = null
    )

    val window = UIApplication.sharedApplication.keyWindow
    val viewController = window?.rootViewController
    viewController?.presentViewController(activityViewController, animated = true, completion = null)
}

actual fun isShareCsvSupported(): Boolean { return false }
