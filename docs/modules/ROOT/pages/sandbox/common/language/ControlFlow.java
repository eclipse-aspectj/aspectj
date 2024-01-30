
package language;

public class ControlFlow {
    public static void main(String[] argList) {
        Fact.factorial(6);
    }
}

class Fact {
    static int factorial(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("negative: " + i);
        }
        if (i > 100) {
            throw new IllegalArgumentException("big: " + i);
        }
        return (i == 0 ? 1 : i * factorial(i-1));
    }
}

/**
 * Demonstrate recursive calls.
 * @author Erik Hilsdale
 */
aspect LogFactorial {
    // START-SAMPLE language-cflowRecursionBasic Pick out latest and original recursive call
    /** call to factorial, with argument */
    pointcut f(int i) : call(int Fact.factorial(int)) && args(i);

    /** print most-recent recursive call */
    before(int i, final int j) : f(i) && cflowbelow(f(j)) { 
        System.err.println(i + "-" + j);
    }

    /** print initial/topmost recursive call */
    before(int i, final int j) : f(i) 
        && cflowbelow(cflow(f(j)) && !cflowbelow(f(int))) { 
        System.err.println(i + "@" + j);
    }
    // END-SAMPLE language-cflowRecursionBasic
}

