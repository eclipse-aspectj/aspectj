/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * Representation of a shadow munger for a declare error or warning declaration.
 * 
 * @author Andy Clement
 */
public class Checker extends ShadowMunger {

	private final String message;
	private final boolean isError; // if not error then it is a warning
	private volatile int hashCode = -1;

	/**
	 * Create a Checker for a deow.
	 * 
	 * @param deow the declare error or warning for which to create the checker munger
	 */
	public Checker(DeclareErrorOrWarning deow) {
		super(deow.getPointcut(), deow.getStart(), deow.getEnd(), deow.getSourceContext());
		this.message = deow.getMessage();
		this.isError = deow.isError();
	}

	/**
	 * Only used when filling in a parameterized Checker.
	 * 
	 * @param pc the pointcut
	 * @param start the start
	 * @param end the end
	 * @param context the source context
	 * @param message the message string
	 * @param isError whether it is an error or just a warning
	 */
	private Checker(Pointcut pc, int start, int end, ISourceContext context, String message, boolean isError) {
		super(pc, start, end, context);
		this.message = message;
		this.isError = isError;
	}

	public boolean isError() {
		return isError;
	}

	/**
	 * Not supported for a Checker
	 */
	public void specializeOn(Shadow shadow) {
		throw new RuntimeException("illegal state");
	}

	/**
	 * Not supported for a Checker
	 */
	public void implementOn(Shadow shadow) {
		throw new RuntimeException("illegal state");
	}

	/**
	 * Determine if the Checker matches at a shadow. If it does then we can immediately report the message. There (currently) can
	 * never be a non-statically determinable match.
	 * 
	 * @param shadow the shadow which to match against
	 * @param world the world through which to access message handlers
	 */
	public boolean match(Shadow shadow, World world) {
		if (super.match(shadow, world)) {
			world.reportCheckerMatch(this, shadow);
		}
		return false;
	}

	// FIXME what the hell?
	public int compareTo(Object other) {
		return 0;
	}

	// FIXME Alex: ATAJ is that ok in all cases ?
	/**
	 * Default to true
	 * 
	 * @return
	 */
	public boolean mustCheckExceptions() {
		return true;
	}

	public Collection getThrownExceptions() {
		return Collections.EMPTY_LIST;
	}

	// FIXME this perhaps ought to take account of the other fields in advice ...
	public boolean equals(Object other) {
		if (!(other instanceof Checker)) {
			return false;
		}
		Checker o = (Checker) other;
		return o.isError == isError && ((o.pointcut == null) ? (pointcut == null) : o.pointcut.equals(pointcut));
	}

	public int hashCode() {
		if (hashCode == -1) {
			int result = 17;
			result = 37 * result + (isError ? 1 : 0);
			result = 37 * result + ((pointcut == null) ? 0 : pointcut.hashCode());
			hashCode = result;
		}
		return hashCode;
	}

	public ShadowMunger parameterizeWith(ResolvedType declaringType, Map typeVariableMap) {
		Checker ret = new Checker(this.pointcut.parameterizeWith(typeVariableMap, declaringType.getWorld()), this.start, this.end,
				this.sourceContext, this.message, this.isError);
		return ret;
	}

	/**
	 * Concretize this Checker by concretizing the pointcut.
	 * 
	 */
	public ShadowMunger concretize(ResolvedType theAspect, World world, PerClause clause) {
		this.pointcut = this.pointcut.concretize(theAspect, getDeclaringType(), 0, this);
		this.hashCode = -1;
		return this;
	}

	public String getMessage() {
		return this.message;
	}

}
