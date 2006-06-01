/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC      initial implementation 
 *     AMC 01.20.2003  extended to support new AspectJ 1.1 options,
 * 				       bugzilla #29769
 * ******************************************************************/

package org.aspectj.weaver;

import org.aspectj.weaver.*;
import org.aspectj.weaver.bcel.*;
import org.apache.bcel.generic.*;

/**
 * Enforces the correct use of BCEL APIs.
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
   