
import org.aspectj.testing.Tester;
import org.aspectj.lang.JoinPoint;

public aspect TraceMains {
    private static String className(JoinPoint.StaticPart jp) {
        return jp.getSignature().getDeclaringType().getName();
    }
    before() : execution(static void main(String[])) {
        Tester.event("before main " + className(thisJoinPointStaticPart));
    }
    // this event is submitted after the Tester does its check,
    // so it is ignored.
    after() returning: execution(static void main(String[])) {
        Tester.event("after main " + className(thisJoinPointStaticPart));
    }
    
}