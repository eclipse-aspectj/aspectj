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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvableTypeList;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

public class TypePatternList extends PatternNode {
	private TypePattern[] typePatterns;
	int ellipsisCount = 0;

	public static final TypePatternList EMPTY = new TypePatternList(new TypePattern[] {});

	public static final TypePatternList ANY = new TypePatternList(new TypePattern[] { new EllipsisTypePattern() }); // can't use

	// TypePattern.ELLIPSIS
	// because of
	// circular
	// static
	// dependency
	// that
	// introduces

	public TypePatternList() {
		typePatterns = new TypePattern[0];
		ellipsisCount = 0;
	}

	public TypePatternList(TypePattern[] arguments) {
		this.typePatterns = arguments;
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i] == TypePattern.ELLIPSIS) {
				ellipsisCount++;
			}
		}
	}

	public TypePatternList(List l) {
		this((TypePattern[]) l.toArray(new TypePattern[l.size()]));
	}

	public int size() {
		return typePatterns.length;
	}

	public TypePattern get(int index) {
		return typePatterns[index];
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for (int i = 0, len = typePatterns.length; i < len; i++) {
			TypePattern type = typePatterns[i];
			if (i > 0) {
				buf.append(", ");
			}
			if (type == TypePattern.ELLIPSIS) {
				buf.append("..");
			} else {
				buf.append(type.toString());
			}
		}
		buf.append(")");
		return buf.toString();
	}

	/*
	 * return true iff this pattern could ever match a signature with the given number of parameters
	 */
	public boolean canMatchSignatureWithNParameters(int numParams) {
		if (ellipsisCount == 0) {
			return numParams == size();
		} else {
			return (size() - ellipsisCount) <= numParams;
		}
	}

	public FuzzyBoolean matches(ResolvedType[] types, TypePattern.MatchKind kind) {
		return matches(types, kind, null);
	}

	// XXX shares much code with WildTypePattern and with NamePattern
	/**
	 * When called with TypePattern.STATIC this will always return either FuzzyBoolean.YES or FuzzyBoolean.NO.
	 * 
	 * When called with TypePattern.DYNAMIC this could return MAYBE if at runtime it would be possible for arguments of the given
	 * static types to dynamically match this, but it is not known for certain.
	 * 
	 * This method will never return FuzzyBoolean.NEVER
	 */
	public FuzzyBoolean matches(ResolvedType[] types, TypePattern.MatchKind kind, ResolvedType[][] parameterAnnotations) {
		int nameLength = types.length;
		int patternLength = typePatterns.length;

		int nameIndex = 0;
		int patternIndex = 0;

		if (ellipsisCount == 0) {
			if (nameLength != patternLength) {
				return FuzzyBoolean.NO;
			}
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				ResolvedType t = types[nameIndex];
				FuzzyBoolean ret = null;
				try {
					if (parameterAnnotations != null) {
						t.temporaryAnnotationTypes = parameterAnnotations[nameIndex];
					}
					ret = typePatterns[patternIndex].matches(t, kind);
				} finally {
					t.temporaryAnnotationTypes = null;
				}
				patternIndex++;
				nameIndex++;
				if (ret == FuzzyBoolean.NO) {
					return ret;
				}
				if (ret == FuzzyBoolean.MAYBE) {
					finalReturn = ret;
				}
			}
			return finalReturn;
		} else if (ellipsisCount == 1) {
			if (nameLength < patternLength - 1) {
				return FuzzyBoolean.NO;
			}
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				TypePattern p = typePatterns[patternIndex++];
				if (p == TypePattern.ELLIPSIS) {
					nameIndex = nameLength - (patternLength - patternIndex);
				} else {
					ResolvedType t = types[nameIndex];
					FuzzyBoolean ret = null;
					try {
						if (parameterAnnotations != null) {
							t.temporaryAnnotationTypes = parameterAnnotations[nameIndex];
						}
						ret = p.matches(t, kind);
					} finally {
						t.temporaryAnnotationTypes = null;
					}
					nameIndex++;
					if (ret == FuzzyBoolean.NO) {
						return ret;
					}
					if (ret == FuzzyBoolean.MAYBE) {
						finalReturn = ret;
					}
				}
			}
			return finalReturn;
		} else {
			// System.err.print("match(" + arguments + ", " + types + ") -> ");
			FuzzyBoolean b = outOfStar(typePatterns, types, 0, 0, patternLength - ellipsisCount, nameLength, ellipsisCount, kind,
					parameterAnnotations);
			// System.err.println(b);
			return b;
		}
	}

	private static FuzzyBoolean outOfStar(final TypePattern[] pattern, final ResolvedType[] target, int pi, int ti, int pLeft,
			int tLeft, final int starsLeft, TypePattern.MatchKind kind, ResolvedType[][] parameterAnnotations) {
		if (pLeft > tLeft) {
			return FuzzyBoolean.NO;
		}
		FuzzyBoolean finalReturn = FuzzyBoolean.YES;
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length && pi < pattern.length)
			if (tLeft == 0) {
				return finalReturn;
			}
			if (pLeft == 0) {
				if (starsLeft > 0) {
					return finalReturn;
				} else {
					return FuzzyBoolean.NO;
				}
			}
			if (pattern[pi] == TypePattern.ELLIPSIS) {
				return inStar(pattern, target, pi + 1, ti, pLeft, tLeft, starsLeft - 1, kind, parameterAnnotations);
			}
			FuzzyBoolean ret = null;
			try {
				if (parameterAnnotations != null) {
					target[ti].temporaryAnnotationTypes = parameterAnnotations[ti];
				}
				ret = pattern[pi].matches(target[ti], kind);
			} finally {
				target[ti].temporaryAnnotationTypes = null;
			}
			if (ret == FuzzyBoolean.NO) {
				return ret;
			}
			if (ret == FuzzyBoolean.MAYBE) {
				finalReturn = ret;
			}
			pi++;
			ti++;
			pLeft--;
			tLeft--;
		}
	}

	private static FuzzyBoolean inStar(final TypePattern[] pattern, final ResolvedType[] target, int pi, int ti, final int pLeft,
			int tLeft, int starsLeft, TypePattern.MatchKind kind, ResolvedType[][] parameterAnnotations) {
		// invariant: pLeft > 0, so we know we'll run out of stars and find a real char in pattern
		TypePattern patternChar = pattern[pi];
		while (patternChar == TypePattern.ELLIPSIS) {
			starsLeft--;
			patternChar = pattern[++pi];
		}
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length)
			if (pLeft > tLeft) {
				return FuzzyBoolean.NO;
			}

			FuzzyBoolean ff = null;
			try {
				if (parameterAnnotations != null) {
					target[ti].temporaryAnnotationTypes = parameterAnnotations[ti];
				}
				ff = patternChar.matches(target[ti], kind);
			} finally {
				target[ti].temporaryAnnotationTypes = null;
			}

			if (ff.maybeTrue()) {
				FuzzyBoolean xx = outOfStar(pattern, target, pi + 1, ti + 1, pLeft - 1, tLeft - 1, starsLeft, kind,
						parameterAnnotations);
				if (xx.maybeTrue()) {
					return ff.and(xx);
				}
			}
			ti++;
			tLeft--;
		}
	}

	public FuzzyBoolean matches(ResolvableTypeList types, TypePattern.MatchKind kind, ResolvedType[][] parameterAnnotations) {
		int nameLength = types.length;
		int patternLength = typePatterns.length;

		int nameIndex = 0;
		int patternIndex = 0;

		if (ellipsisCount == 0) {
			if (nameLength != patternLength) {
				return FuzzyBoolean.NO;
			}
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				ResolvedType t = types.getResolved(nameIndex);
				FuzzyBoolean ret = null;
				try {
					if (parameterAnnotations != null) {
						t.temporaryAnnotationTypes = parameterAnnotations[nameIndex];
					}
					ret = typePatterns[patternIndex].matches(t, kind);
				} finally {
					t.temporaryAnnotationTypes = null;
				}
				patternIndex++;
				nameIndex++;
				if (ret == FuzzyBoolean.NO) {
					return ret;
				}
				if (ret == FuzzyBoolean.MAYBE) {
					finalReturn = ret;
				}
			}
			return finalReturn;
		} else if (ellipsisCount == 1) {
			if (nameLength < patternLength - 1) {
				return FuzzyBoolean.NO;
			}
			FuzzyBoolean finalReturn = FuzzyBoolean.YES;
			while (patternIndex < patternLength) {
				TypePattern p = typePatterns[patternIndex++];
				if (p == TypePattern.ELLIPSIS) {
					nameIndex = nameLength - (patternLength - patternIndex);
				} else {
					ResolvedType t = types.getResolved(nameIndex);
					FuzzyBoolean ret = null;
					try {
						if (parameterAnnotations != null) {
							t.temporaryAnnotationTypes = parameterAnnotations[nameIndex];
						}
						ret = p.matches(t, kind);
					} finally {
						t.temporaryAnnotationTypes = null;
					}
					nameIndex++;
					if (ret == FuzzyBoolean.NO) {
						return ret;
					}
					if (ret == FuzzyBoolean.MAYBE) {
						finalReturn = ret;
					}
				}
			}
			return finalReturn;
		} else {
			// System.err.print("match(" + arguments + ", " + types + ") -> ");
			FuzzyBoolean b = outOfStar(typePatterns, types, 0, 0, patternLength - ellipsisCount, nameLength, ellipsisCount, kind,
					parameterAnnotations);
			// System.err.println(b);
			return b;
		}
	}

	private static FuzzyBoolean outOfStar(final TypePattern[] pattern, ResolvableTypeList target, int pi, int ti, int pLeft,
			int tLeft, final int starsLeft, TypePattern.MatchKind kind, ResolvedType[][] parameterAnnotations) {
		if (pLeft > tLeft) {
			return FuzzyBoolean.NO;
		}
		FuzzyBoolean finalReturn = FuzzyBoolean.YES;
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length && pi < pattern.length)
			if (tLeft == 0) {
				return finalReturn;
			}
			if (pLeft == 0) {
				if (starsLeft > 0) {
					return finalReturn;
				} else {
					return FuzzyBoolean.NO;
				}
			}
			if (pattern[pi] == TypePattern.ELLIPSIS) {
				return inStar(pattern, target, pi + 1, ti, pLeft, tLeft, starsLeft - 1, kind, parameterAnnotations);
			}
			FuzzyBoolean ret = null;
			ResolvedType type = target.getResolved(ti);
			try {
				if (parameterAnnotations != null) {
					type.temporaryAnnotationTypes = parameterAnnotations[ti];
				}
				ret = pattern[pi].matches(type, kind);
			} finally {
				type.temporaryAnnotationTypes = null;
			}
			if (ret == FuzzyBoolean.NO) {
				return ret;
			}
			if (ret == FuzzyBoolean.MAYBE) {
				finalReturn = ret;
			}
			pi++;
			ti++;
			pLeft--;
			tLeft--;
		}
	}

	private static FuzzyBoolean inStar(final TypePattern[] pattern, ResolvableTypeList target, int pi, int ti, final int pLeft,
			int tLeft, int starsLeft, TypePattern.MatchKind kind, ResolvedType[][] parameterAnnotations) {
		// invariant: pLeft > 0, so we know we'll run out of stars and find a real char in pattern
		TypePattern patternChar = pattern[pi];
		while (patternChar == TypePattern.ELLIPSIS) {
			starsLeft--;
			patternChar = pattern[++pi];
		}
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length)
			if (pLeft > tLeft) {
				return FuzzyBoolean.NO;
			}

			ResolvedType type = target.getResolved(ti);
			FuzzyBoolean ff = null;
			try {
				if (parameterAnnotations != null) {
					type.temporaryAnnotationTypes = parameterAnnotations[ti];
				}
				ff = patternChar.matches(type, kind);
			} finally {
				type.temporaryAnnotationTypes = null;
			}

			if (ff.maybeTrue()) {
				FuzzyBoolean xx = outOfStar(pattern, target, pi + 1, ti + 1, pLeft - 1, tLeft - 1, starsLeft, kind,
						parameterAnnotations);
				if (xx.maybeTrue()) {
					return ff.and(xx);
				}
			}
			ti++;
			tLeft--;
		}
	}

	/**
	 * Return a version of this type pattern list in which all type variable references are replaced by their corresponding entry in
	 * the map
	 * 
	 * @param typeVariableMap
	 * @return
	 */
	public TypePatternList parameterizeWith(Map typeVariableMap, World w) {
		TypePattern[] parameterizedPatterns = new TypePattern[typePatterns.length];
		for (int i = 0; i < parameterizedPatterns.length; i++) {
			parameterizedPatterns[i] = typePatterns[i].parameterizeWith(typeVariableMap, w);
		}
		return new TypePatternList(parameterizedPatterns);
	}

	public TypePatternList resolveBindings(IScope scope, Bindings bindings, boolean allowBinding, boolean requireExactType) {
		for (int i = 0; i < typePatterns.length; i++) {
			TypePattern p = typePatterns[i];
			if (p != null) {
				typePatterns[i] = typePatterns[i].resolveBindings(scope, bindings, allowBinding, requireExactType);
			}
		}
		return this;
	}

	public TypePatternList resolveReferences(IntMap bindings) {
		int len = typePatterns.length;
		TypePattern[] ret = new TypePattern[len];
		for (int i = 0; i < len; i++) {
			ret[i] = typePatterns[i].remapAdviceFormals(bindings);
		}
		return new TypePatternList(ret);
	}

	public void postRead(ResolvedType enclosingType) {
		for (int i = 0; i < typePatterns.length; i++) {
			TypePattern p = typePatterns[i];
			p.postRead(enclosingType);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TypePatternList)) {
			return false;
		}
		TypePatternList o = (TypePatternList) other;
		int len = o.typePatterns.length;
		if (len != this.typePatterns.length) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (!this.typePatterns[i].equals(o.typePatterns[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 41;
		for (int i = 0, len = typePatterns.length; i < len; i++) {
			result = 37 * result + typePatterns[i].hashCode();
		}
		return result;
	}

	public static TypePatternList read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		short len = s.readShort();
		TypePattern[] arguments = new TypePattern[len];
		for (int i = 0; i < len; i++) {
			arguments[i] = TypePattern.read(s, context);
		}
		TypePatternList ret = new TypePatternList(arguments);
		if (!s.isAtLeast169()) {
			ret.readLocation(context, s);
		}
		return ret;
	}

	@Override
	public int getEnd() {
		throw new IllegalStateException();
	}

	@Override
	public ISourceContext getSourceContext() {
		throw new IllegalStateException();
	}

	@Override
	public ISourceLocation getSourceLocation() {
		throw new IllegalStateException();
	}

	@Override
	public int getStart() {
		throw new IllegalStateException();
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeShort(typePatterns.length);
		for (int i = 0; i < typePatterns.length; i++) {
			typePatterns[i].write(s);
		}
		// writeLocation(s);
	}

	public TypePattern[] getTypePatterns() {
		return typePatterns;
	}

	public List<UnresolvedType> getExactTypes() {
		List<UnresolvedType> ret = new ArrayList<UnresolvedType>();
		for (int i = 0; i < typePatterns.length; i++) {
			UnresolvedType t = typePatterns[i].getExactType();
			if (!ResolvedType.isMissing(t)) {
				ret.add(t);
			}
		}
		return ret;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public Object traverse(PatternNodeVisitor visitor, Object data) {
		Object ret = accept(visitor, data);
		for (int i = 0; i < typePatterns.length; i++) {
			typePatterns[i].traverse(visitor, ret);
		}
		return ret;
	}

	public boolean areAllExactWithNoSubtypesAllowed() {
		for (int i = 0; i < typePatterns.length; i++) {
			TypePattern array_element = typePatterns[i];
			if (!(array_element instanceof ExactTypePattern)) {
				return false;
			} else {
				ExactTypePattern etp = (ExactTypePattern) array_element;
				if (etp.isIncludeSubtypes()) {
					return false;
				}
			}
		}
		return true;
	}

	public String[] maybeGetCleanNames() {
		String[] theParamNames = new String[typePatterns.length];
		for (int i = 0; i < typePatterns.length; i++) {
			TypePattern string = typePatterns[i];
			if (!(string instanceof ExactTypePattern)) {
				return null;
			}
			theParamNames[i] = ((ExactTypePattern) string).getExactType().getName();
		}
		return theParamNames;
	}
}
