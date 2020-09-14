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

import java.io.IOException;

/**
 * A privileged access munger is for handling privileged access to a member. It determines the names of the getter/setter that will
 * be used to access a private field in some type, or the special method that provides access to a private method.
 * 
 * There are two syntax styles for field access, the older style was in use up to AspectJ 1.6.9 and involves long named getters and
 * setters which include the requesting aspect and the target type. The short style syntax is use from AspectJ 1.6.9 onwards is
 * simply 'ajc$get$&lt;fieldname&gt;' and 'ajc$set$&lt;fieldname&gt;' - as the requesting aspect isn't included in the name they can be shared
 * across aspects.
 */
public class PrivilegedAccessMunger extends ResolvedTypeMunger {

	public boolean shortSyntax = false;

	public PrivilegedAccessMunger(ResolvedMember member, boolean shortSyntax) {
		super(PrivilegedAccess, member);
		this.shortSyntax = shortSyntax;
	}

	@Override
	public void write(CompressingDataOutputStream s) throws IOException {
		throw new RuntimeException("should not be serialized");
	}

	public ResolvedMember getMember() {
		return getSignature();
	}

	@Override
	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedType aspectType) {
		ResolvedMember ret;
		// assert if shortSyntax then aspectType.getCompilerVersion()>=169
		if (getSignature().getKind() == Member.FIELD) {
			ret = AjcMemberMaker.privilegedAccessMethodForFieldGet(aspectType, getSignature(), shortSyntax);
			if (ResolvedType.matches(ret, member)) {
				return getSignature();
			}
			ret = AjcMemberMaker.privilegedAccessMethodForFieldSet(aspectType, getSignature(), shortSyntax);
			if (ResolvedType.matches(ret, member)) {
				return getSignature();
			}
		} else {
			// System.err.println("sig: " + getSignature());
			ret = AjcMemberMaker.privilegedAccessMethodForMethod(aspectType, getSignature());
			if (ResolvedType.matches(ret, member)) {
				return getSignature();
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof PrivilegedAccessMunger)) {
			return false;
		}
		PrivilegedAccessMunger o = (PrivilegedAccessMunger) other;
		return kind.equals(o.kind)
				&& ((o.signature == null) ? (signature == null) : signature.equals(o.signature))
				&& ((o.declaredSignature == null) ? (declaredSignature == null) : declaredSignature.equals(o.declaredSignature))
				&& ((o.typeVariableAliases == null) ? (typeVariableAliases == null) : typeVariableAliases
						.equals(o.typeVariableAliases));
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + kind.hashCode();
		result = 37 * result + ((signature == null) ? 0 : signature.hashCode());
		result = 37 * result + ((declaredSignature == null) ? 0 : declaredSignature.hashCode());
		result = 37 * result + ((typeVariableAliases == null) ? 0 : typeVariableAliases.hashCode());
		return result;
	}

	@Override
	public boolean existsToSupportShadowMunging() {
		return true;
	}
}
