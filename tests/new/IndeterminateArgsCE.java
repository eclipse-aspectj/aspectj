
import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

import java.util.Arrays;

/** @testcase PR#764 binding args with indeterminate prefix and suffix */
public class IndeterminateArgsCE {
    public static void main (String[] args) {
        STarget c;   
        c = new STarget();   
        c = new STarget("s1");
        c = new STarget("s1", "s2");
        c = new STarget("s1", "s2", "s3");
        c.f();
        c.f("s1");
        c.f("s1", "s2");
        c.f("s1", "s2", "s3");
        OTarget o;   
        o = new OTarget();   
        o = new OTarget("o1");
        o = new OTarget("o1", "o2");
        o = new OTarget("o1", "o2", "o3");
        o.f();
        o.f("o1");
        o.f("o1", "o2");
        o.f("o1", "o2", "o3");
    } 
}

class OTarget {
    OTarget() { }
    OTarget(String s1) { }
    OTarget(String s1, Object s2) { }                      // CE
    OTarget(String s1, Object s2, String s3) { }           // CE

    void f() { }
    void f(String s1) { }
    void f(String s1, Object s2) { }                 // CE
    void f(String s1, Object s2, String s3) { }      // CE
}
class STarget {
    STarget() { }
    STarget(String s1) { }
    STarget(String s1, String s2) { }                      // CE
    STarget(String s1, String s2, String s3) { }

    void f() { }
    void f(String s1) { }
    void f(String s1, String s2) { }                 // CE
    void f(String s1, String s2, String s3) { }
}

aspect A {

    void check(String pc, JoinPoint jp) {
        Class[] types = ((CodeSignature) jp.getSignature()).getParameterTypes();
        //T.e(pc + ": " + Arrays.asList(types));
    }

    pointcut safeS()                               : target(STarget) && (call(new(..)) || call(* *(..))); 
    pointcut safeO()                               : target(OTarget) && (call(new(..)) || call(* *(..))); 

    pointcut o1Anywhere()                          : args(.., Object, ..);
    pointcut s1Anywhere()                          : args(.., String, ..);
    pointcut bo1Anywhere(Object o)                 : args(.., o, ..);
    pointcut bs1Anywhere(String s)                 : args(.., s, ..);

    before()                                       : safeO() && o1Anywhere()        { check ("o1Anywhere()", thisJoinPoint); }
    before(Object o1)                              : safeO() && bo1Anywhere(o1)     { check ("bo1Anywhere(o1)", thisJoinPoint); }
    before()                                       : safeS() && s1Anywhere()        { check ("s1Anywhere()", thisJoinPoint); }
    before(String s1)                              : safeS() && bs1Anywhere(s1)     { check ("bs1Anywhere(s1)", thisJoinPoint); }
}


