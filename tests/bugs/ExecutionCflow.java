
import org.aspectj.testing.Tester;
import java.util.*;

/** @testcase PR#903 cflow of execution */
public class ExecutionCflow {

	static int field;
	
    public static void main(String[] args) {
		field = 0;
		Tester.expectEvent("before");
		Tester.checkAllEvents();
    }
}

aspect A {
    before() : cflow(execution(static void main(String[]))) 
    	&& set(int field) {
		Tester.event("before");
    }
}
