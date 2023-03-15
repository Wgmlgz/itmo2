import kotlinx.serialization.json.Json
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.time.ZonedDateTime
import java.util.*

/**
 * Cmd handles main logic
 *
 * @property save path to save file
 * @constructor Create empty Cmd
 */
class CmdServer(private val save: String, private val autoSave: String) : Logging {
    private var io: Io = CaptureIo()
    private val q = PriorityQueue<Product>()
    private val initTime = ZonedDateTime.now()

    private var socket: DatagramSocket = DatagramSocket(4445)
    private var running = false

    fun run() {
        running = true
        while (running) {
            val buf = ByteArray(20000)
            val packet = DatagramPacket(buf, buf.size)
            socket.receive(packet)
            val address = packet.address
            val port = packet.port
            var received = String(packet.data, 0, packet.length)
            received = runCmd(received)
            logger.error(received)
            val out = received.encodeToByteArray()
            val res = DatagramPacket(out, out.size, address, port)
            socket.send(res)
        }
        socket.close()
    }

    init {
        var filename = save
        try {
            if (File(autoSave).exists()) {
                io.printer.print("auto save was found ($autoSave), load it? [y/N]: ")
                val ans = io.scanner.nextLine()
                if (ans == "y") filename = autoSave
            }
        } catch (_: Exception) {
        }

        try {
            io.printer.println("loading save file: $filename")
            File(filename).readLines().forEach {
                try {
                    val io = GenericIo(CsvInput(it), NullOutput())
                    val product = Product.read(io, true)
                    Product.last_id = maxOf(Product.last_id, product.id())
                    q.add(product)
                } catch (e: Exception) {
                    io.printer.println("error: invalid product save: $e, for line $it")
                }
            }
        } catch (e: FileNotFoundException) {
            io.printer.println("file wasn't found, skipping initialization")
        }
        Product.last_id = q.map { it.id() }.max();
        println(Product.last_id)
        (io.printer as CaptureOutput).capture()
    }

    private fun findById(id: Long) = q.find { it.id() == id } ?: throw Error("cannot find product by id: $id")

    private fun help() =
        io.printer.println(commands.filter { it.value.first.isNotEmpty() }.map { it.value.first }.joinToString("\n")
        )

    private fun saveToFile(save: String) {
        var s = ""
        q.stream().forEach { item ->
            s += item.fields().map { it ?: "" }.joinToString(",") + "\n"
        }
        val f = FileWriter(save)
        f.write(s)
        f.flush()
        f.close()
    }

    /** Parse command and serialize to json */
    private val commands: Map<CommandType, Pair<String, (m: Command) -> Unit>> =
        mapOf(CommandType.Help to Pair("help : output help for available commands") { help() },
            CommandType.Info to Pair(
                "info : output information about the collection (type, initialization date, number of items, etc.) to the standard output stream"
            ) {
                io.printer.println("type: java.util.PriorityQueue")
                io.printer.println("init time: $initTime")
                io.printer.println("len: ${q.size}")
            },
            CommandType.Show to Pair(
                "show : output to the standard output stream all the elements of the collection in a string representation"
            ) {
                q.stream().forEach {
                    io.printer.println(it.toString());
                }
            },
            CommandType.Add to Pair(
                "add {element} : add a new item to the collection"
            ) {
                val product = (it.args[0] as ProductArg).product
                product.setId(Product.getId())
                q.add((it.args[0] as ProductArg).product)
            },
            CommandType.Update to Pair(
                "update id {element} : update the value of a collection item whose id is equal to the specified one"
            ) {
                val old = findById((it.args[0] as StrArg).str.toLong())
                q.remove(old)
                val product = (it.args[1] as ProductArg).product
                product.setId(old.id())
                q.add(product)
            },
            CommandType.RemoveById to Pair(
                "remove_by_id id : delete an item from the collection by its id"
            ) { q.remove(findById((it.args[0] as StrArg).str.toLong())) },
            CommandType.Clear to Pair("clear : clear the collection") { q.clear() },
            CommandType.Save to Pair(
                "save : save the collection to a file"
            ) {
                saveToFile(save)
                io.printer.println("saved")
            },
            CommandType.ExecuteScript to Pair(
                "execute_script file_name : read and execute the script from the specified file. The script contains commands in the same form in which they are entered by the user in interactive mode"
            ) { io.printer.println("execute_script on server??? u mad???") },
            CommandType.Exit to Pair("exit : terminate the program (without saving to a file)") { io.printer.println("exit on server??? u mad???") },
            CommandType.RemoveFirst to Pair("remove_first : delete the first item from the collection") {
                q.poll()
                io.printer.println("removed")
            },
            CommandType.AndIfMax to Pair(
                "add_if_max {element} : add a new item to the collection if its value exceeds the value of the largest item in this collection"
            ) {
                val product = (it.args[0] as ProductArg).product
                if (product > q.max()) {
                    q.add(product)
                    io.printer.println("added")
                } else {
                    io.printer.println("not added, isn't max")
                }
            },
            CommandType.RemoveGreater to Pair(
                "remove_greater {element} : remove all items from the collection that exceed the specified"
            ) { command ->
                val product = (command.args[0] as ProductArg).product
                if (!q.removeIf { it > product }) io.printer.println("all elements are less then given ")
            },
            CommandType.MinByManufactureCost to Pair(
                "min_by_manufacture_cost : output any object from the collection whose value of the manufactureCost field is minimal"
            ) {
                io.printer.println(q.reduce { a, b -> if (a.manufactureCost() < b.manufactureCost()) a else b }
                    .toString())
            },
            CommandType.CountLessThanOwner to Pair(
                "count_less_than_owner owner : print the number of elements whose owner field value is less than the specified one"
            ) { command ->
                val owner = (command.args[0] as PersonArg).person
                io.printer.println("count of elements with lower owner: ${q.count { it.owner() < owner }}")
            },
            CommandType.FilterContainsName to Pair(
                "filter_contains_name name : output elements whose name field value contains the specified substring"
            ) { matchResult ->
                val pattern = (matchResult.args[0] as StrArg).str
                io.printer.println("searching for: `$pattern`")
                q.stream().filter { it.nameContains(pattern) }.forEach { io.printer.println(it.toString()) }
                io.printer.println("done")
            })

    private fun runCmd(json: String): String {
        try {
            val command = Json.decodeFromString(Command.serializer(), json)
            commands[command.type]?.second?.invoke(command)
            saveToFile(autoSave)
        } catch (e: Exception) {
            io.printer.println("command failed with error: ${e.message}")
        }
        return (io.printer as CaptureOutput).capture()
    }
}

fun main(args: Array<String>) {
    var file = "save.csv"
    if (args.size != 1) {
        println("no save file provided using default: $file")
    } else {
        file = args[0]
        println("using file path: $file")
    }
    println("Please enter some text here")
    val server = CmdServer(file, "auto-save.csv")
    server.run()
}