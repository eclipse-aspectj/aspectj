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
package org.aspectj.runtime.reflect;

import junit.framework.TestCase;

/**
 */
public class SignatureTest extends TestCase {
	public void testGetDeclaringTypeName() {
		FieldSignatureImpl fsi = new FieldSignatureImpl(0,"x",SignatureTest.class,String.class);
		assertEquals(SignatureTest.class.getName(),fsi.getDeclaringTypeName());
		assertSame(fsi.getDeclaringTypeName(),fsi.getDeclaringTypeName()); // should be cached.
	}
}
