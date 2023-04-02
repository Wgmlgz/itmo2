import io.ktor.http.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.internal.decodeStringToJsonTree
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

/**
 * Cmd handles main logic
 *
 * @property save path to save file
 * @constructor Create empty Cmd
 */
class Server(private val dbHandler: DBHandler) {
    private var running = false
    private val server = CmdServer(dbHandler)

    fun run() {
        running = true

        val n = Runtime.getRuntime().availableProcessors()
        val workerPool: ExecutorService = Executors.newFixedThreadPool(n)
        val socket: DatagramSocket = DatagramSocket(4445)

        repeat(n) {
            workerPool.submit {
                while (running) {
                    val buf = ByteArray(20000)
                    val packet = DatagramPacket(buf, buf.size)
                    socket.receive(packet)
                    println(it)
                    val address = packet.address
                    val port = packet.port
                    val received = String(packet.data, 0, packet.length)

                    val queue = LinkedBlockingQueue<Packet>()
                    thread {
                        val toSend = processRequest(received)
                        queue.put(toSend)
                    }

                    thread {
                        val toSend = queue.take().toJson()
                        val chunks = toSend.chunked(chuckSize) as ArrayList<String>;
                        chunks.add("")
                        chunks.forEach {
                            val out = it.encodeToByteArray()
                            val res = DatagramPacket(out, out.size, address, port)
                            socket.send(res)
                        }
                    }
                }
            }

        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun processRequest(s: String): Packet {
        val res = try {
            val json = Json.decodeStringToJsonTree(JsonElement.serializer(), s)
            println(json)
            println("???")
            server.runCmd(json)
        } catch (e: StackOverflowError) {
            println(228)
            println(e)
            val res = e.message ?: "unknown error"
            Packet(null, mutableListOf(StrArg(res)), mutableMapOf())
        }
        return res
    }
}