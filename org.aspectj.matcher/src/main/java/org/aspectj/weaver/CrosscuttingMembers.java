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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.DeclareTypeErrorOrWarning;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;

/**
 * This holds on to all members that have an invasive effect outside of there own compilation unit. These members need to be all
 * gathered up and in a world before any weaving can take place.
 * 
 * They are also important in the compilation process and need to be gathered up before the inter-type declaration weaving stage
 * (unsurprisingly).
 * 
 * All members are concrete.
 * 
 * @author Jim Hugunin
 */
public class CrosscuttingMembers {
	private final ResolvedType inAspect;
	private final World world;

	private PerClause perClause;

	private List<ShadowMunger> shadowMungers = new ArrayList<>(4);
	private List<ConcreteTypeMunger> typeMungers = new ArrayList<>(4);
	private List<ConcreteTypeMunger> lateTypeMungers = new ArrayList<>(0);

	private Set<DeclareParents> declareParents = new HashSet<>();
	private Set<DeclareSoft> declareSofts = new HashSet<>();
	private List<Declare> declareDominates = new ArrayList<>(4);

	// These are like declare parents type mungers
	private Set<DeclareAnnotation> declareAnnotationsOnType = new LinkedHashSet<>();
	private Set<DeclareAnnotation> declareAnnotationsOnField = new LinkedHashSet<>();
	private Set<DeclareAnnotation> declareAnnotationsOnMethods = new LinkedHashSet<>();
	// declareAnnotationsOnMethods includes constructors too

	private Set<DeclareTypeErrorOrWarning> declareTypeEow = new HashSet<>();

	private boolean shouldConcretizeIfNeeded = true;

	public CrosscuttingMembers(ResolvedType inAspect, boolean shouldConcretizeIfNeeded) {
		this.inAspect = inAspect;
		world = inAspect.getWorld();
		this.shouldConcretizeIfNeeded = shouldConcretizeIfNeeded;
	}

	private final Map<String, Object> cflowFields = new Hashtable<>();
	private final Map<String, Object> cflowBelowFields = new Hashtable<>();

	// public void addConcreteShadowMungers(Collection c) {
	// shadowMungers.addAll(c);
	// }

	public void addConcreteShadowMunger(ShadowMunger m) {
		// assert m is concrete
		shadowMungers.add(m);
	}

	public void addShadowMungers(Collection<ShadowMunger> c) {
		for (ShadowMunger munger : c) {
			addShadowMunger(munger);
		}
	}

	private void addShadowMunger(ShadowMunger m) {
		if (inAspect.isAbstract()) {
			return; // mungers for abstract aspects are not added
		}
		addConcreteShadowMunger(m.concretize(inAspect, world, perClause));
	}

	public void addTypeMungers(Collection<ConcreteTypeMunger> c) {
		typeMungers.addAll(c);
	}

	public void addTypeMunger(ConcreteTypeMunger m) {
		if (m == null) {
			throw new Error("FIXME AV - should not happen or what ?");// return;
		}
		typeMungers.add(m);
	}

	public void addLateTypeMungers(Collection<ConcreteTypeMunger> c) {
		lateTypeMungers.addAll(c);
	}

	public void addLateTypeMunger(ConcreteTypeMunger m) {
		lateTypeMungers.add(m);
	}

	public void addDeclares(Collection<Declare> declares) {
		for (Declare declare : declares) {
			addDeclare(declare);
		}
	}

	public void addDeclare(Declare declare) {
		// this is not extensible, oh well
		if (declare instanceof DeclareErrorOrWarning) {
			ShadowMunger m = new Checker((DeclareErrorOrWarning) declare);
			m.setDeclaringType(declare.getDeclaringType());
			addShadowMunger(m);
		} else if (declare instanceof DeclarePrecedence) {
			declareDominates.add(declare);
		} else if (declare instanceof DeclareParents) {
			DeclareParents dp = (DeclareParents) declare;
			exposeTypes(dp.getParents().getExactTypes());
			declareParents.add(dp);
		} else if (declare instanceof DeclareSoft) {
			DeclareSoft d = (DeclareSoft) declare;
			// Ordered so that during concretization we can check the related
			// munger
			ShadowMunger m = Advice.makeSoftener(world, d.getPointcut(), d.getException(), inAspect, d);
			m.setDeclaringType(d.getDeclaringType());
			Pointcut concretePointcut = d.getPointcut().concretize(inAspect, d.getDeclaringType(), 0, m);
			m.pointcut = concretePointcut;
			declareSofts.add(new DeclareSoft(d.getException(), concretePointcut));
			addConcreteShadowMunger(m);
		} else if (declare instanceof DeclareAnnotation) {
			// FIXME asc perf Possible Improvement. Investigate why this is
			// called twice in a weave ?
			DeclareAnnotation da = (DeclareAnnotation) declare;
			if (da.getAspect() == null) {
				da.setAspect(inAspect);
			}
			if (da.isDeclareAtType()) {
				declareAnnotationsOnType.add(da);
			} else if (da.isDeclareAtField()) {
				declareAnnotationsOnField.add(da);
			} else if (da.isDeclareAtMethod() || da.isDeclareAtConstuctor()) {
				declareAnnotationsOnMethods.add(da);
			}
		} else if (declare instanceof DeclareTypeErrorOrWarning) {
			declareTypeEow.add((DeclareTypeErrorOrWarning) declare);
		} else {
			throw new RuntimeException("unimplemented");
		}
	}

	public void exposeTypes(List<UnresolvedType> typesToExpose) {
		for (UnresolvedType typeToExpose : typesToExpose) {
			exposeType(typeToExpose);
		}
	}

	public void exposeType(UnresolvedType typeToExpose) {
		if (ResolvedType.isMissing(typeToExpose)) {
			return;
		}
		if (typeToExpose.isParameterizedType() || typeToExpose.isRawType()) {
			if (typeToExpose instanceof ResolvedType) {
				typeToExpose = ((ResolvedType) typeToExpose).getGenericType();
			} else {
				typeToExpose = UnresolvedType.forSignature(typeToExpose.getErasureSignature());
			}
		}
		// Check we haven't already got a munger for this:
		String signatureToLookFor = typeToExpose.getSignature();
		for (ConcreteTypeMunger cTM : typeMungers) {
			ResolvedTypeMunger rTM = cTM.getMunger();
			if (rTM != null && rTM instanceof ExposeTypeMunger) {
				String exposedType = ((ExposeTypeMunger) rTM).getExposedTypeSignature();
				if (exposedType.equals(signatureToLookFor)) {
					return; // dont need to bother
				}
			}
		}
		addTypeMunger(world.getWeavingSupport().concreteTypeMunger(new ExposeTypeMunger(typeToExpose), inAspect));
		// ResolvedMember member = new ResolvedMemberImpl(
		// Member.STATIC_INITIALIZATION, typeToExpose, 0, UnresolvedType.VOID,
		// "<clinit>", UnresolvedType.NONE);
		// addTypeMunger(world.concreteTypeMunger(
		// new PrivilegedAccessMunger(member), inAspect));
	}

	public void addPrivilegedAccesses(Collection<ResolvedMember> accessedMembers) {
		int version = inAspect.getCompilerVersion();
		for (ResolvedMember member : accessedMembers) {
			// Looking it up ensures we get the annotations - the accessedMembers are just retrieved from the attribute and
			// don't have that information
			ResolvedMember resolvedMember = world.resolve(member);

			// pr333469
			// If the member is for an ITD (e.g. serialVersionUID) then during resolution we may resolve it on
			// a supertype because it doesn't yet exist on the target.
			// For example: MyList extends ArrayList<String> and the ITD is on MyList - after resolution it may be:
			// ArrayList<String>.serialVersionUID, we need to avoid that happening

			if (resolvedMember == null) {
				// can happen for ITDs - are there many privileged access ITDs??
				resolvedMember = member;
				if (resolvedMember.hasBackingGenericMember()) {
					resolvedMember = resolvedMember.getBackingGenericMember();
				}
			} else {
				UnresolvedType unresolvedDeclaringType = member.getDeclaringType().getRawType();
				UnresolvedType resolvedDeclaringType = resolvedMember.getDeclaringType().getRawType();
				if (!unresolvedDeclaringType.equals(resolvedDeclaringType)) {
					resolvedMember = member;
				}
			}
			PrivilegedAccessMunger privilegedAccessMunger = new PrivilegedAccessMunger(resolvedMember,
					version >= WeaverVersionInfo.WEAVER_VERSION_AJ169);
			ConcreteTypeMunger concreteTypeMunger = world.getWeavingSupport().concreteTypeMunger(privilegedAccessMunger, inAspect);
			addTypeMunger(concreteTypeMunger);
		}
	}

	public Collection<ShadowMunger> getCflowEntries() {
		List<ShadowMunger> ret = new ArrayList<>();
		for (ShadowMunger m : shadowMungers) {
			if (m instanceof Advice) {
				Advice a = (Advice) m;
				if (a.getKind().isCflow()) {
					ret.add(a);
				}
			}
		}
		return ret;
	}

	/**
	 * Updates the records if something has changed. This is called at most twice, firstly whilst collecting ITDs and declares. At
	 * this point the CrosscuttingMembers we're comparing ourselves with doesn't know about shadowmungers. Therefore a straight
	 * comparison with the existing list of shadowmungers would return that something has changed even though it might not have, so
	 * in this first round we ignore the shadowMungers. The second time this is called is whilst we're preparing to weave. At this
	 * point we know everything in the system and so we're able to compare the shadowMunger list. (see bug 129163)
	 * 
	 * @param other
	 * @param careAboutShadowMungers
	 * @return true if something has changed since the last time this method was called, false otherwise
	 */
	public boolean replaceWith(CrosscuttingMembers other, boolean careAboutShadowMungers) {
		boolean changed = false;

		if (careAboutShadowMungers) {
			if (perClause == null || !perClause.equals(other.perClause)) {
				changed = true;
				perClause = other.perClause;
			}
		}

		// XXX all of the below should be set equality rather than list equality
		// System.err.println("old: " + shadowMungers + " new: " +
		// other.shadowMungers);

		if (careAboutShadowMungers) {
			// bug 129163: use set equality rather than list equality
			Set<ShadowMunger> theseShadowMungers = new HashSet<>();
			Set<ShadowMunger> theseInlinedAroundMungers = new HashSet<>();
			for (ShadowMunger munger : shadowMungers) {
				if (munger instanceof Advice) {
					Advice adviceMunger = (Advice) munger;
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
			Set<ShadowMunger> tempSet = new HashSet<>(other.shadowMungers);
			Set<ShadowMunger> otherShadowMungers = new HashSet<>();
			Set<ShadowMunger> otherInlinedAroundMungers = new HashSet<>();
			for (ShadowMunger munger : tempSet) {
				if (munger instanceof Advice) {
					Advice adviceMunger = (Advice) munger;
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
			if (!equivalent(theseInlinedAroundMungers, otherInlinedAroundMungers)) {
				changed = true;
			}

			// bug 158573 - if there are no changes then preserve whether
			// or not a particular shadowMunger has matched something.
			if (!changed) {
				for (ShadowMunger munger : shadowMungers) {
					int i = other.shadowMungers.indexOf(munger);
					ShadowMunger otherMunger = other.shadowMungers.get(i);
					if (munger instanceof Advice) {
						((Advice) otherMunger).setHasMatchedSomething(((Advice) munger).hasMatchedSomething());
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
		Set<Object> theseTypeMungers = new HashSet<>();
		Set<Object> otherTypeMungers = new HashSet<>();
		if (!careAboutShadowMungers) {
			for (Object o : typeMungers) {
				if (o instanceof ConcreteTypeMunger) {
					ConcreteTypeMunger typeMunger = (ConcreteTypeMunger) o;
					if (!typeMunger.existsToSupportShadowMunging()) {
						theseTypeMungers.add(typeMunger);
					}
				} else {
					theseTypeMungers.add(o);
				}
			}

			for (Object o : other.typeMungers) {
				if (o instanceof ConcreteTypeMunger) {
					ConcreteTypeMunger typeMunger = (ConcreteTypeMunger) o;
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

		// initial go at equivalence logic rather than set compare (see
		// pr133532)
		if (theseTypeMungers.size() != otherTypeMungers.size()) {
			changed = true;
			typeMungers = other.typeMungers;
		} else {
			boolean shouldOverwriteThis = false;
			boolean foundInequality = false;
			for (Iterator<Object> iter = theseTypeMungers.iterator(); iter.hasNext() && !foundInequality;) {
				Object thisOne = iter.next();
				boolean foundInOtherSet = false;
				for (Object otherOne : otherTypeMungers) {
					if (thisOne instanceof ConcreteTypeMunger) {
						if (((ConcreteTypeMunger) thisOne).shouldOverwrite()) {
							shouldOverwriteThis = true;
						}
					}
					if (thisOne instanceof ConcreteTypeMunger && otherOne instanceof ConcreteTypeMunger) {
						if (((ConcreteTypeMunger) thisOne).equivalentTo(otherOne)) {
							foundInOtherSet = true;
						} else if (thisOne.equals(otherOne)) {
							foundInOtherSet = true;
						}
					} else {
						if (thisOne.equals(otherOne)) {
							foundInOtherSet = true;
						}
					}
				}
				if (!foundInOtherSet) {
					foundInequality = true;
				}
			}
			if (foundInequality) {
				// System.out.println("type munger change");
				changed = true;
			}
			if (shouldOverwriteThis) {
				typeMungers = other.typeMungers;
			}
		}
		// if (!theseTypeMungers.equals(otherTypeMungers)) {
		// changed = true;
		// typeMungers = other.typeMungers;
		// }

		if (!lateTypeMungers.equals(other.lateTypeMungers)) {
			changed = true;
			lateTypeMungers = other.lateTypeMungers;
		}

		if (!declareDominates.equals(other.declareDominates)) {
			changed = true;
			declareDominates = other.declareDominates;
		}

		if (!declareParents.equals(other.declareParents)) {
			// Are the differences just because of a mixin? These are not created until weave time so should be gotten rid of for
			// the up front comparison
			if (!careAboutShadowMungers) {
				// this means we are in front end compilation and if the differences are purely mixin parents, we can continue OK
				Set<DeclareParents> trimmedThis = new HashSet<>();
				for (DeclareParents decp : declareParents) {
					if (!decp.isMixin()) {
						trimmedThis.add(decp);
					}
				}
				Set<DeclareParents> trimmedOther = new HashSet<>();
				for (DeclareParents decp : other.declareParents) {
					if (!decp.isMixin()) {
						trimmedOther.add(decp);
					}
				}
				if (!trimmedThis.equals(trimmedOther)) {
					changed = true;
					declareParents = other.declareParents;
				}
			} else {
				changed = true;
				declareParents = other.declareParents;
			}
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
		if (!declareTypeEow.equals(other.declareTypeEow)) {
			changed = true;
			declareTypeEow = other.declareTypeEow;
		}

		return changed;
	}

	private boolean equivalent(Set<ShadowMunger> theseInlinedAroundMungers, Set<ShadowMunger> otherInlinedAroundMungers) {
		if (theseInlinedAroundMungers.size() != otherInlinedAroundMungers.size()) {
			return false;
		}
		for (ShadowMunger theseInlinedAroundMunger : theseInlinedAroundMungers) {
			Advice thisAdvice = (Advice) theseInlinedAroundMunger;
			boolean foundIt = false;
			for (ShadowMunger otherInlinedAroundMunger : otherInlinedAroundMungers) {
				Advice otherAdvice = (Advice) otherInlinedAroundMunger;
				if (thisAdvice.equals(otherAdvice)) {
					if (thisAdvice.getSignature() instanceof ResolvedMemberImpl) {
						if (((ResolvedMemberImpl) thisAdvice.getSignature()).isEquivalentTo(otherAdvice.getSignature())) {
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
		Pointcut p = munger.getPointcut();
		Pointcut newP = pr.rewrite(p);
		if (p.m_ignoreUnboundBindingForNames.length != 0) {
			// *sigh* dirty fix for dirty hacky implementation pr149305
			newP.m_ignoreUnboundBindingForNames = p.m_ignoreUnboundBindingForNames;
		}
		munger.setPointcut(newP);
		return munger;
	}

	public void setPerClause(PerClause perClause) {
		if (shouldConcretizeIfNeeded) {
			this.perClause = perClause.concretize(inAspect);
		} else {
			this.perClause = perClause;
		}
	}

	public List<Declare> getDeclareDominates() {
		return declareDominates;
	}

	public Collection<DeclareParents> getDeclareParents() {
		return declareParents;
	}

	public Collection<DeclareSoft> getDeclareSofts() {
		return declareSofts;
	}

	public List<ShadowMunger> getShadowMungers() {
		return shadowMungers;
	}

	public List<ConcreteTypeMunger> getTypeMungers() {
		return typeMungers;
	}

	public List<ConcreteTypeMunger> getLateTypeMungers() {
		return lateTypeMungers;
	}

	public Collection<DeclareAnnotation> getDeclareAnnotationOnTypes() {
		return declareAnnotationsOnType;
	}

	public Collection<DeclareAnnotation> getDeclareAnnotationOnFields() {
		return declareAnnotationsOnField;
	}

	/**
	 * includes declare @method and @constructor
	 */
	public Collection<DeclareAnnotation> getDeclareAnnotationOnMethods() {
		return declareAnnotationsOnMethods;
	}

	public Collection<DeclareTypeErrorOrWarning> getDeclareTypeErrorOrWarning() {
		return declareTypeEow;
	}

	public Map<String, Object> getCflowBelowFields() {
		return cflowBelowFields;
	}

	public Map<String, Object> getCflowFields() {
		return cflowFields;
	}

	public void clearCaches() {
		cflowFields.clear();
		cflowBelowFields.clear();
	}

}
