
package org.aspectj.weaver;

import org.aspectj.weaver.*;
import org.aspectj.weaver.bcel.*;
import org.apache.bcel.generic.*;

/**
 * Enforces the use of BCEL API use.
 */
public aspect BcelApiRules {
	
	/**
	 * The Utility method needs to be used instead of the BCEL method doue to a bug
	 * in the implementation of Instruction.copy()
	 */
	declare error:
		call(* Instruction.copy()) && within(org.aspectj.weaver..*)
			 && !withincode(* Utility.copyInstruction(Instruction)):
		"use Utility.copyInstruction to work-around bug in Select.copy()";


}
   