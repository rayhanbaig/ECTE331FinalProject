import java.util.concurrent.Semaphore;

/**
 * Main entry point for the Threads Synchronisation and Communication application.
 *
 * <p>Runs Thread A and Thread B for a high number of iterations to verify
 * that synchronisation is always correctly enforced, regardless of OS scheduling.</p>
 *
 * <p>Expected final values (per mathematical derivation):</p>
 * <ul>
 *   <li>A1 = 125250  (sum 0..500)</li>
 *   <li>B1 = 31375   (sum 0..250)</li>
 *   <li>B2 = 145450  (A1 + sum 0..200 = 125250 + 20100 + 100 = 145450)</li>
 *   <li>A2 = 190600  (B2 + sum 0..300 = 145450 + 45150)</li>
 *   <li>B3 = 271600  (A2 + sum 0..400 = 190600 + 80200 + 800)</li>
 *   <li>A3 = 351800  (B3 + sum 0..400 = 271600 + 80200)</li>
 * </ul>
 *
 * @author Rayhan
 * @version 1.0
 */
public class Main {

    /** Number of iterations for stress-test verification. */
    private static final int ITERATIONS = 1000;

    /**
     * Creates shared variables, semaphores, and both threads, then runs them
     * for {@link #ITERATIONS} iterations and verifies the final result.
     *
     * @param args Command-line arguments (not used)
     * @throws InterruptedException if the main thread is interrupted while waiting
     */
    public static void main(String[] args) throws InterruptedException {

        // Print expected values first
        long expA1 = MathUtils.sum(500);
        long expB1 = MathUtils.sum(250);
        long expB2 = expA1 + MathUtils.sum(200);
        long expA2 = expB2 + MathUtils.sum(300);
        long expB3 = expA2 + MathUtils.sum(400);
        long expA3 = expB3 + MathUtils.sum(400);

        System.out.println("=== Expected Values ===");
        System.out.println("A1 = " + expA1);
        System.out.println("B1 = " + expB1);
        System.out.println("B2 = " + expB2);
        System.out.println("A2 = " + expA2);
        System.out.println("B3 = " + expB3);
        System.out.println("A3 = " + expA3);
        System.out.println("=======================\n");

        int passCount = 0;
        int failCount = 0;

        // Run many independent trials to verify correctness under any scheduling
        for (int trial = 0; trial < ITERATIONS; trial++) {

            SharedVariables vars = new SharedVariables();

            /*
             * Four semaphores control the execution ordering:
             *   a1Done: ThreadA signals after FuncA1 → ThreadB waits before FuncB2
             *   b2Done: ThreadB signals after FuncB2 → ThreadA waits before FuncA2
             *   a2Done: ThreadA signals after FuncA2 → ThreadB waits before FuncB3
             *   b3Done: ThreadB signals after FuncB3 → ThreadA waits before FuncA3
             *
             * All start at 0 (blocking) so threads must signal before the other proceeds.
             */
            Semaphore a1Done = new Semaphore(0);
            Semaphore b2Done = new Semaphore(0);
            Semaphore a2Done = new Semaphore(0);
            Semaphore b3Done = new Semaphore(0);

            ThreadA threadA = new ThreadA(vars, 1, a1Done, b2Done, a2Done, b3Done);
            ThreadB threadB = new ThreadB(vars, 1, a1Done, b2Done, a2Done, b3Done);

            threadA.start();
            threadB.start();

            threadA.join();
            threadB.join();

            // Verify all final values match expected
            boolean correct = vars.A1 == expA1 && vars.B1 == expB1
                    && vars.B2 == expB2 && vars.A2 == expA2
                    && vars.B3 == expB3 && vars.A3 == expA3;

            if (correct) {
                passCount++;
            } else {
                failCount++;
                System.out.println("MISMATCH at trial " + trial
                        + ": A1=" + vars.A1 + " B1=" + vars.B1
                        + " B2=" + vars.B2 + " A2=" + vars.A2
                        + " B3=" + vars.B3 + " A3=" + vars.A3);
            }
        }

        System.out.println("\n=== Verification Results (" + ITERATIONS + " trials) ===");
        System.out.println("PASSED: " + passCount);
        System.out.println("FAILED: " + failCount);

        if (failCount == 0) {
            System.out.println("All trials produced correct results. Synchronisation is correct.");
        } else {
            System.out.println("Some trials failed! Synchronisation has an issue.");
        }

        // Print one final run's values for reference
        SharedVariables finalVars = new SharedVariables();
        Semaphore a1Done = new Semaphore(0);
        Semaphore b2Done = new Semaphore(0);
        Semaphore a2Done = new Semaphore(0);
        Semaphore b3Done = new Semaphore(0);
        ThreadA tA = new ThreadA(finalVars, 1, a1Done, b2Done, a2Done, b3Done);
        ThreadB tB = new ThreadB(finalVars, 1, a1Done, b2Done, a2Done, b3Done);
        tA.start(); tB.start();
        tA.join();  tB.join();

        System.out.println("\n=== Final Run Values ===");
        System.out.println("A1 = " + finalVars.A1);
        System.out.println("B1 = " + finalVars.B1);
        System.out.println("B2 = " + finalVars.B2);
        System.out.println("A2 = " + finalVars.A2);
        System.out.println("B3 = " + finalVars.B3);
        System.out.println("A3 = " + finalVars.A3);
    }
}
