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
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.CreateTypeHierarchyOperation;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;

public abstract class HierarchyBuilder implements IHierarchyRequestor {
	/**
	 * The hierarchy being built.
	 */
	protected TypeHierarchy hierarchy;
	/**
	 * The name environment used by the HierarchyResolver
	 */
	protected SearchableEnvironment searchableEnvironment;
	/**
	 * @see NameLookup
	 */
	protected NameLookup nameLookup;
	/**
	 * The resolver used to resolve type hierarchies
	 * @see HierarchyResolver
	 */
	protected HierarchyResolver hierarchyResolver;
	/**
	 * A temporary cache of infos to handles to speed info
	 * to handle translation - it only contains the entries
	 * for the types in the region (i.e. no supertypes outside
	 * the region).
	 */
	protected Map infoToHandle;
	public HierarchyBuilder(TypeHierarchy hierarchy) throws JavaModelException {
		
		this.hierarchy = hierarchy;
		JavaProject project = (JavaProject) hierarchy.javaProject();
		this.searchableEnvironment =
			(SearchableEnvironment) project.getSearchableNameEnvironment();
		this.nameLookup = project.getNameLookup();
		this.hierarchyResolver =
			new HierarchyResolver(
				this.searchableEnvironment,
				JavaCore.getOptions(),
				this,
				new DefaultProblemFactory());
		this.infoToHandle = new HashMap(5);
	}
	
	public abstract void build(boolean computeSubtypes)
		throws JavaModelException, CoreException;
	/**
	 * Configure this type hierarchy by computing the supertypes only.
	 */
	protected void buildSupertypes() {
		IType focusType = this.getType();
		if (focusType == null)
			return;
		// get generic type from focus type
		IGenericType type;
		try {
			type = (IGenericType) ((JavaElement) focusType).getRawInfo();
		} catch (JavaModelException e) {
			// if the focus type is not present, or if cannot get workbench path
			// we cannot create the hierarchy
			return;
		}
		//NB: no need to set focus type on hierarchy resolver since no other type is injected
		//    in the hierarchy resolver, thus there is no need to check that a type is 
		//    a sub or super type of the focus type.
		org.eclipse.jdt.core.ICompilationUnit unitToLookInside = focusType.getCompilationUnit();
		if (nameLookup != null) {
			synchronized(nameLookup) { // prevent 2 concurrent accesses to name lookup while the working copies are set
				IWorkingCopy[] workingCopies = this.getWokingCopies();
				IWorkingCopy[] unitsToLookInside;
				if (unitToLookInside != null) {
					int wcLength = workingCopies == null ? 0 : workingCopies.length;
					if (wcLength == 0) {
						unitsToLookInside = new IWorkingCopy[] {unitToLookInside};
					} else {
						unitsToLookInside = new IWorkingCopy[wcLength+1];
						unitsToLookInside[0] = unitToLookInside;
						System.arraycopy(workingCopies, 0, unitsToLookInside, 1, wcLength);
					}
				} else {
					unitsToLookInside = workingCopies;
				}
				try {
					nameLookup.setUnitsToLookInside(unitsToLookInside);
					// resolve
					this.hierarchyResolver.resolve(type);
				} finally {
					nameLookup.setUnitsToLookInside(null);
				}
			}
		} else {
			// resolve
			this.hierarchyResolver.resolve(type);
		}
		// Add focus if not already in (case of a type with no explicit super type)
		if (!this.hierarchy.contains(focusType)) {
			this.hierarchy.addRootClass(focusType);
		}
	}
	/**
	 * @see IHierarchyRequestor
	 */
	public void connect(
		IGenericType suppliedType,
		IGenericType superclass,
		IGenericType[] superinterfaces) {
		this.worked(1);
		// convert all infos to handles
		IType typeHandle = getHandle(suppliedType);
		/*
		 * Temporary workaround for 1G2O5WK: ITPJCORE:WINNT - NullPointerException when selecting "Show in Type Hierarchy" for a inner class
		 */
		if (typeHandle == null)
			return;
		IType superHandle = null;
		if (superclass != null) {
			if (superclass instanceof HierarchyResolver.MissingType) {
				this.hierarchy.missingTypes.add(((HierarchyResolver.MissingType)superclass).simpleName);
			} else {
				superHandle = getHandle(superclass);
			}
		}
		IType[] interfaceHandles = null;
		if (superinterfaces != null && superinterfaces.length > 0) {
			int length = superinterfaces.length;
			IType[] resolvedInterfaceHandles = new IType[length];
			int index = 0;
			for (int i = 0; i < length; i++) {
				IGenericType superInterface = superinterfaces[i];
				if (superInterface != null) {
					if (superInterface instanceof HierarchyResolver.MissingType) {
						this.hierarchy.missingTypes.add(((HierarchyResolver.MissingType)superInterface).simpleName);
					} else {
						resolvedInterfaceHandles[index] = getHandle(superInterface);
						if (resolvedInterfaceHandles[index] != null) {
							index++;
						}
					}
				}
			}
			// resize
			System.arraycopy(
				resolvedInterfaceHandles,
				0,
				interfaceHandles = new IType[index],
				0,
				index);
		}
		if (TypeHierarchy.DEBUG) {
			System.out.println(
				"Connecting: " + ((JavaElement) typeHandle).toStringWithAncestors()); //$NON-NLS-1$
			System.out.println(
				"  to superclass: " //$NON-NLS-1$
					+ (superHandle == null
						? "<None>" //$NON-NLS-1$
						: ((JavaElement) superHandle).toStringWithAncestors()));
			System.out.print("  and superinterfaces:"); //$NON-NLS-1$
			if (interfaceHandles == null || interfaceHandles.length == 0) {
				System.out.println(" <None>"); //$NON-NLS-1$
			} else {
				System.out.println();
				for (int i = 0, length = interfaceHandles.length; i < length; i++) {
					System.out.println(
						"    " + ((JavaElement) interfaceHandles[i]).toStringWithAncestors()); //$NON-NLS-1$
				}
			}
		}
		// now do the caching
		if (suppliedType.isClass()) {
			if (superHandle == null) {
				this.hierarchy.addRootClass(typeHandle);
			} else {
				this.hierarchy.cacheSuperclass(typeHandle, superHandle);
			}
		} else {
			this.hierarchy.addInterface(typeHandle);
		}
		if (interfaceHandles == null) {
			interfaceHandles = this.hierarchy.NO_TYPE;
		}
		this.hierarchy.cacheSuperInterfaces(typeHandle, interfaceHandles);
		 
		// record flags
		this.hierarchy.cacheFlags(typeHandle, suppliedType.getModifiers());
	}
	/**
	 * Returns a handle for the given generic type or null if not found.
	 */
	protected IType getHandle(IGenericType genericType) {
		if (genericType == null)
			return null;
		if (genericType.isBinaryType()) {
			IClassFile classFile = (IClassFile) this.infoToHandle.get(genericType);
			// if it's null, it's from outside the region, so do lookup
			if (classFile == null) {
				IType handle = lookupBinaryHandle((IBinaryType) genericType);
				if (handle == null)
					return null;
				// case of an anonymous type (see 1G2O5WK: ITPJCORE:WINNT - NullPointerException when selecting "Show in Type Hierarchy" for a inner class)
				// optimization: remember the handle for next call (case of java.io.Serializable that a lot of classes implement)
				this.infoToHandle.put(genericType, handle.getParent());
				return handle;
			} else {
				try {
					return classFile.getType();
				} catch (JavaModelException e) {
					return null;
				}
			}
		} else if (genericType instanceof SourceTypeElementInfo) {
			return ((SourceTypeElementInfo) genericType).getHandle();
		} else
			return null;
	}
	protected IType getType() {
		return this.hierarchy.getType();
	}
protected IWorkingCopy[] getWokingCopies() {
	if (this.hierarchy.progressMonitor instanceof CreateTypeHierarchyOperation) {
		return ((CreateTypeHierarchyOperation)this.hierarchy.progressMonitor).workingCopies;
	} else {
		return null;
	}
}
	/**
	 * Looks up and returns a handle for the given binary info.
	 */
	protected IType lookupBinaryHandle(IBinaryType typeInfo) {
		int flag;
		String qualifiedName;
		if (typeInfo.isClass()) {
			flag = this.nameLookup.ACCEPT_CLASSES;
		} else {
			flag = this.nameLookup.ACCEPT_INTERFACES;
		}
		char[] bName = typeInfo.getName();
		qualifiedName = new String(ClassFile.translatedName(bName));
		return this.nameLookup.findType(qualifiedName, false, flag);
	}
	protected void worked(int work) {
		IProgressMonitor progressMonitor = this.hierarchy.progressMonitor;
		if (progressMonitor != null) {
			if (progressMonitor.isCanceled()) {
				throw new OperationCanceledException();
			} else {
				progressMonitor.worked(work);
			}
		}
	}
/**
 * Create an ICompilationUnit info from the given compilation unit on disk.
 */
protected ICompilationUnit createCompilationUnitFromPath(Openable handle, String osPath) throws JavaModelException {
	String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);
	return 
		new BasicCompilationUnit(
			null,
			null,
			osPath,
			encoding);
}
	/**
 * Creates the type info from the given class file on disk and
 * adds it to the given list of infos.
 */
protected IGenericType createInfoFromClassFile(Openable handle, String osPath) throws JavaModelException {
	IGenericType info = null;
	try {
		info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(osPath);
	} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
		e.printStackTrace();
		return null;
	} catch (java.io.IOException e) {
		e.printStackTrace();
		return null;
	}						
	this.infoToHandle.put(info, handle);
	return info;
}
	/**
 * Create a type info from the given class file in a jar and adds it to the given list of infos.
 */
protected IGenericType createInfoFromClassFileInJar(Openable classFile) throws JavaModelException {
	IJavaElement pkg = classFile.getParent();
	String classFilePath = pkg.getElementName().replace('.', '/') + "/" + classFile.getElementName(); //$NON-NLS-1$
	IGenericType info = null;
	java.util.zip.ZipFile zipFile = null;
	try {
		zipFile = ((JarPackageFragmentRoot)pkg.getParent()).getJar();
		info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(
			zipFile,
			classFilePath);
	} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
		e.printStackTrace();
		return null;
	} catch (java.io.IOException e) {
		e.printStackTrace();
		return null;
	} catch (CoreException e) {
		e.printStackTrace();
		return null;
	} finally {
		JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
	}
	this.infoToHandle.put(info, classFile);
	return info;
}

protected void addInfoFromClosedElement(Openable handle, ArrayList infos, ArrayList units, String resourcePath) throws JavaModelException {
	
	// create a temporary info
	IJavaElement pkg = handle.getParent();
	PackageFragmentRoot root = (PackageFragmentRoot)pkg.getParent();
	if (root.isArchive()) {
		// class file in a jar
		IGenericType info = this.createInfoFromClassFileInJar(handle);
		if (info != null) {
			infos.add(info);
		}
	} else {
		// file in a directory
		IPath path = new Path(resourcePath);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		IPath location = file.getLocation();
		if (location != null){
			String osPath = location.toOSString();
			if (handle instanceof CompilationUnit) {
				// compilation unit in a directory
				ICompilationUnit unit = this.createCompilationUnitFromPath(handle, osPath);
				if (unit != null) {
					units.add(unit);
				}
			} else if (handle instanceof ClassFile) {
				// class file in a directory
				IGenericType info = this.createInfoFromClassFile(handle, osPath);
				if (info != null) {
					infos.add(info);
				}
			}
		}
	}
	
}

/**
 * Add the type info from the given CU to the given list of infos.
 */
protected void addInfoFromOpenCU(CompilationUnit cu, ArrayList infos) throws JavaModelException {
	IType[] types = cu.getTypes();
	for (int j = 0; j < types.length; j++) {
		SourceType type = (SourceType)types[j];
		this.addInfoFromOpenSourceType(type, infos);
	}
}


/**
 * Add the type info from the given CU to the given list of infos.
 */
protected void addInfoFromOpenSourceType(SourceType type, ArrayList infos) throws JavaModelException {
	IGenericType info = (IGenericType)type.getRawInfo();
	infos.add(info);
	this.infoToHandle.put(info, type);
	IType[] members = type.getTypes();
	for (int i = 0; i < members.length; i++) {
		this.addInfoFromOpenSourceType((SourceType)members[i], infos);
	}
}

/**
 * Add the type info from the given class file to the given list of infos.
 */
protected void addInfoFromOpenClassFile(ClassFile classFile, ArrayList infos) throws JavaModelException {
	IType type = classFile.getType();
	IGenericType info = (IGenericType) ((BinaryType) type).getRawInfo();
	infos.add(info);
	this.infoToHandle.put(info, classFile);
}

protected void addInfoFromElement(Openable handle, ArrayList infos, ArrayList units, String resourcePath) throws JavaModelException {
	if (handle.isOpen()) {
		// reuse the info from the java model cache
		if (handle instanceof CompilationUnit) {
			this.addInfoFromOpenCU((CompilationUnit)handle, infos);
		} else if (handle instanceof ClassFile) {
			this.addInfoFromOpenClassFile((ClassFile)handle, infos);
		}
	} else {
		this.addInfoFromClosedElement(handle, infos, units, resourcePath);
	}
}




}