package io.burt.jmespath.node

import io.burt.jmespath.Expression
import io.burt.jmespath.function.Function

/**
 * A node factory is used by the expression compiler to create AST nodes.
 */
interface NodeFactory<T: Any> {
    fun createCurrent(): Node<T>

    fun createProperty(name: String): Node<T>

    fun createIndex(index: Int): Node<T>

    fun createSlice(start: Int?, stop: Int?, step: Int?): Node<T>

    fun createProjection(expression: Expression<T>): Node<T>

    fun createFlattenArray(): Node<T>

    fun createFlattenObject(): Node<T>

    fun createSelection(test: Expression<T>, projection: Expression<T>): Node<T>

    fun createComparison(operator: Operator, left: Expression<T>, right: Expression<T>): Node<T>

    fun createOr(left: Expression<T>, right: Expression<T>): Node<T>

    fun createAnd(left: Expression<T>, right: Expression<T>): Node<T>

    fun createFunctionCall(functionName: String, args: List<Expression<T>>): Node<T>

    fun createFunctionCall(function: Function?, args: List<Expression<T>>): Node<T>

    fun createExpressionReference(expression: Expression<T>): Node<T>

    fun createString(str: String): Node<T>

    fun createNegate(negated: Expression<T>): Node<T>

    fun createCreateObject(entries: List<CreateObjectNode.Entry<T>>): Node<T>

    fun createCreateArray(entries: List<Expression<T>>): Node<T>

    fun createJsonLiteral(json: String): Node<T>

    fun createSequence(nodes: List<Node<T>>): Node<T>

    fun createPipeMarker(): Node<T>
}
