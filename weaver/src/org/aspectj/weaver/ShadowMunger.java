/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.util.Collection;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.PartialOrder;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * For every shadow munger, nothing can be done with it until it is concretized.  Then...
 * 
 * (Then we call fast match.)
 * 
 * For every shadow munger, for every shadow, 
 * first match is called, 
 * then (if match returned true) the shadow munger is specialized for the shadow, 
 *     which may modify state.  
 * Then implement is called. 
 */

public abstract class ShadowMunger implements PartialOrder.PartialComparable, IHasPosition {
	protected Pointcut pointcut;
	
	// these three fields hold the source location of this munger
	protected int start, end;
	protected ISourceContext sourceContext;

	
	public ShadowMunger(Pointcut pointcut, int start, int end, ISourceContext sourceContext) {
		this.pointcut = pointcut;
		this.start = start;
		this.end = end;
		this.sourceContext = sourceContext;
	}
	
	public abstract ShadowMunger concretize(ResolvedTypeX fromType, World world, PerClause clause);	

    public abstract void specializeOn(Shadow shadow);
    public abstract void implementOn(Shadow shadow);
	
	/**
	 * All overriding methods should call super
	 */
    public boolean match(Shadow shadow, World world) {
    	return pointcut.match(shadow).maybeTrue();
    }
    
	public int fallbackCompareTo(Object other) {
		return toString().compareTo(toString());
	}
	
	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}
	
    public ISourceLocation getSourceLocation() {
    	//System.out.println("get context: " + this + " is " + sourceContext);
    	if (sourceContext == null) {
    		//System.err.println("no context: " + this);
    		return null;
    	}
    	return sourceContext.makeSourceLocation(this);
    }

	// ---- fields
	
    public static final ShadowMunger[] NONE = new ShadowMunger[0];



	public Pointcut getPointcut() {
		return pointcut;
	}


	/**
	 * @return a Collection of ResolvedTypeX for all checked exceptions that
	 *          might be thrown by this munger
	 */
	public abstract Collection getThrownExceptions();
}
