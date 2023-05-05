package com.wgmlgz.server

import CmdServer
import DBHandler
import Packet
import StrArg
import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import io.ktor.http.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.internal.decodeStringToJsonTree
import org.springframework.web.bind.annotation.RequestBody

val dbHandler = DBHandler(
    Database.connect(
        "jdbc:postgresql://localhost:5432/test", driver = "org.postgresql.Driver",
        user = "sus", password = "sus"
    )
)

@RestController
class Server {
    private var running = false
    private val server = CmdServer(dbHandler)

    @OptIn(InternalSerializationApi::class)
    private fun processRequest(s: String): String {
        val res = try {
            println(s)
            val json = Json.decodeStringToJsonTree(JsonElement.serializer(), s)
            println(json)
            println("???")
            server.runCmd(json)
        } catch (e: Exception) {
            println(228)
            println(e)
            val res = e.message ?: "unknown error"
            Packet(null, mutableListOf(StrArg(res)), mutableMapOf())
        }
        return res.toJson()
    }

    @PostMapping("/")
    fun hello(@RequestBody payload: String): String {
        println(payload)

        return processRequest(payload)
    }
}

@SpringBootApplication
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}