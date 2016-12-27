package com.bitjourney.plantuml

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.google.gson.JsonObject
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.code.AsciiEncoder
import net.sourceforge.plantuml.code.CompressionZlib
import net.sourceforge.plantuml.code.TranscoderImpl
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils
import net.sourceforge.plantuml.preproc.Defines
import org.slf4j.LoggerFactory
import spark.Filter
import spark.Response
import spark.Spark
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class Main {
    companion object {
        val DEFAULT_PORT = 1608

        init {
            //  -Djava.awt.headless=true -Dfile.encoding=UTF-8
            System.setProperty("file.encoding", "UTF-8")
            System.setProperty("java.awt.headless", "true")
        }

        /*
         * Starts the HTTP server
         *
         * See also: http://plantuml.com/server.html
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val port = if (args.size > 0) args[0].toInt() else DEFAULT_PORT
            val graphvizDot = if (args.size > 1) Paths.get(args[1]) else findCommand("dot")
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

    val transcoder = TranscoderImpl(AsciiEncoder(), CompressionZlib())

    val versionJson: ByteArray = {
        val json = JsonObject()
        json.addProperty("PlantUML", javaClass.getPackage().specificationVersion)
        json.addProperty("plantuml-service", javaClass.getPackage().implementationVersion)
        json.toString().toByteArray(Charsets.UTF_8)
    }()

    val loader: LoadingCache<DataSource, ByteArray> = Caffeine.newBuilder()
            .maximumWeight(50 * 1024 * 1024) // about 50MiB
            .weigher { key: DataSource, value: ByteArray -> key.weight() + value.size }
            .build({ key: DataSource -> render(key) })

    val defaultConfig = Arrays.asList("skinparam monochrome true")

    fun installGraphvizDotExecutable(graphvizDot: Path?) {
        graphvizDot?.let { path ->
            GraphvizUtils.setDotExecutable(path.toString())
        }
    }

    fun start(port: Int, graphvizDot: Path?) {
        checkTools(graphvizDot)

        Spark.port(port)

        Spark.before(Filter { request, response ->
            installGraphvizDotExecutable(graphvizDot)
        })
        Spark.after(Filter { request, response ->
            response.header("Content-Encoding", "gzip")
        })

        Spark.exception(Exception::class.java, { exception, request, response ->
            response.status(400)
            renderToResponse("@startuml\n${exception.message}\n\n@enduml\n", response)
        })

        Spark.get("/svg/:source", { request, response ->
            val configArray = request.queryParamsValues("config")
            val source = decodeSource(request.params(":source"))
            renderToResponse(source, response, configArray)
        })

        Spark.get("/version", { request, response ->
            response.type("application/json")

            response.header("Content-Length", versionJson.size.toString())
            response.raw().outputStream.write(versionJson);
        })
    }

    fun renderToResponse(source: String, response: Response, configArray: Array<String>? = null) {
        response.type("image/svg+xml")

        val svg = loader.get(DataSource(source, defaultConfig +  (configArray ?: arrayOf()).toList()))!!
        response.header("Content-Length", svg.size.toString())
        response.raw().outputStream.write(svg);
    }

    fun render(data: DataSource): ByteArray {
        val renderer = SourceStringReader(Defines(), data.source, data.configArray)
        val outputStream = ByteArrayOutputStream()
        renderer.generateImage(outputStream, FileFormatOption(FileFormat.SVG, true))
        return outputStream.toByteArray();
    }

    fun decodeSource(urlEncodedSource: String): String {
        val source = URLDecoder.decode(urlEncodedSource, "UTF-8")

        if (source.startsWith("@startuml")) {
            return source
        } else {
            return transcoder.decode(source)
        }
    }

    fun checkTools(graphvizDot: Path?) {
        installGraphvizDotExecutable(graphvizDot)

        val version = GraphvizUtils.getDotVersion()

        if (version == -1) {
            throw AssertionError("No GraphViz dot found in the PATH.")
        }

        GraphvizUtils.getTestDotStrings(false).forEach { message ->
            logger.info(message)
        }
    }
}
