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

import org.aspectj.bridge.IMessage;

public class NewConstructorTypeMunger extends ResolvedTypeMunger {
	private ResolvedMember syntheticConstructor;
	private ResolvedMember explicitConstructor;


	public NewConstructorTypeMunger(
		ResolvedMember signature,
		ResolvedMember syntheticConstructor,
		ResolvedMember explicitConstructor,
		Set superMethodsCalled)
	{
		super(Constructor, signature);
		this.syntheticConstructor = syntheticConstructor;
		this.explicitConstructor = explicitConstructor;
		this.setSuperMethodsCalled(superMethodsCalled);

	}
	
	//XXX horrible name clash here
	public ResolvedMember getDispatchMethod(TypeX aspectType) {
		return AjcMemberMaker.interMethodBody(signature, aspectType);
	}

	public void write(DataOutputStream s) throws IOException {
		kind.write(s);
		signature.write(s);
		syntheticConstructor.write(s);
		explicitConstructor.write(s);
		writeSuperMethodsCalled(s);
	}
	
	public static ResolvedTypeMunger readConstructor(DataInputStream s, ISourceContext context) throws IOException {
		return new NewConstructorTypeMunger(
				ResolvedMember.readResolvedMember(s, context),
				ResolvedMember.readResolvedMember(s, context),
				ResolvedMember.readResolvedMember(s, context),
				readSuperMethodsCalled(s));
	}

	public ResolvedMember getExplicitConstructor() {
		return explicitConstructor;
	}

	public ResolvedMember getSyntheticConstructor() {
		return syntheticConstructor;
	}

	public void setExplicitConstructor(ResolvedMember explicitConstructor) {
		this.explicitConstructor = explicitConstructor;
	}
	
	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedTypeX aspectType) {
		ResolvedMember ret = getSyntheticConstructor();
		if (ResolvedTypeX.matches(ret, member)) return getSignature();
		return super.getMatchingSyntheticMember(member, aspectType);
	}
	
	public void check(World world) {
		if (getSignature().getDeclaringType().isAspect(world)) {
			world.showMessage(IMessage.ERROR, "can't declare constructor on an aspect",
						getSignature().getSourceLocation(), null);
		}
	}

}
