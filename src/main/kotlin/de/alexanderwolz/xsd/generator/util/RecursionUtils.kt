package de.alexanderwolz.xsd.generator.util

import de.alexanderwolz.commons.util.xsd.XsdFileReference

object RecursionUtils {

    fun getDepth(reference: XsdFileReference): Int {
        var depth = 0
        var current = reference.parent
        while (current != null) {
            depth++
            current = current.parent
        }
        return depth
    }

    fun traverseBottomUp(reference: XsdFileReference, action: (XsdFileReference) -> Unit) {
        reference.children.forEach { child ->
            traverseBottomUp(child, action)
        }
        action(reference)
    }

    fun traverseTopDown(reference: XsdFileReference, action: (XsdFileReference) -> Unit) {
        action(reference)
        reference.children.forEach { child ->
            traverseTopDown(child, action)
        }
    }
}