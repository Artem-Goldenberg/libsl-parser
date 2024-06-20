package org.jetbrains.research.libsl.nodes.helpers

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.references.IntersectionTypeExpression
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.UnionTypeExpression

object TypeReferenceDumper {
    fun dumpType(typeRef: TypeReference, context: LslContextBase): String =
        dump(typeRef, context)

    private fun dump(typeRef: TypeReference, context: LslContextBase): String {
        return when (typeRef) {
            is IntersectionTypeExpression -> dumpIntersectionTypeExpression(
                typeRef,
                context
            )
            is UnionTypeExpression -> dumpUnionTypeExpression(typeRef, context)
            else -> dumpSimpleTypeReference(typeRef, context)
        }
    }

    private fun dumpUnionTypeExpression(
        typeRef: UnionTypeExpression,
        context: LslContextBase
    ): String {
        val left = dump(typeRef.left, context)
        val right = dump(typeRef.right, context)
        return buildString {
            append(left)
            append(" | ")
            append(right)
        }
    }

    private fun dumpSimpleTypeReference(typeRef: TypeReference, context: LslContextBase): String {
        return buildString {
            append(context.resolveType(typeRef)?.fullName.toString())
        }
    }

    private fun dumpIntersectionTypeExpression(
        typeRef: IntersectionTypeExpression,
        context: LslContextBase
    ): String {
        val left = dump(typeRef.left, context)
        val right = dump(typeRef.right, context)
        return buildString {
            append(left)
            append(" & ")
            append(right)
        }
    }
}