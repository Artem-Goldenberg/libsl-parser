package org.jetbrains.research.libsl.nodes.helpers

import org.jetbrains.research.libsl.nodes.references.*
import org.jetbrains.research.libsl.type.GenericType
import org.jetbrains.research.libsl.type.GenericTypeBound
import org.jetbrains.research.libsl.type.Type
import java.util.*

fun appendGeneric(stringBuilder: StringBuilder, typeReference: TypeReference) {
    // TODO: think about pointers (typeReference.isPointer) for generic;

    val queue = LinkedList<Pair<TypeReference, Int>>()

    queue.addLast(Pair(typeReference, 0))

    appendGenericsToQueue(queue, 1)
    var prevDeepLevel = 0

    val mainTypeRef = queue.removeFirst().first
    appendResolvedGeneric(stringBuilder, mainTypeRef)
    var counterOfClosedBrackets = 0

    while (queue.isNotEmpty()) {

        val currentTypeRef = queue.peek().first
        val currentDeepLevel = queue.poll().second

        if (currentDeepLevel > prevDeepLevel) {
            stringBuilder.append("<")
            appendResolvedGeneric(stringBuilder, currentTypeRef)
            ++counterOfClosedBrackets
        }

        if (currentDeepLevel == prevDeepLevel) {
            stringBuilder.append(", ")
            appendResolvedGeneric(stringBuilder, currentTypeRef)
        }

        if (currentDeepLevel < prevDeepLevel) {
            while (counterOfClosedBrackets != currentDeepLevel) {
                stringBuilder.append(">")
                --counterOfClosedBrackets
            }
            stringBuilder.append(", ")
            appendResolvedGeneric(stringBuilder, currentTypeRef)
        }

        prevDeepLevel = currentDeepLevel
    }
    while (counterOfClosedBrackets != 0) {
        stringBuilder.append(">")
        --counterOfClosedBrackets
    }
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

fun appendWhereSection(stringBuilder: StringBuilder, generics: MutableList<GenericType>) {
    stringBuilder.append(" where")
    for (generic in generics) {
        for (constraint: TypeReference in generic.constraints) {
            stringBuilder.append(" " + generic.name + ": ")
            appendGeneric(stringBuilder, constraint)
            stringBuilder.append(",")
        }
    }
    stringBuilder.deleteCharAt(stringBuilder.length - 1)
}

private fun appendResolvedGeneric(stringBuilder: StringBuilder, currentTypeRef: TypeReference) {
    if (currentTypeRef.resolve() != null) {
        stringBuilder.append("${addAsteriskForPointer(currentTypeRef)}${getBound(currentTypeRef)}${currentTypeRef.toSimpleString()}")
    } else
        stringBuilder.append(Type.UNRESOLVED_TYPE_SYMBOL)
}

private fun appendGenericsToQueue(queue: LinkedList<Pair<TypeReference, Int>>, deep: Int) {
    val firstTypeReference = queue.peekLast().first

    val genericReferences = if (firstTypeReference is GenericTypeReference)
        (queue.peekLast().first as GenericTypeReference).genericReferences
    else if (queue.peekLast().first is WildcardTypeReference)
        (queue.peekLast().first as WildcardTypeReference).genericReferences
    else mutableListOf()

    if (genericReferences.isEmpty()) return
    genericReferences.forEach {
        queue.addLast(Pair(it, deep))
        appendGenericsToQueue(queue, deep + 1)
    }
}

private fun getBound(type: TypeReference): String {
    if (type is WildcardTypeReference && type.typeBound != GenericTypeBound.EMPTY)
        return type.typeBound.string + " "
    return ""
}

private fun addAsteriskForPointer(type: TypeReference): String {
    if (type is PlainTypeReference)
        return (if (type.isPointer) "*" else "")
    return ""
}