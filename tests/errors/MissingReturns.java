
import org.aspectj.testing.Tester;

// PR#138, PR#139
// error message could be more informatinve (PR#139)

aspect MissingReturns {

    int baz(int a) { return 1; }
    
    void around(): this(MissingReturns) && call(int baz(int)) {
    // SHOULD BE: 
    //    static advice() returns int: MissingReturns && int baz(int) {
	       return proceed();
    }
    
    pointcut cut(): this(MissingReturns) && call(int baz(int));
    void around(): cut() {
	        proceed();
	        return 2;
    }
}
