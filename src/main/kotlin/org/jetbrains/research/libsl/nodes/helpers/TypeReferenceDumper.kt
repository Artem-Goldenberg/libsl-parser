package org.jetbrains.research.libsl.nodes.helpers

import org.jetbrains.research.libsl.nodes.references.*
import org.jetbrains.research.libsl.type.Type

object TypeReferenceDumper {
    fun dumpType(typeRef: TypeReference): String =
        dump(typeRef)

    private fun dump(typeRef: TypeReference): String {
        return when (typeRef) {
            is IntersectionExpressionTypeReference -> dumpIntersectionTypeExpression(
                typeRef
            )
            is UnionExpressionTypeReference -> dumpUnionTypeExpression(typeRef)
            is GenericTypeReference -> dumpGenericTypeExpression(typeRef)
            is PlainTypeReference -> dumpSimpleTypeReference(typeRef)
            is LiteralTypeReference -> dumpLiteralTypeReference(typeRef)
            else -> error("Undefined TypeReference")
        }
    }

    private fun dumpLiteralTypeReference(typeRef: LiteralTypeReference): String {
        return buildString {
            append(typeRef.context.resolveType(typeRef)?.fullName)
        }
    }

    private fun dumpGenericTypeExpression(typeRef: GenericTypeReference): String {
        return buildString {
            appendGeneric(this, typeRef)
        }
    }

    private fun dumpUnionTypeExpression(
        typeRef: UnionExpressionTypeReference
    ): String {
        val left = dump(typeRef.left)
        val right = dump(typeRef.right)
        return buildString {
            append(left)
            append(" | ")
            append(right)
        }
    }

    private fun dumpSimpleTypeReference(typeRef: TypeReference): String {
        return buildString {
            if (typeRef.resolve() != null)
                append(typeRef.context.resolveType(typeRef)?.fullName)
            else append(Type.UNRESOLVED_TYPE_SYMBOL)
        }
    }

    private fun dumpIntersectionTypeExpression(
        typeRef: IntersectionExpressionTypeReference
    ): String {
        val left = dump(typeRef.left)
        val right = dump(typeRef.right)
        return buildString {
            append(left)
            append(" & ")
            append(right)
        }
    }
}