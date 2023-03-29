import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * CmdClient
 */
class CmdClient(val client: Client) {
    private var io: Io = ConsoleIo()
    private var alive = true
    private val callStack = HashSet<String>()
    private var user = User("", "")

    companion object {
        const val id = "(\\d+)"
        const val item = "(\\S+)"
    }

    private fun help() =
        io.printer.println(commands.filter { it.second.isNotEmpty() }.map { it.second }.joinToString("\n"))

    /** Parse command and serialize to json */
    private val commands: Array<Triple<String, String, (m: MatchResult) -> Request?>> = arrayOf(
        Triple("help", "help : output help for available commands") {
            help()
            null
        },
        Triple("login +$item +$item", "login <login> <password> : login with credentials") {
            user = User(it.groupValues[1], it.groupValues[2])
            Request(Routes.Login, user)
        },
        Triple("register $item $item", "register <login> <password> : register with credentials") {
            Request(Routes.Register, User(it.groupValues[1], it.groupValues[2]))
        },
        Triple("help", "help : output help for available commands") {
            help()
            null
        },
        Triple(
            "info",
            "info : output information about the collection (type, initialization date, number of items, etc.) to the standard output stream"
        ) { Request(Routes.Info) },
        Triple(
            "show",
            "show : output to the standard output stream all the elements of the collection in a string representation"
        ) { Request(Routes.Show) },
        Triple("add", "add {element} : add a new item to the collection") {
            Request(
                Routes.Add,
                Product.read(io)
            )
        },
        Triple(
            "update $id",
            "update id {element} : update the value of a collection item whose id is equal to the specified one"
        ) {
            Request(Routes.Update, mutableListOf(StrArg(it.groupValues[1]), ProductArg(Product.read(io))))
        },
        Triple("remove_by_id $id", "remove_by_id id : delete an item from the collection by its id") {
            Request(
                Routes.RemoveById,
                it.groupValues[1]
            )
        },
        Triple("clear", "clear : clear the collection") { Request(Routes.Clear) },
        Triple(
            "execute_script $item",
            "execute_script file_name : read and execute the script from the specified file. The script contains commands in the same form in which they are entered by the user in interactive mode"
        ) {
            val lastIo = io
            val filename = it.groupValues[1]
            if (callStack.contains(filename)) {
                io.printer.println("file $filename was already called (recursion detected)")
                return@Triple null
            }
            callStack.add(filename)
            io = FileIo(filename)
            start()
            io.printer.println("")
            io.printer.println("script done")
            io = lastIo
            null
        },
        Triple("exit", "exit : terminate the program (without saving to a file)") {
            io.printer.println("finishing...")
            alive = false
            null
        },
        Triple(
            "remove_first",
            "remove_first : delete the first item from the collection"
        ) { Request(Routes.RemoveFirst) },
        Triple(
            "and_if_max",
            "add_if_max {element} : add a new item to the collection if its value exceeds the value of the largest item in this collection"
        ) { Request(Routes.AddIfMax, Product.read(io)) },
        Triple(
            "remove_greater",
            "remove_greater {element} : remove all items from the collection that exceed the specified"
        ) { Request(Routes.RemoveGreater, Product.read(io)) },
        Triple(
            "min_by_manufacture_cost",
            "min_by_manufacture_cost : output any object from the collection whose value of the manufactureCost field is minimal"
        ) { Request(Routes.MinByManufactureCost) },
        Triple(
            "count_less_than_owner$item",
            "count_less_than_owner owner : print the number of elements whose owner field value is less than the specified one"
        ) { Request(Routes.CountLessThanOwner, Person.read(io)) },
        Triple(
            "filter_contains_name $item",
            "filter_contains_name name : output elements whose name field value contains the specified substring"
        ) { Request(Routes.FilterContainsName, it.groupValues[1]) },
        Triple("$^", "") { return@Triple null }, // just newline
        Triple(".*", "") {
            io.printer.println("unknown command")
            null
        },
    )

    private fun runCmd(r: String, input: String, cb: (m: MatchResult) -> Request?): Boolean {
        try {
            val c: MatchResult = Regex(r).find(input) ?: return false
            val request = cb(c)
            if (request !== null) {
                request.args.add(UserArg(user))
                val json = Json.encodeToString(request)
                val res = client.send(json)
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
