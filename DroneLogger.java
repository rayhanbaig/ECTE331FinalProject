import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class responsible for logging drone navigation events to a file.
 * All log entries are timestamped and written to {@code log.txt}.
 * Also mirrors output to the console.
 *
 * <p>Usage: call {@link #init()} once at startup, then use
 * {@link #log(String)} throughout the application, and call
 * {@link #close()} before exit.</p>
 *
 * @author Rayhan
 * @version 1.0
 */
public class DroneLogger {

    /** Output file name for the log. */
    private static final String LOG_FILE = "log.txt";

    /** Formatter for timestamp prefix on each log entry. */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Writer used to append entries to the log file. */
    private static PrintWriter writer;

    /**
     * Initialises the logger by opening (or creating) the log file.
     * Must be called before any calls to {@link #log(String)}.
     */
    public static void init() {
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE, false)); // overwrite each run
            log("=== Drone Navigation System Started ===");
        } catch (IOException e) {
            System.err.println("Failed to open log file: " + e.getMessage());
        }
    }

    /**
     * Writes a timestamped message to the log file and to the console.
     *
     * @param message The event description to log
     */
    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String entry = "[" + timestamp + "] " + message;
        System.out.println(entry);
        if (writer != null) {
            writer.println(entry);
            writer.flush();
        }
    }

    /**
     * Closes the log file writer. Should be called once at application shutdown.
     */
    public static void close() {
        if (writer != null) {
            log("=== Drone Navigation System Stopped ===");
            writer.close();
        }
    }
}
