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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.IInfoConstants;
import org.eclipse.jdt.internal.core.search.IndexSearchAdapter;
import org.eclipse.jdt.internal.core.search.PathCollector;
import org.eclipse.jdt.internal.core.search.PatternSearchJob;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * Collects the super type names of a given declaring type.
 * Returns NOT_FOUND_DECLARING_TYPE if the declaring type was not found.
 * Returns null if the declaring type pattern doesn't require an exact match.
 */
public class SuperTypeNamesCollector implements ITypeRequestor {
	MethodReferencePattern pattern;
	MatchLocator locator;
	IType type; 
	IProgressMonitor progressMonitor;
	char[][][] result;
	int resultIndex;
	
	
/**
 * An ast visitor that visits type declarations and member type declarations
 * collecting their super type names.
 */
public class TypeDeclarationVisitor extends AbstractSyntaxTreeVisitorAdapter {
	public boolean visit(LocalTypeDeclaration typeDeclaration, BlockScope scope) {
		ReferenceBinding type = typeDeclaration.binding;
		if (SuperTypeNamesCollector.this.matches(type)) {
			SuperTypeNamesCollector.this.collectSuperTypeNames(type);
		}
		return true;
	}
	public boolean visit(AnonymousLocalTypeDeclaration typeDeclaration, BlockScope scope) {
		ReferenceBinding type = typeDeclaration.binding;
		if (SuperTypeNamesCollector.this.matches(type)) {
			SuperTypeNamesCollector.this.collectSuperTypeNames(type);
		}
		return true;
	}
	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		ReferenceBinding type = typeDeclaration.binding;
		if (SuperTypeNamesCollector.this.matches(type)) {
			SuperTypeNamesCollector.this.collectSuperTypeNames(type);
		}
		return true;
	}
	public boolean visit(MemberTypeDeclaration memberTypeDeclaration, 	ClassScope scope) {
		ReferenceBinding type = memberTypeDeclaration.binding;
		if (SuperTypeNamesCollector.this.matches(type)) {
			SuperTypeNamesCollector.this.collectSuperTypeNames(type);
		}
		return true;
	}
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		return false; // don't visit field declarations
	}
	public boolean visit(Initializer initializer, MethodScope scope) {
		return false; // don't visit initializers
	}
	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		return false; // don't visit constructor declarations
	}
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		return false; // don't visit method declarations
	}
}
	
public SuperTypeNamesCollector(
	MethodReferencePattern pattern,
	MatchLocator locator,
	IType type, 
	IProgressMonitor progressMonitor) {
		
	this.pattern = pattern;
	this.locator = locator;
	this.type = type;
	this.progressMonitor = progressMonitor;
}

/*
 * Parse the given compiation unit and build its type bindings.
 * Don't build methods and fields.
 */
private CompilationUnitDeclaration buildBindings(ICompilationUnit compilationUnit) throws JavaModelException {
	final IFile file = 
		compilationUnit.isWorkingCopy() ?
			(IFile)compilationUnit.getOriginalElement().getUnderlyingResource() :
			(IFile)compilationUnit.getUnderlyingResource();
	
	// get main type name
	final String fileName = file.getFullPath().lastSegment();
	final char[] mainTypeName =
		fileName.substring(0, fileName.length() - 5).toCharArray();
	
	// source unit
	IBuffer buffer;
	final char[] source = 
		compilationUnit.isWorkingCopy() ?
			(buffer = compilationUnit.getBuffer()) == null ? null : buffer.getCharacters() :
			Util.getResourceContentsAsCharArray(file);
	org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit = 
		new org.eclipse.jdt.internal.compiler.env.ICompilationUnit() {
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
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
	CompilationUnitDeclaration unit = this.locator.parser.dietParse(sourceUnit, compilationResult);
	if (unit != null) {
		this.locator.lookupEnvironment.buildTypeBindings(unit);
		this.locator.lookupEnvironment.completeTypeBindings(unit, false);
	}
	return unit;
}
private BinaryTypeBinding cacheBinaryType(IType type) throws JavaModelException {
	IType enclosingType = type.getDeclaringType();
	if (enclosingType != null) {
		// force caching of enclosing types first, so that binary type can be found in lookup enviroment
		this.cacheBinaryType(enclosingType);
	}
	IBinaryType binaryType = (IBinaryType)((BinaryType)type).getRawInfo();
	return this.locator.lookupEnvironment.cacheBinaryType(binaryType);
}

protected char[][][] collect() throws JavaModelException {
	
	if (this.type != null) {
		// Collect the paths of the cus that are in the hierarchy of the given type
		this.result = new char[1][][];
		this.resultIndex = 0;
		JavaProject javaProject = (JavaProject)this.type.getJavaProject();
		this.locator.createParser(javaProject);
		synchronized(this.locator.nameLookup) { // prevent 2 concurrent accesses to name lookup while the working copies are set
			this.locator.nameLookup.setUnitsToLookInside(this.locator.workingCopies);
			try {
				if (this.type.isBinary()) {
					BinaryTypeBinding binding = this.cacheBinaryType(this.type);
					this.collectSuperTypeNames(binding);
				} else {
					ICompilationUnit unit = this.type.getCompilationUnit();
					CompilationUnitDeclaration parsedUnit = this.buildBindings(unit);
					if (parsedUnit != null) {
						parsedUnit.traverse(new TypeDeclarationVisitor(), parsedUnit.scope);
					}
				}
			} catch (AbortCompilation e) {
				// problem with classpath: report inacurrate matches
				return null;
			} finally {
				this.locator.nameLookup.setUnitsToLookInside(null);
			}
		}
		return this.result;
	} else {	
		// Collect the paths of the cus that declare a type which matches declaringQualification + declaringSimpleName
		String[] paths = this.getPathsOfDeclaringType();
		
		// Create bindings from source types and binary types
		// and collect super type names of the type declaration 
		// that match the given declaring type
		if (paths != null) {
			Util.sort(paths); // sort by projects
			JavaProject previousProject = null;
			this.result = new char[1][][];
			this.resultIndex = 0;
			try {
				for (int i = 0, length = paths.length; i < length; i++) {
					try {
						Openable openable = this.locator.handleFactory.createOpenable(paths[i]);
						if (openable == null)
							continue; // outside classpath
						IJavaProject project = openable.getJavaProject();
						if (!project.equals(previousProject)) {
							if (previousProject != null) {
								this.locator.nameLookup.setUnitsToLookInside(null);
							}
							previousProject = (JavaProject)project;
							this.locator.createParser(previousProject);
							this.locator.nameLookup.setUnitsToLookInside(this.locator.workingCopies);
						}
						if (openable instanceof ICompilationUnit) {
							ICompilationUnit unit = (ICompilationUnit)openable;
							CompilationUnitDeclaration parsedUnit = this.buildBindings(unit);
							if (parsedUnit != null) {
								parsedUnit.traverse(new TypeDeclarationVisitor(), parsedUnit.scope);
							}
						} else if (openable instanceof IClassFile) {
							IClassFile classFile = (IClassFile)openable;
							BinaryTypeBinding binding = this.cacheBinaryType(classFile.getType());
							if (this.matches(binding)) {
								this.collectSuperTypeNames(binding);
							}
						}
					} catch (AbortCompilation e) {
						// ignore: continue with next element
					} catch (JavaModelException e) {
						// ignore: continue with next element
					}
				}
			} finally {
				if (previousProject != null) {
					this.locator.nameLookup.setUnitsToLookInside(null);
				}
			}
			System.arraycopy(this.result, 0, this.result = new char[this.resultIndex][][], 0, this.resultIndex);
			return this.result;
		} else {
			return null;
		}
	}
}
protected boolean matches(ReferenceBinding type) {
	if (type == null || type.compoundName == null) return false;
	return this.matches(type.compoundName);
}
protected boolean matches(char[][] compoundName) {
	int length = compoundName.length;
	if (length == 0) return false;
	char[] simpleName = compoundName[length-1];
	char[] declaringSimpleName = this.pattern.declaringSimpleName;
	char[] declaringQualification = this.pattern.declaringQualification;
	int last = length - 1;
	if (declaringSimpleName != null) {
		// most frequent case: simple name equals last segment of compoundName
		if (this.pattern.matchesName(simpleName, declaringSimpleName)) {
			char[][] qualification = new char[last][];
			System.arraycopy(compoundName, 0, qualification, 0, last);
			return 
				this.pattern.matchesName(
					declaringQualification, 
					CharOperation.concatWith(qualification, '.'));
		} else if (!CharOperation.endsWith(simpleName, declaringSimpleName)) {
			return false;
		} else {
			// member type -> transform A.B.C$D into A.B.C.D
			System.arraycopy(compoundName, 0, compoundName = new char[length+1][], 0, last);
			int dollar = CharOperation.indexOf('$', simpleName);
			if (dollar == -1) return false;
			compoundName[last] = CharOperation.subarray(simpleName, 0, dollar);
			compoundName[length] = CharOperation.subarray(simpleName, dollar+1, simpleName.length); 
			return this.matches(compoundName);
		}
	} else {
		char[][] qualification = new char[last][];
		System.arraycopy(compoundName, 0, qualification, 0, last);
		return 
			this.pattern.matchesName(
				declaringQualification, 
				CharOperation.concatWith(qualification, '.'));
	}
}
private void addToResult(char[][] compoundName) {
	int resultLength = this.result.length;
	for (int i = 0; i < resultLength; i++) {
		if (CharOperation.equals(this.result[i], compoundName)) {
			// already known
			return;
		}
	}
	if (resultLength == this.resultIndex) {
		System.arraycopy(
			this.result, 
			0, 
			this.result = new char[resultLength*2][][], 
			0, 
			resultLength);
	}
	this.result[this.resultIndex++] = compoundName;
}
/**
 * Collects the names of all the supertypes of the given type.
 */
protected void collectSuperTypeNames(ReferenceBinding type) {

	// superclass
	ReferenceBinding superclass = type.superclass();
	if (superclass != null) {
		this.addToResult(superclass.compoundName);
		this.collectSuperTypeNames(superclass);
	}

	// interfaces
	ReferenceBinding[] interfaces = type.superInterfaces();
	if (interfaces != null) {
		for (int i = 0; i < interfaces.length; i++) {
			ReferenceBinding interfase = interfaces[i];
			this.addToResult(interfase.compoundName);
			this.collectSuperTypeNames(interfase);
		}
	}
}


private String[] getPathsOfDeclaringType() {
	char[] declaringQualification = this.pattern.declaringQualification;
	char[] declaringSimpleName = this.pattern.declaringSimpleName;
	if (declaringQualification != null || declaringSimpleName != null) {
		final PathCollector pathCollector = new PathCollector();
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
	
		IndexManager indexManager = ((JavaModelManager)JavaModelManager.getJavaModelManager())
										.getIndexManager();
		int detailLevel = IInfoConstants.PathInfo;
		SearchPattern searchPattern = new TypeDeclarationPattern(
			declaringSimpleName != null ? null : declaringQualification, // use the qualification only if no simple name
			null, // do find member types
			declaringSimpleName,
			IIndexConstants.TYPE_SUFFIX,
			this.pattern.matchMode, 
			true);
		IIndexSearchRequestor searchRequestor = new IndexSearchAdapter(){
			public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
				if (enclosingTypeNames != IIndexConstants.ONE_ZERO_CHAR) { // filter out local and anonymous classes
					pathCollector.acceptClassDeclaration(resourcePath, simpleTypeName, enclosingTypeNames, packageName);
				}
			}		
			public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
				if (enclosingTypeNames != IIndexConstants.ONE_ZERO_CHAR) { // filter out local and anonymous classes
					pathCollector.acceptInterfaceDeclaration(resourcePath, simpleTypeName, enclosingTypeNames, packageName);
				}
			}		
		};		

		indexManager.performConcurrentJob(
			new PatternSearchJob(
				searchPattern, 
				scope, 
				detailLevel, 
				searchRequestor, 
				indexManager),
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 100));
		return pathCollector.getPaths();

	}
	return null;
}
/*
 * @see ITypeRequestor#accept(IBinaryType, PackageBinding)
 */
public void accept(IBinaryType binaryType, PackageBinding packageBinding) {
	this.locator.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
}

/*
 * @see ITypeRequestor#accept(ICompilationUnit)
 */
public void accept(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit) {
	this.locator.lookupEnvironment.problemReporter.abortDueToInternalError(
		new StringBuffer(org.eclipse.jdt.internal.compiler.util.Util.bind("accept.cannot")) //$NON-NLS-1$
			.append(sourceUnit.getFileName())
			.toString());
}

/*
 * @see ITypeRequestor#accept(ISourceType[], PackageBinding)
 */
public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding) {
	CompilationResult result = new CompilationResult(sourceTypes[0].getFileName(), 1, 1, 0);
	CompilationUnitDeclaration unit =
		SourceTypeConverter.buildCompilationUnit(sourceTypes, false, true, this.locator.lookupEnvironment.problemReporter, result);

	if (unit != null) {
		this.locator.lookupEnvironment.buildTypeBindings(unit);
		this.locator.lookupEnvironment.completeTypeBindings(unit, false);
	}
}

}

