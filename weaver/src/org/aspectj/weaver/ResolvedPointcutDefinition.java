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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.weaver.patterns.Pointcut;


public class ResolvedPointcutDefinition extends ResolvedMember {
	private Pointcut pointcut;
	
	public ResolvedPointcutDefinition(
		TypeX declaringType,
		int modifiers,
		String name,
		TypeX[] parameterTypes,
		Pointcut pointcut) 
    {
		super(
			POINTCUT,
			declaringType,
			modifiers,
			ResolvedTypeX.VOID,
			name,
			parameterTypes);
		this.pointcut = pointcut;
		//XXXpointcut.assertState(Pointcut.RESOLVED);
		checkedExceptions = TypeX.NONE;
	}
	
	// ----

	public void write(DataOutputStream s) throws IOException {
		getDeclaringType().write(s);
		s.writeInt(getModifiers());
		s.writeUTF(getName());
		TypeX.writeArray(getParameterTypes(), s);
		pointcut.write(s);
	}
	
	public static ResolvedPointcutDefinition read(DataInputStream s, ISourceContext context) throws IOException {
		return new ResolvedPointcutDefinition(
			TypeX.read(s),
			s.readInt(),
			s.readUTF(),
			TypeX.readArray(s),
			Pointcut.read(s, context));
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
	    new ResolvedPointcutDefinition(TypeX.OBJECT, 0, "missing", 
	    				TypeX.NONE, Pointcut.makeMatchesNothing(Pointcut.RESOLVED));

}
