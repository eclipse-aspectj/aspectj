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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Parent is an IClassFile.
 *
 * @see IType
 */

public class BinaryType extends BinaryMember implements IType {
	
	private static final IField[] NO_FIELDS = new IField[0];
	private static final IMethod[] NO_METHODS = new IMethod[0];
	private static final IType[] NO_TYPES = new IType[0];
	private static final IInitializer[] NO_INITIALIZERS = new IInitializer[0];
	private static final String[] NO_STRINGS = new String[0];
	
protected BinaryType(IJavaElement parent, String name) {
	super(TYPE, parent, name);
	Assert.isTrue(name.indexOf('.') == -1);
}
/**
 * @see IOpenable#close()
 */
public void close() throws JavaModelException {
	
	Object info = JavaModelManager.getJavaModelManager().peekAtInfo(this);
	if (info != null) {
		ClassFileInfo cfi = getClassFileInfo();
		if (cfi.hasReadBinaryChildren()) {
			try {
				IJavaElement[] children = getChildren();
				for (int i = 0, size = children.length; i < size; ++i) {
					JavaElement child = (JavaElement) children[i];
					if (child instanceof BinaryType) {
						((IOpenable)child.getParent()).close();
					} else {
						child.close();
					}
				}
			} catch (JavaModelException e) {
			}
		}
		closing(info);
		JavaModelManager.getJavaModelManager().removeInfo(this);
		if (JavaModelManager.VERBOSE){
			System.out.println("-> Package cache size = " + JavaModelManager.getJavaModelManager().cache.pkgSize()); //$NON-NLS-1$
			System.out.println("-> Openable cache filling ratio = " + JavaModelManager.getJavaModelManager().cache.openableFillingRatio() + "%"); //$NON-NLS-1$//$NON-NLS-2$
		}
	}
}
/**
 * Remove my cached children from the Java Model
 */
protected void closing(Object info) throws JavaModelException {
	if (JavaModelManager.VERBOSE){
		System.out.println("CLOSING Element ("+ Thread.currentThread()+"): " + this.toStringWithAncestors());  //$NON-NLS-1$//$NON-NLS-2$
	}
	ClassFileInfo cfi = getClassFileInfo();
	cfi.removeBinaryChildren();
	if (JavaModelManager.VERBOSE){
		System.out.println("-> Package cache size = " + JavaModelManager.getJavaModelManager().cache.pkgSize()); //$NON-NLS-1$
		System.out.println("-> Openable cache filling ratio = " + JavaModelManager.getJavaModelManager().cache.openableFillingRatio() + "%"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
/**
 * @see IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, ICompletionRequestor)
 */
public void codeComplete(char[] snippet,int insertion,int position,char[][] localVariableTypeNames,char[][] localVariableNames,int[] localVariableModifiers,boolean isStatic,ICompletionRequestor requestor) throws JavaModelException {
	if (requestor == null) {
		throw new IllegalArgumentException(Util.bind("codeAssist.nullRequestor")); //$NON-NLS-1$
	}
	
	SearchableEnvironment environment = (SearchableEnvironment) ((JavaProject) getJavaProject()).getSearchableNameEnvironment();
	NameLookup nameLookup = ((JavaProject) getJavaProject()).getNameLookup();
	CompletionEngine engine = new CompletionEngine(environment, new CompletionRequestorWrapper(requestor,nameLookup), JavaCore.getOptions());
	
	String source = getClassFile().getSource();
	if (source != null && insertion > -1 && insertion < source.length()) {
		String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING); 
		
		char[] prefix = CharOperation.concat(source.substring(0, insertion).toCharArray(), new char[]{'{'});
		char[] suffix =  CharOperation.concat(new char[]{'}'}, source.substring(insertion).toCharArray());
		char[] fakeSource = CharOperation.concat(prefix, snippet, suffix);
		
		BasicCompilationUnit cu = 
			new BasicCompilationUnit(
				fakeSource, 
				null,
				getElementName(),
				encoding); 

		engine.complete(cu, prefix.length + position, prefix.length);
	} else {
		engine.complete(this, snippet, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic);
	}
}
/**
 * @see IType#createField(String, IJavaElement, boolean, IProgressMonitor)
 */
public IField createField(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see IType#createInitializer(String, IJavaElement, IProgressMonitor)
 */
public IInitializer createInitializer(String contents, IJavaElement sibling, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see IType#createMethod(String, IJavaElement, boolean, IProgressMonitor)
 */
public IMethod createMethod(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see IType#createType(String, IJavaElement, boolean, IProgressMonitor)
 */
public IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see IType#findMethods(IMethod)
 */
public IMethod[] findMethods(IMethod method) {
	try {
		return this.findMethods(method, this.getMethods());
	} catch (JavaModelException e) {
		// if type doesn't exist, no matching method can exist
		return null;
	}
}
/**
 * @see IParent#getChildren()
 */
public IJavaElement[] getChildren() throws JavaModelException {
	// ensure present
	// fix for 1FWWVYT
	if (!exists()) {
		throw newNotPresentException();
	}
	// get children
	ClassFileInfo cfi = getClassFileInfo();
	return cfi.getBinaryChildren();
}
protected ClassFileInfo getClassFileInfo() throws JavaModelException {
	ClassFile cf = (ClassFile) fParent;
	return (ClassFileInfo) cf.getElementInfo();
}
/**
 * @see IMember#getDeclaringType()
 */
public IType getDeclaringType() {
	IClassFile classFile = this.getClassFile();
	if (classFile.isOpen()) {
		try {
			char[] enclosingTypeName = ((IBinaryType) getRawInfo()).getEnclosingTypeName();
			if (enclosingTypeName == null) {
				return null;
			}
		 	enclosingTypeName = ClassFile.unqualifiedName(enclosingTypeName);
		 	
			// workaround problem with class files compiled with javac 1.1.* 
			// that return a non-null enclosing type name for local types defined in anonymous (e.g. A$1$B)
			if (classFile.getElementName().length() > enclosingTypeName.length+1 
					&& Character.isDigit(classFile.getElementName().charAt(enclosingTypeName.length+1))) {
				return null;
			} 
			
			return getPackageFragment().getClassFile(new String(enclosingTypeName) + ".class").getType(); //$NON-NLS-1$;
		} catch (JavaModelException npe) {
			return null;
		}
	} else {
		// cannot access .class file without opening it 
		// and getDeclaringType() is supposed to be a handle-only method,
		// so default to assuming $ is an enclosing type separator
		String classFileName = classFile.getElementName();
		int lastDollar = -1;
		for (int i = 0, length = classFileName.length(); i < length; i++) {
			char c = classFileName.charAt(i);
			if (Character.isDigit(c) && lastDollar == i-1) {
				// anonymous or local type
				return null;
			} else if (c == '$') {
				lastDollar = i;
			}
		}
		if (lastDollar == -1) {
			return null;
		} else {
			String enclosingName = classFileName.substring(0, lastDollar);
			String enclosingClassFileName = enclosingName + ".class"; //$NON-NLS-1$
			return 
				new BinaryType(
					this.getPackageFragment().getClassFile(enclosingClassFileName),
					enclosingName.substring(enclosingName.lastIndexOf('$')+1));
		}
	}
}
/**
 * @see IType#getField(String name)
 */
public IField getField(String name) {
	return new BinaryField(this, name);
}
/**
 * @see IType#getFields()
 */
public IField[] getFields() throws JavaModelException {
	ArrayList list = getChildrenOfType(FIELD);
	int size;
	if ((size = list.size()) == 0) {
		return NO_FIELDS;
	} else {
		IField[] array= new IField[size];
		list.toArray(array);
		return array;
	}
}
/**
 * @see IMember#getFlags()
 */
public int getFlags() throws JavaModelException {
	IBinaryType info = (IBinaryType) getRawInfo();
	return info.getModifiers();
}
/**
 * @see IType#getFullyQualifiedName()
 */
public String getFullyQualifiedName() {
	return this.getFullyQualifiedName('$');
}
/**
 * @see IType#getFullyQualifiedName(char enclosingTypeSeparator)
 */
public String getFullyQualifiedName(char enclosingTypeSeparator) {
	String packageName = getPackageFragment().getElementName();
	if (packageName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
		return getTypeQualifiedName(enclosingTypeSeparator);
	}
	return packageName + '.' + getTypeQualifiedName(enclosingTypeSeparator);
}
/**
 * @see IType#getInitializer(int occurrenceCount)
 */
public IInitializer getInitializer(int occurrenceCount) {
	return new Initializer(this, occurrenceCount);
}
/**
 * @see IType#getInitializers()
 */
public IInitializer[] getInitializers() {
	return NO_INITIALIZERS;
}
/**
 * @see IType#getMethod(String name, String[] parameterTypeSignatures)
 */
public IMethod getMethod(String name, String[] parameterTypeSignatures) {
	return new BinaryMethod(this, name, parameterTypeSignatures);
}
/**
 * @see IType#getMethods()
 */
public IMethod[] getMethods() throws JavaModelException {
	ArrayList list = getChildrenOfType(METHOD);
	int size;
	if ((size = list.size()) == 0) {
		return NO_METHODS;
	} else {
		IMethod[] array= new IMethod[size];
		list.toArray(array);
		return array;
	}
}
/**
 * @see IType#getPackageFragment()
 */
public IPackageFragment getPackageFragment() {
	IJavaElement parent = fParent;
	while (parent != null) {
		if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
			return (IPackageFragment) parent;
		}
		else {
			parent = parent.getParent();
		}
	}
	Assert.isTrue(false);  // should not happen
	return null;
}
/**
 * @see IType#getSuperclassName()
 */
public String getSuperclassName() throws JavaModelException {
	IBinaryType info = (IBinaryType) getRawInfo();
	char[] superclassName = info.getSuperclassName();
	if (superclassName == null) {
		return null;
	}
	return new String(ClassFile.translatedName(superclassName));
}
/**
 * @see IType#getSuperInterfaceNames()
 */
public String[] getSuperInterfaceNames() throws JavaModelException {
	IBinaryType info = (IBinaryType) getRawInfo();
	char[][] names= info.getInterfaceNames();
	int length;
	if (names == null || (length = names.length) == 0) {
		return NO_STRINGS;
	}
	names= ClassFile.translatedNames(names);
	String[] strings= new String[length];
	for (int i= 0; i < length; i++) {
		strings[i]= new String(names[i]);
	}
	return strings;
}
/**
 * @see IType#getType(String)
 */
public IType getType(String name) {
	IClassFile classFile= getPackageFragment().getClassFile(getTypeQualifiedName() + "$" + name + ".class"); //$NON-NLS-2$ //$NON-NLS-1$
	return new BinaryType(classFile, name);
}
/**
 * @see IType#getTypeQualifiedName()
 */
public String getTypeQualifiedName() {
	return this.getTypeQualifiedName('$');
}
/**
 * @see IType#getTypeQualifiedName(char)
 */
public String getTypeQualifiedName(char enclosingTypeSeparator) {
	IType declaringType = this.getDeclaringType();
	if (declaringType == null) {
		String classFileName = this.getClassFile().getElementName();
		if (classFileName.indexOf('$') == -1) {
			// top level class file: name of type is same as name of class file
			return fName;
		} else {
			// anonymous or local class file
			return classFileName.substring(0, classFileName.lastIndexOf('.')); // remove .class
		}
	} else {
		return 
			declaringType.getTypeQualifiedName(enclosingTypeSeparator)
			+ enclosingTypeSeparator
			+ fName;
	}
}
/**
 * @see IType#getTypes()
 */
public IType[] getTypes() throws JavaModelException {
	ArrayList list = getChildrenOfType(TYPE);
	int size;
	if ((size = list.size()) == 0) {
		return NO_TYPES;
	} else {
		IType[] array= new IType[size];
		list.toArray(array);
		return array;
	}
}
/**
 * @see IParent#hasChildren()
 */
public boolean hasChildren() throws JavaModelException {
	return getChildren().length > 0;
}
/**
 * @see IType#isAnonymous()
 */
public boolean isAnonymous() throws JavaModelException {
	IBinaryType info = (IBinaryType) getRawInfo();
	return info.isAnonymous();
}
/**
 * @see IType#isClass()
 */
public boolean isClass() throws JavaModelException {
	return !isInterface();
}
/**
 * @see IType#isInterface()
 */
public boolean isInterface() throws JavaModelException {
	IBinaryType info = (IBinaryType) getRawInfo();
	return info.isInterface();
}

/**
 * @see IType#isLocal()
 */
public boolean isLocal() throws JavaModelException {
	IBinaryType info = (IBinaryType) getRawInfo();
	return info.isLocal();
}
/**
 * @see IType#isMember()
 */
public boolean isMember() throws JavaModelException {
	IBinaryType info = (IBinaryType) getRawInfo();
	return info.isMember();
}
/**
 * @see IType#newSupertypeHierarchy(IProgressMonitor monitor)
 */
public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
	return this.newSupertypeHierarchy(null, monitor);
}
/**
 * @see IType#newSupertypeHierarchy(IWorkingCopy[], IProgressMonitor)
 */
public ITypeHierarchy newSupertypeHierarchy(
	IWorkingCopy[] workingCopies,
	IProgressMonitor monitor)
	throws JavaModelException {
		
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, workingCopies, SearchEngine.createWorkspaceScope(), false);
	runOperation(op, monitor);
	return op.getResult();
}

/**
 * @see IType#newTypeHierarchy(IProgressMonitor monitor)
 */
public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
	return newTypeHierarchy((IWorkingCopy[])null, monitor);
}
/**
 * @see IType#newTypeHierarchy(IWorkingCopy[], IProgressMonitor)
 */
public ITypeHierarchy newTypeHierarchy(
	IWorkingCopy[] workingCopies,
	IProgressMonitor monitor)
	throws JavaModelException {

	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, workingCopies, SearchEngine.createWorkspaceScope(), true);
	runOperation(op, monitor);
	return op.getResult();
}

/**
 * @see IType#newTypeHierarchy(IJavaProject project, IProgressMonitor monitor)
 */
public ITypeHierarchy newTypeHierarchy(IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
	if (project == null) {
		throw new IllegalArgumentException(Util.bind("hierarchy.nullProject")); //$NON-NLS-1$
	}
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(
		this, 
		null, // no working copies
		SearchEngine.createJavaSearchScope(new IJavaElement[] {project}), 
		true);
	runOperation(op, monitor);
	return op.getResult();
}
/**
 * Removes all cached info from the Java Model, including all children,
 * but does not close this element.
 */
protected void removeInfo() {
	Object info = JavaModelManager.getJavaModelManager().peekAtInfo(this);
	if (info != null) {
		try {
			IJavaElement[] children = getChildren();
			for (int i = 0, size = children.length; i < size; ++i) {
				JavaElement child = (JavaElement) children[i];
				child.removeInfo();
			}
		} catch (JavaModelException e) {
		}
		JavaModelManager.getJavaModelManager().removeInfo(this);
		try {
			ClassFileInfo cfi = getClassFileInfo();
			cfi.removeBinaryChildren();
		} catch (JavaModelException npe) {
		}
	}
}
public String[][] resolveType(String typeName) throws JavaModelException {
	// not implemented for binary types
	return null;
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	if (info == null) {
		buffer.append(this.getElementName());
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		buffer.append(getElementName());
	} else {
		try {
			if (this.isInterface()) {
				buffer.append("interface "); //$NON-NLS-1$
			} else {
				buffer.append("class "); //$NON-NLS-1$
			}
			buffer.append(this.getElementName());
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
}
