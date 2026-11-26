package com.katorabian

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Test {

    @Test
    fun healthRestoreOk() = testApplication {
        application {
            health()
        }
        val response = client.get("/v1/health")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = Json.decodeFromString<HealthResponse>(response.bodyAsText())
        assertEquals("ok", body.status)
    }

    @Test
    fun unknownRouteReturnsErrorBody() = testApplication {
        application {
            health()
        }
        val response = client.get("/v1/foo/bar")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = Json.decodeFromString<ApiError>(response.bodyAsText())
        assertEquals("not_found", body.code)
        assertEquals("Маршрут не найден", body.message)
    }

    @Test
    fun llamaCommandPointsAtModelAndPort() {
        val exe = "F:\\runtime\\llama.cpp\\llama-server.exe"
        val model = "F:\\models\\gguf\\Dolphin3.0-Llama3.1-8B-Q8_0.gguf"
        val command = llamaCommand(exe, model, 8081)
        assertEquals(exe, command[0])
        assertEquals("8081", command[command.indexOf("--port") + 1])
        assertEquals(model, command[command.indexOf("-m") + 1])
        assertEquals(true, command.contains("--no-webui"))
    }
}