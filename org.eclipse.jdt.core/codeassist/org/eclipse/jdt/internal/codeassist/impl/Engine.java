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
package org.eclipse.jdt.internal.codeassist.impl;
import java.util.Map;

import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.*;

public abstract class Engine implements ITypeRequestor {
	public LookupEnvironment lookupEnvironment;
	
	protected CompilationUnitScope unitScope;
	protected ISearchableNameEnvironment nameEnvironment;

	public AssistOptions options;
	public CompilerOptions compilerOptions; 
	
	public Engine(Map settings){
		this.options = new AssistOptions(settings);
		this.compilerOptions = new CompilerOptions(settings);
	}
	
	/**
	 * Add an additional binary type
	 */
	public void accept(IBinaryType binaryType, PackageBinding packageBinding) {
		lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
	}
	/**
	 * Add an additional compilation unit.
	 */
	public void accept(ICompilationUnit sourceUnit) {
		CompilationResult result = new CompilationResult(sourceUnit, 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit =
			this.getParser().dietParse(sourceUnit, result);

		lookupEnvironment.buildTypeBindings(parsedUnit);
		lookupEnvironment.completeTypeBindings(parsedUnit, true);
	}
	/**
	 * Add additional source types (the first one is the requested type, the rest is formed by the
	 * secondary types defined in the same compilation unit).
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding) {
		CompilationResult result =
			new CompilationResult(sourceTypes[0].getFileName(), 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,
				true,
				true,
				lookupEnvironment.problemReporter,
				result);
		if (unit != null) {
			lookupEnvironment.buildTypeBindings(unit);
			lookupEnvironment.completeTypeBindings(unit, true);
		}
	}
	public abstract AssistParser getParser();
	
	protected boolean mustQualifyType(
		char[] packageName,
		char[] typeName) {

		// If there are no types defined into the current CU yet.
		if (unitScope == null)
			return true;
			
		char[][] compoundPackageName = CharOperation.splitOn('.', packageName);
		char[] readableTypeName = CharOperation.concat(packageName, typeName, '.');

		if (CharOperation.equals(unitScope.fPackage.compoundName, compoundPackageName))
			return false;

		ImportBinding[] imports = unitScope.imports;
		if (imports != null){
			for (int i = 0, length = imports.length; i < length; i++) {
				if (imports[i].onDemand) {
					if (CharOperation.equals(imports[i].compoundName, compoundPackageName)) {
						for (int j = 0; j < imports.length; j++) {
							if(i != j){
								if(imports[j].onDemand) {
									if(nameEnvironment.findType(typeName, imports[j].compoundName) != null){
										return true;
									}
								} else {
									if(CharOperation.equals(CharOperation.lastSegment(imports[j].readableName(), '.'), typeName)) {
										return true;	
									}
								}
							}
						}
						return false; // how do you match p1.p2.A.* ?
					}
	
				} else
	
					if (CharOperation.equals(imports[i].readableName(), readableTypeName)) {
						return false;
					}
			}
		}
		return true;
	}

	protected void parseMethod(CompilationUnitDeclaration unit, int position) {
		for (int i = unit.types.length; --i >= 0;) {
			TypeDeclaration type = unit.types[i];
			if (type.declarationSourceStart < position
				&& type.declarationSourceEnd >= position) {
				getParser().scanner.setSource(
					unit.compilationResult.compilationUnit.getContents());
				parseMethod(type, unit, position);
				return;
			}
		}
	}
	private void parseMethod(
		TypeDeclaration type,
		CompilationUnitDeclaration unit,
		int position) {
		//members
		TypeDeclaration[] memberTypes = type.memberTypes;
		if (memberTypes != null) {
			for (int i = memberTypes.length; --i >= 0;) {
				TypeDeclaration memberType = memberTypes[i];
				if (memberType.bodyStart > position)
					continue;
				if (memberType.declarationSourceEnd >= position) {
					parseMethod(memberType, unit, position);
					return;
				}
			}
		}
		//methods
		AbstractMethodDeclaration[] methods = type.methods;
		if (methods != null) {
			for (int i = methods.length; --i >= 0;) {
				AbstractMethodDeclaration method = methods[i];
				if (method.bodyStart > position)
					continue;
				if (method.declarationSourceEnd >= position) {
					getParser().parseBlockStatements(method, unit);
					return;
				}
			}
		}
		//initializers
		FieldDeclaration[] fields = type.fields;
		if (fields != null) {
			for (int i = fields.length; --i >= 0;) {
				if (!(fields[i] instanceof Initializer))
					continue;
				Initializer initializer = (Initializer) fields[i];
				if (initializer.bodyStart > position)
					continue;
				if (initializer.declarationSourceEnd >= position) {
					getParser().parseBlockStatements(initializer, type, unit);
					return;
				}
			}
		}
	}
	protected void reset() {
		lookupEnvironment.reset();
	}
}