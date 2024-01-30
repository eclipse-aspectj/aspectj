/*
 * Created on 07-May-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectj.aopalliance.tests;

import org.aspectj.lang.reflect.ConstructorSignature;


class MockConstructorSignature extends MockSignature implements ConstructorSignature {
	public MockConstructorSignature(String name,Class decClass, Class[] paramTypes) {
		super(name,decClass,paramTypes);
	}		
}