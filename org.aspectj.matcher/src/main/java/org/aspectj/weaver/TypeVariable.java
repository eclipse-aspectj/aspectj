/* *******************************************************************
 * Copyright (c) 2005-2010 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.IOException;

/**
 * Represents a type variable with possible bounds.
 * 
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class TypeVariable {

	public static final TypeVariable[] NONE = new TypeVariable[0];

	// the name of the type variable as recorded in the generic signature
	private String name;
	// index
	private int rank;
	// computed as required: either ==superclass or ==superInterfaces[0] or is OBJECT
	private UnresolvedType firstbound;
	// the upper bound of the type variable. From the extends clause, eg. T extends Number
	private UnresolvedType superclass;
	// any additional upper (interface) bounds. from the extends clause, e.g. T extends Number & Comparable
	private UnresolvedType[] superInterfaces = UnresolvedType.NONE;
	// It would be nice to push this field onto the TypeVariableDeclaringElement
	// interface (a getKind()) but at the moment we don't always guarantee
	// to set the declaring element (eclipse seems to utilise the knowledge of
	// what declared the type variable, but we dont yet...)
	public static final int UNKNOWN = -1;
	public static final int METHOD = 1;
	public static final int TYPE = 2;
	// What kind of element declared this type variable?
	private int declaringElementKind = UNKNOWN;
	private TypeVariableDeclaringElement declaringElement;
	// whether or not the bounds of this type variable have been resolved
	public boolean isResolved = false;
	// Is this type variable in the process of being resolved (allows for something self-referential like Enum)
	private boolean beingResolved = false;

	/**
	 * Constructor for an unbound type variable, eg. 'T'
	 */
	public TypeVariable(String name) {
		this.name = name;
	}

	public TypeVariable(String name, UnresolvedType anUpperBound) {
		this(name);
		this.superclass = anUpperBound;
	}

	public TypeVariable(String name, UnresolvedType anUpperBound, UnresolvedType[] superInterfaces) {
		this(name, anUpperBound);
		this.superInterfaces = superInterfaces;
	}

	/**
	 * @return the first bound, either the superclass or if non is specified the first interface or if non are specified then OBJECT
	 */
	public UnresolvedType getFirstBound() {
		if (firstbound != null) {
			return firstbound;
		}
		if (superclass == null || superclass.getSignature().equals("Ljava/lang/Object;")) {
			if (superInterfaces.length > 0) {
				firstbound = superInterfaces[0];
			} else {
				firstbound = UnresolvedType.OBJECT;
			}
		} else {
			firstbound = superclass;
		}
		return firstbound;
	}

	public UnresolvedType getUpperBound() {
		return superclass;
	}

	public UnresolvedType[] getSuperInterfaces() {
		return superInterfaces;
	}

	public String getName() {
		return name;
	}

	/**
	 * resolve all the bounds of this type variable
	 */
	public TypeVariable resolve(World world) {
		if (isResolved) {
			return this;
		}
		if (beingResolved) {
			return this;
		}
		beingResolved = true;

		TypeVariable resolvedTVar = null;

		if (declaringElement != null) {
			// resolve by finding the real type var that we refer to...
			if (declaringElementKind == TYPE) {
				UnresolvedType declaring = (UnresolvedType) declaringElement;
				ReferenceType rd = (ReferenceType) declaring.resolve(world);
				TypeVariable[] tVars = rd.getTypeVariables();
				for (TypeVariable tVar : tVars) {
					if (tVar.getName().equals(getName())) {
						resolvedTVar = tVar;
						break;
					}
				}
			} else {
				// look for type variable on method...
				ResolvedMember declaring = (ResolvedMember) declaringElement;
				TypeVariable[] tvrts = declaring.getTypeVariables();
				for (TypeVariable tvrt : tvrts) {
					if (tvrt.getName().equals(getName())) {
						resolvedTVar = tvrt;
						// if (tvrts[i].isTypeVariableReference()) {
						// TypeVariableReferenceType tvrt = (TypeVariableReferenceType) tvrts[i].resolve(inSomeWorld);
						// TypeVariable tv = tvrt.getTypeVariable();
						// if (tv.getName().equals(getName())) resolvedTVar = tv;
						// }
					}
				}
			}

			if (resolvedTVar == null) {
				throw new IllegalStateException();
				// well, this is bad... we didn't find the type variable on the member
				// could be a separate compilation issue...
				// should issue message, this is a workaround to get us going...
				// resolvedTVar = this;
			}
		} else {
			resolvedTVar = this;
		}

		superclass = resolvedTVar.superclass;
		superInterfaces = resolvedTVar.superInterfaces;

		if (superclass != null) {
			ResolvedType rt = superclass.resolve(world);
//			 if (!superclass.isTypeVariableReference() && rt.isInterface()) {
//				 throw new IllegalStateException("Why is the type an interface? " + rt);
//			 }
			superclass = rt;
		}
		firstbound = getFirstBound().resolve(world);

		for (int i = 0; i < superInterfaces.length; i++) {
			superInterfaces[i] = superInterfaces[i].resolve(world);
		}
		isResolved = true;
		beingResolved = false;
		return this;
	}

	/**
	 * answer true if the given type satisfies all of the bound constraints of this type variable. If type variable has not been
	 * resolved then throws IllegalStateException
	 */
	public boolean canBeBoundTo(ResolvedType candidate) {
		if (!isResolved) {
			throw new IllegalStateException("Can't answer binding questions prior to resolving");
		}

		// wildcard can accept any binding
		if (candidate.isGenericWildcard()) {
			return true;
		}

		// otherwise can be bound iff...

		// candidate is a subtype of upperBound
		if (superclass != null && !isASubtypeOf(superclass, candidate)) {
			return false;
		}
		// candidate is a subtype of all superInterfaces
		for (UnresolvedType superInterface : superInterfaces) {
			if (!isASubtypeOf(superInterface, candidate)) {
				return false;
			}
		}
		return true;
	}

	private boolean isASubtypeOf(UnresolvedType candidateSuperType, UnresolvedType candidateSubType) {
		ResolvedType superType = (ResolvedType) candidateSuperType;
		ResolvedType subType = (ResolvedType) candidateSubType;
		return superType.isAssignableFrom(subType);
	}

	// only used when resolving
	public void setUpperBound(UnresolvedType superclass) {
		// if (isResolved) {
		// throw new IllegalStateException("Why set this late?");
		// }
		this.firstbound = null;
		this.superclass = superclass;
	}

	// only used when resolving
	public void setAdditionalInterfaceBounds(UnresolvedType[] superInterfaces) {
		// if (isResolved) {
		// throw new IllegalStateException("Why set this late?");
		// }
		this.firstbound = null;
		this.superInterfaces = superInterfaces;
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
			if (superInterfaces != null) {
				for (UnresolvedType superInterface : superInterfaces) {
					if (!getFirstBound().equals(superInterface)) {
						ret.append(" & ");
						ret.append(superInterface.getName());
					}
				}
			}
		}
		return ret.toString();
	}

	@Override
	public String toString() {
		return "TypeVar " + getDisplayName();
	}

	/**
	 * Return complete signature, e.g. "T extends Number" would return "T:Ljava/lang/Number;" note: MAY INCLUDE P types if bounds
	 * are parameterized types
	 */
	public String getSignature() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		sb.append(":");
		if (superInterfaces.length == 0 || !superclass.getSignature().equals(UnresolvedType.OBJECT.getSignature())) {
			sb.append(superclass.getSignature());
		}
		if (superInterfaces.length != 0) {
			for (UnresolvedType superInterface : superInterfaces) {
				sb.append(":");
				UnresolvedType iBound = superInterface;
				sb.append(iBound.getSignature());
			}
		}
		return sb.toString();
	}

	/**
	 * @return signature for inclusion in an attribute, there must be no 'P' in it signatures
	 */
	public String getSignatureForAttribute() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		sb.append(":");
		if (superInterfaces.length == 0 || !superclass.getSignature().equals(UnresolvedType.OBJECT.getSignature())) {
			sb.append(((ReferenceType)superclass).getSignatureForAttribute());
		}
		if (superInterfaces.length != 0) {
			for (UnresolvedType superInterface : superInterfaces) {
				sb.append(":");
				ResolvedType iBound = (ResolvedType) superInterface;
				sb.append(iBound.getSignatureForAttribute());
			}
		}
		return sb.toString();
	}

	public void setRank(int rank) {
		this.rank = rank;
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
		// if (declaringElementKind==UNKNOWN) throw new RuntimeException("Dont know declarer of this tvar : "+this);
		return declaringElementKind;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		// name, upperbound, additionalInterfaceBounds, lowerbound
		s.writeUTF(name);
		superclass.write(s);
		if (superInterfaces.length == 0) {
			s.writeInt(0);
		} else {
			s.writeInt(superInterfaces.length);
			for (UnresolvedType ibound : superInterfaces) {
				ibound.write(s);
			}
		}
	}

	public static TypeVariable read(VersionedDataInputStream s) throws IOException {

		// if (s.getMajorVersion()>=AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {

		String name = s.readUTF();
		UnresolvedType ubound = UnresolvedType.read(s);
		int iboundcount = s.readInt();
		UnresolvedType[] ibounds = UnresolvedType.NONE;
		if (iboundcount > 0) {
			ibounds = new UnresolvedType[iboundcount];
			for (int i = 0; i < iboundcount; i++) {
				ibounds[i] = UnresolvedType.read(s);
			}
		}

		TypeVariable newVariable = new TypeVariable(name, ubound, ibounds);
		return newVariable;
	}

	public String getGenericSignature() {
		return "T" + name + ";";
	}

	public String getErasureSignature() {
		return getFirstBound().getErasureSignature();
	}

	public UnresolvedType getSuperclass() {
		return superclass;
	}

	public void setSuperclass(UnresolvedType superclass) {
		this.firstbound = null;
		this.superclass = superclass;
	}

}
