import org.jetbrains.exposed.sql.*


fun main(args: Array<String>) {
    val dbHandler = DBHandler(
        Database.connect(
            "jdbc:postgresql://localhost:5432/test", driver = "org.postgresql.Driver",
            user = "sus", password = "sus"
        )
    )

    val server = Server(dbHandler)
    server.run()
}