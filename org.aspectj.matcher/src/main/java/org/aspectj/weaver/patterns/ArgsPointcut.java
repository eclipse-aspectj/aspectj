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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.BCException;
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
 * args(arguments)
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public class ArgsPointcut extends NameBindingPointcut {
	private static final String ASPECTJ_JP_SIGNATURE_PREFIX = "Lorg/aspectj/lang/JoinPoint";
	private static final String ASPECTJ_SYNTHETIC_SIGNATURE_PREFIX = "Lorg/aspectj/runtime/internal/";

	private TypePatternList arguments;
	private String stringRepresentation;

	public ArgsPointcut(TypePatternList arguments) {
		this.arguments = arguments;
		this.pointcutKind = ARGS;
		this.stringRepresentation = "args" + arguments.toString() + "";
	}

	public TypePatternList getArguments() {
		return arguments;
	}

	public Pointcut parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		ArgsPointcut ret = new ArgsPointcut(this.arguments.parameterizeWith(typeVariableMap, w));
		ret.copyLocationFrom(this);
		return ret;
	}

	public int couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS_BITS; // empty args() matches jps with no args
	}

	public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}

	protected FuzzyBoolean matchInternal(Shadow shadow) {
		ResolvedType[] argumentsToMatchAgainst = getArgumentsToMatchAgainst(shadow);
		FuzzyBoolean ret = arguments.matches(argumentsToMatchAgainst, TypePattern.DYNAMIC);
		return ret;
	}

	private ResolvedType[] getArgumentsToMatchAgainst(Shadow shadow) {

		if (shadow.isShadowForArrayConstructionJoinpoint()) {
			return shadow.getArgumentTypesForArrayConstructionShadow();
		}

		ResolvedType[] argumentsToMatchAgainst = shadow.getIWorld().resolve(shadow.getGenericArgTypes());

		// special treatment for adviceexecution which may have synthetic arguments we
		// want to ignore.
		if (shadow.getKind() == Shadow.AdviceExecution) {
			int numExtraArgs = 0;
			for (ResolvedType resolvedType : argumentsToMatchAgainst) {
				String argumentSignature = resolvedType.getSignature();
				if (argumentSignature.startsWith(ASPECTJ_JP_SIGNATURE_PREFIX)
						|| argumentSignature.startsWith(ASPECTJ_SYNTHETIC_SIGNATURE_PREFIX)) {
					numExtraArgs++;
				} else {
					// normal arg after AJ type means earlier arg was NOT synthetic
					numExtraArgs = 0;
				}
			}
			if (numExtraArgs > 0) {
				int newArgLength = argumentsToMatchAgainst.length - numExtraArgs;
				ResolvedType[] argsSubset = new ResolvedType[newArgLength];
				System.arraycopy(argumentsToMatchAgainst, 0, argsSubset, 0, newArgLength);
				argumentsToMatchAgainst = argsSubset;
			}
		} else if (shadow.getKind() == Shadow.ConstructorExecution) {
			if (shadow.getMatchingSignature().getParameterTypes().length < argumentsToMatchAgainst.length) {
				// there are one or more synthetic args on the end, caused by non-public itd constructor
				int newArgLength = shadow.getMatchingSignature().getParameterTypes().length;
				ResolvedType[] argsSubset = new ResolvedType[newArgLength];
				System.arraycopy(argumentsToMatchAgainst, 0, argsSubset, 0, newArgLength);
				argumentsToMatchAgainst = argsSubset;
			}
		}
		return argumentsToMatchAgainst;
	}

	public List<BindingPattern> getBindingAnnotationTypePatterns() {
		return Collections.emptyList();
	}

	public List<BindingTypePattern> getBindingTypePatterns() {
		List<BindingTypePattern> l = new ArrayList<>();
		TypePattern[] pats = arguments.getTypePatterns();
		for (TypePattern pat : pats) {
			if (pat instanceof BindingTypePattern) {
				l.add((BindingTypePattern) pat);
			}
		}
		return l;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.ARGS);
		arguments.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		ArgsPointcut ret = new ArgsPointcut(TypePatternList.read(s, context));
		ret.readLocation(context, s);
		return ret;
	}

	public boolean equals(Object other) {
		if (!(other instanceof ArgsPointcut)) {
			return false;
		}
		ArgsPointcut o = (ArgsPointcut) other;
		return o.arguments.equals(this.arguments);
	}

	public int hashCode() {
		return arguments.hashCode();
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		arguments.resolveBindings(scope, bindings, true, true);
		if (arguments.ellipsisCount > 1) {
			scope.message(IMessage.ERROR, this, "uses more than one .. in args (compiler limitation)");
		}
	}

	public void postRead(ResolvedType enclosingType) {
		arguments.postRead(enclosingType);
	}

	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		if (isDeclare(bindings.getEnclosingAdvice())) {
			// Enforce rule about which designators are supported in declare
			inAspect.getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.ARGS_IN_DECLARE),
					bindings.getEnclosingAdvice().getSourceLocation(), null);
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		TypePatternList args = arguments.resolveReferences(bindings);
		if (inAspect.crosscuttingMembers != null) {
			inAspect.crosscuttingMembers.exposeTypes(args.getExactTypes());
		}
		Pointcut ret = new ArgsPointcut(args);
		ret.copyLocationFrom(this);
		return ret;
	}

	private Test findResidueNoEllipsis(Shadow shadow, ExposedState state, TypePattern[] patterns) {
		ResolvedType[] argumentsToMatchAgainst = getArgumentsToMatchAgainst(shadow);
		int len = argumentsToMatchAgainst.length;
		// System.err.println("boudn to : " + len + ", " + patterns.length);
		if (patterns.length != len) {
			return Literal.FALSE;
		}

		Test ret = Literal.TRUE;

		for (int i = 0; i < len; i++) {
			UnresolvedType argType = shadow.getGenericArgTypes()[i];
			TypePattern type = patterns[i];
			ResolvedType argRTX = shadow.getIWorld().resolve(argType, true);
			if (!(type instanceof BindingTypePattern)) {
				if (argRTX.isMissing()) {
					shadow.getIWorld().getLint().cantFindType.signal(new String[] { WeaverMessages.format(
							WeaverMessages.CANT_FIND_TYPE_ARG_TYPE, argType.getName()) }, shadow.getSourceLocation(),
							new ISourceLocation[] { getSourceLocation() });
					// IMessage msg = new Message(
					// WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_ARG_TYPE,argType.getName()),
					// "",IMessage.ERROR,shadow.getSourceLocation(),null,new ISourceLocation[]{getSourceLocation()});
					// shadow.getIWorld().getMessageHandler().handleMessage(msg);
				}
				if (type.matchesInstanceof(argRTX).alwaysTrue()) {
					continue;
				}
			}

			World world = shadow.getIWorld();
			ResolvedType typeToExpose = type.getExactType().resolve(world);
			if (typeToExpose.isParameterizedType()) {
				boolean inDoubt = (type.matchesInstanceof(argRTX) == FuzzyBoolean.MAYBE);
				if (inDoubt && world.getLint().uncheckedArgument.isEnabled()) {
					String uncheckedMatchWith = typeToExpose.getSimpleBaseName();
					if (argRTX.isParameterizedType() && (argRTX.getRawType() == typeToExpose.getRawType())) {
						uncheckedMatchWith = argRTX.getSimpleName();
					}
					if (!isUncheckedArgumentWarningSuppressed()) {
						world.getLint().uncheckedArgument.signal(new String[] { typeToExpose.getSimpleName(), uncheckedMatchWith,
								typeToExpose.getSimpleBaseName(), shadow.toResolvedString(world) }, getSourceLocation(),
								new ISourceLocation[] { shadow.getSourceLocation() });
					}
				}
			}

			ret = Test.makeAnd(ret, exposeStateForVar(shadow.getArgVar(i), type, state, shadow.getIWorld()));
		}

		return ret;
	}

	/**
	 * We need to find out if someone has put the @SuppressAjWarnings{"uncheckedArgument"} annotation somewhere. That somewhere is
	 * going to be an a piece of advice that uses this pointcut. But how do we find it???
	 * 
	 * @return
	 */
	private boolean isUncheckedArgumentWarningSuppressed() {
		return false;
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		ResolvedType[] argsToMatch = getArgumentsToMatchAgainst(shadow);
		if (arguments.matches(argsToMatch, TypePattern.DYNAMIC).alwaysFalse()) {
			return Literal.FALSE;
		}
		int ellipsisCount = arguments.ellipsisCount;
		if (ellipsisCount == 0) {
			return findResidueNoEllipsis(shadow, state, arguments.getTypePatterns());
		} else if (ellipsisCount == 1) {
			TypePattern[] patternsWithEllipsis = arguments.getTypePatterns();
			TypePattern[] patternsWithoutEllipsis = new TypePattern[argsToMatch.length];
			int lenWithEllipsis = patternsWithEllipsis.length;
			int lenWithoutEllipsis = patternsWithoutEllipsis.length;
			// l1+1 >= l0
			int indexWithEllipsis = 0;
			int indexWithoutEllipsis = 0;
			while (indexWithoutEllipsis < lenWithoutEllipsis) {
				TypePattern p = patternsWithEllipsis[indexWithEllipsis++];
				if (p == TypePattern.ELLIPSIS) {
					int newLenWithoutEllipsis = lenWithoutEllipsis - (lenWithEllipsis - indexWithEllipsis);
					while (indexWithoutEllipsis < newLenWithoutEllipsis) {
						patternsWithoutEllipsis[indexWithoutEllipsis++] = TypePattern.ANY;
					}
				} else {
					patternsWithoutEllipsis[indexWithoutEllipsis++] = p;
				}
			}
			return findResidueNoEllipsis(shadow, state, patternsWithoutEllipsis);
		} else {
			throw new BCException("unimplemented");
		}
	}

	public String toString() {
		return this.stringRepresentation;
	}

	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
