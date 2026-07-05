/**
 * Low-priority real-time thread that records system activity via the MotorController.
 *
 * <p>In the priority inversion scenario (Task 3), this thread acquires the
 * MotorController lock first, then holds it for an extended duration — causing
 * the high-priority SafetyMonitor to block while waiting for the lock.</p>
 *
 * <p>In Task 4 (Priority Inheritance), this thread's priority is temporarily
 * elevated to match the SafetyMonitor when the latter becomes blocked, so it
 * can complete and release the lock sooner.</p>
 *
 * @author Rayhan
 * @version 1.0
 */
public class Logger extends Thread {

    /** Reference to the shared motor resource. */
    private final MotorController motorController;

    /** Number of times this thread should execute its task cycle. */
    private final int cycles;

    /** Duration (ms) to hold the resource during logging. */
    private final long holdTimeMs;

    /** Reference to the SafetyMonitor — used for priority inheritance in Task 4. */
    private Thread blockedHighPriorityThread = null;

    /** Original priority before any inheritance elevation. */
    private int originalPriority;

    /** Flag indicating whether priority inheritance is enabled. */
    private final boolean inheritanceEnabled;

    /** Flag indicating whether priority ceiling is enabled. */
    private final boolean ceilingEnabled;

    /**
     * Constructs a Logger thread with LOW priority (2).
     *
     * @param motorController    Shared motor resource
     * @param cycles             Number of execution cycles
     * @param holdTimeMs         How long (ms) to hold the lock per cycle
     * @param inheritanceEnabled If true, simulates priority inheritance when blocked
     * @param ceilingEnabled     If true, applies priority ceiling on lock acquisition
     */
    public Logger(MotorController motorController, int cycles, long holdTimeMs,
                  boolean inheritanceEnabled, boolean ceilingEnabled) {
        this.motorController = motorController;
        this.cycles = cycles;
        this.holdTimeMs = holdTimeMs;
        this.inheritanceEnabled = inheritanceEnabled;
        this.ceilingEnabled = ceilingEnabled;
        setName("Logger");
        setPriority(Thread.MIN_PRIORITY + 1); // Priority 2
        this.originalPriority = getPriority();
    }

    /**
     * Sets the high-priority thread that may be blocked by this thread.
     * Used by Task 4 to apply priority inheritance.
     *
     * @param t The SafetyMonitor thread
     */
    public void setBlockedHighPriorityThread(Thread t) {
        this.blockedHighPriorityThread = t;
    }

    /**
     * Executes the logging loop. Acquires the MotorController and holds it
     * for the configured duration, simulating a long-running low-priority task.
     *
     * <p>If priority inheritance is enabled and a high-priority thread is
     * registered, this thread elevates its own priority to match before
     * entering the critical section, then restores it after release.</p>
     *
     * <p>If ceiling is enabled, the thread runs at the ceiling priority
     * for the duration of the critical section.</p>
     */
    @Override
    public void run() {
        for (int i = 0; i < cycles; i++) {
            System.out.println("[" + System.currentTimeMillis() + "] "
                    + getName() + " (P=" + getPriority() + ") attempting to acquire MotorController...");

            synchronized (motorController) {

                // --- Priority Ceiling Protocol (Task 5) ---
                if (ceilingEnabled) {
                    int ceiling = motorController.getCeilingPriority();
                    System.out.println("[" + System.currentTimeMillis() + "] "
                            + getName() + " applying CEILING priority: " + ceiling);
                    setPriority(ceiling);
                }

                // --- Priority Inheritance (Task 4) ---
                if (inheritanceEnabled && blockedHighPriorityThread != null) {
                    int inheritedPriority = blockedHighPriorityThread.getPriority();
                    System.out.println("[" + System.currentTimeMillis() + "] "
                            + getName() + " inheriting priority: " + inheritedPriority
                            + " (was " + originalPriority + ")");
                    setPriority(inheritedPriority);
                }

                System.out.println("[" + System.currentTimeMillis() + "] "
                        + getName() + " (P=" + getPriority() + ") ACQUIRED MotorController. Holding for "
                        + holdTimeMs + "ms...");
                motorController.useMotor(getName(), holdTimeMs);
                System.out.println("[" + System.currentTimeMillis() + "] "
                        + getName() + " RELEASING MotorController.");

                // Restore original priority after releasing the resource
                if (inheritanceEnabled || ceilingEnabled) {
                    System.out.println("[" + System.currentTimeMillis() + "] "
                            + getName() + " restoring priority to " + originalPriority);
                    setPriority(originalPriority);
                }
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
