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

import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class DeclarationOfReferencedMethodsPattern extends MethodReferencePattern {
	HashSet knownMethods;
	IJavaElement enclosingElement;
	
public DeclarationOfReferencedMethodsPattern(IJavaElement enclosingElement) {
	super(
		null, 
		PATTERN_MATCH, 
		false, 
		null, 
		null,
		null,
		null,
		null,
		null,
		null);
	this.enclosingElement = enclosingElement;
	this.needsResolve = true;
	this.knownMethods = new HashSet();
}

/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	// need accurate match to be able to open on type ref
	if (accuracy == IJavaSearchResultCollector.POTENTIAL_MATCH) return;
	
	// element that references the method must be included in the enclosing element
	while (element != null && !this.enclosingElement.equals(element)) {
		element = element.getParent();
	}
	if (element == null) return;

	this.reportDeclaration(((MessageSend)reference).binding, locator);
}
private void reportDeclaration(MethodBinding methodBinding, MatchLocator locator) throws CoreException {
	ReferenceBinding declaringClass = methodBinding.declaringClass;
	IType type = locator.lookupType(declaringClass);
	if (type == null) return; // case of a secondary type
	char[] selector = methodBinding.selector;
	TypeBinding[] parameters = methodBinding.parameters;
	int parameterLength = parameters.length;
	String[] parameterTypes = new String[parameterLength];
	for (int i = 0; i  < parameterLength; i++) {
		parameterTypes[i] = Signature.createTypeSignature(parameters[i].sourceName(), false);
	}
	IMethod method = type.getMethod(new String(selector), parameterTypes);
	if (this.knownMethods.contains(method)) return;
	this.knownMethods.add(method);
	IResource resource = type.getUnderlyingResource();
	boolean isBinary = type.isBinary();
	IBinaryType info = null;
	if (isBinary) {
		if (resource == null) {
			resource = type.getJavaProject().getProject();
		}
		info = locator.getBinaryInfo((org.eclipse.jdt.internal.core.ClassFile)type.getClassFile(), resource);
		locator.reportBinaryMatch(resource, method, info, IJavaSearchResultCollector.EXACT_MATCH);
	} else {
		TypeDeclaration typeDecl = ((SourceTypeBinding)declaringClass).scope.referenceContext;
		AbstractMethodDeclaration methodDecl = null;
		AbstractMethodDeclaration[] methodDecls = typeDecl.methods;
		for (int i = 0, length = methodDecls.length; i < length; i++) {
			if (CharOperation.equals(selector, methodDecls[i].selector)) {
				methodDecl = methodDecls[i];
				break;
			}
		} 
		if (methodDecl != null) {
			locator.report(resource, methodDecl.sourceStart, methodDecl.sourceEnd, method, IJavaSearchResultCollector.EXACT_MATCH);
		}
	}
}
}
