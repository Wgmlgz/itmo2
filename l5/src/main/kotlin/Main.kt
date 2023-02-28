fun main(args: Array<String>) {
    var file = "save.csv"
    if (args.size != 1) {
        println("no save file provided using default: $file")
    } else {
        file = args[0]
        println("using file path: $file")
    }
    val cmd = Cmd(file)
    cmd.start()
}
