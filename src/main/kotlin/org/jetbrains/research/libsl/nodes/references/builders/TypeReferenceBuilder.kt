package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.references.GenericTypeReference
import org.jetbrains.research.libsl.nodes.references.LiteralTypeReference
import org.jetbrains.research.libsl.nodes.references.PlainTypeReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.GenericTypeBound
import org.jetbrains.research.libsl.type.Type

object TypeReferenceBuilder {
    fun build(
        name: String,
        typeBound: GenericTypeBound = GenericTypeBound.EMPTY,
        genericReferences: MutableList<TypeReference>,
        isPointer: Boolean = false,
        context: LslContextBase
    ): TypeReference {
        if (isLiteral(name))
            return buildLiteralRef(name, context)
        if (genericReferences.isNotEmpty())
            return buildGenericRef(name, typeBound, genericReferences, context)
        return buildPlainRef(name, isPointer, typeBound, context)
    }

    private fun buildLiteralRef(
        name: String,
        context: LslContextBase
    ): TypeReference {
        return LiteralTypeReference(name, context)
    }

    private fun buildGenericRef(
        name: String,
        typeBound: GenericTypeBound = GenericTypeBound.EMPTY,
        genericReferences: MutableList<TypeReference>,
        context: LslContextBase
    ): TypeReference {
        return GenericTypeReference(name, typeBound, genericReferences, context)
    }

    private fun buildPlainRef(
        name: String,
        isPointer: Boolean = false,
        typeBound: GenericTypeBound = GenericTypeBound.EMPTY,
        context: LslContextBase
    ): TypeReference {
        return PlainTypeReference(name, isPointer, typeBound, context)
    }

    fun Type.getReference(
        context: LslContextBase,
        typeBound: GenericTypeBound = GenericTypeBound.EMPTY
    ): TypeReference {
        if (isLiteral(this.name))
            return buildLiteralRef(this.name, context)
        if (this.generics.isNotEmpty())
            return buildGenericRef(this.name, typeBound, this.generics, context)
        return buildPlainRef(this.name, this.isPointer, typeBound, context)
    }

    fun isLiteral(name: String): Boolean {
        val refNameFirstChar = name.first()
        if (name == "null")
            return true
        else if (refNameFirstChar.isDigit())
            return true
        else if (refNameFirstChar == '\"')
            return true
        else if (refNameFirstChar == '\'')
            return true
        else if (name == "true" || name == "false")
            return true
        return false
    }
}
