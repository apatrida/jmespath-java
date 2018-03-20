package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Result
import io.burt.jmespath.trace.CurrentNodeTrace

class CurrentNode<T: Any>(runtime: Adapter<T>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        return Result(input, tracing.whenActive { CurrentNodeTrace(input) })
    }

    public override fun internalEquals(o: Any): Boolean {
        return (o is CurrentNode<*>)
    }

    override fun internalHashCode(): Int {
        return 17
    }
}
