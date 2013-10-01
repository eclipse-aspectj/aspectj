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

package org.aspectj.weaver.bcel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionCP;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionLV;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InstructionSelect;
import org.aspectj.apache.bcel.generic.InstructionTargeter;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.LineNumberTag;
import org.aspectj.apache.bcel.generic.LocalVariableTag;
import org.aspectj.apache.bcel.generic.MULTIANEWARRAY;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.generic.Tag;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.bridge.context.ContextToken;
import org.aspectj.util.PartialOrder;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.IClassWeaver;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberKind;
import org.aspectj.weaver.MissingResolvedTypeWithKnownSignature;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.NewMethodTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.UnresolvedTypeVariableReferenceType;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.model.AsmRelationshipProvider;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

class BcelClassWeaver implements IClassWeaver {

	private static Trace trace = TraceFactory.getTraceFactory().getTrace(BcelClassWeaver.class);

	public static boolean weave(BcelWorld world, LazyClassGen clazz, List<ShadowMunger> shadowMungers,
			List<ConcreteTypeMunger> typeMungers, List<ConcreteTypeMunger> lateTypeMungers, boolean inReweavableMode) {
		BcelClassWeaver classWeaver = new BcelClassWeaver(world, clazz, shadowMungers, typeMungers, lateTypeMungers);
		classWeaver.setReweavableMode(inReweavableMode);
		boolean b = classWeaver.weave();
		return b;
	}

	// --------------------------------------------

	private final LazyClassGen clazz;
	private final List<ShadowMunger> shadowMungers;
	private final List<ConcreteTypeMunger> typeMungers;
	private final List<ConcreteTypeMunger> lateTypeMungers;

	private List<ShadowMunger>[] indexedShadowMungers;
	private boolean canMatchBodyShadows = false;

	private final BcelObjectType ty; // alias of clazz.getType()
	private final BcelWorld world; // alias of ty.getWorld()
	private final ConstantPool cpg; // alias of clazz.getConstantPoolGen()
	private final InstructionFactory fact; // alias of clazz.getFactory();

	private final List<LazyMethodGen> addedLazyMethodGens = new ArrayList<LazyMethodGen>();
	private final Set<ResolvedMember> addedDispatchTargets = new HashSet<ResolvedMember>();

	private boolean inReweavableMode = false;

	private List<IfaceInitList> addedSuperInitializersAsList = null;
	private final Map<ResolvedType, IfaceInitList> addedSuperInitializers = new HashMap<ResolvedType, IfaceInitList>();
	private final List<ConcreteTypeMunger> addedThisInitializers = new ArrayList<ConcreteTypeMunger>();
	private final List<ConcreteTypeMunger> addedClassInitializers = new ArrayList<ConcreteTypeMunger>();

	private final Map<ResolvedMember, ResolvedMember> mapToAnnotationHolder = new HashMap<ResolvedMember, ResolvedMember>();

	// private BcelShadow clinitShadow = null;

	/**
	 * This holds the initialization and pre-initialization shadows for this class that were actually matched by mungers (if no
	 * match, then we don't even create the shadows really).
	 */
	private final List<BcelShadow> initializationShadows = new ArrayList<BcelShadow>();

	private BcelClassWeaver(BcelWorld world, LazyClassGen clazz, List<ShadowMunger> shadowMungers,
			List<ConcreteTypeMunger> typeMungers, List<ConcreteTypeMunger> lateTypeMungers) {
		super();
		this.world = world;
		this.clazz = clazz;
		this.shadowMungers = shadowMungers;
		this.typeMungers = typeMungers;
		this.lateTypeMungers = lateTypeMungers;
		this.ty = clazz.getBcelObjectType();
		this.cpg = clazz.getConstantPool();
		this.fact = clazz.getFactory();

		indexShadowMungers();

		initializeSuperInitializerMap(ty.getResolvedTypeX());
		if (!checkedXsetForLowLevelContextCapturing) {
			Properties p = world.getExtraConfiguration();
			if (p != null) {
				String s = p.getProperty(World.xsetCAPTURE_ALL_CONTEXT, "false");
				captureLowLevelContext = s.equalsIgnoreCase("true");
				if (captureLowLevelContext) {
					world.getMessageHandler().handleMessage(
							MessageUtil.info("[" + World.xsetCAPTURE_ALL_CONTEXT
									+ "=true] Enabling collection of low level context for debug/crash messages"));
				}
			}
			checkedXsetForLowLevelContextCapturing = true;
		}
	}

	private boolean canMatch(Shadow.Kind kind) {
		return indexedShadowMungers[kind.getKey()] != null;
	}

	// private void fastMatchShadowMungers(List shadowMungers, ArrayList
	// mungers, Kind kind) {
	// FastMatchInfo info = new FastMatchInfo(clazz.getType(), kind);
	// for (Iterator i = shadowMungers.iterator(); i.hasNext();) {
	// ShadowMunger munger = (ShadowMunger) i.next();
	// FuzzyBoolean fb = munger.getPointcut().fastMatch(info);
	// WeaverMetrics.recordFastMatchResult(fb);// Could pass:
	// munger.getPointcut().toString()
	// if (fb.maybeTrue()) mungers.add(munger);
	// }
	// }

	private void initializeSuperInitializerMap(ResolvedType child) {
		ResolvedType[] superInterfaces = child.getDeclaredInterfaces();
		for (int i = 0, len = superInterfaces.length; i < len; i++) {
			if (ty.getResolvedTypeX().isTopmostImplementor(superInterfaces[i])) {
				if (addSuperInitializer(superInterfaces[i])) {
					initializeSuperInitializerMap(superInterfaces[i]);
				}
			}
		}
	}

	/**
	 * Process the shadow mungers into array 'buckets', each bucket represents a shadow kind and contains a list of shadowmungers
	 * that could potentially apply at that shadow kind.
	 */
	private void indexShadowMungers() {
		// beware the annoying property that SHADOW_KINDS[i].getKey == (i+1) !
		indexedShadowMungers = new List[Shadow.MAX_SHADOW_KIND + 1];
		for (ShadowMunger shadowMunger : shadowMungers) {
			int couldMatchKinds = shadowMunger.getPointcut().couldMatchKinds();
			for (Shadow.Kind kind : Shadow.SHADOW_KINDS) {
				if (kind.isSet(couldMatchKinds)) {
					byte k = kind.getKey();
					if (indexedShadowMungers[k] == null) {
						indexedShadowMungers[k] = new ArrayList<ShadowMunger>();
						if (!kind.isEnclosingKind()) {
							canMatchBodyShadows = true;
						}
					}
					indexedShadowMungers[k].add(shadowMunger);
				}
			}
		}
	}

	private boolean addSuperInitializer(ResolvedType onType) {
		if (onType.isRawType() || onType.isParameterizedType()) {
			onType = onType.getGenericType();
		}
		IfaceInitList l = addedSuperInitializers.get(onType);
		if (l != null) {
			return false;
		}
		l = new IfaceInitList(onType);
		addedSuperInitializers.put(onType, l);
		return true;
	}

	public void addInitializer(ConcreteTypeMunger cm) {
		NewFieldTypeMunger m = (NewFieldTypeMunger) cm.getMunger();
		ResolvedType onType = m.getSignature().getDeclaringType().resolve(world);
		if (onType.isRawType()) {
			onType = onType.getGenericType();
		}

		if (Modifier.isStatic(m.getSignature().getModifiers())) {
			addedClassInitializers.add(cm);
		} else {
			if (onType == ty.getResolvedTypeX()) {
				addedThisInitializers.add(cm);
			} else {
				IfaceInitList l = addedSuperInitializers.get(onType);
				l.list.add(cm);
			}
		}
	}

	private static class IfaceInitList implements PartialOrder.PartialComparable {
		final ResolvedType onType;
		List<ConcreteTypeMunger> list = new ArrayList<ConcreteTypeMunger>();

		IfaceInitList(ResolvedType onType) {
			this.onType = onType;
		}

		public int compareTo(Object other) {
			IfaceInitList o = (IfaceInitList) other;
			if (onType.isAssignableFrom(o.onType)) {
				return +1;
			} else if (o.onType.isAssignableFrom(onType)) {
				return -1;
			} else {
				return 0;
			}
		}

		public int fallbackCompareTo(Object other) {
			return 0;
		}
	}

	// XXX this is being called, but the result doesn't seem to be being used
	public boolean addDispatchTarget(ResolvedMember m) {
		return addedDispatchTargets.add(m);
	}

	public void addLazyMethodGen(LazyMethodGen gen) {
		addedLazyMethodGens.add(gen);
	}

	public void addOrReplaceLazyMethodGen(LazyMethodGen mg) {
		if (alreadyDefined(clazz, mg)) {
			return;
		}

		for (Iterator<LazyMethodGen> i = addedLazyMethodGens.iterator(); i.hasNext();) {
			LazyMethodGen existing = i.next();
			if (signaturesMatch(mg, existing)) {
				if (existing.definingType == null) {
					// this means existing was introduced on the class itself
					return;
				} else if (mg.definingType.isAssignableFrom(existing.definingType)) {
					// existing is mg's subtype and dominates mg
					return;
				} else if (existing.definingType.isAssignableFrom(mg.definingType)) {
					// mg is existing's subtype and dominates existing
					i.remove();
					addedLazyMethodGens.add(mg);
					return;
				} else {
					throw new BCException("conflict between: " + mg + " and " + existing);
				}
			}
		}
		addedLazyMethodGens.add(mg);
	}

	private boolean alreadyDefined(LazyClassGen clazz, LazyMethodGen mg) {
		for (Iterator<LazyMethodGen> i = clazz.getMethodGens().iterator(); i.hasNext();) {
			LazyMethodGen existing = i.next();
			if (signaturesMatch(mg, existing)) {
				if (!mg.isAbstract() && existing.isAbstract()) {
					i.remove();
					return false;
				}
				return true;
			}
		}
		return false;
	}

	private boolean signaturesMatch(LazyMethodGen mg, LazyMethodGen existing) {
		return mg.getName().equals(existing.getName()) && mg.getSignature().equals(existing.getSignature());
	}

	protected static LazyMethodGen makeBridgeMethod(LazyClassGen gen, ResolvedMember member) {

		// remove abstract modifier
		int mods = member.getModifiers();
		if (Modifier.isAbstract(mods)) {
			mods = mods - Modifier.ABSTRACT;
		}

		LazyMethodGen ret = new LazyMethodGen(mods, BcelWorld.makeBcelType(member.getReturnType()), member.getName(),
				BcelWorld.makeBcelTypes(member.getParameterTypes()), UnresolvedType.getNames(member.getExceptions()), gen);

		// 43972 : Static crosscutting makes interfaces unusable for javac
		// ret.makeSynthetic();
		return ret;
	}

	/**
	 * Create a single bridge method called 'theBridgeMethod' that bridges to 'whatToBridgeTo'
	 */
	private static void createBridgeMethod(BcelWorld world, LazyMethodGen whatToBridgeToMethodGen, LazyClassGen clazz, ResolvedMember theBridgeMethod) {
		InstructionList body;
		InstructionFactory fact;
		int pos = 0;

		ResolvedMember whatToBridgeTo = whatToBridgeToMethodGen.getMemberView();

		if (whatToBridgeTo == null) {
			whatToBridgeTo = new ResolvedMemberImpl(Member.METHOD, whatToBridgeToMethodGen.getEnclosingClass().getType(),
					whatToBridgeToMethodGen.getAccessFlags(), whatToBridgeToMethodGen.getName(),
					whatToBridgeToMethodGen.getSignature());
		}
		// The bridge method in this type will have the same signature as the one in the supertype
		LazyMethodGen bridgeMethod = makeBridgeMethod(clazz, theBridgeMethod);
		int newflags = bridgeMethod.getAccessFlags() | 0x00000040;// BRIDGE = 0x00000040

		if ((newflags & 0x00000100) != 0) {
			newflags = newflags - 0x100;// NATIVE = 0x00000100 - need to clear it
		}

		bridgeMethod.setAccessFlags(newflags);
		Type returnType = BcelWorld.makeBcelType(theBridgeMethod.getReturnType());
		Type[] paramTypes = BcelWorld.makeBcelTypes(theBridgeMethod.getParameterTypes());
		Type[] newParamTypes = whatToBridgeToMethodGen.getArgumentTypes();
		body = bridgeMethod.getBody();
		fact = clazz.getFactory();

		if (!whatToBridgeToMethodGen.isStatic()) {
			body.append(InstructionFactory.createThis());
			pos++;
		}
		for (int i = 0, len = paramTypes.length; i < len; i++) {
			Type paramType = paramTypes[i];
			body.append(InstructionFactory.createLoad(paramType, pos));
			if (!newParamTypes[i].equals(paramTypes[i])) {
				if (world.forDEBUG_bridgingCode) {
					System.err.println("Bridging: Cast " + newParamTypes[i] + " from " + paramTypes[i]);
				}
				body.append(fact.createCast(paramTypes[i], newParamTypes[i]));
			}
			pos += paramType.getSize();
		}

		body.append(Utility.createInvoke(fact, world, whatToBridgeTo));
		body.append(InstructionFactory.createReturn(returnType));
		clazz.addMethodGen(bridgeMethod);
	}

	/**
	 * Weave a class and indicate through the return value whether the class was modified.
	 * 
	 * @return true if the class was modified
	 */
	public boolean weave() {
		if (clazz.isWoven() && !clazz.isReweavable()) {
			if (world.getLint().nonReweavableTypeEncountered.isEnabled()) {
				world.getLint().nonReweavableTypeEncountered.signal(clazz.getType().getName(), ty.getSourceLocation());
			}
			// Integer uniqueID = new Integer(rm.hashCode() * deca.hashCode());
			// if (!reportedProblems.contains(uniqueID)) {
			// reportedProblems.add(uniqueID);
			// world.getLint().elementAlreadyAnnotated.signal(new String[] { rm.toString(),
			// world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.ALREADY_WOVEN, clazz.getType().getName()),
			// ty.getSourceLocation(), null);
			return false;
		}

		Set<String> aspectsAffectingType = null;
		if (inReweavableMode || clazz.getType().isAspect()) {
			aspectsAffectingType = new HashSet<String>();
		}

		boolean isChanged = false;

		// we want to "touch" all aspects
		if (clazz.getType().isAspect()) {
			isChanged = true;
		}

		WeaverStateInfo typeWeaverState = (world.isOverWeaving() ? getLazyClassGen().getType().getWeaverState() : null);
		// start by munging all typeMungers
		for (ConcreteTypeMunger o : typeMungers) {
			if (!(o instanceof BcelTypeMunger)) {
				// ???System.err.println("surprising: " + o);
				continue;
			}
			BcelTypeMunger munger = (BcelTypeMunger) o;

			if (typeWeaverState != null && typeWeaverState.isAspectAlreadyApplied(munger.getAspectType())) {
				continue;
			}
			boolean typeMungerAffectedType = munger.munge(this);
			if (typeMungerAffectedType) {
				isChanged = true;
				if (inReweavableMode || clazz.getType().isAspect()) {
					aspectsAffectingType.add(munger.getAspectType().getSignature());
				}
			}
		}

		// Weave special half type/half shadow mungers...
		isChanged = weaveDeclareAtMethodCtor(clazz) || isChanged;
		isChanged = weaveDeclareAtField(clazz) || isChanged;

		// XXX do major sort of stuff
		// sort according to: Major: type hierarchy
		// within each list: dominates
		// don't forget to sort addedThisInitialiers according to dominates
		addedSuperInitializersAsList = new ArrayList<IfaceInitList>(addedSuperInitializers.values());
		addedSuperInitializersAsList = PartialOrder.sort(addedSuperInitializersAsList);
		if (addedSuperInitializersAsList == null) {
			throw new BCException("circularity in inter-types");
		}

		// this will create a static initializer if there isn't one
		// this is in just as bad taste as NOPs
		LazyMethodGen staticInit = clazz.getStaticInitializer();
		staticInit.getBody().insert(genInitInstructions(addedClassInitializers, true));

		// now go through each method, and match against each method. This
		// sets up each method's {@link LazyMethodGen#matchedShadows} field,
		// and it also possibly adds to {@link #initializationShadows}.
		List<LazyMethodGen> methodGens = new ArrayList<LazyMethodGen>(clazz.getMethodGens());
		for (LazyMethodGen member : methodGens) {
			if (!member.hasBody()) {
				continue;
			}
			if (world.isJoinpointSynchronizationEnabled() && world.areSynchronizationPointcutsInUse()
					&& member.getMethod().isSynchronized()) {
				transformSynchronizedMethod(member);
			}
			boolean shadowMungerMatched = match(member);
			if (shadowMungerMatched) {
				// For matching mungers, add their declaring aspects to the list
				// that affected this type
				if (inReweavableMode || clazz.getType().isAspect()) {
					aspectsAffectingType.addAll(findAspectsForMungers(member));
				}
				isChanged = true;
			}
		}

		// now we weave all but the initialization shadows
		for (LazyMethodGen methodGen : methodGens) {
			if (!methodGen.hasBody()) {
				continue;
			}
			implement(methodGen);
		}

		// if we matched any initialization shadows, we inline and weave
		if (!initializationShadows.isEmpty()) {
			// Repeat next step until nothing left to inline...cant go on
			// infinetly as compiler will have detected and reported
			// "Recursive constructor invocation"
			List<LazyMethodGen> recursiveCtors = new ArrayList<LazyMethodGen>();
			while (inlineSelfConstructors(methodGens, recursiveCtors)) {
			}
			positionAndImplement(initializationShadows);
		}

		// now proceed with late type mungers
		if (lateTypeMungers != null) {
			for (Iterator<ConcreteTypeMunger> i = lateTypeMungers.iterator(); i.hasNext();) {
				BcelTypeMunger munger = (BcelTypeMunger) i.next();
				if (munger.matches(clazz.getType())) {
					boolean typeMungerAffectedType = munger.munge(this);
					if (typeMungerAffectedType) {
						isChanged = true;
						if (inReweavableMode || clazz.getType().isAspect()) {
							aspectsAffectingType.add(munger.getAspectType().getSignature());
						}
					}
				}
			}
		}

		// FIXME AV - see #75442, for now this is not enough to fix the bug,
		// comment that out until we really fix it
		// // flush to save some memory
		// PerObjectInterfaceTypeMunger.unregisterFromAsAdvisedBy(clazz.getType()
		// );

		// finally, if we changed, we add in the introduced methods.
		if (isChanged) {
			clazz.getOrCreateWeaverStateInfo(inReweavableMode);
			weaveInAddedMethods();
		}

		if (inReweavableMode) {
			WeaverStateInfo wsi = clazz.getOrCreateWeaverStateInfo(true);
			wsi.addAspectsAffectingType(aspectsAffectingType);
			wsi.setUnwovenClassFileData(ty.getJavaClass().getBytes());
			wsi.setReweavable(true);
		} else {
			clazz.getOrCreateWeaverStateInfo(false).setReweavable(false);
		}

		// tidyup, reduce ongoing memory usage of BcelMethods that hang around
		for (LazyMethodGen mg : methodGens) {
			BcelMethod method = mg.getMemberView();
			if (method != null) {
				method.wipeJoinpointSignatures();
			}
		}

		return isChanged;
	}

	// **************************** start of bridge method creation code
	// *****************

	// FIXASC tidy this lot up !!
	// FIXASC refactor into ResolvedType or even ResolvedMember?
	/**
	 * Check if a particular method is overriding another - refactored into this helper so it can be used from multiple places.
	 * @return method that is overriding if it 
	 */
	private static ResolvedMember isOverriding(ResolvedType typeToCheck, ResolvedMember methodThatMightBeGettingOverridden,
			String mname, String mrettype, int mmods, boolean inSamePackage, UnresolvedType[] methodParamsArray) {
		// Check if we can be an override...
		if (Modifier.isStatic(methodThatMightBeGettingOverridden.getModifiers())) {
			// we can't be overriding a static method
			return null;
		}
		if (Modifier.isPrivate(methodThatMightBeGettingOverridden.getModifiers())) {
			// we can't be overriding a private method
			return null;
		}
		if (!methodThatMightBeGettingOverridden.getName().equals(mname)) {
			// names do not match (this will also skip <init> and <clinit>)
			return null;
		}
		if (methodThatMightBeGettingOverridden.getParameterTypes().length != methodParamsArray.length) {
			// not the same number of parameters
			return null;
		}
		if (!isVisibilityOverride(mmods, methodThatMightBeGettingOverridden, inSamePackage)) {
			// not override from visibility point of view
			return null;
		}

		if (typeToCheck.getWorld().forDEBUG_bridgingCode) {
			System.err.println("  Bridging:seriously considering this might be getting overridden '"
					+ methodThatMightBeGettingOverridden + "'");
		}

		World w = typeToCheck.getWorld();

		// Look at erasures of parameters (List<String> erased is List)
		boolean sameParams = true;
		for (int p = 0, max = methodThatMightBeGettingOverridden.getParameterTypes().length; p < max; p++) {

			UnresolvedType mtmbgoParameter = methodThatMightBeGettingOverridden.getParameterTypes()[p];
			UnresolvedType ptype = methodParamsArray[p];

			if (mtmbgoParameter.isTypeVariableReference()) {
				if (!mtmbgoParameter.resolve(w).isAssignableFrom(ptype.resolve(w))) {
					sameParams = false;
				}
			} else {
				// old condition:
				boolean b = !methodThatMightBeGettingOverridden.getParameterTypes()[p].getErasureSignature().equals(
						methodParamsArray[p].getErasureSignature());

				UnresolvedType parameterType = methodThatMightBeGettingOverridden.getParameterTypes()[p];

				// Collapse to first bound (isn't that the same as erasure!
				if (parameterType instanceof UnresolvedTypeVariableReferenceType) {
					parameterType = ((UnresolvedTypeVariableReferenceType) parameterType).getTypeVariable().getFirstBound();
				}

				if (b) { // !parameterType.resolve(w).equals(parameterType2.resolve(w))) {
					sameParams = false;
				}
			}
			//
			// if (!ut.getErasureSignature().equals(ut2.getErasureSignature()))
			// sameParams = false;
		}

		// If the 'typeToCheck' represents a parameterized type then the method
		// will be the parameterized form of the
		// generic method in the generic type. So if the method was 'void
		// m(List<T> lt, T t)' and the parameterized type here
		// is I<String> then the method we are looking at will be 'void
		// m(List<String> lt, String t)' which when erased
		// is 'void m(List lt,String t)' - so if the parameters *do* match then
		// there is a generic method we are
		// overriding

		// FIXASC Why bother with the return type? If it is incompatible then the code has other problems!
		if (sameParams) {
			if (typeToCheck.isParameterizedType()) {
				return methodThatMightBeGettingOverridden.getBackingGenericMember();
			} else if (!methodThatMightBeGettingOverridden.getReturnType().getErasureSignature().equals(mrettype)) {
				// addressing the wierd situation from bug 147801
				// just check whether these things are in the right relationship
				// for covariance...
				ResolvedType superReturn = typeToCheck.getWorld().resolve(
						UnresolvedType.forSignature(methodThatMightBeGettingOverridden.getReturnType().getErasureSignature()));
				ResolvedType subReturn = typeToCheck.getWorld().resolve(UnresolvedType.forSignature(mrettype));
				if (superReturn.isAssignableFrom(subReturn)) {
					return methodThatMightBeGettingOverridden;
				}
				// } else if (typeToCheck.isParameterizedType()) {
				// return methodThatMightBeGettingOverridden.getBackingGenericMember();
			} else {
				return methodThatMightBeGettingOverridden;
			}
		}
		return null;
	}

	/**
	 * Looks at the visibility modifiers between two methods, and knows whether they are from classes in the same package, and
	 * decides whether one overrides the other.
	 * 
	 * @return true if there is an overrides rather than a 'hides' relationship
	 */
	static boolean isVisibilityOverride(int methodMods, ResolvedMember inheritedMethod, boolean inSamePackage) {
		int inheritedModifiers = inheritedMethod.getModifiers();
		if (Modifier.isStatic(inheritedModifiers)) {
			return false;
		}
		if (methodMods == inheritedModifiers) {
			return true;
		}

		if (Modifier.isPrivate(inheritedModifiers)) {
			return false;
		}

		boolean isPackageVisible = !Modifier.isPrivate(inheritedModifiers) && !Modifier.isProtected(inheritedModifiers)
				&& !Modifier.isPublic(inheritedModifiers);
		if (isPackageVisible && !inSamePackage) {
			return false;
		}

		return true;
	}

	/**
	 * This method recurses up a specified type looking for a method that overrides the one passed in.
	 * 
	 * @return the method being overridden or null if none is found
	 */
	public static void checkForOverride(ResolvedType typeToCheck, String mname, String mparams, String mrettype,
			int mmods, String mpkg, UnresolvedType[] methodParamsArray, List<ResolvedMember> overriddenMethodsCollector) {

		if (typeToCheck == null) {
			return;
		}
		if (typeToCheck instanceof MissingResolvedTypeWithKnownSignature) {
			return; // we just can't tell !
		}
		

		if (typeToCheck.getWorld().forDEBUG_bridgingCode) {
			System.err.println("  Bridging:checking for override of " + mname + " in " + typeToCheck);
		}

		String packageName = typeToCheck.getPackageName();
		if (packageName == null) {
			packageName = "";
		}
		// used when looking at visibility rules
		boolean inSamePackage = packageName.equals(mpkg); 

		ResolvedMember[] methods = typeToCheck.getDeclaredMethods();
		for (int ii = 0; ii < methods.length; ii++) {
			// the method we are going to check
			ResolvedMember methodThatMightBeGettingOverridden = methods[ii]; 
			ResolvedMember isOverriding = isOverriding(typeToCheck, methodThatMightBeGettingOverridden, mname, mrettype, mmods,
					inSamePackage, methodParamsArray);
			if (isOverriding != null) {
				overriddenMethodsCollector.add(isOverriding);
			}
		}
		// was: List l = typeToCheck.getInterTypeMungers();
		List<ConcreteTypeMunger> l = (typeToCheck.isRawType() ? typeToCheck.getGenericType().getInterTypeMungers() : typeToCheck
				.getInterTypeMungers());
		for (Iterator<ConcreteTypeMunger> iterator = l.iterator(); iterator.hasNext();) {
			ConcreteTypeMunger o = iterator.next();
			// FIXME asc if its not a BcelTypeMunger then its an
			// EclipseTypeMunger ... do I need to worry about that?
			if (o instanceof BcelTypeMunger) {
				BcelTypeMunger element = (BcelTypeMunger) o;
				if (element.getMunger() instanceof NewMethodTypeMunger) {
					if (typeToCheck.getWorld().forDEBUG_bridgingCode) {
						System.err.println("Possible ITD candidate " + element);
					}
					ResolvedMember aMethod = element.getSignature();
					ResolvedMember isOverriding = isOverriding(typeToCheck, aMethod, mname, mrettype, mmods, inSamePackage,
							methodParamsArray);
					if (isOverriding != null) {
						overriddenMethodsCollector.add(isOverriding);
					}
				}
			}
		}

		if (typeToCheck.equals(UnresolvedType.OBJECT)) {
			return;
		}

		ResolvedType superclass = typeToCheck.getSuperclass();
		checkForOverride(superclass, mname, mparams, mrettype, mmods, mpkg, methodParamsArray,overriddenMethodsCollector);
		
		ResolvedType[] interfaces = typeToCheck.getDeclaredInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			ResolvedType anInterface = interfaces[i];
			checkForOverride(anInterface, mname, mparams, mrettype, mmods, mpkg, methodParamsArray,overriddenMethodsCollector);
		}
	}

	/**
	 * We need to determine if any methods in this type require bridge methods - this method should only be called if necessary to
	 * do this calculation, i.e. we are on a 1.5 VM (where covariance/generics exist) and the type hierarchy for the specified class
	 * has changed (via decp/itd).
	 * 
	 * See pr108101
	 */
	public static boolean calculateAnyRequiredBridgeMethods(BcelWorld world, LazyClassGen clazz) {
		world.ensureAdvancedConfigurationProcessed();
		
		if (!world.isInJava5Mode()) {
			return false; // just double check... the caller should have already
		}
		if (clazz.isInterface()) {
			return false; // dont bother if we are an interface
		}
		
		boolean didSomething = false; // set if we build any bridge methods
		// So what methods do we have right now in this class?
		List<LazyMethodGen> methods = clazz.getMethodGens();

		// Keep a set of all methods from this type - it'll help us to check if bridge methods
		// have already been created, we don't want to do it twice!
		Set<String> methodsSet = new HashSet<String>();
		for (int i = 0; i < methods.size(); i++) {
			LazyMethodGen aMethod = methods.get(i);
			StringBuilder sb = new StringBuilder(aMethod.getName());
			sb.append(aMethod.getSignature());
			methodsSet.add(sb.toString()); // e.g. "foo(Ljava/lang/String;)V"
		}

		// Now go through all the methods in this type
		for (int i = 0; i < methods.size(); i++) {
			// This is the local method that we *might* have to bridge to
			LazyMethodGen bridgeToCandidate = methods.get(i);
			if (bridgeToCandidate.isBridgeMethod()) {
				continue; // Doh!
			}
			String name = bridgeToCandidate.getName();
			String psig = bridgeToCandidate.getParameterSignature();
			String rsig = bridgeToCandidate.getReturnType().getSignature();

			// if (bridgeToCandidate.isAbstract()) continue;
			if (bridgeToCandidate.isStatic()) {
				continue; // ignore static methods
			}
			if (name.endsWith("init>")) {
				continue; // Skip constructors and static initializers
			}

			if (world.forDEBUG_bridgingCode) {
				System.err.println("Bridging: Determining if we have to bridge to " + clazz.getName() + "." + name + "" + bridgeToCandidate.getSignature());
			}

			// Let's take a look at the superclass
			ResolvedType theSuperclass = clazz.getSuperClass();
			if (world.forDEBUG_bridgingCode) {
				System.err.println("Bridging: Checking supertype " + theSuperclass);
			}
			String pkgName = clazz.getPackageName();
			UnresolvedType[] bm = BcelWorld.fromBcel(bridgeToCandidate.getArgumentTypes());
			List<ResolvedMember> overriddenMethodsCollector = new ArrayList<ResolvedMember>();
			checkForOverride(theSuperclass, name, psig, rsig, bridgeToCandidate.getAccessFlags(), pkgName, bm, overriddenMethodsCollector);
			if (overriddenMethodsCollector.size() != 0) {
				for (ResolvedMember overriddenMethod: overriddenMethodsCollector) {
					String key = new StringBuilder(overriddenMethod.getName()).append(overriddenMethod.getSignatureErased()).toString(); // pr237419
					boolean alreadyHaveABridgeMethod = methodsSet.contains(key);
					if (!alreadyHaveABridgeMethod) {
						if (world.forDEBUG_bridgingCode) {
							System.err.println("Bridging:bridging to '" + overriddenMethod + "'");
						}
						createBridgeMethod(world, bridgeToCandidate, clazz, overriddenMethod);
						methodsSet.add(key);
						didSomething = true;
					}
				}
			}

			// Check superinterfaces
			String[] interfaces = clazz.getInterfaceNames();
			for (int j = 0; j < interfaces.length; j++) {
				if (world.forDEBUG_bridgingCode) {
					System.err.println("Bridging:checking superinterface " + interfaces[j]);
				}
				ResolvedType interfaceType = world.resolve(interfaces[j]);
				overriddenMethodsCollector.clear();
				checkForOverride(interfaceType, name, psig, rsig, bridgeToCandidate.getAccessFlags(),
						clazz.getPackageName(), bm, overriddenMethodsCollector);
				for (ResolvedMember overriddenMethod: overriddenMethodsCollector) {
					String key = new StringBuffer().append(overriddenMethod.getName()).append(overriddenMethod.getSignatureErased()).toString(); // pr237419
					boolean alreadyHaveABridgeMethod = methodsSet.contains(key);
					if (!alreadyHaveABridgeMethod) {
						createBridgeMethod(world, bridgeToCandidate, clazz, overriddenMethod);
						methodsSet.add(key);
						didSomething = true;
						if (world.forDEBUG_bridgingCode) {
							System.err.println("Bridging:bridging to " + overriddenMethod);
						}
					}
				}
			}
		}

		return didSomething;
	}

	// **************************** end of bridge method creation code *****************

	/**
	 * Weave any declare @method/@ctor statements into the members of the supplied class
	 */
	private boolean weaveDeclareAtMethodCtor(LazyClassGen clazz) {
		List<Integer> reportedProblems = new ArrayList<Integer>();

		List<DeclareAnnotation> allDecams = world.getDeclareAnnotationOnMethods();
		if (allDecams.isEmpty()) {
			return false;
		}

		boolean isChanged = false;

		// deal with ITDs
		List<ConcreteTypeMunger> itdMethodsCtors = getITDSubset(clazz, ResolvedTypeMunger.Method);
		itdMethodsCtors.addAll(getITDSubset(clazz, ResolvedTypeMunger.Constructor));
		if (!itdMethodsCtors.isEmpty()) {
			// Can't use the subset called 'decaMs' as it won't be right for
			// ITDs...
			isChanged = weaveAtMethodOnITDSRepeatedly(allDecams, itdMethodsCtors, reportedProblems);
		}

		List<DeclareAnnotation> decaMs = getMatchingSubset(allDecams, clazz.getType());
		if (decaMs.isEmpty()) {
			return false; // nothing to do
		}
		
		Set<DeclareAnnotation> unusedDecams = new HashSet<DeclareAnnotation>();
		unusedDecams.addAll(decaMs);

		// These methods may have been targeted with declare annotation.  Example: ITD on an interface
		// where the top most implementor gets a real method.  The top most implementor method
		// is an 'addedLazyMethodGen'
		if (addedLazyMethodGens!=null) {
			for (LazyMethodGen method: addedLazyMethodGens) {
				// They have no resolvedmember of their own, conjure one up for matching purposes
				ResolvedMember resolvedmember = 
					new ResolvedMemberImpl(ResolvedMember.METHOD,method.getEnclosingClass().getType(),method.getAccessFlags(),
							BcelWorld.fromBcel(method.getReturnType()),method.getName(),
							BcelWorld.fromBcel(method.getArgumentTypes()),UnresolvedType.forNames(method.getDeclaredExceptions()));
				resolvedmember.setAnnotationTypes(method.getAnnotationTypes());
				resolvedmember.setAnnotations(method.getAnnotations());

				List<DeclareAnnotation> worthRetrying = new ArrayList<DeclareAnnotation>();
				boolean modificationOccured = false;
				for (DeclareAnnotation decam: decaMs) {
					if (decam.matches(resolvedmember, world)) {
						if (doesAlreadyHaveAnnotation(resolvedmember, decam, reportedProblems,false)) {
							// remove the declare @method since don't want an error when the annotation is already there
							unusedDecams.remove(decam);
							continue;
						}

						AnnotationGen a = ((BcelAnnotation) decam.getAnnotation()).getBcelAnnotation();
						// create copy to get the annotation type into the right constant pool
						AnnotationAJ aj = new BcelAnnotation(new AnnotationGen(a, clazz.getConstantPool(), true),world);
						method.addAnnotation(aj);
						resolvedmember.addAnnotation(decam.getAnnotation());

						AsmRelationshipProvider.addDeclareAnnotationMethodRelationship(decam.getSourceLocation(),
								clazz.getName(), resolvedmember, world.getModelAsAsmManager());
						reportMethodCtorWeavingMessage(clazz, resolvedmember, decam, method.getDeclarationLineNumber());
						isChanged = true;
						modificationOccured = true;
						unusedDecams.remove(decam);
					} else if (!decam.isStarredAnnotationPattern()) {
						// an annotation is specified that might be put on by a subsequent decaf
						worthRetrying.add(decam); 
					}
				}

				// Multiple secondary passes
				while (!worthRetrying.isEmpty() && modificationOccured) {
					modificationOccured = false;
					// lets have another go
					List<DeclareAnnotation> forRemoval = new ArrayList<DeclareAnnotation>();
					for (DeclareAnnotation decam : worthRetrying) {
						if (decam.matches(resolvedmember, world)) {
							if (doesAlreadyHaveAnnotation(resolvedmember, decam, reportedProblems,false)) {
								// remove the declare @method since don't
								// want an error when
								// the annotation is already there
								unusedDecams.remove(decam);
								continue; // skip this one...
							}
							AnnotationGen a = ((BcelAnnotation) decam.getAnnotation()).getBcelAnnotation();
							// create copy to get the annotation type into the right constant pool
							AnnotationAJ aj = new BcelAnnotation(new AnnotationGen(a, clazz.getConstantPool(), true),world);
							method.addAnnotation(aj);
							resolvedmember.addAnnotation(decam.getAnnotation());
							AsmRelationshipProvider.addDeclareAnnotationMethodRelationship(decam.getSourceLocation(),
									clazz.getName(), resolvedmember, world.getModelAsAsmManager());// getMethod());
							isChanged = true;
							modificationOccured = true;
							forRemoval.add(decam);
							unusedDecams.remove(decam);
						}
					}
					worthRetrying.removeAll(forRemoval);
				}
			}
		}
		
		
		// deal with all the other methods...
		List<LazyMethodGen> members = clazz.getMethodGens();
		if (!members.isEmpty()) {
			for (int memberCounter = 0; memberCounter < members.size(); memberCounter++) {
				LazyMethodGen mg = members.get(memberCounter);
				if (!mg.getName().startsWith(NameMangler.PREFIX)) {

					// Single first pass
					List<DeclareAnnotation> worthRetrying = new ArrayList<DeclareAnnotation>();
					boolean modificationOccured = false;
					List<AnnotationGen> annotationsToAdd = null;
					for (DeclareAnnotation decaM : decaMs) {

						if (decaM.matches(mg.getMemberView(), world)) {
							if (doesAlreadyHaveAnnotation(mg.getMemberView(), decaM, reportedProblems,true)) {
								// remove the declare @method since don't want
								// an error when the annotation is already there
								unusedDecams.remove(decaM);
								continue; // skip this one...
							}

							if (annotationsToAdd == null) {
								annotationsToAdd = new ArrayList<AnnotationGen>();
							}
							AnnotationGen a = ((BcelAnnotation) decaM.getAnnotation()).getBcelAnnotation();
							AnnotationGen ag = new AnnotationGen(a, clazz.getConstantPool(), true);
							annotationsToAdd.add(ag);
							mg.addAnnotation(decaM.getAnnotation());

							AsmRelationshipProvider.addDeclareAnnotationMethodRelationship(decaM.getSourceLocation(),
									clazz.getName(), mg.getMemberView(), world.getModelAsAsmManager());// getMethod());
							reportMethodCtorWeavingMessage(clazz, mg.getMemberView(), decaM, mg.getDeclarationLineNumber());
							isChanged = true;
							modificationOccured = true;
							// remove the declare @method since have matched
							// against it
							unusedDecams.remove(decaM);
						} else {
							if (!decaM.isStarredAnnotationPattern()) {
								worthRetrying.add(decaM); // an annotation is
								// specified that
								// might be put on
								// by a subsequent
								// decaf
							}
						}
					}

					// Multiple secondary passes
					while (!worthRetrying.isEmpty() && modificationOccured) {
						modificationOccured = false;
						// lets have another go
						List<DeclareAnnotation> forRemoval = new ArrayList<DeclareAnnotation>();
						for (DeclareAnnotation decaM : worthRetrying) {
							if (decaM.matches(mg.getMemberView(), world)) {
								if (doesAlreadyHaveAnnotation(mg.getMemberView(), decaM, reportedProblems,true)) {
									// remove the declare @method since don't
									// want an error when
									// the annotation is already there
									unusedDecams.remove(decaM);
									continue; // skip this one...
								}

								if (annotationsToAdd == null) {
									annotationsToAdd = new ArrayList<AnnotationGen>();
								}
								AnnotationGen a = ((BcelAnnotation) decaM.getAnnotation()).getBcelAnnotation();
								// create copy to get the annotation type into the right constant pool
								AnnotationGen ag = new AnnotationGen(a, clazz.getConstantPool(), true);
								annotationsToAdd.add(ag);
								mg.addAnnotation(decaM.getAnnotation());
								AsmRelationshipProvider.addDeclareAnnotationMethodRelationship(decaM.getSourceLocation(),
										clazz.getName(), mg.getMemberView(), world.getModelAsAsmManager());// getMethod());
								isChanged = true;
								modificationOccured = true;
								forRemoval.add(decaM);
								// remove the declare @method since have matched
								// against it
								unusedDecams.remove(decaM);
							}
						}
						worthRetrying.removeAll(forRemoval);
					}
					if (annotationsToAdd != null) {
						Method oldMethod = mg.getMethod();
						MethodGen myGen = new MethodGen(oldMethod, clazz.getClassName(), clazz.getConstantPool(), false);
						for (AnnotationGen a : annotationsToAdd) {
							myGen.addAnnotation(a);
						}
						Method newMethod = myGen.getMethod();
						members.set(memberCounter, new LazyMethodGen(newMethod, clazz));
					}

				}
			}
			checkUnusedDeclareAts(unusedDecams, false);
		}
		return isChanged;
	}

	// TAG: WeavingMessage
	private void reportMethodCtorWeavingMessage(LazyClassGen clazz, ResolvedMember member, DeclareAnnotation decaM,
			int memberLineNumber) {
		if (!getWorld().getMessageHandler().isIgnoring(IMessage.WEAVEINFO)) {
			StringBuffer parmString = new StringBuffer("(");
			UnresolvedType[] paramTypes = member.getParameterTypes();
			for (int i = 0; i < paramTypes.length; i++) {
				UnresolvedType type = paramTypes[i];
				String s = org.aspectj.apache.bcel.classfile.Utility.signatureToString(type.getSignature());
				if (s.lastIndexOf('.') != -1) {
					s = s.substring(s.lastIndexOf('.') + 1);
				}
				parmString.append(s);
				if ((i + 1) < paramTypes.length) {
					parmString.append(",");
				}
			}
			parmString.append(")");
			String methodName = member.getName();
			StringBuffer sig = new StringBuffer();
			sig.append(org.aspectj.apache.bcel.classfile.Utility.accessToString(member.getModifiers()));
			sig.append(" ");
			sig.append(member.getReturnType().toString());
			sig.append(" ");
			sig.append(member.getDeclaringType().toString());
			sig.append(".");
			sig.append(methodName.equals("<init>") ? "new" : methodName);
			sig.append(parmString);

			StringBuffer loc = new StringBuffer();
			if (clazz.getFileName() == null) {
				loc.append("no debug info available");
			} else {
				loc.append(clazz.getFileName());
				if (memberLineNumber != -1) {
					loc.append(":" + memberLineNumber);
				}
			}
			getWorld().getMessageHandler().handleMessage(
					WeaveMessage.constructWeavingMessage(
							WeaveMessage.WEAVEMESSAGE_ANNOTATES,
							new String[] { sig.toString(), loc.toString(), decaM.getAnnotationString(),
									methodName.startsWith("<init>") ? "constructor" : "method", decaM.getAspect().toString(),
									Utility.beautifyLocation(decaM.getSourceLocation()) }));
		}
	}

	/**
	 * Looks through a list of declare annotation statements and only returns those that could possibly match on a field/method/ctor
	 * in type.
	 */
	private List<DeclareAnnotation> getMatchingSubset(List<DeclareAnnotation> declareAnnotations, ResolvedType type) {
		List<DeclareAnnotation> subset = new ArrayList<DeclareAnnotation>();
		for (DeclareAnnotation da : declareAnnotations) {
			if (da.couldEverMatch(type)) {
				subset.add(da);
			}
		}
		return subset;
	}

	/**
	 * Get a subset of all the type mungers defined on this aspect
	 */
	private List<ConcreteTypeMunger> getITDSubset(LazyClassGen clazz, ResolvedTypeMunger.Kind wantedKind) {
		List<ConcreteTypeMunger> subset = new ArrayList<ConcreteTypeMunger>();
		for (ConcreteTypeMunger typeMunger : clazz.getBcelObjectType().getTypeMungers()) {
			if (typeMunger.getMunger().getKind() == wantedKind) {
				subset.add(typeMunger);
			}
		}
		return subset;
	}

	public LazyMethodGen locateAnnotationHolderForFieldMunger(LazyClassGen clazz, ConcreteTypeMunger fieldMunger) {
		NewFieldTypeMunger newFieldMunger = (NewFieldTypeMunger) fieldMunger.getMunger();
		ResolvedMember lookingFor = AjcMemberMaker.interFieldInitializer(newFieldMunger.getSignature(), clazz.getType());
		for (LazyMethodGen method : clazz.getMethodGens()) {
			if (method.getName().equals(lookingFor.getName())) {
				return method;
			}
		}
		return null;
	}

	// FIXME asc refactor this to neaten it up
	public LazyMethodGen locateAnnotationHolderForMethodCtorMunger(LazyClassGen clazz, ConcreteTypeMunger methodCtorMunger) {
		ResolvedTypeMunger rtMunger = methodCtorMunger.getMunger();
		ResolvedMember lookingFor = null;
		if (rtMunger instanceof NewMethodTypeMunger) {
			NewMethodTypeMunger nftm = (NewMethodTypeMunger) rtMunger;
			lookingFor = AjcMemberMaker.interMethodDispatcher(nftm.getSignature(), methodCtorMunger.getAspectType());
		} else if (rtMunger instanceof NewConstructorTypeMunger) {
			NewConstructorTypeMunger nftm = (NewConstructorTypeMunger) rtMunger;
			lookingFor = AjcMemberMaker.postIntroducedConstructor(methodCtorMunger.getAspectType(), nftm.getSignature()
					.getDeclaringType(), nftm.getSignature().getParameterTypes());
		} else {
			throw new BCException("Not sure what this is: " + methodCtorMunger);
		}
		String name = lookingFor.getName();
		String paramSignature = lookingFor.getParameterSignature();
		for (LazyMethodGen member : clazz.getMethodGens()) {
			if (member.getName().equals(name) && member.getParameterSignature().equals(paramSignature)) {
				return member;
			}
		}
		return null;
	}

	/**
	 * Applies some set of declare @field constructs (List<DeclareAnnotation>) to some bunch of ITDfields (List<BcelTypeMunger>. It
	 * will iterate over the fields repeatedly until everything has been applied.
	 * 
	 */
	private boolean weaveAtFieldRepeatedly(List<DeclareAnnotation> decaFs, List<ConcreteTypeMunger> itdFields,
			List<Integer> reportedErrors) {
		boolean isChanged = false;
		for (Iterator<ConcreteTypeMunger> iter = itdFields.iterator(); iter.hasNext();) {
			BcelTypeMunger fieldMunger = (BcelTypeMunger) iter.next();
			ResolvedMember itdIsActually = fieldMunger.getSignature();
			Set<DeclareAnnotation> worthRetrying = new LinkedHashSet<DeclareAnnotation>();
			boolean modificationOccured = false;

			for (Iterator<DeclareAnnotation> iter2 = decaFs.iterator(); iter2.hasNext();) {
				DeclareAnnotation decaF = iter2.next();
				if (decaF.matches(itdIsActually, world)) {
					if (decaF.isRemover()) {
						LazyMethodGen annotationHolder = locateAnnotationHolderForFieldMunger(clazz, fieldMunger);
						if (annotationHolder.hasAnnotation(decaF.getAnnotationType())) {
							isChanged = true;
							// something to remove
							annotationHolder.removeAnnotation(decaF.getAnnotationType());
							AsmRelationshipProvider.addDeclareAnnotationRelationship(world.getModelAsAsmManager(),
									decaF.getSourceLocation(), itdIsActually.getSourceLocation(), true);
						} else {
							worthRetrying.add(decaF);
						}
					} else {

						LazyMethodGen annotationHolder = locateAnnotationHolderForFieldMunger(clazz, fieldMunger);
						if (doesAlreadyHaveAnnotation(annotationHolder, itdIsActually, decaF, reportedErrors)) {
							continue; // skip this one...
						}
						annotationHolder.addAnnotation(decaF.getAnnotation());
						AsmRelationshipProvider.addDeclareAnnotationRelationship(world.getModelAsAsmManager(),
								decaF.getSourceLocation(), itdIsActually.getSourceLocation(), false);
						isChanged = true;
						modificationOccured = true;
					}
				} else {
					if (!decaF.isStarredAnnotationPattern()) {
						worthRetrying.add(decaF); // an annotation is specified
						// that might be put on by a
						// subsequent decaf
					}
				}
			}

			while (!worthRetrying.isEmpty() && modificationOccured) {
				modificationOccured = false;
				List<DeclareAnnotation> forRemoval = new ArrayList<DeclareAnnotation>();
				for (Iterator<DeclareAnnotation> iter2 = worthRetrying.iterator(); iter2.hasNext();) {
					DeclareAnnotation decaF = iter2.next();
					if (decaF.matches(itdIsActually, world)) {
						if (decaF.isRemover()) {
							LazyMethodGen annotationHolder = locateAnnotationHolderForFieldMunger(clazz, fieldMunger);
							if (annotationHolder.hasAnnotation(decaF.getAnnotationType())) {
								isChanged = true;
								// something to remove
								annotationHolder.removeAnnotation(decaF.getAnnotationType());
								AsmRelationshipProvider.addDeclareAnnotationRelationship(world.getModelAsAsmManager(),
										decaF.getSourceLocation(), itdIsActually.getSourceLocation(), true);
								forRemoval.add(decaF);
							}
						} else {
							LazyMethodGen annotationHolder = locateAnnotationHolderForFieldMunger(clazz, fieldMunger);
							if (doesAlreadyHaveAnnotation(annotationHolder, itdIsActually, decaF, reportedErrors)) {
								continue; // skip this one...
							}
							annotationHolder.addAnnotation(decaF.getAnnotation());
							AsmRelationshipProvider.addDeclareAnnotationRelationship(world.getModelAsAsmManager(),
									decaF.getSourceLocation(), itdIsActually.getSourceLocation(), false);
							isChanged = true;
							modificationOccured = true;
							forRemoval.add(decaF);
						}
					}
				}
				worthRetrying.removeAll(forRemoval);
			}
		}
		return isChanged;
	}

	/**
	 * Applies some set of declare @method/@ctor constructs (List<DeclareAnnotation>) to some bunch of ITDmembers
	 * (List<BcelTypeMunger>. It will iterate over the fields repeatedly until everything has been applied.
	 */
	private boolean weaveAtMethodOnITDSRepeatedly(List<DeclareAnnotation> decaMCs,
			List<ConcreteTypeMunger> itdsForMethodAndConstructor, List<Integer> reportedErrors) {
		boolean isChanged = false;
		AsmManager asmManager = world.getModelAsAsmManager();
		for (ConcreteTypeMunger methodctorMunger : itdsForMethodAndConstructor) {
			// for (Iterator iter = itdsForMethodAndConstructor.iterator(); iter.hasNext();) {
			// BcelTypeMunger methodctorMunger = (BcelTypeMunger) iter.next();
			ResolvedMember unMangledInterMethod = methodctorMunger.getSignature();
			List<DeclareAnnotation> worthRetrying = new ArrayList<DeclareAnnotation>();
			boolean modificationOccured = false;

			for (Iterator<DeclareAnnotation> iter2 = decaMCs.iterator(); iter2.hasNext();) {
				DeclareAnnotation decaMC = iter2.next();
				if (decaMC.matches(unMangledInterMethod, world)) {
					LazyMethodGen annotationHolder = locateAnnotationHolderForMethodCtorMunger(clazz, methodctorMunger);
					if (annotationHolder == null
							|| doesAlreadyHaveAnnotation(annotationHolder, unMangledInterMethod, decaMC, reportedErrors)) {
						continue; // skip this one...
					}
					annotationHolder.addAnnotation(decaMC.getAnnotation());
					isChanged = true;
					AsmRelationshipProvider.addDeclareAnnotationRelationship(asmManager, decaMC.getSourceLocation(),
							unMangledInterMethod.getSourceLocation(), false);
					reportMethodCtorWeavingMessage(clazz, unMangledInterMethod, decaMC, -1);
					modificationOccured = true;
				} else {
					// If an annotation is specified, it might be added by one of the other declare annotation statements
					if (!decaMC.isStarredAnnotationPattern()) {
						worthRetrying.add(decaMC);
					}
				}
			}

			while (!worthRetrying.isEmpty() && modificationOccured) {
				modificationOccured = false;
				List<DeclareAnnotation> forRemoval = new ArrayList<DeclareAnnotation>();
				for (Iterator<DeclareAnnotation> iter2 = worthRetrying.iterator(); iter2.hasNext();) {
					DeclareAnnotation decaMC = iter2.next();
					if (decaMC.matches(unMangledInterMethod, world)) {
						LazyMethodGen annotationHolder = locateAnnotationHolderForFieldMunger(clazz, methodctorMunger);
						if (doesAlreadyHaveAnnotation(annotationHolder, unMangledInterMethod, decaMC, reportedErrors)) {
							continue; // skip this one...
						}
						annotationHolder.addAnnotation(decaMC.getAnnotation());
						unMangledInterMethod.addAnnotation(decaMC.getAnnotation());
						AsmRelationshipProvider.addDeclareAnnotationRelationship(asmManager, decaMC.getSourceLocation(),
								unMangledInterMethod.getSourceLocation(), false);
						isChanged = true;
						modificationOccured = true;
						forRemoval.add(decaMC);
					}
					worthRetrying.removeAll(forRemoval);
				}
			}
		}
		return isChanged;
	}

	private boolean dontAddTwice(DeclareAnnotation decaF, AnnotationAJ[] dontAddMeTwice) {
		for (AnnotationAJ ann : dontAddMeTwice) {
			if (ann != null && decaF.getAnnotation().getTypeName().equals(ann.getTypeName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove an annotation from the supplied array, if it is in there.
	 */
	private AnnotationAJ[] removeFromAnnotationsArray(AnnotationAJ[] annotations,AnnotationAJ annotation) {
		for (int i=0;i<annotations.length;i++) {
			if (annotations[i] != null && annotation.getTypeName().equals(annotations[i].getTypeName())) {
				// Remove it!
				AnnotationAJ[] newArray = new AnnotationAJ[annotations.length-1];
				int index=0;
				for (int j=0;j<annotations.length;j++) {
					if (j!=i) {
						newArray[index++]=annotations[j];
					}
				}
				return newArray;
			}
		}
		return annotations;
	}

	// BUGWARNING not getting enough warnings out on declare @field ? There is a potential problem here with warnings not
	// coming out - this will occur if they are created on the second iteration round this loop.
	// We currently deactivate error reporting for the second time round. A possible solution is to record what annotations
	// were added by what decafs and check that to see if an error needs to be reported - this would be expensive so lets
	// skip it for now
	/**
	 * Weave any declare @field statements into the fields of the supplied class. This will attempt to apply them to the ITDs too.
	 * 
	 * Interesting case relating to public ITDd fields. The annotations are really stored against the interfieldinit method in the
	 * aspect, but the public field is placed in the target type and then is processed in the 2nd pass over fields that occurs. I
	 * think it would be more expensive to avoid putting the annotation on that inserted public field than just to have it put there
	 * as well as on the interfieldinit method.
	 */
	private boolean weaveDeclareAtField(LazyClassGen clazz) {
		List<Integer> reportedProblems = new ArrayList<Integer>();
		List<DeclareAnnotation> allDecafs = world.getDeclareAnnotationOnFields();
		if (allDecafs.isEmpty()) {
			return false;
		}
		boolean typeIsChanged = false;
		List<ConcreteTypeMunger> relevantItdFields = getITDSubset(clazz, ResolvedTypeMunger.Field);
		if (relevantItdFields != null) {
			typeIsChanged = weaveAtFieldRepeatedly(allDecafs, relevantItdFields, reportedProblems);
		}

		List<DeclareAnnotation> decafs = getMatchingSubset(allDecafs, clazz.getType());
		if (decafs.isEmpty()) {
			return typeIsChanged;
		}

		List<BcelField> fields = clazz.getFieldGens();
		if (fields != null) {
			Set<DeclareAnnotation> unusedDecafs = new HashSet<DeclareAnnotation>();
			unusedDecafs.addAll(decafs);
			for (BcelField field : fields) {
				if (!field.getName().startsWith(NameMangler.PREFIX)) {
					// Single first pass
					Set<DeclareAnnotation> worthRetrying = new LinkedHashSet<DeclareAnnotation>();
					boolean modificationOccured = false;
					AnnotationAJ[] dontAddMeTwice = field.getAnnotations();

					// go through all the declare @field statements
					for (DeclareAnnotation decaf : decafs) {
						if (decaf.getAnnotation() == null) {
							return false;
						}
						if (decaf.matches(field, world)) {
							if (decaf.isRemover()) {
								AnnotationAJ annotation = decaf.getAnnotation();
								if (field.hasAnnotation(annotation.getType())) {
									// something to remove
									typeIsChanged = true;
									field.removeAnnotation(annotation);
									AsmRelationshipProvider.addDeclareAnnotationFieldRelationship(world.getModelAsAsmManager(),
											decaf.getSourceLocation(), clazz.getName(), field, true);
									reportFieldAnnotationWeavingMessage(clazz, field, decaf, true);
									dontAddMeTwice = removeFromAnnotationsArray(dontAddMeTwice, annotation);
								} else {
									worthRetrying.add(decaf);
								}
								unusedDecafs.remove(decaf);
							} else {
								if (!dontAddTwice(decaf, dontAddMeTwice)) {
									if (doesAlreadyHaveAnnotation(field, decaf, reportedProblems,true )) {
										// remove the declare @field since don't want an error when the annotation is already there
										unusedDecafs.remove(decaf);
										continue;
									}
									field.addAnnotation(decaf.getAnnotation());
								}
								AsmRelationshipProvider.addDeclareAnnotationFieldRelationship(world.getModelAsAsmManager(),
										decaf.getSourceLocation(), clazz.getName(), field, false);
								reportFieldAnnotationWeavingMessage(clazz, field, decaf, false);
								typeIsChanged = true;
								modificationOccured = true;
								unusedDecafs.remove(decaf);
							}
						} else if (!decaf.isStarredAnnotationPattern() || decaf.isRemover()) {
							worthRetrying.add(decaf); // an annotation is specified that might be put on by a subsequent decaf
						}
					}

					// Multiple secondary passes
					while (!worthRetrying.isEmpty() && modificationOccured) {
						modificationOccured = false;
						// lets have another go with any remaining ones
						List<DeclareAnnotation> forRemoval = new ArrayList<DeclareAnnotation>();
						for (Iterator<DeclareAnnotation> iter = worthRetrying.iterator(); iter.hasNext();) {
							DeclareAnnotation decaF = iter.next();

							if (decaF.matches(field, world)) {
								if (decaF.isRemover()) {
									AnnotationAJ annotation = decaF.getAnnotation();
									if (field.hasAnnotation(annotation.getType())) {
										// something to remove
										typeIsChanged = modificationOccured = true;
										forRemoval.add(decaF);
										field.removeAnnotation(annotation);
										AsmRelationshipProvider.addDeclareAnnotationFieldRelationship(world.getModelAsAsmManager(),
												decaF.getSourceLocation(), clazz.getName(), field, true);
										reportFieldAnnotationWeavingMessage(clazz, field, decaF, true);
									}
								} else {
									// below code is for recursive things
									unusedDecafs.remove(decaF);
									if (doesAlreadyHaveAnnotation(field, decaF, reportedProblems,true)) {
										continue;
									}
									field.addAnnotation(decaF.getAnnotation());
									AsmRelationshipProvider.addDeclareAnnotationFieldRelationship(world.getModelAsAsmManager(),
											decaF.getSourceLocation(), clazz.getName(), field, false);
									typeIsChanged = modificationOccured = true;
									forRemoval.add(decaF);
								}
							}
						}
						worthRetrying.removeAll(forRemoval);
					}
				}
			}
			checkUnusedDeclareAts(unusedDecafs, true);
		}
		return typeIsChanged;
	}

	// bug 99191 - put out an error message if the type doesn't exist
	/**
	 * Report an error if the reason a "declare @method/ctor/field" was not used was because the member specified does not exist.
	 * This method is passed some set of declare statements that didn't match and a flag indicating whether the set contains declare @field
	 * or declare @method/ctor entries.
	 */
	private void checkUnusedDeclareAts(Set<DeclareAnnotation> unusedDecaTs, boolean isDeclareAtField) {
		for (DeclareAnnotation declA : unusedDecaTs) {

			// Error if an exact type pattern was specified
			boolean shouldCheck = declA.isExactPattern() || declA.getSignaturePattern().getExactDeclaringTypes().size() != 0;

			if (shouldCheck && declA.getKind() != DeclareAnnotation.AT_CONSTRUCTOR) {
				if (declA.getSignaturePattern().isMatchOnAnyName()) {
					shouldCheck = false;
				} else {
					List<ExactTypePattern> declaringTypePatterns = declA.getSignaturePattern().getExactDeclaringTypes();
					if (declaringTypePatterns.size() == 0) {
						shouldCheck = false;
					} else {
						for (ExactTypePattern exactTypePattern : declaringTypePatterns) {
							if (exactTypePattern.isIncludeSubtypes()) {
								shouldCheck = false;
								break;
							}
						}
					}
				}
			}
			if (shouldCheck) {
				// Quickly check if an ITD supplies the 'missing' member
				boolean itdMatch = false;
				List<ConcreteTypeMunger> lst = clazz.getType().getInterTypeMungers();
				for (Iterator<ConcreteTypeMunger> iterator = lst.iterator(); iterator.hasNext() && !itdMatch;) {
					ConcreteTypeMunger element = iterator.next();
					if (element.getMunger() instanceof NewFieldTypeMunger) {
						NewFieldTypeMunger nftm = (NewFieldTypeMunger) element.getMunger();
						itdMatch = declA.matches(nftm.getSignature(), world);
					} else if (element.getMunger() instanceof NewMethodTypeMunger) {
						NewMethodTypeMunger nmtm = (NewMethodTypeMunger) element.getMunger();
						itdMatch = declA.matches(nmtm.getSignature(), world);
					} else if (element.getMunger() instanceof NewConstructorTypeMunger) {
						NewConstructorTypeMunger nctm = (NewConstructorTypeMunger) element.getMunger();
						itdMatch = declA.matches(nctm.getSignature(), world);
					}
				}
				if (!itdMatch) {
					IMessage message = null;
					if (isDeclareAtField) {
						message = new Message("The field '" + declA.getSignaturePattern().toString() + "' does not exist",
								declA.getSourceLocation(), true);
					} else {
						message = new Message("The method '" + declA.getSignaturePattern().toString() + "' does not exist",
								declA.getSourceLocation(), true);
					}
					world.getMessageHandler().handleMessage(message);
				}
			}
		}
	}

	// TAG: WeavingMessage
	private void reportFieldAnnotationWeavingMessage(LazyClassGen clazz, BcelField theField, DeclareAnnotation decaf,
			boolean isRemove) {
		if (!getWorld().getMessageHandler().isIgnoring(IMessage.WEAVEINFO)) {
			world.getMessageHandler().handleMessage(
					WeaveMessage.constructWeavingMessage(
							isRemove ? WeaveMessage.WEAVEMESSAGE_REMOVES_ANNOTATION : WeaveMessage.WEAVEMESSAGE_ANNOTATES,
							new String[] { theField.getFieldAsIs().toString() + "' of type '" + clazz.getName(),
									clazz.getFileName(), decaf.getAnnotationString(), "field", decaf.getAspect().toString(),
									Utility.beautifyLocation(decaf.getSourceLocation()) }));
		}
	}

	/**
	 * Check if a resolved member (field/method/ctor) already has an annotation, if it does then put out a warning and return true
	 */
	private boolean doesAlreadyHaveAnnotation(ResolvedMember rm, DeclareAnnotation deca, List<Integer> reportedProblems, boolean reportError) {
		if (rm.hasAnnotation(deca.getAnnotationType())) {
			if (reportError && world.getLint().elementAlreadyAnnotated.isEnabled()) {
				Integer uniqueID = new Integer(rm.hashCode() * deca.hashCode());
				if (!reportedProblems.contains(uniqueID)) {
					reportedProblems.add(uniqueID);
					world.getLint().elementAlreadyAnnotated.signal(new String[] { rm.toString(),
							deca.getAnnotationType().toString() }, rm.getSourceLocation(),
							new ISourceLocation[] { deca.getSourceLocation() });
				}
			}
			return true;
		}
		return false;
	}

	private boolean doesAlreadyHaveAnnotation(LazyMethodGen rm, ResolvedMember itdfieldsig, DeclareAnnotation deca,
			List<Integer> reportedProblems) {
		if (rm != null && rm.hasAnnotation(deca.getAnnotationType())) {
			if (world.getLint().elementAlreadyAnnotated.isEnabled()) {
				Integer uniqueID = new Integer(rm.hashCode() * deca.hashCode());
				if (!reportedProblems.contains(uniqueID)) {
					reportedProblems.add(uniqueID);
					reportedProblems.add(new Integer(itdfieldsig.hashCode() * deca.hashCode()));
					world.getLint().elementAlreadyAnnotated.signal(new String[] { itdfieldsig.toString(),
							deca.getAnnotationType().toString() }, rm.getSourceLocation(),
							new ISourceLocation[] { deca.getSourceLocation() });
				}
			}
			return true;
		}
		return false;
	}

	private Set<String> findAspectsForMungers(LazyMethodGen mg) {
		Set<String> aspectsAffectingType = new HashSet<String>();
		for (BcelShadow shadow : mg.matchedShadows) {
			for (ShadowMunger munger : shadow.getMungers()) {
				if (munger instanceof BcelAdvice) {
					BcelAdvice bcelAdvice = (BcelAdvice) munger;
					if (bcelAdvice.getConcreteAspect() != null) {
						aspectsAffectingType.add(bcelAdvice.getConcreteAspect().getSignature());
					}
				} else {
					// It is a 'Checker' - we don't need to remember aspects
					// that only contributed Checkers...
				}
			}
		}
		return aspectsAffectingType;
	}

	private boolean inlineSelfConstructors(List<LazyMethodGen> methodGens, List<LazyMethodGen> recursiveCtors) {
		boolean inlinedSomething = false;
		List<LazyMethodGen> newRecursiveCtors = new ArrayList<LazyMethodGen>();
		for (LazyMethodGen methodGen : methodGens) {
			if (!methodGen.getName().equals("<init>")) {
				continue;
			}
			InstructionHandle ih = findSuperOrThisCall(methodGen);
			if (ih != null && isThisCall(ih)) {
				LazyMethodGen donor = getCalledMethod(ih);
				if (donor.equals(methodGen)) {
					newRecursiveCtors.add(donor);
				} else {
					if (!recursiveCtors.contains(donor)) {
						inlineMethod(donor, methodGen, ih);
						inlinedSomething = true;
					}
				}
			}
		}
		recursiveCtors.addAll(newRecursiveCtors);
		return inlinedSomething;
	}

	private void positionAndImplement(List<BcelShadow> initializationShadows) {
		for (BcelShadow s : initializationShadows) {
			positionInitializationShadow(s);
			// s.getEnclosingMethod().print();
			s.implement();
		}
	}

	private void positionInitializationShadow(BcelShadow s) {
		LazyMethodGen mg = s.getEnclosingMethod();
		InstructionHandle call = findSuperOrThisCall(mg);
		InstructionList body = mg.getBody();
		ShadowRange r = new ShadowRange(body);
		r.associateWithShadow(s);
		if (s.getKind() == Shadow.PreInitialization) {
			// XXX assert first instruction is an ALOAD_0.
			// a pre shadow goes from AFTER the first instruction (which we
			// believe to
			// be an ALOAD_0) to just before the call to super
			r.associateWithTargets(Range.genStart(body, body.getStart().getNext()), Range.genEnd(body, call.getPrev()));
		} else {
			// assert s.getKind() == Shadow.Initialization
			r.associateWithTargets(Range.genStart(body, call.getNext()), Range.genEnd(body));
		}
	}

	private boolean isThisCall(InstructionHandle ih) {
		InvokeInstruction inst = (InvokeInstruction) ih.getInstruction();
		return inst.getClassName(cpg).equals(clazz.getName());
	}

	/**
	 * inline a particular call in bytecode.
	 * 
	 * @param donor the method we want to inline
	 * @param recipient the method containing the call we want to inline
	 * @param call the instructionHandle in recipient's body holding the call we want to inline.
	 */
	public static void inlineMethod(LazyMethodGen donor, LazyMethodGen recipient, InstructionHandle call) {
		// assert recipient.contains(call)

		/*
		 * Implementation notes:
		 * 
		 * We allocate two slots for every tempvar so we don't screw up longs and doubles which may share space. This could be
		 * conservatively avoided (no reference to a long/double instruction, don't do it) or packed later. Right now we don't
		 * bother to pack.
		 * 
		 * Allocate a new var for each formal param of the inlined. Fill with stack contents. Then copy the inlined instructions in
		 * with the appropriate remap table. Any framelocs used by locals in inlined are reallocated to top of frame,
		 */
		final InstructionFactory fact = recipient.getEnclosingClass().getFactory();

		IntMap frameEnv = new IntMap();

		// this also sets up the initial environment
		InstructionList argumentStores = genArgumentStores(donor, recipient, frameEnv, fact);

		InstructionList inlineInstructions = genInlineInstructions(donor, recipient, frameEnv, fact, false);

		inlineInstructions.insert(argumentStores);

		recipient.getBody().append(call, inlineInstructions);
		Utility.deleteInstruction(call, recipient);
	}

	// public BcelVar genTempVar(UnresolvedType typeX) {
	// return new BcelVar(typeX.resolve(world),
	// genTempVarIndex(typeX.getSize()));
	// }
	//
	// private int genTempVarIndex(int size) {
	// return enclosingMethod.allocateLocal(size);
	// }

	/**
	 * Input method is a synchronized method, we remove the bit flag for synchronized and then insert a try..finally block
	 * 
	 * Some jumping through firey hoops required - depending on the input code level (1.5 or not) we may or may not be able to use
	 * the LDC instruction that takes a class literal (doesnt on <1.5).
	 * 
	 * FIXME asc Before promoting -Xjoinpoints:synchronization to be a standard option, this needs a bunch of tidying up - there is
	 * some duplication that can be removed.
	 */
	public static void transformSynchronizedMethod(LazyMethodGen synchronizedMethod) {
		if (trace.isTraceEnabled()) {
			trace.enter("transformSynchronizedMethod", synchronizedMethod);
		}
		// System.err.println("DEBUG: Transforming synchronized method: "+
		// synchronizedMethod.getName());
		final InstructionFactory fact = synchronizedMethod.getEnclosingClass().getFactory();
		InstructionList body = synchronizedMethod.getBody();
		InstructionList prepend = new InstructionList();
		Type enclosingClassType = BcelWorld.makeBcelType(synchronizedMethod.getEnclosingClass().getType());

		// STATIC METHOD TRANSFORMATION
		if (synchronizedMethod.isStatic()) {

			// What to do here depends on the level of the class file!
			// LDC can handle class literals in Java5 and above *sigh*
			if (synchronizedMethod.getEnclosingClass().isAtLeastJava5()) {
				// MONITORENTER logic:
				// 0: ldc #2; //class C
				// 2: dup
				// 3: astore_0
				// 4: monitorenter
				int slotForLockObject = synchronizedMethod.allocateLocal(enclosingClassType);
				prepend.append(fact.createConstant(enclosingClassType));
				prepend.append(InstructionFactory.createDup(1));
				prepend.append(InstructionFactory.createStore(enclosingClassType, slotForLockObject));
				prepend.append(InstructionFactory.MONITORENTER);

				// MONITOREXIT logic:

				// We basically need to wrap the code from the method in a
				// finally block that
				// will ensure monitorexit is called. Content on the finally
				// block seems to
				// be always:
				//
				// E1: ALOAD_1
				// MONITOREXIT
				// ATHROW
				//
				// so lets build that:
				InstructionList finallyBlock = new InstructionList();
				finallyBlock.append(InstructionFactory.createLoad(Type.getType(java.lang.Class.class), slotForLockObject));
				finallyBlock.append(InstructionConstants.MONITOREXIT);
				finallyBlock.append(InstructionConstants.ATHROW);

				// finally -> E1
				// | GETSTATIC java.lang.System.out Ljava/io/PrintStream; (line
				// 21)
				// | LDC "hello"
				// | INVOKEVIRTUAL java.io.PrintStream.println
				// (Ljava/lang/String;)V
				// | ALOAD_1 (line 20)
				// | MONITOREXIT
				// finally -> E1
				// GOTO L0
				// finally -> E1
				// | E1: ALOAD_1
				// | MONITOREXIT
				// finally -> E1
				// ATHROW
				// L0: RETURN (line 23)

				// search for 'returns' and make them jump to the
				// aload_<n>,monitorexit
				InstructionHandle walker = body.getStart();
				List<InstructionHandle> rets = new ArrayList<InstructionHandle>();
				while (walker != null) {
					if (walker.getInstruction().isReturnInstruction()) {
						rets.add(walker);
					}
					walker = walker.getNext();
				}
				if (!rets.isEmpty()) {
					// need to ensure targeters for 'return' now instead target
					// the load instruction
					// (so we never jump over the monitorexit logic)

					for (Iterator<InstructionHandle> iter = rets.iterator(); iter.hasNext();) {
						InstructionHandle element = iter.next();
						InstructionList monitorExitBlock = new InstructionList();
						monitorExitBlock.append(InstructionFactory.createLoad(enclosingClassType, slotForLockObject));
						monitorExitBlock.append(InstructionConstants.MONITOREXIT);
						// monitorExitBlock.append(Utility.copyInstruction(element
						// .getInstruction()));
						// element.setInstruction(InstructionFactory.createLoad(
						// classType,slotForThis));
						InstructionHandle monitorExitBlockStart = body.insert(element, monitorExitBlock);

						// now move the targeters from the RET to the start of
						// the monitorexit block
						for (InstructionTargeter targeter : element.getTargetersCopy()) {
							// what kinds are there?
							if (targeter instanceof LocalVariableTag) {
								// ignore
							} else if (targeter instanceof LineNumberTag) {
								// ignore
								// } else if (targeter instanceof
								// InstructionBranch &&
								// ((InstructionBranch)targeter).isGoto()) {
								// // move it...
								// targeter.updateTarget(element,
								// monitorExitBlockStart);
							} else if (targeter instanceof InstructionBranch) {
								// move it
								targeter.updateTarget(element, monitorExitBlockStart);
							} else {
								throw new BCException("Unexpected targeter encountered during transform: " + targeter);
							}
						}
					}
				}

				// now the magic, putting the finally block around the code
				InstructionHandle finallyStart = finallyBlock.getStart();

				InstructionHandle tryPosition = body.getStart();
				InstructionHandle catchPosition = body.getEnd();
				body.insert(body.getStart(), prepend); // now we can put the
				// monitorenter stuff on
				synchronizedMethod.getBody().append(finallyBlock);
				synchronizedMethod.addExceptionHandler(tryPosition, catchPosition, finallyStart, null/* ==finally */, false);
				synchronizedMethod.addExceptionHandler(finallyStart, finallyStart.getNext(), finallyStart, null, false);
			} else {

				// TRANSFORMING STATIC METHOD ON PRE JAVA5

				// Hideous nightmare, class literal references prior to Java5

				// YIKES! this is just the code for MONITORENTER !
				// 0: getstatic #59; //Field class$1:Ljava/lang/Class;
				// 3: dup
				// 4: ifnonnull 32
				// 7: pop
				// try
				// 8: ldc #61; //String java.lang.String
				// 10: invokestatic #44; //Method
				// java/lang/Class.forName:(Ljava/lang/String;)Ljava/lang/Class;
				// 13: dup
				// catch
				// 14: putstatic #59; //Field class$1:Ljava/lang/Class;
				// 17: goto 32
				// 20: new #46; //class java/lang/NoClassDefFoundError
				// 23: dup_x1
				// 24: swap
				// 25: invokevirtual #52; //Method
				// java/lang/Throwable.getMessage:()Ljava/lang/String;
				// 28: invokespecial #54; //Method
				// java/lang/NoClassDefFoundError."<init>":(Ljava/lang/String;)V
				// 31: athrow
				// 32: dup <-- partTwo (branch target)
				// 33: astore_0
				// 34: monitorenter
				//
				// plus exceptiontable entry!
				// 8 13 20 Class java/lang/ClassNotFoundException
				Type classType = BcelWorld.makeBcelType(synchronizedMethod.getEnclosingClass().getType());
				Type clazzType = Type.getType(Class.class);

				InstructionList parttwo = new InstructionList();
				parttwo.append(InstructionFactory.createDup(1));
				int slotForThis = synchronizedMethod.allocateLocal(classType);
				parttwo.append(InstructionFactory.createStore(clazzType, slotForThis)); // ? should be the real type ? String or
				// something?
				parttwo.append(InstructionFactory.MONITORENTER);

				String fieldname = synchronizedMethod.getEnclosingClass().allocateField("class$");
				FieldGen f = new FieldGen(Modifier.STATIC | Modifier.PRIVATE, Type.getType(Class.class), fieldname,
						synchronizedMethod.getEnclosingClass().getConstantPool());
				synchronizedMethod.getEnclosingClass().addField(f, null);

				// 10: invokestatic #44; //Method
				// java/lang/Class.forName:(Ljava/lang/String;)Ljava/lang/Class;
				// 13: dup
				// 14: putstatic #59; //Field class$1:Ljava/lang/Class;
				// 17: goto 32
				// 20: new #46; //class java/lang/NoClassDefFoundError
				// 23: dup_x1
				// 24: swap
				// 25: invokevirtual #52; //Method
				// java/lang/Throwable.getMessage:()Ljava/lang/String;
				// 28: invokespecial #54; //Method
				// java/lang/NoClassDefFoundError."<init>":(Ljava/lang/String;)V
				// 31: athrow
				String name = synchronizedMethod.getEnclosingClass().getName();

				prepend.append(fact.createGetStatic(name, fieldname, Type.getType(Class.class)));
				prepend.append(InstructionFactory.createDup(1));
				prepend.append(InstructionFactory.createBranchInstruction(Constants.IFNONNULL, parttwo.getStart()));
				prepend.append(InstructionFactory.POP);

				prepend.append(fact.createConstant(name));
				InstructionHandle tryInstruction = prepend.getEnd();
				prepend.append(fact.createInvoke("java.lang.Class", "forName", clazzType,
						new Type[] { Type.getType(String.class) }, Constants.INVOKESTATIC));
				InstructionHandle catchInstruction = prepend.getEnd();
				prepend.append(InstructionFactory.createDup(1));

				prepend.append(fact.createPutStatic(synchronizedMethod.getEnclosingClass().getType().getName(), fieldname,
						Type.getType(Class.class)));
				prepend.append(InstructionFactory.createBranchInstruction(Constants.GOTO, parttwo.getStart()));

				// start of catch block
				InstructionList catchBlockForLiteralLoadingFail = new InstructionList();
				catchBlockForLiteralLoadingFail.append(fact.createNew((ObjectType) Type.getType(NoClassDefFoundError.class)));
				catchBlockForLiteralLoadingFail.append(InstructionFactory.createDup_1(1));
				catchBlockForLiteralLoadingFail.append(InstructionFactory.SWAP);
				catchBlockForLiteralLoadingFail.append(fact.createInvoke("java.lang.Throwable", "getMessage",
						Type.getType(String.class), new Type[] {}, Constants.INVOKEVIRTUAL));
				catchBlockForLiteralLoadingFail.append(fact.createInvoke("java.lang.NoClassDefFoundError", "<init>", Type.VOID,
						new Type[] { Type.getType(String.class) }, Constants.INVOKESPECIAL));
				catchBlockForLiteralLoadingFail.append(InstructionFactory.ATHROW);
				InstructionHandle catchBlockStart = catchBlockForLiteralLoadingFail.getStart();
				prepend.append(catchBlockForLiteralLoadingFail);
				prepend.append(parttwo);
				// MONITORENTER
				// pseudocode: load up 'this' (var0), dup it, store it in a new
				// local var (for use with monitorexit) and call
				// monitorenter:
				// ALOAD_0, DUP, ASTORE_<n>, MONITORENTER
				// prepend.append(InstructionFactory.createLoad(classType,0));
				// prepend.append(InstructionFactory.createDup(1));
				// int slotForThis =
				// synchronizedMethod.allocateLocal(classType);
				// prepend.append(InstructionFactory.createStore(classType,
				// slotForThis));
				// prepend.append(InstructionFactory.MONITORENTER);

				// MONITOREXIT
				// here be dragons

				// We basically need to wrap the code from the method in a
				// finally block that
				// will ensure monitorexit is called. Content on the finally
				// block seems to
				// be always:
				//
				// E1: ALOAD_1
				// MONITOREXIT
				// ATHROW
				//
				// so lets build that:
				InstructionList finallyBlock = new InstructionList();
				finallyBlock.append(InstructionFactory.createLoad(Type.getType(java.lang.Class.class), slotForThis));
				finallyBlock.append(InstructionConstants.MONITOREXIT);
				finallyBlock.append(InstructionConstants.ATHROW);

				// finally -> E1
				// | GETSTATIC java.lang.System.out Ljava/io/PrintStream; (line
				// 21)
				// | LDC "hello"
				// | INVOKEVIRTUAL java.io.PrintStream.println
				// (Ljava/lang/String;)V
				// | ALOAD_1 (line 20)
				// | MONITOREXIT
				// finally -> E1
				// GOTO L0
				// finally -> E1
				// | E1: ALOAD_1
				// | MONITOREXIT
				// finally -> E1
				// ATHROW
				// L0: RETURN (line 23)
				// frameEnv.put(donorFramePos, thisSlot);

				// search for 'returns' and make them to the
				// aload_<n>,monitorexit
				InstructionHandle walker = body.getStart();
				List<InstructionHandle> rets = new ArrayList<InstructionHandle>();
				while (walker != null) { // !walker.equals(body.getEnd())) {
					if (walker.getInstruction().isReturnInstruction()) {
						rets.add(walker);
					}
					walker = walker.getNext();
				}
				if (rets.size() > 0) {
					// need to ensure targeters for 'return' now instead target
					// the load instruction
					// (so we never jump over the monitorexit logic)

					for (InstructionHandle ret : rets) {
						// System.err.println("Adding monitor exit block at "+
						// element);
						InstructionList monitorExitBlock = new InstructionList();
						monitorExitBlock.append(InstructionFactory.createLoad(classType, slotForThis));
						monitorExitBlock.append(InstructionConstants.MONITOREXIT);
						// monitorExitBlock.append(Utility.copyInstruction(element
						// .getInstruction()));
						// element.setInstruction(InstructionFactory.createLoad(
						// classType,slotForThis));
						InstructionHandle monitorExitBlockStart = body.insert(ret, monitorExitBlock);

						// now move the targeters from the RET to the start of
						// the monitorexit block
						for (InstructionTargeter targeter : ret.getTargetersCopy()) {
							// what kinds are there?
							if (targeter instanceof LocalVariableTag) {
								// ignore
							} else if (targeter instanceof LineNumberTag) {
								// ignore
								// } else if (targeter instanceof GOTO ||
								// targeter instanceof GOTO_W) {
								// // move it...
								// targeter.updateTarget(element,
								// monitorExitBlockStart);
							} else if (targeter instanceof InstructionBranch) {
								// move it
								targeter.updateTarget(ret, monitorExitBlockStart);
							} else {
								throw new BCException("Unexpected targeter encountered during transform: " + targeter);
							}
						}
					}
				}
				// body =
				// rewriteWithMonitorExitCalls(body,fact,true,slotForThis,
				// classType);
				// synchronizedMethod.setBody(body);

				// now the magic, putting the finally block around the code
				InstructionHandle finallyStart = finallyBlock.getStart();

				InstructionHandle tryPosition = body.getStart();
				InstructionHandle catchPosition = body.getEnd();
				body.insert(body.getStart(), prepend); // now we can put the
				// monitorenter stuff on

				synchronizedMethod.getBody().append(finallyBlock);
				synchronizedMethod.addExceptionHandler(tryPosition, catchPosition, finallyStart, null/* ==finally */, false);
				synchronizedMethod.addExceptionHandler(tryInstruction, catchInstruction, catchBlockStart,
						(ObjectType) Type.getType(ClassNotFoundException.class), true);
				synchronizedMethod.addExceptionHandler(finallyStart, finallyStart.getNext(), finallyStart, null, false);
			}
		} else {

			// TRANSFORMING NON STATIC METHOD
			Type classType = BcelWorld.makeBcelType(synchronizedMethod.getEnclosingClass().getType());
			// MONITORENTER
			// pseudocode: load up 'this' (var0), dup it, store it in a new
			// local var (for use with monitorexit) and call
			// monitorenter:
			// ALOAD_0, DUP, ASTORE_<n>, MONITORENTER
			prepend.append(InstructionFactory.createLoad(classType, 0));
			prepend.append(InstructionFactory.createDup(1));
			int slotForThis = synchronizedMethod.allocateLocal(classType);
			prepend.append(InstructionFactory.createStore(classType, slotForThis));
			prepend.append(InstructionFactory.MONITORENTER);
			// body.insert(body.getStart(),prepend);

			// MONITOREXIT

			// We basically need to wrap the code from the method in a finally
			// block that
			// will ensure monitorexit is called. Content on the finally block
			// seems to
			// be always:
			//
			// E1: ALOAD_1
			// MONITOREXIT
			// ATHROW
			//
			// so lets build that:
			InstructionList finallyBlock = new InstructionList();
			finallyBlock.append(InstructionFactory.createLoad(classType, slotForThis));
			finallyBlock.append(InstructionConstants.MONITOREXIT);
			finallyBlock.append(InstructionConstants.ATHROW);

			// finally -> E1
			// | GETSTATIC java.lang.System.out Ljava/io/PrintStream; (line 21)
			// | LDC "hello"
			// | INVOKEVIRTUAL java.io.PrintStream.println (Ljava/lang/String;)V
			// | ALOAD_1 (line 20)
			// | MONITOREXIT
			// finally -> E1
			// GOTO L0
			// finally -> E1
			// | E1: ALOAD_1
			// | MONITOREXIT
			// finally -> E1
			// ATHROW
			// L0: RETURN (line 23)
			// frameEnv.put(donorFramePos, thisSlot);

			// search for 'returns' and make them to the aload_<n>,monitorexit
			InstructionHandle walker = body.getStart();
			List<InstructionHandle> rets = new ArrayList<InstructionHandle>();
			while (walker != null) { // !walker.equals(body.getEnd())) {
				if (walker.getInstruction().isReturnInstruction()) {
					rets.add(walker);
				}
				walker = walker.getNext();
			}
			if (!rets.isEmpty()) {
				// need to ensure targeters for 'return' now instead target the
				// load instruction
				// (so we never jump over the monitorexit logic)

				for (Iterator<InstructionHandle> iter = rets.iterator(); iter.hasNext();) {
					InstructionHandle element = iter.next();
					// System.err.println("Adding monitor exit block at "+element
					// );
					InstructionList monitorExitBlock = new InstructionList();
					monitorExitBlock.append(InstructionFactory.createLoad(classType, slotForThis));
					monitorExitBlock.append(InstructionConstants.MONITOREXIT);
					// monitorExitBlock.append(Utility.copyInstruction(element.
					// getInstruction()));
					// element.setInstruction(InstructionFactory.createLoad(
					// classType,slotForThis));
					InstructionHandle monitorExitBlockStart = body.insert(element, monitorExitBlock);

					// now move the targeters from the RET to the start of the
					// monitorexit block
					for (InstructionTargeter targeter : element.getTargetersCopy()) {
						// what kinds are there?
						if (targeter instanceof LocalVariableTag) {
							// ignore
						} else if (targeter instanceof LineNumberTag) {
							// ignore
							// } else if (targeter instanceof GOTO ||
							// targeter instanceof GOTO_W) {
							// // move it...
							// targeter.updateTarget(element,
							// monitorExitBlockStart);
						} else if (targeter instanceof InstructionBranch) {
							// move it
							targeter.updateTarget(element, monitorExitBlockStart);
						} else {
							throw new BCException("Unexpected targeter encountered during transform: " + targeter);
						}
					}
				}
			}

			// now the magic, putting the finally block around the code
			InstructionHandle finallyStart = finallyBlock.getStart();

			InstructionHandle tryPosition = body.getStart();
			InstructionHandle catchPosition = body.getEnd();
			body.insert(body.getStart(), prepend); // now we can put the
			// monitorenter stuff on
			synchronizedMethod.getBody().append(finallyBlock);
			synchronizedMethod.addExceptionHandler(tryPosition, catchPosition, finallyStart, null/* ==finally */, false);
			synchronizedMethod.addExceptionHandler(finallyStart, finallyStart.getNext(), finallyStart, null, false);
			// also the exception handling for the finally block jumps to itself

			// max locals will already have been modified in the allocateLocal()
			// call

			// synchronized bit is removed on LazyMethodGen.pack()
		}

		// gonna have to go through and change all aload_0s to load the var from
		// a variable,
		// going to add a new variable for the this var

		if (trace.isTraceEnabled()) {
			trace.exit("transformSynchronizedMethod");
		}
	}

	/**
	 * generate the instructions to be inlined.
	 * 
	 * @param donor the method from which we will copy (and adjust frame and jumps) instructions.
	 * @param recipient the method the instructions will go into. Used to get the frame size so we can allocate new frame locations
	 *        for locals in donor.
	 * @param frameEnv an environment to map from donor frame to recipient frame, initially populated with argument locations.
	 * @param fact an instruction factory for recipient
	 */
	static InstructionList genInlineInstructions(LazyMethodGen donor, LazyMethodGen recipient, IntMap frameEnv,
			InstructionFactory fact, boolean keepReturns) {
		InstructionList footer = new InstructionList();
		InstructionHandle end = footer.append(InstructionConstants.NOP);

		InstructionList ret = new InstructionList();
		InstructionList sourceList = donor.getBody();

		Map<InstructionHandle, InstructionHandle> srcToDest = new HashMap<InstructionHandle, InstructionHandle>();
		ConstantPool donorCpg = donor.getEnclosingClass().getConstantPool();
		ConstantPool recipientCpg = recipient.getEnclosingClass().getConstantPool();

		boolean isAcrossClass = donorCpg != recipientCpg;

		// first pass: copy the instructions directly, populate the srcToDest
		// map,
		// fix frame instructions
		for (InstructionHandle src = sourceList.getStart(); src != null; src = src.getNext()) {
			Instruction fresh = Utility.copyInstruction(src.getInstruction());
			InstructionHandle dest;

			// OPTIMIZE optimize this stuff?
			if (fresh.isConstantPoolInstruction()) {
				// need to reset index to go to new constant pool. This is
				// totally
				// a computation leak... we're testing this LOTS of times. Sigh.
				if (isAcrossClass) {
					InstructionCP cpi = (InstructionCP) fresh;
					cpi.setIndex(recipientCpg.addConstant(donorCpg.getConstant(cpi.getIndex()), donorCpg));
				}
			}
			if (src.getInstruction() == Range.RANGEINSTRUCTION) {
				dest = ret.append(Range.RANGEINSTRUCTION);
			} else if (fresh.isReturnInstruction()) {
				if (keepReturns) {
					dest = ret.append(fresh);
				} else {
					dest = ret.append(InstructionFactory.createBranchInstruction(Constants.GOTO, end));
				}
			} else if (fresh instanceof InstructionBranch) {
				dest = ret.append((InstructionBranch) fresh);
			} else if (fresh.isLocalVariableInstruction() || fresh instanceof RET) {

				// IndexedInstruction indexed = (IndexedInstruction) fresh;
				int oldIndex = fresh.getIndex();
				int freshIndex;
				if (!frameEnv.hasKey(oldIndex)) {
					freshIndex = recipient.allocateLocal(2);
					frameEnv.put(oldIndex, freshIndex);
				} else {
					freshIndex = frameEnv.get(oldIndex);
				}
				if (fresh instanceof RET) {
					fresh.setIndex(freshIndex);
				} else {
					fresh = ((InstructionLV) fresh).setIndexAndCopyIfNecessary(freshIndex);
				}
				dest = ret.append(fresh);
			} else {
				dest = ret.append(fresh);
			}
			srcToDest.put(src, dest);
		}

		// second pass: retarget branch instructions, copy ranges and tags
		Map<Tag, Tag> tagMap = new HashMap<Tag, Tag>();
		Map<BcelShadow, BcelShadow> shadowMap = new HashMap<BcelShadow, BcelShadow>();
		for (InstructionHandle dest = ret.getStart(), src = sourceList.getStart(); dest != null; dest = dest.getNext(), src = src
				.getNext()) {
			Instruction inst = dest.getInstruction();

			// retarget branches
			if (inst instanceof InstructionBranch) {
				InstructionBranch branch = (InstructionBranch) inst;
				InstructionHandle oldTarget = branch.getTarget();
				InstructionHandle newTarget = srcToDest.get(oldTarget);
				if (newTarget == null) {
					// assert this is a GOTO
					// this was a return instruction we previously replaced
				} else {
					branch.setTarget(newTarget);
					if (branch instanceof InstructionSelect) {
						InstructionSelect select = (InstructionSelect) branch;
						InstructionHandle[] oldTargets = select.getTargets();
						for (int k = oldTargets.length - 1; k >= 0; k--) {
							select.setTarget(k, srcToDest.get(oldTargets[k]));
						}
					}
				}
			}

			// copy over tags and range attributes

			Iterator<InstructionTargeter> tIter = src.getTargeters().iterator();
			while (tIter.hasNext()) {
				InstructionTargeter old = tIter.next();
				if (old instanceof Tag) {
					Tag oldTag = (Tag) old;
					Tag fresh = tagMap.get(oldTag);
					if (fresh == null) {
						fresh = oldTag.copy();
						if (old instanceof LocalVariableTag) {
							// LocalVariable
							LocalVariableTag lvTag = (LocalVariableTag) old;
							LocalVariableTag lvTagFresh = (LocalVariableTag) fresh;
							if (lvTag.getSlot() == 0) {
								fresh = new LocalVariableTag(lvTag.getRealType().getSignature(), "ajc$aspectInstance",
										frameEnv.get(lvTag.getSlot()), 0);
							} else {
								// // Do not move it - when copying the code from the aspect to the affected target, 'this' is
								// // going to change from aspect to affected type. So just fix the type
								// System.out.println("For local variable tag at instruction " + src + " changing slot from "
								// + lvTag.getSlot() + " > " + frameEnv.get(lvTag.getSlot()));
								lvTagFresh.updateSlot(frameEnv.get(lvTag.getSlot()));
							}
						}
						tagMap.put(oldTag, fresh);
					}
					dest.addTargeter(fresh);
				} else if (old instanceof ExceptionRange) {
					ExceptionRange er = (ExceptionRange) old;
					if (er.getStart() == src) {
						ExceptionRange freshEr = new ExceptionRange(recipient.getBody(), er.getCatchType(), er.getPriority());
						freshEr.associateWithTargets(dest, srcToDest.get(er.getEnd()), srcToDest.get(er.getHandler()));
					}
				} else if (old instanceof ShadowRange) {
					ShadowRange oldRange = (ShadowRange) old;
					if (oldRange.getStart() == src) {
						BcelShadow oldShadow = oldRange.getShadow();
						BcelShadow freshEnclosing = oldShadow.getEnclosingShadow() == null ? null : (BcelShadow) shadowMap
								.get(oldShadow.getEnclosingShadow());
						BcelShadow freshShadow = oldShadow.copyInto(recipient, freshEnclosing);
						ShadowRange freshRange = new ShadowRange(recipient.getBody());
						freshRange.associateWithShadow(freshShadow);
						freshRange.associateWithTargets(dest, srcToDest.get(oldRange.getEnd()));
						shadowMap.put(oldShadow, freshShadow); // oldRange, freshRange
						// recipient.matchedShadows.add(freshShadow);
						// XXX should go through the NEW copied shadow and
						// update
						// the thisVar, targetVar, and argsVar
						// ??? Might want to also go through at this time and
						// add
						// "extra" vars to the shadow.
					}
				}
			}
		}
		if (!keepReturns) {
			ret.append(footer);
		}
		return ret;
	}

	// static InstructionList rewriteWithMonitorExitCalls(InstructionList
	// sourceList,InstructionFactory fact,boolean keepReturns,int
	// monitorVarSlot,Type monitorVarType)
	// {
	// InstructionList footer = new InstructionList();
	// InstructionHandle end = footer.append(InstructionConstants.NOP);
	//
	// InstructionList newList = new InstructionList();
	//
	// Map srcToDest = new HashMap();
	//
	// // first pass: copy the instructions directly, populate the srcToDest
	// map,
	// // fix frame instructions
	// for (InstructionHandle src = sourceList.getStart(); src != null; src =
	// src.getNext()) {
	// Instruction fresh = Utility.copyInstruction(src.getInstruction());
	// InstructionHandle dest;
	// if (src.getInstruction() == Range.RANGEINSTRUCTION) {
	// dest = newList.append(Range.RANGEINSTRUCTION);
	// } else if (fresh.isReturnInstruction()) {
	// if (keepReturns) {
	// newList.append(InstructionFactory.createLoad(monitorVarType,monitorVarSlot
	// ));
	// newList.append(InstructionConstants.MONITOREXIT);
	// dest = newList.append(fresh);
	// } else {
	// dest =
	// newList.append(InstructionFactory.createBranchInstruction(Constants.GOTO,
	// end));
	// }
	// } else if (fresh instanceof InstructionBranch) {
	// dest = newList.append((InstructionBranch) fresh);
	// } else if (
	// fresh.isLocalVariableInstruction() || fresh instanceof RET) {
	// //IndexedInstruction indexed = (IndexedInstruction) fresh;
	// int oldIndex = fresh.getIndex();
	// int freshIndex;
	// // if (!frameEnv.hasKey(oldIndex)) {
	// // freshIndex = recipient.allocateLocal(2);
	// // frameEnv.put(oldIndex, freshIndex);
	// // } else {
	// freshIndex = oldIndex;//frameEnv.get(oldIndex);
	// // }
	// if (fresh instanceof RET) {
	// fresh.setIndex(freshIndex);
	// } else {
	// fresh = ((InstructionLV)fresh).setIndexAndCopyIfNecessary(freshIndex);
	// }
	// dest = newList.append(fresh);
	// } else {
	// dest = newList.append(fresh);
	// }
	// srcToDest.put(src, dest);
	// }
	//
	// // second pass: retarget branch instructions, copy ranges and tags
	// Map tagMap = new HashMap();
	// for (InstructionHandle dest = newList.getStart(), src =
	// sourceList.getStart();
	// dest != null;
	// dest = dest.getNext(), src = src.getNext()) {
	// Instruction inst = dest.getInstruction();
	//
	// // retarget branches
	// if (inst instanceof InstructionBranch) {
	// InstructionBranch branch = (InstructionBranch) inst;
	// InstructionHandle oldTarget = branch.getTarget();
	// InstructionHandle newTarget =
	// (InstructionHandle) srcToDest.get(oldTarget);
	// if (newTarget == null) {
	// // assert this is a GOTO
	// // this was a return instruction we previously replaced
	// } else {
	// branch.setTarget(newTarget);
	// if (branch instanceof InstructionSelect) {
	// InstructionSelect select = (InstructionSelect) branch;
	// InstructionHandle[] oldTargets = select.getTargets();
	// for (int k = oldTargets.length - 1; k >= 0; k--) {
	// select.setTarget(
	// k,
	// (InstructionHandle) srcToDest.get(oldTargets[k]));
	// }
	// }
	// }
	// }
	//
	// //copy over tags and range attributes
	// Iterator tIter = src.getTargeters().iterator();
	//
	// while (tIter.hasNext()) {
	// InstructionTargeter old = (InstructionTargeter)tIter.next();
	// if (old instanceof Tag) {
	// Tag oldTag = (Tag) old;
	// Tag fresh = (Tag) tagMap.get(oldTag);
	// if (fresh == null) {
	// fresh = oldTag.copy();
	// tagMap.put(oldTag, fresh);
	// }
	// dest.addTargeter(fresh);
	// } else if (old instanceof ExceptionRange) {
	// ExceptionRange er = (ExceptionRange) old;
	// if (er.getStart() == src) {
	// ExceptionRange freshEr =
	// new ExceptionRange(newList/*recipient.getBody()*/,er.getCatchType(),er.
	// getPriority());
	// freshEr.associateWithTargets(
	// dest,
	// (InstructionHandle)srcToDest.get(er.getEnd()),
	// (InstructionHandle)srcToDest.get(er.getHandler()));
	// }
	// }
	// /*else if (old instanceof ShadowRange) {
	// ShadowRange oldRange = (ShadowRange) old;
	// if (oldRange.getStart() == src) {
	// BcelShadow oldShadow = oldRange.getShadow();
	// BcelShadow freshEnclosing =
	// oldShadow.getEnclosingShadow() == null
	// ? null
	// : (BcelShadow) shadowMap.get(oldShadow.getEnclosingShadow());
	// BcelShadow freshShadow =
	// oldShadow.copyInto(recipient, freshEnclosing);
	// ShadowRange freshRange = new ShadowRange(recipient.getBody());
	// freshRange.associateWithShadow(freshShadow);
	// freshRange.associateWithTargets(
	// dest,
	// (InstructionHandle) srcToDest.get(oldRange.getEnd()));
	// shadowMap.put(oldRange, freshRange);
	// //recipient.matchedShadows.add(freshShadow);
	// // XXX should go through the NEW copied shadow and update
	// // the thisVar, targetVar, and argsVar
	// // ??? Might want to also go through at this time and add
	// // "extra" vars to the shadow.
	// }
	// }*/
	// }
	// }
	// if (!keepReturns) newList.append(footer);
	// return newList;
	// }

	/**
	 * generate the argument stores in preparation for inlining.
	 * 
	 * @param donor the method we will inline from. Used to get the signature.
	 * @param recipient the method we will inline into. Used to get the frame size so we can allocate fresh locations.
	 * @param frameEnv an empty environment we populate with a map from donor frame to recipient frame.
	 * @param fact an instruction factory for recipient
	 */
	private static InstructionList genArgumentStores(LazyMethodGen donor, LazyMethodGen recipient, IntMap frameEnv,
			InstructionFactory fact) {
		InstructionList ret = new InstructionList();

		int donorFramePos = 0;

		// writing ret back to front because we're popping.
		if (!donor.isStatic()) {
			int targetSlot = recipient.allocateLocal(Type.OBJECT);
			ret.insert(InstructionFactory.createStore(Type.OBJECT, targetSlot));
			frameEnv.put(donorFramePos, targetSlot);
			donorFramePos += 1;
		}
		Type[] argTypes = donor.getArgumentTypes();
		for (int i = 0, len = argTypes.length; i < len; i++) {
			Type argType = argTypes[i];
			int argSlot = recipient.allocateLocal(argType);
			ret.insert(InstructionFactory.createStore(argType, argSlot));
			frameEnv.put(donorFramePos, argSlot);
			donorFramePos += argType.getSize();
		}
		return ret;
	}

	/**
	 * get a called method: Assumes the called method is in this class, and the reference to it is exact (a la INVOKESPECIAL).
	 * 
	 * @param ih The InvokeInstruction instructionHandle pointing to the called method.
	 */
	private LazyMethodGen getCalledMethod(InstructionHandle ih) {
		InvokeInstruction inst = (InvokeInstruction) ih.getInstruction();

		String methodName = inst.getName(cpg);
		String signature = inst.getSignature(cpg);

		return clazz.getLazyMethodGen(methodName, signature);
	}

	private void weaveInAddedMethods() {
		Collections.sort(addedLazyMethodGens, new Comparator<LazyMethodGen>() {
			public int compare(LazyMethodGen aa, LazyMethodGen bb) {
				int i = aa.getName().compareTo(bb.getName());
				if (i != 0) {
					return i;
				}
				return aa.getSignature().compareTo(bb.getSignature());
			}
		});

		for (LazyMethodGen addedMember : addedLazyMethodGens) {
			clazz.addMethodGen(addedMember);
		}
	}

	// void addPerSingletonField(Member field) {
	// ObjectType aspectType = (ObjectType)
	// BcelWorld.makeBcelType(field.getReturnType());
	// String aspectName = field.getReturnType().getName();
	//
	// LazyMethodGen clinit = clazz.getStaticInitializer();
	// InstructionList setup = new InstructionList();
	// InstructionFactory fact = clazz.getFactory();
	//
	// setup.append(fact.createNew(aspectType));
	// setup.append(InstructionFactory.createDup(1));
	// setup.append(fact.createInvoke(aspectName, "<init>", Type.VOID, new
	// Type[0], Constants.INVOKESPECIAL));
	// setup.append(fact.createFieldAccess(aspectName, field.getName(),
	// aspectType, Constants.PUTSTATIC));
	// clinit.getBody().insert(setup);
	// }

	/**
	 * Returns null if this is not a Java constructor, and then we won't weave into it at all
	 */
	private InstructionHandle findSuperOrThisCall(LazyMethodGen mg) {
		int depth = 1;
		InstructionHandle start = mg.getBody().getStart();
		while (true) {
			if (start == null) {
				return null;
			}

			Instruction inst = start.getInstruction();
			if (inst.opcode == Constants.INVOKESPECIAL && ((InvokeInstruction) inst).getName(cpg).equals("<init>")) {
				depth--;
				if (depth == 0) {
					return start;
				}
			} else if (inst.opcode == Constants.NEW) {
				depth++;
			}
			start = start.getNext();
		}
	}

	// ----

	private boolean match(LazyMethodGen mg) {
		BcelShadow enclosingShadow;
		List<BcelShadow> shadowAccumulator = new ArrayList<BcelShadow>();
		boolean isOverweaving = world.isOverWeaving();
		boolean startsAngly = mg.getName().charAt(0) == '<';
		// we want to match ajsynthetic constructors...
		if (startsAngly && mg.getName().equals("<init>")) {
			return matchInit(mg, shadowAccumulator);
		} else if (!shouldWeaveBody(mg)) {
			return false;
		} else {
			if (startsAngly && mg.getName().equals("<clinit>")) {
				// clinitShadow =
				enclosingShadow = BcelShadow.makeStaticInitialization(world, mg);
				// System.err.println(enclosingShadow);
			} else if (mg.isAdviceMethod()) {
				enclosingShadow = BcelShadow.makeAdviceExecution(world, mg);
			} else {
				AjAttribute.EffectiveSignatureAttribute effective = mg.getEffectiveSignature();
				if (effective == null) {
					// Don't want ajc$preClinit to be considered for matching
					if (isOverweaving && mg.getName().startsWith(NameMangler.PREFIX)) {
						return false;
					}
					enclosingShadow = BcelShadow.makeMethodExecution(world, mg, !canMatchBodyShadows);
				} else if (effective.isWeaveBody()) {
					ResolvedMember rm = effective.getEffectiveSignature();

					// Annotations for things with effective signatures are
					// never stored in the effective
					// signature itself - we have to hunt for them. Storing them
					// in the effective signature
					// would mean keeping two sets up to date (no way!!)

					fixParameterNamesForResolvedMember(rm, mg.getMemberView());
					fixAnnotationsForResolvedMember(rm, mg.getMemberView());

					enclosingShadow = BcelShadow.makeShadowForMethod(world, mg, effective.getShadowKind(), rm);
				} else {
					return false;
				}
			}

			if (canMatchBodyShadows) {
				for (InstructionHandle h = mg.getBody().getStart(); h != null; h = h.getNext()) {
					match(mg, h, enclosingShadow, shadowAccumulator);
				}
			}
			// FIXME asc change from string match if we can, rather brittle.
			// this check actually prevents field-exec jps
			if (canMatch(enclosingShadow.getKind())
					&& !(mg.getName().charAt(0) == 'a' && mg.getName().startsWith("ajc$interFieldInit"))) {
				if (match(enclosingShadow, shadowAccumulator)) {
					enclosingShadow.init();
				}
			}
			mg.matchedShadows = shadowAccumulator;
			return !shadowAccumulator.isEmpty();
		}
	}

	private boolean matchInit(LazyMethodGen mg, List<BcelShadow> shadowAccumulator) {
		BcelShadow enclosingShadow;
		// XXX the enclosing join point is wrong for things before ignoreMe.
		InstructionHandle superOrThisCall = findSuperOrThisCall(mg);

		// we don't walk bodies of things where it's a wrong constructor thingie
		if (superOrThisCall == null) {
			return false;
		}

		enclosingShadow = BcelShadow.makeConstructorExecution(world, mg, superOrThisCall);
		if (mg.getEffectiveSignature() != null) {
			enclosingShadow.setMatchingSignature(mg.getEffectiveSignature().getEffectiveSignature());
		}

		// walk the body
		boolean beforeSuperOrThisCall = true;
		if (shouldWeaveBody(mg)) {
			if (canMatchBodyShadows) {
				for (InstructionHandle h = mg.getBody().getStart(); h != null; h = h.getNext()) {
					if (h == superOrThisCall) {
						beforeSuperOrThisCall = false;
						continue;
					}
					match(mg, h, beforeSuperOrThisCall ? null : enclosingShadow, shadowAccumulator);
				}
			}
			if (canMatch(Shadow.ConstructorExecution)) {
				match(enclosingShadow, shadowAccumulator);
			}
		}

		// XXX we don't do pre-inits of interfaces

		// now add interface inits
		if (!isThisCall(superOrThisCall)) {
			InstructionHandle curr = enclosingShadow.getRange().getStart();
			for (Iterator<IfaceInitList> i = addedSuperInitializersAsList.iterator(); i.hasNext();) {
				IfaceInitList l = i.next();

				Member ifaceInitSig = AjcMemberMaker.interfaceConstructor(l.onType);

				BcelShadow initShadow = BcelShadow.makeIfaceInitialization(world, mg, ifaceInitSig);

				// insert code in place
				InstructionList inits = genInitInstructions(l.list, false);
				if (match(initShadow, shadowAccumulator) || !inits.isEmpty()) {
					initShadow.initIfaceInitializer(curr);
					initShadow.getRange().insert(inits, Range.OutsideBefore);
				}
			}

			// now we add our initialization code
			InstructionList inits = genInitInstructions(addedThisInitializers, false);
			enclosingShadow.getRange().insert(inits, Range.OutsideBefore);
		}

		// actually, you only need to inline the self constructors that are
		// in a particular group (partition the constructors into groups where
		// members
		// call or are called only by those in the group). Then only inline
		// constructors
		// in groups where at least one initialization jp matched. Future work.
		boolean addedInitialization = match(BcelShadow.makeUnfinishedInitialization(world, mg), initializationShadows);
		addedInitialization |= match(BcelShadow.makeUnfinishedPreinitialization(world, mg), initializationShadows);
		mg.matchedShadows = shadowAccumulator;
		return addedInitialization || !shadowAccumulator.isEmpty();
	}

	private boolean shouldWeaveBody(LazyMethodGen mg) {
		if (mg.isBridgeMethod()) {
			return false;
		}
		if (mg.isAjSynthetic()) {
			return mg.getName().equals("<clinit>");
		}
		AjAttribute.EffectiveSignatureAttribute a = mg.getEffectiveSignature();
		if (a != null) {
			return a.isWeaveBody();
		}
		return true;
	}

	/**
	 * first sorts the mungers, then gens the initializers in the right order
	 */
	private InstructionList genInitInstructions(List<ConcreteTypeMunger> list, boolean isStatic) {
		list = PartialOrder.sort(list);
		if (list == null) {
			throw new BCException("circularity in inter-types");
		}

		InstructionList ret = new InstructionList();

		for (ConcreteTypeMunger cmunger : list) {
			NewFieldTypeMunger munger = (NewFieldTypeMunger) cmunger.getMunger();
			ResolvedMember initMethod = munger.getInitMethod(cmunger.getAspectType());
			if (!isStatic) {
				ret.append(InstructionConstants.ALOAD_0);
			}
			ret.append(Utility.createInvoke(fact, world, initMethod));
		}
		return ret;
	}

	private void match(LazyMethodGen mg, InstructionHandle ih, BcelShadow enclosingShadow, List<BcelShadow> shadowAccumulator) {
		Instruction i = ih.getInstruction();

		// Exception handlers (pr230817)
		if (canMatch(Shadow.ExceptionHandler) && !Range.isRangeHandle(ih)) {
			Set<InstructionTargeter> targeters = ih.getTargetersCopy();
			// If in Java7 there may be overlapping exception ranges for multi catch - we should recognize that
			for (InstructionTargeter t : targeters) {
				if (t instanceof ExceptionRange) {
					// assert t.getHandler() == ih
					ExceptionRange er = (ExceptionRange) t;
					if (er.getCatchType() == null) {
						continue;
					}
					if (isInitFailureHandler(ih)) {
						return;
					}
					if (!ih.getInstruction().isStoreInstruction() && ih.getInstruction().getOpcode() != Constants.NOP) {
						// If using cobertura, the catch block stats with
						// INVOKESTATIC rather than ASTORE, in order that the ranges
						// for the methodcall and exceptionhandler shadows
						// that occur at this same
						// line, we need to modify the instruction list to
						// split them - adding a
						// NOP before the invokestatic that gets all the targeters
						// that were aimed at the INVOKESTATIC
						mg.getBody().insert(ih, InstructionConstants.NOP);
						InstructionHandle newNOP = ih.getPrev();
						// what about a try..catch that starts at the start
						// of the exception handler? need to only include
						// certain targeters really.
						er.updateTarget(ih, newNOP, mg.getBody());
						for (InstructionTargeter t2 : targeters) {
							newNOP.addTargeter(t2);
						}
						ih.removeAllTargeters();
						match(BcelShadow.makeExceptionHandler(world, er, mg, newNOP, enclosingShadow), shadowAccumulator);
					} else {
						match(BcelShadow.makeExceptionHandler(world, er, mg, ih, enclosingShadow), shadowAccumulator);
					}
				}
			}
		}

		if ((i instanceof FieldInstruction) && (canMatch(Shadow.FieldGet) || canMatch(Shadow.FieldSet))) {
			FieldInstruction fi = (FieldInstruction) i;

			if (fi.opcode == Constants.PUTFIELD || fi.opcode == Constants.PUTSTATIC) {
				// check for sets of constant fields. We first check the
				// previous
				// instruction. If the previous instruction is a LD_WHATEVER
				// (push
				// constant on the stack) then we must resolve the field to
				// determine
				// if it's final. If it is final, then we don't generate a
				// shadow.
				InstructionHandle prevHandle = ih.getPrev();
				Instruction prevI = prevHandle.getInstruction();
				if (Utility.isConstantPushInstruction(prevI)) {
					Member field = BcelWorld.makeFieldJoinPointSignature(clazz, (FieldInstruction) i);
					ResolvedMember resolvedField = field.resolve(world);
					if (resolvedField == null) {
						// we can't find the field, so it's not a join point.
					} else if (Modifier.isFinal(resolvedField.getModifiers())) {
						// it's final, so it's the set of a final constant, so
						// it's
						// not a join point according to 1.0.6 and 1.1.
					} else {
						if (canMatch(Shadow.FieldSet)) {
							matchSetInstruction(mg, ih, enclosingShadow, shadowAccumulator);
						}
					}
				} else {
					if (canMatch(Shadow.FieldSet)) {
						matchSetInstruction(mg, ih, enclosingShadow, shadowAccumulator);
					}
				}
			} else {
				if (canMatch(Shadow.FieldGet)) {
					matchGetInstruction(mg, ih, enclosingShadow, shadowAccumulator);
				}
			}
		} else if (i instanceof InvokeInstruction) {
			InvokeInstruction ii = (InvokeInstruction) i;
			if (ii.getMethodName(clazz.getConstantPool()).equals("<init>")) {
				if (canMatch(Shadow.ConstructorCall)) {
					match(BcelShadow.makeConstructorCall(world, mg, ih, enclosingShadow), shadowAccumulator);
				}
			} else if (ii.opcode == Constants.INVOKESPECIAL) {
				String onTypeName = ii.getClassName(cpg);
				if (onTypeName.equals(mg.getEnclosingClass().getName())) {
					// we are private
					matchInvokeInstruction(mg, ih, ii, enclosingShadow, shadowAccumulator);
				} else {
					// we are a super call, and this is not a join point in
					// AspectJ-1.{0,1}
				}
			} else {
				if (ii.getOpcode()!=Constants.INVOKEDYNAMIC) {
					matchInvokeInstruction(mg, ih, ii, enclosingShadow, shadowAccumulator);
				}
			}
		} else if (world.isJoinpointArrayConstructionEnabled() && i.isArrayCreationInstruction()) {
			if (canMatch(Shadow.ConstructorCall)) {
				if (i.opcode == Constants.ANEWARRAY) {
					// ANEWARRAY arrayInstruction = (ANEWARRAY)i;
					// ObjectType arrayType = i.getLoadClassType(clazz.getConstantPool());
					BcelShadow ctorCallShadow = BcelShadow.makeArrayConstructorCall(world, mg, ih, enclosingShadow);
					match(ctorCallShadow, shadowAccumulator);
				} else if (i.opcode == Constants.NEWARRAY) {
					// NEWARRAY arrayInstruction = (NEWARRAY)i;
					// Type arrayType = i.getType();
					BcelShadow ctorCallShadow = BcelShadow.makeArrayConstructorCall(world, mg, ih, enclosingShadow);
					match(ctorCallShadow, shadowAccumulator);
				} else if (i instanceof MULTIANEWARRAY) {
					// MULTIANEWARRAY arrayInstruction = (MULTIANEWARRAY) i;
					// ObjectType arrayType = arrayInstruction.getLoadClassType(clazz.getConstantPool());
					BcelShadow ctorCallShadow = BcelShadow.makeArrayConstructorCall(world, mg, ih, enclosingShadow);
					match(ctorCallShadow, shadowAccumulator);
				}
			}
			// see pr77166 if you are thinking about implementing this
			// } else if (i instanceof AALOAD ) {
			// AALOAD arrayLoad = (AALOAD)i;
			// Type arrayType = arrayLoad.getType(clazz.getConstantPoolGen());
			// BcelShadow arrayLoadShadow =
			// BcelShadow.makeArrayLoadCall(world,mg,ih,enclosingShadow);
			// match(arrayLoadShadow,shadowAccumulator);
			// } else if (i instanceof AASTORE) {
			// // ... magic required
		} else if (world.isJoinpointSynchronizationEnabled()
				&& ((i.getOpcode() == Constants.MONITORENTER) || (i.getOpcode() == Constants.MONITOREXIT))) {
			// if (canMatch(Shadow.Monitoring)) {
			if (i.getOpcode() == Constants.MONITORENTER) {
				BcelShadow monitorEntryShadow = BcelShadow.makeMonitorEnter(world, mg, ih, enclosingShadow);
				match(monitorEntryShadow, shadowAccumulator);
			} else {
				BcelShadow monitorExitShadow = BcelShadow.makeMonitorExit(world, mg, ih, enclosingShadow);
				match(monitorExitShadow, shadowAccumulator);
			}
			// }
		}

	}

	private boolean isInitFailureHandler(InstructionHandle ih) {
		// Skip the astore_0 and aload_0 at the start of the handler and
		// then check if the instruction following these is
		// 'putstatic ajc$initFailureCause'. If it is then we are
		// in the handler we created in AspectClinit.generatePostSyntheticCode()
		InstructionHandle twoInstructionsAway = ih.getNext().getNext();
		if (twoInstructionsAway.getInstruction().opcode == Constants.PUTSTATIC) {
			String name = ((FieldInstruction) twoInstructionsAway.getInstruction()).getFieldName(cpg);
			if (name.equals(NameMangler.INITFAILURECAUSE_FIELD_NAME)) {
				return true;
			}
		}
		return false;
	}

	private void matchSetInstruction(LazyMethodGen mg, InstructionHandle ih, BcelShadow enclosingShadow,
			List<BcelShadow> shadowAccumulator) {
		FieldInstruction fi = (FieldInstruction) ih.getInstruction();
		Member field = BcelWorld.makeFieldJoinPointSignature(clazz, fi);

		// synthetic fields are never join points
		if (field.getName().startsWith(NameMangler.PREFIX)) {
			return;
		}

		ResolvedMember resolvedField = field.resolve(world);
		if (resolvedField == null) {
			// we can't find the field, so it's not a join point.
			return;
		} else if (Modifier.isFinal(resolvedField.getModifiers())
				&& Utility.isConstantPushInstruction(ih.getPrev().getInstruction())) {
			// it's the set of a final constant, so it's
			// not a join point according to 1.0.6 and 1.1.
			return;
		} else if (resolvedField.isSynthetic()) {
			// sets of synthetics aren't join points in 1.1
			return;
		} else {
			// Fix for bug 172107 (similar the "get" fix for bug 109728)
			BcelShadow bs = BcelShadow.makeFieldSet(world, resolvedField, mg, ih, enclosingShadow);
			String cname = fi.getClassName(cpg);
			if (!resolvedField.getDeclaringType().getName().equals(cname)) {
				bs.setActualTargetType(cname);
			}
			match(bs, shadowAccumulator);
		}
	}

	private void matchGetInstruction(LazyMethodGen mg, InstructionHandle ih, BcelShadow enclosingShadow,
			List<BcelShadow> shadowAccumulator) {
		FieldInstruction fi = (FieldInstruction) ih.getInstruction();
		Member field = BcelWorld.makeFieldJoinPointSignature(clazz, fi);

		// synthetic fields are never join points
		if (field.getName().startsWith(NameMangler.PREFIX)) {
			return;
		}

		ResolvedMember resolvedField = field.resolve(world);
		if (resolvedField == null) {
			// we can't find the field, so it's not a join point.
			return;
		} else if (resolvedField.isSynthetic()) {
			// sets of synthetics aren't join points in 1.1
			return;
		} else {
			BcelShadow bs = BcelShadow.makeFieldGet(world, resolvedField, mg, ih, enclosingShadow);
			String cname = fi.getClassName(cpg);
			if (!resolvedField.getDeclaringType().getName().equals(cname)) {
				bs.setActualTargetType(cname);
			}
			match(bs, shadowAccumulator);
		}
	}

	/**
	 * For some named resolved type, this method looks for a member with a particular name - it should only be used when you truly
	 * believe there is only one member with that name in the type as it returns the first one it finds.
	 */
	private ResolvedMember findResolvedMemberNamed(ResolvedType type, String methodName) {
		ResolvedMember[] allMethods = type.getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			ResolvedMember member = allMethods[i];
			if (member.getName().equals(methodName)) {
				return member;
			}
		}
		return null;
	}

	/**
	 * Find the specified member in the specified type.
	 * 
	 * @param type the type to search for the member
	 * @param methodName the name of the method to find
	 * @param params the method parameters that the discovered method should have
	 */
	private ResolvedMember findResolvedMemberNamed(ResolvedType type, String methodName, UnresolvedType[] params) {
		ResolvedMember[] allMethods = type.getDeclaredMethods();
		List<ResolvedMember> candidates = new ArrayList<ResolvedMember>();
		for (int i = 0; i < allMethods.length; i++) {
			ResolvedMember candidate = allMethods[i];
			if (candidate.getName().equals(methodName)) {
				if (candidate.getArity() == params.length) {
					candidates.add(candidate);
				}
			}
		}

		if (candidates.size() == 0) {
			return null;
		} else if (candidates.size() == 1) {
			return candidates.get(0);
		} else {
			// multiple candidates
			for (ResolvedMember candidate : candidates) {
				// These checks will break down with generics... but that would need two ITDs with the same name, same arity and
				// generics
				boolean allOK = true;
				UnresolvedType[] candidateParams = candidate.getParameterTypes();
				for (int p = 0; p < candidateParams.length; p++) {
					if (!candidateParams[p].getErasureSignature().equals(params[p].getErasureSignature())) {
						allOK = false;
						break;
					}
				}
				if (allOK) {
					return candidate;
				}
			}
		}
		return null;
	}

	/**
	 * For a given resolvedmember, this will discover the real annotations for it. <b>Should only be used when the resolvedmember is
	 * the contents of an effective signature attribute, as thats the only time when the annotations aren't stored directly in the
	 * resolvedMember</b>
	 * 
	 * @param rm the sig we want it to pretend to be 'int A.m()' or somesuch ITD like thing
	 * @param declaredSig the real sig 'blah.ajc$xxx'
	 */
	private void fixParameterNamesForResolvedMember(ResolvedMember rm, ResolvedMember declaredSig) {

		UnresolvedType memberHostType = declaredSig.getDeclaringType();
		String methodName = declaredSig.getName();
		String[] pnames = null;
		if (rm.getKind() == Member.METHOD && !rm.isAbstract()) {
			if (methodName.startsWith("ajc$inlineAccessMethod") || methodName.startsWith("ajc$superDispatch")) {
				ResolvedMember resolvedDooberry = world.resolve(declaredSig);
				pnames = resolvedDooberry.getParameterNames();
			} else {
				ResolvedMember realthing = AjcMemberMaker.interMethodDispatcher(rm.resolve(world), memberHostType).resolve(world);
				ResolvedMember theRealMember = findResolvedMemberNamed(memberHostType.resolve(world), realthing.getName());
				if (theRealMember != null) {
					pnames = theRealMember.getParameterNames();
					// static ITDs don't need any parameter shifting
					if (pnames.length > 0 && pnames[0].equals("ajc$this_")) {
						String[] pnames2 = new String[pnames.length - 1];
						System.arraycopy(pnames, 1, pnames2, 0, pnames2.length);
						pnames = pnames2;
					}
				}
			}
			// i think ctors are missing from here... copy code from below...
		}
		rm.setParameterNames(pnames);
	}

	/**
	 * For a given resolvedmember, this will discover the real annotations for it. <b>Should only be used when the resolvedmember is
	 * the contents of an effective signature attribute, as thats the only time when the annotations aren't stored directly in the
	 * resolvedMember</b>
	 * 
	 * @param rm the sig we want it to pretend to be 'int A.m()' or somesuch ITD like thing
	 * @param declaredSig the real sig 'blah.ajc$xxx'
	 */
	private void fixAnnotationsForResolvedMember(ResolvedMember rm, ResolvedMember declaredSig) {
		try {
			UnresolvedType memberHostType = declaredSig.getDeclaringType();
			boolean containsKey = mapToAnnotationHolder.containsKey(rm);
			ResolvedMember realAnnotationHolder = mapToAnnotationHolder.get(rm);
			String methodName = declaredSig.getName();
			// FIXME asc shouldnt really rely on string names !
			if (!containsKey) {
				if (rm.getKind() == Member.FIELD) {
					if (methodName.startsWith("ajc$inlineAccessField")) {
						realAnnotationHolder = world.resolve(rm);
					} else {
						ResolvedMember realthing = AjcMemberMaker.interFieldInitializer(rm, memberHostType);
						realAnnotationHolder = world.resolve(realthing);
					}
				} else if (rm.getKind() == Member.METHOD && !rm.isAbstract()) {
					if (methodName.startsWith("ajc$inlineAccessMethod") || methodName.startsWith("ajc$superDispatch")) {
						realAnnotationHolder = world.resolve(declaredSig);
					} else {
						ResolvedMember realthing = AjcMemberMaker.interMethodDispatcher(rm.resolve(world), memberHostType).resolve(world);
						realAnnotationHolder = findResolvedMemberNamed(memberHostType.resolve(world), realthing.getName(),realthing.getParameterTypes());
						if (realAnnotationHolder == null) {
							throw new UnsupportedOperationException(
									"Known limitation in M4 - can't find ITD members when type variable is used as an argument and has upper bound specified");
						}
					}
				} else if (rm.getKind() == Member.CONSTRUCTOR) {
					ResolvedMember realThing = AjcMemberMaker.postIntroducedConstructor(memberHostType.resolve(world),rm.getDeclaringType(), rm.getParameterTypes());
					realAnnotationHolder = world.resolve(realThing);
					// AMC temp guard for M4
					if (realAnnotationHolder == null) {
						throw new UnsupportedOperationException("Known limitation in M4 - can't find ITD members when type variable is used as an argument and has upper bound specified");
					}
				}
				mapToAnnotationHolder.put(rm, realAnnotationHolder);
			}
			ResolvedType[] annotationTypes;
			AnnotationAJ[] annotations;
			if (realAnnotationHolder!=null) {
				annotationTypes = realAnnotationHolder.getAnnotationTypes();
				annotations = realAnnotationHolder.getAnnotations();
				if (annotationTypes==null) {
					annotationTypes = ResolvedType.EMPTY_ARRAY;
				}
				if (annotations==null) {
					annotations = AnnotationAJ.EMPTY_ARRAY;
				}
			} else {
				annotations = AnnotationAJ.EMPTY_ARRAY;
				annotationTypes = ResolvedType.EMPTY_ARRAY;
			}
			rm.setAnnotations(annotations);
			rm.setAnnotationTypes(annotationTypes);
		} catch (UnsupportedOperationException ex) {
			throw ex;
		} catch (Throwable t) {
			// FIXME asc remove this catch after more testing has confirmed the
			// above stuff is OK
			throw new BCException("Unexpectedly went bang when searching for annotations on " + rm, t);
		}
	}

	private void matchInvokeInstruction(LazyMethodGen mg, InstructionHandle ih, InvokeInstruction invoke,
			BcelShadow enclosingShadow, List<BcelShadow> shadowAccumulator) {
		String methodName = invoke.getName(cpg);
		if (methodName.startsWith(NameMangler.PREFIX)) {
			Member jpSig = world.makeJoinPointSignatureForMethodInvocation(clazz, invoke);
			ResolvedMember declaredSig = jpSig.resolve(world);
			// System.err.println(method + ", declaredSig: " +declaredSig);
			if (declaredSig == null) {
				return;
			}

			if (declaredSig.getKind() == Member.FIELD) {
				Shadow.Kind kind;
				if (jpSig.getReturnType().equals(UnresolvedType.VOID)) {
					kind = Shadow.FieldSet;
				} else {
					kind = Shadow.FieldGet;
				}

				if (canMatch(Shadow.FieldGet) || canMatch(Shadow.FieldSet)) {
					match(BcelShadow.makeShadowForMethodCall(world, mg, ih, enclosingShadow, kind, declaredSig), shadowAccumulator);
				}
			} else {
				AjAttribute.EffectiveSignatureAttribute effectiveSig = declaredSig.getEffectiveSignature();
				if (effectiveSig == null) {
					return;
				}
				// System.err.println("call to inter-type member: " +
				// effectiveSig);
				if (effectiveSig.isWeaveBody()) {
					return;
				}

				ResolvedMember rm = effectiveSig.getEffectiveSignature();
				fixParameterNamesForResolvedMember(rm, declaredSig);
				fixAnnotationsForResolvedMember(rm, declaredSig); // abracadabra

				if (canMatch(effectiveSig.getShadowKind())) {
					match(BcelShadow.makeShadowForMethodCall(world, mg, ih, enclosingShadow, effectiveSig.getShadowKind(), rm),
							shadowAccumulator);
				}
			}
		} else {
			if (canMatch(Shadow.MethodCall)) {
				boolean proceed = true;
				// overweaving needs to ignore some calls added by the previous weave
				if (world.isOverWeaving()) {
					String s = invoke.getClassName(mg.getConstantPool());
					// skip all the inc/dec/isValid/etc
					if (s.length() > 4
							&& s.charAt(4) == 'a'
							&& (s.equals("org.aspectj.runtime.internal.CFlowCounter")
									|| s.equals("org.aspectj.runtime.internal.CFlowStack") || s
										.equals("org.aspectj.runtime.reflect.Factory"))) {
						proceed = false;
					} else {
						if (methodName.equals("aspectOf")) {
							proceed = false;
						}
					}
				}
				if (proceed) {
					match(BcelShadow.makeMethodCall(world, mg, ih, enclosingShadow), shadowAccumulator);
				}
			}
		}
	}

	// static ... so all worlds will share the config for the first one
	// created...
	private static boolean checkedXsetForLowLevelContextCapturing = false;
	private static boolean captureLowLevelContext = false;

	private boolean match(BcelShadow shadow, List<BcelShadow> shadowAccumulator) {
		// Duplicate blocks - one with context one without, seems faster than multiple 'ifs'
		if (captureLowLevelContext) {
			ContextToken shadowMatchToken = CompilationAndWeavingContext.enteringPhase(
					CompilationAndWeavingContext.MATCHING_SHADOW, shadow);
			boolean isMatched = false;

			Shadow.Kind shadowKind = shadow.getKind();
			List<ShadowMunger> candidateMungers = indexedShadowMungers[shadowKind.getKey()];

			// System.out.println("Candidates " + candidateMungers);
			if (candidateMungers != null) {
				for (ShadowMunger munger : candidateMungers) {

					ContextToken mungerMatchToken = CompilationAndWeavingContext.enteringPhase(
							CompilationAndWeavingContext.MATCHING_POINTCUT, munger.getPointcut());
					if (munger.match(shadow, world)) {
						shadow.addMunger(munger);
						isMatched = true;
						if (shadow.getKind() == Shadow.StaticInitialization) {
							clazz.warnOnAddedStaticInitializer(shadow, munger.getSourceLocation());
						}
					}
					CompilationAndWeavingContext.leavingPhase(mungerMatchToken);
				}

				if (isMatched) {
					shadowAccumulator.add(shadow);
				}
			}
			CompilationAndWeavingContext.leavingPhase(shadowMatchToken);
			return isMatched;
		} else {
			boolean isMatched = false;

			Shadow.Kind shadowKind = shadow.getKind();
			List<ShadowMunger> candidateMungers = indexedShadowMungers[shadowKind.getKey()];

			// System.out.println("Candidates at " + shadowKind + " are " + candidateMungers);
			if (candidateMungers != null) {
				for (ShadowMunger munger : candidateMungers) {
					if (munger.match(shadow, world)) {
						shadow.addMunger(munger);
						isMatched = true;
						if (shadow.getKind() == Shadow.StaticInitialization) {
							clazz.warnOnAddedStaticInitializer(shadow, munger.getSourceLocation());
						}
					}
				}
				if (isMatched) {
					shadowAccumulator.add(shadow);
				}
			}
			return isMatched;
		}
	}

	// ----

	private void implement(LazyMethodGen mg) {
		List<BcelShadow> shadows = mg.matchedShadows;
		if (shadows == null) {
			return;
		}
		// We depend on a partial order such that inner shadows are earlier on
		// the list than outer shadows. That's fine. This order is preserved if:

		// A preceeds B iff B.getStart() is LATER THAN A.getStart().

		for (BcelShadow shadow : shadows) {
			ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.IMPLEMENTING_ON_SHADOW,
					shadow);
			shadow.implement();
			CompilationAndWeavingContext.leavingPhase(tok);
		}
		// int ii =
		mg.getMaxLocals();
		mg.matchedShadows = null;
	}

	// ----

	public LazyClassGen getLazyClassGen() {
		return clazz;
	}

	public BcelWorld getWorld() {
		return world;
	}

	public void setReweavableMode(boolean mode) {
		inReweavableMode = mode;
	}

	public boolean getReweavableMode() {
		return inReweavableMode;
	}

	@Override
	public String toString() {
		return "BcelClassWeaver instance for : " + clazz;
	}

}
