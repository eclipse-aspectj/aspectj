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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.PerThreadObject;
/**
 * A <code>NameLookup</code> provides name resolution within a Java project.
 * The name lookup facility uses the project's classpath to prioritize the 
 * order in which package fragments are searched when resolving a name.
 *
 * <p>Name lookup only returns a handle when the named element actually
 * exists in the model; otherwise <code>null</code> is returned.
 *
 * <p>There are two logical sets of methods within this interface.  Methods
 * which start with <code>find*</code> are intended to be convenience methods for quickly
 * finding an element within another element, i.e. finding a class within a
 * package.  The other set of methods all begin with <code>seek*</code>.  These methods
 * do comprehensive searches of the <code>IJavaProject</code> returning hits
 * in real time through an <code>IJavaElementRequestor</code>.
 *
 */
public class NameLookup {
	/**
	 * Accept flag for specifying classes.
	 */
	public static final int ACCEPT_CLASSES = 0x00000002;

	/**
	 * Accept flag for specifying interfaces.
	 */
	public static final int ACCEPT_INTERFACES = 0x00000004;

	/**
	 * The <code>IPackageFragmentRoot</code>'s associated
	 * with the classpath of this NameLookup facility's
	 * project.
	 */
	protected IPackageFragmentRoot[] fPackageFragmentRoots= null;

	/**
	 * Table that maps package names to lists of package fragments for
	 * all package fragments in the package fragment roots known
	 * by this name lookup facility. To allow > 1 package fragment
	 * with the same name, values are arrays of package fragments
	 * ordered as they appear on the classpath.
	 */
	protected Map fPackageFragments;

	/**
	 * The <code>IWorkspace</code> that this NameLookup
	 * is configure within.
	 */
	protected IWorkspace workspace;
	
	/**
	 * A map from compilation unit handles to units to look inside (compilation
	 * units or working copies).
	 * Allows working copies to take precedence over compilation units.
	 * The cache is a 2-level cache, first keyed by thread.
	 */
	protected PerThreadObject unitsToLookInside = new PerThreadObject();

	public NameLookup(IJavaProject project) throws JavaModelException {
		configureFromProject(project);
	}

	/**
	 * Returns true if:<ul>
	 *  <li>the given type is an existing class and the flag's <code>ACCEPT_CLASSES</code>
	 *      bit is on
	 *  <li>the given type is an existing interface and the <code>ACCEPT_INTERFACES</code>
	 *      bit is on
	 *  <li>neither the <code>ACCEPT_CLASSES</code> or <code>ACCEPT_INTERFACES</code>
	 *      bit is on
	 *  </ul>
	 * Otherwise, false is returned. 
	 */
	protected boolean acceptType(IType type, int acceptFlags) {
		if (acceptFlags == 0)
			return true; // no flags, always accepted
		try {
			if (type.isClass()) {
				return (acceptFlags & ACCEPT_CLASSES) != 0;
			} else {
				return (acceptFlags & ACCEPT_INTERFACES) != 0;
			}
		} catch (JavaModelException npe) {
			return false; // the class is not present, do not accept.
		}
	}

	/**
	 * Configures this <code>NameLookup</code> based on the
	 * info of the given <code>IJavaProject</code>.
	 *
	 * @throws JavaModelException if the <code>IJavaProject</code> has no classpath.
	 */
	private void configureFromProject(IJavaProject project) throws JavaModelException {
		workspace= project.getJavaModel().getWorkspace();
		fPackageFragmentRoots= ((JavaProject) project).getAllPackageFragmentRoots();
		fPackageFragments= new HashMap();
		IPackageFragment[] frags = this.getPackageFragmentsInRoots(fPackageFragmentRoots, project);
		for (int i= 0; i < frags.length; i++) {
			IPackageFragment fragment= frags[i];
			IPackageFragment[] entry= (IPackageFragment[]) fPackageFragments.get(fragment.getElementName());
			if (entry == null) {
				entry= new IPackageFragment[1];
				entry[0]= fragment;
				fPackageFragments.put(fragment.getElementName(), entry);
			} else {
				IPackageFragment[] copy= new IPackageFragment[entry.length + 1];
				System.arraycopy(entry, 0, copy, 0, entry.length);
				copy[entry.length]= fragment;
				fPackageFragments.put(fragment.getElementName(), copy);
			}
		}
	}

	/**
	 * Finds every type in the project whose simple name matches
	 * the prefix, informing the requestor of each hit. The requestor
	 * is polled for cancellation at regular intervals.
	 *
	 * <p>The <code>partialMatch</code> argument indicates partial matches
	 * should be considered.
	 */
	private void findAllTypes(String prefix, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {
		int count= fPackageFragmentRoots.length;
		for (int i= 0; i < count; i++) {
			if (requestor.isCanceled())
				return;
			IPackageFragmentRoot root= fPackageFragmentRoots[i];
			IJavaElement[] packages= null;
			try {
				packages= root.getChildren();
			} catch (JavaModelException npe) {
				continue; // the root is not present, continue;
			}
			if (packages != null) {
				for (int j= 0, packageCount= packages.length; j < packageCount; j++) {
					if (requestor.isCanceled())
						return;
					seekTypes(prefix, (IPackageFragment) packages[j], partialMatch, acceptFlags, requestor);
				}
			}
		}
	}

	/**
	 * Returns the <code>ICompilationUnit</code> which defines the type
	 * named <code>qualifiedTypeName</code>, or <code>null</code> if
	 * none exists. The domain of the search is bounded by the classpath
	 * of the <code>IJavaProject</code> this <code>NameLookup</code> was
	 * obtained from.
	 * <p>
	 * The name must be fully qualified (eg "java.lang.Object", "java.util.Hashtable$Entry")
	 */
	public ICompilationUnit findCompilationUnit(String qualifiedTypeName) {
		String pkgName= IPackageFragment.DEFAULT_PACKAGE_NAME;
		String cuName= qualifiedTypeName;

		int index= qualifiedTypeName.lastIndexOf('.');
		if (index != -1) {
			pkgName= qualifiedTypeName.substring(0, index);
			cuName= qualifiedTypeName.substring(index + 1);
		}
		index= cuName.indexOf('$');
		if (index != -1) {
			cuName= cuName.substring(0, index);
		}
		cuName += ".java"; //$NON-NLS-1$
		IPackageFragment[] frags= (IPackageFragment[]) fPackageFragments.get(pkgName);
		if (frags != null) {
			for (int i= 0; i < frags.length; i++) {
				IPackageFragment frag= frags[i];
				if (!(frag instanceof JarPackageFragment)) {
					ICompilationUnit cu= frag.getCompilationUnit(cuName);
					if (cu != null && cu.exists()) {
						return cu;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the package fragment whose path matches the given
	 * (absolute) path, or <code>null</code> if none exist. The domain of
	 * the search is bounded by the classpath of the <code>IJavaProject</code>
	 * this <code>NameLookup</code> was obtained from.
	 * The path can be:
	 * 	- internal to the workbench: "/Project/src"
	 *  - external to the workbench: "c:/jdk/classes.zip/java/lang"
	 */
	public IPackageFragment findPackageFragment(IPath path) {
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException(Util.bind("path.mustBeAbsolute")); //$NON-NLS-1$
		}
/*
 * this code should rather use the package fragment map to find the candidate package, then
 * check if the respective enclosing root maps to the one on this given IPath.
 */		
		IResource possibleFragment = workspace.getRoot().findMember(path);
		if (possibleFragment == null) {
			//external jar
			for (int i = 0; i < fPackageFragmentRoots.length; i++) {
				IPackageFragmentRoot root = fPackageFragmentRoots[i];
				if (!root.isExternal()) {
					continue;
				}
				IPath rootPath = root.getPath();
				int matchingCount = rootPath.matchingFirstSegments(path);
				if (matchingCount != 0) {
					String name = path.toOSString();
					// + 1 is for the File.separatorChar
					name = name.substring(rootPath.toOSString().length() + 1, name.length());
					name = name.replace(File.separatorChar, '.');
					IJavaElement[] list = null;
					try {
						list = root.getChildren();
					} catch (JavaModelException npe) {
						continue; // the package fragment root is not present;
					}
					int elementCount = list.length;
					for (int j = 0; j < elementCount; j++) {
						IPackageFragment packageFragment = (IPackageFragment) list[j];
						if (nameMatches(name, packageFragment, false)) {
							return packageFragment;
						}
					}
				}
			}
		} else {
			IJavaElement fromFactory = JavaCore.create(possibleFragment);
			if (fromFactory == null) {
				return null;
			}
			if (fromFactory instanceof IPackageFragment) {
				return (IPackageFragment) fromFactory;
			} else
				if (fromFactory instanceof IJavaProject) {
					// default package in a default root
					JavaProject project = (JavaProject) fromFactory;
					try {
						IClasspathEntry entry = project.getClasspathEntryFor(path);
						if (entry != null) {
							IPackageFragmentRoot root =
								project.getPackageFragmentRoot(project.getUnderlyingResource());
							IPackageFragment[] pkgs = (IPackageFragment[]) fPackageFragments.get(IPackageFragment.DEFAULT_PACKAGE_NAME);
							if (pkgs == null) {
								return null;
							}
							for (int i = 0; i < pkgs.length; i++) {
								if (pkgs[i].getParent().equals(root)) {
									return pkgs[i];
								}
							}
						}
					} catch (JavaModelException e) {
						return null;
					}
				}
		}
		return null;
	}

	/**
	 * Returns the package fragments whose name matches the given
	 * (qualified) name, or <code>null</code> if none exist.
	 *
	 * The name can be:
	 *	- empty: ""
	 *	- qualified: "pack.pack1.pack2"
	 * @param partialMatch partial name matches qualify when <code>true</code>,
	 *	only exact name matches qualify when <code>false</code>
	 */
	public IPackageFragment[] findPackageFragments(String name, boolean partialMatch) {
		int count= fPackageFragmentRoots.length;
		if (partialMatch) {
			name= name.toLowerCase();
			for (int i= 0; i < count; i++) {
				IPackageFragmentRoot root= fPackageFragmentRoots[i];
				IJavaElement[] list= null;
				try {
					list= root.getChildren();
				} catch (JavaModelException npe) {
					continue; // the package fragment root is not present;
				}
				int elementCount= list.length;
				IPackageFragment[] result = new IPackageFragment[elementCount];
				int resultLength = 0; 
				for (int j= 0; j < elementCount; j++) {
					IPackageFragment packageFragment= (IPackageFragment) list[j];
					if (nameMatches(name, packageFragment, true)) {
						result[resultLength++] = packageFragment;
					}
				}
				if (resultLength > 0) {
					System.arraycopy(result, 0, result = new IPackageFragment[resultLength], 0, resultLength);
					return result;
				} else {
					return null;
				}
			}
		} else {
			IPackageFragment[] fragments= (IPackageFragment[]) fPackageFragments.get(name);
			if (fragments != null) {
				IPackageFragment[] result = new IPackageFragment[fragments.length];
				int resultLength = 0; 
				for (int i= 0; i < fragments.length; i++) {
					IPackageFragment packageFragment= fragments[i];
					result[resultLength++] = packageFragment;
				}
				if (resultLength > 0) {
					System.arraycopy(result, 0, result = new IPackageFragment[resultLength], 0, resultLength);
					return result;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 */
	public IType findType(String typeName, String packageName, boolean partialMatch, int acceptFlags) {
		if (packageName == null) {
			packageName= IPackageFragment.DEFAULT_PACKAGE_NAME;
		}
		JavaElementRequestor elementRequestor = new JavaElementRequestor();
		seekPackageFragments(packageName, false, elementRequestor);
		IPackageFragment[] packages= elementRequestor.getPackageFragments();

		for (int i= 0, length= packages.length; i < length; i++) {
			IType type= findType(typeName, packages[i], partialMatch, acceptFlags);
			if (type != null)
				return type;
		}
		return null;
	}
	/**
	 * Returns all the package fragments found in the specified
	 * package fragment roots. Make sure the returned fragments have the given
	 * project as great parent. This ensures the name lookup will not refer to another
	 * project (through jar package fragment roots)
	 */
	private IPackageFragment[] getPackageFragmentsInRoots(IPackageFragmentRoot[] roots, IJavaProject project) {

		// The following code assumes that all the roots have the given project as their parent
		ArrayList frags = new ArrayList();
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			try {
				IJavaElement[] children = root.getChildren();

				/* 2 jar package fragment roots can be equals but not belonging 
				   to the same project. As a result, they share the same element info.
				   So this jar package fragment root could get the children of
				   another jar package fragment root.
				   The following code ensures that the children of this jar package
				   fragment root have the given project as a great parent.
				 */
				int length = children.length;
				if (length == 0) continue;
				if (children[0].getParent().getParent().equals(project)) {
					// the children have the right parent, simply add them to the list
					for (int j = 0; j < length; j++) {
						frags.add(children[j]);
					}
				} else {
					// create a new handle with the root as the parent
					for (int j = 0; j < length; j++) {
						frags.add(root.getPackageFragment(children[j].getElementName()));
					}
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[frags.size()];
		frags.toArray(fragments);
		return fragments;
	}

	/**
	 * Returns the first type in the given package whose name
	 * matches the given (unqualified) name, or <code>null</code> if none
	 * exist. Specifying a <code>null</code> package will result in no matches.
	 * The domain of the search is bounded by the Java project from which 
	 * this name lookup was obtained.
	 *
	 * @param name the name of the type to find
	 * @param pkg the package to search
	 * @param partialMatch partial name matches qualify when <code>true</code>,
	 *	only exact name matches qualify when <code>false</code>
	 * @param acceptFlags a bit mask describing if classes, interfaces or both classes and interfaces
	 * 	are desired results. If no flags are specified, all types are returned.
	 *
	 * @see #ACCEPT_CLASSES
	 * @see #ACCEPT_INTERFACES
	 */
	public IType findType(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags) {
		if (pkg == null) {
			return null;
		}
		// Return first found (ignore duplicates).
//synchronized(JavaModelManager.getJavaModelManager()){	
		SingleTypeRequestor typeRequestor = new SingleTypeRequestor();
		seekTypes(name, pkg, partialMatch, acceptFlags, typeRequestor);
		IType type= typeRequestor.getType();
		return type;
//}
	}

	/**
	 * Returns the type specified by the qualified name, or <code>null</code>
	 * if none exist. The domain of
	 * the search is bounded by the Java project from which this name lookup was obtained.
	 *
	 * @param name the name of the type to find
	 * @param partialMatch partial name matches qualify when <code>true</code>,
	 *	only exact name matches qualify when <code>false</code>
	 * @param acceptFlags a bit mask describing if classes, interfaces or both classes and interfaces
	 * 	are desired results. If no flags are specified, all types are returned.
	 *
	 * @see #ACCEPT_CLASSES
	 * @see #ACCEPT_INTERFACES
	 */
	public IType findType(String name, boolean partialMatch, int acceptFlags) {
		int index= name.lastIndexOf('.');
		String className= null, packageName= null;
		if (index == -1) {
			packageName= IPackageFragment.DEFAULT_PACKAGE_NAME;
			className= name;
		} else {
			packageName= name.substring(0, index);
			className= name.substring(index + 1);
		}
		return findType(className, packageName, partialMatch, acceptFlags);
	}

	/**
	 * Returns true if the given element's name matches the
	 * specified <code>searchName</code>, otherwise false.
	 *
	 * <p>The <code>partialMatch</code> argument indicates partial matches
	 * should be considered.
	 * NOTE: in partialMatch mode, the case will be ignored, and the searchName must already have
	 *          been lowercased.
	 */
	protected boolean nameMatches(String searchName, IJavaElement element, boolean partialMatch) {
		if (partialMatch) {
			// partial matches are used in completion mode, thus case insensitive mode
			return element.getElementName().toLowerCase().startsWith(searchName);
		} else {
			return element.getElementName().equals(searchName);
		}
	}

	/**
	 * Notifies the given requestor of all package fragments with the
	 * given name. Checks the requestor at regular intervals to see if the
	 * requestor has canceled. The domain of
	 * the search is bounded by the <code>IJavaProject</code>
	 * this <code>NameLookup</code> was obtained from.
	 *
	 * @param partialMatch partial name matches qualify when <code>true</code>;
	 *	only exact name matches qualify when <code>false</code>
	 */
	public void seekPackageFragments(String name, boolean partialMatch, IJavaElementRequestor requestor) {
		int count= fPackageFragmentRoots.length;
		String matchName= partialMatch ? name.toLowerCase() : name;
		for (int i= 0; i < count; i++) {
			if (requestor.isCanceled())
				return;
			IPackageFragmentRoot root= fPackageFragmentRoots[i];
			IJavaElement[] list= null;
			try {
				list= root.getChildren();
			} catch (JavaModelException npe) {
				continue; // this root package fragment is not present
			}
			int elementCount= list.length;
			for (int j= 0; j < elementCount; j++) {
				if (requestor.isCanceled())
					return;
				IPackageFragment packageFragment= (IPackageFragment) list[j];
				if (nameMatches(matchName, packageFragment, partialMatch))
					requestor.acceptPackageFragment(packageFragment);
			}
		}
	}

	/**
	 * Notifies the given requestor of all types (classes and interfaces) in the
	 * given package fragment with the given (unqualified) name.
	 * Checks the requestor at regular intervals to see if the requestor
	 * has canceled. If the given package fragment is <code>null</code>, all types in the
	 * project whose simple name matches the given name are found.
	 *
	 * @param name The name to search
	 * @param pkg The corresponding package fragment
	 * @param partialMatch partial name matches qualify when <code>true</code>;
	 *	only exact name matches qualify when <code>false</code>
	 * @param acceptFlags a bit mask describing if classes, interfaces or both classes and interfaces
	 * 	are desired results. If no flags are specified, all types are returned.
	 * @param requestor The requestor that collects the result
	 *
	 * @see #ACCEPT_CLASSES
	 * @see #ACCEPT_INTERFACES
	 */
	public void seekTypes(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {

		String matchName= partialMatch ? name.toLowerCase() : name;
		if (matchName.indexOf('.') >= 0) { //looks for member type A.B
			matchName= matchName.replace('.', '$');
		}
		if (pkg == null) {
			findAllTypes(matchName, partialMatch, acceptFlags, requestor);
			return;
		}
		IPackageFragmentRoot root= (IPackageFragmentRoot) pkg.getParent();
		try {
			int packageFlavor= root.getKind();
			switch (packageFlavor) {
				case IPackageFragmentRoot.K_BINARY :
					seekTypesInBinaryPackage(matchName, pkg, partialMatch, acceptFlags, requestor);
					break;
				case IPackageFragmentRoot.K_SOURCE :
					seekTypesInSourcePackage(matchName, pkg, partialMatch, acceptFlags, requestor);
					break;
				default :
					return;
			}
		} catch (JavaModelException e) {
			return;
		}
	}

	/**
	 * Performs type search in a binary package.
	 */
	protected void seekTypesInBinaryPackage(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {
		IClassFile[] classFiles= null;
		try {
			classFiles= pkg.getClassFiles();
		} catch (JavaModelException npe) {
			return; // the package is not present
		}
		int length= classFiles.length;

		String unqualifiedName= name;
		int index= name.lastIndexOf('$');
		if (index != -1) {
			//the type name of the inner type
			unqualifiedName= name.substring(index + 1, name.length());
			// unqualifiedName is empty if the name ends with a '$' sign.
			// See http://dev.eclipse.org/bugs/show_bug.cgi?id=14642
			if ((unqualifiedName.length() > 0 && Character.isDigit(unqualifiedName.charAt(0))) || unqualifiedName.length() == 0){
				unqualifiedName = name;
			}
		}
		String matchName= partialMatch ? name.toLowerCase() : name;
		for (int i= 0; i < length; i++) {
			if (requestor.isCanceled())
				return;
			IClassFile classFile= classFiles[i];
			String elementName = classFile.getElementName();
			if (partialMatch) elementName = elementName.toLowerCase();

			/**
			 * Must use startWith because matchName will never have the 
			 * extension ".class" and the elementName always will.
			 */
			if (elementName.startsWith(matchName)) {
				IType type= null;
				try {
					type= classFile.getType();
				} catch (JavaModelException npe) {
					continue; // the classFile is not present
				}
				if (!partialMatch || (type.getElementName().length() > 0 && !Character.isDigit(type.getElementName().charAt(0)))) { //not an anonymous type
					if (nameMatches(unqualifiedName, type, partialMatch) && acceptType(type, acceptFlags))
						requestor.acceptType(type);
				}
			}
		}
	}

	/**
	 * Performs type search in a source package.
	 */
	protected void seekTypesInSourcePackage(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {
		ICompilationUnit[] compilationUnits = null;
		try {
			compilationUnits = pkg.getCompilationUnits();
		} catch (JavaModelException npe) {
			return; // the package is not present
		}
		int length= compilationUnits.length;
		String matchName = name;
		int index= name.indexOf('$');
		boolean potentialMemberType = false;
		String potentialMatchName = null;
		if (index != -1) {
			//the compilation unit name of the inner type
			potentialMatchName = name.substring(0, index);
			potentialMemberType = true;
		}

		/**
		 * In the following, matchName will never have the extension ".java" and 
		 * the compilationUnits always will. So add it if we're looking for 
		 * an exact match.
		 */
		String unitName = partialMatch ? matchName.toLowerCase() : matchName + ".java"; //$NON-NLS-1$
		String potentialUnitName = null;
		if (potentialMemberType) {
			potentialUnitName = partialMatch ? potentialMatchName.toLowerCase() : potentialMatchName + ".java"; //$NON-NLS-1$
		}

		for (int i= 0; i < length; i++) {
			if (requestor.isCanceled())
				return;
			ICompilationUnit compilationUnit= compilationUnits[i];
			
			// unit to look inside
			ICompilationUnit unitToLookInside = null;
			Map workingCopies = (Map) this.unitsToLookInside.getCurrent();
			if (workingCopies != null 
					&& (unitToLookInside = (ICompilationUnit)workingCopies.get(compilationUnit)) != null){
					compilationUnit = unitToLookInside;
				}
			if ((unitToLookInside != null && !potentialMemberType) || nameMatches(unitName, compilationUnit, partialMatch)) {
				IType[] types= null;
				try {
					types= compilationUnit.getTypes();
				} catch (JavaModelException npe) {
					continue; // the compilation unit is not present
				}
				int typeLength= types.length;
				for (int j= 0; j < typeLength; j++) {
					if (requestor.isCanceled())
						return;
					IType type= types[j];
					if (nameMatches(matchName, type, partialMatch)) {
						if (acceptType(type, acceptFlags)) requestor.acceptType(type);
					}
				}
			} else if (potentialMemberType && nameMatches(potentialUnitName, compilationUnit, partialMatch)) {
				IType[] types= null;
				try {
					types= compilationUnit.getTypes();
				} catch (JavaModelException npe) {
					continue; // the compilation unit is not present
				}
				int typeLength= types.length;
				for (int j= 0; j < typeLength; j++) {
					if (requestor.isCanceled())
						return;
					IType type= types[j]; 
					if (nameMatches(potentialMatchName, type, partialMatch)) {
						seekQualifiedMemberTypes(name.substring(index + 1, name.length()), type, partialMatch, requestor, acceptFlags);
					}
				}
			}

		}
	}
/**
 * Remembers a set of compilation units that will be looked inside
 * when looking up a type. If they are working copies, they take
 * precedence of their compilation units.
 * <code>null</code> means that no special compilation units should be used.
 */
public void setUnitsToLookInside(IWorkingCopy[] unitsToLookInside) {
	
	if (unitsToLookInside == null) {
		this.unitsToLookInside.setCurrent(null); 
	} else {
		HashMap workingCopies = new HashMap();
		this.unitsToLookInside.setCurrent(workingCopies);
		for (int i = 0, length = unitsToLookInside.length; i < length; i++) {
			IWorkingCopy unitToLookInside = unitsToLookInside[i];
			ICompilationUnit original = (ICompilationUnit)unitToLookInside.getOriginalElement();
			if (original != null) {
				workingCopies.put(original, unitToLookInside);
			} else {
				workingCopies.put(unitToLookInside, unitToLookInside);
			}
		}
	}
}

	/**
	 * Notifies the given requestor of all types (classes and interfaces) in the
	 * given type with the given (possibly qualified) name. Checks
	 * the requestor at regular intervals to see if the requestor
	 * has canceled.
	 *
	 * @param partialMatch partial name matches qualify when <code>true</code>,
	 *  only exact name matches qualify when <code>false</code>
	 */
	protected void seekQualifiedMemberTypes(String qualifiedName, IType type, boolean partialMatch, IJavaElementRequestor requestor, int acceptFlags) {
		if (type == null)
			return;
		IType[] types= null;
		try {
			types= type.getTypes();
		} catch (JavaModelException npe) {
			return; // the enclosing type is not present
		}
		String matchName= qualifiedName;
		int index= qualifiedName.indexOf('$');
		boolean nested= false;
		if (index != -1) {
			matchName= qualifiedName.substring(0, index);
			nested= true;
		}
		int length= types.length;
		for (int i= 0; i < length; i++) {
			if (requestor.isCanceled())
				return;
			IType memberType= types[i];
			if (nameMatches(matchName, memberType, partialMatch))
				if (nested) {
					seekQualifiedMemberTypes(qualifiedName.substring(index + 1, qualifiedName.length()), memberType, partialMatch, requestor, acceptFlags);
				} else {
					if (acceptType(memberType, acceptFlags)) requestor.acceptMemberType(memberType);
				}
		}
	}
}
