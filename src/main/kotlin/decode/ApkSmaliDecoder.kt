package decode

import org.jf.baksmali.Baksmali
import org.jf.baksmali.BaksmaliOptions
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.analysis.InlineMethodResolver
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import org.jf.dexlib2.dexbacked.DexBackedOdexFile
import util.ZipFileUtils
import java.io.File
import java.io.IOException

class ApkSmaliDecoder internal constructor(
    private val apkFilePath: String,
    private val outDirPath: String,
    private val apiVersion: Int
) {

    private val numberOfAvailableProcessors: Int
        get() {
            val jobs = Runtime.getRuntime().availableProcessors()
            return Math.min(jobs, MAXIMUM_NUMBER_OF_PROCESSORS)
        }

    @Throws(IOException::class)
    internal fun decode() {
        val apkFile = File(this.apkFilePath)
        if (!apkFile.exists()) {
            throw IOException(WARNING_FILE_IS_NOT_FOUND)
        }
        val outDir = File(this.outDirPath)

        // Read all dex files in the APK file and so decode each one.
        for (dexFileName in getDexFiles(this.apkFilePath)) {
            decodeDexFile(apkFile, dexFileName, this.apiVersion, outDir)
        }
    }

    @Throws(IOException::class)
    private fun decodeDexFile(
        apkFile: File, dexFileName: String, apiVersion: Int, outDir: File
    ) {
        try {
            log("Baksmaling $dexFileName...")
            val dexFile = loadDexFile(apkFile, dexFileName, apiVersion)

            Baksmali.disassembleDexFile(
                dexFile,
                outDir,
                numberOfAvailableProcessors,
                getSmaliOptions(dexFile)
            )
        } catch (ex: Exception) {
            throw IOException(ex)
        }

    }

    private fun getSmaliOptions(dexFile: DexBackedDexFile): BaksmaliOptions {
        val options = BaksmaliOptions()

        options.deodex = false
        options.implicitReferences = false
        options.parameterRegisters = true
        options.localsDirective = true
        options.sequentialLabels = true
        options.debugInfo = false
        options.codeOffsets = false
        options.accessorComments = false
        options.registerInfo = 0

        if (dexFile is DexBackedOdexFile) {
            options.inlineResolver = InlineMethodResolver.createInlineMethodResolver(
                (dexFile as DexBackedOdexFile).getOdexVersion()
            )
        } else {
            options.inlineResolver = null
        }

        return options
    }

    @Throws(IOException::class)
    private fun loadDexFile(
        apkFile: File, dexFilePath: String, apiVersion: Int
    ): DexBackedDexFile {
        val dexFile = DexFileFactory.loadDexEntry(
            apkFile, dexFilePath, true, Opcodes.forApi(apiVersion)
        )

        if (dexFile == null || dexFile!!.isOdexFile()) {
            throw IOException(WARNING_DISASSEMBLING_ODEX_FILE)
        }

        return dexFile
    }

    @Throws(IOException::class)
    private fun getDexFiles(apkFilePath: String): List<String> {
        return ZipFileUtils.filterByExtension(apkFilePath, DEX_FILE_EXTENSION)
    }

    private fun log(text: String) {
        println(text)
    }

    companion object {
        private val MAXIMUM_NUMBER_OF_PROCESSORS = 6

        private val DEX_FILE_EXTENSION = ".dex"

        private val WARNING_DISASSEMBLING_ODEX_FILE =
            "Warning: You are disassembling an odex file without deodexing it."
        private val WARNING_FILE_IS_NOT_FOUND = "Apk file is not found!"
    }
}