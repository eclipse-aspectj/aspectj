/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
	
	public void testMatching() {
		assertTrue("Map+ matches Map",tpm.matches(Map.class));
		assertTrue("Map+ matches HashMap",tpm.matches(HashMap.class));
		assertFalse("Map+ does not match String",tpm.matches(String.class));
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		PointcutParser pp = new PointcutParser();
		tpm = pp.parseTypePattern("java.util.Map+");
	}
	
	

}
