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

import java.util.ArrayList;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;
import org.eclipse.jdt.internal.codeassist.SelectionEngine;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Implementation of <code>ISelectionRequestor</code> to assist with
 * code resolve in a compilation unit. Translates names to elements.
 */
public class SelectionRequestor implements ISelectionRequestor {
	/**
	 * The name lookup facility used to resolve packages
	 */
	protected NameLookup fNameLookup= null;

	/**
	 * Fix for 1FVXGDK
	 *
	 * The compilation unit we are resolving in
	 */
	protected IJavaElement fCodeResolve;

	/**
	 * The collection of resolved elements.
	 */
	protected IJavaElement[] fElements= fgEmptyElements;

	/**
	 * Empty collection used for efficiency.
	 */
	protected static IJavaElement[] fgEmptyElements = new IJavaElement[]{};
/**
 * Creates a selection requestor that uses that given
 * name lookup facility to resolve names.
 *
 * Fix for 1FVXGDK
 */
public SelectionRequestor(NameLookup nameLookup, IJavaElement codeResolve) {
	super();
	fNameLookup = nameLookup;
	fCodeResolve = codeResolve;
}
/**
 * Resolve the binary method
 *
 * fix for 1FWFT6Q
 */
protected void acceptBinaryMethod(IType type, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames) {
	String[] parameterTypes= null;
	if (parameterTypeNames != null) {
		parameterTypes= new String[parameterTypeNames.length];
		for (int i= 0, max = parameterTypeNames.length; i < max; i++) {
			String pkg = IPackageFragment.DEFAULT_PACKAGE_NAME;
			if (parameterPackageNames[i] != null && parameterPackageNames[i].length > 0) {
				pkg = new String(parameterPackageNames[i]) + "."; //$NON-NLS-1$
			}
			
			String typeName = new String(parameterTypeNames[i]);
			if (typeName.indexOf('.') > 0) 
				typeName = typeName.replace('.', '$');
			parameterTypes[i]= Signature.createTypeSignature(
				pkg + typeName, true);
		}
	}
	IMethod method= type.getMethod(new String(selector), parameterTypes);
	if (method.exists()) {
		fElements = growAndAddToArray(fElements, method);
		if(SelectionEngine.DEBUG){
			System.out.print("SELECTION - accept method("); //$NON-NLS-1$
			System.out.print(method.toString());
			System.out.println(")"); //$NON-NLS-1$
		}
	}
}
/**
 * Resolve the class.
 */
public void acceptClass(char[] packageName, char[] className, boolean needQualification) {
	acceptType(packageName, className, NameLookup.ACCEPT_CLASSES, needQualification);
}
/**
 * Do nothing.
 */
public void acceptError(IProblem error) {}
/**
 * Resolve the field.
 */
public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name) {
	IType type= resolveType(declaringTypePackageName, declaringTypeName,
		NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
	if (type != null) {
		IField field= type.getField(new String(name));
		if (field.exists()) {
			fElements= growAndAddToArray(fElements, field);
			if(SelectionEngine.DEBUG){
				System.out.print("SELECTION - accept field("); //$NON-NLS-1$
				System.out.print(field.toString());
				System.out.println(")"); //$NON-NLS-1$
			}
		}
	}
}
/**
 * Resolve the interface
 */
public void acceptInterface(char[] packageName, char[] interfaceName, boolean needQualification) {
	acceptType(packageName, interfaceName, NameLookup.ACCEPT_INTERFACES, needQualification);
}
/**
 * Resolve the method
 */
public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, boolean isConstructor) {
	IType type= resolveType(declaringTypePackageName, declaringTypeName,
		NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
	// fix for 1FWFT6Q
	if (type != null) {
		if (type.isBinary()) {
			
			// need to add a paramater for constructor in binary type
			IType declaringDeclaringType = type.getDeclaringType();
			if(declaringDeclaringType != null && isConstructor) {
				int length = parameterPackageNames.length;
				System.arraycopy(parameterPackageNames, 0, parameterPackageNames = new char[length+1][], 1, length);
				System.arraycopy(parameterTypeNames, 0, parameterTypeNames = new char[length+1][], 1, length);
				
				parameterPackageNames[0] = declaringDeclaringType.getPackageFragment().getElementName().toCharArray();
				parameterTypeNames[0] = declaringDeclaringType.getTypeQualifiedName().toCharArray();
			}
			
			acceptBinaryMethod(type, selector, parameterPackageNames, parameterTypeNames);
		} else {
			acceptSourceMethod(type, selector, parameterPackageNames, parameterTypeNames);
		}
	}
}
/**
 * Resolve the package
 */
public void acceptPackage(char[] packageName) {
	IPackageFragment[] pkgs = fNameLookup.findPackageFragments(new String(packageName), false);
	if (pkgs != null) {
		for (int i = 0, length = pkgs.length; i < length; i++) {
			fElements = growAndAddToArray(fElements, pkgs[i]);
			if(SelectionEngine.DEBUG){
				System.out.print("SELECTION - accept package("); //$NON-NLS-1$
				System.out.print(pkgs[i].toString());
				System.out.println(")"); //$NON-NLS-1$
			}
		}
	}
}
/**
 * Resolve the source method
 *
 * fix for 1FWFT6Q
 */
protected void acceptSourceMethod(IType type, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames) {
	String name = new String(selector);
	IMethod[] methods = null;
	IJavaElement[] matches = new IJavaElement[] {};
	try {
		methods = type.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getElementName().equals(name) && methods[i].getParameterTypes().length == parameterTypeNames.length) {
				matches = growAndAddToArray(matches, methods[i]);
			}
		}
	} catch (JavaModelException e) {
		return; 
	}

	// if no matches, nothing to report
	if (matches.length == 0) {
		// no match was actually found, but a method was originally given -> default constructor
		fElements = growAndAddToArray(fElements, type);
		if(SelectionEngine.DEBUG){
			System.out.print("SELECTION - accept type("); //$NON-NLS-1$
			System.out.print(type.toString());
			System.out.println(")"); //$NON-NLS-1$
		}
		return;
	}

	// if there is only one match, we've got it
	if (matches.length == 1) {
		fElements = growAndAddToArray(fElements, matches[0]);
		if(SelectionEngine.DEBUG){
			System.out.print("SELECTION - accept method("); //$NON-NLS-1$
			System.out.print(matches[0].toString());
			System.out.println(")"); //$NON-NLS-1$
		}
		return;
	}

	// more than one match - must match simple parameter types
	for (int i = 0; i < matches.length; i++) {
		IMethod method= (IMethod)matches[i];
		String[] signatures = method.getParameterTypes();
		boolean match= true;
		for (int p = 0; p < signatures.length; p++) {
			String simpleName= Signature.getSimpleName(Signature.toString(signatures[p]));
			if (!simpleName.equals(new String(parameterTypeNames[p]))) {
				match = false;
				break;
			}
		}
		if (match) {
			fElements = growAndAddToArray(fElements, method);
			if(SelectionEngine.DEBUG){
				System.out.print("SELECTION - accept method("); //$NON-NLS-1$
				System.out.print(method.toString());
				System.out.println(")"); //$NON-NLS-1$
			}
		}
	}
	
}
/**
 * Resolve the type, adding to the resolved elements.
 */
protected void acceptType(char[] packageName, char[] typeName, int acceptFlags, boolean needQualification) {
	IType type= resolveType(packageName, typeName, acceptFlags);
	if (type != null) {
		fElements= growAndAddToArray(fElements, type);
		if(SelectionEngine.DEBUG){
			System.out.print("SELECTION - accept type("); //$NON-NLS-1$
			System.out.print(type.toString());
			System.out.println(")"); //$NON-NLS-1$
		}
	} 
	
}
/**
 * Returns the resolved elements.
 */
public IJavaElement[] getElements() {
	return fElements;
}
/**
 * Adds the new element to a new array that contains all of the elements of the old array.
 * Returns the new array.
 */
protected IJavaElement[] growAndAddToArray(IJavaElement[] array, IJavaElement addition) {
	IJavaElement[] old = array;
	array = new IJavaElement[old.length + 1];
	System.arraycopy(old, 0, array, 0, old.length);
	array[old.length] = addition;
	return array;
}
/**
 * Resolve the type
 */
protected IType resolveType(char[] packageName, char[] typeName, int acceptFlags) {

	IType type= null;
	
	if (fCodeResolve instanceof WorkingCopy) {
		WorkingCopy wc = (WorkingCopy) fCodeResolve;
		try {
			if(((packageName == null || packageName.length == 0) && wc.getPackageDeclarations().length == 0) ||
				(!(packageName == null || packageName.length == 0) && wc.getPackageDeclaration(new String(packageName)).exists())) {
					
				char[][] compoundName = CharOperation.splitOn('.', typeName);
				if(compoundName.length > 0) {
					type = wc.getType(new String(compoundName[0]));
					for (int i = 1, length = compoundName.length; i < length; i++) {
						type = type.getType(new String(compoundName[i]));
					}
				}
				
				if(type != null && !type.exists()) {
					type = null;
				}
			}
		}catch (JavaModelException e) {
			type = null;
		}
	}

	if(type == null) {
		IPackageFragment[] pkgs = fNameLookup.findPackageFragments(
			(packageName == null || packageName.length == 0) ? IPackageFragment.DEFAULT_PACKAGE_NAME : new String(packageName), 
			false);
		// iterate type lookup in each package fragment
		for (int i = 0, length = pkgs == null ? 0 : pkgs.length; i < length; i++) {
			type= fNameLookup.findType(new String(typeName), pkgs[i], false, acceptFlags);
			if (type != null) break;	
		}
		if (type == null) {
			String pName= IPackageFragment.DEFAULT_PACKAGE_NAME;
			if (packageName != null) {
				pName = new String(packageName);
			}
			if (fCodeResolve != null && fCodeResolve.getParent().getElementName().equals(pName)) {
				// look inside the type in which we are resolving in
				String tName= new String(typeName);
				tName = tName.replace('.','$');
				IType[] allTypes= null;
				try {
					ArrayList list = ((JavaElement)fCodeResolve).getChildrenOfType(IJavaElement.TYPE);
					allTypes = new IType[list.size()];
					list.toArray(allTypes);
				} catch (JavaModelException e) {
					return null;
				}
				for (int i= 0; i < allTypes.length; i++) {
					if (allTypes[i].getTypeQualifiedName().equals(tName)) {
						return allTypes[i];
					}
				}
			}
		}
	}
	return type;
}
}
