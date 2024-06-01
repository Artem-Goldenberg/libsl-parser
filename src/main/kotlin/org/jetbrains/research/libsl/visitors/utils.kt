package org.jetbrains.research.libsl.visitors

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.VocabularyImpl
import org.antlr.v4.runtime.tree.TerminalNode
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.PeriodSeparatedFullNameContext
import org.jetbrains.research.libsl.errors.Position


fun String.removeDoubleQuotes(): String = removeSurrounding("\"", "\"")
fun String.removeQuotes(): String = removeSurrounding("'", "'")

fun TerminalNode.asPeriodSeparatedString(): String = this.text.extractIdentifier()
fun Token.asPeriodSeparatedString(): String = this.text.extractIdentifier()

fun String.extractIdentifier(): String = removeSurrounding("`", "`")

fun PeriodSeparatedFullNameContext.asPeriodSeparatedString(): String {
    var result = Identifier().joinToString(separator = ".") { it.asPeriodSeparatedString() }
    if (result.isEmpty())
        result = this.UNBOUNDED().text
    return result
}

fun PeriodSeparatedFullNameContext.asPeriodSeparatedParts(): List<String> {
    var result = this.Identifier().map { it.text }
    if (result.size == 0)
        result = listOf(this.UNBOUNDED().text)
    return result
}

fun Token.position(): Position {
    return Position(this.line, this.charPositionInLine)
}

fun ParserRuleContext.position() = start.position()

val keywords = (LibSLParser.VOCABULARY as VocabularyImpl).literalNames.filterNotNull().map { k -> k.removeQuotes() }
