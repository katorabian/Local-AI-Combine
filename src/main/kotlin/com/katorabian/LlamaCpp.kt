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