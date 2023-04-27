fun main() {
    val client = Client()
    val cmdClient = CmdClient(client)
    cmdClient.start()
    client.close()
}
