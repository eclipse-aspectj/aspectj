

public class FactorialCflow {
    static int fact(int i) {
        return (i == 0 ? 1 : i * fact(i-1));
    }
    public static void main(String args[]) {
        System.err.println(expect);
        System.err.println("---------- actual ");
        System.err.println(720 == fact(6) ? "pass" : "fail");
    }

    static final String expect = "---------- expect "
        // most-recent
        + "\n5-6"
        + "\n4-5"
        + "\n3-4"
        + "\n2-3"
        + "\n1-2"
        + "\n0-1"
        // top
        + "\n5@6"
        + "\n4@6"
        + "\n3@6"
        + "\n2@6"
        + "\n1@6"
        + "\n0@6"
            ;
}

aspect A {
    pointcut f(int i) : call(int fact(int)) && args(i);

    // most-recent
    int around(int i, final int j) : f(i) && cflowbelow(f(j)) { 
        System.err.println(i + "-" + j);
        int r = proceed(i, j);
        return r;
    }

    // top
    int around(int i, final int j) : f(i) 
        && cflowbelow(cflow(f(j)) && !cflowbelow(f(int))) { 
        System.err.println(i + "@" + j);
        int r = proceed(i, j);
        return r;
    }
}
