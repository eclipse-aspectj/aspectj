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

import org.aspectj.weaver.CrosscuttingMembers;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.TestScope;

public class NonstaticWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public NonstaticWeaveTestCase(String name) {
		super(name);
	}
	
	
	public void testBefore() throws IOException {
		String s = "before(): get(* *.*) -> void Aspect.ajc_before()";
		PerClause per = new PerSingleton();
		per = per.concretize(world.resolve("Aspect"));

        ShadowMunger myMunger = this.makeConcreteAdvice(s, 0, per);
		
		weaveTest(getStandardTargets(), "NonStaticBefore", myMunger);
	}
	
	public void testBeforeCflow() throws IOException {
		String s = "before(): get(* *.*) -> void Aspect.ajc_before()";
		PerClause per = new PatternParser("percflow(execution(void main(..)))").maybeParsePerClause();
		per.resolve(new TestScope(new String[0], new String[0], world));
		
		ResolvedType onAspect = world.resolve("Aspect");
		CrosscuttingMembers xcut = new CrosscuttingMembers(onAspect,true);
		onAspect.crosscuttingMembers = xcut;
		
		per = per.concretize(onAspect);

        ShadowMunger myMunger = this.makeConcreteAdvice(s, 0, per);
		
		xcut.addConcreteShadowMunger(myMunger);		
		
		
		weaveTest(getStandardTargets(), "CflowNonStaticBefore", xcut.getShadowMungers());
	}
	
	public void testBeforePerThis() throws IOException {
		String s = "before(): call(* println(..)) -> void Aspect.ajc_before()";
		PerClause per = new PatternParser("pertarget(call(* println(..)))").maybeParsePerClause();
		per.resolve(new TestScope(new String[0], new String[0], world));
		
		ResolvedType onAspect = world.resolve("Aspect");
		CrosscuttingMembers xcut = new CrosscuttingMembers(onAspect,true);
		onAspect.crosscuttingMembers = xcut;
		per = per.concretize(onAspect);

        ShadowMunger myMunger = this.makeConcreteAdvice(s, 0, per);
		xcut.addConcreteShadowMunger(myMunger);
		
//		List mungers = new ArrayList();
//		mungers.add(myMunger);
//		mungers.addAll(onAspect.getExtraConcreteShadowMungers());		
		
		
		weaveTest(getStandardTargets(), "PerThisNonStaticBefore", xcut.getShadowMungers());
	}
	
	
	
}
