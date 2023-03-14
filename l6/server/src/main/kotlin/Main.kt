import org.apache.logging.log4j.kotlin.Logging
import java.net.DatagramPacket
import java.net.DatagramSocket

internal object UDPServerEx : Logging {
    private var socket: DatagramSocket? = null
    private var running = false

    fun EchoServer() {
        socket = DatagramSocket(4445)
    }

    fun run() {
        running = true
        while (running) {
            val buf = ByteArray(20000)
            var packet = DatagramPacket(buf, buf.size)
            socket!!.receive(packet)
            val address = packet.address
            val port = packet.port
            var received = String(packet.data, 0, packet.length)
            received += "sus?"
            logger.info(received)
            val out = received.encodeToByteArray()
            val out_packet = DatagramPacket(out, out.size, address, port)
            if (received == "end") {
                running = false
                continue
            }
            socket!!.send(out_packet)
        }
        socket!!.close()
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        println("Please enter some text here")
        EchoServer()
        run()
    }
}