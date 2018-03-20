package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.Result
import io.burt.jmespath.trace.FlattenObjectNodeTrace

class FlattenObjectNode<T: Any>(runtime: Adapter<T>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        return if (runtime.typeOf(input) == JmesPathType.OBJECT) {
            val response = runtime.createArray(runtime.toList(input))
            Result(response, tracing.whenActive { FlattenObjectNodeTrace(response) })
        } else {
            Result(runtime.createNull(), tracing.whenActive { FlattenObjectNodeTrace<T>(null) })
        }
    }

    override fun internalEquals(o: Any): Boolean {
        return o is FlattenObjectNode<*>
    }

    override fun internalHashCode(): Int {
        return 19
    }
}
