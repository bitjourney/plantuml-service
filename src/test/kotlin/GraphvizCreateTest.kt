package com.bitjourney.plantuml

import net.sourceforge.plantuml.dot.Graphviz
import net.sourceforge.plantuml.dot.GraphvizUtils
import org.junit.Test
import java.io.File

class GraphvizCreateTest {
    
    @Test
    fun testGraphvizCreation() {
        val output = StringBuilder()
        
        try {
            // Look for factory methods or ways to create Graphviz instances
            output.appendLine("Looking for Graphviz creation methods...")
            
            // Check if GraphvizUtils has create methods
            val graphvizUtilsClass = GraphvizUtils::class.java
            output.appendLine("\nGraphvizUtils static methods:")
            graphvizUtilsClass.declaredMethods
                .filter { java.lang.reflect.Modifier.isStatic(it.modifiers) }
                .forEach { method ->
                    output.appendLine("  static ${method.name}(${method.parameterTypes.joinToString(", ") { it.simpleName }}) -> ${method.returnType.simpleName}")
                }
            
            // Check if there are any other GraphViz related classes
            val possibleClasses = listOf(
                "net.sourceforge.plantuml.dot.GraphvizMaker",
                "net.sourceforge.plantuml.dot.DotExecutor", 
                "net.sourceforge.plantuml.dot.GraphvizCmdLine",
                "net.sourceforge.plantuml.dot.DotRenderer",
                "net.sourceforge.plantuml.security.SecurityProfile"
            )
            
            possibleClasses.forEach { className ->
                try {
                    val clazz = Class.forName(className)
                    output.appendLine("\nFound class: $className")
                    // Check for static factory methods
                    clazz.declaredMethods
                        .filter { java.lang.reflect.Modifier.isStatic(it.modifiers) }
                        .forEach { method ->
                            output.appendLine("  static ${method.name}(${method.parameterTypes.joinToString(", ") { it.simpleName }}) -> ${method.returnType.simpleName}")
                        }
                } catch (e: ClassNotFoundException) {
                    output.appendLine("Class not found: $className")
                }
            }
            
        } catch (e: Exception) {
            output.appendLine("Error: ${e.message}")
            e.printStackTrace()
        }
        
        // Write results to file
        File("graphviz-create-test.txt").writeText(output.toString())
    }
}