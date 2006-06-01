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
package org.aspectj.aopalliance;

import java.lang.reflect.Constructor;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aspectj.lang.JoinPoint;

public abstract class ConstructorInvocationClosure extends InvocationJoinPointClosure
		implements ConstructorInvocation {

	public ConstructorInvocationClosure(JoinPoint jp) {
		super(jp);
	}
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInvocation#getMethod()
	 */
	public Constructor getConstructor() {
		return (Constructor) getStaticPart();
	}

}
