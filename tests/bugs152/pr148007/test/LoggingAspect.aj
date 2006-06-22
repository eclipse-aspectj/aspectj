package test;

public aspect LoggingAspect {
	
	pointcut logPointcut() :
		execution (* *(..))
        && within (test..*)
		&& !within(LoggingAspect);
	
	before() : logPointcut() {
			System.out.println("entering");
	}

    after() : logPointcut() {
            System.out.println("exiting");
    }
}
