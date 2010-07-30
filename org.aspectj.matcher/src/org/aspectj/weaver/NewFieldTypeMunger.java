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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;

/**
 * Code that created version one style ITD type mungers will be using direct field access from the dispatchers
 * 
 * @author Andy
 * 
 */
public class NewFieldTypeMunger extends ResolvedTypeMunger {

	public static final int VersionOne = 1;
	public static final int VersionTwo = 2; // new style ITDs

	public int version = VersionOne;

	public NewFieldTypeMunger(ResolvedMember signature, Set superMethodsCalled, List typeVariableAliases) {
		super(Field, signature);
		this.version = VersionTwo;
		this.typeVariableAliases = typeVariableAliases;
		signature.setAnnotatedElsewhere(true);
		this.setSuperMethodsCalled(superMethodsCalled);
	}

	public ResolvedMember getInitMethod(UnresolvedType aspectType) {
		return AjcMemberMaker.interFieldInitializer(signature, aspectType);
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		kind.write(s);
		signature.write(s);
		writeSuperMethodsCalled(s);
		writeSourceLocation(s);
		writeOutTypeAliases(s);
		s.writeInt(version);
	}

	public static ResolvedTypeMunger readField(VersionedDataInputStream s, ISourceContext context) throws IOException {
		ISourceLocation sloc = null;
		ResolvedMember fieldSignature = ResolvedMemberImpl.readResolvedMember(s, context);
		Set superMethodsCalled = readSuperMethodsCalled(s);
		sloc = readSourceLocation(s);
		List aliases = readInTypeAliases(s);
		NewFieldTypeMunger munger = new NewFieldTypeMunger(fieldSignature, superMethodsCalled, aliases);
		if (sloc != null) {
			munger.setSourceLocation(sloc);
		}
		if (s.getMajorVersion() >= WeaverVersionInfo.WEAVER_VERSION_AJ169) {
			// there is a version int
			munger.version = s.readInt();
		} else {
			munger.version = VersionOne;
		}
		return munger;
	}

	public ResolvedMember getMatchingSyntheticMember(Member member, ResolvedType aspectType) {
		// ??? might give a field where a method is expected
		ResolvedType onType = aspectType.getWorld().resolve(getSignature().getDeclaringType());
		if (onType.isRawType()) {
			onType = onType.getGenericType();
		}

		ResolvedMember ret = AjcMemberMaker.interFieldGetDispatcher(getSignature(), aspectType);
		if (ResolvedType.matches(ret, member)) {
			return getSignature();
		}
		ret = AjcMemberMaker.interFieldSetDispatcher(getSignature(), aspectType);
		if (ResolvedType.matches(ret, member)) {
			return getSignature();
		}
		ret = AjcMemberMaker.interFieldInterfaceGetter(getSignature(), onType, aspectType);
		if (ResolvedType.matches(ret, member)) {
			return getSignature();
		}
		ret = AjcMemberMaker.interFieldInterfaceSetter(getSignature(), onType, aspectType);
		if (ResolvedType.matches(ret, member)) {
			return getSignature();
		}
		return super.getMatchingSyntheticMember(member, aspectType);
	}

	/**
	 * see ResolvedTypeMunger.parameterizedFor(ResolvedType)
	 */
	public ResolvedTypeMunger parameterizedFor(ResolvedType target) {
		ResolvedType genericType = target;
		if (target.isRawType() || target.isParameterizedType()) {
			genericType = genericType.getGenericType();
		}
		ResolvedMember parameterizedSignature = null;
		// If we are parameterizing it for a generic type, we just need to 'swap the letters' from the ones used
		// in the original ITD declaration to the ones used in the actual target type declaration.
		if (target.isGenericType()) {
			TypeVariable vars[] = target.getTypeVariables();
			UnresolvedTypeVariableReferenceType[] varRefs = new UnresolvedTypeVariableReferenceType[vars.length];
			for (int i = 0; i < vars.length; i++) {
				varRefs[i] = new UnresolvedTypeVariableReferenceType(vars[i]);
			}
			parameterizedSignature = getSignature().parameterizedWith(varRefs, genericType, true, typeVariableAliases);
		} else {
			// For raw and 'normal' parameterized targets (e.g. Interface, Interface<String>)
			parameterizedSignature = getSignature().parameterizedWith(target.getTypeParameters(), genericType,
					target.isParameterizedType(), typeVariableAliases);
		}
		NewFieldTypeMunger nftm = new NewFieldTypeMunger(parameterizedSignature, getSuperMethodsCalled(), typeVariableAliases);
		nftm.setDeclaredSignature(getSignature());
		nftm.setSourceLocation(getSourceLocation());
		return nftm;
	}

	public ResolvedTypeMunger parameterizeWith(Map<String, UnresolvedType> m, World w) {
		ResolvedMember parameterizedSignature = getSignature().parameterizedWith(m, w);
		NewFieldTypeMunger nftm = new NewFieldTypeMunger(parameterizedSignature, getSuperMethodsCalled(), typeVariableAliases);
		nftm.setDeclaredSignature(getSignature());
		nftm.setSourceLocation(getSourceLocation());
		return nftm;
	}

	public boolean equals(Object other) {
		if (!(other instanceof NewFieldTypeMunger)) {
			return false;
		}
		NewFieldTypeMunger o = (NewFieldTypeMunger) other;
		return ((kind == null) ? (o.kind == null) : kind.equals(o.kind))
				&& ((signature == null) ? (o.signature == null) : signature.equals(o.signature))
				&& ((declaredSignature == null) ? (o.declaredSignature == null) : declaredSignature.equals(o.declaredSignature))
				&& ((typeVariableAliases == null) ? (o.typeVariableAliases == null) : typeVariableAliases
						.equals(o.typeVariableAliases));
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + kind.hashCode();
		result = 37 * result + ((signature == null) ? 0 : signature.hashCode());
		result = 37 * result + ((declaredSignature == null) ? 0 : declaredSignature.hashCode());
		result = 37 * result + ((typeVariableAliases == null) ? 0 : typeVariableAliases.hashCode());
		return result;
	}

}
