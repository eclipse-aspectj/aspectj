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


package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.text.CollationElementIterator;
import java.util.*;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.ast.*;
import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseShadow;
import org.aspectj.asm.*;
import org.aspectj.asm.StructureModel;
import org.aspectj.bridge.*;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.Member;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.problem.ProblemHandler;

public class AsmBuilder extends AbstractSyntaxTreeVisitorAdapter {
	
	private Stack stack  = new Stack();
	private CompilationResult currCompilationResult = null;
	
	public static void build(
		CompilationUnitDeclaration unit,
		StructureModel structureModel) {
		new AsmBuilder().internalBuild(unit, structureModel);
	}
	
	private void internalBuild(CompilationUnitDeclaration unit, StructureModel structureModel) {
		currCompilationResult = unit.compilationResult();
		File file = new File(new String(unit.getFileName()));
		ISourceLocation sourceLocation = new SourceLocation(file, 1);		

		ProgramElementNode cuNode = new ProgramElementNode(
			new String(file.getName()),
			ProgramElementNode.Kind.FILE_JAVA,
			sourceLocation,
			0,
			"",
			new ArrayList());

		ImportReference currentPackage = unit.currentPackage;
		if (currentPackage != null) {
			StringBuffer nameBuffer = new StringBuffer();
			for (int i = 0; i < currentPackage.getImportName().length; i++) {
				nameBuffer.append(new String(currentPackage.getImportName()[i]));
				if (i < currentPackage.getImportName().length-1) nameBuffer.append('.');
			}
			String pkgName = nameBuffer.toString();
			
			boolean found = false;
			ProgramElementNode pkgNode = null;
			for (Iterator it = StructureModelManager.INSTANCE.getStructureModel().getRoot().getChildren().iterator(); it.hasNext(); ) {
				ProgramElementNode currNode = (ProgramElementNode)it.next();
				if (currNode.getName().equals(pkgName)) pkgNode = currNode;
			}
			if (pkgNode == null) {
				pkgNode = new ProgramElementNode(
					pkgName, 
					ProgramElementNode.Kind.PACKAGE, 
					new ArrayList());
				StructureModelManager.INSTANCE.getStructureModel().getRoot().addChild(pkgNode);
			}	
			pkgNode.addChild(cuNode);
		} else {
			StructureModelManager.INSTANCE.getStructureModel().getRoot().addChild(cuNode);
		}
		
		stack.push(cuNode);
		unit.traverse(this, unit.scope);  
		
        StructureModelManager.INSTANCE.getStructureModel().getFileMap().put(
        	file.getAbsolutePath().replace('\\', '/'),
        	cuNode
        );
//		if (currImports != null) peNode.addChild(0, currImports);
//		currImports = null;
	}

	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		String name = new String(typeDeclaration.name);
		ProgramElementNode.Kind kind = ProgramElementNode.Kind.CLASS;
		if (typeDeclaration instanceof AspectDeclaration) kind = ProgramElementNode.Kind.ASPECT;
		else if (typeDeclaration.isInterface()) kind = ProgramElementNode.Kind.INTERFACE;

		ProgramElementNode peNode = new ProgramElementNode(
			name,
			kind,
			makeLocation(typeDeclaration),
			typeDeclaration.modifiers,
			"",
			new ArrayList());
		
		((StructureNode)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;
	}
	public void endVisit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		stack.pop();
	}
	
	// ??? share impl with visit(TypeDeclaration, ..) ?
	public boolean visit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope) {
		String name = new String(memberTypeDeclaration.name);
		ProgramElementNode.Kind kind = ProgramElementNode.Kind.CLASS;
		if (memberTypeDeclaration instanceof AspectDeclaration) kind = ProgramElementNode.Kind.ASPECT;
		else if (memberTypeDeclaration.isInterface()) kind = ProgramElementNode.Kind.INTERFACE;

		ProgramElementNode peNode = new ProgramElementNode(
			name,
			kind,
			makeLocation(memberTypeDeclaration),
			memberTypeDeclaration.modifiers,
			"",
			new ArrayList());
		
		((StructureNode)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;
	}
	public void endVisit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope) {
		stack.pop();
	}
	
	// !!! improve name and type generation
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		ProgramElementNode.Kind kind = ProgramElementNode.Kind.METHOD;
		String label = new String(methodDeclaration.selector);
		
		if (methodDeclaration instanceof AdviceDeclaration) { 
			kind = ProgramElementNode.Kind.ADVICE;
			label = translateAdviceName(label);
		} else if (methodDeclaration instanceof PointcutDeclaration) { 
			kind = ProgramElementNode.Kind.POINTCUT;
		} else if (methodDeclaration instanceof DeclareDeclaration) { 
			DeclareDeclaration declare = (DeclareDeclaration)methodDeclaration;
			label = translateDeclareName(declare.toString());
			if (label.indexOf("warning") != -1) kind = ProgramElementNode.Kind.DECLARE_WARNING;
			if (label.indexOf("error") != -1) kind = ProgramElementNode.Kind.DECLARE_ERROR;
		} else if (methodDeclaration instanceof InterTypeDeclaration) {
			kind = ProgramElementNode.Kind.INTRODUCTION;
			label = translateInterTypeDecName(new String(((InterTypeDeclaration)methodDeclaration).selector));
		} 
		
		ProgramElementNode peNode = new ProgramElementNode(
			label,
			kind,
			makeLocation(methodDeclaration),
			methodDeclaration.modifiers,
			"",
			new ArrayList());
			
		Member member = EclipseWorld.makeResolvedMember(methodDeclaration.binding);
		peNode.setBytecodeName(member.getName());
		peNode.setBytecodeSignature(member.getSignature());
		((StructureNode)stack.peek()).addChild(peNode);
		stack.push(peNode);
		
		return true;
	}


	public void endVisit(MethodDeclaration methodDeclaration, ClassScope scope) {
		stack.pop();
	}

	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {		
		ProgramElementNode peNode = new ProgramElementNode(
			new String(fieldDeclaration.name),
			ProgramElementNode.Kind.FIELD,	
			makeLocation(fieldDeclaration),
			fieldDeclaration.modifiers,
			"",
			new ArrayList());	
		((StructureNode)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;		
	}
	public void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		stack.pop();
	}


//	public boolean visit(ImportReference importRef, CompilationUnitScope scope) {
//		ProgramElementNode peNode = new ProgramElementNode(
//			new String(importRef.toString()),
//			ProgramElementNode.Kind.,	
//			makeLocation(importRef),
//			0,
//			"",
//			new ArrayList());	
//		((StructureNode)stack.peek()).addChild(0, peNode);
//		stack.push(peNode);
//		return true;	
//	}
//	public void endVisit(ImportReference importRef,CompilationUnitScope scope) {
//		stack.pop();		
//	}

	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		if (constructorDeclaration.isDefaultConstructor) {
			stack.push(null); // a little wierd but does the job
			return true;	
		}
		ProgramElementNode peNode = new ProgramElementNode(
			new String(constructorDeclaration.selector),
			ProgramElementNode.Kind.CONSTRUCTOR,	
			makeLocation(constructorDeclaration),
			constructorDeclaration.modifiers,
			"",
			new ArrayList());	
		((StructureNode)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;	
	}
	public void endVisit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		stack.pop();
	}

//	public boolean visit(Clinit clinit, ClassScope scope) {
//		ProgramElementNode peNode = new ProgramElementNode(
//			"<clinit>",
//			ProgramElementNode.Kind.INITIALIZER,	
//			makeLocation(clinit),
//			clinit.modifiers,
//			"",
//			new ArrayList());	
//		((StructureNode)stack.peek()).addChild(peNode);
//		stack.push(peNode);  
//		return false;	
//	}
//	public void endVisit(Clinit clinit, ClassScope scope) {
//		stack.pop();
//	}

	/** This method works-around an odd traverse implementation on Initializer
	 */
	private Initializer inInitializer = null;
	public boolean visit(Initializer initializer, MethodScope scope) {
		if (initializer == inInitializer) return false;
		inInitializer = initializer;
		
		ProgramElementNode peNode = new ProgramElementNode(
			"...",
			ProgramElementNode.Kind.INITIALIZER,	
			makeLocation(initializer),
			initializer.modifiers,
			"",
			new ArrayList());	
		((StructureNode)stack.peek()).addChild(peNode);
		stack.push(peNode);
		initializer.block.traverse(this, scope);
		stack.pop();
		return false;	
	}

	// ??? handle non-existant files
	private ISourceLocation makeLocation(AstNode node) {
		String fileName = new String(currCompilationResult.getFileName());
		int line = ProblemHandler.searchLineNumber(
				currCompilationResult.lineSeparatorPositions, 
				node.sourceStart);
		return new SourceLocation(new File(fileName), line);	
	}

	// !!! move or replace
	private String translateAdviceName(String label) {
		if (label.indexOf("before") != -1) return "before";
		if (label.indexOf("returning") != -1) return "after returning";
		if (label.indexOf("after") != -1) return "after";
		if (label.indexOf("around") != -1) return "around";
		else return "<advice>";
	}
	
	// !!! move or replace
	private String translateDeclareName(String name) {
		int colonIndex = name.indexOf(":");
		if (colonIndex != -1) {
			return name.substring(0, colonIndex);
		} else { 
			return name;
		}
	}

	// !!! move or replace
	private String translateInterTypeDecName(String name) {
		int index = name.lastIndexOf('$');
		if (index != -1) {
			return name.substring(index+1);
		} else { 
			return name;
		}
	}

}
