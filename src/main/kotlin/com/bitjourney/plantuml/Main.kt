package com.bitjourney.plantuml

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.google.gson.JsonObject
import net.sourceforge.plantuml.Option
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.code.TranscoderSmart
import net.sourceforge.plantuml.dot.GraphvizUtils
import org.slf4j.LoggerFactory
import spark.Filter
import spark.Response
import spark.Spark
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Main {
    companion object {
        val DEFAULT_PORT = 1608

        init {
            // -Dfile.encoding=UTF-8
            System.setProperty("file.encoding", "UTF-8")

            // -Djava.awt.headless=true
            System.setProperty("java.awt.headless", "true")
        }

        /*
         * Starts the HTTP server
         *
         * See also: http://plantuml.com/server.html
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val port = if (args.isNotEmpty()) {
                args[0].toInt()
            } else {
                DEFAULT_PORT
            }
            val graphvizDot = if (args.size > 1) {
                Paths.get(args[1])
            } else {
                findCommand("dot")
            }

            Main().start(port, graphvizDot)
        }

        fun findCommand(command: String): Path? {
            return System.getenv("PATH").splitToSequence(File.pathSeparator)
                    .map { path ->
                        Paths.get(path).resolve(command)
                    }
                    .find { path ->
                        Files.exists(path)
                    }
        }
    }

    val logger = LoggerFactory.getLogger(Main::class.java)

    val transcoder = TranscoderSmart()

    val versionJson: ByteArray = JsonObject().let { json ->
        json.addProperty("PlantUML", javaClass.getPackage().specificationVersion)
        json.addProperty("plantuml-service", javaClass.getPackage().implementationVersion)
        json.toString().toByteArray(Charsets.UTF_8)
    }

    val identityJson: ByteArray = JsonObject().let { json ->
        json.addProperty("repository", "https://github.com/bitjourney/plantuml-service")
        json.toString().toByteArray(Charsets.UTF_8)
    }

    // NOTE: Remove "skinparam monochrome true" for a while because it lets "SALT" to cause errors :(
    //val defaultConfig: List<String> = Arrays.asList()
    val option = Option("-tsvg")

    val loader: LoadingCache<DataSource, ByteArray> = Caffeine.newBuilder()
            .maximumWeight(50 * 1024 * 1024) // about 50MiB
            .weigher<DataSource, ByteArray> { key: DataSource, value: ByteArray -> key.weight() + value.size }
            .build({ key: DataSource -> render(key) })

    fun installGraphvizDotExecutable(graphvizDot: Path?) {
        graphvizDot?.let { path ->
            // Set GraphViz executable path via system property (new approach in PlantUML 1.2025.x)
            System.setProperty("GRAPHVIZ_DOT", path.toString())
            System.setProperty("plantuml.graphviz.dot", path.toString())
            logger.info("GraphViz dot executable set to: $path")
        }
    }

    fun start(port: Int, graphvizDot: Path?) {
        checkTools(graphvizDot)

        Spark.port(port)

        Spark.before(Filter { _, response ->
            installGraphvizDotExecutable(graphvizDot)
            response.header("Access-Control-Allow-Origin", "*")
        })
        Spark.after(Filter { _, response ->
            response.header("Content-Encoding", "gzip")
        })

        Spark.exception(Exception::class.java, { exception, _, response ->
            response.status(400)
            renderToResponse("@startuml\n${exception.message}\n\n@enduml\n", response)
        })

        Spark.options("/*", { request, response ->
            val accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            val accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
        })

        Spark.get("/", { request, response ->
            response.type("application/json")
            response.header("Content-Length", identityJson.size.toString())
            response.raw().outputStream.write(identityJson)
        })

        Spark.get("/svg/:source", { request, response ->
            val source = decodeSource(request.params(":source"))
            renderToResponse(source, response)
        })

        Spark.post("/svg", { request, response ->
            val source = decodeSource(request.body())
            renderToResponse(source, response)
        })

        Spark.get("/version", { _, response ->
            response.type("application/json")

            response.header("Content-Length", versionJson.size.toString())
            response.raw().outputStream.write(versionJson)
        })
    }

    fun renderToResponse(source: String, response: Response) {
        response.type("image/svg+xml")

        val svg = loader.get(DataSource(source, option))!!
        response.header("Content-Length", svg.size.toString())
        response.raw().outputStream.write(svg)
    }

    fun render(data: DataSource): ByteArray {
        val renderer = SourceStringReader(data.option.defaultDefines, data.source, data.option.config)
        val outputStream = ByteArrayOutputStream()
        renderer.outputImage(outputStream, data.option.fileFormatOption)
        return outputStream.toByteArray()
    }

    fun decodeSource(source: String): String {
        if (source.startsWith("@startuml")) {
            return source
        } else {
            return transcoder.decode(source)
        }
    }

    fun checkTools(graphvizDot: Path?) {
        installGraphvizDotExecutable(graphvizDot)
        
        // Check GraphViz availability using ProcessBuilder (equivalent functionality)
        try {
            val dotCommand = graphvizDot?.toString() ?: "dot"
            val process = ProcessBuilder(dotCommand, "-V")
                .redirectErrorStream(true)
                .start()
            
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            
            if (exitCode == 0) {
                logger.info("GraphViz is available: $output")
            } else {
                logger.warn("GraphViz check failed with exit code: $exitCode")
            }
        } catch (e: Exception) {
            logger.warn("GraphViz dot not found. Some diagrams may not render correctly: ${e.message}")
        }
    }
}
