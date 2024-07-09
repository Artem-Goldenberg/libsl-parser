package org.jetbrains.research.libsl.context

import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.GenericType
import org.jetbrains.research.libsl.type.Type

open class ActionContext(
    override val parentContext: LslContextBase
) : LslContextBase(parentContext.fileName) {
    private val actionGenericTypes = mutableListOf<GenericType>()

    override fun resolveType(reference: TypeReference): Type? {
        return actionGenericTypes.firstOrNull { types -> reference.isReferenceMatchWithNode(types) }
            ?: parentContext.resolveType(reference)
    }
    
    fun storeActionType(type: GenericType) {
        // TODO: maybe add exception if such type was stored previously ?
        if (type in actionGenericTypes)
            return

        actionGenericTypes.add(type)
    }

    fun getActionGenericTypes(): MutableList<GenericType> {
        return actionGenericTypes
    }
}