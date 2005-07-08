/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 */
public class GenericsWildCardTypeXTestCase extends TestCase {

	public void testIdentity() {
		TypeX anything = GenericsWildcardTypeX.GENERIC_WILDCARD;
		assertEquals("Ljava/lang/Object;",anything.getSignature());
	}
	
	public void testResolving() {
		BoundedReferenceType brt = (BoundedReferenceType)
			GenericsWildcardTypeX.GENERIC_WILDCARD.resolve(new BcelWorld());
		assertEquals("Ljava/lang/Object;",brt.getSignature());
		assertTrue(brt.isExtends());
		assertEquals("Ljava/lang/Object;",brt.getUpperBound().getSignature());
	}
	
}
