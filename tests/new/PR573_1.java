
import org.aspectj.testing.Tester;

/** @testcase PR#573 pertarget stack overflow getting name of anonymous class */
public class PR573_1 { 
    static public void main(String[] params) {
        final Object o1 = new Object();
        final Object o = new Object() {
            public void m() {
                o1.toString(); 
            }};
        Tester.expectEvent("A.init0");
        Tester.check(null != o, "null != o");
        o.toString(); // no exceptions
        Tester.check(1 == A.num, "1 == A.num: " + A.num);
        Tester.checkAllEvents();
    }
}
// different stack overflow when using Object, not Interface
aspect A pertarget(target(Object) && !target(A)) {  // was a warning in 1.0 
    public static int num;
    A(){ Tester.event("A.init" + num++); }
}
