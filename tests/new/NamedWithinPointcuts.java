
import org.aspectj.testing.Tester; 

public class NamedWithinPointcuts {
    public static void main (String[] args) { 
        Tester.expectEvent("before"); 
        Tester.checkAllEventsIgnoreDups();
    } 
}

aspect Test {
    pointcut withinAspects() : within(Test) ;

    static void log() { }

    /** @testcase PR#635 Named Within pointcuts failing */
    //before() : !within(Test) {    // works fine
    before() : !(withinAspects()) { // stack overflow
        log();     // comment out to avoid stack overflow
        Tester.event("before");
    }
}
