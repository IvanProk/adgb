package io

class Arguments(
    val apkFilePath: String,
    var projectPath: String,
    var resultPath: String,
    var filter: String,
    private var withInnerClasses: Boolean
) {
    fun withInnerClasses(): Boolean {
        return withInnerClasses
    }

    fun setWithInnerClasses(withInnerClasses: Boolean) {
        this.withInnerClasses = withInnerClasses
    }
}