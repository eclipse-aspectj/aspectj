/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer initial implementation 
 *      Andy Clement wired up to back end
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.aspectj.org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.aspectj.weaver.patterns.DeclareAnnotation;

public class DeclareAnnotationDeclaration extends DeclareDeclaration {

	private Annotation annotation;
	private boolean isRemover = false;

	public DeclareAnnotationDeclaration(CompilationResult result, DeclareAnnotation symbolicDeclare, Annotation annotation) {
		super(result, symbolicDeclare);
		this.annotation = annotation;

		addAnnotation(annotation);
		if (symbolicDeclare == null) {
			return; // there is an error that will already be getting reported (e.g. incorrect pattern on decaf/decac)
		}
		this.isRemover = symbolicDeclare.isRemover();
		symbolicDeclare.setAnnotationString(annotation.toString());
		symbolicDeclare.setAnnotationLocation(annotation.sourceStart, annotation.sourceEnd);
	}

	@Override
	public void analyseCode(ClassScope classScope, FlowContext flowContext, FlowInfo flowInfo) {
		super.analyseCode(classScope, flowContext, flowInfo);

		if (isRemover) {
			if (((DeclareAnnotation) declareDecl).getKind() != DeclareAnnotation.AT_FIELD) {
				classScope.problemReporter().signalError(this.sourceStart(), this.sourceEnd,
						"Annotation removal only supported for declare @field (compiler limitation)");
			}
			else if (isRemover && !(annotation instanceof MarkerAnnotation)) {
				classScope.problemReporter().signalError(this.sourceStart(), this.sourceEnd,
						"Annotation removal does not allow values to be specified for the annotation (compiler limitation)");
			}
		}
		long bits = annotation.resolvedType.getAnnotationTagBits();

		if ((bits & TagBits.AnnotationTarget) != 0) {
			// The annotation is stored against a method. For declare @type we need to
			// confirm the annotation targets the right types. Earlier checking will
			// have not found this problem because an annotation for target METHOD will
			// not be reported on as we *do* store it against a method in this case
			DeclareAnnotation.Kind k = ((DeclareAnnotation) declareDecl).getKind();
			if (k.equals(DeclareAnnotation.AT_TYPE)) {
				if ((bits & TagBits.AnnotationForMethod) != 0) {
					classScope.problemReporter().disallowedTargetForAnnotation(annotation);
				}
			}
			if (k.equals(DeclareAnnotation.AT_FIELD)) {
				if ((bits & TagBits.AnnotationForMethod) != 0) {
					classScope.problemReporter().disallowedTargetForAnnotation(annotation);
				}
			}
		}

	}

	public Annotation getDeclaredAnnotation() {
		return annotation;
	}

	protected boolean shouldDelegateCodeGeneration() {
		return true; // declare annotation needs a method to be written out.
	}

	protected boolean shouldBeSynthetic() {
		return false;
	}

	private void addAnnotation(Annotation ann) {
		if (this.annotations == null) {
			this.annotations = new Annotation[1];
		}
		else {
			Annotation[] old = this.annotations;
			this.annotations = new Annotation[old.length + 1];
			System.arraycopy(old, 0, this.annotations, 1, old.length);
		}
		this.annotations[0] = ann;
	}

	public void postParse(TypeDeclaration typeDec) {
		super.postParse(typeDec);
		if (declareDecl != null) {
			((DeclareAnnotation) declareDecl).setAnnotationMethod(new String(selector));
		}
	}

	public boolean isRemover() {
		return isRemover;
	}
}
