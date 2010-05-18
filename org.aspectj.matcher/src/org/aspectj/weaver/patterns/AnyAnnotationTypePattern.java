/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
	public AnnotationTypePattern parameterizeWith(Map arg0, World w) {
		return this;
	}

	@Override
	public void setForParameterAnnotationMatch() {

	}
}