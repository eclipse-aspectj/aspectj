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

import java.io.IOException;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;

public class NotAnnotationTypePattern extends AnnotationTypePattern {

	AnnotationTypePattern negatedPattern;

	public NotAnnotationTypePattern(AnnotationTypePattern pattern) {
		this.negatedPattern = pattern;
		setLocation(pattern.getSourceContext(), pattern.getStart(), pattern.getEnd());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#matches(org.aspectj.weaver.AnnotatedElement)
	 */
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		return negatedPattern.matches(annotated).not();
	}

	public FuzzyBoolean matches(AnnotatedElement annotated, ResolvedType[] parameterAnnotations) {
		return negatedPattern.matches(annotated, parameterAnnotations).not();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolve(org.aspectj.weaver.World)
	 */
	public void resolve(World world) {
		negatedPattern.resolve(world);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolveBindings(org.aspectj.weaver.patterns.IScope,
	 * org.aspectj.weaver.patterns.Bindings, boolean)
	 */
	public AnnotationTypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding) {
		negatedPattern = negatedPattern.resolveBindings(scope, bindings, allowBinding);
		return this;
	}

	public AnnotationTypePattern parameterizeWith(Map typeVariableMap, World w) {
		AnnotationTypePattern newNegatedPattern = negatedPattern.parameterizeWith(typeVariableMap, w);
		NotAnnotationTypePattern ret = new NotAnnotationTypePattern(newNegatedPattern);
		ret.copyLocationFrom(this);
		if (this.isForParameterAnnotationMatch()) {
			ret.setForParameterAnnotationMatch();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.NOT);
		negatedPattern.write(s);
		writeLocation(s);
		s.writeBoolean(isForParameterAnnotationMatch());
	}

	public static AnnotationTypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		AnnotationTypePattern ret = new NotAnnotationTypePattern(AnnotationTypePattern.read(s, context));
		ret.readLocation(context, s);
		if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ160) {
			if (s.readBoolean()) {
				ret.setForParameterAnnotationMatch();
			}
		}
		return ret;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof NotAnnotationTypePattern)) {
			return false;
		}
		NotAnnotationTypePattern other = (NotAnnotationTypePattern) obj;
		return other.negatedPattern.equals(negatedPattern)
				&& other.isForParameterAnnotationMatch() == isForParameterAnnotationMatch();
	}

	public int hashCode() {
		int result = 17 + 37 * negatedPattern.hashCode();
		result = 37 * result + (isForParameterAnnotationMatch() ? 0 : 1);
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
		Object ret = accept(visitor, data);
		negatedPattern.traverse(visitor, ret);
		return ret;
	}

	public void setForParameterAnnotationMatch() {
		negatedPattern.setForParameterAnnotationMatch();
	}
}
