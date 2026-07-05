/**
 * Simulates the shared MotorController resource of the robotic arm system.
 *
 * <p>Only one thread may access this resource at a time. Access is
 * controlled externally by the thread implementations using synchronised
 * blocks, priority inheritance, or priority ceiling depending on the task.</p>
 *
 * <p>This class also supports dynamic priority ceiling assignment for Task 5.</p>
 *
 * @author Rayhan
 * @version 1.0
 */
public class MotorController {

    /** The ceiling priority applied when the Priority Ceiling Protocol is active. */
    private final int ceilingPriority;

    /** Name of the thread currently holding the resource (for logging). */
    private volatile String currentHolder = "none";

    /**
     * Constructs a MotorController with a specified ceiling priority.
     *
     * @param ceilingPriority Priority to assign to any thread accessing this resource
     *                        (used in Task 5 – Priority Ceiling Protocol)
     */
    public MotorController(int ceilingPriority) {
        this.ceilingPriority = ceilingPriority;
    }

    /**
     * Returns the priority ceiling assigned to this resource.
     *
     * @return ceiling priority value
     */
    public int getCeilingPriority() {
        return ceilingPriority;
    }

    /**
     * Simulates using the motor controller for a specified duration.
     * Must be called while holding the lock on this object.
     *
     * @param threadName  Name of the calling thread (for display)
     * @param durationMs  How long (ms) to simulate motor activity
     */
    public void useMotor(String threadName, long durationMs) {
        currentHolder = threadName;
        System.out.println("[" + System.currentTimeMillis() + "] "
                + threadName + " is using MotorController for " + durationMs + "ms");
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        currentHolder = "none";
    }

    /**
     * Returns the name of the thread currently holding the resource.
     *
     * @return holder thread name, or "none"
     */
    public String getCurrentHolder() {
        return currentHolder;
    }
}
