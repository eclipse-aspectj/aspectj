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
package org.eclipse.jdt.internal.core.hierarchy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.Region;
import org.eclipse.jdt.internal.core.TypeVector;

public class RegionBasedTypeHierarchy extends TypeHierarchy {
	/**
	 * The region of types for which to build the hierarchy
	 */
	protected IRegion fRegion;

	/**
	 * The Java Project in which the hierarchy is being built - this
	 * provides the context (i.e. classpath and namelookup rules)
	 */
	protected IJavaProject fProject;
/**
 * Creates a TypeHierarchy on the types in the specified region,
 * using the given project for a name lookup contenxt. If a specific
 * type is also specified, the type hierarchy is pruned to only
 * contain the branch including the specified type.
 */
public RegionBasedTypeHierarchy(IRegion region, IJavaProject project, IType type, boolean computeSubtypes) throws JavaModelException {
	super(type, null, computeSubtypes);
	fRegion = region;
	fProject = project;
}
/**
 * Activates this hierarchy for change listeners
 */
protected void activate() {
	super.activate();
	IJavaElement[] roots = fRegion.getElements();
	for (int i = 0; i < roots.length; i++) {
		IJavaElement root = roots[i];
		if (root instanceof IOpenable) {
			this.files.put(root, root);
		} else {
			Openable o = (Openable) ((JavaElement) root).getOpenableParent();
			if (o != null) {
				this.files.put(o, o);
			}
		}
		checkCanceled();
	}
}
/**
 * Compute this type hierarchy.
 */
protected void compute() throws JavaModelException, CoreException {
	HierarchyBuilder builder = new RegionBasedHierarchyBuilder(this);
	builder.build(this.computeSubtypes);
}
protected void destroy() {
	fRegion = new Region();
	super.destroy();	
}
protected boolean isAffectedByOpenable(IJavaElementDelta delta, IJavaElement element) {
	// ignore changes to working copies
	if (element instanceof CompilationUnit && ((CompilationUnit)element).isWorkingCopy()) {
		return false;
	}

	// if no focus, hierarchy is affected if the element is part of the region
	if (this.type == null) {
		return fRegion.contains(element);
	} else {
		return super.isAffectedByOpenable(delta, element);
	}
}
/**
 * Returns the java project this hierarchy was created in.
 */
public IJavaProject javaProject() {
	return fProject;
}
public void pruneDeadBranches() {
	this.pruneDeadBranches(this.getRootClasses(), false);
}
private void pruneDeadBranches(IType[] types, boolean superInRegion) {
	for (int i = 0, length = types.length; i < length; i++) {
		IType type = types[i];
		if (fRegion.contains(type)) {
			TypeVector subtypes = (TypeVector)this.typeToSubtypes.get(type);
			if (subtypes != null) {
				this.pruneDeadBranches(subtypes.copy().elements(), true);
			}
		} else {
			if (superInRegion) {
				this.removeType(type);
			} else {
				TypeVector subtypes = (TypeVector)this.typeToSubtypes.get(type);
				if (subtypes != null) {
					this.pruneDeadBranches(subtypes.copy().elements(), false);
				}
				subtypes = (TypeVector)this.typeToSubtypes.get(type);
				if (subtypes == null || subtypes.size == 0) {
					this.removeType(type);
				} 
			}
		}
	}
}
/**
 * Removes all the subtypes of the given type from the type hierarchy,
 * removes its superclass entry and removes the references from its super types.
 */
protected void removeType(IType type) {
	IType[] subtypes = this.getSubtypes(type);
	this.typeToSubtypes.remove(type);
	if (subtypes != null) {
		for (int i= 0; i < subtypes.length; i++) {
			this.removeType(subtypes[i]);
		}
	}
	IType superclass = (IType)this.classToSuperclass.remove(type);
	if (superclass != null) {
		TypeVector types = (TypeVector)this.typeToSubtypes.get(superclass);
		if (types != null) types.remove(type);
	}
	IType[] superinterfaces = (IType[])this.typeToSuperInterfaces.remove(type);
	if (superinterfaces != null) {
		for (int i = 0, length = superinterfaces.length; i < length; i++) {
			IType superinterface = superinterfaces[i];
			TypeVector types = (TypeVector)this.typeToSubtypes.get(superinterface);
			if (types != null) types.remove(type);
		}
	}
}

}
