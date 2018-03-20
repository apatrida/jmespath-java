package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Result
import io.burt.jmespath.trace.SequenceNodeTrace
import java.util.*

class SequenceNode<T: Any>(runtime: Adapter<T>, private val nodes: List<Node<T>>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        var value: T? = input
        val traceItems = if (!tracing) null else ArrayList<Result<T>>()
        for (node in nodes) {
            val response = node.search(value, tracing)
            traceItems?.add(response)
            value = response.value
        }
        return Result(value, tracing.whenActive { SequenceNodeTrace(value, traceItems!!) })
    }

    override fun internalToString(): String? {
        if (nodes.isEmpty()) {
            return null
        } else {
            val buffer = StringBuilder()
            val iterator = nodes.iterator()
            buffer.append(iterator.next())
            while (iterator.hasNext()) {
                buffer.append(", ")
                buffer.append(iterator.next())
            }
            return buffer.toString()
        }
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? SequenceNode<*>
        return nodes == other?.nodes
    }

    override fun internalHashCode(): Int {
        return nodes.hashCode()
    }

}
