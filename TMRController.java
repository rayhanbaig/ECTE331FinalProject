import java.util.ArrayList;
import java.util.List;

/**
 * Implements Triple Modular Redundancy (TMR) majority voting for the drone
 * altitude estimation system.
 *
 * <p>Voting rules:</p>
 * <ol>
 *   <li>If two or more valid sensor readings agree, that value is used.</li>
 *   <li>If all valid readings differ, the last known valid altitude is used (fallback).</li>
 *   <li>If fewer than 2 valid readings exist, or no majority is found,
 *       a reliability failure is recorded.</li>
 * </ol>
 *
 * <p>After two consecutive reliability failures, a
 * {@link SystemReliabilityException} is thrown to trigger SAFE MODE.</p>
 *
 * @author Rayhan
 * @version 1.0
 */
public class TMRController {

    /** The last successfully determined altitude value. */
    private int lastValidAltitude;

    /** Counter for consecutive reliability failures. */
    private int consecutiveFailures;

    /** Maximum allowed consecutive failures before SAFE MODE. */
    private static final int MAX_CONSECUTIVE_FAILURES = 2;

    /**
     * Constructs a TMRController with an initial known altitude.
     *
     * @param initialAltitude Starting altitude before any sensor readings
     */
    public TMRController(int initialAltitude) {
        this.lastValidAltitude = initialAltitude;
        this.consecutiveFailures = 0;
    }

    /**
     * Processes raw sensor readings from three sensors and determines the
     * final altitude using majority voting.
     *
     * <p>Each entry in {@code readings} is either a valid integer or
     * {@code null} if that sensor failed or produced a corrupted value.</p>
     *
     * @param readings      List of up to 3 nullable altitude readings
     * @param sensorIds     Corresponding sensor IDs for logging purposes
     * @return              The decided altitude value
     * @throws SystemReliabilityException if two consecutive failures occur
     */
    public int vote(List<Integer> readings, List<String> sensorIds)
            throws SystemReliabilityException {

        // Collect valid readings and track which sensors are outliers
        List<Integer> validReadings = new ArrayList<>();
        List<String> validIds = new ArrayList<>();
        List<String> outlierIds = new ArrayList<>();

        for (int i = 0; i < readings.size(); i++) {
            if (readings.get(i) != null) {
                validReadings.add(readings.get(i));
                validIds.add(sensorIds.get(i));
            } else {
                outlierIds.add(sensorIds.get(i));
            }
        }

        // Log outlier sensors
        if (!outlierIds.isEmpty()) {
            DroneLogger.log("Outlier/failed sensors: " + outlierIds);
        }

        // Need at least 2 valid readings to attempt a vote
        if (validReadings.size() < 2) {
            return handleFailure("Fewer than 2 valid sensor readings available.");
        }

        // Check for majority agreement among valid readings
        for (int i = 0; i < validReadings.size(); i++) {
            for (int j = i + 1; j < validReadings.size(); j++) {
                if (validReadings.get(i).equals(validReadings.get(j))) {
                    int agreedValue = validReadings.get(i);
                    DroneLogger.log("Majority decision: Altitude = " + agreedValue
                            + "m (sensors " + validIds.get(i) + " & " + validIds.get(j) + " agree)");
                    lastValidAltitude = agreedValue;
                    consecutiveFailures = 0; // reset on success
                    return agreedValue;
                }
            }
        }

        // No majority found among valid readings — use fallback
        DroneLogger.log("No majority found among valid sensors. "
                + "Fallback to last valid altitude: " + lastValidAltitude + "m");
        return handleFailure("No majority consensus among sensors.");
    }

    /**
     * Handles a reliability failure event. Increments the consecutive failure
     * counter and throws {@link SystemReliabilityException} if the threshold
     * is reached, otherwise returns the last valid altitude as a fallback.
     *
     * @param reason Human-readable reason for the failure
     * @return The last valid altitude (fallback value)
     * @throws SystemReliabilityException if consecutive failures reach the limit
     */
    private int handleFailure(String reason) throws SystemReliabilityException {
        consecutiveFailures++;
        DroneLogger.log("Reliability failure #" + consecutiveFailures + ": " + reason);

        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
            DroneLogger.log("SAFE MODE ACTIVATED — Two consecutive reliability failures detected.");
            throw new SystemReliabilityException(
                    "System entered SAFE MODE after " + consecutiveFailures + " consecutive failures.");
        }

        DroneLogger.log("Fallback altitude used: " + lastValidAltitude + "m");
        return lastValidAltitude;
    }

    /**
     * Returns the last successfully determined altitude.
     *
     * @return last valid altitude in meters
     */
    public int getLastValidAltitude() {
        return lastValidAltitude;
    }
}
