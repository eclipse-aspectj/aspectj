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

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;
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
	}
	
	public FuzzyBoolean fastMatch(ResolvedTypeX type) {
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
		return exposeStateForVar(var, type, state, shadow.getIWorld());
	}

	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		TypePattern newType = type.remapAdviceFormals(bindings);
		if (inAspect.crosscuttingMembers != null) {
			inAspect.crosscuttingMembers.exposeType(newType.getExactType());
		}
		
		return new ThisOrTargetPointcut(isThis, newType);
	}

}
