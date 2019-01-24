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
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

/**
 * !TypePattern
 * 
 * <p>
 * any binding to formals is explicitly forbidden for any composite, ! is just the most obviously wrong case.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class NotTypePattern extends TypePattern {
	private TypePattern negatedPattern;
	private boolean isBangVoid = false; // is this '!void'
	private boolean checked = false;

	public NotTypePattern(TypePattern pattern) {
		super(false, false); // ??? we override all methods that care about includeSubtypes
		this.negatedPattern = pattern;
		setLocation(pattern.getSourceContext(), pattern.getStart(), pattern.getEnd());
	}

	public TypePattern getNegatedPattern() {
		return negatedPattern;
	}

	@Override
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true;
	}

	@Override
	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
		return negatedPattern.matchesInstanceof(type).not();
	}

	@Override
	protected boolean matchesExactly(ResolvedType type) {
		return (!negatedPattern.matchesExactly(type) && annotationPattern.matches(type).alwaysTrue());
	}

	@Override
	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		return (!negatedPattern.matchesExactly(type, annotatedType) && annotationPattern.matches(annotatedType).alwaysTrue());
	}

	@Override
	public boolean matchesStatically(ResolvedType type) {
		return !negatedPattern.matchesStatically(type);
	}

	@Override
	public void setAnnotationTypePattern(AnnotationTypePattern annPatt) {
		super.setAnnotationTypePattern(annPatt);
	}

	@Override
	public void setIsVarArgs(boolean isVarArgs) {
		negatedPattern.setIsVarArgs(isVarArgs);
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(TypePattern.NOT);
		negatedPattern.write(s);
		annotationPattern.write(s);
		writeLocation(s);
	}

	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		TypePattern ret = new NotTypePattern(TypePattern.read(s, context));
		if (s.getMajorVersion() >= AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {
			ret.annotationPattern = AnnotationTypePattern.read(s, context);
		}
		ret.readLocation(context, s);
		return ret;
	}

	@Override
	public TypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding, boolean requireExactType) {
		if (requireExactType) {
			return notExactType(scope);
		}
		negatedPattern = negatedPattern.resolveBindings(scope, bindings, false, false);
		return this;
	}

	@Override
	public boolean isBangVoid() {
		if (!checked) {
			isBangVoid = negatedPattern.getExactType().isVoid();
			checked = true;
		}
		return isBangVoid;
	}

	@Override
	public TypePattern parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		TypePattern newNegatedPattern = negatedPattern.parameterizeWith(typeVariableMap, w);
		NotTypePattern ret = new NotTypePattern(newNegatedPattern);
		ret.copyLocationFrom(this);
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
		buff.append('!');
		buff.append(negatedPattern);
		if (annotationPattern != AnnotationTypePattern.ANY) {
			buff.append(')');
		}
		return buff.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NotTypePattern)) {
			return false;
		}
		return (negatedPattern.equals(((NotTypePattern) obj).negatedPattern));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 17 + 37 * negatedPattern.hashCode();
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	public Object traverse(PatternNodeVisitor visitor, Object data) {
		Object ret = accept(visitor, data);
		negatedPattern.traverse(visitor, ret);
		return ret;
	}

}
