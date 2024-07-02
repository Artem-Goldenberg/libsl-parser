package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.type.*

interface TypeReference : LslReference<Type, TypeReference> {
    override val context: LslContextBase
}

data class UnionExpressionTypeReference(
    val left: TypeReference,
    val right: TypeReference,
    override val context: LslContextBase
) : TypeReference {
    override fun resolve(): Type? {
        this.left.resolve()
        this.right.resolve()
        // We don't have type for UnionExpression
        return null
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        // we don't have Type for UnionExpressionTypeReference
        return false
    }

    override fun isSameReference(other: TypeReference): Boolean {
        // This is right realization ??
        if (other is UnionExpressionTypeReference) {
            this.left.isSameReference(other.left) && this.right.isSameReference(other.right)
        }
        return false
    }

    fun getName(): String {
        return this.left.getName() + " | " + this.right.getName()
    }

}

data class IntersectionExpressionTypeReference(
    val left: TypeReference,
    val right: TypeReference,
    override val context: LslContextBase
) : TypeReference {
    override fun resolve(): Type? {
        this.left.resolve()
        this.right.resolve()
        // We don't have type for IntersectionExpression
        return null
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        // we don't have Type for IntersectionExpressionTypeReference
        return false
    }

    override fun isSameReference(other: TypeReference): Boolean {
        // This is right realization ??
        if (other is IntersectionExpressionTypeReference) {
            this.left.isSameReference(other.left) && this.right.isSameReference(other.right)
        }
        return false
    }

    fun getName(): String {
        return this.left.getName() + " & " + this.right.getName()
    }
}

data class LiteralTypeReference(
    val value: String,
    override val context: LslContextBase
) : TypeReference {
    override fun resolve(): Type? {
        return resolveLiteralType()
    }

    private fun resolveLiteralType(): Type? {
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
        if (other is LiteralTypeReference) {
            return this.value == other.value
        }
        return false
    }
}


data class GenericTypeReference(
    val name: String,
    var typeBound: GenericTypeBound = GenericTypeBound.EMPTY,
    val genericReferences: MutableList<TypeReference>,
    override val context: LslContextBase,
) : TypeReference {
    override fun resolve(): Type? {
        return resolveArrayType() ?: resolveListType() ?: resolveMapType()
        ?: context.resolveType(this)
    }

    override fun isSameReference(other: TypeReference): Boolean {
        if (other is GenericTypeReference) {
            return this.name == other.name && this.typeBound == other.typeBound && this.genericReferences.filterIndexed { i, it ->
                !it.isSameReference(
                    other.genericReferences.get(i)
                )
            }.isEmpty()
        }
        return false
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
) : TypeReference {
    override fun resolve(): Type? {
        return context.resolveType(this)
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
        if (other is PlainTypeReference) {
            this.name == other.name && this.typeBound == other.typeBound
        }
        return false
    }

}

data class WildcardTypeReference(
    override val context: LslContextBase,
) : TypeReference {
    override fun resolve(): Type? {
        return context.resolveType(this)
    }

    override fun isSameReference(other: TypeReference): Boolean {
        return other is WildcardTypeReference
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        return node.name == this.getName()
    }
}

fun TypeReference.getName(): String {
    return when (this) {
        is PlainTypeReference -> this.name
        is GenericTypeReference -> this.name
        is LiteralTypeReference -> this.value
        is UnionExpressionTypeReference -> this.getName()
        is IntersectionExpressionTypeReference -> this.getName()
        is WildcardTypeReference -> "?"
        else -> error("Unsupported reference type")
    }
} 