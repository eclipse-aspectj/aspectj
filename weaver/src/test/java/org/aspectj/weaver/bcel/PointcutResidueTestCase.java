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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.CrosscuttingMembers;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.patterns.ConstantPoolSimulator;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;

public class PointcutResidueTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public PointcutResidueTestCase(String name) {
		super(name);
	}

	String[] none = new String[0];

	// -----

	// ----

	public void testArgResidue1() throws IOException {
		checkMultiArgWeave("StringResidue1",
				"call(* *(java.lang.Object, java.lang.Object)) && args(java.lang.String, java.lang.String)");
	}

	public void testArgResidue2() throws IOException {
		checkMultiArgWeave("StringResidue2", "call(* *(java.lang.Object, java.lang.Object)) && args(.., java.lang.String)");
	}

	public void testArgResidue3() throws IOException {
		checkMultiArgWeave("StringResidue3", "call(* *(java.lang.Object, java.lang.Object)) && args(java.lang.String, ..)");
	}

	// BETAX this is a beta feature.
	// public void testArgResidue4() throws IOException {
	// checkMultiArgWeave(
	// "StringResidue4",
	// "call(* *(java.lang.Object, java.lang.Object)) && args(.., java.lang.String, ..)");
	// }

	public void testMultiArgState() throws IOException {
		checkWeave("StateResidue", "MultiArgHelloWorld", "call(* *(java.lang.Object, java.lang.Object)) && args(s, ..)",
				new String[] { "java.lang.String" }, new String[] { "s" });
		checkWeave("StateResidue", "MultiArgHelloWorld", "call(* *(java.lang.Object, java.lang.Object)) && args(s, *)",
				new String[] { "java.lang.String" }, new String[] { "s" });
	}

	public void testAdd() throws IOException {
		checkDynamicWeave("AddResidue", "call(public * add(..)) && target(java.util.ArrayList)");
		checkDynamicWeave("AddResidue", "call(public * add(..)) && (target(java.util.ArrayList) || target(java.lang.String))");
		checkDynamicWeave("AddResidue",
				"call(public * add(..)) && this(java.io.Serializable) && target(java.util.ArrayList) && !this(java.lang.Integer)");
	}

	public void testNot() throws IOException {
		checkDynamicWeave("AddNotResidue", "call(public * add(..)) && !target(java.util.ArrayList)");
		checkDynamicWeave("AddNotResidue", "call(public * add(..)) && !(target(java.util.ArrayList) || target(java.lang.String)) ");
		checkDynamicWeave("AddNotResidue", "call(public * add(..)) && target(java.lang.Object) && !target(java.util.ArrayList)");
	}

	public void testState() throws IOException {
		checkWeave("AddStateResidue", "DynamicHelloWorld", "call(public * add(..)) && target(list)",
				new String[] { "java.util.ArrayList" }, new String[] { "list" });
		checkWeave("AddStateResidue", "DynamicHelloWorld", "target(foo) && !target(java.lang.Integer) && call(public * add(..))",
				new String[] { "java.util.ArrayList" }, new String[] { "foo" });
		checkDynamicWeave("AddResidue", "call(public * add(..)) && (target(java.util.ArrayList) || target(java.lang.String))");
		checkDynamicWeave("AddResidue",
				"call(public * add(..)) && this(java.io.Serializable) && target(java.util.ArrayList) && !this(java.lang.Integer)");
	}

	public void testNoResidueArgs() throws IOException {
		checkDynamicWeave("NoResidue", "call(public * add(..)) && args(java.lang.Object)");
		checkDynamicWeave("NoResidue", "call(public * add(..)) && args(*)");
		checkDynamicWeave("NoResidue", "call(public * add(..))");
	}

	// ---- cflow tests

	public void testCflowState() throws IOException {
		checkWeave("CflowStateResidue", "DynamicHelloWorld",
				"cflow(call(public * add(..)) && target(list)) && execution(public void main(..))",
				new String[] { "java.util.ArrayList" }, new String[] { "list" });
		// checkWeave(
		// "CflowStateResidue",
		// "DynamicHelloWorld",
		// "cflow(call(public * add(..)) && target(list)) && this(obj) && execution(public void doit(..))",
		// new String[] { "java.lang.Object", "java.util.ArrayList" },
		// new String[] { "obj", "list" });
		// checkWeave(
		// "AddStateResidue",
		// "DynamicHelloWorld",
		// "target(foo) && !target(java.lang.Integer) && call(public * add(..))",
		// new String[] { "java.util.ArrayList" },
		// new String[] { "foo" });
		// checkDynamicWeave(
		// "AddResidue",
		// "call(public * add(..)) && (target(java.util.ArrayList) || target(java.lang.String))");
		// checkDynamicWeave(
		// "AddResidue",
		// "call(public * add(..)) && this(java.io.Serializable) && target(java.util.ArrayList) && !this(java.lang.Integer)");
	}

	// ----

	private void checkDynamicWeave(String label, String pointcutSource) throws IOException {
		checkWeave(label, "DynamicHelloWorld", pointcutSource, new String[0], new String[0]);
	}

	private void checkMultiArgWeave(String label, String pointcutSource) throws IOException {
		checkWeave(label, "MultiArgHelloWorld", pointcutSource, new String[0], new String[0]);
	}

	private void checkWeave(String label, String filename, String pointcutSource, String[] formalTypes, String[] formalNames)
			throws IOException {
		final Pointcut sp = Pointcut.fromString(pointcutSource);
		final Pointcut rp = sp.resolve(new SimpleScope(world, SimpleScope.makeFormalBindings(UnresolvedType.forNames(formalTypes),
				formalNames)));

		ShadowMunger pp = new BcelAdvice(AdviceKind.Before, rp, MemberImpl.method(UnresolvedType.forName("Aspect"),
				Modifier.STATIC, "ajc_before_0",
				MemberImpl.typesToSignature(UnresolvedType.VOID, UnresolvedType.forNames(formalTypes), false)), 0, -1, -1, null,
				null);

		ResolvedType inAspect = world.resolve("Aspect");
		CrosscuttingMembers xcut = new CrosscuttingMembers(inAspect, true);
		inAspect.crosscuttingMembers = xcut;

		ShadowMunger cp = pp.concretize(inAspect, world, null);

		xcut.addConcreteShadowMunger(cp);

		// System.out.println("extras: " + inAspect.getExtraConcreteShadowMungers());
		// List advice = new ArrayList();
		// advice.add(cp);
		// advice.addAll(inAspect.getExtraConcreteShadowMungers());
		weaveTest(new String[] { filename }, label, xcut.getShadowMungers());

		checkSerialize(rp);
	}

	public void weaveTest(String name, String outName, ShadowMunger planner) throws IOException {
		List<ShadowMunger> l = Collections.singletonList(planner);
		weaveTest(name, outName, l);
	}

	public void checkSerialize(Pointcut p) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ConstantPoolSimulator cps = new ConstantPoolSimulator();
		CompressingDataOutputStream out = new CompressingDataOutputStream(bo, cps);
		p.write(out);
		out.close();

		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		VersionedDataInputStream in = new VersionedDataInputStream(bi, cps);
		Pointcut newP = Pointcut.read(in, null);

		assertEquals("write/read", p, newP);
	}

}
