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
import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;
import org.eclipse.jdt.internal.codeassist.SelectionEngine;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Handle for a source type. Info object is a SourceTypeElementInfo.
 *
 * Note: Parent is either an IClassFile, an ICompilationUnit or an IType.
 *
 * @see IType
 */

public class SourceType extends Member implements IType {
	/**
	 * An empty list of Strings
	 */
	protected static final String[] fgEmptyList= new String[] {};
protected SourceType(IJavaElement parent, String name) {
	super(TYPE, parent, name);
	Assert.isTrue(name.indexOf('.') == -1);
}
/**
 * @see IType
 */
public void codeComplete(char[] snippet,int insertion,int position,char[][] localVariableTypeNames,char[][] localVariableNames,int[] localVariableModifiers,boolean isStatic,ICompletionRequestor requestor) throws JavaModelException {
	if (requestor == null) {
		throw new IllegalArgumentException(Util.bind("codeAssist.nullRequestor")); //$NON-NLS-1$
	}
	
	SearchableEnvironment environment = (SearchableEnvironment) ((JavaProject) getJavaProject()).getSearchableNameEnvironment();
	NameLookup nameLookup = ((JavaProject) getJavaProject()).getNameLookup();
	CompletionEngine engine = new CompletionEngine(environment, new CompletionRequestorWrapper(requestor,nameLookup), JavaCore.getOptions());
	
	String source = getCompilationUnit().getSource();
	if (source != null && insertion > -1 && insertion < source.length()) {
		String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);
		
		char[] prefix = CharOperation.concat(source.substring(0, insertion).toCharArray(), new char[]{'{'});
		char[] suffix = CharOperation.concat(new char[]{'}'}, source.substring(insertion).toCharArray());
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
 * @see IType
 */
public IField createField(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateFieldOperation op = new CreateFieldOperation(this, contents, force);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	runOperation(op, monitor);
	return (IField) op.getResultElements()[0];
}
/**
 * @see IType
 */
public IInitializer createInitializer(String contents, IJavaElement sibling, IProgressMonitor monitor) throws JavaModelException {
	CreateInitializerOperation op = new CreateInitializerOperation(this, contents);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	runOperation(op, monitor);
	return (IInitializer) op.getResultElements()[0];
}
/**
 * @see IType
 */
public IMethod createMethod(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateMethodOperation op = new CreateMethodOperation(this, contents, force);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	runOperation(op, monitor);
	return (IMethod) op.getResultElements()[0];
}
/**
 * @see IType
 */
public IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateTypeOperation op = new CreateTypeOperation(this, contents, force);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	runOperation(op, monitor);
	return (IType) op.getResultElements()[0];
}
/**
 * @see JavaElement#equalsDOMNode
 */
protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
	return (node.getNodeType() == IDOMNode.TYPE) && super.equalsDOMNode(node);
}
/*
 * @see IType
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
 * @see IMember
 */
public IType getDeclaringType() {
	IJavaElement parent = getParent();
	while (parent != null) {
		if (parent.getElementType() == IJavaElement.TYPE) {
			return (IType) parent;
		} else
			if (parent instanceof IMember) {
				parent = parent.getParent();
			} else {
				return null;
			}
	}
	return null;
}
/**
 * @see IType#getField
 */
public IField getField(String name) {
	return new SourceField(this, name);
}
/**
 * @see IType
 */
public IField[] getFields() throws JavaModelException {
	ArrayList list = getChildrenOfType(FIELD);
	IField[] array= new IField[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IType#getFullyQualifiedName
 */
public String getFullyQualifiedName() {
	return this.getFullyQualifiedName('$');
}
/**
 * @see IType#getFullyQualifiedName(char)
 */
public String getFullyQualifiedName(char enclosingTypeSeparator) {
	String packageName = getPackageFragment().getElementName();
	if (packageName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
		return getTypeQualifiedName(enclosingTypeSeparator);
	}
	return packageName + '.' + getTypeQualifiedName(enclosingTypeSeparator);
}

/**
 * @see IType
 */
public IInitializer getInitializer(int occurrenceCount) {
	return new Initializer(this, occurrenceCount);
}
/**
 * @see IType
 */
public IInitializer[] getInitializers() throws JavaModelException {
	ArrayList list = getChildrenOfType(INITIALIZER);
	IInitializer[] array= new IInitializer[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IType#getMethod
 */
public IMethod getMethod(String name, String[] parameterTypeSignatures) {
	return new SourceMethod(this, name, parameterTypeSignatures);
}
/**
 * @see IType
 */
public IMethod[] getMethods() throws JavaModelException {
	ArrayList list = getChildrenOfType(METHOD);
	IMethod[] array= new IMethod[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IType
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
 * @see IType
 */
public String getSuperclassName() throws JavaModelException {
	SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
	char[] superclassName= info.getSuperclassName();
	if (superclassName == null) {
		return null;
	}
	return new String(superclassName);
}
/**
 * @see IType
 */
public String[] getSuperInterfaceNames() throws JavaModelException {
	SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
	char[][] names= info.getInterfaceNames();
	if (names == null) {
		return fgEmptyList;
	}
	String[] strings= new String[names.length];
	for (int i= 0; i < names.length; i++) {
		strings[i]= new String(names[i]);
	}
	return strings;
}
/**
 * @see IType
 */
public IType getType(String name) {
	return new SourceType(this, name);
}
/**
 * @see IType#getTypeQualifiedName
 */
public String getTypeQualifiedName() {
	return this.getTypeQualifiedName('$');
}
/**
 * @see IType#getTypeQualifiedName(char)
 */
public String getTypeQualifiedName(char enclosingTypeSeparator) {
	if (fParent.getElementType() == IJavaElement.COMPILATION_UNIT) {
		return fName;
	} else {
		return ((IType) fParent).getTypeQualifiedName(enclosingTypeSeparator) + enclosingTypeSeparator + fName;
	}
}

/**
 * @see IType
 */
public IType[] getTypes() throws JavaModelException {
	ArrayList list= getChildrenOfType(TYPE);
	IType[] array= new IType[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IParent 
 */
public boolean hasChildren() throws JavaModelException {
	return getChildren().length > 0;
}
/**
 * @see IType#isAnonymous()
 */
public boolean isAnonymous() throws JavaModelException {
	return false; // cannot create source handle onto anonymous types
}
/**
 * @see IType
 */
public boolean isClass() throws JavaModelException {
	return !isInterface();
}
/**
 * @see IType
 */
public boolean isInterface() throws JavaModelException {
	SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
	return info.isInterface();
}
/**
 * @see IType#isLocal()
 */
public boolean isLocal() throws JavaModelException {
	return false; // cannot create source handle onto local types
}
/**
 * @see IType#isMember()
 */
public boolean isMember() throws JavaModelException {
	return getDeclaringType() == null;
}

/**
 * @see IType
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
 * @see IType
 */
public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
	return this.newTypeHierarchy((IWorkingCopy[])null, monitor);
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
 * @see IType
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
 * See ISourceType.resolveType(...)
 */

 public String[][] resolveType(String typeName) throws JavaModelException {
	ISourceType info = (ISourceType) this.getElementInfo();
	ISearchableNameEnvironment environment = ((JavaProject)getJavaProject()).getSearchableNameEnvironment();

	class TypeResolveRequestor implements ISelectionRequestor {
		String[][] answers = null;
		void acceptType(String[] answer){
			if (answers == null) {
				answers = new String[][]{ answer };
			} else {
				// grow
				int length = answers.length;
				System.arraycopy(answers, 0, answers = new String[length+1][], 0, length);
				answers[length] = answer;
			}
		}
		public void acceptClass(char[] packageName, char[] className, boolean needQualification) {
			acceptType(new String[]  { new String(packageName), new String(className) });
		}
		
		public void acceptInterface(char[] packageName, char[] interfaceName, boolean needQualification) {
			acceptType(new String[]  { new String(packageName), new String(interfaceName) });
		}

		public void acceptError(IProblem error) {}
		public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name) {}
		public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, boolean isConstructor) {}
		public void acceptPackage(char[] packageName){}

	}
	TypeResolveRequestor requestor = new TypeResolveRequestor();
	SelectionEngine engine = 
		new SelectionEngine(environment, requestor, JavaCore.getOptions());
		
	engine.selectType(info, typeName.toCharArray(), false);
	return requestor.answers;
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
