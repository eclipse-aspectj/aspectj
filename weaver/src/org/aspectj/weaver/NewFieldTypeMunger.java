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

public class NewFieldTypeMunger extends ResolvedTypeMunger {
	public NewFieldTypeMunger(ResolvedMember signature, Set superMethodsCalled) {
		super(Field, signature);
		this.setSuperMethodsCalled(superMethodsCalled);
	}

	public ResolvedMember getInitMethod(TypeX aspectType) {
		return AjcMemberMaker.interFieldInitializer(signature, aspectType);
	}

	public void write(DataOutputStream s) throws IOException {
		kind.write(s);
		signature.write(s);
		writeSuperMethodsCalled(s);
	}

	public static ResolvedTypeMunger readField(DataInputStream s, ISourceContext context) throws IOException {
		return new NewFieldTypeMunger(
			ResolvedMember.readResolvedMember(s, context),
			readSuperMethodsCalled(s));
	}
	
	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedTypeX aspectType) {
		//??? might give a field where a method is expected	
		ResolvedTypeX onType = aspectType.getWorld().resolve(getSignature().getDeclaringType());
		
		ResolvedMember ret = AjcMemberMaker.interFieldGetDispatcher(getSignature(), aspectType);
		if (ResolvedTypeX.matches(ret, member)) return getSignature();
		ret = AjcMemberMaker.interFieldSetDispatcher(getSignature(), aspectType);
		if (ResolvedTypeX.matches(ret, member)) return getSignature();
		ret = AjcMemberMaker.interFieldInterfaceGetter(getSignature(), onType, aspectType);
		if (ResolvedTypeX.matches(ret, member)) return getSignature();
		ret = AjcMemberMaker.interFieldInterfaceSetter(getSignature(), onType, aspectType);
		if (ResolvedTypeX.matches(ret, member)) return getSignature();
		return super.getMatchingSyntheticMember(member, aspectType);
	}
}
