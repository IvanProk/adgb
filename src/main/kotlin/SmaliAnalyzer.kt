import code.CodeUtils
import io.Arguments
import java.io.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class SmaliAnalyzer(private val arguments: Arguments) {
    private var filterAsPath: String? = null

    private val dependencies = HashMap<String, MutableSet<String>>()

    private val projectFolder: File
        get() = File(arguments.projectPath)

    private val isInstantRunEnabled: Boolean
        get() {
            val unknownFolder = File(arguments.projectPath + File.separator + "unknown")
            if (unknownFolder.exists()) {
                for (file in unknownFolder.listFiles()!!) {
                    if (file.getName() == "instant-run.zip") {
                        return true
                    }
                }

            }
            return false
        }

    private val filteredDependencies: Map<String, Set<String>>
        get() {
            val filteredDependencies = HashMap<String, Set<String>>()
            for (key in dependencies.keys) {
                if (!key.contains("$")) {
                    val dependencySet = HashSet<String>()
                    for (dependency in dependencies[key]!!) {
                        if (!dependency.contains("$")) {
                            dependencySet.add(dependency)
                        }
                    }
                    if (dependencySet.size > 0) {
                        filteredDependencies[key] = dependencySet
                    }
                }
            }
            return filteredDependencies
        }

    fun getDependencies(): Map<String, Set<String>> {
        return if (arguments.withInnerClasses()) {
            dependencies
        } else filteredDependencies
    }

    fun run(): Boolean {
        val filter = arguments.filter
        if (filter == null) {
            System.err.println("Please check your filter!")
            return false
        }

        val replacement = Matcher.quoteReplacement(File.separator)
        val searchString = Pattern.quote(".")
        filterAsPath = filter!!.replace(searchString.toRegex(), replacement)
        val projectFolder = projectFolder
        if (projectFolder.exists()) {
            traverseSmaliCode(projectFolder)
            return true
        } else if (isInstantRunEnabled) {
            System.err.println("Enabled Instant Run feature detected. We cannot decompile it. Please, disable Instant Run and rebuild your app.")
        } else {
            System.err.println("Smali folder cannot be absent!")
        }
        return false
    }

    private fun traverseSmaliCode(folder: File) {
        val listOfFiles = folder.listFiles()
        for (i in listOfFiles!!.indices) {
            val currentFile = listOfFiles[i]
            if (currentFile.isFile) {
                if (currentFile.name.endsWith(".smali") && currentFile.absolutePath.contains(filterAsPath!!)) {
                    processSmaliFile(currentFile)
                }
            } else if (currentFile.isDirectory) {
                traverseSmaliCode(currentFile)
            }
        }
    }

    private fun processSmaliFile(file: File) {
        try {
            BufferedReader(FileReader(file)).use { br ->

                var fileName = file.name.substring(0, file.name.lastIndexOf("."))

                if (CodeUtils.isClassR(fileName)) {
                    return
                }

                if (CodeUtils.isClassAnonymous(fileName)) {
                    fileName = CodeUtils.getAnonymousNearestOuter(fileName)!!
                }

                val classNames = HashSet<String>()
                val dependencyNames = HashSet<String>()

                var line: String?
                line = br.readLine()

                while (line != null) {
                    try {
                        classNames.clear()

                        parseAndAddClassNames(classNames, line)

                        // filtering
                        for (fullClassName: String? in classNames) {
                            if (fullClassName != null && isFilterOk(fullClassName)) {
                                val simpleClassName = getClassSimpleName(fullClassName)
                                if (isClassOk(simpleClassName, fileName)) {
                                    dependencyNames.add(simpleClassName)
                                }
                            }
                        }
                    } catch (e: Exception) {
                    }
                    line = br.readLine()
                }

                // inner/nested class always depends on the outer class
                if (CodeUtils.isClassInner(fileName)) {
                    dependencyNames.add(CodeUtils.getOuterClass(fileName))
                }

                if (!dependencyNames.isEmpty()) {
                    addDependencies(fileName, dependencyNames)
                }
            }
        } catch (e: FileNotFoundException) {
            System.err.println("Cannot found " + file.absolutePath)
        } catch (e: IOException) {
            System.err.println("Cannot read " + file.absolutePath)
        }

    }

    private fun getClassSimpleName(fullClassName: String): String {
        var simpleClassName = fullClassName.substring(
            fullClassName.lastIndexOf("/") + 1,
            fullClassName.length
        )
        val startGenericIndex = simpleClassName.indexOf("<")
        if (startGenericIndex != -1) {
            simpleClassName = simpleClassName.substring(0, startGenericIndex)
        }
        return simpleClassName
    }

    /**
     * The last filter. Do not show anonymous classes (their dependencies belongs to outer class),
     * generated classes, avoid circular dependencies, do not show generated R class
     * @param simpleClassName class name to inspect
     * @param fileName full class name
     * @return true if class is good with these conditions
     */
    private fun isClassOk(simpleClassName: String, fileName: String): Boolean {
        return (!CodeUtils.isClassAnonymous(simpleClassName) && !CodeUtils.isClassGenerated(simpleClassName)
                && fileName != simpleClassName && !CodeUtils.isClassR(simpleClassName))
    }

    private fun parseAndAddClassNames(classNames: MutableSet<String>, line: String) {
        var index = line.indexOf("L")
        while (index != -1) {
            val colonIndex = line.indexOf(";", index)
            if (colonIndex == -1) {
                break
            }

            var className = line.substring(index + 1, colonIndex)
            if (className.matches("[\\w\\d/$<>]*".toRegex())) {
                val startGenericIndex = className.indexOf("<")
                if (startGenericIndex != -1 && className[startGenericIndex + 1] == 'L') {
                    // generic
                    val startGenericInLineIndex = index + startGenericIndex + 1 // index of "<" in the original string
                    val endGenericInLineIndex = getEndGenericIndex(line, startGenericInLineIndex)
                    val generic = line.substring(startGenericInLineIndex + 1, endGenericInLineIndex)
                    parseAndAddClassNames(classNames, generic)
                    index = line.indexOf("L", endGenericInLineIndex)
                    className = className.substring(0, startGenericIndex)
                } else {
                    index = line.indexOf("L", colonIndex)
                }
            } else {
                index = line.indexOf("L", index + 1)
                continue
            }

            classNames.add(className)
        }
    }

    private fun getEndGenericIndex(line: String, startGenericIndex: Int): Int {
        var endIndex = line.indexOf(">", startGenericIndex)
        var i = endIndex + 2
        while (i < line.length) {
            if (line[i] == '>') {
                endIndex = i
            }
            i += 2
        }
        return endIndex
    }

    private fun isFilterOk(className: String): Boolean {
        return arguments.filter == null || className.startsWith(arguments.filter.replace("\\.", "/"))
    }

    private fun addDependencies(className: String, dependenciesList: MutableSet<String>) {
        val depList = dependencies[className]
        if (depList == null) {
            // add this class and its dependencies
            dependencies[className] = dependenciesList
        } else {
            // if this class is already added - update its dependencies
            depList.addAll(dependenciesList)
        }
    }
}
