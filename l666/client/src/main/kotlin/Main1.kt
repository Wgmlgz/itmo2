import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

internal object UDPClient {
    private var socket: DatagramSocket? = null
    private var address: InetAddress? = null

    fun EchoClient() {
        socket = DatagramSocket()
        address = InetAddress.getByName("localhost")
    }

    fun sendEcho(msg: String): String? {
        var buf = msg.toByteArray()
        var packet = DatagramPacket(buf, buf.size, address, 4445)
        socket!!.send(packet)
        buf = ByteArray(20000)
        val res_packet = DatagramPacket(buf, buf.size)
        socket!!.receive(res_packet)
        return String(
            res_packet.data, 0, res_packet.length
        )
    }

    fun close() {
        socket!!.close()
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        println(
            "You need to press CTRL+C"
                    + " in order to quit."
        )
        EchoClient()
        while (true) {
            val line = readLine()!!
            println(sendEcho(line))
        }
    }
}