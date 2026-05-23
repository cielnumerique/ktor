package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Application.configureRouting() {
    val engine = ComputationEngine()

    routing {
        get("/") {
            call.respondText("Matrix Computation Service\nPOST /compute\nGET  /result/{id}")
        }

        post("/compute") {
            val request = call.receive<ComputeRequest>()
            val id = UUID.randomUUID().toString()
            engine.submit(id, request)
            call.respond(HttpStatusCode.Accepted, SubmitResponse(id, ComputationStatus.PENDING))
        }

        get("/result/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
            val result = engine.getResult(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "No task with id=$id")
            call.respond(result)
        }
    }
}
