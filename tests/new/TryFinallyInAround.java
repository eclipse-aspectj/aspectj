
import org.aspectj.testing.Tester;

// XXX broken in 1.1rc1, fixed in tree as of 4/22
/** @testcase try/finally in around advice (same as ...messy arounds?) */
public class TryFinallyInAround {
    public static void main(String[] args) {
        int i = new C().go();
        Tester.check(2 == i, "expected 2 got " + i);
    }
}

class C {
    int i = 1;
    int go() {
        dowork();
        return i;
    }
    void dowork() {
        i++;
    }
}

aspect A {
    Object around() : 
        within(C)       
        && !cflow(within(A)) 
        && !handler(*)
        && !preinitialization(new(..)) // 1.1
        && !initialization(new(..))
        {
            // no bug if reduced to proceed();
        try {
            return proceed();
        } finally {
            if (false) System.out.println("ok");
        }
    }
}
