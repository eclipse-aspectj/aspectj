/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

//import java.util.List;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.FormalBinding;

public class DeclareDeclaration extends AjMethodDeclaration {

	public Declare declareDecl;

	/**
	 * Constructor for IntraTypeDeclaration.
	 */
	static int counter = 0; //XXX evil
	public DeclareDeclaration(CompilationResult result, Declare symbolicDeclare) {
		super(result);
		this.declareDecl = symbolicDeclare;
		if (declareDecl != null) {
			// AMC added init of declarationSourceXXX fields which are used
			// in AsmBuilder for processing of MethodDeclaration locations. 
			declarationSourceStart = sourceStart = declareDecl.getStart();
			declarationSourceEnd = sourceEnd = declareDecl.getEnd();
		}
		//??? we might need to set parameters to be empty
		this.returnType = TypeReference.baseTypeReference(T_void, 0);
		
        StringBuffer sb = new StringBuffer();
        sb.append("ajc$declare");
        // Declares can choose to provide a piece of the name - to enable
        // them to be easily distinguised at weave time (e.g. see declare annotation)
        if (symbolicDeclare!=null) {
          String suffix = symbolicDeclare.getNameSuffix();
          if (suffix.length()!=0) {
        	sb.append("_");
        	sb.append(suffix);
          }
        }
        sb.append("_");
        sb.append(counter++);
        this.selector = sb.toString().toCharArray();
	}


	/**
	 * A declare declaration exists in a classfile only as an attibute on the
	 * class.  Unlike advice and inter-type declarations, it has no corresponding
	 * method.
	 * **AMC** changed the above policy in the case of declare annotation, which uses a 
	 * corresponding method as the anchor for the declared annotation
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.DeclareAttribute(declareDecl)));
		if (shouldDelegateCodeGeneration()) {
			super.generateCode(classScope,classFile);
		}
		return;
	}
	
	protected boolean shouldDelegateCodeGeneration() {
		return false;
	}

	public void parseStatements(
		Parser parser,
		CompilationUnitDeclaration unit) {
			// do nothing
	}
	
	public void resolveStatements(ClassScope upperScope) {
		// do nothing 
	}
	
//	public boolean finishResolveTypes(SourceTypeBinding sourceTypeBinding) {
//		// there's nothing for our super to resolve usefully
//		//if (!super.finishResolveTypes(sourceTypeBinding)) return false;
////		if (declare == null) return true;
////        
////        EclipseScope scope = new EclipseScope(new FormalBinding[0], this.scope);
////
////        declare.resolve(scope);
////        return true;
//	}

	
	public Declare build(ClassScope classScope) {
		if (declareDecl == null) return null;
        
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
	 * We need the ajc$declare method that is created to represent this declare to
	 * be marked as synthetic
	 */
	protected int generateInfoAttributes(ClassFile classFile) {
		return super.generateInfoAttributes(classFile,true);
	}

}
