/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.util.*;
import java.io.*;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.aspectj.weaver.patterns.*;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.*;

/**.
 */
public class WeaveOrderTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public WeaveOrderTestCase(String name) {
		super(name);
	}
    

	public void testLexicalOrder() {
		Advice a1 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.OBJECT, 1);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.THROWABLE, 2);
		
		assertEquals(-1, a2.compareTo(a1));
		assertEquals(+1, a1.compareTo(a2));
	}

	public void testLexicalOrderWithAfter() {
		Advice a1 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.OBJECT, 1);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.After, TypeX.OBJECT, TypeX.THROWABLE, 2);
		
		assertEquals(+1, a2.compareTo(a1));
		assertEquals(-1, a1.compareTo(a2));

		a1 =
			makeConcreteAdvice(AdviceKind.After, TypeX.OBJECT, TypeX.OBJECT, 1);
		a2 =
			makeConcreteAdvice(AdviceKind.After, TypeX.OBJECT, TypeX.THROWABLE, 2);
		
		assertEquals(+1, a2.compareTo(a1));
		assertEquals(-1, a1.compareTo(a2));
	}
	
	public void testSubtypes() {
		Advice a1 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.OBJECT, 1);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.THROWABLE, TypeX.OBJECT, 1);
		Advice a3 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.forName("java.lang.String"), TypeX.OBJECT, 1);
			
		assertEquals(+1, a2.compareTo(a1));
		assertEquals(-1, a1.compareTo(a2));

		assertEquals(+1, a3.compareTo(a1));
		assertEquals(-1, a1.compareTo(a3));

		assertEquals(0, a3.compareTo(a2));
		assertEquals(0, a2.compareTo(a3));
	}


	public void testDominates() {
		Declare dom =
			new PatternParser("declare precedence: java.lang.String, java.lang.Throwable").parseDeclare();
		//??? concretize dom
		ResolvedTypeX aType =  world.resolve("Aspect");
		CrosscuttingMembers xcut = new CrosscuttingMembers(aType);
		aType.crosscuttingMembers = xcut;
		xcut.addDeclare(dom);
		world.getCrosscuttingMembersSet().addFixedCrosscuttingMembers(aType);
		
		Advice a1 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.OBJECT, 1);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.THROWABLE, 2);
		Advice a3 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.forName("java.lang.String"), 2);
		
		assertEquals(-1, a2.compareTo(a1));
		assertEquals(+1, a1.compareTo(a2));

		assertEquals(-1, a3.compareTo(a1));
		assertEquals(+1, a1.compareTo(a3));
		
		
		assertEquals(+1, a3.compareTo(a2));
		assertEquals(-1, a2.compareTo(a3));
	}
	
	public void testDominatesHarder() {
		Declare dom =
			new PatternParser("declare precedence: *, java.lang.String, java.lang.Throwable").parseDeclare();
		//??? concretize dom
		ResolvedTypeX aType =  world.resolve("Aspect");
		CrosscuttingMembers xcut = new CrosscuttingMembers(aType);
		aType.crosscuttingMembers = xcut;
		xcut.addDeclare(dom);
		world.getCrosscuttingMembersSet().addFixedCrosscuttingMembers(aType);
		
		Advice a1 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.OBJECT, 2);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.THROWABLE, 1);
		Advice a3 =
			makeConcreteAdvice(AdviceKind.Before, TypeX.OBJECT, TypeX.forName("java.lang.String"), 1);
		
		assertEquals(-1, a2.compareTo(a1));
		assertEquals(+1, a1.compareTo(a2));

		assertEquals(-1, a3.compareTo(a1));
		assertEquals(+1, a1.compareTo(a3));
		
		
		assertEquals(+1, a3.compareTo(a2));
		assertEquals(-1, a2.compareTo(a3));
	}
	
	


	private Advice makeConcreteAdvice(AdviceKind kind, TypeX declaringAspect, 
				TypeX concreteAspect, int lexicalPosition)
	{
		Advice a1 = new BcelAdvice(kind, makeResolvedPointcut("this(*)"),  
				Member.method(declaringAspect, 0, "foo", "()V"),
				0, lexicalPosition, lexicalPosition, null, null);
		a1 = (Advice)a1.concretize(concreteAspect.resolve(world), world, null);
		return a1;
	}
	
	
	
}
