/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Test;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ThisOrTargetAnnotationPointcut extends NameBindingPointcut {

	private boolean isThis;
	private AnnotationTypePattern type;
	
	/**
	 * 
	 */
	public ThisOrTargetAnnotationPointcut(boolean isThis, AnnotationTypePattern type) {
		super();
		this.isThis = isThis;
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#match(org.aspectj.weaver.Shadow)
	 */
	public FuzzyBoolean match(Shadow shadow) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings)
	 */
	protected void resolveBindings(IScope scope, Bindings bindings) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindingsFromRTTI()
	 */
	protected void resolveBindingsFromRTTI() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#concretize1(org.aspectj.weaver.ResolvedTypeX, org.aspectj.weaver.IntMap)
	 */
	protected Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#findResidue(org.aspectj.weaver.Shadow, org.aspectj.weaver.patterns.ExposedState)
	 */
	public Test findResidue(Shadow shadow, ExposedState state) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		// TODO Auto-generated method stub

	}

}
