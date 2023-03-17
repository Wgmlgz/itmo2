import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.internal.decodeStringToJsonTree
import kotlinx.serialization.json.jsonObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*
import org.jetbrains.exposed.sql.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    val dbHandler = DBHandler(
        Database.connect(
            "jdbc:postgresql://localhost:5432/test", driver = "org.postgresql.Driver",
            user = "postgres", password = "cat"
        )
    )

    val server = Server(dbHandler)
    server.run()
}