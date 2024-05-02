package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.EntityPosition
import java.util.*
import kotlin.NoSuchElementException

enum class ArithmeticUnaryOp(val string: String) {
    PLUS("+"), MINUS("-"), INVERSION("!"), TILDE("~");

    companion object {
        fun fromString(str: String) = ArithmeticUnaryOp.values().firstOrNull { op ->
            op.string == str
        }
            ?: throw NoSuchElementException("Unknown operator: $str")

    }
}

enum class VariableKind(val string: String) {
    VAR("var"), VAL("val");

    companion object {
        fun fromString(str: String) = VariableKind.values().first { op -> op.string == str }
    }
}

open class Variable(
    open var name: String,
    open var typeReference: TypeReference,
    open val entityPosition: EntityPosition
) : Expression() {
    open val fullName: String
        get() = name

    override fun dumpToString(): String = name
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Variable) return false
        if (name != other.name) return false
        if (typeReference != other.typeReference) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + typeReference.hashCode()
        return result
    }
}

data class ResultVariable(
    override var typeReference: TypeReference,
    override val entityPosition: EntityPosition
) : Variable(name = "result", typeReference, entityPosition)

data class NullVariable(
    override var typeReference: TypeReference,
    override val entityPosition: EntityPosition
) : Variable(name = "null", typeReference, entityPosition)

@Suppress("unused")
class FunctionArgument(
    name: String,
    typeReference: TypeReference,
    val index: Int,
    var annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    var targetAutomaton: AutomatonReference? = null,
    override val entityPosition: EntityPosition
) : Variable(name, typeReference, entityPosition) {
    lateinit var function: Function

    override val fullName: String
        get() = "${function.name}.$name"

    override fun dumpToString(): String = buildString {
        if (annotationUsages.isNotEmpty()) {
            append(
                formatListEmptyLineAtEndIfNeeded(
                    annotationUsages,
                    appendEndLineAtTheEnd = false,
                    onSeparatedLines = false
                )
            )
            append(IPrinter.SPACE)
        }

        append(BackticksPolitics.forIdentifier(name))
        append(": ")
        if (targetAutomaton != null) {
            append(targetAutomaton!!.name)
        } else {
            append(typeReference.name)
            if (typeReference.genericReferences.size > 0) {
                append("<")
                val lastIndex = typeReference.genericReferences.size - 1
                for (i in 0 until lastIndex) {
                    appendGeneric(this, i, true)
                }
                appendGeneric(this, lastIndex, false)
                append(">")
            }
        }
    }

    private fun appendGeneric(t: StringBuilder, i: Int, addComma: Boolean) {
        val type =
            typeReference.genericReferences[i].name + if (addComma) ", " else ""
        t.append(type)
    }
}

@Suppress("unused")
class ActionParameter(
    name: String,
    typeReference: TypeReference,
    val index: Int,
    var annotation: AnnotationReference? = null,
    override val entityPosition: EntityPosition
) : Variable(name, typeReference, entityPosition)

class ConstructorArgument(
    val keyword: VariableKind,
    name: String,
    typeReference: TypeReference,
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    val initialValue: Expression?,
    override val entityPosition: EntityPosition
) : Variable(name, typeReference, entityPosition) {
    lateinit var automaton: Automaton

    override val fullName: String
        get() = "${automaton.name}.$name"

    override fun dumpToString(): String = buildString {
        if (annotationUsages.isNotEmpty()) {
            append(formatListEmptyLineAtEndIfNeeded(annotationUsages, onSeparatedLines = false))
            append(IPrinter.SPACE)
        }
        append("${keyword.string} ${BackticksPolitics.forIdentifier(name)}: ")
        append(BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL))
        if (initialValue != null) {
            append(" = ${initialValue.dumpToString()};")
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class VariableWithInitialValue(
    val keyword: VariableKind,
    name: String,
    typeReference: TypeReference,
    val annotationUsage: MutableList<AnnotationUsage> = mutableListOf(),
    val initialValue: Expression?,
    override val entityPosition: EntityPosition
) : Variable(name, typeReference, entityPosition) {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsage))
        append("${keyword.string} ${BackticksPolitics.forIdentifier(name)}: ")

        if (typeReference.resolve()?.fullName != null)
            appendGeneric(this, typeReference)
        else
            append(UNRESOLVED_TYPE_SYMBOL)

        if (initialValue != null) {
            append(" = ${initialValue.dumpToString()};")
        } else {
            append(";")
        }
    }

    private fun appendGeneric(stringBuilder: StringBuilder, typeReference: TypeReference) {

        // stringBuilder.append(if (typeReference!!.isPointer) "*" else "")

        val queue = LinkedList<Pair<TypeReference, Int>>()

        queue.addLast(Pair(typeReference, 0))

        appendGenericsToQueue(queue, 1)
        var prevDeepLevel = 0

        val mainType = queue.removeFirst()
        stringBuilder.append(mainType.first.name)
        var counterOfClosedBrackets = 0

        while (queue.isNotEmpty()) {

            val currentTypeRef = queue.peek().first
            val currentDeepLevel = queue.poll().second

            if (currentDeepLevel > prevDeepLevel) {
                stringBuilder.append("<${currentTypeRef.name}")
                ++counterOfClosedBrackets
            }

            if (currentDeepLevel == prevDeepLevel) {
                stringBuilder.append(", ${currentTypeRef.name}")
            }

            if (currentDeepLevel < prevDeepLevel){
                stringBuilder.append(">, ${currentTypeRef.name}")
                --counterOfClosedBrackets
            }

            prevDeepLevel = currentDeepLevel
        }
        while (counterOfClosedBrackets != 0) {
            stringBuilder.append(">")
            --counterOfClosedBrackets
        }
    }

    private fun appendGenericsToQueue(queue: LinkedList<Pair<TypeReference, Int>>, deep: Int) {
        val genericReferences = queue.peekLast().first.genericReferences
        if (genericReferences.isEmpty()) return
        genericReferences.forEach {
            queue.addLast(Pair(it, deep))
            appendGenericsToQueue(queue, deep + 1)
        }
    }
}
