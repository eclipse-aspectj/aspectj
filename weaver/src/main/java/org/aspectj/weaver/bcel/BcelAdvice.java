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
 *     Alexandre Vasseur    support for @AJ aspects
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.LineNumberTag;
import org.aspectj.apache.bcel.generic.LocalVariableTag;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.IEclipseSourceContext;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Lint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * Advice implemented for BCEL
 *
 * @author Erik Hilsdale
 * @author Jim Hugunin
 * @author Andy Clement
 */
class BcelAdvice extends Advice {

	/**
	 * If a match is not entirely statically determinable, this captures the runtime test that must succeed in order for the advice
	 * to run.
	 */
	private Test runtimeTest;
	private ExposedState exposedState;
	private int containsInvokedynamic = 0;// 0 = dontknow, 1=no, 2=yes

	public BcelAdvice(AjAttribute.AdviceAttribute attribute, Pointcut pointcut, Member adviceSignature, ResolvedType concreteAspect) {
		super(attribute, pointcut, simplify(attribute.getKind(), adviceSignature));
		this.concreteAspect = concreteAspect;
	}

	public boolean bindsProceedingJoinPoint() {
		UnresolvedType[] parameterTypes = signature.getParameterTypes();
		for (UnresolvedType parameterType : parameterTypes) {
			if (parameterType.equals(UnresolvedType.PROCEEDING_JOINPOINT)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A heavyweight BcelMethod object is only required for around advice that will be inlined. For other kinds of advice it is
	 * possible to save some space.
	 */
	private static Member simplify(AdviceKind kind, Member adviceSignature) {
		if (adviceSignature != null) {
			UnresolvedType adviceDeclaringType = adviceSignature.getDeclaringType();
			// if it isnt around advice or it is but inlining is turned off then shrink it to a ResolvedMemberImpl
			if (kind != AdviceKind.Around
					|| ((adviceDeclaringType instanceof ResolvedType) && ((ResolvedType) adviceDeclaringType).getWorld()
							.isXnoInline())) {
				if (adviceSignature instanceof BcelMethod) {
					BcelMethod bm = (BcelMethod) adviceSignature;
					if (bm.getMethod() != null && bm.getMethod().getAnnotations() != null) {
						return adviceSignature;
					}
					ResolvedMemberImpl simplermember = new ResolvedMemberImpl(bm.getKind(), bm.getDeclaringType(),
							bm.getModifiers(), bm.getReturnType(), bm.getName(), bm.getParameterTypes());// ,bm.getExceptions(),bm.getBackingGenericMember()
					// );
					simplermember.setParameterNames(bm.getParameterNames());
					return simplermember;
				}
			}
		}
		return adviceSignature;
	}

	@Override
	public ShadowMunger concretize(ResolvedType fromType, World world, PerClause clause) {
		if (!world.areAllLintIgnored()) {
			suppressLintWarnings(world);
		}
		ShadowMunger ret = super.concretize(fromType, world, clause);
		if (!world.areAllLintIgnored()) {
			clearLintSuppressions(world, this.suppressedLintKinds);
		}
		IfFinder ifinder = new IfFinder();
		ret.getPointcut().accept(ifinder, null);
		boolean hasGuardTest = ifinder.hasIf && getKind() != AdviceKind.Around;
		boolean isAround = getKind() == AdviceKind.Around;
		if ((getExtraParameterFlags() & ThisJoinPoint) != 0) {
			if (!isAround && !hasGuardTest && world.getLint().noGuardForLazyTjp.isEnabled()) {
				// can't build tjp lazily, no suitable test...
				// ... only want to record it once against the advice(bug 133117)
				world.getLint().noGuardForLazyTjp.signal("", getSourceLocation());
			}
		}
		return ret;
	}

	@Override
	public ShadowMunger parameterizeWith(ResolvedType declaringType, Map<String, UnresolvedType> typeVariableMap) {
		Pointcut pc = getPointcut().parameterizeWith(typeVariableMap, declaringType.getWorld());

		BcelAdvice ret = null;
		Member adviceSignature = signature;
		// allows for around advice where the return value is a type variable (see pr115250)
		if (signature instanceof ResolvedMember && signature.getDeclaringType().isGenericType()) {
			adviceSignature = ((ResolvedMember) signature).parameterizedWith(declaringType.getTypeParameters(), declaringType,
					declaringType.isParameterizedType());
		}
		ret = new BcelAdvice(this.attribute, pc, adviceSignature, this.concreteAspect);
		return ret;
	}

	@Override
	public boolean match(Shadow shadow, World world) {
		if (world.areAllLintIgnored()) {
			return super.match(shadow, world);
		} else {
			suppressLintWarnings(world);
			boolean ret = super.match(shadow, world);
			clearLintSuppressions(world, this.suppressedLintKinds);
			return ret;
		}
	}

	@Override
	public void specializeOn(Shadow shadow) {
		if (getKind() == AdviceKind.Around) {
			((BcelShadow) shadow).initializeForAroundClosure();
		}

		// XXX this case is just here for supporting lazy test code
		if (getKind() == null) {
			exposedState = new ExposedState(0);
			return;
		}
		if (getKind().isPerEntry()) {
			exposedState = new ExposedState(0);
		} else if (getKind().isCflow()) {
			exposedState = new ExposedState(nFreeVars);
		} else if (getSignature() != null) {
			exposedState = new ExposedState(getSignature());
		} else {
			exposedState = new ExposedState(0);
			return; // XXX this case is just here for supporting lazy test code
		}

		World world = shadow.getIWorld();
		if (!world.areAllLintIgnored()) {
			suppressLintWarnings(world);
		}
		exposedState.setConcreteAspect(concreteAspect);
		runtimeTest = getPointcut().findResidue(shadow, exposedState);
		if (!world.areAllLintIgnored()) {
			clearLintSuppressions(world, this.suppressedLintKinds);
		}

		// these initializations won't be performed by findResidue, but need to be
		// so that the joinpoint is primed for weaving
		if (getKind() == AdviceKind.PerThisEntry) {
			shadow.getThisVar();
		} else if (getKind() == AdviceKind.PerTargetEntry) {
			shadow.getTargetVar();
		}

		// make sure thisJoinPoint parameters are initialized
		if ((getExtraParameterFlags() & ThisJoinPointStaticPart) != 0) {
			((BcelShadow) shadow).getThisJoinPointStaticPartVar();
			((BcelShadow) shadow).getEnclosingClass().warnOnAddedStaticInitializer(shadow, getSourceLocation());
		}

		if ((getExtraParameterFlags() & ThisJoinPoint) != 0) {
			boolean hasGuardTest = runtimeTest != Literal.TRUE && getKind() != AdviceKind.Around;
			boolean isAround = getKind() == AdviceKind.Around;
			((BcelShadow) shadow).requireThisJoinPoint(hasGuardTest, isAround);
			((BcelShadow) shadow).getEnclosingClass().warnOnAddedStaticInitializer(shadow, getSourceLocation());
			if (!hasGuardTest && world.getLint().multipleAdviceStoppingLazyTjp.isEnabled()) {
				// collect up the problematic advice
				((BcelShadow) shadow).addAdvicePreventingLazyTjp(this);
			}
		}

		if ((getExtraParameterFlags() & ThisEnclosingJoinPointStaticPart) != 0) {
			((BcelShadow) shadow).getThisEnclosingJoinPointStaticPartVar();
			((BcelShadow) shadow).getEnclosingClass().warnOnAddedStaticInitializer(shadow, getSourceLocation());
		}
	}

	private boolean canInline(Shadow s) {
		if (attribute.isProceedInInners()) {
			return false;
		}
		// XXX this guard seems to only be needed for bad test cases
		if (concreteAspect == null || concreteAspect.isMissing()) {
			return false;
		}

		if (concreteAspect.getWorld().isXnoInline()) {
			return false;
		}
		// System.err.println("isWoven? " + ((BcelObjectType)concreteAspect).getLazyClassGen().getWeaverState());
		BcelObjectType boType = BcelWorld.getBcelObjectType(concreteAspect);
		if (boType == null) {
			// Could be a symptom that the aspect failed to build last build... return the default answer of false
			return false;
		}
		// Need isJava8 check
		// Does the advice contain invokedynamic...
		if (boType.javaClass.getMajor() >= Constants.MAJOR_1_8) {
			if (containsInvokedynamic == 0) {
				containsInvokedynamic = 1;
				LazyMethodGen lmg = boType.getLazyClassGen().getLazyMethodGen(this.signature.getName(), this.signature.getSignature(), true);
				// Check Java8 supertypes
				ResolvedType searchType = concreteAspect;
				while (lmg == null) {
					searchType = searchType.getSuperclass();
					if (searchType == null) break;
					ReferenceTypeDelegate rtd = ((ReferenceType)searchType).getDelegate();
					if (rtd instanceof BcelObjectType) {
						BcelObjectType bot = (BcelObjectType)rtd;
						if (bot.javaClass.getMajor() < Constants.MAJOR_1_8) {
							break;
						}
						lmg = bot.getLazyClassGen().getLazyMethodGen(this.signature.getName(), this.signature.getSignature(), true);
					}
				}
				if (lmg != null) {
					InstructionList ilist = lmg.getBody();
					for (InstructionHandle src = ilist.getStart(); src != null; src = src.getNext()) {
						if (src.getInstruction().opcode == Constants.INVOKEDYNAMIC) {
							containsInvokedynamic = 2;
							break;
						}
					}
				}
			}
		}
		if (containsInvokedynamic == 2) {
			return false;
		}
		return boType.getLazyClassGen().isWoven();
	}

	private boolean aspectIsBroken() {
		if (concreteAspect instanceof ReferenceType) {
			ReferenceTypeDelegate rtDelegate = ((ReferenceType) concreteAspect).getDelegate();
			if (!(rtDelegate instanceof BcelObjectType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean implementOn(Shadow s) {
		hasMatchedAtLeastOnce = true;

		// pr263323 - if the aspect is broken then the delegate will not be usable for weaving
		if (aspectIsBroken()) {
			return false;
		}

		BcelShadow shadow = (BcelShadow) s;

		// remove any unnecessary exceptions if the compiler option is set to
		// error or warning and if this piece of advice throws exceptions
		// (bug 129282). This may be expanded to include other compiler warnings
		// at the moment it only deals with 'declared exception is not thrown'
		if (!shadow.getWorld().isIgnoringUnusedDeclaredThrownException() && !getThrownExceptions().isEmpty()) {
			Member member = shadow.getSignature();
			if (member instanceof BcelMethod) {
				removeUnnecessaryProblems((BcelMethod) member, ((BcelMethod) member).getDeclarationLineNumber());
			} else {
				// we're in a call shadow therefore need the line number of the
				// declared method (which may be in a different type). However,
				// we want to remove the problems from the CompilationResult
				// held within the current type's EclipseSourceContext so need
				// the enclosing shadow too
				ResolvedMember resolvedMember = shadow.getSignature().resolve(shadow.getWorld());
				if (resolvedMember instanceof BcelMethod && shadow.getEnclosingShadow() instanceof BcelShadow) {
					Member enclosingMember = shadow.getEnclosingShadow().getSignature();
					if (enclosingMember instanceof BcelMethod) {
						removeUnnecessaryProblems((BcelMethod) enclosingMember,
								((BcelMethod) resolvedMember).getDeclarationLineNumber());
					}
				}
			}
		}

		if (shadow.getIWorld().isJoinpointSynchronizationEnabled() && shadow.getKind() == Shadow.MethodExecution
				&& (s.getSignature().getModifiers() & Modifier.SYNCHRONIZED) != 0) {
			shadow.getIWorld().getLint().advisingSynchronizedMethods.signal(new String[] { shadow.toString() },
					shadow.getSourceLocation(), new ISourceLocation[] { getSourceLocation() });
		}

		// FIXME AV - see #75442, this logic is not enough so for now comment it out until we fix the bug
		// // callback for perObject AJC MightHaveAspect postMunge (#75442)
		// if (getConcreteAspect() != null
		// && getConcreteAspect().getPerClause() != null
		// && PerClause.PEROBJECT.equals(getConcreteAspect().getPerClause().getKind())) {
		// final PerObject clause;
		// if (getConcreteAspect().getPerClause() instanceof PerFromSuper) {
		// clause = (PerObject)((PerFromSuper) getConcreteAspect().getPerClause()).lookupConcretePerClause(getConcreteAspect());
		// } else {
		// clause = (PerObject) getConcreteAspect().getPerClause();
		// }
		// if (clause.isThis()) {
		// PerObjectInterfaceTypeMunger.registerAsAdvisedBy(s.getThisVar().getType(), getConcreteAspect());
		// } else {
		// PerObjectInterfaceTypeMunger.registerAsAdvisedBy(s.getTargetVar().getType(), getConcreteAspect());
		// }
		// }
		if (runtimeTest == Literal.FALSE) { // not usually allowed, except in one case (260384)
			Member sig = shadow.getSignature();
			if (sig.getArity() == 0 && shadow.getKind() == Shadow.MethodCall && sig.getName().charAt(0) == 'c'
					&& sig.getReturnType().equals(ResolvedType.OBJECT) && sig.getName().equals("clone")) {
				return false;
			}
		}

		if (getKind() == AdviceKind.Before) {
			shadow.weaveBefore(this);
		} else if (getKind() == AdviceKind.AfterReturning) {
			shadow.weaveAfterReturning(this);
		} else if (getKind() == AdviceKind.AfterThrowing) {
			UnresolvedType catchType = hasExtraParameter() ? getExtraParameterType() : UnresolvedType.THROWABLE;
			shadow.weaveAfterThrowing(this, catchType);
		} else if (getKind() == AdviceKind.After) {
			shadow.weaveAfter(this);
		} else if (getKind() == AdviceKind.Around) {
			// Note: under regular LTW the aspect is usually loaded after the first use of any class affected by it.
			// This means that as long as the aspect has not been thru the LTW, it's woven state is unknown
			// and thus canInline(s) will return false.
			// To force inlining (test), ones can do Class aspect = FQNAspect.class in the clinit of the target class
			// FIXME AV : for AJC compiled @AJ aspect (or any code style aspect), the woven state can never be known
			// if the aspect belongs to a parent classloader. In that case the aspect will never be inlined.
			// It might be dangerous to change that especially for @AJ aspect non compiled with AJC since if those
			// are not weaved (f.e. use of some limited LTW etc) then they cannot be prepared for inlining.
			// One solution would be to flag @AJ aspect with an annotation as "prepared" and query that one.
			LazyClassGen enclosingClass = shadow.getEnclosingClass();
			if (enclosingClass != null && enclosingClass.isInterface() && shadow.getEnclosingMethod().getName().charAt(0) == '<') {
				// Do not add methods with bodies to an interface (252198, 163005)
				shadow.getWorld().getLint().cannotAdviseJoinpointInInterfaceWithAroundAdvice.signal(shadow.toString(),
						shadow.getSourceLocation());
				return false;
			}
			if (!canInline(s)) {
				shadow.weaveAroundClosure(this, hasDynamicTests());
			} else {
				shadow.weaveAroundInline(this, hasDynamicTests());
			}
		} else if (getKind() == AdviceKind.InterInitializer) {
			shadow.weaveAfterReturning(this);
		} else if (getKind().isCflow()) {
			shadow.weaveCflowEntry(this, getSignature());
		} else if (getKind() == AdviceKind.PerThisEntry) {
			shadow.weavePerObjectEntry(this, (BcelVar) shadow.getThisVar());
		} else if (getKind() == AdviceKind.PerTargetEntry) {
			shadow.weavePerObjectEntry(this, (BcelVar) shadow.getTargetVar());
		} else if (getKind() == AdviceKind.Softener) {
			shadow.weaveSoftener(this, ((ExactTypePattern) exceptionType).getType());
		} else if (getKind() == AdviceKind.PerTypeWithinEntry) {
			// PTWIMPL Entry to ptw is the static initialization of a type that matched the ptw type pattern
			shadow.weavePerTypeWithinAspectInitialization(this, shadow.getEnclosingType());
		} else {
			throw new BCException("unimplemented kind: " + getKind());
		}
		return true;
	}

	private void removeUnnecessaryProblems(BcelMethod method, int problemLineNumber) {
		ISourceContext sourceContext = method.getSourceContext();
		if (sourceContext instanceof IEclipseSourceContext) {
			((IEclipseSourceContext) sourceContext).removeUnnecessaryProblems(method, problemLineNumber);
		}
	}

	// ---- implementations

	private Collection<ResolvedType> collectCheckedExceptions(UnresolvedType[] excs) {
		if (excs == null || excs.length == 0) {
			return Collections.emptyList();
		}

		Collection<ResolvedType> ret = new ArrayList<>();
		World world = concreteAspect.getWorld();
		ResolvedType runtimeException = world.getCoreType(UnresolvedType.RUNTIME_EXCEPTION);
		ResolvedType error = world.getCoreType(UnresolvedType.ERROR);

		for (UnresolvedType exc : excs) {
			ResolvedType t = world.resolve(exc, true);
			if (t.isMissing()) {
				world.getLint().cantFindType
						.signal(WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_EXCEPTION_TYPE, exc.getName()),
								getSourceLocation());
				// IMessage msg = new Message(
				// WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_EXCEPTION_TYPE,excs[i].getName()),
				// "",IMessage.ERROR,getSourceLocation(),null,null);
				// world.getMessageHandler().handleMessage(msg);
			}
			if (!(runtimeException.isAssignableFrom(t) || error.isAssignableFrom(t))) {
				ret.add(t);
			}
		}

		return ret;
	}

	private Collection<ResolvedType> thrownExceptions = null;

	@Override
	public Collection<ResolvedType> getThrownExceptions() {
		if (thrownExceptions == null) {
			// ??? can we really lump in Around here, how does this interact with Throwable
			if (concreteAspect != null && concreteAspect.getWorld() != null && // null tests for test harness
					(getKind().isAfter() || getKind() == AdviceKind.Before || getKind() == AdviceKind.Around)) {
				World world = concreteAspect.getWorld();
				ResolvedMember m = world.resolve(signature);
				if (m == null) {
					thrownExceptions = Collections.emptyList();
				} else {
					thrownExceptions = collectCheckedExceptions(m.getExceptions());
				}
			} else {
				thrownExceptions = Collections.emptyList();
			}
		}
		return thrownExceptions;
	}

	/**
	 * The munger must not check for the advice exceptions to be declared by the shadow in the case of @AJ aspects so that around
	 * can throws Throwable
	 *
	 * @return
	 */
	@Override
	public boolean mustCheckExceptions() {
		if (getConcreteAspect() == null) {
			return true;
		}
		return !getConcreteAspect().isAnnotationStyleAspect();
	}

	// only call me after prepare has been called
	@Override
	public boolean hasDynamicTests() {
		// if (hasExtraParameter() && getKind() == AdviceKind.AfterReturning) {
		// UnresolvedType extraParameterType = getExtraParameterType();
		// if (! extraParameterType.equals(UnresolvedType.OBJECT)
		// && ! extraParameterType.isPrimitive())
		// return true;
		// }

		return runtimeTest != null && !(runtimeTest == Literal.TRUE);// || pointcutTest == Literal.NO_TEST);
	}

	/**
	 * get the instruction list for the really simple version of this advice. Is broken apart for other advice, but if you want it
	 * in one block, this is the method to call.
	 *
	 * @param s The shadow around which these instructions will eventually live.
	 * @param extraArgVar The var that will hold the return value or thrown exception for afterX advice
	 * @param ifNoAdvice The instructionHandle to jump to if the dynamic tests for this munger fails.
	 */
	InstructionList getAdviceInstructions(BcelShadow s, BcelVar extraArgVar, InstructionHandle ifNoAdvice) {
		BcelShadow shadow = s;
		InstructionFactory fact = shadow.getFactory();
		BcelWorld world = shadow.getWorld();

		InstructionList il = new InstructionList();

		// we test to see if we have the right kind of thing...
		// after throwing does this just by the exception mechanism.
		if (hasExtraParameter() && getKind() == AdviceKind.AfterReturning) {
			UnresolvedType extraParameterType = getExtraParameterType();
			if (!extraParameterType.equals(UnresolvedType.OBJECT) && !extraParameterType.isPrimitiveType()) {
				il.append(BcelRenderer.renderTest(fact, world,
						Test.makeInstanceof(extraArgVar, getExtraParameterType().resolve(world)), null, ifNoAdvice, null));
			}
		}
		il.append(getAdviceArgSetup(shadow, extraArgVar, null));
		il.append(getNonTestAdviceInstructions(shadow));

		InstructionHandle ifYesAdvice = il.getStart();
		il.insert(getTestInstructions(shadow, ifYesAdvice, ifNoAdvice, ifYesAdvice));

		// If inserting instructions at the start of a method, we need a nice line number for this entry
		// in the stack trace
		if (shadow.getKind() == Shadow.MethodExecution && getKind() == AdviceKind.Before) {
			int lineNumber = 0;
			// Uncomment this code if you think we should use the method decl line number when it exists...
			// // If the advised join point is in a class built by AspectJ, we can use the declaration line number
			// boolean b = shadow.getEnclosingMethod().getMemberView().hasDeclarationLineNumberInfo();
			// if (b) {
			// lineNumber = shadow.getEnclosingMethod().getMemberView().getDeclarationLineNumber();
			// } else { // If it wasn't, the best we can do is the line number of the first instruction in the method
			lineNumber = shadow.getEnclosingMethod().getMemberView().getLineNumberOfFirstInstruction();
			// }
			InstructionHandle start = il.getStart();
			if (lineNumber > 0) {
				start.addTargeter(new LineNumberTag(lineNumber));
			}
			// Fix up the local variables: find any that have a startPC of 0 and ensure they target the new start of the method
			LocalVariableTable lvt = shadow.getEnclosingMethod().getMemberView().getMethod().getLocalVariableTable();
			if (lvt != null) {
				LocalVariable[] lvTable = lvt.getLocalVariableTable();
				for (LocalVariable lv : lvTable) {
					if (lv.getStartPC() == 0) {
						start.addTargeter(new LocalVariableTag(lv.getSignature(), lv.getName(), lv.getIndex(), 0));
					}
				}
			}
		}

		return il;
	}

	public InstructionList getAdviceArgSetup(BcelShadow shadow, BcelVar extraVar, InstructionList closureInstantiation) {
		InstructionFactory fact = shadow.getFactory();
		BcelWorld world = shadow.getWorld();
		InstructionList il = new InstructionList();

		// if (targetAspectField != null) {
		// il.append(fact.createFieldAccess(
		// targetAspectField.getDeclaringType().getName(),
		// targetAspectField.getName(),
		// BcelWorld.makeBcelType(targetAspectField.getType()),
		// Constants.GETSTATIC));
		// }
		//
		// System.err.println("BcelAdvice: " + exposedState);

		if (exposedState.getAspectInstance() != null) {
			il.append(BcelRenderer.renderExpr(fact, world, exposedState.getAspectInstance()));
		}
		// pr121385
		boolean x = this.getDeclaringAspect().resolve(world).isAnnotationStyleAspect();
		final boolean isAnnotationStyleAspect = getConcreteAspect() != null && getConcreteAspect().isAnnotationStyleAspect() && x;
		boolean previousIsClosure = false;
		for (int i = 0, len = exposedState.size(); i < len; i++) {
			if (exposedState.isErroneousVar(i)) {
				continue; // Erroneous vars have already had error msgs reported!
			}
			BcelVar v = (BcelVar) exposedState.get(i);

			if (v == null) {
				// if not @AJ aspect, go on with the regular binding handling
				if (!isAnnotationStyleAspect) {

				} else {
					// ATAJ: for @AJ aspects, handle implicit binding of xxJoinPoint
					// if (getKind() == AdviceKind.Around) {
					// previousIsClosure = true;
					// il.append(closureInstantiation);
					if ("Lorg/aspectj/lang/ProceedingJoinPoint;".equals(getSignature().getParameterTypes()[i].getSignature())) {
						// make sure we are in an around, since we deal with the closure, not the arg here
						if (getKind() != AdviceKind.Around) {
							previousIsClosure = false;
							getConcreteAspect()
									.getWorld()
									.getMessageHandler()
									.handleMessage(
											new Message("use of ProceedingJoinPoint is allowed only on around advice (" + "arg "
													+ i + " in " + toString() + ")", this.getSourceLocation(), true));
							// try to avoid verify error and pass in null
							il.append(InstructionConstants.ACONST_NULL);
						} else {
							if (previousIsClosure) {
								il.append(InstructionConstants.DUP);
							} else {
								previousIsClosure = true;
								il.append(closureInstantiation.copy());
								shadow.closureVarInitialized = true;
							}
						}
					} else if ("Lorg/aspectj/lang/JoinPoint$StaticPart;".equals(getSignature().getParameterTypes()[i]
							.getSignature())) {
						previousIsClosure = false;
						if ((getExtraParameterFlags() & ThisJoinPointStaticPart) != 0) {
							shadow.getThisJoinPointStaticPartBcelVar().appendLoad(il, fact);
						}
					} else if ("Lorg/aspectj/lang/JoinPoint;".equals(getSignature().getParameterTypes()[i].getSignature())) {
						previousIsClosure = false;
						if ((getExtraParameterFlags() & ThisJoinPoint) != 0) {
							il.append(shadow.loadThisJoinPoint());
						}
					} else if ("Lorg/aspectj/lang/JoinPoint$EnclosingStaticPart;".equals(getSignature().getParameterTypes()[i]
							.getSignature())) {
						previousIsClosure = false;
						if ((getExtraParameterFlags() & ThisEnclosingJoinPointStaticPart) != 0) {
							shadow.getThisEnclosingJoinPointStaticPartBcelVar().appendLoad(il, fact);
						}
					} else if (hasExtraParameter()) {
						previousIsClosure = false;
						extraVar.appendLoadAndConvert(il, fact, getExtraParameterType().resolve(world));
					} else {
						previousIsClosure = false;
						getConcreteAspect()
								.getWorld()
								.getMessageHandler()
								.handleMessage(
										new Message("use of ProceedingJoinPoint is allowed only on around advice (" + "arg " + i
												+ " in " + toString() + ")", this.getSourceLocation(), true));
						// try to avoid verify error and pass in null
						il.append(InstructionConstants.ACONST_NULL);
					}
				}
			} else {
				UnresolvedType desiredTy = getBindingParameterTypes()[i];
				v.appendLoadAndConvert(il, fact, desiredTy.resolve(world));
			}
		}

		// ATAJ: for code style aspect, handles the extraFlag as usual ie not
		// in the middle of the formal bindings but at the end, in a rock solid ordering
		if (!isAnnotationStyleAspect) {
			if (getKind() == AdviceKind.Around) {
				il.append(closureInstantiation);
			} else if (hasExtraParameter()) {
				extraVar.appendLoadAndConvert(il, fact, getExtraParameterType().resolve(world));
			}

			// handle thisJoinPoint parameters
			// these need to be in that same order as parameters in
			// org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration
			if ((getExtraParameterFlags() & ThisJoinPointStaticPart) != 0) {
				shadow.getThisJoinPointStaticPartBcelVar().appendLoad(il, fact);
			}

			if ((getExtraParameterFlags() & ThisJoinPoint) != 0) {
				il.append(shadow.loadThisJoinPoint());
			}

			if ((getExtraParameterFlags() & ThisEnclosingJoinPointStaticPart) != 0) {
				shadow.getThisEnclosingJoinPointStaticPartBcelVar().appendLoad(il, fact);
			}
		}

		return il;
	}

	public InstructionList getNonTestAdviceInstructions(BcelShadow shadow) {
		return new InstructionList(Utility.createInvoke(shadow.getFactory(), shadow.getWorld(), getOriginalSignature()));
	}

	@Override
	public Member getOriginalSignature() {
		Member sig = getSignature();
		if (sig instanceof ResolvedMember) {
			ResolvedMember rsig = (ResolvedMember) sig;
			if (rsig.hasBackingGenericMember()) {
				return rsig.getBackingGenericMember();
			}
		}
		return sig;
	}

	public InstructionList getTestInstructions(BcelShadow shadow, InstructionHandle sk, InstructionHandle fk, InstructionHandle next) {
		// System.err.println("test: " + pointcutTest);
		return BcelRenderer.renderTest(shadow.getFactory(), shadow.getWorld(), runtimeTest, sk, fk, next);
	}

	public int compareTo(Object other) {
		if (!(other instanceof BcelAdvice)) {
			return 0;
		}
		BcelAdvice o = (BcelAdvice) other;

		// System.err.println("compareTo: " + this + ", " + o);
		if (kind.getPrecedence() != o.kind.getPrecedence()) {
			if (kind.getPrecedence() > o.kind.getPrecedence()) {
				return +1;
			} else {
				return -1;
			}
		}

		if (kind.isCflow()) {
			// System.err.println("sort: " + this + " innerCflowEntries " + innerCflowEntries);
			// System.err.println("      " + o + " innerCflowEntries " + o.innerCflowEntries);
			boolean isBelow = (kind == AdviceKind.CflowBelowEntry);

			if (this.innerCflowEntries.contains(o)) {
				return isBelow ? +1 : -1;
			} else if (o.innerCflowEntries.contains(this)) {
				return isBelow ? -1 : +1;
			} else {
				return 0;
			}
		}

		if (kind.isPerEntry() || kind == AdviceKind.Softener) {
			return 0;
		}

		// System.out.println("compare: " + this + " with " + other);
		World world = concreteAspect.getWorld();

		int ret = concreteAspect.getWorld().compareByPrecedence(concreteAspect, o.concreteAspect);
		if (ret != 0) {
			return ret;
		}

		ResolvedType declaringAspect = getDeclaringAspect().resolve(world);
		ResolvedType o_declaringAspect = o.getDeclaringAspect().resolve(world);

		if (declaringAspect == o_declaringAspect) {
			if (kind.isAfter() || o.kind.isAfter()) {
				return this.getStart() < o.getStart() ? -1 : +1;
			} else {
				return this.getStart() < o.getStart() ? +1 : -1;
			}
		} else if (declaringAspect.isAssignableFrom(o_declaringAspect)) {
			return -1;
		} else if (o_declaringAspect.isAssignableFrom(declaringAspect)) {
			return +1;
		} else {
			return 0;
		}
	}

	public BcelVar[] getExposedStateAsBcelVars(boolean isAround) {
		// ATAJ aspect
		if (isAround) {
			// the closure instantiation has the same mapping as the extracted method from wich it is called
			if (getConcreteAspect() != null && getConcreteAspect().isAnnotationStyleAspect()) {
				return BcelVar.NONE;
			}
		}

		// System.out.println("vars: " + Arrays.asList(exposedState.vars));
		if (exposedState == null) {
			return BcelVar.NONE;
		}
		int len = exposedState.vars.length;
		BcelVar[] ret = new BcelVar[len];
		for (int i = 0; i < len; i++) {
			ret[i] = (BcelVar) exposedState.vars[i];
		}
		return ret; // (BcelVar[]) exposedState.vars;
	}

	protected void suppressLintWarnings(World inWorld) {
		if (suppressedLintKinds == null) {
			if (signature instanceof BcelMethod) {
				this.suppressedLintKinds = Utility.getSuppressedWarnings(signature.getAnnotations(), inWorld.getLint());
			} else {
				this.suppressedLintKinds = Collections.emptyList();
				return;
			}
		}
		inWorld.getLint().suppressKinds(suppressedLintKinds);
	}

	protected void clearLintSuppressions(World inWorld, Collection<Lint.Kind> toClear) {
		inWorld.getLint().clearSuppressions(toClear);
	}

	/**
	 * For testing only
	 */
	public BcelAdvice(AdviceKind kind, Pointcut pointcut, Member signature, int extraArgumentFlags, int start, int end,
			ISourceContext sourceContext, ResolvedType concreteAspect) {
		this(new AjAttribute.AdviceAttribute(kind, pointcut, extraArgumentFlags, start, end, sourceContext), pointcut, signature,
				concreteAspect);
		thrownExceptions = Collections.emptyList(); // !!! interaction with unit tests
	}

}