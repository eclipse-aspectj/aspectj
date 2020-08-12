/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariableReference;
import org.aspectj.weaver.TypeVariableReferenceType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

public class ExactTypePattern extends TypePattern {
	protected UnresolvedType type;
	protected transient ResolvedType resolvedType;
	public boolean checked = false;
	public boolean isVoid = false;

	public static final Map<String, Class<?>> primitiveTypesMap;
	public static final Map<String, Class<?>> boxedPrimitivesMap;
	private static final Map<String, Class<?>> boxedTypesMap;

	static {
		primitiveTypesMap = new HashMap<>();
		primitiveTypesMap.put("int", int.class);
		primitiveTypesMap.put("short", short.class);
		primitiveTypesMap.put("long", long.class);
		primitiveTypesMap.put("byte", byte.class);
		primitiveTypesMap.put("char", char.class);
		primitiveTypesMap.put("float", float.class);
		primitiveTypesMap.put("double", double.class);

		boxedPrimitivesMap = new HashMap<>();
		boxedPrimitivesMap.put("java.lang.Integer", Integer.class);
		boxedPrimitivesMap.put("java.lang.Short", Short.class);
		boxedPrimitivesMap.put("java.lang.Long", Long.class);
		boxedPrimitivesMap.put("java.lang.Byte", Byte.class);
		boxedPrimitivesMap.put("java.lang.Character", Character.class);
		boxedPrimitivesMap.put("java.lang.Float", Float.class);
		boxedPrimitivesMap.put("java.lang.Double", Double.class);

		boxedTypesMap = new HashMap<>();
		boxedTypesMap.put("int", Integer.class);
		boxedTypesMap.put("short", Short.class);
		boxedTypesMap.put("long", Long.class);
		boxedTypesMap.put("byte", Byte.class);
		boxedTypesMap.put("char", Character.class);
		boxedTypesMap.put("float", Float.class);
		boxedTypesMap.put("double", Double.class);

	}

	@Override
	protected boolean matchesSubtypes(ResolvedType type) {
		boolean match = super.matchesSubtypes(type);
		if (match) {
			return match;
		}
		// are we dealing with array funkyness - pattern might be jlObject[]+ and type jlString[]
		if (type.isArray() && this.type.isArray()) {
			ResolvedType componentType = type.getComponentType().resolve(type.getWorld());
			UnresolvedType newPatternType = this.type.getComponentType();
			ExactTypePattern etp = new ExactTypePattern(newPatternType, includeSubtypes, false);
			return etp.matchesSubtypes(componentType, type);
		}
		return match;
	}

	public ExactTypePattern(UnresolvedType type, boolean includeSubtypes, boolean isVarArgs) {
		super(includeSubtypes, isVarArgs);
		this.type = type;
	}

	@Override
	public boolean isArray() {
		return type.isArray();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.aspectj.weaver.patterns.TypePattern#couldEverMatchSameTypesAs(org.aspectj.weaver.patterns.TypePattern)
	 */
	@Override
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		if (super.couldEverMatchSameTypesAs(other)) {
			return true;
		}
		// false is necessary but not sufficient
		UnresolvedType otherType = other.getExactType();
		if (!ResolvedType.isMissing(otherType)) {
			return type.equals(otherType);
		}
		if (other instanceof WildTypePattern) {
			WildTypePattern owtp = (WildTypePattern) other;
			String yourSimpleNamePrefix = owtp.getNamePatterns()[0].maybeGetSimpleName();
			if (yourSimpleNamePrefix != null) {
				return (type.getName().startsWith(yourSimpleNamePrefix));
			}
		}
		return true;
	}

	@Override
	protected boolean matchesExactly(ResolvedType matchType) {
		boolean typeMatch = this.type.equals(matchType);
		if (!typeMatch && (matchType.isParameterizedType() || matchType.isGenericType())) {
			typeMatch = this.type.equals(matchType.getRawType());
		}
		if (!typeMatch && matchType.isTypeVariableReference()) {
			typeMatch = matchesTypeVariable((TypeVariableReferenceType) matchType);
		}
		if (!typeMatch) {
			return false;
		}
		annotationPattern.resolve(matchType.getWorld());
		boolean annMatch = false;
		if (matchType.temporaryAnnotationTypes != null) {
			annMatch = annotationPattern.matches(matchType, matchType.temporaryAnnotationTypes).alwaysTrue();
		} else {
			annMatch = annotationPattern.matches(matchType).alwaysTrue();
		}
		return (typeMatch && annMatch);
	}

	private boolean matchesTypeVariable(TypeVariableReferenceType matchType) {
		// was this method previously coded to return false *on purpose* ?? pr124808
		return this.type.equals(((TypeVariableReference) matchType).getTypeVariable().getFirstBound());
		// return false;
	}

	@Override
	protected boolean matchesExactly(ResolvedType matchType, ResolvedType annotatedType) {
		boolean typeMatch = this.type.equals(matchType);
		if (!typeMatch && (matchType.isParameterizedType() || matchType.isGenericType())) {
			typeMatch = this.type.equals(matchType.getRawType());
		}
		if (!typeMatch && matchType.isTypeVariableReference()) {
			typeMatch = matchesTypeVariable((TypeVariableReferenceType) matchType);
		}
		annotationPattern.resolve(matchType.getWorld());
		boolean annMatch = false;
		if (annotatedType.temporaryAnnotationTypes != null) {
			annMatch = annotationPattern.matches(annotatedType, annotatedType.temporaryAnnotationTypes).alwaysTrue();
		} else {
			annMatch = annotationPattern.matches(annotatedType).alwaysTrue();
		}
		return (typeMatch && annMatch);
	}

	public UnresolvedType getType() {
		return type;
	}

	public ResolvedType getResolvedExactType(World world) {
		if (resolvedType == null) {
			resolvedType = world.resolve(type);
		}
		return resolvedType;
	}

	@Override
	public boolean isVoid() {
		if (!checked) {
			isVoid = this.type.getSignature().equals("V");
			checked = true;
		}
		return isVoid;
	}

	// true if (matchType instanceof this.type)
	@Override
	public FuzzyBoolean matchesInstanceof(ResolvedType matchType) {
		// in our world, Object is assignable from anything
		annotationPattern.resolve(matchType.getWorld());
		if (type.equals(ResolvedType.OBJECT)) {
			return FuzzyBoolean.YES.and(annotationPattern.matches(matchType));
		}

		ResolvedType resolvedType = type.resolve(matchType.getWorld());
		if (resolvedType.isAssignableFrom(matchType)) {
			return FuzzyBoolean.YES.and(annotationPattern.matches(matchType));
		}

		// fix for PR 64262 - shouldn't try to coerce primitives
		if (type.isPrimitiveType()) {
			return FuzzyBoolean.NO;
		} else {
			return matchType.isCoerceableFrom(type.resolve(matchType.getWorld())) ? FuzzyBoolean.MAYBE : FuzzyBoolean.NO;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ExactTypePattern)) {
			return false;
		}
		if (other instanceof BindingTypePattern) {
			return false;
		}
		ExactTypePattern o = (ExactTypePattern) other;
		if (includeSubtypes != o.includeSubtypes) {
			return false;
		}
		if (isVarArgs != o.isVarArgs) {
			return false;
		}
		if (!typeParameters.equals(o.typeParameters)) {
			return false;
		}
		return (o.type.equals(this.type) && o.annotationPattern.equals(this.annotationPattern));
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + type.hashCode();
		result = 37 * result + Boolean.valueOf(includeSubtypes).hashCode();
		result = 37 * result + Boolean.valueOf(isVarArgs).hashCode();
		result = 37 * result + typeParameters.hashCode();
		result = 37 * result + annotationPattern.hashCode();
		return result;
	}

	private static final byte EXACT_VERSION = 1; // rev if changed

	@Override
	public void write(CompressingDataOutputStream out) throws IOException {
		out.writeByte(TypePattern.EXACT);
		out.writeByte(EXACT_VERSION);
		out.writeCompressedSignature(type.getSignature());
		out.writeBoolean(includeSubtypes);
		out.writeBoolean(isVarArgs);
		annotationPattern.write(out);
		typeParameters.write(out);
		writeLocation(out);
	}

	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		if (s.getMajorVersion() >= AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {
			return readTypePattern150(s, context);
		} else {
			return readTypePatternOldStyle(s, context);
		}
	}

	public static TypePattern readTypePattern150(VersionedDataInputStream s, ISourceContext context) throws IOException {
		byte version = s.readByte();
		if (version > EXACT_VERSION) {
			throw new BCException("ExactTypePattern was written by a more recent version of AspectJ");
		}
		TypePattern ret = new ExactTypePattern(s.isAtLeast169() ? s.readSignatureAsUnresolvedType() : UnresolvedType.read(s), s
				.readBoolean(), s.readBoolean());
		ret.setAnnotationTypePattern(AnnotationTypePattern.read(s, context));
		ret.setTypeParameters(TypePatternList.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public static TypePattern readTypePatternOldStyle(DataInputStream s, ISourceContext context) throws IOException {
		TypePattern ret = new ExactTypePattern(UnresolvedType.read(s), s.readBoolean(), false);
		ret.readLocation(context, s);
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		if (annotationPattern != AnnotationTypePattern.ANY) {
			buff.append('(');
			buff.append(annotationPattern.toString());
			buff.append(' ');
		}
		String typeString = type.toString();
		if (isVarArgs) {
			typeString = typeString.substring(0, typeString.lastIndexOf('['));
		}
		buff.append(typeString);
		if (includeSubtypes) {
			buff.append('+');
		}
		if (isVarArgs) {
			buff.append("...");
		}
		if (annotationPattern != AnnotationTypePattern.ANY) {
			buff.append(')');
		}
		return buff.toString();
	}

	@Override
	public TypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding, boolean requireExactType) {
		throw new BCException("trying to re-resolve");
	}

	/**
	 * return a version of this type pattern with all type variables references replaced by the corresponding entry in the map.
	 */
	@Override
	public TypePattern parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		UnresolvedType newType = type;
		if (type.isTypeVariableReference()) {
			TypeVariableReference t = (TypeVariableReference) type;
			String key = t.getTypeVariable().getName();
			if (typeVariableMap.containsKey(key)) {
				newType = typeVariableMap.get(key);
			}
		} else if (type.isParameterizedType()) {
			newType = w.resolve(type).parameterize(typeVariableMap);
		}
		ExactTypePattern ret = new ExactTypePattern(newType, includeSubtypes, isVarArgs);
		ret.annotationPattern = annotationPattern.parameterizeWith(typeVariableMap, w);
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

}
