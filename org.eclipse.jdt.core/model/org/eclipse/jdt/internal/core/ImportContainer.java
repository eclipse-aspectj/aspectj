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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @see IImportContainer
 */
public class ImportContainer extends SourceRefElement implements IImportContainer {
protected ImportContainer(ICompilationUnit parent) {
	super(IMPORT_CONTAINER, parent, ""); //$NON-NLS-1$
}
/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento(){
	return ((JavaElement)getParent()).getHandleMemento();
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	Assert.isTrue(false, Util.bind("assert.shouldNotImplement")); //$NON-NLS-1$
	return 0;
}
/**
 * @see IImportContainer
 */
public IImportDeclaration getImport(String name) {
	return new ImportDeclaration(this, name);
}
/**
 * @see ISourceReference
 */
public ISourceRange getSourceRange() throws JavaModelException {
	IJavaElement[] imports= getChildren();
	ISourceRange firstRange= ((ISourceReference)imports[0]).getSourceRange();
	ISourceRange lastRange= ((ISourceReference)imports[imports.length - 1]).getSourceRange();
	SourceRange range= new SourceRange(firstRange.getOffset(), lastRange.getOffset() + lastRange.getLength() - firstRange.getOffset());
	return range;
}
/**
 * Import containers only exist if they have children.
 * @see IParent
 */
public boolean hasChildren() throws JavaModelException {
	return true;
}
/**
 */
public String readableName() {

	return null;
}
/**
 * @private Debugging purposes
 */
protected void toString(int tab, StringBuffer buffer) {
	Object info = JavaModelManager.getJavaModelManager().peekAtInfo(this);
	if (info == null || !(info instanceof JavaElementInfo)) return;
	IJavaElement[] children = ((JavaElementInfo)info).getChildren();
	for (int i = 0; i < children.length; i++) {
		if (i > 0) buffer.append("\n"); //$NON-NLS-1$
		((JavaElement)children[i]).toString(tab, buffer);
	}
}
}
