import kotlinx.serialization.Serializable

@Serializable
enum class CommandType {
    Help,
    Info,
    Show,
    Add,
    Update,
    RemoveById,
    Clear,
    Save,
    ExecuteScript,
    Exit,
    RemoveFirst,
    AndIfMax,
    RemoveGreater,
    MinByManufactureCost,
    CountLessThanOwner,
    FilterContainsName,
}


@Serializable
sealed class Arg

@Serializable
class StrArg(val str: String) : Arg()

@Serializable
class ProductArg(val product: Product) : Arg()
class PersonArg(val person: Person) : Arg()

@Serializable
class Command(val type: CommandType, val args: Array<Arg> = arrayOf()) {
    constructor(type: CommandType, product: Product) : this(type, arrayOf(ProductArg(product)))
    constructor(type: CommandType, str: String) : this(type, arrayOf(StrArg(str)))
    constructor(type: CommandType, person: Person) : this(type, arrayOf(PersonArg(person)))
}

