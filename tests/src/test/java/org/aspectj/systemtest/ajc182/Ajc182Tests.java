/*******************************************************************************
 * Copyright (c) 2014 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc182;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisTypeAnnos;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc182Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testInvisTypeAnnos_440983() throws ClassNotFoundException {
		runTest("invis type annos");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "Code");
		Method m = getMethodStartsWith(jc, "xxx");
		RuntimeInvisTypeAnnos rita = (RuntimeInvisTypeAnnos)getAttributeStartsWith(m.getCode().getAttributes(),"RuntimeInvisibleTypeAnnotations");
		assertEquals("AnnotationGen:[Anno #0 {}]",rita.getTypeAnnotations()[0].getAnnotation().toString());
	}

	public void testInvisTypeAnnos_440983_2() throws ClassNotFoundException {
		runTest("invis type annos 2");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc182Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
        return getClassResource("tests.xml");
	}

}
