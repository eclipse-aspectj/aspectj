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

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.core.util.ReferenceInfoAdapter;

/**
 * A requestor for the fuzzy parser, used to compute the children of an ICompilationUnit.
 */
public class CompilationUnitStructureRequestor extends ReferenceInfoAdapter implements ISourceElementRequestor {

	/**
	 * The handle to the compilation unit being parsed
	 */
	protected ICompilationUnit fUnit;

	/**
	 * The info object for the compilation unit being parsed
	 */
	protected CompilationUnitElementInfo fUnitInfo;

	/**
	 * The import container info - null until created
	 */
	protected JavaElementInfo fImportContainerInfo= null;

	/**
	 * Hashtable of children elements of the compilation unit.
	 * Children are added to the table as they are found by
	 * the parser. Keys are handles, values are corresponding
	 * info objects.
	 */
	protected Map fNewElements;

	/**
	 * Stack of parent scope info objects - i.e. the info on the
	 * top of the stack is the parent of the next element found.
	 * For example, when we locate a method, the parent info object
	 * will be the type the method is contained in.
	 */
	protected Stack fInfoStack;

	/**
	 * Stack of parent handles, corresponding to the info stack. We
	 * keep both, since info objects do not have back pointers to
	 * handles.
	 */
	protected Stack fHandleStack;

	/**
	 * The name of the source file being parsed.
	 */
	protected char[] fSourceFileName= null;

	/**
	 * The dot-separated name of the package the compilation unit
	 * is contained in - based on the package statement in the
	 * compilation unit, and initialized by #acceptPackage.
	 * Initialized to <code>null</code> for the default package.
	 */
	protected char[] fPackageName= null;

	/**
	 * The number of references reported thus far. Used to
	 * expand the arrays of reference kinds and names.
	 */
	protected int fRefCount= 0;

	/**
	 * The initial size of the reference kind and name
	 * arrays. If the arrays fill, they are doubled in
	 * size
	 */
	protected static int fgReferenceAllocation= 50;

	/**
	 * Problem requestor which will get notified of discovered problems
	 */
	protected boolean hasSyntaxErrors = false;
	
	/**
	 * Empty collections used for efficient initialization
	 */
	protected static String[] fgEmptyStringArray = new String[0];
	protected static byte[] fgEmptyByte= new byte[]{};
	protected static char[][] fgEmptyCharChar= new char[][]{};
	protected static char[] fgEmptyChar= new char[]{};


	protected HashtableOfObject fieldRefCache;
	protected HashtableOfObject messageRefCache;
	protected HashtableOfObject typeRefCache;
	protected HashtableOfObject unknownRefCache;

protected CompilationUnitStructureRequestor(ICompilationUnit unit, CompilationUnitElementInfo unitInfo, Map newElements) throws JavaModelException {
	this.fUnit = unit;
	this.fUnitInfo = unitInfo;
	this.fNewElements = newElements;
	this.fSourceFileName= unit.getElementName().toCharArray();
} 
/**
 * @see ISourceElementRequestor
 */
public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand) {
	JavaElementInfo parentInfo = (JavaElementInfo) fInfoStack.peek();
	JavaElement parentHandle= (JavaElement)fHandleStack.peek();
	if (!(parentHandle.getElementType() == IJavaElement.COMPILATION_UNIT)) {
		Assert.isTrue(false); // Should not happen
	}

	ICompilationUnit parentCU= (ICompilationUnit)parentHandle;
	//create the import container and its info
	IImportContainer importContainer= parentCU.getImportContainer();
	if (fImportContainerInfo == null) {
		fImportContainerInfo= new JavaElementInfo();
		fImportContainerInfo.setIsStructureKnown(true);
		parentInfo.addChild(importContainer);
		fNewElements.put(importContainer, fImportContainerInfo);
	}
	
	// tack on the '.*' if it is onDemand
	String importName;
	if (onDemand) {
		importName= new String(name) + ".*"; //$NON-NLS-1$
	} else {
		importName= new String(name);
	}
	
	ImportDeclaration handle = new ImportDeclaration(importContainer, importName);
	resolveDuplicates(handle);
	
	SourceRefElementInfo info = new SourceRefElementInfo();
	info.setSourceRangeStart(declarationStart);
	info.setSourceRangeEnd(declarationEnd);

	fImportContainerInfo.addChild(handle);
	fNewElements.put(handle, info);
}
/*
 * Table of line separator position. This table is passed once at the end
 * of the parse action, so as to allow computation of normalized ranges.
 *
 * A line separator might corresponds to several characters in the source,
 * 
 */
public void acceptLineSeparatorPositions(int[] positions) {}
/**
 * @see ISourceElementRequestor
 */
public void acceptPackage(int declarationStart, int declarationEnd, char[] name) {

		JavaElementInfo parentInfo = (JavaElementInfo) fInfoStack.peek();
		JavaElement parentHandle= (JavaElement)fHandleStack.peek();
		IPackageDeclaration handle = null;
		fPackageName= name;
		
		if (parentHandle.getElementType() == IJavaElement.COMPILATION_UNIT) {
			handle = new PackageDeclaration((ICompilationUnit) parentHandle, new String(name));
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		SourceRefElementInfo info = new SourceRefElementInfo();
		info.setSourceRangeStart(declarationStart);
		info.setSourceRangeEnd(declarationEnd);

		parentInfo.addChild(handle);
		fNewElements.put(handle, info);

}
public void acceptProblem(IProblem problem) {
	if ((problem.getID() & IProblem.Syntax) != 0){
		this.hasSyntaxErrors = true;
	}
}
/**
 * Convert these type names to signatures.
 * @see Signature.
 */
/* default */ static String[] convertTypeNamesToSigs(char[][] typeNames) {
	if (typeNames == null)
		return fgEmptyStringArray;
	int n = typeNames.length;
	if (n == 0)
		return fgEmptyStringArray;
	String[] typeSigs = new String[n];
	for (int i = 0; i < n; ++i) {
		typeSigs[i] = Signature.createTypeSignature(typeNames[i], false);
	}
	return typeSigs;
}
/**
 * @see ISourceElementRequestor
 */
public void enterClass(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] superclass,
	char[][] superinterfaces) {

	enterType(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, superclass, superinterfaces);

}
/**
 * @see ISourceElementRequestor
 */
public void enterCompilationUnit() {
	fInfoStack = new Stack();
	fHandleStack= new Stack();
	fInfoStack.push(fUnitInfo);
	fHandleStack.push(fUnit);
}
/**
 * @see ISourceElementRequestor
 */
public void enterConstructor(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes) {

		enterMethod(declarationStart, modifiers, null, name, nameSourceStart,
			nameSourceEnd,	parameterTypes, parameterNames, exceptionTypes, true);
}
/**
 * @see ISourceElementRequestor
 */
public void enterField(
	int declarationStart,
	int modifiers,
	char[] type,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd) {

		SourceTypeElementInfo parentInfo = (SourceTypeElementInfo) fInfoStack.peek();
		JavaElement parentHandle= (JavaElement)fHandleStack.peek();
		IField handle = null;
		
		if (parentHandle.getElementType() == IJavaElement.TYPE) {
			handle = new SourceField((IType) parentHandle, new String(name));
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		SourceFieldElementInfo info = new SourceFieldElementInfo();
		info.setName(name);
		info.setNameSourceStart(nameSourceStart);
		info.setNameSourceEnd(nameSourceEnd);
		info.setSourceRangeStart(declarationStart);
		info.setFlags(modifiers);
		info.setTypeName(type);

		parentInfo.addChild(handle);
		fNewElements.put(handle, info);

		fInfoStack.push(info);
		fHandleStack.push(handle);
}
/**
 * @see ISourceElementRequestor
 */
public void enterInitializer(
	int declarationSourceStart,
	int modifiers) {
		JavaElementInfo parentInfo = (JavaElementInfo) fInfoStack.peek();
		JavaElement parentHandle= (JavaElement)fHandleStack.peek();
		IInitializer handle = null;
		
		if (parentHandle.getElementType() == IJavaElement.TYPE) {
			handle = ((IType) parentHandle).getInitializer(1);
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		InitializerElementInfo info = new InitializerElementInfo();
		info.setSourceRangeStart(declarationSourceStart);
		info.setFlags(modifiers);

		parentInfo.addChild(handle);
		fNewElements.put(handle, info);

		fInfoStack.push(info);
		fHandleStack.push(handle);
}
/**
 * @see ISourceElementRequestor
 */
public void enterInterface(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] superinterfaces) {

	enterType(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, null, superinterfaces);

}
/**
 * @see ISourceElementRequestor
 */
public void enterMethod(
	int declarationStart,
	int modifiers,
	char[] returnType,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes) {

		enterMethod(declarationStart, modifiers, returnType, name, nameSourceStart,
			nameSourceEnd, parameterTypes, parameterNames, exceptionTypes, false);
}
/**
 * @see ISourceElementRequestor
 */
protected void enterMethod(
	int declarationStart,
	int modifiers,
	char[] returnType,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes,
	boolean isConstructor) {

		SourceTypeElementInfo parentInfo = (SourceTypeElementInfo) fInfoStack.peek();
		JavaElement parentHandle= (JavaElement)fHandleStack.peek();
		IMethod handle = null;

		// translate nulls to empty arrays
		if (parameterTypes == null) {
			parameterTypes= fgEmptyCharChar;
		}
		if (parameterNames == null) {
			parameterNames= fgEmptyCharChar;
		}
		if (exceptionTypes == null) {
			exceptionTypes= fgEmptyCharChar;
		}
		
		String[] parameterTypeSigs = convertTypeNamesToSigs(parameterTypes);
		if (parentHandle.getElementType() == IJavaElement.TYPE) {
			handle = new SourceMethod((IType) parentHandle, new String(name), parameterTypeSigs);
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		SourceMethodElementInfo info = new SourceMethodElementInfo();
		info.setSourceRangeStart(declarationStart);
		int flags = modifiers;
		info.setName(name);
		info.setNameSourceStart(nameSourceStart);
		info.setNameSourceEnd(nameSourceEnd);
		info.setConstructor(isConstructor);
		info.setFlags(flags);
		info.setArgumentNames(parameterNames);
		info.setArgumentTypeNames(parameterTypes);
		info.setReturnType(returnType == null ? new char[]{'v', 'o','i', 'd'} : returnType);
		info.setExceptionTypeNames(exceptionTypes);

		parentInfo.addChild(handle);
		fNewElements.put(handle, info);
		fInfoStack.push(info);
		fHandleStack.push(handle);
}
/**
 * Common processing for classes and interfaces.
 */
protected void enterType(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] superclass,
	char[][] superinterfaces) {

	char[] enclosingTypeName= null;
	char[] qualifiedName= null;
	
	JavaElementInfo parentInfo = (JavaElementInfo) fInfoStack.peek();
	JavaElement parentHandle= (JavaElement)fHandleStack.peek();
	IType handle = null;
	String nameString= new String(name);
	
	if (parentHandle.getElementType() == IJavaElement.COMPILATION_UNIT) {
		handle = ((ICompilationUnit) parentHandle).getType(nameString);
		if (fPackageName == null) {
			qualifiedName= nameString.toCharArray();
		} else {
			qualifiedName= (new String(fPackageName) + "." + nameString).toCharArray(); //$NON-NLS-1$
		}
	}
	else if (parentHandle.getElementType() == IJavaElement.TYPE) {
		handle = ((IType) parentHandle).getType(nameString);
		enclosingTypeName= ((SourceTypeElementInfo)parentInfo).getName();
		qualifiedName= (new String(((SourceTypeElementInfo)parentInfo).getQualifiedName()) + "." + nameString).toCharArray(); //$NON-NLS-1$
	}
	else {
		Assert.isTrue(false); // Should not happen
	}
	resolveDuplicates(handle);
	
	SourceTypeElementInfo info = new SourceTypeElementInfo();
	info.setHandle(handle);
	info.setSourceRangeStart(declarationStart);
	info.setFlags(modifiers);
	info.setName(name);
	info.setNameSourceStart(nameSourceStart);
	info.setNameSourceEnd(nameSourceEnd);
	info.setSuperclassName(superclass);
	info.setSuperInterfaceNames(superinterfaces);
	info.setEnclosingTypeName(enclosingTypeName);
	info.setSourceFileName(fSourceFileName);
	info.setPackageName(fPackageName);
	info.setQualifiedName(qualifiedName);
	for (Iterator iter = fNewElements.keySet().iterator(); iter.hasNext();){
		Object object = iter.next();
		if (object instanceof IImportDeclaration)
			info.addImport(((IImportDeclaration)object).getElementName().toCharArray());
	}
	

	parentInfo.addChild(handle);
	fNewElements.put(handle, info);

	fInfoStack.push(info);
	fHandleStack.push(handle);

}
/**
 * @see ISourceElementRequestor
 */
public void exitClass(int declarationEnd) {

	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitCompilationUnit(int declarationEnd) {
	fUnitInfo.setSourceLength(declarationEnd + 1);

	// determine if there were any parsing errors
	fUnitInfo.setIsStructureKnown(!this.hasSyntaxErrors);
}
/**
 * @see ISourceElementRequestor
 */
public void exitConstructor(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitField(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitInitializer(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitInterface(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * common processing for classes and interfaces
 */
protected void exitMember(int declarationEnd) {
	SourceRefElementInfo info = (SourceRefElementInfo) fInfoStack.pop();
	info.setSourceRangeEnd(declarationEnd);
	fHandleStack.pop();
}
/**
 * @see ISourceElementRequestor
 */
public void exitMethod(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * Resolves duplicate handles by incrementing the occurrence count
 * of the handle being created until there is no conflict.
 */
protected void resolveDuplicates(IJavaElement handle) {
	while (fNewElements.containsKey(handle)) {
		JavaElement h = (JavaElement) handle;
		h.setOccurrenceCount(h.getOccurrenceCount() + 1);
	}
}
}
