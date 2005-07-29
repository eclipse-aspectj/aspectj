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
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

public class WithinPointcut extends Pointcut {
	private TypePattern typePattern;
	
	public WithinPointcut(TypePattern type) {
		this.typePattern = type;
		this.pointcutKind = WITHIN;
	}

    public TypePattern getTypePattern() {
        return typePattern;
    }

	private FuzzyBoolean isWithinType(ResolvedType type) {
		while (type != null) {
			if (typePattern.matchesStatically(type)) {			    
				return FuzzyBoolean.YES;
			}
			type = type.getDeclaringType();
		}
		return FuzzyBoolean.NO;
	}

	public Set couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS;
	}
	
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
	    if (typePattern.annotationPattern instanceof AnyAnnotationTypePattern) {
	        return isWithinType(info.getType());
	    }
	    return FuzzyBoolean.MAYBE;
	}
	
	public FuzzyBoolean fastMatch(Class targetType) {
		return FuzzyBoolean.MAYBE;
	}
    
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		ResolvedType enclosingType = shadow.getIWorld().resolve(shadow.getEnclosingType(),true);
		if (enclosingType == ResolvedType.MISSING) {
			IMessage msg = new Message(
			    WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_WITHINPCD,
			    		              shadow.getEnclosingType().getName()),
				shadow.getSourceLocation(),true,new ISourceLocation[]{getSourceLocation()});
			shadow.getIWorld().getMessageHandler().handleMessage(msg);
		}
		typePattern.resolve(shadow.getIWorld());
		return isWithinType(enclosingType);
	}

	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart encJp) {
		return isWithinType(encJp.getSignature().getDeclaringType());
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
	public FuzzyBoolean matchesStatically(
			String joinpointKind, Member member, Class thisClass,
			Class targetClass, Member withinCode) {
		if ((member != null) &&
			!(joinpointKind.equals(Shadow.ConstructorCall.getName()) ||
			  joinpointKind.equals(Shadow.MethodCall.getName()) ||
			  joinpointKind.equals(Shadow.FieldGet.getName()) ||
			  joinpointKind.equals(Shadow.FieldSet.getName()))
			) {
			return isWithinType(member.getDeclaringClass());
		} else {
			return isWithinType(thisClass);
		}
	}
		
	private FuzzyBoolean isWithinType(Class type) {
		while (type != null) {
			if (typePattern.matchesStatically(type)) {
				return FuzzyBoolean.YES;
			} 
			type = type.getDeclaringClass();
		}		
		return FuzzyBoolean.NO;
	}

	
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.WITHIN);
		typePattern.write(s);
		writeLocation(s);
	}
	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		TypePattern type = TypePattern.read(s, context);
		WithinPointcut ret = new WithinPointcut(type);
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		typePattern = typePattern.resolveBindings(scope, bindings, false, false);
		
		// look for parameterized type patterns which are not supported...
		HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor 
			visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
		typePattern.traverse(visitor, null);
		if (visitor.wellHasItThen/*?*/()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.WITHIN_PCD_DOESNT_SUPPORT_PARAMETERS),
				getSourceLocation()));
		}		
	}

	public void resolveBindingsFromRTTI() {
		typePattern = typePattern.resolveBindingsFromRTTI(false,false);
	}
	
	public void postRead(ResolvedType enclosingType) {
		typePattern.postRead(enclosingType);
	}

	public boolean couldEverMatchSameJoinPointsAs(WithinPointcut other) {
		return typePattern.couldEverMatchSameTypesAs(other.typePattern);
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof WithinPointcut)) return false;
		WithinPointcut o = (WithinPointcut)other;
		return o.typePattern.equals(this.typePattern);
	}
    public int hashCode() {
        int result = 43;
        result = 37*result + typePattern.hashCode();
        return result;
    }

	public String toString() {
		return "within(" + typePattern + ")";
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	
	public Pointcut concretize1(ResolvedType inAspect, IntMap bindings) {
		Pointcut ret = new WithinPointcut(typePattern);
		ret.copyLocationFrom(this);
		return ret;
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
