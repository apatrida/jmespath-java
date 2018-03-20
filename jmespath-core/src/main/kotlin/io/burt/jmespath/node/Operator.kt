package io.burt.jmespath.node

enum class Operator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN,
    LESS_THAN_OR_EQUALS;

    companion object {
        fun fromString(str: String): Operator {
            return if ("==" == str) {
                EQUALS
            } else if ("!=" == str) {
                NOT_EQUALS
            } else if (">" == str) {
                GREATER_THAN
            } else if (">=" == str) {
                GREATER_THAN_OR_EQUALS
            } else if ("<" == str) {
                LESS_THAN
            } else if ("<=" == str) {
                LESS_THAN_OR_EQUALS
            } else {
                throw IllegalArgumentException("No such operator $str")
            }
        }
    }
}
