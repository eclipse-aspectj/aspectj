
package fluffy.bunny.rocks;

import foo.*;
import fluffy.*;
import fluffy.bunny.*;

public aspect UseThisAspectForLinkCheckToo {
	
	before(): execution(* *..*(..)) {
		System.err.println("yo");
	}
} 