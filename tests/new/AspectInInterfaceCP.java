
import org.aspectj.testing.Tester;
public class AspectInInterfaceCP {
    public static void main(String[] args) {
        Tester.checkEvents("before");
    }
}

interface HasPrivateAspect {
    /** @testcase PR#534 static aspect in interface causes CE if - usejavac */
    static aspect Inner {
        before(): execution(* main(..)) { Tester.event("before"); }
    }
}
