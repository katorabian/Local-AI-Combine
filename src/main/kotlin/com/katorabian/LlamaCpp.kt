package com.katorabian

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.util.concurrent.TimeUnit

class LlamaCpp(
    private val exe: String,
    private val model: String,
    private val port: Int
) {
    private var process: Process? = null

    fun start() {
        val builder = ProcessBuilder(llamaCommand(exe, model, port))
        builder.directory(File(exe).parentFile)
        builder.redirectErrorStream(true)

        val started = builder.start()
        process = started

        Thread {
            started.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line -> println("[llama] $line")}
            }
        }.apply {
            isDaemon = true
            name = "llama-log"
            start()
        }
        waitUntilReady()
    }

    private fun waitUntilReady() {
        val deadline = System.currentTimeMillis() + 180_000
        val url = URI("http://127.0.0.1:$port/health").toURL()

        while(System.currentTimeMillis() < deadline) {
            val running = process?: error("Запуск llama.cpp прерван")
            if (!running.isAlive) {
                error("llama.cpp завершился до готовности, код ${running.exitValue()}")
            }

            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 500
                connection.readTimeout = 500
                connection.requestMethod = "GET"

                val code = connection.responseCode
                connection.disconnect()

                if (code == HttpURLConnection.HTTP_OK) return
            } catch (_: IOException) {

            }
            Thread.sleep(300)
        }

        error("llama.cpp не ответил на /health за 180 сек")
    }

    fun stop() {
        val running = process?: return
        process = null

        if (!running.isAlive) return
        running.destroy()

        if (!running.waitFor(5, TimeUnit.SECONDS)) {
            running.destroyForcibly()
        }
    }
}

fun llamaCommand(
    exe: String,
    model: String,
    port: Int
): List<String> {
    return listOf(
        exe,
        "--host", "127.0.0.1",
        "--port", port.toString(),
        "-m", model,
        "-ngl", "99",
        "-c", "2048",
        "--no-webui"
    )
}