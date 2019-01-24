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

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.PartialOrder;
import org.aspectj.util.TypeSafeEnum;
import org.aspectj.weaver.ast.Var;

/*
 * The superclass of anything representing a the shadow of a join point.  A shadow represents
 * some bit of code, and encompasses both entry and exit from that code.  All shadows have a kind
 * and a signature.
 */

public abstract class Shadow {

	// every Shadow has a unique id, doesn't matter if it wraps...
	private static int nextShadowID = 100; // easier to spot than zero. // OPTIMIZE is this a bug? static?

	private final Kind kind;
	private final Member signature;
	private Member matchingSignature;
	private ResolvedMember resolvedSignature;
	protected final Shadow enclosingShadow;
	protected List<ShadowMunger> mungers = Collections.emptyList();

	public int shadowId = nextShadowID++; // every time we build a shadow, it gets a new id

	// ----
	protected Shadow(Kind kind, Member signature, Shadow enclosingShadow) {
		this.kind = kind;
		this.signature = signature;
		this.enclosingShadow = enclosingShadow;
	}

	// ----

	public abstract World getIWorld();

	public List<ShadowMunger> getMungers() {
		return mungers;
	}

	/**
	 * could this(*) pcd ever match
	 */
	public final boolean hasThis() {
		if (getKind().neverHasThis()) {
			return false;
		} else if (getKind().isEnclosingKind()) {
			return !Modifier.isStatic(getSignature().getModifiers());
		} else if (enclosingShadow == null) {
			return false;
		} else {
			return enclosingShadow.hasThis();
		}
	}

	/**
	 * the type of the this object here
	 * 
	 * @throws IllegalStateException if there is no this here
	 */
	public final UnresolvedType getThisType() {
		if (!hasThis()) {
			throw new IllegalStateException("no this");
		}
		if (getKind().isEnclosingKind()) {
			return getSignature().getDeclaringType();
		} else {
			return enclosingShadow.getThisType();
		}
	}

	/**
	 * a var referencing this
	 * 
	 * @throws IllegalStateException if there is no target here
	 */
	public abstract Var getThisVar();

	/**
	 * could target(*) pcd ever match
	 */
	public final boolean hasTarget() {
		if (getKind().neverHasTarget()) {
			return false;
		} else if (getKind().isTargetSameAsThis()) {
			return hasThis();
		} else {
			return !Modifier.isStatic(getSignature().getModifiers());
		}
	}

	/**
	 * the type of the target object here
	 * 
	 * @throws IllegalStateException if there is no target here
	 */
	public final UnresolvedType getTargetType() {
		if (!hasTarget()) {
			throw new IllegalStateException("no target");
		}
		return getSignature().getDeclaringType();
	}

	/**
	 * a var referencing the target
	 * 
	 * @throws IllegalStateException if there is no target here
	 */
	public abstract Var getTargetVar();

	public UnresolvedType[] getArgTypes() {
		if (getKind() == FieldSet) {
			return new UnresolvedType[] { getSignature().getReturnType() };
		}
		return getSignature().getParameterTypes();
	}

	public boolean isShadowForArrayConstructionJoinpoint() {
		return (getKind() == ConstructorCall && signature.getDeclaringType().isArray());
	}

	public boolean isShadowForMonitor() {
		return (getKind() == SynchronizationLock || getKind() == SynchronizationUnlock);
	}

	// will return the right length array of ints depending on how many dimensions the array has
	public ResolvedType[] getArgumentTypesForArrayConstructionShadow() {
		String s = signature.getDeclaringType().getSignature();
		int pos = s.indexOf("[");
		int dims = 1;
		while (pos < s.length()) {
			pos++;
			if (pos < s.length()) {
				dims += (s.charAt(pos) == '[' ? 1 : 0);
			}
		}
		ResolvedType intType = UnresolvedType.INT.resolve(this.getIWorld());
		if (dims == 1) {
			return new ResolvedType[] { intType };
		}
		ResolvedType[] someInts = new ResolvedType[dims];
		for (int i = 0; i < dims; i++) {
			someInts[i] = intType;
		}
		return someInts;
	}

	public UnresolvedType[] getGenericArgTypes() {
		if (isShadowForArrayConstructionJoinpoint()) {
			return getArgumentTypesForArrayConstructionShadow();
		}
		if (isShadowForMonitor()) {
			return UnresolvedType.ARRAY_WITH_JUST_OBJECT;
		}
		if (getKind() == FieldSet) {
			return new UnresolvedType[] { getResolvedSignature().getGenericReturnType() };
		}
		return getResolvedSignature().getGenericParameterTypes();
	}

	public UnresolvedType getArgType(int arg) {
		if (getKind() == FieldSet) {
			return getSignature().getReturnType();
		}
		return getSignature().getParameterTypes()[arg];
	}

	public int getArgCount() {
		if (getKind() == FieldSet) {
			return 1;
		}
		return getSignature().getParameterTypes().length;
	}

	// /**
	// * Return name of the argument at position 'i' at this shadow. This does not make sense for all shadows - but can be useful in
	// * the case of, for example, method-execution.
	// *
	// * @return null if it cannot be determined
	// */
	// public String getArgName(int i, World w) {
	// String[] names = getSignature().getParameterNames(w);
	// if (names == null || i >= names.length)
	// return null;
	// return names[i];
	// }

	public abstract UnresolvedType getEnclosingType();

	public abstract Var getArgVar(int i);

	public abstract Var getThisJoinPointVar();

	public abstract Var getThisJoinPointStaticPartVar();

	public abstract Var getThisEnclosingJoinPointStaticPartVar();

	public abstract Var getThisAspectInstanceVar(ResolvedType aspectType);

	// annotation variables
	public abstract Var getKindedAnnotationVar(UnresolvedType forAnnotationType);

	public abstract Var getWithinAnnotationVar(UnresolvedType forAnnotationType);

	public abstract Var getWithinCodeAnnotationVar(UnresolvedType forAnnotationType);

	public abstract Var getThisAnnotationVar(UnresolvedType forAnnotationType);

	public abstract Var getTargetAnnotationVar(UnresolvedType forAnnotationType);

	public abstract Var getArgAnnotationVar(int i, UnresolvedType forAnnotationType);

	public abstract Member getEnclosingCodeSignature();

	/**
	 * returns the kind of shadow this is, representing what happens under this shadow
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * returns the signature of the thing under this shadow
	 */
	public Member getSignature() {
		return signature;
	}

	/**
	 * returns the signature of the thing under this shadow, with any synthetic arguments removed
	 */
	public Member getMatchingSignature() {
		return matchingSignature != null ? matchingSignature : signature;
	}

	public void setMatchingSignature(Member member) {
		this.matchingSignature = member;
	}

	/**
	 * returns the resolved signature of the thing under this shadow
	 * 
	 */
	public ResolvedMember getResolvedSignature() {
		if (resolvedSignature == null) {
			resolvedSignature = signature.resolve(getIWorld());
		}
		return resolvedSignature;
	}

	public UnresolvedType getReturnType() {
		if (kind == ConstructorCall) {
			return getSignature().getDeclaringType();
		} else if (kind == FieldSet) {
			return UnresolvedType.VOID;
		} else if (kind == SynchronizationLock || kind == SynchronizationUnlock) {
			return UnresolvedType.VOID;
		}
		return getResolvedSignature().getGenericReturnType();
	}

	public static String METHOD_EXECUTION = "method-execution";
	public static String METHOD_CALL = "method-call";
	public static String CONSTRUCTOR_EXECUTION = "constructor-execution";
	public static String CONSTRUCTOR_CALL = "constructor-call";
	public static String FIELD_GET = "field-get";
	public static String FIELD_SET = "field-set";
	public static String STATICINITIALIZATION = "staticinitialization";
	public static String PREINITIALIZATION = "preinitialization";
	public static String INITIALIZATION = "initialization";
	public static String EXCEPTION_HANDLER = "exception-handler";
	public static String SYNCHRONIZATION_LOCK = "lock";
	public static String SYNCHRONIZATION_UNLOCK = "unlock";
	public static String ADVICE_EXECUTION = "adviceexecution";

	/**
	 * These names are the ones that will be returned by thisJoinPoint.getKind() Those need to be documented somewhere
	 */
	public static final Kind MethodCall = new Kind(METHOD_CALL, 1, true);
	public static final Kind ConstructorCall = new Kind(CONSTRUCTOR_CALL, 2, true);
	public static final Kind MethodExecution = new Kind(METHOD_EXECUTION, 3, false);
	public static final Kind ConstructorExecution = new Kind(CONSTRUCTOR_EXECUTION, 4, false);
	public static final Kind FieldGet = new Kind(FIELD_GET, 5, true);
	public static final Kind FieldSet = new Kind(FIELD_SET, 6, true);
	public static final Kind StaticInitialization = new Kind(STATICINITIALIZATION, 7, false);
	public static final Kind PreInitialization = new Kind(PREINITIALIZATION, 8, false);
	public static final Kind AdviceExecution = new Kind(ADVICE_EXECUTION, 9, false);
	public static final Kind Initialization = new Kind(INITIALIZATION, 10, false);
	public static final Kind ExceptionHandler = new Kind(EXCEPTION_HANDLER, 11, true);
	public static final Kind SynchronizationLock = new Kind(SYNCHRONIZATION_LOCK, 12, true);
	public static final Kind SynchronizationUnlock = new Kind(SYNCHRONIZATION_UNLOCK, 13, true);

	// Bits here are 1<<(Kind.getKey()) - and unfortunately keys didn't start at zero so bits here start at 2
	public static final int MethodCallBit = 0x002;
	public static final int ConstructorCallBit = 0x004;
	public static final int MethodExecutionBit = 0x008;
	public static final int ConstructorExecutionBit = 0x010;
	public static final int FieldGetBit = 0x020;
	public static final int FieldSetBit = 0x040;
	public static final int StaticInitializationBit = 0x080;
	public static final int PreInitializationBit = 0x100;
	public static final int AdviceExecutionBit = 0x200;
	public static final int InitializationBit = 0x400;
	public static final int ExceptionHandlerBit = 0x800;
	public static final int SynchronizationLockBit = 0x1000;
	public static final int SynchronizationUnlockBit = 0x2000;

	public static final int MAX_SHADOW_KIND = 13;
	public static final Kind[] SHADOW_KINDS = new Kind[] { MethodCall, ConstructorCall, MethodExecution, ConstructorExecution,
			FieldGet, FieldSet, StaticInitialization, PreInitialization, AdviceExecution, Initialization, ExceptionHandler,
			SynchronizationLock, SynchronizationUnlock };

	public static final int ALL_SHADOW_KINDS_BITS;
	public static final int NO_SHADOW_KINDS_BITS;

	static {
		ALL_SHADOW_KINDS_BITS = 0x3ffe;
		NO_SHADOW_KINDS_BITS = 0x0000;
	}

	/**
	 * Return count of how many bits set in the supplied parameter.
	 */
	public static int howMany(int i) {
		int count = 0;
		for (int j = 0; j < SHADOW_KINDS.length; j++) {
			if ((i & SHADOW_KINDS[j].bit) != 0) {
				count++;
			}
		}
		return count;
	}

	/**
	 * A type-safe enum representing the kind of shadows
	 */
	public static final class Kind extends TypeSafeEnum {
		// private boolean argsOnStack; //XXX unused

		public int bit;

		public Kind(String name, int key, boolean argsOnStack) {
			super(name, key);
			bit = 1 << key;
			// this.argsOnStack = argsOnStack;
		}

		public String toLegalJavaIdentifier() {
			return getName().replace('-', '_');
		}

		public boolean argsOnStack() {
			return !isTargetSameAsThis();
		}

		// false for handlers
		public boolean allowsExtraction() {
			return true;
		}

		public boolean isSet(int i) {
			return (i & bit) != 0;
		}

		// XXX revisit along with removal of priorities
		public boolean hasHighPriorityExceptions() {
			return !isTargetSameAsThis();
		}

		private final static int hasReturnValueFlag = MethodCallBit | ConstructorCallBit | MethodExecutionBit | FieldGetBit
				| AdviceExecutionBit;

		/**
		 * These shadow kinds have return values that can be bound in after returning(Dooberry doo) advice.
		 * 
		 * @return
		 */
		public boolean hasReturnValue() {
			return (bit & hasReturnValueFlag) != 0;
		}

		private final static int isEnclosingKindFlag = MethodExecutionBit | ConstructorExecutionBit | AdviceExecutionBit
				| StaticInitializationBit | InitializationBit;

		/**
		 * These are all the shadows that contains other shadows within them and are often directly associated with methods.
		 */
		public boolean isEnclosingKind() {
			return (bit & isEnclosingKindFlag) != 0;
		}

		private final static int isTargetSameAsThisFlag = MethodExecutionBit | ConstructorExecutionBit | StaticInitializationBit
				| PreInitializationBit | AdviceExecutionBit | InitializationBit;

		public boolean isTargetSameAsThis() {
			return (bit & isTargetSameAsThisFlag) != 0;
		}

		private final static int neverHasTargetFlag = ConstructorCallBit | ExceptionHandlerBit | PreInitializationBit
				| StaticInitializationBit | SynchronizationLockBit | SynchronizationUnlockBit;

		public boolean neverHasTarget() {
			return (bit & neverHasTargetFlag) != 0;
		}

		private final static int neverHasThisFlag = PreInitializationBit | StaticInitializationBit;

		public boolean neverHasThis() {
			return (bit & neverHasThisFlag) != 0;
		}

		public String getSimpleName() {
			int dash = getName().lastIndexOf('-');
			if (dash == -1) {
				return getName();
			} else {
				return getName().substring(dash + 1);
			}
		}

		public static Kind read(DataInputStream s) throws IOException {
			int key = s.readByte();
			switch (key) {
			case 1:
				return MethodCall;
			case 2:
				return ConstructorCall;
			case 3:
				return MethodExecution;
			case 4:
				return ConstructorExecution;
			case 5:
				return FieldGet;
			case 6:
				return FieldSet;
			case 7:
				return StaticInitialization;
			case 8:
				return PreInitialization;
			case 9:
				return AdviceExecution;
			case 10:
				return Initialization;
			case 11:
				return ExceptionHandler;
			case 12:
				return SynchronizationLock;
			case 13:
				return SynchronizationUnlock;
			}
			throw new BCException("unknown kind: " + key);
		}
	}

	/**
	 * Only does the check if the munger requires it (@AJ aspects don't)
	 * 
	 * @param munger
	 * @return
	 */
	protected boolean checkMunger(ShadowMunger munger) {
		if (munger.mustCheckExceptions()) {
			for (Iterator<ResolvedType> i = munger.getThrownExceptions().iterator(); i.hasNext();) {
				if (!checkCanThrow(munger, i.next())) {
					return false;
				}
			}
		}
		return true;
	}

	protected boolean checkCanThrow(ShadowMunger munger, ResolvedType resolvedTypeX) {
		if (getKind() == ExceptionHandler) {
			// XXX much too lenient rules here, need to walk up exception handlers
			return true;
		}

		if (!isDeclaredException(resolvedTypeX, getSignature())) {
			getIWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.CANT_THROW_CHECKED, resolvedTypeX, this), // from
					// advice
					// in
					// \
					// '"
					// +
					// munger
					// .
					// +
					// "\'"
					// ,
					getSourceLocation(), munger.getSourceLocation());
		}

		return true;
	}

	private boolean isDeclaredException(ResolvedType resolvedTypeX, Member member) {
		ResolvedType[] excs = getIWorld().resolve(member.getExceptions(getIWorld()));
		for (int i = 0, len = excs.length; i < len; i++) {
			if (excs[i].isAssignableFrom(resolvedTypeX)) {
				return true;
			}
		}
		return false;
	}

	public void addMunger(ShadowMunger munger) {
		if (checkMunger(munger)) {
			if (mungers == Collections.EMPTY_LIST) {
				mungers = new ArrayList<ShadowMunger>();
			}
			this.mungers.add(munger);
		}
	}

	public final void implement() {
		sortMungers();
		if (mungers == null) {
			return;
		}
		prepareForMungers();
		implementMungers();
	}

	private void sortMungers() {

		List sorted = PartialOrder.sort(mungers);

		// Bunch of code to work out whether to report xlints for advice that isn't ordered at this Joinpoint
		possiblyReportUnorderedAdvice(sorted);

		if (sorted == null) {
			// this means that we have circular dependencies
			for (ShadowMunger m : mungers) {
				getIWorld().getMessageHandler().handleMessage(
						MessageUtil.error(WeaverMessages.format(WeaverMessages.CIRCULAR_DEPENDENCY, this), m.getSourceLocation()));
			}
		}
		mungers = sorted;
	}

	// not quite optimal... but the xlint is ignore by default
	private void possiblyReportUnorderedAdvice(List sorted) {
		if (sorted != null && getIWorld().getLint().unorderedAdviceAtShadow.isEnabled() && mungers.size() > 1) {

			// Stores a set of strings of the form 'aspect1:aspect2' which indicates there is no
			// precedence specified between the two aspects at this shadow.
			Set<String> clashingAspects = new HashSet<String>();
			int max = mungers.size();

			// Compare every pair of advice mungers
			for (int i = max - 1; i >= 0; i--) {
				for (int j = 0; j < i; j++) {
					Object a = mungers.get(i);
					Object b = mungers.get(j);

					// Make sure they are the right type
					if (a instanceof Advice && b instanceof Advice) {
						Advice adviceA = (Advice) a;
						Advice adviceB = (Advice) b;
						if (!adviceA.concreteAspect.equals(adviceB.concreteAspect)) {
							AdviceKind adviceKindA = adviceA.getKind();
							AdviceKind adviceKindB = adviceB.getKind();

							// make sure they are the nice ones (<6) and not any synthetic advice ones we
							// create to support other features of the language.
							if (adviceKindA.getKey() < (byte) 6 && adviceKindB.getKey() < (byte) 6
									&& adviceKindA.getPrecedence() == adviceKindB.getPrecedence()) {

								// Ask the world if it knows about precedence between these
								Integer order = getIWorld().getPrecedenceIfAny(adviceA.concreteAspect, adviceB.concreteAspect);

								if (order != null && order.equals(new Integer(0))) {
									String key = adviceA.getDeclaringAspect() + ":" + adviceB.getDeclaringAspect();
									String possibleExistingKey = adviceB.getDeclaringAspect() + ":" + adviceA.getDeclaringAspect();
									if (!clashingAspects.contains(possibleExistingKey)) {
										clashingAspects.add(key);
									}
								}
							}
						}
					}
				}
			}
			for (Iterator<String> iter = clashingAspects.iterator(); iter.hasNext();) {
				String element = iter.next();
				String aspect1 = element.substring(0, element.indexOf(":"));
				String aspect2 = element.substring(element.indexOf(":") + 1);
				getIWorld().getLint().unorderedAdviceAtShadow.signal(new String[] { this.toString(), aspect1, aspect2 },
						this.getSourceLocation(), null);
			}
		}
	}

	/**
	 * Prepare the shadow for implementation. After this is done, the shadow should be in such a position that each munger simply
	 * needs to be implemented.
	 */
	protected void prepareForMungers() {
		throw new RuntimeException("Generic shadows cannot be prepared");
	}

	/** Actually implement the (non-empty) mungers associated with this shadow */
	private void implementMungers() {
		World world = getIWorld(); 
		for (ShadowMunger munger : mungers) {
			if (munger.implementOn(this)) {
				world.reportMatch(munger, this);
			}
		}
	}

	public abstract ISourceLocation getSourceLocation();

	// ---- utility

	public String toString() {
		return getKind() + "(" + getSignature() + ")"; // + getSourceLines();
	}

	public String toResolvedString(World world) {
		StringBuffer sb = new StringBuffer();
		sb.append(getKind());
		sb.append("(");
		Member m = getSignature();
		if (m == null) {
			sb.append("<<missing signature>>");
		} else {
			ResolvedMember rm = world.resolve(m);
			if (rm == null) {
				sb.append("<<unresolvableMember:").append(m).append(">>");
			} else {
				String genString = rm.toGenericString();
				if (genString == null) {
					sb.append("<<unableToGetGenericStringFor:").append(rm).append(">>");
				} else {
					sb.append(genString);
				}

			}
		}
		sb.append(")");
		return sb.toString();
		// was: return getKind() + "(" + world.resolve(getSignature()).toGenericString() + ")";
	}

	/**
	 * Convert a bit array for the shadow kinds into a set of them... should only be used for testing - mainline code should do bit
	 * manipulation!
	 */
	public static Set<Kind> toSet(int i) {
		Set<Kind> results = new HashSet<Kind>();
		for (int j = 0; j < Shadow.SHADOW_KINDS.length; j++) {
			Kind k = Shadow.SHADOW_KINDS[j];
			if (k.isSet(i)) {
				results.add(k);
			}
		}
		return results;
	}

}
