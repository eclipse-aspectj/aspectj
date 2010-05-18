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
import java.util.List;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

/**
 * @author colyer
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class AnnotationPatternList extends PatternNode {

	private AnnotationTypePattern[] typePatterns;
	int ellipsisCount = 0;

	public static final AnnotationPatternList EMPTY = new AnnotationPatternList(new AnnotationTypePattern[] {});

	public static final AnnotationPatternList ANY = new AnnotationPatternList(
			new AnnotationTypePattern[] { AnnotationTypePattern.ELLIPSIS });

	public AnnotationPatternList() {
		typePatterns = new AnnotationTypePattern[0];
		ellipsisCount = 0;
	}

	public AnnotationPatternList(AnnotationTypePattern[] arguments) {
		this.typePatterns = arguments;
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i] == AnnotationTypePattern.ELLIPSIS) {
				ellipsisCount++;
			}
		}
	}

	public AnnotationPatternList(List l) {
		this((AnnotationTypePattern[]) l.toArray(new AnnotationTypePattern[l.size()]));
	}

	protected AnnotationTypePattern[] getAnnotationPatterns() {
		return typePatterns;
	}

	public AnnotationPatternList parameterizeWith(Map typeVariableMap, World w) {
		AnnotationTypePattern[] parameterizedPatterns = new AnnotationTypePattern[this.typePatterns.length];
		for (int i = 0; i < parameterizedPatterns.length; i++) {
			parameterizedPatterns[i] = this.typePatterns[i].parameterizeWith(typeVariableMap, w);
		}
		AnnotationPatternList ret = new AnnotationPatternList(parameterizedPatterns);
		ret.copyLocationFrom(this);
		return ret;
	}

	public void resolve(World inWorld) {
		for (int i = 0; i < typePatterns.length; i++) {
			typePatterns[i].resolve(inWorld);
		}
	}

	public FuzzyBoolean matches(ResolvedType[] someArgs) {
		// do some quick length tests first
		int numArgsMatchedByEllipsis = (someArgs.length + ellipsisCount) - typePatterns.length;
		if (numArgsMatchedByEllipsis < 0) {
			return FuzzyBoolean.NO;
		}
		if ((numArgsMatchedByEllipsis > 0) && (ellipsisCount == 0)) {
			return FuzzyBoolean.NO;
		}
		// now work through the args and the patterns, skipping at ellipsis
		FuzzyBoolean ret = FuzzyBoolean.YES;
		int argsIndex = 0;
		for (int i = 0; i < typePatterns.length; i++) {
			if (typePatterns[i] == AnnotationTypePattern.ELLIPSIS) {
				// match ellipsisMatchCount args
				argsIndex += numArgsMatchedByEllipsis;
			} else if (typePatterns[i] == AnnotationTypePattern.ANY) {
				argsIndex++;
			} else {
				// match the argument type at argsIndex with the ExactAnnotationTypePattern
				// we know it is exact because nothing else is allowed in args
				if (someArgs[argsIndex].isPrimitiveType()) {
					return FuzzyBoolean.NO; // can never match
				}
				ExactAnnotationTypePattern ap = (ExactAnnotationTypePattern) typePatterns[i];
				FuzzyBoolean matches = ap.matchesRuntimeType(someArgs[argsIndex]);
				if (matches == FuzzyBoolean.NO) {
					return FuzzyBoolean.MAYBE; // could still match at runtime
				} else {
					argsIndex++;
					ret = ret.and(matches);
				}
			}
		}
		return ret;
	}

	public int size() {
		return typePatterns.length;
	}

	public AnnotationTypePattern get(int index) {
		return typePatterns[index];
	}

	public AnnotationPatternList resolveBindings(IScope scope, Bindings bindings, boolean allowBinding) {
		for (int i = 0; i < typePatterns.length; i++) {
			AnnotationTypePattern p = typePatterns[i];
			if (p != null) {
				typePatterns[i] = typePatterns[i].resolveBindings(scope, bindings, allowBinding);
			}
		}
		return this;
	}

	public AnnotationPatternList resolveReferences(IntMap bindings) {
		int len = typePatterns.length;
		AnnotationTypePattern[] ret = new AnnotationTypePattern[len];
		for (int i = 0; i < len; i++) {
			ret[i] = typePatterns[i].remapAdviceFormals(bindings);
		}
		return new AnnotationPatternList(ret);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for (int i = 0, len = typePatterns.length; i < len; i++) {
			AnnotationTypePattern type = typePatterns[i];
			if (i > 0) {
				buf.append(", ");
			}
			if (type == AnnotationTypePattern.ELLIPSIS) {
				buf.append("..");
			} else {
				String annPatt = type.toString();
				buf.append(annPatt.startsWith("@") ? annPatt.substring(1) : annPatt);
			}
		}
		buf.append(")");
		return buf.toString();
	}

	public boolean equals(Object other) {
		if (!(other instanceof AnnotationPatternList)) {
			return false;
		}
		AnnotationPatternList o = (AnnotationPatternList) other;
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

	public int hashCode() {
		int result = 41;
		for (int i = 0, len = typePatterns.length; i < len; i++) {
			result = 37 * result + typePatterns[i].hashCode();
		}
		return result;
	}

	public static AnnotationPatternList read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		short len = s.readShort();
		AnnotationTypePattern[] arguments = new AnnotationTypePattern[len];
		for (int i = 0; i < len; i++) {
			arguments[i] = AnnotationTypePattern.read(s, context);
		}
		AnnotationPatternList ret = new AnnotationPatternList(arguments);
		ret.readLocation(context, s);
		return ret;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeShort(typePatterns.length);
		for (int i = 0; i < typePatterns.length; i++) {
			typePatterns[i].write(s);
		}
		writeLocation(s);
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	public Object traverse(PatternNodeVisitor visitor, Object data) {
		Object ret = accept(visitor, data);
		for (int i = 0; i < typePatterns.length; i++) {
			typePatterns[i].traverse(visitor, ret);
		}
		return ret;
	}

}
