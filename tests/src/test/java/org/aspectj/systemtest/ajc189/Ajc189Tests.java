/*******************************************************************************
 * Copyright (c) 2016 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc189;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc189Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testJarWeaving_433351() {
		runTest("jar weaving");
	}
	
	public void testJarWeaving_433351_4() {
		runTest("jar weaving 4");
	}

	public void testJarWeaving_433351_3() {
		runTest("jar weaving 3");
	}

	public void testJarWeaving_433351_2() {
		runTest("jar weaving 2");
	}
	
	public void testNPEAtAspectJ() throws Exception {
		runTest("NPE at aspectj");
	}
	
	public void testLostBounds() throws Exception {
		runTest("lost bounds");
		// This type has I added via declare parents
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "B");
		assertEquals("<T:LSomeClass;:LSomeInterface;>LC<TT;>;Ljava/io/Serializable;LI;",jc.getGenericSignature());
		jc = getClassFrom(ajc.getSandboxDirectory(), "D");
		assertEquals("<T::LSomeInterface;:LSomeOtherInterface;>Ljava/lang/Object;LI;",jc.getGenericSignature());
	}
	
	public void testWhileNPE_486203() throws Exception {
		runTest("while npe");
	}
	
//	public void testOverweaving_352389() throws Exception {
//		runTest("overweaving");
//	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc189Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc189.xml");
	}

}
