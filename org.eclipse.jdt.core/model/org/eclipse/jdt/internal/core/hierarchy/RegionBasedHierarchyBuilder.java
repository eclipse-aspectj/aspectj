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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.Util;

public class RegionBasedHierarchyBuilder extends HierarchyBuilder {
	
	public RegionBasedHierarchyBuilder(TypeHierarchy hierarchy)
		throws JavaModelException {
			
		super(hierarchy);
	}
	
public void build(boolean computeSubtypes) {
		
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	try {
		// optimize access to zip files while building hierarchy
		manager.cacheZipFiles();
				
		if (this.hierarchy.type == null || computeSubtypes) {
			ArrayList allTypesInRegion = determineTypesInRegion();
			this.hierarchy.initialize(allTypesInRegion.size());
			createTypeHierarchyBasedOnRegion(allTypesInRegion);
			((RegionBasedTypeHierarchy)this.hierarchy).pruneDeadBranches();
		} else {
			this.hierarchy.initialize(1);
			this.buildSupertypes();
		}
	} finally {
		manager.flushZipFiles();
	}
}
/**
 * Configure this type hierarchy that is based on a region.
 */
private void createTypeHierarchyBasedOnRegion(ArrayList allTypesInRegion) {
	
	int size = allTypesInRegion.size();
	if (size != 0) {
		this.infoToHandle = new HashMap(size);
	}
	IType[] types = new IType[size];
	allTypesInRegion.toArray(types);

	/*
	 * NOTE: To workaround pb with hierarchy resolver that requests top  
	 * level types in the process of caching an enclosing type, this needs to
	 * be sorted in reverse alphabetical order so that top level types are cached
	 * before their inner types.
	 */
	Util.sort(
		types,
		new Util.Comparer() {
			/**
			 * @see Comparer#compare(Object, Object)
			 */
			public int compare(Object a, Object b) {
				return - ((IJavaElement)a).getParent().getElementName().compareTo(((IJavaElement)b).getParent().getElementName());
			}
		}
	);

	// collect infos and compilation units
	ArrayList infos = new ArrayList();
	ArrayList units = new ArrayList();
	types : for (int i = 0; i < size; i++) {
		try {
			IType type = types[i];
			this.addInfoFromElement((Openable)type.getOpenable(), infos, units, type.getPath().toString());
			worked(1);
		} catch (JavaModelException npe) {
			continue types;
		}
	}

	// copy vectors into arrays
	IGenericType[] genericTypes;
	int infosSize = infos.size();
	if (infosSize > 0) {
		genericTypes = new IGenericType[infosSize];
		infos.toArray(genericTypes);
	} else {
		genericTypes = new IGenericType[0];
	}
	org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] compilationUnits;
	int unitsSize = units.size();
	if (unitsSize > 0) {
		compilationUnits = new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[unitsSize];
		units.toArray(compilationUnits);
	} else {
		compilationUnits = new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[0];
	}

	// resolve
	if (infosSize > 0 || unitsSize > 0) {
		IType focusType = this.getType();
		CompilationUnit unitToLookInside = null;
		if (focusType != null) {
			unitToLookInside = (CompilationUnit)focusType.getCompilationUnit();
		}
		if (this.nameLookup != null && unitToLookInside != null) {
			synchronized(this.nameLookup) { // prevent 2 concurrent accesses to name lookup while the working copies are set
				try {
					nameLookup.setUnitsToLookInside(new IWorkingCopy[] {unitToLookInside});
					this.hierarchyResolver.resolve(genericTypes, compilationUnits);
				} finally {
					nameLookup.setUnitsToLookInside(null);
				}
			}
		} else {
			this.hierarchyResolver.resolve(genericTypes, compilationUnits);
		}
	}
}
	
	/**
	 * Returns all of the types defined in the region of this type hierarchy.
	 */
	private ArrayList determineTypesInRegion() {

		ArrayList types = new ArrayList();
		IJavaElement[] roots =
			((RegionBasedTypeHierarchy) this.hierarchy).fRegion.getElements();
		for (int i = 0; i < roots.length; i++) {
			try {
				IJavaElement root = roots[i];
				switch (root.getElementType()) {
					case IJavaElement.JAVA_PROJECT :
						injectAllTypesForJavaProject((IJavaProject) root, types);
						break;
					case IJavaElement.PACKAGE_FRAGMENT_ROOT :
						injectAllTypesForPackageFragmentRoot((IPackageFragmentRoot) root, types);
						break;
					case IJavaElement.PACKAGE_FRAGMENT :
						injectAllTypesForPackageFragment((IPackageFragment) root, types);
						break;
					case IJavaElement.CLASS_FILE :
						types.add(((IClassFile) root).getType());
						break;
					case IJavaElement.COMPILATION_UNIT :
						IType[] cuTypes = ((ICompilationUnit) root).getAllTypes();
						for (int j = 0; j < cuTypes.length; j++) {
							types.add(cuTypes[j]);
						}
						break;
					case IJavaElement.TYPE :
						types.add(root);
						break;
					default :
						break;
				}
			} catch (JavaModelException e) {
				// just continue
			}
		}
		return types;
	}
	
	/**
	 * Adds all of the types defined within this java project to the
	 * list.
	 */
	private void injectAllTypesForJavaProject(
		IJavaProject project,
		ArrayList types) {
		try {
			IPackageFragmentRoot[] devPathRoots =
				((JavaProject) project).getPackageFragmentRoots();
			if (devPathRoots == null) {
				return;
			}
			for (int j = 0; j < devPathRoots.length; j++) {
				IPackageFragmentRoot root = devPathRoots[j];
				injectAllTypesForPackageFragmentRoot(root, types);
			}
		} catch (JavaModelException e) {
		}
	}
	
	/**
	 * Adds all of the types defined within this package fragment to the
	 * list.
	 */
	private void injectAllTypesForPackageFragment(
		IPackageFragment packFrag,
		ArrayList types) {
			
		try {
			IPackageFragmentRoot root = (IPackageFragmentRoot) packFrag.getParent();
			int kind = root.getKind();
			if (kind != 0) {
				boolean isSourcePackageFragment = (kind == IPackageFragmentRoot.K_SOURCE);
				if (isSourcePackageFragment) {
					ICompilationUnit[] typeContainers = packFrag.getCompilationUnits();
					injectAllTypesForTypeContainers(typeContainers, types);
				} else {
					IClassFile[] typeContainers = packFrag.getClassFiles();
					injectAllTypesForTypeContainers(typeContainers, types);
				}
			}
		} catch (JavaModelException e) {
		}
	}
	
	/**
	 * Adds all of the types defined within this package fragment root to the
	 * list.
	 */
	private void injectAllTypesForPackageFragmentRoot(
		IPackageFragmentRoot root,
		ArrayList types) {
		try {
			IJavaElement[] packFrags = root.getChildren();
			for (int k = 0; k < packFrags.length; k++) {
				IPackageFragment packFrag = (IPackageFragment) packFrags[k];
				injectAllTypesForPackageFragment(packFrag, types);
			}
		} catch (JavaModelException e) {
			return;
		}
	}
	
	/**
	 * Adds all of the types defined within the type containers (IClassFile).
	 */
	private void injectAllTypesForTypeContainers(
		IClassFile[] containers,
		ArrayList types) {
			
		try {
			for (int i = 0; i < containers.length; i++) {
				IClassFile cf = containers[i];
				types.add(cf.getType());
				this.worked(1);
			}
		} catch (JavaModelException e) {
		}
	}
	
	/**
	 * Adds all of the types defined within the type containers (ICompilationUnit).
	 */
	private void injectAllTypesForTypeContainers(
		ICompilationUnit[] containers,
		ArrayList types) {
			
		try {
			for (int i = 0; i < containers.length; i++) {
				ICompilationUnit cu = containers[i];
				IType[] cuTypes = cu.getAllTypes();
				for (int j = 0; j < cuTypes.length; j++) {
					types.add(cuTypes[j]);
				}
				this.worked(1);
			}
		} catch (JavaModelException e) {
		}
	}
}