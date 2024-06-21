package org.jetbrains.research.libsl.nodes.helpers

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.references.GenericTypeReference
import org.jetbrains.research.libsl.nodes.references.IntersectionExpressionTypeReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.UnionExpressionTypeReference

object TypeReferenceDumper {
    fun dumpType(typeRef: TypeReference, context: LslContextBase): String =
        dump(typeRef, context)

    private fun dump(typeRef: TypeReference, context: LslContextBase): String {
        return when (typeRef) {
            is IntersectionExpressionTypeReference -> dumpIntersectionTypeExpression(
                typeRef,
                context
            )
            is UnionExpressionTypeReference -> dumpUnionTypeExpression(typeRef, context)
            is GenericTypeReference -> dumpGenericTypeExpression(typeRef, context)
            else -> dumpSimpleTypeReference(typeRef, context)
        }
    }

    private fun dumpGenericTypeExpression(typeRef: GenericTypeReference, context: LslContextBase): String {
        // TODO: think about optimizations;
        // TODO: add UNRESOLVED_TYPE_SYMBOL testing for debug purposes
        return buildString {
            appendGeneric(this, typeRef)
        }
    }

    private fun dumpUnionTypeExpression(
        typeRef: UnionExpressionTypeReference,
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
        // TODO: add UNRESOLVED_TYPE_SYMBOL testing for debug purposes
        return buildString {
            append(context.resolveType(typeRef)?.fullName.toString())
        }
    }

    private fun dumpIntersectionTypeExpression(
        typeRef: IntersectionExpressionTypeReference,
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