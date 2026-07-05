/**
 * Utility class providing mathematical helper methods for thread computations.
 *
 * @author Rayhan
 * @version 1.0
 */
public class MathUtils {

    /**
     * Computes the sum of integers from 0 to n (inclusive) using a loop.
     * Formula equivalent: n * (n + 1) / 2
     *
     * @param n Upper bound of summation (inclusive)
     * @return  Sum of 0 + 1 + 2 + ... + n
     */
    public static long sum(int n) {
        long result = 0;
        for (int i = 0; i <= n; i++) {
            result += i;
        }
        return result;
    }
}
