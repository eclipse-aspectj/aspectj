
import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

import java.util.Arrays;

/** @testcase PR#764 binding args with indeterminate prefix and suffix */
public class IndeterminateArgs {
    public static void main (String[] args) {
        C c;   
        c = new C();   
        c = new C("s1");
        c = new C("s1", "s2");
        c = new C("s1", "s2", "s3");
        c.f();
        c.f("s1");
        c.f("s1", "s2");
        c.f("s1", "s2", "s3");
        Tester.checkEventsFromFile("IndeterminateArgs.events");
    } 
}
class T {
    static void e(String s) { 
        Tester.event(s); 
    }
    static void ee(String s) { Tester.expectEvent(s); }
}

class C {
    C() { T.e("C()"); }
    C(String s1) { T.e("C(" + s1 + ")"); }
    C(String s1, String s2) { T.e("C(" + s1 + ", " + s2 + ")"); }
    C(String s1, String s2, String s3) { T.e("C(" + s1 + ", " + s2 + ", " + s3 + ")"); }

    void f() { T.e("f()"); }
    void f(String s1) { T.e("f(" + s1 + ")"); }
    void f(String s1, String s2) { T.e("f(" + s1 + ", " + s2 + ")"); }
    void f(String s1, String s2, String s3) { T.e("f(" + s1 + ", " + s2 + ", " + s3 + ")"); }
}

aspect A {

    void check(String pc, JoinPoint jp) {
        Class[] types = ((CodeSignature) jp.getSignature()).getParameterTypes();
        T.e(pc + ": " + Arrays.asList(types));
    }

    pointcut safe()                                : target(C) && (call(new(..)) || call(* *(..))); 

    pointcut o1()                                  : args(Object);
    pointcut o2()                                  : args(Object, Object);
    pointcut o1Start()                             : args(Object,..);
    pointcut o1End()                               : args(..,Object);
    pointcut o2Start()                             : args(Object, Object,..);
    pointcut o2End()                               : args(..,Object, Object);

    pointcut s1()                                  : args(String);
    pointcut s2()                                  : args(String, String);
    pointcut s1Start()                             : args(String,..);
    pointcut s1End()                               : args(..,String);
    pointcut s2Start()                             : args(String, String,..);
    pointcut s2End()                               : args(..,String, String);

    // bind
    pointcut bo1(Object o1)                        : args(o1);
    pointcut bo2(Object o1, Object o2)             : args(o1, o2);
    pointcut bo1Start(Object o1)                   : args(o1,..);
    pointcut bo1End(Object o1)                     : args(..,o1);
    pointcut bo2Start(Object o1, Object o2)        : args(o1, o2,..);
    pointcut bo2End(Object o1, Object o2)          : args(..,o1, o2);

    pointcut bs1(String s1)                        : args(s1);
    pointcut bs2(String s1, String s2)             : args(s1, s2);
    pointcut bs1Start(String s1)                   : args(s1,..);
    pointcut bs1End(String s1)                     : args(..,s1);
    pointcut bs2Start(String s1, String s2)        : args(s1, s2,..);
    pointcut bs2End(String s1, String s2)          : args(..,s1, s2);

    before()                                       : safe() && o1()                { check ("o1()", thisJoinPoint); }
    before()                                       : safe() && o2()                { check ("o2()", thisJoinPoint); }
    before()                                       : safe() && o1Start()           { check ("o1Start()", thisJoinPoint); }
    before()                                       : safe() && o1End()             { check ("o1End()", thisJoinPoint); }
    before()                                       : safe() && o2Start()           { check ("o2Start()", thisJoinPoint); }
    before()                                       : safe() && o2End()             { check ("o2End()", thisJoinPoint); }

    before()                                       : safe() && s1()                { check ("s1()", thisJoinPoint); }
    before()                                       : safe() && s2()                { check ("s2()", thisJoinPoint); }
    before()                                       : safe() && s1Start()           { check ("s1Start()", thisJoinPoint); }
    before()                                       : safe() && s1End()             { check ("s1End()", thisJoinPoint); }
    before()                                       : safe() && s2Start()           { check ("s2Start()", thisJoinPoint); }
    before()                                       : safe() && s2End()             { check ("s2End()", thisJoinPoint); }

    before(Object o1)                              : safe() && bo1(o1)             { check ("bo1()", thisJoinPoint); }
    before(Object o1, Object o2)                   : safe() && bo2(o1, o2)         { check ("bo2()", thisJoinPoint); }
    before(Object o1)                              : safe() && bo1Start(o1)        { check ("bo1Start()", thisJoinPoint); }
    before(Object o1)                              : safe() && bo1End(o1)          { check ("bo1End()", thisJoinPoint); }
    before(Object o1, Object o2)                   : safe() && bo2Start(o1, o2)    { check ("bo2Start()", thisJoinPoint); }
    before(Object o1, Object o2)                   : safe() && bo2End(o1, o2)      { check ("bo2End()", thisJoinPoint); }

    before(String s1)                              : safe() && bs1(s1)             { check ("bs1()", thisJoinPoint); }
    before(String s1, String s2)                   : safe() && bs2(s1, s2)         { check ("bs2()", thisJoinPoint); }
    before(String s1)                              : safe() && bs1Start(s1)        { check ("bs1Start()", thisJoinPoint); }
    before(String s1)                              : safe() && bs1End(s1)          { check ("bs1End()", thisJoinPoint); }
    before(String s1, String s2)                   : safe() && bs2Start(s1, s2)    { check ("bs2Start()", thisJoinPoint); }
    before(String s1, String s2)                   : safe() && bs2End(s1, s2)      { check ("bs2End()", thisJoinPoint); }
}


