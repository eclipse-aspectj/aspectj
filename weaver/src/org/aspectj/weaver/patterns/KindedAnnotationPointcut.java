/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

/**
 * A KindedAnnotationPointcut matches iff the kind of a join point 
 * matches the kind of the pointcut (with no distinction between
 * method and constructor for call and execution), AND if the
 * member (field, method or constructor) has an annotation of the
 * given type.
 */
public class KindedAnnotationPointcut extends NameBindingPointcut {

	private Shadow.Kind kind;
	private AnnotationTypePattern type;
    private ShadowMunger munger = null; // only set after concretization
	
	public KindedAnnotationPointcut(Shadow.Kind kind, AnnotationTypePattern type) {
		super();
		this.kind = kind;
		this.type = type;
		this.pointcutKind = Pointcut.ATKINDED;
	}

	public KindedAnnotationPointcut(Shadow.Kind kind, AnnotationTypePattern type, ShadowMunger munger) {
		this(kind,type);
		this.munger = munger;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		if (info.getKind() != null) {
			if (info.getKind() != kind) {
				// no distinction between method and constructors
				if ((info.getKind() == Shadow.ConstructorExecution) &&
					 kind == Shadow.MethodExecution) {
					return FuzzyBoolean.MAYBE;
				} 
				if ((info.getKind() == Shadow.ConstructorCall) &&
						 kind == Shadow.MethodCall) {
						return FuzzyBoolean.MAYBE;
				} 				
			} else {
				return FuzzyBoolean.NO;
			}
    	}
		return FuzzyBoolean.MAYBE;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#match(org.aspectj.weaver.Shadow)
	 */
	public FuzzyBoolean match(Shadow shadow) {
		if (!couldMatch(shadow)) return FuzzyBoolean.NO;
		return type.matches(shadow.getSignature());
	}
	
	private boolean couldMatch(Shadow shadow) {
		Shadow.Kind kindToMatch = shadow.getKind();
		if (kindToMatch == Shadow.ConstructorExecution) kindToMatch = Shadow.MethodExecution;
		if (kindToMatch == Shadow.ConstructorCall) kindToMatch = Shadow.MethodCall;
		return (kindToMatch == kind);		
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings)
	 */
	protected void resolveBindings(IScope scope, Bindings bindings) {
		type = type.resolveBindings(scope,bindings,true);
		// must be either a Var, or an annotation type pattern
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindingsFromRTTI()
	 */
	protected void resolveBindingsFromRTTI() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#concretize1(org.aspectj.weaver.ResolvedTypeX, org.aspectj.weaver.IntMap)
	 */
	protected Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		AnnotationTypePattern newType = type.remapAdviceFormals(bindings);		
		Pointcut ret = new KindedAnnotationPointcut(kind, newType, bindings.getEnclosingAdvice());
        ret.copyLocationFrom(this);
        return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#findResidue(org.aspectj.weaver.Shadow, org.aspectj.weaver.patterns.ExposedState)
	 */
	public Test findResidue(Shadow shadow, ExposedState state) {
		if (!couldMatch(shadow)) return Literal.FALSE;
		
		if (type instanceof BindingAnnotationTypePattern) {
			BindingAnnotationTypePattern btp = (BindingAnnotationTypePattern)type;
			TypeX annotationType = btp.annotationType;
			Var var = shadow.getKindedAnnotationVar(annotationType);
			if (var == null) return Literal.FALSE;
			// Check if we have already bound something to this formal
			if (state.get(btp.getFormalIndex())!=null) {
				ISourceLocation pcdSloc = getSourceLocation(); 
				ISourceLocation shadowSloc = shadow.getSourceLocation();
				Message errorMessage = new Message(
					"Cannot use @pointcut to match at this location and bind a formal to type '"+var.getType()+
					"' - the formal is already bound to type '"+state.get(btp.getFormalIndex()).getType()+"'"+
					".  The secondary source location points to the problematic binding.",
					shadowSloc,true,new ISourceLocation[]{pcdSloc}); 
				shadow.getIWorld().getMessageHandler().handleMessage(errorMessage);
				state.setErroneousVar(btp.getFormalIndex());
			}
			state.set(btp.getFormalIndex(),var);
		} 
		return Literal.TRUE;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.ATKINDED);
		kind.write(s);
		type.write(s);
		writeLocation(s);
	}

	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		Shadow.Kind kind = Shadow.Kind.read(s);
		AnnotationTypePattern type = AnnotationTypePattern.read(s, context);
		KindedAnnotationPointcut ret = new KindedAnnotationPointcut(kind, type);
		ret.readLocation(context, s);
		return ret;
	}

	public boolean equals(Object other) {
		if (!(other instanceof KindedAnnotationPointcut)) return false;
		KindedAnnotationPointcut o = (KindedAnnotationPointcut)other;
		return o.kind == this.kind && o.type.equals(this.type);
	}
    
    public int hashCode() {
        int result = 17;
        result = 37*result + kind.hashCode();
        result = 37*result + type.hashCode();
        return result;
    }
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(kind.getSimpleName());
		buf.append("(");
		buf.append(type.toString());
		buf.append(")");
		return buf.toString();
	}

}
