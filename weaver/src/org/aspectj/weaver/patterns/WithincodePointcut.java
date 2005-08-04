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

import org.aspectj.bridge.MessageUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.runtime.reflect.Factory;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

public class WithincodePointcut extends Pointcut {
	private SignaturePattern signature;
    private static final Set matchedShadowKinds = new HashSet();
    static {
    	matchedShadowKinds.addAll(Shadow.ALL_SHADOW_KINDS);
    	for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			if (Shadow.SHADOW_KINDS[i].isEnclosingKind()) 
				matchedShadowKinds.remove(Shadow.SHADOW_KINDS[i]);
		}
    	// these next two are needed for inlining of field initializers
    	matchedShadowKinds.add(Shadow.ConstructorExecution);
    	matchedShadowKinds.add(Shadow.Initialization);
    }
	
	public WithincodePointcut(SignaturePattern signature) {
		this.signature = signature;
		this.pointcutKind = WITHINCODE;
	}

    public SignaturePattern getSignature() {
        return signature;
    }

	public Set couldMatchKinds() {
		return matchedShadowKinds;
	}
	
	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}
	
	public FuzzyBoolean fastMatch(Class targetType) {
		return FuzzyBoolean.MAYBE;
	}

    
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		//This will not match code in local or anonymous classes as if
		//they were withincode of the outer signature
		return FuzzyBoolean.fromBoolean(
			signature.matches(shadow.getEnclosingCodeSignature(), shadow.getIWorld()));
	}

	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart encJP) {
		return FuzzyBoolean.fromBoolean(signature.matches(encJP));
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(String joinpointKind, Member member,
			Class thisClass, Class targetClass, Member withinCode) {
		if (withinCode == null) return FuzzyBoolean.NO;
		return FuzzyBoolean.fromBoolean(signature.matches(Factory.makeEncSJP(withinCode)));
	}
	
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.WITHINCODE);
		signature.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		WithincodePointcut ret = new WithincodePointcut(SignaturePattern.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		signature = signature.resolveBindings(scope, bindings);
		
		// look for inappropriate use of parameterized types and tell user...
		HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor 
			visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
		signature.getDeclaringType().traverse(visitor, null);
		if (visitor.wellHasItThen/*?*/()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.WITHINCODE_DOESNT_SUPPORT_PARAMETERIZED_DECLARING_TYPES),
					getSourceLocation()));
		}						
		
		visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
		signature.getThrowsPattern().traverse(visitor, null);
		if (visitor.wellHasItThen/*?*/()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.NO_GENERIC_THROWABLES),
					getSourceLocation()));
		}					
	}
	
	public void resolveBindingsFromRTTI() {
		signature = signature.resolveBindingsFromRTTI();
	}

	public void postRead(ResolvedType enclosingType) {
		signature.postRead(enclosingType);
	}

	public boolean equals(Object other) {
		if (!(other instanceof WithincodePointcut)) return false;
		WithincodePointcut o = (WithincodePointcut)other;
		return o.signature.equals(this.signature);
	}
    public int hashCode() {
        int result = 43;
        result = 37*result + signature.hashCode();
        return result;
    }

	public String toString() {
		return "withincode(" + signature + ")";
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	
	public Pointcut concretize1(ResolvedType inAspect, IntMap bindings) {
		Pointcut ret = new WithincodePointcut(signature);
		ret.copyLocationFrom(this);
		return ret;
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
