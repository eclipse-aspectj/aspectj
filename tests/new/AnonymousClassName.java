
import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

/** @testcase declaring type in signature of anonymous class */
public class AnonymousClassName {
    public static void main(String[] args) {
        Tester.expectEvent("run()");
        new Runnable(){
                public void run() { 
                    Tester.event("run()");
                }
            }.run();
        Tester.checkAllEvents(); 
    }
}

aspect A {
    static {
        Tester.expectEvent("before constructRunnable");
        Tester.expectEvent("before callRun");
        Tester.expectEvent("before executeRun");
        Tester.expectEvent("before initRunnable");
        Tester.expectEvent("around constructRunnable");
        Tester.expectEvent("around callRun");
        Tester.expectEvent("around executeRun");
        Tester.expectEvent("around initRunnable");
        Tester.expectEvent("after constructRunnable");
        Tester.expectEvent("after callRun");
        Tester.expectEvent("after executeRun");
        Tester.expectEvent("after initRunnable");
    }
    static final int Runnable+.provokeStaticInit = 1;

    pointcut constructRunnable () : call(Runnable+.new());
    pointcut initRunnable () : staticinitialization(Runnable+);
    pointcut callRun () : target(Runnable) && call(void run());
    pointcut executeRun () : target(Runnable) && execution(void run());
    before () : constructRunnable() { check("before constructRunnable", thisJoinPoint); }
    before () : callRun() { check("before callRun", thisJoinPoint); }
    before () : executeRun() { check("before executeRun", thisJoinPoint); }
    before () : initRunnable() { check("before initRunnable", thisJoinPoint); }
    Object around () : constructRunnable() { 
        check("around constructRunnable", thisJoinPoint); 
        return proceed(); 
    }
    Object around () : callRun() { 
        check("around callRun", thisJoinPoint); 
        return proceed(); 
    }
    Object around () : executeRun() { 
        check("around executeRun", thisJoinPoint); 
        return proceed(); 
    }
    Object around () : initRunnable() { 
        check("around initRunnable", thisJoinPoint); 
        return proceed(); 
    }

    after () : constructRunnable() { check("after constructRunnable", thisJoinPoint); }
    after () : callRun() { check("after callRun", thisJoinPoint); }
    after () : executeRun() { check("after executeRun", thisJoinPoint); }
    after () : initRunnable() { check("after initRunnable", thisJoinPoint); }

    void check(String event, JoinPoint jp) {
        Tester.event(event);
        //System.err.println(event + ": " + jp.toLongString());
        Signature sig = jp.getSignature();
        Class c = sig.getDeclaringType();
        Tester.check(null != c, event + " null class");
        Tester.check(ClassNotFoundException.class != c, 
                     event + " got: " + c);
        String name = "" + sig;
        Tester.check(-1 != name.indexOf("AnonymousClassName"), 
                     event + " expecting AnonymousClassName..: " + name);
    }
}

