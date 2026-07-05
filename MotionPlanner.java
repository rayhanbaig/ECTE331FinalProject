/**
 * Medium-priority real-time thread that sends movement commands to the robotic arm motor.
 *
 * <p>In a priority inversion scenario, this thread can preempt the Logger (low priority)
 * even while the Logger holds the MotorController lock — effectively blocking the higher
 * priority SafetyMonitor from running, as it is waiting on the same lock.</p>
 *
 * @author Rayhan
 * @version 1.0
 */
public class MotionPlanner extends Thread {

    /** Reference to the shared motor resource. */
    private final MotorController motorController;

    /** Number of times this thread should execute its task cycle. */
    private final int cycles;

    /** Additional CPU work (ms) performed outside the critical section. */
    private final long cpuWorkMs;

    /**
     * Constructs a MotionPlanner thread with MEDIUM priority (5).
     *
     * @param motorController Shared motor resource
     * @param cycles          Number of execution cycles
     * @param cpuWorkMs       Duration of non-critical CPU work per cycle (ms)
     */
    public MotionPlanner(MotorController motorController, int cycles, long cpuWorkMs) {
        this.motorController = motorController;
        this.cycles = cycles;
        this.cpuWorkMs = cpuWorkMs;
        setName("MotionPlanner");
        setPriority(Thread.NORM_PRIORITY); // Priority 5
    }

    /**
     * Executes the motion planning loop. Performs CPU work outside the critical
     * section, then acquires the motor controller to send a movement command.
     */
    @Override
    public void run() {
        for (int i = 0; i < cycles; i++) {
            // Simulate medium-priority CPU work (outside critical section)
            System.out.println("[" + System.currentTimeMillis() + "] "
                    + getName() + " (P=" + getPriority() + ") doing CPU work for " + cpuWorkMs + "ms...");
            try {
                Thread.sleep(cpuWorkMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            System.out.println("[" + System.currentTimeMillis() + "] "
                    + getName() + " attempting to acquire MotorController...");

            synchronized (motorController) {
                System.out.println("[" + System.currentTimeMillis() + "] "
                        + getName() + " ACQUIRED MotorController.");
                motorController.useMotor(getName(), 50); // send movement command
                System.out.println("[" + System.currentTimeMillis() + "] "
                        + getName() + " RELEASED MotorController.");
            }
        }
    }
}
