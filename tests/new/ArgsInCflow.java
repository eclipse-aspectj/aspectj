
import org.aspectj.testing.*;

/** PR#659 name binding in around cflow containing cflowbelow */
public class ArgsInCflow {
    public static void main(String[] args) { 
        Tester.check(6==fact(3), "6==fact(3)");
        Tester.checkAllEvents();
    }

    static int fact(int x) {
        if (x<0) x = -x;
        if (x==0) return 1;
        else return x*fact(x-1);
    }
    static { 
        Tester.expectEvent("3-2");
        Tester.expectEvent("3-1");
        Tester.expectEvent("3-0");
    }
}

aspect Test {

    // picks any calls to fact.
    pointcut factCall(int n) : call(int fact(int)) && args(n);

    // picks parameter to the first and current fact calls
    before(int n, int firstN, int dummy) :
        factCall(n) 
        && cflow(factCall(firstN)
                 && !cflowbelow(factCall(dummy))) {
        Tester.event(firstN + "-" + n);
    }
}
