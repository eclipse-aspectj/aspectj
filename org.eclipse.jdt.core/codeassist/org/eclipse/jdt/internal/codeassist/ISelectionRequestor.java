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
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * A selection requestor accepts results from the selection engine.
 */
public interface ISelectionRequestor {
	/**
	 * Code assist notification of a class selection.
	 * @param packageName char[]
	 * 		Declaring package name of the class.
	 * 
	 * @param className char[]
	 * 		Name of the class.
	 * 
	 * @param needQualification boolean
	 * 		Flag indicating if the type name 
	 *    	must be qualified by its package name (depending on imports).
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	void acceptClass(
		char[] packageName,
		char[] className,
		boolean needQualification);

	/**
	 * Code assist notification of a compilation error detected during selection.
	 *  @param error org.eclipse.jdt.internal.compiler.IProblem
	 *      Only problems which are categorized as errors are notified to the requestor,
	 *		warnings are silently ignored.
	 *		In case an error got signaled, no other completions might be available,
	 *		therefore the problem message should be presented to the user.
	 *		The source positions of the problem are related to the source where it was
	 *		detected (might be in another compilation unit, if it was indirectly requested
	 *		during the code assist process).
	 *      Note: the problem knows its originating file name.
	 */
	void acceptError(IProblem error);

	/**
	 * Code assist notification of a field selection.
	 * @param declaringTypePackageName char[]
	 * 		Name of the package in which the type that contains this field is declared.
	 * 
	 * @param declaringTypeName char[]
	 * 		Name of the type declaring this new field.
	 * 
	 * @param name char[]
	 * 		Name of the field.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	void acceptField(
		char[] declaringTypePackageName,
		char[] declaringTypeName,
		char[] name);

	/**
	 * Code assist notification of an interface selection.
	 * @param packageName char[]
	 * 		Declaring package name of the interface.
	 * 
	 * @param interfaceName char[]
	 * 		Name of the interface.
	 * 
	 * @param needQualification boolean
	 * 		Flag indicating if the type name 
	 *    	must be qualified by its package name (depending on imports).
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.I".
	 *    The default package is represented by an empty array.
	 */
	void acceptInterface(
		char[] packageName,
		char[] interfaceName,
		boolean needQualification);

	/**
	 * Code assist notification of a method selection.
	 * @param declaringTypePackageName char[]
	 * 		Name of the package in which the type that contains this new method is declared.
	 * 
	 * @param declaringTypeName char[]
	 * 		Name of the type declaring this new method.
	 * 
	 * @param selector char[]
	 * 		Name of the new method.
	 * 
	 * @param parameterPackageNames char[][]
	 * 		Names of the packages in which the parameter types are declared.
	 *    	Should contain as many elements as parameterTypeNames.
	 * 
	 * @param parameterTypeNames char[][]
	 * 		Names of the parameters types.
	 *    	Should contain as many elements as parameterPackageNames.
	 * 
	 *  @param isConstructor boolean
	 * 		Answer if the method is a constructor.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Base types are in the form "int" or "boolean".
	 *    Array types are in the qualified form "M[]" or "int[]".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	void acceptMethod(
		char[] declaringTypePackageName,
		char[] declaringTypeName,
		char[] selector,
		char[][] parameterPackageNames,
		char[][] parameterTypeNames,
		boolean isConstructor);

	/**
	 * Code assist notification of a package selection.
	 * @param packageName char[]
	 * 		The package name.
	 *
	 * NOTE - All package names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    The default package is represented by an empty array.
	 */
	void acceptPackage(char[] packageName);
}