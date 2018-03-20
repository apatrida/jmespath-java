package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Result
import io.burt.jmespath.trace.PropertyNodeTrace

class PropertyNode<T: Any>(runtime: Adapter<T>, private val rawPropertyName: String) : Node<T>(runtime) {
    private val propertyName: T = runtime.createString(rawPropertyName)


    override fun search(input: T?, tracing: Boolean): Result<T> {
        val response = runtime.getProperty(input, propertyName)
        return Result(response, tracing.whenActive { PropertyNodeTrace(response, rawPropertyName) })
    }


    override fun internalToString(): String? {
        return rawPropertyName
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? PropertyNode<*>
        return rawPropertyName == other?.rawPropertyName
    }

    override fun internalHashCode(): Int {
        return rawPropertyName.hashCode()
    }
}
