import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
enum class CommandType{
    info,
    show,
    add,
    update,
    remove_by_id,
    save,
    execute_script,
    exit,
    remove_first,
    and_if_max,
    remove_greater,
    min_by_manufacture_cost,
    count_less_than_owner,
    filter_contains_name,
}
@Serializable
class Command(val type: CommandType)


@Serializable
class StrCommand(val type: CommandType, val str: String)

@Serializable
class ProductCommand(val type: CommandType, val product: Product)


@Serializable
class ProductCommand(val type: CommandType, val str: String)

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
        val json = Json.encodeToString(Command(CommandType.info))
        println(json)
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