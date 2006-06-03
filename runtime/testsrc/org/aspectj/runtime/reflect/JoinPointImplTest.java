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
package org.aspectj.runtime.reflect;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 */
public class JoinPointImplTest extends TestCase {

	public void testGetArgs() {
	    String arg1 = "abc";
	    StringBuffer arg2 = new StringBuffer("def");
	    Object arg3 = new Object();
		Object[] args = new Object[] { arg1, arg2, arg3 };
		JoinPointImpl jpi = new JoinPointImpl(null,null,null,args);
		
		Object[] retrievedArgs = jpi.getArgs();
		assertEquals("First arg unchanged",arg1,retrievedArgs[0]);
		assertEquals("Second arg unchanged",arg2,retrievedArgs[1]);
		assertEquals("Third arg unchanged",arg3,retrievedArgs[2]);
		retrievedArgs[0] = "xyz";
		((StringBuffer)retrievedArgs[1]).append("ghi");
		retrievedArgs[2] = "jkl";
		Object[] afterUpdateArgs = jpi.getArgs();
		assertEquals("Object reference not changed",arg1,afterUpdateArgs[0]);
		assertEquals("Object reference unchanged",arg2,afterUpdateArgs[1]);
		assertEquals("state of referenced object updated","defghi",afterUpdateArgs[1].toString());
		assertEquals("Object reference not changed",arg3,afterUpdateArgs[2]);
	}
	
}
