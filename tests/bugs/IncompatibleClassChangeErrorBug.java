
import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

/** @testcase PR#901 IncompatibleClassChangeError bug  */
public class IncompatibleClassChangeErrorBug {

    public static void main(String[] args) {
		Tester.expectEvent("printed");
        method1();
        Tester.checkAllEvents();
    }
    public static void method1() {
    }
}

aspect JoinpointTestAspect {
    before() : call(static void method1()) {
        printArgs(thisJoinPoint);
    
		// This call is required to reproduce the bug...
        printStaticInfo(thisJoinPointStaticPart);
    }

    
    private void printArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
	// While the original code had a for() loop to print arguments
	// bug can be seen without it...
    }
    
    private void printStaticInfo(JoinPoint.StaticPart 
				 joinPointStaticPart) {
		Tester.check(null != joinPointStaticPart, "null parm");
		Tester.event("printed");
    }
}
