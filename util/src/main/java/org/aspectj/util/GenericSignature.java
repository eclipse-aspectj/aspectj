/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * ******************************************************************/
package org.aspectj.util;

/**
 * Encapsulate generic signature parsing
 *
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class GenericSignature {

	/**
	 * structure holding a parsed class signature
	 */
	public static class ClassSignature {
		public FormalTypeParameter[] formalTypeParameters = FormalTypeParameter.NONE;
		public ClassTypeSignature superclassSignature;
		public ClassTypeSignature[] superInterfaceSignatures = ClassTypeSignature.NONE;

		public String toString() {
			StringBuilder ret = new StringBuilder();
			ret.append(formalTypeParameters.toString());
			ret.append(superclassSignature.toString());
			for (ClassTypeSignature superInterfaceSignature : superInterfaceSignatures) {
				ret.append(superInterfaceSignature.toString());
			}
			return ret.toString();
		}
	}

	public static class MethodTypeSignature {
		public FormalTypeParameter[] formalTypeParameters = FormalTypeParameter.NONE;
		public final TypeSignature[] parameters;
		public TypeSignature returnType;
		public final FieldTypeSignature[] throwsSignatures;

		public MethodTypeSignature(FormalTypeParameter[] aFormalParameterList, TypeSignature[] aParameterList,
				TypeSignature aReturnType, FieldTypeSignature[] aThrowsSignatureList) {
			this.formalTypeParameters = aFormalParameterList;
			this.parameters = aParameterList;
			this.returnType = aReturnType;
			this.throwsSignatures = aThrowsSignatureList;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (formalTypeParameters.length > 0) {
				sb.append("<");
				for (FormalTypeParameter formalTypeParameter : formalTypeParameters) {
					sb.append(formalTypeParameter.toString());
				}
				sb.append(">");
			}
			sb.append("(");
			for (TypeSignature parameter : parameters) {
				sb.append(parameter.toString());
			}
			sb.append(")");
			sb.append(returnType.toString());
			for (FieldTypeSignature throwsSignature : throwsSignatures) {
				sb.append("^");
				sb.append(throwsSignature.toString());
			}
			return sb.toString();
		}
	}

	/**
	 * structure capturing a FormalTypeParameter from the Signature grammar
	 */
	public static class FormalTypeParameter {
		public static final FormalTypeParameter[] NONE = new FormalTypeParameter[0];
		public String identifier;
		public FieldTypeSignature classBound;
		public FieldTypeSignature[] interfaceBounds;

		public String toString() {
			StringBuilder ret = new StringBuilder();
			ret.append("T");
			ret.append(identifier);
			ret.append(":");
			ret.append(classBound.toString());
			for (FieldTypeSignature interfaceBound : interfaceBounds) {
				ret.append(":");
				ret.append(interfaceBound.toString());
			}
			return ret.toString();
		}
	}

	public static abstract class TypeSignature {
		public boolean isBaseType() {
			return false;
		}
	}

	public static class BaseTypeSignature extends TypeSignature {
		private final String sig;

		public BaseTypeSignature(String aPrimitiveType) {
			sig = aPrimitiveType;
		}

		public boolean isBaseType() {
			return true;
		}

		public String toString() {
			return sig;
		}
	}

	public static abstract class FieldTypeSignature extends TypeSignature {
		public boolean isClassTypeSignature() {
			return false;
		}

		public boolean isTypeVariableSignature() {
			return false;
		}

		public boolean isArrayTypeSignature() {
			return false;
		}
	}

	public static class ClassTypeSignature extends FieldTypeSignature {

		public static final ClassTypeSignature[] NONE = new ClassTypeSignature[0];
		public String classSignature;
		public SimpleClassTypeSignature outerType;
		public SimpleClassTypeSignature[] nestedTypes;

		public ClassTypeSignature(String sig, String identifier) {
			this.classSignature = sig;
			this.outerType = new SimpleClassTypeSignature(identifier);
			this.nestedTypes = new SimpleClassTypeSignature[0];
		}

		public ClassTypeSignature(String sig, SimpleClassTypeSignature outer, SimpleClassTypeSignature[] inners) {
			this.classSignature = sig;
			this.outerType = outer;
			this.nestedTypes = inners;
		}

		public boolean isClassTypeSignature() {
			return true;
		}

		public String toString() {
			return classSignature;
		}
	}

	public static class TypeVariableSignature extends FieldTypeSignature {
		public String typeVariableName;

		public TypeVariableSignature(String typeVarToken) {
			this.typeVariableName = typeVarToken.substring(1);
		}

		public boolean isTypeVariableSignature() {
			return true;
		}

		public String toString() {
			return "T" + typeVariableName + ";";
		}
	}

	public static class ArrayTypeSignature extends FieldTypeSignature {
		public TypeSignature typeSig;

		public ArrayTypeSignature(TypeSignature aTypeSig) {
			this.typeSig = aTypeSig;
		}

		public boolean isArrayTypeSignature() {
			return true;
		}

		public String toString() {
			return "[" + typeSig.toString();
		}
	}

	public static class SimpleClassTypeSignature {
		public String identifier;
		public TypeArgument[] typeArguments;

		public SimpleClassTypeSignature(String identifier) {
			this.identifier = identifier;
			this.typeArguments = new TypeArgument[0];
		}

		public SimpleClassTypeSignature(String identifier, TypeArgument[] args) {
			this.identifier = identifier;
			this.typeArguments = args;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(identifier);
			if (typeArguments.length > 0) {
				sb.append("<");
				for (TypeArgument typeArgument : typeArguments) {
					sb.append(typeArgument.toString());
				}
				sb.append(">");
			}
			return sb.toString();
		}
	}

	public static class TypeArgument {
		public boolean isWildcard = false;
		public boolean isPlus = false;
		public boolean isMinus = false;
		public FieldTypeSignature signature; // null if isWildcard

		public TypeArgument() {
			isWildcard = true;
		}

		public TypeArgument(boolean plus, boolean minus, FieldTypeSignature aSig) {
			this.isPlus = plus;
			this.isMinus = minus;
			this.signature = aSig;
		}

		public String toString() {
			if (isWildcard)
				return "*";
			StringBuilder sb = new StringBuilder();
			if (isPlus)
				sb.append("+");
			if (isMinus)
				sb.append("-");
			sb.append(signature.toString());
			return sb.toString();
		}
	}
}
