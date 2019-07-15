package util

import java.io.File

object FileUtils {
    fun deleteDir(dirPath: String): Boolean {
        val dirFile = File(dirPath)
        return dirFile.exists() && deleteDirectory(dirFile)
    }

    private fun deleteDirectory(dir: File): Boolean {
        val allContents = dir.listFiles()
        if (allContents != null) {
            for (file in allContents) {
                deleteDirectory(file)
            }
        }
        return dir.delete()
    }
}