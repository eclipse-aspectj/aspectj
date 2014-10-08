/* *******************************************************************
 * Copyright (c) 2002, 2010 Palo Alto Research Center, Incorporated (PARC) and others.
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
import java.lang.reflect.Modifier;
import java.util.Map;

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;


/**
 * This type represents a type pattern of '*' but with an annotation specified, e.g. '@Color *'
 */
public class AnyWithAnnotationTypePattern extends TypePattern {

	public AnyWithAnnotationTypePattern(AnnotationTypePattern atp) {
		super(false, false);
		annotationPattern = atp;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	@Override
	protected boolean couldEverMatchSameTypesAs(TypePattern other) {
		return true;
	}

	@Override
	protected boolean matchesExactly(ResolvedType type) {
		annotationPattern.resolve(type.getWorld());
		boolean b = false;
		if (type.temporaryAnnotationTypes != null) {
			b = annotationPattern.matches(type, type.temporaryAnnotationTypes).alwaysTrue();
		} else {
			b = annotationPattern.matches(type).alwaysTrue();
		}
		return b;
	}

	@Override
	public TypePattern resolveBindings(IScope scope, Bindings bindings, boolean allowBinding, boolean requireExactType) {
		if (requireExactType) {
			scope.getWorld().getMessageHandler().handleMessage(
					MessageUtil.error(WeaverMessages.format(WeaverMessages.WILDCARD_NOT_ALLOWED), getSourceLocation()));
			return NO;
		}
		return super.resolveBindings(scope, bindings, allowBinding, requireExactType);
	}

	@Override
	protected boolean matchesExactly(ResolvedType type, ResolvedType annotatedType) {
		annotationPattern.resolve(type.getWorld());
		return annotationPattern.matches(annotatedType).alwaysTrue();
	}

	@Override
	public FuzzyBoolean matchesInstanceof(ResolvedType type) {
		if (Modifier.isFinal(type.getModifiers())) {
			return FuzzyBoolean.fromBoolean(matchesExactly(type));
		}
		return FuzzyBoolean.MAYBE;
	}

	@Override
	public TypePattern parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		AnyWithAnnotationTypePattern ret = new AnyWithAnnotationTypePattern(this.annotationPattern.parameterizeWith(
				typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(TypePattern.ANY_WITH_ANNO);
		annotationPattern.write(s);
		writeLocation(s);
	}

	public static TypePattern read(VersionedDataInputStream s, ISourceContext c) throws IOException {
		AnnotationTypePattern annPatt = AnnotationTypePattern.read(s, c);
		AnyWithAnnotationTypePattern ret = new AnyWithAnnotationTypePattern(annPatt);
		ret.readLocation(c, s);
		return ret;
	}

	// public FuzzyBoolean matches(IType type, MatchKind kind) {
	// return FuzzyBoolean.YES;
	// }

	@Override
	protected boolean matchesSubtypes(ResolvedType type) {
		return true;
	}

	@Override
	public boolean isStar() {
		return false;
	}

	@Override
	public String toString() {
		return "(" + annotationPattern + " *)";
	}
	
	public AnnotationTypePattern getAnnotationTypePattern() {
		return annotationPattern;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AnyWithAnnotationTypePattern)) {
			return false;
		}
		AnyWithAnnotationTypePattern awatp = (AnyWithAnnotationTypePattern) obj;
		return (annotationPattern.equals(awatp.annotationPattern));
	}

	@Override
	public int hashCode() {
		return annotationPattern.hashCode();
	}
}
