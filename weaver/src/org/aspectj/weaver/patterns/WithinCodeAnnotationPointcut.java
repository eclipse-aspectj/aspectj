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
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WithinCodeAnnotationPointcut extends NameBindingPointcut {

	private ExactAnnotationTypePattern annotationTypePattern;
    private ShadowMunger munger = null; // only set after concretization
	
	public WithinCodeAnnotationPointcut(ExactAnnotationTypePattern type) {
		super();
		this.annotationTypePattern =  type;
		this.pointcutKind = Pointcut.ANNOTATION;
	}

	public WithinCodeAnnotationPointcut(ExactAnnotationTypePattern type, ShadowMunger munger) {
		this(type);
		this.munger = munger;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		return FuzzyBoolean.MAYBE;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#match(org.aspectj.weaver.Shadow)
	 */
	public FuzzyBoolean match(Shadow shadow) {
		AnnotatedElement toMatchAgainst = null;
		Member member = shadow.getEnclosingCodeSignature();		
		ResolvedMember rMember = member.resolve(shadow.getIWorld());

		if (rMember == null) {
		    if (member.getName().startsWith(NameMangler.PREFIX)) {
		    	return FuzzyBoolean.NO;
			}
			shadow.getIWorld().getLint().unresolvableMember.signal(member.toString(), getSourceLocation());
			return FuzzyBoolean.NO;
		}

		toMatchAgainst = TypeX.forName(rMember.getSignature()).resolve(shadow.getIWorld());
		
		return annotationTypePattern.matches(toMatchAgainst);
	}
	

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings)
	 */
	protected void resolveBindings(IScope scope, Bindings bindings) {
		annotationTypePattern = (ExactAnnotationTypePattern) annotationTypePattern.resolveBindings(scope,bindings,true);
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
		ExactAnnotationTypePattern newType = (ExactAnnotationTypePattern) annotationTypePattern.remapAdviceFormals(bindings);		
		Pointcut ret = new WithinCodeAnnotationPointcut(newType, bindings.getEnclosingAdvice());
        ret.copyLocationFrom(this);
        return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#findResidue(org.aspectj.weaver.Shadow, org.aspectj.weaver.patterns.ExposedState)
	 */
	public Test findResidue(Shadow shadow, ExposedState state) {
		
		if (annotationTypePattern instanceof BindingAnnotationTypePattern) {
			BindingAnnotationTypePattern btp = (BindingAnnotationTypePattern)annotationTypePattern;
			TypeX annotationType = btp.annotationType;
			Var var = shadow.getWithinCodeAnnotationVar(annotationType);
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
		s.writeByte(Pointcut.ATWITHINCODE);
		annotationTypePattern.write(s);
		writeLocation(s);
	}

	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		AnnotationTypePattern type = AnnotationTypePattern.read(s, context);
		WithinCodeAnnotationPointcut ret = new WithinCodeAnnotationPointcut((ExactAnnotationTypePattern)type);
		ret.readLocation(context, s);
		return ret;
	}

	public boolean equals(Object other) {
		if (!(other instanceof WithinCodeAnnotationPointcut)) return false;
		WithinCodeAnnotationPointcut o = (WithinCodeAnnotationPointcut)other;
		return o.annotationTypePattern.equals(this.annotationTypePattern);
	}
    
    public int hashCode() {
        int result = 17;
        result = 23*result + annotationTypePattern.hashCode();
        return result;
    }
	
	public String toString() {
	    StringBuffer buf = new StringBuffer();
		buf.append("@withincode(");
		buf.append(annotationTypePattern.toString());
		buf.append(")");
		return buf.toString();
	}
}
