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

package org.aspectj.weaver;

import java.util.Collections;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;

public abstract class Advice extends ShadowMunger {

	protected AjAttribute.AdviceAttribute attribute;
	protected transient AdviceKind kind; // alias for attribute.getKind()
	protected Member signature;
	private boolean isAnnotationStyle;

	// not necessarily declaring aspect, this is a semantics change from 1.0
	protected ResolvedType concreteAspect; // null until after concretize

	// Just for Cflow*entry kinds
	protected List<ShadowMunger> innerCflowEntries = Collections.emptyList();
	protected int nFreeVars;

	protected TypePattern exceptionType; // just for Softener kind

	// if we are parameterized, these type may be different to the advice
	// signature types
	protected UnresolvedType[] bindingParameterTypes;

	protected boolean hasMatchedAtLeastOnce = false;

	// based on annotations on this advice
	protected List<Lint.Kind> suppressedLintKinds = null;

	public ISourceLocation lastReportedMonitorExitJoinpointLocation = null;

	public static Advice makeCflowEntry(World world, Pointcut entry, boolean isBelow, Member stackField, int nFreeVars,
			List<ShadowMunger> innerCflowEntries, ResolvedType inAspect) {
		Advice ret = world.createAdviceMunger(isBelow ? AdviceKind.CflowBelowEntry : AdviceKind.CflowEntry, entry, stackField, 0,
				entry, inAspect);
		ret.innerCflowEntries = innerCflowEntries;
		ret.nFreeVars = nFreeVars;
		ret.setDeclaringType(inAspect); // correct?
		return ret;
	}

	public static Advice makePerCflowEntry(World world, Pointcut entry, boolean isBelow, Member stackField, ResolvedType inAspect,
			List<ShadowMunger> innerCflowEntries) {
		Advice ret = world.createAdviceMunger(isBelow ? AdviceKind.PerCflowBelowEntry : AdviceKind.PerCflowEntry, entry,
				stackField, 0, entry, inAspect);
		ret.innerCflowEntries = innerCflowEntries;
		ret.concreteAspect = inAspect;
		return ret;
	}

	public static Advice makePerObjectEntry(World world, Pointcut entry, boolean isThis, ResolvedType inAspect) {
		Advice ret = world.createAdviceMunger(isThis ? AdviceKind.PerThisEntry : AdviceKind.PerTargetEntry, entry, null, 0, entry,
				inAspect);

		ret.concreteAspect = inAspect;
		return ret;
	}

	// PTWIMPL per type within entry advice is what initializes the aspect
	// instance in the matched type
	public static Advice makePerTypeWithinEntry(World world, Pointcut p, ResolvedType inAspect) {
		Advice ret = world.createAdviceMunger(AdviceKind.PerTypeWithinEntry, p, null, 0, p, inAspect);
		ret.concreteAspect = inAspect;
		return ret;
	}

	public static Advice makeSoftener(World world, Pointcut entry, TypePattern exceptionType, ResolvedType inAspect,
			IHasSourceLocation loc) {
		Advice ret = world.createAdviceMunger(AdviceKind.Softener, entry, null, 0, loc, inAspect);
		ret.exceptionType = exceptionType;
		return ret;
	}

	public Advice(AjAttribute.AdviceAttribute attribute, Pointcut pointcut, Member signature) {
		super(pointcut, attribute.getStart(), attribute.getEnd(), attribute.getSourceContext(), ShadowMungerAdvice);
		this.attribute = attribute;
		this.isAnnotationStyle = signature != null && !signature.getName().startsWith("ajc$");
		this.kind = attribute.getKind(); // alias
		this.signature = signature;
		if (signature != null) {
			bindingParameterTypes = signature.getParameterTypes();
		} else {
			bindingParameterTypes = new UnresolvedType[0];
		}
	}

	@Override
	public boolean match(Shadow shadow, World world) {
		if (super.match(shadow, world)) {
			if (shadow.getKind() == Shadow.ExceptionHandler) {
				if (kind.isAfter() || kind == AdviceKind.Around) {
					world.showMessage(IMessage.WARNING, WeaverMessages.format(WeaverMessages.ONLY_BEFORE_ON_HANDLER),
							getSourceLocation(), shadow.getSourceLocation());
					return false;
				}
			}
			if (shadow.getKind() == Shadow.SynchronizationLock || shadow.getKind() == Shadow.SynchronizationUnlock) {
				if (kind == AdviceKind.Around
				// Don't work, see comments in SynchronizationTests
				// && attribute.getProceedCallSignatures()!=null
				// && attribute.getProceedCallSignatures().length!=0
				) {
					world.showMessage(IMessage.WARNING, WeaverMessages.format(WeaverMessages.NO_AROUND_ON_SYNCHRONIZATION),
							getSourceLocation(), shadow.getSourceLocation());
					return false;
				}
			}

			if (hasExtraParameter() && kind == AdviceKind.AfterReturning) {
				ResolvedType resolvedExtraParameterType = getExtraParameterType().resolve(world);
				ResolvedType shadowReturnType = shadow.getReturnType().resolve(world);
				boolean matches = (resolvedExtraParameterType.isConvertableFrom(shadowReturnType) && shadow.getKind()
						.hasReturnValue());
				if (matches && resolvedExtraParameterType.isParameterizedType()) {
					maybeIssueUncheckedMatchWarning(resolvedExtraParameterType, shadowReturnType, shadow, world);
				}
				return matches;
			} else if (hasExtraParameter() && kind == AdviceKind.AfterThrowing) { // pr119749
				ResolvedType exceptionType = getExtraParameterType().resolve(world);
				if (!exceptionType.isCheckedException() || exceptionType.getName().equals("java.lang.Exception")) { // pr292239
					return true;
				}
				UnresolvedType[] shadowThrows = shadow.getSignature().getExceptions(world);
				boolean matches = false;
				for (int i = 0; i < shadowThrows.length && !matches; i++) {
					ResolvedType type = shadowThrows[i].resolve(world);
					if (exceptionType.isAssignableFrom(type)) {
						matches = true;
					}
				}
				return matches;
			} else if (kind == AdviceKind.PerTargetEntry) {
				return shadow.hasTarget();
			} else if (kind == AdviceKind.PerThisEntry) {
				// Groovy Constructors have a strange switch statement in them - this switch statement can leave us in places where
				// the
				// instance is not initialized (a super ctor hasn't been called yet).
				// In these situations it isn't safe to do a perObjectBind, the instance is not initialized and cannot be passed
				// over.
				if (shadow.getEnclosingCodeSignature().getName().equals("<init>")) {
					if (world.resolve(shadow.getEnclosingType()).isGroovyObject()) {
						return false;
					}
				}
				return shadow.hasThis();
			} else if (kind == AdviceKind.Around) {
				if (shadow.getKind() == Shadow.PreInitialization) {
					world.showMessage(IMessage.WARNING, WeaverMessages.format(WeaverMessages.AROUND_ON_PREINIT),
							getSourceLocation(), shadow.getSourceLocation());
					return false;
				} else if (shadow.getKind() == Shadow.Initialization) {
					world.showMessage(IMessage.WARNING, WeaverMessages.format(WeaverMessages.AROUND_ON_INIT), getSourceLocation(),
							shadow.getSourceLocation());
					return false;
				} else if (shadow.getKind() == Shadow.StaticInitialization
						&& shadow.getEnclosingType().resolve(world).isInterface()) {
					world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.AROUND_ON_INTERFACE_STATICINIT, shadow
							.getEnclosingType().getName()), getSourceLocation(), shadow.getSourceLocation());
					return false;
				} else {
					// System.err.println(getSignature().getReturnType() +
					// " from " + shadow.getReturnType());
					if (getSignature().getReturnType().equals(UnresolvedType.VOID)) {
						if (!shadow.getReturnType().equals(UnresolvedType.VOID)) {
							String s = shadow.toString();
							String s2 = WeaverMessages.format(WeaverMessages.NON_VOID_RETURN, s);
							world.showMessage(IMessage.ERROR, s2, getSourceLocation(), shadow.getSourceLocation());
							return false;
						}
					} else if (getSignature().getReturnType().equals(UnresolvedType.OBJECT)) {
						return true;
					} else {
						ResolvedType shadowReturnType = shadow.getReturnType().resolve(world);
						ResolvedType adviceReturnType = getSignature().getGenericReturnType().resolve(world);

						if (shadowReturnType.isParameterizedType() && adviceReturnType.isRawType()) { // Set
							// <
							// Integer
							// >
							// and
							// Set
							ResolvedType shadowReturnGenericType = shadowReturnType.getGenericType(); // Set
							ResolvedType adviceReturnGenericType = adviceReturnType.getGenericType(); // Set
							if (shadowReturnGenericType.isAssignableFrom(adviceReturnGenericType)
									&& world.getLint().uncheckedAdviceConversion.isEnabled()) {
								world.getLint().uncheckedAdviceConversion.signal(
										new String[] { shadow.toString(), shadowReturnType.getName(), adviceReturnType.getName() },
										shadow.getSourceLocation(), new ISourceLocation[] { getSourceLocation() });
							}
						} else if (!shadowReturnType.isAssignableFrom(adviceReturnType)) {
							// System.err.println(this + ", " + sourceContext +
							// ", " + start);
							world.showMessage(IMessage.ERROR,
									WeaverMessages.format(WeaverMessages.INCOMPATIBLE_RETURN_TYPE, shadow), getSourceLocation(),
									shadow.getSourceLocation());
							return false;
						}
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * In after returning advice if we are binding the extra parameter to a parameterized type we may not be able to do a type-safe
	 * conversion.
	 * 
	 * @param resolvedExtraParameterType the type in the after returning declaration
	 * @param shadowReturnType the type at the shadow
	 * @param world
	 */
	private void maybeIssueUncheckedMatchWarning(ResolvedType afterReturningType, ResolvedType shadowReturnType, Shadow shadow,
			World world) {
		boolean inDoubt = !afterReturningType.isAssignableFrom(shadowReturnType);
		if (inDoubt && world.getLint().uncheckedArgument.isEnabled()) {
			String uncheckedMatchWith = afterReturningType.getSimpleBaseName();
			if (shadowReturnType.isParameterizedType() && (shadowReturnType.getRawType() == afterReturningType.getRawType())) {
				uncheckedMatchWith = shadowReturnType.getSimpleName();
			}
			if (!Utils.isSuppressing(getSignature().getAnnotations(), "uncheckedArgument")) {
				world.getLint().uncheckedArgument.signal(new String[] { afterReturningType.getSimpleName(), uncheckedMatchWith,
						afterReturningType.getSimpleBaseName(), shadow.toResolvedString(world) }, getSourceLocation(),
						new ISourceLocation[] { shadow.getSourceLocation() });
			}
		}
	}

	// ----

	public AdviceKind getKind() {
		return kind;
	}

	public Member getSignature() {
		return signature;
	}

	public boolean hasExtraParameter() {
		return (getExtraParameterFlags() & ExtraArgument) != 0;
	}

	protected int getExtraParameterFlags() {
		return attribute.getExtraParameterFlags();
	}

	protected int getExtraParameterCount() {
		return countOnes(getExtraParameterFlags() & ParameterMask);
	}

	public UnresolvedType[] getBindingParameterTypes() {
		return bindingParameterTypes;
	}

	public void setBindingParameterTypes(UnresolvedType[] types) {
		bindingParameterTypes = types;
	}

	public static int countOnes(int bits) {
		int ret = 0;
		while (bits != 0) {
			if ((bits & 1) != 0) {
				ret += 1;
			}
			bits = bits >> 1;
		}
		return ret;
	}

	public int getBaseParameterCount() {
		return getSignature().getParameterTypes().length - getExtraParameterCount();
	}

	public String[] getBaseParameterNames(World world) {
		String[] allNames = getSignature().getParameterNames(world);
		int extras = getExtraParameterCount();
		if (extras == 0) {
			return allNames;
		}
		String[] result = new String[getBaseParameterCount()];
		for (int i = 0; i < result.length; i++) {
			result[i] = allNames[i];
		}
		return result;
	}

	/**
	 * Return the type of the 'extra argument'. For either after returning or after throwing advice, the extra argument will be the
	 * returned value or the thrown exception respectively. With annotation style the user may declare the parameters in any order,
	 * whereas for code style they are in a well defined order. So there is some extra complexity in here for annotation style that
	 * looks up the correct parameter in the advice signature by name, based on the name specified in the annotation. If this fails
	 * then we 'fallback' to guessing at positions, where the extra argument is presumed to come at the end.
	 * 
	 * @return the type of the extraParameter
	 */
	public UnresolvedType getExtraParameterType() {
		if (!hasExtraParameter()) {
			return ResolvedType.MISSING;
		}
		if (signature instanceof ResolvedMember) {
			ResolvedMember method = (ResolvedMember) signature;
			UnresolvedType[] parameterTypes = method.getGenericParameterTypes();
			if (getConcreteAspect().isAnnotationStyleAspect()) {

				// Examine the annotation to determine the parameter name then look it up in the parameters for the method
				String[] pnames = method.getParameterNames();
				if (pnames != null) {
					// It is worth attempting to look up the correct parameter
					AnnotationAJ[] annos = getSignature().getAnnotations();
					String parameterToLookup = null;
					if (annos != null && (getKind() == AdviceKind.AfterThrowing || getKind() == AdviceKind.AfterReturning)) {
						for (int i = 0; i < annos.length && parameterToLookup == null; i++) {
							AnnotationAJ anno = annos[i];
							String annosig = anno.getType().getSignature();
							if (annosig.equals("Lorg/aspectj/lang/annotation/AfterThrowing;")) {
								// the 'throwing' value in the annotation will name the parameter to bind to
								parameterToLookup = anno.getStringFormOfValue("throwing");
							} else if (annosig.equals("Lorg/aspectj/lang/annotation/AfterReturning;")) {
								// the 'returning' value in the annotation will name the parameter to bind to
								parameterToLookup = anno.getStringFormOfValue("returning");
							}
						}
					}
					if (parameterToLookup != null) {
						for (int i = 0; i < pnames.length; i++) {
							if (pnames[i].equals(parameterToLookup)) {
								return parameterTypes[i];
							}
						}
					}
				}

				// Don't think this code works so well... why isnt it getBaseParameterCount()-1 ?

				int baseParmCnt = getBaseParameterCount();

				// bug 122742 - if we're an annotation style aspect then one
				// of the extra parameters could be JoinPoint which we want
				// to ignore
				while ((baseParmCnt + 1 < parameterTypes.length)
						&& (parameterTypes[baseParmCnt].equals(AjcMemberMaker.TYPEX_JOINPOINT)
								|| parameterTypes[baseParmCnt].equals(AjcMemberMaker.TYPEX_STATICJOINPOINT) || parameterTypes[baseParmCnt]
									.equals(AjcMemberMaker.TYPEX_ENCLOSINGSTATICJOINPOINT))) {
					baseParmCnt++;
				}
				return parameterTypes[baseParmCnt];
			} else {
				return parameterTypes[getBaseParameterCount()];
			}
		} else {
			return signature.getParameterTypes()[getBaseParameterCount()];
		}
	}

	public UnresolvedType getDeclaringAspect() {
		return getOriginalSignature().getDeclaringType();
	}

	protected Member getOriginalSignature() {
		return signature;
	}

	protected String extraParametersToString() {
		if (getExtraParameterFlags() == 0) {
			return "";
		} else {
			return "(extraFlags: " + getExtraParameterFlags() + ")";
		}
	}

	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}

	// ----

	/**
	 * @param fromType is guaranteed to be a non-abstract aspect
	 * @param clause has been concretized at a higher level
	 */
	@Override
	public ShadowMunger concretize(ResolvedType fromType, World world, PerClause clause) {
		// assert !fromType.isAbstract();
		Pointcut p = pointcut.concretize(fromType, getDeclaringType(), signature.getArity(), this);
		if (clause != null) {
			Pointcut oldP = p;
			p = new AndPointcut(clause, p);
			p.copyLocationFrom(oldP);
			p.state = Pointcut.CONCRETE;

			// FIXME ? ATAJ copy unbound bindings to ignore
			p.m_ignoreUnboundBindingForNames = oldP.m_ignoreUnboundBindingForNames;
		}

		Advice munger = world.getWeavingSupport().createAdviceMunger(attribute, p, signature, fromType);
		munger.bindingParameterTypes = bindingParameterTypes;
		munger.setDeclaringType(getDeclaringType());
		// System.err.println("concretizing here " + p + " with clause " +
		// clause);
		return munger;
	}

	// ---- from object

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(").append(getKind()).append(extraParametersToString());
		sb.append(": ").append(pointcut).append("->").append(signature).append(")");
		return sb.toString();
		// return "("
		// + getKind()
		// + extraParametersToString()
		// + ": "
		// + pointcut
		// + "->"
		// + signature
		// + ")";
	}

	// XXX this perhaps ought to take account of the other fields in advice ...
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Advice)) {
			return false;
		}
		Advice o = (Advice) other;
		return o.kind.equals(kind) && ((o.pointcut == null) ? (pointcut == null) : o.pointcut.equals(pointcut))
				&& ((o.signature == null) ? (signature == null) : o.signature.equals(signature));
		// && (AsmManager.getDefault().getHandleProvider().dependsOnLocation() ? ((o.getSourceLocation() == null) ?
		// (getSourceLocation() == null)
		// : o.getSourceLocation().equals(getSourceLocation()))
		// : true) // pr134471 - remove when handles are improved
		// // to be independent of location
		// ;

	}

	private volatile int hashCode = 0;

	@Override
	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			result = 37 * result + kind.hashCode();
			result = 37 * result + ((pointcut == null) ? 0 : pointcut.hashCode());
			result = 37 * result + ((signature == null) ? 0 : signature.hashCode());
			hashCode = result;
		}
		return hashCode;
	}

	// ---- fields

	public static final int ExtraArgument = 0x01;
	public static final int ThisJoinPoint = 0x02;
	public static final int ThisJoinPointStaticPart = 0x04;
	public static final int ThisEnclosingJoinPointStaticPart = 0x08;
	public static final int ParameterMask = 0x0f;
	// For an if pointcut, this indicates it is hard wired to access a constant of either true or false
	public static final int ConstantReference = 0x10;
	// When the above flag is set, this indicates whether it is true or false
	public static final int ConstantValue = 0x20;
	// public static final int CanInline = 0x40; // didnt appear to be getting used
	public static final int ThisAspectInstance = 0x40;

	// cant use 0x80 ! the value is written out as a byte and -1 has special meaning (-1 is 0x80...)

	// for testing only
	public void setLexicalPosition(int lexicalPosition) {
		start = lexicalPosition;
	}

	public boolean isAnnotationStyle() {
		return isAnnotationStyle;
	}

	public ResolvedType getConcreteAspect() {
		return concreteAspect;
	}

	public boolean hasMatchedSomething() {
		return hasMatchedAtLeastOnce;
	}

	public void setHasMatchedSomething(boolean hasMatchedSomething) {
		hasMatchedAtLeastOnce = hasMatchedSomething;
	}

	public abstract boolean hasDynamicTests();

}
