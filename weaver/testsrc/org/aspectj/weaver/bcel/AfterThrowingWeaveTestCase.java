/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import java.io.IOException;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ShadowMunger;

public class AfterThrowingWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public AfterThrowingWeaveTestCase(String name) {
		super(name);
	}

	public void testAfterThrowing() throws IOException {
		weaveTest(getStandardTargets(), "AfterThrowing", makeAdviceAll("afterThrowing"));
	}

	public void testAfterThrowingParam() throws IOException {
		BcelWorld world = new BcelWorld();

		ShadowMunger myMunger = BcelTestUtils.shadowMunger(world,
				"afterThrowing(): get(* *.out) -> static void Aspect.ajc_afterThrowing_field_get(java.lang.Throwable)",
				Advice.ExtraArgument);
		ShadowMunger cm = myMunger.concretize(ResolvedType.MISSING, world, null);

		weaveTest(getStandardTargets(), "AfterThrowingParam", cm);
	}

}
