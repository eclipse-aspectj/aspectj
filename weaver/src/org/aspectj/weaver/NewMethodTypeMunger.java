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
import java.util.Set;

public class NewMethodTypeMunger extends ResolvedTypeMunger {
	public NewMethodTypeMunger(
		ResolvedMember signature,
		Set superMethodsCalled)
	{
		super(Method, signature);
		this.setSuperMethodsCalled(superMethodsCalled);

	}
	
	//XXX horrible name clash here
	public ResolvedMember getDispatchMethod(TypeX aspectType) {
		return AjcMemberMaker.interMethodBody(signature, aspectType);
	}

	public void write(DataOutputStream s) throws IOException {
		kind.write(s);
		signature.write(s);
		writeSuperMethodsCalled(s);
	}
	
	public static ResolvedTypeMunger readMethod(DataInputStream s, ISourceContext context) throws IOException {
		return new NewMethodTypeMunger(
				ResolvedMember.readResolvedMember(s, context),
				readSuperMethodsCalled(s));
	}
	
	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedTypeX aspectType) {	
		ResolvedMember ret = AjcMemberMaker.interMethodDispatcher(getSignature(), aspectType);
		if (ResolvedTypeX.matches(ret, member)) return getSignature();
		return super.getMatchingSyntheticMember(member, aspectType);
	}
}
