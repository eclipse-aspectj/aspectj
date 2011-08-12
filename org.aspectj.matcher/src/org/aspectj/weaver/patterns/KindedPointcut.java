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

import static org.aspectj.util.FuzzyBoolean.MAYBE;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Checker;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

public class KindedPointcut extends Pointcut {
	Shadow.Kind kind;
	private SignaturePattern signature;
	private int matchKinds;

	private ShadowMunger munger = null; // only set after concretization

	public KindedPointcut(Shadow.Kind kind, SignaturePattern signature) {
		this.kind = kind;
		this.signature = signature;
		this.pointcutKind = KINDED;
		this.matchKinds = kind.bit;
	}

	public KindedPointcut(Shadow.Kind kind, SignaturePattern signature, ShadowMunger munger) {
		this(kind, signature);
		this.munger = munger;
	}

	public SignaturePattern getSignature() {
		return signature;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.patterns.Pointcut#couldMatchKinds()
	 */
	@Override
	public int couldMatchKinds() {
		return matchKinds;
	}

	public boolean couldEverMatchSameJoinPointsAs(KindedPointcut other) {
		if (this.kind != other.kind) {
			return false;
		}
		String myName = signature.getName().maybeGetSimpleName();
		String yourName = other.signature.getName().maybeGetSimpleName();
		if (myName != null && yourName != null) {
			if (!myName.equals(yourName)) {
				return false;
			}
		}
		if (signature.getParameterTypes().ellipsisCount == 0) {
			if (other.signature.getParameterTypes().ellipsisCount == 0) {
				if (signature.getParameterTypes().getTypePatterns().length != other.signature.getParameterTypes().getTypePatterns().length) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		// info.getKind()==null means all kinds
		if (info.getKind() != null) {
			if (info.getKind() != kind) {
				return FuzzyBoolean.NO;
			}
		}

		// KindedPointcut represents these join points:
		// method-execution/ctor-execution/method-call/ctor-call/field-get/field-set/advice-execution/static-initialization
		// initialization/pre-initialization

		// Check if the global fastmatch flag is on - the flag can be removed (and this made default) once it proves stable!
		if (info.world.optimizedMatching) {

			// For now, just consider MethodExecution and Initialization
			if ((kind == Shadow.MethodExecution || kind == Shadow.Initialization) && info.getKind() == null) {
				boolean fastMatchingOnAspect = info.getType().isAspect();
				// an Aspect may define ITDs, and although our signature declaring type pattern may not match on the
				// aspect, the ITDs may have a different signature as we iterate through the members of the aspect. Let's not
				// try and work through that here and just say MAYBE
				if (fastMatchingOnAspect) {
					return MAYBE;
				}
				// Aim here is to do the same test as is done for signature pattern declaring type pattern matching
				if (this.getSignature().isExactDeclaringTypePattern()) {
					ExactTypePattern typePattern = (ExactTypePattern) this.getSignature().getDeclaringType();
					// Interface checks are more expensive, they could be anywhere...
					ResolvedType patternExactType = typePattern.getResolvedExactType(info.world);
					if (patternExactType.isInterface()) {
						ResolvedType curr = info.getType();
						Iterator<ResolvedType> hierarchyWalker = curr.getHierarchy(true, true);
						boolean found = false;
						while (hierarchyWalker.hasNext()) {
							curr = hierarchyWalker.next();
							if (typePattern.matchesStatically(curr)) {
								found = true;
								break;
							}
						}
						if (!found) {
							return FuzzyBoolean.NO;
						}
					} else if (patternExactType.isClass()) {
						ResolvedType curr = info.getType();
						do {
							if (typePattern.matchesStatically(curr)) {
								break;
							}
							curr = curr.getSuperclass();
						} while (curr != null);
						if (curr == null) {
							return FuzzyBoolean.NO;
						}
					}
				} else if (this.getSignature().getDeclaringType() instanceof AnyWithAnnotationTypePattern) {
					// aim here is to say NO if the annotation is not possible in the hierarchy here
					ResolvedType type = info.getType();
					AnnotationTypePattern annotationTypePattern = ((AnyWithAnnotationTypePattern) getSignature().getDeclaringType())
							.getAnnotationPattern();
					if (annotationTypePattern instanceof ExactAnnotationTypePattern) {
						ExactAnnotationTypePattern exactAnnotationTypePattern = (ExactAnnotationTypePattern) annotationTypePattern;
						if (exactAnnotationTypePattern.getAnnotationValues() == null
								|| exactAnnotationTypePattern.getAnnotationValues().size() == 0) {
							ResolvedType annotationType = exactAnnotationTypePattern.getAnnotationType().resolve(info.world);
							if (type.hasAnnotation(annotationType)) {
								return FuzzyBoolean.MAYBE;
							}
							if (annotationType.isInheritedAnnotation()) {
								// ok - we may be picking it up from further up the hierarchy (but only a super*class*)
								ResolvedType toMatchAgainst = type.getSuperclass();
								boolean found = false;
								while (toMatchAgainst != null) {
									if (toMatchAgainst.hasAnnotation(annotationType)) {
										found = true;
										break;
									}
									toMatchAgainst = toMatchAgainst.getSuperclass();
								}
								if (!found) {
									return FuzzyBoolean.NO;
								}
							} else {
								return FuzzyBoolean.NO;
							}
						}
					}
				}
			}
		}

		return FuzzyBoolean.MAYBE;
	}

	@Override
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		if (shadow.getKind() != kind) {
			return FuzzyBoolean.NO;
		}

		if (shadow.getKind() == Shadow.SynchronizationLock && kind == Shadow.SynchronizationLock) {
			return FuzzyBoolean.YES;
		}
		if (shadow.getKind() == Shadow.SynchronizationUnlock && kind == Shadow.SynchronizationUnlock) {
			return FuzzyBoolean.YES;
		}

		if (!signature.matches(shadow.getMatchingSignature(), shadow.getIWorld(), this.kind == Shadow.MethodCall)) {

			if (kind == Shadow.MethodCall) {
				warnOnConfusingSig(shadow);
				// warnOnBridgeMethod(shadow);
			}
			return FuzzyBoolean.NO;
		}

		return FuzzyBoolean.YES;
	}

	// private void warnOnBridgeMethod(Shadow shadow) {
	// if (shadow.getIWorld().getLint().noJoinpointsForBridgeMethods.isEnabled()) {
	// ResolvedMember rm = shadow.getSignature().resolve(shadow.getIWorld());
	// if (rm!=null) {
	// int shadowModifiers = rm.getModifiers(); //shadow.getSignature().getModifiers(shadow.getIWorld());
	// if (ResolvedType.hasBridgeModifier(shadowModifiers)) {
	// shadow.getIWorld().getLint().noJoinpointsForBridgeMethods.signal(new String[]{},getSourceLocation(),
	// new ISourceLocation[]{shadow.getSourceLocation()});
	// }
	// }
	// }
	// }

	private void warnOnConfusingSig(Shadow shadow) {
		// Don't do all this processing if we don't need to !
		if (!shadow.getIWorld().getLint().unmatchedSuperTypeInCall.isEnabled()) {
			return;
		}

		// no warnings for declare error/warning
		if (munger instanceof Checker) {
			return;
		}

		World world = shadow.getIWorld();

		// warning never needed if the declaring type is any
		UnresolvedType exactDeclaringType = signature.getDeclaringType().getExactType();

		ResolvedType shadowDeclaringType = shadow.getSignature().getDeclaringType().resolve(world);

		if (signature.getDeclaringType().isStar() || ResolvedType.isMissing(exactDeclaringType)
				|| exactDeclaringType.resolve(world).isMissing()) {
			return;
		}

		// warning not needed if match type couldn't ever be the declaring type
		if (!shadowDeclaringType.isAssignableFrom(exactDeclaringType.resolve(world))) {
			return;
		}

		// if the method in the declaring type is *not* visible to the
		// exact declaring type then warning not needed.
		ResolvedMember rm = shadow.getSignature().resolve(world);
		// rm can be null in the case where we are binary weaving, and looking at a class with a call to a method in another class,
		// but because of class incompatibilities, the method does not exist on the target class anymore.
		// this will be reported elsewhere.
		if (rm == null) {
			return;
		}

		int shadowModifiers = rm.getModifiers();
		if (!ResolvedType.isVisible(shadowModifiers, shadowDeclaringType, exactDeclaringType.resolve(world))) {
			return;
		}

		if (!signature.getReturnType().matchesStatically(shadow.getSignature().getReturnType().resolve(world))) {
			// Covariance issue...
			// The reason we didn't match is that the type pattern for the pointcut (Car) doesn't match the
			// return type for the specific declaration at the shadow. (FastCar Sub.getCar())
			// XXX Put out another XLINT in this case?
			return;
		}
		// PR60015 - Don't report the warning if the declaring type is object and 'this' is an interface
		if (exactDeclaringType.resolve(world).isInterface() && shadowDeclaringType.equals(world.resolve("java.lang.Object"))) {
			return;
		}

		SignaturePattern nonConfusingPattern = new SignaturePattern(signature.getKind(), signature.getModifiers(),
				signature.getReturnType(), TypePattern.ANY, signature.getName(), signature.getParameterTypes(),
				signature.getThrowsPattern(), signature.getAnnotationPattern());

		if (nonConfusingPattern.matches(shadow.getSignature(), shadow.getIWorld(), true)) {
			shadow.getIWorld().getLint().unmatchedSuperTypeInCall.signal(new String[] {
					shadow.getSignature().getDeclaringType().toString(), signature.getDeclaringType().toString() },
					this.getSourceLocation(), new ISourceLocation[] { shadow.getSourceLocation() });
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof KindedPointcut)) {
			return false;
		}
		KindedPointcut o = (KindedPointcut) other;
		return o.kind == this.kind && o.signature.equals(this.signature);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + kind.hashCode();
		result = 37 * result + signature.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(kind.getSimpleName());
		buf.append("(");
		buf.append(signature.toString());
		buf.append(")");
		return buf.toString();
	}

	@Override
	public void postRead(ResolvedType enclosingType) {
		signature.postRead(enclosingType);
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Pointcut.KINDED);
		kind.write(s);
		signature.write(s);
		writeLocation(s);
	}

	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		Shadow.Kind kind = Shadow.Kind.read(s);
		SignaturePattern sig = SignaturePattern.read(s, context);
		KindedPointcut ret = new KindedPointcut(kind, sig);
		ret.readLocation(context, s);
		return ret;
	}

	// XXX note: there is no namebinding in any kinded pointcut.
	// still might want to do something for better error messages
	// We want to do something here to make sure we don't sidestep the parameter
	// list in capturing type identifiers.
	@Override
	public void resolveBindings(IScope scope, Bindings bindings) {
		if (kind == Shadow.Initialization) {
			// scope.getMessageHandler().handleMessage(
			// MessageUtil.error(
			// "initialization unimplemented in 1.1beta1",
			// this.getSourceLocation()));
		}
		signature = signature.resolveBindings(scope, bindings);

		if (kind == Shadow.ConstructorExecution) { // Bug fix 60936
			if (signature.getDeclaringType() != null) {
				World world = scope.getWorld();
				UnresolvedType exactType = signature.getDeclaringType().getExactType();
				if (signature.getKind() == Member.CONSTRUCTOR && !ResolvedType.isMissing(exactType)
						&& exactType.resolve(world).isInterface() && !signature.getDeclaringType().isIncludeSubtypes()) {
					world.getLint().noInterfaceCtorJoinpoint.signal(exactType.toString(), getSourceLocation());
				}
			}
		}

		// no parameterized types
		if (kind == Shadow.StaticInitialization) {
			HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
			signature.getDeclaringType().traverse(visitor, null);
			if (visitor.wellHasItThen/* ? */()) {
				scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.NO_STATIC_INIT_JPS_FOR_PARAMETERIZED_TYPES),
						getSourceLocation()));
			}
		}

		// no parameterized types in declaring type position
		if ((kind == Shadow.FieldGet) || (kind == Shadow.FieldSet)) {
			HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
			signature.getDeclaringType().traverse(visitor, null);
			if (visitor.wellHasItThen/* ? */()) {
				scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.GET_AND_SET_DONT_SUPPORT_DEC_TYPE_PARAMETERS),
						getSourceLocation()));
			}

			// fields can't have a void type!
			UnresolvedType returnType = signature.getReturnType().getExactType();
			if (returnType.equals(UnresolvedType.VOID)) {
				scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.FIELDS_CANT_HAVE_VOID_TYPE),
						getSourceLocation()));
			}
		}

		// no join points for initialization and preinitialization of parameterized types
		// no throwable parameterized types
		if ((kind == Shadow.Initialization) || (kind == Shadow.PreInitialization)) {
			HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
			signature.getDeclaringType().traverse(visitor, null);
			if (visitor.wellHasItThen/* ? */()) {
				scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.NO_INIT_JPS_FOR_PARAMETERIZED_TYPES),
						getSourceLocation()));
			}

			visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
			signature.getThrowsPattern().traverse(visitor, null);
			if (visitor.wellHasItThen/* ? */()) {
				scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.NO_GENERIC_THROWABLES), getSourceLocation()));
			}
		}

		// no parameterized types in declaring type position
		// no throwable parameterized types
		if ((kind == Shadow.MethodExecution) || (kind == Shadow.ConstructorExecution)) {
			HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
			signature.getDeclaringType().traverse(visitor, null);
			if (visitor.wellHasItThen/* ? */()) {
				scope.message(MessageUtil.error(
						WeaverMessages.format(WeaverMessages.EXECUTION_DOESNT_SUPPORT_PARAMETERIZED_DECLARING_TYPES),
						getSourceLocation()));
			}

			visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
			signature.getThrowsPattern().traverse(visitor, null);
			if (visitor.wellHasItThen/* ? */()) {
				scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.NO_GENERIC_THROWABLES), getSourceLocation()));
			}
		}

		// no parameterized types in declaring type position
		// no throwable parameterized types
		if ((kind == Shadow.MethodCall) || (kind == Shadow.ConstructorCall)) {
			HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
			signature.getDeclaringType().traverse(visitor, null);
			if (visitor.wellHasItThen/* ? */()) {
				scope.message(MessageUtil.error(
						WeaverMessages.format(WeaverMessages.CALL_DOESNT_SUPPORT_PARAMETERIZED_DECLARING_TYPES),
						getSourceLocation()));
			}

			visitor = new HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor();
			signature.getThrowsPattern().traverse(visitor, null);
			if (visitor.wellHasItThen/* ? */()) {
				scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.NO_GENERIC_THROWABLES), getSourceLocation()));
			}
			if (!scope.getWorld().isJoinpointArrayConstructionEnabled() && kind == Shadow.ConstructorCall
					&& signature.getDeclaringType().isArray()) {
				scope.message(MessageUtil.warn(WeaverMessages.format(WeaverMessages.NO_NEWARRAY_JOINPOINTS_BY_DEFAULT),
						getSourceLocation()));
			}
		}
	}

	@Override
	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}

	@Override
	public Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {
		Pointcut ret = new KindedPointcut(kind, signature, bindings.getEnclosingAdvice());
		ret.copyLocationFrom(this);
		return ret;
	}

	@Override
	public Pointcut parameterizeWith(Map typeVariableMap, World w) {
		Pointcut ret = new KindedPointcut(kind, signature.parameterizeWith(typeVariableMap, w), munger);
		ret.copyLocationFrom(this);
		return ret;
	}

	public Shadow.Kind getKind() {
		return kind;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
