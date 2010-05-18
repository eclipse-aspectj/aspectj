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

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

public class WithinPointcut extends Pointcut {
	private TypePattern typePattern;

	public WithinPointcut(TypePattern type) {
		this.typePattern = type;
		this.pointcutKind = WITHIN;
	}

	public TypePattern getTypePattern() {
		return typePattern;
	}

	private FuzzyBoolean isWithinType(ResolvedType type) {
		while (type != null) {
			if (typePattern.matchesStatically(type)) {
				return FuzzyBoolean.YES;
			}
			type = type.getDeclaringType();
		}
		return FuzzyBoolean.NO;
	}

	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS;
	}

	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		WithinPointcut ret = new WithinPointcut(this.typePattern.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		if (typePattern.annotationPattern instanceof AnyAnnotationTypePattern) {
			return isWithinType(info.getType());
		}
		return FuzzyBoolean.MAYBE;
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		ResolvedType enclosingType = shadow.getIWorld().resolve(shadow.getEnclosingType(), true);
		if (enclosingType.isMissing()) {
			shadow.getIWorld().getLint().cantFindType.signal(new String[] { WeaverMessages.format(
					WeaverMessages.CANT_FIND_TYPE_WITHINPCD, shadow.getEnclosingType().getName()) }, shadow.getSourceLocation(),
					new ISourceLocation[] { getSourceLocation() });
		}
		typePattern.resolve(shadow.getIWorld());
		return isWithinType(enclosingType);
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.WITHIN);
		typePattern.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		TypePattern type = TypePattern.read(s, context);
		WithinPointcut ret = new WithinPointcut(type);
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		typePattern = typePattern.resolveBindings(scope, bindings, false, false);

		// look for parameterized type patterns which are not supported...
		HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
		typePattern.traverse(visitor, null);
		if (visitor.wellHasItThen/* ? */()) {
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.WITHIN_PCD_DOESNT_SUPPORT_PARAMETERS),
					getSourceLocation()));
		}
	}

	public void postRead(ResolvedType enclosingType) {
		typePattern.postRead(enclosingType);
	}

	public boolean couldEverMatchSameJoinPointsAs(WithinPointcut other) {
		return typePattern.couldEverMatchSameTypesAs(other.typePattern);
	}

	public boolean equals(Object other) {
		if (!(other instanceof WithinPointcut)) {
			return false;
		}
		WithinPointcut o = (WithinPointcut) other;
		return o.typePattern.equals(this.typePattern);
	}

	public int hashCode() {
		return typePattern.hashCode();
	}

	public String toString() {
		return "within(" + typePattern + ")";
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}

	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		Pointcut ret = new WithinPointcut(typePattern);
		ret.copyLocationFrom(this);
		return ret;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
