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

	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedTypeX aspectType) {
		ResolvedMember ret;
		if (getSignature().getKind() == Member.FIELD) {
			ret = AjcMemberMaker.privilegedAccessMethodForFieldGet(aspectType, getSignature());
			if (ResolvedTypeX.matches(ret, member)) return getSignature();
			ret = AjcMemberMaker.privilegedAccessMethodForFieldSet(aspectType, getSignature());
			if (ResolvedTypeX.matches(ret, member)) return getSignature();
		} else {
			//System.err.println("sig: " + getSignature());
			ret = AjcMemberMaker.privilegedAccessMethodForMethod(aspectType, getSignature());
			if (ResolvedTypeX.matches(ret, member)) return getSignature();
		}
		return null;
	}

}
