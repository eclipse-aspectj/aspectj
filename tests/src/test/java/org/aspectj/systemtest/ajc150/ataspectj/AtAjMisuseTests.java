/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150.ataspectj;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjMisuseTests extends XMLBasedAjcTestCase {

    protected java.net.URL getSpecFile() {
        return getClassResource("misuse.xml");
    }

    public static Test suite() {
        return XMLBasedAjcTestCase.loadSuite(AtAjMisuseTests.class);
    }

    public void testQAspectClassExtendingQAspectClass() {
		runTest("@Aspect class extending @Aspect class");
	}

	// TODO asc commented out for now until Alex manages to get ajdtcore up to date...
//	public void testClassWithQBeforeExtendingQAspectClass() {
//		runTest("class with @Before extending @Aspect class");
//	}

	public void testQPointcutNotReturningVoid() {
		runTest("@Pointcut not returning void");
	}

	public void testQPointcutWithGarbageString() {
		runTest("@Pointcut with garbage string");
	}

	public void testQPointcutWithThrowsClause() {
		runTest("@Pointcut with throws clause");
	}

	public void testQAfterReturningWithWrongNumberOfArgs() {
		runTest("@AfterReturning with wrong number of args");
	}

	public void testQBeforeOnNon_publicMethod() {
		runTest("@Before on non-public method");
	}

	public void testQBeforeOnMethodNotReturningVoid() {
		runTest("@Before on method not returning void");
	}

    public void testQBeforeWithPJP() {
        runTest("@Before with PJP");
    }
}
