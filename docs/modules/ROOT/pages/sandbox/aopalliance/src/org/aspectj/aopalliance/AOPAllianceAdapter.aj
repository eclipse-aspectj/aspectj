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

import org.aspectj.lang.SoftException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.ConstructorInterceptor;

public abstract aspect AOPAllianceAdapter {

	/**
	 * Return the interceptor to use at method execution join points.
	 * Must be overriden by subclasses.
	 * @return MethodInterceptor, or null if no method advice required
	 */
	protected abstract MethodInterceptor getMethodInterceptor();

	/**
	 * Return the interceptor to use at constructor execution join points.
	 * May be overriden by subclasses.
	 * @return ConstructorInterceptor, or null if no constructor advice required
	 */
	protected ConstructorInterceptor getConstructorInterceptor() {
	  return null;
	}

	protected abstract pointcut targetJoinPoint();

	pointcut methodExecution() : execution(* *(..));
	pointcut constructorExecution() : execution(new(..));

	Object around() : targetJoinPoint() && methodExecution() {
		MethodInvocationClosure mic = new MethodInvocationClosure(thisJoinPoint) {
			public Object execute() { return proceed();}
		};
		MethodInterceptor mInt = getMethodInterceptor();
		if (mInt != null) {
			try {
				return mInt.invoke(mic);
			} catch (Throwable t) {
				throw new SoftException(t);
			}
		} else {
			return proceed();
		}
	}

	Object around() : targetJoinPoint() && constructorExecution() {
		ConstructorInvocationClosure cic = new ConstructorInvocationClosure(thisJoinPoint) {
			public Object execute() { proceed(); return thisJoinPoint.getThis();}
		};
		ConstructorInterceptor cInt = getConstructorInterceptor();
		if (cInt != null) {
			try {
				return cInt.construct(cic);
			} catch (Throwable t) {
				throw new SoftException(t);
			}
		} else {
			return proceed();
		}
	}

}
