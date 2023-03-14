import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
    private var id: Long,
    private val name: String,
    private val coordinates: Coordinates,
    private val creationDate: ZonedDateTime,
    private val price: Double,
    private val manufactureCost: Float,
    private val unitOfMeasure: UnitOfMeasure?,
    private val owner: Person
) : Comparable<Product> {
    companion object : Io.IoReadable<Product> {
        var last_id: Long = 1
        private fun getId() = ++last_id

        override fun read(io: Io, full: Boolean) = Product(
            (if (full) io.readVal("id") { it.toLong() } else getId()),
            io.readVal("name") { (if (it == "") throw Exception("can't be empty") else it) },
            io.readNested("coordinates") { Coordinates.read(io, full) },
            (if (full) io.readVal("creationDate") { ZonedDateTime.parse(it) } else ZonedDateTime.now()),
            io.readVal("price") { (if (it.toDouble() > 0) it.toDouble() else throw Exception("must be > 0")) },
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

    /**
     * Set id
     *
     * @param id
     */// i hate jvm ecosystem
    fun setId(id: Long) {
        this.id = id
    }

    /**
     * Id
     *
     */
    fun id() = id

    /**
     * Manufacture cost
     *
     */
    fun manufactureCost() = manufactureCost

    /**
     * Owner
     *
     */
    fun owner() = owner

    /**
     * Name contains
     *
     * @param pattern
     */
    fun nameContains(pattern: String) = name.contains(pattern)

    /**
     * Fields
     *
     */
    fun fields() = arrayOf(
        arrayOf(
            id,
            name
        ),
        coordinates.fields(),
        arrayOf(
            creationDate,
            price,
            manufactureCost,
            unitOfMeasure
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
    private val x: Float,
    private val y: Long
) {
    companion object : Io.IoReadable<Coordinates> {
        override fun read(io: Io, full: Boolean) = Coordinates(
            io.readVal("coordinates.x") { it.toFloat() },
            io.readVal("coordinates.y") { it.toLong() })
    }

    /**
     * Fields
     *
     */
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
class Person(
    private val name: String,
    private val birthday: LocalDateTime?,
    private val nationality: Country
) : Comparable<Person> {
    override operator fun compareTo(other: Person) = name.compareTo(other.name)

    /**
     * Fields
     *
     */
    fun fields() = arrayOf(
        name,
        birthday,
        nationality,
    )

    companion object : Io.IoReadable<Person> {
        override fun read(io: Io, full: Boolean) =
            Person(
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
    SQUARE_METERS,
    LITERS,
    GRAMS }

/**
 * Country from technical task
 */
enum class Country {
    CHINA,
    SOUTH_KOREA,
    JAPAN }