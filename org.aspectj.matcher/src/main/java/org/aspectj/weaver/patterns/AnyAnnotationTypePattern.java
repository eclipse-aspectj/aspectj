/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors
 * Andy Clement - extracted from AnnotationTypePattern
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

public class AnyAnnotationTypePattern extends AnnotationTypePattern {

	@Override
	public FuzzyBoolean fastMatches(AnnotatedElement annotated) {
		return FuzzyBoolean.YES;
	}

	@Override
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		return FuzzyBoolean.YES;
	}

	@Override
	public FuzzyBoolean matches(AnnotatedElement annotated, ResolvedType[] parameterAnnotations) {
		return FuzzyBoolean.YES;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.ANY_KEY);
	}

	@Override
	public void resolve(World world) {
	}

	@Override
	public String toString() {
		return "@ANY";
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public boolean isAny() {
		return true;
	}

	@Override
	public AnnotationTypePattern parameterizeWith(Map<String,UnresolvedType> arg0, World w) {
		return this;
	}

	@Override
	public void setForParameterAnnotationMatch() {

	}
}
