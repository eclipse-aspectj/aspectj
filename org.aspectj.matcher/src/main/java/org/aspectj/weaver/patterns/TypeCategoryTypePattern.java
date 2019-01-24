/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *     Andy Clement
 *     Nieraj Singh
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

/**
 * A TypeCategoryTypePattern matches on the category of a type, one of class/interface/aspect/inner/anonymous/enum/annotation, and
 * these are specified in the pointcut via isClass() isInterface() isAspect() isInner() isAnonymous() isEnum() isAnnotation().
 * 
 * @author Andy Clement
 * @since 1.6.9
 */
public class TypeCategoryTypePattern extends TypePattern {

	public static final int CLASS = 1;
	public static final int INTERFACE = 2;
	public static final int ASPECT = 3;
	public static final int INNER = 4;
	public static final int ANONYMOUS = 5;
	public static final int ENUM = 6;
	public static final int ANNOTATION = 7;
	public static final int FINAL = 8;
	public static final int ABSTRACT = 9;

	private int category;

	private int VERSION = 1;

	public TypeCategoryTypePattern(int category) {
		super(false);
		this.category = category;
	}
	
	public int getTypeCategory() {
		return category;
	}

	@Override
	protected boolean matchesExactly(ResolvedType type) {
		return isRightCategory(type);
	}

	@Override
	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		return isRightCategory(type);
	}

	@Override
	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
		return FuzzyBoolean.fromBoolean(isRightCategory(type));
	}

	@Override
	public TypePattern parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		return this;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TypeCategoryTypePattern)) {
			return false;
		}
		TypeCategoryTypePattern o = (TypeCategoryTypePattern) other;
		return o.category == category;
	}

	// TODO is sourcelocation part of the identity or just a 'nice to have' - if important it should be in hashcode/equals
	// TODO but if that is the case it needs addressing for all type patterns

	@Override
	public int hashCode() {
		return category * 37;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(TypePattern.TYPE_CATEGORY);
		s.writeInt(VERSION);
		s.writeInt(category);
		writeLocation(s);
	}

	@SuppressWarnings("unused")
	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		int version = s.readInt();
		int category = s.readInt();
		TypeCategoryTypePattern tp = new TypeCategoryTypePattern(category);
		tp.readLocation(context, s);
		return tp;
	}

	/**
	 * @return true if the supplied type is of the category specified for this type pattern
	 */
	private boolean isRightCategory(ResolvedType type) {
		switch (category) {
		case CLASS:
			return type.isClass();
		case INTERFACE:
			return type.isInterface();
		case ASPECT:
			return type.isAspect();
		case ANONYMOUS:
			return type.isAnonymous();
		case INNER:
			return type.isNested();
		case ENUM:
			return type.isEnum();
		case ANNOTATION:
			return type.isAnnotation();
		case FINAL:
			return Modifier.isFinal(type.getModifiers());
		case ABSTRACT:
			return Modifier.isAbstract(type.getModifiers());
		}
		return false;
	}

}
