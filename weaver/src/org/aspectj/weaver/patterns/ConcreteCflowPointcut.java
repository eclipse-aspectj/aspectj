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
import java.util.Iterator;
import java.util.List;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.bcel.BcelCflowAccessVar;


public class ConcreteCflowPointcut extends Pointcut {
	private Member cflowStackField;
	List/*Slot*/ slots; // exposed for testing

	
	public ConcreteCflowPointcut(Member cflowStackField, List slots) {
		this.cflowStackField = cflowStackField;
		this.slots = slots;
	}
    
    public FuzzyBoolean fastMatch(ResolvedTypeX type) {
		return FuzzyBoolean.MAYBE;
	}
    
	public FuzzyBoolean match(Shadow shadow) {
		//??? this is not maximally efficient
		return FuzzyBoolean.MAYBE;
	}

	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("unimplemented");
	}


	public void resolveBindings(IScope scope, Bindings bindings) {
		throw new RuntimeException("unimplemented");
	}
	
	
	public boolean equals(Object other) {
		if (!(other instanceof ConcreteCflowPointcut)) return false;
		ConcreteCflowPointcut o = (ConcreteCflowPointcut)other;
		return o.cflowStackField.equals(this.cflowStackField);
	}
    public int hashCode() {
        int result = 17;
        result = 37*result + cflowStackField.hashCode();
        return result;
    }
	public String toString() {
		return "concretecflow(" + cflowStackField + ")";
	}

	public Test findResidue(Shadow shadow, ExposedState state) {
		//System.out.println("find residue: " + this);
		for (Iterator i = slots.iterator(); i.hasNext();) {
			Slot slot = (Slot) i.next();
			//System.out.println("slot: " + slot.formalIndex);
			state.set(slot.formalIndex, 
				new BcelCflowAccessVar(slot.formalType, cflowStackField, slot.arrayIndex));
		}
		
		return Test.makeFieldGetCall(cflowStackField, cflowStackIsValidMethod, Expr.NONE);
	}
	
	private static final Member cflowStackIsValidMethod = 
		Member.method(TypeX.forName(NameMangler.CFLOW_STACK_TYPE), 0, "isValid", "()Z");

	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		throw new RuntimeException("unimplemented");
	}


	public static class Slot {
		int formalIndex;
		ResolvedTypeX formalType;
		int arrayIndex;
		
		public Slot(
			int formalIndex,
			ResolvedTypeX formalType,
			int arrayIndex) {
			this.formalIndex = formalIndex;
			this.formalType = formalType;
			this.arrayIndex = arrayIndex;
		}
		
		public boolean equals(Object other) {
			if (!(other instanceof Slot)) return false;
			
			Slot o = (Slot)other;
			return o.formalIndex == this.formalIndex &&
				o.arrayIndex == this.arrayIndex &&
				o.formalType.equals(this.formalType);
		}
		
		public String toString() {
			return "Slot(" + formalIndex + ", " + formalType + ", " + arrayIndex + ")";
		}
	}

}
