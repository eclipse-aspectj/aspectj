
package foo;

import fluffy.*;
import fluffy.bunny.*;
import fluffy.bunny.rocks.*;

public aspect UseThisAspectForLinkCheck {
	
	int foo;
	
	pointcut allExecutions(): execution(* *..*(..));
	
	before(): allExecutions() {
		System.err.println("yo");
	}
 
	after(): allExecutions() {
		System.err.println("go");
	}
} 