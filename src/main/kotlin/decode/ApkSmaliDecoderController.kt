package decode

import java.io.IOException

object ApkSmaliDecoderController {
    // TODO: Change the default API version to the current version of
    // the APK, to be according to the APK version.
    private val DEFAULT_ANDROID_VERSION = 28

    fun decode(
        apkFilePath: String,
        outDirPath: String
    ) {
        try {
            ApkSmaliDecoder(
                apkFilePath, outDirPath, DEFAULT_ANDROID_VERSION
            ).decode()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }
}