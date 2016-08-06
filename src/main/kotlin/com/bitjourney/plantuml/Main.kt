package com.bitjourney.plantuml

import spark.Request
import spark.Response
import spark.Spark.*

fun main(args: Array<String>) {
    get("/", {
        request: Request, response: Response ->

        response.type("image/svg+xml")

        "<root />"
    })
}
