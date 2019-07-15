package util

import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object ZipFileUtils {
    @Throws(IOException::class)
    fun filterByExtension(
        zipFilePath: String, extension: String
    ): List<String> {
        val files = ArrayList<String>()
        val zipFile = ZipFile(zipFilePath)
        val zipEntries = zipFile.entries()
        while (zipEntries.hasMoreElements()) {
            val zipEntry = zipEntries.nextElement() as ZipEntry
            if (!zipEntry.isDirectory) {
                val fileName = zipEntry.name
                if (checkExtension(fileName, extension)) {
                    files.add(fileName)
                }
            }
        }
        return files
    }

    private fun checkExtension(
        filePath: String, extension: String
    ): Boolean {
        return filePath.endsWith(extension)
    }
}