package org.example

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.Serializable

class EnsureSerializableProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val visitedNodes = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(EnsureSerializable::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        symbols.forEach { node  -> processRecursively(node) }
        return emptyList()
    }

    private fun processRecursively(node: KSClassDeclaration) {
        val nodeName = node.qualifiedName!!.asString()
        if (isNotVisited(nodeName)) markAsVisited(nodeName) else return

        val childNodes = node.getAllProperties().mapNotNull { it.type.resolve().declaration as? KSClassDeclaration }
        for (childNode in childNodes) {
            if (!hasSerializableInterface(childNode)) {
                val message = errorMessage(node, childNode)
                environment.logger.error(message)
            } else {
                processRecursively(childNode)
            }
        }
    }

    private fun errorMessage(node: KSClassDeclaration, childNode: KSClassDeclaration): String {
        return "Class \"${node.qualifiedName?.asString()}\" has not serializable child of type \"${childNode.qualifiedName?.asString()}\""
    }

    private fun isNotVisited(nodeName: String): Boolean {
        return !visitedNodes.contains(nodeName)
    }

    private fun markAsVisited(nodeName: String) {
        visitedNodes.add(nodeName)
    }

    private fun hasSerializableInterface(node: KSClassDeclaration): Boolean {
        // TODO (Fix  it. It doesn't work for case, when class doesn't implement Serializable, but parent implement)
        return node.superTypes
            .map { it.resolve().declaration.qualifiedName?.asString() }
            .contains(Serializable::class.java.typeName)
    }

}