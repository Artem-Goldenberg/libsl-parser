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
        throw error("Operation is not supported")
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        return left.isReferenceMatchWithNode(node) || right.isReferenceMatchWithNode(node)
    }

    override fun isSameReference(other: TypeReference): Boolean {
        if (other is UnionExpressionTypeReference) {
            val LLm = this.left.isSameReference(other.left) && this.right.isSameReference(other.right)
            val LRm = this.left.isSameReference(other.right) && this.right.isSameReference(other.left)
            return LLm or LRm
        }
        return false
    }

    fun toSimpleString(): String {
        return this.left.toSimpleString() + " | " + this.right.toSimpleString()
    }

}

data class IntersectionExpressionTypeReference(
    val left: TypeReference,
    val right: TypeReference,
    override val context: LslContextBase
) : TypeReference {
    override fun resolve(): Type? {
        throw error("Operation is not supported")
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        return left.isReferenceMatchWithNode(node) && right.isReferenceMatchWithNode(node)
    }

    override fun isSameReference(other: TypeReference): Boolean {
        if (other is IntersectionExpressionTypeReference) {
            val LLm = this.left.isSameReference(other.left) && this.right.isSameReference(other.right)
            val LRm = this.left.isSameReference(other.right) && this.right.isSameReference(other.left)
            return LLm or LRm
        }
        return false
    }

    fun toSimpleString(): String {
        return this.left.toSimpleString() + " & " + this.right.toSimpleString()
    }
}

data class LiteralTypeReference(
    val value: Any,
    val literalType: Type,
    override val context: LslContextBase
) : TypeReference {
    override fun resolve(): Type {
        return resolveLiteralType()
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        val type = resolveLiteralType()
        return type == node
    }

    override fun isSameReference(other: TypeReference): Boolean {
        if (other is LiteralTypeReference) {
            return this.value == other.value
        }
        return false
    }

    private fun resolveLiteralType(): Type {
        return literalType
    }
}


data class GenericTypeReference(
    val name: String,
    val genericReferences: MutableList<TypeReference>,
    override val context: LslContextBase,
) : TypeReference {
    override fun resolve(): Type? {
        return resolveArrayType() ?: resolveListType() ?: resolveMapType()
        ?: context.resolveType(this)
    }

    override fun isSameReference(other: TypeReference): Boolean {
        if (other is GenericTypeReference) {
            return this.name == other.name && this.genericReferences.filterIndexed { i, it ->
                !it.isSameReference(
                    other.genericReferences.get(i)
                )
            }.isEmpty()
        }
        return false
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
            return this.name == other.name
        }
        return false
    }

}

data class WildcardTypeReference(
    val name: String,
    var typeBound: GenericTypeBound = GenericTypeBound.EMPTY,
    val genericReferences: MutableList<TypeReference> = mutableListOf(),
    override val context: LslContextBase
) : TypeReference {
    override fun resolve(): Type? {
        return context.resolveType(this)
    }

    override fun isSameReference(other: TypeReference): Boolean {
        if (other is WildcardTypeReference) {
            return this.name == other.name && this.typeBound == other.typeBound && this.genericReferences.filterIndexed { i, it ->
                !it.isSameReference(
                    other.genericReferences.get(i)
                )
            }.isEmpty()
        }
        return false
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

fun TypeReference.toSimpleString(): String {
    return when (this) {
        is PlainTypeReference -> this.name
        is GenericTypeReference -> this.name
        is LiteralTypeReference -> this.value.toString()
        is UnionExpressionTypeReference -> this.toSimpleString()
        is IntersectionExpressionTypeReference -> this.toSimpleString()
        is WildcardTypeReference -> this.name
        else -> error("Unsupported reference type")
    }
} 