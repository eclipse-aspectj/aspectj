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

import java.io.DataOutputStream;
import java.io.IOException;

public class PrivilegedAccessMunger extends ResolvedTypeMunger {
	public PrivilegedAccessMunger(ResolvedMember member) {
		super(PrivilegedAccess, member);
	}
	

	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("shouldn't be serialized");
	}

	public ResolvedMember getMember() {
		return getSignature();
	}

	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedType aspectType) {
		ResolvedMember ret;
		if (getSignature().getKind() == Member.FIELD) {
			ret = AjcMemberMaker.privilegedAccessMethodForFieldGet(aspectType, getSignature());
			if (ResolvedType.matches(ret, member)) return getSignature();
			ret = AjcMemberMaker.privilegedAccessMethodForFieldSet(aspectType, getSignature());
			if (ResolvedType.matches(ret, member)) return getSignature();
		} else {
			//System.err.println("sig: " + getSignature());
			ret = AjcMemberMaker.privilegedAccessMethodForMethod(aspectType, getSignature());
			if (ResolvedType.matches(ret, member)) return getSignature();
		}
		return null;
	}

    public boolean equals(Object other) {
        if (! (other instanceof PrivilegedAccessMunger)) return false;
        PrivilegedAccessMunger o = (PrivilegedAccessMunger) other;
        return kind.equals(o.kind)
        		&& ((o.signature == null) ? (signature == null ) : signature.equals(o.signature))
        		&& ((o.declaredSignature == null) ? (declaredSignature == null ) : declaredSignature.equals(o.declaredSignature))
        		&& ((o.typeVariableAliases == null) ? (typeVariableAliases == null ) : typeVariableAliases.equals(o.typeVariableAliases));
    }
	   
    public int hashCode() {
    	int result = 17;
        result = 37*result + kind.hashCode();
        result = 37*result + ((signature == null) ? 0 : signature.hashCode());
        result = 37*result + ((declaredSignature == null) ? 0 : declaredSignature.hashCode());
        result = 37*result + ((typeVariableAliases == null) ? 0 : typeVariableAliases.hashCode());
        return result;
    }
	
 	public boolean existsToSupportShadowMunging() {
		return true;
	}
}
