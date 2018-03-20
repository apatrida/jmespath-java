package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.Result
import io.burt.jmespath.trace.ExpressionReferenceNodeTrace

class ExpressionReferenceNode<T: Any>(runtime: Adapter<T>, private val expression: Expression<T>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        val response = expression.search(input, tracing)
        return Result(response.value, tracing.whenActive { ExpressionReferenceNodeTrace(response.value, response) })
    }

    override fun toString(): String {
        return String.format("ExpressionReference(%s)", expression.toString())
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? ExpressionReferenceNode<*>
        return expression == other?.expression
    }

    override fun internalHashCode(): Int {
        return expression.hashCode()
    }
}
