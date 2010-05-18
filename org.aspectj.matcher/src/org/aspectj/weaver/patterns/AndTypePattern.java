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
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

/**
 * left && right
 * 
 * <p>
 * any binding to formals is explicitly forbidden for any composite by the language
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class AndTypePattern extends TypePattern {
	private TypePattern left, right;

	public AndTypePattern(TypePattern left, TypePattern right) {
		super(false, false); // ?? we override all methods that care about includeSubtypes
		this.left = left;
		this.right = right;
		setLocation(left.getSourceContext(), left.getStart(), right.getEnd());
	}

	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true; // don't dive into ands yet....
	}

	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
		return left.matchesInstanceof(type).and(right.matchesInstanceof(type));
	}

	protected boolean matchesExactly(ResolvedType type) {
		// ??? if these had side-effects, this sort-circuit could be a mistake
		return left.matchesExactly(type) && right.matchesExactly(type);
	}

	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		return left.matchesExactly(type, annotatedType) && right.matchesExactly(type, annotatedType);
	}

	public boolean matchesStatically(ResolvedType type) {
		return left.matchesStatically(type) && right.matchesStatically(type);
	}

	public void setIsVarArgs(boolean isVarArgs) {
		this.isVarArgs = isVarArgs;
		left.setIsVarArgs(isVarArgs);
		right.setIsVarArgs(isVarArgs);
	}

	public void setAnnotationTypePattern(AnnotationTypePattern annPatt) {
		if (annPatt == AnnotationTypePattern.ANY) {
			return;
		}
		if (left.annotationPattern == AnnotationTypePattern.ANY) {
			left.setAnnotationTypePattern(annPatt);
		} else {
			left.setAnnotationTypePattern(new AndAnnotationTypePattern(left.annotationPattern, annPatt));
		}
		if (right.annotationPattern == AnnotationTypePattern.ANY) {
			right.setAnnotationTypePattern(annPatt);
		} else {
			right.setAnnotationTypePattern(new AndAnnotationTypePattern(right.annotationPattern, annPatt));
		}
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(TypePattern.AND);
		left.write(s);
		right.write(s);
		writeLocation(s);
	}

	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		AndTypePattern ret = new AndTypePattern(TypePattern.read(s, context), TypePattern.read(s, context));
		ret.readLocation(context, s);
		if (ret.left.isVarArgs && ret.right.isVarArgs) {
			ret.isVarArgs = true;
		}
		return ret;
	}

	public TypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding, boolean requireExactType) {
		if (requireExactType) {
			return notExactType(scope);
		}
		left = left.resolveBindings(scope, bindings, false, false);
		right = right.resolveBindings(scope, bindings, false, false);
		return this;
	}

	public TypePattern parameterizeWith(Map typeVariableMap, World w) {
		TypePattern newLeft = left.parameterizeWith(typeVariableMap, w);
		TypePattern newRight = right.parameterizeWith(typeVariableMap, w);
		AndTypePattern ret = new AndTypePattern(newLeft, newRight);
		ret.copyLocationFrom(this);
		return ret;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		if (annotationPattern != AnnotationTypePattern.ANY) {
			buff.append('(');
			buff.append(annotationPattern.toString());
			buff.append(' ');
		}
		buff.append('(');
		buff.append(left.toString());
		buff.append(" && ");
		buff.append(right.toString());
		buff.append(')');
		if (annotationPattern != AnnotationTypePattern.ANY) {
			buff.append(')');
		}
		return buff.toString();
	}

	public TypePattern getLeft() {
		return left;
	}

	public TypePattern getRight() {
		return right;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof AndTypePattern)) {
			return false;
		}
		AndTypePattern atp = (AndTypePattern) obj;
		return left.equals(atp.left) && right.equals(atp.right);
	}

	public boolean isStarAnnotation() {
		return left.isStarAnnotation() && right.isStarAnnotation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int ret = 17;
		ret = ret + 37 * left.hashCode();
		ret = ret + 37 * right.hashCode();
		return ret;
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

}
