
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

/** @testcase PR#573 pertarget stack overflow getting name of anonymous class */
public class PR573 { 
    static public void main(String[] params) {
        Tester.expectEvent("A.init0");
        final Object o = new Interface() {
            public void m(Object oa) {
                oa.toString(); 
            }};
        Tester.check(null != o, "null != o");
        ((Interface) o).m("hi"); // no exceptions
        Tester.check(1 == A.num, "1 == A.num: " + A.num);
        Tester.checkAllEvents();
    }
}
interface Interface { void m(Object o);}

aspect A pertarget(target(Interface)) { // was a warning in 1.0
    public static int num;
    A(){ Tester.event("A.init" + num++); }
}

