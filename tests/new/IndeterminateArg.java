
import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

import java.util.Arrays;

/** @testcase PR#764 binding args with single indeterminate prefix and suffix */
public class IndeterminateArg {
    public static void main (String[] args) {
        Object o1 = new OObject("o1");
        Object o2 = new OObject("o2");
        Object o3 = new OObject("o3");
        String s1 = "s1";
        String s2 = "s2";
        String s3 = "s3";

        SClass c;   
        c = new SClass();   
        c = new SClass(s1);
        c = new SClass(s1, s2);
        c = new SClass(s1, s2, s3);
        c.f();
        c.f(s1);
        c.f(s1, s2);
        c.f(s1, s2, s3);

        OClass o;
        o = new OClass();   
        o = new OClass(o1);
        o = new OClass(s1);
        o = new OClass(o1, o2);
        o = new OClass(o1, s2);
        o = new OClass(s1, o2);
        o = new OClass(s1, s2);
        o = new OClass(o1, o2, o3);
        o = new OClass(o1, o2, s3);
        o = new OClass(o1, s2, o3);
        o = new OClass(o1, s2, s3);
        o = new OClass(s1, o2, o3);
        o = new OClass(s1, o2, s3);
        o = new OClass(s1, s2, o3);
        o = new OClass(s1, s2, s3);

        o.f();   
        o.f(o1);
        o.f(s1);
        o.f(o1, o2);
        o.f(o1, s2);
        o.f(s1, o2);
        o.f(s1, s2);
        o.f(o1, o2, o3);
        o.f(o1, o2, s3);
        o.f(o1, s2, o3);
        o.f(o1, s2, s3);
        o.f(s1, o2, o3);
        o.f(s1, o2, s3);
        o.f(s1, s2, o3);
        o.f(s1, s2, s3);

        Tester.checkEventsFromFile("IndeterminateArg.events");
    } 
}

class OObject {
    final String s;
    OObject(String s) { this.s = s; }
    public String toString() { return s; }
}

class T {
    static void e(String s) { 
        Tester.event(s); 
    }
}

interface C {}
class SClass implements C {
    SClass() { T.e("SClass()"); }
    SClass(String s1) { T.e("SClass(" + s1 + ")"); }
    SClass(String s1, String s2) { T.e("SClass(" + s1 + ", " + s2 + ")"); }
    SClass(String s1, String s2, String s3) { T.e("SClass(" + s1 + ", " + s2 + ", " + s3 + ")"); }

    void f() { T.e("SClass.f()"); }
    void f(String s1) { T.e("SClass.f(" + s1 + ")"); }
    void f(String s1, String s2) { T.e("SClass.f(" + s1 + ", " + s2 + ")"); }
    void f(String s1, String s2, String s3) { T.e("SClass.f(" + s1 + ", " + s2 + ", " + s3 + ")"); }
}

class OClass implements C {
    OClass() { T.e("OClass()"); }
    OClass(Object s1) { T.e("OClass(" + s1 + ")"); }
    OClass(Object s1, Object s2) { T.e("OClass(" + s1 + ", " + s2 + ")"); }
    OClass(Object s1, Object s2, Object s3) { T.e("OClass(" + s1 + ", " + s2 + ", " + s3 + ")"); }

    void f() { T.e("OClass.f()"); }
    void f(Object s1) { T.e("OClass.f(" + s1 + ")"); }
    void f(Object s1, Object s2) { T.e("OClass.f(" + s1 + ", " + s2 + ")"); }
    void f(Object s1, Object s2, Object s3) { T.e("OClass.f(" + s1 + ", " + s2 + ", " + s3 + ")"); }
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

    pointcut safe()                                : (call(C+.new(..))) ||
                                                        (call(* *.*(..)) && target(C)); 
    // XXX should encode slots, range: a0o1 = args(Object); a3o2Start = args(Object, Object, *)
    pointcut none()                                : args();
    pointcut o1()                                  : args(Object);
    pointcut o2()                                  : args(Object, Object);
    pointcut o3()                                  : args(Object, Object, Object);
    pointcut o1Start()                             : args(Object,*);
    pointcut o1End()                               : args(*,Object);
    pointcut o2Start()                             : args(Object, Object,*);
    pointcut o2End()                               : args(*,Object, Object);

    pointcut s1()                                  : args(String);
    pointcut s2()                                  : args(String, String);
    pointcut s3()                                  : args(String, String, String);
    pointcut s1Start()                             : args(String,*);
    pointcut s1End()                               : args(*,String);
    pointcut s2Start()                             : args(String, String,*);
    pointcut s2End()                               : args(*,String, String);

    // bind
    pointcut bo1(Object o1)                        : args(o1);
    pointcut bo2(Object o1, Object o2)             : args(o1, o2);
    pointcut bo3(Object o1, Object o2, Object o3)  : args(o1, o2, o3);
    pointcut bo1Start(Object o1)                   : args(o1,*);
    pointcut bo1End(Object o1)                     : args(*,o1);
    pointcut bo2Start(Object o1, Object o2)        : args(o1, o2,*);
    pointcut bo2End(Object o1, Object o2)          : args(*,o1, o2);

    pointcut bs1(String s1)                        : args(s1);
    pointcut bs2(String s1, String s2)             : args(s1, s2);
    pointcut bs3(String s1, String s2, String s3)  : args(s1, s2, s3);
    pointcut bs1Start(String s1)                   : args(s1,*);
    pointcut bs1End(String s1)                     : args(*,s1);
    pointcut bs2Start(String s1, String s2)        : args(s1, s2,*);
    pointcut bs2End(String s1, String s2)          : args(*,s1, s2);

    before()                                       : safe() && none()              { check ("none()", thisJoinPoint); }
    before()                                       : safe() && o1()                { check ("o1()", thisJoinPoint); }
    before()                                       : safe() && o2()                { check ("o2()", thisJoinPoint); }
    before()                                       : safe() && o3()                { check ("o3()", thisJoinPoint); }
    before()                                       : safe() && o1Start()           { check ("o1Start()", thisJoinPoint); }
    before()                                       : safe() && o1End()             { check ("o1End()", thisJoinPoint); }
    before()                                       : safe() && o2Start()           { check ("o2Start()", thisJoinPoint); }
    before()                                       : safe() && o2End()             { check ("o2End()", thisJoinPoint); }

    before()                                       : safe() && s1()                { check ("s1()", thisJoinPoint); }
    before()                                       : safe() && s2()                { check ("s2()", thisJoinPoint); }
    before()                                       : safe() && s3()                { check ("s3()", thisJoinPoint); }
    before()                                       : safe() && s1Start()           { check ("s1Start()", thisJoinPoint); }
    before()                                       : safe() && s1End()             { check ("s1End()", thisJoinPoint); }
    before()                                       : safe() && s2Start()           { check ("s2Start()", thisJoinPoint); }
    before()                                       : safe() && s2End()             { check ("s2End()", thisJoinPoint); }

    before(Object o1)                              : safe() && bo1(o1)             { check ("bo1()", thisJoinPoint); }
    before(Object o1, Object o2)                   : safe() && bo2(o1, o2)         { check ("bo2()", thisJoinPoint); }
    before(Object o1, Object o2, Object o3)        : safe() && bo3(o1, o2, o3)     { check ("bo3()", thisJoinPoint); }
    before(Object o1)                              : safe() && bo1Start(o1)        { check ("bo1Start()", thisJoinPoint); }
    before(Object o1)                              : safe() && bo1End(o1)          { check ("bo1End()", thisJoinPoint); }
    before(Object o1, Object o2)                   : safe() && bo2Start(o1, o2)    { check ("bo2Start()", thisJoinPoint); }
    before(Object o1, Object o2)                   : safe() && bo2End(o1, o2)      { check ("bo2End()", thisJoinPoint); }

    before(String s1)                              : safe() && bs1(s1)             { check ("bs1()", thisJoinPoint); }
    before(String s1, String s2)                   : safe() && bs2(s1, s2)         { check ("bs2()", thisJoinPoint); }
    before(String s1, String s2, String s3)        : safe() && bs3(s1, s2, s3)     { check ("bs3()", thisJoinPoint); }
    before(String s1)                              : safe() && bs1Start(s1)        { check ("bs1Start()", thisJoinPoint); }
    before(String s1)                              : safe() && bs1End(s1)          { check ("bs1End()", thisJoinPoint); }
    before(String s1, String s2)                   : safe() && bs2Start(s1, s2)    { check ("bs2Start()", thisJoinPoint); }
    before(String s1, String s2)                   : safe() && bs2End(s1, s2)      { check ("bs2End()", thisJoinPoint); }
}


