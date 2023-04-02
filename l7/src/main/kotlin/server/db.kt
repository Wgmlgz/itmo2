import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.javatime.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.*
import java.util.*

object Users : IntIdTable() {
    val login = text("login").uniqueIndex()
    val passwordHash = binary("passwordHash")
    val refreshToken = text("refreshToken").nullable()
}

object Products : IntIdTable() {
    val userId = integer("userId")
    val name = text("name")
    val coordinates_x = float("coordinates_x")
    val coordinates_y = long("coordinates_y")
    val creationDate = datetime("creationDate")
    val price = double("price")
    val manufactureCost = float("manufactureCost")
    val unitOfMeasure = text("unitOfMeasure").nullable()
    val owner_name = text("owner_name")
    val owner_birthday = datetime("owner_birthday").nullable()
    val owner_nationality = text("owner_nationality")
}

class DBHandler(val db: Database) {
    init {
        transaction {
            SchemaUtils.create(Products, Users)
        }
    }

    fun getRefresh(user: User) =
        transaction {
            Users.select { (Users.login eq user.login) and (Users.passwordHash eq user.passwordHash) }
                .map { userFromRow(it) }
        }[0].refreshToken

    fun setRefresh(user: User, refreshToken: String) =
        transaction {
            Users.update({ (Users.login eq user.login) and (Users.passwordHash eq user.passwordHash) })
            {
                it[Users.refreshToken] = refreshToken
            }
        }

    fun register(user: User) =
        transaction {
            Users.insert {
                it[login] = user.login
                it[passwordHash] = user.passwordHash
            }
        }


    fun checkLogin(user: User) =
        transaction {
            Users.select { (Users.login eq user.login) }.count()
                .toInt()
        }

    fun checkLogged(user: User) =
        transaction {
            Users.select { (Users.login eq user.login) and (Users.passwordHash eq user.passwordHash) }
                .map { userFromRow(it) }
        }

    fun deleteById(id: Long, userId: Int) =
        transaction {
            Products.deleteWhere { (Products.id eq id.toInt()) and (Products.userId eq userId) }
        }

    fun removeGreater(product: Product, userId: Int) =
        transaction {
            Products.deleteWhere { (name less product.name) and (Products.userId eq userId) }
        }

    fun updateById(product: Product, id: Long, userId: Int) =
        transaction {
            Products.update({ (Products.id eq id.toInt()) and (Products.userId eq userId) }) {
                it[name] = product.name
                it[coordinates_x] = product.coordinates.x
                it[coordinates_y] = product.coordinates.y
                it[creationDate] = product.creationDate.toLocalDateTime()
                it[price] = product.price
                it[manufactureCost] = product.manufactureCost
                it[unitOfMeasure] = product.unitOfMeasure?.toString()
                it[owner_name] = product.owner.name
                it[owner_birthday] = product.owner.birthday
                it[owner_nationality] = product.owner.nationality.toString()
            }
        }

    fun insert(product: Product, newUserId: Int) {
        transaction {
            Products.insert {
                it[userId] = newUserId
                it[name] = product.name
                it[coordinates_x] = product.coordinates.x
                it[coordinates_y] = product.coordinates.y
                it[creationDate] = product.creationDate.toLocalDateTime()
                it[price] = product.price
                it[manufactureCost] = product.manufactureCost
                it[unitOfMeasure] = product.unitOfMeasure?.toString()
                it[owner_name] = product.owner.name
                it[owner_birthday] = product.owner.birthday
                it[owner_nationality] = product.owner.nationality.toString()
            }
        }
    }

    fun fetch(): PriorityQueue<Product> {
        val q = PriorityQueue<Product>()
        transaction {
            q.addAll(Products.selectAll().map { fromRow(it) })
        }
        return q
    }

    companion object {
        fun userFromRow(row: ResultRow) = User(
            id = row[Users.id].value,
            login = row[Users.login],
            passwordHash = row[Users.passwordHash],
            refreshToken = row[Users.refreshToken]
        )

        fun fromRow(row: ResultRow) = Product(
            id = row[Products.id].value.toLong(),
            name = row[Products.name],
            coordinates = Coordinates(
                x = row[Products.coordinates_x],
                y = row[Products.coordinates_y],
            ),
            creationDate = row[Products.creationDate].atZone(ZoneOffset.UTC),
            price = row[Products.price],
            manufactureCost = row[Products.manufactureCost],
            unitOfMeasure = row[Products.unitOfMeasure]?.let { UnitOfMeasure.valueOf(it) },
            owner = Person(
                name = row[Products.owner_name],
                birthday = row[Products.owner_birthday],
                nationality = Country.valueOf(row[Products.owner_nationality]),
            )
        )
    }
}
