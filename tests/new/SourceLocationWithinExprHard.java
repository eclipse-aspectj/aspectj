
import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

/** @testcase PR#885 call source locations within expression */
public class SourceLocationWithinExprHard {
    public static void main (String[] args) {
        new                  // 9*
            C()              // 10
            .                // 11
            getD()           // 12*
            .                // 13
            getE()           // 14*
            .                // 15
            getF()           // 16*
            ;
        Tester.expectEvent("setup");
        Tester.checkAllEvents();
    } 
}
class C { D getD() { return new D(); } }
class D { E getE() { return new E(); } }
class E { F getF() { return new F(); } }
class F { }

aspect A {
    private static final String SEP = " - ";
    static {
        // using qualifying expr?
        Tester.expectEvent("C()" + SEP + "9");
        Tester.expectEvent("getD()" + SEP + "12");
        Tester.expectEvent("getE()" + SEP + "14");
        Tester.expectEvent("getF()" + SEP + "16");
        Tester.event("setup");
    }
    pointcut filter() : withincode(static void SourceLocationWithinExpr.main(String[]));
    before() : filter() && call(C.new()) { signal("C()", thisJoinPoint); }
    before() : filter() && call(D C.getD()) { signal("getD()", thisJoinPoint); }
    before() : filter() && call(E D.getE()) { signal("getE()", thisJoinPoint); }
    before() : filter() && call(F E.getF()) { signal("getF()", thisJoinPoint); }
    void signal(String prefix, JoinPoint jp) {
        SourceLocation sl = jp.getSourceLocation();
        System.out.println(prefix + SEP + sl.getLine());
        Tester.event(prefix + SEP + sl.getLine());
    }
}
