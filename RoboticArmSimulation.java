import java.util.ArrayList;
import java.util.List;

/**
 * Main simulation class for the Real-Time Robotic Arm Controller.
 *
 * <p>Runs six scenarios as specified in the project:</p>
 * <ol>
 *   <li>Basic multi-threaded execution (Tasks 1 &amp; 2)</li>
 *   <li>Priority inversion demonstration (Task 3)</li>
 *   <li>Priority inheritance protocol (Task 4)</li>
 *   <li>Priority ceiling protocol (Task 5)</li>
 *   <li>Performance evaluation across multiple runs (Task 6)</li>
 * </ol>
 *
 * @author Rayhan
 * @version 1.0
 */
public class RoboticArmSimulation {

    /** Separator line for console readability. */
    private static final String SEP = "=".repeat(60);

    /**
     * Application entry point. Runs all task scenarios sequentially.
     *
     * @param args Command-line arguments (not used)
     * @throws InterruptedException if any thread join is interrupted
     */
    public static void main(String[] args) throws InterruptedException {

        System.out.println(SEP);
        System.out.println("TASK 1 & 2: Basic Multi-threaded Implementation + Synchronisation");
        System.out.println(SEP);
        runBasic();

        Thread.sleep(500);

        System.out.println("\n" + SEP);
        System.out.println("TASK 3: Priority Inversion Demonstration");
        System.out.println(SEP);
        long inversionWait = runPriorityInversion();

        Thread.sleep(500);

        System.out.println("\n" + SEP);
        System.out.println("TASK 4: Priority Inheritance Protocol");
        System.out.println(SEP);
        long inheritanceWait = runPriorityInheritance();

        Thread.sleep(500);

        System.out.println("\n" + SEP);
        System.out.println("TASK 5: Priority Ceiling Protocol");
        System.out.println(SEP);
        long ceilingWait = runPriorityCeiling();

        Thread.sleep(500);

        System.out.println("\n" + SEP);
        System.out.println("TASK 6: Performance Evaluation (10 runs each)");
        System.out.println(SEP);
        runPerformanceEvaluation();
    }

    // =========================================================================
    // TASK 1 & 2: Basic implementation with mutual exclusion
    // =========================================================================

    /**
     * Runs the basic multi-threaded scenario. All three threads operate
     * independently with normal priorities. The {@code synchronized} block
     * on the MotorController ensures mutual exclusion (Task 2).
     *
     * @throws InterruptedException if thread join is interrupted
     */
    private static void runBasic() throws InterruptedException {
        MotorController motor = new MotorController(Thread.MAX_PRIORITY);

        SafetyMonitor safety  = new SafetyMonitor(motor, 2);
        MotionPlanner planner = new MotionPlanner(motor, 2, 30);
        Logger        logger  = new Logger(motor, 2, 80, false, false);

        safety.start();
        planner.start();
        logger.start();

        safety.join();
        planner.join();
        logger.join();

        System.out.println("\n[Basic] SafetyMonitor total wait: " + safety.getTotalWaitTime() + "ms");
    }

    // =========================================================================
    // TASK 3: Priority Inversion
    // =========================================================================

    /**
     * Demonstrates priority inversion:
     * <ol>
     *   <li>Logger (LOW) acquires the resource and holds it for a long time.</li>
     *   <li>SafetyMonitor (HIGH) tries to acquire and is blocked.</li>
     *   <li>MotionPlanner (MEDIUM) runs freely during this time, delaying release.</li>
     * </ol>
     *
     * @return Wait time of the SafetyMonitor in this run
     * @throws InterruptedException if thread join is interrupted
     */
    private static long runPriorityInversion() throws InterruptedException {
        MotorController motor = new MotorController(Thread.MAX_PRIORITY);

        // Logger holds resource for a long time to force inversion
        Logger        logger  = new Logger(motor, 1, 400, false, false);
        SafetyMonitor safety  = new SafetyMonitor(motor, 1);
        MotionPlanner planner = new MotionPlanner(motor, 1, 50);

        // Logger must start first to grab the lock
        logger.start();
        Thread.sleep(20); // let Logger acquire the lock
        safety.start();
        planner.start();

        logger.join();
        safety.join();
        planner.join();

        long wait = safety.getTotalWaitTime();
        System.out.println("\n[Inversion] SafetyMonitor waited: " + wait + "ms");
        System.out.println("[Inversion] MotionPlanner ran freely while SafetyMonitor was blocked.");
        return wait;
    }

    // =========================================================================
    // TASK 4: Priority Inheritance
    // =========================================================================

    /**
     * Demonstrates priority inheritance. When SafetyMonitor is blocked by Logger,
     * Logger temporarily inherits SafetyMonitor's priority, preventing MotionPlanner
     * from preempting it and reducing the wait time.
     *
     * @return Wait time of the SafetyMonitor in this run
     * @throws InterruptedException if thread join is interrupted
     */
    private static long runPriorityInheritance() throws InterruptedException {
        MotorController motor = new MotorController(Thread.MAX_PRIORITY);

        Logger        logger  = new Logger(motor, 1, 400, true, false);
        SafetyMonitor safety  = new SafetyMonitor(motor, 1);
        MotionPlanner planner = new MotionPlanner(motor, 1, 50);

        // Tell logger which high-priority thread to inherit from
        logger.setBlockedHighPriorityThread(safety);

        logger.start();
        Thread.sleep(20);
        safety.start();
        planner.start();

        logger.join();
        safety.join();
        planner.join();

        long wait = safety.getTotalWaitTime();
        System.out.println("\n[Inheritance] SafetyMonitor waited: " + wait + "ms");
        return wait;
    }

    // =========================================================================
    // TASK 5: Priority Ceiling
    // =========================================================================

    /**
     * Demonstrates the priority ceiling protocol. Any thread acquiring the
     * MotorController is elevated to the ceiling priority (MAX), preventing
     * any medium-priority thread from preempting during the critical section.
     *
     * @return Wait time of the SafetyMonitor in this run
     * @throws InterruptedException if thread join is interrupted
     */
    private static long runPriorityCeiling() throws InterruptedException {
        // Ceiling = MAX_PRIORITY so no thread can preempt inside critical section
        MotorController motor = new MotorController(Thread.MAX_PRIORITY);

        Logger        logger  = new Logger(motor, 1, 400, false, true);
        SafetyMonitor safety  = new SafetyMonitor(motor, 1);
        MotionPlanner planner = new MotionPlanner(motor, 1, 50);

        logger.start();
        Thread.sleep(20);
        safety.start();
        planner.start();

        logger.join();
        safety.join();
        planner.join();

        long wait = safety.getTotalWaitTime();
        System.out.println("\n[Ceiling] SafetyMonitor waited: " + wait + "ms");
        return wait;
    }

    // =========================================================================
    // TASK 6: Performance Evaluation
    // =========================================================================

    /**
     * Runs each protocol 10 times and reports average wait times for
     * the SafetyMonitor (high-priority thread).
     *
     * @throws InterruptedException if any thread join is interrupted
     */
    private static void runPerformanceEvaluation() throws InterruptedException {
        int runs = 10;

        List<Long> inversionTimes   = new ArrayList<>();
        List<Long> inheritanceTimes = new ArrayList<>();
        List<Long> ceilingTimes     = new ArrayList<>();

        for (int r = 0; r < runs; r++) {
            System.out.println("\n--- Performance Run " + (r + 1) + " ---");
            inversionTimes.add(runPriorityInversion());
            inheritanceTimes.add(runPriorityInheritance());
            ceilingTimes.add(runPriorityCeiling());
            Thread.sleep(100);
        }

        double avgInversion   = average(inversionTimes);
        double avgInheritance = average(inheritanceTimes);
        double avgCeiling     = average(ceilingTimes);

        System.out.println("\n" + SEP);
        System.out.println("PERFORMANCE EVALUATION RESULTS (" + runs + " runs)");
        System.out.println(SEP);
        System.out.printf("%-30s %10s%n", "Protocol", "Avg Wait (ms)");
        System.out.println("-".repeat(42));
        System.out.printf("%-30s %10.2f%n", "No Protocol (Inversion)", avgInversion);
        System.out.printf("%-30s %10.2f%n", "Priority Inheritance",    avgInheritance);
        System.out.printf("%-30s %10.2f%n", "Priority Ceiling",        avgCeiling);
        System.out.println(SEP);

        System.out.println("\nDetailed Results:");
        System.out.println("Run | No Protocol | Inheritance | Ceiling");
        System.out.println("-".repeat(50));
        for (int i = 0; i < runs; i++) {
            System.out.printf("%-3d | %-11d | %-11d | %d%n",
                    i + 1,
                    inversionTimes.get(i),
                    inheritanceTimes.get(i),
                    ceilingTimes.get(i));
        }
    }

    /**
     * Computes the average of a list of long values.
     *
     * @param values List of measurements
     * @return Arithmetic mean as a double
     */
    private static double average(List<Long> values) {
        return values.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}
