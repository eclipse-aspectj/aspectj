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


package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.ast.Test;

public class OrPointcut extends Pointcut {
	private Pointcut left, right;
	private Set couldMatchKinds;

	public OrPointcut(Pointcut left, Pointcut right) {
		super();
		this.left = left;
		this.right = right;
		setLocation(left.getSourceContext(), left.getStart(), right.getEnd());
		this.pointcutKind = OR;
		this.couldMatchKinds = new HashSet(left.couldMatchKinds());
		this.couldMatchKinds.addAll(right.couldMatchKinds());
	}

	public Set couldMatchKinds() {
		return couldMatchKinds;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return left.fastMatch(type).or(right.fastMatch(type));
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		FuzzyBoolean leftMatch = left.match(shadow);
		if (leftMatch.alwaysTrue()) return leftMatch;
		return leftMatch.or(right.match(shadow));
	}
	
	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart encJP) {
		return left.match(jp,encJP).or(right.match(jp,encJP));
	}
	
	public FuzzyBoolean match(JoinPoint.StaticPart jpsp) {
		return left.match(jpsp).or(right.match(jpsp));
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		return left.matchesDynamically(thisObject,targetObject,args)
		       ||
			   right.matchesDynamically(thisObject,targetObject,args);
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(
			String joinpointKind, Member member, Class thisClass,
			Class targetClass, Member withinCode) {
		return left.matchesStatically(joinpointKind,member,thisClass,targetClass,withinCode)
		       .or(
		       right.matchesStatically(joinpointKind,member,thisClass,targetClass,withinCode));
	}
	public String toString() {
		return "(" + left.toString() + " || " + right.toString() + ")";
	}
	
	public boolean equals(Object other) { 
		if (!(other instanceof OrPointcut)) return false;
		OrPointcut o = (OrPointcut)other;
		return o.left.equals(left) && o.right.equals(right);
	}
    public int hashCode() {
        int result = 31;
        result = 37*result + left.hashCode();
        result = 37*result + right.hashCode();
        return result;
    }
	/**
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(IScope, Bindings)
	 */
	public void resolveBindings(IScope scope, Bindings bindings) {
		Bindings old = bindings == null ? null : bindings.copy();
		
		left.resolveBindings(scope, bindings);
		right.resolveBindings(scope, old);
		if (bindings != null) bindings.checkEquals(old, scope);
		
	}
	
	public void resolveBindingsFromRTTI() {
		left.resolveBindingsFromRTTI();
		right.resolveBindingsFromRTTI();
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.OR);
		left.write(s);
		right.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		OrPointcut ret = new OrPointcut(Pointcut.read(s, context), Pointcut.read(s, context));
		ret.readLocation(context, s);
		return ret;
	
	}
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return Test.makeOr(left.findResidue(shadow, state), right.findResidue(shadow, state));
	}
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		Pointcut ret = new OrPointcut(left.concretize(inAspect, bindings),
								right.concretize(inAspect, bindings));
		ret.copyLocationFrom(this);
		return ret;
	}
	
	public Pointcut getLeft() {
		return left;
	}

	public Pointcut getRight() {
		return right;
	}
}
