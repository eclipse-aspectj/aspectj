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

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.CrosscuttingMembers;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PatternParser;

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
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.OBJECT, 1);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.THROWABLE, 2);
		
		assertEquals(-1, a2.compareTo(a1));
		assertEquals(+1, a1.compareTo(a2));
	}

	public void testLexicalOrderWithAfter() {
		Advice a1 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.OBJECT, 1);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.After, UnresolvedType.OBJECT, UnresolvedType.THROWABLE, 2);
		
		assertEquals(+1, a2.compareTo(a1));
		assertEquals(-1, a1.compareTo(a2));

		a1 =
			makeConcreteAdvice(AdviceKind.After, UnresolvedType.OBJECT, UnresolvedType.OBJECT, 1);
		a2 =
			makeConcreteAdvice(AdviceKind.After, UnresolvedType.OBJECT, UnresolvedType.THROWABLE, 2);
		
		assertEquals(+1, a2.compareTo(a1));
		assertEquals(-1, a1.compareTo(a2));
	}
	
	public void testSubtypes() {
		Advice a1 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.OBJECT, 1);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.THROWABLE, UnresolvedType.OBJECT, 1);
		Advice a3 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.forName("java.lang.String"), UnresolvedType.OBJECT, 1);
			
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
		ResolvedType aType =  world.resolve("Aspect");
		CrosscuttingMembers xcut = new CrosscuttingMembers(aType,true);
		aType.crosscuttingMembers = xcut;
		xcut.addDeclare(dom);
		world.getCrosscuttingMembersSet().addFixedCrosscuttingMembers(aType);
		
		Advice a1 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.OBJECT, 1);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.THROWABLE, 2);
		Advice a3 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.forName("java.lang.String"), 2);
		
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
		ResolvedType aType =  world.resolve("Aspect");
		CrosscuttingMembers xcut = new CrosscuttingMembers(aType,true);
		aType.crosscuttingMembers = xcut;
		xcut.addDeclare(dom);
		world.getCrosscuttingMembersSet().addFixedCrosscuttingMembers(aType);
		
		Advice a1 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.OBJECT, 2);
		Advice a2 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.THROWABLE, 1);
		Advice a3 =
			makeConcreteAdvice(AdviceKind.Before, UnresolvedType.OBJECT, UnresolvedType.forName("java.lang.String"), 1);
		
		assertEquals(-1, a2.compareTo(a1));
		assertEquals(+1, a1.compareTo(a2));

		assertEquals(-1, a3.compareTo(a1));
		assertEquals(+1, a1.compareTo(a3));
		
		
		assertEquals(+1, a3.compareTo(a2));
		assertEquals(-1, a2.compareTo(a3));
	}
	
	


	private Advice makeConcreteAdvice(AdviceKind kind, UnresolvedType declaringAspect, 
				UnresolvedType concreteAspect, int lexicalPosition)
	{
		Advice a1 = new BcelAdvice(kind, makeResolvedPointcut("this(*)"),  
				MemberImpl.method(declaringAspect, 0, "foo", "()V"),
				0, lexicalPosition, lexicalPosition, null, null);
		a1 = (Advice)a1.concretize(concreteAspect.resolve(world), world, null);
		return a1;
	}
	
	
	
}
