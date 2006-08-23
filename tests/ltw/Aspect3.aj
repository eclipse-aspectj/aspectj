/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Matthew Webster         initial implementation
 *******************************************************************************/
import org.aspectj.lang.JoinPoint;

public aspect Aspect3 {
	
	before () : execution(void Main.test999()) {
		System.err.println("Aspect1.before_" + thisJoinPoint.getSignature().getName());
	}

    // triggers noGuardForLazyTjp warning if that warning is enabled
	before(): call(* someNonExistentMethod(..)) {
		System.out.println(thisJoinPoint);
	}
}
