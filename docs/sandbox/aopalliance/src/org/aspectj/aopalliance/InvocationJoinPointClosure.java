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

import java.lang.reflect.AccessibleObject;

import org.aopalliance.intercept.Invocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

public abstract class InvocationJoinPointClosure extends JoinPointClosure implements Invocation {

	public InvocationJoinPointClosure(JoinPoint jp) {
		super(jp);
	}
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#getStaticPart()
	 */
	public AccessibleObject getStaticPart() {
		CodeSignature cSig = (CodeSignature)jp.getSignature();
		Class clazz = cSig.getDeclaringType();
		AccessibleObject ret = null;
		try {
			if (cSig instanceof MethodSignature) {
				ret = clazz.getMethod(cSig.getName(),cSig.getParameterTypes());				
			} else if (cSig instanceof ConstructorSignature) {
				ret = clazz.getConstructor(cSig.getParameterTypes());
			}
		} catch (NoSuchMethodException mEx) {
			throw new UnsupportedOperationException(
					"Can't find member " + cSig.toLongString());
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.Invocation#getArguments()
	 */
	public Object[] getArguments() {
		return jp.getArgs();
	}	

}
