/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import java.io.IOException;
import java.util.ArrayList;

import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.Shadow;

public class MoveInstructionsWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public MoveInstructionsWeaveTestCase(String name) {
		super(name);
	}

	public void testHello() throws IOException {
		BcelAdvice p = new BcelAdvice(null, makePointcutAll(), null, 0, -1, -1, null, null) {
			public void specializeOn(Shadow s) {
				super.specializeOn(s);
				((BcelShadow) s).initializeForAroundClosure();
			}

			public boolean implementOn(Shadow s) {
				BcelShadow shadow = (BcelShadow) s;
				LazyMethodGen newMethod = shadow.extractShadowInstructionsIntoNewMethod(NameMangler.getExtractableName(shadow
						.getSignature())
						+ "_extracted", 0, this.getSourceLocation(), new ArrayList(),shadow.getEnclosingClass().isInterface());
				shadow.getRange().append(shadow.makeCallToCallback(newMethod));

				if (!shadow.isFallsThrough()) {
					shadow.getRange().append(InstructionFactory.createReturn(newMethod.getReturnType()));
				}
				return true;
			}
		};

		weaveTest("HelloWorld", "ExtractedHelloWorld", p);
	}

	static int counter = 0;

	public void testFancyHello() throws IOException {
		// Reset counter, just in case this test runs multiple times in one JVM. This can happen e.g. due to "run all tests"
		// in IntelliJ IDEA, which directly runs this test class and als WeaverModuleTests, both of which implement
		// junit.framework.TestCase. In that case, during the second run the counter would start at a higher base count,
		// making the 2nd test run fail.
		counter = 0;
		BcelAdvice p = new BcelAdvice(null, makePointcutAll(), null, 0, -1, -1, null, null) {
			public void specializeOn(Shadow s) {
				super.specializeOn(s);
				((BcelShadow) s).initializeForAroundClosure();
			}

			public boolean implementOn(Shadow s) {
				BcelShadow shadow = (BcelShadow) s;
				LazyMethodGen newMethod =
						shadow.extractShadowInstructionsIntoNewMethod(NameMangler.getExtractableName(shadow
						.getSignature())
						+ "_extracted" + counter++, 0, this.getSourceLocation(), new ArrayList(),shadow.getEnclosingClass().isInterface());
				shadow.getRange().append(shadow.makeCallToCallback(newMethod));

				if (!shadow.isFallsThrough()) {
					shadow.getRange().append(InstructionFactory.createReturn(newMethod.getReturnType()));
				}
				return true;
			}
		};

		weaveTest("FancyHelloWorld", "ExtractedFancyHelloWorld", p);
	}
}
