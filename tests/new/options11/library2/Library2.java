
package library2;

import org.aspectj.lang.JoinPoint;
import org.aspectj.testing.Tester;

public abstract aspect Library2 {
    public abstract pointcut targetJoinPoints();
    
    before() : targetJoinPoints() {
        Tester.event("before 2 " + renderId(thisJoinPoint));
    }

    before() : targetJoinPoints() {
        Tester.event("after 2 " + renderId(thisJoinPoint));
    }
    protected String renderId(JoinPoint jp) {
        return jp.getSignature().getName();
    }
}