package com.wgmlgz.server

import CmdServer
import DBHandler
import Packet
import StrArg
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors
import kotlin.concurrent.thread

val dbHandler = DBHandler(
    Database.connect(
        "jdbc:postgresql://localhost:5432/test", driver = "org.postgresql.Driver",
        user = "sus", password = "sus"
    )
)

@CrossOrigin(origins = ["http://localhost:5173"], maxAge = 3600)
@RestController
class Server {
    private val server = CmdServer(dbHandler)

    @OptIn(InternalSerializationApi::class)
    private fun processRequest(json: JsonElement): String {
//        val res = try {
        println(json)
//			val json = Json.decodeStringToJsonTree(JsonElement.serializer(), s)
//			println(json)
        val res = server.runCmd(json)
//        }
//        catch (e: Exception) {
//            println(e)
//            val res = e.toString()
//            Packet(null, mutableListOf(StrArg(res)), mutableMapOf())
//        }
        return res.toJson()
    }

    @PostMapping("/cmd")
    fun hello(@RequestBody payload: JsonElement): String {
        println(payload)
        val res = processRequest(payload)
//        sendEvents()
        return res
    }

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()
//
//    @GetMapping(path = ["/sse"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
//    suspend fun createConnection(): SseEmitter {
//        val emitter = SseEmitter()
//
//        println('1')
//        return emitter
//    }

    // in another thread
//    suspend fun sendEvents() {
//        println('2')
//
////        _events.tryEmit(server.toJson())
//        println('3')
//
//    }

}

@SpringBootApplication
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}
