import org.aspectj.testing.Tester;

public class CircularAdvice {
    public static void main(String[] args) {
        Tester.checkEqual(m(5), 5*4*3*2*1, "factorial with advice");
    }

    public static long m(long l) {
        return -1;
    }
}

aspect FactorialViaAround {
    // this advice uses recursive calls within its body to compute factorial
    // on an otherwise innocent method
    long around (long l): call(long m(long)) && args(l) {
        if (l == 0) {
            return 1;
        } else {
            return l * CircularAdvice.m(l - 1);
        }
    }
}

