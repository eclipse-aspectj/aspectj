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

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WithinAnnotationPointcut extends NameBindingPointcut {

	private AnnotationTypePattern annotationTypePattern;
	private ShadowMunger munger;
	
	/**
	 * 
	 */
	public WithinAnnotationPointcut(AnnotationTypePattern type) {
		super();
		this.annotationTypePattern = type;
	}
	
	public WithinAnnotationPointcut(AnnotationTypePattern type, ShadowMunger munger) {
	    this(type);
	    this.munger = munger;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
	    return annotationTypePattern.matches(info.getType());
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#match(org.aspectj.weaver.Shadow)
	 */
	public FuzzyBoolean match(Shadow shadow) {
		ResolvedTypeX enclosingType = shadow.getIWorld().resolve(shadow.getEnclosingType(),true);
		if (enclosingType == ResolvedTypeX.MISSING) {
			IMessage msg = new Message(
			    WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_WITHINPCD,
			    		              shadow.getEnclosingType().getName()),
				shadow.getSourceLocation(),true,new ISourceLocation[]{getSourceLocation()});
			shadow.getIWorld().getMessageHandler().handleMessage(msg);
		}
		return annotationTypePattern.matches(enclosingType);
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
		Pointcut ret = new WithinAnnotationPointcut(newType, bindings.getEnclosingAdvice());
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
			Var var = shadow.getWithinAnnotationVar(annotationType);
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
		s.writeByte(Pointcut.ATWITHIN);
		annotationTypePattern.write(s);
		writeLocation(s);
	}

	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		AnnotationTypePattern type = AnnotationTypePattern.read(s, context);
		WithinAnnotationPointcut ret = new WithinAnnotationPointcut((ExactAnnotationTypePattern)type);
		ret.readLocation(context, s);
		return ret;
	}
	
	/* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof WithinAnnotationPointcut)) return false;
        WithinAnnotationPointcut other = (WithinAnnotationPointcut) obj;
        return other.annotationTypePattern.equals(this.annotationTypePattern);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return 17 + 19*annotationTypePattern.hashCode();
    }
    
	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("@within(");
		buf.append(annotationTypePattern.toString());
		buf.append(")");
		return buf.toString();
    }
}
