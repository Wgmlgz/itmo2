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

    private fun sendEcho(msg: String): String {
        socket = DatagramChannel.open()
        address = InetSocketAddress("localhost", 4445);
        var buf = ByteBuffer.wrap(msg.toByteArray())
        socket.send(buf, address)
        buf = ByteBuffer.wrap(ByteArray(20000))
        socket.receive(buf)
        return String(buf.array(), 0, buf.position())
    }

    fun send(s: String): String {
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
