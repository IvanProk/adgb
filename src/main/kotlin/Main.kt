@file:JvmName("Launcher")

import decode.ApkSmaliDecoderController
import io.ArgumentReader
import io.Writer
import util.FileUtils
import java.io.File

fun main(args: Array<String>) {

    val arguments = ArgumentReader(args).read() ?: return

    // Delete the output directory for a better decoding result.
    if (FileUtils.deleteDir(arguments.projectPath)) {
        println("The output directory was deleted!")
    }

    // Decode the APK file for smali code in the output directory.
    ApkSmaliDecoderController.decode(
        arguments.apkFilePath, arguments.projectPath
    )

    val resultFile = File(arguments.resultPath)
    val analyzer = SmaliAnalyzer(arguments)
    if (analyzer.run()) {
        Writer(resultFile).write(analyzer.getDependencies())
        println("Success! Now open index.html in your browser.")
    }
}