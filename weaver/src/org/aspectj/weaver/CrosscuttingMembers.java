/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;


/**
 * This holds on to all members that have an invasive effect outside of
 * there own compilation unit.  These members need to be all gathered up and in
 * a world before any weaving can take place.
 * 
 * They are also important in the compilation process and need to be gathered
 * up before the inter-type declaration weaving stage (unsurprisingly).
 * 
 * All members are concrete.
 * 
 * @author Jim Hugunin
 */
public class CrosscuttingMembers {
	private ResolvedTypeX inAspect;
	private World world;
	
	private PerClause perClause;
	
	private List shadowMungers = new ArrayList(4);
	private List typeMungers = new ArrayList(4);
	
	private List declareParents = new ArrayList(4);
	private List declareSofts = new ArrayList(0);
	private List declareDominates = new ArrayList(4);
	
	
	public CrosscuttingMembers(ResolvedTypeX inAspect) {
		this.inAspect = inAspect;
		this.world = inAspect.getWorld();
	}
	
//	public void addConcreteShadowMungers(Collection c) {
//		shadowMungers.addAll(c);
//	}
	
	public void addConcreteShadowMunger(ShadowMunger m) {
		// assert m is concrete
		shadowMungers.add(m);
	}

	public void addShadowMungers(Collection c) {
		for (Iterator i = c.iterator(); i.hasNext(); ) {
			addShadowMunger( (ShadowMunger)i.next() );
		}
	}
	
	private void addShadowMunger(ShadowMunger m) {
		if (inAspect.isAbstract()) return; // we don't do mungers for abstract aspects
		addConcreteShadowMunger(m.concretize(inAspect, world, perClause));
	}
	
	public void addTypeMungers(Collection c) {
		typeMungers.addAll(c);
	}
	
	public void addTypeMunger(ConcreteTypeMunger m) {
		if (m == null) return; //???
		typeMungers.add(m);
	}
	
	public void addDeclares(Collection c) {
		for (Iterator i = c.iterator(); i.hasNext(); ) {
			addDeclare( (Declare)i.next() );
		}
	}
		
		
		
	public void addDeclare(Declare declare) {
		// this is not extensible, oh well
		if (declare instanceof DeclareErrorOrWarning) {
			ShadowMunger m = new Checker((DeclareErrorOrWarning)declare);
			addShadowMunger(m);
		} else if (declare instanceof DeclarePrecedence) {
			declareDominates.add(declare);
		} else if (declare instanceof DeclareParents) {
			DeclareParents dp = (DeclareParents)declare;
			exposeTypes(dp.getParents().getExactTypes());
			declareParents.add(dp);
		} else if (declare instanceof DeclareSoft) {
			DeclareSoft d = (DeclareSoft)declare;
			Pointcut concretePointcut = d.getPointcut().concretize(inAspect, 0);
			declareSofts.add(new DeclareSoft(d.getException(), concretePointcut));
			ShadowMunger m = Advice.makeSoftener(world, concretePointcut, d.getException());
			addConcreteShadowMunger(m);
		} else {
			throw new RuntimeException("unimplemented");
		}
	}
	
	public void exposeTypes(Collection typesToExpose) {
		for (Iterator i = typesToExpose.iterator(); i.hasNext(); ) {
			exposeType((TypeX)i.next());
		}
	}
	
	public void exposeType(TypeX typeToExpose) {
		if (typeToExpose == ResolvedTypeX.MISSING) return;
		
		ResolvedMember member = new ResolvedMember(
			Member.STATIC_INITIALIZATION, typeToExpose, 0, ResolvedTypeX.VOID, "", TypeX.NONE);
		addTypeMunger(world.concreteTypeMunger(
			new PrivilegedAccessMunger(member), inAspect));
	}
	
	public void addPrivilegedAccesses(Collection accessedMembers) {
		for (Iterator i = accessedMembers.iterator(); i.hasNext(); ) {
			addPrivilegedAccess( (ResolvedMember)i.next() );
		}
	}

	private void addPrivilegedAccess(ResolvedMember member) {
		//System.err.println("add priv access: " + member);
		addTypeMunger(world.concreteTypeMunger(new PrivilegedAccessMunger(member), inAspect));
	}


	
	public Collection getCflowEntries() {
		ArrayList ret = new ArrayList();
		for (Iterator i = shadowMungers.iterator(); i.hasNext(); ) {
			ShadowMunger m = (ShadowMunger)i.next();
			if (m instanceof Advice) {
				Advice a = (Advice)m;
				if (a.getKind().isCflow()) {
					ret.add(a);
				}
			}
		}
		return ret;
	}

	public boolean replaceWith(CrosscuttingMembers other) {
		boolean changed = false;
		
		if (perClause == null || !perClause.equals(other.perClause)) {
			changed = true;
			perClause = other.perClause;
		}
		
		//XXX all of the below should be set equality rather than list equality
		//System.err.println("old: " + shadowMungers + " new: " + other.shadowMungers);
		if (!shadowMungers.equals(other.shadowMungers)) {
			changed = true;
			shadowMungers = other.shadowMungers;
		}
		
		if (!typeMungers.equals(other.typeMungers)) {
			changed = true;
			typeMungers = other.typeMungers;
		}
		
		if (!declareDominates.equals(other.declareDominates)) {
			changed = true;
			declareDominates = other.declareDominates;
		}
		
		if (!declareParents.equals(other.declareParents)) {
			changed = true;
			declareParents = other.declareParents;
		}
		
		if (!declareSofts.equals(other.declareSofts)) {
			changed = true;
			declareSofts = other.declareSofts;
		}
		
		return changed;
	}

	public PerClause getPerClause() {
		return perClause;
	}

	public void setPerClause(PerClause perClause) {
		this.perClause = perClause.concretize(inAspect);
	}

	public List getDeclareDominates() {
		return declareDominates;
	}

	public List getDeclareParents() {
		return declareParents;
	}

	public List getDeclareSofts() {
		return declareSofts;
	}

	public List getShadowMungers() {
		return shadowMungers;
	}

	public List getTypeMungers() {
		return typeMungers;
	}

}
