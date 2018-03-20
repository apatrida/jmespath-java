package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.Result
import io.burt.jmespath.trace.CreateObjectNodeTrace
import java.util.*

class CreateObjectNode<T : Any>(runtime: Adapter<T>, private val entries: List<Entry<T>>) : Node<T>(runtime) {

    data class Entry<U : Any>(val key: String, val value: Expression<U>)

    override fun search(input: T?, tracing: Boolean): Result<T> {
        if (runtime.typeOf(input) == JmesPathType.NULL) {
            return Result(input, tracing.whenActive { CreateObjectNodeTrace<T>(null, LinkedHashMap()) })
        } else {
            val newObj = LinkedHashMap<T, T?>()
            val traceItems = if (!tracing) null else LinkedHashMap<T, Result<T>>()
            for (entry in entries) {
                val keyResult = runtime.createString(entry.key)
                val valueResult = entry.value.search(input, tracing)
                newObj.put(keyResult, valueResult.value)
                traceItems?.put(keyResult, valueResult)
            }
            val response = runtime.createObject(newObj)
            return Result(response, tracing.whenActive { CreateObjectNodeTrace(response, traceItems!!) })
        }
    }

    override fun internalToString(): String? {
        val str = StringBuilder("{")
        val entryIterator = entries.iterator()
        while (entryIterator.hasNext()) {
            val entry = entryIterator.next()
            str.append(entry.key).append('=').append(entry.value)
            if (entryIterator.hasNext()) {
                str.append(", ")
            }
        }
        str.append('}')
        return str.toString()
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? CreateObjectNode<*>
        return entries == other?.entries
    }

    override fun internalHashCode(): Int {
        var h = 1
        for (entry in entries) {
            h = h * 31 + entry.hashCode()
        }
        return h
    }
}
