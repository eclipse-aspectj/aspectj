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

import java.io.*;
import java.util.*;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.asm.*;
import org.aspectj.asm.internal.*;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.*;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.Member;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemHandler;

public class AsmHierarchyBuilder extends AbstractSyntaxTreeVisitorAdapter {
	
    public static void build(    
        CompilationUnitDeclaration unit,
		IHierarchy structureModel) {
        LangUtil.throwIaxIfNull(unit, "unit");
        new AsmHierarchyBuilder(unit.compilationResult()).internalBuild(unit, structureModel);
    }

	private final Stack stack;
	private final CompilationResult currCompilationResult;
	private AsmElementFormatter formatter = new AsmElementFormatter();
	
    protected AsmHierarchyBuilder(CompilationResult result) {
        LangUtil.throwIaxIfNull(result, "result");
        currCompilationResult = result;
        stack = new Stack();
    }
	   
    /** 
     * Called only by 
     * build(CompilationUnitDeclaration unit, StructureModel structureModel) 
     */
    private void internalBuild(
        CompilationUnitDeclaration unit, 
		IHierarchy structureModel) {
        LangUtil.throwIaxIfNull(structureModel, "structureModel");        
        if (!currCompilationResult.equals(unit.compilationResult())) {
            throw new IllegalArgumentException("invalid unit: " + unit);
        }
        // ---- summary
        // add unit to package (or root if no package),
        // first removing any duplicate (XXX? removes children if 3 classes in same file?)
        // push the node on the stack
        // and traverse
        
        // -- create node to add
        final File file = new File(new String(unit.getFileName()));
        final IProgramElement cuNode;
        {
            // AMC - use the source start and end from the compilation unit decl
            int startLine = getStartLine(unit);
            int endLine = getEndLine(unit);     
            ISourceLocation sourceLocation 
                = new SourceLocation(file, startLine, endLine);
            cuNode = new ProgramElement(
                new String(file.getName()),
                IProgramElement.Kind.FILE_JAVA,
                sourceLocation,
                0,
                "",
                new ArrayList());
        }

		cuNode.addChild(new ProgramElement(
			"import declarations",
			IProgramElement.Kind.IMPORT_REFERENCE,
			null,
			0,
			"",
			new ArrayList()));		

        final IProgramElement addToNode = genAddToNode(unit, structureModel);
        
        // -- remove duplicates before adding (XXX use them instead?)
        for (ListIterator itt = addToNode.getChildren().listIterator(); itt.hasNext(); ) {
            IProgramElement child = (IProgramElement)itt.next();
            ISourceLocation childLoc = child.getSourceLocation();
            if (null == childLoc) {
                // XXX ok, packages have null source locations
                // signal others?
            } else if (childLoc.getSourceFile().equals(file)) {
                itt.remove();
            }
        }
        // -- add and traverse
        addToNode.addChild(cuNode);     
        stack.push(cuNode);
        unit.traverse(this, unit.scope);  
        
        // -- update file map (XXX do this before traversal?)
        try {
            structureModel.addToFileMap(file.getCanonicalPath(), cuNode);
        } catch (IOException e) { 
            System.err.println("IOException " + e.getMessage() 
                + " creating path for " + file );
            // XXX signal IOException when canonicalizing file path
        }
        
	}

	/**
	 * Get/create the node (package or root) to add to.
	 */
	private IProgramElement genAddToNode(
		CompilationUnitDeclaration unit,
		IHierarchy structureModel) {
		final IProgramElement addToNode;
		{
		    ImportReference currentPackage = unit.currentPackage;
		    if (null == currentPackage) {
		        addToNode = structureModel.getRoot();
		    } else {
		        String pkgName;
		        {
		            StringBuffer nameBuffer = new StringBuffer();
		            final char[][] importName = currentPackage.getImportName();
		            final int last = importName.length-1;
		            for (int i = 0; i < importName.length; i++) {
		                nameBuffer.append(new String(importName[i]));
		                if (i < last) {
		                    nameBuffer.append('.');
		                } 
		            }
		            pkgName = nameBuffer.toString();
		        }
		    
		        IProgramElement pkgNode = null;
		        for (Iterator it = structureModel.getRoot().getChildren().iterator(); 
		            it.hasNext(); ) {
		            IProgramElement currNode = (IProgramElement)it.next();
		            if (pkgName.equals(currNode.getName())) {
		                pkgNode = currNode;
		                break; 
		            } 
		        }
		        if (pkgNode == null) {
		            // note packages themselves have no source location
		            pkgNode = new ProgramElement(
		                pkgName, 
		                IProgramElement.Kind.PACKAGE, 
		                new ArrayList()
		            );
		            structureModel.getRoot().addChild(pkgNode);
		        }
		        addToNode = pkgNode;
		    }
		}
		return addToNode;
	}

	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		String name = new String(typeDeclaration.name);
		IProgramElement.Kind kind = IProgramElement.Kind.CLASS;
		if (typeDeclaration instanceof AspectDeclaration) kind = IProgramElement.Kind.ASPECT;
		else if (typeDeclaration.isInterface()) kind = IProgramElement.Kind.INTERFACE;

		IProgramElement peNode = new ProgramElement(
			name,
			kind,
			makeLocation(typeDeclaration),
			typeDeclaration.modifiers,			
			"",
			new ArrayList());
		
		((IProgramElement)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;
	}
	public void endVisit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		stack.pop();
	}
	
	// ??? share impl with visit(TypeDeclaration, ..) ?
	public boolean visit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope) {
		String name = new String(memberTypeDeclaration.name);
		//System.err.println("member type with name: " + name);
		
		IProgramElement.Kind kind = IProgramElement.Kind.CLASS;
		if (memberTypeDeclaration instanceof AspectDeclaration) kind = IProgramElement.Kind.ASPECT;
		else if (memberTypeDeclaration.isInterface()) kind = IProgramElement.Kind.INTERFACE;

		IProgramElement peNode = new ProgramElement(
			name,
			kind,
			makeLocation(memberTypeDeclaration),
			memberTypeDeclaration.modifiers,
			"",
			new ArrayList());
		
		((IProgramElement)stack.peek()).addChild(peNode);
		stack.push(peNode);
		return true;
	}
	public void endVisit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope) {
		stack.pop();
	}
	
	public boolean visit(LocalTypeDeclaration memberTypeDeclaration, BlockScope scope) {
		String name = new String(memberTypeDeclaration.name);
		
		String fullName = "<undefined>";
		if (memberTypeDeclaration.binding != null
			&& memberTypeDeclaration.binding.constantPoolName() != null) {
			fullName = new String(memberTypeDeclaration.binding.constantPoolName());
		}
		 
		int dollar = fullName.indexOf('$');
		fullName = fullName.substring(dollar+1);
//		
//		System.err.println("member type with name: " + name + ", " + 
//				new String(fullName));
		
		IProgramElement.Kind kind = IProgramElement.Kind.CLASS;
		if (memberTypeDeclaration.isInterface()) kind = IProgramElement.Kind.INTERFACE;

		IProgramElement peNode = new ProgramElement(
			fullName,
			kind,
			makeLocation(memberTypeDeclaration),
			memberTypeDeclaration.modifiers,
			"",
			new ArrayList());
		
		//??? we add this to the compilation unit
		findEnclosingClass(stack).addChild(peNode);
		stack.push(peNode);
		return true;
	}
	public void endVisit(LocalTypeDeclaration memberTypeDeclaration, BlockScope scope) {
		stack.pop();
	}
	
	public boolean visit(AnonymousLocalTypeDeclaration memberTypeDeclaration, BlockScope scope) {
		return visit((LocalTypeDeclaration)memberTypeDeclaration, scope);
	}

	public void endVisit(AnonymousLocalTypeDeclaration memberTypeDeclaration, BlockScope scope) {
		stack.pop();
	}
	
	private IProgramElement findEnclosingClass(Stack stack) {
		for (int i = stack.size()-1; i >= 0; i--) {
			IProgramElement pe = (IProgramElement)stack.get(i);
			if (pe.getKind() == IProgramElement.Kind.CLASS) {
				return pe;
			}
			
		}
		return (IProgramElement)stack.peek();
	}	
	
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {				
		IProgramElement peNode = new ProgramElement(
			"",
			IProgramElement.Kind.ERROR,
			makeLocation(methodDeclaration),
			methodDeclaration.modifiers,
			"",
			new ArrayList());  

		formatter.genLabelAndKind(methodDeclaration, peNode);
		genBytecodeInfo(methodDeclaration, peNode);
		peNode.setModifiers(methodDeclaration.modifiers);

		// TODO: add return type test
		if (peNode.getKind().equals(IProgramElement.Kind.METHOD)) {
			if (peNode.toLabelString().equals("main(String[])")
				&& peNode.getModifiers().contains(IProgramElement.Modifiers.STATIC)
				&& peNode.getAccessibility().equals(IProgramElement.Accessibility.PUBLIC)) {
				((IProgramElement)stack.peek()).setRunnable(true);
			}	
		}
		stack.push(peNode);
		return true;
	}

	private void genBytecodeInfo(MethodDeclaration methodDeclaration, IProgramElement peNode) {
		if (methodDeclaration.binding != null) {
			String memberName = "";
			String memberBytecodeSignature = "";
			try {
				Member member = EclipseFactory.makeResolvedMember(methodDeclaration.binding);
				memberName = member.getName();
				memberBytecodeSignature = member.getSignature();
			} catch (NullPointerException npe) {
				memberName = "<undefined>";
			}
		
			peNode.setBytecodeName(memberName);
			peNode.setBytecodeSignature(memberBytecodeSignature);
		}
		((IProgramElement)stack.peek()).addChild(peNode);
	}

	public void endVisit(MethodDeclaration methodDeclaration, ClassScope scope) {
		stack.pop();
	}

	public boolean visit(ImportReference importRef, CompilationUnitScope scope) {
		int dotIndex = importRef.toString().lastIndexOf('.');
		String currPackageImport = "";
		if (dotIndex != -1) {
			currPackageImport = importRef.toString().substring(0, dotIndex);
		}
		if (!((ProgramElement)stack.peek()).getPackageName().equals(currPackageImport)) {
		
			IProgramElement peNode = new ProgramElement(
				new String(importRef.toString()),
				IProgramElement.Kind.IMPORT_REFERENCE,	
				makeLocation(importRef),
				0,
				"", 
				new ArrayList());	
			
			ProgramElement imports = (ProgramElement)((ProgramElement)stack.peek()).getChildren().get(0);
			imports.addChild(0, peNode);
			stack.push(peNode);
		}
		return true;	 
	}
	public void endVisit(ImportReference importRef, CompilationUnitScope scope) {
		int dotIndex = importRef.toString().lastIndexOf('.');
		String currPackageImport = "";
		if (dotIndex != -1) {
			currPackageImport = importRef.toString().substring(0, dotIndex);
		}
		if (!((ProgramElement)stack.peek()).getPackageName().equals(currPackageImport)) {
			stack.pop();
		}
	}

	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {		
		IProgramElement peNode = new ProgramElement(
			new String(fieldDeclaration.name),
			IProgramElement.Kind.FIELD,	
			makeLocation(fieldDeclaration),
			fieldDeclaration.modifiers,
			"",
			new ArrayList());	
		((IProgramElement)stack.peek()).addChild(peNode);
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
//		((IProgramElement)stack.peek()).addChild(0, peNode);
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
		IProgramElement peNode = new ProgramElement(
			new String(constructorDeclaration.selector),
			IProgramElement.Kind.CONSTRUCTOR,	
			makeLocation(constructorDeclaration),
			constructorDeclaration.modifiers,
			"",
			new ArrayList());	
		((IProgramElement)stack.peek()).addChild(peNode);
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
//		((IProgramElement)stack.peek()).addChild(peNode);
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
		
		IProgramElement peNode = new ProgramElement(
			"...",
			IProgramElement.Kind.INITIALIZER,	
			makeLocation(initializer),
			initializer.modifiers,
			"",
			new ArrayList());	
		((IProgramElement)stack.peek()).addChild(peNode);
		stack.push(peNode);
		initializer.block.traverse(this, scope);
		stack.pop();
		return false;	
	}

	// ??? handle non-existant files
	private ISourceLocation makeLocation(AstNode node) {		
		String fileName = "";
		if (currCompilationResult.getFileName() != null) {
			fileName = new String(currCompilationResult.getFileName());
		}
		// AMC - different strategies based on node kind
		int startLine = getStartLine(node);
		int endLine = getEndLine(node);
		ISourceLocation loc = null;
		if ( startLine <= endLine ) {
			// found a valid end line for this node...
			loc = new SourceLocation(new File(fileName), startLine, endLine);			
		} else {
			loc = new SourceLocation(new File(fileName), startLine);
		}
		return loc;
	}
  

	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getStartLine( AstNode n){
//		if (  n instanceof AbstractVariableDeclaration ) return getStartLine( (AbstractVariableDeclaration)n);
//		if (  n instanceof AbstractMethodDeclaration ) return getStartLine( (AbstractMethodDeclaration)n);
//		if (  n instanceof TypeDeclaration ) return getStartLine( (TypeDeclaration)n);
		return ProblemHandler.searchLineNumber(
			currCompilationResult.lineSeparatorPositions,
			n.sourceStart);		
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getEndLine( AstNode n){
		if (  n instanceof AbstractVariableDeclaration ) return getEndLine( (AbstractVariableDeclaration)n);
		if (  n instanceof AbstractMethodDeclaration ) return getEndLine( (AbstractMethodDeclaration)n);
		if (  n instanceof TypeDeclaration ) return getEndLine( (TypeDeclaration)n);	
		return ProblemHandler.searchLineNumber(
			currCompilationResult.lineSeparatorPositions,
			n.sourceEnd);
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getStartLine( AbstractVariableDeclaration avd ) {
		return ProblemHandler.searchLineNumber(
			currCompilationResult.lineSeparatorPositions,
			avd.declarationSourceStart);
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getEndLine( AbstractVariableDeclaration avd ){
		return ProblemHandler.searchLineNumber(
			currCompilationResult.lineSeparatorPositions,
			avd.declarationSourceEnd);		
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getStartLine( AbstractMethodDeclaration amd ){
		return ProblemHandler.searchLineNumber(
			currCompilationResult.lineSeparatorPositions,
			amd.declarationSourceStart);
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getEndLine( AbstractMethodDeclaration amd) {
		return ProblemHandler.searchLineNumber(
			currCompilationResult.lineSeparatorPositions,
			amd.declarationSourceEnd);
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getStartLine( TypeDeclaration td ){
		return ProblemHandler.searchLineNumber(
			currCompilationResult.lineSeparatorPositions,
			td.declarationSourceStart);
	}
	
	// AMC - overloaded set of methods to get start and end lines for
	// various ASTNode types. They have no common ancestor in the
	// hierarchy!!
	private int getEndLine( TypeDeclaration td){
		return ProblemHandler.searchLineNumber(
			currCompilationResult.lineSeparatorPositions,
			td.declarationSourceEnd);
	}


}
