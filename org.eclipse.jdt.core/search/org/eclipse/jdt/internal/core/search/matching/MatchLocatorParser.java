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
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * A parser that locates ast nodes that match a given search pattern.
 */
public class MatchLocatorParser extends Parser {

	public MatchSet matchSet;
	private LocalDeclarationVisitor localDeclarationVisitor = new LocalDeclarationVisitor();
	
/**
 * An ast visitor that visits local type declarations.
 */
public class LocalDeclarationVisitor extends AbstractSyntaxTreeVisitorAdapter {
	public boolean visit(
			AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
			BlockScope scope) {
		if ((matchSet.matchContainer & SearchPattern.METHOD) != 0) {
			matchSet.checkMatching(anonymousTypeDeclaration);
		}
		return true; 
	}
	public boolean visit(
			ConstructorDeclaration constructorDeclaration,
			ClassScope scope) {
		if ((matchSet.matchContainer & SearchPattern.CLASS) != 0) {
			matchSet.checkMatching(constructorDeclaration);
		}
		return (constructorDeclaration.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type
	}
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		if ((matchSet.matchContainer & SearchPattern.CLASS) != 0) {
			matchSet.checkMatching(fieldDeclaration);
		}
		return (fieldDeclaration.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type;
	}
	public boolean visit(Initializer initializer, MethodScope scope) {
		if ((matchSet.matchContainer & SearchPattern.CLASS) != 0) {
			matchSet.checkMatching(initializer);
		}
		return (initializer.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type
	}
	public boolean visit(
			LocalTypeDeclaration localTypeDeclaration,
			BlockScope scope) {
		if ((matchSet.matchContainer & SearchPattern.METHOD) != 0) {
			matchSet.checkMatching(localTypeDeclaration);
		}
		return true;
	}
	public boolean visit(
			MemberTypeDeclaration memberTypeDeclaration,
			ClassScope scope) {
		if ((matchSet.matchContainer & SearchPattern.CLASS) != 0) {
			matchSet.checkMatching(memberTypeDeclaration);
		}
		return true;
	}
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		if ((matchSet.matchContainer & SearchPattern.CLASS) != 0) {
			matchSet.checkMatching(methodDeclaration);
		}
		return (methodDeclaration.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type
	}
	
}

public MatchLocatorParser(ProblemReporter problemReporter, boolean assertMode) {
	super(problemReporter, true, assertMode);
}
protected void classInstanceCreation(boolean alwaysQualified) {
	super.classInstanceCreation(alwaysQualified);
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.expressionStack[this.expressionPtr]);
	}
}
protected void consumeAssignment() {
	super.consumeAssignment();
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.expressionStack[this.expressionPtr]);
	}
}
	
protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {
	super.consumeExplicitConstructorInvocation(flag, recFlag);
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.astStack[this.astPtr]);
	}
}
protected void consumeFieldAccess(boolean isSuperAccess) {
	super.consumeFieldAccess(isSuperAccess);
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.expressionStack[this.expressionPtr]);
	}
}
protected void consumeMethodInvocationName() {
	super.consumeMethodInvocationName();
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.expressionStack[this.expressionPtr]);
	}
}
protected void consumeMethodInvocationPrimary() {
	super.consumeMethodInvocationPrimary();
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.expressionStack[this.expressionPtr]);
	}
}
protected void consumeMethodInvocationSuper() {
	super.consumeMethodInvocationSuper();
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.expressionStack[this.expressionPtr]);
	}
}
protected void consumeSingleTypeImportDeclarationName() {
	super.consumeSingleTypeImportDeclarationName();
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.astStack[this.astPtr]);
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	super.consumeTypeImportOnDemandDeclarationName();
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.astStack[this.astPtr]);
	}
}
protected void consumeUnaryExpression(int op, boolean post) {
	super.consumeUnaryExpression(op, post);
	if (this.matchSet != null) {
		this.matchSet.checkMatching(this.expressionStack[this.expressionPtr]);
	}
}
protected TypeReference copyDims(TypeReference typeRef, int dim) {
	TypeReference result = super.copyDims(typeRef, dim);
	if (this.matchSet != null) {
		 if (this.matchSet.removePossibleMatch(typeRef) != null) {
			this.matchSet.addPossibleMatch(result);
		 } else if (this.matchSet.removeTrustedMatch(typeRef) != null) {
			this.matchSet.addTrustedMatch(result);
		 }
	}
	return result;
}
protected CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, MatchLocator locator, IFile file, CompilationUnit compilationUnit) {
	MatchSet originalMatchSet = this.matchSet;
	CompilationUnitDeclaration unit = null;
	try {
		this.matchSet = new MatchSet(locator);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		unit = this.dietParse(sourceUnit, compilationResult);
	} finally {
		if (originalMatchSet == null) {
			if (!this.matchSet.isEmpty() 
					&& unit != null && file != null) {
				// potential matches were found while initializing the search pattern
				// from the lookup environment: add the corresponding openable in the list
				MatchingOpenable matchingOpenable = 
					new MatchingOpenable(
						locator,
						file, 
						compilationUnit, 
						unit,
						this.matchSet);
				locator.matchingOpenables.add(matchingOpenable);
			}
			this.matchSet = null;
		} else {
			this.matchSet = originalMatchSet;
		}
	}
	return unit;
}
protected TypeReference getTypeReference(int dim) {
	TypeReference typeRef = super.getTypeReference(dim);
	if (this.matchSet != null) { 
		this.matchSet.checkMatching(typeRef); // NB: Don't check container since type reference can happen anywhere
	}
	return typeRef;
}
protected NameReference getUnspecifiedReference() {
	NameReference nameRef = super.getUnspecifiedReference();
	if (this.matchSet != null) {
		this.matchSet.checkMatching(nameRef); // NB: Don't check container since unspecified reference can happen anywhere
	}
	return nameRef;
}
protected NameReference getUnspecifiedReferenceOptimized() {
	NameReference nameRef = super.getUnspecifiedReferenceOptimized();
	if (this.matchSet != null) {
		this.matchSet.checkMatching(nameRef); // NB: Don't check container since unspecified reference can happen anywhere
	}
	return nameRef;
}
/**
 * Parses the method bodies in the given compilation unit
 */
public void parseBodies(CompilationUnitDeclaration unit) {
	TypeDeclaration[] types = unit.types;
	if (types != null) {
		for (int i = 0; i < types.length; i++) {
			TypeDeclaration type = types[i];
			if ((this.matchSet.matchContainer & SearchPattern.COMPILATION_UNIT) != 0 // type declaration in compilation unit
				|| (this.matchSet.matchContainer & SearchPattern.CLASS) != 0			// or in another type
				|| (this.matchSet.matchContainer & SearchPattern.METHOD) != 0) {		// or in a local class
					
				this.matchSet.checkMatching(type);
			}
			this.parseBodies(type, unit);
		}
	}
}
/**
 * Parses the member bodies in the given type.
 */
private void parseBodies(TypeDeclaration type, CompilationUnitDeclaration unit) {
	// fields
	FieldDeclaration[] fields = type.fields;
	if (fields != null) {
		for (int i = 0; i < fields.length; i++) {
			FieldDeclaration field = fields[i];
			if (field instanceof Initializer) { // initializer block
				this.parse((Initializer)field, type, unit);		
			}
			field.traverse(localDeclarationVisitor, null);
		}
	}
	
	// methods
	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		for (int i = 0; i < methods.length; i++) {
			AbstractMethodDeclaration method = methods[i];
			if (method.sourceStart >= type.bodyStart) { // if not synthetic
				if (method instanceof MethodDeclaration) {
					MethodDeclaration methodDeclaration = (MethodDeclaration)method;
					this.parse(methodDeclaration, unit);
					methodDeclaration.traverse(localDeclarationVisitor, (ClassScope)null);
				} else if (method instanceof ConstructorDeclaration) {
					ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration)method;
					this.parse(constructorDeclaration, unit);
					constructorDeclaration.traverse(localDeclarationVisitor, (ClassScope)null);
				}
			}	
		}
	}

	// member types
	MemberTypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null) {
		for (int i = 0; i < memberTypes.length; i++) {
			MemberTypeDeclaration memberType = memberTypes[i];
			this.parseBodies(memberType, unit);
			memberType.traverse(localDeclarationVisitor, (ClassScope)null);
		}
	}
}
}
