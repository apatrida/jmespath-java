package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.Result
import io.burt.jmespath.trace.CreateArrayNodeTrace
import java.util.*

class CreateArrayNode<T: Any>(runtime: Adapter<T>, val entries: List<Expression<T>>) : Node<T>(runtime) {
    override fun search(input: T?, tracing: Boolean): Result<T> {
        if (runtime.typeOf(input) == JmesPathType.NULL) {
            return Result(input, tracing.whenActive { CreateArrayNodeTrace<T>(null, emptyList()) })
        } else {
            val array = ArrayList<T?>()
            val traceItems = if (!tracing) null else ArrayList<Result<T>>()
            for (entry in entries) {
                val oneNewItem = entry.search(input, tracing)
                array.add(oneNewItem.value)
                traceItems?.add(oneNewItem)
            }
            val arrayResult = runtime.createArray(array)
            val trace = tracing.whenActive { CreateArrayNodeTrace(arrayResult, traceItems!!) }
            return Result(arrayResult, trace)
        }
    }

    override fun internalToString(): String? {
        val str = StringBuilder("[")
        val entryIterator = entries.iterator()
        while (entryIterator.hasNext()) {
            val entry = entryIterator.next()
            str.append(entry)
            if (entryIterator.hasNext()) {
                str.append(", ")
            }
        }
        str.append(']')
        return str.toString()
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? CreateArrayNode<*>
        return entries == other?.entries
    }

    override fun internalHashCode(): Int {
        return entries.hashCode()
    }
}
