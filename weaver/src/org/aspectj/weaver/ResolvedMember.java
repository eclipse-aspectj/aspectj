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
import java.lang.reflect.Modifier;

import org.aspectj.bridge.ISourceLocation;

/**
 * This is the declared member, i.e. it will always correspond to an
 * actual method/... declaration
 */
public class ResolvedMember extends Member implements IHasPosition {
    
    protected String[] parameterNames = null;
    protected TypeX[] checkedExceptions = TypeX.NONE;
    
    
    // these three fields hold the source location of this member
	protected int start, end;
	protected ISourceContext sourceContext = null;
    
    // ----
    
	public ResolvedMember(
		Kind kind,
		TypeX declaringType,
		int modifiers,
		TypeX returnType,
		String name,
		TypeX[] parameterTypes) 
	{
		super(kind, declaringType, modifiers, returnType, name, parameterTypes);
	}
    
	public ResolvedMember(
		Kind kind,
		TypeX declaringType,
		int modifiers,
		String name,
		String signature) 
	{
		super(kind, declaringType, modifiers, name, signature);
	}    

    public static final ResolvedMember[] NONE = new ResolvedMember[0];

	// ----

    public final int getModifiers(World world) {
        return modifiers;
    }
    public final int getModifiers() {
        return modifiers;
    }

	// ----
	

    public final TypeX[] getExceptions(World world) {
        return getExceptions();
    }
    
    public TypeX[] getExceptions() {
        return checkedExceptions;
    }
    
    public ShadowMunger getAssociatedShadowMunger() {
		return null;
    }
    
    // ??? true or false?
    public boolean isAjSynthetic() {
    	return true;
    }
    
    public void write(DataOutputStream s) throws IOException {
    	getKind().write(s);
    	getDeclaringType().write(s);
    	s.writeInt(modifiers);
    	s.writeUTF(getName());
    	s.writeUTF(getSignature());
		TypeX.writeArray(getExceptions(), s);

		s.writeInt(getStart());
		s.writeInt(getEnd());

    }

    public static void writeArray(ResolvedMember[] members, DataOutputStream s) throws IOException {
		s.writeInt(members.length);
		for (int i = 0, len = members.length; i < len; i++) {
			members[i].write(s);
		}
    }

    
    public static ResolvedMember readResolvedMember(DataInputStream s, ISourceContext sourceContext) throws IOException {
    	ResolvedMember m = new ResolvedMember(Kind.read(s), TypeX.read(s), s.readInt(), s.readUTF(), s.readUTF());
		m.checkedExceptions = TypeX.readArray(s);
		m.start = s.readInt();
		m.end = s.readInt();
		m.sourceContext = sourceContext;
		return m;
    }
    
    public static ResolvedMember[] readResolvedMemberArray(DataInputStream s, ISourceContext context) throws IOException {
    	int len = s.readInt();
		ResolvedMember[] members = new ResolvedMember[len];
		for (int i=0; i < len; i++) {
			members[i] = ResolvedMember.readResolvedMember(s, context);
		}
		return members;
    }
    
    
    
	public ResolvedMember resolve(World world) {
		return this;
	}
	
	public ISourceContext getSourceContext(World world) {
		return getDeclaringType().resolve(world).getSourceContext();
	}

	public final String[] getParameterNames() {
		return parameterNames;
	}
	public final String[] getParameterNames(World world) {
		return getParameterNames();
	}
	
	public AjAttribute.EffectiveSignatureAttribute getEffectiveSignature() {
		return null;
	}
	
    public ISourceLocation getSourceLocation() {
    	//System.out.println("get context: " + this + " is " + sourceContext);
    	if (sourceContext == null) {
    		//System.err.println("no context: " + this);
    		return null;
    	}
    	return sourceContext.makeSourceLocation(this);
    }
    
	public int getEnd() {
		return end;
	}

	public ISourceContext getSourceContext() {
		return sourceContext;
	}

	public int getStart() {
		return start;
	}

	public void setPosition(int sourceStart, int sourceEnd) {
		this.start = sourceStart;
		this.end = sourceEnd;
	}

	public void setSourceContext(ISourceContext sourceContext) {
		this.sourceContext = sourceContext;
	}
	
	public boolean isAbstract() {
		return Modifier.isAbstract(modifiers);
	}

	public boolean isPublic() {
		return Modifier.isPublic(modifiers);
	}

	public boolean isVisible(ResolvedTypeX fromType) {
		World world = fromType.getWorld();
		return ResolvedTypeX.isVisible(getModifiers(), getDeclaringType().resolve(world),
					fromType);
	}
	public void setCheckedExceptions(TypeX[] checkedExceptions) {
		this.checkedExceptions = checkedExceptions;
	}

}
   
