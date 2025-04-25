/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.weaver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.AspectJElementHierarchy;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.bridge.context.ContextToken;
import org.aspectj.util.FileUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.bcel.BcelObjectType;
import org.aspectj.weaver.bcel.BcelPerClauseAspectAdder;
import org.aspectj.weaver.bcel.BcelTypeMunger;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.LazyClassGen;
import org.aspectj.weaver.bcel.Utility;
import org.aspectj.weaver.model.AsmRelationshipProvider;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.BindingPattern;
import org.aspectj.weaver.patterns.BindingTypePattern;
import org.aspectj.weaver.patterns.ConcreteCflowPointcut;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclareTypeErrorOrWarning;
import org.aspectj.weaver.patterns.FastMatchInfo;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NameBindingPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;
import org.aspectj.weaver.patterns.WithinPointcut;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

public abstract class BytecodeWeaver {

	protected static Trace trace = TraceFactory.getTraceFactory().getTrace(BytecodeWeaver.class);
	
	protected Manifest manifest = null;
	
	protected boolean isBatchWeave = true;
	
	public static final String CLOSURE_CLASS_PREFIX = "$Ajc";
	public static final String SYNTHETIC_CLASS_POSTFIX = "$ajc";

	// These four are setup by prepareForWeave
	protected transient List<ShadowMunger> shadowMungerList = null;
	protected transient List<ConcreteTypeMunger> typeMungerList = null;
	protected transient List<ConcreteTypeMunger> lateTypeMungerList = null;
	protected transient List<DeclareParents> declareParentsList = null;

	protected CrosscuttingMembersSet xcutSet;
	
	protected boolean needToReweaveWorld = false;

	protected transient List<UnwovenClassFile> addedClasses = new ArrayList<>();
	protected transient List<String> deletedTypenames = new ArrayList<>();

	protected boolean inReweavableMode = false;

	private CustomMungerFactory customMungerFactory;
	
	protected Set<IProgramElement> candidatesForRemoval = null;

	private ZipOutputStream zipOutputStream;
	
	public Manifest getManifest(boolean shouldCreate) {
		if (manifest == null && shouldCreate) {
			String WEAVER_MANIFEST_VERSION = "1.0";
			Attributes.Name CREATED_BY = new Name("Created-By");
			String WEAVER_CREATED_BY = "AspectJ Compiler";

			manifest = new Manifest();

			Attributes attributes = manifest.getMainAttributes();
			attributes.put(Name.MANIFEST_VERSION, WEAVER_MANIFEST_VERSION);
			attributes.put(CREATED_BY, WEAVER_CREATED_BY);
		}

		return manifest;
	}
	
	public abstract BytecodeWorld getWorld();

	/**
	 * In 1.5 mode and with XLint:adviceDidNotMatch enabled, put out messages for any mungers that did not match anything.
	 */
	private void warnOnUnmatchedAdvice() {

		class AdviceLocation {
			private final int lineNo;
			private final UnresolvedType inAspect;

			public AdviceLocation(Advice advice) {
				this.lineNo = advice.getSourceLocation().getLine();
				this.inAspect = advice.getDeclaringAspect();
			}

			@Override
			public boolean equals(Object obj) {
				if (!(obj instanceof AdviceLocation)) {
					return false;
				}
				AdviceLocation other = (AdviceLocation) obj;
				if (this.lineNo != other.lineNo) {
					return false;
				}
				if (!this.inAspect.equals(other.inAspect)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				return 37 + 17 * lineNo + 17 * inAspect.hashCode();
			}
		}

		World world = getWorld();
		// FIXME asc Should be factored out into Xlint code and done
		// automatically for all xlint messages, ideally.
		// if a piece of advice hasn't matched anywhere and we are in -1.5 mode,
		// put out a warning
		if (world.isInJava5Mode() && world.getLint().adviceDidNotMatch.isEnabled()) {
			List<ShadowMunger> l = world.getCrosscuttingMembersSet().getShadowMungers();
			Set<AdviceLocation> alreadyWarnedLocations = new HashSet<>();

			for (ShadowMunger element : l) {
				// This will stop us incorrectly reporting deow checkers:
				if (element instanceof Advice) {
					Advice ba = (Advice) element;
					if (ba.getKind() == AdviceKind.CflowEntry || ba.getKind() == AdviceKind.CflowBelowEntry) {
						continue;
					}
					if (ba.getKind() == AdviceKind.PerCflowEntry || ba.getKind() == AdviceKind.PerTargetEntry ||
						ba.getKind() == AdviceKind.PerCflowBelowEntry || ba.getKind() == AdviceKind.PerThisEntry ||
						ba.getKind() == AdviceKind.PerTypeWithinEntry) {
						// Unclear if we want advice did not match warnings for per clauses...
						continue;
					}
					if (!ba.hasMatchedSomething()) {
						// Because we implement some features of AJ itself by
						// creating our own kind of mungers, you sometimes
						// find that ba.getSignature() is not a BcelMethod - for
						// example it might be a cflow entry munger.
						if (ba.getSignature() != null) {
							// check we haven't already warned on this advice and line
							// (cflow creates multiple mungers for the same advice)
							AdviceLocation loc = new AdviceLocation(ba);
							if (alreadyWarnedLocations.contains(loc)) {
								continue;
							} else {
								alreadyWarnedLocations.add(loc);
							}

							if (!(ba.getSignature() instanceof ResolvedMemberImpl) // TODO was BcelMethod - is this check ok?
									|| !Utility.isSuppressing(ba.getSignature(), "adviceDidNotMatch")) {
								world.getLint().adviceDidNotMatch.signal(ba.getDeclaringAspect().toString(), new SourceLocation(
										element.getSourceLocation().getSourceFile(), element.getSourceLocation().getLine()));
							}
						}
					}
				}
			}
		}
	}

	public abstract ReferenceType addClassFile(UnwovenClassFile classFile, boolean fromInpath);
	
	public abstract void deleteClassFile(String typename);
	
	public void setIsBatchWeave(boolean b) {
		isBatchWeave = b;
	}
	

	public void prepareForWeave() {
		if (trace.isTraceEnabled()) {
			trace.enter("prepareForWeave", this);
		}
		needToReweaveWorld = xcutSet.hasChangedSinceLastReset();

		// update mungers
		for (UnwovenClassFile jc : addedClasses) {
			String name = jc.getClassName();
			ResolvedType type = getWorld().resolve(name);
			// No overweaving guard. If you have one then when overweaving is on the
			// addOrReplaceAspect will not be called when the aspect delegate changes from
			// EclipseSourceType to BcelObjectType. This will mean the mungers
			// are not picked up.
			if (type.isAspect()) {
				needToReweaveWorld |= xcutSet.addOrReplaceAspect(type);
			}
		}

		for (String name : deletedTypenames) {
			if (xcutSet.deleteAspect(UnresolvedType.forName(name))) {
				needToReweaveWorld = true;
			}
		}

		shadowMungerList = xcutSet.getShadowMungers();
		// world.debug("shadow mungers=" + shadowMungerList);
		rewritePointcuts(shadowMungerList);
		// Sometimes an error occurs during rewriting pointcuts (for example, if
		// ambiguous bindings
		// are detected) - we ought to fail the prepare when this happens
		// because continuing with
		// inconsistent pointcuts could lead to problems
		typeMungerList = xcutSet.getTypeMungers();
		lateTypeMungerList = xcutSet.getLateTypeMungers();
		declareParentsList = xcutSet.getDeclareParents();

		addCustomMungers();

		// The ordering here used to be based on a string compare on toString()
		// for the two mungers -
		// that breaks for the @AJ style where advice names aren't
		// programmatically generated. So we
		// have changed the sorting to be based on source location in the file -
		// this is reliable, in
		// the case of source locations missing, we assume they are 'sorted' -
		// i.e. the order in
		// which they were added to the collection is correct, this enables the
		// @AJ stuff to work properly.

		// When @AJ processing starts filling in source locations for mungers,
		// this code may need
		// a bit of alteration...

		shadowMungerList.sort(new Comparator<ShadowMunger>() {
			public int compare(ShadowMunger sm1, ShadowMunger sm2) {
				if (sm1.getSourceLocation() == null) {
					return (sm2.getSourceLocation() == null ? 0 : 1);
				}
				if (sm2.getSourceLocation() == null) {
					return -1;
				}

				return (sm2.getSourceLocation().getOffset() - sm1.getSourceLocation().getOffset());
			}
		});

		if (inReweavableMode) {
			getWorld().showMessage(IMessage.INFO, WeaverMessages.format(WeaverMessages.REWEAVABLE_MODE), null, null);
		}

		if (trace.isTraceEnabled()) {
			trace.exit("prepareForWeave");
		}
	}

	public void setCustomMungerFactory(CustomMungerFactory factory) {
		customMungerFactory = factory;
	}

	protected void addCustomMungers() {
		if (customMungerFactory != null) {
			for (UnwovenClassFile jc : addedClasses) {
				String name = jc.getClassName();
				ResolvedType type = getWorld().resolve(name);
				if (type.isAspect()) {
					Collection<ShadowMunger> shadowMungers = customMungerFactory.createCustomShadowMungers(type);
					if (shadowMungers != null) {
						shadowMungerList.addAll(shadowMungers);
					}
					Collection<ConcreteTypeMunger> typeMungers = customMungerFactory.createCustomTypeMungers(type);
					if (typeMungers != null) {
						typeMungerList.addAll(typeMungers);
					}
				}
			}
		}
	}

	public void allWeavingComplete() {
		warnOnUnmatchedAdvice();
	}

	/*
	 * Rewrite all of the pointcuts in the world into their most efficient form for subsequent matching. Also ensure that if
	 * pc1.equals(pc2) then pc1 == pc2 (for non-binding pcds) by making references all point to the same instance. Since pointcuts
	 * remember their match decision on the last shadow, this makes matching faster when many pointcuts share common elements, or
	 * even when one single pointcut has one common element (which can be a side-effect of DNF rewriting).
	 */
	private void rewritePointcuts(List<ShadowMunger> shadowMungers) {
		PointcutRewriter rewriter = new PointcutRewriter();
		World world = getWorld();
		for (ShadowMunger munger : shadowMungers) {
			Pointcut p = munger.getPointcut();
			Pointcut newP = rewriter.rewrite(p);
			// validateBindings now whilst we still have around the pointcut
			// that resembles what the user actually wrote in their program
			// text.
			if (munger instanceof Advice) {
				Advice advice = (Advice) munger;
				if (advice.getSignature() != null) {
					final int numFormals;
					final String names[];
					// If the advice is being concretized in a @AJ aspect *and*
					// the advice was declared in
					// an @AJ aspect (it could have been inherited from a code
					// style aspect) then
					// evaluate the alternative set of formals. pr125699
					if ((advice.getConcreteAspect().isAnnotationStyleAspect() && advice.getDeclaringAspect() != null && advice
							.getDeclaringAspect().resolve(world).isAnnotationStyleAspect())
							|| advice.isAnnotationStyle()) {
						numFormals = advice.getBaseParameterCount();
						int numArgs = advice.getSignature().getParameterTypes().length;
						if (numFormals > 0) {
							names = advice.getSignature().getParameterNames(world);
							validateBindings(newP, p, numArgs, names);
						}
					} else {
						numFormals = advice.getBaseParameterCount();
						if (numFormals > 0) {
							names = advice.getBaseParameterNames(world);
							validateBindings(newP, p, numFormals, names);
						}
					}
				}
			}
			newP.m_ignoreUnboundBindingForNames = p.m_ignoreUnboundBindingForNames;
			munger.setPointcut(newP);
		}
		// now that we have optimized individual pointcuts, optimize
		// across the set of pointcuts....
		// Use a map from key based on pc equality, to value based on
		// pc identity.
		Map<Pointcut, Pointcut> pcMap = new HashMap<>();
		for (ShadowMunger munger: shadowMungers) {
			Pointcut p = munger.getPointcut();
			Pointcut newP = shareEntriesFromMap(p, pcMap);
			newP.m_ignoreUnboundBindingForNames = p.m_ignoreUnboundBindingForNames;
			munger.setPointcut(newP);
		}
	}


	// userPointcut is the pointcut that the user wrote in the program text.
	// dnfPointcut is the same pointcut rewritten in DNF
	// numFormals is the number of formal parameters in the pointcut
	// if numFormals > 0 then every branch of a disjunction must bind each
	// formal once and only once.
	// in addition, the left and right branches of a disjunction must hold on
	// join point kinds in common.
	private void validateBindings(Pointcut dnfPointcut, Pointcut userPointcut, int numFormals, String[] names) {
		if (numFormals == 0) {
			return; // nothing to check
		}
		if (dnfPointcut.couldMatchKinds() == Shadow.NO_SHADOW_KINDS_BITS) {
			return; // cant have problems if you dont match!
		}
		if (dnfPointcut instanceof OrPointcut) {
			OrPointcut orBasedDNFPointcut = (OrPointcut) dnfPointcut;
			Pointcut[] leftBindings = new Pointcut[numFormals];
			Pointcut[] rightBindings = new Pointcut[numFormals];
			validateOrBranch(orBasedDNFPointcut, userPointcut, numFormals, names, leftBindings, rightBindings);
		} else {
			Pointcut[] bindings = new Pointcut[numFormals];
			validateSingleBranch(dnfPointcut, userPointcut, numFormals, names, bindings);
		}
	}

	private void validateOrBranch(OrPointcut pc, Pointcut userPointcut, int numFormals, String[] names, Pointcut[] leftBindings,
			Pointcut[] rightBindings) {
		Pointcut left = pc.getLeft();
		Pointcut right = pc.getRight();
		if (left instanceof OrPointcut) {
			Pointcut[] newRightBindings = new Pointcut[numFormals];
			validateOrBranch((OrPointcut) left, userPointcut, numFormals, names, leftBindings, newRightBindings);
		} else {
			if (left.couldMatchKinds() != Shadow.NO_SHADOW_KINDS_BITS) {
				validateSingleBranch(left, userPointcut, numFormals, names, leftBindings);
			}
		}
		if (right instanceof OrPointcut) {
			Pointcut[] newLeftBindings = new Pointcut[numFormals];
			validateOrBranch((OrPointcut) right, userPointcut, numFormals, names, newLeftBindings, rightBindings);
		} else {
			if (right.couldMatchKinds() != Shadow.NO_SHADOW_KINDS_BITS) {
				validateSingleBranch(right, userPointcut, numFormals, names, rightBindings);
			}
		}
		int kindsInCommon = left.couldMatchKinds() & right.couldMatchKinds();
		if (kindsInCommon != Shadow.NO_SHADOW_KINDS_BITS && couldEverMatchSameJoinPoints(left, right)) {
			// we know that every branch binds every formal, so there is no
			// ambiguity if each branch binds it in exactly the same way...
			List<String> ambiguousNames = new ArrayList<>();
			for (int i = 0; i < numFormals; i++) {
				if (leftBindings[i] == null) {
					if (rightBindings[i] != null) {
						ambiguousNames.add(names[i]);
					}
				} else if (!leftBindings[i].equals(rightBindings[i])) {
					ambiguousNames.add(names[i]);
				}
			}
			if (!ambiguousNames.isEmpty()) {
				raiseAmbiguityInDisjunctionError(userPointcut, ambiguousNames);
			}
		}
	}

	// pc is a pointcut that does not contain any disjunctions
	// check that every formal is bound (negation doesn't count).
	// we know that numFormals > 0 or else we would not be called
	private void validateSingleBranch(Pointcut pc, Pointcut userPointcut, int numFormals, String[] names, Pointcut[] bindings) {
		boolean[] foundFormals = new boolean[numFormals];
		for (int i = 0; i < foundFormals.length; i++) {
			foundFormals[i] = false;
		}
		validateSingleBranchRecursion(pc, userPointcut, foundFormals, names, bindings);
		for (int i = 0; i < foundFormals.length; i++) {
			if (!foundFormals[i]) {
				boolean ignore = false;
				// ATAJ soften the unbound error for implicit bindings like
				// JoinPoint in @AJ style
				for (int j = 0; j < userPointcut.m_ignoreUnboundBindingForNames.length; j++) {
					if (names[i] != null && names[i].equals(userPointcut.m_ignoreUnboundBindingForNames[j])) {
						ignore = true;
						break;
					}
				}
				if (!ignore) {
					raiseUnboundFormalError(names[i], userPointcut);
				}
			}
		}
	}

	// only called for testing
	public void setShadowMungers(List<ShadowMunger> shadowMungers) {
		shadowMungerList = shadowMungers;
	}

	// each formal must appear exactly once
	private void validateSingleBranchRecursion(Pointcut pc, Pointcut userPointcut, boolean[] foundFormals, String[] names,
			Pointcut[] bindings) {
		if (pc instanceof NotPointcut) {
			// nots can only appear at leaves in DNF
			NotPointcut not = (NotPointcut) pc;
			if (not.getNegatedPointcut() instanceof NameBindingPointcut) {
				NameBindingPointcut nnbp = (NameBindingPointcut) not.getNegatedPointcut();
				if (!nnbp.getBindingAnnotationTypePatterns().isEmpty() && !nnbp.getBindingTypePatterns().isEmpty()) {
					raiseNegationBindingError(userPointcut);
				}
			}
		} else if (pc instanceof AndPointcut) {
			AndPointcut and = (AndPointcut) pc;
			validateSingleBranchRecursion(and.getLeft(), userPointcut, foundFormals, names, bindings);
			validateSingleBranchRecursion(and.getRight(), userPointcut, foundFormals, names, bindings);
		} else if (pc instanceof NameBindingPointcut) {
			List<BindingTypePattern> bindingTypePatterns = ((NameBindingPointcut) pc).getBindingTypePatterns();
			for (BindingTypePattern bindingTypePattern: bindingTypePatterns) {
				int index = bindingTypePattern.getFormalIndex();
				bindings[index] = pc;
				if (foundFormals[index]) {
					raiseAmbiguousBindingError(names[index], userPointcut);
				} else {
					foundFormals[index] = true;
				}
			}
			List<BindingPattern> bindingAnnotationTypePatterns = ((NameBindingPointcut) pc).getBindingAnnotationTypePatterns();
			for (BindingPattern bindingAnnotationTypePattern: bindingAnnotationTypePatterns) {
				int index = bindingAnnotationTypePattern.getFormalIndex();
				bindings[index] = pc;
				if (foundFormals[index]) {
					raiseAmbiguousBindingError(names[index], userPointcut);
				} else {
					foundFormals[index] = true;
				}
			}
		} else if (pc instanceof ConcreteCflowPointcut) {
			ConcreteCflowPointcut cfp = (ConcreteCflowPointcut) pc;
			int[] slots = cfp.getUsedFormalSlots();
			for (int slot : slots) {
				bindings[slot] = cfp;
				if (foundFormals[slot]) {
					raiseAmbiguousBindingError(names[slot], userPointcut);
				} else {
					foundFormals[slot] = true;
				}
			}
		}
	}
	

	private void raiseNegationBindingError(Pointcut userPointcut) {
		getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.NEGATION_DOESNT_ALLOW_BINDING), userPointcut
				.getSourceContext().makeSourceLocation(userPointcut), null);
	}

	private void raiseAmbiguousBindingError(String name, Pointcut pointcut) {
		getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.AMBIGUOUS_BINDING, name), pointcut
				.getSourceContext().makeSourceLocation(pointcut), null);
	}

	private void raiseAmbiguityInDisjunctionError(Pointcut userPointcut, List<String> names) {
		StringBuffer formalNames = new StringBuffer(names.get(0).toString());
		for (int i = 1; i < names.size(); i++) {
			formalNames.append(", ");
			formalNames.append(names.get(i));
		}
		getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.AMBIGUOUS_BINDING_IN_OR, formalNames), userPointcut
				.getSourceContext().makeSourceLocation(userPointcut), null);
	}

	/**
	 * @param name
	 * @param userPointcut
	 */
	private void raiseUnboundFormalError(String name, Pointcut userPointcut) {
		getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.UNBOUND_FORMAL, name),
				userPointcut.getSourceLocation(), null);
	}


	// By returning false from this method, we are allowing binding of the same
	// variable on either side of an or.
	// Be conservative :- have to consider overriding, varargs, autoboxing,
	// the effects of itds (on within for example), interfaces, the fact that
	// join points can have multiple signatures and so on.
	private boolean couldEverMatchSameJoinPoints(Pointcut left, Pointcut right) {

		if (left instanceof OrPointcut) {
			OrPointcut leftOrPointcut = (OrPointcut) left;
			if (couldEverMatchSameJoinPoints(leftOrPointcut.getLeft(), right)) {
				return true;
			}
			if (couldEverMatchSameJoinPoints(leftOrPointcut.getRight(), right)) {
				return true;
			}
			return false;
		}

		if (right instanceof OrPointcut) {
			OrPointcut rightOrPointcut = (OrPointcut) right;
			if (couldEverMatchSameJoinPoints(left, rightOrPointcut.getLeft())) {
				return true;
			}
			if (couldEverMatchSameJoinPoints(left, rightOrPointcut.getRight())) {
				return true;
			}
			return false;
		}

		// look for withins
		WithinPointcut leftWithin = (WithinPointcut) findFirstPointcutIn(left, WithinPointcut.class);
		WithinPointcut rightWithin = (WithinPointcut) findFirstPointcutIn(right, WithinPointcut.class);
		if ((leftWithin != null) && (rightWithin != null)) {
			if (!leftWithin.couldEverMatchSameJoinPointsAs(rightWithin)) {
				return false;
			}
		}
		// look for kinded
		KindedPointcut leftKind = (KindedPointcut) findFirstPointcutIn(left, KindedPointcut.class);
		KindedPointcut rightKind = (KindedPointcut) findFirstPointcutIn(right, KindedPointcut.class);
		if ((leftKind != null) && (rightKind != null)) {
			if (!leftKind.couldEverMatchSameJoinPointsAs(rightKind)) {
				return false;
			}
		}
		return true;
	}

	private Pointcut findFirstPointcutIn(Pointcut toSearch, Class<?> toLookFor) {
		if (toSearch instanceof NotPointcut) {
			return null;
		}
		if (toLookFor.isInstance(toSearch)) {
			return toSearch;
		}
		if (toSearch instanceof AndPointcut) {
			AndPointcut apc = (AndPointcut) toSearch;
			Pointcut left = findFirstPointcutIn(apc.getLeft(), toLookFor);
			if (left != null) {
				return left;
			}
			return findFirstPointcutIn(apc.getRight(), toLookFor);
		}
		return null;
	}

	private Pointcut shareEntriesFromMap(Pointcut p, Map<Pointcut, Pointcut> pcMap) {
		// some things cant be shared...
		if (p instanceof NameBindingPointcut) {
			return p;
		}
		if (p instanceof IfPointcut) {
			return p;
		}
		if (p instanceof ConcreteCflowPointcut) {
			return p;
		}
		if (p instanceof AndPointcut) {
			AndPointcut apc = (AndPointcut) p;
			Pointcut left = shareEntriesFromMap(apc.getLeft(), pcMap);
			Pointcut right = shareEntriesFromMap(apc.getRight(), pcMap);
			return new AndPointcut(left, right);
		} else if (p instanceof OrPointcut) {
			OrPointcut opc = (OrPointcut) p;
			Pointcut left = shareEntriesFromMap(opc.getLeft(), pcMap);
			Pointcut right = shareEntriesFromMap(opc.getRight(), pcMap);
			return new OrPointcut(left, right);
		} else if (p instanceof NotPointcut) {
			NotPointcut npc = (NotPointcut) p;
			Pointcut not = shareEntriesFromMap(npc.getNegatedPointcut(), pcMap);
			return new NotPointcut(not);
		} else {
			// primitive pcd
			if (pcMap.containsKey(p)) { // based on equality
				return pcMap.get(p); // same instance (identity)
			} else {
				pcMap.put(p, p);
				return p;
			}
		}
	}

	public void write(CompressingDataOutputStream dos) throws IOException {
		xcutSet.write(dos);
	}

	public boolean needToReweaveWorld() {
		return needToReweaveWorld;
	}
	

	// variation of "weave" that sources class files from an external source.
	public Collection<String> weave(IClassFileProvider input) throws IOException {
		if (trace.isTraceEnabled()) {
			trace.enter("weave", this, input);
		}
		BytecodeWorld world = getWorld();
		ContextToken weaveToken = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.WEAVING, "");
		Collection<String> wovenClassNames = new ArrayList<>();
		IWeaveRequestor requestor = input.getRequestor();

		if (world.getModel() != null && world.isMinimalModel()) {
			candidatesForRemoval = new HashSet<>();
		}
		if (world.getModel() != null && !isBatchWeave) {
			AsmManager manager = world.getModelAsAsmManager();
			for (Iterator<UnwovenClassFile> i = input.getClassFileIterator(); i.hasNext();) {
				UnwovenClassFile classFile = i.next();
				// remove all relationships where this file being woven is
				// the target of the relationship
				manager.removeRelationshipsTargettingThisType(classFile.getClassName());
			}
		}

		// Go through the types and ensure any 'damaged' during compile time are
		// repaired prior to weaving
		for (Iterator<UnwovenClassFile> i = input.getClassFileIterator(); i.hasNext();) {
			UnwovenClassFile classFile = i.next();
			if (classFile.shouldBeWoven()) {
				String className = classFile.getClassName();
				ResolvedType theType = world.resolve(className);
				if (theType != null) {
					theType.ensureConsistent();
				}
			}
		}

		// special case for AtAspectJMungerOnly - see #113587
		if (input.isApplyAtAspectJMungersOnly()) {
			ContextToken atAspectJMungersOnly = CompilationAndWeavingContext.enteringPhase(
					CompilationAndWeavingContext.PROCESSING_ATASPECTJTYPE_MUNGERS_ONLY, "");
			requestor.weavingAspects();
			// ContextToken aspectToken =
			CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.WEAVING_ASPECTS, "");
			for (Iterator<UnwovenClassFile> i = input.getClassFileIterator(); i.hasNext();) {
				UnwovenClassFile classFile = i.next();
				if (classFile.shouldBeWoven()) {
					String className = classFile.getClassName();
					ResolvedType theType = world.resolve(className);
					if (theType.isAnnotationStyleAspect()) {
						BcelObjectType classType = BcelWorld.getBcelObjectType(theType);
						if (classType == null) {
							throw new BCException("Can't find bcel delegate for " + className + " type=" + theType.getClass());
						}
						LazyClassGen clazz = classType.getLazyClassGen();
						BcelPerClauseAspectAdder selfMunger = new BcelPerClauseAspectAdder(theType, theType.getPerClause().getKind());
						selfMunger.forceMunge(clazz, true);
						classType.finishedWith();
						UnwovenClassFile[] newClasses = getClassFilesFor(clazz);
						for (UnwovenClassFile newClass : newClasses) {
							requestor.acceptResult(newClass);
						}
						wovenClassNames.add(classFile.getClassName());
					}
				}
			}
			requestor.weaveCompleted();
			CompilationAndWeavingContext.leavingPhase(atAspectJMungersOnly);
			return wovenClassNames;
		}

		requestor.processingReweavableState();
		ContextToken reweaveToken = CompilationAndWeavingContext.enteringPhase(
				CompilationAndWeavingContext.PROCESSING_REWEAVABLE_STATE, "");
		prepareToProcessReweavableState();
		// clear all state from files we'll be reweaving
		for (Iterator<UnwovenClassFile> i = input.getClassFileIterator(); i.hasNext();) {
			UnwovenClassFile classFile = i.next();
			if (classFile.shouldBeWoven()) {
				String className = classFile.getClassName();
				BcelObjectType classType = getClassType(className);

				// null return from getClassType() means the delegate is an eclipse
				// source type - so
				// there *cant* be any reweavable state... (he bravely claimed...)
				if (classType != null) {
					ContextToken tok = CompilationAndWeavingContext.enteringPhase(
							CompilationAndWeavingContext.PROCESSING_REWEAVABLE_STATE, className);
					processReweavableStateIfPresent(className, classType);
					CompilationAndWeavingContext.leavingPhase(tok);
				}
			}
		}

		CompilationAndWeavingContext.leavingPhase(reweaveToken);

		ContextToken typeMungingToken = CompilationAndWeavingContext.enteringPhase(
				CompilationAndWeavingContext.PROCESSING_TYPE_MUNGERS, "");
		requestor.addingTypeMungers();

		// We process type mungers in two groups, first mungers that change the
		// type
		// hierarchy, then 'normal' ITD type mungers.

		// Process the types in a predictable order (rather than the order
		// encountered).
		// For class A, the order is superclasses of A then superinterfaces of A
		// (and this mechanism is applied recursively)
		List<String> typesToProcess = new ArrayList<>();
		for (Iterator<UnwovenClassFile> iter = input.getClassFileIterator(); iter.hasNext();) {
			UnwovenClassFile clf = iter.next();
			if (clf.shouldBeWoven()) {
				typesToProcess.add(clf.getClassName());
			}
		}
		while (typesToProcess.size() > 0) {
			weaveParentsFor(typesToProcess, typesToProcess.get(0), null);
		}

		for (Iterator<UnwovenClassFile> i = input.getClassFileIterator(); i.hasNext();) {
			UnwovenClassFile classFile = i.next();
			if (classFile.shouldBeWoven()) {
				String className = classFile.getClassName();
				addNormalTypeMungers(className);
			}
		}

		CompilationAndWeavingContext.leavingPhase(typeMungingToken);

		requestor.weavingAspects();
		ContextToken aspectToken = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.WEAVING_ASPECTS, "");
		// first weave into aspects
		for (Iterator<UnwovenClassFile> i = input.getClassFileIterator(); i.hasNext();) {
			UnwovenClassFile classFile = i.next();
			if (classFile.shouldBeWoven()) {
				String className = classFile.getClassName();
				ResolvedType theType = world.resolve(className);
				if (theType.isAspect()) {
					BcelObjectType classType = BcelWorld.getBcelObjectType(theType);
					if (classType == null) {

						// Sometimes.. if the Bcel Delegate couldn't be found then a
						// problem occurred at compile time - on
						// a previous compiler run. In this case I assert the
						// delegate will still be an EclipseSourceType
						// and we can ignore the problem here (the original compile
						// error will be reported again from
						// the eclipse source type) - pr113531
						if (theType.hasEclipseSourceTypeDelegate()) {
							continue;
						}
						throw new BCException("Can't find bcel delegate for " + className + " type=" + theType.getClass());
					}
					weaveAndNotify(classFile, classType, requestor);
					wovenClassNames.add(className);
				}
			}
		}

		CompilationAndWeavingContext.leavingPhase(aspectToken);

		requestor.weavingClasses();
		ContextToken classToken = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.WEAVING_CLASSES, "");
		// then weave into non-aspects
		for (Iterator<UnwovenClassFile> i = input.getClassFileIterator(); i.hasNext();) {
			UnwovenClassFile classFile = i.next();
			if (classFile.shouldBeWoven()) {
				String className = classFile.getClassName();
				ResolvedType theType = world.resolve(className);
				if (!theType.isAspect()) {
					BcelObjectType classType = BcelWorld.getBcelObjectType(theType);
					if (classType == null) {

						// bug 119882 - see above comment for bug 113531
						ReferenceTypeDelegate theDelegate = ((ReferenceType) theType).getDelegate();

						// TODO urgh - put a method on the interface to check this, string compare is hideous
						if (theDelegate.getClass().getName().endsWith("EclipseSourceType")) {
							continue;
						}

						throw new BCException("Can't find bcel delegate for " + className + " type=" + theType.getClass());
					}
					weaveAndNotify(classFile, classType, requestor);
					wovenClassNames.add(className);
				}
			}
		}
		CompilationAndWeavingContext.leavingPhase(classToken);

		addedClasses.clear();
		deletedTypenames.clear();

		requestor.weaveCompleted();
		CompilationAndWeavingContext.leavingPhase(weaveToken);
		if (trace.isTraceEnabled()) {
			trace.exit("weave", wovenClassNames);
		}
		if (getWorld().getModel() != null && getWorld().isMinimalModel()) {
			candidatesForRemoval.clear();
		}
		return wovenClassNames;
	}

	
	/**
	 * 'typeToWeave' is one from the 'typesForWeaving' list. This routine ensures we process supertypes (classes/interfaces) of
	 * 'typeToWeave' that are in the 'typesForWeaving' list before 'typeToWeave' itself. 'typesToWeave' is then removed from the
	 * 'typesForWeaving' list.
	 *
	 * Note: Future gotcha in here ... when supplying partial hierarchies, this algorithm may break down. If you have a hierarchy
	 * A>B>C and only give A and C to the weaver, it may choose to weave them in either order - but you'll probably have other
	 * problems if you are supplying partial hierarchies like that !
	 */
	private void weaveParentsFor(List<String> typesForWeaving, String typeToWeave, ResolvedType resolvedTypeToWeave) {
		if (resolvedTypeToWeave == null) {
			// resolve it if the caller could not pass in the resolved type
			resolvedTypeToWeave = getWorld().resolve(typeToWeave);
		}
		ResolvedType superclassType = resolvedTypeToWeave.getSuperclass();
		String superclassTypename = (superclassType == null ? null : superclassType.getName());

		// PR336654 added the 'typesForWeaving.contains(superclassTypename)' clause.
		// Without it we can delete all type mungers on the parents and yet we only
		// add back in the declare parents related ones, not the regular ITDs.
		if (superclassType != null && !superclassType.isTypeHierarchyComplete() && superclassType.isExposedToWeaver()
				&& typesForWeaving.contains(superclassTypename)) {
			weaveParentsFor(typesForWeaving, superclassTypename, superclassType);
		}

		ResolvedType[] interfaceTypes = resolvedTypeToWeave.getDeclaredInterfaces();
		for (ResolvedType resolvedSuperInterface : interfaceTypes) {
			if (!resolvedSuperInterface.isTypeHierarchyComplete()) {
				String interfaceTypename = resolvedSuperInterface.getName();
				if (resolvedSuperInterface.isExposedToWeaver()) { // typesForWeaving.contains(interfaceTypename)) {
					weaveParentsFor(typesForWeaving, interfaceTypename, resolvedSuperInterface);
				}
			}
		}
		ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.PROCESSING_DECLARE_PARENTS,
				resolvedTypeToWeave.getName());
		// If A was processed before B (and was declared 'class A implements B') then there is no need to complete B again, it
		// will have been done whilst processing A.
		if (!resolvedTypeToWeave.isTypeHierarchyComplete()) {
			weaveParentTypeMungers(resolvedTypeToWeave);
		}
		CompilationAndWeavingContext.leavingPhase(tok);
		typesForWeaving.remove(typeToWeave);
		resolvedTypeToWeave.tagAsTypeHierarchyComplete();
	}
	

	/**
	 * Weaves new parents and annotations onto a type ("declare parents" and "declare @type")
	 *
	 * Algorithm: 1. First pass, do parents then do annotations. During this pass record: - any parent mungers that don't match but
	 * have a non-wild annotation type pattern - any annotation mungers that don't match 2. Multiple subsequent passes which go over
	 * the munger lists constructed in the first pass, repeatedly applying them until nothing changes. FIXME asc confirm that
	 * algorithm is optimal ??
	 */
	public void weaveParentTypeMungers(ResolvedType onType) {
		if (onType.isRawType() || onType.isParameterizedType()) {
			onType = onType.getGenericType();
		}
		onType.clearInterTypeMungers();

		List<DeclareParents> decpToRepeat = new ArrayList<>();

		boolean aParentChangeOccurred = false;
		boolean anAnnotationChangeOccurred = false;
		// First pass - apply all decp mungers
		for (DeclareParents decp : declareParentsList) {
			boolean typeChanged = applyDeclareParents(decp, onType);
			if (typeChanged) {
				aParentChangeOccurred = true;
			} else {
				decpToRepeat.add(decp);
			}
		}

		// Still first pass - apply all dec @type mungers
		for (DeclareAnnotation decA : xcutSet.getDeclareAnnotationOnTypes()) {
			boolean typeChanged = applyDeclareAtType(decA, onType, true);
			if (typeChanged) {
				anAnnotationChangeOccurred = true;
			}
		}

		while ((aParentChangeOccurred || anAnnotationChangeOccurred) && !decpToRepeat.isEmpty()) {
			anAnnotationChangeOccurred = aParentChangeOccurred = false;
			List<DeclareParents> decpToRepeatNextTime = new ArrayList<>();
			for (DeclareParents decp : decpToRepeat) {
				boolean typeChanged = applyDeclareParents(decp, onType);
				if (typeChanged) {
					aParentChangeOccurred = true;
				} else {
					decpToRepeatNextTime.add(decp);
				}
			}

			for (DeclareAnnotation decA : xcutSet.getDeclareAnnotationOnTypes()) {
				boolean typeChanged = applyDeclareAtType(decA, onType, false);
				if (typeChanged) {
					anAnnotationChangeOccurred = true;
				}
			}
			decpToRepeat = decpToRepeatNextTime;
		}
	}

	/**
	 * Apply a declare @type - return true if we change the type
	 */
	private boolean applyDeclareAtType(DeclareAnnotation decA, ResolvedType onType, boolean reportProblems) {
		boolean didSomething = false;
		if (decA.matches(onType)) {
			AnnotationAJ theAnnotation = decA.getAnnotation();
			// can be null for broken code!
			if (theAnnotation == null) {
				return false;
			}
			if (onType.hasAnnotation(theAnnotation.getType())) {
				// Could put out a lint here for an already annotated type ...
				// if (reportProblems) {
				// world.getLint().elementAlreadyAnnotated.signal(
				// new
				// String[]{onType.toString(),decA.getAnnotationTypeX().toString
				// ()},
				// onType.getSourceLocation(),new
				// ISourceLocation[]{decA.getSourceLocation()});
				// }
				return false;
			}

			AnnotationAJ annoX = decA.getAnnotation();

			// check the annotation is suitable for the target
			boolean problemReported = verifyTargetIsOK(decA, onType, annoX, reportProblems);

			if (!problemReported) {
				AsmRelationshipProvider.addDeclareAnnotationRelationship(getWorld().getModelAsAsmManager(), decA.getSourceLocation(),
						onType.getSourceLocation(), false);
				// TAG: WeavingMessage
				if (!getWorld().getMessageHandler().isIgnoring(IMessage.WEAVEINFO)) {
					getWorld().getMessageHandler().handleMessage(
							WeaveMessage.constructWeavingMessage(
									WeaveMessage.WEAVEMESSAGE_ANNOTATES,
									new String[] { onType.toString(), Utility.beautifyLocation(onType.getSourceLocation()),
											decA.getAnnotationString(), "type", decA.getAspect().toString(),
											Utility.beautifyLocation(decA.getSourceLocation()) }));
				}
				didSomething = true;
				ResolvedTypeMunger newAnnotationTM = new AnnotationOnTypeMunger(annoX);
				newAnnotationTM.setSourceLocation(decA.getSourceLocation());
				onType.addInterTypeMunger(new BcelTypeMunger(newAnnotationTM, decA.getAspect().resolve(getWorld())), false);
				decA.copyAnnotationTo(onType);
			}
		}
		return didSomething;
	}


	private void weaveAndNotify(UnwovenClassFile classFile, BcelObjectType classType, IWeaveRequestor requestor) throws IOException {
		trace.enter("weaveAndNotify", this, new Object[] { classFile, classType, requestor });

		ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.WEAVING_TYPE, classType
				.getResolvedTypeX().getName());
		LazyClassGen clazz = weaveWithoutDump(classFile, classType);
		classType.finishedWith();
		// clazz is null if the classfile was unchanged by weaving...
		if (clazz != null) {
			UnwovenClassFile[] newClasses = getClassFilesFor(clazz);
			// OPTIMIZE can we avoid using the string name at all in
			// UnwovenClassFile instances?
			// Copy the char[] across as it means the
			// WeaverAdapter.removeFromMap() can be fast!
			if (newClasses[0].getClassName().equals(classFile.getClassName())) {
				newClasses[0].setClassNameAsChars(classFile.getClassNameAsChars());
			}
			for (UnwovenClassFile newClass : newClasses) {
				requestor.acceptResult(newClass);
			}
		} else {
			requestor.acceptResult(classFile);
		}
		classType.weavingCompleted();
		CompilationAndWeavingContext.leavingPhase(tok);

		trace.exit("weaveAndNotify");
	}

	public void addParentTypeMungers(String typeName) {
		weaveParentTypeMungers(getWorld().resolve(typeName));
	}

	public void addNormalTypeMungers(String typeName) {
		weaveNormalTypeMungers(getWorld().resolve(typeName));
	}

	/**
	 * Apply a single declare parents - return true if we change the type
	 */
	private boolean applyDeclareParents(DeclareParents p, ResolvedType onType) {
		boolean didSomething = false;
		List<ResolvedType> newParents = p.findMatchingNewParents(onType, true);
		if (!newParents.isEmpty()) {
			didSomething = true;
			BcelWorld.getBcelObjectType(onType);
			// System.err.println("need to do declare parents for: " + onType);
			for (ResolvedType newParent : newParents) {
				// We set it here so that the imminent matching for ITDs can
				// succeed - we still haven't done the necessary changes to the class file
				// itself (like transform super calls) - that is done in
				// BcelTypeMunger.mungeNewParent()
				// classType.addParent(newParent);
				onType.addParent(newParent);
				NewParentTypeMunger newParentMunger = new NewParentTypeMunger(newParent, p.getDeclaringType());
				if (p.isMixin()) {
					newParentMunger.setIsMixin(true);
				}
				newParentMunger.setSourceLocation(p.getSourceLocation());
				onType.addInterTypeMunger(new BcelTypeMunger(newParentMunger, xcutSet.findAspectDeclaringParents(p)), false);
			}
		}
		return didSomething;
	}

	public void weaveNormalTypeMungers(ResolvedType onType) {
		ContextToken tok = CompilationAndWeavingContext.enteringPhase(CompilationAndWeavingContext.PROCESSING_TYPE_MUNGERS,
				onType.getName());
		if (onType.isRawType() || onType.isParameterizedType()) {
			onType = onType.getGenericType();
		}
		for (ConcreteTypeMunger m : typeMungerList) {
			if (!m.isLateMunger() && m.matches(onType)) {
				onType.addInterTypeMunger(m, false);
			}
		}
		CompilationAndWeavingContext.leavingPhase(tok);
	}

	public UnwovenClassFile[] getClassFilesFor(LazyClassGen clazz) {
		List<UnwovenClassFile.ChildClass> childClasses = clazz.getChildClasses(getWorld());
		UnwovenClassFile[] ret = new UnwovenClassFile[1 + childClasses.size()];
		ret[0] = makeUnwovenClassFile(clazz.getFileName(), clazz.getClassName(), clazz.getJavaClassBytesIncludingReweavable(getWorld()));
//		ret[0] = new BcelUnwovenClassFile(clazz.getFileName(), clazz.getClassName(), clazz.getJavaClassBytesIncludingReweavable(getWorld()));
		int index = 1;
		for (UnwovenClassFile.ChildClass element : childClasses) {
			UnwovenClassFile childClass = makeUnwovenClassFile(clazz.getFileName() + "$" + element.name, element.bytes);
			ret[index++] = childClass;
		}
		return ret;
	}

	protected abstract LazyClassGen weave(UnwovenClassFile classFile, BcelObjectType classType, boolean dump) throws IOException;
	
	// exposed for ClassLoader dynamic weaving
	public LazyClassGen weaveWithoutDump(UnwovenClassFile classFile, BcelObjectType classType) throws IOException {
		return weave(classFile, classType, false);
	}

	public void prepareToProcessReweavableState() {
	}

	public void processReweavableStateIfPresent(String className, BcelObjectType classType) {
		// If the class is marked reweavable, check any aspects around when it
		// was built are in this world
		WeaverStateInfo wsi = classType.getWeaverState();
		// System.out.println(">> processReweavableStateIfPresent " + className + " wsi=" + wsi);
		if (wsi != null && wsi.isReweavable()) { // Check all necessary types
			// are around!
			getWorld().showMessage(IMessage.INFO, WeaverMessages.format(WeaverMessages.PROCESSING_REWEAVABLE, className, classType
					.getSourceLocation().getSourceFile()), null, null);
			Set<String> aspectsPreviouslyInWorld = wsi.getAspectsAffectingType();
			// keep track of them just to ensure unique missing aspect error
			// reporting
			Set<String> alreadyConfirmedReweavableState = new HashSet<>();
			for (String requiredTypeSignature : aspectsPreviouslyInWorld) {
				// for (Iterator iter = aspectsPreviouslyInWorld.iterator(); iter.hasNext();) {
				// String requiredTypeName = (String) iter.next();
				if (!alreadyConfirmedReweavableState.contains(requiredTypeSignature)) {
					ResolvedType rtx = getWorld().resolve(UnresolvedType.forSignature(requiredTypeSignature), true);
					boolean exists = !rtx.isMissing();
					if (!getWorld().isOverWeaving()) {
						if (!exists) {
							getWorld().getLint().missingAspectForReweaving.signal(new String[] { rtx.getName(), className },
								classType.getSourceLocation(), null);
							// world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.MISSING_REWEAVABLE_TYPE,
							// requiredTypeName, className), classType.getSourceLocation(), null);
						} else {
							// weaved in aspect that are not declared in aop.xml
							// trigger an error for now
							// may cause headhache for LTW and packaged lib
							// without aop.xml in
							// see #104218
							if (!xcutSet.containsAspect(rtx)) {
								getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(
										WeaverMessages.REWEAVABLE_ASPECT_NOT_REGISTERED, rtx.getName(), className), null, null);
							} else if (!getWorld().getMessageHandler().isIgnoring(IMessage.INFO)) {
								getWorld().showMessage(IMessage.INFO, WeaverMessages.format(WeaverMessages.VERIFIED_REWEAVABLE_TYPE,
										rtx.getName(), rtx.getSourceLocation().getSourceFile()), null, null);
							}
							alreadyConfirmedReweavableState.add(requiredTypeSignature);
						}
					}
				}
			}
			// old:
			// classType.setJavaClass(Utility.makeJavaClass(classType.getJavaClass
			// ().getFileName(), wsi.getUnwovenClassFileData()));
			// new: reweavable default with clever diff
			if (!getWorld().isOverWeaving()) {
				byte[] ucfd = wsi.getUnwovenClassFileData();
				if (ucfd.length == 0) {
					// Size 0 indicates the class was previously overwoven, so you need to be overweaving now!
					getWorld().getMessageHandler().handleMessage(
							MessageUtil.error(
							WeaverMessages.format(WeaverMessages.MUST_KEEP_OVERWEAVING_ONCE_START,
									className)));
//									onType.getName(), annoX.getTypeName(), annoX.getValidTargets()),
//							decA.getSourceLocation()));
				} else {
					byte[] bytes = wsi.getUnwovenClassFileData(classType.getJavaClass().getBytes());
					JavaClass newJavaClass = Utility.makeJavaClass(classType.getJavaClass().getFileName(), bytes);
					classType.setJavaClass(newJavaClass, true);
					classType.getResolvedTypeX().ensureConsistent();
				}
			}
			// } else {
			// classType.resetState();
		}
	}



	/**
	 * Checks for an @target() on the annotation and if found ensures it allows the annotation to be attached to the target type
	 * that matched.
	 */
	private boolean verifyTargetIsOK(DeclareAnnotation decA, ResolvedType onType, AnnotationAJ annoX, boolean outputProblems) {
		boolean problemReported = false;
		if (annoX.specifiesTarget()) {
			if ((onType.isAnnotation() && !annoX.allowedOnAnnotationType()) || (!annoX.allowedOnRegularType())) {
				if (outputProblems) {
					if (decA.isExactPattern()) {
						getWorld().getMessageHandler().handleMessage(
								MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INCORRECT_TARGET_FOR_DECLARE_ANNOTATION,
												onType.getName(), annoX.getTypeName(), annoX.getValidTargets()),
										decA.getSourceLocation()));
					} else {
						if (getWorld().getLint().invalidTargetForAnnotation.isEnabled()) {
							getWorld().getLint().invalidTargetForAnnotation.signal(new String[] { onType.getName(), annoX.getTypeName(),
									annoX.getValidTargets() }, decA.getSourceLocation(),
									new ISourceLocation[] { onType.getSourceLocation() });
						}
					}
				}
				problemReported = true;
			}
		}
		return problemReported;
	}
	

	/**
	 * helper method - will return NULL if the underlying delegate is an EclipseSourceType and not a BcelObjectType
	 */
	public BcelObjectType getClassType(String forClass) {
		return BcelWorld.getBcelObjectType(getWorld().resolve(forClass));
	}


	/**
	 * Perform a fast match of the specified list of shadowmungers against the specified type. A subset of those that might match is
	 * returned.
	 *
	 * @param list list of all shadow mungers that might match
	 * @param type the target type
	 * @return a list of shadow mungers that might match with those that cannot (according to fast match rules) removed
	 */
	protected List<ShadowMunger> fastMatch(List<ShadowMunger> list, ResolvedType type) {
		if (list == null) {
			return Collections.emptyList();
		}
		boolean isOverweaving = getWorld().isOverWeaving();
		WeaverStateInfo typeWeaverState = (isOverweaving ? type.getWeaverState() : null);

		// here we do the coarsest grained fast match with no kind constraints
		// this will remove all obvious non-matches and see if we need to do any
		// weaving
		FastMatchInfo info = new FastMatchInfo(type, null, getWorld());

		List<ShadowMunger> result = new ArrayList<>();

		if (getWorld().areInfoMessagesEnabled() && getWorld().isTimingEnabled()) {
			for (ShadowMunger munger : list) {
				if (typeWeaverState != null) { // will only be null if overweaving is ON and there is weaverstate
					ResolvedType declaringAspect = munger.getDeclaringType();
					if (typeWeaverState.isAspectAlreadyApplied(declaringAspect)) {
						continue;
					}
				}
				Pointcut pointcut = munger.getPointcut();
				long starttime = System.nanoTime();
				FuzzyBoolean fb = pointcut.fastMatch(info);
				long endtime = System.nanoTime();
				getWorld().recordFastMatch(pointcut, endtime - starttime);
				if (fb.maybeTrue()) {
					result.add(munger);
				}
			}
		} else {
			for (ShadowMunger munger : list) {
				if (typeWeaverState != null) { // will only be null if overweaving is ON and there is weaverstate
					ResolvedType declaringAspect = munger.getConcreteAspect();// getDeclaringType();
					if (typeWeaverState.isAspectAlreadyApplied(declaringAspect)) {
						continue;
					}
				}
				Pointcut pointcut = munger.getPointcut();
				FuzzyBoolean fb = pointcut.fastMatch(info);
				if (fb.maybeTrue()) {
					result.add(munger);
				}
			}
		}
		return result;
	}


	protected void checkDeclareTypeErrorOrWarning(BytecodeWorld world2, BcelObjectType classType) {
		List<DeclareTypeErrorOrWarning> dteows = getWorld().getDeclareTypeEows();
		for (DeclareTypeErrorOrWarning dteow : dteows) {
			if (dteow.getTypePattern().matchesStatically(classType.getResolvedTypeX())) {
				if (dteow.isError()) {
					getWorld().getMessageHandler().handleMessage(
							MessageUtil.error(dteow.getMessage(), classType.getResolvedTypeX().getSourceLocation()));
				} else {
					getWorld().getMessageHandler().handleMessage(
							MessageUtil.warn(dteow.getMessage(), classType.getResolvedTypeX().getSourceLocation()));
				}
			}
		}
	}
	
	protected abstract Clazz makeClazz(String filename, byte[] bytes);
	protected void removeSourceFileIfNoMoreTypeDeclarationsInside(AspectJElementHierarchy hierarchy, IProgramElement typeElement,
			IProgramElement sourceFileNode) {
		IProgramElement compilationUnit = sourceFileNode;
		boolean anyOtherTypeDeclarations = false;
		for (IProgramElement child : compilationUnit.getChildren()) {
			IProgramElement.Kind k = child.getKind();
			if (k.isType()) {
				anyOtherTypeDeclarations = true;
				break;
			}
		}
		// If the compilation unit node contained no
		// other types, there is no need to keep it
		if (!anyOtherTypeDeclarations) {
			IProgramElement cuParent = compilationUnit.getParent();
			if (cuParent != null) {
				compilationUnit.setParent(null);
				cuParent.removeChild(compilationUnit);
			}
			// need to update some caches and structures too?
			hierarchy.forget(sourceFileNode, typeElement);
		} else {
			hierarchy.forget(null, typeElement);
		}
	}

	public void tidyUp() {
		if (trace.isTraceEnabled()) {
			trace.enter("tidyUp", this);
		}
		shadowMungerList = null; // setup by prepareForWeave
		typeMungerList = null; // setup by prepareForWeave
		lateTypeMungerList = null; // setup by prepareForWeave
		declareParentsList = null; // setup by prepareForWeave
		if (trace.isTraceEnabled()) {
			trace.exit("tidyUp");
		}
	}

	public boolean isReweavable() {
		return inReweavableMode;
	}

	public void setReweavableMode(boolean xNotReweavable) {
		inReweavableMode = !xNotReweavable;
		WeaverStateInfo.setReweavableModeDefaults(!xNotReweavable, false, true);
	}

	protected void dumpUnchanged(UnwovenClassFile classFile) throws IOException {
		if (zipOutputStream != null) {
			writeZipEntry(getEntryName(classFile.getJavaClass().getClassName()), classFile.getBytes());
		} else {
			classFile.writeUnchangedBytes();
		}
	}

	private String getEntryName(String className) {
		// XXX what does bcel's getClassName do for inner names
		return className.replace('.', '/') + ".class";
	}

	// FOR TESTING
	public Collection<String> weave(File file) throws IOException {
		OutputStream os = FileUtil.makeOutputStream(file);
		this.zipOutputStream = new ZipOutputStream(os);
		prepareForWeave();
		Collection<String> c = weave(new IClassFileProvider() {

			public boolean isApplyAtAspectJMungersOnly() {
				return false;
			}

			public Iterator<UnwovenClassFile> getClassFileIterator() {
				return addedClasses.iterator();
			}

			public IWeaveRequestor getRequestor() {
				return new WeaveRequestor();
			}
		});
		// /* BUG 40943*/
		// dumpResourcesToOutJar();
		zipOutputStream.close(); // this flushes and closes the acutal file
		return c;
	}

	protected void dump(UnwovenClassFile classFile, LazyClassGen clazz) throws IOException {
		if (zipOutputStream != null) {
			String mainClassName = classFile.getJavaClass().getClassName();
			writeZipEntry(getEntryName(mainClassName), clazz.getJavaClass(getWorld()).getBytes());
			List<UnwovenClassFile.ChildClass> childClasses = clazz.getChildClasses(getWorld());
			if (!childClasses.isEmpty()) {
				for (UnwovenClassFile.ChildClass c : childClasses) {
					writeZipEntry(getEntryName(mainClassName + "$" + c.name), c.bytes);
				}
			}
		} else {
			classFile.writeWovenBytes(clazz.getJavaClass(getWorld()).getBytes(), clazz.getChildClasses(getWorld()));
		}
	}
	public void writeZipEntry(String name, byte[] bytes) throws IOException {
		ZipEntry newEntry = new ZipEntry(name); // ??? get compression scheme
		// right

		zipOutputStream.putNextEntry(newEntry);
		zipOutputStream.write(bytes);
		zipOutputStream.closeEntry();
	}
	

	private class WeaveRequestor implements IWeaveRequestor {
		public void acceptResult(IUnwovenClassFile result) {
			try {
				writeZipEntry(result.getFilename(), result.getBytes());
			} catch (IOException ex) {
			}
		}

		public void processingReweavableState() {}

		public void addingTypeMungers() {}

		public void weavingAspects() {}

		public void weavingClasses() {}

		public void weaveCompleted() {}
	}

	public void addManifest(Manifest newManifest) {
		// System.out.println("? addManifest() newManifest=" + newManifest);
		if (manifest == null) {
			manifest = newManifest;
		}
	}

	protected void walkUpRemovingEmptyTypesAndPossiblyEmptySourceFile(AspectJElementHierarchy hierarchy, String tname,
			IProgramElement typeThatHasChildRemoved) {
		// typeThatHasChildRemoved might be a source file, type or a method/ctor
		// - for a method/ctor find the type/sourcefile
		while (typeThatHasChildRemoved != null
				&& !(typeThatHasChildRemoved.getKind().isType() || typeThatHasChildRemoved.getKind().isSourceFile())) {
			// this will take us 'up' through methods that contain anonymous
			// inner classes
			typeThatHasChildRemoved = typeThatHasChildRemoved.getParent();
		}
		// now typeThatHasChildRemoved points to the type or sourcefile that has
		// had something removed
		if (candidatesForRemoval.contains(typeThatHasChildRemoved) && !hasInnerType(typeThatHasChildRemoved)) {
			// now we can get rid of it
			IProgramElement parent = typeThatHasChildRemoved.getParent();
			if (parent != null) {
				parent.removeChild(typeThatHasChildRemoved);
				candidatesForRemoval.remove(typeThatHasChildRemoved);
				if (parent.getKind().isSourceFile()) {
					removeSourceFileIfNoMoreTypeDeclarationsInside(hierarchy, typeThatHasChildRemoved, parent);
					// System.out.println("Removed on second pass: " +
					// typeThatHasChildRemoved.getName());
				} else {
					// System.out.println("On later pass, parent of type " +
					// typeThatHasChildRemoved.getName()
					// + " was found not to be a sourcefile, recursing up...");
					walkUpRemovingEmptyTypesAndPossiblyEmptySourceFile(hierarchy, tname, parent);
				}
			}
		}
	}

	// TODO could be smarter - really only matters if inner type has been woven, but there is a chance we haven't woven it *yet*
	protected boolean hasInnerType(IProgramElement typeNode) {
		for (IProgramElement child : typeNode.getChildren()) {
			IProgramElement.Kind kind = child.getKind();
			if (kind.isType()) {
				return true;
			}
			// if (kind == IProgramElement.Kind.ASPECT) {
			// return true;
			// }
			if (kind.isType() || kind == IProgramElement.Kind.METHOD || kind == IProgramElement.Kind.CONSTRUCTOR) {
				boolean b = hasInnerType(child);
				if (b) {
					return b;
				}
			}
		}
		return false;
	}

	/**
	 * Determine if the supplied bytes represent an aspect, if they do then create a ResolvedType instance for the aspect and return
	 * it, otherwise return null
	 *
	 * @param classbytes the classbytes that might represent an aspect
	 * @param name the name of the class
	 * @param directory directory which contained the class file
	 * @return a ResolvedType if the classbytes represent an aspect, otherwise null
	 */
	protected ResolvedType isAspect(byte[] classbytes, String name, File dir) throws IOException {
		Clazz clazz = makeClazz(name, classbytes);
		ResolvedType type = getWorld().addSourceObjectType(clazz, false).getResolvedTypeX();
		String typeName = type.getName().replace('.', File.separatorChar);
		int end = name.lastIndexOf(typeName + ".class");
		String binaryPath = null;
		// if end is -1 then something weird happened, the class file is not in
		// the correct place, something like
		// bin/A.class when the declaration for A specifies it is in a package.
		if (end == -1) {
			binaryPath = dir.getAbsolutePath();
		} else {
			binaryPath = name.substring(0, end - 1);
		}
		type.setBinaryPath(binaryPath);
		if (type.isAspect()) {
			return type;
		} else {
			// immediately demote the type we just added since it will have
			// have been stuffed into the permanent map (assumed to be
			// an aspect)
			getWorld().demote(type);
			return null;
		}
	}

	/**
	 * Look for .class files that represent aspects in the supplied directory - return the list of accumulated aspects.
	 *
	 * @param directory the directory in which to look for Aspect .class files
	 * @return the list of discovered aspects
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected List<ResolvedType> addAspectsFromDirectory(File directory) throws FileNotFoundException, IOException {
		List<ResolvedType> addedAspects = new ArrayList<>();
		File[] classFiles = FileUtil.listFiles(directory, new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".class");
			}
		});
		for (File classFile : classFiles) {
			FileInputStream fis = new FileInputStream(classFile);
			byte[] classBytes = FileUtil.readAsByteArray(fis);
			ResolvedType aspectType = isAspect(classBytes, classFile.getAbsolutePath(), directory);
			if (aspectType != null) {
				addedAspects.add(aspectType);
			}
			fis.close();
		}
		return addedAspects;
	}

	/**
	 *
	 * @param inFile directory containing classes or zip/jar class archive
	 */
	public void addLibraryJarFile(File inFile) throws IOException {
		List<ResolvedType> addedAspects = null;
		if (inFile.isDirectory()) {
			addedAspects = addAspectsFromDirectory(inFile);
		} else {
			addedAspects = addAspectsFromJarFile(inFile);
		}
		for (ResolvedType addedAspect : addedAspects) {
			xcutSet.addOrReplaceAspect(addedAspect);
		}
	}

	private List<ResolvedType> addAspectsFromJarFile(File inFile) throws FileNotFoundException, IOException {
		ZipInputStream inStream = new ZipInputStream(new FileInputStream(inFile)); // ??? buffered
		List<ResolvedType> addedAspects = new ArrayList<>();
		try {
			while (true) {
				ZipEntry entry = inStream.getNextEntry();
				if (entry == null) {
					break;
				}

				if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
					continue;
				}

				// FIXME ASC performance? of this alternative soln.
				int size = (int) entry.getSize();
				Clazz clazz = makeClazz(entry.getName(), FileUtil.readAsByteArray(inStream));
				inStream.closeEntry();
				ResolvedType type = getWorld().addSourceObjectType(clazz, false).getResolvedTypeX();
				type.setBinaryPath(inFile.getAbsolutePath());
				if (type.isAspect()) {
					addedAspects.add(type);
				} else {
					getWorld().demote(type);
				}

			}
		} finally {
			inStream.close();
		}
		return addedAspects;
	}
	
	public abstract UnwovenClassFile makeUnwovenClassFile(String filename, byte[] bytes);

	public abstract UnwovenClassFile makeUnwovenClassFile(String filename, String classname, byte[] bytes);

	/**
	 * Adds all class files in the jar
	 */
	public List<UnwovenClassFile> addJarFile(File inFile, File outDir, boolean canBeDirectory) {
		// System.err.println("? addJarFile(" + inFile + ", " + outDir + ")");
		List<UnwovenClassFile> addedClassFiles = new ArrayList<>();
		needToReweaveWorld = true;
		JarFile inJar = null;

		try {
			// Is this a directory we are looking at?
			if (inFile.isDirectory() && canBeDirectory) {
				addedClassFiles.addAll(addDirectoryContents(inFile, outDir));
			} else {

				inJar = new JarFile(inFile);
				try {
					addManifest(inJar.getManifest());
					Enumeration<JarEntry> entries = inJar.entries();

					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						InputStream inStream = inJar.getInputStream(entry);

						byte[] bytes = FileUtil.readAsByteArray(inStream);
						String filename = entry.getName();
						// System.out.println("? addJarFile() filename='" + filename
						// + "'");
						UnwovenClassFile classFile = makeUnwovenClassFile(new File(outDir, filename).getAbsolutePath(), bytes);

						if (filename.endsWith(".class")) {
							ReferenceType type = this.addClassFile(classFile, false);
							StringBuilder sb = new StringBuilder();
							sb.append(inFile.getAbsolutePath());
							sb.append("!");
							sb.append(entry.getName());
							type.setBinaryPath(sb.toString());
							addedClassFiles.add(classFile);
						}
						// else if (!entry.isDirectory()) {
						//
						// /* bug-44190 Copy meta-data */
						// addResource(filename,classFile);
						// }

						inStream.close();
					}
				} finally {
					inJar.close();
				}
				inJar.close();
			}
		} catch (FileNotFoundException ex) {
			IMessage message = new Message("Could not find input jar file " + inFile.getPath() + ", ignoring", new SourceLocation(
					inFile, 0), false);
			getWorld().getMessageHandler().handleMessage(message);
		} catch (IOException ex) {
			IMessage message = new Message("Could not read input jar file " + inFile.getPath() + "(" + ex.getMessage() + ")",
					new SourceLocation(inFile, 0), true);
			getWorld().getMessageHandler().handleMessage(message);
		} finally {
			if (inJar != null) {
				try {
					inJar.close();
				} catch (IOException ex) {
					IMessage message = new Message("Could not close input jar file " + inFile.getPath() + "(" + ex.getMessage()
							+ ")", new SourceLocation(inFile, 0), true);
					getWorld().getMessageHandler().handleMessage(message);
				}
			}
		}

		return addedClassFiles;
	}

	/**
	 * Add any .class files in the directory to the outdir. Anything other than .class files in the directory (or its
	 * subdirectories) are considered resources and are also copied.
	 *
	 */
	public List<UnwovenClassFile> addDirectoryContents(File inFile, File outDir) throws IOException {
		List<UnwovenClassFile> addedClassFiles = new ArrayList<>();

		// Get a list of all files (i.e. everything that isnt a directory)
		File[] files = FileUtil.listFiles(inFile, new FileFilter() {
			public boolean accept(File f) {
				boolean accept = !f.isDirectory();
				return accept;
			}
		});

		// For each file, add it either as a real .class file or as a resource
		for (File file : files) {
			addedClassFiles.add(addClassFile(file, inFile, outDir));
		}

		return addedClassFiles;
	}

	public UnwovenClassFile addClassFile(File classFile, File inPathDir, File outDir) throws IOException {
		FileInputStream fis = new FileInputStream(classFile);
		byte[] bytes = FileUtil.readAsByteArray(fis);
		// String relativePath = files[i].getPath();

		// ASSERT:
		// files[i].getAbsolutePath().startsWith(inFile.getAbsolutePath()
		// or we are in trouble...
		String filename = classFile.getAbsolutePath().substring(inPathDir.getAbsolutePath().length() + 1);
		UnwovenClassFile ucf = makeUnwovenClassFile(new File(outDir, filename).getAbsolutePath(), bytes);
		if (filename.endsWith(".class")) {
			// System.err.println(
			// "BCELWeaver: processing class from input directory "+classFile);
			StringBuilder sb = new StringBuilder();
			sb.append(inPathDir.getAbsolutePath());
			sb.append("!");
			sb.append(filename);
			ReferenceType type = this.addClassFile(ucf, false);
			type.setBinaryPath(sb.toString());
		}
		fis.close();
		return ucf;
	}
}
