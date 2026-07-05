import java.util.concurrent.Semaphore;

/**
 * Thread A executes three functions in sequence: FuncA1, FuncA2, FuncA3.
 *
 * <p>Execution dependencies (from Figure 2.1):</p>
 * <ul>
 *   <li>FuncA1 runs freely (no dependency)</li>
 *   <li>FuncA2 must wait for FuncB2 to complete</li>
 *   <li>FuncA3 must wait for FuncB3 to complete</li>
 * </ul>
 *
 * <p>Synchronisation is achieved using {@link Semaphore} objects.
 * No active waiting or {@code Thread.sleep()} is used.</p>
 *
 * @author Rayhan
 * @version 1.0
 */
public class ThreadA extends Thread {

    /** Shared variable store. */
    private final SharedVariables vars;

    /** Number of iterations to run (for verification). */
    private final int iterations;

    /**
     * Semaphore signalled by ThreadA after FuncA1 completes.
     * ThreadB's FuncB2 waits on this.
     */
    private final Semaphore a1Done;

    /**
     * Semaphore signalled by ThreadB after FuncB2 completes.
     * ThreadA's FuncA2 waits on this.
     */
    private final Semaphore b2Done;

    /**
     * Semaphore signalled by ThreadA after FuncA2 completes.
     * ThreadB's FuncB3 waits on this.
     */
    private final Semaphore a2Done;

    /**
     * Semaphore signalled by ThreadB after FuncB3 completes.
     * ThreadA's FuncA3 waits on this.
     */
    private final Semaphore b3Done;

    /**
     * Constructs ThreadA with all required synchronisation semaphores.
     *
     * @param vars       Shared variable container
     * @param iterations Number of times to repeat the function sequence
     * @param a1Done     Semaphore to release after FuncA1
     * @param b2Done     Semaphore to acquire before FuncA2
     * @param a2Done     Semaphore to release after FuncA2
     * @param b3Done     Semaphore to acquire before FuncA3
     */
    public ThreadA(SharedVariables vars, int iterations,
                   Semaphore a1Done, Semaphore b2Done,
                   Semaphore a2Done, Semaphore b3Done) {
        this.vars = vars;
        this.iterations = iterations;
        this.a1Done = a1Done;
        this.b2Done = b2Done;
        this.a2Done = a2Done;
        this.b3Done = b3Done;
        setName("ThreadA");
    }

    /**
     * Runs FuncA1 → FuncA2 → FuncA3 in the correct synchronised order
     * for the configured number of iterations.
     */
    @Override
    public void run() {
        for (int i = 0; i < iterations; i++) {
            funcA1();
            a1Done.release();       // Signal: A1 is ready for B's FuncB2

            try {
                b2Done.acquire();   // Wait: B2 must be ready before A2
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            funcA2();
            a2Done.release();       // Signal: A2 is ready for B's FuncB3

            try {
                b3Done.acquire();   // Wait: B3 must be ready before A3
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            funcA3();
        }
    }

    /**
     * FuncA1: Computes A1 = sum(0..500).
     * No dependency — runs immediately.
     */
    private void funcA1() {
        vars.A1 = MathUtils.sum(500);
    }

    /**
     * FuncA2: Computes A2 = B2 + sum(0..300).
     * Depends on FuncB2 completing first.
     */
    private void funcA2() {
        vars.A2 = vars.B2 + MathUtils.sum(300);
    }

    /**
     * FuncA3: Computes A3 = B3 + sum(0..400).
     * Depends on FuncB3 completing first.
     */
    private void funcA3() {
        vars.A3 = vars.B3 + MathUtils.sum(400);
    }
}
