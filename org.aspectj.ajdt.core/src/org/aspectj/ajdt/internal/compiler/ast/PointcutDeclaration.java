/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import java.io.*;
import java.io.IOException;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseWorld;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;


public class PointcutDeclaration extends MethodDeclaration implements IAjDeclaration {
	public PointcutDesignator pointcutDesignator;

	public PointcutDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
		this.returnType = TypeReference.baseTypeReference(T_void, 0);
	}

//	public PointcutDeclaration(MethodDeclaration decl, Parser parser) {
//		this(decl.compilationResult);
//		this.sourceEnd = decl.sourceEnd;
//		this.sourceStart = decl.sourceStart;
//		
//		this.arguments = decl.arguments;
//		if (this.arguments == null) this.arguments = new Argument[0];
//		this.modifiers = decl.modifiers;
////		if ((modifiers & AccAbstract) == 0) {
////			modifiers |= AccNative;
////		}
////		modifiers |= AccSemicolonBody; //XXX hack to make me have no body
//		
//		this.modifiersSourceStart = decl.modifiersSourceStart;
//		this.selector = decl.selector;
//		if (decl.thrownExceptions != null && decl.thrownExceptions.length > 0) {
//			//XXX need a better problem to report
//			TypeReference e1 = decl.thrownExceptions[0];
//			parser.problemReporter().parseError(e1.sourceStart, e1.sourceEnd,
//							new char[0], "throws", new String[] {":"});
//		}
//	}
	
	private Pointcut getPointcut() {
		if (pointcutDesignator == null) {
			return Pointcut.makeMatchesNothing(Pointcut.RESOLVED);
		} else {
			return pointcutDesignator.getPointcut();
		}
	}
	

	public ResolvedPointcutDefinition makeResolvedPointcutDefinition() {
		//System.out.println("pc: " + getPointcut());
		return new ResolvedPointcutDefinition(
            EclipseWorld.fromBinding(this.binding.declaringClass), 
            this.modifiers, // & ~AccNative, 
            new String(selector),
			EclipseWorld.fromBindings(this.binding.parameters),
			getPointcut());
	}


	public AjAttribute makeAttribute() {
		return new AjAttribute.PointcutDeclarationAttribute(makeResolvedPointcutDefinition());
//		return new Attribute() {
//			public char[] getAttributeName() { return ResolvedPointcutDefinition.AttributeName.toCharArray(); }
//			public void writeTo(DataOutputStream s) throws IOException {
//				makeResolvedPointcut().writeAttribute(s);
//			}
//		};
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
	
	
	
//	public boolean finishResolveTypes(SourceTypeBinding sourceTypeBinding) {
//		if (!super.finishResolveTypes(sourceTypeBinding)) return false;
//		if (pointcutDesignator != null) {
//			return pointcutDesignator.finishResolveTypes(this, this.binding, arguments.length, sourceTypeBinding);
//		} else {
//			return true;
//		}
//	}
	
	public String toString(int tab) {
		StringBuffer buf = new StringBuffer();
		buf.append(tabString(tab));
		if (modifiers != 0) {
			buf.append(modifiersString(modifiers));
		}
		
//		if (modifiers != AccNative) {
//			buf.append(modifiersString(modifiers & ~AccNative));
//		}
		
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

	public void parseStatements(
		Parser parser,
		CompilationUnitDeclaration unit) {
		if (pointcutDesignator == null) {
			//XXXthrow new RuntimeException("unimplemented");
		} else {
			// do nothing
		}
	}

	public void postParse(TypeDeclaration typeDec) {
		if (arguments == null) arguments = new Argument[0];
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

}
