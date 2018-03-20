package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.Result
import io.burt.jmespath.function.Function
import io.burt.jmespath.function.FunctionArgument
import io.burt.jmespath.trace.FunctionCallNodeTrace
import java.util.*

class FunctionCallNode<T: Any>(runtime: Adapter<T>, private val implementation: Function?, private val args: List<Expression<T>>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        val arguments = ArrayList<FunctionArgument<T>>(args.size)
        val traceItems = if (!tracing) null else ArrayList<Result<T>>(args.size)
        for (arg in args) {
            if (arg is ExpressionReferenceNode<*>) {
                // TODO: how do we do a trace on this since it isn't resolved now
                arguments.add(FunctionArgument.of(arg))
            } else {
                val argItem = arg.search(input, tracing)
                arguments.add(FunctionArgument.of<T>(argItem.value))
                traceItems?.add(argItem)
            }
        }
        val response = implementation?.call(runtime, arguments)
        return Result(response, tracing.whenActive { FunctionCallNodeTrace(response, traceItems!!) })
    }

    override fun internalToString(): String? {
        val str = StringBuilder()
        str.append(implementation?.name() ?: "")
        str.append(", [")
        val argIterator = args.iterator()
        while (argIterator.hasNext()) {
            val arg = argIterator.next()
            str.append(arg)
            if (argIterator.hasNext()) {
                str.append(", ")
            }
        }
        str.append(']')
        return str.toString()
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? FunctionCallNode<*>
        return implementation?.name() == other?.implementation?.name() && args == other?.args
    }

    override fun internalHashCode(): Int {
        var h = 31 + (implementation?.name()?.hashCode() ?: 881)
        for (node in args) {
            h = h * 31 + node.hashCode()
        }
        return h
    }
}
