import org.aspectj.testing.*;
import java.util.*;

public class StaticMethodsShouldNotReceiveInstanceofAdvice {
    public static void main(String[] args) {
        ClassWithStaticMethods.staticMethod();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEventsInString("good0");
    }
}

class ClassWithStaticMethods {
    public static void staticMethod() {}
}


aspect PutsAdviceOnStaticMethods {
    
    static void bad(Object msg) { Tester.check(false, "Shouldn't have seen: " + msg); }
    static void good(String msg) { Tester.event(msg); }
    
    // These shouldn't run
    pointcut bad0(): this(ClassWithStaticMethods) && execution(void staticMethod());
    pointcut bad1(ClassWithStaticMethods c): this(c) && execution(void staticMethod());
    pointcut bad2(): target(ClassWithStaticMethods) && call(void staticMethod());
    pointcut bad3(ClassWithStaticMethods c): target(c) && call(void staticMethod());
    pointcut bad4(): target(*) && call(void ClassWithStaticMethods.staticMethod());
    pointcut bad5(ClassWithStaticMethods c): target(c) && call(void staticMethod());

    before(): bad0() { bad("bad0:" + thisJoinPoint); }
    before(ClassWithStaticMethods c): bad1(c) { bad("bad1:" + thisJoinPoint); }
    before(): bad2() { bad("bad2:" + thisJoinPoint); }
    before(ClassWithStaticMethods c): bad3(c) { bad("bad3:" + thisJoinPoint); }
    before(): bad4() { bad("bad4:" + thisJoinPoint); }
    before(ClassWithStaticMethods c): bad5(c) { bad("bad5:" + thisJoinPoint); }     

    // This should run
    pointcut good0(): execution(void ClassWithStaticMethods.staticMethod());

    before(): good0() { good("good0"); }

}
