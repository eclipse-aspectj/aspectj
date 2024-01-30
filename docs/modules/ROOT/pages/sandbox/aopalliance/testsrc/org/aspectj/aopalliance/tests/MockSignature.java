/*
 * Created on 07-May-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectj.aopalliance.tests;

import org.aspectj.lang.reflect.CodeSignature;


class MockSignature implements CodeSignature {
	
	private String name;
	private Class decClass;
	private Class[] paramTypes;
	
	public MockSignature(String name,Class decClass, Class[] paramTypes) {
		this.name = name;
		this.decClass = decClass;
		this.paramTypes = paramTypes;
	}
	
	public Class[] getExceptionTypes() {
		return null;
	}
	public String[] getParameterNames() {
		return null;
	}
	public Class[] getParameterTypes() {
		return paramTypes;
	}
	public Class getDeclaringType() {
		return decClass;
	}
	public String getDeclaringTypeName() {
		return null;
	}
	public int getModifiers() {
		return 0;
	}
	public String getName() {
		return name;
	}
	public String toLongString() {
		return "long string";
	}
	public String toShortString() {
		return null;
	}
}