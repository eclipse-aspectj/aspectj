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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.WorkingCopy;
 
public class MatchingOpenable {
	private MatchLocator locator;
	public IResource resource;
	public Openable openable;
	private CompilationUnitDeclaration parsedUnit;
	private MatchSet matchSet;
	public boolean shouldResolve = true;
public MatchingOpenable(MatchLocator locator, IResource resource, Openable openable) {
	this.locator = locator;
	this.resource = resource;
	this.openable = openable;
}
public MatchingOpenable(
		MatchLocator locator, 
		IResource resource, 
		Openable openable,
		CompilationUnitDeclaration parsedUnit,
		MatchSet matchSet) {
	this.locator = locator;
	this.resource = resource;
	this.openable = openable;
	this.parsedUnit = parsedUnit;
	this.matchSet = matchSet;
}
public void buildTypeBindings() {
	
	// if a parsed unit exits, its bindings have already been built
	if (this.parsedUnit != null) return;
	
	char[] source = this.getSource();
	if (source == null) return;
	this.buildTypeBindings(source);
			
	if (this.openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
		// try to use the main type's class file as the openable
		TypeDeclaration[] types = this.parsedUnit.types;
		if (types != null) {
			String classFileName = openable.getElementName();
			for (int i = 0, length = types.length; i < length; i++) {
				TypeDeclaration typeDeclaration = types[i];
				String simpleTypeName = new String(typeDeclaration.name);
				if (classFileName.startsWith(simpleTypeName)) {
					IPackageFragment parent = (IPackageFragment)openable.getParent();
					this.openable = (Openable)parent.getClassFile(simpleTypeName + ".class"); //$NON-NLS-1$
					break;
				}
			} 
		}
	}
}
private void buildTypeBindings(final char[] source) {
	// get qualified name
	char[] qualifiedName = this.getQualifiedName();
	if (qualifiedName == null) return;

	// create match set	
	this.matchSet = new MatchSet(this.locator);
	
	try {
		this.locator.parser.matchSet = this.matchSet;

		this.parsedUnit = (CompilationUnitDeclaration)this.locator.parsedUnits.get(qualifiedName);
		if (this.parsedUnit == null) {
			// diet parse
			this.parsedUnit = this.locator.dietParse(source);
			
			// initial type binding creation
			this.locator.lookupEnvironment.buildTypeBindings(this.parsedUnit);
		} else {
			// free memory
			this.locator.parsedUnits.put(qualifiedName, null);
		}
	} finally {
		this.locator.parser.matchSet = null;
	}
}
private char[] getQualifiedName() {
	if (this.openable instanceof CompilationUnit) {
		// get file name
		String fileName = this.resource.getFullPath().lastSegment();
		// get main type name
		char[] mainTypeName = fileName.substring(0, fileName.length()-5).toCharArray(); 
		CompilationUnit cu = (CompilationUnit)this.openable;
		return cu.getType(new String(mainTypeName)).getFullyQualifiedName().toCharArray();
	} else {
		org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
		try {
			return classFile.getType().getFullyQualifiedName().toCharArray();
		} catch (JavaModelException e) {
			return null; // nothing we can do here
		}
	}
}
public char[] getSource() {
	try {
		if (this.openable instanceof WorkingCopy) {
			IBuffer buffer = this.openable.getBuffer();
			if (buffer == null) return null;
			return buffer.getCharacters();
		} else if (this.openable instanceof CompilationUnit) {
			return Util.getResourceContentsAsCharArray((IFile)this.resource);
		} else if (this.openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
			org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
			return this.locator.findSource(classFile);
		} else {
			return null;
		}
	} catch (JavaModelException e) {
		return null;
	}
}
public boolean hasAlreadyDefinedType() {
	if (this.parsedUnit == null) return false;
	CompilationResult result = this.parsedUnit.compilationResult;
	if (result == null) return false;
	for (int i = 0; i < result.problemCount; i++) {
		IProblem problem = result.problems[i];
		if (problem.getID() == IProblem.DuplicateTypes) {
			return true;
		}
	}
	return false;
}

public void locateMatches() throws CoreException {
	char[] source = this.getSource();
	if (source == null) {
		if (this.openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
			this.locateMatchesInClassFile();
		}
	} else {
		this.locateMatchesInCompilationUnit(source);
	}
}
/**
 * Locate declaration in the current class file. This class file is always in a jar.
 */
private void locateMatchesInClassFile() throws CoreException, JavaModelException {
	org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
	IBinaryType info = this.locator.getBinaryInfo(classFile, this.resource);
	if (info == null) 
		return; // unable to go further

	// check class definition
	BinaryType binaryType = (BinaryType)classFile.getType();
	if (this.locator.pattern.matchesBinary(info, null)) {
		this.locator.reportBinaryMatch(binaryType, info, IJavaSearchResultCollector.EXACT_MATCH);
	}

	boolean compilationAborted = false;
	if (this.locator.pattern.needsResolve) {
		// resolve
		BinaryTypeBinding binding = null;
		try {
			binding = this.locator.lookupEnvironment.cacheBinaryType(info);
			if (binding == null) { // it was already cached as a result of a previous query
				char[][] compoundName = CharOperation.splitOn('.', binaryType.getFullyQualifiedName().toCharArray());
				ReferenceBinding referenceBinding = this.locator.lookupEnvironment.getCachedType(compoundName);
				if (referenceBinding != null && (referenceBinding instanceof BinaryTypeBinding)) {
					// if the binding could be found and if it comes from a source type,
					binding = (BinaryTypeBinding)referenceBinding;
				}
			}

			// check methods
			if (binding != null) {
				MethodBinding[] methods = binding.methods();
				for (int i = 0; i < methods.length; i++) {
					MethodBinding method = methods[i];
					int level = this.locator.pattern.matchLevel(method);
					switch (level) {
						case SearchPattern.IMPOSSIBLE_MATCH:
						case SearchPattern.INACCURATE_MATCH:
							break;
						default:
							IMethod methodHandle = 
								binaryType.getMethod(
									new String(method.isConstructor() ? binding.compoundName[binding.compoundName.length-1] : method.selector),
									Signature.getParameterTypes(new String(method.signature()).replace('/', '.'))
								);
							this.locator.reportBinaryMatch(
								methodHandle, 
								info, 
								level == SearchPattern.ACCURATE_MATCH ? 
									IJavaSearchResultCollector.EXACT_MATCH : 
									IJavaSearchResultCollector.POTENTIAL_MATCH);
					}
				}
			}
		
			// check fields
			if (binding != null) {
				FieldBinding[] fields = binding.fields();
				for (int i = 0; i < fields.length; i++) {
					FieldBinding field = fields[i];
					int level = this.locator.pattern.matchLevel(field);
					switch (level) {
						case SearchPattern.IMPOSSIBLE_MATCH:
						case SearchPattern.INACCURATE_MATCH:
							break;
						default:
							IField fieldHandle = binaryType.getField(new String(field.name));
							this.locator.reportBinaryMatch(
								fieldHandle, 
								info, 
								level == SearchPattern.ACCURATE_MATCH ? 
									IJavaSearchResultCollector.EXACT_MATCH : 
									IJavaSearchResultCollector.POTENTIAL_MATCH);
					}
				}
			}
		} catch (AbortCompilation e) {
			binding = null;
		}

		// no need to check binary info if resolve was successful
		compilationAborted = binding == null;
		if (!compilationAborted) return;
	}

	// if compilation was aborted it is a problem with the class path: 
	// report as a potential match if binary info matches the pattern
	int accuracy = compilationAborted ? IJavaSearchResultCollector.POTENTIAL_MATCH : IJavaSearchResultCollector.EXACT_MATCH;
	
	// check methods
	IBinaryMethod[] methods = info.getMethods();
	int length = methods == null ? 0 : methods.length;
	for (int i = 0; i < length; i++) {
		IBinaryMethod method = methods[i];
		if (this.locator.pattern.matchesBinary(method, info)) {
			IMethod methodHandle = 
				binaryType.getMethod(
					new String(method.isConstructor() ? info.getName() : method.getSelector()),
					Signature.getParameterTypes(new String(method.getMethodDescriptor()).replace('/', '.'))
				);
			this.locator.reportBinaryMatch(methodHandle, info, accuracy);
		}
	}

	// check fields
	IBinaryField[] fields = info.getFields();
	length = fields == null ? 0 : fields.length;
	for (int i = 0; i < length; i++) {
		IBinaryField field = fields[i];
		if (this.locator.pattern.matchesBinary(field, info)) {
			IField fieldHandle = binaryType.getField(new String(field.getName()));
			this.locator.reportBinaryMatch(fieldHandle, info, accuracy);
		}
	}
}
private void locateMatchesInCompilationUnit(char[] source) throws CoreException {
	if (this.parsedUnit == null) { // case where no binding resolution is needed
		// create match set	
		this.matchSet = new MatchSet(this.locator);
		this.locator.parser.matchSet = this.matchSet;
		
		// diet parse
		char[] qualifiedName = this.getQualifiedName();
		if (qualifiedName == null || (this.parsedUnit = (CompilationUnitDeclaration)this.locator.parsedUnits.get(qualifiedName)) == null) {
			this.parsedUnit = this.locator.dietParse(source);
		}
	}
	if (this.parsedUnit != null) {
		try {
			this.locator.parser.matchSet = this.matchSet;
			this.locator.parser.scanner.setSource(source);
			this.locator.parser.parseBodies(this.parsedUnit);
			// report matches that don't need resolve
			this.matchSet.cuHasBeenResolved = false;
			this.matchSet.reportMatching(parsedUnit);
			
			// resolve if needed
			if (this.matchSet.needsResolve()) {
				if (this.parsedUnit.types != null) {
					if (this.shouldResolve) {
						try {
							if (this.parsedUnit.scope == null) {
								// bindings were not created (case of a FieldReferencePattern that doesn't need resolve, 
								// but we need to resolve because of a SingleNameReference being a potential match)
								this.locator.lookupEnvironment.buildTypeBindings(this.parsedUnit);
								this.locator.lookupEnvironment.completeTypeBindings(this.parsedUnit, true);
							}
							if (this.parsedUnit.scope != null) {
								this.parsedUnit.scope.faultInTypes();
								this.parsedUnit.resolve();
							}
							// report matches that needed resolve
							this.matchSet.cuHasBeenResolved = true;
							this.matchSet.reportMatching(this.parsedUnit);
						} catch (AbortCompilation e) {
							// could not resolve: report innacurate matches
							this.matchSet.cuHasBeenResolved = true;
							this.matchSet.reportMatching(this.parsedUnit);
							if (!(e instanceof AbortCompilationUnit)) {
								// problem with class path
								throw e;
							}
						}
					} else {
						// problem ocured while completing the bindings for the base classes
						// -> report innacurate matches
						this.matchSet.cuHasBeenResolved = true;
						this.matchSet.reportMatching(this.parsedUnit);
					}
				}
			}
		} finally {
			this.locator.parser.matchSet = null;
		}
	}
}
/**
 * Free memory.
 */
public void reset() {
	this.locator.parsedUnits.removeKey(this.getQualifiedName());
	this.parsedUnit = null;
	this.matchSet = null;
}
public String toString() {
	return this.openable.toString();
}
}
