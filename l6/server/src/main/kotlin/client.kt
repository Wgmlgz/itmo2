import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.*


class Interrupter(private val th1: Thread) : Runnable {
    var wait = 100
    override fun run() {
        while (true) {
            try {
                wait = if (wait < 0) {
                    th1.join()
                    break
                } else {
                    Thread.sleep(wait.toLong())
                    th1.interrupt()
                    -1
                }
            } catch (e: java.lang.Exception) {
                throw e
            }
        }
    }
}

/**
 * Client
 */
class Client {
    private var io: Io = ConsoleIo()
    private var alive = true
    private val callStack = HashSet<String>()


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

    private fun sendWrapper(s: String): String {
        val timeout: Long = 5
        val executor = Executors.newCachedThreadPool()
        val task = Callable { sendEcho(s) }
        val future = executor.submit(task)
        try {
            return future[timeout, TimeUnit.SECONDS]
        } catch (ex: TimeoutException) {
            println("timeout ${timeout}s, run again? [y/N]")
            if (readLine() == "y") return sendWrapper(s)
            throw Exception("timeout")
        }
    }

    fun close() {
        socket.close()
    }

    companion object {
        const val id = "(\\d+)"
        const val item = "(.+)"
    }

    /** Parse command and serialize to json */
    private val commands: Array<Pair<String, (m: MatchResult) -> Command?>> = arrayOf(
        Pair("help") { Command(CommandType.Help) },
        Pair("info") { Command(CommandType.Info) },
        Pair("show") { Command(CommandType.Show) },
        Pair("add") { Command(CommandType.Add, Product.read(io)) },
        Pair("update $id") {
            Command(CommandType.Update, arrayOf(StrArg(it.groupValues[1]), ProductArg(Product.read(io))))
        },
        Pair("remove_by_id $id") { Command(CommandType.RemoveById, it.groupValues[1]) },
        Pair("clear") { Command(CommandType.Clear) },
        Pair("save") { Command(CommandType.Save) },
        Pair("execute_script $item") {
            val lastIo = io
            val filename = it.groupValues[1]
            if (callStack.contains(filename)) {
                io.printer.println("file $filename was already called (recursion detected)")
                return@Pair null
            }
            callStack.add(filename)
            io = FileIo(filename)
            start()
            io.printer.println("")
            io.printer.println("script done")
            io = lastIo

            return@Pair null
        },
        Pair("exit") {
            io.printer.println("finishing...")
            alive = false
            return@Pair null
        },
        Pair("remove_first") { Command(CommandType.RemoveFirst) },
        Pair("and_if_max") { Command(CommandType.AndIfMax, Product.read(io)) },
        Pair("remove_greater") { Command(CommandType.RemoveGreater, Product.read(io)) },
        Pair("min_by_manufacture_cost") { Command(CommandType.MinByManufactureCost) },
        Pair("count_less_than_owner +$item") { Command(CommandType.CountLessThanOwner, Person.read(io)) },
        Pair("filter_contains_name $item") { Command(CommandType.FilterContainsName, it.groupValues[1]) },
        Pair("$^") { return@Pair null }, // just newline
        Pair(".*") {
            io.printer.println("unknown command")
            return@Pair null
        },
    )

    private fun runCmd(r: String, input: String, cb: (m: MatchResult) -> Command?): Boolean {
        try {
            val c: MatchResult = Regex(r).find(input) ?: return false
            val command = cb(c)
            if (command !== null) {
                val json = Json.encodeToString(command)
                val res = sendWrapper(json)
                io.printer.print(res)
            }
        } catch (e: Exception) {
            io.printer.println("command failed with error: ${e.message}")
        }
        return true
    }

    /**
     * Cmd
     *
     * @param input
     */
    private fun cmd(input: String) {
        for ((r, cb) in commands) if (runCmd(r, input, cb)) break
    }

    /**
     * Start
     *
     */
    fun start() {
        if (alive) io.printer.print("-> ")
        while (alive && io.scanner.hasNextLine()) {
            cmd(io.scanner.nextLine())
            if (alive) io.printer.print("-> ")
        }
    }
}


fun main() {
    val client = Client()
    client.start()
    client.close()
}
