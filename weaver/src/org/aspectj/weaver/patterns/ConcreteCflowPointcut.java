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
import java.util.Set;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.bcel.BcelCflowAccessVar;


public class ConcreteCflowPointcut extends Pointcut {
	private Member cflowField;
	List/*Slot*/ slots; // exposed for testing
    boolean usesCounter;
	
    // Can either use a counter or a stack to implement cflow.
	public ConcreteCflowPointcut(Member cflowField, List slots,boolean usesCounter) {
		this.cflowField  = cflowField;
		this.slots       = slots;
		this.usesCounter = usesCounter;
		this.pointcutKind = CFLOW;
	}
    
	public Set couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS;
	}
	
    public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}
	
	public FuzzyBoolean fastMatch(Class targetType) {
		return FuzzyBoolean.MAYBE;
	}
    
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		//??? this is not maximally efficient
		return FuzzyBoolean.MAYBE;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		throw new UnsupportedOperationException("cflow pointcut matching not supported by this operation");
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(
			String joinpointKind, java.lang.reflect.Member member,
			Class thisClass, Class targetClass,
			java.lang.reflect.Member withinCode) {
		throw new UnsupportedOperationException("cflow pointcut matching not supported by this operation");
	}

	// used by weaver when validating bindings
	public int[] getUsedFormalSlots() {
		if (slots == null) return new int[0];
		int[] indices = new int[slots.size()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = ((Slot)slots.get(i)).formalIndex;
		}
		return indices;
	}
	
	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("unimplemented");
	}


	public void resolveBindings(IScope scope, Bindings bindings) {
		throw new RuntimeException("unimplemented");
	}
	
	public void resolveBindingsFromRTTI() {
		throw new RuntimeException("unimplemented");
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof ConcreteCflowPointcut)) return false;
		ConcreteCflowPointcut o = (ConcreteCflowPointcut)other;
		return o.cflowField.equals(this.cflowField);
	}
    public int hashCode() {
        int result = 17;
        result = 37*result + cflowField.hashCode();
        return result;
    }
	public String toString() {
		return "concretecflow(" + cflowField + ")";
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		//System.out.println("find residue: " + this);
		if (usesCounter) {
			return Test.makeFieldGetCall(cflowField, cflowCounterIsValidMethod, Expr.NONE);
		} else {
		  if (slots != null) { // null for cflows managed by counters
		    for (Iterator i = slots.iterator(); i.hasNext();) {
		 	  Slot slot = (Slot) i.next();
			  //System.out.println("slot: " + slot.formalIndex);
			  state.set(slot.formalIndex, 
				new BcelCflowAccessVar(slot.formalType, cflowField, slot.arrayIndex));
		    }
		  }
		  return Test.makeFieldGetCall(cflowField, cflowStackIsValidMethod, Expr.NONE);
		}
	}
	
	private static final Member cflowStackIsValidMethod = 
		MemberImpl.method(UnresolvedType.forName(NameMangler.CFLOW_STACK_TYPE), 0, "isValid", "()Z");

	private static final Member cflowCounterIsValidMethod = 
		MemberImpl.method(UnresolvedType.forName(NameMangler.CFLOW_COUNTER_TYPE), 0, "isValid", "()Z");

	
	public Pointcut concretize1(ResolvedType inAspect, IntMap bindings) {
		throw new RuntimeException("unimplemented");
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


	public static class Slot {
		int formalIndex;
		ResolvedType formalType;
		int arrayIndex;
		
		public Slot(
			int formalIndex,
			ResolvedType formalType,
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
