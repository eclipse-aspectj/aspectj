/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MemberTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Converter from a type to an AST type declaration.
 */
public class TypeConverter {
	
	/**
	 * Convert a type into an AST type declaration and put it in the given compilation unit.
	 */
	public static TypeDeclaration buildTypeDeclaration(IType type, CompilationUnitDeclaration compilationUnit, CompilationResult compilationResult, ProblemReporter problemReporter)  throws JavaModelException {
		char[] packageName = type.getPackageFragment().getElementName().toCharArray();
		
		if (packageName != null && packageName.length > 0) { 
			compilationUnit.currentPackage = new ImportReference(CharOperation.splitOn('.', packageName), new long[]{0}, false);
		}
	
		/* convert type */
		TypeDeclaration typeDeclaration = convert(type, null, null, compilationResult);
		
		IType alreadyComputedMember = type;
		IType parent = type.getDeclaringType();
		TypeDeclaration previousDeclaration = typeDeclaration;
		while(parent != null) {
			TypeDeclaration declaration = convert(parent, alreadyComputedMember, (MemberTypeDeclaration)previousDeclaration, compilationResult);
			
			alreadyComputedMember = parent;
			previousDeclaration = declaration;
			parent = parent.getDeclaringType();
		}
		
		compilationUnit.types = new TypeDeclaration[]{previousDeclaration};

		return typeDeclaration;
	}
	
	private static FieldDeclaration convert(IField field, IType type) throws JavaModelException {

		FieldDeclaration fieldDeclaration = new FieldDeclaration();

		fieldDeclaration.name = field.getElementName().toCharArray();
		fieldDeclaration.type = createTypeReference(Signature.toString(field.getTypeSignature()).toCharArray(), type);
		fieldDeclaration.modifiers = field.getFlags();

		return fieldDeclaration;
	}
	
	private static AbstractMethodDeclaration convert(IMethod method, IType type, CompilationResult compilationResult) throws JavaModelException {

		AbstractMethodDeclaration methodDeclaration;

		if (method.isConstructor()) {
			ConstructorDeclaration decl = new ConstructorDeclaration(compilationResult);
			decl.isDefaultConstructor = false;
			methodDeclaration = decl;
		} else {
			MethodDeclaration decl = new MethodDeclaration(compilationResult);
			/* convert return type */
			decl.returnType = createTypeReference(Signature.toString(method.getReturnType()).toCharArray(), type);
			methodDeclaration = decl;
		}
		methodDeclaration.selector = method.getElementName().toCharArray();
		methodDeclaration.modifiers = method.getFlags();

		/* convert arguments */
		String[] argumentTypeNames = method.getParameterTypes();
		String[] argumentNames = method.getParameterNames();
		int argumentCount = argumentTypeNames == null ? 0 : argumentTypeNames.length;
		methodDeclaration.arguments = new Argument[argumentCount];
		for (int i = 0; i < argumentCount; i++) {
			methodDeclaration.arguments[i] = new Argument(
				argumentNames[i].toCharArray(),
				0,
				createTypeReference(Signature.toString(argumentTypeNames[i]).toCharArray(), type),
				CompilerModifiers.AccDefault);
			// do not care whether was final or not
		}

		/* convert thrown exceptions */
		String[] exceptionTypeNames = method.getExceptionTypes();
		int exceptionCount = exceptionTypeNames == null ? 0 : exceptionTypeNames.length;
		if(exceptionCount > 0) {
			methodDeclaration.thrownExceptions = new TypeReference[exceptionCount];
			for (int i = 0; i < exceptionCount; i++) {
				methodDeclaration.thrownExceptions[i] =
					createTypeReference(Signature.toString(exceptionTypeNames[i]).toCharArray(), type);
			}
		}
		return methodDeclaration;
	}
	
	private static TypeDeclaration convert(IType type, IType alreadyComputedMember,MemberTypeDeclaration alreadyComputedMemberDeclaration, CompilationResult compilationResult) throws JavaModelException {
		/* create type declaration - can be member type */
		TypeDeclaration typeDeclaration;
		if (type.getDeclaringType() == null) {
			typeDeclaration = new TypeDeclaration(compilationResult);
		} else {
			typeDeclaration = new MemberTypeDeclaration(compilationResult);
		}
		typeDeclaration.name = type.getElementName().toCharArray();
		typeDeclaration.modifiers = type.getFlags();


		/* set superclass and superinterfaces */
		if (type.getSuperclassName() != null) {
			typeDeclaration.superclass = createTypeReference(type.getSuperclassName().toCharArray(), type);
		}
		String[] interfaceNames = type.getSuperInterfaceNames();
		int interfaceCount = interfaceNames == null ? 0 : interfaceNames.length;
		typeDeclaration.superInterfaces = new TypeReference[interfaceCount];
		for (int i = 0; i < interfaceCount; i++) {
			typeDeclaration.superInterfaces[i] = createTypeReference(interfaceNames[i].toCharArray(), type);
		}
		
		/* convert member types */
		IType[] memberTypes = type.getTypes();
		int memberTypeCount =	memberTypes == null ? 0 : memberTypes.length;
		typeDeclaration.memberTypes = new MemberTypeDeclaration[memberTypeCount];
		for (int i = 0; i < memberTypeCount; i++) {
			if(alreadyComputedMember != null && alreadyComputedMember.getFullyQualifiedName().equals(memberTypes[i].getFullyQualifiedName())) {
				typeDeclaration.memberTypes[i] = alreadyComputedMemberDeclaration;
			} else {
				typeDeclaration.memberTypes[i] =
					(MemberTypeDeclaration) convert(memberTypes[i], null, null, compilationResult);
			}
		}

		/* convert fields */
		IField[] fields = type.getFields();
		int fieldCount = fields == null ? 0 : fields.length;
		typeDeclaration.fields = new FieldDeclaration[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			typeDeclaration.fields[i] = convert(fields[i], type);
		}

		/* convert methods - need to add default constructor if necessary */
		IMethod[] methods = type.getMethods();
		int methodCount = methods == null ? 0 : methods.length;

		/* source type has a constructor ?           */
		/* by default, we assume that one is needed. */
		int neededCount = 1;
		for (int i = 0; i < methodCount; i++) {
			if (methods[i].isConstructor()) {
				neededCount = 0;
				// Does not need the extra constructor since one constructor already exists.
				break;
			}
		}
		typeDeclaration.methods = new AbstractMethodDeclaration[methodCount + neededCount];
		if (neededCount != 0) { // add default constructor in first position
			typeDeclaration.methods[0] = typeDeclaration.createsInternalConstructor(false, false);
		}
		boolean isInterface = type.isInterface();
		for (int i = 0; i < methodCount; i++) {
			AbstractMethodDeclaration method =convert(methods[i], type, compilationResult);
			if (isInterface || method.isAbstract()) { // fix-up flag 
				method.modifiers |= CompilerModifiers.AccSemicolonBody;
			}
			typeDeclaration.methods[neededCount + i] = method;
		}
		return typeDeclaration;
	}
	
	private static TypeReference createTypeReference(char[] type, IType contextType) {
		try {
			String[][] resolvedName = contextType.resolveType(new String(type));
			char[] superClassName = null;
			if(resolvedName != null && resolvedName.length == 1) {
				type= CharOperation.concat(resolvedName[0][0].toCharArray(), resolvedName[0][1].toCharArray(), '.');
			}
		} catch (JavaModelException e) {
			
		}
		
		/* count identifiers and dimensions */
		int max = type.length;
		int dimStart = max;
		int dim = 0;
		int identCount = 1;
		for (int i = 0; i < max; i++) {
			switch (type[i]) {
				case '[' :
					if (dim == 0)
						dimStart = i;
					dim++;
					break;
				case '.' :
					identCount++;
					break;
			}
		}
		/* rebuild identifiers and dimensions */
		if (identCount == 1) { // simple type reference
			if (dim == 0) {
				return new SingleTypeReference(type, 0);
			} else {
				char[] identifier = new char[dimStart];
				System.arraycopy(type, 0, identifier, 0, dimStart);
				return new ArrayTypeReference(identifier, dim, 0);
			}
		} else { // qualified type reference
			char[][] identifiers =	CharOperation.splitOn('.', type, 0, dimStart - 1);
			if (dim == 0) {
				return new QualifiedTypeReference(identifiers, new long[]{0});
			} else {
				return new ArrayQualifiedTypeReference(identifiers, dim, new long[]{0});
			}
		}
	}
}
