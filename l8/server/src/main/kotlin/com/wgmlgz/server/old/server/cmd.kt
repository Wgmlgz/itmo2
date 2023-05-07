import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/*
 * Cmd handles main logic
 * @constructor Create empty Cmd
 */
class CmdServer(private val dbHandler: DBHandler) {
    private var io: Io = CaptureIo()
    private var q = PriorityQueue<Product>()
    private val initTime = ZonedDateTime.now()

    private val auth = Auth(dbHandler)

    private fun sync() {
        q = dbHandler.fetch()
    }

    fun toJson() = Json.encodeToString(ArrayList(q))

    init {
        sync()
        (io.printer as CaptureOutput).capture()
    }

    private fun findById(id: Long) = q.find { it.id() == id } ?: throw Exception("cannot find product by id: $id")

    private fun authorize(req: Packet): Int {
        val authorization = try {
            (req.headers["authorization"] as StrArg).str
        } catch (e: Exception) {
            throw TestException(ResponseCode.UNAUTHORIZED, Exception("authorization header was not found"))
        }
        return auth.checkAuth(authorization).id!!
    }

    /** Parse command and serialize to json */
    private val commands: Map<Routes, (m: Packet) -> Packet?> = mapOf<Routes, (m: Packet) -> Packet?>(
        /** auth routes */
        Routes.Register to {
            val user = (it.args[0] as UserArg).user
            println(user)
            if (dbHandler.checkLogin(user) != 0) {
                println("huh?")
                throw Exception("user with this credentials already exists")
            }
            dbHandler.register(user)
            println("huh?")
            io.printer.println("registered user ${user.login}")
            null
        },
        /** const commands */
        Routes.Info to {
            io.printer.println("type: java.util.PriorityQueue")
            io.printer.println("init time: $initTime")
            io.printer.println("len: ${q.size}")
            null
        },
        Routes.Show to {
            io.printer.println(toJson());
            null
        },
        Routes.Clear to {
            q.clear()
            null
        },
        Routes.MinByManufactureCost to {
            io.printer.println(q.stream().reduce { a, b -> if (a.manufactureCost() < b.manufactureCost()) a else b }
                .toString())
            null
        },
        Routes.CountLessThanOwner to { command ->
            val owner = (command.args[0] as PersonArg).person
            io.printer.println("count of elements with lower owner: ${q.count { it.owner() < owner }}")
            null
        },
        Routes.FilterContainsName to { matchResult ->
            val pattern = (matchResult.args[0] as StrArg).str
            io.printer.println("searching for: `$pattern`")
            q.stream().filter { it.nameContains(pattern) }.forEach { io.printer.println(it.toString()) }
            io.printer.println("done")
            null
        },
        /** mut commands */
        Routes.Add to {
            dbHandler.insert((it.args[0] as ProductArg).product, authorize(it))
            sync()
            null
        },
        Routes.Update to {
            val old = findById((it.args[0] as StrArg).str.toLong())
            val product = (it.args[1] as ProductArg).product
            val n = dbHandler.updateById(product, old.id, authorize(it))
            if (n == 0)
                throw TestException(ResponseCode.FORBIDDEN, Exception("you cannot modify this element"))
            io.printer.println("updated $n products")
            sync()
            null
        },
        Routes.RemoveById to {
            val old = findById((it.args[0] as StrArg).str.toLong())
            val n = dbHandler.deleteById(old.id, authorize(it))
            if (n == 0)
                throw TestException(ResponseCode.FORBIDDEN, Exception("you cannot modify this element"))

            io.printer.println("removed $n products")
            sync()
            null
        },

        Routes.RemoveFirst to {
            val old = q.poll()
            val n = dbHandler.deleteById(old.id, authorize(it))
            if (n == 0)
                throw TestException(ResponseCode.FORBIDDEN, Exception("you cannot modify this element"))

            io.printer.println("removed $n products")
            sync()
            null
        },
        Routes.AddIfMax to {
            val product = (it.args[0] as ProductArg).product
            if (product > q.max()) {
                dbHandler.insert(product, authorize(it))
                io.printer.println("added")
                sync()
            } else {
                io.printer.println("not added, isn't max")
            }
            null
        },
        Routes.RemoveGreater to { command ->
            val product = (command.args[0] as ProductArg).product
            val n = dbHandler.removeGreater(product, authorize(command))
            if (n != 0)
                io.printer.println("all elements are less then given ")
            io.printer.println("removed $n products")
            sync()
            null
        },
    )

    private val needsAuth: Map<Routes, Boolean> = mapOf(
        /** auth commands */
        Routes.Register to false,
        Routes.Login to false,
        Routes.Refresh to false,
        /** const commands */
        Routes.Info to false,
        Routes.Show to false,
        Routes.ExecuteScript to false,
        Routes.Clear to false,
        Routes.Exit to false,
        Routes.MinByManufactureCost to false,
        Routes.CountLessThanOwner to false,
        Routes.FilterContainsName to false,
        /** mut commands */
        Routes.Add to false,
        Routes.Update to false,
        Routes.RemoveById to false,
        Routes.RemoveFirst to false,
        Routes.AddIfMax to false,
        Routes.RemoveGreater to false
    )

    fun runCmd(json: JsonElement): Packet {
        var res = Packet()
        res.code = ResponseCode.OK
        try {
            val packet = Json.decodeFromJsonElement(Packet.serializer(), json)
            if (!needsAuth.contains(packet.type) || !commands.contains(packet.type))
                throw TestException(ResponseCode.NOT_FOUND, Exception("Command was not found"))

            if (needsAuth[packet.type]!!)
                authorize(packet)

            val cmdOutput = commands[packet.type]!!.invoke(packet)
            if (cmdOutput != null) res = cmdOutput
            res.type = packet.type
        } catch (e: TestException) {
            res.code = e.code
            io.printer.println(e.message!!)
        } catch (e: Exception) {
            res.code = ResponseCode.INTERNAL_ERROR
            io.printer.println(e.toString())
        }
        res.args.add(StrArg((io.printer as CaptureOutput).capture()))
        return res
    }
}
