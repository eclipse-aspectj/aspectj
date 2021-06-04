/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
