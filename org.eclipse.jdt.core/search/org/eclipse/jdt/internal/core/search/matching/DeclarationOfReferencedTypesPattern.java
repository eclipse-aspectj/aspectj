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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.ProblemBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class DeclarationOfReferencedTypesPattern extends TypeReferencePattern {
	HashSet knownTypes;
	IJavaElement enclosingElement;
public DeclarationOfReferencedTypesPattern(IJavaElement enclosingElement) {
	super(null, null, PATTERN_MATCH, false);
	this.enclosingElement = enclosingElement;
	this.needsResolve = true;
	this.knownTypes = new HashSet();
}
/**
 * @see SearchPattern#matchReportImportRef(ImportReference, Binding, IJavaElement, int, MatchLocator)
 */
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	// need accurate match to be able to open on type ref
	if (accuracy == IJavaSearchResultCollector.POTENTIAL_MATCH) return;
	
	while (binding instanceof ReferenceBinding) {
		ReferenceBinding typeBinding = (ReferenceBinding)binding;
		this.reportDeclaration(typeBinding, 1, locator);
		binding = typeBinding.enclosingType();
	}
}

/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	// need accurate match to be able to open on type ref
	if (accuracy == IJavaSearchResultCollector.POTENTIAL_MATCH) return;
	
	// element that references the type must be included in the enclosing element
	while (element != null && !this.enclosingElement.equals(element)) {
		element = element.getParent();
	}
	if (element == null) return;

	int maxType = -1;
	TypeBinding typeBinding = null;
	if (reference instanceof TypeReference) {
		typeBinding = ((TypeReference)reference).binding;
		maxType = Integer.MAX_VALUE;
	} else if (reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference)reference;
		Binding binding = qNameRef.binding;
		maxType = qNameRef.tokens.length-1;
		switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
			case BindingIds.FIELD : // reading a field
				typeBinding = qNameRef.actualReceiverType;
				int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
				maxType -= otherBindingsCount + 1;
				break;
			case BindingIds.TYPE : //=============only type ==============
				typeBinding = (TypeBinding)binding;
				break;
			case BindingIds.VARIABLE : //============unbound cases===========
			case BindingIds.TYPE | BindingIds.VARIABLE :						
				if (binding instanceof ProblemBinding) {
					ProblemBinding pbBinding = (ProblemBinding) binding;
					typeBinding = pbBinding.searchType; // second chance with recorded type so far
					char[] partialQualifiedName = pbBinding.name;
					maxType = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
					if (typeBinding == null || maxType < 0) return;
				}
				break;
		}
	} else if (reference instanceof SingleNameReference) {
		typeBinding = (TypeBinding)((SingleNameReference)reference).binding;
		maxType = 1;
	}
	
	if (typeBinding == null || typeBinding instanceof BaseTypeBinding) return;
	if (typeBinding instanceof ArrayBinding) {
		typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
	}
	this.reportDeclaration(typeBinding, maxType, locator);
}
private void reportDeclaration(TypeBinding typeBinding, int maxType, MatchLocator locator) throws CoreException {
	IType type = locator.lookupType(typeBinding);
	if (type == null) return; // case of a secondary type
	IResource resource = type.getUnderlyingResource();
	boolean isBinary = type.isBinary();
	IBinaryType info = null;
	if (isBinary) {
		if (resource == null) {
			resource = type.getJavaProject().getProject();
		}
		info = locator.getBinaryInfo((org.eclipse.jdt.internal.core.ClassFile)type.getClassFile(), resource);
	}
	while (maxType >= 0 && type != null) {
		if (!this.knownTypes.contains(type)) {
			if (isBinary) {
				locator.reportBinaryMatch(resource, type, info, IJavaSearchResultCollector.EXACT_MATCH);
			} else {
				TypeDeclaration typeDecl = ((SourceTypeBinding)typeBinding).scope.referenceContext;
				locator.report(resource, typeDecl.sourceStart, typeDecl.sourceEnd, type, IJavaSearchResultCollector.EXACT_MATCH);
			}
			this.knownTypes.add(type);
		}
		if (typeBinding instanceof BinaryTypeBinding) {
			typeBinding = ((BinaryTypeBinding)typeBinding).enclosingType();
		} else {
			typeBinding = ((SourceTypeBinding)typeBinding).enclosingType();
		}
		IJavaElement parent = type.getParent();
		if (parent instanceof IType) {
			type = (IType)parent;
		} else {
			type = null;
		}
		maxType--;
	}
}
}
