/* *******************************************************************
 * Copyright (c) 2002, 2010 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Nieraj Singh
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariableReference;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

/**
 * On creation, type pattern only contains WildTypePattern nodes, not BindingType or ExactType.
 * 
 * <p>
 * Then we call resolveBindings() during compilation During concretization of enclosing pointcuts, we call remapAdviceFormals
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public abstract class TypePattern extends PatternNode {
	public static class MatchKind {
		private String name;

		public MatchKind(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static final MatchKind STATIC = new MatchKind("STATIC");
	public static final MatchKind DYNAMIC = new MatchKind("DYNAMIC");

	public static final TypePattern ELLIPSIS = new EllipsisTypePattern();
	public static final TypePattern ANY = new AnyTypePattern();
	public static final TypePattern NO = new NoTypePattern();

	protected boolean includeSubtypes;
	protected boolean isVarArgs = false;
	protected AnnotationTypePattern annotationPattern = AnnotationTypePattern.ANY;
	protected TypePatternList typeParameters = TypePatternList.EMPTY;

	protected TypePattern(boolean includeSubtypes, boolean isVarArgs, TypePatternList typeParams) {
		this.includeSubtypes = includeSubtypes;
		this.isVarArgs = isVarArgs;
		this.typeParameters = (typeParams == null ? TypePatternList.EMPTY : typeParams);
	}

	protected TypePattern(boolean includeSubtypes, boolean isVarArgs) {
		this(includeSubtypes, isVarArgs, null);
	}

	public AnnotationTypePattern getAnnotationPattern() {
		return annotationPattern;
	}

	public boolean isVarArgs() {
		return isVarArgs;
	}

	public boolean isStarAnnotation() {
		return annotationPattern == AnnotationTypePattern.ANY;
	}

	public boolean isArray() {
		return false;
	}

	protected TypePattern(boolean includeSubtypes) {
		this(includeSubtypes, false);
	}

	public void setAnnotationTypePattern(AnnotationTypePattern annPatt) {
		this.annotationPattern = annPatt;
	}

	public void setTypeParameters(TypePatternList typeParams) {
		this.typeParameters = typeParams;
	}

	public TypePatternList getTypeParameters() {
		return this.typeParameters;
	}

	public void setIsVarArgs(boolean isVarArgs) {
		this.isVarArgs = isVarArgs;
	}

	// answer conservatively...
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		if (this.includeSubtypes || other.includeSubtypes) {
			return true;
		}
		if (this.annotationPattern != AnnotationTypePattern.ANY) {
			return true;
		}
		if (other.annotationPattern != AnnotationTypePattern.ANY) {
			return true;
		}
		return false;
	}

	// XXX non-final for Not, && and ||
	public boolean matchesStatically(ResolvedType type) {
		if (includeSubtypes) {
			return matchesSubtypes(type);
		} else {
			return matchesExactly(type);
		}
	}

	public abstract FuzzyBoolean matchesInstanceof(ResolvedType type);

	public final FuzzyBoolean matches(ResolvedType type, MatchKind kind) {
		// FuzzyBoolean typeMatch = null;
		// ??? This is part of gracefully handling missing references
		if (type.isMissing()) {
			return FuzzyBoolean.NO;
		}

		if (kind == STATIC) {
			return FuzzyBoolean.fromBoolean(matchesStatically(type));
		} else if (kind == DYNAMIC) {
			// System.err.println("matching: " + this + " with " + type);
			// typeMatch = matchesInstanceof(type);
			// System.err.println("    got: " + ret);
			// return typeMatch.and(annotationPattern.matches(type));
			return matchesInstanceof(type);
		} else {
			throw new IllegalArgumentException("kind must be DYNAMIC or STATIC");
		}
	}

	protected abstract boolean matchesExactly(ResolvedType type);

	protected abstract boolean matchesExactly(ResolvedType type, ResolvedType annotatedType);

	protected boolean matchesSubtypes(ResolvedType type) {
		// System.out.println("matching: " + this + " to " + type);
		if (matchesExactly(type)) {
			return true;
		}

		// pr124808
		Iterator<ResolvedType> typesIterator = null;
		if (type.isTypeVariableReference()) {
			typesIterator = ((TypeVariableReference) type).getTypeVariable().getFirstBound().resolve(type.getWorld())
					.getDirectSupertypes();
		} else {
			// pr223605
			if (type.isRawType()) {
				type = type.getGenericType();
			}
			typesIterator = type.getDirectSupertypes();
		}

		for (Iterator<ResolvedType> i = typesIterator; i.hasNext();) {
			ResolvedType superType = i.next();
			if (matchesSubtypes(superType, type)) {
				return true;
			}
		}
		return false;
	}

	protected boolean matchesSubtypes(ResolvedType superType, ResolvedType annotatedType) {
		// System.out.println("matching2: " + this + " to " + superType);
		if (matchesExactly(superType, annotatedType)) {
			// System.out.println("    true");
			return true;
		}
		// If an ITD is applied, it will be put onto the generic type, not the parameterized or raw form
		if (superType.isParameterizedType() || superType.isRawType()) {
			superType = superType.getGenericType();
		}
		// FuzzyBoolean ret = FuzzyBoolean.NO; // ??? -eh
		for (Iterator<ResolvedType> i = superType.getDirectSupertypes(); i.hasNext();) {
			ResolvedType superSuperType = (ResolvedType) i.next();
			if (matchesSubtypes(superSuperType, annotatedType)) {
				return true;
			}
		}
		return false;
	}

	public UnresolvedType resolveExactType(IScope scope, Bindings bindings) {
		TypePattern p = resolveBindings(scope, bindings, false, true);
		if (!(p instanceof ExactTypePattern)) {
			return ResolvedType.MISSING;
		}
		return ((ExactTypePattern) p).getType();
	}

	public UnresolvedType getExactType() {
		if (this instanceof ExactTypePattern) {
			return ((ExactTypePattern) this).getType();
		} else {
			return ResolvedType.MISSING;
		}
	}

	protected TypePattern notExactType(IScope s) {
		s.getMessageHandler().handleMessage(
				MessageUtil.error(WeaverMessages.format(WeaverMessages.EXACT_TYPE_PATTERN_REQD), getSourceLocation()));
		return NO;
	}

	// public boolean assertExactType(IMessageHandler m) {
	// if (this instanceof ExactTypePattern) return true;
	//		
	// //XXX should try harder to avoid multiple errors for one problem
	// m.handleMessage(MessageUtil.error("exact type pattern required", getSourceLocation()));
	// return false;
	// }

	/**
	 * This can modify in place, or return a new TypePattern if the type changes.
	 */
	public TypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding, boolean requireExactType) {
		annotationPattern = annotationPattern.resolveBindings(scope, bindings, allowBinding);
		return this;
	}

	public void resolve(World world) {
		annotationPattern.resolve(world);
	}

	/**
	 * return a version of this type pattern in which all type variable references have been replaced by their corresponding entry
	 * in the map.
	 */
	public abstract TypePattern parameterizeWith(Map<String, UnresolvedType> typeVariableMap, World w);

	public void postRead(ResolvedType enclosingType) {
	}

	public boolean isEllipsis() {
		return false;
	}

	public boolean isStar() {
		return false;
	}

	/**
	 * This is called during concretization of pointcuts, it is used by BindingTypePattern to return a new BindingTypePattern with a
	 * formal index appropiate for the advice, rather than for the lexical declaration, i.e. this handles transforamtions through
	 * named pointcuts.
	 * 
	 * <pre>
	 * pointcut foo(String name): args(name);
	 * --&gt; This makes a BindingTypePattern(0) pointing to the 0th formal
	 * 
	 * before(Foo f, String n): this(f) &amp;&amp; foo(n) { ... }
	 * --&gt; when resolveReferences is called on the args from the above, it
	 *     will return a BindingTypePattern(1)
	 * 
	 * before(Foo f): this(f) &amp;&amp; foo(*) { ... }
	 * --&gt; when resolveReferences is called on the args from the above, it
	 *     will return an ExactTypePattern(String)
	 * </pre>
	 */
	public TypePattern remapAdviceFormals(IntMap bindings) {
		return this;
	}

	public static final byte WILD = 1;
	public static final byte EXACT = 2;
	public static final byte BINDING = 3;
	public static final byte ELLIPSIS_KEY = 4;
	public static final byte ANY_KEY = 5;
	public static final byte NOT = 6;
	public static final byte OR = 7;
	public static final byte AND = 8;
	public static final byte NO_KEY = 9;
	public static final byte ANY_WITH_ANNO = 10;
	public static final byte HAS_MEMBER = 11;
	public static final byte TYPE_CATEGORY = 12;

	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		byte key = s.readByte();
		switch (key) {
		case WILD:
			return WildTypePattern.read(s, context);
		case EXACT:
			return ExactTypePattern.read(s, context);
		case BINDING:
			return BindingTypePattern.read(s, context);
		case ELLIPSIS_KEY:
			return ELLIPSIS;
		case ANY_KEY:
			return ANY;
		case NO_KEY:
			return NO;
		case NOT:
			return NotTypePattern.read(s, context);
		case OR:
			return OrTypePattern.read(s, context);
		case AND:
			return AndTypePattern.read(s, context);
		case ANY_WITH_ANNO:
			return AnyWithAnnotationTypePattern.read(s, context);
		case HAS_MEMBER:
			return HasMemberTypePattern.read(s, context);
		case TYPE_CATEGORY:
			return TypeCategoryTypePattern.read(s, context);
		}
		throw new BCException("unknown TypePattern kind: " + key);
	}

	public boolean isIncludeSubtypes() {
		return includeSubtypes;
	}

	/**
	 * For quickly recognizing the pattern '!void'
	 */
	public boolean isBangVoid() {
		return false;
	}

	/**
	 * for quickly recognizing the pattern 'void'
	 */
	public boolean isVoid() {
		return false;
	}

	public boolean hasFailedResolution() {
		return false;
	}

}



