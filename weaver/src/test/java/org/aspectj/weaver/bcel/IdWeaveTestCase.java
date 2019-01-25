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
import java.util.ArrayList;
import java.util.List;

import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.Pointcut;

public class IdWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public IdWeaveTestCase(String name) {
		super(name);
	}

	public void testFancyId() throws IOException {
		final List l = new ArrayList();
		Pointcut pointcut2 = makePointcutAll();
		BcelAdvice p = new BcelAdvice(null, pointcut2, null, 0, -1, -1, null, null) {
			public boolean match(Shadow shadow, World world) {
				if (super.match(shadow, world)) {
					l.add(shadow);
				}
				return false;
			}
		};
		weaveTest(new String[] { "FancyHelloWorld" }, "Id2", p);

		checkShadowSet(l, new String[] { "method-call(void java.io.PrintStream.println(java.lang.Object))",
				"method-call(void java.io.PrintStream.println(java.lang.String))",
				"method-call(java.lang.StringBuffer java.lang.StringBuffer.append(int))",
				"method-call(java.lang.String java.lang.StringBuffer.toString())",
				"method-execution(java.lang.String FancyHelloWorld.getName())",
				"field-get(java.io.PrintStream java.lang.System.out)",
				"method-call(void java.io.PrintStream.println(java.lang.String))",
				"method-execution(void FancyHelloWorld.main(java.lang.String[]))", "method-call(int java.lang.String.hashCode())",
				"constructor-execution(void FancyHelloWorld.<init>())",
				"constructor-call(void java.lang.StringBuffer.<init>(java.lang.String))" });
	}

	public void testId() throws IOException {
		final List l = new ArrayList();
		BcelAdvice p = new BcelAdvice(null, makePointcutAll(), null, 0, -1, -1, null, null) {
			public boolean implementOn(Shadow shadow) {
				l.add(shadow);
				return true;
			}
		};
		weaveTest(new String[] { "HelloWorld" }, "Id2", p);

		checkShadowSet(l, new String[] { "method-execution(void HelloWorld.main(java.lang.String[]))",
				"method-call(void java.io.PrintStream.println(java.lang.String))",
				"field-get(java.io.PrintStream java.lang.System.out)", "constructor-execution(void HelloWorld.<init>())", });
	}

	// this test requires that Trace has been unzipped and placed in the correct place
	// public void testTraceId() throws IOException {
	// String saveClassDir = classDir;
	// try {
	// classDir = "testdata/dummyAspect.jar";
	//	    	
	//	    	
	//	    	
	// final List l = new ArrayList();
	// BcelAdvice p = new BcelAdvice(null, makePointcutAll(), null, 0, -1, -1, null, null) {
	// public void implementOn(Shadow shadow) {
	// l.add(shadow);
	// }
	// };
	// boolean tempRunTests = runTests;
	// runTests = false;
	// weaveTest(new String[] {"DummyAspect"}, "Id", p);
	// runTests = tempRunTests;
	//	        
	// checkShadowSet(l, new String[] {
	// "constructor-execution(void DummyAspect.<init>())",
	// // XXX waiting on parser stuff
	// //"advice-execution(void DummyAspect.ajc_before_1(java.lang.Object))",
	// });
	// } finally {
	// classDir = saveClassDir;
	// }
	// }

}
