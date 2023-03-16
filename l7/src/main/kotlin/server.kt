import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.internal.decodeStringToJsonTree
import kotlinx.serialization.json.jsonObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*
import org.jetbrains.exposed.sql.*


/**
 * Cmd handles main logic
 *
 * @property save path to save file
 * @constructor Create empty Cmd
 */
class Server(private val dbHandler: DBHandler) {
    private var socket: DatagramSocket = DatagramSocket(4445)
    private var running = false
    private val server = CmdServer(dbHandler)

    fun run() {
        running = true
        while (running) {
            val buf = ByteArray(20000)
            val packet = DatagramPacket(buf, buf.size)
            socket.receive(packet)
            val address = packet.address
            val port = packet.port
            var received = String(packet.data, 0, packet.length)
            received = processRequest(received)
            val out = received.encodeToByteArray()
            val res = DatagramPacket(out, out.size, address, port)
            socket.send(res)
        }
        socket.close()
    }

    @OptIn(InternalSerializationApi::class)
    private fun processRequest(s: String): String {
        val res = try {
            val json = Json.decodeStringToJsonTree(JsonElement.serializer(), s)
            print(json)
            server.runCmd(json)
        } catch (e: Exception) {
            println(e)
            e.message ?: "unknown error"
        }
        return res
    }
}

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