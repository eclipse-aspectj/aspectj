/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Signature.SimpleClassTypeSignature;
import org.aspectj.weaver.BoundedReferenceType;
import org.aspectj.weaver.GenericsWildcardTypeX;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.TypeVariableReferenceType;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;

/**
 * A utility class that assists in unpacking constituent parts of 
 * generic signature attributes and returning their equivalents in
 * TypeX world.
 */
public class BcelGenericSignatureToTypeXConverter {

	public static ResolvedTypeX classTypeSignature2TypeX(
			Signature.ClassTypeSignature aClassTypeSignature,
			Signature.FormalTypeParameter[] typeParams,
			World world) {
		// class type sig consists of an outer type, and zero or more nested types
		// the fully qualified name is outer-type.nested-type1.nested-type2....
		// each type in the hierarchy may have type arguments
		
		// first build the 'raw type' signature
		StringBuffer sig = new StringBuffer();
		sig.append(aClassTypeSignature.outerType.identifier.replace(';',' ').trim());
		for (int i = 0; i < aClassTypeSignature.nestedTypes.length; i++) {
			sig.append("/"); 
			sig.append(aClassTypeSignature.nestedTypes[i].identifier.replace(';',' ').trim());
		}
		sig.append(";");
		
		// now look for any type parameters.
		// I *think* we only need to worry about the 'right-most' type...
		SimpleClassTypeSignature innerType = aClassTypeSignature.outerType;
		if (aClassTypeSignature.nestedTypes.length > 0) {
			innerType = aClassTypeSignature.nestedTypes[aClassTypeSignature.nestedTypes.length-1];
		}
		if (innerType.typeArguments.length > 0) {
			// we have to create a parameterized type
			// type arguments may be array types, class types, or typevariable types
			TypeX[] typeArgumentTypes = new TypeX[innerType.typeArguments.length];
			for (int i = 0; i < typeArgumentTypes.length; i++) {
				typeArgumentTypes[i] = typeArgument2TypeX(innerType.typeArguments[i],typeParams,world);
			}
			return world.resolve(TypeX.forParameterizedTypes(TypeX.forSignature(sig.toString()), typeArgumentTypes));
		} else {
			// we have a non-parameterized type
			return world.resolve(TypeX.forSignature(sig.toString()));
		}
	}
	
	public static ResolvedTypeX fieldTypeSignature2TypeX(
			Signature.FieldTypeSignature aFieldTypeSignature,
			Signature.FormalTypeParameter[] typeParams,
			World world) {
		if (aFieldTypeSignature.isClassTypeSignature()) {
			return classTypeSignature2TypeX((Signature.ClassTypeSignature)aFieldTypeSignature,typeParams,world);
		} else if (aFieldTypeSignature.isArrayTypeSignature()) {
			int dims = 0;
			Signature.TypeSignature ats =  aFieldTypeSignature;
			while(ats instanceof Signature.ArrayTypeSignature) {
				dims++;
				ats = ((Signature.ArrayTypeSignature)ats).typeSig;
			}
			return world.resolve(TypeX.makeArray(typeSignature2TypeX(ats,typeParams,world), dims));
		} else if (aFieldTypeSignature.isTypeVariableSignature()) {
			return typeVariableSignature2TypeX((Signature.TypeVariableSignature)aFieldTypeSignature,typeParams,world);
		} else {
			throw new IllegalStateException("Cant understand field type signature: "  + aFieldTypeSignature);
		}
	}
	
	public static TypeVariable formalTypeParameter2TypeVariable(
			Signature.FormalTypeParameter aFormalTypeParameter,
			Signature.FormalTypeParameter[] typeParams,
			World world) {
			TypeX upperBound = fieldTypeSignature2TypeX(aFormalTypeParameter.classBound,typeParams,world);
			TypeX[] ifBounds = new TypeX[aFormalTypeParameter.interfaceBounds.length];
			for (int i = 0; i < ifBounds.length; i++) {
				ifBounds[i] = fieldTypeSignature2TypeX(aFormalTypeParameter.interfaceBounds[i], typeParams,world);
			}
			return new TypeVariable(aFormalTypeParameter.identifier,upperBound,ifBounds);
	}
	
	private static ResolvedTypeX typeArgument2TypeX(
			Signature.TypeArgument aTypeArgument,
			Signature.FormalTypeParameter[] typeParams,
			World world) {
		if (aTypeArgument.isWildcard) return GenericsWildcardTypeX.GENERIC_WILDCARD.resolve(world);
		if (aTypeArgument.isMinus) {
			TypeX bound = fieldTypeSignature2TypeX(aTypeArgument.signature, typeParams,world);
			ReferenceType rBound = (ReferenceType) world.resolve(bound);
			return new BoundedReferenceType(rBound,false,world);
		} else if (aTypeArgument.isPlus) {
			TypeX bound = fieldTypeSignature2TypeX(aTypeArgument.signature, typeParams,world);
			ReferenceType rBound = (ReferenceType) world.resolve(bound);
			return new BoundedReferenceType(rBound,true,world);
		} else {
			return fieldTypeSignature2TypeX(aTypeArgument.signature,typeParams,world);
		}
	}
	
	public static ResolvedTypeX typeSignature2TypeX(
			Signature.TypeSignature aTypeSig,
			Signature.FormalTypeParameter[] typeParams,
			World world) {
		if (aTypeSig.isBaseType()) {
			return world.resolve(TypeX.forSignature(((Signature.BaseTypeSignature)aTypeSig).toString()));
		} else {
			return fieldTypeSignature2TypeX((Signature.FieldTypeSignature)aTypeSig,typeParams,world);
		}
	}
	
	private static ResolvedTypeX typeVariableSignature2TypeX(
			Signature.TypeVariableSignature aTypeVarSig,
			Signature.FormalTypeParameter[] typeParams,
			World world) {
		Signature.FormalTypeParameter typeVarBounds = null;
		for (int i = 0; i < typeParams.length; i++) {
			if (typeParams[i].identifier.equals(aTypeVarSig.typeVariableName)) {
				typeVarBounds = typeParams[i];
				break;
			}
		}
		if (typeVarBounds == null) {
			throw new IllegalStateException("Undeclared type variable in signature: " + aTypeVarSig.typeVariableName);
		}
		return new TypeVariableReferenceType(
				formalTypeParameter2TypeVariable(typeVarBounds,typeParams,world),
				world);
	}
	
	
}
