package io

import java.io.File

class ArgumentReader(private val args: Array<String>) {

    fun read(): Arguments? {
        var projectPath: String? = null
        var resultPath: String? = null
        var filter: String? = null
        var apkPath: String? = null
        var withInnerClasses = false
        for (i in args.indices) {
            if (i < args.size - 1) {
                if (args[i] == "-i") {
                    projectPath = args[i + 1]
                } else if (args[i] == "-o") {
                    resultPath = args[i + 1]
                } else if (args[i] == "-f") {
                    filter = args[i + 1]
                } else if (args[i] == "-d") {
                    withInnerClasses = java.lang.Boolean.valueOf(args[i + 1])
                } else if (args[i] == "-a") {
                    apkPath = args[i + 1]
                }
            }
        }
        if (projectPath == null || resultPath == null || filter == null ||
            apkPath == null
        ) {
            System.err.println("Arguments are incorrect!")
            System.err.println(USAGE_STRING)
            return null
        }
        if (filter == "nofilter") {
            filter = ""
            println("Warning! Processing without filter.")
        }
        if (!withInnerClasses) {
            println("Warning! Processing without inner classes.")
        }

        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            println("$apkFile is not found!")
            return null
        }

        return Arguments(
            apkPath, projectPath, resultPath, filter, withInnerClasses
        )
    }

    companion object {

        private val USAGE_STRING = "Usage:\n" +
                "-i path : path to the decompiled project\n" +
                "-o path : path to the result js file\n" +
                "-f filter : java package to filter by (to show all dependencies pass '-f nofilter')\n" +
                "-d boolean : if true it will contain inner class processing (the ones creating ClassName\$InnerClass files)\n" +
                "-a path : path to the apk file"
    }
}