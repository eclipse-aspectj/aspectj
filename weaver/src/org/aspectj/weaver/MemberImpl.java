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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class MemberImpl implements Member {

	protected MemberKind kind;
	protected int modifiers;
	protected String name;
	protected UnresolvedType returnType;
	protected UnresolvedType declaringType;
	protected UnresolvedType[] parameterTypes;
	private final String signature;
	private String paramSignature;

	// OPTIMIZE move out of the member!
	private boolean reportedCantFindDeclaringType = false;
	private boolean reportedUnresolvableMember = false;

	/**
	 * All the signatures that a join point with this member as its signature
	 * has.
	 */
	private JoinPointSignatureIterator joinPointSignatures = null;

	public MemberImpl(MemberKind kind, UnresolvedType declaringType,
			int modifiers, String name, String signature) {
		this.kind = kind;
		this.declaringType = declaringType;
		this.modifiers = modifiers;
		this.name = name;
		this.signature = signature;
		if (kind == FIELD) {
			this.returnType = UnresolvedType.forSignature(signature);
			this.parameterTypes = UnresolvedType.NONE;
		} else {
			Object[] returnAndParams = signatureToTypes(signature, false);
			this.returnType = (UnresolvedType) returnAndParams[0];
			this.parameterTypes = (UnresolvedType[]) returnAndParams[1];
			// always safe not to do this ?!?
			// String oldsig=new String(signature);
			// signature = typesToSignature(returnType,parameterTypes,true);
		}
	}

	public MemberImpl(MemberKind kind, UnresolvedType declaringType,
			int modifiers, UnresolvedType returnType, String name,
			UnresolvedType[] parameterTypes) {
		this.kind = kind;
		this.declaringType = declaringType;
		this.modifiers = modifiers;
		this.returnType = returnType;
		this.name = name;
		this.parameterTypes = parameterTypes;
		if (kind == FIELD) {
			this.signature = returnType.getErasureSignature();
		} else {
			this.signature = typesToSignature(returnType, parameterTypes, true);
		}
	}

	public ResolvedMember resolve(World world) {
		return world.resolve(this);
	}

	// ---- utility methods

	/**
	 * returns an Object[] pair of UnresolvedType, UnresolvedType[] representing
	 * return type, argument types parsed from the JVM bytecode signature of a
	 * method. Yes, this should actually return a nice statically-typed pair
	 * object, but we don't have one of those.
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 *   UnresolvedType.signatureToTypes(&quot;()[Z&quot;)[0].equals(Type.forSignature(&quot;[Z&quot;))
	 *   UnresolvedType.signatureToTypes(&quot;(JJ)I&quot;)[1]
	 *      .equals(UnresolvedType.forSignatures(new String[] {&quot;J&quot;, &quot;J&quot;}))
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param signature
	 *            the JVM bytecode method signature string we want to break
	 *            apart
	 * @return a pair of UnresolvedType, UnresolvedType[] representing the
	 *         return types and parameter types.
	 */
	// OPTIMIZE move static util methods out into a memberutils class
	public static String typesToSignature(UnresolvedType returnType,
			UnresolvedType[] paramTypes, boolean useRawTypes) {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for (int i = 0, len = paramTypes.length; i < len; i++) {
			if (paramTypes[i].isParameterizedType() && useRawTypes)
				buf.append(paramTypes[i].getErasureSignature());
			else if (paramTypes[i].isTypeVariableReference() && useRawTypes)
				buf.append(paramTypes[i].getErasureSignature());
			else
				buf.append(paramTypes[i].getSignature());
		}
		buf.append(")");
		if (returnType.isParameterizedType() && useRawTypes)
			buf.append(returnType.getErasureSignature());
		else if (returnType.isTypeVariableReference() && useRawTypes)
			buf.append(returnType.getErasureSignature());
		else
			buf.append(returnType.getSignature());
		return buf.toString();
	}

	/**
	 * Returns "(<signaturesOfParamTypes>,...)" - unlike the other
	 * typesToSignature that also includes the return type, this one just deals
	 * with the parameter types.
	 */
	public static String typesToSignature(UnresolvedType[] paramTypes) {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for (int i = 0; i < paramTypes.length; i++) {
			buf.append(paramTypes[i].getSignature());
		}
		buf.append(")");
		return buf.toString();
	}

	/**
	 * returns an Object[] pair of UnresolvedType, UnresolvedType[] representing
	 * return type, argument types parsed from the JVM bytecode signature of a
	 * method. Yes, this should actually return a nice statically-typed pair
	 * object, but we don't have one of those.
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 *   UnresolvedType.signatureToTypes(&quot;()[Z&quot;)[0].equals(Type.forSignature(&quot;[Z&quot;))
	 *   UnresolvedType.signatureToTypes(&quot;(JJ)I&quot;)[1]
	 *      .equals(UnresolvedType.forSignatures(new String[] {&quot;J&quot;, &quot;J&quot;}))
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param signature
	 *            the JVM bytecode method signature string we want to break
	 *            apart
	 * @return a pair of UnresolvedType, UnresolvedType[] representing the
	 *         return types and parameter types.
	 */
	private static Object[] signatureToTypes(String sig,
			boolean keepParameterizationInfo) {
		boolean hasParameters = sig.charAt(1) != ')';
		if (hasParameters) {
			List l = new ArrayList();
			int i = 1;
			boolean hasAnyAnglies = sig.indexOf('<') != -1;
			while (true) {
				char c = sig.charAt(i);
				if (c == ')')
					break; // break out when the hit the ')'
				int start = i;
				while (c == '[')
					c = sig.charAt(++i);
				if (c == 'L' || c == 'P') {
					int nextSemicolon = sig.indexOf(';', start);
					int firstAngly = (hasAnyAnglies ? sig.indexOf('<', start)
							: -1);
					if (!hasAnyAnglies || firstAngly == -1
							|| firstAngly > nextSemicolon) {
						i = nextSemicolon + 1;
						l.add(UnresolvedType.forSignature(sig.substring(start,
								i)));
					} else {
						// generics generics generics
						// Have to skip to the *correct* ';'
						boolean endOfSigReached = false;
						int posn = firstAngly;
						int genericDepth = 0;
						while (!endOfSigReached) {
							switch (sig.charAt(posn)) {
							case '<':
								genericDepth++;
								break;
							case '>':
								genericDepth--;
								break;
							case ';':
								if (genericDepth == 0)
									endOfSigReached = true;
								break;
							default:
							}
							posn++;
						}
						// posn now points to the correct nextSemicolon :)
						i = posn;
						l.add(UnresolvedType.forSignature(sig.substring(start,
								i)));
					}
				} else if (c == 'T') { // assumed 'reference' to a type
										// variable, so just "Tname;"
					int nextSemicolon = sig.indexOf(';', start);
					String nextbit = sig.substring(start, nextSemicolon);
					l.add(UnresolvedType.forSignature(nextbit));
					i = nextSemicolon + 1;
				} else {
					i++;
					l.add(UnresolvedType.forSignature(sig.substring(start, i)));
				}
			}
			UnresolvedType[] paramTypes = (UnresolvedType[]) l
					.toArray(new UnresolvedType[l.size()]);
			UnresolvedType returnType = UnresolvedType.forSignature(sig
					.substring(i + 1, sig.length()));
			return new Object[] { returnType, paramTypes };
		} else {
			UnresolvedType returnType = UnresolvedType.forSignature(sig
					.substring(2));
			return new Object[] { returnType, UnresolvedType.NONE };
		}
	}

	// ---- factory methods
	public static MemberImpl field(String declaring, int mods, String name,
			String signature) {
		return field(declaring, mods, UnresolvedType.forSignature(signature),
				name);
	}

	// public static Member field(UnresolvedType declaring, int mods, String
	// name, UnresolvedType type) {
	// return new MemberImpl(FIELD, declaring, mods, type, name,
	// UnresolvedType.NONE);
	// }
	// OPTIMIZE do we need to call this? unless necessary the signatureToTypes()
	// call smacks of laziness on the behalf of the caller
	// of this method
	public static MemberImpl method(UnresolvedType declaring, int mods,
			String name, String signature) {
		Object[] pair = signatureToTypes(signature, false);
		return method(declaring, mods, (UnresolvedType) pair[0], name,
				(UnresolvedType[]) pair[1]);
	}

	public static MemberImpl monitorEnter() {
		return new MemberImpl(MONITORENTER, UnresolvedType.OBJECT,
				Modifier.STATIC, ResolvedType.VOID, "<lock>",
				UnresolvedType.ARRAY_WITH_JUST_OBJECT);
	}

	public static MemberImpl monitorExit() {
		return new MemberImpl(MONITOREXIT, UnresolvedType.OBJECT,
				Modifier.STATIC, ResolvedType.VOID, "<unlock>",
				UnresolvedType.ARRAY_WITH_JUST_OBJECT);
	}

	public static Member pointcut(UnresolvedType declaring, String name,
			String signature) {
		Object[] pair = signatureToTypes(signature, false);
		return pointcut(declaring, 0, (UnresolvedType) pair[0], name,
				(UnresolvedType[]) pair[1]);
	}

	private static MemberImpl field(String declaring, int mods,
			UnresolvedType ty, String name) {
		return new MemberImpl(FIELD, UnresolvedType.forName(declaring), mods,
				ty, name, UnresolvedType.NONE);
	}

	public static MemberImpl method(UnresolvedType declTy, int mods,
			UnresolvedType rTy, String name, UnresolvedType[] paramTys) {
		return new MemberImpl(
				// ??? this calls <clinit> a method
				name.equals("<init>") ? CONSTRUCTOR : METHOD, declTy, mods,
				rTy, name, paramTys);
	}

	private static Member pointcut(UnresolvedType declTy, int mods,
			UnresolvedType rTy, String name, UnresolvedType[] paramTys) {
		return new MemberImpl(POINTCUT, declTy, mods, rTy, name, paramTys);
	}

	public static ResolvedMemberImpl makeExceptionHandlerSignature(
			UnresolvedType inType, UnresolvedType catchType) {
		return new ResolvedMemberImpl(HANDLER, inType, Modifier.STATIC,
				"<catch>", "(" + catchType.getSignature() + ")V");
	}

	public boolean equals(Object other) {
		if (!(other instanceof Member))
			return false;
		Member o = (Member) other;
		return (getKind() == o.getKind() && getName().equals(o.getName())
				&& getSignature().equals(o.getSignature()) && getDeclaringType()
				.equals(o.getDeclaringType()));
	}

	/**
	 * Equality is checked based on the underlying signature, so the hash code
	 * of a member is based on its kind, name, signature, and declaring type.
	 * The algorithm for this was taken from page 38 of effective java.
	 */
	private volatile int hashCode = 0;

	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			result = 37 * result + getKind().hashCode();
			result = 37 * result + getName().hashCode();
			result = 37 * result + getSignature().hashCode();
			result = 37 * result + getDeclaringType().hashCode();
			hashCode = result;
		}
		return hashCode;
	}

	public int compareTo(Object other) {
		Member o = (Member) other;
		int i = getName().compareTo(o.getName());
		if (i != 0)
			return i;
		return getSignature().compareTo(o.getSignature());
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(returnType.getName());
		buf.append(' ');
		buf.append(declaringType.getName());
		buf.append('.');
		buf.append(name);
		if (kind != FIELD) {
			buf.append("(");
			if (parameterTypes.length != 0) {
				buf.append(parameterTypes[0]);
				for (int i = 1, len = parameterTypes.length; i < len; i++) {
					buf.append(", ");
					buf.append(parameterTypes[i].getName());
				}
			}
			buf.append(")");
		}
		return buf.toString();
	}

	public MemberKind getKind() {
		return kind;
	}

	public UnresolvedType getDeclaringType() {
		return declaringType;
	}

	public UnresolvedType getReturnType() {
		return returnType;
	}

	public UnresolvedType getGenericReturnType() {
		return getReturnType();
	}

	public UnresolvedType[] getGenericParameterTypes() {
		return getParameterTypes();
	}

	public final UnresolvedType getType() {
		return returnType;
	}

	public String getName() {
		return name;
	}

	public UnresolvedType[] getParameterTypes() {
		return parameterTypes;
	}

	public String getSignature() {
		return signature;
	}

	public int getArity() {
		return parameterTypes.length;
	}

	public String getParameterSignature() {
		if (paramSignature != null)
			return paramSignature;
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for (int i = 0; i < parameterTypes.length; i++) {
			UnresolvedType tx = parameterTypes[i];
			sb.append(tx.getSignature());
		}
		sb.append(")");
		paramSignature = sb.toString();
		return paramSignature;
	}

	// OPTIMIZE see next line. Why the hell are they in here if we only know it
	// once resolution has occurred...
	// ---- things we know only with resolution

	public int getModifiers(World world) {
		ResolvedMember resolved = resolve(world);
		if (resolved == null) {
			reportDidntFindMember(world);
			return 0;
		}
		return resolved.getModifiers();
	}

	public UnresolvedType[] getExceptions(World world) {
		ResolvedMember resolved = resolve(world);
		if (resolved == null) {
			reportDidntFindMember(world);
			return UnresolvedType.NONE;
		}
		return resolved.getExceptions();
	}

	public final boolean isStatic() {
		return Modifier.isStatic(modifiers);
	}

	public final boolean isInterface() {
		return Modifier.isInterface(modifiers);
	}

	public final boolean isPrivate() {
		return Modifier.isPrivate(modifiers);
	}

	public boolean canBeParameterized() {
		return false;
	}

	public int getModifiers() {
		return modifiers;
	}

	public AnnotationAJ[] getAnnotations() {
		throw new UnsupportedOperationException(
				"You should resolve this member '" + this
						+ "' and call getAnnotations() on the result...");
	}

	// ---- fields 'n' stuff

	public Collection/* ResolvedType */getDeclaringTypes(World world) {
		ResolvedType myType = getDeclaringType().resolve(world);
		Collection ret = new HashSet();
		if (kind == CONSTRUCTOR) {
			// this is wrong if the member doesn't exist, but that doesn't
			// matter
			ret.add(myType);
		} else if (isStatic() || kind == FIELD) {
			walkUpStatic(ret, myType);
		} else {
			walkUp(ret, myType);
		}

		return ret;
	}

	private boolean walkUp(Collection acc, ResolvedType curr) {
		if (acc.contains(curr))
			return true;

		boolean b = false;
		for (Iterator i = curr.getDirectSupertypes(); i.hasNext();) {
			b |= walkUp(acc, (ResolvedType) i.next());
		}

		if (!b && curr.isParameterizedType()) {
			b = walkUp(acc, curr.getGenericType());
		}

		if (!b) {
			b = curr.lookupMemberNoSupers(this) != null;
		}
		if (b)
			acc.add(curr);
		return b;
	}

	private boolean walkUpStatic(Collection acc, ResolvedType curr) {
		if (curr.lookupMemberNoSupers(this) != null) {
			acc.add(curr);
			return true;
		} else {
			boolean b = false;
			for (Iterator i = curr.getDirectSupertypes(); i.hasNext();) {
				b |= walkUpStatic(acc, (ResolvedType) i.next());
			}
			if (!b && curr.isParameterizedType()) {
				b = walkUpStatic(acc, curr.getGenericType());
			}
			if (b)
				acc.add(curr);
			return b;
		}
	}

	public String[] getParameterNames(World world) {
		ResolvedMember resolved = resolve(world);
		if (resolved == null) {
			reportDidntFindMember(world);
			return null;
		}
		return resolved.getParameterNames();
	}

	/**
	 * All the signatures that a join point with this member as its signature
	 * has.
	 */
	public Iterator getJoinPointSignatures(World inAWorld) {
		if (joinPointSignatures == null) {
			joinPointSignatures = new JoinPointSignatureIterator(this, inAWorld);
		}
		joinPointSignatures.reset();
		return joinPointSignatures;
	}

	/**
	 * Raises an [Xlint:cantFindType] message if the declaring type cannot be
	 * found or an [Xlint:unresolvableMember] message if the type can be found
	 * (bug 149908)
	 */
	private void reportDidntFindMember(World world) {
		if (reportedCantFindDeclaringType || reportedUnresolvableMember)
			return;
		ResolvedType rType = getDeclaringType().resolve(world);
		if (rType.isMissing()) {
			world.getLint().cantFindType.signal(WeaverMessages.format(
					WeaverMessages.CANT_FIND_TYPE, rType.getName()), null);
			reportedCantFindDeclaringType = true;
		} else {
			world.getLint().unresolvableMember.signal(getName(), null);
			reportedUnresolvableMember = true;
		}
	}

	public void wipeJoinpointSignatures() {
		joinPointSignatures = null;
	}
}
