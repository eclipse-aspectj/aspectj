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

public class AndPointcut extends Pointcut {
	Pointcut left, right;  // exposed for testing

	private Set couldMatchKinds;
	
	public AndPointcut(Pointcut left, Pointcut right) {
		super();
		this.left = left;
		this.right = right;
		this.pointcutKind = AND;
		setLocation(left.getSourceContext(), left.getStart(), right.getEnd());
		couldMatchKinds = new HashSet();
		couldMatchKinds.addAll(left.couldMatchKinds());
		couldMatchKinds.retainAll(right.couldMatchKinds());
	}
	
	public Set couldMatchKinds() {
		return couldMatchKinds;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return left.fastMatch(type).and(right.fastMatch(type));
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		FuzzyBoolean leftMatch = left.match(shadow);
		if (leftMatch.alwaysFalse()) return leftMatch;
		return leftMatch.and(right.match(shadow));
	}
	
	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart encJP) {
		return left.match(jp,encJP).and(right.match(jp,encJP));
	}
	
	public FuzzyBoolean match(JoinPoint.StaticPart jpsp) {
		return left.match(jpsp).and(right.match(jpsp));
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.tools.PointcutExpression#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		return left.matchesDynamically(thisObject,targetObject,args) &&
		       right.matchesDynamically(thisObject,targetObject,args);
	}	

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(
			String joinpointKind, Member member, Class thisClass,
			Class targetClass, Member withinCode) {
		return left.matchesStatically(joinpointKind,member,thisClass,targetClass,withinCode)
		       .and(
		       right.matchesStatically(joinpointKind,member,thisClass,targetClass,withinCode));
	}
	
	public String toString() {
		return "(" + left.toString() + " && " + right.toString() + ")";
	}
	
	public boolean equals(Object other) { 
		if (!(other instanceof AndPointcut)) return false;
		AndPointcut o = (AndPointcut)other;
		return o.left.equals(left) && o.right.equals(right);
	}
    
    public int hashCode() {
        int result = 19;
        result = 37*result + left.hashCode();
        result = 37*result + right.hashCode();
        return result;
    }

	public void resolveBindings(IScope scope, Bindings bindings) {
		left.resolveBindings(scope, bindings);
		right.resolveBindings(scope, bindings);
	}
	
	public void resolveBindingsFromRTTI() {
		left.resolveBindingsFromRTTI();
		right.resolveBindingsFromRTTI();
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.AND);
		left.write(s);
		right.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		AndPointcut ret = new AndPointcut(Pointcut.read(s, context), Pointcut.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}


	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return Test.makeAnd(left.findResidue(shadow, state), right.findResidue(shadow, state));
	}

	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		AndPointcut ret =  new AndPointcut(left.concretize(inAspect, bindings),
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
