/*
 * Created on 07-May-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectj.aopalliance.tests;

import org.aspectj.lang.reflect.MethodSignature;


class MockMethodSignature extends MockSignature implements MethodSignature {
	public MockMethodSignature(String name,Class decClass, Class[] paramTypes) {
		super(name,decClass,paramTypes);
	}

	public Class getReturnType() {return null;}
}