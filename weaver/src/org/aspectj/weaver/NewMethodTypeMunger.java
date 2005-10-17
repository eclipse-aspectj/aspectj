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

import org.aspectj.bridge.ISourceLocation;

public class NewMethodTypeMunger extends ResolvedTypeMunger {
	public NewMethodTypeMunger(
		ResolvedMember signature,
		Set superMethodsCalled,
		List typeVariableAliases) {
		super(Method, signature);
		this.typeVariableAliases = typeVariableAliases;
		this.setSuperMethodsCalled(superMethodsCalled);
	}
	
	public ResolvedMember getInterMethodBody(UnresolvedType aspectType) {
		return AjcMemberMaker.interMethodBody(signature, aspectType);
	}
	
	public ResolvedMember getInterMethodDispatcher(UnresolvedType aspectType) {
		return AjcMemberMaker.interMethodDispatcher(signature, aspectType);
	}

	public void write(DataOutputStream s) throws IOException {
		kind.write(s);
		signature.write(s);
		writeSuperMethodsCalled(s);
		writeSourceLocation(s);
		writeOutTypeAliases(s);
	}
	
	public static ResolvedTypeMunger readMethod(VersionedDataInputStream s, ISourceContext context) throws IOException {
		
		ISourceLocation sloc = null;		
		ResolvedMemberImpl rmImpl = ResolvedMemberImpl.readResolvedMember(s, context);
		Set superMethodsCalled = readSuperMethodsCalled(s);
		sloc = readSourceLocation(s);
		List typeVarAliases = readInTypeAliases(s);
		
		ResolvedTypeMunger munger = new NewMethodTypeMunger(rmImpl,superMethodsCalled,typeVarAliases);
		if (sloc!=null) munger.setSourceLocation(sloc);
		return munger;
	}
	
	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedType aspectType) {	
		ResolvedMember ret = AjcMemberMaker.interMethodDispatcher(getSignature(), aspectType);
		if (ResolvedType.matches(ret, member)) return getSignature();
		return super.getMatchingSyntheticMember(member, aspectType);
	}
	
	/**
     * ResolvedTypeMunger.parameterizedFor(ResolvedType)
     */
	public ResolvedTypeMunger parameterizedFor(ResolvedType target) {
		ResolvedType genericType = target;
		if (target.isRawType() || target.isParameterizedType()) genericType = genericType.getGenericType();
		ResolvedMember parameterizedSignature = getSignature().parameterizedWith(target.getTypeParameters(),genericType,target.isParameterizedType());
		return new NewMethodTypeMunger(parameterizedSignature,getSuperMethodsCalled(),typeVariableAliases);
	}

}
