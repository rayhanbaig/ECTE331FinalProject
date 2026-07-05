import java.io.IOException;

/**
 * Custom exception representing a sensor hardware failure.
 * Thrown when a sensor is unable to produce a reading.
 *
 * @author Rayhan
 * @version 1.0
 */
public class SensorReadException extends IOException {

    /**
     * Constructs a SensorReadException with a descriptive message.
     *
     * @param message Description of the sensor failure
     */
    public SensorReadException(String message) {
        super(message);
    }
}
