package code

import util.StringUtils

object CodeUtils {

    fun isClassR(className: String?): Boolean {
        return className != null && className == "R" || className!!.startsWith("R$")
    }

    fun isClassGenerated(className: String?): Boolean {
        return className != null && className.contains("$$")
    }

    fun isClassInner(className: String?): Boolean {
        return className != null && className.contains("$") && !isClassAnonymous(className) && !isClassGenerated(
            className
        )
    }

    fun getOuterClass(className: String): String {
        return className.substring(0, className.lastIndexOf("$"))
    }

    fun isClassAnonymous(className: String?): Boolean {
        return (className != null && className.contains("$")
                && StringUtils.isNumber(className.substring(className.lastIndexOf("$") + 1, className.length)))
    }

    fun getAnonymousNearestOuter(className: String): String? {
        val classes = className.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in classes.indices) {
            if (StringUtils.isNumber(classes[i])) {
                var anonHolder = ""
                for (j in 0 until i) {
                    anonHolder += classes[j] + if (j == i - 1) "" else "$"
                }
                return anonHolder
            }
        }
        return null
    }
}
