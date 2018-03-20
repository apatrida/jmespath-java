package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression

abstract class OperatorNode<T: Any> (runtime: Adapter<T>, protected vararg val operands: Expression<T>) : Node<T>(runtime) {
    override fun internalToString(): String? {
        val operandsString = StringBuilder()
        val operandIterator = operands.iterator()
        while (operandIterator.hasNext()) {
            val operand = operandIterator.next()
            operandsString.append(operand)
            if (operandIterator.hasNext()) {
                operandsString.append(", ")
            }
        }
        return operandsString.toString()
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? OperatorNode<*>
        return operands === other?.operands || (other != null && operands.contentEquals(other.operands))
    }

    override fun internalHashCode(): Int {
        return operands.contentHashCode()
    }
}
