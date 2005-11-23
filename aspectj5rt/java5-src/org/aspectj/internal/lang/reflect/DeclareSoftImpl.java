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
package org.aspectj.internal.lang.reflect;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.DeclareSoft;
import org.aspectj.lang.reflect.PointcutExpression;

/**
 * @author colyer
 *
 */
public class DeclareSoftImpl implements DeclareSoft {

	private AjType<?> declaringType;
	private PointcutExpression pointcut;
	private AjType<?> exceptionType;
	private String missingTypeName;
	
	
	public DeclareSoftImpl(AjType<?> declaringType, String pcut, String exceptionTypeName) {
		this.declaringType = declaringType;
		this.pointcut = new PointcutExpressionImpl(pcut);
		try {
			ClassLoader cl = declaringType.getJavaClass().getClassLoader();
			this.exceptionType = AjTypeSystem.getAjType(Class.forName(exceptionTypeName,false,cl));
		} catch (ClassNotFoundException ex) {
			this.missingTypeName = exceptionTypeName;
		}
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareSoft#getDeclaringType()
	 */
	public AjType getDeclaringType() {
		return this.declaringType;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareSoft#getSoftenedExceptionType()
	 */
	public AjType getSoftenedExceptionType() throws ClassNotFoundException {
		if (this.missingTypeName != null) throw new ClassNotFoundException(this.missingTypeName);
		return this.exceptionType;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareSoft#getPointcutExpression()
	 */
	public PointcutExpression getPointcutExpression() {
		return this.pointcut;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("declare soft : ");
		if (this.missingTypeName != null) {
			sb.append(this.exceptionType.getName());
		} else {
			sb.append(this.missingTypeName);
		}
		sb.append(" : ");
		sb.append(getPointcutExpression().asString());
		return sb.toString();
	}
}
