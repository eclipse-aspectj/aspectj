import java.util.*;

/**
 * PR#479
 * A variant of Hunter Kelly's bug PR#479.  This
 * doesn't get his desired, but should compile.
 */
public class BindingArgumentsInWithincode {
    public static void main(String[] args) {
        org.aspectj.testing.Tester.check(true, "compiled");
    }
}

class C {
    public void someMethod(String s) {
        new ArrayList().add(s+":"+s);
    }
}
aspect A {

    pointcut top(String s):
        withincode(void someMethod(String)) && args(s);
    
    pointcut method(Object o):
        call(* java.util.List.add(Object)) && args(o);

    /*
     * Won't capture what we're after
     * but it should compile
     */
    before(String s, Object o): top(s) &&  method(o) {}
}
