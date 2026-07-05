    import java.util.Random;

/**
 * Simulates a redundant altitude sensor for the drone navigation system.
 * Each call to {@link #readSensor(int, int)} may produce a valid reading,
 * a corrupted reading, or throw a {@link SensorReadException} based on
 * a randomly generated fault scenario.
 *
 * <p>Fault probability distribution:</p>
 * <ul>
 *   <li>0–14  : Sensor failure → {@link SensorReadException} thrown</li>
 *   <li>15–29 : Corrupted reading (outside valid range [0, 200])</li>
 *   <li>30–99 : Valid reading within [0, 200]</li>
 * </ul>
 *
 * @author Rayhan
 * @version 1.0
 */
public class Sensor {

    /** Unique identifier for this sensor (e.g., "A", "B", "C"). */
    private final String sensorId;

    /** Shared random number generator. */
    private final Random random;

    /** Minimum valid altitude in meters. */
    private static final int MIN_ALTITUDE = 0;

    /** Maximum valid altitude in meters. */
    private static final int MAX_ALTITUDE = 200;

    /**
     * Constructs a Sensor with the given ID.
     *
     * @param sensorId Unique label for this sensor
     */
    public Sensor(String sensorId) {
        this.sensorId = sensorId;
        this.random = new Random();
    }

    /**
     * Returns the sensor's identifier.
     *
     * @return sensor ID string
     */
    public String getSensorId() {
        return sensorId;
    }

    /**
     * Attempts to read an altitude value from this sensor.
     *
     * <p>A random "chance" value (0–99) determines the outcome:</p>
     * <ul>
     *   <li>&lt; 15 : throws {@link SensorReadException}</li>
     *   <li>&lt; 30 : returns a corrupted (out-of-range) value</li>
     *   <li>else    : returns a valid altitude reading</li>
     * </ul>
     *
     * @param baselineValue The base altitude around which readings are generated
     * @param range         The spread of random variation added to baselineValue
     * @return An integer altitude reading (may be out of range if corrupted)
     * @throws SensorReadException if the sensor fails to produce any reading
     */
    public int readSensor(int baselineValue, int range) throws SensorReadException {
        int chance = random.nextInt(100); // 0 to 99 inclusive

        if (chance < 15) {
            // Sensor hardware failure
            throw new SensorReadException("Sensor " + sensorId + " hardware failure.");
        } else if (chance < 30) {
            // Corrupted reading: value outside [0, 200]
            // Generate either a negative value or one above 200
            int corruptedValue = random.nextBoolean()
                    ? -(random.nextInt(100) + 1)          // negative
                    : MAX_ALTITUDE + random.nextInt(100) + 1; // above max
            return corruptedValue;
        } else {
            // Valid reading within [baselineValue, baselineValue + range - 1]
            // Clamped to [MIN_ALTITUDE, MAX_ALTITUDE]
            int reading = baselineValue + random.nextInt(range);
            return Math.max(MIN_ALTITUDE, Math.min(MAX_ALTITUDE, reading));
        }
    }

    /**
     * Checks whether a given altitude value is within the valid range [0, 200].
     *
     * @param value The altitude value to validate
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValid(int value) {
        return value >= MIN_ALTITUDE && value <= MAX_ALTITUDE;
    }
}
