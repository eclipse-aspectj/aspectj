import org.aspectj.testing.*;

/*
 * Advice is not getting the calledMethodName.
 */

public class NoCalledMethodName {
    public static void main(String[] args) {
        new NoCalledMethodName().go(args);
    }

    void go(String[] args) {
        Tester.check("Go was called");
    }
}

aspect NoCalledMethodNameAspect of eachobject(instanceof(NoCalledMethodName)) {

    pointcut p2(NoCalledMethodName f): receptions(void go(..)) && instanceof(f);
    
    around(NoCalledMethodName f) returns void: p2(f) {
        String s = thisJoinPoint.methodName;
    }
}
