
import org.aspectj.testing.Tester;
import java.util.*;

/** @testcase PR#771 static initializer member name */
public class StaticInitName {

    public static void main(String[] args) {
        Tester.expectEvent("C");
        Tester.event(""+C.class.getName());
        Tester.expectEvent("D");
        Tester.event(""+D.class.getName());
        Tester.checkAllEvents();
    }
}

class C {
    static int i = 1;
}

class D {
    static int i;
    static {
        i = 2;
    }
}

aspect A {
    static {
        Tester.expectEvent("before");
        Tester.expectEvent("before-D");
    }

    before() : within(C) && !set(* *) {
        Tester.event("before");
        String memberName = thisJoinPoint.getSignature().getName();
        Tester.check("<clinit>".equals(memberName),
                     "\"<clinit>\".equals(\"" + memberName + "\")");
    }

    before() : within(D) && !set(* *) {
        Tester.event("before-D");
        String memberName = thisJoinPoint.getSignature().getName();
        Tester.check("<clinit>".equals(memberName),
                     "\"<clinit>\".equals(\"" + memberName + "\")");
    }
}
