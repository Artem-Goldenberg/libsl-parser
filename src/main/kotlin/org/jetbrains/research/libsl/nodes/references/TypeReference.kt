package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.type.*

abstract class TypeReference(
    override val context: LslContextBase
) : LslReference<Type, TypeReference> {

    override abstract fun isReferenceMatchWithNode(node: Type): Boolean

    override abstract fun resolve(): Type?
}

data class UnionExpressionTypeReference(
    val left: TypeReference,
    val right: TypeReference,
    override val context: LslContextBase
) : TypeReference(context = context) {
    override fun resolve(): Type? {
        TODO("Not yet implemented")
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSameReference(other: TypeReference): Boolean {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnionExpressionTypeReference

        if (left != other.left) return false
        if (right != other.right) return false
        if (context != other.context) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + right.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }


}

data class IntersectionExpressionTypeReference(
    val left: TypeReference,
    val right: TypeReference,
    override val context: LslContextBase
) : TypeReference(context = context) {
    override fun resolve(): Type? {
        TODO("Not yet implemented")
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSameReference(other: TypeReference): Boolean {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntersectionExpressionTypeReference

        if (left != other.left) return false
        if (right != other.right) return false
        if (context != other.context) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + right.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }


}

data class LiteralTypeReference(
    val value: String,
    override val context: LslContextBase
) : TypeReference(context = context) {
    override fun resolve(): Type? {
        if (value.startsWith("\""))
            return StringType(context)
        else if (value.startsWith("\'"))
            return CharType(context)
        else if ((Character.isDigit(value.first()) && value.contains(",")) || (value.startsWith("-") && value.contains(",")))
            return Float64Type(context)
        else if (Character.isDigit(value.first()) || value.startsWith("-"))
            return Int64Type(context)
        return null
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        return node.name == this.value
    }

    override fun isSameReference(other: TypeReference): Boolean {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LiteralTypeReference

        if (value != other.value) return false
        if (context != other.context) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }


}


data class GenericTypeReference(
    val name: String,
    var typeBound: GenericTypeBound = GenericTypeBound.EMPTY,
    val genericReferences: MutableList<TypeReference>,
    override val context: LslContextBase,
) : TypeReference(context = context) {
    override fun resolve(): Type? {
        return resolveArrayType() ?: resolveListType() ?: resolveMapType() ?: resolveNullType()
        ?: context.resolveType(this)
    }

    override fun isSameReference(other: TypeReference): Boolean {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenericTypeReference

        if (name != other.name) return false
        if (typeBound != other.typeBound) return false
        if (genericReferences != other.genericReferences) return false
        if (context != other.context) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + typeBound.hashCode()
        result = 31 * result + genericReferences.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }

    private fun resolveArrayType(): ArrayType? {
        if (name != "array")
            return null
        genericReferences.forEach { it.resolve() }
        return ArrayType(generics = genericReferences, context = context)
    }

    private fun resolveListType(): ListType? {
        if (name != "list")
            return null
        genericReferences.forEach { it.resolve() }
        return ListType(generics = genericReferences, context = context)
    }

    private fun resolveMapType(): MapType? {
        if (name != "map")
            return null
        genericReferences.forEach { it.resolve() }
        return MapType(generics = genericReferences, context = context)
    }

    private fun resolveNullType(): NullType? {
        if (name != "null") {
            return null
        }
        return NullType(false, mutableListOf(), context)
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        if (this.name != node.name) {
            return false
        }

        if (!areGenericsMatch(node.generics)) {
            return false
        }

        return true
    }

    private fun areGenericsMatch(generics: MutableList<TypeReference>): Boolean {
        if (this.genericReferences.isEmpty() && generics.isEmpty()) {
            return true
        }

        if (this.genericReferences.isEmpty() || generics.isEmpty()) {
            return false
        }

        return true
    }
}


data class PlainTypeReference(
    val name: String,
    val isPointer: Boolean,
    var typeBound: GenericTypeBound = GenericTypeBound.EMPTY,
    override val context: LslContextBase
) : TypeReference(context = context) {
    override fun resolve(): Type? {
        return resolveNullType()
            ?: context.resolveType(this)
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        if (this.name != node.name) {
            return false
        }

        if (this.isPointer != node.isPointer) {
            return false
        }

        return true
    }

    override fun isSameReference(other: TypeReference): Boolean {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlainTypeReference

        if (name != other.name) return false
        if (isPointer != other.isPointer) return false
        if (typeBound != other.typeBound) return false
        if (context != other.context) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + isPointer.hashCode()
        result = 31 * result + typeBound.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }

    private fun resolveNullType(): NullType? {
        if (name != "null") {
            return null
        }
        return NullType(false, mutableListOf(), context)
    }

}

fun TypeReference.getName(): String {
    return when (this) {
        is PlainTypeReference -> this.name
        is GenericTypeReference -> this.name
        is LiteralTypeReference -> this.value
        is UnionExpressionTypeReference -> ""
        is IntersectionExpressionTypeReference -> ""
        else -> error("")
    }
} 