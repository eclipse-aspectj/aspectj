
import org.aspectj.testing.Tester;
public class AspectInInterfaceCF {
    public static void main(String[] args) {
        Tester.checkEvents("before");
    }
}

interface HasPrivateAspect {
    /** @testcase PR#534 static aspect in interface causes CE if - usejavac */
    private static aspect Inner {
        before(): execution(* main(..)) { Tester.event("before"); }
    }
    protected static aspect Inner1 {
        before(): execution(* main(..)) { Tester.event("before"); }
    }
}
