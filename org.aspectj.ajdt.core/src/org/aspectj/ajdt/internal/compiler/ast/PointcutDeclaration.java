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

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.core.builder.EclipseSourceContext;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ResolvedPointcutDefinition;
//import org.aspectj.weaver.ResolvedTypeX;
//import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * pointcut [declaredModifiers] [declaredName]([arguments]): [pointcutDesignator];
 * 
 * <p>No method will actually be generated for this node but an attribute
 * will be added to the enclosing class.</p>
 * 
 * @author Jim Hugunin
 */
public class PointcutDeclaration extends AjMethodDeclaration {
	public static final char[] mangledPrefix = "ajc$pointcut$".toCharArray();
	
	public PointcutDesignator pointcutDesignator;
	private int declaredModifiers;
	private String declaredName;
    
    private ResolvedPointcutDefinition resolvedPointcutDeclaration = null;

	public PointcutDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
		this.returnType = TypeReference.baseTypeReference(T_void, 0);
	}

	private Pointcut getPointcut() {
		if (pointcutDesignator == null) {
			return Pointcut.makeMatchesNothing(Pointcut.RESOLVED);
		} else {
			return pointcutDesignator.getPointcut();
		}
	}
	
	
	public void parseStatements(
		Parser parser,
		CompilationUnitDeclaration unit) {
		// do nothing
	}

	public void postParse(TypeDeclaration typeDec) {
		if (arguments == null) arguments = new Argument[0];
		this.declaredModifiers = modifiers;
		this.declaredName = new String(selector);
		selector = CharOperation.concat(mangledPrefix, '$', selector, '$',
				Integer.toHexString(sourceStart).toCharArray());
				
		if (Modifier.isAbstract(this.declaredModifiers) && 
			!(typeDec instanceof AspectDeclaration))
		{
			typeDec.scope.problemReporter().signalError(sourceStart, sourceEnd, 
				"The abstract pointcut " + new String(declaredName) +
				" can only be defined in an aspect");
			ignoreFurtherInvestigation = true;
			return;
		}
		
		if (pointcutDesignator != null) {
			pointcutDesignator.postParse(typeDec, this);
		}
	}
    
    public void resolve(ClassScope upperScope) {
        // this method should do nothing, use the entry point below...
    }

    public void resolvePointcut(ClassScope upperScope) {
        super.resolve(upperScope);
    }


	public void resolveStatements() {
		if (isAbstract()) {
			this.modifiers |= AccSemicolonBody;
		}
		
		
		if (binding == null || ignoreFurtherInvestigation) return;

		if (Modifier.isAbstract(this.declaredModifiers)&& (pointcutDesignator != null)) {
			scope.problemReporter().signalError(sourceStart, sourceEnd, "abstract pointcut can't have body");
			ignoreFurtherInvestigation = true;
			return;
		}
		
		if (pointcutDesignator != null) {
			pointcutDesignator.finishResolveTypes(this, this.binding, arguments.length, 
					scope.enclosingSourceType());
		}
        
        //System.out.println("resolved: " + getPointcut() + ", " + getPointcut().state);
		makeResolvedPointcutDefinition();
        resolvedPointcutDeclaration.setPointcut(getPointcut());
		super.resolveStatements();
	}
	

	public ResolvedPointcutDefinition makeResolvedPointcutDefinition() {
        if (resolvedPointcutDeclaration != null) return resolvedPointcutDeclaration;
		//System.out.println("pc: " + getPointcut() + ", " + getPointcut().state);
		resolvedPointcutDeclaration = new ResolvedPointcutDefinition(
            EclipseFactory.fromBinding(this.binding.declaringClass), 
            declaredModifiers, 
            declaredName,
			EclipseFactory.fromBindings(this.binding.parameters),
			getPointcut()); //??? might want to use null 
			
		resolvedPointcutDeclaration.setPosition(sourceStart, sourceEnd);
		resolvedPointcutDeclaration.setSourceContext(new EclipseSourceContext(compilationResult));
		return resolvedPointcutDeclaration;
	}


	public AjAttribute makeAttribute() {
		return new AjAttribute.PointcutDeclarationAttribute(makeResolvedPointcutDefinition());
	}
	
	/**
	 * A pointcut declaration exists in a classfile only as an attibute on the
	 * class.  Unlike advice and inter-type declarations, it has no corresponding
	 * method.
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (ignoreFurtherInvestigation) return ;
		classFile.extraAttributes.add(new EclipseAttributeAdapter(makeAttribute()));
		return;
	}
	
	public StringBuffer printReturnType(int indent, StringBuffer output) {
		return output.append("pointcut");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration#printBody(int, java.lang.StringBuffer)
	 */
	public StringBuffer printBody(int indent, StringBuffer output) {
		output.append(": ");
		output.append(getPointcut());
		output.append(";");
		return output;
	}

}
