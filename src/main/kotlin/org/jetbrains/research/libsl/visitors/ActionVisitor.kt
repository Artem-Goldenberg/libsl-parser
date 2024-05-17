package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.ActionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.nodes.ActionArgumentDescriptor
import org.jetbrains.research.libsl.nodes.ActionDecl
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

        val actionGenericTypes = if (ctx.generic() != null)
            ctx.actionGenerics
        else
            mutableListOf()

        actionGenericTypes.forEach { actionContext.storeActionType(it) }

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
        get() = getGenericTypes(this.generic(), this.whereConstraints(), context)
}