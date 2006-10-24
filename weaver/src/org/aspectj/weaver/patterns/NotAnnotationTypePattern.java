/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

public class NotAnnotationTypePattern extends AnnotationTypePattern {

	AnnotationTypePattern negatedPattern;
	
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
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolve(org.aspectj.weaver.World)
	 */
	public void resolve(World world) {
		negatedPattern.resolve(world);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings, boolean)
	 */
	public AnnotationTypePattern resolveBindings(IScope scope,
			Bindings bindings, boolean allowBinding) {
		negatedPattern = negatedPattern.resolveBindings(scope,bindings,allowBinding);
		return this;
	}

	
	public AnnotationTypePattern parameterizeWith(Map typeVariableMap,World w) {
		AnnotationTypePattern newNegatedPattern = negatedPattern.parameterizeWith(typeVariableMap,w);
		NotAnnotationTypePattern ret = new NotAnnotationTypePattern(newNegatedPattern);
		ret.copyLocationFrom(this);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.NOT);
		negatedPattern.write(s);
		writeLocation(s);
	}

	public static AnnotationTypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
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
		return "!" + negatedPattern.toString();
	}
	
	public AnnotationTypePattern getNegatedPattern() {
		return negatedPattern;
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
	
	public Object traverse(PatternNodeVisitor visitor, Object data) {
		Object ret = accept(visitor,data);
		negatedPattern.traverse(visitor,ret);
		return ret;
	}
}
