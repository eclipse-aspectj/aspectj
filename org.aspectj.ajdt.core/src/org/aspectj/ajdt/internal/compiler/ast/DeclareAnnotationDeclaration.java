/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.weaver.patterns.DeclareAnnotation;

public class DeclareAnnotationDeclaration extends DeclareDeclaration {
   private Annotation annotation;
	
   public DeclareAnnotationDeclaration(CompilationResult result, DeclareAnnotation symbolicDeclare, Annotation annotation)  {
   	  super(result,symbolicDeclare);
   	  this.annotation = annotation;
   	  addAnnotation(annotation);
   	  symbolicDeclare.setAnnotationString(annotation.toString());
   	  symbolicDeclare.setAnnotationMethod(new String(selector));
   }
   
   public Annotation getDeclaredAnnotation() {
   		return annotation;
   }
   	
    /* (non-Javadoc)
	 * @see org.aspectj.ajdt.internal.compiler.ast.DeclareDeclaration#shouldDelegateCodeGeneration()
	 */
	protected boolean shouldDelegateCodeGeneration() {
		return true;  // declare annotation needs a method to be written out.
	}
   
   private void addAnnotation(Annotation ann) {
   	if (this.annotations == null) {
   		this.annotations = new Annotation[1];
   	} else {
   		Annotation[] old = this.annotations;
   		this.annotations = new Annotation[old.length + 1];
   		System.arraycopy(old,0,this.annotations,1,old.length);
   	}
	this.annotations[0] = ann;
   }
   
}
