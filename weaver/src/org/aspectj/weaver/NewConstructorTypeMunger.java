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
import java.util.List;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;

public class NewConstructorTypeMunger extends ResolvedTypeMunger {
	private ResolvedMember syntheticConstructor;
	private ResolvedMember explicitConstructor;


	public NewConstructorTypeMunger(
		ResolvedMember signature,
		ResolvedMember syntheticConstructor,
		ResolvedMember explicitConstructor,
		Set superMethodsCalled,
		List typeVariableAliases) {
		super(Constructor, signature);
		this.syntheticConstructor = syntheticConstructor;
		this.typeVariableAliases = typeVariableAliases;
		this.explicitConstructor = explicitConstructor;
		this.setSuperMethodsCalled(superMethodsCalled);

	}
	
	// doesnt seem required....
//	public ResolvedMember getDispatchMethod(UnresolvedType aspectType) {
//		return AjcMemberMaker.interMethodBody(signature, aspectType);
//	}

	public void write(DataOutputStream s) throws IOException {
		kind.write(s);
		signature.write(s);
		syntheticConstructor.write(s);
		explicitConstructor.write(s);
		writeSuperMethodsCalled(s);
		writeSourceLocation(s);
		writeOutTypeAliases(s);
	}
	
	public static ResolvedTypeMunger readConstructor(VersionedDataInputStream s, ISourceContext context) throws IOException {
		ISourceLocation sloc = null;
		ResolvedMember sig           = ResolvedMemberImpl.readResolvedMember(s, context);
		ResolvedMember syntheticCtor = ResolvedMemberImpl.readResolvedMember(s, context);
		ResolvedMember explicitCtor  = ResolvedMemberImpl.readResolvedMember(s, context);
		Set superMethodsCalled       = readSuperMethodsCalled(s);
		sloc                         = readSourceLocation(s);
		List typeVarAliases          = readInTypeAliases(s);
		ResolvedTypeMunger munger = new NewConstructorTypeMunger(sig,syntheticCtor,explicitCtor,superMethodsCalled,typeVarAliases);
		if (sloc!=null) munger.setSourceLocation(sloc);
		return munger;
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
	
	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedType aspectType) {
		ResolvedMember ret = getSyntheticConstructor();
		if (ResolvedType.matches(ret, member)) return getSignature();
		return super.getMatchingSyntheticMember(member, aspectType);
	}
	
	public void check(World world) {
		if (getSignature().getDeclaringType().resolve(world).isAspect()) {
			world.showMessage(IMessage.ERROR, 
					WeaverMessages.format(WeaverMessages.ITD_CONS_ON_ASPECT),
					getSignature().getSourceLocation(), null);
		}
	}

}
