import org.aspectj.testing.Tester; 

/** @testcase PR#619 direct use outside aspect of defined abstract pointcut */
public abstract aspect AbstractPointcutAccess { 
    public static void main (String[] args) {
        Tester.event("main");
        Tester.checkAllEvents();
    } 
    static {
        Tester.expectEvent("main");
        Tester.expectEvent("used");
    }
    
    abstract pointcut abstractPointcut();
} 
aspect ConcretePointcutAccess extends AbstractPointcutAccess {
    pointcut abstractPointcut() : execution(void main(..));
}

aspect AbstractPointcutUser { 
    before () : ConcretePointcutAccess.abstractPointcut() {
        Tester.event("used");
    }
}

