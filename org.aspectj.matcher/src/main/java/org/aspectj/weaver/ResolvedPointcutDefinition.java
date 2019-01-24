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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.weaver.patterns.Pointcut;

public class ResolvedPointcutDefinition extends ResolvedMemberImpl {
	private Pointcut pointcut;

	public ResolvedPointcutDefinition(UnresolvedType declaringType, int modifiers, String name, UnresolvedType[] parameterTypes,
			Pointcut pointcut) {
		this(declaringType, modifiers, name, parameterTypes, UnresolvedType.VOID, pointcut);
	}

	/**
	 * An instance which can be given a specific returnType, used f.e. in if() pointcut for @AJ
	 * 
	 * @param declaringType
	 * @param modifiers
	 * @param name
	 * @param parameterTypes
	 * @param returnType
	 * @param pointcut
	 */
	public ResolvedPointcutDefinition(UnresolvedType declaringType, int modifiers, String name, UnresolvedType[] parameterTypes,
			UnresolvedType returnType, Pointcut pointcut) {
		super(POINTCUT, declaringType, modifiers, returnType, name, parameterTypes);
		this.pointcut = pointcut;
		// XXXpointcut.assertState(Pointcut.RESOLVED);
		checkedExceptions = UnresolvedType.NONE;
	}

	// ----

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		getDeclaringType().write(s);
		s.writeInt(getModifiers());
		s.writeUTF(getName());
		UnresolvedType.writeArray(getParameterTypes(), s);
		pointcut.write(s);
	}

	public static ResolvedPointcutDefinition read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		ResolvedPointcutDefinition rpd = new ResolvedPointcutDefinition(UnresolvedType.read(s), s.readInt(), s.readUTF(),
				UnresolvedType.readArray(s), Pointcut.read(s, context));
		rpd.setSourceContext(context); // whilst we have a source context, let's remember it
		return rpd;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("pointcut ");
		buf.append((getDeclaringType() == null ? "<nullDeclaringType>" : getDeclaringType().getName()));
		buf.append(".");
		buf.append(getName());
		buf.append("(");
		for (int i = 0; i < getParameterTypes().length; i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(getParameterTypes()[i].toString());
		}
		buf.append(")");
		// buf.append(pointcut);

		return buf.toString();
	}

	public Pointcut getPointcut() {
		return pointcut;
	}

	@Override
	public boolean isAjSynthetic() {
		return true;
	}

	/**
	 * Called when asking a parameterized super-aspect for its pointcuts.
	 */
	@Override
	public ResolvedMemberImpl parameterizedWith(UnresolvedType[] typeParameters, ResolvedType newDeclaringType,
			boolean isParameterized) {
		TypeVariable[] typeVariables = getDeclaringType().resolve(newDeclaringType.getWorld()).getTypeVariables();
		if (isParameterized && (typeVariables.length != typeParameters.length)) {
			throw new IllegalStateException("Wrong number of type parameters supplied");
		}
		Map typeMap = new HashMap();
		boolean typeParametersSupplied = typeParameters != null && typeParameters.length > 0;
		if (typeVariables != null) {
			// If no 'replacements' were supplied in the typeParameters array then collapse
			// type variables to their first bound.
			for (int i = 0; i < typeVariables.length; i++) {
				UnresolvedType ut = (!typeParametersSupplied ? typeVariables[i].getFirstBound() : typeParameters[i]);
				typeMap.put(typeVariables[i].getName(), ut);
			}
		}
		UnresolvedType parameterizedReturnType = parameterize(getGenericReturnType(), typeMap, isParameterized,
				newDeclaringType.getWorld());
		UnresolvedType[] parameterizedParameterTypes = new UnresolvedType[getGenericParameterTypes().length];
		for (int i = 0; i < parameterizedParameterTypes.length; i++) {
			parameterizedParameterTypes[i] = parameterize(getGenericParameterTypes()[i], typeMap, isParameterized,
					newDeclaringType.getWorld());
		}
		ResolvedPointcutDefinition ret = new ResolvedPointcutDefinition(newDeclaringType, getModifiers(), getName(),
				parameterizedParameterTypes, parameterizedReturnType, pointcut.parameterizeWith(typeMap,
						newDeclaringType.getWorld()));
		ret.setTypeVariables(getTypeVariables());
		ret.setSourceContext(getSourceContext());
		ret.setPosition(getStart(), getEnd());
		ret.setParameterNames(getParameterNames());
		return ret;
		// return this;
	}

	// for testing
	public static final ResolvedPointcutDefinition DUMMY = new ResolvedPointcutDefinition(UnresolvedType.OBJECT, 0, "missing",
			UnresolvedType.NONE, Pointcut.makeMatchesNothing(Pointcut.RESOLVED));

	public static final ResolvedPointcutDefinition[] NO_POINTCUTS = new ResolvedPointcutDefinition[] {};

	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

}
