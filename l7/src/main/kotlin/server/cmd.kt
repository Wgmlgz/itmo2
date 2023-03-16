import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.time.ZonedDateTime
import java.util.*

/**
 * Cmd handles main logic
 *
 * @constructor Create empty Cmd
 */
class CmdServer(private val dbHandler: DBHandler) {
    private var io: Io = CaptureIo()
    private var q = PriorityQueue<Product>()
    private val initTime = ZonedDateTime.now()

    /**
     * syncs collection from db and between clients on change
     */
    private fun sync() {
        q = dbHandler.fetch()
    }

    init {
        sync()
        (io.printer as CaptureOutput).capture()
    }

    private fun findById(id: Long) = q.find { it.id() == id } ?: throw Error("cannot find product by id: $id")

    /** Parse command and serialize to json */
    private val commands: Map<Routes, (m: Request) -> Unit> =
        mapOf(
            /** auth routes */
            Routes.Register to {
                val user = (it.args.last() as UserArg).user
                if (dbHandler.checkLogin(user) != 0) throw Exception("user with this credentials already exists")
                dbHandler.register(user)
                io.printer.println("registered user ${user.login}")

            },
            Routes.Login to {
                val user = (it.args.removeLast() as UserArg).user;
                if (dbHandler.checkLogged(user).isEmpty()) throw Exception("invalid credentials")
                io.printer.println("logged in as ${user.login}")
            },
            /** const commands */
            Routes.Info to {
                io.printer.println("type: java.util.PriorityQueue")
                io.printer.println("init time: $initTime")
                io.printer.println("len: ${q.size}")
            },
            Routes.Show to {
                q.stream().forEach {
                    io.printer.println(it.toString());
                }
            },
            Routes.ExecuteScript to { io.printer.println("execute_script on server??? u mad???") },
            Routes.Clear to { q.clear() },
            Routes.Exit to { io.printer.println("exit on server??? u mad???") },
            Routes.MinByManufactureCost to {
                io.printer.println(q.stream().reduce { a, b -> if (a.manufactureCost() < b.manufactureCost()) a else b }
                    .toString())
            },
            Routes.CountLessThanOwner to { command ->
                val owner = (command.args[0] as PersonArg).person
                io.printer.println("count of elements with lower owner: ${q.count { it.owner() < owner }}")
            },
            Routes.FilterContainsName to { matchResult ->
                val pattern = (matchResult.args[0] as StrArg).str
                io.printer.println("searching for: `$pattern`")
                q.stream().filter { it.nameContains(pattern) }.forEach { io.printer.println(it.toString()) }
                io.printer.println("done")
            },
            /** mut commands */
            Routes.Add to {
                dbHandler.insert((it.args[0] as ProductArg).product, it.args.last())
                sync()
            },
            Routes.Update to {
                val old = findById((it.args[0] as StrArg).str.toLong())
                val product = (it.args[1] as ProductArg).product
                dbHandler.updateById(product, old.id, it.args.last())
                io.printer.println("updated")
                sync()
            },
            Routes.RemoveById to {
                val old = findById((it.args[0] as StrArg).str.toLong())
                dbHandler.deleteById(old.id, it.args.last())
                io.printer.println("removed")
                sync()
            },

            Routes.RemoveFirst to {
                val old = q.poll()
                dbHandler.deleteById(old.id, it.args.last())
                io.printer.println("removed")
                sync()
            },
            Routes.AddIfMax to {
                val product = (it.args[0] as ProductArg).product
                if (product > q.max()) {
                    dbHandler.insert(product, it.args.last())
                    io.printer.println("added")
                    sync()
                } else {
                    io.printer.println("not added, isn't max")
                }
            },
            Routes.RemoveGreater to { command ->
                val product = (command.args[0] as ProductArg).product
                if (dbHandler.removeGreater(
                        product,
                        command.args.last()
                    ) != 0
                ) io.printer.println("all elements are less then given ")
                sync()
            },
        )

    private val needsAuth: Map<Routes, Boolean> =
        mapOf(
            /** auth commands */
            Routes.Register to false,
            Routes.Login to false,
            /** const commands */
            Routes.Info to true,
            Routes.Show to true,
            Routes.ExecuteScript to true,
            Routes.Clear to true,
            Routes.Exit to true,
            Routes.MinByManufactureCost to true,
            Routes.CountLessThanOwner to true,
            Routes.FilterContainsName to true,
            /** mut commands */
            Routes.Add to true,
            Routes.Update to true,
            Routes.RemoveById to true,
            Routes.RemoveFirst to true,
            Routes.AddIfMax to true,
            Routes.RemoveGreater to true
        )

    fun runCmd(json: JsonElement): String {
        try {
            val request = Json.decodeFromJsonElement(Request.serializer(), json)

            if (needsAuth[request.type]!!) {
                val user = (request.args.last() as UserArg).user;
                if (dbHandler.checkLogged(user).isEmpty()) throw Exception("you aren't logged in")
            }

            commands[request.type]?.invoke(request)
        } catch (e: Exception) {
            io.printer.println("command failed with error: ${e.message}")
        }
        return (io.printer as CaptureOutput).capture()
    }
}
