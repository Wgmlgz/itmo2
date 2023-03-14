import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.PrintWriter
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.HashSet

/**
 * Cmd handles main logic
 *
 * @property save path to save file
 * @constructor Create empty Cmd
 */
class Cmd(private val save: String) {
    private var io: Io = ConsoleIo()
    private var alive = true
    private val q = PriorityQueue<Product>()
    private val initTime = ZonedDateTime.now()
    private val callStack = HashSet<String>()

    init {
        try {
            File(save).readLines().forEach {
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
    }

    companion object {
        const val id = "(\\d+)"
        const val item = "(.+)"
    }

    private fun findById(id: Long) = q.find { it.id() == id } ?: throw Error("cannot find product by id: $id")

    private fun help() =
        io.printer.println(commands.filter { (_, b, _) -> b.isNotEmpty() }.joinToString("\n") { (_, b, _) -> b })

    private val commands: Array<Triple<String, String, (m: MatchResult) -> Unit>> = arrayOf(
        Triple("help", "help : output help for available commands") { help() },
        Triple(
            "info",
            "info : output information about the collection (type, initialization date, number of items, etc.) to the standard output stream"
        ) {
            println("type: java.util.PriorityQueue")
            println("init time: $initTime")
            println("len: ${q.size}")
        },
        Triple(
            "show",
            "show : output to the standard output stream all the elements of the collection in a string representation"
        ) { q.forEach { io.printer.println(it.toString()) } },
        Triple(
            "add", "add {element} : add a new item to the collection"
        ) { q.add(Product.read(io)) },
        Triple(
            "update $id",
            "update id {element} : update the value of a collection item whose id is equal to the specified one"
        ) {
            val old = findById(it.groupValues[1].toLong())
            q.remove(old)
            val product = Product.read(io)
            product.setId(old.id())
            q.add(product)
        },
        Triple(
            "remove_by_id $id", "remove_by_id id : delete an item from the collection by its id"
        ) { q.remove(findById(it.groupValues[1].toLong())) },
        Triple("clear", "clear : clear the collection") { q.clear() },
        Triple(
            "save", "save : save the collection to a file"
        ) {
            var s = ""
            q.forEach { item ->
                s += item.fields().map { it ?: "" }.joinToString(",") + "\n"
            }
            val f = FileWriter(save)
            f.write(s)
            f.flush()
            f.close()
            io.printer.println("saved")
        },
        Triple(
            "execute_script $item",
            "execute_script file_name : read and execute the script from the specified file. The script contains commands in the same form in which they are entered by the user in interactive mode"
        ) {
            val lastIo = io
            val filename = it.groupValues[1]
            if (callStack.contains(filename)) {
                throw Error("file $filename was already called (recursion detected)")
            }
            callStack.add(filename)
            io = FileIo(filename)
            start()
            io.printer.println("")
            io.printer.println("script done")
            io = lastIo
        },
        Triple(
            "exit", "exit : terminate the program (without saving to a file)"
        ) {
            io.printer.println("finishing...")
            alive = false
        },
        Triple(
            "remove_first", "remove_first : delete the first item from the collection"
        ) {
            q.poll()
            io.printer.println("removed")
        },
        Triple(
            "and_if_max",
            "add_if_max {element} : add a new item to the collection if its value exceeds the value of the largest item in this collection"
        ) {
            val product = Product.read(io)
            if (product > q.max()) {
                q.add(product)
                io.printer.println("added")
            } else {
                io.printer.println("not added, isn't max")
            }
        },
        Triple(
            "remove_greater",
            "remove_greater {element} : remove all items from the collection that exceed the specified"
        ) {
            val product = Product.read(io)
            if (!q.removeIf { it > product }) println("all elements are less then given ")
        },
        Triple(
            "min_by_manufacture_cost",
            "min_by_manufacture_cost : output any object from the collection whose value of the manufactureCost field is minimal"
        ) {
            io.printer.println(q.reduce { a, b -> if (a.manufactureCost() < b.manufactureCost()) a else b }.toString())
        },
        Triple(
            "count_less_than_owner $item",
            "count_less_than_owner owner : print the number of elements whose owner field value is less than the specified one"
        ) {
            val owner = Person.read(io)
            io.printer.println("count of elements with lower owner: ${q.count { it.owner() < owner }}")
        },
        Triple(
            "filter_contains_name $item",
            "filter_contains_name name : output elements whose name field value contains the specified substring"
        ) { matchResult ->
            val pattern = matchResult.groupValues[1]
            println("searching for: `$pattern`")
            q.filter { it.nameContains(pattern) }.forEach { io.printer.println(it.toString()) }
            io.printer.println("done")
        },
        Triple("$^", "") { }, // just newline
        Triple(".*", "") { io.printer.println("unknown command") },
    )

    private fun runCmd(r: String, input: String, cb: (m: MatchResult) -> Unit): Boolean {
        try {
            val c: MatchResult = Regex(r).find(input) ?: return false
            cb(c)
        } catch (e: Exception) {
            println("command failed with error: ${e.message}")
        }
        return true
    }

    /**
     * Cmd
     *
     * @param input
     */
    private fun cmd(input: String) {
        for ((r, _, cb) in commands) if (runCmd(r, input, cb)) break
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
