import org.aspectj.testing.*;

/**
 * -usejavac mode: no error
 * not -usejavac mode: VerifyError
 */
public class ConstructorExecInitFails {
    public static void main(String[] args) {
    	try {
        	new ConstructorExecInitFails();
    	} catch (ExceptionInInitializerError e) {
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
