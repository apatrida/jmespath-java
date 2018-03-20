package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.Result
import io.burt.jmespath.trace.IndexNodeTrace

class IndexNode<T: Any>(runtime: Adapter<T>, private val index: Int) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        if (runtime.typeOf(input) == JmesPathType.ARRAY) {
            val elements = runtime.toList(input)
            var i = index
            if (i < 0) {
                i = elements.size + i
            }
            if (i >= 0 && i < elements.size) {
                val response = elements[i]
                return Result(response, tracing.whenActive { IndexNodeTrace(response, i) })
            }
        }
        return Result(runtime.createNull(), tracing.whenActive { IndexNodeTrace<T>(null, index) })
    }

    override fun internalToString(): String? {
        return index.toString()
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? IndexNode<*>
        return index == other?.index
    }

    override fun internalHashCode(): Int {
        return index
    }
}
