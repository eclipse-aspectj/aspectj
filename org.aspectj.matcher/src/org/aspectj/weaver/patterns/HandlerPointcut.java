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

import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

/**
 * This is a kind of KindedPointcut. This belongs either in a hierarchy with it or in a new place to share code with other potential
 * future statement-level pointcuts like synchronized and throws
 */
public class HandlerPointcut extends Pointcut {
	TypePattern exceptionType;

	private static final int MATCH_KINDS = Shadow.ExceptionHandler.bit;

	public HandlerPointcut(TypePattern exceptionType) {
		this.exceptionType = exceptionType;
		this.pointcutKind = HANDLER;
	}

	public int couldMatchKinds() {
		return MATCH_KINDS;
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		// ??? should be able to do better by finding all referenced types in type
		return FuzzyBoolean.MAYBE;
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		if (shadow.getKind() != Shadow.ExceptionHandler) {
			return FuzzyBoolean.NO;
		}

		exceptionType.resolve(shadow.getIWorld());

		// we know we have exactly one parameter since we're checking an exception handler
		return exceptionType.matches(shadow.getSignature().getParameterTypes()[0].resolve(shadow.getIWorld()), TypePattern.STATIC);
	}

	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		HandlerPointcut ret = new HandlerPointcut(exceptionType.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	public boolean equals(Object other) {
		if (!(other instanceof HandlerPointcut)) {
			return false;
		}
		HandlerPointcut o = (HandlerPointcut) other;
		return o.exceptionType.equals(this.exceptionType);
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + exceptionType.hashCode();
		return result;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("handler(");
		buf.append(exceptionType.toString());
		buf.append(")");
		return buf.toString();
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.HANDLER);
		exceptionType.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		HandlerPointcut ret = new HandlerPointcut(TypePattern.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	// XXX note: there is no namebinding in any kinded pointcut.
	// still might want to do something for better error messages
	// We want to do something here to make sure we don't sidestep the parameter
	// list in capturing type identifiers.
	public void resolveBindings(IScope scope, Bindings bindings) {
		exceptionType = exceptionType.resolveBindings(scope, bindings, false, false);
		boolean invalidParameterization = false;
		if (exceptionType.getTypeParameters().size() > 0) {
			invalidParameterization = true;
		}
		UnresolvedType exactType = exceptionType.getExactType();
		if (exactType != null && exactType.isParameterizedType()) {
			invalidParameterization = true;
		}
		if (invalidParameterization) {
			// no parameterized or generic types for handler
			scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.HANDLER_PCD_DOESNT_SUPPORT_PARAMETERS),
					getSourceLocation()));
		}
		// XXX add error if exact binding and not an exception
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}

	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		Pointcut ret = new HandlerPointcut(exceptionType);
		ret.copyLocationFrom(this);
		return ret;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
