package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.Result
import io.burt.jmespath.trace.ComparisonNodeTrace

abstract class ComparisonNode<T: Any>(runtime: Adapter<T>, left: Expression<T>, right: Expression<T>) : OperatorNode<T>(runtime, left, right) {
    class EqualsNode<T: Any>(runtime: Adapter<T>, left: Expression<T>, right: Expression<T>) : ComparisonNode<T>(runtime, left, right) {
        override fun compareObjects(leftResult: T?, rightResult: T?): T {
            return runtime.createBoolean(comparisonResult(leftResult, rightResult) == 0)
        }

        override fun compareNumbers(leftResult: T?, rightResult: T?): T {
            return runtime.createBoolean(comparisonResult(leftResult, rightResult) == 0)
        }

        override fun operatorToString(): String = "=="
    }

    class NotEqualsNode<T: Any>(runtime: Adapter<T>, left: Expression<T>, right: Expression<T>) : ComparisonNode<T>(runtime, left, right) {

        override fun compareObjects(leftResult: T?, rightResult: T?): T {
            return runtime.createBoolean(comparisonResult(leftResult, rightResult) != 0)
        }

        override fun compareNumbers(leftResult: T?, rightResult: T?): T {
            return runtime.createBoolean(comparisonResult(leftResult, rightResult) != 0)
        }

        override fun operatorToString(): String = "!="
    }

    class GreaterThanNode<T: Any>(runtime: Adapter<T>, left: Expression<T>, right: Expression<T>) : ComparisonNode<T>(runtime, left, right) {

        override fun compareNumbers(leftResult: T?, rightResult: T?): T {
            return runtime.createBoolean(comparisonResult(leftResult, rightResult) > 0)
        }

        override fun operatorToString(): String = ">"
    }

    class GreaterThanOrEqualsNode<T: Any>(runtime: Adapter<T>, left: Expression<T>, right: Expression<T>) : ComparisonNode<T>(runtime, left, right) {

        override fun compareNumbers(leftResult: T?, rightResult: T?): T {
            return runtime.createBoolean(comparisonResult(leftResult, rightResult) >= 0)
        }

        override fun operatorToString(): String = ">="
    }

    class LessThanNode<T: Any>(runtime: Adapter<T>, left: Expression<T>, right: Expression<T>) : ComparisonNode<T>(runtime, left, right) {

        override fun compareNumbers(leftResult: T?, rightResult: T?): T {
            return runtime.createBoolean(comparisonResult(leftResult, rightResult) < 0)
        }

        override fun operatorToString(): String = "<"
    }

    class LessThanOrEqualsNode<T: Any>(runtime: Adapter<T>, left: Expression<T>, right: Expression<T>) : ComparisonNode<T>(runtime, left, right) {

        override fun compareNumbers(leftResult: T?, rightResult: T?): T {
            return runtime.createBoolean(comparisonResult(leftResult, rightResult) <= 0)
        }

        override fun operatorToString(): String = "<="
    }

    override fun search(input: T?, tracing: Boolean): Result<T> {
        val leftResult = operands[0].search(input, tracing)
        val rightResult = operands[1].search(input, tracing)
        val leftType = runtime.typeOf(leftResult.value)
        val rightType = runtime.typeOf(rightResult.value)
        val response = if (leftType == JmesPathType.NUMBER && rightType == JmesPathType.NUMBER) {
            compareNumbers(leftResult.value, rightResult.value)
        } else {
            compareObjects(leftResult.value, rightResult.value)
        }
        val trace = tracing.whenActive { ComparisonNodeTrace(response, leftResult, rightResult) }
        return Result(response, trace)
    }

    protected fun comparisonResult(leftResult: T?, rightResult: T?): Int {
        return runtime.compare(leftResult, rightResult)
    }

    protected open fun compareObjects(leftResult: T?, rightResult: T?): T? {
        return runtime.createNull()
    }

    protected abstract fun compareNumbers(leftResult: T?, rightResult: T?): T

    override fun internalToString(): String? {
        return String.format("%s, %s, %s", operatorToString(), operands[0], operands[1])
    }

    protected abstract fun operatorToString(): String

    override fun internalHashCode(): Int {
        return operatorToString().hashCode()
    }

    companion object {

        fun <U: Any> create(runtime: Adapter<U>, operator: Operator, left: Expression<U>, right: Expression<U>): Node<U> {
            when (operator) {
                Operator.EQUALS -> return EqualsNode(runtime, left, right)
                Operator.NOT_EQUALS -> return NotEqualsNode(runtime, left, right)
                Operator.GREATER_THAN -> return GreaterThanNode(runtime, left, right)
                Operator.GREATER_THAN_OR_EQUALS -> return GreaterThanOrEqualsNode(runtime, left, right)
                Operator.LESS_THAN -> return LessThanNode(runtime, left, right)
                Operator.LESS_THAN_OR_EQUALS -> return LessThanOrEqualsNode(runtime, left, right)
                else -> throw IllegalStateException(String.format("Unknown operator encountered: %s", operator))
            }
        }
    }
}
