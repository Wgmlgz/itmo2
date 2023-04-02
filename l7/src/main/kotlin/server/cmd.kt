import jdk.incubator.vector.VectorOperators.Test
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Pack
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/*
 * Cmd handles main logic
 *
 * @constructor Create empty Cmd
 */
class CmdServer(private val dbHandler: DBHandler) {
    private var io: Io = CaptureIo()
    private var q = PriorityQueue<Product>()
    private val lock: Lock = ReentrantLock()
    private val initTime = ZonedDateTime.now()

    private val auth = Auth(dbHandler)

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
        Routes.Login to {
            val user = (it.args[0] as UserArg).user;
            val (token, refreshToken) = auth.login(user)
//                    println("huh?")
//                    throw Exception("invalid credentials")
//                }
            io.printer.println("logged in as ${user.login}")
            Packet(
                headers = mutableMapOf(
                    "token" to StrArg(token), "refreshToken" to StrArg(refreshToken)
                )
            )
        },
        Routes.Refresh to {
            val oldRefreshToken = (it.args[0] as StrArg).str;
            val (token, refreshToken) = auth.refresh(oldRefreshToken)
            io.printer.println("refreshed")
            Packet(
                headers = mutableMapOf(
                    "token" to StrArg(token), "refreshToken" to StrArg(refreshToken)
                )
            )
        },
        /** const commands */
        Routes.Info to {
            io.printer.println("type: java.util.PriorityQueue")
            io.printer.println("init time: $initTime")
            io.printer.println("len: ${q.size}")
            null
        },
        Routes.Show to {
            q.stream().forEach {
                io.printer.println(it.toString());
            }
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
            dbHandler.updateById(product, old.id, authorize(it))
            io.printer.println("updated")
            sync()
            null
        },
        Routes.RemoveById to {
            val old = findById((it.args[0] as StrArg).str.toLong())
            dbHandler.deleteById(old.id, authorize(it))
            sync()
            null
        },

        Routes.RemoveFirst to {
            val old = q.poll()
            dbHandler.deleteById(old.id, authorize(it))
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
            if (dbHandler.removeGreater(product, authorize(command)) != 0)
                io.printer.println("all elements are less then given ")
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

    fun runCmd(json: JsonElement): Packet {
        var res = Packet()
        try {
            val packet = Json.decodeFromJsonElement(Packet.serializer(), json)
            lock.lock();
            if (!needsAuth.contains(packet.type) || !commands.contains(packet.type))
                throw TestException(ResponseCode.NOT_FOUND, Exception("Command was not found"))

            if (needsAuth[packet.type]!!)
                authorize(packet)

            println("11")
            val cmdOutput = commands[packet.type]!!.invoke(packet)
            if (cmdOutput != null) res = cmdOutput
            res.type = packet.type
            println("22")
        } catch (e: TestException) {
            res.code = e.code
            e.message?.let { io.printer.println(it) }
        }
//        catch (e: Exception) {
//            println(e)
//            io.printer.println(e.message!!)
//        }
        finally {
            lock.unlock();
        }
        res.args.add(StrArg((io.printer as CaptureOutput).capture()))
        return res
    }
}
