import org.aspectj.testing.*;

/**
 * with -usejavac: cannot resolve symbol
 * without -usejavac: VerifyError
 */
public aspect AroundDoubleAssignment {
    public static void main( String[] args ){
        Tester.expectEvent("test");
        Tester.expectEvent("proceed");
        StaticSet.test();
        Tester.checkAllEvents();
    }
    Object around() : execution( * StaticSet.*() ) {
        Tester.event("proceed");
        return proceed();
    }
}

class StaticSet {
    /** @testcase PR#687 around all execution with double assignment in initializer (simple) */
    public static void test(){
        String s = s = "test";
        Tester.event("test");
    }
}
