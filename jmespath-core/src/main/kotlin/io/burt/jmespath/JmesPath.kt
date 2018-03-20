package io.burt.jmespath

/**
 * A JMESPath runtime can compile JMESPath expressions.
 */
interface JmesPath<T: Any> {
    /**
     * Compile a JMESPath expression into a reusable expression object.
     *
     *
     * The expression objects should be stateless and thread safe, but the exact
     * details are up to the concrete implementations.
     *
     * @throws io.burt.jmespath.parser.ParseException when the string is not a valid JMESPath expression
     */
    fun compile(expression: String): Expression<T>
}
