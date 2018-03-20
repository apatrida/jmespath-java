package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.Result
import io.burt.jmespath.trace.FlattenArrayNodeTrace
import java.util.*

class FlattenArrayNode<T: Any>(runtime: Adapter<T>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        return if (runtime.typeOf(input) == JmesPathType.ARRAY) {
            val elements = runtime.toList(input)
            val flattened = LinkedList<T>()
            for (element in elements) {
                if (runtime.typeOf(element) == JmesPathType.ARRAY) {
                    flattened.addAll(runtime.toList(element))
                } else {
                    flattened.add(element)
                }
            }
            val response = runtime.createArray(flattened)
            Result(response, tracing.whenActive { FlattenArrayNodeTrace(response) })
        } else {
            Result(runtime.createNull(), tracing.whenActive { FlattenArrayNodeTrace<T>(null) })
        }
    }

    override fun internalEquals(o: Any): Boolean {
        return o is FlattenArrayNode<*>
    }

    override fun internalHashCode(): Int {
        return 19
    }
}
