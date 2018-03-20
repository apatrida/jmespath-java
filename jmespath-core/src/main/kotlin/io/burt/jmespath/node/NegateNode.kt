package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.Result
import io.burt.jmespath.trace.NegateNodeTrace

class NegateNode<T: Any>(runtime: Adapter<T>, private val negated: Expression<T>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        val traceItem = negated.search(input, tracing)
        val response = runtime.createBoolean(!runtime.isTruthy(traceItem.value))
        return Result(response, tracing.whenActive { NegateNodeTrace(response, traceItem) })
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? NegateNode<*>
        return negated == other?.negated
    }

    override fun internalHashCode(): Int {
        return 17 + 31 * negated.hashCode()
    }
}
