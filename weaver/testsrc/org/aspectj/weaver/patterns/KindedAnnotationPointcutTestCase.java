/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class KindedAnnotationPointcutTestCase extends TestCase {

	public void testParsing() {
		PatternParser p = new PatternParser("@call(@String)");
		Pointcut pc = p.parsePointcut();
		assertTrue(pc instanceof KindedAnnotationPointcut);
	}
}
