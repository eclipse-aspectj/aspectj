/* *******************************************************************
 * Copyright (c) 2002-2009 Contributors
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.DeclareTypeErrorOrWarning;
import org.aspectj.weaver.patterns.IVerificationRequired;

/**
 * This holds on to all CrosscuttingMembers for a world. It handles management of change.
 * 
 * @author Jim Hugunin
 * @author Andy Clement
 */
public class CrosscuttingMembersSet {

	private transient World world;

	// FIXME AV - ? we may need a sequencedHashMap there to ensure source based precedence for @AJ advice
	private final Map<ResolvedType, CrosscuttingMembers> members = new HashMap<>();

	// List of things to be verified once the type system is 'complete'
	private transient List<IVerificationRequired> verificationList = null;

	private List<ShadowMunger> shadowMungers = null;
	private List<ConcreteTypeMunger> typeMungers = null;
	private List<ConcreteTypeMunger> lateTypeMungers = null;
	private List<DeclareSoft> declareSofts = null;
	private List<DeclareParents> declareParents = null;
	private List<DeclareAnnotation> declareAnnotationOnTypes = null;
	private List<DeclareAnnotation> declareAnnotationOnFields = null;
	private List<DeclareAnnotation> declareAnnotationOnMethods = null; // includes constructors
	private List<DeclareTypeErrorOrWarning> declareTypeEows = null;
	private List<Declare> declareDominates = null;
	private boolean changedSinceLastReset = false;

	public CrosscuttingMembersSet(World world) {
		this.world = world;
	}

	public boolean addOrReplaceAspect(ResolvedType aspectType) {
		return addOrReplaceAspect(aspectType, true);
	}
	
	/**
	 * Check if any parent aspects of the supplied aspect have unresolved dependencies (and so
	 * should cause this aspect to be turned off).
	 * @param aspectType the aspect whose parents should be checked
	 * @return true if this aspect should be excluded because of a parents' missing dependencies
	 */
	private boolean excludeDueToParentAspectHavingUnresolvedDependency(ResolvedType aspectType) {
		ResolvedType parent = aspectType.getSuperclass();
		boolean excludeDueToParent = false;
		while (parent != null) {
			if (parent.isAspect() && parent.isAbstract() && world.hasUnsatisfiedDependency(parent)) {
				if (!world.getMessageHandler().isIgnoring(IMessage.INFO)) {
					world.getMessageHandler().handleMessage(
							MessageUtil.info("deactivating aspect '" + aspectType.getName() + "' as the parent aspect '"+parent.getName()+
									"' has unsatisfied dependencies"));
				}
				excludeDueToParent = true;
			}
			parent = parent.getSuperclass();
		}
		return excludeDueToParent;
	}

	/**
	 * @return whether or not that was a change to the global signature XXX for efficiency we will need a richer representation than
	 *         this
	 */
	public boolean addOrReplaceAspect(ResolvedType aspectType, boolean inWeavingPhase) {
		if (!world.isAspectIncluded(aspectType)) {
			return false;
		}
		if (world.hasUnsatisfiedDependency(aspectType)) {
			return false;				
		}
		// Abstract super aspects might have unsatisfied dependencies
		if (excludeDueToParentAspectHavingUnresolvedDependency(aspectType)) {
			return false;
		}
		
		boolean change = false;
		CrosscuttingMembers xcut = members.get(aspectType);
		if (xcut == null) {
			members.put(aspectType, aspectType.collectCrosscuttingMembers(inWeavingPhase));
			clearCaches();
			change = true;
		} else {
			if (xcut.replaceWith(aspectType.collectCrosscuttingMembers(inWeavingPhase), inWeavingPhase)) {
				clearCaches();
				change = true;
			} else {
				if (inWeavingPhase) {
					// bug 134541 - even though we haven't changed we may have updated the
					// sourcelocation for the shadowMunger which we need to pick up
					shadowMungers = null;
				}
				change = false;
			}
		}
		if (aspectType.isAbstract()) {
			// we might have sub-aspects that need to re-collect their crosscutting members from us
			boolean ancestorChange = addOrReplaceDescendantsOf(aspectType, inWeavingPhase);
			change = change || ancestorChange;
		}
		changedSinceLastReset = changedSinceLastReset || change;

		return change;
	}
	
	private boolean addOrReplaceDescendantsOf(ResolvedType aspectType, boolean inWeavePhase) {
		// System.err.println("Looking at descendants of "+aspectType.getName());
		Set<ResolvedType> knownAspects = members.keySet();
		Set<ResolvedType> toBeReplaced = new HashSet<>();
		for (ResolvedType candidateDescendant : knownAspects) {
			// allowMissing = true - if something is missing, it really probably is not a descendant
			if ((candidateDescendant != aspectType) && (aspectType.isAssignableFrom(candidateDescendant, true))) {
				toBeReplaced.add(candidateDescendant);
			}
		}
		boolean change = false;
		for (ResolvedType next : toBeReplaced) {
			boolean thisChange = addOrReplaceAspect(next, inWeavePhase);
			change = change || thisChange;
		}
		return change;
	}

	public void addAdviceLikeDeclares(ResolvedType aspectType) {
		if (!members.containsKey(aspectType)) {
			return;
		}
		CrosscuttingMembers xcut = members.get(aspectType);
		xcut.addDeclares(aspectType.collectDeclares(true));
	}

	public boolean deleteAspect(UnresolvedType aspectType) {
		boolean isAspect = members.remove(aspectType) != null;
		clearCaches();
		return isAspect;
	}

	public boolean containsAspect(UnresolvedType aspectType) {
		return members.containsKey(aspectType);
	}

	// XXX only for testing
	public void addFixedCrosscuttingMembers(ResolvedType aspectType) {
		members.put(aspectType, aspectType.crosscuttingMembers);
		clearCaches();
	}

	private void clearCaches() {
		shadowMungers = null;
		typeMungers = null;
		lateTypeMungers = null;
		declareSofts = null;
		declareParents = null;
		declareAnnotationOnFields = null;
		declareAnnotationOnMethods = null;
		declareAnnotationOnTypes = null;
		declareDominates = null;
	}

	public List<ShadowMunger> getShadowMungers() {
		if (shadowMungers == null) {
			List<ShadowMunger> ret = new ArrayList<>();
			for (CrosscuttingMembers crosscuttingMembers : members.values()) {
				ret.addAll(crosscuttingMembers.getShadowMungers());
			}
			shadowMungers = ret;
		}
		return shadowMungers;
	}

	public List<ConcreteTypeMunger> getTypeMungers() {
		if (typeMungers == null) {
			List<ConcreteTypeMunger> ret = new ArrayList<>();
			for (CrosscuttingMembers xmembers : members.values()) {
				// With 1.6.9 there is a change that enables use of more optimal accessors (accessors for private fields).
				// Here is where we determine if two aspects are asking for access to the same field. If they are
				// and
				// In the new style multiple aspects can share the same privileged accessors, so here we check if
				// two aspects are asking for access to the same field. If they are then we don't add a duplicate
				// accessor.
				for (ConcreteTypeMunger mungerToAdd : xmembers.getTypeMungers()) {
					ResolvedTypeMunger resolvedMungerToAdd = mungerToAdd.getMunger();
					if (isNewStylePrivilegedAccessMunger(resolvedMungerToAdd)) {
						String newFieldName = resolvedMungerToAdd.getSignature().getName();
						boolean alreadyExists = false;
						for (ConcreteTypeMunger existingMunger : ret) {
							ResolvedTypeMunger existing = existingMunger.getMunger();
							if (isNewStylePrivilegedAccessMunger(existing)) {
								String existingFieldName = existing.getSignature().getName();
								if (existingFieldName.equals(newFieldName)
										&& existing.getSignature().getDeclaringType().equals(
												resolvedMungerToAdd.getSignature().getDeclaringType())) {
									alreadyExists = true;
									break;
								}
							}
						}
						if (!alreadyExists) {
							ret.add(mungerToAdd);
						}
					} else {
						ret.add(mungerToAdd);
					}
				}
			}
			typeMungers = ret;
		}
		return typeMungers;
	}

	/**
	 * Retrieve a subset of all known mungers, those of a specific kind.
	 * 
	 * @param kind the kind of munger requested
	 * @return a list of those mungers (list is empty if none found)
	 */
	public List<ConcreteTypeMunger> getTypeMungersOfKind(ResolvedTypeMunger.Kind kind) {
		List<ConcreteTypeMunger> collected = null;
		for (ConcreteTypeMunger typeMunger : typeMungers) {
			if (typeMunger.getMunger() != null && typeMunger.getMunger().getKind() == kind) {
				if (collected == null) {
					collected = new ArrayList<>();
				}
				collected.add(typeMunger);
			}
		}
		if (collected == null) {
			return Collections.emptyList();
		} else {
			return collected;
		}
	}

	/**
	 * Determine if the type munger is: (1) for privileged access (2) for a normally non visible field (3) is from an aspect wanting
	 * 'old style' (ie. long) accessor names
	 */
	private boolean isNewStylePrivilegedAccessMunger(ResolvedTypeMunger typeMunger) {
		boolean b = (typeMunger != null && typeMunger.getKind() == ResolvedTypeMunger.PrivilegedAccess && typeMunger.getSignature()
				.getKind() == Member.FIELD);
		if (!b) {
			return b;
		}
		PrivilegedAccessMunger privAccessMunger = (PrivilegedAccessMunger) typeMunger;
		return privAccessMunger.shortSyntax;
	}

	public List<ConcreteTypeMunger> getLateTypeMungers() {
		if (lateTypeMungers == null) {
			List<ConcreteTypeMunger> ret = new ArrayList<>();
			for (CrosscuttingMembers crosscuttingMembers : members.values()) {
				ret.addAll(crosscuttingMembers.getLateTypeMungers());
			}
			lateTypeMungers = ret;
		}
		return lateTypeMungers;
	}

	public List<DeclareSoft> getDeclareSofts() {
		if (declareSofts == null) {
			Set<DeclareSoft> ret = new HashSet<>();
			for (CrosscuttingMembers crosscuttingMembers : members.values()) {
				ret.addAll(crosscuttingMembers.getDeclareSofts());
			}
			declareSofts = new ArrayList<>();
			declareSofts.addAll(ret);
		}
		return declareSofts;
	}

	public List<DeclareParents> getDeclareParents() {
		if (declareParents == null) {
			Set<DeclareParents> ret = new HashSet<>();
			for (CrosscuttingMembers crosscuttingMembers : members.values()) {
				ret.addAll(crosscuttingMembers.getDeclareParents());
			}
			declareParents = new ArrayList<>();
			declareParents.addAll(ret);
		}
		return declareParents;
	}

	/**
	 * @return an amalgamation of the declare @type statements.
	 */
	public List<DeclareAnnotation> getDeclareAnnotationOnTypes() {
		if (declareAnnotationOnTypes == null) {
			Set<DeclareAnnotation> ret = new LinkedHashSet<>();
			for (CrosscuttingMembers crosscuttingMembers : members.values()) {
				ret.addAll(crosscuttingMembers.getDeclareAnnotationOnTypes());
			}
			declareAnnotationOnTypes = new ArrayList<>();
			declareAnnotationOnTypes.addAll(ret);
		}
		return declareAnnotationOnTypes;
	}

	/**
	 * @return an amalgamation of the declare @field statements.
	 */
	public List<DeclareAnnotation> getDeclareAnnotationOnFields() {
		if (declareAnnotationOnFields == null) {
			Set<DeclareAnnotation> ret = new LinkedHashSet<>();
			for (CrosscuttingMembers crosscuttingMembers : members.values()) {
				ret.addAll(crosscuttingMembers.getDeclareAnnotationOnFields());
			}
			declareAnnotationOnFields = new ArrayList<>();
			declareAnnotationOnFields.addAll(ret);
		}
		return declareAnnotationOnFields;
	}

	/**
	 * @return an amalgamation of the declare @method/@constructor statements.
	 */
	public List<DeclareAnnotation> getDeclareAnnotationOnMethods() {
		if (declareAnnotationOnMethods == null) {
			Set<DeclareAnnotation> ret = new LinkedHashSet<>();
			for (CrosscuttingMembers crosscuttingMembers : members.values()) {
				ret.addAll(crosscuttingMembers.getDeclareAnnotationOnMethods());
			}
			declareAnnotationOnMethods = new ArrayList<>();
			declareAnnotationOnMethods.addAll(ret);
			// world.sortDeclareAnnotations(declareAnnotationOnMethods);
		}
		return declareAnnotationOnMethods;
	}

	/**
	 * Return an amalgamation of the declare type eow statements
	 */
	public List<DeclareTypeErrorOrWarning> getDeclareTypeEows() {
		if (declareTypeEows == null) {
			Set<DeclareTypeErrorOrWarning> ret = new HashSet<>();
			for (CrosscuttingMembers crosscuttingMembers : members.values()) {
				ret.addAll(crosscuttingMembers.getDeclareTypeErrorOrWarning());
			}
			declareTypeEows = new ArrayList<>();
			declareTypeEows.addAll(ret);
		}
		return declareTypeEows;
	}

	public List<Declare> getDeclareDominates() {
		if (declareDominates == null) {
			List<Declare> ret = new ArrayList<>();
			for (CrosscuttingMembers crosscuttingMembers : members.values()) {
				ret.addAll(crosscuttingMembers.getDeclareDominates());
			}
			declareDominates = ret;
		}
		return declareDominates;
	}

	public ResolvedType findAspectDeclaringParents(DeclareParents p) {
		Set<ResolvedType> keys = this.members.keySet();
		for (ResolvedType element : keys) {
			for (DeclareParents dp : members.get(element).getDeclareParents()) {
				if (dp.equals(p)) {
					return element;
				}
			}
		}
		return null;
	}

	public void reset() {
		verificationList = null;
		changedSinceLastReset = false;
	}

	public boolean hasChangedSinceLastReset() {
		return changedSinceLastReset;
	}

	/**
	 * Record something that needs verifying when we believe the type system is complete. Used for things that can't be verified as
	 * we go along - for example some recursive type variable references (pr133307)
	 */
	public void recordNecessaryCheck(IVerificationRequired verification) {
		if (verificationList == null) {
			verificationList = new ArrayList<>();
		}
		verificationList.add(verification);
	}

	/**
	 * Called when type bindings are complete - calls all registered verification objects in turn.
	 */
	public void verify() {
		if (verificationList == null) {
			return;
		}
		for (IVerificationRequired element : verificationList) {
			element.verify();
		}
		verificationList = null;
	}

	public int serializationVersion = 1;

	public void write(CompressingDataOutputStream stream) throws IOException {
		// stream.writeInt(serializationVersion);
		stream.writeInt(shadowMungers.size());
		for (ShadowMunger shadowMunger : shadowMungers) {
			shadowMunger.write(stream);
		}
		// // private List /* ShadowMunger */shadowMungers = null;
		// // private List typeMungers = null;
		// // private List lateTypeMungers = null;
		// // private List declareSofts = null;
		// // private List declareParents = null;
		// // private List declareAnnotationOnTypes = null;
		// // private List declareAnnotationOnFields = null;
		// // private List declareAnnotationOnMethods = null; // includes constructors
		// // private List declareDominates = null;
		// // private boolean changedSinceLastReset = false;
		//
	}
}
