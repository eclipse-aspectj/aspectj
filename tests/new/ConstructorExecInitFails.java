import org.aspectj.testing.*;
import org.aspectj.lang.*;

/**
 * -usejavac mode: no error
 * not -usejavac mode: VerifyError
 */
public class ConstructorExecInitFails {
    public static void main(String[] args) {
    	try {
        	new ConstructorExecInitFails();
    	} catch (NoAspectBoundException e) {
    		
    		Tester.check(e.getCause() instanceof NoAspectBoundException,
    				"Expected NoAspectBoundException, found " + e.getCause());
    		return;
    	}
        Tester.checkFailed("shouldn't be able to run");
    }
}

/** @testcase after returning from initialization and after executing constructor */
aspect A {
    after (Object target) : execution(*.new(..)) && target(target) { 
        Tester.checkFailed("shouldn't be able to run");
    }
    after () returning (Object target) : initialization(new(..)) { 
        Tester.checkFailed("shouldn't be able to run");
    }
}
