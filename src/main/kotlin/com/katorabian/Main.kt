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
    val llama = LlamaCpp(
        exe = "F:\\runtime\\llama.cpp\\llama-server.exe",
        model = "F:\\models\\gguf\\Dolphin3.0-Llama3.1-8B-Q8_0.gguf",
        port = 8081
    )
    Runtime.getRuntime().addShutdownHook(Thread {
        runCatching { llama.stop() }
    })

    val server = embeddedServer(Netty, host = "127.0.0.1", port = 8080) {
        health()
    }
    server.start(wait = false)

    llama.start()
    println("llama.cpp готов")
    Thread.currentThread().join()
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