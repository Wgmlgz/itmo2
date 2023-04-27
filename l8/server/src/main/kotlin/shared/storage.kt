import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object KZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val string = decoder.decodeString()
        return ZonedDateTime.parse(string)
    }
}

object KLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val string = decoder.decodeString()
        return LocalDateTime.parse(string)
    }
}


/**
 * Product from technical task
 *
 * @property id
 * @property name
 * @property coordinates
 * @property creationDate
 * @property price
 * @property manufactureCost
 * @property unitOfMeasure
 * @property owner
 * @constructor Create empty Product
 */
@Serializable
class Product(
    var id: Long,
    val name: String,
    val coordinates: Coordinates,
    @Serializable(KZonedDateTimeSerializer::class) val creationDate: ZonedDateTime,
    val price: Double,
    val manufactureCost: Float,
    val unitOfMeasure: UnitOfMeasure?,
    val owner: Person
) : Comparable<Product> {
    companion object : Io.IoReadable<Product> {
        private var last_id: Long = 1
        private fun getId() = ++last_id

        override fun read(io: Io, full: Boolean) = Product(
            (if (full) io.readVal("id") { it.toLong() } else getId()),
            io.readVal("name") {
                (if (it == "") throw Exception("can't be empty") else it)
            },
            io.readNested("coordinates") {
                Coordinates.read(io, full)
            },
            (if (full) io.readVal("creationDate") {
                ZonedDateTime.parse(it)
            } else ZonedDateTime.now()),
            io.readVal("price") {
                (if (it.toDouble() > 0) it.toDouble() else throw Exception("must be > 0"))
            },
            io.readVal("manufactureCost") {
                (if (it.toFloat() > 0) it.toFloat() else throw Exception("must be > 0"))
            },
            io.readVal("unitOfMeasure (${enumValues<UnitOfMeasure>().joinToString { it.name }})") {
                (if (it == "") null else UnitOfMeasure.valueOf(it.uppercase()))
            },
            io.readNested("owner") { Person.read(io, full) },
        )

        fun header() = arrayOf(
            "id",
            "name",
            "coordinates.x",
            "coordinates.y",
            "creationDate",
            "price",
            "manufactureCost",
            "unitOfMeasure",
            "owner.name",
            "owner.birthday",
            "owner.nationality"
        )
    }

    fun id() = id
    fun manufactureCost() = manufactureCost
    fun owner() = owner
    fun nameContains(pattern: String) = name.contains(pattern)

    private fun fields() = arrayOf(
        arrayOf(
            id, name
        ),
        coordinates.fields(),
        arrayOf(
            creationDate, price, manufactureCost, unitOfMeasure
        ),
        owner.fields(),
    ).flatten()

    override fun toString() = header().zip(fields()).joinToString(", ") { (key, value) -> "$key: $value" }

    override operator fun compareTo(other: Product) = name.compareTo(other.name)


}

/**
 * Coordinates from technical task
 *
 * @property x
 * @property y
 * @constructor Create empty Coordinates
 */
@Serializable
class Coordinates(
    val x: Float, val y: Long
) {
    companion object : Io.IoReadable<Coordinates> {
        override fun read(io: Io, full: Boolean) =
            Coordinates(io.readVal("coordinates.x") { it.toFloat() }, io.readVal("coordinates.y") { it.toLong() })
    }

    fun fields() = arrayOf(
        x,
        y,
    )
}

/**
 * Person from technical task
 *
 * @property name
 * @property birthday
 * @property nationality
 * @constructor Create empty Person
 */
@Serializable
class Person(
    val name: String,
    @Serializable(KLocalDateTimeSerializer::class) internal val birthday: LocalDateTime?,
    val nationality: Country
) : Comparable<Person> {
    override operator fun compareTo(other: Person) = name.compareTo(other.name)

    fun fields() = arrayOf(
        name,
        birthday,
        nationality,
    )

    companion object : Io.IoReadable<Person> {
        override fun read(io: Io, full: Boolean) = Person(
            io.readVal("owner.name") { (if (it == "") throw Exception("can't be empty") else it) },
            io.readVal("owner.birthday (yyyy-MM-dd HH:mm)") {
                (if (it == "") null else LocalDateTime.parse(
                    it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                ))
            },
            io.readVal(
                "owner.nationality (${enumValues<Country>().joinToString { it.name }})"
            ) { Country.valueOf(it.uppercase()) },
        )
    }
}

/**
 * Unit of measure from technical task
 */
enum class UnitOfMeasure {
    SQUARE_METERS, LITERS, GRAMS
}

/**
 * Country from technical task
 */
enum class Country {
    CHINA, SOUTH_KOREA, JAPAN
}