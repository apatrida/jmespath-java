package io.burt.jmespath

import io.burt.jmespath.trace.TraceNode
import io.burt.jmespath.trace.TraceResult

/**
 * A compiled JMESPath expression that can be used to search a JSON-like structure.
 *
 *
 * Expression objects should be stateless and thread safe, but the exact details
 * are up to the concrete implementations.
 */
interface Expression<T: Any> {
    val runtime: Adapter<T>

    /**
     * Evaluate this expression against a JSON-like structure and return the result.
     */
    fun search(input: T?, tracing: Boolean): Result<T>

    fun search(input: T?) = search(input, false).value

    fun searchWrapped(input: T?, tracing: Boolean): WrappedResult<T> {
        val temp = search(input, tracing)
        return wrapWithAdapter(temp)
    }

    fun wrapWithAdapter(result: Result<T>): WrappedResult<T> = WrappedResult(result.value, result.traceNode?.let { TraceResult(it, runtime) })
}

data class Result<T: Any>(val value: T?, val traceNode: TraceNode<T>?)
data class WrappedResult<T: Any>(val value: T?, val traceRoot: TraceResult<T>?)