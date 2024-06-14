package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.ActionContext
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.AnnotationArgumentDescriptor
import org.jetbrains.research.libsl.nodes.VariableKind
import org.jetbrains.research.libsl.nodes.VariableWithInitialValue
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder
import org.jetbrains.research.libsl.utils.PositionGetter

class TopLevelDeclarationsVisitor(
    private val basePath: String,
    private val errorManager: ErrorManager,
    private val globalContext: LslGlobalContext
) : LibSLParserVisitor<Unit>(globalContext) {
    private val fileName = context.fileName
    private val posGetter = PositionGetter()

    override fun visitAnnotationDecl(ctx: LibSLParser.AnnotationDeclContext) {
        val annotationName = ctx.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(context)
        val params = mutableListOf<AnnotationArgumentDescriptor>()

        ctx.annotationDeclParams()?.annotationDeclParamsPart()?.forEach { parameterCtx ->
            val param = AnnotationArgumentDescriptor(
                parameterCtx.nameWithType().name.text.extractIdentifier(),
                processTypeIdentifier(parameterCtx.nameWithType().type),
                parameterCtx.expression()?.let {
                    expressionVisitor.visitExpression(it)
                },
                posGetter.getCtxPosition(fileName, ctx)
            )
            params.add(param)
        }

        val annotation = Annotation(
            annotationName,
            params,
            posGetter.getCtxPosition(fileName, ctx)
        )
        if (annotation !in globalContext.getAllAnnotations()) {
            globalContext.storeAnnotation(annotation)
        }
    }

    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext) {
        val automatonContext = AutomatonContext(context)
        AutomatonVisitor(basePath, errorManager, globalContext, automatonContext).visitAutomatonDecl(ctx)
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext) {
        val parentContext = if (ctx.functionHeader().automatonName != null) {
            val automatonRef =
                AutomatonReferenceBuilder.build(ctx.functionHeader().automatonName.text.extractIdentifier(), context)
            globalContext.resolveAutomaton(automatonRef)!!.context
        } else {
            globalContext
        }

        val functionContext = FunctionContext(parentContext)
        FunctionVisitor(functionContext, parentAutomaton = null, globalContext, errorManager).visitFunctionDecl(ctx)
    }

    override fun visitTypeDefBlock(ctx: LibSLParser.TypeDefBlockContext) {
        TypeVisitor(basePath, errorManager, globalContext).visitTypeDefBlock(ctx)
    }

    override fun visitSimpleSemanticType(ctx: LibSLParser.SimpleSemanticTypeContext) {
        TypeVisitor(basePath, errorManager, globalContext).visitSimpleSemanticType(ctx)
    }

    override fun visitEnumSemanticType(ctx: LibSLParser.EnumSemanticTypeContext) {
        TypeVisitor(basePath, errorManager, globalContext).visitEnumSemanticType(ctx)
    }

    override fun visitTypealiasStatement(ctx: LibSLParser.TypealiasStatementContext) {
        TypeVisitor(basePath, errorManager, globalContext).visitTypealiasStatement(ctx)
    }

    override fun visitEnumBlock(ctx: LibSLParser.EnumBlockContext) {
        TypeVisitor(basePath, errorManager, globalContext).visitEnumBlock(ctx)
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val variableName = ctx.nameWithType().name.text.extractIdentifier()
        val typeRef = processTypeIdentifier(ctx.nameWithType().type)

        val expressionVisitor = ExpressionVisitor(context)
        val initValue = ctx.assignmentRight()?.let { right ->
            when {
                right.expression() != null -> {
                    expressionVisitor.visitExpression(right.expression())
                }
                else -> error("unknown initializer kind")
            }
        }

        val annotationUsages = getAnnotationUsages(ctx.annotationUsage())
        val variable = VariableWithInitialValue(
            keyword,
            variableName,
            typeRef,
            annotationUsages,
            initValue,
            posGetter.getCtxPosition(fileName, ctx)
        )
        if (variable !in globalContext.getAllVariables()) {
            globalContext.storeVariable(variable)
        }
    }

    override fun visitActionDecl(ctx: LibSLParser.ActionDeclContext) {
        val actionContext = ActionContext(globalContext)
        ActionVisitor(actionContext, globalContext, fileName).visitActionDecl(ctx)
    }
}
