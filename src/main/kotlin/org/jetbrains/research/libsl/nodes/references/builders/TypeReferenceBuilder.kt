package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.references.*
import org.jetbrains.research.libsl.type.*

object TypeReferenceBuilder {
    fun build(
        name: String,
        typeBound: GenericTypeBound = GenericTypeBound.EMPTY,
        genericReferences: MutableList<TypeReference>,
        isPointer: Boolean = false,
        context: LslContextBase
    ): TypeReference {
        if (isWildCard(name, typeBound))
            return buildWildcardRef(name, typeBound, genericReferences, context)
        else if (isLiteral(name))
            return buildLiteralRef(name, context)
        else if (isGeneric(genericReferences))
            return buildGenericRef(name, genericReferences, context)
        return buildPlainRef(name, isPointer, context)
    }

    private fun buildLiteralRef(
        value: String,
        context: LslContextBase
    ): TypeReference {
        return LiteralTypeReference(value as Any, createLiteralType(value, context), context)
    }

    private fun createLiteralType(value: String, context: LslContextBase): Type {
        if (value.startsWith("\""))
            return StringType(context)
        else if (value.startsWith("\'"))
            return CharType(context)
        else if ((Character.isDigit(value.first()) && value.contains(",")) || (value.startsWith("-") && value.contains(",")))
            return Float64Type(context)
        return Int64Type(context)
    }

    private fun buildWildcardRef(
        name: String,
        typeBound: GenericTypeBound,
        genericReferences: MutableList<TypeReference>,
        context: LslContextBase
    ): TypeReference {
        return WildcardTypeReference(name, typeBound, genericReferences, context)
    }

    private fun buildGenericRef(
        name: String,
        genericReferences: MutableList<TypeReference>,
        context: LslContextBase
    ): TypeReference {
        return GenericTypeReference(name, genericReferences, context)
    }

    private fun buildPlainRef(
        name: String,
        isPointer: Boolean = false,
        context: LslContextBase
    ): TypeReference {
        return PlainTypeReference(name, isPointer, context)
    }

    fun Type.getReference(
        context: LslContextBase,
        typeBound: GenericTypeBound = GenericTypeBound.EMPTY
    ): TypeReference {
        if (isWildCard(name, typeBound))
            return buildWildcardRef(this.name, typeBound, this.generics, context)
        else if (isLiteral(this.name))
            return buildLiteralRef(this.name, context)
        else if (isGeneric(generics))
            return buildGenericRef(this.name, this.generics, context)
        return buildPlainRef(this.name, this.isPointer, context)
    }

    private fun isLiteral(name: String): Boolean {
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

    private fun isWildCard(name: String, typeBound: GenericTypeBound): Boolean {
        return name == "?" || typeBound != GenericTypeBound.EMPTY
    }

    private fun isGeneric(genericReferences: MutableList<TypeReference>): Boolean {
        return genericReferences.isNotEmpty()
    }

}
