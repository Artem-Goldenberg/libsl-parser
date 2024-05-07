package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
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
        return TypeReference(name, isPointer, typeBound, genericReferences, context)
    }

    fun Type.getReference(context: LslContextBase): TypeReference {
        return build(this.name, this.typeBound, this.generics, this.isPointer, context)
    }
}
