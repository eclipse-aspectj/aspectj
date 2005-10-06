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
import java.util.Map;

import org.aspectj.asm.AsmManager;
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
	private ISourceLocation sourceLocation;
	private String handle = null;
	private ResolvedType declaringType;  // the type that declared this munger.

	
	public ShadowMunger(Pointcut pointcut, int start, int end, ISourceContext sourceContext) {
		this.pointcut = pointcut;
		this.start = start;
		this.end = end;
		this.sourceContext = sourceContext;
	}
	
	public abstract ShadowMunger concretize(ResolvedType fromType, World world, PerClause clause);	

    public abstract void specializeOn(Shadow shadow);
    public abstract void implementOn(Shadow shadow);
	
	/**
	 * All overriding methods should call super
	 */
    public boolean match(Shadow shadow, World world) {
    	return pointcut.match(shadow).maybeTrue();
    }
    
    public abstract ShadowMunger parameterizeWith(Map typeVariableMap); 
    
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
    	if (sourceLocation == null) {
	    	if (sourceContext != null) {
				sourceLocation = sourceContext.makeSourceLocation(this);
	    	}
    	}
    	return sourceLocation;
    }

	public String getHandle() {
		if (null == handle) {
			ISourceLocation sl = getSourceLocation();
			if (sl != null) {
				handle = AsmManager.getDefault().getHandleProvider().createHandleIdentifier(
				            sl.getSourceFile(),
				            sl.getLine(),
				            sl.getColumn(),
							sl.getOffset());
			}
		}
		return handle;
	}

	// ---- fields
	
    public static final ShadowMunger[] NONE = new ShadowMunger[0];



	public Pointcut getPointcut() {
		return pointcut;
	}
	
	// pointcut may be updated during rewriting...
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
	 * Invoked when the shadow munger of a resolved type are processed.
	 * @param aType
	 */
	public void setDeclaringType(ResolvedType aType) {
		this.declaringType = aType;
	}
	
	public ResolvedType getDeclaringType() {
		return this.declaringType;
	}
	
	/**
	 * @return a Collection of ResolvedType for all checked exceptions that
	 *          might be thrown by this munger
	 */
	public abstract Collection getThrownExceptions();

    /**
     * Does the munger has to check that its exception are accepted by the shadow ?
     * ATAJ: It s not the case for @AJ around advice f.e. that can throw Throwable, even if the advised
     * method does not throw any exceptions. 
     * @return true if munger has to check that its exceptions can be throwned based on the shadow
     */
    public abstract boolean mustCheckExceptions();
}
