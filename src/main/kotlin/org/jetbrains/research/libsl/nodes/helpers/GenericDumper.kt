package org.jetbrains.research.libsl.nodes.helpers

import org.jetbrains.research.libsl.nodes.references.TypeReference
import java.util.*

fun appendGeneric(stringBuilder: StringBuilder, typeReference: TypeReference) {

    // stringBuilder.append(if (typeReference!!.isPointer) "*" else "")

    val queue = LinkedList<Pair<TypeReference, Int>>()

    queue.addLast(Pair(typeReference, 0))

    appendGenericsToQueue(queue, 1)
    var prevDeepLevel = 0

    val mainType = queue.removeFirst()
    stringBuilder.append(mainType.first.name)
    var counterOfClosedBrackets = 0

    while (queue.isNotEmpty()) {

        val currentTypeRef = queue.peek().first
        val currentDeepLevel = queue.poll().second

        if (currentDeepLevel > prevDeepLevel) {
            stringBuilder.append("<${currentTypeRef.name}")
            ++counterOfClosedBrackets
        }

        if (currentDeepLevel == prevDeepLevel) {
            stringBuilder.append(", ${currentTypeRef.name}")
        }

        if (currentDeepLevel < prevDeepLevel){
            stringBuilder.append(">, ${currentTypeRef.name}")
            --counterOfClosedBrackets
        }

        prevDeepLevel = currentDeepLevel
    }
    while (counterOfClosedBrackets != 0) {
        stringBuilder.append(">")
        --counterOfClosedBrackets
    }
}

private fun appendGenericsToQueue(queue: LinkedList<Pair<TypeReference, Int>>, deep: Int) {
    val genericReferences = queue.peekLast().first.genericReferences
    if (genericReferences.isEmpty()) return
    genericReferences.forEach {
        queue.addLast(Pair(it, deep))
        appendGenericsToQueue(queue, deep + 1)
    }
}