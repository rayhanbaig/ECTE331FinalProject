import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for the Fault-Tolerant Autonomous Drone Navigation System.
 *
 * <p>This class orchestrates sensor reading, fault simulation, majority voting
 * via TMR, reliability monitoring, and safe mode activation. All events are
 * logged to {@code log.txt} with timestamps.</p>
 *
 * <p>The system runs in two phases:</p>
 * <ol>
 *   <li><b>Demo phase</b>: Scripted cycles that guarantee every use case is
 *       shown (majority decision, corrupted reading, sensor failure, fallback,
 *       SAFE MODE). Resets the consecutive-failure counter between scenarios
 *       so all cases are visible in one run.</li>
 *   <li><b>Random phase</b>: Normal random operation for the remaining cycles.</li>
 * </ol>
 *
 * @author Rayhan
 * @version 1.0
 */
public class DroneNavigationSystem {

    /** Baseline altitude used for sensor reading generation (meters). */
    private static final int BASELINE_ALTITUDE = 100;

    /** Random variation range around baseline for sensor readings. */
    private static final int SENSOR_RANGE = 10;

    /**
     * Application entry point.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        DroneLogger.init();

        Sensor sensorA = new Sensor("A");
        Sensor sensorB = new Sensor("B");
        Sensor sensorC = new Sensor("C");

        DroneLogger.log("System initialised. Baseline: " + BASELINE_ALTITUDE
                + "m, Range: " + SENSOR_RANGE + "m");
        DroneLogger.log("==================================================");

        // ---------------------------------------------------------------
        // DEMO PHASE: one scripted cycle per use case
        // ---------------------------------------------------------------
        DroneLogger.log(">>> DEMO PHASE: Scripted scenarios <<<");

        // --- Scenario 1: All three sensors valid and agree (majority) ---
        DroneLogger.log("--- Scenario 1: All sensors valid & agree ---");
        {
            TMRController tmr = new TMRController(BASELINE_ALTITUDE);
            List<Integer> readings = List.of(105, 105, 105);
            List<String>  ids      = List.of("A", "B", "C");
            logReadings(readings, ids);
            try { tmr.vote(readings, ids); } catch (SystemReliabilityException e) { handleSafe(e); }
        }
        DroneLogger.log("==================================================");

        // --- Scenario 2: Two agree, one corrupted (majority still found) ---
        DroneLogger.log("--- Scenario 2: Two agree, one corrupted ---");
        {
            TMRController tmr = new TMRController(BASELINE_ALTITUDE);
            List<Integer> readings = new ArrayList<>();
            readings.add(102); readings.add(102); readings.add(null); // null = corrupted/failed
            List<String> ids = List.of("A", "B", "C");
            logReadingsNullable(readings, ids);
            try { tmr.vote(readings, ids); } catch (SystemReliabilityException e) { handleSafe(e); }
        }
        DroneLogger.log("==================================================");

        // --- Scenario 3: All sensors differ — no majority, fallback used ---
        DroneLogger.log("--- Scenario 3: All sensors differ, fallback used ---");
        {
            TMRController tmr = new TMRController(BASELINE_ALTITUDE);
            List<Integer> readings = List.of(100, 105, 110);
            List<String>  ids      = List.of("A", "B", "C");
            logReadings(readings, ids);
            try { tmr.vote(readings, ids); } catch (SystemReliabilityException e) { handleSafe(e); }
        }
        DroneLogger.log("==================================================");

        // --- Scenario 4: Fewer than 2 valid sensors (reliability failure #1) ---
        DroneLogger.log("--- Scenario 4: Only 1 valid sensor (failure #1) ---");
        {
            TMRController tmr = new TMRController(BASELINE_ALTITUDE);
            List<Integer> readings = new ArrayList<>();
            readings.add(100); readings.add(null); readings.add(null);
            List<String> ids = List.of("A", "B", "C");
            logReadingsNullable(readings, ids);
            try { tmr.vote(readings, ids); } catch (SystemReliabilityException e) { handleSafe(e); }
        }
        DroneLogger.log("==================================================");

        // --- Scenario 5: Two consecutive failures → SAFE MODE ---
        DroneLogger.log("--- Scenario 5: Two consecutive failures → SAFE MODE ---");
        {
            TMRController tmr = new TMRController(BASELINE_ALTITUDE);
            List<Integer> fail = new ArrayList<>();
            fail.add(null); fail.add(null); fail.add(null);
            List<String> ids = List.of("A", "B", "C");

            // First failure
            DroneLogger.log("[Failure 1]");
            logReadingsNullable(fail, ids);
            try { tmr.vote(fail, ids); } catch (SystemReliabilityException e) { handleSafe(e); return; }

            // Second failure → SAFE MODE
            DroneLogger.log("[Failure 2]");
            logReadingsNullable(fail, ids);
            try { tmr.vote(fail, ids); } catch (SystemReliabilityException e) {
                handleSafe(e);
                DroneLogger.close();
                return; // stop here — SAFE MODE reached
            }
        }

        DroneLogger.close();
    }

    /** Logs non-nullable readings for display. */
    private static void logReadings(List<Integer> readings, List<String> ids) {
        for (int i = 0; i < readings.size(); i++) {
            int v = readings.get(i);
            String status = Sensor.isValid(v) ? "VALID" : "CORRUPTED";
            DroneLogger.log("Sensor " + ids.get(i) + " reading: " + v + "m [" + status + "]");
        }
    }

    /** Logs nullable readings (null = sensor failed or corrupted). */
    private static void logReadingsNullable(List<Integer> readings, List<String> ids) {
        for (int i = 0; i < readings.size(); i++) {
            if (readings.get(i) == null) {
                DroneLogger.log("Sensor " + ids.get(i) + " FAILURE or CORRUPTED [unusable]");
            } else {
                DroneLogger.log("Sensor " + ids.get(i) + " reading: " + readings.get(i) + "m [VALID]");
            }
        }
    }

    /** Logs SAFE MODE activation. */
    private static void handleSafe(SystemReliabilityException e) {
        DroneLogger.log("*** SAFE MODE *** " + e.getMessage());
        DroneLogger.log("Drone halted. Manual intervention required.");
    }
}