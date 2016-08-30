package com.bitjourney.plantuml

import com.google.gson.JsonObject
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.code.TranscoderUtil
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils
import org.slf4j.LoggerFactory
import spark.Response
import spark.Spark
import java.io.File
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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

    fun start(port: Int, graphvizDot: Path?) {
        graphvizDot?.let { path ->
            GraphvizUtils.setDotExecutable(path.toString());
        }

        checkTools()

        Spark.port(port)

        Spark.after { request, response ->
            response.header("Content-Encoding", "gzip")
        }

        Spark.exception(Exception::class.java, { exception, request, response ->
            response.status(400)
            render("@startuml\n${exception.message}\n@enduml\n", response)
        })

        Spark.get("/svg/:source", { request, response ->
            val source = decodeSource(request.params(":source"))
            render(source, response)
        })

        Spark.get("/version", { request, response ->
            response.type("application/json")
            val json = JsonObject()
            json.addProperty("PlantUML", javaClass.getPackage().implementationVersion)
            response.body(json.toString())
        })
    }

    fun render(source: String, response: Response) {
        response.type("image/svg+xml")

        val renderer = SourceStringReader(source)
        renderer.generateImage(response.raw().outputStream, FileFormatOption(FileFormat.SVG, true))
    }

    fun decodeSource(urlEncodedSource: String): String {
        val source = URLDecoder.decode(urlEncodedSource, "UTF-8")

        if (source.startsWith("@startuml")) {
            return source;
        } else {
            val transcoder = TranscoderUtil.getDefaultTranscoder()
            return transcoder.decode(source)
        }
    }

    fun checkTools() {
        val version = GraphvizUtils.getDotVersion();

        if (version == -1) {
            throw AssertionError("No GraphViz dot found in the PATH.")
        }

        GraphvizUtils.getTestDotStrings(false).forEach { message ->
            logger.info(message)
        }
    }
}
