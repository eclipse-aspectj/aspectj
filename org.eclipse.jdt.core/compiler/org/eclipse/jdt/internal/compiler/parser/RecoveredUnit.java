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
package org.eclipse.jdt.internal.compiler.parser;

/**
 * Internal field structure for parsing recovery 
 */
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public class RecoveredUnit extends RecoveredElement {

	public CompilationUnitDeclaration unitDeclaration;
	
	public RecoveredImport[] imports;
	public int importCount;
	public RecoveredType[] types;
	public int typeCount;
public RecoveredUnit(CompilationUnitDeclaration unitDeclaration, int bracketBalance, Parser parser){
	super(null, bracketBalance, parser);
	this.unitDeclaration = unitDeclaration;
}
/*
 *	Record a method declaration: should be attached to last type
 */
public RecoveredElement add(AbstractMethodDeclaration methodDeclaration, int bracketBalance) {

	/* attach it to last type - if any */
	if (typeCount > 0){
		RecoveredType type = this.types[typeCount -1];
		type.bodyEnd = 0; // reset position
		type.typeDeclaration.declarationSourceEnd = 0; // reset position
		type.typeDeclaration.bodyEnd = 0;
		return type.add(methodDeclaration, bracketBalance);
	}
	return this; // ignore
}
/*
 *	Record a field declaration: should be attached to last type
 */
public RecoveredElement add(FieldDeclaration fieldDeclaration, int bracketBalance) {

	/* attach it to last type - if any */
	if (typeCount > 0){
		RecoveredType type = this.types[typeCount -1];
		type.bodyEnd = 0; // reset position
		type.typeDeclaration.declarationSourceEnd = 0; // reset position
		type.typeDeclaration.bodyEnd = 0;
		return type.add(fieldDeclaration, bracketBalance);
	}
	return this; // ignore
}
public RecoveredElement add(ImportReference importReference, int bracketBalance) {
	if (imports == null) {
		imports = new RecoveredImport[5];
		importCount = 0;
	} else {
		if (importCount == imports.length) {
			System.arraycopy(
				imports, 
				0, 
				(imports = new RecoveredImport[2 * importCount]), 
				0, 
				importCount); 
		}
	}
	RecoveredImport element = new RecoveredImport(importReference, this, bracketBalance);
	imports[importCount++] = element;

	/* if import not finished, then import becomes current */
	if (importReference.declarationSourceEnd == 0) return element;
	return this;		
}
public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalance) {
	
	if (typeDeclaration instanceof AnonymousLocalTypeDeclaration){
		if (this.typeCount > 0) {
			// add it to the last type
			RecoveredType lastType = this.types[this.typeCount-1];
			lastType.bodyEnd = 0; // reopen type
			lastType.typeDeclaration.bodyEnd = 0; // reopen type
			lastType.typeDeclaration.declarationSourceEnd = 0; // reopen type
			lastType.bracketBalance++; // expect one closing brace
			return lastType.add(typeDeclaration, bracketBalance);
		}
	}
	if (types == null) {
		types = new RecoveredType[5];
		typeCount = 0;
	} else {
		if (typeCount == types.length) {
			System.arraycopy(
				types, 
				0, 
				(types = new RecoveredType[2 * typeCount]), 
				0, 
				typeCount); 
		}
	}
	RecoveredType element = new RecoveredType(typeDeclaration, this, bracketBalance);
	types[typeCount++] = element;

	/* if type not finished, then type becomes current */
	if (typeDeclaration.declarationSourceEnd == 0) return element;
	return this;	
}
/* 
 * Answer the associated parsed structure
 */
public AstNode parseTree(){
	return unitDeclaration;
}
/*
 * Answer the very source end of the corresponding parse node
 */
public int sourceEnd(){
	return this.unitDeclaration.sourceEnd;
}
public String toString(int tab) {
	StringBuffer result = new StringBuffer(tabString(tab));
	result.append("Recovered unit: [\n"); //$NON-NLS-1$
	result.append(unitDeclaration.toString(tab + 1));
	result.append(tabString(tab + 1));
	result.append("]"); //$NON-NLS-1$
	if (this.imports != null) {
		for (int i = 0; i < this.importCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.imports[i].toString(tab + 1));
		}
	}
	if (this.types != null) {
		for (int i = 0; i < this.typeCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.types[i].toString(tab + 1));
		}
	}
	return result.toString();
}
public CompilationUnitDeclaration updatedCompilationUnitDeclaration(){

	/* update imports */
	if (importCount > 0){
		ImportReference[] importRefences = new ImportReference[importCount];
		for (int i = 0; i < importCount; i++){
			importRefences[i] = imports[i].updatedImportReference();
		}
		unitDeclaration.imports = importRefences;
	}
	/* update types */
	if (typeCount > 0){
		int existingCount = unitDeclaration.types == null ? 0 : unitDeclaration.types.length;
		TypeDeclaration[] typeDeclarations = new TypeDeclaration[existingCount + typeCount];
		if (existingCount > 0){
			System.arraycopy(unitDeclaration.types, 0, typeDeclarations, 0, existingCount);
		}
		// may need to update the declarationSourceEnd of the last type
		if (types[typeCount - 1].typeDeclaration.declarationSourceEnd == 0){
			types[typeCount - 1].typeDeclaration.declarationSourceEnd = unitDeclaration.sourceEnd;
			types[typeCount - 1].typeDeclaration.bodyEnd = unitDeclaration.sourceEnd;
		}
		int actualCount = existingCount;
		for (int i = 0; i < typeCount; i++){
			TypeDeclaration typeDecl = types[i].updatedTypeDeclaration();
			// filter out local types (12454)
			if (!(typeDecl instanceof LocalTypeDeclaration)){
				typeDeclarations[actualCount++] = typeDecl;
			}
		}
		if (actualCount != typeCount){
			System.arraycopy(
				typeDeclarations, 
				0, 
				typeDeclarations = new TypeDeclaration[existingCount+actualCount], 
				0, 
				existingCount+actualCount);
		}
		unitDeclaration.types = typeDeclarations;
	}
	return unitDeclaration;
}
public void updateParseTree(){
	this.updatedCompilationUnitDeclaration();
}
/*
 * Update the sourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int sourceEnd){
	if (this.unitDeclaration.sourceEnd == 0)
		this.unitDeclaration.sourceEnd = sourceEnd;
}
}
