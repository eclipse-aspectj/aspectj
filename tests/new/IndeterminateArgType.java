
import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

import java.util.Arrays;

/** @testcase PR#764 check arg types with indeterminate prefix and suffix */
public class IndeterminateArgType {
    public static void main (String[] args) {
        OTarget o;   
        STarget c;   
        // both pointcuts pick out args 1..3; neither picks out args 0
        // both actual Object and String match
        c = new STarget();
        c = new STarget("s1");
        c = new STarget("s1", "s2");
        c = new STarget("s1", "s2", "s3");
        c.f();
        c.f("s1");
        c.f("s1", "s2");
        c.f("s1", "s2", "s3");
        // String pointcut should match even though declared type is Object
        o = new OTarget();
        o = new OTarget("o1");
        o = new OTarget("o1", "o2");
        o = new OTarget("o1", "o2", "o3");
        o.f();
        o.f("o1");
        o.f("o1", "o2");
        o.f("o1", "o2", "o3");

        // only actual Object types match these
        Object input = new Object();
        o = new OTarget();   
        o = new OTarget(input);
        o = new OTarget(input, input);
        o = new OTarget(input, input, input);
        o.f();
        o.f(input);
        o.f(input, input);
        o.f(input, input, input);

        Tester.checkEventsFromFile("IndeterminateArgType.events");
    } 
}

interface Safe {}
class OTarget implements Safe {
    OTarget() { }
    OTarget(Object s1) { }
    OTarget(Object s1, Object s2) { }
    OTarget(Object s1, Object s2, Object s3) { }

    void f() { }
    void f(Object s1) { }
    void f(Object s1, Object s2) { }
    void f(Object s1, Object s2, Object s3) { }
}

class STarget implements Safe {
    STarget() { }
    STarget(String s1) { }
    STarget(String s1, String s2) { }
    STarget(String s1, String s2, String s3) { }

    void f() { }
    void f(String s1) { }
    void f(String s1, String s2) { }
    void f(String s1, String s2, String s3) { }
}

class T {
    static void e(String s) { 
        Tester.event(s); 
    }
}

aspect A {

    String actualTypes(JoinPoint jp) { // XXX gather as utility
        Object[] types = jp.getArgs();
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < types.length; i++) {
            sb.append(types[i].getClass().getName());
            if ((1+i) < types.length) {
                sb.append(", ");
            }
        } 
        sb.append("]");
        return sb.toString();
    }
    void check(String pc, JoinPoint jp) {
        Class[] types = ((CodeSignature) jp.getSignature()).getParameterTypes();
        String name = jp.toLongString() + " " + actualTypes(jp) + ": " + pc;
        T.e(name);
    }

    pointcut safe()                                : (call(Safe+.new(..))) ||
                                                        (call(* *.*(..)) && target(Safe)); 

    pointcut o1End()                               : args(.., Object);
    pointcut s1End()                               : args(.., String);
    pointcut o1Start()                             : args(Object, ..);
    pointcut s1Start()                             : args(String, ..);

    before()                                       : safe() && o1Start()   { check ("o1Start()", thisJoinPoint); }
    before()                                       : safe() && o1End()     { check ("o1End()", thisJoinPoint); }
    before()                                       : safe() && s1Start()   { check ("s1Start()", thisJoinPoint); }
    before()                                       : safe() && s1End()     { check ("s1End()", thisJoinPoint); }
}


