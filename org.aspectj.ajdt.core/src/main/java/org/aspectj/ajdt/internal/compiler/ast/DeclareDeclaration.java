/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.ast;

//import java.util.List;

import java.util.Collection;
import java.util.Iterator;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseScope;
import org.aspectj.org.eclipse.jdt.core.Flags;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.FormalBinding;

public class DeclareDeclaration extends AjMethodDeclaration {

	public Declare declareDecl;

	/**
	 * Constructor for IntraTypeDeclaration.
	 */
	public DeclareDeclaration(CompilationResult result, Declare symbolicDeclare) {
		super(result);

		this.declareDecl = symbolicDeclare;
		if (declareDecl != null) {
			// AMC added init of declarationSourceXXX fields which are used
			// in AsmBuilder for processing of MethodDeclaration locations.
			declarationSourceStart = sourceStart = declareDecl.getStart();
			declarationSourceEnd = sourceEnd = declareDecl.getEnd();
		}
		// ??? we might need to set parameters to be empty
		this.returnType = TypeReference.baseTypeReference(T_void, 0, null);
	}

	public void addAtAspectJAnnotations() {
		Annotation annotation = null;
		if (declareDecl instanceof DeclareAnnotation) {
			DeclareAnnotation da = (DeclareAnnotation) declareDecl;
			String patternString = da.getPatternAsString();
			String annString = da.getAnnotationString();
			String kind = da.getKind().toString();
			annotation = AtAspectJAnnotationFactory.createDeclareAnnAnnotation(patternString, annString, kind,
					declarationSourceStart);
		} else if (declareDecl instanceof DeclareErrorOrWarning) {
			DeclareErrorOrWarning dd = (DeclareErrorOrWarning) declareDecl;
			annotation = AtAspectJAnnotationFactory.createDeclareErrorOrWarningAnnotation(dd.getPointcut().toString(),
					dd.getMessage(), dd.isError(), declarationSourceStart);
		} else if (declareDecl instanceof DeclareParents) {
			DeclareParents dp = (DeclareParents) declareDecl;
			String childPattern = dp.getChild().toString();
			Collection parentPatterns = dp.getParents().getExactTypes();
			StringBuffer parents = new StringBuffer();
			for (Iterator iter = parentPatterns.iterator(); iter.hasNext();) {
				UnresolvedType urt = ((UnresolvedType) iter.next());
				parents.append(urt.getName());
				if (iter.hasNext()) {
					parents.append(", ");
				}
			}
			annotation = AtAspectJAnnotationFactory.createDeclareParentsAnnotation(childPattern, parents.toString(),
					dp.isExtends(), declarationSourceStart);
		} else if (declareDecl instanceof DeclarePrecedence) {
			DeclarePrecedence dp = (DeclarePrecedence) declareDecl;
			String precedenceList = dp.getPatterns().toString();
			annotation = AtAspectJAnnotationFactory.createDeclarePrecedenceAnnotation(precedenceList, declarationSourceStart);
		} else if (declareDecl instanceof DeclareSoft) {
			DeclareSoft ds = (DeclareSoft) declareDecl;
			annotation = AtAspectJAnnotationFactory.createDeclareSoftAnnotation(ds.getPointcut().toString(), ds.getException()
					.getExactType().getName(), declarationSourceStart);
		}
		if (annotation != null) {
			AtAspectJAnnotationFactory.addAnnotation(this, annotation, this.scope);
		}
	}

	/**
	 * A declare declaration exists in a classfile only as an attibute on the class. Unlike advice and inter-type declarations, it
	 * has no corresponding method. **AMC** changed the above policy in the case of declare annotation, which uses a corresponding
	 * method as the anchor for the declared annotation
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (shouldBeSynthetic()) {
			this.binding.modifiers |= Flags.AccSynthetic;
		}
		classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.DeclareAttribute(declareDecl)));
		if (shouldDelegateCodeGeneration()) {
			super.generateCode(classScope, classFile);
		}
		return;
	}

	protected boolean shouldDelegateCodeGeneration() {
		return true;
	}

	protected boolean shouldBeSynthetic() {
		return true;
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		// do nothing
	}

	public void resolveStatements(ClassScope upperScope) {
		// do nothing
	}

	// public boolean finishResolveTypes(SourceTypeBinding sourceTypeBinding) {
	// // there's nothing for our super to resolve usefully
	// //if (!super.finishResolveTypes(sourceTypeBinding)) return false;
	// // if (declare == null) return true;
	// //
	// // EclipseScope scope = new EclipseScope(new FormalBinding[0], this.scope);
	// //
	// // declare.resolve(scope);
	// // return true;
	// }

	public Declare build(ClassScope classScope) {
		if (declareDecl == null) {
			return null;
		}

		EclipseScope scope = new EclipseScope(new FormalBinding[0], classScope);

		declareDecl.resolve(scope);
		return declareDecl;
	}

	public StringBuffer print(int tab, StringBuffer output) {
		printIndent(tab, output);
		if (declareDecl == null) {
			output.append("<declare>");
		} else {
			output.append(declareDecl.toString());
		}
		return output;
	}

	/**
	 * We need the ajc$declare method that is created to represent this declare to be marked as synthetic
	 */
	protected int generateInfoAttributes(ClassFile classFile) {
		return super.generateInfoAttributes(classFile, true);
	}

	public void postParse(TypeDeclaration typeDec) {
		super.postParse(typeDec);
		int declareSequenceNumberInType = ((AspectDeclaration) typeDec).declareCounter++;
		// FIXME asc the name should perhaps include the hashcode of the pattern (type/sig) for binary compatibility reasons!
		StringBuffer sb = new StringBuffer();
		sb.append("ajc$declare");
		// Declares can choose to provide a piece of the name - to enable
		// them to be easily distinguised at weave time (e.g. see declare annotation)
		if (declareDecl != null) {
			String suffix = declareDecl.getNameSuffix();
			if (suffix.length() != 0) {
				sb.append("_");
				sb.append(suffix);
			}
		}
		sb.append("_");
		sb.append(declareSequenceNumberInType);
		this.selector = sb.toString().toCharArray();
	}
}
