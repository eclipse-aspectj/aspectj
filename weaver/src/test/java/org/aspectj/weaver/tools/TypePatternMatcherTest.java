/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver.tools;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class TypePatternMatcherTest extends TestCase {

	TypePatternMatcher tpm;

	private boolean needToSkip = false;
	
	/** this condition can occur on the build machine only, and is way too complex to fix right now... */
	private boolean needToSkipPointcutParserTests() {
		try {
			Class.forName("org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate",false,this.getClass().getClassLoader());//ReflectionBasedReferenceTypeDelegate.class.getClassLoader()); 
		} catch (ClassNotFoundException cnfEx) {
			return true;
		}
		return false;
	}
	
	public void testMatching() {
		if (needToSkip) return;
		
		assertTrue("Map+ matches Map",tpm.matches(Map.class));
		assertTrue("Map+ matches HashMap",tpm.matches(HashMap.class));
		assertFalse("Map+ does not match String",tpm.matches(String.class));
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		needToSkip = needToSkipPointcutParserTests();
		if (needToSkip) return;
		PointcutParser pp = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		tpm = pp.parseTypePattern("java.util.Map+");
	}
	
	

}
