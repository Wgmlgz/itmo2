/**
 * CmdClient
 */
class CmdClient(private val client: Client) {
    private var io: Io = ConsoleIo()
    private var alive = true
    private val callStack = HashSet<String>()
    private var token: String? = null
    private var refresh: String? = null

    companion object {
        const val id = "(\\d+)"
        const val item = "(\\S+)"
        const val RETRIES = 1
    }

    private fun help() =
        io.printer.println(commands.filter { it.help.isNotEmpty() }.map { it.help }.joinToString("\n"))

    class Command {
        val regex: String
        val help: String
        val processor: (m: MatchResult) -> Packet?
        var after: ((res: Packet) -> Unit)? = null

        constructor(
            regex: String,
            help: String,
            processor: (m: MatchResult) -> Packet?,
        ) {
            this.regex = regex
            this.help = help
            this.processor = processor
        }

        constructor(
            regex: String,
            help: String,
            processor: (m: MatchResult) -> Packet?,
            after: ((res: Packet) -> Unit)?
        ) {
            this.regex = regex
            this.help = help
            this.processor = processor
            this.after = after
        }
    }


    /** Parse command and serialize to json */
    private val commands: Array<Command> =
        arrayOf<Command>(
            Command("help", "help : output help for available commands") {
                help()
                null
            },
            Command("login +$item +$item", "login <login> <password> : login with credentials", {
                val user = User(it.groupValues[1], it.groupValues[2])
                Packet(Routes.Login, user)
            }) {
                if (it.headers["token"] !== null && it.headers["refreshToken"] !== null) {
                  token = (it.headers["token"] as StrArg).str
                  refresh = (it.headers["refreshToken"] as StrArg).str
                }
            },
            Command("refresh", "refreshes access token by refresh token", {
                Packet(Routes.Refresh, refresh!!)
            }) {
                if (it.code == ResponseCode.OK) {
                    token = (it.headers["token"] as StrArg).str
                    refresh = (it.headers["refreshToken"] as StrArg).str
                }
            },
            Command("register $item $item", "register <login> <password> : register with credentials") {
                Packet(Routes.Register, User(it.groupValues[1], it.groupValues[2]))
            },
            Command("help", "help : output help for available commands") {
                help()
                null
            },
            Command(
                "info",
                "info : output information about the collection (type, initialization date, number of items, etc.) to the standard output stream"
            ) { Packet(Routes.Info) },
            Command(
                "show",
                "show : output to the standard output stream all the elements of the collection in a string representation"
            ) { Packet(Routes.Show) },
            Command("add", "add {element} : add a new item to the collection") {
                Packet(
                    Routes.Add,
                    Product.read(io)
                )
            },
            Command(
                "update $id",
                "update id {element} : update the value of a collection item whose id is equal to the specified one"
            ) {
                Packet(Routes.Update, mutableListOf(StrArg(it.groupValues[1]), ProductArg(Product.read(io))))
            },
            Command("remove_by_id $id", "remove_by_id id : delete an item from the collection by its id") {
                Packet(
                    Routes.RemoveById,
                    it.groupValues[1]
                )
            },
            Command("clear", "clear : clear the collection") { Packet(Routes.Clear) },
            Command(
                "execute_script $item",
                "execute_script file_name : read and execute the script from the specified file. The script contains commands in the same form in which they are entered by the user in interactive mode"
            ) {
                val lastIo = io
                val filename = it.groupValues[1]
                if (callStack.contains(filename)) {
                    io.printer.println("file $filename was already called (recursion detected)")
                    return@Command null
                }
                callStack.add(filename)
                io = FileIo(filename)
                start()
                io.printer.println("")
                io.printer.println("script done")
                io = lastIo
                null
            },
            Command("exit", "exit : terminate the program (without saving to a file)") {
                io.printer.println("finishing...")
                alive = false
                null
            },
            Command(
                "remove_first",
                "remove_first : delete the first item from the collection"
            ) { Packet(Routes.RemoveFirst) },
            Command(
                "and_if_max",
                "add_if_max {element} : add a new item to the collection if its value exceeds the value of the largest item in this collection"
            ) { Packet(Routes.AddIfMax, Product.read(io)) },
            Command(
                "remove_greater",
                "remove_greater {element} : remove all items from the collection that exceed the specified"
            ) { Packet(Routes.RemoveGreater, Product.read(io)) },
            Command(
                "min_by_manufacture_cost",
                "min_by_manufacture_cost : output any object from the collection whose value of the manufactureCost field is minimal"
            ) { Packet(Routes.MinByManufactureCost) },
            Command(
                "count_less_than_owner$item",
                "count_less_than_owner owner : print the number of elements whose owner field value is less than the specified one"
            ) { Packet(Routes.CountLessThanOwner, Person.read(io)) },
            Command(
                "filter_contains_name $item",
                "filter_contains_name name : output elements whose name field value contains the specified substring"
            ) { Packet(Routes.FilterContainsName, it.groupValues[1]) },
            Command("$^", "") { return@Command null }, // just newline
            Command(".*", "") {
                io.printer.println("unknown command")
                null
            },
        )

    private fun runCmd(cmd: Command, input: String, depth: Int = 1, print: Boolean = true): Boolean {
//        try {
            val c: MatchResult = Regex(cmd.regex).find(input) ?: return false
            val request = cmd.processor(c)
            if (request !== null) {
                if (token !== null) {
                    request.headers["authorization"] = StrArg(token!!)
                }
                val res = client.send(request)
                cmd.after?.invoke(res)

//                io.printer.println(res.toJson())
//                io.printer.println(res.code.toString())

                if (res.code == ResponseCode.LOGIN_TIMEOUT && depth <= RETRIES) {
                    cmd("refresh", depth + 1, false) // vary bad code lol
                    return runCmd(cmd, input, depth + 99999)
                } else if (print) {
                    io.printer.print((res.args[0] as StrArg).str)
                }
            }
//        } catch (e: Exception) {
//            io.printer.println("command failed with error: ${e.message}")
//        }
        return true
    }

    /**
     * Cmd
     *
     * @param input
     */
    private fun cmd(input: String, depth: Int = 1, print: Boolean = true) {
        for (command in commands)
            if (runCmd(command, input, depth, print))
                break
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
