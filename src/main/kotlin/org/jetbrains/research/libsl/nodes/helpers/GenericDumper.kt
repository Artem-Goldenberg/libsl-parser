package org.jetbrains.research.libsl.nodes.helpers

import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.GenericType
import org.jetbrains.research.libsl.type.GenericTypeBound
import java.util.*

fun appendGeneric(stringBuilder: StringBuilder, typeReference: TypeReference) {
    // TODO: think about pointers (typeReference.isPointer) for generic;

    val queue = LinkedList<Pair<TypeReference, Int>>()

    queue.addLast(Pair(typeReference, 0))

    appendGenericsToQueue(queue, 1)
    var prevDeepLevel = 0

    val mainType = queue.removeFirst()
    stringBuilder.append("${addAsteriskForPointer(mainType.first)}${getBound(mainType.first)}${mainType.first.name}")
    var counterOfClosedBrackets = 0

    while (queue.isNotEmpty()) {

        val currentTypeRef = queue.peek().first
        val currentDeepLevel = queue.poll().second

        if (currentDeepLevel > prevDeepLevel) {
            stringBuilder.append("<${addAsteriskForPointer(mainType.first)}${getBound(currentTypeRef)}${currentTypeRef.name}")
            ++counterOfClosedBrackets
        }

        if (currentDeepLevel == prevDeepLevel) {
            stringBuilder.append(", ${addAsteriskForPointer(mainType.first)}${getBound(currentTypeRef)}${currentTypeRef.name}")
        }

        if (currentDeepLevel < prevDeepLevel) {
            while (counterOfClosedBrackets != currentDeepLevel) {
                stringBuilder.append(">")
                --counterOfClosedBrackets
            }
            stringBuilder.append(", ${addAsteriskForPointer(mainType.first)}${getBound(currentTypeRef)}${currentTypeRef.name}")
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

private fun getBound(type: TypeReference): String {
    if (type.typeBound != GenericTypeBound.EMPTY) {
        return type.typeBound.string + " "
    }
    return ""
}


fun appendGenericArray(stringBuilder: StringBuilder, generics: MutableList<TypeReference>) {
    stringBuilder.append("<")
    val size = generics.size - 1
    for (i in 0 until size) {
        appendGeneric(stringBuilder, generics[i])
        stringBuilder.append(", ")
    }
    appendGeneric(stringBuilder, generics[size])
    stringBuilder.append(">")
}

private fun addAsteriskForPointer(type: TypeReference): String {
    return (if (type.isPointer) "*" else "")
}

fun appendWhereSection(stringBuilder: StringBuilder, generics: MutableList<GenericType>) {
    stringBuilder.append(" where")
    for (generic in generics) {
        for (constraint: TypeReference in generic.constraints) {
            val typeBound =
                if (!GenericTypeBound.EMPTY.equals(generic.typeBound)) generic.typeBound.string + " " else ""
            stringBuilder.append(" " + generic.name + ": " + typeBound)
            appendGeneric(stringBuilder, constraint)
            stringBuilder.append(",")
        }
    }
    stringBuilder.deleteCharAt(stringBuilder.length - 1)
}