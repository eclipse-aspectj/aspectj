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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.weaver.bcel.BcelAdvice;
import org.aspectj.weaver.bcel.BcelMethod;
import org.aspectj.weaver.bcel.BcelTypeMunger;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;


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
	private ResolvedType inAspect;
	private World world;
	
	private PerClause perClause;
	
	private List shadowMungers = new ArrayList(4);
	private List typeMungers = new ArrayList(4);
    private List lateTypeMungers = new ArrayList(0);

	private List declareParents = new ArrayList(4);
	private List declareSofts = new ArrayList(0);
	private List declareDominates = new ArrayList(4);
	
	// These are like declare parents type mungers
	private List declareAnnotationsOnType    = new ArrayList();
	private List declareAnnotationsOnField   = new ArrayList();
	private List declareAnnotationsOnMethods = new ArrayList(); // includes ctors
	
	private boolean shouldConcretizeIfNeeded = true;
	
	public CrosscuttingMembers(ResolvedType inAspect, boolean shouldConcretizeIfNeeded) {
		this.inAspect = inAspect;
		this.world = inAspect.getWorld();
		this.shouldConcretizeIfNeeded = shouldConcretizeIfNeeded;
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
		if (m == null) throw new Error("FIXME AV - should not happen or what ?");//return; //???
		typeMungers.add(m);
	}

    public void addLateTypeMungers(Collection c) {
        lateTypeMungers.addAll(c);
    }

    public void addLateTypeMunger(ConcreteTypeMunger m) {
        lateTypeMungers.add(m);
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
			m.setDeclaringType(declare.getDeclaringType());
			addShadowMunger(m);
		} else if (declare instanceof DeclarePrecedence) {
			declareDominates.add(declare);
		} else if (declare instanceof DeclareParents) {
			DeclareParents dp = (DeclareParents)declare;
			exposeTypes(dp.getParents().getExactTypes());
			declareParents.add(dp);
		} else if (declare instanceof DeclareSoft) {
			DeclareSoft d = (DeclareSoft)declare;
			// Ordered so that during concretization we can check the related munger
			ShadowMunger m = Advice.makeSoftener(world, d.getPointcut(), d.getException(),inAspect,d);
			m.setDeclaringType(d.getDeclaringType());
			Pointcut concretePointcut = d.getPointcut().concretize(inAspect, d.getDeclaringType(), 0,m);
			m.pointcut = concretePointcut;
			declareSofts.add(new DeclareSoft(d.getException(), concretePointcut));
			addConcreteShadowMunger(m);
		} else if (declare instanceof DeclareAnnotation) {
		    // FIXME asc perf Possible Improvement. Investigate why this is called twice in a weave ?
			DeclareAnnotation da = (DeclareAnnotation)declare;
			if (da.getAspect() == null) da.setAspect(this.inAspect);
			if (da.isDeclareAtType()) {
				declareAnnotationsOnType.add(da);	
			} else if (da.isDeclareAtField()) {
				declareAnnotationsOnField.add(da);
			} else if (da.isDeclareAtMethod() || da.isDeclareAtConstuctor()) {
				declareAnnotationsOnMethods.add(da);
			}
		} else {
			throw new RuntimeException("unimplemented");
		}
	}
	
	public void exposeTypes(Collection typesToExpose) {
		for (Iterator i = typesToExpose.iterator(); i.hasNext(); ) {
			exposeType((UnresolvedType)i.next());
		}
	}
	
	public void exposeType(UnresolvedType typeToExpose) {
		if (ResolvedType.isMissing(typeToExpose)) return;
		if (typeToExpose.isParameterizedType() || typeToExpose.isRawType()) {
			if (typeToExpose instanceof ResolvedType) {
				typeToExpose = ((ResolvedType)typeToExpose).getGenericType();
			} else {
				typeToExpose = UnresolvedType.forSignature(typeToExpose.getErasureSignature());
			}
		}
		// Check we haven't already got a munger for this:
		String signatureToLookFor = typeToExpose.getSignature();
		for (Iterator iterator = typeMungers.iterator(); iterator.hasNext();) {
			ConcreteTypeMunger cTM = (ConcreteTypeMunger) iterator.next();
			ResolvedTypeMunger rTM = cTM.getMunger();
			if (rTM!=null && rTM instanceof ExposeTypeMunger) {
				String exposedType = ((ExposeTypeMunger)rTM).getExposedTypeSignature();
				if (exposedType.equals(signatureToLookFor)) return; // dont need to bother
			}
		}
		addTypeMunger(world.concreteTypeMunger(new ExposeTypeMunger(typeToExpose), inAspect));
//		ResolvedMember member = new ResolvedMemberImpl(
//			Member.STATIC_INITIALIZATION, typeToExpose, 0, ResolvedType.VOID, "<clinit>", UnresolvedType.NONE);
//		addTypeMunger(world.concreteTypeMunger(
//			new PrivilegedAccessMunger(member), inAspect));
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

	/**
	 * Updates the records if something has changed. This is called at most twice, firstly
	 * whilst collecting ITDs and declares. At this point the CrosscuttingMembers we're 
	 * comparing ourselves with doesn't know about shadowmungers. Therefore a straight comparison
	 * with the existing list of shadowmungers would return that something has changed
	 * even though it might not have, so in this first round we ignore the shadowMungers. 
	 * The second time this is called is whilst we're preparing to weave. At this point 
	 * we know everything in the system and so we're able to compare the shadowMunger list.
	 * (see bug 129163)
	 * 
	 * @param other
	 * @param careAboutShadowMungers
	 * @return true if something has changed since the last time this method was
	 *         called, false otherwise
	 */
	public boolean replaceWith(CrosscuttingMembers other,boolean careAboutShadowMungers) {
		boolean changed = false;
		
		if (careAboutShadowMungers) {
			if (perClause == null || !perClause.equals(other.perClause)) {
				changed = true;
				perClause = other.perClause;
			}
		}
		
		//XXX all of the below should be set equality rather than list equality
		//System.err.println("old: " + shadowMungers + " new: " + other.shadowMungers);
		
  	    if (careAboutShadowMungers) {
		    // bug 129163: use set equality rather than list equality 
			Set theseShadowMungers = new HashSet();
			Set theseInlinedAroundMungers = new HashSet();
			for (Iterator iter = shadowMungers.iterator(); iter
					.hasNext();) {
				ShadowMunger munger = (ShadowMunger) iter.next();
				if (munger instanceof Advice) {
					Advice adviceMunger = (Advice)munger;
					// bug 154054: if we're around advice that has been inlined
					// then we need to do more checking than existing equals
					// methods allow
					if (!world.isXnoInline() && adviceMunger.getKind().equals(AdviceKind.Around)) {
						theseInlinedAroundMungers.add(adviceMunger);
					} else {
						theseShadowMungers.add(adviceMunger);
					}
				} else {
					theseShadowMungers.add(munger);
				}
			}
			Set tempSet = new HashSet();
			tempSet.addAll(other.shadowMungers);
			Set otherShadowMungers = new HashSet();
			Set otherInlinedAroundMungers = new HashSet();
			for (Iterator iter = tempSet.iterator(); iter.hasNext();) {
				ShadowMunger munger = (ShadowMunger) iter.next();
				if (munger instanceof Advice) {
					Advice adviceMunger = (Advice)munger;
					// bug 154054: if we're around advice that has been inlined
					// then we need to do more checking than existing equals
					// methods allow
					if (!world.isXnoInline() && adviceMunger.getKind().equals(AdviceKind.Around)) {
						otherInlinedAroundMungers.add(rewritePointcutInMunger(adviceMunger));
					} else {
						otherShadowMungers.add(rewritePointcutInMunger(adviceMunger));
					}
				} else {
					otherShadowMungers.add(rewritePointcutInMunger(munger));
				}
			}
			if (!theseShadowMungers.equals(otherShadowMungers)) {
				changed = true;
			}
			if (!equivalent(theseInlinedAroundMungers,otherInlinedAroundMungers)) {
				changed = true;
			}
			
			// bug 158573 - if there are no changes then preserve whether
			// or not a particular shadowMunger has matched something.
			if (!changed) {
				for (Iterator iter = shadowMungers.iterator(); iter
						.hasNext();) {
					ShadowMunger munger = (ShadowMunger) iter.next();
					int i = other.shadowMungers.indexOf(munger);
					ShadowMunger otherMunger = (ShadowMunger) other.shadowMungers.get(i);
					if (munger instanceof BcelAdvice) {
						((BcelAdvice)otherMunger).setHasMatchedSomething(((BcelAdvice)munger).hasMatchedSomething());
					}
				}
			} 
			// replace the existing list of shadowmungers with the 
			// new ones in case anything like the sourcelocation has
			// changed, however, don't want this flagged as a change
			// which will force a full build - bug 134541
			shadowMungers = other.shadowMungers;				
  	    }
  	    
		// bug 129163: use set equality rather than list equality and
  	    // if we dont care about shadow mungers then ignore those 
  	    // typeMungers which are created to help with the implementation 
  	    // of shadowMungers
		Set theseTypeMungers = new HashSet();
		Set otherTypeMungers = new HashSet();
		if (!careAboutShadowMungers) {
			for (Iterator iter = typeMungers.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof BcelTypeMunger) {
					BcelTypeMunger typeMunger = (BcelTypeMunger) o;
					if (!typeMunger.existsToSupportShadowMunging()) {
						theseTypeMungers.add(typeMunger);
					}				
				} else {
					theseTypeMungers.add(o);
				}
			}
			
			for (Iterator iter = other.typeMungers.iterator(); iter.hasNext();) {
				Object o = iter.next();
				if (o instanceof BcelTypeMunger) {
					BcelTypeMunger typeMunger = (BcelTypeMunger) o;
					if (!typeMunger.existsToSupportShadowMunging()) {
						otherTypeMungers.add(typeMunger);
					}
				} else {
					otherTypeMungers.add(o);
				}
			}
		} else {
			theseTypeMungers.addAll(typeMungers);
			otherTypeMungers.addAll(other.typeMungers);
		}
  	    
  	    // initial go at equivalence logic rather than set compare (see pr133532)
//		if (theseTypeMungers.size()!=otherTypeMungers.size()) {
//			changed = true;
//			typeMungers = other.typeMungers;
//		} else {
//			boolean foundInequality=false;
//			for (Iterator iter = theseTypeMungers.iterator(); iter.hasNext() && !foundInequality;) {
//				Object thisOne = (Object) iter.next();
//				boolean foundInOtherSet = false;
//				for (Iterator iterator = otherTypeMungers.iterator(); iterator.hasNext();) {
//					Object otherOne = (Object) iterator.next();
//					if (thisOne instanceof ConcreteTypeMunger && otherOne instanceof ConcreteTypeMunger) {
//						if (((ConcreteTypeMunger)thisOne).equivalentTo(otherOne)) {
//							foundInOtherSet=true;
//						} else if (thisOne.equals(otherOne)) {
//							foundInOtherSet=true;
//						}
//					} else {
//						if (thisOne.equals(otherOne)) {
//							foundInOtherSet=true;
//						} 
//					}
//				}
//				if (!foundInOtherSet) foundInequality=true;
//			}
//			if (foundInequality) {
//				changed = true;
//				typeMungers = other.typeMungers;
////			} else {
////				typeMungers = other.typeMungers;
//			}
//		}
  	    if (!theseTypeMungers.equals(otherTypeMungers)) {
			changed = true;
			typeMungers = other.typeMungers;
		}

        if (!lateTypeMungers.equals(other.lateTypeMungers)) {
            changed = true;
            lateTypeMungers = other.lateTypeMungers;
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
		
		// DECAT for when attempting to replace an aspect
		if (!declareAnnotationsOnType.equals(other.declareAnnotationsOnType)) {
			changed = true;
			declareAnnotationsOnType = other.declareAnnotationsOnType;
		}
		
		if (!declareAnnotationsOnField.equals(other.declareAnnotationsOnField)) {
			changed = true;
			declareAnnotationsOnField = other.declareAnnotationsOnField;
		}
		
		if (!declareAnnotationsOnMethods.equals(other.declareAnnotationsOnMethods)) {
			changed = true;
			declareAnnotationsOnMethods = other.declareAnnotationsOnMethods;
		}
		
		return changed;
	}

	private boolean equivalent(Set theseInlinedAroundMungers, Set otherInlinedAroundMungers) {
		if (theseInlinedAroundMungers.size() != otherInlinedAroundMungers.size()) {
			return false;
		}
		for (Iterator iter = theseInlinedAroundMungers.iterator(); iter.hasNext();) {
			Advice thisAdvice = (Advice) iter.next();
			boolean foundIt = false;
			for (Iterator iterator = otherInlinedAroundMungers.iterator(); iterator.hasNext();) {
				Advice otherAdvice = (Advice) iterator.next();				
				if (thisAdvice.equals(otherAdvice)) {
					if(thisAdvice.getSignature() instanceof BcelMethod) {
						if (((BcelMethod)thisAdvice.getSignature())
								.isEquivalentTo(otherAdvice.getSignature()) ) {
							foundIt = true;
							continue;
						}						
					}
					return false;
				}
			}
			if (!foundIt) {
				return false;
			}
		}
		return true;
	}

	private ShadowMunger rewritePointcutInMunger(ShadowMunger munger) {
		PointcutRewriter pr = new PointcutRewriter();
		Pointcut p          = munger.getPointcut();
		Pointcut newP       = pr.rewrite(p);
		if (p.m_ignoreUnboundBindingForNames.length!=0) {// *sigh* dirty fix for dirty hacky implementation pr149305
			newP.m_ignoreUnboundBindingForNames = p.m_ignoreUnboundBindingForNames;
		}
		munger.setPointcut(newP);
		return munger;
	}
	
	public void setPerClause(PerClause perClause) {
		if (this.shouldConcretizeIfNeeded) {
			this.perClause = perClause.concretize(inAspect);
		} else {
			this.perClause = perClause;
		}
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

    public List getLateTypeMungers() {
        return lateTypeMungers;
    }

	public List getDeclareAnnotationOnTypes() {
		return declareAnnotationsOnType;
	}
	
	public List getDeclareAnnotationOnFields() {
		return declareAnnotationsOnField;
	}
	
    /**
     * includes declare @method and @constructor
     */
	public List getDeclareAnnotationOnMethods() {
		return declareAnnotationsOnMethods;
	}

}
