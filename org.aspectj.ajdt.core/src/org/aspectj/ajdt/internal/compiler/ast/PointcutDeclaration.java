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

import java.io.*;
import java.io.IOException;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseWorld;
import org.aspectj.ajdt.internal.core.builder.EclipseSourceContext;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * pointcut [declaredModifiers] [declaredName]([arguments]): [pointcutDesignator];
 * 
 * <p>No method will actually be generated for this node but an attribute
 * will be added to the enclosing class.</p>
 * 
 * @author Jim Hugunin
 */
public class PointcutDeclaration extends MethodDeclaration {
	public static final char[] mangledPrefix = "ajc$pointcut$".toCharArray();
	
	public PointcutDesignator pointcutDesignator;
	private int declaredModifiers;
	private String declaredName;

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
		if (pointcutDesignator == null) return; //XXX
		pointcutDesignator.postParse(typeDec, this);
	}

	public void resolveStatements(ClassScope upperScope) {
		if (isAbstract()) this.modifiers |= AccSemicolonBody;
		
		if (binding == null || ignoreFurtherInvestigation) return;
		
		if (pointcutDesignator != null) {
			pointcutDesignator.finishResolveTypes(this, this.binding, arguments.length, 
					upperScope.referenceContext.binding);
		}
		
		super.resolveStatements(upperScope);
	}
	

	public ResolvedPointcutDefinition makeResolvedPointcutDefinition() {
		//System.out.println("pc: " + getPointcut());
		ResolvedPointcutDefinition ret = new ResolvedPointcutDefinition(
            EclipseWorld.fromBinding(this.binding.declaringClass), 
            declaredModifiers, 
            declaredName,
			EclipseWorld.fromBindings(this.binding.parameters),
			getPointcut());
			
		ret.setPosition(sourceStart, sourceEnd);
		ret.setSourceContext(new EclipseSourceContext(compilationResult));
		return ret;
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
	
	public String toString(int tab) {
		StringBuffer buf = new StringBuffer();
		buf.append(tabString(tab));
		if (modifiers != 0) {
			buf.append(modifiersString(modifiers));
		}

		buf.append("pointcut ");
		buf.append(new String(selector));
		buf.append("(");
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) buf.append(", ");
				buf.append(arguments[i].toString(0));
			};
		};
		buf.append("): "); 

		buf.append(getPointcut());

		buf.append(";");
		return buf.toString();
	}
}
