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

import java.util.HashMap;
import java.util.Map;

import org.aspectj.util.GenericSignature;
import org.aspectj.util.GenericSignature.SimpleClassTypeSignature;
import org.aspectj.weaver.BoundedReferenceType;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeFactory;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.TypeVariableReferenceType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

/**
 * A utility class that assists in unpacking constituent parts of generic signature attributes and returning their equivalents in
 * UnresolvedType world.
 */
public class BcelGenericSignatureToTypeXConverter {

	private static Trace trace = TraceFactory.getTraceFactory().getTrace(BcelGenericSignatureToTypeXConverter.class);

	public static ResolvedType classTypeSignature2TypeX(GenericSignature.ClassTypeSignature aClassTypeSignature,
			GenericSignature.FormalTypeParameter[] typeParams, World world) throws GenericSignatureFormatException {
		Map<GenericSignature.FormalTypeParameter, ReferenceType> typeMap = new HashMap<>();
		ResolvedType ret = classTypeSignature2TypeX(aClassTypeSignature, typeParams, world, typeMap);
		fixUpCircularDependencies(ret, typeMap);
		return ret;
	}

	private static ResolvedType classTypeSignature2TypeX(GenericSignature.ClassTypeSignature aClassTypeSignature,
			GenericSignature.FormalTypeParameter[] typeParams, World world,
			Map<GenericSignature.FormalTypeParameter, ReferenceType> inProgressTypeVariableResolutions)
			throws GenericSignatureFormatException {
		// class type sig consists of an outer type, and zero or more nested types
		// the fully qualified name is outer-type.nested-type1.nested-type2....
		// each type in the hierarchy may have type arguments

		// first build the 'raw type' signature
		StringBuffer sig = new StringBuffer();
		sig.append(aClassTypeSignature.outerType.identifier.replace(';', ' ').trim());
		for (int i = 0; i < aClassTypeSignature.nestedTypes.length; i++) {
			sig.append("$");
			sig.append(aClassTypeSignature.nestedTypes[i].identifier.replace(';', ' ').trim());
		}
		sig.append(";");

		// now look for any type parameters.
		// I *think* we only need to worry about the 'right-most' type...
		SimpleClassTypeSignature innerType = aClassTypeSignature.outerType;
		if (aClassTypeSignature.nestedTypes.length > 0) {
			innerType = aClassTypeSignature.nestedTypes[aClassTypeSignature.nestedTypes.length - 1];
		}
		if (innerType.typeArguments.length > 0) {
			// we have to create a parameterized type
			// type arguments may be array types, class types, or typevariable types
			ResolvedType theBaseType = UnresolvedType.forSignature(sig.toString()).resolve(world);

			// Sometimes we may find that when the code is being load-time woven that the types have changed.
			// Perhaps an old form of a library jar is being used - this can mean we discover right here
			// that a type is not parameterizable (is that a word?). I think in these cases it is ok to
			// just return with what we know (the base type). (see pr152848)
			if (!(theBaseType.isGenericType() || theBaseType.isRawType())) {
				if (trace.isTraceEnabled()) {
					trace.event("classTypeSignature2TypeX: this type is not a generic type:", null, new Object[] { theBaseType });
				}
				return theBaseType;
			}

			ResolvedType[] typeArgumentTypes = new ResolvedType[innerType.typeArguments.length];
			for (int i = 0; i < typeArgumentTypes.length; i++) {
				typeArgumentTypes[i] = typeArgument2TypeX(innerType.typeArguments[i], typeParams, world,
						inProgressTypeVariableResolutions);
			}
			return TypeFactory.createParameterizedType(theBaseType, typeArgumentTypes, world);

			// world.resolve(UnresolvedType.forParameterizedTypes(
			// UnresolvedType.forSignature(sig.toString()).resolve(world),
			// typeArgumentTypes));
		} else {
			// we have a non-parameterized type
			return world.resolve(UnresolvedType.forSignature(sig.toString()));
		}
	}

	public static ResolvedType fieldTypeSignature2TypeX(GenericSignature.FieldTypeSignature aFieldTypeSignature,
			GenericSignature.FormalTypeParameter[] typeParams, World world) throws GenericSignatureFormatException {
		Map<GenericSignature.FormalTypeParameter, ReferenceType> typeMap = new HashMap<>();
		ResolvedType ret = fieldTypeSignature2TypeX(aFieldTypeSignature, typeParams, world, typeMap);
		fixUpCircularDependencies(ret, typeMap);
		return ret;
	}

	private static ResolvedType fieldTypeSignature2TypeX(GenericSignature.FieldTypeSignature aFieldTypeSignature,
			GenericSignature.FormalTypeParameter[] typeParams, World world,
			Map<GenericSignature.FormalTypeParameter, ReferenceType> inProgressTypeVariableResolutions)
			throws GenericSignatureFormatException {
		if (aFieldTypeSignature.isClassTypeSignature()) {
			return classTypeSignature2TypeX((GenericSignature.ClassTypeSignature) aFieldTypeSignature, typeParams, world,
					inProgressTypeVariableResolutions);
		} else if (aFieldTypeSignature.isArrayTypeSignature()) {
			int dims = 0;
			GenericSignature.TypeSignature ats = aFieldTypeSignature;
			while (ats instanceof GenericSignature.ArrayTypeSignature) {
				dims++;
				ats = ((GenericSignature.ArrayTypeSignature) ats).typeSig;
			}
			return world.resolve(UnresolvedType.makeArray(
					typeSignature2TypeX(ats, typeParams, world, inProgressTypeVariableResolutions), dims));
		} else if (aFieldTypeSignature.isTypeVariableSignature()) {
			ResolvedType rtx = typeVariableSignature2TypeX((GenericSignature.TypeVariableSignature) aFieldTypeSignature,
					typeParams, world, inProgressTypeVariableResolutions);
			return rtx;
		} else {
			throw new GenericSignatureFormatException("Cant understand field type signature: " + aFieldTypeSignature);
		}
	}

	public static TypeVariable formalTypeParameter2TypeVariable(GenericSignature.FormalTypeParameter aFormalTypeParameter,
			GenericSignature.FormalTypeParameter[] typeParams, World world) throws GenericSignatureFormatException {
		Map<GenericSignature.FormalTypeParameter, ReferenceType> typeMap = new HashMap<>();
		return formalTypeParameter2TypeVariable(aFormalTypeParameter, typeParams, world, typeMap);
	}

	private static TypeVariable formalTypeParameter2TypeVariable(GenericSignature.FormalTypeParameter aFormalTypeParameter,
			GenericSignature.FormalTypeParameter[] typeParams, World world,
			Map<GenericSignature.FormalTypeParameter, ReferenceType> inProgressTypeVariableResolutions)
			throws GenericSignatureFormatException {
		UnresolvedType upperBound = fieldTypeSignature2TypeX(aFormalTypeParameter.classBound, typeParams, world,
				inProgressTypeVariableResolutions);
		UnresolvedType[] ifBounds = new UnresolvedType[aFormalTypeParameter.interfaceBounds.length];
		for (int i = 0; i < ifBounds.length; i++) {
			ifBounds[i] = fieldTypeSignature2TypeX(aFormalTypeParameter.interfaceBounds[i], typeParams, world,
					inProgressTypeVariableResolutions);
		}
		return new TypeVariable(aFormalTypeParameter.identifier, upperBound, ifBounds);
	}

	private static ResolvedType typeArgument2TypeX(GenericSignature.TypeArgument aTypeArgument,
			GenericSignature.FormalTypeParameter[] typeParams, World world,
			Map<GenericSignature.FormalTypeParameter, ReferenceType> inProgressTypeVariableResolutions)
			throws GenericSignatureFormatException {
		if (aTypeArgument.isWildcard) {
			return UnresolvedType.SOMETHING.resolve(world);
		}
		if (aTypeArgument.isMinus) {
			UnresolvedType bound = fieldTypeSignature2TypeX(aTypeArgument.signature, typeParams, world,
					inProgressTypeVariableResolutions);
			ResolvedType resolvedBound = world.resolve(bound);
			if (resolvedBound.isMissing()) {
				world.getLint().cantFindType.signal("Unable to find type (for bound): " + resolvedBound.getName(), null);
				resolvedBound = world.resolve(UnresolvedType.OBJECT);
			}
			ReferenceType rBound = (ReferenceType) resolvedBound;
			return new BoundedReferenceType(rBound, false, world);
		} else if (aTypeArgument.isPlus) {
			UnresolvedType bound = fieldTypeSignature2TypeX(aTypeArgument.signature, typeParams, world,
					inProgressTypeVariableResolutions);
			ResolvedType resolvedBound = world.resolve(bound);
			if (resolvedBound.isMissing()) {
				world.getLint().cantFindType.signal("Unable to find type (for bound): " + resolvedBound.getName(), null);
				resolvedBound = world.resolve(UnresolvedType.OBJECT);
			}
			ReferenceType rBound = (ReferenceType) resolvedBound;
			return new BoundedReferenceType(rBound, true, world);
		} else {
			return fieldTypeSignature2TypeX(aTypeArgument.signature, typeParams, world, inProgressTypeVariableResolutions);
		}
	}

	public static ResolvedType typeSignature2TypeX(GenericSignature.TypeSignature aTypeSig,
			GenericSignature.FormalTypeParameter[] typeParams, World world) throws GenericSignatureFormatException {
		Map<GenericSignature.FormalTypeParameter, ReferenceType> typeMap = new HashMap<>();
		ResolvedType ret = typeSignature2TypeX(aTypeSig, typeParams, world, typeMap);
		fixUpCircularDependencies(ret, typeMap);
		return ret;
	}

	private static ResolvedType typeSignature2TypeX(GenericSignature.TypeSignature aTypeSig,
			GenericSignature.FormalTypeParameter[] typeParams, World world,
			Map<GenericSignature.FormalTypeParameter, ReferenceType> inProgressTypeVariableResolutions)
			throws GenericSignatureFormatException {
		if (aTypeSig.isBaseType()) {
			return world.resolve(UnresolvedType.forSignature(((GenericSignature.BaseTypeSignature) aTypeSig).toString()));
		} else {
			return fieldTypeSignature2TypeX((GenericSignature.FieldTypeSignature) aTypeSig, typeParams, world,
					inProgressTypeVariableResolutions);
		}
	}

	private static ResolvedType typeVariableSignature2TypeX(GenericSignature.TypeVariableSignature aTypeVarSig,
			GenericSignature.FormalTypeParameter[] typeParams, World world,
			Map<GenericSignature.FormalTypeParameter, ReferenceType> inProgressTypeVariableResolutions)
			throws GenericSignatureFormatException {
		GenericSignature.FormalTypeParameter typeVarBounds = null;
		for (GenericSignature.FormalTypeParameter typeParam : typeParams) {
			if (typeParam.identifier.equals(aTypeVarSig.typeVariableName)) {
				typeVarBounds = typeParam;
				break;
			}
		}
		if (typeVarBounds == null) {
			// blowing up here breaks the situation with ITDs where the type variable is mentioned in the
			// declaring type and used somewhere in the signature. Temporary change to allow it to return just a
			// 'dumb' typevariablereference.
			return new TypeVariableReferenceType(new TypeVariable(aTypeVarSig.typeVariableName), world);
			// throw new GenericSignatureFormatException("Undeclared type variable in signature: " + aTypeVarSig.typeVariableName);
		}
		if (inProgressTypeVariableResolutions.containsKey(typeVarBounds)) {
			return inProgressTypeVariableResolutions.get(typeVarBounds);
		}
		inProgressTypeVariableResolutions.put(typeVarBounds, new FTPHolder(typeVarBounds, world));
		ReferenceType ret = new TypeVariableReferenceType(formalTypeParameter2TypeVariable(typeVarBounds, typeParams, world,
				inProgressTypeVariableResolutions), world);
		inProgressTypeVariableResolutions.put(typeVarBounds, ret);
		return ret;
	}

	private static void fixUpCircularDependencies(ResolvedType aTypeX,
			Map<GenericSignature.FormalTypeParameter, ReferenceType> typeVariableResolutions) {
		if (!(aTypeX instanceof ReferenceType)) {
			return;
		}

		ReferenceType rt = (ReferenceType) aTypeX;
		TypeVariable[] typeVars = rt.getTypeVariables();
		if (typeVars != null) {
			for (TypeVariable typeVar : typeVars) {
				if (typeVar.getUpperBound() instanceof FTPHolder) {
					GenericSignature.FormalTypeParameter key = ((FTPHolder) typeVar.getUpperBound()).ftpToBeSubstituted;
					typeVar.setUpperBound(typeVariableResolutions.get(key));
				}
			}
		}
	}

	private static class FTPHolder extends ReferenceType {
		public GenericSignature.FormalTypeParameter ftpToBeSubstituted;

		public FTPHolder(GenericSignature.FormalTypeParameter ftp, World world) {
			super("Ljava/lang/Object;", world);
			this.ftpToBeSubstituted = ftp;
		}

		public String toString() {
			return "placeholder for TypeVariable of " + ftpToBeSubstituted.toString();
		}

		public ResolvedType resolve(World world) {
			return this;
		}

		public boolean isCacheable() {
			return false;
		}
	}

	public static class GenericSignatureFormatException extends Exception {
		public GenericSignatureFormatException(String explanation) {
			super(explanation);
		}
	}
}
