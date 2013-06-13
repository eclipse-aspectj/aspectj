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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.DeclareTypeErrorOrWarning;
import org.aspectj.weaver.patterns.IVerificationRequired;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

/**
 * This holds on to all CrosscuttingMembers for a world. It handles management of change.
 * 
 * @author Jim Hugunin
 * @author Andy Clement
 */
public class CrosscuttingMembersSet {

	private static Trace trace = TraceFactory.getTraceFactory().getTrace(CrosscuttingMembersSet.class);

	private transient World world;

	// FIXME AV - ? we may need a sequencedHashMap there to ensure source based precedence for @AJ advice
	private final Map /* ResolvedType (the aspect) > CrosscuttingMembers */<ResolvedType, CrosscuttingMembers> members = new HashMap<ResolvedType, CrosscuttingMembers>();

	// List of things to be verified once the type system is 'complete'
	private transient List /* IVerificationRequired */<IVerificationRequired> verificationList = null;

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
	 * @return whether or not that was a change to the global signature XXX for efficiency we will need a richer representation than
	 *         this
	 */
	public boolean addOrReplaceAspect(ResolvedType aspectType, boolean inWeavingPhase) {

		if (!world.isAspectIncluded(aspectType) || world.hasUnsatisfiedDependency(aspectType)) {
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
		Set<ResolvedType> toBeReplaced = new HashSet<ResolvedType>();
		for (Iterator<ResolvedType> it = knownAspects.iterator(); it.hasNext();) {
			ResolvedType candidateDescendant = it.next();
			if ((candidateDescendant != aspectType) && (aspectType.isAssignableFrom(candidateDescendant))) {
				toBeReplaced.add(candidateDescendant);
			}
		}
		boolean change = false;
		for (Iterator<ResolvedType> it = toBeReplaced.iterator(); it.hasNext();) {
			ResolvedType next = it.next();
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
			List<ShadowMunger> ret = new ArrayList<ShadowMunger>();
			for (Iterator<CrosscuttingMembers> i = members.values().iterator(); i.hasNext();) {
				ret.addAll(i.next().getShadowMungers());
			}
			shadowMungers = ret;
		}
		return shadowMungers;
	}

	public List<ConcreteTypeMunger> getTypeMungers() {
		if (typeMungers == null) {
			List<ConcreteTypeMunger> ret = new ArrayList<ConcreteTypeMunger>();
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
					collected = new ArrayList<ConcreteTypeMunger>();
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
			List<ConcreteTypeMunger> ret = new ArrayList<ConcreteTypeMunger>();
			for (Iterator<CrosscuttingMembers> i = members.values().iterator(); i.hasNext();) {
				ret.addAll(i.next().getLateTypeMungers());
			}
			lateTypeMungers = ret;
		}
		return lateTypeMungers;
	}

	public List<DeclareSoft> getDeclareSofts() {
		if (declareSofts == null) {
			Set<DeclareSoft> ret = new HashSet<DeclareSoft>();
			for (Iterator<CrosscuttingMembers> i = members.values().iterator(); i.hasNext();) {
				ret.addAll(i.next().getDeclareSofts());
			}
			declareSofts = new ArrayList<DeclareSoft>();
			declareSofts.addAll(ret);
		}
		return declareSofts;
	}

	public List<DeclareParents> getDeclareParents() {
		if (declareParents == null) {
			Set<DeclareParents> ret = new HashSet<DeclareParents>();
			for (Iterator<CrosscuttingMembers> i = members.values().iterator(); i.hasNext();) {
				ret.addAll(i.next().getDeclareParents());
			}
			declareParents = new ArrayList<DeclareParents>();
			declareParents.addAll(ret);
		}
		return declareParents;
	}

	/**
	 * @return an amalgamation of the declare @type statements.
	 */
	public List<DeclareAnnotation> getDeclareAnnotationOnTypes() {
		if (declareAnnotationOnTypes == null) {
			Set<DeclareAnnotation> ret = new LinkedHashSet<DeclareAnnotation>();
			for (Iterator<CrosscuttingMembers> i = members.values().iterator(); i.hasNext();) {
				ret.addAll(i.next().getDeclareAnnotationOnTypes());
			}
			declareAnnotationOnTypes = new ArrayList<DeclareAnnotation>();
			declareAnnotationOnTypes.addAll(ret);
		}
		return declareAnnotationOnTypes;
	}

	/**
	 * @return an amalgamation of the declare @field statements.
	 */
	public List<DeclareAnnotation> getDeclareAnnotationOnFields() {
		if (declareAnnotationOnFields == null) {
			Set<DeclareAnnotation> ret = new LinkedHashSet<DeclareAnnotation>();
			for (Iterator<CrosscuttingMembers> i = members.values().iterator(); i.hasNext();) {
				ret.addAll(i.next().getDeclareAnnotationOnFields());
			}
			declareAnnotationOnFields = new ArrayList<DeclareAnnotation>();
			declareAnnotationOnFields.addAll(ret);
		}
		return declareAnnotationOnFields;
	}

	/**
	 * @return an amalgamation of the declare @method/@constructor statements.
	 */
	public List<DeclareAnnotation> getDeclareAnnotationOnMethods() {
		if (declareAnnotationOnMethods == null) {
			Set<DeclareAnnotation> ret = new LinkedHashSet<DeclareAnnotation>();
			for (Iterator<CrosscuttingMembers> i = members.values().iterator(); i.hasNext();) {
				ret.addAll(i.next().getDeclareAnnotationOnMethods());
			}
			declareAnnotationOnMethods = new ArrayList<DeclareAnnotation>();
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
			Set<DeclareTypeErrorOrWarning> ret = new HashSet<DeclareTypeErrorOrWarning>();
			for (Iterator<CrosscuttingMembers> i = members.values().iterator(); i.hasNext();) {
				ret.addAll(i.next().getDeclareTypeErrorOrWarning());
			}
			declareTypeEows = new ArrayList<DeclareTypeErrorOrWarning>();
			declareTypeEows.addAll(ret);
		}
		return declareTypeEows;
	}

	public List<Declare> getDeclareDominates() {
		if (declareDominates == null) {
			List<Declare> ret = new ArrayList<Declare>();
			for (Iterator<CrosscuttingMembers> i = members.values().iterator(); i.hasNext();) {
				ret.addAll(i.next().getDeclareDominates());
			}
			declareDominates = ret;
		}
		return declareDominates;
	}

	public ResolvedType findAspectDeclaringParents(DeclareParents p) {
		Set<ResolvedType> keys = this.members.keySet();
		for (Iterator<ResolvedType> iter = keys.iterator(); iter.hasNext();) {
			ResolvedType element = iter.next();
			for (Iterator i = members.get(element).getDeclareParents().iterator(); i.hasNext();) {
				DeclareParents dp = (DeclareParents) i.next();
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
			verificationList = new ArrayList<IVerificationRequired>();
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
		for (Iterator<IVerificationRequired> iter = verificationList.iterator(); iter.hasNext();) {
			IVerificationRequired element = iter.next();
			element.verify();
		}
		verificationList = null;
	}

	public int serializationVersion = 1;

	public void write(CompressingDataOutputStream stream) throws IOException {
		// stream.writeInt(serializationVersion);
		stream.writeInt(shadowMungers.size());
		for (Iterator iterator = shadowMungers.iterator(); iterator.hasNext();) {
			ShadowMunger shadowMunger = (ShadowMunger) iterator.next();
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
