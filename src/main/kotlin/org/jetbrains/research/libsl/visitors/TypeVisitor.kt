package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.EnumSemanticTypeEntryContext
import org.jetbrains.research.libsl.LibSLParser.FunctionDeclContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.references.GenericTypeReference
import org.jetbrains.research.libsl.nodes.references.IntersectionExpressionTypeReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.UnionExpressionTypeReference
import org.jetbrains.research.libsl.type.*
import org.jetbrains.research.libsl.utils.PositionGetter

class TypeVisitor(
    context: LslContextBase
) : LibSLParserVisitor<TypeReference>(context) {
    private val fileName = context.fileName
    private val posGetter = PositionGetter()

    override fun visitSimpleSemanticType(ctx: LibSLParser.SimpleSemanticTypeContext): TypeReference {
        val typeName = ctx.semanticName.name.periodSeparatedFullName().asPeriodSeparatedString()
        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())
        val realNameCtx = ctx.realName
        val originType = getRealTypeOrArray(realNameCtx)

        val type = SimpleType(
            typeName,
            originType,
            annotationReferences,
            context = context,
            entityPosition = posGetter.getCtxPosition(fileName, ctx)
        )
        if (originType !in context.getAllTypes()) {
            context.storeType(originType)
        }
        if (type !in context.getAllTypes()) {
            context.storeType(type)
        }
        return processTypeIdentifier(ctx.typeIdentifier(0))
    }

    override fun visitEnumSemanticType(ctx: LibSLParser.EnumSemanticTypeContext): TypeReference {
        val typeName = ctx.semanticName.asPeriodSeparatedString()
        val realTypeCtx = ctx.realName
        val originType = getRealTypeOrArray(realTypeCtx)
        val entriesContexts = ctx.enumSemanticTypeEntry()
        val entries = processBlockTypeStatements(entriesContexts)
        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val type = EnumLikeSemanticType(
            typeName,
            originType,
            entries,
            annotationReferences,
            context,
            posGetter.getCtxPosition(fileName, ctx)
        )

        if (originType !in context.getAllTypes()) {
            context.storeType(originType)
        }
        if (type !in context.getAllTypes()) {
            context.storeType(type)
        }
        return processTypeIdentifier(ctx.typeIdentifier())
    }

    override fun visitTypealiasStatement(ctx: LibSLParser.TypealiasStatementContext): TypeReference {
        val name = ctx.left.typeIdentifierName().periodSeparatedFullName().asPeriodSeparatedString()
        val originalTypeReference = processTypeIdentifier(ctx.right)
        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val type = TypeAlias(
            name,
            originalTypeReference,
            annotationReferences,
            context,
            posGetter.getCtxPosition(fileName, ctx)
        )

        context.storeType(type)
        return processTypeIdentifier(ctx.typeIdentifier(0))
    }

    override fun visitEnumBlock(ctx: LibSLParser.EnumBlockContext): TypeReference {
        val name = ctx.typeIdentifier().text.extractIdentifier()
        val statementsContexts = ctx.enumBlockStatement()
        val statements = processEnumStatements(statementsContexts)
        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val type = EnumType(
            name,
            statements,
            annotationReferences,
            context,
            posGetter.getCtxPosition(fileName, ctx)
        )

        context.storeType(type)
        return processTypeIdentifier(ctx.typeIdentifier())
    }

    override fun visitTypeDefBlock(ctx: LibSLParser.TypeDefBlockContext): TypeReference {
        val name = ctx.type.name.periodSeparatedFullName().asPeriodSeparatedString()
        val isTypeIdentifier = ctx.targetType()?.typeIdentifier()?.name?.text
        val forTypeList = mutableListOf<String>()
        ctx.targetType()?.typeList()?.typeIdentifier()?.forEach { forTypeList.add(it.name.text) }

        val genericTypes: MutableList<GenericType> = if (ctx.typeIdentifier().generic() != null)
            ctx.typeDefBlockGenerics
        else
            mutableListOf()

        val variables = mutableListOf<Variable>()
        val functions = mutableListOf<Function>()
        ctx.typeDefBlockStatement().forEach { statement ->
            when {
                statement.variableDecl() != null ->
                    variables.add(processVariableDecl(statement.variableDecl()))

                statement.functionDecl() != null ->
                    functions.add(processFunctionDecl(statement.functionDecl()))
            }
        }

        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())
        val typeIdentifier = processTypeIdentifier(ctx.typeIdentifier())
        val generics = if (typeIdentifier is GenericTypeReference) typeIdentifier.genericReferences else mutableListOf()
        val type = StructuredType(
            name,
            variables,
            functions,
            genericTypes,
            isTypeIdentifier,
            forTypeList,
            annotationReferences,
            context,
            posGetter.getCtxPosition(fileName, ctx),
            generics
        )
        if (type !in context.getAllTypes()) {
            context.storeType(type)
        }
        return processTypeIdentifier(ctx.typeIdentifier())
    }

    override fun visitTypeExpression(ctx: LibSLParser.TypeExpressionContext): TypeReference {
        return when {
            ctx.typeIdentifier() != null -> {
                processTypeIdentifier(ctx.typeIdentifier())
            }
            ctx.AMPERSAND() != null -> {
                processIntersection(ctx)
            }
            ctx.BIT_OR() != null -> {
                processUnion(ctx)
            }
            else -> error("unknown expression type")
        }
    }
    
    private fun processBlockTypeStatements(statementsContexts: List<EnumSemanticTypeEntryContext>): Map<String, Atomic> {
        return statementsContexts.map { ctx -> processBlockTypeStatement(ctx) }.associate { it }
    }

    private fun processBlockTypeStatement(statementContext: EnumSemanticTypeEntryContext): Pair<String, Atomic> {
        val entryName = statementContext.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(context)
        val atomicValueContext = statementContext.expressionAtomic()
        val atomicValue = expressionVisitor.visitExpressionAtomic(atomicValueContext)

        return entryName to atomicValue
    }
    
    private fun processEnumStatements(statements: List<LibSLParser.EnumBlockStatementContext>): Map<String, Atomic> {
        return statements.map { processEnumStatement(it) }.associate { it }
    }

    private fun processEnumStatement(statement: LibSLParser.EnumBlockStatementContext): Pair<String, Atomic> {
        val name = statement.Identifier().asPeriodSeparatedString()

        val expressionVisitor = ExpressionVisitor(context)
        val atomicContext = statement.integerNumber()
        val atomic = expressionVisitor.visit(atomicContext) as Atomic

        return name to atomic
    }

    private val LibSLParser.TypeDefBlockContext.typeDefBlockGenerics: MutableList<GenericType>
        get() = getGenericTypes(this.typeIdentifier().generic(), this.whereConstraints(), context)

    private fun processVariableDecl(ctx: LibSLParser.VariableDeclContext): Variable {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val name = ctx.nameWithType().name.asPeriodSeparatedString()

        val typeReference = TypeVisitor(context).visitTypeExpression(ctx.nameWithType().typeExpression())

        val expressionVisitor = ExpressionVisitor(context)
        val initValue = ctx.assignmentRight()?.let { right -> expressionVisitor.visitAssignmentRight(right) }

        // TODO() Type Context
        return VariableWithInitialValue(
            keyword,
            name,
            typeReference,
            getAnnotationUsages(ctx.annotationUsage()),
            initValue,
            posGetter.getCtxPosition(fileName, ctx)
        )
    }

    private fun processFunctionDecl(ctx: FunctionDeclContext): Function {
        val isMethod = ctx.functionHeader().headerWithAsterisk() != null
        val functionContext = FunctionContext(context)
        var isStatic = false
        if (ctx.functionHeader().modifier != null) {
            if (ctx.functionHeader().modifier.text == "static") {
                isStatic = true
            } else {
                throw IllegalStateException("Unknown modifier, only static allowed")
            }
        }
        val functionName = ctx.functionHeader().functionName.text.extractIdentifier()

        val annotationReferences = getAnnotationUsages(ctx.functionHeader().annotationUsage())

        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        var returnType: TypeReference? = null
        if (ctx.functionHeader().functionType != null)
            returnType = visitTypeExpression(ctx.functionHeader().functionType)

        return Function(
            kind = FunctionKind.FUNCTION,
            functionName,
            automatonReference = null,
            args,
            returnType,
            annotationReferences,
            hasBody = false,
            targetAutomatonRef = null,
            context = functionContext,
            isStatic = isStatic,
            isMethod = isMethod,
            entityPosition = posGetter.getCtxPosition(fileName, ctx)
        )
    }

    private fun processIntersection(ctx: LibSLParser.TypeExpressionContext): TypeReference {
        val left = visitTypeExpression(ctx.typeExpression(0))
        val right = visitTypeExpression(ctx.typeExpression(1))
        return IntersectionExpressionTypeReference(
            left,
            right,
            context
        )
    }

    private fun processUnion(ctx: LibSLParser.TypeExpressionContext): TypeReference {
        val left = visitTypeExpression(ctx.typeExpression(0))
        val right = visitTypeExpression(ctx.typeExpression(1))
        return UnionExpressionTypeReference(
            left,
            right,
            context
        )
    }

    private val FunctionDeclContext.args: List<FunctionArgument>
        get() = this
            .functionHeader().functionDeclArgList()
            ?.parameter()
            ?.mapIndexed { i, parameter ->

                val typeRef = TypeVisitor(context).visitTypeExpression(parameter.typeExpression())

                val annotationsReferences = getAnnotationUsages(parameter.annotationUsage())
                val arg = FunctionArgument(
                    parameter.name.text.extractIdentifier(), typeRef, i,
                    annotationsReferences,
                    targetAutomaton = null,
                    entityPosition = posGetter.getCtxPosition(fileName, parameter)
                )
                arg
            }
            .orEmpty()
}
