
import org.aspectj.testing.*;
import org.aspectj.lang.*;

/** @rfe our SourceLocation implementation should implement toString as filename:column */
public class SourceLocationToString {
    public static final String EXPECT = "SourceLocationToString.java:9";
    public static void main (String[] args) {
        docall();                  // line 9
        Tester.checkAllEvents();
    } 
    static {
        Tester.expectEvent("docall");
    }
    static void docall() {
        Tester.event("docall");
    }
    static aspect A {
        before () : call(void docall()) {
            Tester.event("before");
            String sl = thisJoinPoint.getSourceLocation().toString();
            Tester.check(sl.endsWith(EXPECT),
                         "sl=\"" + sl + "\" did not end with \""
                         + EXPECT + "\"");
        }
    }
    
}
 
