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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Member;

import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Test;

public class NotPointcut extends Pointcut {
	private Pointcut body;
	public NotPointcut(Pointcut left) {
		super();
		this.body = left;
	}

	public NotPointcut(Pointcut pointcut, int startPos) {
		this(pointcut);
		setLocation(pointcut.getSourceContext(), startPos, pointcut.getEnd());		
	}

	public Pointcut getNegatedPointcut() { return body; }

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return body.fastMatch(type).not();
	}

	public FuzzyBoolean match(Shadow shadow) {
		return body.match(shadow).not();
	}

	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart encJP) {
		return body.match(jp,encJP).not();
	}

	public FuzzyBoolean match(JoinPoint.StaticPart jpsp) {
		return body.match(jpsp).not();
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		return !body.matchesDynamically(thisObject,targetObject,args);
	}
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(
			String joinpointKind, Member member, Class thisClass,
			Class targetClass, Member withinCode) {
		return body.matchesStatically(joinpointKind,member,thisClass,targetClass,withinCode).not();
	}

	public String toString() {
		return "!" + body.toString();

	}
	
	public boolean equals(Object other) { 
		if (!(other instanceof NotPointcut)) return false;
		NotPointcut o = (NotPointcut)other;
		return o.body.equals(body);
	}
    public int hashCode() {
        return 37*23 + body.hashCode();
    }


	public void resolveBindings(IScope scope, Bindings bindings) {
		//Bindings old = bindings.copy();
		
		//Bindings newBindings = new Bindings(bindings.size());
		
		
		body.resolveBindings(scope, null);
		
		//newBindings.checkEmpty(scope, "negation does not allow binding");
		//bindings.checkEquals(old, scope);
		
	}
	
	public void resolveBindingsFromRTTI() {
		body.resolveBindingsFromRTTI();
	}
	

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.NOT);
		body.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		NotPointcut ret = new NotPointcut(Pointcut.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public Test findResidue(Shadow shadow, ExposedState state) {
		return Test.makeNot(body.findResidue(shadow, state));
	}
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		return new NotPointcut(body.concretize(inAspect, bindings));
	}

}
