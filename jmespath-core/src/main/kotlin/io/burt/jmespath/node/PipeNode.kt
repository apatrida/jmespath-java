package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Result
import io.burt.jmespath.trace.PipeNodeTrace

class PipeNode<T: Any>(runtime: Adapter<T>) : Node<T>(runtime) {

    override fun search(input: T?, tracing: Boolean): Result<T> {
        return Result(input, tracing.whenActive { PipeNodeTrace(input) })
    }

    public override fun internalEquals(o: Any): Boolean {
        return (o is PipeNode<*>)
    }

    override fun internalHashCode(): Int {
        return 919
    }
}
