package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.LiteralType
import org.jetbrains.research.libsl.type.NextCompositionTypesSymbol
import org.jetbrains.research.libsl.type.СompositeType
import org.jetbrains.research.libsl.utils.PositionGetter

class BlockStatementVisitor(
    private val functionContext: FunctionContext,
    val globalContext: LslGlobalContext
) : LibSLParserVisitor<Unit>(functionContext) {
    val statements: MutableList<Statement> = mutableListOf()
    private val fileName = context.fileName
    private val posGetter = PositionGetter()

    override fun visitExpression(ctx: LibSLParser.ExpressionContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val expression = ExpressionStatement(
            expressionVisitor.visitExpression(ctx),
            posGetter.getCtxPosition(fileName, ctx)
        )
        statements.add(expression)
    }

    override fun visitVariableAssignment(ctx: LibSLParser.VariableAssignmentContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val left = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
        val op = AssignOps.fromString(ctx.op.text)
        val value = ctx.assignmentRight().let { expressionVisitor.visitAssignmentRight(it) }
        val assignment = Assignment(
            left,
            op,
            value,
            posGetter.getCtxPosition(fileName, ctx)
        )
        statements.add(assignment)
    }

    override fun visitIfStatement(ifCtx: LibSLParser.IfStatementContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val value = expressionVisitor.visitExpression(ifCtx.expression())

        val ifStatementVisitor = BlockStatementVisitor(functionContext, globalContext)
        ifCtx.functionBodyStatement().forEach { ifStatementVisitor.visit(it) }
        val ifStatements = ifStatementVisitor.statements

        val elseStatement = ifCtx.elseStatement()?.let { elseStmt ->

            val elseStatementsVisitor = BlockStatementVisitor(functionContext, globalContext)
            elseStmt.functionBodyStatement().forEach { elseStatementsVisitor.visit(it) }
            val elseStatements = elseStatementsVisitor.statements
            ElseStatement(
                elseStatements,
                posGetter.getCtxPosition(fileName, ifCtx)
            )
        }

        val ifBlock = IfStatement(
            value,
            ifStatements,
            elseStatement,
            posGetter.getCtxPosition(fileName, ifCtx)
        )

        statements.add(ifBlock)
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val name = ctx.nameWithType().name.asPeriodSeparatedString()
        
        val isCompositeType = ctx.nameWithType().typesIdentifiersArray().typeIdentifier().size > 1
        lateinit var compositeType: СompositeType
        if (isCompositeType) {
            compositeType = buildCompositeType(ctx.nameWithType().typesIdentifiersArray())
            globalContext.storeType(compositeType)
        }

        val typeReference = if (!isCompositeType) processTypeIdentifier(
            ctx.nameWithType().typesIdentifiersArray().typeIdentifier(0)
        ) else processCompositeType(compositeType)

        if (isNotStoredLiteralType(
                globalContext,
                typeReference,
                ctx.nameWithType().typesIdentifiersArray().typeIdentifier(0)
            )
        )
            globalContext.storeType(LiteralType(context, typeReference.name))

        val expressionVisitor = ExpressionVisitor(context)
        val initValue = ctx.assignmentRight()?.let {
            expressionVisitor.visitAssignmentRight(it)
        }

        val variable = VariableWithInitialValue(
            keyword,
            name,
            typeReference,
            getAnnotationUsages(ctx.annotationUsage()),
            initValue,
            posGetter.getCtxPosition(fileName, ctx)
        )
        val variableDeclaration = VariableDeclaration(
            variable,
            posGetter.getCtxPosition(fileName, ctx)
        )
        statements.add(variableDeclaration)
        context.storeVariable(variable)
    }

    // Why do we need this fun ?
    override fun visitElseStatement(ctx: LibSLParser.ElseStatementContext) {
        error("Unreachable")
    }
}
