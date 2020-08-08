/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation 
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.weaver.AjAttribute;

/**
 * Root class for all MethodDeclaration objects created by the parser.
 * Enables us to generate extra attributes in the method_info attribute
 * to support aspectj.
 *
 */
public class AjMethodDeclaration extends MethodDeclaration {

	private List attributes = null;
	
	/**
	 * @param compilationResult
	 */
	public AjMethodDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	// general purpose hook to add an AjAttribute to this method
	// used by @AspectJ visitor to add pointcut attribute to @Advice
	protected void addAttribute(EclipseAttributeAdapter eaa) {
		if (attributes==null) attributes = new ArrayList();
		attributes.add(eaa);
	}
	
	/**
	 * Overridden to add extra AJ stuff, also adds synthetic if boolean is true.
	 */
	protected int generateInfoAttributes(ClassFile classFile,boolean addAjSynthetic) {
		// add extra attributes into list then call 2-arg version of generateInfoAttributes...
		List extras = (attributes==null?new ArrayList():attributes);
		addDeclarationStartLineAttribute(extras,classFile);
		if (addAjSynthetic) {
			extras.add(new EclipseAttributeAdapter(new AjAttribute.AjSynthetic()));
		}
		return classFile.generateMethodInfoAttributes(binding,extras);
	}
	
	@Override
	protected int generateInfoAttributes(ClassFile classFile) {
	    return generateInfoAttributes(classFile,false);
	} 
	
	protected void addDeclarationStartLineAttribute(List extraAttributeList, ClassFile classFile) {
		if ((classFile.codeStream.generateAttributes & ClassFileConstants.ATTR_LINES)==0) return;
		
		int[] separators = compilationResult().lineSeparatorPositions;
		int declarationStartLine = 1;
		for (int separator : separators) {
			if (sourceStart < separator) break;
			declarationStartLine++;
		}
		
		extraAttributeList.add(
				new EclipseAttributeAdapter(new AjAttribute.MethodDeclarationLineNumberAttribute(declarationStartLine, this.sourceStart())));
	}
}
