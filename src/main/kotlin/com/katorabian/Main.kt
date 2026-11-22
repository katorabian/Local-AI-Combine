package com.katorabian

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

fun main() {
    embeddedServer(Netty, host = "127.0.0.1", port = 8080) {
        health()
    }.start(wait = true)
}

fun Application.health() {
    install(ContentNegotiation) {
        json()
    }
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ApiError(code = "not_found", message = "Маршрут не найден")
            )
        }
    }
    routing {
        get("/v1/health") {
            call.respond(HealthResponse(status = "ok"))
        }
    }
}

@Serializable
data class HealthResponse(
    val status: String
)

@Serializable
data class ApiError(
    val code: String,
    val message: String
)