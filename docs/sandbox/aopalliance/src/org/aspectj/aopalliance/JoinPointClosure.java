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

import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.JoinPoint;

public abstract class JoinPointClosure implements Joinpoint {
	
	protected JoinPoint jp;
	
	public JoinPointClosure(JoinPoint joinPoint) {
		this.jp = joinPoint;
	}
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#proceed()
	 */
	public Object proceed() throws Throwable {
		return execute();
	}
	
	// for subclasses, renamed from proceed to avoid confusion in 
	// AspectJ around advice.
	public abstract Object execute();
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#getThis()
	 */
	public Object getThis() {
		return jp.getThis();  
	}
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#getStaticPart()
	 * Must return either a Field, Method or Constructor representing the entity
	 * at the joinpoint.
	 */
	public abstract AccessibleObject getStaticPart();

}
