package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.ActionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.nodes.ActionArgumentDescriptor
import org.jetbrains.research.libsl.nodes.ActionDecl
import org.jetbrains.research.libsl.nodes.GenericTypeBound
import org.jetbrains.research.libsl.type.GenericType
import org.jetbrains.research.libsl.utils.PositionGetter

class ActionVisitor(
    private val actionContext: ActionContext,
    private val globalContext: LslGlobalContext,
    private var fileName: String
) : LibSLParserVisitor<Unit>(actionContext) {
    private val posGetter = PositionGetter()

    override fun visitActionDecl(ctx: LibSLParser.ActionDeclContext) {
        val actionName = ctx.actionName.text.extractIdentifier()
        val actionParams = mutableListOf<ActionArgumentDescriptor>()

        ctx.actionDeclParamList()?.actionParameter()?.forEach { parameterCtx ->
            val param = ActionArgumentDescriptor(
                getAnnotationUsages(parameterCtx.annotationUsage()),
                parameterCtx.name.text.extractIdentifier(),
                processTypeIdentifier(parameterCtx.type),
                posGetter.getCtxPosition(fileName, ctx)
            )
            actionParams.add(param)
        }

        val returnType = ctx.actionType?.let { processTypeIdentifier(it) }

        val actionAnnotations = getAnnotationUsages(ctx.annotationUsage())

        val funGenericTypes: MutableList<GenericType> = if (ctx.generic() != null)
            ctx.actionGenerics
        else
            mutableListOf()

        funGenericTypes.forEach { actionContext.storeActionType(it) }

        val declaredAction =
            ActionDecl(
                actionName,
                actionParams,
                actionAnnotations,
                actionContext,
                returnType,
                posGetter.getCtxPosition(fileName, ctx)
            )
        if (declaredAction !in globalContext.getAllDeclaredActions()) {
            globalContext.storeDeclaredAction(declaredAction)
        }
    }


    private val LibSLParser.ActionDeclContext.actionGenerics: MutableList<GenericType>
        get() = getGenerics(this.generic(), this.whereConstraints())

    // TODO: union with getGenerics in FunctionVisitor.kt class
    private fun getGenerics(
        genericContext: LibSLParser.GenericContext,
        whereContext: LibSLParser.WhereConstraintsContext
    ): MutableList<GenericType> {

        val actionGenerics: MutableList<GenericType> = mutableListOf()
        // GenericType? - this is bad; Only temporary
        val actionGenericsOrdered: LinkedHashMap<String, GenericType?> = linkedMapOf()

        genericContext.typeIdentifier().forEach { actionGenericsOrdered[it.name.text] = null }

        for (typeConstraint in whereContext.typeConstraint()) {
            val paramName = typeConstraint.paramName.text
            val bound =
                if (typeConstraint.paramConstraint.bound == null) GenericTypeBound.EMPTY else GenericTypeBound.fromString(
                    typeConstraint.paramConstraint.bound.text
                )
            val constraints = mutableListOf<String>(typeConstraint.paramConstraint.constraintType.text)

            if (
                actionGenericsOrdered[paramName] == null
            ) {
                actionGenericsOrdered[paramName] = GenericType(
                    paramName,
                    typeBound = bound,
                    constraints = constraints,
                    context = actionContext
                )
            } else {
                actionGenericsOrdered[paramName]?.constraints?.addAll(constraints)
            }
        }

        actionGenericsOrdered.forEach { it.value?.let { it1 -> actionGenerics.add(it1) } }
        return actionGenerics
    }
}