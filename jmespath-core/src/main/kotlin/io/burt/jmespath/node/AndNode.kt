package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.Result
import io.burt.jmespath.trace.AndNodeTrace

class AndNode<T: Any>(adapter: Adapter<T>, left: Expression<T>, right: Expression<T>) : OperatorNode<T>(adapter, left, right) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        val leftResult = operands[0].search(input, tracing)
        return if (runtime.isTruthy(leftResult.value)) {
            val rightResult = operands[1].search(input, tracing)
            val trace = tracing.whenActive { AndNodeTrace(runtime.createBoolean(runtime.isTruthy(rightResult.value)), leftResult, rightResult) }
            Result(rightResult.value, trace)
        } else {
            val trace = tracing.whenActive { AndNodeTrace<T>(runtime.createBoolean(false), leftResult, Result(null,null)) }
            Result(leftResult.value, trace)
        }
    }
}
