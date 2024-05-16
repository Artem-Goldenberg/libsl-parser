package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.nodes.helpers.appendGeneric
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.GenericType
import org.jetbrains.research.libsl.type.GenericTypeBound
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.EntityPosition

open class Function(
    open val kind: FunctionKind,
    open val name: String,
    open val automatonReference: AutomatonReference?,
    open var args: MutableList<FunctionArgument> = mutableListOf(),
    open val returnType: TypeReference?,
    open val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    open var contracts: MutableList<Contract> = mutableListOf(),
    open var statements: MutableList<Statement> = mutableListOf(),
    open var hasBody: Boolean = statements.isNotEmpty(),
    open var targetAutomatonRef: AutomatonReference? = null,
    open val context: FunctionContext,
    open val isMethod: Boolean,
    val isStatic: Boolean,
    open val entityPosition: EntityPosition
) : Node() {
    val fullName: String
        get() = if (automatonReference?.name?.isEmpty() == true) "${automatonReference!!.name}.$name" else name

    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        if (isStatic) {
            append("static ")
        }
        val functionGenerics: MutableList<GenericType> = context.getFunctionGenericTypes()

        append("${kind.value} ")
        if (isMethod) {
            append("*.")
        }
        append(BackticksPolitics.forIdentifier(name))

        if (functionGenerics.isNotEmpty()) {
            append(" <")
            append(functionGenerics.joinToString(separator = ", "))
            append("> ")
        }

        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> arg.dumpToString() }
        )

        if (returnType != null) {
            append(": ")
            if (functionGenerics.contains(
                    GenericType(
                        returnType!!.name,
                        typeBound = GenericTypeBound.EMPTY,
                        constraints = mutableListOf(),
                        context = context
                    )
                )
            ) {
                append(returnType!!.name)
            } else {
                if (returnType!!.resolve()?.fullName != null)
                    appendGeneric(this)
                else
                    append(UNRESOLVED_TYPE_SYMBOL)
            }
        }

        if (functionGenerics.isNotEmpty()) {
            var isWhereWasAdded = false
            for (generic in functionGenerics) {
                if (generic.constraints.size > 0) {
                    if (!isWhereWasAdded) {
                        append(" where")
                        isWhereWasAdded = true
                    }
                    for (constraint: TypeReference in generic.constraints) {
                        val typeBound =
                            if (!GenericTypeBound.EMPTY.equals(generic.typeBound)) generic.typeBound.string + " " else ""
                        append(" " + generic.name + ": " + typeBound)
                        appendGeneric(this, constraint)
                        append(",")
                    }
                }
            }
            if (isWhereWasAdded)
                deleteCharAt(this.length - 1)
        }
        if (!hasBody && contracts.isEmpty()) {
            appendLine(";")
        } else {
            appendLine(" {")
            hasBody = true

            if (contracts.isNotEmpty()) {
                append(withIndent(formatListEmptyLineAtEndIfNeeded(contracts)))
            }

            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
            appendLine("}")
        }
    }

    private fun appendGeneric(stringBuilder: StringBuilder) {
        stringBuilder.append(returnType!!.name)
        stringBuilder.append(if (returnType!!.isPointer) "*" else "")
        if (returnType!!.genericReferences.isNotEmpty()) {
            stringBuilder.append("<")
            stringBuilder.append(returnType!!.genericReferences.joinToString(separator = ", ") {
                it.name
            })
            stringBuilder.append(">")
        }
    }
}

enum class FunctionKind(val value: String) {
    FUNCTION("fun"), CONSTRUCTOR("constructor"), DESTRUCTOR("destructor"), PROC("proc");

    companion object {
        fun fromString(str: String) = FunctionKind.values().first { k -> k.value == str }
    }
}

data class Constructor(
    override val name: String,
    override var args: MutableList<FunctionArgument> = mutableListOf(),
    override val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    override var contracts: MutableList<Contract> = mutableListOf(),
    override var statements: MutableList<Statement> = mutableListOf(),
    override var hasBody: Boolean = statements.isNotEmpty(),
    override val context: FunctionContext,
    override val isMethod: Boolean,
    override val entityPosition: EntityPosition
) : Function(
    kind = FunctionKind.CONSTRUCTOR, name, automatonReference = null, args, returnType = null,
    annotationUsages, contracts,
    statements, hasBody, null, context, false, isMethod, entityPosition
)

class Destructor(
    override val name: String,
    override var args: MutableList<FunctionArgument> = mutableListOf(),
    override val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    override var contracts: MutableList<Contract> = mutableListOf(),
    override var statements: MutableList<Statement> = mutableListOf(),
    override var hasBody: Boolean = statements.isNotEmpty(),
    override val context: FunctionContext,
    override val isMethod: Boolean,
    override val entityPosition: EntityPosition
) : Function(
    kind = FunctionKind.DESTRUCTOR, name, null, args, null,
    annotationUsages, contracts,
    statements, hasBody, null, context, false, isMethod, entityPosition
)

class Procedure(
    override val name: String,
    override var args: MutableList<FunctionArgument> = mutableListOf(),
    override val returnType: TypeReference?,
    override val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    override var contracts: MutableList<Contract> = mutableListOf(),
    override var statements: MutableList<Statement> = mutableListOf(),
    override var hasBody: Boolean = statements.isNotEmpty(),
    override val context: FunctionContext,
    override val isMethod: Boolean,
    override val entityPosition: EntityPosition
) : Function(
    kind = FunctionKind.PROC, name, null, args, returnType,
    annotationUsages, contracts,
    statements, hasBody, null, context, false, isMethod, entityPosition
)
