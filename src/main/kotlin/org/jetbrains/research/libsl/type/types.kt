package org.jetbrains.research.libsl.type

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.helpers.appendGeneric
import org.jetbrains.research.libsl.nodes.helpers.appendWhereSection
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.EntityPosition

sealed interface Type : IPrinter {
    val name: String
    val isPointer: Boolean
    val context: LslContextBase

    val generics: MutableList<TypeReference>

    val fullName: String
        get() = buildString {
            append(if (isPointer) "*" else "")
            append(name)
            if (generics.isNotEmpty()) {
                append("<")
                append(generics.joinToString(separator = ", ") {
                    it.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL
                })
                append(">")
            }
        }

    val isArray: Boolean
        get() = (this as? TypeAlias)?.originalType?.resolve()?.isArray == true || this is ArrayType

    val isTopLevelType: Boolean
        get() = false

    val isTypeBlockType: Boolean
        get() = false

    companion object {
        const val UNRESOLVED_TYPE_SYMBOL = "`<UNRESOLVED_TYPE>`"
    }
}

sealed interface LibslType : Type
data class RealType(
    val nameParts: List<String>,
    override val isPointer: Boolean = false,
    override val generics: MutableList<TypeReference>,
    override val context: LslContextBase,
    val entityPosition: EntityPosition
) : Type {
    override val name: String
        get() = nameParts.joinToString(".")

    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = fullName
}

data class SimpleType(
    override val name: String,
    val realType: Type,
    val annotationUsages: MutableList<AnnotationUsage>,
    override val isPointer: Boolean = false,
    override val context: LslContextBase,
    val entityPosition: EntityPosition
) : LibslType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val isTypeBlockType: Boolean = true

    override fun dumpToString(): String {
        return buildString {
            append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
            append("${BackticksPolitics.forTypeIdentifier(name)}(${BackticksPolitics.forTypeIdentifier(realType.fullName)});")
        }
    }

    override fun toString() = dumpToString()
}

data class TypeAlias(
    override val name: String,
    val originalType: TypeReference,
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    override val context: LslContextBase,
    val entityPosition: EntityPosition
) : LibslType {
    override val isPointer: Boolean = false
    override val generics: MutableList<TypeReference> = mutableListOf()

    override val isTopLevelType: Boolean = true

    override fun dumpToString(): String {
        return buildString {
            append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
            append("typealias ")
            append(BackticksPolitics.forTypeIdentifier(name))
            append(" = ")
            appendGeneric(this, originalType)
            append(";")
        }
    }

    override fun toString() = dumpToString()
}

data class EnumLikeSemanticType(
    override val name: String,
    val type: Type,
    val entries: Map<String, Atomic>,
    val annotationUsages: MutableList<AnnotationUsage>,
    override val context: LslContextBase,
    val entityPosition: EntityPosition
) : LibslType {
    override val isPointer: Boolean = false
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val isTypeBlockType: Boolean = true

    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        appendLine("$name(${type.fullName}) {")
        val formattedEntries = entries.map { (k, v) -> "${BackticksPolitics.forIdentifier(k)}: ${v.dumpToString()}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }

    override fun toString() = dumpToString()
}

data class StructuredType(
    override val name: String,
    val variables: MutableList<Variable> = mutableListOf(),
    val functions: MutableList<Function> = mutableListOf(),
    val genericsTypes: MutableList<GenericType> = mutableListOf(),
    val isTypeIdentifier: String?,
    val forTypeList: MutableList<String> = mutableListOf(),
    val annotationUsages: MutableList<AnnotationUsage>,
    override val context: LslContextBase,
    val entityPosition: EntityPosition,
    override val generics: MutableList<TypeReference>
) : Type {
    override val isPointer: Boolean = false
    override val isTopLevelType: Boolean = true

    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        append("type $name ")
        if (genericsTypes.isNotEmpty()) {
            append("<")
            append(genericsTypes.joinToString(separator = ", "))
            append("> ")
        }
        if (isTypeIdentifier != null) {
            append("is $isTypeIdentifier ")
        }
        if (forTypeList.isNotEmpty()) {
            append("for ")
            append(forTypeList.joinToString(separator = ", "))
            append(" ")
        }
        if (genericsTypes.isNotEmpty()) {
            appendWhereSection(this, genericsTypes)
        }
        appendLine("{")
        variables.forEach { v ->
            appendLine(withIndent(v.dumpToString()))
        }
        functions.forEach { f ->
            appendLine(withIndent(f.dumpToString()))
        }
        appendLine("}")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructuredType) return false

        if (name != other.name) return false
        if (generics != other.generics) return false
        if (variables != other.variables) return false
        if (functions != other.functions) return false
        if (context != other.context) return false
        if (isPointer != other.isPointer) return false
        if (isTopLevelType != other.isTopLevelType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + isPointer.hashCode()
        result = 31 * result + isTopLevelType.hashCode()
        return result
    }

    override fun toString() = dumpToString()
}

data class EnumType(
    override val name: String,
    val entries: Map<String, Atomic>,
    val annotationUsages: MutableList<AnnotationUsage>,
    override val context: LslContextBase,
    val entityPosition: EntityPosition
) : Type {
    override val isPointer: Boolean = false
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val isTopLevelType: Boolean = true

    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        appendLine("enum $name {")
        val formattedEntries = entries.map { (k, v) -> "${BackticksPolitics.forIdentifier(k)} = ${v.dumpToString()}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }

    override fun toString() = dumpToString()
}

data class ArrayType(
    override val isPointer: Boolean = false,
    override val generics: MutableList<TypeReference>,
    override val context: LslContextBase
) : Type {
    override val name: String = "array"

    override fun dumpToString(): String {
        return BackticksPolitics.forTypeIdentifier(fullName)
    }

    override fun toString() = dumpToString()
}

data class ListType(
    override val isPointer: Boolean = false,
    override val generics: MutableList<TypeReference>,
    override val context: LslContextBase
) : Type {
    override val name: String = "list"

    override fun dumpToString(): String {
        return BackticksPolitics.forTypeIdentifier(fullName)
    }

    override fun toString() = dumpToString()
}

data class MapType(
    override val isPointer: Boolean = false,
    override val generics: MutableList<TypeReference>,
    override val context: LslContextBase
) : Type {
    override val name: String = "map"

    override fun dumpToString(): String {
        return BackticksPolitics.forTypeIdentifier(fullName)
    }

    override fun toString() = dumpToString()
}

data class NullType(
    override val isPointer: Boolean = false,
    override val generics: MutableList<TypeReference> = mutableListOf(),
    override val context: LslContextBase
) : Type {
    override val name: String = "null"

    override fun dumpToString(): String {
        return BackticksPolitics.forTypeIdentifier(fullName)
    }

    override fun toString() = dumpToString()
}

enum class GenericTypeBound(val string: String) {
    IN("in"), OUT("out"), EMPTY("");

    companion object {
        fun fromString(str: String) = values().first { op -> op.string == str }
    }
}

data class GenericType(
    override val name: String,
    override val isPointer: Boolean = false,
    override val generics: MutableList<TypeReference> = mutableListOf(),
    val constraints: MutableList<TypeReference> = mutableListOf(),
    override val context: LslContextBase
) : Type {
    override fun dumpToString(): String {
        return BackticksPolitics.forTypeIdentifier(fullName)
    }

    override fun toString() = dumpToString()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenericType

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}