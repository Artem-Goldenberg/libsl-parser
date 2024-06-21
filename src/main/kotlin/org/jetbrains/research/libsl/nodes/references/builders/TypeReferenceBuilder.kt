package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.references.LiteralTypeReference
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
            return buildLiteral(name, context)
        return TypeReference(name, isPointer, typeBound, genericReferences, context)
    }

    fun buildLiteral(
        name: String,
        context: LslContextBase
    ): TypeReference {
        return LiteralTypeReference(name, context)
    }

    fun Type.getReference(
        context: LslContextBase,
        typeBound: GenericTypeBound = GenericTypeBound.EMPTY
    ): TypeReference {
        if (isLiteral(this.name))
            return buildLiteral(this.name, context)
        return build(this.name, typeBound, this.generics, this.isPointer, context)
    }

    fun isLiteral(name: String): Boolean {
        val refNameFirstChar = name.first()
        if (refNameFirstChar.isDigit())
            return true
        else if (refNameFirstChar == '\"')
            return true
        else if (refNameFirstChar == '\'')
            return true
        return false
    }
}
