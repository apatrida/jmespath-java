package io.burt.jmespath.trace

import io.burt.jmespath.Adapter
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.Result
import java.util.*

data class TraceResult<T: Any>(val root: TraceNode<T>, protected val adapter: Adapter<T>) {
    val queriedPaths: List<String> by lazy { root.queriedPaths(adapter) }
    val queriedJsonPathCompatiblePaths: List<String> by lazy {
        convertToNormalizedJsonPath(queriedPaths)
    }
}

sealed class TraceNode<T : Any>() {
    abstract val value: T?

    fun Stack<String>.popUntilDepth(depth: Int): Unit {
        while (this.size > depth) pop()
    }

    fun Stack<String>.build(): String {
        val builder = StringBuilder()
        var idx = 0
        for (item in this) {
            if (item.isEmpty()) {
                continue
            } else if (idx > 0 && item[0] != '[') {
                builder.append(".").append(item)
            } else {
                builder.append(item)
            }
            idx++
        }
        return builder.toString()
    }

    fun Adapter<T>.isValueable(test: T?): Boolean = typeOf(test) == JmesPathType.BOOLEAN || isTruthy(test)

    fun queriedPaths(adapter: Adapter<T>): List<String> {
        val result = queriedPaths(adapter, this, 0, Stack())
        return if (adapter.isValueable(this.value)) {
            if (result.sequenceTerminalStateMuted) commonPrefix(result.paths) else result.paths
        } else emptyList()
    }

    val isStopType: Boolean
        get() = when (this) {
            is CreateArrayNodeTrace,
            is CreateObjectNodeTrace,
            is ExpressionReferenceNodeTrace,
            is FlattenArrayNodeTrace,
            is FlattenObjectNodeTrace,
            is FunctionCallNodeTrace,
            is JsonLiteralNodeTrace,
            is NegateNodeTrace,
            is PipeNodeTrace,
            is SliceNodeTrace,
            is StringNodeTrace -> true
            else -> false
        }

    // TODO: slice node could survive tracing, similar to how we track indexes in projection and selection

    val isCollapseType: Boolean
        get() = isStopType || isPruneType

    val isPruneType: Boolean
        get() = when (this) {
            is AndNodeTrace,
            is ComparisonNodeTrace,
            is OrNodeTrace -> true
            else -> false
        }

    private data class InternalQueryResponse(val paths: List<String>, val sequenceTerminalStateMuted: Boolean = false) {
        val isEmptyResponse = paths.isEmpty()
        val hasResponse = paths.isNotEmpty()
    }

    private val emptyQueryResponse = InternalQueryResponse(emptyList())

    private fun queriedPaths(adapter: Adapter<T>, node: TraceNode<T>, indent: Int, currentPath: Stack<String>): InternalQueryResponse {
        fun hasValue(): Boolean {
            return adapter.isValueable(this.value)
        }


        when (node) {
            is IndexNodeTrace -> {
                /*
                    for any indexed node, we push the index, then we flush the path
                */
                currentPath.push("[${node.index}]")
                return if (adapter.isValueable(node.value)) InternalQueryResponse(listOf(currentPath.build())) else emptyQueryResponse
            }

            is PropertyNodeTrace -> {
                /*
                    for any property node, we push the property name, then we flush the path
                */
                currentPath.push(node.propertyName)
                return if (adapter.isValueable(node.value)) InternalQueryResponse(listOf(currentPath.build())) else emptyQueryResponse
            }

            is CurrentNodeTrace -> {
                /*
                    this is a noop, but likely is traceable, so we just flush the path and see
                 */
                return if (adapter.isValueable(node.value)) InternalQueryResponse(listOf(currentPath.build())) else emptyQueryResponse
            }

            is SequenceNodeTrace -> {
                /*
                    during a sequence, create provisional flush tank, we mark stack depth:
                        until we hit a STOP:
                            we can build up the stack AND collect flushed paths into provisional
                            otherwise collect flushed paths into dump tank
                        we record the latest value
                    at end of sequence:
                        if the latest value is TRUTHY we can flush the provisional paths
                        we always reset stack to old depth
                */
                val collected = if (adapter.isValueable(node.value)) {
                    var provisional = emptyQueryResponse
                    val resetDepth = currentPath.size
                    var muted = false
                    for ((stepIdx, step) in node.steps.withIndex()) {
                        muted = muted || step.traceNode?.isStopType ?: false
                        val pruned = step.traceNode?.isPruneType ?: true
                        val ignored = !adapter.isValueable(step.value)

                        val nested = if (muted || pruned || ignored) {
                            InternalQueryResponse(emptyList(), muted)
                        } else {
                            step.traceNode?.let { queriedPaths(adapter, it, indent + 4, currentPath) } ?: emptyQueryResponse
                        }

                        // a embedded sequence that entered mute state should mute us
                        if (nested.sequenceTerminalStateMuted) {
                            muted = true
                        }

                        if (nested.hasResponse) {
                            provisional = nested
                        }

                        if (step.traceNode?.isCollapseType ?: false) {
                            provisional = provisional.copy(paths = commonPrefix(provisional.paths))
                        }
                    }
                    currentPath.popUntilDepth(resetDepth)
                    if (!muted) provisional else provisional.copy(sequenceTerminalStateMuted = true)
                } else {
                    emptyQueryResponse
                }
                return collected
            }

            is SelectionNodeTrace,
            is ProjectionNodeTrace -> {
                /*
                    during a projection/selection, for each item:
                        we mark stack depth
                        we add to the stack
                        create provisional flush tank
                        do sub actions
                        flush provisional tank
                        reset stack to old depth
                 */
                val collected = arrayListOf<String>()
                val sourceItems = when (node) {
                    is SelectionNodeTrace -> node.selections
                    is ProjectionNodeTrace -> node.items
                    else -> throw IllegalStateException("Unhandled projection type ${node.javaClass.name}")
                }

                for ((itemIdx, indexedItem) in sourceItems.withIndex()) {
                    val item = indexedItem.result
                    val originalIndex = indexedItem.idx

                    val muted = item.traceNode?.isStopType ?: false
                    val pruned = item.traceNode?.isPruneType ?: true
                    val ignored = !adapter.isValueable(item.value)

                    if (muted || pruned || ignored) {
                        // do nothing
                    } else {
                        val resetDepth = currentPath.size
                        currentPath.push("[${originalIndex}]")

                        val provisional = item.traceNode?.let { queriedPaths(adapter, it, indent + 4, currentPath) } ?: emptyQueryResponse
                        if (provisional.hasResponse) {
                            collected.addAll(provisional.paths)
                        }
                        currentPath.popUntilDepth(resetDepth)
                    }
                }

                return InternalQueryResponse(collected)
            }

            else -> {
                // ignored
                return emptyQueryResponse
            }
        }
    }

}

data class AndNodeTrace<T : Any>(override val value: T?, val leftOperand: Result<T>, val rightOperand: Result<T>) : TraceNode<T>()
data class ComparisonNodeTrace<T : Any>(override val value: T?, val leftOperand: Result<T>, val rightOperand: Result<T>) : TraceNode<T>()
data class CreateArrayNodeTrace<T : Any>(override val value: T?, val items: List<Result<T>>) : TraceNode<T>()
data class CreateObjectNodeTrace<T : Any>(override val value: T?, val items: LinkedHashMap<T, Result<T>>) : TraceNode<T>()
data class CurrentNodeTrace<T : Any>(override val value: T?) : TraceNode<T>()
data class ExpressionReferenceNodeTrace<T : Any>(override val value: T?, val original: Result<T>) : TraceNode<T>()
data class FlattenArrayNodeTrace<T : Any>(override val value: T?) : TraceNode<T>()
data class FlattenObjectNodeTrace<T : Any>(override val value: T?) : TraceNode<T>()
data class FunctionCallNodeTrace<T : Any>(override val value: T?, val args: List<Result<T>>) : TraceNode<T>()
data class IndexNodeTrace<T : Any>(override val value: T?, val index: Int) : TraceNode<T>()
data class JsonLiteralNodeTrace<T : Any>(override val value: T?) : TraceNode<T>()
data class NegateNodeTrace<T : Any>(override val value: T?, val original: Result<T>) : TraceNode<T>()
data class OrNodeTrace<T : Any>(override val value: T, val leftOperand: Result<T>, val rightOperand: Result<T>) : TraceNode<T>()
data class PipeNodeTrace<T : Any>(override val value: T?) : TraceNode<T>()
data class ProjectionNodeTrace<T : Any>(override val value: T?, val items: List<IndexedTraceResult<T>>) : TraceNode<T>()
data class PropertyNodeTrace<T : Any>(override val value: T?, val propertyName: String) : TraceNode<T>()
data class SelectionNodeTrace<T : Any>(override val value: T?, val selections: List<IndexedTraceResult<T>>) : TraceNode<T>()
data class SequenceNodeTrace<T : Any>(override val value: T?, val steps: List<Result<T>>) : TraceNode<T>()
data class SliceNodeTrace<T : Any>(override val value: T?, val start: Int, val stop: Int, val step: Int) : TraceNode<T>()
data class StringNodeTrace<T : Any>(override val value: T?) : TraceNode<T>()

data class IndexedTraceResult<T: Any>(val idx: Int, val result: Result<T>, val filteredBy: Result<T>? = null)

fun commonPrefix(srcList: List<String>): List<String> {
    if (srcList.size <= 1) return srcList
    val sample = srcList.first()
    var lastIndex = 0
    var lastGood = ""
    while (true) {
        val firstIndex = sample.indexOfAny(charArrayOf('.', '['), startIndex = lastIndex)
        if (firstIndex < 0) {
            // we made it to the end, they are all the same?
            return listOf(sample)
        }
        else {
            val prefix = sample.substring(0, firstIndex)
            if (srcList.all { it.startsWith(prefix + ".") || it.startsWith(prefix + "[") }) {
                lastIndex = firstIndex+1
                lastGood = prefix
            } else {
                return if (lastGood.isBlank()) emptyList() else listOf(lastGood)
            }
        }
    }
}

fun convertToNormalizedJsonPath(srcList: List<String>): List<String> {
    return srcList.map {
        it.split('.').map { part ->
            if (part.contains('[')) {
                val first = part.substringBefore('[')
                val after = part.substring(first.length)
                "['$first']$after"
            } else {
                "['$part']"
            }
        }.joinToString("", prefix = "$")
    }
}