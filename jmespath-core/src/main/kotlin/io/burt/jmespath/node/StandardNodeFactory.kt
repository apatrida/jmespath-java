package io.burt.jmespath.node

import io.burt.jmespath.Adapter
import io.burt.jmespath.Expression
import io.burt.jmespath.function.Function

/**
 * This node factory creates instances of the standard node classes.
 */
class StandardNodeFactory<T: Any>(private val runtime: Adapter<T>) : NodeFactory<T> {

    override fun createCurrent(): Node<T> {
        return CurrentNode<T>(runtime)
    }

    override fun createSequence(nodes: List<Node<T>>): Node<T> {
        return SequenceNode<T>(runtime, nodes)
    }

    override fun createProperty(name: String): Node<T> {
        return PropertyNode<T>(runtime, name)
    }

    override fun createIndex(index: Int): Node<T> {
        return IndexNode<T>(runtime, index)
    }

    override fun createSlice(start: Int?, stop: Int?, step: Int?): Node<T> {
        return SliceNode<T>(runtime, start, stop, step)
    }

    override fun createProjection(expression: Expression<T>): Node<T> {
        return ProjectionNode<T>(runtime, expression)
    }

    override fun createFlattenArray(): Node<T> {
        return FlattenArrayNode<T>(runtime)
    }

    override fun createFlattenObject(): Node<T> {
        return FlattenObjectNode<T>(runtime)
    }

    override fun createSelection(test: Expression<T>, projection: Expression<T>): Node<T> {
        return SelectionNode<T>(runtime, test, projection)
    }

    override fun createComparison(operator: Operator, left: Expression<T>, right: Expression<T>): Node<T> {
        return ComparisonNode.create<T>(runtime, operator, left, right)
    }

    override fun createOr(left: Expression<T>, right: Expression<T>): Node<T> {
        return OrNode<T>(runtime, left, right)
    }

    override fun createAnd(left: Expression<T>, right: Expression<T>): Node<T> {
        return AndNode<T>(runtime, left, right)
    }

    override fun createFunctionCall(functionName: String, args: List<Expression<T>>): Node<T> {
        return FunctionCallNode<T>(runtime, runtime.functionRegistry().getFunction(functionName), args)
    }

    override fun createFunctionCall(function: Function?, args: List<Expression<T>>): Node<T> {
        return FunctionCallNode<T>(runtime, function, args)
    }

    override fun createExpressionReference(expression: Expression<T>): Node<T> {
        return ExpressionReferenceNode<T>(runtime, expression)
    }

    override fun createString(str: String): Node<T> {
        return StringNode(runtime, str)
    }

    override fun createNegate(negated: Expression<T>): Node<T> {
        return NegateNode<T>(runtime, negated)
    }

    override fun createCreateObject(entries: List<CreateObjectNode.Entry<T>>): Node<T> {
        return CreateObjectNode<T>(runtime, entries)
    }

    override fun createCreateArray(entries: List<Expression<T>>): Node<T> {
        return CreateArrayNode<T>(runtime, entries)
    }

    override fun createJsonLiteral(json: String): Node<T> {
        return JsonLiteralNode<T>(runtime, json)
    }

    override fun createPipeMarker(): Node<T> {
        return PipeNode<T>(runtime);
    }
}
