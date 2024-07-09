package org.jetbrains.research.libsl.context

import org.jetbrains.research.libsl.nodes.FunctionArgument
import org.jetbrains.research.libsl.nodes.Variable
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.VariableReference
import org.jetbrains.research.libsl.type.GenericType
import org.jetbrains.research.libsl.type.Type

class FunctionContext(
    override val parentContext: LslContextBase
) : LslContextBase(parentContext.fileName) {
    private val functionArguments = mutableListOf<FunctionArgument>()
    private val functionGenericTypes = mutableListOf<GenericType>()

    override fun resolveVariable(reference: VariableReference): Variable? {
        return functionArguments.firstOrNull { it.name == reference.name } ?: super.resolveVariable(reference)
    }

    override fun resolveType(reference: TypeReference): Type? {
        return functionGenericTypes.firstOrNull { types -> reference.isReferenceMatchWithNode(types) }
            ?: parentContext.resolveType(reference)
    }
    
    fun resolveFunctionArgumentByName(name: String): FunctionArgument? {
        return functionArguments.firstOrNull { arg -> arg.name == name }
    }

    fun storeFunctionArgument(arg: FunctionArgument) {
        functionArguments.add(arg)
    }

    fun storeFunctionType(type: GenericType) {
        // TODO: maybe add exception if such type was stored previously ?
        if (type in functionGenericTypes)
            return

        functionGenericTypes.add(type)
    }

    fun getFunctionGenericTypes(): MutableList<GenericType> {
        return functionGenericTypes
    }
}
