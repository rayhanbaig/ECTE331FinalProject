/**
 * Custom exception representing a system-level reliability failure.
 * Thrown when two consecutive reliability failures occur, triggering SAFE MODE.
 *
 * @author Rayhan
 * @version 1.0
 */
public class SystemReliabilityException extends Exception {

    /**
     * Constructs a SystemReliabilityException with a descriptive message.
     *
     * @param message Description of the reliability failure
     */
    public SystemReliabilityException(String message) {
        super(message);
    }
}
