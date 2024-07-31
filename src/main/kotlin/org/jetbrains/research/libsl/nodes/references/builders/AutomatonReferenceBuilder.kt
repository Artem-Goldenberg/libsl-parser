package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Automaton
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference

object AutomatonReferenceBuilder {
    fun build(
        name: String,
        context: LslContextBase,
        generics: MutableList<TypeReference> = mutableListOf()
    ): AutomatonReference {
        return AutomatonReference(name, context, generics)
    }

    fun Automaton.getReference(context: LslContextBase): AutomatonReference {
        return build(this.name, context)
    }
}
