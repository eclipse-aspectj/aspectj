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

/**
 * @author colyer
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class AndAnnotationTypePattern extends AnnotationTypePattern {

	private AnnotationTypePattern left;
	private AnnotationTypePattern right;

	public AndAnnotationTypePattern(AnnotationTypePattern left, AnnotationTypePattern right) {
		this.left = left;
		this.right = right;
		setLocation(left.getSourceContext(), left.getStart(), right.getEnd());
	}

	public FuzzyBoolean matches(AnnotatedElement annotated) {
		return left.matches(annotated).and(right.matches(annotated));
	}

	public FuzzyBoolean matches(AnnotatedElement annotated, ResolvedType[] parameterAnnotations) {
		return left.matches(annotated, parameterAnnotations).and(right.matches(annotated, parameterAnnotations));
	}

	public void resolve(World world) {
		left.resolve(world);
		right.resolve(world);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolveBindings(org.aspectj.weaver.patterns.IScope,
	 * org.aspectj.weaver.patterns.Bindings, boolean)
	 */
	public AnnotationTypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding) {
		left = left.resolveBindings(scope, bindings, allowBinding);
		right = right.resolveBindings(scope, bindings, allowBinding);
		return this;
	}

	public AnnotationTypePattern parameterizeWith(Map typeVariableMap, World w) {
		AnnotationTypePattern newLeft = left.parameterizeWith(typeVariableMap, w);
		AnnotationTypePattern newRight = right.parameterizeWith(typeVariableMap, w);
		AndAnnotationTypePattern ret = new AndAnnotationTypePattern(newLeft, newRight);
		ret.copyLocationFrom(this);
		if (this.isForParameterAnnotationMatch()) {
			ret.setForParameterAnnotationMatch();
		}
		return ret;
	}

	public static AnnotationTypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		AnnotationTypePattern p = new AndAnnotationTypePattern(AnnotationTypePattern.read(s, context), AnnotationTypePattern.read(
				s, context));
		p.readLocation(context, s);
		if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ160) {
			if (s.readBoolean()) {
				p.setForParameterAnnotationMatch();
			}
		}
		return p;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.AND);
		left.write(s);
		right.write(s);
		writeLocation(s);
		s.writeBoolean(isForParameterAnnotationMatch());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof AndAnnotationTypePattern)) {
			return false;
		}
		AndAnnotationTypePattern other = (AndAnnotationTypePattern) obj;
		return (left.equals(other.left) && right.equals(other.right) && left.isForParameterAnnotationMatch() == right
				.isForParameterAnnotationMatch());
	}

	public int hashCode() {
		int result = 17;
		result = result * 37 + left.hashCode();
		result = result * 37 + right.hashCode();
		result = result * 37 + (isForParameterAnnotationMatch() ? 0 : 1);
		return result;
	}

	public String toString() {
		return left.toString() + " " + right.toString();
	}

	public AnnotationTypePattern getLeft() {
		return left;
	}

	public AnnotationTypePattern getRight() {
		return right;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public Object traverse(PatternNodeVisitor visitor, Object data) {
		Object ret = accept(visitor, data);
		left.traverse(visitor, ret);
		right.traverse(visitor, ret);
		return ret;
	}

	public void setForParameterAnnotationMatch() {
		left.setForParameterAnnotationMatch();
		right.setForParameterAnnotationMatch();
	}
}
