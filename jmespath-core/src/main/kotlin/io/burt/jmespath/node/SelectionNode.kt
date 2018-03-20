package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.Result
import io.burt.jmespath.trace.IndexedTraceResult
import io.burt.jmespath.trace.SelectionNodeTrace
import java.util.*

class SelectionNode<T: Any>(runtime: Adapter<T>, private val test: Expression<T>, private val projection: Expression<T>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        if (runtime.typeOf(input) == JmesPathType.ARRAY) {
            val inputList = runtime.toList(input)
            val results = ArrayList<T>(inputList.size)
            val traceItems = if (!tracing) null else ArrayList<IndexedTraceResult<T>>()

            inputList.forEachIndexed { idx, inputItem ->
                val filterResult = test.search(inputItem, tracing)
                if (runtime.isTruthy(filterResult.value)) {
                    val result = projection.search(inputItem, tracing)
                    val type = runtime.typeOf(result.value)
                    if (type != JmesPathType.NULL) {
                        results.add(result.value!!)
                        traceItems?.add(IndexedTraceResult(idx, result, filterResult))
                    }
                }
            }
            val response = runtime.createArray(results)
            return Result(response, tracing.whenActive { SelectionNodeTrace(response, traceItems!!) })
        } else {
            return Result(runtime.createNull(), tracing.whenActive { SelectionNodeTrace<T>(null, emptyList()) })
        }
    }

    override fun internalToString(): String? {
        return test.toString()
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? SelectionNode<*>
        return test == other?.test
    }

    override fun internalHashCode(): Int {
        return test.hashCode()
    }
}
