/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.bridge.context;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 */
public class CompilationAndWeavingContextTest extends TestCase {

	public void testEnteringPhase() {
		CompilationAndWeavingContext.enteringPhase(1,"XYZ");
		assertEquals("when fiddling XYZ\n",CompilationAndWeavingContext.getCurrentContext());
	}

	public void testDoubleEntry() {
		CompilationAndWeavingContext.enteringPhase(1,"XYZ");
		CompilationAndWeavingContext.enteringPhase(2, "ABC");
		assertEquals("when mucking about with ABC\nwhen fiddling XYZ\n",CompilationAndWeavingContext.getCurrentContext());
	}

	public void testEntryEntryExit() {
		CompilationAndWeavingContext.enteringPhase(1,"XYZ");
		ContextToken ct = CompilationAndWeavingContext.enteringPhase(2, "ABC");
		CompilationAndWeavingContext.leavingPhase(ct);
		assertEquals("when fiddling XYZ\n",CompilationAndWeavingContext.getCurrentContext());
	}

	public void testEntryExitTop() {
		ContextToken ct = CompilationAndWeavingContext.enteringPhase(1,"XYZ");
		CompilationAndWeavingContext.enteringPhase(2, "ABC");
		CompilationAndWeavingContext.leavingPhase(ct);
		assertEquals("",CompilationAndWeavingContext.getCurrentContext());
	}


	protected void setUp() throws Exception {
		CompilationAndWeavingContext.reset();
		CompilationAndWeavingContext.registerFormatter(1, new MyContextFormatter("fiddling "));
		CompilationAndWeavingContext.registerFormatter(2, new MyContextFormatter("mucking about with "));
	}

	private static class MyContextFormatter implements ContextFormatter {

		private String prefix;

		public MyContextFormatter(String prefix) {
			this.prefix = prefix;
		}

		public String formatEntry(int phaseId, Object data) {
			return prefix + data.toString();
		}

	}
}
