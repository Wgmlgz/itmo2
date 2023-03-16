import kotlinx.serialization.Serializable
import java.security.MessageDigest

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
}

@Serializable
class User(val id: Int? = null, val login: String, val passwordHash: ByteArray) {
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
class Request(val type: Routes, val args: MutableList<Arg> = mutableListOf()) {
    constructor(type: Routes, product: Product) : this(type, mutableListOf(ProductArg(product)))
    constructor(type: Routes, str: String) : this(type, mutableListOf(StrArg(str)))
    constructor(type: Routes, person: Person) : this(type, mutableListOf(PersonArg(person)))
    constructor(type: Routes, user: User) : this(type, mutableListOf(UserArg(user)))
}

