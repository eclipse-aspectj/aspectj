
import org.aspectj.testing.*;

/** @testcase PR#647 abstract aspect used statically should not cause instantiation of advice or pointcut */
public abstract aspect AbstractAspectUsedStatically {
    public static void main (String[] args) {
        Tester.event("main");
        Tester.checkAllEvents();
    } 
    static {
        Tester.expectEvent("main");
    }
    
    before() : definePrivate() {
        Tester.check(false, "definePrivate");
    }

    /** private must be implemented in defining class */
    private pointcut definePrivate() : execution(void main(..));
}
