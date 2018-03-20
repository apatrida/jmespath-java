package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.Result
import io.burt.jmespath.trace.IndexedTraceResult
import io.burt.jmespath.trace.ProjectionNodeTrace
import java.util.*

class ProjectionNode<T: Any>(runtime: Adapter<T>, private val projection: Expression<T>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        if (runtime.typeOf(input) == JmesPathType.ARRAY) {
            val inputList = runtime.toList(input)
            val results = ArrayList<T>(inputList.size)
            val traceItems = if (!tracing) null else ArrayList<IndexedTraceResult<T>>()

            inputList.forEachIndexed { idx, inputItem ->
                val result = projection.search(inputItem, tracing)
                val type = runtime.typeOf(result.value)
                if (type != JmesPathType.NULL) {
                    results.add(result.value!!)
                    traceItems?.add(IndexedTraceResult(idx, result, null))
                }
            }
            val response = runtime.createArray(results)
            return Result(response, tracing.whenActive { ProjectionNodeTrace(response, traceItems!!) })
        } else {
            return Result(runtime.createNull(), tracing.whenActive { ProjectionNodeTrace<T>(null, emptyList()) })
        }
    }

    override fun internalToString(): String? {
        return projection.toString()
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? ProjectionNode<*>
        return projection == other?.projection
    }

    override fun internalHashCode(): Int {
        return projection.hashCode()
    }
}
