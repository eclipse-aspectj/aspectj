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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.weaver.patterns.CflowPointcut;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.IVerificationRequired;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

/**
 * This holds on to all CrosscuttingMembers for a world.  It handles 
 * management of change.
 * 
 * @author Jim Hugunin
 */
public class CrosscuttingMembersSet {
	private World world;
	//FIXME AV - ? we may need a sequencedHashMap there to ensure source based precedence for @AJ advice
    private Map /* ResolvedType (the aspect) > CrosscuttingMembers */members = new HashMap();
	
	private List shadowMungers             = null;
	private List typeMungers               = null;
    private List lateTypeMungers           = null;
	private List declareSofts              = null;
	private List declareParents            = null;
	private List declareAnnotationOnTypes  = null;
	private List declareAnnotationOnFields = null; 
	private List declareAnnotationOnMethods= null; // includes ctors
	private List declareDominates          = null;
	private boolean changedSinceLastReset = false;

	private List /*IVerificationRequired*/ verificationList = null; // List of things to be verified once the type system is 'complete'
	
	private static Trace trace = TraceFactory.getTraceFactory().getTrace(CrosscuttingMembersSet.class);
	
	public CrosscuttingMembersSet(World world) {
		trace.enter("<init>",this,world);

		this.world = world;

		trace.exit("<init>");
	}

	public boolean addOrReplaceAspect(ResolvedType aspectType) {
		return addOrReplaceAspect(aspectType,true);
	}

	/**
	 * @return whether or not that was a change to the global signature
	 * 			XXX for efficiency we will need a richer representation than this
	 */
	public boolean addOrReplaceAspect(ResolvedType aspectType, boolean inWeavingPhase) {
		trace.enter("addOrReplaceAspect",this,new Object[] {aspectType,new Boolean(inWeavingPhase)});
		
		boolean change = false;
		CrosscuttingMembers xcut = (CrosscuttingMembers)members.get(aspectType);
		if (xcut == null) {
			members.put(aspectType, aspectType.collectCrosscuttingMembers(inWeavingPhase));
			clearCaches();
			CflowPointcut.clearCaches(aspectType);
			change = true;
		} else {
			if (xcut.replaceWith(aspectType.collectCrosscuttingMembers(inWeavingPhase),inWeavingPhase)) {
				clearCaches();

				CflowPointcut.clearCaches(aspectType);
				change = true;
			} else {
				if (!AsmManager.getDefault().getHandleProvider().dependsOnLocation()
						&& inWeavingPhase) {
					// bug 134541 - even though we haven't changed we may have updated the 
					// sourcelocation for the shadowMunger which we need to pick up
					shadowMungers = null;
				}
				change = false;
			}
		}
		if (aspectType.isAbstract()) {
			// we might have sub-aspects that need to re-collect their crosscutting members from us
			boolean ancestorChange = addOrReplaceDescendantsOf(aspectType,inWeavingPhase); 
			change = change || ancestorChange;
		}
		changedSinceLastReset = changedSinceLastReset || change;

		trace.exit("addOrReplaceAspect",change);
		return change;
	}
    
	private boolean addOrReplaceDescendantsOf(ResolvedType aspectType, boolean inWeavePhase) {
		//System.err.println("Looking at descendants of "+aspectType.getName());
		Set knownAspects = members.keySet();
		Set toBeReplaced = new HashSet();
		for(Iterator it = knownAspects.iterator(); it.hasNext(); ) {
			ResolvedType candidateDescendant = (ResolvedType)it.next();
			if ((candidateDescendant != aspectType) && (aspectType.isAssignableFrom(candidateDescendant))) {
				toBeReplaced.add(candidateDescendant);
			}
		}
		boolean change = false;
		for (Iterator it = toBeReplaced.iterator(); it.hasNext(); ) {
			ResolvedType next = (ResolvedType)it.next();
			boolean thisChange = addOrReplaceAspect(next,inWeavePhase);
			change = change || thisChange;
		}
		return change;
	}
	
    public void addAdviceLikeDeclares(ResolvedType aspectType) {
        CrosscuttingMembers xcut = (CrosscuttingMembers)members.get(aspectType);
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
    
	//XXX only for testing
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
		declareAnnotationOnFields=null;
		declareAnnotationOnMethods=null;
		declareAnnotationOnTypes=null;
		declareDominates = null;
	}
	
	
	public List getShadowMungers() {
		if (shadowMungers == null) {
			ArrayList ret = new ArrayList();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getShadowMungers());
			}
			shadowMungers = ret;
		}
		return shadowMungers;
	}
	
	public List getTypeMungers() {
		if (typeMungers == null) {
			ArrayList ret = new ArrayList();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getTypeMungers());
			}
			typeMungers = ret;
		}
		return typeMungers;
	}

    public List getLateTypeMungers() {
        if (lateTypeMungers == null) {
            ArrayList ret = new ArrayList();
            for (Iterator i = members.values().iterator(); i.hasNext(); ) {
                ret.addAll(((CrosscuttingMembers)i.next()).getLateTypeMungers());
            }
            lateTypeMungers = ret;
        }
        return lateTypeMungers;
    }

	public List getDeclareSofts() {
		if (declareSofts == null) {
			Set ret = new HashSet();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getDeclareSofts());
			}
			declareSofts = new ArrayList();
			declareSofts.addAll(ret);
		}
		return declareSofts;
	}
	
	public List getDeclareParents() {
		if (declareParents == null) {
			Set ret = new HashSet();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getDeclareParents());
			}
			declareParents = new ArrayList();
			declareParents.addAll(ret);
		}
		return declareParents;
	}
	
	// DECAT Merge multiple together
	public List getDeclareAnnotationOnTypes() {
		if (declareAnnotationOnTypes == null) {
			Set ret = new HashSet();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getDeclareAnnotationOnTypes());
			}
			declareAnnotationOnTypes = new ArrayList();
			declareAnnotationOnTypes.addAll(ret);
		}
		return declareAnnotationOnTypes;
	}
	
	public List getDeclareAnnotationOnFields() {
		if (declareAnnotationOnFields == null) {
			Set ret = new HashSet();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getDeclareAnnotationOnFields());
			}
			declareAnnotationOnFields = new ArrayList();
			declareAnnotationOnFields.addAll(ret);
		}
		return declareAnnotationOnFields;
	}
	
	/**
	 * Return an amalgamation of the declare @method/@constructor statements.
	 */
	public List getDeclareAnnotationOnMethods() {
		if (declareAnnotationOnMethods == null) {
			Set ret = new HashSet();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getDeclareAnnotationOnMethods());
			}
			declareAnnotationOnMethods = new ArrayList();
			declareAnnotationOnMethods.addAll(ret);
		}
		return declareAnnotationOnMethods;
	}
	
	public List getDeclareDominates() {
		if (declareDominates == null) {
			ArrayList ret = new ArrayList();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getDeclareDominates());
			}
			declareDominates = ret;
		}
		return declareDominates;
	}


	public ResolvedType findAspectDeclaringParents(DeclareParents p) {
		Set keys = this.members.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			ResolvedType element = (ResolvedType) iter.next();
			for (Iterator i = ((CrosscuttingMembers)members.get(element)).getDeclareParents().iterator(); i.hasNext(); ) {
				DeclareParents dp = (DeclareParents)i.next();
				if (dp.equals(p)) return element;
			}
		}
		return null;
	}

	public void reset() {
		verificationList=null;
		changedSinceLastReset = false;
	}
	
	public boolean hasChangedSinceLastReset() {
		return changedSinceLastReset;
	}

	/**
	 * Record something that needs verifying when we believe the type system is complete.
	 * Used for things that can't be verified as we go along - for example some
	 * recursive type variable references (pr133307)
	 */
	public void recordNecessaryCheck(IVerificationRequired verification) {
		if (verificationList==null) verificationList = new ArrayList();
		verificationList.add(verification);
	}
	
	
	/**
	 * Called when type bindings are complete - calls all registered verification
	 * objects in turn.
	 */
	public void verify() {
		if (verificationList==null) return;
		for (Iterator iter = verificationList.iterator(); iter.hasNext();) {
			IVerificationRequired element = (IVerificationRequired) iter.next();
			element.verify();
		}
		verificationList = null;
	}
	
}
