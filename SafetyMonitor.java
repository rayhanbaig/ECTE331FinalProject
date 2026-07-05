/**
 * High-priority real-time thread that monitors the robotic arm for emergency conditions.
 *
 * <p>As the highest priority thread, it should always preempt lower-priority threads
 * and access the {@link MotorController} as quickly as possible. Priority inversion
 * occurs when this thread is blocked by a lower-priority thread holding the resource.</p>
 *
 * <p>This class is used across Tasks 1–5. Its behaviour adapts based on which
 * synchronisation strategy the {@link RoboticArmSimulation} applies.</p>
 *
 * @author Rayhan
 * @version 1.0
 */
public class SafetyMonitor extends Thread {

    /** Reference to the shared motor resource. */
    private final MotorController motorController;

    /** Number of times this thread should execute its task cycle. */
    private final int cycles;

    /** Tracks the time (ms) this thread spent waiting to acquire the resource. */
    private long totalWaitTime = 0;

    /** Records the last measured wait time for reporting. */
    private long lastWaitTime = 0;

    /**
     * Constructs a SafetyMonitor thread with HIGH priority.
     *
     * @param motorController Shared motor resource
     * @param cycles          Number of execution cycles
     */
    public SafetyMonitor(MotorController motorController, int cycles) {
        this.motorController = motorController;
        this.cycles = cycles;
        setName("SafetyMonitor");
        setPriority(Thread.MAX_PRIORITY); // Priority 10
    }

    /**
     * Executes the safety monitoring loop. In each cycle, the thread attempts
     * to acquire the MotorController lock and perform a brief safety check.
     * Waits are measured for performance evaluation.
     */
    @Override
    public void run() {
        for (int i = 0; i < cycles; i++) {
            System.out.println("[" + System.currentTimeMillis() + "] "
                    + getName() + " (P=" + getPriority() + ") attempting to acquire MotorController...");

            long waitStart = System.currentTimeMillis();

            synchronized (motorController) {
                lastWaitTime = System.currentTimeMillis() - waitStart;
                totalWaitTime += lastWaitTime;

                System.out.println("[" + System.currentTimeMillis() + "] "
                        + getName() + " ACQUIRED MotorController. Wait=" + lastWaitTime + "ms");
                motorController.useMotor(getName(), 50); // brief safety check
                System.out.println("[" + System.currentTimeMillis() + "] "
                        + getName() + " RELEASED MotorController.");
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Returns the total time this thread spent waiting for the resource.
     *
     * @return total wait time in milliseconds
     */
    public long getTotalWaitTime() {
        return totalWaitTime;
    }

    /**
     * Returns the last recorded wait time.
     *
     * @return last wait time in milliseconds
     */
    public long getLastWaitTime() {
        return lastWaitTime;
    }
}
