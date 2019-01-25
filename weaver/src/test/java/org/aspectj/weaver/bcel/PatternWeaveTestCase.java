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
import java.util.ArrayList;
import java.util.List;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.patterns.ConstantPoolSimulator;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;

public class PatternWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public PatternWeaveTestCase(String name) {
		super(name);
	}

	String[] none = new String[0];

	// XXX this test is incompatible with optimizations made to weaver

	public void testPublic() throws IOException {
		String[] publicHello = new String[] { "method-execution(void HelloWorld.main(java.lang.String[]))", };
		String[] publicFancyHello = new String[] { "method-execution(void FancyHelloWorld.main(java.lang.String[]))",
				"method-execution(java.lang.String FancyHelloWorld.getName())", };
		checkPointcut("execution(public * *(..))", publicHello, publicFancyHello);
	}

	//	
	// public void testPrintln() throws IOException {
	// String[] callPrintlnHello = new String[] {
	// "method-call(void java.io.PrintStream.println(java.lang.String))",
	// };
	// String[] callPrintlnFancyHello = new String[] {
	// "method-call(void java.io.PrintStream.println(java.lang.String))",
	// "method-call(void java.io.PrintStream.println(java.lang.String))",
	// "method-call(void java.io.PrintStream.println(java.lang.Object))",
	// };
	// checkPointcut("call(* println(*))", callPrintlnHello, callPrintlnFancyHello);
	// }
	//	
	// public void testMumble() throws IOException {
	// checkPointcut("call(* mumble(*))", none, none);
	// }
	//	
	// public void testFooBar() throws IOException {
	// checkPointcut("call(FooBar *(..))", none, none);
	// }
	//	
	// public void testGetOut() throws IOException {
	// String[] getOutHello = new String[] {
	// "field-get(java.io.PrintStream java.lang.System.out)",
	// };
	//		
	// checkPointcut("get(* java.lang.System.out)", getOutHello, getOutHello);
	// }
	//	
	// // private Pointcut makePointcut(String s) {
	// // return new PatternParser(s).parsePointcut();
	// // }
	//		
	private void checkPointcut(String pointcutSource, String[] expectedHelloShadows, String[] expectedFancyShadows)
			throws IOException {
		Pointcut sp = Pointcut.fromString(pointcutSource);
		Pointcut rp = sp.resolve(new SimpleScope(world, FormalBinding.NONE));
		Pointcut cp = rp.concretize(ResolvedType.MISSING, ResolvedType.MISSING, 0);

		final List l = new ArrayList();
		BcelAdvice p = new BcelAdvice(null, cp, null, 0, -1, -1, null, null) {
			public boolean implementOn(Shadow shadow) {
				l.add(shadow);
				return true;
			}
		};
		weaveTest(new String[] { "HelloWorld" }, "PatternWeave", p);

		checkShadowSet(l, expectedHelloShadows);

		l.clear();
		weaveTest(new String[] { "FancyHelloWorld" }, "PatternWeave", p);

		checkShadowSet(l, expectedFancyShadows);

		checkSerialize(rp);
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
