/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a type variable with bounds
 */
public class TypeVariable {
	
	public static final TypeVariable[] NONE = new TypeVariable[0];
	/**
	 * whether or not the bounds of this type variable have been 
	 * resolved
	 */
	private boolean isResolved = false;
	
	
	private boolean beingResolved = false;
	
	/**
	 * the name of the type variable as recorded in the generic signature
	 */
	private String name;
	
	private int rank;

    // It would be nice to push this field onto the TypeVariableDeclaringElement
    // interface (a getKind()) but at the moment we don't always guarantee
    // to set the declaring element (eclipse seems to utilise the knowledge of
    // what declared the type variable, but we dont yet...)
	/**
	 * What kind of element declared this type variable?
	 */
	private int declaringElementKind = UNKNOWN;
	public static final int UNKNOWN = -1;
	public static final int METHOD  = 1;
	public static final int TYPE    = 2;
	private TypeVariableDeclaringElement declaringElement;
	
	/**
	 * the upper bound of the type variable (default to Object).
	 * From the extends clause, eg. T extends Number.
	 */
	private UnresolvedType upperBound = UnresolvedType.OBJECT;
	
	/**
	 * any additional upper (interface) bounds.
	 * from the extends clause, e.g. T extends Number & Comparable
	 */
	private UnresolvedType[] additionalInterfaceBounds = new UnresolvedType[0];
	
	/**
	 * any lower bound.
	 * from the super clause, eg T super Foo
	 */
	private UnresolvedType lowerBound = null;
	
	public TypeVariable(String aName) {
		this.name = aName;
	}
	
	public TypeVariable(String aName, UnresolvedType anUpperBound) {
		this(aName);
		this.upperBound = anUpperBound;
	}
	
	public TypeVariable(String aName, UnresolvedType anUpperBound, 
			                        UnresolvedType[] someAdditionalInterfaceBounds) {
		this(aName,anUpperBound);
		this.additionalInterfaceBounds = someAdditionalInterfaceBounds;
	}
	
	public TypeVariable(String aName, UnresolvedType anUpperBound, 
            UnresolvedType[] someAdditionalInterfaceBounds, UnresolvedType aLowerBound) {
		this(aName,anUpperBound,someAdditionalInterfaceBounds);
		this.lowerBound = aLowerBound;
	}
	
	// First bound is the first 'real' bound, this can be an interface if 
	// no class bound was specified (it will default to object)
	public UnresolvedType getFirstBound() {
		if (upperBound.equals(UnresolvedType.OBJECT) && additionalInterfaceBounds!=null && additionalInterfaceBounds.length!=0) {
			return additionalInterfaceBounds[0];
		}
		return upperBound;
	}
	
	public UnresolvedType getUpperBound() {
		return upperBound;
	}
	
	public UnresolvedType[] getAdditionalInterfaceBounds() {
		return additionalInterfaceBounds;
	}
	
	public UnresolvedType getLowerBound() {
		return lowerBound;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * resolve all the bounds of this type variable
	 */
	public TypeVariable resolve(World inSomeWorld) {
		if (beingResolved) { return this; } // avoid spiral of death
		beingResolved = true;
		if (isResolved) return this;

		TypeVariable resolvedTVar = null;

		if (declaringElement != null) {
			// resolve by finding the real type var that we refer to...
			if (declaringElementKind == TYPE) {
				UnresolvedType declaring = (UnresolvedType) declaringElement;
				ReferenceType rd = (ReferenceType) declaring.resolve(inSomeWorld);
				TypeVariable[] tVars = rd.getTypeVariables();
				for (int i = 0; i < tVars.length; i++) {
					if (tVars[i].getName().equals(getName())) {
						resolvedTVar = tVars[i];
						break;
					}
				}
			} else {
				// look for type variable on method...
				ResolvedMember declaring = (ResolvedMember) declaringElement;
				TypeVariable[] tvrts = declaring.getTypeVariables();
				for (int i = 0; i < tvrts.length; i++) {
					if (tvrts[i].getName().equals(getName())) resolvedTVar = tvrts[i];
//					if (tvrts[i].isTypeVariableReference()) {
//						TypeVariableReferenceType tvrt = (TypeVariableReferenceType) tvrts[i].resolve(inSomeWorld);
//						TypeVariable tv = tvrt.getTypeVariable();
//						if (tv.getName().equals(getName())) resolvedTVar = tv;
//					}
				}			
			}
			
			if (resolvedTVar == null) {
				// well, this is bad... we didn't find the type variable on the member
				// could be a separate compilation issue...
				// should issue message, this is a workaround to get us going...
				resolvedTVar = this;				
			}
		} else {
			resolvedTVar = this;
		}
				
		upperBound = resolvedTVar.upperBound;
		lowerBound = resolvedTVar.lowerBound;
		additionalInterfaceBounds = resolvedTVar.additionalInterfaceBounds;
		
		upperBound = upperBound.resolve(inSomeWorld);
		if (lowerBound != null) lowerBound = lowerBound.resolve(inSomeWorld);
		
		if (additionalInterfaceBounds!=null) {
			for (int i = 0; i < additionalInterfaceBounds.length; i++) {
				additionalInterfaceBounds[i] = additionalInterfaceBounds[i].resolve(inSomeWorld);
			}
		}
		isResolved = true;
		beingResolved = false;
		return this;
	}
	
	/**
	 * answer true if the given type satisfies all of the bound constraints of this
	 * type variable.
	 * If type variable has not been resolved then throws IllegalStateException
	 */
	public boolean canBeBoundTo(ResolvedType aCandidateType) {
		if (!isResolved) throw new IllegalStateException("Can't answer binding questions prior to resolving");
		if (aCandidateType.isTypeVariableReference()) {
			return matchingBounds((TypeVariableReferenceType)aCandidateType);
		}
		
		// wildcard can accept any binding
		if (aCandidateType.isGenericWildcard()) {  // AMC - need a more robust test!
			return true;
		}
		
		// otherwise can be bound iff...
		//  aCandidateType is a subtype of upperBound
		if (!isASubtypeOf(upperBound,aCandidateType)) {
			return false;
		}
		//  aCandidateType is a subtype of all additionalInterfaceBounds
		for (int i = 0; i < additionalInterfaceBounds.length; i++) {
			if (!isASubtypeOf(additionalInterfaceBounds[i], aCandidateType)) {
				return false;
			}
		}
		//  lowerBound is a subtype of aCandidateType
		if ((lowerBound != null) && (!isASubtypeOf(aCandidateType,lowerBound))) {
			return false;
		}
		return true;
	}
	
	// can match any type in the range of the type variable...
	// XXX what about interfaces?
	private boolean matchingBounds(TypeVariableReferenceType tvrt) {
		if (tvrt.getUpperBound() != getUpperBound()) return false;
		if (tvrt.hasLowerBound() != (getLowerBound() != null)) return false;
		if (tvrt.hasLowerBound() && tvrt.getLowerBound() != getLowerBound()) return false;
		// either we both have bounds, or neither of us have bounds
		ReferenceType[] tvrtBounds = tvrt.getAdditionalBounds();
		if ((tvrtBounds != null) != (additionalInterfaceBounds != null)) return false;
		if (additionalInterfaceBounds != null) {
			// we both have bounds, compare
			if (tvrtBounds.length != additionalInterfaceBounds.length) return false;
			Set aAndNotB = new HashSet();
			Set bAndNotA = new HashSet();
			for (int i = 0; i < additionalInterfaceBounds.length; i++) {
				aAndNotB.add(additionalInterfaceBounds[i]);
			}
			for (int i = 0; i < tvrtBounds.length; i++) {
				bAndNotA.add(tvrtBounds[i]);
			}
			for (int i = 0; i < additionalInterfaceBounds.length; i++) {
				bAndNotA.remove(additionalInterfaceBounds[i]);
			}
			for (int i = 0; i < tvrtBounds.length; i++) {
				aAndNotB.remove(tvrtBounds[i]);
			}
			if (! (aAndNotB.isEmpty() && bAndNotA.isEmpty()) ) return false;
		}
		return true;
	}
	
	private boolean isASubtypeOf(UnresolvedType candidateSuperType, UnresolvedType candidateSubType) {
		ResolvedType superType = (ResolvedType) candidateSuperType;
		ResolvedType subType = (ResolvedType) candidateSubType;
		return superType.isAssignableFrom(subType);
	}

	// only used when resolving 
	public void setUpperBound(UnresolvedType aTypeX) {
		this.upperBound = aTypeX;
	}
	
	// only used when resolving
	public void setLowerBound(UnresolvedType aTypeX) {
		this.lowerBound = aTypeX;
	}
	
	// only used when resolving
	public void setAdditionalInterfaceBounds(UnresolvedType[] someTypeXs) {
		this.additionalInterfaceBounds = someTypeXs;
	}
	
	public String toDebugString() {
		return getDisplayName();
	}
	
	public String getDisplayName() {
		StringBuffer ret = new StringBuffer();
		ret.append(name);
		if (!getFirstBound().getName().equals("java.lang.Object")) {
			ret.append(" extends ");
			ret.append(getFirstBound().getName());
			if (additionalInterfaceBounds != null) {
				for (int i = 0; i < additionalInterfaceBounds.length; i++) {
					if (!getFirstBound().equals(additionalInterfaceBounds[i])) {
						ret.append(" & ");
						ret.append(additionalInterfaceBounds[i].getName());
					}
				}
			}
		}
		if (lowerBound != null) {
			ret.append(" super ");
			ret.append(lowerBound.getName());
		}
		return ret.toString();
	}
	
	// good enough approximation
	public String toString() {
		return "TypeVar " + getDisplayName();
	}
	
	/**
	 * Return *full* signature for insertion in signature attribute, e.g. "T extends Number" would return "T:Ljava/lang/Number;"
	 */
	public String getSignature() {
	  	StringBuffer sb = new StringBuffer();
	  	sb.append(name);
		sb.append(":");
  		sb.append(upperBound.getSignature());
	  	if (additionalInterfaceBounds!=null && additionalInterfaceBounds.length!=0) {
		  	sb.append(":");
		  	for (int i = 0; i < additionalInterfaceBounds.length; i++) {
				UnresolvedType iBound = additionalInterfaceBounds[i];
				sb.append(iBound.getSignature());
			}
	  	}
		return sb.toString();
	}
	
	public void setRank(int rank) {
		this.rank=rank;
	}
	
	public int getRank() {
		return rank;
	}

	public void setDeclaringElement(TypeVariableDeclaringElement element) {
		this.declaringElement = element;
		if (element instanceof UnresolvedType) {
			this.declaringElementKind = TYPE;
		} else {
			this.declaringElementKind = METHOD;
		}
	}
	
	public TypeVariableDeclaringElement getDeclaringElement() {
		return declaringElement;
	}
	
	public void setDeclaringElementKind(int kind) {
		this.declaringElementKind = kind;
	}
	
	public int getDeclaringElementKind() {
//		if (declaringElementKind==UNKNOWN) throw new RuntimeException("Dont know declarer of this tvar : "+this);
		return declaringElementKind;
	}
	
	public void write(DataOutputStream s) throws IOException {
	// name, upperbound, additionalInterfaceBounds, lowerbound
		s.writeUTF(name);
		upperBound.write(s);
		if (additionalInterfaceBounds==null || additionalInterfaceBounds.length==0) {
			s.writeInt(0);
		} else {
			s.writeInt(additionalInterfaceBounds.length);
			for (int i = 0; i < additionalInterfaceBounds.length; i++) {
				UnresolvedType ibound = additionalInterfaceBounds[i];
				ibound.write(s);
			}
		}
	}
	
	public static TypeVariable read(VersionedDataInputStream s) throws IOException {
    	
		//if (s.getMajorVersion()>=AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {
			
		String name = s.readUTF();
		UnresolvedType ubound = UnresolvedType.read(s);
		int iboundcount = s.readInt();
		UnresolvedType[] ibounds = null;
		if (iboundcount>0) {
			ibounds = new UnresolvedType[iboundcount];
			for (int i=0; i<iboundcount; i++) {
				ibounds[i] = UnresolvedType.read(s);
			}
		}
		
		TypeVariable newVariable = new TypeVariable(name,ubound,ibounds);
		return newVariable;		
    }

	public String getGenericSignature() {
		return "T"+name+";";
	}
	public String getErasureSignature() {
		return getFirstBound().getErasureSignature();
	}
}
