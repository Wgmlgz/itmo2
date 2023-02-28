import java.io.File
import java.io.PrintWriter
import java.util.*

interface Input {
    fun hasNextLine(): Boolean
    fun nextLine(): String
}

/**
 * Csv input implements input for csv line
 *
 * @param csv_str csv line
 */
class CsvInput(csv_str: String) : Input {
    private val partitioned = csv_str.split(",").iterator()
    override fun hasNextLine() = partitioned.hasNext()
    override fun nextLine() = partitioned.next()
}

/**
 * Scanner input  `Input` interface wrapper for `Scanner`
 *
 * @property scanner
 */
class ScannerInput(private val scanner: Scanner) : Input {
    override fun hasNextLine() = scanner.hasNextLine()
    override fun nextLine(): String = scanner.nextLine()
}

/**
 * Echo input echoes lines from input to out
 *
 * @property input
 * @property out
 */
class EchoInput(private val input: Input, private val out: Output) : Input {
    override fun hasNextLine() = input.hasNextLine()
    override fun nextLine(): String {
        val res = input.nextLine()
        out.println(res)
        return res
    }
}

interface Output {
    fun println(s: String) {}
    fun print(s: String) {}
}

/**
 * Null output - outputs to nothing
 */
class NullOutput : Output

/**
 * Print writer output `Output` wrapper for `PrintWriter`
 *
 * @property print_writer printer
 */
class PrintWriterOutput(private val print_writer: PrintWriter) : Output {
    override fun println(s: String) {
        print_writer.println(s)
        print_writer.flush()
    }

    override fun print(s: String) {
        print_writer.print(s)
        print_writer.flush()
    }
}

interface Io {
    val scanner: Input
    val printer: Output

    fun <T> readVal(prompt: String, parser: (s: String) -> T): T {
        while (true) {
            printer.print("$prompt: ")
            if (!scanner.hasNextLine()) throw Exception("unexpected end of input")
            try {
                return parser(scanner.nextLine())
            } catch (e: Exception) {
                printer.println("error: ${e.message}}")
            }
        }
    }

    fun <T> readNested(prompt: String, parser: () -> T): T {
        printer.println("$prompt: ")
        return parser()
    }

    interface IoReadable<T> {
        fun read(io: Io, full: Boolean = false): T
    }
}

/**
 * Generic io  - Io implementer for generic scanner+printer
 *
 * @property scanner
 * @property printer
 */
class GenericIo(override val scanner: Input, override val printer: Output) : Io

/**
 * Console io - Io implementer for standard in+out
 */
class ConsoleIo : Io {
    override val scanner = ScannerInput(Scanner(System.`in`))
    override val printer = PrintWriterOutput(PrintWriter(System.out))
}

/**
 * File io - Io implementer for files
 * @param file filepath
 */
class FileIo(file: String) : Io {
    override var printer = PrintWriterOutput(PrintWriter(System.out))
    override var scanner = EchoInput(ScannerInput(Scanner(File(file))), printer)
}
