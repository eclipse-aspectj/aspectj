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
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.batch.ClasspathDirectory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.HandleFactory;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.Util;

/**
 * Locate matches in compilation units.
 */
public class MatchLocator implements ITypeRequestor {
	public SearchPattern pattern;
	public int detailLevel;
	public IJavaSearchResultCollector collector;
	public IJavaSearchScope scope;

	public MatchLocatorParser parser;
	private INameEnvironment nameEnvironment;
	public NameLookup nameLookup;
	public LookupEnvironment lookupEnvironment;
	public HashtableOfObject parsedUnits;
	public MatchingOpenableSet matchingOpenables;
	private MatchingOpenable currentMatchingOpenable;
	public HandleFactory handleFactory;
	public IWorkingCopy[] workingCopies;

	private static char[] EMPTY_FILE_NAME = new char[0];

	public MatchLocator(
		SearchPattern pattern,
		int detailLevel,
		IJavaSearchResultCollector collector,
		IJavaSearchScope scope) {

		this.pattern = pattern;
		this.detailLevel = detailLevel;
		this.collector = collector;
		this.scope = scope;
	}
	
	/**
	 * Add an additional binary type
	 */
	public void accept(IBinaryType binaryType, PackageBinding packageBinding) {
		BinaryTypeBinding binaryBinding =  new BinaryTypeBinding(packageBinding, binaryType, this.lookupEnvironment);
		ReferenceBinding cachedType = this.lookupEnvironment.getCachedType(binaryBinding.compoundName);
		if (cachedType == null || cachedType instanceof UnresolvedReferenceBinding) { // NB: cachedType is not null if already cached as a source type
			this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
		}
	}

	/**
	 * Add an additional compilation unit.
	 */
	public void accept(ICompilationUnit sourceUnit) {
		// diet parse
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(new String(sourceUnit.getFileName())));
		CompilationUnit compilationUnit = (CompilationUnit)JavaCore.create(file);
		CompilationUnitDeclaration parsedUnit = this.parser.dietParse(sourceUnit, this, file, compilationUnit);

		// build bindings
		this.lookupEnvironment.buildTypeBindings(parsedUnit);
		this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
		
		// remember parsed unit
		ImportReference pkg = parsedUnit.currentPackage;
		char[][] packageName = pkg == null ? null : pkg.tokens;
		char[] mainTypeName = sourceUnit.getMainTypeName();
		char[] qualifiedName = packageName == null ? mainTypeName : CharOperation.concatWith(packageName, mainTypeName, '.');
		this.parsedUnits.put(qualifiedName, parsedUnit);
	}

	/**
	 * Add an additional source type
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding) {
		ISourceType sourceType = sourceTypes[0];
		while (sourceType.getEnclosingType() != null)
			sourceType = sourceType.getEnclosingType();
		if (sourceType instanceof SourceTypeElementInfo) {
			// get source
			SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) sourceType;
			IType type = elementInfo.getHandle();
			try {
				this.buildBindings(type.getCompilationUnit());
			} catch (JavaModelException e) {
				// nothing we can do here: ignore
			}
		} else {
			CompilationResult result =
				new CompilationResult(sourceType.getFileName(), 0, 0, 0);
			CompilationUnitDeclaration unit =
				SourceTypeConverter.buildCompilationUnit(
					sourceTypes,
					true,
					true,
					lookupEnvironment.problemReporter,
					result);
			this.lookupEnvironment.buildTypeBindings(unit);
			this.lookupEnvironment.completeTypeBindings(unit, true);
			this.parsedUnits.put(sourceType.getQualifiedName(), unit);
		}
	}

/*
 * Parse the given compiation unit and build its type bindings.
 * Remember the parsed unit.
 */
public CompilationUnitDeclaration buildBindings(org.eclipse.jdt.core.ICompilationUnit compilationUnit) throws JavaModelException {
	final IFile file = 
		compilationUnit.isWorkingCopy() ?
			(IFile)compilationUnit.getOriginalElement().getUnderlyingResource() :
			(IFile)compilationUnit.getUnderlyingResource();
	CompilationUnitDeclaration unit = null;
	
	// get main type name
	final String fileName = file.getFullPath().lastSegment();
	final char[] mainTypeName =
		fileName.substring(0, fileName.length() - 5).toCharArray();
	
	// find out if unit is already known
	char[] qualifiedName = compilationUnit.getType(new String(mainTypeName)).getFullyQualifiedName().toCharArray();
	unit = (CompilationUnitDeclaration)this.parsedUnits.get(qualifiedName);
	if (unit != null) return unit;

	// source unit
	IBuffer buffer;
	final char[] source = 
		compilationUnit.isWorkingCopy() ?
			(buffer = compilationUnit.getBuffer()) == null ? null : buffer.getCharacters() :
			Util.getResourceContentsAsCharArray(file);
	ICompilationUnit sourceUnit = new ICompilationUnit() {
		public char[] getContents() {
			return source;
		}
		public char[] getFileName() {
			return fileName.toCharArray();
		}
		public char[] getMainTypeName() {
			return mainTypeName;
		}
		public char[][] getPackageName() {
			return null;
		}
	};
	
	// diet parse
	unit = this.parser.dietParse(sourceUnit, this, file, (CompilationUnit)compilationUnit);
	if (unit != null) {
		this.lookupEnvironment.buildTypeBindings(unit);
		this.lookupEnvironment.completeTypeBindings(unit, true);
		this.parsedUnits.put(qualifiedName, unit);
	}
	return unit;
}

	/**
	 * Creates an IField from the given field declaration and type. 
	 */
	public IField createFieldHandle(
		FieldDeclaration field,
		IType type) {
		if (type == null) return null;
		return type.getField(new String(field.name));
	}

	/**
	 * Creates an IImportDeclaration from the given import statement
	 */
	public IJavaElement createImportHandle(ImportReference importRef) {
		char[] importName = CharOperation.concatWith(importRef.getImportName(), '.');
		if (importRef.onDemand) {
			importName = CharOperation.concat(importName, ".*" .toCharArray()); //$NON-NLS-1$
		}
		Openable currentOpenable = this.getCurrentOpenable();
		if (currentOpenable instanceof CompilationUnit) {
			return ((CompilationUnit)currentOpenable).getImport(
				new String(importName));
		} else {
			try {
				return ((org.eclipse.jdt.internal.core.ClassFile)currentOpenable).getType();
			} catch (JavaModelException e) {
				return null;
			}
		}
	}

	/**
	 * Creates an IInitializer from the given field declaration and type. 
	 */
	public IInitializer createInitializerHandle(
		TypeDeclaration typeDecl,
		FieldDeclaration initializer,
		IType type) {
		if (type == null) return null;

		// find occurence count of the given initializer in its type declaration
		int occurrenceCount = 0;
		FieldDeclaration[] fields = typeDecl.fields;
		for (int i = 0, length = fields.length; i < length; i++) {
			FieldDeclaration field = fields[i];
			if (!field.isField()) {
				occurrenceCount++;
				if (field.equals(initializer)) {
					break;
				}
			}
		}

		return type.getInitializer(occurrenceCount);
	}

	/**
	 * Creates an IMethod from the given method declaration and type. 
	 */
	public IMethod createMethodHandle(
		AbstractMethodDeclaration method,
		IType type) {
		if (type == null) return null;
		Argument[] arguments = method.arguments;
		int length = arguments == null ? 0 : arguments.length;
		if (type.isBinary()) {
			// don't cache the methods of the binary type
			ClassFileReader reader = this.classFileReader(type);
			if (reader == null) return null;
			IBinaryMethod[] methods = reader.getMethods();

			if (methods != null) {
				for (int i = 0, methodsLength = methods.length; i < methodsLength; i++) {
					IBinaryMethod binaryMethod = methods[i];
					char[] selector = binaryMethod.isConstructor() ? type.getElementName().toCharArray() : binaryMethod.getSelector();
					if (CharOperation.equals(selector, method.selector)) {
						String[] parameterTypes = Signature.getParameterTypes(new String(binaryMethod.getMethodDescriptor()));
						if (length != parameterTypes.length) continue;
						boolean sameParameters = true;
						for (int j = 0; j < length; j++) {
							TypeReference parameterType = arguments[j].type;
							char[] typeName = CharOperation.concatWith(parameterType.getTypeName(), '.');
							for (int k = 0; k < parameterType.dimensions(); k++) {
								typeName = CharOperation.concat(typeName, "[]" .toCharArray()); //$NON-NLS-1$
							}
							String parameterTypeName = parameterTypes[j].replace('/', '.');
							if (!Signature.toString(parameterTypeName).endsWith(new String(typeName))) {
								sameParameters = false;
								break;
							} else {
								parameterTypes[j] = parameterTypeName;
							}
						}
						if (sameParameters) {
							return type.getMethod(new String(selector), parameterTypes);
						}
					}
				}
			}
			return null;
		} else {
			String[] parameterTypeSignatures = new String[length];
			for (int i = 0; i < length; i++) {
				TypeReference parameterType = arguments[i].type;
				char[] typeName = CharOperation.concatWith(parameterType.getTypeName(), '.');
				for (int j = 0; j < parameterType.dimensions(); j++) {
					typeName = CharOperation.concat(typeName, "[]" .toCharArray()); //$NON-NLS-1$
				}
				parameterTypeSignatures[i] = Signature.createTypeSignature(typeName, false);
			}
			return type.getMethod(new String(method.selector), parameterTypeSignatures);
		}
	}

	private ClassFileReader classFileReader(IType type) {
		IClassFile classFile = type.getClassFile(); 
		if (((IOpenable)classFile).isOpen()) {
			JavaModelManager manager = JavaModelManager.getJavaModelManager();
			synchronized(manager){
				return (ClassFileReader)manager.getInfo(type);
			}
		} else {
			IPackageFragment pkg = type.getPackageFragment();
			IPackageFragmentRoot root = (IPackageFragmentRoot)pkg.getParent();
			try {
				if (root.isArchive()) {
					IPath zipPath = root.isExternal() ? root.getPath() : root.getUnderlyingResource().getLocation();
					ZipFile zipFile = null;
					try {
						zipFile = new ZipFile(zipPath.toOSString());
						char[] pkgPath = pkg.getElementName().toCharArray();
						CharOperation.replace(pkgPath, '.', '/');
						char[] classFileName = classFile.getElementName().toCharArray();
						char[] path = pkgPath.length == 0 ? classFileName : CharOperation.concat(pkgPath, classFileName, '/');
						return ClassFileReader.read(zipFile, new String(path));
					} finally {
						if (zipFile != null) {
							try {
								zipFile.close();
							} catch (IOException e) {
							}
						}
					}
				} else {
					return ClassFileReader.read(type.getUnderlyingResource().getLocation().toOSString());
				}
			} catch (JavaModelException e) {
				return null;
			} catch (ClassFormatException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
		}
	}

	/**
	 * Creates an IType from the given simple top level type name. 
	 */
	public IType createTypeHandle(char[] simpleTypeName) {
		Openable currentOpenable = this.getCurrentOpenable();
		if (currentOpenable instanceof CompilationUnit) {
			// creates compilation unit
			CompilationUnit unit = (CompilationUnit)currentOpenable;
	
			// create type
			return unit.getType(new String(simpleTypeName));
		} else {
			IType type; 
			try {
				type = ((org.eclipse.jdt.internal.core.ClassFile)currentOpenable).getType();
			} catch (JavaModelException e) {
				return null;
			}
			// ensure this is a top level type (see bug 20011  Searching for Inner Classes gives bad search results)
			IType declaringType = type.getDeclaringType();
			while (declaringType != null) {
				type = declaringType;
				declaringType = type.getDeclaringType();
			}
			return type;
		}
	}
	/**
	 * Creates an IType from the given simple inner type name and parent type. 
	 */
	public IType createTypeHandle(IType parent, char[] simpleTypeName) {
		return parent.getType(new String(simpleTypeName));
	}
	protected IResource getCurrentResource() {
		return this.currentMatchingOpenable.resource;
	}

	protected Scanner getScanner() {
		return this.parser == null ? null : this.parser.scanner;
	}


	/**
	 * Locate the matches in the given files and report them using the search requestor. 
	 */
	public void locateMatches(
		String[] filePaths, 
		IWorkspace workspace,
		IWorkingCopy[] workingCopies, 
		IProgressMonitor progressMonitor)
		throws JavaModelException {
			
		if (SearchEngine.VERBOSE) {
			System.out.println("Locating matches in files ["); //$NON-NLS-1$
			for (int i = 0, length = filePaths.length; i < length; i++) {
				String path = filePaths[i];
				System.out.println("\t" + path); //$NON-NLS-1$
			}
			System.out.println("]"); //$NON-NLS-1$
			if (workingCopies != null) {
				 System.out.println(" and working copies ["); //$NON-NLS-1$
				for (int i = 0, length = workingCopies.length; i < length; i++) {
					IWorkingCopy wc = workingCopies[i];
					System.out.println("\t" + ((JavaElement)wc).toStringWithAncestors()); //$NON-NLS-1$
				}
				System.out.println("]"); //$NON-NLS-1$
			}
		}
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			// optimize access to zip files during search operation
			manager.cacheZipFiles();
				
			// initialize handle factory (used as a cache of handles so as to optimize space)
			if (this.handleFactory == null) {
				this.handleFactory = new HandleFactory(workspace);
			}
			
			// initialize locator with working copies
			this.workingCopies = workingCopies;
			
			// substitute compilation units with working copies
			HashMap wcPaths = new HashMap(); // a map from path to working copies
			int wcLength;
			if (workingCopies != null && (wcLength = workingCopies.length) > 0) {
				String[] newPaths = new String[wcLength];
				for (int i = 0; i < wcLength; i++) {
					IWorkingCopy workingCopy = workingCopies[i];
					String path = workingCopy.getOriginalElement().getPath().toString();
					wcPaths.put(path, workingCopy);
					newPaths[i] = path;
				}
				int filePathsLength = filePaths.length;
				System.arraycopy(filePaths, 0, filePaths = new String[filePathsLength+wcLength], 0, filePathsLength);
				System.arraycopy(newPaths, 0, filePaths, filePathsLength, wcLength);
			}
			
			int length = filePaths.length;
			if (progressMonitor != null) {
				if (this.pattern.needsResolve) {
					progressMonitor.beginTask("", length * 10); // 1 for file path, 3 for parsing, 6 for binding resolution //$NON-NLS-1$
				} else {
					progressMonitor.beginTask("", length * 4); // 1 for file path, 3 for parsing //$NON-NLS-1$
				}
			}
	
			// sort file paths projects
			Util.sort(filePaths); 
			
			// initialize pattern for polymorphic search (ie. method reference pattern)
			this.matchingOpenables = new MatchingOpenableSet();
			this.pattern.initializePolymorphicSearch(this, progressMonitor);
			
			JavaProject previousJavaProject = null;
			for (int i = 0; i < length; i++) {
				if (progressMonitor != null && progressMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				String pathString = filePaths[i];
				
				// skip duplicate paths
				if (i > 0 && pathString.equals(filePaths[i-1])) continue;
				
				Openable openable;
				IWorkingCopy workingCopy = (IWorkingCopy)wcPaths.get(pathString);
				if (workingCopy != null) {
					openable = (Openable)workingCopy;
				} else {
					openable = this.handleFactory.createOpenable(pathString);
					if (openable == null)
						continue; // match is outside classpath
				}
	
				// create new parser and lookup environment if this is a new project
				IResource resource = null;
				JavaProject javaProject = null;
				try {
					javaProject = (JavaProject) openable.getJavaProject();
					if (workingCopy != null) {
						resource = workingCopy.getOriginalElement().getUnderlyingResource();
					} else {
						resource = openable.getUnderlyingResource();
					}
					if (resource == null) { // case of a file in an external jar
						resource = javaProject.getProject();
					}
					if (!javaProject.equals(previousJavaProject)) {
						// locate matches in previous project
						if (previousJavaProject != null) {
							try {
								this.locateMatches(previousJavaProject, progressMonitor);
							} catch (JavaModelException e) {
								if (e.getException() instanceof CoreException) {
									throw e;
								} else {
									// problem with classpath in this project -> skip it
								}
							}
							this.matchingOpenables = new MatchingOpenableSet();
						}
	
						// create parser for this project
						this.createParser(javaProject);
						previousJavaProject = javaProject;
					}
				} catch (JavaModelException e) {
					// file doesn't exist -> skip it
					continue;
				}
	
				// add matching openable
				this.addMatchingOpenable(resource, openable);
	
				if (progressMonitor != null) {
					progressMonitor.worked(1);
				}
			}
			
			// last project
			if (previousJavaProject != null) {
				try {
					this.locateMatches(previousJavaProject, progressMonitor);
				} catch (JavaModelException e) {
					if (e.getException() instanceof CoreException) {
						throw e;
					} else {
						// problem with classpath in last project -> skip it
					}
				}
				this.matchingOpenables = new MatchingOpenableSet();
			} 
			
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		} finally {
			if (this.nameEnvironment != null) {
				this.nameEnvironment.cleanup();
			}
			this.parsedUnits = null;
			manager.flushZipFiles();
		}	
	}

	/**
	 * Locates the package declarations corresponding to this locator's pattern. 
	 */
	public void locatePackageDeclarations(IWorkspace workspace)
		throws JavaModelException {
		this.locatePackageDeclarations(this.pattern, workspace);
	}

	/**
	 * Locates the package declarations corresponding to the search pattern. 
	 */
	private void locatePackageDeclarations(
		SearchPattern searchPattern,
		IWorkspace workspace)
		throws JavaModelException {
		if (searchPattern instanceof OrPattern) {
			OrPattern orPattern = (OrPattern) searchPattern;
			this.locatePackageDeclarations(orPattern.leftPattern, workspace);
			this.locatePackageDeclarations(orPattern.rightPattern, workspace);
		} else
			if (searchPattern instanceof PackageDeclarationPattern) {
				PackageDeclarationPattern pkgPattern =
					(PackageDeclarationPattern) searchPattern;
				IJavaProject[] projects =
					JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
				for (int i = 0, length = projects.length; i < length; i++) {
					IJavaProject javaProject = projects[i];
					IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
					for (int j = 0, rootsLength = roots.length; j < rootsLength; j++) {
						IJavaElement[] pkgs = roots[j].getChildren();
						for (int k = 0, pksLength = pkgs.length; k < pksLength; k++) {
							IPackageFragment pkg = (IPackageFragment)pkgs[k];
							if (pkg.getChildren().length > 0 
									&& pkgPattern.matchesName(pkgPattern.pkgName, pkg.getElementName().toCharArray())) {
								IResource resource = pkg.getUnderlyingResource();
								if (resource == null) { // case of a file in an external jar
									resource = javaProject.getProject();
								}
								this.currentMatchingOpenable = new MatchingOpenable(this, resource, null);
								try {
									this.report(-1, -2, pkg, IJavaSearchResultCollector.EXACT_MATCH);
								} catch (CoreException e) {
									if (e instanceof JavaModelException) {
										throw (JavaModelException) e;
									} else {
										throw new JavaModelException(e);
									}
								}
							}
						}
					}
				}
			}
	}
public IType lookupType(TypeBinding typeBinding) {
	char[] packageName = typeBinding.qualifiedPackageName();
	char[] typeName = typeBinding.qualifiedSourceName();
	
	// find package fragments
	IPackageFragment[] pkgs = 
		this.nameLookup.findPackageFragments(
			(packageName == null || packageName.length == 0) ? 
				IPackageFragment.DEFAULT_PACKAGE_NAME : 
				new String(packageName), 
			false);
			
	// iterate type lookup in each package fragment
	for (int i = 0, length = pkgs == null ? 0 : pkgs.length; i < length; i++) {
		IType type = 
			this.nameLookup.findType(
				new String(typeName), 
				pkgs[i], 
				false, 
				typeBinding.isClass() ?
					NameLookup.ACCEPT_CLASSES:
					NameLookup.ACCEPT_INTERFACES);
		if (type != null) return type;	
	}
	
	// search inside enclosing element
	char[][] qualifiedName = CharOperation.splitOn('.', typeName);
	int length = qualifiedName.length;
	if (length == 0) return null;
	IType type = this.createTypeHandle(qualifiedName[0]);
	if (type == null) return null;
	for (int i = 1; i < length; i++) {
		type = this.createTypeHandle(type, qualifiedName[i]);
		if (type == null) return null;
	}
	if (type.exists()) return type;	
	
	return null;
}
	public void report(
		int sourceStart,
		int sourceEnd,
		IJavaElement element,
		int accuracy)
		throws CoreException {

		if (this.scope.encloses(element)) {
			if (SearchEngine.VERBOSE) {
				IResource res = this.getCurrentResource();
				System.out.println("Reporting match"); //$NON-NLS-1$
				System.out.println("\tResource: " + (res == null ? " <unknown> " : res.getFullPath().toString())); //$NON-NLS-2$//$NON-NLS-1$
				System.out.println("\tPositions: [" + sourceStart + ", " + sourceEnd + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				System.out.println("\tJava element: " + ((JavaElement)element).toStringWithAncestors()); //$NON-NLS-1$
				if (accuracy == IJavaSearchResultCollector.EXACT_MATCH) {
					System.out.println("\tAccuracy: EXACT_MATCH"); //$NON-NLS-1$
				} else {
					System.out.println("\tAccuracy: POTENTIAL_MATCH"); //$NON-NLS-1$
				}
			}
			this.report(
				this.getCurrentResource(),
				sourceStart,
				sourceEnd,
				element,
				accuracy);
		}
	}
	public void report(
		IResource resource,
		int sourceStart,
		int sourceEnd,
		IJavaElement element,
		int accuracy)
		throws CoreException {

		this.collector.accept(
			resource,
			sourceStart,
			sourceEnd + 1,
			element,
			accuracy);
	}

	public void reportBinaryMatch(
		IMember binaryMember,
		IBinaryType info,
		int accuracy)
		throws CoreException, JavaModelException {
			
		this.reportBinaryMatch(null, binaryMember, info, accuracy);
	}
	public void reportBinaryMatch(
		IResource resource,
		IMember binaryMember,
		IBinaryType info,
		int accuracy)
		throws CoreException, JavaModelException {
		ISourceRange range = binaryMember.getNameRange();
		if (range.getOffset() == -1) {
			ClassFile classFile = (ClassFile) binaryMember.getClassFile();
			SourceMapper mapper = classFile.getSourceMapper();
			if (mapper != null) {
				IType type = classFile.getType();
				char[] contents = mapper.findSource(type, info);
				if (contents != null) {
					range = mapper.mapSource(type, contents, binaryMember);
				}
			}
		}
		int startIndex = range.getOffset();
		int endIndex = startIndex + range.getLength() - 1;
		if (resource == null) {
			this.report(startIndex, endIndex, binaryMember, accuracy);
		} else {
			this.report(resource, startIndex, endIndex, binaryMember, accuracy);
		}
	}

	/**
	 * Reports the given field declaration to the search requestor.
	 */
	public void reportFieldDeclaration(
		FieldDeclaration fieldDeclaration,
		IJavaElement parent,
		int accuracy)
		throws CoreException {

		// accept field declaration
		this.report(
			fieldDeclaration.sourceStart,
			fieldDeclaration.sourceEnd,
			(parent instanceof IType) ?
				((IType)parent).getField(new String(fieldDeclaration.name)) :
				parent,
			accuracy);
	}

	/**
	 * Reports the given import to the search requestor.
	 */
	public void reportImport(ImportReference reference, int accuracy)
		throws CoreException {

		// create defining import handle
		IJavaElement importHandle = this.createImportHandle(reference);

		// accept reference
		this.pattern.matchReportImportRef(reference, null, importHandle, accuracy, this);
	}

	/**
	 * Reports the given method declaration to the search requestor.
	 */
	public void reportMethodDeclaration(
		AbstractMethodDeclaration methodDeclaration,
		IJavaElement parent,
		int accuracy)
		throws CoreException {

		IJavaElement enclosingElement;
		if (parent instanceof IType) {
			// create method handle
			enclosingElement = this.createMethodHandle(methodDeclaration, (IType)parent);
			if (enclosingElement == null) return;
		} else {
			enclosingElement = parent;
		}

		// compute source positions of the selector 
		Scanner scanner = parser.scanner;
		int nameSourceStart = methodDeclaration.sourceStart;
		scanner.setSource(
			this.currentMatchingOpenable.getSource());
		scanner.resetTo(nameSourceStart, methodDeclaration.sourceEnd);
		try {
			scanner.getNextToken();
		} catch (InvalidInputException e) {
		}
		int nameSourceEnd = scanner.currentPosition - 1;

		// accept method declaration
		this.report(nameSourceStart, nameSourceEnd, enclosingElement, accuracy);
	}

	/**
	 * Reports the given package declaration to the search requestor.
	 */
	public void reportPackageDeclaration(ImportReference node) {
		// TBD
	}

	/**
	 * Reports the given package reference to the search requestor.
	 */
	public void reportPackageReference(ImportReference node) {
		// TBD
	}

	/**
	 * Finds the accurate positions of the sequence of tokens given by qualifiedName
	 * in the source and reports a reference to this this qualified name
	 * to the search requestor.
	 */
	public void reportAccurateReference(
		int sourceStart,
		int sourceEnd,
		char[][] qualifiedName,
		IJavaElement element,
		int accuracy)
		throws CoreException {
	
		if (accuracy == -1) return;
	
		// compute source positions of the qualified reference 
		Scanner scanner = parser.scanner;
		scanner.setSource(
			this.currentMatchingOpenable.getSource());
		scanner.resetTo(sourceStart, sourceEnd);
	
		int refSourceStart = -1, refSourceEnd = -1;
		int tokenNumber = qualifiedName.length;
		int token = -1;
		int previousValid = -1;
		int i = 0;
		int currentPosition;
		do {
			// find first token that is an identifier (parenthesized expressions include parenthesises in source range - see bug 20693 - Finding references to variables does not find all occurrences  )
			do {
				currentPosition = scanner.currentPosition;
				try {
					token = scanner.getNextToken();
				} catch (InvalidInputException e) {
				}
			} while (token !=  ITerminalSymbols.TokenNameIdentifier && token !=  ITerminalSymbols.TokenNameEOF);
	
			if (token != ITerminalSymbols.TokenNameEOF) {
				char[] currentTokenSource = scanner.getCurrentTokenSource();
				boolean equals = false;
				while (i < tokenNumber
					&& !(equals = this.pattern.matchesName(qualifiedName[i++], currentTokenSource))) {
				}
				if (equals && (previousValid == -1 || previousValid == i - 2)) {
					previousValid = i - 1;
					if (refSourceStart == -1) {
						refSourceStart = currentPosition;
					}
					refSourceEnd = scanner.currentPosition - 1;
				} else {
					i = 0;
					refSourceStart = -1;
					previousValid = -1;
				}
				// read '.'
				try {
					token = scanner.getNextToken();
				} catch (InvalidInputException e) {
				}
			}
			if (i == tokenNumber) {
				// accept reference
				if (refSourceStart != -1) {
					this.report(refSourceStart, refSourceEnd, element, accuracy);
				} else {
					this.report(sourceStart, sourceEnd, element, accuracy);
				}
				return;
			}
		} while (token != ITerminalSymbols.TokenNameEOF);
	
	}
	/**
	 * Finds the accurate positions of each valid token in the source and
	 * reports a reference to this token to the search requestor.
	 * A token is valid if it has an accurracy which is not -1.
	 */
	public void reportAccurateReference(
		int sourceStart,
		int sourceEnd,
		char[][] tokens,
		IJavaElement element,
		int[] accuracies)
		throws CoreException {

		// compute source positions of the qualified reference 
		Scanner scanner = parser.scanner;
		scanner.setSource(
			this.currentMatchingOpenable.getSource());
		scanner.resetTo(sourceStart, sourceEnd);

		int refSourceStart = -1, refSourceEnd = -1;
		int length = tokens.length;
		int token = -1;
		int previousValid = -1;
		int i = 0;
		int accuracyIndex = 0;
		do {
			int currentPosition = scanner.currentPosition;
			// read token
			try {
				token = scanner.getNextToken();
			} catch (InvalidInputException e) {
			}
			if (token != ITerminalSymbols.TokenNameEOF) {
				char[] currentTokenSource = scanner.getCurrentTokenSource();
				boolean equals = false;
				while (i < length
					&& !(equals = this.pattern.matchesName(tokens[i++], currentTokenSource))) {
				}
				if (equals && (previousValid == -1 || previousValid == i - 2)) {
					previousValid = i - 1;
					if (refSourceStart == -1) {
						refSourceStart = currentPosition;
					}
					refSourceEnd = scanner.currentPosition - 1;
				} else {
					i = 0;
					refSourceStart = -1;
					previousValid = -1;
				}
				// read '.'
				try {
					token = scanner.getNextToken();
				} catch (InvalidInputException e) {
				}
			}
			if (accuracies[accuracyIndex] != -1) {
				// accept reference
				if (refSourceStart != -1) {
					this.report(refSourceStart, refSourceEnd, element, accuracies[accuracyIndex]);
				} else {
					this.report(sourceStart, sourceEnd, element, accuracies[accuracyIndex]);
				}
				i = 0;
			}
			refSourceStart = -1;
			previousValid = -1;
			if (accuracyIndex < accuracies.length-1) {
				accuracyIndex++;
			}
		} while (token != ITerminalSymbols.TokenNameEOF);

	}

	/**
	 * Reports the given reference to the search requestor.
	 * It is done in the given method and the method's defining types 
	 * have the given simple names.
	 */
	public void reportReference(
		AstNode reference,
		AbstractMethodDeclaration methodDeclaration,
		IJavaElement parent,
		int accuracy)
		throws CoreException {

		IJavaElement enclosingElement;
		if (parent instanceof IType) {
			// create defining method handle
			enclosingElement = this.createMethodHandle(methodDeclaration, (IType)parent);
			if (enclosingElement == null) return; // case of a match found in a type other than the current class file
		} else {
			enclosingElement = parent;
		}

		// accept reference
		this.pattern.matchReportReference(reference, enclosingElement, accuracy, this);
	}

	/**
	 * Reports the given reference to the search requestor.
	 * It is done in the given field and given type.
	 * The field's defining types have the given simple names.
	 */
	public void reportReference(
		AstNode reference,
		TypeDeclaration typeDeclaration,
		FieldDeclaration fieldDeclaration,
		IJavaElement parent,
		int accuracy)
		throws CoreException {

		IJavaElement enclosingElement;
		if (fieldDeclaration.isField()) {
			if (parent instanceof IType) {
				// create defining field handle
				enclosingElement = this.createFieldHandle(fieldDeclaration, (IType)parent);
				if (enclosingElement == null) return;
			} else {
				enclosingElement = parent;
			}

			// accept reference
			this.pattern.matchReportReference(reference, enclosingElement, accuracy, this);
		} else { // initializer
			if (parent instanceof IType) {
				// create defining initializer
				enclosingElement =
					this.createInitializerHandle(
						typeDeclaration,
						fieldDeclaration,
						(IType)parent);
				if (enclosingElement == null) return;
			} else {
				enclosingElement = parent;
			}

			// accept reference
			this.pattern.matchReportReference(reference, enclosingElement, accuracy, this);
		}
	}

	/**
	 * Reports the given super type reference to the search requestor.
	 * It is done in the given defining type (with the given simple names).
	 */
	public void reportSuperTypeReference(
		TypeReference typeRef,
		IJavaElement type,
		int accuracy)
		throws CoreException {

		// accept type reference
		this.pattern.matchReportReference(typeRef, type, accuracy, this);
	}

	/**
	 * Reports the given type declaration to the search requestor.
	 */
	public void reportTypeDeclaration(
		TypeDeclaration typeDeclaration,
		IJavaElement parent,
		int accuracy)
		throws CoreException {

		// accept class or interface declaration
		this.report(
			typeDeclaration.sourceStart,
			typeDeclaration.sourceEnd,
			(parent == null) ?
				this.createTypeHandle(typeDeclaration.name) :
				(parent instanceof IType) ?
					this.createTypeHandle((IType)parent, typeDeclaration.name) :
					parent,
			accuracy);
	}

private MatchingOpenable newMatchingOpenable(IResource resource, Openable openable) {
	MatchingOpenable matchingOpenable;
	try {
		matchingOpenable = new MatchingOpenable(this, resource, openable);
	} catch (AbortCompilation e) {
		// problem with class path: ignore this matching openable
		return null;
	}
	return matchingOpenable;
}

private void addMatchingOpenable(IResource resource, Openable openable)
		throws JavaModelException {
		
	MatchingOpenable matchingOpenable = this.newMatchingOpenable(resource, openable);
	if (matchingOpenable != null) {
		this.matchingOpenables.add(matchingOpenable);
	}
}


	/**
	 * Create a new parser for the given project, as well as a lookup environment.
	 * Asks the pattern to initialize itself for polymorphic search.
	 */
	public void createParser(JavaProject project) throws JavaModelException {
		// cleaup and recreate file name environment
		if (this.nameEnvironment != null) {
			this.nameEnvironment.cleanup();
		}
		this.nameEnvironment = this.getNameEnvironment(project);
		
		// create lookup environment
		CompilerOptions options = new CompilerOptions(JavaCore.getOptions());
		ProblemReporter problemReporter =
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options,
				new DefaultProblemFactory());
		this.lookupEnvironment =
			new LookupEnvironment(this, options, problemReporter, this.nameEnvironment);
			
		// create parser
		this.parser = new MatchLocatorParser(problemReporter, options.assertMode);
		
		// reset parsed units (they could hold onto obsolete bindings: see bug 16052)
		MatchingOpenable[] openables = this.matchingOpenables.getMatchingOpenables(project.getPackageFragmentRoots());
		for (int i = 0, length = openables.length; i < length; i++) {
			MatchingOpenable matchingOpenable = openables[i];
			matchingOpenable.reset();
		}
		this.parsedUnits = new HashtableOfObject(10);
		
		// remember project's name lookup
		this.nameLookup = project.getNameLookup();
	}

	private INameEnvironment getNameEnvironment(JavaProject project) throws JavaModelException {
		//return project.getSearchableNameEnvironment();
		
		IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
		int length = roots.length;
		String[] classpathNames = new String[length];
		int rootModes[] = new int[length];
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = 0; i < length; i++) {
			IPackageFragmentRoot root = roots[i];
			IPath path = root.getPath();
			rootModes[i] = (root.getKind() == IPackageFragmentRoot.K_SOURCE)  ? ClasspathDirectory.SOURCE : ClasspathDirectory.BINARY;
			if (root.isArchive()) {
				// pass in a relative path (for internal jar) as this is what is needed by FileNamewEnviroment.getZipFile(File)
				classpathNames[i] = path.toOSString();
			} else {
				Object target = JavaModel.getTarget(workspaceRoot, path, false);
				if (target instanceof IResource) {
					classpathNames[i] = ((IResource)target).getLocation().toOSString();
				} else {
					classpathNames[i] = path.toOSString();
				}
			}
		}
		String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);
		return new FileNameEnvironment(classpathNames, encoding, rootModes);
		
	}

	public CompilationUnitDeclaration dietParse(final char[] source) {
		// source unit
		ICompilationUnit sourceUnit = new ICompilationUnit() {
			public char[] getContents() {
				return source;
			}
			public char[] getFileName() {
				return EMPTY_FILE_NAME; // not used
			}
			public char[] getMainTypeName() {
				return null; // don't need to check if main type name == compilation unit name
			}
			public char[][] getPackageName() {
				return null;
			}
		};
		
		// diet parse
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);  
		return this.parser.dietParse(sourceUnit, compilationResult);
	}
	
	public char[] findSource(ClassFile classFile) {
		char[] source = null; 
		try {
			SourceMapper sourceMapper = classFile.getSourceMapper();
			if (sourceMapper != null) {
				IType type = classFile.getType();
				if (classFile.isOpen() && type.getDeclaringType() == null) {
					source = sourceMapper.findSource(type);
				} else {
					ClassFileReader reader = this.classFileReader(type);
					if (reader != null) {
						source = sourceMapper.findSource(type, reader);
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return source;
	}
public IBinaryType getBinaryInfo(org.eclipse.jdt.internal.core.ClassFile classFile, IResource resource) throws CoreException {
	BinaryType binaryType = (BinaryType)classFile.getType();
	if (classFile.isOpen()) {
		// reuse the info from the java model cache
		return (IBinaryType)binaryType.getRawInfo();
	} else {
		// create a temporary info
		IBinaryType info;
		try {
			IJavaElement pkg = classFile.getParent();
			PackageFragmentRoot root = (PackageFragmentRoot)pkg.getParent();
			if (root.isArchive()) {
				// class file in a jar
				String pkgPath = pkg.getElementName().replace('.', '/');
				String classFilePath = 
					(pkgPath.length() > 0) ?
						pkgPath + "/" + classFile.getElementName() : //$NON-NLS-1$
						classFile.getElementName();
				ZipFile zipFile = null;
				try {
					zipFile = ((JarPackageFragmentRoot)root).getJar();
					info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(
						zipFile,
						classFilePath);
				} finally {
					JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
				}
			} else {
				// class file in a directory
				String osPath = resource.getFullPath().toOSString();
				info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(osPath);
			}
			return info;
		} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
			//e.printStackTrace();
			return null;
		} catch (java.io.IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}
}
	protected Openable getCurrentOpenable() {
		return this.currentMatchingOpenable.openable;
	}

	/**
	 * Locate the matches amongst the matching openables.
	 */
	private void locateMatches(JavaProject javaProject, IProgressMonitor progressMonitor) throws JavaModelException {
		MatchingOpenable[] openables = this.matchingOpenables.getMatchingOpenables(javaProject.getPackageFragmentRoots());
	
		boolean compilationAborted = false;

		if (this.pattern.needsResolve) {
			// binding creation
			for (int i = 0, length = openables.length; i < length; i++) { 
				openables[i].buildTypeBindings();
				if (progressMonitor != null) {
					if (progressMonitor.isCanceled()) {
						throw new OperationCanceledException();
					} else {
						progressMonitor.worked(6);
					}
				}
			}
	
			// binding resolution
			try {
				this.lookupEnvironment.completeTypeBindings();
			} catch (AbortCompilation e) {
				// problem with class path: it could not find base classes
				// continue reporting innacurate matches (since bindings will be null)
				compilationAborted = true;
			}
		}

		// matching openable resolution
		for (int i = 0, length = openables.length; i < length; i++) { 
			if (progressMonitor != null && progressMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			 
			try {
				this.currentMatchingOpenable = openables[i];
				
				if (!this.currentMatchingOpenable.hasAlreadyDefinedType()) {
					this.currentMatchingOpenable.shouldResolve = !compilationAborted;
					this.currentMatchingOpenable.locateMatches();
				} // else skip type has it is hidden so not visible
			} catch (AbortCompilation e) {
				// problem with class path: it could not find base classes
				// continue and try next matching openable reporting innacurate matches (since bindings will be null)
				compilationAborted = true;
			} catch (CoreException e) {
				if (e instanceof JavaModelException) {
					// problem with class path: it could not find base classes
					// continue and try next matching openable reporting innacurate matches (since bindings will be null)
					compilationAborted = true;
				} else {
					// core exception thrown by client's code: let it through
					throw new JavaModelException(e);
				}
			} finally {
				this.currentMatchingOpenable.reset();
			}
			if (progressMonitor != null) {
				progressMonitor.worked(3);
			}
		}
		this.currentMatchingOpenable = null;
	}
}