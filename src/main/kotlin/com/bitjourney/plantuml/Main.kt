package com.bitjourney.plantuml

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.code.TranscoderUtil
import spark.Response
import spark.Spark
import java.net.URLDecoder

class Main {
    companion object {
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
            val port = if (args.size > 0) args[0].toInt() else 4567
            Main().start(port)
        }
    }

    fun start(port: Int) {
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
            return transcoder.decode(source);
        }
    }
}
