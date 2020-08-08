/*******************************************************************************
 * Copyright (c) 2013 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc173;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc173Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testAddRemoveAnnos_407739() throws Exception {
		runTest("add remove annos");
	}
	
//	public void testOrdering_407966() throws Exception {
//		runTest("ordering");
//	}
//	
//	public void testInnerInterfaceMixin_408014() throws Exception {
//		runTest("inner interface mixin");
//	}
	
	public void testClassAnnoValue_405016_1() throws Exception {
		// test that class literals allowed
		runTest("class anno value 1");
	}
	
	public void testInnerNames_407494() throws Exception {
		runTest("inner names");
	}
	
	public void testInnerNames_407494_2() throws Exception {
		runTest("inner names 2");
	}

//	public void testClassAnnoValue_405016() throws Exception {
//		runTest("class anno value");
//	}

	public void testAbstractMethodError_404601() throws Exception {
		runTest("abstract method error");
	}
	
	public void testDeclareAnnoOnItd() throws Exception {
		runTest("declare anno on itd");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"C");
		Method m = getMethodStartsWith(jc, "getName");
		assertNotNull(m);
		AnnotationGen[] ags = m.getAnnotations();
		for (AnnotationGen ag : ags) {
			System.out.println(ag);
		}
		assertEquals(1,ags.length);
		assertEquals("LFoo;",ags[0].getTypeSignature());
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc173Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc173.xml");
	}

}
