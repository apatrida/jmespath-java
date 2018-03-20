package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Result
import io.burt.jmespath.trace.SliceNodeTrace
import java.util.*

class SliceNode<T: Any>(runtime: Adapter<T>, start: Int?, stop: Int?, step: Int?) : Node<T>(runtime) {
    private val absoluteStart: Boolean = start != null
    private val absoluteStop: Boolean  = stop != null
    private val absoluteStep: Boolean = step != null
    private val step: Int = step ?: 1
    private val limit: Int = if (this.step < 0) -1 else 0
    private val start: Int = start ?: this.limit
    private val stop: Int = stop ?: if (this.step < 0) Integer.MIN_VALUE else Integer.MAX_VALUE
    private val rounding: Int = if (this.step < 0) this.step + 1 else this.step - 1

    override fun search(input: T?, tracing: Boolean): Result<T> {
        val elements = runtime.toList(input)
        val begin = if (start < 0) Math.max(elements.size + start, 0) else Math.min(start, elements.size + limit)
        val end = if (stop < 0) Math.max(elements.size + stop, limit) else Math.min(stop, elements.size)
        val steps = Math.max(0, (end - begin + rounding) / step)
        val output = ArrayList<T>(steps)
        var i = 0
        var offset = begin
        while (i < steps) {
            output.add(elements[offset])
            i++
            offset += step
        }
        val response = runtime.createArray(output)
        return Result(response, tracing.whenActive { SliceNodeTrace(response, start, stop, step) })
    }

    override fun internalToString(): String? {
        return String.format("%s, %s, %s", if (absoluteStart) start else null, if (absoluteStop) stop else null, if (absoluteStep) step else null)
    }

    override fun internalEquals(o: Any): Boolean {
        val other = o as? SliceNode<*>
        return start == other?.start && stop == other.stop && step == other.step
    }

    override fun internalHashCode(): Int {
        var h = 1
        h = h * 31 + start
        h = h * 31 + stop
        h = h * 31 + step
        return h
    }
}
