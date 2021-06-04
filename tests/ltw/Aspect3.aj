/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
