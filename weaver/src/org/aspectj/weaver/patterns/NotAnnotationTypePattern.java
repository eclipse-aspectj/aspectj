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

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.ISourceContext;

public class NotAnnotationTypePattern extends AnnotationTypePattern {

	private AnnotationTypePattern negatedPattern;
	
	public NotAnnotationTypePattern(AnnotationTypePattern pattern) {
		this.negatedPattern = pattern;
		setLocation(pattern.getSourceContext(), pattern.getStart(), pattern.getEnd());
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#matches(org.aspectj.weaver.AnnotatedElement)
	 */
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		return negatedPattern.matches(annotated).not();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings, boolean)
	 */
	public AnnotationTypePattern resolveBindings(IScope scope,
			Bindings bindings, boolean allowBinding) {
		negatedPattern = negatedPattern.resolveBindings(scope,bindings,allowBinding);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.NOT);
		negatedPattern.write(s);
		writeLocation(s);
	}

	public static AnnotationTypePattern read(DataInputStream s, ISourceContext context) throws IOException {
		AnnotationTypePattern ret = new NotAnnotationTypePattern(AnnotationTypePattern.read(s,context));
		ret.readLocation(context,s);
		return ret;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof NotAnnotationTypePattern)) return false;
		NotAnnotationTypePattern other = (NotAnnotationTypePattern) obj;
		return other.negatedPattern.equals(negatedPattern);
	}

	public int hashCode() {
		int result = 17 + 37*negatedPattern.hashCode();
		return result;
	}
	
	public String toString() {
		return "(!" + negatedPattern.toString() + ")";
	}
	
	public AnnotationTypePattern getNegatedPattern() {
		return negatedPattern;
	}
}
