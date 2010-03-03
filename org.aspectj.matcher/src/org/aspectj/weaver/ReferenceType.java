/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement - June 2005 - separated out from ResolvedType
 * ******************************************************************/
package org.aspectj.weaver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;

/**
 * A reference type represents some 'real' type, not a primitive, not an array - but a real type, for example java.util.List. Each
 * ReferenceType has a delegate that is the underlying artifact - either an eclipse artifact or a bcel artifact. If the type
 * represents a raw type (i.e. there is a generic form) then the genericType field is set to point to the generic type. If it is for
 * a parameterized type then the generic type is also set to point to the generic form.
 */
public class ReferenceType extends ResolvedType {

	public static final ReferenceType[] EMPTY_ARRAY = new ReferenceType[0];

	/**
	 * For generic types, this list holds references to all the derived raw and parameterized versions. We need this so that if the
	 * generic delegate is swapped during incremental compilation, the delegate of the derivatives is swapped also.
	 */
	private final List/* ReferenceType */derivativeTypes = new ArrayList();

	/**
	 * For parameterized types (or the raw type) - this field points to the actual reference type from which they are derived.
	 */
	ReferenceType genericType = null;

	ReferenceTypeDelegate delegate = null;
	int startPos = 0;
	int endPos = 0;

	// cached values for members
	ResolvedMember[] parameterizedMethods = null;
	ResolvedMember[] parameterizedFields = null;
	ResolvedMember[] parameterizedPointcuts = null;
	WeakReference<ResolvedType[]> parameterizedInterfaces = new WeakReference<ResolvedType[]>(null);
	Collection<Declare> parameterizedDeclares = null;
	Collection parameterizedTypeMungers = null;

	// During matching it can be necessary to temporary mark types as annotated. For example
	// a declare @type may trigger a separate declare parents to match, and so the annotation
	// is temporarily held against the referencetype, the annotation will be properly
	// added to the class during weaving.
	private ResolvedType[] annotationTypes = null;
	private AnnotationAJ[] annotations = null;

	// Similarly these are temporary replacements and additions for the superclass and
	// superinterfaces
	private ResolvedType newSuperclass;
	private ResolvedType[] newInterfaces;

	// ??? should set delegate before any use
	public ReferenceType(String signature, World world) {
		super(signature, world);
	}

	public ReferenceType(String signature, String signatureErasure, World world) {
		super(signature, signatureErasure, world);
	}

	public static ReferenceType fromTypeX(UnresolvedType tx, World world) {
		ReferenceType rt = new ReferenceType(tx.getErasureSignature(), world);
		rt.typeKind = tx.typeKind;
		return rt;
	}

	/**
	 * Constructor used when creating a parameterized type.
	 */
	public ReferenceType(ResolvedType theGenericType, ResolvedType[] theParameters, World aWorld) {
		super(makeParameterizedSignature(theGenericType, theParameters), theGenericType.signatureErasure, aWorld);
		ReferenceType genericReferenceType = (ReferenceType) theGenericType;
		this.typeParameters = theParameters;
		this.genericType = genericReferenceType;
		this.typeKind = TypeKind.PARAMETERIZED;
		this.delegate = genericReferenceType.getDelegate();
		genericReferenceType.addDependentType(this);
	}

	/**
	 * Constructor used when creating a raw type.
	 */
	// public ReferenceType(
	// ResolvedType theGenericType,
	// World aWorld) {
	// super(theGenericType.getErasureSignature(),
	// theGenericType.getErasureSignature(),
	// aWorld);
	// ReferenceType genericReferenceType = (ReferenceType) theGenericType;
	// this.typeParameters = null;
	// this.genericType = genericReferenceType;
	// this.typeKind = TypeKind.RAW;
	// this.delegate = genericReferenceType.getDelegate();
	// genericReferenceType.addDependentType(this);
	// }
	private void addDependentType(ReferenceType dependent) {
		this.derivativeTypes.add(dependent);
	}

	@Override
	public String getSignatureForAttribute() {
		if (genericType == null || typeParameters == null) {
			return getSignature();
		}
		return makeDeclaredSignature(genericType, typeParameters);
	}

	/**
	 * Create a reference type for a generic type
	 */
	public ReferenceType(UnresolvedType genericType, World world) {
		super(genericType.getSignature(), world);
		typeKind = TypeKind.GENERIC;
	}

	@Override
	public boolean isClass() {
		return delegate.isClass();
	}

	@Override
	public int getCompilerVersion() {
		return delegate.getCompilerVersion();
	}

	@Override
	public boolean isGenericType() {
		return !isParameterizedType() && !isRawType() && delegate.isGeneric();
	}

	public String getGenericSignature() {
		String sig = delegate.getDeclaredGenericSignature();
		return (sig == null) ? "" : sig;
	}

	@Override
	public AnnotationAJ[] getAnnotations() {
		return delegate.getAnnotations();
	}

	@Override
	public void addAnnotation(AnnotationAJ annotationX) {
		if (annotations == null) {
			annotations = new AnnotationAJ[1];
			annotations[0] = annotationX;
		} else {
			AnnotationAJ[] newAnnotations = new AnnotationAJ[annotations.length + 1];
			System.arraycopy(annotations, 0, newAnnotations, 1, annotations.length);
			newAnnotations[0] = annotationX;
			annotations = newAnnotations;
		}
		addAnnotationType(annotationX.getType());
	}

	public boolean hasAnnotation(UnresolvedType ofType) {
		boolean onDelegate = delegate.hasAnnotation(ofType);
		if (onDelegate) {
			return true;
		}
		if (annotationTypes != null) {
			for (int i = 0; i < annotationTypes.length; i++) {
				if (annotationTypes[i].equals(ofType)) {
					return true;
				}
			}
		}
		return false;
	}

	private void addAnnotationType(ResolvedType ofType) {
		if (annotationTypes == null) {
			annotationTypes = new ResolvedType[1];
			annotationTypes[0] = ofType;
		} else {
			ResolvedType[] newAnnotationTypes = new ResolvedType[annotationTypes.length + 1];
			System.arraycopy(annotationTypes, 0, newAnnotationTypes, 1, annotationTypes.length);
			newAnnotationTypes[0] = ofType;
			annotationTypes = newAnnotationTypes;
		}
	}

	@Override
	public ResolvedType[] getAnnotationTypes() {
		if (delegate == null) {
			throw new BCException("Unexpected null delegate for type " + this.getName());
		}
		if (annotationTypes == null) {
			// there are no extras:
			return delegate.getAnnotationTypes();
		} else {
			ResolvedType[] delegateAnnotationTypes = delegate.getAnnotationTypes();
			ResolvedType[] result = new ResolvedType[annotationTypes.length + delegateAnnotationTypes.length];
			System.arraycopy(delegateAnnotationTypes, 0, result, 0, delegateAnnotationTypes.length);
			System.arraycopy(annotationTypes, 0, result, delegateAnnotationTypes.length, annotationTypes.length);
			return result;
		}
	}

	@Override
	public String getNameAsIdentifier() {
		return getRawName().replace('.', '_');
	}

	@Override
	public AnnotationAJ getAnnotationOfType(UnresolvedType ofType) {
		AnnotationAJ[] axs = delegate.getAnnotations();
		if (axs == null) {
			if (annotations != null) {
				String searchSig = ofType.getSignature();
				for (int i = 0; i < annotations.length; i++) {
					if (annotations[i].getTypeSignature().equals(searchSig)) {
						return annotations[i];
					}
				}
			}
			return null;
		}
		for (int i = 0; i < axs.length; i++) {
			if (axs[i].getTypeSignature().equals(ofType.getSignature())) {
				return axs[i];
			}
		}
		return null;
	}

	@Override
	public boolean isAspect() {
		return delegate.isAspect();
	}

	@Override
	public boolean isAnnotationStyleAspect() {
		return delegate.isAnnotationStyleAspect();
	}

	@Override
	public boolean isEnum() {
		return delegate.isEnum();
	}

	@Override
	public boolean isAnnotation() {
		return delegate.isAnnotation();
	}

	@Override
	public boolean isAnonymous() {
		return delegate.isAnonymous();
	}

	@Override
	public boolean isNested() {
		return delegate.isNested();
	}

	public ResolvedType getOuterClass() {
		return delegate.getOuterClass();
	}

	public String getRetentionPolicy() {
		return delegate.getRetentionPolicy();
	}

	@Override
	public boolean isAnnotationWithRuntimeRetention() {
		return delegate.isAnnotationWithRuntimeRetention();
	}

	@Override
	public boolean canAnnotationTargetType() {
		return delegate.canAnnotationTargetType();
	}

	@Override
	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		return delegate.getAnnotationTargetKinds();
	}

	// true iff the statement "this = (ThisType) other" would compile
	@Override
	public boolean isCoerceableFrom(ResolvedType o) {
		ResolvedType other = o.resolve(world);

		if (this.isAssignableFrom(other) || other.isAssignableFrom(this)) {
			return true;
		}

		if (this.isParameterizedType() && other.isParameterizedType()) {
			return isCoerceableFromParameterizedType(other);
		}

		if (this.isParameterizedType() && other.isRawType()) {
			return ((ReferenceType) this.getRawType()).isCoerceableFrom(other.getGenericType());
		}

		if (this.isRawType() && other.isParameterizedType()) {
			return this.getGenericType().isCoerceableFrom((other.getRawType()));
		}

		if (!this.isInterface() && !other.isInterface()) {
			return false;
		}
		if (this.isFinal() || other.isFinal()) {
			return false;
		}
		// ??? needs to be Methods, not just declared methods? JLS 5.5 unclear
		ResolvedMember[] a = getDeclaredMethods();
		ResolvedMember[] b = other.getDeclaredMethods(); // ??? is this cast
		// always safe
		for (int ai = 0, alen = a.length; ai < alen; ai++) {
			for (int bi = 0, blen = b.length; bi < blen; bi++) {
				if (!b[bi].isCompatibleWith(a[ai])) {
					return false;
				}
			}
		}
		return true;
	}

	private final boolean isCoerceableFromParameterizedType(ResolvedType other) {
		if (!other.isParameterizedType()) {
			return false;
		}
		ResolvedType myRawType = getRawType();
		ResolvedType theirRawType = other.getRawType();
		if (myRawType == theirRawType || myRawType.isCoerceableFrom(theirRawType)) {
			if (getTypeParameters().length == other.getTypeParameters().length) {
				// there's a chance it can be done
				ResolvedType[] myTypeParameters = getResolvedTypeParameters();
				ResolvedType[] theirTypeParameters = other.getResolvedTypeParameters();
				for (int i = 0; i < myTypeParameters.length; i++) {
					if (myTypeParameters[i] != theirTypeParameters[i]) {
						// thin ice now... but List<String> may still be
						// coerceable from e.g. List<T>
						if (myTypeParameters[i].isGenericWildcard()) {
							BoundedReferenceType wildcard = (BoundedReferenceType) myTypeParameters[i];
							if (!wildcard.canBeCoercedTo(theirTypeParameters[i])) {
								return false;
							}
						} else if (myTypeParameters[i].isTypeVariableReference()) {
							TypeVariableReferenceType tvrt = (TypeVariableReferenceType) myTypeParameters[i];
							TypeVariable tv = tvrt.getTypeVariable();
							tv.resolve(world);
							if (!tv.canBeBoundTo(theirTypeParameters[i])) {
								return false;
							}
						} else if (theirTypeParameters[i].isTypeVariableReference()) {
							TypeVariableReferenceType tvrt = (TypeVariableReferenceType) theirTypeParameters[i];
							TypeVariable tv = tvrt.getTypeVariable();
							tv.resolve(world);
							if (!tv.canBeBoundTo(myTypeParameters[i])) {
								return false;
							}
						} else if (theirTypeParameters[i].isGenericWildcard()) {
							BoundedReferenceType wildcard = (BoundedReferenceType) theirTypeParameters[i];
							if (!wildcard.canBeCoercedTo(myTypeParameters[i])) {
								return false;
							}
						} else {
							return false;
						}
					}
				}
				return true;
			}
			// } else {
			// // we do this walk for situations like the following:
			// // Base<T>, Sub<S,T> extends Base<S>
			// // is Sub<Y,Z> coerceable from Base<X> ???
			// for (Iterator i = getDirectSupertypes(); i.hasNext();) {
			// ReferenceType parent = (ReferenceType) i.next();
			// if (parent.isCoerceableFromParameterizedType(other))
			// return true;
			// }
		}
		return false;
	}

	@Override
	public boolean isAssignableFrom(ResolvedType other) {
		return isAssignableFrom(other, false);
	}

	// TODO rewrite this method - it is a terrible mess

	// true iff the statement "this = other" would compile.
	@Override
	public boolean isAssignableFrom(ResolvedType other, boolean allowMissing) {
		if (other.isPrimitiveType()) {
			if (!world.isInJava5Mode()) {
				return false;
			}
			if (ResolvedType.validBoxing.contains(this.getSignature() + other.getSignature())) {
				return true;
			}
		}

		if (this == other) {
			return true;
		}

		if (this.getSignature().equals(ResolvedType.OBJECT.getSignature())) {
			return true;
		}

		boolean thisRaw = this.isRawType();
		boolean thisGeneric = this.isGenericType();

		if ((thisRaw || thisGeneric) && other.isParameterizedType()) {
			if (isAssignableFrom(other.getRawType())) {
				return true;
			}
		}
		if (thisRaw && other.isGenericType()) {
			if (isAssignableFrom(other.getRawType())) {
				return true;
			}
		}
		if (thisGeneric && other.isRawType()) {
			if (isAssignableFrom(other.getGenericType())) {
				return true;
			}
		}

		if (this.isParameterizedType()) {
			// look at wildcards...
			if (((ReferenceType) this.getRawType()).isAssignableFrom(other)) {
				boolean wildcardsAllTheWay = true;
				ResolvedType[] myParameters = this.getResolvedTypeParameters();
				for (int i = 0; i < myParameters.length; i++) {
					if (!myParameters[i].isGenericWildcard()) {
						wildcardsAllTheWay = false;
					} else {
						BoundedReferenceType boundedRT = (BoundedReferenceType) myParameters[i];
						if (boundedRT.isExtends() || boundedRT.isSuper()) {
							wildcardsAllTheWay = false;
						}
					}
				}
				if (wildcardsAllTheWay && !other.isParameterizedType()) {
					return true;
				}
				// we have to match by parameters one at a time
				ResolvedType[] theirParameters = other.getResolvedTypeParameters();
				boolean parametersAssignable = true;
				if (myParameters.length == theirParameters.length) {
					for (int i = 0; i < myParameters.length && parametersAssignable; i++) {
						if (myParameters[i] == theirParameters[i]) {
							continue;
						}
						// dont do this: pr253109
						// if (myParameters[i].isAssignableFrom(theirParameters[i], allowMissing)) {
						// continue;
						// }
						ResolvedType mp = myParameters[i];
						ResolvedType tp = theirParameters[i];
						if (mp.isParameterizedType() && tp.isParameterizedType()) {
							if (mp.getGenericType().equals(tp.getGenericType())) {
								UnresolvedType[] mtps = mp.getTypeParameters();
								UnresolvedType[] ttps = tp.getTypeParameters();
								for (int ii = 0; ii < mtps.length; ii++) {
									if (mtps[ii].isTypeVariableReference() && ttps[ii].isTypeVariableReference()) {
										TypeVariable mtv = ((TypeVariableReferenceType) mtps[ii]).getTypeVariable();
										boolean b = mtv.canBeBoundTo((ResolvedType) ttps[ii]);
										if (!b) {// TODO incomplete testing here I think
											parametersAssignable = false;
											break;
										}
									} else {
										parametersAssignable = false;
										break;
									}
								}
								continue;
							} else {
								parametersAssignable = false;
								break;
							}
						}
						if (myParameters[i].isTypeVariableReference() && theirParameters[i].isTypeVariableReference()) {
							TypeVariable myTV = ((TypeVariableReferenceType) myParameters[i]).getTypeVariable();
							// TypeVariable theirTV = ((TypeVariableReferenceType) theirParameters[i]).getTypeVariable();
							boolean b = myTV.canBeBoundTo(theirParameters[i]);
							if (!b) {// TODO incomplete testing here I think
								parametersAssignable = false;
								break;
							} else {
								continue;
							}
						}
						if (!myParameters[i].isGenericWildcard()) {
							parametersAssignable = false;
							break;
						} else {
							BoundedReferenceType wildcardType = (BoundedReferenceType) myParameters[i];
							if (!wildcardType.alwaysMatches(theirParameters[i])) {
								parametersAssignable = false;
								break;
							}
						}
					}
				} else {
					parametersAssignable = false;
				}
				if (parametersAssignable) {
					return true;
				}
			}
		}

		if (isTypeVariableReference() && !other.isTypeVariableReference()) { // eg
			// .
			// this
			// =
			// T
			// other
			// =
			// Ljava
			// /
			// lang
			// /
			// Object
			// ;
			TypeVariable aVar = ((TypeVariableReference) this).getTypeVariable();
			return aVar.resolve(world).canBeBoundTo(other);
		}

		if (other.isTypeVariableReference()) {
			TypeVariableReferenceType otherType = (TypeVariableReferenceType) other;
			if (this instanceof TypeVariableReference) {
				return ((TypeVariableReference) this).getTypeVariable().resolve(world).canBeBoundTo(
						otherType.getTypeVariable().getFirstBound().resolve(world));// pr171952
				// return
				// ((TypeVariableReference)this).getTypeVariable()==otherType
				// .getTypeVariable();
			} else {
				// FIXME asc should this say canBeBoundTo??
				return this.isAssignableFrom(otherType.getTypeVariable().getFirstBound().resolve(world));
			}
		}

		if (allowMissing && other.isMissing()) {
			return false;
		}

		ResolvedType[] interfaces = other.getDeclaredInterfaces();
		for (ResolvedType intface : interfaces) {
			if (this.isAssignableFrom(intface, allowMissing)) {
				return true;
			}
		}
		ResolvedType superclass = other.getSuperclass();
		if (superclass != null) {
			if (this.isAssignableFrom(superclass, allowMissing)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ISourceContext getSourceContext() {
		return delegate.getSourceContext();
	}

	@Override
	public ISourceLocation getSourceLocation() {
		ISourceContext isc = delegate.getSourceContext();
		return isc.makeSourceLocation(new Position(startPos, endPos));
	}

	@Override
	public boolean isExposedToWeaver() {
		return (delegate == null) || delegate.isExposedToWeaver();
	}

	@Override
	public WeaverStateInfo getWeaverState() {
		return delegate.getWeaverState();
	}

	@Override
	public ResolvedMember[] getDeclaredFields() {
		if (parameterizedFields != null) {
			return parameterizedFields;
		}
		if (isParameterizedType() || isRawType()) {
			ResolvedMember[] delegateFields = delegate.getDeclaredFields();
			parameterizedFields = new ResolvedMember[delegateFields.length];
			for (int i = 0; i < delegateFields.length; i++) {
				parameterizedFields[i] = delegateFields[i].parameterizedWith(getTypesForMemberParameterization(), this,
						isParameterizedType());
			}
			return parameterizedFields;
		} else {
			return delegate.getDeclaredFields();
		}
	}

	/**
	 * Find out from the generic signature the true signature of any interfaces I implement. If I am parameterized, these may then
	 * need to be parameterized before returning.
	 */
	@Override
	public ResolvedType[] getDeclaredInterfaces() {
		ResolvedType[] interfaces = parameterizedInterfaces.get();
		if (interfaces != null) {
			return interfaces;
		}
		ResolvedType[] delegateInterfaces = delegate.getDeclaredInterfaces();
		if (newInterfaces != null) {
			// OPTIMIZE does this part of the method trigger often?
			ResolvedType[] extraInterfaces = new ResolvedType[delegateInterfaces.length + newInterfaces.length];
			System.arraycopy(delegateInterfaces, 0, extraInterfaces, 0, delegateInterfaces.length);
			System.arraycopy(newInterfaces, 0, extraInterfaces, delegateInterfaces.length, newInterfaces.length);
			delegateInterfaces = extraInterfaces;
		}
		if (isParameterizedType()) {
			// UnresolvedType[] paramTypes =
			// getTypesForMemberParameterization();
			interfaces = new ResolvedType[delegateInterfaces.length];
			for (int i = 0; i < delegateInterfaces.length; i++) {
				// We may have to sub/super set the set of parametertypes if the
				// implemented interface
				// needs more or less than this type does. (pr124803/pr125080)

				if (delegateInterfaces[i].isParameterizedType()) {
					interfaces[i] = delegateInterfaces[i].parameterize(getMemberParameterizationMap()).resolve(world);
				} else {
					interfaces[i] = delegateInterfaces[i];
				}
			}
			parameterizedInterfaces = new WeakReference<ResolvedType[]>(interfaces);
			return interfaces;
		} else if (isRawType()) {
			UnresolvedType[] paramTypes = getTypesForMemberParameterization();
			interfaces = new ResolvedType[delegateInterfaces.length];
			for (int i = 0, max = interfaces.length; i < max; i++) {
				interfaces[i] = delegateInterfaces[i];
				if (interfaces[i].isGenericType()) {
					// a generic supertype of a raw type is replaced by its raw
					// equivalent
					interfaces[i] = interfaces[i].getRawType().resolve(getWorld());
				} else if (interfaces[i].isParameterizedType()) {
					// a parameterized supertype collapses any type vars to
					// their upper bounds
					UnresolvedType[] toUseForParameterization = determineThoseTypesToUse(interfaces[i], paramTypes);
					interfaces[i] = interfaces[i].parameterizedWith(toUseForParameterization);
				}
			}
			parameterizedInterfaces = new WeakReference<ResolvedType[]>(interfaces);
			return interfaces;
		}
		if (delegate.isCacheable()) {
			parameterizedInterfaces = new WeakReference<ResolvedType[]>(delegateInterfaces);
		}
		return delegateInterfaces;
	}

	private String toString(ResolvedType[] delegateInterfaces) {
		StringBuffer sb = new StringBuffer();
		if (delegateInterfaces != null) {
			for (ResolvedType rt : delegateInterfaces) {
				sb.append(rt).append(" ");
			}
		}
		return sb.toString();
	}

	/**
	 * Locates the named type variable in the list of those on this generic type and returns the type parameter from the second list
	 * supplied. Returns null if it can't be found
	 */
	// private UnresolvedType findTypeParameterInList(String name,
	// TypeVariable[] tvarsOnThisGenericType, UnresolvedType[]
	// paramTypes) {
	// int position = -1;
	// for (int i = 0; i < tvarsOnThisGenericType.length; i++) {
	// TypeVariable tv = tvarsOnThisGenericType[i];
	// if (tv.getName().equals(name)) position = i;
	// }
	// if (position == -1 ) return null;
	// return paramTypes[position];
	// }
	/**
	 * It is possible this type has multiple type variables but the interface we are about to parameterize only uses a subset - this
	 * method determines the subset to use by looking at the type variable names used. For example: <code>
	 * class Foo<T extends String,E extends Number> implements SuperInterface<T> {}
	 * </code> where <code>
	 * interface SuperInterface<Z> {}
	 * </code> In that
	 * example, a use of the 'Foo' raw type should know that it implements the SuperInterface<String>.
	 */
	private UnresolvedType[] determineThoseTypesToUse(ResolvedType parameterizedInterface, UnresolvedType[] paramTypes) {
		// What are the type parameters for the supertype?
		UnresolvedType[] tParms = parameterizedInterface.getTypeParameters();
		UnresolvedType[] retVal = new UnresolvedType[tParms.length];

		// Go through the supertypes type parameters, if any of them is a type
		// variable, use the
		// real type variable on the declaring type.

		// it is possibly overkill to look up the type variable - ideally the
		// entry in the type parameter list for the
		// interface should be the a ref to the type variable in the current
		// type ... but I'm not 100% confident right now.
		for (int i = 0; i < tParms.length; i++) {
			UnresolvedType tParm = tParms[i];
			if (tParm.isTypeVariableReference()) {
				TypeVariableReference tvrt = (TypeVariableReference) tParm;
				TypeVariable tv = tvrt.getTypeVariable();
				int rank = getRank(tv.getName());
				// -1 probably means it is a reference to a type variable on the
				// outer generic type (see pr129566)
				if (rank != -1) {
					retVal[i] = paramTypes[rank];
				} else {
					retVal[i] = tParms[i];
				}
			} else {
				retVal[i] = tParms[i];
			}

		}
		return retVal;
	}

	/**
	 * Returns the position within the set of type variables for this type for the specified type variable name. Returns -1 if there
	 * is no type variable with the specified name.
	 */
	private int getRank(String tvname) {
		TypeVariable[] thisTypesTVars = getGenericType().getTypeVariables();
		for (int i = 0; i < thisTypesTVars.length; i++) {
			TypeVariable tv = thisTypesTVars[i];
			if (tv.getName().equals(tvname)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public ResolvedMember[] getDeclaredMethods() {
		if (parameterizedMethods != null) {
			return parameterizedMethods;
		}
		if (isParameterizedType() || isRawType()) {
			ResolvedMember[] delegateMethods = delegate.getDeclaredMethods();
			UnresolvedType[] parameters = getTypesForMemberParameterization();
			parameterizedMethods = new ResolvedMember[delegateMethods.length];
			for (int i = 0; i < delegateMethods.length; i++) {
				parameterizedMethods[i] = delegateMethods[i].parameterizedWith(parameters, this, isParameterizedType());
			}
			return parameterizedMethods;
		} else {
			return delegate.getDeclaredMethods();
		}
	}

	@Override
	public ResolvedMember[] getDeclaredPointcuts() {
		if (parameterizedPointcuts != null) {
			return parameterizedPointcuts;
		}
		if (isParameterizedType()) {
			ResolvedMember[] delegatePointcuts = delegate.getDeclaredPointcuts();
			parameterizedPointcuts = new ResolvedMember[delegatePointcuts.length];
			for (int i = 0; i < delegatePointcuts.length; i++) {
				parameterizedPointcuts[i] = delegatePointcuts[i].parameterizedWith(getTypesForMemberParameterization(), this,
						isParameterizedType());
			}
			return parameterizedPointcuts;
		} else {
			return delegate.getDeclaredPointcuts();
		}
	}

	private UnresolvedType[] getTypesForMemberParameterization() {
		UnresolvedType[] parameters = null;
		if (isParameterizedType()) {
			parameters = getTypeParameters();
		} else if (isRawType()) {
			// raw type, use upper bounds of type variables on generic type
			TypeVariable[] tvs = getGenericType().getTypeVariables();
			parameters = new UnresolvedType[tvs.length];
			for (int i = 0; i < tvs.length; i++) {
				parameters[i] = tvs[i].getFirstBound();
			}
		}
		return parameters;
	}

	@Override
	public TypeVariable[] getTypeVariables() {
		if (this.typeVariables == null) {
			this.typeVariables = delegate.getTypeVariables();
			for (int i = 0; i < this.typeVariables.length; i++) {
				this.typeVariables[i].resolve(world);
			}
		}
		return this.typeVariables;
	}

	@Override
	public PerClause getPerClause() {
		PerClause pclause = delegate.getPerClause();
		if (isParameterizedType()) { // could cache the result here...
			Map parameterizationMap = getAjMemberParameterizationMap();
			pclause = (PerClause) pclause.parameterizeWith(parameterizationMap, world);
		}
		return pclause;
	}

	@Override
	public Collection<Declare> getDeclares() {
		if (parameterizedDeclares != null) {
			return parameterizedDeclares;
		}
		Collection<Declare> declares = null;
		if (ajMembersNeedParameterization()) {
			Collection<Declare> genericDeclares = delegate.getDeclares();
			parameterizedDeclares = new ArrayList<Declare>();
			Map<String, UnresolvedType> parameterizationMap = getAjMemberParameterizationMap();
			for (Declare declareStatement : genericDeclares) {
				parameterizedDeclares.add(declareStatement.parameterizeWith(parameterizationMap, world));
			}
			declares = parameterizedDeclares;
		} else {
			declares = delegate.getDeclares();
		}
		for (Declare d : declares) {
			d.setDeclaringType(this);
		}
		return declares;
	}

	@Override
	public Collection<ConcreteTypeMunger> getTypeMungers() {
		return delegate.getTypeMungers();
	}

	// GENERICITDFIX
	// // Map parameterizationMap = getAjMemberParameterizationMap();
	//		
	// // if (parameterizedTypeMungers != null) return parameterizedTypeMungers;
	// Collection ret = null;
	// if (ajMembersNeedParameterization()) {
	// Collection genericDeclares = delegate.getTypeMungers();
	// parameterizedTypeMungers = new ArrayList();
	// Map parameterizationMap = getAjMemberParameterizationMap();
	// for (Iterator iter = genericDeclares.iterator(); iter.hasNext();) {
	// ConcreteTypeMunger munger = (ConcreteTypeMunger)iter.next();
	// parameterizedTypeMungers.add(munger.parameterizeWith(parameterizationMap,
	// world));
	// }
	// ret = parameterizedTypeMungers;
	// } else {
	// ret = delegate.getTypeMungers();
	// }
	// return ret;
	// }

	@Override
	public Collection getPrivilegedAccesses() {
		return delegate.getPrivilegedAccesses();
	}

	@Override
	public int getModifiers() {
		return delegate.getModifiers();
	}

	WeakReference<ResolvedType> superclassReference = new WeakReference<ResolvedType>(null);

	@Override
	public ResolvedType getSuperclass() {
		ResolvedType ret = null;// superclassReference.get();
		if (ret != null) {
			return ret;
		}
		if (newSuperclass != null) {
			if (this.isParameterizedType() && newSuperclass.isParameterizedType()) {
				return newSuperclass.parameterize(getMemberParameterizationMap()).resolve(getWorld());
			}
			if (delegate.isCacheable()) {
				superclassReference = new WeakReference<ResolvedType>(ret);
			}
			return newSuperclass;
		}
		try {
			world.setTypeVariableLookupScope(this);
			ret = delegate.getSuperclass();
		} finally {
			world.setTypeVariableLookupScope(null);
		}
		if (this.isParameterizedType() && ret.isParameterizedType()) {
			ret = ret.parameterize(getMemberParameterizationMap()).resolve(getWorld());
		}
		if (delegate.isCacheable()) {
			superclassReference = new WeakReference<ResolvedType>(ret);
		}
		return ret;
	}

	public ReferenceTypeDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(ReferenceTypeDelegate delegate) {
		// Don't copy from BcelObjectType to EclipseSourceType - the context may
		// be tidied (result null'd) after previous weaving
		if (this.delegate != null && this.delegate.copySourceContext()
				&& this.delegate.getSourceContext() != SourceContextImpl.UNKNOWN_SOURCE_CONTEXT) {
			((AbstractReferenceTypeDelegate) delegate).setSourceContext(this.delegate.getSourceContext());
		}
		this.delegate = delegate;
		for (Iterator it = this.derivativeTypes.iterator(); it.hasNext();) {
			ReferenceType dependent = (ReferenceType) it.next();
			dependent.setDelegate(delegate);
		}

		// If we are raw, we have a generic type - we should ensure it uses the
		// same delegate
		if (isRawType() && getGenericType() != null) {
			ReferenceType genType = (ReferenceType) getGenericType();
			if (genType.getDelegate() != delegate) { // avoids circular updates
				genType.setDelegate(delegate);
			}
		}
		clearParameterizationCaches();
		ensureConsistent();
	}

	private void clearParameterizationCaches() {
		parameterizedFields = null;
		parameterizedInterfaces.clear();
		parameterizedMethods = null;
		parameterizedPointcuts = null;
		superclassReference = new WeakReference<ResolvedType>(null);
	}

	public int getEndPos() {
		return endPos;
	}

	public int getStartPos() {
		return startPos;
	}

	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	@Override
	public boolean doesNotExposeShadowMungers() {
		return delegate.doesNotExposeShadowMungers();
	}

	public String getDeclaredGenericSignature() {
		return delegate.getDeclaredGenericSignature();
	}

	public void setGenericType(ReferenceType rt) {
		genericType = rt;
		// Should we 'promote' this reference type from simple to raw?
		// makes sense if someone is specifying that it has a generic form
		if (typeKind == TypeKind.SIMPLE) {
			typeKind = TypeKind.RAW;
			signatureErasure = signature;
		}
	}

	public void demoteToSimpleType() {
		genericType = null;
		typeKind = TypeKind.SIMPLE;
		signatureErasure = null;
	}

	@Override
	public ResolvedType getGenericType() {
		if (isGenericType()) {
			return this;
		}
		return genericType;
	}

	/**
	 * a parameterized signature starts with a "P" in place of the "L", see the comment on signatures in UnresolvedType.
	 * 
	 * @param aGenericType
	 * @param someParameters
	 * @return
	 */
	private static String makeParameterizedSignature(ResolvedType aGenericType, ResolvedType[] someParameters) {
		String rawSignature = aGenericType.getErasureSignature();
		StringBuffer ret = new StringBuffer();
		ret.append(PARAMETERIZED_TYPE_IDENTIFIER);
		ret.append(rawSignature.substring(1, rawSignature.length() - 1));
		ret.append("<");
		for (int i = 0; i < someParameters.length; i++) {
			ret.append(someParameters[i].getSignature());
		}
		ret.append(">;");
		return ret.toString();
	}

	private static String makeDeclaredSignature(ResolvedType aGenericType, UnresolvedType[] someParameters) {
		StringBuffer ret = new StringBuffer();
		String rawSig = aGenericType.getErasureSignature();
		ret.append(rawSig.substring(0, rawSig.length() - 1));
		ret.append("<");
		for (int i = 0; i < someParameters.length; i++) {
			ret.append(((ReferenceType) someParameters[i]).getSignatureForAttribute());
		}
		ret.append(">;");
		return ret.toString();
	}

	@Override
	public void ensureConsistent() {
		annotations = null;
		annotationTypes = null;
		newSuperclass = null;
		newInterfaces = null;
		parameterizedInterfaces.clear();
		superclassReference = new WeakReference<ResolvedType>(null);
		if (delegate != null) {
			delegate.ensureConsistent();
		}
	}

	@Override
	public void addParent(ResolvedType newParent) {
		if (newParent.isClass()) {
			newSuperclass = newParent;
			superclassReference = new WeakReference<ResolvedType>(null);
		} else {
			if (newInterfaces == null) {
				newInterfaces = new ResolvedType[1];
				newInterfaces[0] = newParent;
			} else {
				ResolvedType[] existing = delegate.getDeclaredInterfaces();
				if (existing != null) {
					for (int i = 0; i < existing.length; i++) {
						if (existing[i].equals(newParent)) {
							return; // already has this interface
						}
					}
				}
				ResolvedType[] newNewInterfaces = new ResolvedType[newInterfaces.length + 1];
				System.arraycopy(newInterfaces, 0, newNewInterfaces, 1, newInterfaces.length);
				newNewInterfaces[0] = newParent;
				newInterfaces = newNewInterfaces;
			}
			parameterizedInterfaces.clear();
		}
	}
}