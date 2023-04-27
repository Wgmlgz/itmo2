import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.*

/**
 * Client
 */
class Client {
    private var socket = DatagramChannel.open()
    private var address: SocketAddress = InetSocketAddress("localhost", 4445);

    private fun sendEcho(msg: Packet): Packet {
        socket = DatagramChannel.open()
        address = InetSocketAddress("localhost", 4445);
        val buf = ByteBuffer.wrap(msg.toJson().toByteArray())
        socket.send(buf, address)

        var res = "";
        while (true) {
            val bufChunk = ByteBuffer.wrap(ByteArray(chuckSize))
            socket.receive(bufChunk)
            val chunk = String(bufChunk.array(), 0, bufChunk.position())
            res += chunk
            if (chunk.isEmpty()) {
                break
            }
        }

        return Packet.fromJson(res)
    }

    fun send(s: Packet): Packet {
        val timeout: Long = 5
        val executor = Executors.newCachedThreadPool()
        val task = Callable { sendEcho(s) }
        val future = executor.submit(task)
        try {
            return future[timeout, TimeUnit.SECONDS]
        } catch (ex: TimeoutException) {
            println("timeout ${timeout}s, run again? [y/N]")
            if (readLine() == "y") return send(s)
            throw Exception("timeout")
        }
    }

    fun close() {
        socket.close()
    }

}
