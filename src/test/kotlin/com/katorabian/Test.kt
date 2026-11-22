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
}