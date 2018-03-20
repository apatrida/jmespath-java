package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Result
import io.burt.jmespath.trace.StringNodeTrace

class StringNode<T: Any>(runtime: Adapter<T>, private val rawString: String?) : Node<T>(runtime) {
    private val string: T? = runtime.createString(rawString)

    override fun search(input: T?, tracing: Boolean): Result<T> {
        return Result(string, StringNodeTrace(string))
    }

    override fun internalToString(): String? {
        return rawString
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? StringNode<*>
        return rawString == other?.rawString
    }

    override fun internalHashCode(): Int {
        return rawString?.hashCode() ?: 99
    }
}
