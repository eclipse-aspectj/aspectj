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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.util.TypeSafeEnum;

/** This is an abstraction over method/field introduction.  It might not have the chops
 * to handle other inter-type declarations.  This is the thing that is used on the 
 * eclipse side and serialized into a ConcreteTypeMunger.
 */
public abstract class ResolvedTypeMunger {
	protected Kind kind;
	protected ResolvedMember signature;
	
	private Set /* resolvedMembers */ superMethodsCalled = Collections.EMPTY_SET;

	public ResolvedTypeMunger(Kind kind, ResolvedMember signature) {
		this.kind = kind;
		this.signature = signature;
	}

	// ----

    // fromType is guaranteed to be a non-abstract aspect
    public ConcreteTypeMunger concretize(World world, ResolvedTypeX aspectType) {
		ConcreteTypeMunger munger = world.concreteTypeMunger(this, aspectType);
        return munger;
    }
    
    
    public boolean matches(ResolvedTypeX matchType, ResolvedTypeX aspectType) {
    	ResolvedTypeX onType = matchType.getWorld().resolve(signature.getDeclaringType());
    	//System.err.println("matching: " + this + " to " + matchType + " onType = " + onType);
   		if (matchType.equals(onType)) { 
   			if (!onType.isExposedToWeaver()) {
   				if (!onType.isWovenBy(aspectType)) {
	   				if (matchType.getWorld().getLint().typeNotExposedToWeaver.isEnabled()) {
	   					matchType.getWorld().getLint().typeNotExposedToWeaver.signal(
	   						matchType.getName(), signature.getSourceLocation());
	   				}
   				}
   			}
   			return true;
   		}
   		//System.err.println("NO MATCH DIRECT");
   		
    	if (onType.isInterface()) {
    		return matchType.isTopmostImplementor(onType);
    	} else {
    		return false;
    	}
    }

	// ----

	public String toString() {
		return "ResolvedTypeMunger(" + getKind() + ", " + getSignature() +")";
		//.superMethodsCalled + ")";
	}

	// ----

	public static ResolvedTypeMunger read(DataInputStream s, ISourceContext context) throws IOException {
		Kind kind = Kind.read(s);
		if (kind == Field) {
			return NewFieldTypeMunger.readField(s, context);
		} else if (kind == Method) {
			return NewMethodTypeMunger.readMethod(s, context);

		} else if (kind == Constructor) {
			return NewConstructorTypeMunger.readConstructor(s, context);
		} else {
			throw new RuntimeException("unimplemented");
		}
	}

	protected static Set readSuperMethodsCalled(DataInputStream s) throws IOException {
		Set ret = new HashSet();
		int n = s.readInt();
		for (int i=0; i < n; i++) {
			ret.add(ResolvedMember.readResolvedMember(s, null));
		}
		return ret;
	}
	
	protected void writeSuperMethodsCalled(DataOutputStream s) throws IOException {
		if (superMethodsCalled == null) {
			s.writeInt(0);
			return;
		}
		
		List ret = new ArrayList(superMethodsCalled);
		Collections.sort(ret);
		int n = ret.size();
		s.writeInt(n);
		for (Iterator i = ret.iterator(); i.hasNext(); ) {
			ResolvedMember m = (ResolvedMember)i.next();
			m.write(s);
		}
	}

	
	public abstract void write(DataOutputStream s) throws IOException;

	public Kind getKind() {
		return kind;
	}

	
	
	public static class Kind extends TypeSafeEnum {
		private Kind(String name, int key) {
			super(name, key);
		}
		
	    public static Kind read(DataInputStream s) throws IOException {
	        int key = s.readByte();
	        switch(key) {
	            case 1: return Field;
	            case 2: return Method;
	            case 5: return Constructor;
	        }
	        throw new BCException("bad kind: " + key);
	    }
	}
	
	// ---- fields
	
	public static final Kind Field = new Kind("Field", 1);
	public static final Kind Method = new Kind("Method", 2);
	public static final Kind Constructor = new Kind("Constructor", 5);
	
	// not serialized, only created during concretization of aspects
	public static final Kind PerObjectInterface = new Kind("PerObjectInterface", 3);
	public static final Kind PrivilegedAccess = new Kind("PrivilegedAccess", 4);
	
	public static final Kind Parent = new Kind("Parent", 6);

	public static final String SUPER_DISPATCH_NAME = "superDispatch";


	public void setSuperMethodsCalled(Set c) {
		this.superMethodsCalled = c;
	}

	public Set getSuperMethodsCalled() {
		return superMethodsCalled;
	}
	

	public ResolvedMember getSignature() {
		return signature;
	}
	
	// ---- 

	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedTypeX aspectType) {
		if ((getSignature() != null) && getSignature().isPublic() && member.equals(getSignature())) { 
			return getSignature();
		}
			
		return null;
	}

}
