
package library1;

import org.aspectj.lang.JoinPoint;
import org.aspectj.testing.Tester;

public aspect Library1 {
    pointcut targetJoinPoints() : 
        //execution(public static void main(String[]));
        execution(public static void main(..));
    
    before() : targetJoinPoints() {
        Tester.event("before " + renderId(thisJoinPoint));
    }

    before() : targetJoinPoints() {
        Tester.event("after " + renderId(thisJoinPoint));
    }

    protected String renderId(JoinPoint jp) {
        return jp.getSignature().getName();
    }
}