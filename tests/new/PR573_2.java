
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

/** @testcase PR#573 pertarget stack overflow getting name of anonymous class (runtime overflow) */
public class PR573_2 { 
    static public void main(String[] params) {
        final Object o = new Object() {
            public void m() { }};
        Tester.expectEvent("A.init0");
        Tester.check(null != o, "null != o");
        Tester.check(1 == A.num, "1 == A.num: " + A.num);
        Tester.checkAllEvents();
    }
}
// different stack overflow when using Object, not Interface
//aspect A pertarget(target(Object)) {  
aspect A pertarget(target(Object) && !target(A)) {  
    public static int num;
    A(){ Tester.event("A.init" + num++); }
}
