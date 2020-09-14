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
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

public abstract class AnnotationTypePattern extends PatternNode {

	public static final AnnotationTypePattern ANY = new AnyAnnotationTypePattern();
	public static final AnnotationTypePattern ELLIPSIS = new EllipsisAnnotationTypePattern();
	public static final AnnotationTypePattern[] NONE = new AnnotationTypePattern[0];
	private boolean isForParameterAnnotationMatch;

	/**
	 * TODO: write, read, equals &amp; hashCode both in annotation hierarchy and in altered TypePattern hierarchy
	 */
	protected AnnotationTypePattern() {
		super();
	}

	public abstract FuzzyBoolean matches(AnnotatedElement annotated);

	public abstract FuzzyBoolean matches(AnnotatedElement annotated, ResolvedType[] parameterAnnotations);

	public FuzzyBoolean fastMatches(AnnotatedElement annotated) {
		return FuzzyBoolean.MAYBE;
	}

	public AnnotationTypePattern remapAdviceFormals(IntMap bindings) {
		return this;
	}

	public abstract void resolve(World world);

	public abstract AnnotationTypePattern parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w);

	public boolean isAny() {
		return false;
	}

	/**
	 * This can modify in place, or return a new TypePattern if the type changes.
	 */
	public AnnotationTypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding) {
		return this;
	}

	public static final byte EXACT = 1;
	public static final byte BINDING = 2;
	public static final byte NOT = 3;
	public static final byte OR = 4;
	public static final byte AND = 5;
	public static final byte ELLIPSIS_KEY = 6;
	public static final byte ANY_KEY = 7;
	public static final byte WILD = 8;
	public static final byte EXACTFIELD = 9;
	public static final byte BINDINGFIELD = 10;
	public static final byte BINDINGFIELD2 = 11;

	public static AnnotationTypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		byte key = s.readByte();
		switch (key) {
		case EXACT:
			return ExactAnnotationTypePattern.read(s, context);
		case BINDING:
			return BindingAnnotationTypePattern.read(s, context);
		case NOT:
			return NotAnnotationTypePattern.read(s, context);
		case OR:
			return OrAnnotationTypePattern.read(s, context);
		case AND:
			return AndAnnotationTypePattern.read(s, context);
		case WILD:
			return WildAnnotationTypePattern.read(s, context);
		case EXACTFIELD:
			return ExactAnnotationFieldTypePattern.read(s, context);
		case BINDINGFIELD:
			return BindingAnnotationFieldTypePattern.read(s, context);
		case BINDINGFIELD2:
			return BindingAnnotationFieldTypePattern.read2(s, context);
		case ELLIPSIS_KEY:
			return ELLIPSIS;
		case ANY_KEY:
			return ANY;
		}
		throw new BCException("unknown TypePattern kind: " + key);
	}

	public void setForParameterAnnotationMatch() {
		isForParameterAnnotationMatch = true;
	}

	public boolean isForParameterAnnotationMatch() {
		return isForParameterAnnotationMatch;
	}

}

class EllipsisAnnotationTypePattern extends AnnotationTypePattern {

	@Override
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		return FuzzyBoolean.NO;
	}

	@Override
	public FuzzyBoolean matches(AnnotatedElement annotated, ResolvedType[] parameterAnnotations) {
		return FuzzyBoolean.NO;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.ELLIPSIS_KEY);
	}

	@Override
	public void resolve(World world) {
	}

	@Override
	public String toString() {
		return "..";
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public AnnotationTypePattern parameterizeWith(Map arg0, World w) {
		return this;
	}

	@Override
	public void setForParameterAnnotationMatch() {
	}

}
