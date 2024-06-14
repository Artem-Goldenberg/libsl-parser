package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.TypeIdentifierContext
import org.jetbrains.research.libsl.LibSLParserBaseVisitor
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.nodes.AnnotationUsage
import org.jetbrains.research.libsl.nodes.NamedArgumentWithValue
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.builders.AnnotationReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference
import org.jetbrains.research.libsl.type.*
import org.jetbrains.research.libsl.utils.PositionGetter
import org.jetbrains.research.libsl.type.СompositeType

abstract class LibSLParserVisitor<T>(open val context: LslContextBase) : LibSLParserBaseVisitor<T>() {

    private val posGetter = PositionGetter()

    internal fun processTypeIdentifier(
        ctx: TypeIdentifierContext,
        typeBound: String = GenericTypeBound.EMPTY.string
    ): TypeReference {
        val typeName = if (ctx.name.primitiveLiteral() == null) ctx.name.periodSeparatedFullName()
            .asPeriodSeparatedString() else ctx.name.primitiveLiteral().text
        val isPointer = ctx.asterisk != null
        var genericReferences = mutableListOf<TypeReference>()

        if (ctx.generic() != null) {
            val genericTypeIdentifierContext = ctx.generic().typeArgument()
            genericReferences = processGenerics(genericTypeIdentifierContext)
        }
        val bound = GenericTypeBound.fromString(typeBound)

        return TypeReferenceBuilder.build(typeName, bound, genericReferences, isPointer, context)
    }

    // TODO: refactor this method
    internal fun processCompositeType(
        type: СompositeType
    ): TypeReference {
        return TypeReferenceBuilder.build(name = type.name, genericReferences = mutableListOf(), context = context)
    }

    fun processGenerics(ctx: MutableList<LibSLParser.TypeArgumentContext>): MutableList<TypeReference> {
        val genericReferences = mutableListOf<TypeReference>()
        ctx.forEach {
            val generic = if (it.typeIdentifierBounded() != null) getRealType(
                it.typeIdentifierBounded().typeIdentifier()
            ) else getRealType(it.typeIdentifier())
            val genericRef = if ((it.typeIdentifierBounded() != null)) generic.getReference(
                context,
                GenericTypeBound.fromString(it.typeIdentifierBounded().genericBound().text)
            ) else generic.getReference(context)
            genericReferences.add(genericRef)
        }
        return genericReferences
    }

    private fun getRealType(ctx: TypeIdentifierContext): RealType {
        val typeNameParts = if (ctx.name.primitiveLiteral() == null) ctx.name.periodSeparatedFullName()
            .asPeriodSeparatedParts() else listOf(ctx.name.primitiveLiteral().text)
        val isPointer = ctx.asterisk != null

        var genericReferences = mutableListOf<TypeReference>()

        if (ctx.generic() != null) {
            val genericTypeIdentifierContext = ctx.generic().typeArgument()
            genericReferences = processGenerics(genericTypeIdentifierContext)
        }


        val realType = RealType(
            typeNameParts,
            isPointer,
            genericReferences,
            context,
            posGetter.getCtxPosition(context.fileName, ctx)
        )

        val previouslyStoredType = context.resolveType(realType.getReference(context))
        if (previouslyStoredType != null && previouslyStoredType is RealType) {
            return previouslyStoredType
        }

        context.storeType(realType)
        return realType
    }

    private fun getArrayType(ctx: TypeIdentifierContext): ArrayType {
        val typeNameParts = ctx.name.periodSeparatedFullName().asPeriodSeparatedParts()
        check(typeNameParts[0] == "array" && typeNameParts.size == 1) { "not an array" }

        val isPointer = ctx.asterisk != null
        var genericReferences = mutableListOf<TypeReference>()

        if (ctx.generic() != null) {
            val genericTypeIdentifierContext = ctx.generic().typeArgument()
            genericReferences = processGenerics(genericTypeIdentifierContext)
        }
        val arrayType = ArrayType(isPointer, genericReferences, context)
        context.storeType(arrayType)
        return arrayType
    }

    fun getRealTypeOrArray(ctx: TypeIdentifierContext): Type {
        val typeNameParts = ctx.name.periodSeparatedFullName().asPeriodSeparatedParts()

        return if (typeNameParts[0] == "array") {
            getArrayType(ctx)
        } else {
            getRealType(ctx)
        }
    }

    protected fun getAnnotationUsages(ctx: List<LibSLParser.AnnotationUsageContext>): MutableList<AnnotationUsage> {
        return ctx.map { processAnnotationUsage(it) }.toMutableList()
    }

    private fun processAnnotationUsage(ctx: LibSLParser.AnnotationUsageContext): AnnotationUsage {
        val name = ctx.Identifier().asPeriodSeparatedString()
        val args = if (ctx.annotationArgs() != null) {
            processAnnotationArgs(ctx)
        } else {
            emptyList()
        }

        val argTypes =
            args.map { argument -> context.typeInferrer.getExpressionType(argument.value).getReference(context) }
        val annotationRef = AnnotationReferenceBuilder.build(name, argTypes, context)

        return AnnotationUsage(
            annotationRef,
            args,
            posGetter.getCtxPosition(context.fileName, ctx)
        )
    }

    private fun processAnnotationArgs(ctx: LibSLParser.AnnotationUsageContext): List<NamedArgumentWithValue> {
        val namedArgs = mutableListOf<NamedArgumentWithValue>()
        ctx.annotationArgs().forEach { a ->
            val name = a.argName()?.name?.text
            val value = ExpressionVisitor(context).visitExpression(a.expression())
            namedArgs.add(
                NamedArgumentWithValue(
                    name,
                    value,
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
            )
        }

        return namedArgs
    }

    fun getGenericTypes(
        genericContext: LibSLParser.GenericContext,
        whereContext: LibSLParser.WhereConstraintsContext,
        context: LslContextBase
    ): MutableList<GenericType> {

        val genericTypesOrdered: LinkedHashMap<String, GenericType?> = linkedMapOf()

        genericContext.typeArgument().forEach {
            if (it.typeIdentifier() != null)
                genericTypesOrdered[it.typeIdentifier().name.text] = null
            else
                genericTypesOrdered[it.typeIdentifierBounded().typeIdentifier().name.text] = null
        }

        for (typeConstraint in whereContext.typeConstraint()) {
            val paramName = typeConstraint.paramName.text
            val constraints: MutableList<TypeReference> = mutableListOf(
                if (typeConstraint.paramConstraint.typeIdentifier() != null)
                    processTypeIdentifier(typeConstraint.paramConstraint.typeIdentifier())
                else
                    processTypeIdentifier(
                        typeConstraint.paramConstraint.typeIdentifierBounded().typeIdentifier(),
                        typeConstraint.paramConstraint.typeIdentifierBounded().genericBound().text
                    )
            )
            if (
                genericTypesOrdered[paramName] == null
            ) {
                genericTypesOrdered[paramName] = GenericType(
                    name = paramName,
                    constraints = constraints,
                    context = context
                )
            } else {
                genericTypesOrdered[paramName]?.constraints?.addAll(constraints)
            }
        }

        return genericTypesOrdered.values.filterNotNull().toMutableList()
    }

    internal fun buildCompositeType(ctx: LibSLParser.TypesIdentifiersArrayContext): СompositeType {
        val types: MutableList<Pair<TypeReference, NextCompositionTypesSymbol>> = mutableListOf()
        ctx.typeIdentifier().forEachIndexed { index, item ->
            val compositionType = if (ctx.typeConcatination(index) != null) NextCompositionTypesSymbol.fromString(
                ctx.typeConcatination(index).text
            ) else NextCompositionTypesSymbol.NONE
            types.add(Pair(processTypeIdentifier(item), compositionType))
        }
        return СompositeType(context, types)
    }

    internal fun isNotStoredLiteralType(
        ctx: LslGlobalContext,
        typeReference: TypeReference?,
        type: LibSLParser.TypeIdentifierContext?
    ): Boolean {
        return type?.typeIdentifierName()?.primitiveLiteral() != null && typeReference != null && ctx.resolveType(typeReference) == null
    }
}
