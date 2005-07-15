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

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.weaver.patterns.Pointcut;


public class ResolvedPointcutDefinition extends ResolvedMember {
	private Pointcut pointcut;

    public ResolvedPointcutDefinition(
        UnresolvedType declaringType,
        int modifiers,
        String name,
        UnresolvedType[] parameterTypes,
        Pointcut pointcut)
    {
        this(declaringType, modifiers, name, parameterTypes, ResolvedType.VOID, pointcut);
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
    public ResolvedPointcutDefinition(
		UnresolvedType declaringType,
		int modifiers,
		String name,
		UnresolvedType[] parameterTypes,
        UnresolvedType returnType,
		Pointcut pointcut)
    {
		super(
			POINTCUT,
			declaringType,
			modifiers,
			returnType,
			name,
			parameterTypes);
		this.pointcut = pointcut;
		//XXXpointcut.assertState(Pointcut.RESOLVED);
		checkedExceptions = UnresolvedType.NONE;
	}
	
	// ----

	public void write(DataOutputStream s) throws IOException {
		getDeclaringType().write(s);
		s.writeInt(getModifiers());
		s.writeUTF(getName());
		UnresolvedType.writeArray(getParameterTypes(), s);
		pointcut.write(s);
	}
	
	public static ResolvedPointcutDefinition read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		ResolvedPointcutDefinition rpd =
		  new ResolvedPointcutDefinition(
			UnresolvedType.read(s),
			s.readInt(),
			s.readUTF(),
			UnresolvedType.readArray(s),
			Pointcut.read(s, context));
		rpd.setSourceContext(context); // whilst we have a source context, let's remember it
		return rpd;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("pointcut ");
		buf.append(getDeclaringType().getName());
		buf.append(".");
		buf.append(getName());
		buf.append("(");
		for (int i=0; i < getParameterTypes().length; i++) {
			if (i > 0) buf.append(", ");
			buf.append(getParameterTypes()[i].toString());
		}
		buf.append(")");
		//buf.append(pointcut);
		
		return buf.toString();
	}
	
	public Pointcut getPointcut() {
		return pointcut;
	}
	
	public boolean isAjSynthetic() {
		return true;
	}
	
	// for testing
	public static final ResolvedPointcutDefinition DUMMY =
	    new ResolvedPointcutDefinition(UnresolvedType.OBJECT, 0, "missing", 
	    				UnresolvedType.NONE, Pointcut.makeMatchesNothing(Pointcut.RESOLVED));

	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

}
