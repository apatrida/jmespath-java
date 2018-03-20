package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.trace.TraceNode

abstract class Node<T: Any>(override val runtime: Adapter<T>) : Expression<T> {
    override fun toString(): String {
        val extraArgs = internalToString()
        val str = StringBuilder()
        val name = javaClass.name
        str.append(name.substring(name.lastIndexOf('.') + 1))
        str.delete(str.length - 4, str.length)
        str.append('(')
        if (extraArgs != null) {
            str.append(extraArgs)
        }
        str.append(')')
        return str.toString()
    }

    inline fun <K: Any, T: TraceNode<K>> Boolean.whenActive(calc: ()->T) = if (this) calc() else null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        return if (!javaClass.isInstance(other)) {
            false
        } else {
            internalEquals(other)
        }
    }

    override fun hashCode(): Int {
        return internalHashCode()
    }

    protected open fun internalToString(): String? = null
    protected abstract fun internalEquals(o: Any): Boolean
    protected abstract fun internalHashCode(): Int
}
