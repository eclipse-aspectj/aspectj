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
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.TypeX;

/**
 * Matches an annotation of a given type
 */
public class ExactAnnotationTypePattern extends AnnotationTypePattern {

	protected TypeX annotationType;
	protected String formalName;
	
	/**
	 * 
	 */
	public ExactAnnotationTypePattern(TypeX annotationType) {
		this.annotationType = annotationType;
	}

	public ExactAnnotationTypePattern(String formalName) {
		this.formalName = formalName;
		// will be turned into BindingAnnotationTypePattern during resolution
	}
	
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		return (annotated.hasAnnotation(annotationType) ?
				   FuzzyBoolean.YES : FuzzyBoolean.NO);
	}

	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings, boolean)
	 */
	public AnnotationTypePattern resolveBindings(IScope scope,
			Bindings bindings, boolean allowBinding) {
		if (formalName != null) {
			FormalBinding formalBinding = scope.lookupFormal(formalName);
			if (formalBinding != null) {
				if (bindings == null) {
					scope.message(IMessage.ERROR, this, "negation doesn't allow binding");
					return this;
				}
				if (!allowBinding) {
					scope.message(IMessage.ERROR, this, 
						"name binding only allowed in @pcds, args, this, and target");
					return this;
				}
				
				BindingAnnotationTypePattern binding = new BindingAnnotationTypePattern(formalBinding);
				binding.copyLocationFrom(this);
				bindings.register(binding, scope);
				
				return binding;
			} else {
				scope.message(IMessage.ERROR,this,"unbound formal " + formalName);
				return this;
			}
		} else {
			return this;
		}
	}
	
	private static byte VERSION = 1; // rev if serialisation form changes
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.EXACT);
		s.writeByte(VERSION);
		annotationType.write(s);
		writeLocation(s);
	}

	public static AnnotationTypePattern read(DataInputStream s,ISourceContext context) throws IOException {
		AnnotationTypePattern ret;
		byte version = s.readByte();
		if (version > VERSION) {
			throw new BCException("ExactAnnotationTypePattern was written by a newer version of AspectJ");
		}
		ret = new ExactAnnotationTypePattern(TypeX.read(s));
		ret.readLocation(context,s);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ExactAnnotationTypePattern)) return false;
		ExactAnnotationTypePattern other = (ExactAnnotationTypePattern) obj;
		return (other.annotationType.equals(annotationType));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return annotationType.hashCode();
	}
	
	public String toString() {
		return "@" + annotationType.toString();
	}
}
