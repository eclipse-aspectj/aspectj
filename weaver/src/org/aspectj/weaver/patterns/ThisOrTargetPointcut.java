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

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

// 

/**
 * Corresponds to target or this pcd.
 * 
 * <p>type is initially a WildTypePattern.  If it stays that way, it's a this(Foo) 
 * type deal.
 * however, the resolveBindings method may convert it to a BindingTypePattern, 
 * in which
 * case, it's a this(foo) type deal.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class ThisOrTargetPointcut extends NameBindingPointcut {
	private boolean isThis;
	private TypePattern type;
	
	public ThisOrTargetPointcut(boolean isThis, TypePattern type) {
		this.isThis = isThis;
		this.type = type;
		this.pointcutKind = THIS_OR_TARGET;
	}
	
	public boolean isThis() { return isThis; }
	
	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}
	
	private boolean couldMatch(Shadow shadow) {
		return isThis ? shadow.hasThis() : shadow.hasTarget();
	}
    
	public FuzzyBoolean match(Shadow shadow) {
		if (!couldMatch(shadow)) return FuzzyBoolean.NO;
		TypeX typeToMatch = isThis ? shadow.getThisType() : shadow.getTargetType(); 
		//if (typeToMatch == ResolvedTypeX.MISSING) return FuzzyBoolean.NO;
		
		return type.matches(typeToMatch.resolve(shadow.getIWorld()), TypePattern.DYNAMIC);
	}

	public FuzzyBoolean match(JoinPoint jp, JoinPoint.StaticPart encJP) {
		Object toMatch = isThis ? jp.getThis() : jp.getTarget(); 
		if (toMatch == null) return FuzzyBoolean.NO;
		return type.matches(toMatch.getClass(), TypePattern.DYNAMIC);
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		Object toMatch = isThis ? thisObject : targetObject; 
		if (toMatch == null) return false;
		return type.matchesSubtypes(toMatch.getClass());
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(String joinpointKind, Member member,
			Class thisClass, Class targetClass, Member withinCode) {
		Class staticType = isThis ? thisClass : targetClass; 
		if (joinpointKind.equals(Shadow.StaticInitialization.getName())) {
			return FuzzyBoolean.NO;  // no this or target at these jps
		}
		return(((ExactTypePattern)type).willMatchDynamically(staticType));
	}
	
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.THIS_OR_TARGET);
		s.writeBoolean(isThis);
		type.write(s);
		writeLocation(s);
	}
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		boolean isThis = s.readBoolean();
		TypePattern type = TypePattern.read(s, context);
		ThisOrTargetPointcut ret = new ThisOrTargetPointcut(isThis, type);
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		type = type.resolveBindings(scope, bindings, true, true);
		
		// ??? handle non-formal
	}
	
	public void resolveBindingsFromRTTI() {
		type = type.resolveBindingsFromRTTI(true,true);
	}
	
	public void postRead(ResolvedTypeX enclosingType) {
		type.postRead(enclosingType);
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof ThisOrTargetPointcut)) return false;
		ThisOrTargetPointcut o = (ThisOrTargetPointcut)other;
		return o.isThis == this.isThis && o.type.equals(this.type);
	}
    public int hashCode() {
        int result = 17;
        result = 37*result + (isThis ? 0 : 1);
        result = 37*result + type.hashCode();
        return result;
    }
	public String toString() {
		return (isThis ? "this(" : "target(") + type + ")";
	}

	public Test findResidue(Shadow shadow, ExposedState state) {
		if (!couldMatch(shadow)) return Literal.FALSE;
		
		if (type == TypePattern.ANY) return Literal.TRUE;
		
		Var var = isThis ? shadow.getThisVar() : shadow.getTargetVar();	

		if (type instanceof BindingTypePattern) {
		  BindingTypePattern btp = (BindingTypePattern)type;
		  // Check if we have already bound something to this formal
		  if (state.get(btp.getFormalIndex())!=null) {
		  	ISourceLocation pcdSloc = getSourceLocation(); 
		  	ISourceLocation shadowSloc = shadow.getSourceLocation();
			Message errorMessage = new Message(
				"Cannot use "+(isThis?"this()":"target()")+" to match at this location and bind a formal to type '"+var.getType()+
				"' - the formal is already bound to type '"+state.get(btp.getFormalIndex()).getType()+"'"+
				".  The secondary source location points to the problematic "+(isThis?"this()":"target()")+".",
				shadowSloc,true,new ISourceLocation[]{pcdSloc}); 
			shadow.getIWorld().getMessageHandler().handleMessage(errorMessage);
			state.setErroneousVar(btp.getFormalIndex());
			//return null;
		  }
		}
		return exposeStateForVar(var, type, state, shadow.getIWorld());
	}

	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		if (isDeclare(bindings.getEnclosingAdvice())) {
		  // Enforce rule about which designators are supported in declare
		  inAspect.getWorld().showMessage(IMessage.ERROR,
		  		WeaverMessages.format(WeaverMessages.THIS_OR_TARGET_IN_DECLARE,isThis?"this":"target"),
				bindings.getEnclosingAdvice().getSourceLocation(), null);
		  return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		
		TypePattern newType = type.remapAdviceFormals(bindings);
		if (inAspect.crosscuttingMembers != null) {
			inAspect.crosscuttingMembers.exposeType(newType.getExactType());
		}
		
		return new ThisOrTargetPointcut(isThis, newType);
	}

}
