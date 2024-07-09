package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.context.ActionContext
import org.jetbrains.research.libsl.nodes.helpers.appendGeneric
import org.jetbrains.research.libsl.nodes.helpers.appendWhereSection
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.toSimpleString
import org.jetbrains.research.libsl.type.GenericType
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.EntityPosition

data class ActionDecl(
    val name: String,
    val argumentDescriptors: MutableList<ActionArgumentDescriptor> = mutableListOf(),
    val annotations: MutableList<AnnotationUsage> = mutableListOf(),
    val context: ActionContext,
    val returnType: TypeReference?,
    val entityPosition: EntityPosition
) : IPrinter {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotations))
        append("define action ")
        val actionGenericTypes = context.getActionGenericTypes()

        if (actionGenericTypes.isNotEmpty()) {
            append("<")
            append(actionGenericTypes.joinToString(separator = ", "))
            append("> ")
        }
        append("${BackticksPolitics.forIdentifier(name)}")
        if (argumentDescriptors.isNotEmpty()) {
            appendLine("(")
            appendLine(
                withIndent(
                    argumentDescriptors.joinToString(
                        separator = ",\n",
                        transform = ActionArgumentDescriptor::dumpToString
                    )
                )
            )
            append(")")
        }

        if (returnType != null) {
            append(": ")
            if (actionGenericTypes.contains(
                    GenericType(
                        returnType.toSimpleString(),
                        context = context
                    )
                )
            ) {
                append(returnType.toSimpleString())
            } else {
                if (returnType.resolve()?.fullName != null)
                    appendGeneric(this, returnType)
                else
                    append(Type.UNRESOLVED_TYPE_SYMBOL)
            }
        }

        if (actionGenericTypes.isNotEmpty()) {
            appendWhereSection(this, actionGenericTypes)
        }
        appendLine(";")
    }
}

data class ActionArgumentDescriptor(
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    val name: String,
    val typeReference: TypeReference,
    val entityPosition: EntityPosition
) : IPrinter {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        val type = BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: Type.UNRESOLVED_TYPE_SYMBOL)
        append("${BackticksPolitics.forIdentifier(name)}: $type")
    }
}
