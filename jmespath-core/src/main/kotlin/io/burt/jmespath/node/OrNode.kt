package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.Result
import io.burt.jmespath.trace.OrNodeTrace

class OrNode<T : Any>(runtime: Adapter<T>, left: Expression<T>, right: Expression<T>) : OperatorNode<T>(runtime, left, right) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        val leftResult = operands[0].search(input, tracing)
        return if (runtime.isTruthy(leftResult.value)) {
            val trace = tracing.whenActive { OrNodeTrace<T>(runtime.createBoolean(false), leftResult, Result(null, null)) }
            Result(leftResult.value, trace)
        } else {
            val rightResult = operands[1].search(input, tracing)
            val trace = tracing.whenActive { OrNodeTrace(runtime.createBoolean(runtime.isTruthy(rightResult.value)), leftResult, rightResult) }
            Result(rightResult.value, trace)
        }
    }

}
