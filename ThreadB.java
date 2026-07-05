import java.util.concurrent.Semaphore;

/**
 * Thread B executes three functions in sequence: FuncB1, FuncB2, FuncB3.
 *
 * <p>Execution dependencies (from Figure 2.1):</p>
 * <ul>
 *   <li>FuncB1 runs freely (no dependency)</li>
 *   <li>FuncB2 must wait for FuncA1 to complete</li>
 *   <li>FuncB3 must wait for FuncA2 to complete</li>
 * </ul>
 *
 * <p>Synchronisation is achieved using {@link Semaphore} objects.
 * No active waiting or {@code Thread.sleep()} is used.</p>
 *
 * @author Rayhan
 * @version 1.0
 */
public class ThreadB extends Thread {

    /** Shared variable store. */
    private final SharedVariables vars;

    /** Number of iterations to run (for verification). */
    private final int iterations;

    /** Semaphore to acquire before FuncB2 (waits for FuncA1). */
    private final Semaphore a1Done;

    /** Semaphore to release after FuncB2 (signals ThreadA's FuncA2). */
    private final Semaphore b2Done;

    /** Semaphore to acquire before FuncB3 (waits for FuncA2). */
    private final Semaphore a2Done;

    /** Semaphore to release after FuncB3 (signals ThreadA's FuncA3). */
    private final Semaphore b3Done;

    /**
     * Constructs ThreadB with all required synchronisation semaphores.
     *
     * @param vars       Shared variable container
     * @param iterations Number of times to repeat the function sequence
     * @param a1Done     Semaphore to acquire before FuncB2
     * @param b2Done     Semaphore to release after FuncB2
     * @param a2Done     Semaphore to acquire before FuncB3
     * @param b3Done     Semaphore to release after FuncB3
     */
    public ThreadB(SharedVariables vars, int iterations,
                   Semaphore a1Done, Semaphore b2Done,
                   Semaphore a2Done, Semaphore b3Done) {
        this.vars = vars;
        this.iterations = iterations;
        this.a1Done = a1Done;
        this.b2Done = b2Done;
        this.a2Done = a2Done;
        this.b3Done = b3Done;
        setName("ThreadB");
    }

    /**
     * Runs FuncB1 → FuncB2 → FuncB3 in the correct synchronised order
     * for the configured number of iterations.
     */
    @Override
    public void run() {
        for (int i = 0; i < iterations; i++) {
            funcB1();
            // FuncB1 has no dependency on A — runs immediately

            try {
                a1Done.acquire();   // Wait: A1 must be ready before B2
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            funcB2();
            b2Done.release();       // Signal: B2 is ready for A's FuncA2

            try {
                a2Done.acquire();   // Wait: A2 must be ready before B3
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            funcB3();
            b3Done.release();       // Signal: B3 is ready for A's FuncA3
        }
    }

    /**
     * FuncB1: Computes B1 = sum(0..250).
     * No dependency — runs immediately.
     */
    private void funcB1() {
        vars.B1 = MathUtils.sum(250);
    }

    /**
     * FuncB2: Computes B2 = A1 + sum(0..200).
     * Depends on FuncA1 completing first.
     */
    private void funcB2() {
        vars.B2 = vars.A1 + MathUtils.sum(200);
    }

    /**
     * FuncB3: Computes B3 = A2 + sum(0..400).
     * Depends on FuncA2 completing first.
     */
    private void funcB3() {
        vars.B3 = vars.A2 + MathUtils.sum(400);
    }
}
