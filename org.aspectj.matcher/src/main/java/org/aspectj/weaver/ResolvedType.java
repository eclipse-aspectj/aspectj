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
 *     Alexandre Vasseur    @AspectJ ITDs
 * ******************************************************************/

package org.aspectj.weaver;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.Iterators.Getter;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;

public abstract class ResolvedType extends UnresolvedType implements AnnotatedElement {

	public static final ResolvedType[] EMPTY_RESOLVED_TYPE_ARRAY = new ResolvedType[0];
	public static final String PARAMETERIZED_TYPE_IDENTIFIER = "P";

	// Set temporarily during a type pattern match call - this currently used to hold the
	// annotations that may be attached to a type when it used as a parameter
	public ResolvedType[] temporaryAnnotationTypes;
	private ResolvedType[] resolvedTypeParams;
	private String binaryPath;

	protected World world;

	protected int bits;

	private static int AnnotationBitsInitialized = 0x0001;
	private static int AnnotationMarkedInherited = 0x0002;
	private static int MungersAnalyzed = 0x0004;
	private static int HasParentMunger = 0x0008;
	private static int TypeHierarchyCompleteBit = 0x0010;
	private static int GroovyObjectInitialized = 0x0020;
	private static int IsGroovyObject = 0x0040;
	private static int IsPrivilegedBitInitialized = 0x0080;
	private static int IsPrivilegedAspect = 0x0100;

	protected ResolvedType(String signature, World world) {
		super(signature);
		this.world = world;
	}

	protected ResolvedType(String signature, String signatureErasure, World world) {
		super(signature, signatureErasure);
		this.world = world;
	}

	@Override
	public int getSize() {
		return 1;
	}

	/**
	 * Returns an iterator through ResolvedType objects representing all the direct supertypes of this type. That is, through the
	 * superclass, if any, and all declared interfaces.
	 */
	public final Iterator<ResolvedType> getDirectSupertypes() {
		Iterator<ResolvedType> interfacesIterator = Iterators.array(getDeclaredInterfaces());
		ResolvedType superclass = getSuperclass();
		if (superclass == null) {
			return interfacesIterator;
		} else {
			return Iterators.snoc(interfacesIterator, superclass);
		}
	}

	public abstract ResolvedMember[] getDeclaredFields();

	public abstract ResolvedMember[] getDeclaredMethods();

	public abstract ResolvedType[] getDeclaredInterfaces();

	public abstract ResolvedMember[] getDeclaredPointcuts();

	public boolean isCacheable() {
		return true;
	}

	/**
	 * @return the superclass of this type, or null (if this represents a jlObject, primitive, or void)
	 */
	public abstract ResolvedType getSuperclass();

	public abstract int getModifiers();

	public boolean canBeSeenBy(ResolvedType from) {
		int targetMods = getModifiers();
		if (Modifier.isPublic(targetMods)) {
			return true;
		}
		if (Modifier.isPrivate(targetMods)) {
			return false;
		}
		// isProtected() or isDefault()
		return getPackageName().equals(from.getPackageName());
	}

	// return true if this resolved type couldn't be found (but we know it's name maybe)
	public boolean isMissing() {
		return false;
	}

	// FIXME asc I wonder if in some circumstances MissingWithKnownSignature
	// should not be considered
	// 'really' missing as some code can continue based solely on the signature
	public static boolean isMissing(UnresolvedType unresolved) {
		if (unresolved instanceof ResolvedType) {
			ResolvedType resolved = (ResolvedType) unresolved;
			return resolved.isMissing();
		} else {
			return (unresolved == MISSING);
		}
	}

	@Override
	public ResolvedType[] getAnnotationTypes() {
		return EMPTY_RESOLVED_TYPE_ARRAY;
	}

	@Override
	public AnnotationAJ getAnnotationOfType(UnresolvedType ofType) {
		return null;
	}

	// public final UnresolvedType getSuperclass(World world) {
	// return getSuperclass();
	// }

	// This set contains pairs of types whose signatures are concatenated
	// together, this means with a fast lookup we can tell if two types
	// are equivalent.
	protected static Set<String> validBoxing = new HashSet<>();

	static {
		validBoxing.add("Ljava/lang/Byte;B");
		validBoxing.add("Ljava/lang/Character;C");
		validBoxing.add("Ljava/lang/Double;D");
		validBoxing.add("Ljava/lang/Float;F");
		validBoxing.add("Ljava/lang/Integer;I");
		validBoxing.add("Ljava/lang/Long;J");
		validBoxing.add("Ljava/lang/Short;S");
		validBoxing.add("Ljava/lang/Boolean;Z");
		validBoxing.add("BLjava/lang/Byte;");
		validBoxing.add("CLjava/lang/Character;");
		validBoxing.add("DLjava/lang/Double;");
		validBoxing.add("FLjava/lang/Float;");
		validBoxing.add("ILjava/lang/Integer;");
		validBoxing.add("JLjava/lang/Long;");
		validBoxing.add("SLjava/lang/Short;");
		validBoxing.add("ZLjava/lang/Boolean;");
	}

	// utilities
	public ResolvedType getResolvedComponentType() {
		return null;
	}

	public World getWorld() {
		return world;
	}

	// ---- things from object

	@Override
	public boolean equals(Object other) {
		if (other instanceof ResolvedType) {
			return this == other;
		} else {
			return super.equals(other);
		}
	}

	// ---- difficult things

	/**
	 * returns an iterator through all of the fields of this type, in order for checking from JVM spec 2ed 5.4.3.2. This means that
	 * the order is
	 * <ul>
	 * <li>fields from current class</li>
	 * <li>recur into direct superinterfaces</li>
	 * <li>recur into superclass</li>
	 * </ul>
	 * <p>
	 * We keep a hashSet of interfaces that we've visited so we don't spiral out into 2^n land.
	 * </p>
	 */
	public Iterator<ResolvedMember> getFields() {
		final Iterators.Filter<ResolvedType> dupFilter = Iterators.dupFilter();
		Iterators.Getter<ResolvedType, ResolvedType> typeGetter = new Iterators.Getter<ResolvedType, ResolvedType>() {
			@Override
			public Iterator<ResolvedType> get(ResolvedType o) {
				return dupFilter.filter(o.getDirectSupertypes());
			}
		};
		return Iterators.mapOver(Iterators.recur(this, typeGetter), FieldGetterInstance);
	}

	/**
	 * returns an iterator through all of the methods of this type, in order for checking from JVM spec 2ed 5.4.3.3. This means that
	 * the order is
	 * <ul>
	 * <li>methods from current class</li>
	 * <li>recur into superclass, all the way up, not touching interfaces</li>
	 * <li>recur into all superinterfaces, in some unspecified order (but those 'closest' to this type are first)</li>
	 * </ul>
	 * 
	 * @param wantGenerics is true if the caller would like all generics information, otherwise those methods are collapsed to their
	 *        erasure
	 */
	public Iterator<ResolvedMember> getMethods(boolean wantGenerics, boolean wantDeclaredParents) {
		return Iterators.mapOver(getHierarchy(wantGenerics, wantDeclaredParents), MethodGetterInstance);
	}

	public Iterator<ResolvedMember> getMethodsIncludingIntertypeDeclarations(boolean wantGenerics, boolean wantDeclaredParents) {
		return Iterators.mapOver(getHierarchy(wantGenerics, wantDeclaredParents), MethodGetterWithItdsInstance);
	}

	/**
	 * An Iterators.Getter that returns an iterator over all methods declared on some resolved type.
	 */
	private static class MethodGetter implements Iterators.Getter<ResolvedType, ResolvedMember> {
		@Override
		public Iterator<ResolvedMember> get(ResolvedType type) {
			return Iterators.array(type.getDeclaredMethods());
		}
	}

	/**
	 * An Iterators.Getter that returns an iterator over all pointcuts declared on some resolved type.
	 */
	private static class PointcutGetter implements Iterators.Getter<ResolvedType, ResolvedMember> {
		@Override
		public Iterator<ResolvedMember> get(ResolvedType o) {
			return Iterators.array(o.getDeclaredPointcuts());
		}
	}

	// OPTIMIZE could cache the result of discovering ITDs

	// Getter that returns all declared methods for a type through an iterator - including intertype declarations
	private static class MethodGetterIncludingItds implements Iterators.Getter<ResolvedType, ResolvedMember> {
		@Override
		public Iterator<ResolvedMember> get(ResolvedType type) {
			ResolvedMember[] methods = type.getDeclaredMethods();
			if (type.interTypeMungers != null) {
				int additional = 0;
				for (ConcreteTypeMunger typeTransformer : type.interTypeMungers) {
					ResolvedMember rm = typeTransformer.getSignature();
					// BUG won't this include fields? When we are looking for methods
					if (rm != null) { // new parent type munger can have null signature
						additional++;
					}
				}
				if (additional > 0) {
					ResolvedMember[] methods2 = new ResolvedMember[methods.length + additional];
					System.arraycopy(methods, 0, methods2, 0, methods.length);
					additional = methods.length;
					for (ConcreteTypeMunger typeTransformer : type.interTypeMungers) {
						ResolvedMember rm = typeTransformer.getSignature();
						if (rm != null) { // new parent type munger can have null signature
							methods2[additional++] = typeTransformer.getSignature();
						}
					}
					methods = methods2;
				}
			}
			return Iterators.array(methods);
		}
	}

	/**
	 * An Iterators.Getter that returns an iterator over all fields declared on some resolved type.
	 */
	private static class FieldGetter implements Iterators.Getter<ResolvedType, ResolvedMember> {
		@Override
		public Iterator<ResolvedMember> get(ResolvedType type) {
			return Iterators.array(type.getDeclaredFields());
		}
	}

	private final static MethodGetter MethodGetterInstance = new MethodGetter();
	private final static MethodGetterIncludingItds MethodGetterWithItdsInstance = new MethodGetterIncludingItds();
	private final static PointcutGetter PointcutGetterInstance = new PointcutGetter();
	private final static FieldGetter FieldGetterInstance = new FieldGetter();

	/**
	 * Return an iterator over the types in this types hierarchy - starting with this type first, then all superclasses up to Object
	 * and then all interfaces (starting with those 'nearest' this type).
	 * @return an iterator over all types in the hierarchy of this type
	 */
	public Iterator<ResolvedType> getHierarchy() {
		return getHierarchy(false, false);
	}

	/**
	 * Return an iterator over the types in this types hierarchy - starting with this type first, then all superclasses up to Object
	 * and then all interfaces (starting with those 'nearest' this type).
	 *
	 * @param wantGenerics true if the caller wants full generic information
	 * @param wantDeclaredParents true if the caller even wants those parents introduced via declare parents
	 * @return an iterator over all types in the hierarchy of this type
	 */
	public Iterator<ResolvedType> getHierarchy(final boolean wantGenerics, final boolean wantDeclaredParents) {

		final Iterators.Getter<ResolvedType, ResolvedType> interfaceGetter = new Iterators.Getter<ResolvedType, ResolvedType>() {
			List<String> alreadySeen = new ArrayList<>(); // Strings are signatures (ResolvedType.getSignature())

			@Override
			public Iterator<ResolvedType> get(ResolvedType type) {
				ResolvedType[] interfaces = type.getDeclaredInterfaces();

				// remove interfaces introduced by declare parents
				// relatively expensive but hopefully uncommon
				if (!wantDeclaredParents && type.hasNewParentMungers()) {
					// Throw away interfaces from that array if they were decp'd onto here
					List<Integer> forRemoval = new ArrayList<>();
					for (ConcreteTypeMunger munger : type.interTypeMungers) {
						if (munger.getMunger() != null) {
							ResolvedTypeMunger m = munger.getMunger();
							if (m.getKind() == ResolvedTypeMunger.Parent) {
								ResolvedType newType = ((NewParentTypeMunger) m).getNewParent();
								if (!wantGenerics && newType.isParameterizedOrGenericType()) {
									newType = newType.getRawType();
								}
								for (int ii = 0; ii < interfaces.length; ii++) {
									ResolvedType iface = interfaces[ii];
									if (!wantGenerics && iface.isParameterizedOrGenericType()) {
										iface = iface.getRawType();
									}
									if (newType.getSignature().equals(iface.getSignature())) { // pr171953
										forRemoval.add(ii);
									}
								}
							}
						}
					}
					// Found some to remove from those we are going to iterate over
					if (forRemoval.size() > 0) {
						ResolvedType[] interfaces2 = new ResolvedType[interfaces.length - forRemoval.size()];
						int p = 0;
						for (int ii = 0; ii < interfaces.length; ii++) {
							if (!forRemoval.contains(ii)) {
								interfaces2[p++] = interfaces[ii];
							}
						}
						interfaces = interfaces2;
					}
				}
				return new Iterators.ResolvedTypeArrayIterator(interfaces, alreadySeen, wantGenerics);
			}
		};

		// If this type is an interface, there are only interfaces to walk
		if (this.isInterface()) {
			return new SuperInterfaceWalker(interfaceGetter, this);
		} else {
			SuperInterfaceWalker superInterfaceWalker = new SuperInterfaceWalker(interfaceGetter);
			Iterator<ResolvedType> superClassesIterator = new SuperClassWalker(this, superInterfaceWalker, wantGenerics);
			// append() will check if the second iterator is empty before appending - but the types which the superInterfaceWalker
			// needs to visit are only accumulated whilst the superClassesIterator is in progress
			return Iterators.append1(superClassesIterator, superInterfaceWalker);
		}
	}

	/**
	 * Return a list of methods, first those declared on this class, then those declared on the superclass (recurse) and then those
	 * declared on the superinterfaces. This is expensive - use the getMethods() method if you can!
	 */
	public List<ResolvedMember> getMethodsWithoutIterator(boolean includeITDs, boolean allowMissing, boolean genericsAware) {
		List<ResolvedMember> methods = new ArrayList<>();
		Set<String> knowninterfaces = new HashSet<>();
		addAndRecurse(knowninterfaces, methods, this, includeITDs, allowMissing, genericsAware);
		return methods;
	}

	/**
	 * Return a list of the types in the hierarchy of this type, starting with this type. The order in the list is the superclasses
	 * followed by the super interfaces.
	 * 
	 * @param genericsAware should the list include parameterized/generic types (if not, they will be collapsed to raw)?
	 * @return list of resolvedtypes in this types hierarchy, including this type first
	 */
	public List<ResolvedType> getHierarchyWithoutIterator(boolean includeITDs, boolean allowMissing, boolean genericsAware) {
		List<ResolvedType> types = new ArrayList<>();
		Set<String> visited = new HashSet<>();
		recurseHierarchy(visited, types, this, includeITDs, allowMissing, genericsAware);
		return types;
	}

	private void addAndRecurse(Set<String> knowninterfaces, List<ResolvedMember> collector, ResolvedType resolvedType,
			boolean includeITDs, boolean allowMissing, boolean genericsAware) {
		// Add the methods declared on this type
		collector.addAll(Arrays.asList(resolvedType.getDeclaredMethods()));
		// now add all the inter-typed members too
		if (includeITDs && resolvedType.interTypeMungers != null) {
			for (ConcreteTypeMunger typeTransformer : interTypeMungers) {
				ResolvedMember rm = typeTransformer.getSignature();
				if (rm != null) { // new parent type munger can have null signature
					collector.add(typeTransformer.getSignature());
				}
			}
		}
		// BUG? interface type superclass is Object - is that correct?
		if (!resolvedType.isInterface() && !resolvedType.equals(ResolvedType.OBJECT)) {
			ResolvedType superType = resolvedType.getSuperclass();
			if (superType != null && !superType.isMissing()) {
				if (!genericsAware && superType.isParameterizedOrGenericType()) {
					superType = superType.getRawType();
				}
				// Recurse if we are not at the top
				addAndRecurse(knowninterfaces, collector, superType, includeITDs, allowMissing, genericsAware);
			}
		}
		// Go through the interfaces on the way back down
		ResolvedType[] interfaces = resolvedType.getDeclaredInterfaces();
		for (ResolvedType anInterface : interfaces) {
			ResolvedType iface = anInterface;
			if (!genericsAware && iface.isParameterizedOrGenericType()) {
				iface = iface.getRawType();
			}
			// we need to know if it is an interface from Parent kind munger
			// as those are used for @AJ ITD and we precisely want to skip those
			boolean shouldSkip = false;
			for (int j = 0; j < resolvedType.interTypeMungers.size(); j++) {
				ConcreteTypeMunger munger = resolvedType.interTypeMungers.get(j);
				if (munger.getMunger() != null && munger.getMunger().getKind() == ResolvedTypeMunger.Parent
						&& ((NewParentTypeMunger) munger.getMunger()).getNewParent().equals(iface) // pr171953
				) {
					shouldSkip = true;
					break;
				}
			}

			// Do not do interfaces more than once
			if (!shouldSkip && !knowninterfaces.contains(iface.getSignature())) {
				knowninterfaces.add(iface.getSignature());
				if (allowMissing && iface.isMissing()) {
					if (iface instanceof MissingResolvedTypeWithKnownSignature) {
						((MissingResolvedTypeWithKnownSignature) iface).raiseWarningOnMissingInterfaceWhilstFindingMethods();
					}
				} else {
					addAndRecurse(knowninterfaces, collector, iface, includeITDs, allowMissing, genericsAware);
				}
			}
		}
	}

	/**
	 * Recurse up a type hierarchy, first the superclasses then the super interfaces.
	 */
	private void recurseHierarchy(Set<String> knowninterfaces, List<ResolvedType> collector, ResolvedType resolvedType,
			boolean includeITDs, boolean allowMissing, boolean genericsAware) {
		collector.add(resolvedType);
		if (!resolvedType.isInterface() && !resolvedType.equals(ResolvedType.OBJECT)) {
			ResolvedType superType = resolvedType.getSuperclass();
			if (superType != null && !superType.isMissing()) {
				if (!genericsAware && (superType.isParameterizedType() || superType.isGenericType())) {
					superType = superType.getRawType();
				}
				// Recurse if we are not at the top
				recurseHierarchy(knowninterfaces, collector, superType, includeITDs, allowMissing, genericsAware);
			}
		}
		// Go through the interfaces on the way back down
		ResolvedType[] interfaces = resolvedType.getDeclaredInterfaces();
		for (ResolvedType anInterface : interfaces) {
			ResolvedType iface = anInterface;
			if (!genericsAware && (iface.isParameterizedType() || iface.isGenericType())) {
				iface = iface.getRawType();
			}
			// we need to know if it is an interface from Parent kind munger
			// as those are used for @AJ ITD and we precisely want to skip those
			boolean shouldSkip = false;
			for (int j = 0; j < resolvedType.interTypeMungers.size(); j++) {
				ConcreteTypeMunger munger = resolvedType.interTypeMungers.get(j);
				if (munger.getMunger() != null && munger.getMunger().getKind() == ResolvedTypeMunger.Parent
						&& ((NewParentTypeMunger) munger.getMunger()).getNewParent().equals(iface) // pr171953
				) {
					shouldSkip = true;
					break;
				}
			}

			// Do not do interfaces more than once
			if (!shouldSkip && !knowninterfaces.contains(iface.getSignature())) {
				knowninterfaces.add(iface.getSignature());
				if (allowMissing && iface.isMissing()) {
					if (iface instanceof MissingResolvedTypeWithKnownSignature) {
						((MissingResolvedTypeWithKnownSignature) iface).raiseWarningOnMissingInterfaceWhilstFindingMethods();
					}
				} else {
					recurseHierarchy(knowninterfaces, collector, iface, includeITDs, allowMissing, genericsAware);
				}
			}
		}
	}

	public ResolvedType[] getResolvedTypeParameters() {
		if (resolvedTypeParams == null) {
			resolvedTypeParams = world.resolve(typeParameters);
		}
		return resolvedTypeParams;
	}

	/**
	 * described in JVM spec 2ed 5.4.3.2
	 */
	public ResolvedMember lookupField(Member field) {
		Iterator<ResolvedMember> i = getFields();
		while (i.hasNext()) {
			ResolvedMember resolvedMember = i.next();
			if (matches(resolvedMember, field)) {
				return resolvedMember;
			}
			if (resolvedMember.hasBackingGenericMember() && field.getName().equals(resolvedMember.getName())) {
				// might be worth checking the member behind the parameterized member (see pr137496)
				if (matches(resolvedMember.getBackingGenericMember(), field)) {
					return resolvedMember;
				}
			}
		}
		return null;
	}

	/**
	 * described in JVM spec 2ed 5.4.3.3. Doesnt check ITDs.
	 * 
	 * <p>
	 * Check the current type for the method. If it is not found, check the super class and any super interfaces. Taking care not to
	 * process interfaces multiple times.
	 */
	public ResolvedMember lookupMethod(Member m) {
		List<ResolvedType> typesTolookat = new ArrayList<>();
		typesTolookat.add(this);
		int pos = 0;
		while (pos < typesTolookat.size()) {
			ResolvedType type = typesTolookat.get(pos++);
			if (!type.isMissing()) {
				ResolvedMember[] methods = type.getDeclaredMethods();
				if (methods != null) {
					for (ResolvedMember method : methods) {
						if (matches(method, m)) {
							return method;
						}
						// might be worth checking the method behind the parameterized method (137496)
						if (method.hasBackingGenericMember() && m.getName().equals(method.getName())) {
							if (matches(method.getBackingGenericMember(), m)) {
								return method;
							}
						}
					}
				}
			}
			// Queue the superclass:
			ResolvedType superclass = type.getSuperclass();
			if (superclass != null) {
				typesTolookat.add(superclass);
			}
			// Queue any interfaces not already checked:
			ResolvedType[] superinterfaces = type.getDeclaredInterfaces();
			if (superinterfaces != null) {
				for (ResolvedType interf : superinterfaces) {
					if (!typesTolookat.contains(interf)) {
						typesTolookat.add(interf);
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param member the member to lookup in intertype declarations affecting this type
	 * @return the real signature defined by any matching intertype declaration, otherwise null
	 */
	public ResolvedMember lookupMethodInITDs(Member member) {
		for (ConcreteTypeMunger typeTransformer : interTypeMungers) {
			if (matches(typeTransformer.getSignature(), member)) {
				return typeTransformer.getSignature();
			}
		}
		return null;
	}

	/**
	 * return null if not found
	 */
	private ResolvedMember lookupMember(Member m, ResolvedMember[] a) {
		for (ResolvedMember f : a) {
			if (matches(f, m)) {
				return f;
			}
		}
		return null;
	}

	// Bug (1) Do callers expect ITDs to be involved in the lookup? or do they do their own walk over ITDs?
	/**
	 * Looks for the first member in the hierarchy matching aMember. This method differs from lookupMember(Member) in that it takes
	 * into account parameters which are type variables - which clearly an unresolved Member cannot do since it does not know
	 * anything about type variables.
	 */
	public ResolvedMember lookupResolvedMember(ResolvedMember aMember, boolean allowMissing, boolean eraseGenerics) {
		Iterator<ResolvedMember> toSearch = null;
		ResolvedMember found = null;
		if ((aMember.getKind() == Member.METHOD) || (aMember.getKind() == Member.CONSTRUCTOR)) {
			// toSearch = getMethodsWithoutIterator(true, allowMissing, !eraseGenerics).iterator();
			toSearch = getMethodsIncludingIntertypeDeclarations(!eraseGenerics, true);
		} else if (aMember.getKind()==Member.ADVICE) {
			return null;
		} else { 
			assert aMember.getKind() == Member.FIELD;
			toSearch = getFields();
		}
		while (toSearch.hasNext()) {
			ResolvedMember candidate = toSearch.next();
			if (eraseGenerics) {
				if (candidate.hasBackingGenericMember()) {
					candidate = candidate.getBackingGenericMember();
				}
			}
			// OPTIMIZE speed up matches? optimize order of checks
			if (candidate.matches(aMember, eraseGenerics)) {
				found = candidate;
				break;
			}
		}

		return found;
	}

	public static boolean matches(Member m1, Member m2) {
		if (m1 == null) {
			return m2 == null;
		}
		if (m2 == null) {
			return false;
		}

		// Check the names
		boolean equalNames = m1.getName().equals(m2.getName());
		if (!equalNames) {
			return false;
		}

		// Check the signatures
		boolean equalSignatures = m1.getSignature().equals(m2.getSignature());
		if (equalSignatures) {
			return true;
		}

		// If they aren't the same, we need to allow for covariance ... where
		// one sig might be ()LCar; and
		// the subsig might be ()LFastCar; - where FastCar is a subclass of Car
		boolean equalCovariantSignatures = m1.getParameterSignature().equals(m2.getParameterSignature());
		if (equalCovariantSignatures) {
			return true;
		}

		return false;
	}
	public static boolean conflictingSignature(Member m1, Member m2) {
		return conflictingSignature(m1,m2,true);
	}

	/**
	 * Do the two members conflict?  Due to the change in 1.7.1, field itds on interfaces now act like 'default' fields - so types implementing
	 * those fields get the field if they don't have it already, otherwise they keep what they have.  The conflict detection below had to be
	 * altered.  Previously (&lt;1.7.1) it is not a conflict if the declaring types are different.  With v2itds it may still be a conflict if the
	 * declaring types are different.
	 */
	public static boolean conflictingSignature(Member m1, Member m2, boolean v2itds) {
		if (m1 == null || m2 == null) {
			return false;
		}
		if (!m1.getName().equals(m2.getName())) {
			return false;
		}
		if (m1.getKind() != m2.getKind()) {
			return false;
		}
		if (m1.getKind() == Member.FIELD) {
			if (v2itds) {
				if (m1.getDeclaringType().equals(m2.getDeclaringType())) {
					return true;
				}
			} else {
				return m1.getDeclaringType().equals(m2.getDeclaringType());
			}
		} else if (m1.getKind() == Member.POINTCUT) {
			return true;
		}

		UnresolvedType[] p1 = m1.getGenericParameterTypes();
		UnresolvedType[] p2 = m2.getGenericParameterTypes();
		if (p1 == null) {
			p1 = m1.getParameterTypes();
		}
		if (p2 == null) {
			p2 = m2.getParameterTypes();
		}
		int n = p1.length;
		if (n != p2.length) {
			return false;
		}

		for (int i = 0; i < n; i++) {
			if (!p1[i].equals(p2[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * returns an iterator through all of the pointcuts of this type, in order for checking from JVM spec 2ed 5.4.3.2 (as for
	 * fields). This means that the order is
	 * <ul>
	 * <li>pointcuts from current class</li>
	 * <li>recur into direct superinterfaces</li>
	 * <li>recur into superclass</li>
	 * </ul>
	 * <p>
	 * We keep a hashSet of interfaces that we've visited so we don't spiral out into 2^n land.
	 * </p>
	 */
	public Iterator<ResolvedMember> getPointcuts() {
		final Iterators.Filter<ResolvedType> dupFilter = Iterators.dupFilter();
		// same order as fields
		Iterators.Getter<ResolvedType, ResolvedType> typeGetter = new Iterators.Getter<ResolvedType, ResolvedType>() {
			@Override
			public Iterator<ResolvedType> get(ResolvedType o) {
				return dupFilter.filter(o.getDirectSupertypes());
			}
		};
		return Iterators.mapOver(Iterators.recur(this, typeGetter), PointcutGetterInstance);
	}

	public ResolvedPointcutDefinition findPointcut(String name) {
		for (Iterator<ResolvedMember> i = getPointcuts(); i.hasNext();) {
			ResolvedPointcutDefinition f = (ResolvedPointcutDefinition) i.next();
			// the ResolvedPointcutDefinition can be null if there are other problems that prevented its resolution
			if (f != null && name.equals(f.getName())) {
				return f;
			}
		}
		// pr120521
		if (!getOutermostType().equals(this)) {
			ResolvedType outerType = getOutermostType().resolve(world);
			ResolvedPointcutDefinition rpd = outerType.findPointcut(name);
			return rpd;
		}
		return null; // should we throw an exception here?
	}

	// all about collecting CrosscuttingMembers

	// ??? collecting data-structure, shouldn't really be a field
	public CrosscuttingMembers crosscuttingMembers;

	public CrosscuttingMembers collectCrosscuttingMembers(boolean shouldConcretizeIfNeeded) {
		crosscuttingMembers = new CrosscuttingMembers(this, shouldConcretizeIfNeeded);
		if (getPerClause() == null) {
			return crosscuttingMembers;
		}
		crosscuttingMembers.setPerClause(getPerClause());
		crosscuttingMembers.addShadowMungers(collectShadowMungers());
		// GENERICITDFIX
		// crosscuttingMembers.addTypeMungers(collectTypeMungers());
		crosscuttingMembers.addTypeMungers(getTypeMungers());
		// FIXME AV - skip but needed ?? or ??
		// crosscuttingMembers.addLateTypeMungers(getLateTypeMungers());
		crosscuttingMembers.addDeclares(collectDeclares(!this.doesNotExposeShadowMungers()));
		crosscuttingMembers.addPrivilegedAccesses(getPrivilegedAccesses());

		// System.err.println("collected cc members: " + this + ", " +
		// collectDeclares());
		return crosscuttingMembers;
	}

	public final List<Declare> collectDeclares(boolean includeAdviceLike) {
		if (!this.isAspect()) {
			return Collections.emptyList();
		}

		List<Declare> ret = new ArrayList<>();
		// if (this.isAbstract()) {
		// for (Iterator i = getDeclares().iterator(); i.hasNext();) {
		// Declare dec = (Declare) i.next();
		// if (!dec.isAdviceLike()) ret.add(dec);
		// }
		//
		// if (!includeAdviceLike) return ret;

		if (!this.isAbstract()) {
			// ret.addAll(getDeclares());
			final Iterators.Filter<ResolvedType> dupFilter = Iterators.dupFilter();
			Iterators.Getter<ResolvedType, ResolvedType> typeGetter = new Iterators.Getter<ResolvedType, ResolvedType>() {
				@Override
				public Iterator<ResolvedType> get(ResolvedType o) {
					return dupFilter.filter((o).getDirectSupertypes());
				}
			};
			Iterator<ResolvedType> typeIterator = Iterators.recur(this, typeGetter);

			while (typeIterator.hasNext()) {
				ResolvedType ty = typeIterator.next();
				// System.out.println("super: " + ty + ", " + );
				for (Declare dec : ty.getDeclares()) {
					if (dec.isAdviceLike()) {
						if (includeAdviceLike) {
							ret.add(dec);
						}
					} else {
						ret.add(dec);
					}
				}
			}
		}

		return ret;
	}

	private final List<ShadowMunger> collectShadowMungers() {
		if (!this.isAspect() || this.isAbstract() || this.doesNotExposeShadowMungers()) {
			return Collections.emptyList();
		}

		List<ShadowMunger> acc = new ArrayList<>();
		final Iterators.Filter<ResolvedType> dupFilter = Iterators.dupFilter();
		Iterators.Getter<ResolvedType, ResolvedType> typeGetter = new Iterators.Getter<ResolvedType, ResolvedType>() {
			@Override
			public Iterator<ResolvedType> get(ResolvedType o) {
				return dupFilter.filter((o).getDirectSupertypes());
			}
		};
		Iterator<ResolvedType> typeIterator = Iterators.recur(this, typeGetter);

		while (typeIterator.hasNext()) {
			ResolvedType ty = typeIterator.next();
			acc.addAll(ty.getDeclaredShadowMungers());
		}

		return acc;
	}

	public void addParent(ResolvedType newParent) {
		// Nothing to do for anything except a ReferenceType
	}

	protected boolean doesNotExposeShadowMungers() {
		return false;
	}

	public PerClause getPerClause() {
		return null;
	}

	public Collection<Declare> getDeclares() {
		return Collections.emptyList();
	}

	public Collection<ConcreteTypeMunger> getTypeMungers() {
		return Collections.emptyList();
	}

	public Collection<ResolvedMember> getPrivilegedAccesses() {
		return Collections.emptyList();
	}

	// ---- useful things

	public final boolean isInterface() {
		return Modifier.isInterface(getModifiers());
	}

	public final boolean isAbstract() {
		return Modifier.isAbstract(getModifiers());
	}

	public boolean isClass() {
		return false;
	}

	public boolean isAspect() {
		return false;
	}

	public boolean isAnnotationStyleAspect() {
		return false;
	}

	/**
	 * Note: Only overridden by Name subtype.
	 */
	public boolean isEnum() {
		return false;
	}

	/**
	 * Note: Only overridden by Name subtype.
	 */
	public boolean isAnnotation() {
		return false;
	}

	public boolean isAnonymous() {
		return false;
	}

	public boolean isNested() {
		return false;
	}

	public ResolvedType getOuterClass() {
		return null;
	}

	public void addAnnotation(AnnotationAJ annotationX) {
		throw new RuntimeException("ResolvedType.addAnnotation() should never be called");
	}

	public AnnotationAJ[] getAnnotations() {
		throw new RuntimeException("ResolvedType.getAnnotations() should never be called");
	}
	
	public boolean hasAnnotations() {
		throw new RuntimeException("ResolvedType.getAnnotations() should never be called");
	}


	/**
	 * Note: Only overridden by ReferenceType subtype
	 */
	public boolean canAnnotationTargetType() {
		return false;
	}

	/**
	 * Note: Only overridden by ReferenceType subtype
	 */
	public AnnotationTargetKind[] getAnnotationTargetKinds() {
		return null;
	}

	/**
	 * Note: Only overridden by Name subtype.
	 */
	public boolean isAnnotationWithRuntimeRetention() {
		return false;
	}

	public boolean isSynthetic() {
		return signature.contains("$ajc");
	}

	public final boolean isFinal() {
		return Modifier.isFinal(getModifiers());
	}

	protected Map<String, UnresolvedType> getMemberParameterizationMap() {
		if (!isParameterizedType()) {
			return Collections.emptyMap();
		}
		TypeVariable[] tvs = getGenericType().getTypeVariables();
		Map<String, UnresolvedType> parameterizationMap = new HashMap<>();
		if (tvs.length != typeParameters.length) {
			world.getMessageHandler()
					.handleMessage(
							new Message("Mismatch when building parameterization map. For type '" + this.signature +
									"' expecting "+tvs.length+":["+toString(tvs)+"] type parameters but found "+typeParameters.length+
									":["+toString(typeParameters)+"]", "",
									IMessage.ERROR, getSourceLocation(), null,
									new ISourceLocation[] { getSourceLocation() }));
		} else {
			for (int i = 0; i < tvs.length; i++) {
				parameterizationMap.put(tvs[i].getName(), typeParameters[i]);
			}
		}
		return parameterizationMap;
	}

	private String toString(UnresolvedType[] typeParameters) {
		StringBuilder s = new StringBuilder();
		for (UnresolvedType tv: typeParameters) {
			s.append(tv.getSignature()).append(" ");
		}
		return s.toString().trim();
	}

	private String toString(TypeVariable[] tvs) {
		StringBuilder s = new StringBuilder();
		for (TypeVariable tv: tvs) {
			s.append(tv.getName()).append(" ");
		}
		return s.toString().trim();
	}

	public List<ShadowMunger> getDeclaredAdvice() {
		List<ShadowMunger> l = new ArrayList<>();
		ResolvedMember[] methods = getDeclaredMethods();
		if (isParameterizedType()) {
			methods = getGenericType().getDeclaredMethods();
		}
		Map<String, UnresolvedType> typeVariableMap = getAjMemberParameterizationMap();
		for (ResolvedMember method : methods) {
			ShadowMunger munger = method.getAssociatedShadowMunger();
			if (munger != null) {
				if (ajMembersNeedParameterization()) {
					// munger.setPointcut(munger.getPointcut().parameterizeWith(
					// typeVariableMap));
					munger = munger.parameterizeWith(this, typeVariableMap);
					if (munger instanceof Advice) {
						Advice advice = (Advice) munger;
						// update to use the parameterized signature...
						UnresolvedType[] ptypes = method.getGenericParameterTypes();
						UnresolvedType[] newPTypes = new UnresolvedType[ptypes.length];
						for (int j = 0; j < ptypes.length; j++) {
							if (ptypes[j] instanceof TypeVariableReferenceType) {
								TypeVariableReferenceType tvrt = (TypeVariableReferenceType) ptypes[j];
								if (typeVariableMap.containsKey(tvrt.getTypeVariable().getName())) {
									newPTypes[j] = typeVariableMap.get(tvrt.getTypeVariable().getName());
								} else {
									newPTypes[j] = ptypes[j];
								}
							} else {
								newPTypes[j] = ptypes[j];
							}
						}
						advice.setBindingParameterTypes(newPTypes);
					}
				}
				munger.setDeclaringType(this);
				l.add(munger);
			}
		}
		return l;
	}

	public List<ShadowMunger> getDeclaredShadowMungers() {
		return getDeclaredAdvice();
	}

	// ---- only for testing!

	public ResolvedMember[] getDeclaredJavaFields() {
		return filterInJavaVisible(getDeclaredFields());
	}

	public ResolvedMember[] getDeclaredJavaMethods() {
		return filterInJavaVisible(getDeclaredMethods());
	}

	private ResolvedMember[] filterInJavaVisible(ResolvedMember[] ms) {
		List<ResolvedMember> l = new ArrayList<>();
		for (ResolvedMember m : ms) {
			if (!m.isAjSynthetic() && m.getAssociatedShadowMunger() == null) {
				l.add(m);
			}
		}
		return l.toArray(new ResolvedMember[0]);
	}

	public abstract ISourceContext getSourceContext();

	// ---- fields

	public static final ResolvedType[] NONE = new ResolvedType[0];
	public static final ResolvedType[] EMPTY_ARRAY = NONE;

	public static final Missing MISSING = new Missing();

	// ---- types
	public static ResolvedType makeArray(ResolvedType type, int dim) {
		if (dim == 0) {
			return type;
		}
		ResolvedType array = new ArrayReferenceType("[" + type.getSignature(), "[" + type.getErasureSignature(), type.getWorld(),
				type);
		return makeArray(array, dim - 1);
	}

	static class Primitive extends ResolvedType {
		private final int size;
		private final int index;

		Primitive(String signature, int size, int index) {
			super(signature, null);
			this.size = size;
			this.index = index;
			this.typeKind = TypeKind.PRIMITIVE;
		}

		@Override
		public final int getSize() {
			return size;
		}
		
		@Override
		public final int getModifiers() {
			return Modifier.PUBLIC | Modifier.FINAL;
		}

		@Override
		public final boolean isPrimitiveType() {
			return true;
		}

		@Override
		public boolean hasAnnotation(UnresolvedType ofType) {
			return false;
		}

		@Override
		public final boolean isAssignableFrom(ResolvedType other) {
			if (!other.isPrimitiveType()) {
				if (!world.isInJava5Mode()) {
					return false;
				}
				return validBoxing.contains(this.getSignature() + other.getSignature());
			}
			return assignTable[((Primitive) other).index][index];
		}

		@Override
		public final boolean isAssignableFrom(ResolvedType other, boolean allowMissing) {
			return isAssignableFrom(other);
		}

		@Override
		public final boolean isCoerceableFrom(ResolvedType other) {
			if (this == other) {
				return true;
			}
			if (!other.isPrimitiveType()) {
				return false;
			}
			if (index > 6 || ((Primitive) other).index > 6) {
				return false;
			}
			return true;
		}

		@Override
		public ResolvedType resolve(World world) {
			if (this.world != world) {
				throw new IllegalStateException();
			}
			this.world = world;
			return super.resolve(world);
		}

		@Override
		public final boolean needsNoConversionFrom(ResolvedType other) {
			if (!other.isPrimitiveType()) {
				return false;
			}
			return noConvertTable[((Primitive) other).index][index];
		}

		private static final boolean[][] assignTable = {// to: B C D F I J S V Z
		// from
				{ true, true, true, true, true, true, true, false, false }, // B
				{ false, true, true, true, true, true, false, false, false }, // C
				{ false, false, true, false, false, false, false, false, false }, // D
				{ false, false, true, true, false, false, false, false, false }, // F
				{ false, false, true, true, true, true, false, false, false }, // I
				{ false, false, true, true, false, true, false, false, false }, // J
				{ false, false, true, true, true, true, true, false, false }, // S
				{ false, false, false, false, false, false, false, true, false }, // V
				{ false, false, false, false, false, false, false, false, true }, // Z
		};
		private static final boolean[][] noConvertTable = {// to: B C D F I J S
		// V Z from
				{ true, true, false, false, true, false, true, false, false }, // B
				{ false, true, false, false, true, false, false, false, false }, // C
				{ false, false, true, false, false, false, false, false, false }, // D
				{ false, false, false, true, false, false, false, false, false }, // F
				{ false, false, false, false, true, false, false, false, false }, // I
				{ false, false, false, false, false, true, false, false, false }, // J
				{ false, false, false, false, true, false, true, false, false }, // S
				{ false, false, false, false, false, false, false, true, false }, // V
				{ false, false, false, false, false, false, false, false, true }, // Z
		};

		// ----

		@Override
		public final ResolvedMember[] getDeclaredFields() {
			return ResolvedMember.NONE;
		}

		@Override
		public final ResolvedMember[] getDeclaredMethods() {
			return ResolvedMember.NONE;
		}

		@Override
		public final ResolvedType[] getDeclaredInterfaces() {
			return ResolvedType.NONE;
		}

		@Override
		public final ResolvedMember[] getDeclaredPointcuts() {
			return ResolvedMember.NONE;
		}

		@Override
		public final ResolvedType getSuperclass() {
			return null;
		}

		@Override
		public ISourceContext getSourceContext() {
			return null;
		}

	}

	static class Missing extends ResolvedType {
		Missing() {
			super(MISSING_NAME, null);
		}

		// public final String toString() {
		// return "<missing>";
		// }
		@Override
		public final String getName() {
			return MISSING_NAME;
		}

		@Override
		public final boolean isMissing() {
			return true;
		}

		@Override
		public boolean hasAnnotation(UnresolvedType ofType) {
			return false;
		}

		@Override
		public final ResolvedMember[] getDeclaredFields() {
			return ResolvedMember.NONE;
		}

		@Override
		public final ResolvedMember[] getDeclaredMethods() {
			return ResolvedMember.NONE;
		}

		@Override
		public final ResolvedType[] getDeclaredInterfaces() {
			return ResolvedType.NONE;
		}

		@Override
		public final ResolvedMember[] getDeclaredPointcuts() {
			return ResolvedMember.NONE;
		}

		@Override
		public final ResolvedType getSuperclass() {
			return null;
		}

		@Override
		public final int getModifiers() {
			return 0;
		}

		@Override
		public final boolean isAssignableFrom(ResolvedType other) {
			return false;
		}

		@Override
		public final boolean isAssignableFrom(ResolvedType other, boolean allowMissing) {
			return false;
		}

		@Override
		public final boolean isCoerceableFrom(ResolvedType other) {
			return false;
		}

		@Override
		public boolean needsNoConversionFrom(ResolvedType other) {
			return false;
		}

		@Override
		public ISourceContext getSourceContext() {
			return null;
		}

	}

	/**
	 * Look up a member, takes into account any ITDs on this type. return null if not found
	 */
	public ResolvedMember lookupMemberNoSupers(Member member) {
		ResolvedMember ret = lookupDirectlyDeclaredMemberNoSupers(member);
		if (ret == null && interTypeMungers != null) {
			for (ConcreteTypeMunger tm : interTypeMungers) {
				if (matches(tm.getSignature(), member)) {
					return tm.getSignature();
				}
			}
		}
		return ret;
	}

	public ResolvedMember lookupMemberWithSupersAndITDs(Member member) {
		ResolvedMember ret = lookupMemberNoSupers(member);
		if (ret != null) {
			return ret;
		}

		ResolvedType supert = getSuperclass();
		while (ret == null && supert != null) {
			ret = supert.lookupMemberNoSupers(member);
			if (ret == null) {
				supert = supert.getSuperclass();
			}
		}

		return ret;
	}

	/**
	 * as lookupMemberNoSupers, but does not include ITDs
	 * 
	 * @param member
	 * @return
	 */
	public ResolvedMember lookupDirectlyDeclaredMemberNoSupers(Member member) {
		ResolvedMember ret;
		if (member.getKind() == Member.FIELD) {
			ret = lookupMember(member, getDeclaredFields());
		} else {
			// assert member.getKind() == Member.METHOD || member.getKind() ==
			// Member.CONSTRUCTOR
			ret = lookupMember(member, getDeclaredMethods());
		}
		return ret;
	}

	/**
	 * This lookup has specialized behaviour - a null result tells the EclipseTypeMunger that it should make a default
	 * implementation of a method on this type.
	 * 
	 * @param member
	 * @return
	 */
	public ResolvedMember lookupMemberIncludingITDsOnInterfaces(Member member) {
		return lookupMemberIncludingITDsOnInterfaces(member, this);
	}

	private ResolvedMember lookupMemberIncludingITDsOnInterfaces(Member member, ResolvedType onType) {
		ResolvedMember ret = onType.lookupMemberNoSupers(member);
		if (ret != null) {
			return ret;
		} else {
			ResolvedType superType = onType.getSuperclass();
			if (superType != null) {
				ret = lookupMemberIncludingITDsOnInterfaces(member, superType);
			}
			if (ret == null) {
				// try interfaces then, but only ITDs now...
				ResolvedType[] superInterfaces = onType.getDeclaredInterfaces();
				for (ResolvedType superInterface : superInterfaces) {
					ret = superInterface.lookupMethodInITDs(member);
					if (ret != null) {
						return ret;
					}
				}
			}
		}
		return ret;
	}

	protected List<ConcreteTypeMunger> interTypeMungers = new ArrayList<>();

	public List<ConcreteTypeMunger> getInterTypeMungers() {
		return interTypeMungers;
	}

	public List<ConcreteTypeMunger> getInterTypeParentMungers() {
		List<ConcreteTypeMunger> l = new ArrayList<>();
		for (ConcreteTypeMunger element : interTypeMungers) {
			if (element.getMunger() instanceof NewParentTypeMunger) {
				l.add(element);
			}
		}
		return l;
	}

	/**
	 * ??? This method is O(N*M) where N = number of methods and M is number of inter-type declarations in my super
	 */
	public List<ConcreteTypeMunger> getInterTypeMungersIncludingSupers() {
		List<ConcreteTypeMunger> ret = new ArrayList<>();
		collectInterTypeMungers(ret);
		return ret;
	}

	public List<ConcreteTypeMunger> getInterTypeParentMungersIncludingSupers() {
		List<ConcreteTypeMunger> ret = new ArrayList<>();
		collectInterTypeParentMungers(ret);
		return ret;
	}

	private void collectInterTypeParentMungers(List<ConcreteTypeMunger> collector) {
		for (Iterator<ResolvedType> iter = getDirectSupertypes(); iter.hasNext();) {
			ResolvedType superType = iter.next();
			superType.collectInterTypeParentMungers(collector);
		}
		collector.addAll(getInterTypeParentMungers());
	}

	protected void collectInterTypeMungers(List<ConcreteTypeMunger> collector) {
		for (Iterator<ResolvedType> iter = getDirectSupertypes(); iter.hasNext();) {
			ResolvedType superType = iter.next();
			if (superType == null) {
				throw new BCException("UnexpectedProblem: a supertype in the hierarchy for " + this.getName() + " is null");
			}
			superType.collectInterTypeMungers(collector);
		}

		outer: for (Iterator<ConcreteTypeMunger> iter1 = collector.iterator(); iter1.hasNext();) {
			ConcreteTypeMunger superMunger = iter1.next();
			if (superMunger.getSignature() == null) {
				continue;
			}

			if (!superMunger.getSignature().isAbstract()) {
				continue;
			}

			for (ConcreteTypeMunger myMunger : getInterTypeMungers()) {
				if (conflictingSignature(myMunger.getSignature(), superMunger.getSignature())) {
					iter1.remove();
					continue outer;
				}
			}

			if (!superMunger.getSignature().isPublic()) {
				continue;
			}

			for (Iterator<ResolvedMember> iter = getMethods(true, true); iter.hasNext();) {
				ResolvedMember method = iter.next();
				if (conflictingSignature(method, superMunger.getSignature())) {
					iter1.remove();
					continue outer;
				}
			}
		}

		collector.addAll(getInterTypeMungers());
	}

	/**
	 * Check: 1) That we don't have any abstract type mungers unless this type is abstract. 2) That an abstract ITDM on an interface
	 * is declared public. (Compiler limitation) (PR70794)
	 */
	public void checkInterTypeMungers() {
		if (isAbstract()) {
			return;
		}

		boolean itdProblem = false;

		for (ConcreteTypeMunger munger : getInterTypeMungersIncludingSupers()) {
			itdProblem = checkAbstractDeclaration(munger) || itdProblem; // Rule 2
		}

		if (itdProblem) {
			return; // If the rules above are broken, return right now
		}

		for (ConcreteTypeMunger munger : getInterTypeMungersIncludingSupers()) {
			if (munger.getSignature() != null && munger.getSignature().isAbstract() && munger.getMunger().getKind()!=ResolvedTypeMunger.PrivilegedAccess) { // Rule 1
				if (munger.getMunger().getKind() == ResolvedTypeMunger.MethodDelegate2) {
					// ignore for @AJ ITD as munger.getSignature() is the
					// interface method hence abstract
				} else {
					world.getMessageHandler()
							.handleMessage(
									new Message("must implement abstract inter-type declaration: " + munger.getSignature(), "",
											IMessage.ERROR, getSourceLocation(), null,
											new ISourceLocation[] { getMungerLocation(munger) }));
				}
			}
		}
	}

	/**
	 * See PR70794. This method checks that if an abstract inter-type method declaration is made on an interface then it must also
	 * be public. This is a compiler limitation that could be made to work in the future (if someone provides a worthwhile usecase)
	 * 
	 * @return indicates if the munger failed the check
	 */
	private boolean checkAbstractDeclaration(ConcreteTypeMunger munger) {
		if (munger.getMunger() != null && (munger.getMunger() instanceof NewMethodTypeMunger)) {
			ResolvedMember itdMember = munger.getSignature();
			ResolvedType onType = itdMember.getDeclaringType().resolve(world);
			if (onType.isInterface() && itdMember.isAbstract() && !itdMember.isPublic()) {
				world.getMessageHandler().handleMessage(
						new Message(WeaverMessages.format(WeaverMessages.ITD_ABSTRACT_MUST_BE_PUBLIC_ON_INTERFACE,
								munger.getSignature(), onType), "", Message.ERROR, getSourceLocation(), null,
								new ISourceLocation[] { getMungerLocation(munger) }));
				return true;
			}
		}
		return false;
	}

	/**
	 * Get a source location for the munger. Until intertype mungers remember where they came from, the source location for the
	 * munger itself is null. In these cases use the source location for the aspect containing the ITD.
	 */
	private ISourceLocation getMungerLocation(ConcreteTypeMunger munger) {
		ISourceLocation sloc = munger.getSourceLocation();
		if (sloc == null) {
			sloc = munger.getAspectType().getSourceLocation();
		}
		return sloc;
	}

	/**
	 * Returns a ResolvedType object representing the declaring type of this type, or null if this type does not represent a
	 * non-package-level-type.
	 * <p>
	 * <strong>Warning</strong>: This is guaranteed to work for all member types. For anonymous/local types, the only guarantee is
	 * given in JLS 13.1, where it guarantees that if you call getDeclaringType() repeatedly, you will eventually get the top-level
	 * class, but it does not say anything about classes in between.
	 * </p>
	 * 
	 * @return the declaring type, or null if it is not an nested type.
	 */
	public ResolvedType getDeclaringType() {
		if (isArray()) {
			return null;
		}
		if (isNested() || isAnonymous()) {
			return getOuterClass();
		}
		return null;
	}

	public static boolean isVisible(int modifiers, ResolvedType targetType, ResolvedType fromType) {
		// System.err.println("mod: " + modifiers + ", " + targetType + " and "
		// + fromType);

		if (Modifier.isPublic(modifiers)) {
			return true;
		} else if (Modifier.isPrivate(modifiers)) {
			return targetType.getOutermostType().equals(fromType.getOutermostType());
		} else if (Modifier.isProtected(modifiers)) {
			return samePackage(targetType, fromType) || targetType.isAssignableFrom(fromType);
		} else { // package-visible
			return samePackage(targetType, fromType);
		}
	}

	private static boolean samePackage(ResolvedType targetType, ResolvedType fromType) {
		String p1 = targetType.getPackageName();
		String p2 = fromType.getPackageName();
		if (p1 == null) {
			return p2 == null;
		}
		if (p2 == null) {
			return false;
		}
		return p1.equals(p2);
	}

	/**
	 * Checks if the generic type for 'this' and the generic type for 'other' are the same - it can be passed raw or parameterized
	 * versions and will just compare the underlying generic type.
	 */
	private boolean genericTypeEquals(ResolvedType other) {
		ResolvedType rt = other;
		if (rt.isParameterizedType() || rt.isRawType()) {
			rt.getGenericType();
		}
		if (((isParameterizedType() || isRawType()) && getGenericType().equals(rt)) || (this.equals(other))) {
			return true;
		}
		return false;
	}

	/**
	 * Look up the actual occurence of a particular type in the hierarchy for 'this' type. The input is going to be a generic type,
	 * and the caller wants to know if it was used in its RAW or a PARAMETERIZED form in this hierarchy.
	 * 
	 * returns null if it can't be found.
	 */
	public ResolvedType discoverActualOccurrenceOfTypeInHierarchy(ResolvedType lookingFor) {
		if (!lookingFor.isGenericType()) {
			throw new BCException("assertion failed: method should only be called with generic type, but " + lookingFor + " is "
					+ lookingFor.typeKind);
		}

		if (this.equals(ResolvedType.OBJECT)) {
			return null;
		}

		if (genericTypeEquals(lookingFor)) {
			return this;
		}

		ResolvedType superT = getSuperclass();
		if (superT.genericTypeEquals(lookingFor)) {
			return superT;
		}

		ResolvedType[] superIs = getDeclaredInterfaces();
		for (ResolvedType superI : superIs) {
			if (superI.genericTypeEquals(lookingFor)) {
				return superI;
			}
			ResolvedType checkTheSuperI = superI.discoverActualOccurrenceOfTypeInHierarchy(lookingFor);
			if (checkTheSuperI != null) {
				return checkTheSuperI;
			}
		}
		return superT.discoverActualOccurrenceOfTypeInHierarchy(lookingFor);
	}

	/**
	 * Called for all type mungers but only does something if they share type variables with a generic type which they target. When
	 * this happens this routine will check for the target type in the target hierarchy and 'bind' any type parameters as
	 * appropriate. For example, for the ITD "List&lt;T&gt; I&lt;T&gt;.x" against a type like this: "class A implements I&lt;String&gt;" this routine
	 * will return a parameterized form of the ITD "List&lt;String&gt; I.x"
	 */
	public ConcreteTypeMunger fillInAnyTypeParameters(ConcreteTypeMunger munger) {
		boolean debug = false;
		ResolvedMember member = munger.getSignature();
		if (munger.isTargetTypeParameterized()) {
			if (debug) {
				System.err.println("Processing attempted parameterization of " + munger + " targetting type " + this);
			}
			if (debug) {
				System.err.println("  This type is " + this + "  (" + typeKind + ")");
			}
			// need to tailor this munger instance for the particular target...
			if (debug) {
				System.err.println("  Signature that needs parameterizing: " + member);
			}
			// Retrieve the generic type
			ResolvedType onTypeResolved = world.resolve(member.getDeclaringType());
			ResolvedType onType = onTypeResolved.getGenericType();
			if (onType == null) {
				// The target is not generic
				getWorld().getMessageHandler().handleMessage(
						MessageUtil.error("The target type for the intertype declaration is not generic",
								munger.getSourceLocation()));
				return munger;
			}
			member.resolve(world); // Ensure all parts of the member are resolved
			if (debug) {
				System.err.println("  Actual target ontype: " + onType + "  (" + onType.typeKind + ")");
			}
			// quickly find the targettype in the type hierarchy for this type
			// (it will be either RAW or PARAMETERIZED)
			ResolvedType actualTarget = discoverActualOccurrenceOfTypeInHierarchy(onType);
			if (actualTarget == null) {
				throw new BCException("assertion failed: asked " + this + " for occurrence of " + onType + " in its hierarchy??");
			}

			// only bind the tvars if its a parameterized type or the raw type
			// (in which case they collapse to bounds) - don't do it
			// for generic types ;)
			if (!actualTarget.isGenericType()) {
				if (debug) {
					System.err.println("Occurrence in " + this + " is actually " + actualTarget + "  (" + actualTarget.typeKind
							+ ")");
					// parameterize the signature
					// ResolvedMember newOne =
					// member.parameterizedWith(actualTarget.getTypeParameters(),
					// onType,actualTarget.isParameterizedType());
				}
			}
			// if (!actualTarget.isRawType())
			munger = munger.parameterizedFor(actualTarget);
			if (debug) {
				System.err.println("New sig: " + munger.getSignature());
			}

			if (debug) {
				System.err.println("=====================================");
			}
		}
		return munger;
	}

	/**
	 * Add an intertype munger to this type. isDuringCompilation tells us if we should be checking for an error scenario where two
	 * ITD fields are trying to use the same name. When this happens during compilation one of them is altered to get mangled name
	 * but when it happens during weaving it is too late and we need to put out an error asking them to recompile.
	 */
	public void addInterTypeMunger(ConcreteTypeMunger munger, boolean isDuringCompilation) {
		ResolvedMember sig = munger.getSignature();
		bits = (bits & ~MungersAnalyzed); // clear the bit - as the mungers have changed
		if (sig == null || munger.getMunger() == null || munger.getMunger().getKind() == ResolvedTypeMunger.PrivilegedAccess) {
			interTypeMungers.add(munger);
			return;
		}

		// ConcreteTypeMunger originalMunger = munger;
		// we will use the 'parameterized' ITD for all the comparisons but we
		// say the original
		// one passed in actually matched as it will be added to the intertype
		// member finder
		// for the target type. It is possible we only want to do this if a
		// generic type
		// is discovered and the tvar is collapsed to a bound?
		munger = fillInAnyTypeParameters(munger);
		sig = munger.getSignature(); // possibly changed when type parms filled in

		if (sig.getKind() == Member.METHOD) {
			// OPTIMIZE can this be sped up?
			if (clashesWithExistingMember(munger, getMethods(true, false))) { // ITDs checked below
				return;
			}
			if (this.isInterface()) {
				// OPTIMIZE this set of methods are always the same - must we keep creating them as a list?
				if (clashesWithExistingMember(munger, Arrays.asList(world.getCoreType(OBJECT).getDeclaredMethods()).iterator())) {
					return;
				}
			}
		} else if (sig.getKind() == Member.FIELD) {
			if (clashesWithExistingMember(munger, Arrays.asList(getDeclaredFields()).iterator())) {
				return;
			}
			// Cannot cope with two version '2' style mungers for the same field on the same type
			// Must error and request the user recompile at least one aspect with the
			// -Xset:itdStyle=1 option
			if (!isDuringCompilation) {
				ResolvedTypeMunger thisRealMunger = munger.getMunger();
				if (thisRealMunger instanceof NewFieldTypeMunger) {
					NewFieldTypeMunger newFieldTypeMunger = (NewFieldTypeMunger) thisRealMunger;
					if (newFieldTypeMunger.version == NewFieldTypeMunger.VersionTwo) {
						String thisRealMungerSignatureName = newFieldTypeMunger.getSignature().getName();
						for (ConcreteTypeMunger typeMunger : interTypeMungers) {
							if (typeMunger.getMunger() instanceof NewFieldTypeMunger) {
								if (typeMunger.getSignature().getKind() == Member.FIELD) {
									NewFieldTypeMunger existing = (NewFieldTypeMunger) typeMunger.getMunger();
									if (existing.getSignature().getName().equals(thisRealMungerSignatureName)
											&& existing.version == NewFieldTypeMunger.VersionTwo
											// this check ensures no problem for a clash with an ITD on an interface
											&& existing.getSignature().getDeclaringType()
													.equals(newFieldTypeMunger.getSignature().getDeclaringType())) {

										// report error on the aspect
										StringBuffer sb = new StringBuffer();
										sb.append("Cannot handle two aspects both attempting to use new style ITDs for the same named field ");
										sb.append("on the same target type.  Please recompile at least one aspect with '-Xset:itdVersion=1'.");
										sb.append(" Aspects involved: " + munger.getAspectType().getName() + " and "
												+ typeMunger.getAspectType().getName() + ".");
										sb.append(" Field is named '" + existing.getSignature().getName() + "'");
										getWorld().getMessageHandler().handleMessage(
												new Message(sb.toString(), getSourceLocation(), true));
										return;
									}
								}
							}
						}
					}
				}
			}
		} else {
			if (clashesWithExistingMember(munger, Arrays.asList(getDeclaredMethods()).iterator())) {
				return;
			}
		}

		boolean needsAdding =true;
		boolean needsToBeAddedEarlier =false;
		// now compare to existingMungers
		for (Iterator<ConcreteTypeMunger> i = interTypeMungers.iterator(); i.hasNext();) {
			ConcreteTypeMunger existingMunger = i.next();
			boolean v2itds = munger.getSignature().getKind()== Member.FIELD && (munger.getMunger() instanceof NewFieldTypeMunger) && ((NewFieldTypeMunger)munger.getMunger()).version==NewFieldTypeMunger.VersionTwo;

			if (conflictingSignature(existingMunger.getSignature(), munger.getSignature(),v2itds)) {
				// System.err.println("match " + munger + " with " + existingMunger);
				if (isVisible(munger.getSignature().getModifiers(), munger.getAspectType(), existingMunger.getAspectType())) {
					// System.err.println("    is visible");
					int c = compareMemberPrecedence(sig, existingMunger.getSignature());
					if (c == 0) {
						c = getWorld().compareByPrecedenceAndHierarchy(munger.getAspectType(), existingMunger.getAspectType());
					}
					// System.err.println("       compare: " + c);
					if (c < 0) {
						// the existing munger dominates the new munger
						checkLegalOverride(munger.getSignature(), existingMunger.getSignature(), 0x11, null);
						needsAdding = false;
						if (munger.getSignature().getKind()== Member.FIELD && munger.getSignature().getDeclaringType().resolve(world).isInterface() && ((NewFieldTypeMunger)munger.getMunger()).version==NewFieldTypeMunger.VersionTwo) {
							// still need to add it
							needsAdding=true;
						}
						break;
					} else if (c > 0) {
						// the new munger dominates the existing one
						checkLegalOverride(existingMunger.getSignature(), munger.getSignature(), 0x11, null);
//						i.remove();
						if (existingMunger.getSignature().getKind()==Member.FIELD &&
								existingMunger.getSignature().getDeclaringType().resolve(world).isInterface()
								&& ((NewFieldTypeMunger)existingMunger.getMunger()).version==NewFieldTypeMunger.VersionTwo) {
							needsToBeAddedEarlier=true;
						} else {
							i.remove();
						}
						break;
					} else {
						interTypeConflictError(munger, existingMunger);
						interTypeConflictError(existingMunger, munger);
						return;
					}
				}
			}
		}
		// System.err.println("adding: " + munger + " to " + this);
		// we are adding the parameterized form of the ITD to the list of
		// mungers. Within it, the munger knows the original declared
		// signature for the ITD so it can be retrieved.
		if (needsAdding) {
			if (!needsToBeAddedEarlier) {
				interTypeMungers.add(munger);
			} else {
				interTypeMungers.add(0,munger);
			}
		}
	}

	/**
	 * Compare the type transformer with the existing members. A clash may not be an error (the ITD may be the 'default
	 * implementation') so returning false is not always a sign of an error.
	 * 
	 * @return true if there is a clash
	 */
	private boolean clashesWithExistingMember(ConcreteTypeMunger typeTransformer, Iterator<ResolvedMember> existingMembers) {
		ResolvedMember typeTransformerSignature = typeTransformer.getSignature();

		// ResolvedType declaringAspectType = munger.getAspectType();
		// if (declaringAspectType.isRawType()) declaringAspectType =
		// declaringAspectType.getGenericType();
		// if (declaringAspectType.isGenericType()) {
		//
		// ResolvedType genericOnType =
		// getWorld().resolve(sig.getDeclaringType()).getGenericType();
		// ConcreteTypeMunger ctm =
		// munger.parameterizedFor(discoverActualOccurrenceOfTypeInHierarchy
		// (genericOnType));
		// sig = ctm.getSignature(); // possible sig change when type
		// }
		// if (munger.getMunger().hasTypeVariableAliases()) {
		// ResolvedType genericOnType =
		// getWorld().resolve(sig.getDeclaringType()).getGenericType();
		// ConcreteTypeMunger ctm =
		// munger.parameterizedFor(discoverActualOccurrenceOfTypeInHierarchy(
		// genericOnType));
		// sig = ctm.getSignature(); // possible sig change when type parameters
		// filled in
		// }
		ResolvedTypeMunger rtm = typeTransformer.getMunger();
		boolean v2itds = true;
		if (rtm instanceof NewFieldTypeMunger && ((NewFieldTypeMunger)rtm).version==NewFieldTypeMunger.VersionOne) {
			v2itds = false;
		}
		while (existingMembers.hasNext()) {
			ResolvedMember existingMember = existingMembers.next();
			// don't worry about clashing with bridge methods
			if (existingMember.isBridgeMethod()) {
				continue;
			}
			if (conflictingSignature(existingMember, typeTransformerSignature,v2itds)) {
				// System.err.println("conflict: existingMember=" +
				// existingMember + "   typeMunger=" + munger);
				// System.err.println(munger.getSourceLocation() + ", " +
				// munger.getSignature() + ", " +
				// munger.getSignature().getSourceLocation());

				if (isVisible(existingMember.getModifiers(), this, typeTransformer.getAspectType())) {
					int c = compareMemberPrecedence(typeTransformerSignature, existingMember);
					// System.err.println("   c: " + c);
					if (c < 0) {
						ResolvedType typeTransformerTargetType = typeTransformerSignature.getDeclaringType().resolve(world);
						if (typeTransformerTargetType.isInterface()) {
							ResolvedType existingMemberType = existingMember.getDeclaringType().resolve(world);
							if ((rtm instanceof NewMethodTypeMunger) && !typeTransformerTargetType.equals(existingMemberType)) {
								// Might be pr404601. ITD is on an interface with a different visibility to the real member
								if (Modifier.isPrivate(typeTransformerSignature.getModifiers()) &&
									Modifier.isPublic(existingMember.getModifiers())) {
									world.getMessageHandler().handleMessage(new Message("private intertype declaration '"+typeTransformerSignature.toString()+"' clashes with public member '"+existingMember.toString()+"'",existingMember.getSourceLocation(),true));
								}	
							}
						}
						// existingMember dominates munger
						checkLegalOverride(typeTransformerSignature, existingMember, 0x10, typeTransformer.getAspectType());
						return true;
					} else if (c > 0) {
						// munger dominates existingMember
						checkLegalOverride(existingMember, typeTransformerSignature, 0x01, typeTransformer.getAspectType());
						// interTypeMungers.add(munger);
						// ??? might need list of these overridden abstracts
						continue;
					} else {
						// bridge methods can differ solely in return type.
						// FIXME this whole method seems very hokey - unaware of covariance/varargs/bridging - it
						// could do with a rewrite !
						boolean sameReturnTypes = (existingMember.getReturnType().equals(typeTransformerSignature.getReturnType()));
						if (sameReturnTypes) {
							// pr206732 - if the existingMember is due to a
							// previous application of this same ITD (which can
							// happen if this is a binary type being brought in
							// from the aspectpath). The 'better' fix is
							// to recognize it is from the aspectpath at a
							// higher level and dont do this, but that is rather
							// more work.
							boolean isDuplicateOfPreviousITD = false;
							ResolvedType declaringRt = existingMember.getDeclaringType().resolve(world);
							WeaverStateInfo wsi = declaringRt.getWeaverState();
							if (wsi != null) {
								List<ConcreteTypeMunger> mungersAffectingThisType = wsi.getTypeMungers(declaringRt);
								if (mungersAffectingThisType != null) {
									for (Iterator<ConcreteTypeMunger> iterator = mungersAffectingThisType.iterator(); iterator
											.hasNext() && !isDuplicateOfPreviousITD;) {
										ConcreteTypeMunger ctMunger = iterator.next();
										// relatively crude check - is the ITD
										// for the same as the existingmember
										// and does it come
										// from the same aspect
										if (ctMunger.getSignature().equals(existingMember)
												&& ctMunger.aspectType.equals(typeTransformer.getAspectType())) {
											isDuplicateOfPreviousITD = true;
										}
									}
								}
							}
							if (!isDuplicateOfPreviousITD) {
								// b275032 - this is OK if it is the default ctor and that default ctor was generated
								// at compile time, otherwise we cannot overwrite it
								if (!(typeTransformerSignature.getName().equals("<init>") && existingMember.isDefaultConstructor())) {
									String aspectName = typeTransformer.getAspectType().getName();
									ISourceLocation typeTransformerLocation = typeTransformer.getSourceLocation();
									ISourceLocation existingMemberLocation = existingMember.getSourceLocation();
									String msg = WeaverMessages.format(WeaverMessages.ITD_MEMBER_CONFLICT, aspectName,
											existingMember);

									// this isn't quite right really... as I think the errors should only be recorded against
									// what is currently being processed or they may get lost or reported twice

									// report error on the aspect
									getWorld().getMessageHandler().handleMessage(new Message(msg, typeTransformerLocation, true));

									// report error on the affected type, if we can
									if (existingMemberLocation != null) {
										getWorld().getMessageHandler()
												.handleMessage(new Message(msg, existingMemberLocation, true));
									}
									return true; // clash - so ignore this itd
								}
							}
						}
					}
				} else if (isDuplicateMemberWithinTargetType(existingMember, this, typeTransformerSignature)) {
					getWorld().getMessageHandler().handleMessage(
							MessageUtil.error(WeaverMessages.format(WeaverMessages.ITD_MEMBER_CONFLICT, typeTransformer
									.getAspectType().getName(), existingMember), typeTransformer.getSourceLocation()));
					return true;
				}
			}
		}
		return false;
	}

	// we know that the member signature matches, but that the member in the
	// target type is not visible to the aspect.
	// this may still be disallowed if it would result in two members within the
	// same declaring type with the same
	// signature AND more than one of them is concrete AND they are both visible
	// within the target type.
	private boolean isDuplicateMemberWithinTargetType(ResolvedMember existingMember, ResolvedType targetType,
			ResolvedMember itdMember) {
		if ((existingMember.isAbstract() || itdMember.isAbstract())) {
			return false;
		}
		UnresolvedType declaringType = existingMember.getDeclaringType();
		if (!targetType.equals(declaringType)) {
			return false;
		}
		// now have to test that itdMember is visible from targetType
		if (Modifier.isPrivate(itdMember.getModifiers())) {
			return false;
		}
		if (itdMember.isPublic()) {
			return true;
		}
		// must be in same package to be visible then...
		if (!targetType.getPackageName().equals(itdMember.getDeclaringType().getPackageName())) {
			return false;
		}

		// trying to put two members with the same signature into the exact same
		// type..., and both visible in that type.
		return true;
	}

	/**
	 * @param transformerPosition which parameter is the type transformer (0x10 for first, 0x01 for second, 0x11 for both, 0x00 for
	 *        neither)
	 * @param aspectType the declaring type of aspect defining the *first* type transformer
	 * @return true if the override is legal note: calling showMessage with two locations issues TWO messages, not ONE message with
	 *         an additional source location.
	 */
	public boolean checkLegalOverride(ResolvedMember parent, ResolvedMember child, int transformerPosition, ResolvedType aspectType) {
		// System.err.println("check: " + child.getDeclaringType() + " overrides " + parent.getDeclaringType());
		if (Modifier.isFinal(parent.getModifiers())) {
			// If the ITD matching is occurring due to pulling in a BinaryTypeBinding then this check can incorrectly
			// signal an error because the ITD transformer being examined here will exactly match the member it added
			// during the first round of compilation. This situation can only occur if the ITD is on an interface whilst
			// the class is the top most implementor. If the ITD is on the same type that received it during compilation,
			// this method won't be called as the previous check for precedence level will return 0.

			if (transformerPosition == 0x10 && aspectType != null) {
				ResolvedType nonItdDeclaringType = child.getDeclaringType().resolve(world);
				WeaverStateInfo wsi = nonItdDeclaringType.getWeaverState();
				if (wsi != null) {
					List<ConcreteTypeMunger> transformersOnThisType = wsi.getTypeMungers(nonItdDeclaringType);
					if (transformersOnThisType != null) {
						for (ConcreteTypeMunger transformer : transformersOnThisType) {
							// relatively crude check - is the ITD for the same as the existingmember
							// and does it come from the same aspect
							if (transformer.aspectType.equals(aspectType)) {
								if (parent.equalsApartFromDeclaringType(transformer.getSignature())) {
									return true;
								}
							}
						}
					}
				}
			}

			world.showMessage(Message.ERROR, WeaverMessages.format(WeaverMessages.CANT_OVERRIDE_FINAL_MEMBER, parent),
					child.getSourceLocation(), null);
			return false;
		}

		boolean incompatibleReturnTypes = false;
		// In 1.5 mode, allow for covariance on return type
		if (world.isInJava5Mode() && parent.getKind() == Member.METHOD) {

			// Look at the generic types when doing this comparison
			ResolvedType rtParentReturnType = parent.resolve(world).getGenericReturnType().resolve(world);
			ResolvedType rtChildReturnType = child.resolve(world).getGenericReturnType().resolve(world);
			incompatibleReturnTypes = !rtParentReturnType.isAssignableFrom(rtChildReturnType);
			// For debug, uncomment this bit and we'll repeat the check - stick
			// a breakpoint on the call
			// if (incompatibleReturnTypes) {
			// incompatibleReturnTypes =
			// !rtParentReturnType.isAssignableFrom(rtChildReturnType);
			// }
		} else {
			ResolvedType rtParentReturnType = parent.resolve(world).getGenericReturnType().resolve(world);
			ResolvedType rtChildReturnType = child.resolve(world).getGenericReturnType().resolve(world);
			
			incompatibleReturnTypes = !rtParentReturnType.equals(rtChildReturnType);
		}

		if (incompatibleReturnTypes) {
			world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.ITD_RETURN_TYPE_MISMATCH, parent, child),
					child.getSourceLocation(), parent.getSourceLocation());
			return false;
		}
		if (parent.getKind() == Member.POINTCUT) {
			UnresolvedType[] pTypes = parent.getParameterTypes();
			UnresolvedType[] cTypes = child.getParameterTypes();
			if (!Arrays.equals(pTypes, cTypes)) {
				world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.ITD_PARAM_TYPE_MISMATCH, parent, child),
						child.getSourceLocation(), parent.getSourceLocation());
				return false;
			}
		}
		// System.err.println("check: " + child.getModifiers() +
		// " more visible " + parent.getModifiers());
		if (isMoreVisible(parent.getModifiers(), child.getModifiers())) {
			world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.ITD_VISIBILITY_REDUCTION, parent, child),
					child.getSourceLocation(), parent.getSourceLocation());
			return false;
		}

		// check declared exceptions
		ResolvedType[] childExceptions = world.resolve(child.getExceptions());
		ResolvedType[] parentExceptions = world.resolve(parent.getExceptions());
		ResolvedType runtimeException = world.resolve("java.lang.RuntimeException");
		ResolvedType error = world.resolve("java.lang.Error");

		outer:
		for (ResolvedType childException : childExceptions) {
			// System.err.println("checking: " + childExceptions[i]);
			if (runtimeException.isAssignableFrom(childException)) {
				continue;
			}
			if (error.isAssignableFrom(childException)) {
				continue;
			}

			for (ResolvedType parentException : parentExceptions) {
				if (parentException.isAssignableFrom(childException)) {
					continue outer;
				}
			}

			// this message is now better handled my MethodVerifier in JDT core.
			// world.showMessage(IMessage.ERROR,
			// WeaverMessages.format(WeaverMessages.ITD_DOESNT_THROW,
			// childExceptions[i].getName()),
			// child.getSourceLocation(), null);

			return false;
		}
		boolean parentStatic = Modifier.isStatic(parent.getModifiers());
		boolean childStatic = Modifier.isStatic(child.getModifiers());
		if (parentStatic && !childStatic) {
			world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.ITD_OVERRIDDEN_STATIC, child, parent),
					child.getSourceLocation(), null);
			return false;
		} else if (childStatic && !parentStatic) {
			world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.ITD_OVERIDDING_STATIC, child, parent),
					child.getSourceLocation(), null);
			return false;
		}
		return true;

	}

	private int compareMemberPrecedence(ResolvedMember m1, ResolvedMember m2) {
		// if (!m1.getReturnType().equals(m2.getReturnType())) return 0;

		// need to allow for the special case of 'clone' - which is like
		// abstract but is
		// not marked abstract. The code below this next line seems to make
		// assumptions
		// about what will have gotten through the compiler based on the normal
		// java rules. clone goes against these...
		if (Modifier.isProtected(m2.getModifiers()) && m2.getName().charAt(0) == 'c') {
			UnresolvedType declaring = m2.getDeclaringType();
			if (declaring != null) {
				if (declaring.getName().equals("java.lang.Object") && m2.getName().equals("clone")) {
					return +1;
				}
			}
		}

		if (Modifier.isAbstract(m1.getModifiers())) {
			return -1;
		}
		if (Modifier.isAbstract(m2.getModifiers())) {
			return +1;
		}

		if (m1.getDeclaringType().equals(m2.getDeclaringType())) {
			return 0;
		}

		ResolvedType t1 = m1.getDeclaringType().resolve(world);
		ResolvedType t2 = m2.getDeclaringType().resolve(world);
		if (t1.isAssignableFrom(t2)) {
			return -1;
		}
		if (t2.isAssignableFrom(t1)) {
			return +1;
		}
		return 0;
	}

	public static boolean isMoreVisible(int m1, int m2) {
		if (Modifier.isPrivate(m1)) {
			return false;
		}
		if (isPackage(m1)) {
			return Modifier.isPrivate(m2);
		}
		if (Modifier.isProtected(m1)) {
			return /* private package */(Modifier.isPrivate(m2) || isPackage(m2));
		}
		if (Modifier.isPublic(m1)) {
			return /* private package protected */!Modifier.isPublic(m2);
		}
		throw new RuntimeException("bad modifier: " + m1);
	}

	private static boolean isPackage(int i) {
		return (0 == (i & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED)));
	}

	private void interTypeConflictError(ConcreteTypeMunger m1, ConcreteTypeMunger m2) {
		// XXX this works only if we ignore separate compilation issues
		// XXX dual errors possible if (this instanceof BcelObjectType) return;
		/*
		 * if (m1.getMunger().getKind() == ResolvedTypeMunger.Field && m2.getMunger().getKind() == ResolvedTypeMunger.Field) { // if
		 * *exactly* the same, it's ok return true; }
		 */
		// System.err.println("conflict at " + m2.getSourceLocation());
		getWorld().showMessage(
				IMessage.ERROR,
				WeaverMessages.format(WeaverMessages.ITD_CONFLICT, m1.getAspectType().getName(), m2.getSignature(), m2
						.getAspectType().getName()), m2.getSourceLocation(), getSourceLocation());
		// return false;
	}

	public ResolvedMember lookupSyntheticMember(Member member) {
		// ??? horribly inefficient
		// for (Iterator i =
		// System.err.println("lookup " + member + " in " + interTypeMungers);
		for (ConcreteTypeMunger m : interTypeMungers) {
			ResolvedMember ret = m.getMatchingSyntheticMember(member);
			if (ret != null) {
				// System.err.println("   found: " + ret);
				return ret;
			}
		}

		// Handling members for the new array join point
		if (world.isJoinpointArrayConstructionEnabled() && this.isArray()) {
			if (member.getKind() == Member.CONSTRUCTOR) {
				ResolvedMemberImpl ret = new ResolvedMemberImpl(Member.CONSTRUCTOR, this, Modifier.PUBLIC, UnresolvedType.VOID,
						"<init>", world.resolve(member.getParameterTypes()));
				// Give the parameters names - they are going to be the dimensions uses to build the array (dim0 > dimN)
				int count = ret.getParameterTypes().length;
				String[] paramNames = new String[count];
				for (int i = 0; i < count; i++) {
					paramNames[i] = new StringBuffer("dim").append(i).toString();
				}
				ret.setParameterNames(paramNames);
				return ret;
			}
		}

		// if (this.getSuperclass() != ResolvedType.OBJECT &&
		// this.getSuperclass() != null) {
		// return getSuperclass().lookupSyntheticMember(member);
		// }

		return null;
	}

	static class SuperClassWalker implements Iterator<ResolvedType> {

		private ResolvedType curr;
		private SuperInterfaceWalker iwalker;
		private boolean wantGenerics;

		public SuperClassWalker(ResolvedType type, SuperInterfaceWalker iwalker, boolean genericsAware) {
			this.curr = type;
			this.iwalker = iwalker;
			this.wantGenerics = genericsAware;
		}

		@Override
		public boolean hasNext() {
			return curr != null;
		}

		@Override
		public ResolvedType next() {
			ResolvedType ret = curr;
			if (!wantGenerics && ret.isParameterizedOrGenericType()) {
				ret = ret.getRawType();
			}
			iwalker.push(ret); // tell the interface walker about another class whose interfaces need visiting
			curr = curr.getSuperclass();
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	static class SuperInterfaceWalker implements Iterator<ResolvedType> {

		private Getter<ResolvedType, ResolvedType> ifaceGetter;
		Iterator<ResolvedType> delegate = null;
		public Queue<ResolvedType> toPersue = new LinkedList<>();
		public Set<ResolvedType> visited = new HashSet<>();

		SuperInterfaceWalker(Iterators.Getter<ResolvedType, ResolvedType> ifaceGetter) {
			this.ifaceGetter = ifaceGetter;
		}

		SuperInterfaceWalker(Iterators.Getter<ResolvedType, ResolvedType> ifaceGetter, ResolvedType interfaceType) {
			this.ifaceGetter = ifaceGetter;
			this.delegate = Iterators.one(interfaceType);
		}

		@Override
		public boolean hasNext() {
			if (delegate == null || !delegate.hasNext()) {
				// either we set it up or we have run out, is there anything else to look at?
				if (toPersue.isEmpty()) {
					return false;
				}
				do {
					ResolvedType next = toPersue.remove();
					visited.add(next);
					delegate = ifaceGetter.get(next); // retrieve interfaces from a class or another interface
				} while (!delegate.hasNext() && !toPersue.isEmpty());
			}
			return delegate.hasNext();
		}

		public void push(ResolvedType ret) {
			toPersue.add(ret);
		}

		@Override
		public ResolvedType next() {
			ResolvedType next = delegate.next();
			// BUG should check for generics and erase?
			// if (!visited.contains(next)) {
			// visited.add(next);
			if (visited.add(next)) {
				toPersue.add(next); // pushes on interfaces already visited?
			}
			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public void clearInterTypeMungers() {
		if (isRawType()) {
			ResolvedType genericType = getGenericType();
			if (genericType.isRawType()) { // ERROR SITUATION: PR341926
				// For some reason the raw type is pointing to another raw form (possibly itself)
				System.err.println("DebugFor341926: Type " + this.getName() + " has an incorrect generic form");
			} else {
				genericType.clearInterTypeMungers();
			}
		}
		// interTypeMungers.clear();
		// BUG? Why can't this be clear() instead: 293620 c6
		interTypeMungers = new ArrayList<>();
	}

	public boolean isTopmostImplementor(ResolvedType interfaceType) {
		boolean b = true;
		if (isInterface()) {
			b = false;
		} else if (!interfaceType.isAssignableFrom(this, true)) {
			b = false;
		} else {
			ResolvedType superclass = this.getSuperclass();
			if (superclass.isMissing()) {
				b = true; // we don't know anything about supertype, and it can't be exposed to weaver
			} else if (interfaceType.isAssignableFrom(superclass, true)) { // check that I'm truly the topmost implementor
				b = false;
			}
		}
		// System.out.println("is " + getName() + " topmostimplementor of " + interfaceType + "? " + b);
		return b;
	}

	public ResolvedType getTopmostImplementor(ResolvedType interfaceType) {
		if (isInterface()) {
			return null;
		}
		if (!interfaceType.isAssignableFrom(this)) {
			return null;
		}
		// Check if my super class is an implementor?
		ResolvedType higherType = this.getSuperclass().getTopmostImplementor(interfaceType);
		if (higherType != null) {
			return higherType;
		}
		return this;
	}

	public List<ResolvedMember> getExposedPointcuts() {
		List<ResolvedMember> ret = new ArrayList<>();
		if (getSuperclass() != null) {
			ret.addAll(getSuperclass().getExposedPointcuts());
		}

		for (ResolvedType type : getDeclaredInterfaces()) {
			addPointcutsResolvingConflicts(ret, Arrays.asList(type.getDeclaredPointcuts()), false);
		}

		addPointcutsResolvingConflicts(ret, Arrays.asList(getDeclaredPointcuts()), true);

		for (ResolvedMember member : ret) {
			ResolvedPointcutDefinition inherited = (ResolvedPointcutDefinition) member;
			if (inherited != null && inherited.isAbstract()) {
				if (!this.isAbstract()) {
					getWorld().showMessage(IMessage.ERROR,
							WeaverMessages.format(WeaverMessages.POINCUT_NOT_CONCRETE, inherited, this.getName()),
							inherited.getSourceLocation(), this.getSourceLocation());
				}
			}
		}
		return ret;
	}

	private void addPointcutsResolvingConflicts(List<ResolvedMember> acc, List<ResolvedMember> added, boolean isOverriding) {
		for (ResolvedMember resolvedMember : added) {
			ResolvedPointcutDefinition toAdd = (ResolvedPointcutDefinition) resolvedMember;
			for (Iterator<ResolvedMember> j = acc.iterator(); j.hasNext(); ) {
				ResolvedPointcutDefinition existing = (ResolvedPointcutDefinition) j.next();
				if (toAdd == null || existing == null || existing == toAdd) {
					continue;
				}
				UnresolvedType pointcutDeclaringTypeUT = existing.getDeclaringType();
				if (pointcutDeclaringTypeUT != null) {
					ResolvedType pointcutDeclaringType = pointcutDeclaringTypeUT.resolve(getWorld());
					if (!isVisible(existing.getModifiers(), pointcutDeclaringType, this)) {
						// if they intended to override it but it is not visible,
						// give them a nicer message
						if (existing.isAbstract() && conflictingSignature(existing, toAdd)) {
							getWorld().showMessage(
									IMessage.ERROR,
									WeaverMessages.format(WeaverMessages.POINTCUT_NOT_VISIBLE, existing.getDeclaringType()
											.getName() + "." + existing.getName() + "()", this.getName()),
									toAdd.getSourceLocation(), null);
							j.remove();
						}
						continue;
					}
				}
				if (conflictingSignature(existing, toAdd)) {
					if (isOverriding) {
						checkLegalOverride(existing, toAdd, 0x00, null);
						j.remove();
					} else {
						getWorld().showMessage(
								IMessage.ERROR,
								WeaverMessages.format(WeaverMessages.CONFLICTING_INHERITED_POINTCUTS,
										this.getName() + toAdd.getSignature()), existing.getSourceLocation(),
								toAdd.getSourceLocation());
						j.remove();
					}
				}
			}
			acc.add(toAdd);
		}
	}

	public ISourceLocation getSourceLocation() {
		return null;
	}

	public boolean isExposedToWeaver() {
		return false;
	}

	public WeaverStateInfo getWeaverState() {
		return null;
	}

	/**
	 * Overridden by ReferenceType to return a sensible answer for parameterized and raw types.
	 * 
	 * @return
	 */
	public ReferenceType getGenericType() {
		// if (!(isParameterizedType() || isRawType()))
		// throw new BCException("The type " + getBaseName() + " is not parameterized or raw - it has no generic type");
		return null;
	}

	@Override
	public ResolvedType getRawType() {
		return super.getRawType().resolve(world);
	}

	public ResolvedType parameterizedWith(UnresolvedType[] typeParameters) {
		if (!(isGenericType() || isParameterizedType())) {
			return this;
		}
		return TypeFactory.createParameterizedType(this.getGenericType(), typeParameters, getWorld());
	}

	/**
	 * Iff I am a parameterized type, and any of my parameters are type variable references (or nested parameterized types), 
	 * return a version with those type parameters replaced in accordance with the passed bindings.
	 */
	@Override
	public UnresolvedType parameterize(Map<String, UnresolvedType> typeBindings) {
		if (!isParameterizedType()) {
			// throw new IllegalStateException("Can't parameterize a type that is not a parameterized type");
			return this;
		}
		boolean workToDo = false;
		for (UnresolvedType typeParameter : typeParameters) {
			if (typeParameter.isTypeVariableReference() || (typeParameter instanceof BoundedReferenceType) || typeParameter.isParameterizedType()) {
				workToDo = true;
			}
		}
		if (!workToDo) {
			return this;
		} else {
			UnresolvedType[] newTypeParams = new UnresolvedType[typeParameters.length];
			for (int i = 0; i < newTypeParams.length; i++) {
				newTypeParams[i] = typeParameters[i];
				if (newTypeParams[i].isTypeVariableReference()) {
					TypeVariableReferenceType tvrt = (TypeVariableReferenceType) newTypeParams[i];
					UnresolvedType binding = typeBindings.get(tvrt.getTypeVariable().getName());
					if (binding != null) {
						newTypeParams[i] = binding;
					}
				} else if (newTypeParams[i] instanceof BoundedReferenceType) {
					BoundedReferenceType brType = (BoundedReferenceType) newTypeParams[i];
					newTypeParams[i] = brType.parameterize(typeBindings);
					// brType.parameterize(typeBindings)
				} else if (newTypeParams[i].isParameterizedType()) {
					newTypeParams[i] = newTypeParams[i].parameterize(typeBindings);
				}
			}
			return TypeFactory.createParameterizedType(getGenericType(), newTypeParams, getWorld());
		}
	}

	// public boolean hasParameterizedSuperType() {
	// getParameterizedSuperTypes();
	// return parameterizedSuperTypes.length > 0;
	// }

	// public boolean hasGenericSuperType() {
	// ResolvedType[] superTypes = getDeclaredInterfaces();
	// for (int i = 0; i < superTypes.length; i++) {
	// if (superTypes[i].isGenericType())
	// return true;
	// }
	// return false;
	// }

	// private ResolvedType[] parameterizedSuperTypes = null;

	/**
	 * Similar to the above method, but accumulates the super types
	 * 
	 * @return
	 */
	// public ResolvedType[] getParameterizedSuperTypes() {
	// if (parameterizedSuperTypes != null)
	// return parameterizedSuperTypes;
	// List accumulatedTypes = new ArrayList();
	// accumulateParameterizedSuperTypes(this, accumulatedTypes);
	// ResolvedType[] ret = new ResolvedType[accumulatedTypes.size()];
	// parameterizedSuperTypes = (ResolvedType[]) accumulatedTypes.toArray(ret);
	// return parameterizedSuperTypes;
	// }
	// private void accumulateParameterizedSuperTypes(ResolvedType forType, List
	// parameterizedTypeList) {
	// if (forType.isParameterizedType()) {
	// parameterizedTypeList.add(forType);
	// }
	// if (forType.getSuperclass() != null) {
	// accumulateParameterizedSuperTypes(forType.getSuperclass(),
	// parameterizedTypeList);
	// }
	// ResolvedType[] interfaces = forType.getDeclaredInterfaces();
	// for (int i = 0; i < interfaces.length; i++) {
	// accumulateParameterizedSuperTypes(interfaces[i], parameterizedTypeList);
	// }
	// }
	/**
	 * @return true if assignable to java.lang.Exception
	 */
	public boolean isException() {
		return (world.getCoreType(UnresolvedType.JL_EXCEPTION).isAssignableFrom(this));
	}

	/**
	 * @return true if it is an exception and it is a checked one, false otherwise.
	 */
	public boolean isCheckedException() {
		if (!isException()) {
			return false;
		}
		if (world.getCoreType(UnresolvedType.RUNTIME_EXCEPTION).isAssignableFrom(this)) {
			return false;
		}
		return true;
	}

	/**
	 * Determines if variables of this type could be assigned values of another with lots of help. java.lang.Object is convertable
	 * from all types. A primitive type is convertable from X iff it's assignable from X. A reference type is convertable from X iff
	 * it's coerceable from X. In other words, X isConvertableFrom Y iff the compiler thinks that _some_ value of Y could be
	 * assignable to a variable of type X without loss of precision.
	 * 
	 * @param other the other type
	 * @return true iff variables of this type could be assigned values of other with possible conversion
	 */
	public final boolean isConvertableFrom(ResolvedType other) {

		// // version from TypeX
		// if (this.equals(OBJECT)) return true;
		// if (this.isPrimitiveType() || other.isPrimitiveType()) return
		// this.isAssignableFrom(other);
		// return this.isCoerceableFrom(other);
		//

		// version from ResolvedTypeX
		if (this.equals(OBJECT)) {
			return true;
		}
		if (world.isInJava5Mode()) {
			if (this.isPrimitiveType() ^ other.isPrimitiveType()) { // If one is
				// primitive
				// and the
				// other
				// isnt
				if (validBoxing.contains(this.getSignature() + other.getSignature())) {
					return true;
				}
			}
		}
		if (this.isPrimitiveType() || other.isPrimitiveType()) {
			return this.isAssignableFrom(other);
		}
		return this.isCoerceableFrom(other);
	}

	/**
	 * Determines if the variables of this type could be assigned values of another type without casting. This still allows for
	 * assignment conversion as per JLS 2ed 5.2. For object types, this means supertypeOrEqual(THIS, OTHER).
	 * 
	 * @param other the other type
	 * @return true iff variables of this type could be assigned values of other without casting
	 * @throws NullPointerException if other is null
	 */
	public abstract boolean isAssignableFrom(ResolvedType other);

	public abstract boolean isAssignableFrom(ResolvedType other, boolean allowMissing);

	/**
	 * Determines if values of another type could possibly be cast to this type. The rules followed are from JLS 2ed 5.5,
	 * "Casting Conversion".
	 * <p>
	 * This method should be commutative, i.e., for all UnresolvedType a, b and all World w:
	 * </p>
	 * <blockquote>
	 * 
	 * <pre>
	 * a.isCoerceableFrom(b, w) == b.isCoerceableFrom(a, w)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param other the other type
	 * @return true iff values of other could possibly be cast to this type.
	 * @throws NullPointerException if other is null.
	 */
	public abstract boolean isCoerceableFrom(ResolvedType other);

	public boolean needsNoConversionFrom(ResolvedType o) {
		return isAssignableFrom(o);
	}

	public String getSignatureForAttribute() {
		return signature; // Assume if this is being called that it is for a
		// simple type (eg. void, int, etc)
	}

	private FuzzyBoolean parameterizedWithTypeVariable = FuzzyBoolean.MAYBE;

	/**
	 * return true if the parameterization of this type includes a member type variable. Member type variables occur in generic
	 * methods/ctors.
	 */
	public boolean isParameterizedWithTypeVariable() {
		// MAYBE means we haven't worked it out yet...
		if (parameterizedWithTypeVariable == FuzzyBoolean.MAYBE) {

			// if there are no type parameters then we cant be...
			if (typeParameters == null || typeParameters.length == 0) {
				parameterizedWithTypeVariable = FuzzyBoolean.NO;
				return false;
			}

			for (UnresolvedType typeParameter : typeParameters) {
				ResolvedType aType = (ResolvedType) typeParameter;
				if (aType.isTypeVariableReference()
					// Changed according to the problems covered in bug 222648
					// Don't care what kind of type variable - the fact that there
					// is one
					// at all means we can't risk caching it against we get confused
					// later
					// by another variation of the parameterization that just
					// happens to
					// use the same type variable name

					// assume the worst - if its definetly not a type declared one,
					// it could be anything
					// && ((TypeVariableReference)aType).getTypeVariable().
					// getDeclaringElementKind()!=TypeVariable.TYPE
				) {
					parameterizedWithTypeVariable = FuzzyBoolean.YES;
					return true;
				}
				if (aType.isParameterizedType()) {
					boolean b = aType.isParameterizedWithTypeVariable();
					if (b) {
						parameterizedWithTypeVariable = FuzzyBoolean.YES;
						return true;
					}
				}
				if (aType.isGenericWildcard()) {
					BoundedReferenceType boundedRT = (BoundedReferenceType) aType;
					if (boundedRT.isExtends()) {
						boolean b = false;
						UnresolvedType upperBound = boundedRT.getUpperBound();
						if (upperBound.isParameterizedType()) {
							b = ((ResolvedType) upperBound).isParameterizedWithTypeVariable();
						} else if (upperBound.isTypeVariableReference()
								&& ((TypeVariableReference) upperBound).getTypeVariable().getDeclaringElementKind() == TypeVariable.METHOD) {
							b = true;
						}
						if (b) {
							parameterizedWithTypeVariable = FuzzyBoolean.YES;
							return true;
						}
						// FIXME asc need to check additional interface bounds
					}
					if (boundedRT.isSuper()) {
						boolean b = false;
						UnresolvedType lowerBound = boundedRT.getLowerBound();
						if (lowerBound.isParameterizedType()) {
							b = ((ResolvedType) lowerBound).isParameterizedWithTypeVariable();
						} else if (lowerBound.isTypeVariableReference()
								&& ((TypeVariableReference) lowerBound).getTypeVariable().getDeclaringElementKind() == TypeVariable.METHOD) {
							b = true;
						}
						if (b) {
							parameterizedWithTypeVariable = FuzzyBoolean.YES;
							return true;
						}
					}
				}
			}
			parameterizedWithTypeVariable = FuzzyBoolean.NO;
		}
		return parameterizedWithTypeVariable.alwaysTrue();
	}

	protected boolean ajMembersNeedParameterization() {
		if (isParameterizedType()) {
			return true;
		}
		ResolvedType superclass = getSuperclass();
		if (superclass != null && !superclass.isMissing()) {
			return superclass.ajMembersNeedParameterization();
		}
		return false;
	}

	protected Map<String, UnresolvedType> getAjMemberParameterizationMap() {
		Map<String, UnresolvedType> myMap = getMemberParameterizationMap();
		if (myMap.isEmpty()) {
			// might extend a parameterized aspect that we also need to
			// consider...
			if (getSuperclass() != null) {
				return getSuperclass().getAjMemberParameterizationMap();
			}
		}
		return myMap;
	}

	public void setBinaryPath(String binaryPath) {
		this.binaryPath = binaryPath;
	}

	/**
	 * Returns the path to the jar or class file from which this binary aspect came or null if not a binary aspect
	 */
	public String getBinaryPath() {
		return binaryPath;
	}

	/**
	 * Undo any temporary modifications to the type (for example it may be holding annotations temporarily whilst some matching is
	 * occurring - These annotations will be added properly during weaving but sometimes for type completion they need to be held
	 * here for a while).
	 */
	public void ensureConsistent() {
		// Nothing to do for anything except a ReferenceType
	}

	/**
	 * For an annotation type, this will return if it is marked with @Inherited
	 */
	public boolean isInheritedAnnotation() {
		ensureAnnotationBitsInitialized();
		return (bits & AnnotationMarkedInherited) != 0;
	}

	/*
	 * Setup the bitflags if they have not already been done.
	 */
	private void ensureAnnotationBitsInitialized() {
		if ((bits & AnnotationBitsInitialized) == 0) {
			bits |= AnnotationBitsInitialized;
			// Is it marked @Inherited?
			if (hasAnnotation(UnresolvedType.AT_INHERITED)) {
				bits |= AnnotationMarkedInherited;
			}
		}
	}

	private boolean hasNewParentMungers() {
		if ((bits & MungersAnalyzed) == 0) {
			bits |= MungersAnalyzed;
			for (ConcreteTypeMunger munger : interTypeMungers) {
				ResolvedTypeMunger resolvedTypeMunger = munger.getMunger();
				if (resolvedTypeMunger != null && resolvedTypeMunger.getKind() == ResolvedTypeMunger.Parent) {
					bits |= HasParentMunger;
				}
			}
		}
		return (bits & HasParentMunger) != 0;
	}

	public void tagAsTypeHierarchyComplete() {
		if (isParameterizedOrRawType()) {
			ReferenceType genericType = this.getGenericType();
			genericType.tagAsTypeHierarchyComplete();
			return;
		}
		bits |= TypeHierarchyCompleteBit;
	}

	public boolean isTypeHierarchyComplete() {
		if (isParameterizedOrRawType()) {
			return this.getGenericType().isTypeHierarchyComplete();
		}
		return (bits & TypeHierarchyCompleteBit) != 0;
	}

	/**
	 * return the weaver version used to build this type - defaults to the most recent version unless discovered otherwise.
	 * 
	 * @return the (major) version, {@link WeaverVersionInfo}
	 */
	public int getCompilerVersion() {
		return WeaverVersionInfo.getCurrentWeaverMajorVersion();
	}

	public boolean isPrimitiveArray() {
		return false;
	}

	public boolean isGroovyObject() {
		if ((bits & GroovyObjectInitialized) == 0) {
			ResolvedType[] intfaces = getDeclaredInterfaces();
			boolean done = false;
			// TODO do we need to walk more of these? (i.e. the interfaces interfaces and supertypes supertype). Check what groovy
			// does in the case where a hierarchy is involved and there are types in between GroovyObject/GroovyObjectSupport and
			// the type
			if (intfaces != null) {
				for (ResolvedType intface : intfaces) {
					if (intface.getName().equals("groovy.lang.GroovyObject")) {
						bits |= IsGroovyObject;
						done = true;
						break;
					}
				}
			}
			if (!done) {
				// take a look at the supertype
				if (getSuperclass().getName().equals("groovy.lang.GroovyObjectSupport")) {
					bits |= IsGroovyObject;
				}
			}
			bits |= GroovyObjectInitialized;
		}
		return (bits & IsGroovyObject) != 0;
	}
	
	public boolean isPrivilegedAspect() {
		if ((bits & IsPrivilegedBitInitialized) == 0) {
			AnnotationAJ privilegedAnnotation = getAnnotationOfType(UnresolvedType.AJC_PRIVILEGED);
			if (privilegedAnnotation != null) {
				bits |= IsPrivilegedAspect;
			}
			// TODO do we need to reset this bit if the annotations are set again ?
			bits |= IsPrivilegedBitInitialized;
		}
		return (bits & IsPrivilegedAspect) != 0;
	}

}
