package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Result
import io.burt.jmespath.trace.JsonLiteralNodeTrace

class JsonLiteralNode<T: Any>(runtime: Adapter<T>, private val rawValue: String?) : Node<T>(runtime) {
    private val value: T? = runtime.parseString(rawValue)

    override fun search(input: T?, tracing: Boolean): Result<T> {
        return Result(value, tracing.whenActive { JsonLiteralNodeTrace(value) })
    }

    override fun internalToString(): String? {
        return rawValue
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as JsonLiteralNode<*>
        return rawValue == other.rawValue
    }

    override fun internalHashCode(): Int {
        return rawValue?.hashCode() ?: 111
    }
}
