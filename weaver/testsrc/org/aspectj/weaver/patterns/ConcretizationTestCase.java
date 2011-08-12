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

package org.aspectj.weaver.patterns;

import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.bcel.WeaveTestCase;

public class ConcretizationTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public ConcretizationTestCase(String name) {
		super(name);
	}

	public void testNothingForAntJUnit() {
	}

	// String[] none = new String[0];

	/*
	 * XXX temporarily skipping public void testCflowResidual() throws IOException {
	 * 
	 * BcelAdvice a = (BcelAdvice) makeConcreteTestAdviceEntryPart();
	 * 
	 * TestShadow shadow = new TestShadow(Shadow.MethodCall, Member.methodFromString("int Aspect.i(int x)"), UnresolvedType.OBJECT,
	 * world);
	 * 
	 * ExposedState state = new ExposedState(1);
	 * 
	 * a.specializeOn(shadow);
	 * 
	 * //System.err.println(shadow); //System.err.println(a);
	 * 
	 * //System.err.println(a.exposedState);
	 * 
	 * 
	 * }
	 * 
	 * 
	 * 
	 * public Advice makeConcreteTestAdviceEntryPart() throws IOException { // XXX copied from below, refactor later
	 * 
	 * 
	 * // returns the advice for the entry part of cflow(foo(a)) Pointcut in = createResolvedPointcut(
	 * "cflow(foo(a)) && (args(b) && !cflow(foo(int)))", new String[] { "b", "a" }, new String[] { "float", "int" });
	 * 
	 * ResolvedPointcutDefinition ref = new ResolvedPointcutDefinition( UnresolvedType.forName("Aspect"), 0, "foo", new
	 * UnresolvedType[] { UnresolvedType.INT }, createResolvedPointcut( "args(refA)", new String[] { "refA" }, new String[] { "int"
	 * })); BcelObjectType target = (BcelObjectType) world.resolve("Aspect");
	 * 
	 * // now munge this to get the pointcut in it
	 * 
	 * target.addPointcutDefinition(ref); CrosscuttingMembers xcut = new CrosscuttingMembers(target); target.crosscuttingMembers =
	 * xcut;
	 * 
	 * Advice adviceMember = new BcelAdvice( AdviceKind.Before, in, Member.method(UnresolvedType.forName("FOO"), 0, "garadf",
	 * "(FI)V"), 0, 0, 0, null, null); // The pointcut to concretize
	 * 
	 * // this returns the actual advice, but we don't care about it now. in.concretize(target, 2, adviceMember);
	 * 
	 * List c = (List)xcut.getCflowEntries(); //target.getExtraConcreteShadowMungers();
	 * 
	 * return (Advice) c.get(0); }
	 * 
	 * public void XtestCflow() throws IOException { Pointcut in =
	 * createResolvedPointcut("cflow(foo(a)) && (args(b) && !cflow(foo(int)))", new String[] {"b", "a"}, new String[] {"float",
	 * "int"} );
	 * 
	 * ResolvedPointcutDefinition ref = new ResolvedPointcutDefinition(UnresolvedType.forName("Aspect"), 0, "foo", new
	 * UnresolvedType[] { UnresolvedType.INT }, createResolvedPointcut("args(refA)", new String[] {"refA"}, new String[] {"int"}));
	 * 
	 * List expectedSlots = new ArrayList(); expectedSlots.add(new ConcreteCflowPointcut.Slot(1, UnresolvedType.INT, 0));
	 * 
	 * checkConcr(in, ref, expectedSlots); }
	 * 
	 * public void checkConcr( Pointcut in, ResolvedPointcutDefinition referredTo, List expectedSlots) throws IOException {
	 * 
	 * BcelObjectType target = (BcelObjectType)world.resolve("Aspect");
	 * 
	 * // now munge this to get the pointcut in it
	 * 
	 * target.addPointcutDefinition(referredTo);
	 * 
	 * 
	 * Advice adviceMember = new BcelAdvice(AdviceKind.Before, in, Member.method(UnresolvedType.forName("FOO"), 0, "garadf",
	 * "(FI)V"), 0, 0, 0, null, null);
	 * 
	 * // The pointcut to concretize AndPointcut ap = (AndPointcut)in.concretize(target, 2, adviceMember);
	 * 
	 * 
	 * ConcreteCflowPointcut conc = (ConcreteCflowPointcut)ap.left;
	 * 
	 * List slots = conc.slots; TestUtil.assertSetEquals(expectedSlots, slots);
	 * 
	 * }
	 */

	public Pointcut createResolvedPointcut(String pointcutSource, String[] formalNames, String[] formalTypes) {
		final Pointcut sp = Pointcut.fromString(pointcutSource);
		final Pointcut rp = sp.resolve(new SimpleScope(world, SimpleScope.makeFormalBindings(UnresolvedType.forNames(formalTypes),
				formalNames)));
		return rp;
	}
}
