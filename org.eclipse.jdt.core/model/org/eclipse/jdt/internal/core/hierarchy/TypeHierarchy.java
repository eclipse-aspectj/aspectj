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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeHierarchyChangedListener;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.ImportContainer;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.Region;
import org.eclipse.jdt.internal.core.TypeVector;
import org.eclipse.jdt.internal.core.Util;

/**
 * @see ITypeHierarchy
 */
public class TypeHierarchy implements ITypeHierarchy, IElementChangedListener {
	public static boolean DEBUG = false;
	/**
	 * The type the hierarchy was specifically computed for,
	 * possibly null.
	 */
	protected IType type;

	protected Map classToSuperclass;
	protected Map typeToSuperInterfaces;
	protected Map typeToSubtypes;
	protected Map typeFlags;
	protected TypeVector rootClasses = new TypeVector();
	protected ArrayList interfaces = new ArrayList(10);
	public ArrayList missingTypes = new ArrayList(4);
	
	protected static final IType[] NO_TYPE = new IType[0];
	
	/**
	 * The progress monitor to report work completed too.
	 */
	protected IProgressMonitor progressMonitor = null;

	/**
	 * Change listeners - null if no one is listening.
	 */
	protected ArrayList changeListeners = null;

	/**
	 * A set of the compilation units and class
	 * files that are considered in this hierarchy. Null if
	 * not activated.
	 */
	protected Map files = null;

	/**
	 * A region describing the packages considered by this
	 * hierarchy. Null if not activated.
	 */
	protected Region packageRegion = null;

	/**
	 * A region describing the package fragment roots considered by this
	 * hierarchy. Null if not activated.
	 */
	protected Region rootRegion = null;
	
	/**
	 * A region describing the projects considered by this
	 * hierarchy. Null if not activated.
	 */
	protected Region projectRegion = null;

	/**
	 * A boolean indicating if this hierarchy is actively tracking changes
	 * in the Java Model.
	 */
	protected boolean isActivated = false;

	/**
	 * A boolean indicating if the hierarchy exists
	 *
	 * fix for 1FW67PA
	 */
	protected boolean exists = true;
	
	/**
	 * Whether this hierarchy should contains subtypes.
	 */
	protected boolean computeSubtypes;

	/**
	 * The scope this hierarchy should restrain itsef in.
	 */
	IJavaSearchScope scope;

/**
 * Creates a TypeHierarchy on the given type.
 */
public TypeHierarchy(IType type, IJavaSearchScope scope, boolean computeSubtypes) throws JavaModelException {
	this.type = type;
	this.computeSubtypes = computeSubtypes;
	this.scope = scope;
}
/**
 * Activates this hierarchy for change listeners
 */
protected void activate() {

	// determine my file, package, root, & project regions.
	this.files = new HashMap(5);
	this.projectRegion = new Region();
	this.packageRegion = new Region();
	this.rootRegion = new Region();
	IType[] types = getAllTypes();
	for (int i = 0; i < types.length; i++) {
		IType type = types[i];
		Openable o = (Openable) ((JavaElement) type).getOpenableParent();
		if (o != null) {
			this.files.put(o, o);
		}
		IPackageFragment pkg = type.getPackageFragment();
		this.packageRegion.add(pkg);
		this.rootRegion.add(pkg.getParent());
		IJavaProject project = type.getJavaProject();
		if (project != null) {
			this.projectRegion.add(project);
		}
		checkCanceled();
	}
	JavaCore.addElementChangedListener(this);
	this.isActivated = true;
}
/**
 * Adds all of the elements in the collection to the list if the
 * element is not already in the list.
 */
private void addAllCheckingDuplicates(ArrayList list, IType[] collection) {
	for (int i = 0; i < collection.length; i++) {
		IType element = collection[i];
		if (!list.contains(element)) {
			list.add(element);
		}
	}
}
/**
 * Adds the type to the collection of interfaces.
 */
protected void addInterface(IType type) {
	this.interfaces.add(type);
}
/**
 * Adds the type to the collection of root classes
 * if the classes is not already present in the collection.
 */
protected void addRootClass(IType type) {
	if (this.rootClasses.contains(type)) return;
	this.rootClasses.add(type);
}
/**
 * Adds the given subtype to the type.
 */
protected void addSubtype(IType type, IType subtype) {
	TypeVector subtypes = (TypeVector)this.typeToSubtypes.get(type);
	if (subtypes == null) {
		subtypes = new TypeVector();
		this.typeToSubtypes.put(type, subtypes);
	}
	if (!subtypes.contains(subtype)) {
		subtypes.add(subtype);
	}
}
/**
 * @see ITypeHierarchy
 */
public void addTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener) {
	if (this.changeListeners == null) {
		this.changeListeners = new ArrayList();
		// fix for 1FW67PA
		if (this.exists) {
			activate();
		}
	}
	// add listener only if it is not already present
	if (this.changeListeners.indexOf(listener) == -1) {
		this.changeListeners.add(listener);
	}
}
/**
 * cacheFlags.
 */
public void cacheFlags(IType type, int flags) {
	this.typeFlags.put(type, new Integer(flags));
}
/**
 * Caches the handle of the superclass for the specified type.
 * As a side effect cache this type as a subtype of the superclass.
 */
protected void cacheSuperclass(IType type, IType superclass) {
	if (superclass != null) {
		this.classToSuperclass.put(type, superclass);
		addSubtype(superclass, type);
	} 
}
/**
 * Caches all of the superinterfaces that are specified for the
 * type.
 */
protected void cacheSuperInterfaces(IType type, IType[] superinterfaces) {
	this.typeToSuperInterfaces.put(type, superinterfaces);
	for (int i = 0; i < superinterfaces.length; i++) {
		IType superinterface = superinterfaces[i];
		if (superinterface != null) {
			addSubtype(superinterface, type);
		}
	}
}
/**
 * Checks with the progress monitor to see whether the creation of the type hierarchy
 * should be canceled. Should be regularly called
 * so that the user can cancel.
 *
 * @exception OperationCanceledException if cancelling the operation has been requested
 * @see IProgressMonitor#isCanceled
 */
protected void checkCanceled() {
	if (this.progressMonitor != null && this.progressMonitor.isCanceled()) {
		throw new OperationCanceledException();
	}
}
/**
 * Compute this type hierarchy.
 */
protected void compute() throws JavaModelException, CoreException {
	if (this.type != null) {
		HierarchyBuilder builder = 
			new IndexBasedHierarchyBuilder(
				this, 
				this.scope);
		builder.build(this.computeSubtypes);
	} // else a RegionBasedTypeHierarchy should be used
}
/**
 * @see ITypeHierarchy
 */
public boolean contains(IType type) {
	// classes
	if (this.classToSuperclass.get(type) != null) {
		return true;
	}

	// root classes
	if (this.rootClasses.contains(type)) return true;

	// interfaces
	if (this.interfaces.contains(type)) return true;
	
	return false;
}
/**
 * Deactivates this hierarchy for change listeners
 */
protected void deactivate() {
	JavaModelManager.getJavaModelManager().removeElementChangedListener(this);
	this.files= null;
	this.packageRegion= null;
	this.rootRegion= null;
	this.projectRegion= null;
	this.changeListeners= null;
	this.isActivated= false;
}
/**
 * Empties this hierarchy.
 *
 * fix for 1FW67PA
 */
protected void destroy() {
	this.exists = false;
	this.classToSuperclass = new HashMap(1);
	this.files = new HashMap(5);
	this.interfaces = new ArrayList(0);
	this.packageRegion = new Region();
	this.projectRegion = new Region();
	this.rootClasses = new TypeVector();
	this.rootRegion = new Region();
	this.typeToSubtypes = new HashMap(1);
	this.typeFlags = new HashMap(1);
	this.typeToSuperInterfaces = new HashMap(1);
	this.missingTypes = new ArrayList(4);
	JavaModelManager.getJavaModelManager().removeElementChangedListener(this);
}
/**
 * Determines if the change effects this hierarchy, and fires
 * change notification if required.
 */
public void elementChanged(ElementChangedEvent event) {
	// fix for 1FW67PA
	if (this.exists && this.isActivated()) {
		if (exists()) {
			if (isAffected(event.getDelta())) {
				fireChange();
			}
		} else {
			destroy();
			fireChange();
		}
	}

}
/**
 * @see ITypeHierarchy
 *
 * fix for 1FW67PA
 */
public boolean exists() {
	if (this.exists) {
		this.exists = (this.type == null || (this.type != null && this.type.exists())) && this.javaProject().exists();
		if (!this.exists) {
			destroy();
		}
	}
	return this.exists;
}
/**
 * Notifies listeners that this hierarchy has changed and needs
 * refreshing. Note that listeners can be removed as we iterate
 * through the list.
 */
protected void fireChange() {
	if (this.changeListeners == null) {
		return;
	}
	if (DEBUG) {
		System.out.println("FIRING hierarchy change ["+Thread.currentThread()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
		if (this.type != null) {
			System.out.println("    for hierarchy focused on " + ((JavaElement)this.type).toStringWithAncestors()); //$NON-NLS-1$
		}
	}
	ArrayList listeners= (ArrayList)this.changeListeners.clone();
	for (int i= 0; i < listeners.size(); i++) {
		final ITypeHierarchyChangedListener listener= (ITypeHierarchyChangedListener)listeners.get(i);
		// ensure the listener is still a listener
		if (this.changeListeners != null  && this.changeListeners.indexOf(listener) >= 0) {
			Platform.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					Util.log(exception, "Exception occurred in listener of Type hierarchy change notification"); //$NON-NLS-1$
				}
				public void run() throws Exception {
					listener.typeHierarchyChanged(TypeHierarchy.this);
				}
			});
		}
	}
}
/**
 * @see ITypeHierarchy
 */
public IType[] getAllClasses() {

	TypeVector classes = this.rootClasses.copy();
	for (Iterator iter = this.classToSuperclass.keySet().iterator(); iter.hasNext();){
		classes.add((IType)iter.next());
	}
	return classes.elements();
}
/**
 * @see ITypeHierarchy
 */
public IType[] getAllInterfaces() {
	IType[] collection= new IType[this.interfaces.size()];
	this.interfaces.toArray(collection);
	return collection;
}
/**
 * @see ITypeHierarchy
 */
public IType[]  getAllSubtypes(IType type) {
	return getAllSubtypesForType(type);
}
/**
 * @see getAllSubtypes(IType)
 */
private IType[] getAllSubtypesForType(IType type) {
	ArrayList subTypes = new ArrayList();
	getAllSubtypesForType0(type, subTypes);
	IType[] subClasses = new IType[subTypes.size()];
	subTypes.toArray(subClasses);
	return subClasses;
}
/**
 */
private void getAllSubtypesForType0(IType type, ArrayList subs) {
	IType[] subTypes = getSubtypesForType(type);
	if (subTypes.length != 0) {
		for (int i = 0; i < subTypes.length; i++) {
			IType subType = subTypes[i];
			subs.add(subType);
			getAllSubtypesForType0(subType, subs);
		}
	}
}
/**
 * @see ITypeHierarchy
 */
public IType[] getAllSuperclasses(IType type) {
	IType superclass = getSuperclass(type);
	TypeVector supers = new TypeVector();
	while (superclass != null) {
		supers.add(superclass);
		superclass = getSuperclass(superclass);
	}
	return supers.elements();
}
/**
 * @see ITypeHierarchy
 */
public IType[] getAllSuperInterfaces(IType type) {
	ArrayList supers = new ArrayList();
	if (this.typeToSuperInterfaces.get(type) == null) {
		return NO_TYPE;
	}
	getAllSuperInterfaces0(type, supers);
	IType[] superinterfaces = new IType[supers.size()];
	supers.toArray(superinterfaces);
	return superinterfaces;
}
private void getAllSuperInterfaces0(IType type, ArrayList supers) {
	IType[] superinterfaces = (IType[]) this.typeToSuperInterfaces.get(type);
	if (superinterfaces != null && superinterfaces.length != 0) {
		addAllCheckingDuplicates(supers, superinterfaces);
		for (int i = 0; i < superinterfaces.length; i++) {
			getAllSuperInterfaces0(superinterfaces[i], supers);
		}
	}
	IType superclass = (IType) this.classToSuperclass.get(type);
	if (superclass != null) {
		getAllSuperInterfaces0(superclass, supers);
	}
}
/**
 * @see ITypeHierarchy
 */
public IType[] getAllSupertypes(IType type) {
	ArrayList supers = new ArrayList();
	if (this.typeToSuperInterfaces.get(type) == null) {
		return NO_TYPE;
	}
	getAllSupertypes0(type, supers);
	IType[] supertypes = new IType[supers.size()];
	supers.toArray(supertypes);
	return supertypes;
}
private void getAllSupertypes0(IType type, ArrayList supers) {
	IType[] superinterfaces = (IType[]) this.typeToSuperInterfaces.get(type);
	if (superinterfaces != null && superinterfaces.length != 0) {
		addAllCheckingDuplicates(supers, superinterfaces);
		for (int i = 0; i < superinterfaces.length; i++) {
			getAllSuperInterfaces0(superinterfaces[i], supers);
		}
	}
	IType superclass = (IType) this.classToSuperclass.get(type);
	if (superclass != null) {
		supers.add(superclass);
		getAllSupertypes0(superclass, supers);
	}
}
/**
 * @see ITypeHierarchy
 */
public IType[] getAllTypes() {
	IType[] classes = getAllClasses();
	int classesLength = classes.length;
	IType[] interfaces = getAllInterfaces();
	int interfacesLength = interfaces.length;
	IType[] all = new IType[classesLength + interfacesLength];
	System.arraycopy(classes, 0, all, 0, classesLength);
	System.arraycopy(interfaces, 0, all, classesLength, interfacesLength);
	return all;
}

/**
 * @see ITypeHierarchy#getCachedFlags(IType)
 */
public int getCachedFlags(IType type) {
	Integer flagObject = (Integer) this.typeFlags.get(type);
	if (flagObject != null){
		return flagObject.intValue();
	}
	return -1;
}

/**
 * @see ITypeHierarchy
 */
public IType[] getExtendingInterfaces(IType type) {
	try {
		if (type.isClass()) {
			return new IType[] {};
		}
	} catch (JavaModelException npe) {
		return new IType[] {};
	}
	return getExtendingInterfaces0(type);
}
/**
 * Assumes that the type is an interface
 * @see getExtendingInterfaces
 */
private IType[] getExtendingInterfaces0(IType interfce) {
	Iterator iter = this.typeToSuperInterfaces.keySet().iterator();
	ArrayList xers = new ArrayList();
	while (iter.hasNext()) {
		IType type = (IType) iter.next();
		try {
			if (type.isClass()) {
				continue;
			}
		} catch (JavaModelException npe) {
			continue;
		}
		IType[] interfaces = (IType[]) this.typeToSuperInterfaces.get(type);
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				IType iFace = interfaces[i];
				if (iFace.equals(interfce)) {
					xers.add(type);
				}
			}
		}
	}
	IType[] extenders = new IType[xers.size()];
	xers.toArray(extenders);
	return extenders;
}
/**
 * @see ITypeHierarchy
 */
public IType[] getImplementingClasses(IType type) {
	try {
		if (type.isClass()) {
			return NO_TYPE;
		}
	} catch (JavaModelException npe) {
		return NO_TYPE;
	}
	return getImplementingClasses0(type);
}
/**
 * Assumes that the type is an interface
 * @see getImplementingClasses
 */
private IType[] getImplementingClasses0(IType interfce) {
	
	Iterator iter = this.typeToSuperInterfaces.keySet().iterator();
	ArrayList iMenters = new ArrayList();
	while (iter.hasNext()) {
		IType type = (IType) iter.next();
		try {
			if (type.isInterface()) {
				continue;
			}
		} catch (JavaModelException npe) {
			continue;
		}
		IType[] interfaces = (IType[]) this.typeToSuperInterfaces.get(type);
		for (int i = 0; i < interfaces.length; i++) {
			IType iFace = interfaces[i];
			if (iFace.equals(interfce)) {
				iMenters.add(type);
			}
		}
	}
	IType[] implementers = new IType[iMenters.size()];
	iMenters.toArray(implementers);
	return implementers;
}
/**
 * @see ITypeHierarchy
 */
public IType[] getRootClasses() {
	return this.rootClasses.elements();
}
/**
 * @see ITypeHierarchy
 */
public IType[] getRootInterfaces() {
	IType[] allInterfaces = getAllInterfaces();
	IType[] roots = new IType[allInterfaces.length];
	int rootNumber = 0;
	for (int i = 0; i < allInterfaces.length; i++) {
		IType[] superInterfaces = getSuperInterfaces(allInterfaces[i]);
		if (superInterfaces == null || superInterfaces.length == 0) {
			roots[rootNumber++] = allInterfaces[i];
		}
	}
	IType[] result = new IType[rootNumber];
	if (result.length > 0) {
		System.arraycopy(roots, 0, result, 0, rootNumber);
	}
	return result;
}
/**
 * @see ITypeHierarchy
 */
public IType[] getSubclasses(IType type) {
	try {
		if (type.isInterface()) {
			return NO_TYPE;
		}
	} catch (JavaModelException npe) {
		return new IType[] {};
	}
	TypeVector vector = (TypeVector)this.typeToSubtypes.get(type);
	if (vector == null)
		return NO_TYPE;
	else 
		return vector.elements();
}
/**
 * @see ITypeHierarchy
 */
public IType[] getSubtypes(IType type) {
	return getSubtypesForType(type);
}
/**
 * Returns an array of subtypes for the given type - will never return null.
 */
private IType[] getSubtypesForType(IType type) {
	TypeVector vector = (TypeVector)this.typeToSubtypes.get(type);
	if (vector == null)
		return NO_TYPE;
	else 
		return vector.elements();
}
/**
 * @see ITypeHierarchy
 */
public IType getSuperclass(IType type) {
	try {
		if (type.isInterface()) {
			return null;
		}
		return (IType) this.classToSuperclass.get(type);

	} catch (JavaModelException npe) {
		return null;
	}
}
/**
 * @see ITypeHierarchy
 */
public IType[] getSuperInterfaces(IType type) {
	IType[] interfaces = (IType[]) this.typeToSuperInterfaces.get(type);
	if (interfaces == null) {
		return NO_TYPE;
	}
	return interfaces;
}
/**
 * @see ITypeHierarchy
 */
public IType[] getSupertypes(IType type) {
	IType superclass = getSuperclass(type);
	if (superclass == null) {
		return getSuperInterfaces(type);
	} else {
		TypeVector superTypes = new TypeVector(getSuperInterfaces(type));
		superTypes.add(superclass);
		return superTypes.elements();
	}
}
/**
 * @see ITypeHierarchy
 */
public IType getType() {
	return this.type;
}
/**
 * Adds the new elements to a new array that contains all of the elements of the old array.
 * Returns the new array.
 */
protected IType[] growAndAddToArray(IType[] array, IType[] additions) {
	if (array == null || array.length == 0) {
		return additions;
	}
	IType[] old = array;
	array = new IType[old.length + additions.length];
	System.arraycopy(old, 0, array, 0, old.length);
	System.arraycopy(additions, 0, array, old.length, additions.length);
	return array;
}
/**
 * Adds the new element to a new array that contains all of the elements of the old array.
 * Returns the new array.
 */
protected IType[] growAndAddToArray(IType[] array, IType addition) {
	if (array == null || array.length == 0) {
		return new IType[] {addition};
	}
	IType[] old = array;
	array = new IType[old.length + 1];
	System.arraycopy(old, 0, array, 0, old.length);
	array[old.length] = addition;
	return array;
}
/**
 * Returns whether one of the subtypes in this hierarchy has the given simple name
 * or this type has the given simple name.
 */
private boolean hasSubtypeNamed(String simpleName) {
	if (this.type.getElementName().equals(simpleName)) {
		return true;
	}
	IType[] types = this.getAllSubtypes(this.type);
	for (int i = 0, length = types.length; i < length; i++) {
		if (types[i].getElementName().equals(simpleName)) {
			return true;
		}
	}
	return false;
}

/**
 * Returns whether one of the types in this hierarchy has the given simple name.
 */
private boolean hasTypeNamed(String simpleName) {
	IType[] types = this.getAllTypes();
	for (int i = 0, length = types.length; i < length; i++) {
		if (types[i].getElementName().equals(simpleName)) {
			return true;
		}
	}
	return false;
}

/**
 * Returns whether the simple name of the given type or one of its supertypes is 
 * the simple name of one of the types in this hierarchy.
 */
private boolean includesTypeOrSupertype(IType type) {
	try {
		// check type
		if (hasTypeNamed(type.getElementName())) return true;
		
		// check superclass
		String superclassName = type.getSuperclassName();
		if (superclassName != null) {
			int lastSeparator = superclassName.lastIndexOf('.');
			String simpleName = (lastSeparator > -1) ? superclassName.substring(lastSeparator) : superclassName;
			if (hasTypeNamed(simpleName)) return true;
		}
	
		// check superinterfaces
		String[] superinterfaceNames = type.getSuperInterfaceNames();
		if (superinterfaceNames != null) {
			for (int i = 0, length = superinterfaceNames.length; i < length; i++) {
				String superinterfaceName = superinterfaceNames[i];
				int lastSeparator = superinterfaceName.lastIndexOf('.');
				String simpleName = (lastSeparator > -1) ? superinterfaceName.substring(lastSeparator) : superinterfaceName;
				if (hasTypeNamed(simpleName)) return true;
			}
		}
	} catch (JavaModelException e) {
	}
	return false;
}
/**
 * Initializes this hierarchy's internal tables with the given size.
 */
protected void initialize(int size) {
	if (size < 10) {
		size = 10;
	}
	int smallSize = (size / 2);
	this.classToSuperclass = new HashMap(size);
	this.interfaces = new ArrayList(smallSize);
	this.missingTypes = new ArrayList(smallSize);
	this.rootClasses = new TypeVector();
	this.typeToSubtypes = new HashMap(smallSize);
	this.typeToSuperInterfaces = new HashMap(smallSize);
	this.typeFlags = new HashMap(smallSize);
}
/**
 * Returns true if this hierarchy is actively tracking changes
 * in the Java Model.
 */
protected boolean isActivated() {
	return this.isActivated;
}
/**
 * Returns true if the given delta could change this type hierarchy
 */
public boolean isAffected(IJavaElementDelta delta) {
	IJavaElement element= delta.getElement();
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return isAffectedByJavaModel(delta, element);
		case IJavaElement.JAVA_PROJECT:
			return isAffectedByJavaProject(delta, element);
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return isAffectedByPackageFragmentRoot(delta, element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return isAffectedByPackageFragment(delta, element);
		case IJavaElement.CLASS_FILE:
		case IJavaElement.COMPILATION_UNIT:
			return isAffectedByOpenable(delta, element);
	}
	return false;
}
/**
 * Returns true if any of the children of a project, package
 * fragment root, or package fragment have changed in a way that
 * effects this type hierarchy.
 */
private boolean isAffectedByChildren(IJavaElementDelta delta) {
	if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) > 0) {
		IJavaElementDelta[] children= delta.getAffectedChildren();
		for (int i= 0; i < children.length; i++) {
			if (isAffected(children[i])) {
				return true;
			}
		}
	}
	return false;
}
/**
 * Returns true if the given java model delta could affect this type hierarchy
 */
private boolean isAffectedByJavaModel(IJavaElementDelta delta, IJavaElement element) {
	switch (delta.getKind()) {
		case IJavaElementDelta.ADDED :
		case IJavaElementDelta.REMOVED :
			return element.equals(this.javaProject().getJavaModel());
		case IJavaElementDelta.CHANGED :
			return isAffectedByChildren(delta);
	}
	return false;
}
/**
 * Returns true if the given java project delta could affect this type hierarchy
 */
private boolean isAffectedByJavaProject(IJavaElementDelta delta, IJavaElement element) {
	switch (delta.getKind()) {
		case IJavaElementDelta.ADDED :
			try {
				// if the added project is on the classpath, then the hierarchy has changed
				IClasspathEntry[] classpath = ((JavaProject)this.javaProject()).getExpandedClasspath(true);
				for (int i = 0; i < classpath.length; i++) {
					if (classpath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT 
							&& classpath[i].getPath().equals(element.getUnderlyingResource().getFullPath())) {
						return true;
					}
				}
				return false;
			} catch (JavaModelException e) {
				return false;
			}
		case IJavaElementDelta.REMOVED :
			// removed project - if it contains packages we are interested in
			// then the type hierarchy has changed
			IJavaElement[] pkgs = this.packageRegion.getElements();
			for (int i = 0; i < pkgs.length; i++) {
				IJavaProject project = pkgs[i].getJavaProject();
				if (project != null && project.equals(element)) {
					return true;
				}
			}
			return false;
		case IJavaElementDelta.CHANGED :
			return isAffectedByChildren(delta);
	}
	return false;
}
/**
 * Returns true if the given package fragment delta could affect this type hierarchy
 */
private boolean isAffectedByPackageFragment(IJavaElementDelta delta, IJavaElement element) {
	switch (delta.getKind()) {
		case IJavaElementDelta.ADDED :
			// if the package fragment is in the projects being considered, this could
			// introduce new types, changing the hierarchy
			return this.projectRegion.contains(element);
		case IJavaElementDelta.REMOVED :
			// is a change if the package fragment contains types in this hierarchy
			return packageRegionContainsSamePackageFragment(element);
		case IJavaElementDelta.CHANGED :
			// look at the files in the package fragment
			return isAffectedByChildren(delta);
	}
	return false;
}
/**
 * Returns true if the given package fragment root delta could affect this type hierarchy
 */
private boolean isAffectedByPackageFragmentRoot(IJavaElementDelta delta, IJavaElement element) {
	switch (delta.getKind()) {
		case IJavaElementDelta.ADDED :
			return this.projectRegion.contains(element);
		case IJavaElementDelta.REMOVED :
		case IJavaElementDelta.CHANGED :
			int flags = delta.getFlags();
			if ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) > 0) {
				// check if the root is in the classpath of one of the projects of this hierarchy
				if (this.projectRegion != null) {
					IPackageFragmentRoot root = (IPackageFragmentRoot)element;
					IPath rootPath = root.getPath();
					IJavaElement[] elements = this.projectRegion.getElements();
					for (int i = 0; i < elements.length; i++) {
						IJavaProject project = (IJavaProject)elements[i];
						try {
							IClasspathEntry[] classpath = project.getResolvedClasspath(true);
							for (int j = 0; j < classpath.length; j++) {
								IClasspathEntry entry = classpath[j];
								if (entry.getPath().equals(rootPath)) {
									return true;
								}
							}
						} catch (JavaModelException e) {
							// igmore this project
						}
					}
				}
			}
			if ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) > 0 || (flags & IJavaElementDelta.F_CONTENT) > 0) {
				// 1. removed from classpath - if it contains packages we are interested in
				// the the type hierarchy has changed
				// 2. content of a jar changed - if it contains packages we are interested in
				// the the type hierarchy has changed
				IJavaElement[] pkgs = this.packageRegion.getElements();
				for (int i = 0; i < pkgs.length; i++) {
					if (pkgs[i].getParent().equals(element)) {
						return true;
					}
				}
				return false;
			}
	}
	return isAffectedByChildren(delta);
}
/**
 * Returns true if the given type delta (a compilation unit delta or a class file delta)
 * could affect this type hierarchy.
 */
protected boolean isAffectedByOpenable(IJavaElementDelta delta, IJavaElement element) {
	// ignore changes to working copies
	if (element instanceof CompilationUnit && ((CompilationUnit)element).isWorkingCopy()) {
		return false;
	}
		
	int kind = delta.getKind();
	switch (kind) {
		case IJavaElementDelta.REMOVED:
			return this.files.get(element) != null;
		case IJavaElementDelta.ADDED:
			IType[] types = null;
			try {
				types = (element instanceof CompilationUnit) ?
					((CompilationUnit)element).getAllTypes() :
					new IType[] {((org.eclipse.jdt.internal.core.ClassFile)element).getType()};
			} catch (JavaModelException e) {
				e.printStackTrace();
				return false;
			}
			for (int i = 0, length = types.length; i < length; i++) {
				IType type = types[i];
				if (typeHasSupertype(type) 
					|| subtypesIncludeSupertypeOf(type)
					|| this.missingTypes.contains(type.getElementName())) {
						
					return true;
				}
			}
			break;
		case IJavaElementDelta.CHANGED:
			boolean hasImportChange = false;
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				IJavaElementDelta child = children[i];
				IJavaElement childElement = child.getElement();
				if (childElement instanceof IType) {
					// NB: rely on the fact that import statements are before type declarations
					if (this.isAffectedByType(child, (IType)childElement, hasImportChange)) {
						return true;
					}
				} else if (childElement instanceof ImportContainer) {
					if (!hasImportChange) {
						hasImportChange = true;
						types = null;
						try {
							types = (element instanceof CompilationUnit) ?
								((CompilationUnit)element).getAllTypes() :
								new IType[] {((org.eclipse.jdt.internal.core.ClassFile)element).getType()};
						} catch (JavaModelException e) {
							e.printStackTrace();
							return false;
						}
						for (int j = 0, typesLength = types.length; j < typesLength; j++) {
							if (includesTypeOrSupertype(types[j])) {
								return true;
							}
						}
					}
				}
			}
			break;
		
	}
	return false;
}
/*
 * The rules are:
 * - if the delta is an added type X, then the hierarchy is changed 
 *   . if one of the types in this hierarchy has a supertype whose simple name is the
 *     simple name of X
 *   . if the simple name of a supertype of X is the simple name of one of
 *     the subtypes in this hierarchy (X will be added as one of the subtypes)
 * - if the delta is a changed type X, then the hierarchy is changed
 *   . if the visibility of X has changed and if one of the types in this hierarchy has a 
 *	   supertype whose simple name is the simple name of X
 *   . if one of the supertypes of X has changed or one of the imports has changed,
 *     and if the simple name of a supertype of X is the simple name of one of 
 *     the types in this hierarchy
 * - if the delta is a removed type X, then the hierarchy is changed
 *   . if the given element is part of this hierarchy (note we cannot acces the types 
 *     because the element has been removed)
 */
protected boolean isAffectedByType(IJavaElementDelta delta, IType type, boolean hasImportChange) {
	switch (delta.getKind()) {
		case IJavaElementDelta.ADDED:
			if (typeHasSupertype(type) 
				|| subtypesIncludeSupertypeOf(type) 
				|| this.missingTypes.contains(type.getElementName())) {
				
				return true;
			}
			break;
		case IJavaElementDelta.CHANGED:
			boolean hasVisibilityChange = (delta.getFlags() & IJavaElementDelta.F_MODIFIERS) > 0;
			boolean hasSupertypeChange = (delta.getFlags() & IJavaElementDelta.F_SUPER_TYPES) > 0;
			if ((hasVisibilityChange && typeHasSupertype(type))
					|| ((hasImportChange || hasSupertypeChange) 
						&& includesTypeOrSupertype(type))) {
				return true;
			}
			break;
		case IJavaElementDelta.REMOVED:
			if (this.contains(type)) {
				return true;
			}
			break;
	}
	IJavaElementDelta[] children = delta.getAffectedChildren();
	for (int i = 0, length = children.length; i < length; i++) {
		IJavaElementDelta child = children[i];
		IJavaElement childElement = child.getElement();
		if (childElement instanceof IType) {
			if (this.isAffectedByType(child, (IType)childElement, hasImportChange)) {
				return true;
			}
		}
	}
	return false;
} 
/**
 * Returns the java project this hierarchy was created in.
 */
public IJavaProject javaProject() {
	return this.type.getJavaProject();
}
/**
 * Returns <code>true</code> if an equivalent package fragment is included in the package
 * region. Package fragments are equivalent if they both have the same name.
 */
protected boolean packageRegionContainsSamePackageFragment(IJavaElement element) {
	IJavaElement[] pkgs = this.packageRegion.getElements();
	for (int i = 0; i < pkgs.length; i++) {
		if (pkgs[i].getElementName().equals(element.getElementName())) {
			return true;
		}
	}
	return false;
}

/**
 * @see ITypeHierarchy
 */
public void refresh(IProgressMonitor monitor) throws JavaModelException {
	try {
		boolean reactivate = isActivated();
		ArrayList listeners = this.changeListeners;
		if (reactivate) {
			deactivate();
		}
		this.progressMonitor = monitor;
		if (monitor != null) {
			monitor.beginTask(Util.bind("hierarchy.creating"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		}
		long start = -1;
		if (DEBUG) {
			start = System.currentTimeMillis();
			if (this.computeSubtypes) {
				System.out.println("CREATING TYPE HIERARCHY [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				System.out.println("CREATING SUPER TYPE HIERARCHY [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (this.type != null) {
				System.out.println("  on type " + ((JavaElement)this.type).toStringWithAncestors()); //$NON-NLS-1$
			}
		}
		compute();
		if (reactivate) {
			activate();
			this.changeListeners = listeners;
		}
		if (monitor != null) {
			monitor.done();
		}
		this.progressMonitor = null;
		if (DEBUG) {
			if (this.computeSubtypes) {
				System.out.println("CREATED TYPE HIERARCHY in " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				System.out.println("CREATED SUPER TYPE HIERARCHY in " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			System.out.println(this.toString());
		}
	} catch (JavaModelException e) {
		this.progressMonitor = null;
		throw e;
	} catch (CoreException e) {
		this.progressMonitor = null;
		throw new JavaModelException(e);
	} catch (OperationCanceledException oce) {
		refreshCancelled(oce);
	}
}
/**
 * The refresh of this type hierarchy has been cancelled.
 * Cleanup the state of this now invalid type hierarchy.
 */
protected void refreshCancelled(OperationCanceledException oce) throws JavaModelException {
	destroy();
	this.progressMonitor = null;
	throw oce;
}

/**
 * @see ITypeHierarchy
 */
public void removeTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener) {
	if (this.changeListeners == null) {
		return;
	}
	this.changeListeners.remove(listener);
	if (this.changeListeners.isEmpty()) {
		deactivate();
	}
}
/**
 * Returns whether the simple name of a supertype of the given type is 
 * the simple name of one of the subtypes in this hierarchy or the
 * simple name of this type.
 */
private boolean subtypesIncludeSupertypeOf(IType type) {
	// look for superclass
	String superclassName = null;
	try {
		superclassName = type.getSuperclassName();
	} catch (JavaModelException e) {
		e.printStackTrace();
		return false;
	}
	if (superclassName == null) {
		superclassName = "Object"; //$NON-NLS-1$
	}
	int dot = -1;
	String simpleSuper = (dot = superclassName.lastIndexOf('.')) > -1 ?
		superclassName.substring(dot + 1) :
		superclassName;
	if (hasSubtypeNamed(simpleSuper)) {
		return true;
	}

	// look for super interfaces
	String[] interfaceNames = null;
	try {
		interfaceNames = type.getSuperInterfaceNames();
	} catch (JavaModelException e) {
		e.printStackTrace();
		return false;
	}
	for (int i = 0, length = interfaceNames.length; i < length; i++) {
		dot = -1;
		String interfaceName = interfaceNames[i];
		String simpleInterface = (dot = interfaceName.lastIndexOf('.')) > -1 ?
			interfaceName.substring(dot) :
			interfaceName;
		if (hasSubtypeNamed(simpleInterface)) {
			return true;
		}
	}
	
	return false;
}
/**
 * @see ITypeHierarchy
 */
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("Focus: "); //$NON-NLS-1$
	buffer.append(this.type == null ? "<NONE>" : this.type.getFullyQualifiedName()); //$NON-NLS-1$
	buffer.append("\n"); //$NON-NLS-1$
	if (exists()) {
		if (this.type != null) {
			buffer.append("Super types:\n"); //$NON-NLS-1$
			toString(buffer, this.type, 1, true);
			buffer.append("Sub types:\n"); //$NON-NLS-1$
			toString(buffer, this.type, 1, false);
		} else {
			buffer.append("Sub types of root classes:\n"); //$NON-NLS-1$
			IType[] roots= getRootClasses();
			for (int i= 0; i < roots.length; i++) {
				toString(buffer, roots[i], 1, false);
			}
		}
		if (this.rootClasses.size > 1) {
			buffer.append("Root classes:\n"); //$NON-NLS-1$
			IType[] roots = this.getRootClasses();
			for (int i = 0, length = roots.length; i < length; i++) {
				IType type = roots[i];
				toString(buffer, type, 1, false);
			}
		}
	} else {
		buffer.append("(Hierarchy became stale)"); //$NON-NLS-1$
	}
	return buffer.toString();
}
/**
 * Append a String to the given buffer representing the hierarchy for the type,
 * beginning with the specified indentation level.
 * If ascendant, shows the super types, otherwise show the sub types.
 */
private void toString(StringBuffer buffer, IType type, int indent, boolean ascendant) {
	for (int i= 0; i < indent; i++) {
		buffer.append("  "); //$NON-NLS-1$
	}
	JavaElement element = (JavaElement)type;
	buffer.append(element.toStringWithAncestors());
	buffer.append('\n');

	IType[] types= ascendant ? getSupertypes(type) : getSubtypes(type);
	for (int i= 0; i < types.length; i++) {
		toString(buffer, types[i], indent + 1, ascendant);
	}

}
/**
 * Returns whether one of the types in this hierarchy has a supertype whose simple 
 * name is the simple name of the given type.
 */
private boolean typeHasSupertype(IType type) {
	String simpleName = type.getElementName();
	for(Iterator iter = this.classToSuperclass.values().iterator(); iter.hasNext();){
		IType superType = (IType)iter.next();
		if (superType.getElementName().equals(simpleName)) {
			return true;
		}
	}
	return false;
}
/**
 * @see IProgressMonitor
 */
protected void worked(int work) {
	if (this.progressMonitor != null) {
		this.progressMonitor.worked(work);
		checkCanceled();
	}
}

}
