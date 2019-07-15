package util

object StringUtils {
    fun isNumber(str: String): Boolean {
        for (c in str.toCharArray()) {
            if (!Character.isDigit(c))
                return false
        }
        return true
    }
}