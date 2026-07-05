/**
 * Shared data container holding the integer variables accessed by
 * Thread A and Thread B.
 *
 * <p>Variables are declared {@code volatile} to ensure visibility across
 * threads, but actual ordering guarantees are enforced by semaphores
 * in the thread implementations.</p>
 *
 * @author Rayhan
 * @version 1.0
 */
public class SharedVariables {

    /** Result of FuncA1: sum(0..500) */
    public volatile long A1 = 0;

    /** Result of FuncA2: B2 + sum(0..300) */
    public volatile long A2 = 0;

    /** Result of FuncA3: B3 + sum(0..400) */
    public volatile long A3 = 0;

    /** Result of FuncB1: sum(0..250) */
    public volatile long B1 = 0;

    /** Result of FuncB2: A1 + sum(0..200) */
    public volatile long B2 = 0;

    /** Result of FuncB3: A2 + sum(0..400) */
    public volatile long B3 = 0;
}
