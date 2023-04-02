import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

const val chuckSize = 4;

@Serializable
enum class Routes {
    Info,
    Show,
    Add,
    Update,
    RemoveById,
    Clear,
    ExecuteScript,
    Exit,
    RemoveFirst,
    AddIfMax,
    RemoveGreater,
    MinByManufactureCost,
    CountLessThanOwner,
    FilterContainsName,
    Login,
    Register,
    Refresh,
}

@Serializable
class User(val id: Int? = null, val login: String, val passwordHash: ByteArray, val refreshToken: String? = null) {
    constructor(login: String, password: String) : this(
        null,
        login,
        MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    )
}


@Serializable
sealed class Arg

@Serializable
class StrArg(val str: String) : Arg()

@Serializable
class ProductArg(val product: Product) : Arg()

@Serializable
class PersonArg(val person: Person) : Arg()

@Serializable
class UserArg(val user: User) : Arg()

@Serializable
class Packet(
    var type: Routes? = null,
    val args: MutableList<Arg> = mutableListOf(),
    val headers: MutableMap<String, Arg> = mutableMapOf(),
    var code: ResponseCode = ResponseCode.OK,
) {
    constructor(type: Routes, product: Product) : this(type, mutableListOf(ProductArg(product)))
    constructor(type: Routes, str: String) : this(type, mutableListOf(StrArg(str)))
    constructor(type: Routes, person: Person) : this(type, mutableListOf(PersonArg(person)))
    constructor(type: Routes, user: User) : this(type, mutableListOf(UserArg(user)))

    companion object {
        fun fromJson(json: String) = (Json.decodeFromString(Packet.serializer(), json))
    }

    fun toJson() = Json.encodeToString(this)
}

@Serializable
enum class ResponseCode(val code: Int) {
    OK(200),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    LOGIN_TIMEOUT(440),
    IM_A_TEAPOT(418),
    INTERNAL_ERROR(500)
}

class TestException(val code: ResponseCode, e: Exception) :
    Exception("${code.code} ($code) ${e.message}")