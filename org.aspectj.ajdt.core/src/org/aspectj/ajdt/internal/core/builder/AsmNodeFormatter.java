/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.util.Iterator;

import org.aspectj.ajdt.internal.compiler.ast.*;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.weaver.*;
import org.aspectj.weaver.patterns.*;
import org.eclipse.jdt.internal.compiler.ast.*;

public class AsmNodeFormatter {

	public static final String DECLARE_PRECEDENCE = "precedence: ";
	public static final String DECLARE_SOFT = "soft: ";
	public static final String DECLARE_PARENTS = "parents: ";
	public static final String DECLARE_WARNING = "warning: ";
	public static final String DECLARE_ERROR = "error: ";
	public static final String DECLARE_UNKNONWN = "<unknown declare>";
	public static final String POINTCUT_ABSTRACT = "<abstract pointcut>";
	public static final String POINTCUT_ANONYMOUS = "<anonymous pointcut>";
	public static final int MAX_MESSAGE_LENGTH = 18;
	public static final String DEC_LABEL = "declare";

	public void genLabelAndKind(MethodDeclaration methodDeclaration, ProgramElementNode node) {
		if (methodDeclaration instanceof AdviceDeclaration) { 
			AdviceDeclaration ad = (AdviceDeclaration)methodDeclaration;
			node.setKind( ProgramElementNode.Kind.ADVICE);
			String label = "";
			label += ad.kind.toString();
			label += "(" + genArguments(ad) + "): ";

			if (ad.kind == AdviceKind.Around) {
				node.setReturnType(ad.returnTypeToString(0));
			}
	
			if (ad.pointcutDesignator != null) {	
				if (ad.pointcutDesignator.getPointcut() instanceof ReferencePointcut) {
					ReferencePointcut rp = (ReferencePointcut)ad.pointcutDesignator.getPointcut();
					label += rp.name + "..";
				} else if (ad.pointcutDesignator.getPointcut() instanceof AndPointcut) {
					AndPointcut ap = (AndPointcut)ad.pointcutDesignator.getPointcut();
					if (ap.getLeft() instanceof ReferencePointcut) {
						label += ap.getLeft().toString() + "..";	
					} else {
						label += POINTCUT_ANONYMOUS + "..";
					}
				} else if (ad.pointcutDesignator.getPointcut() instanceof OrPointcut) {
					OrPointcut op = (OrPointcut)ad.pointcutDesignator.getPointcut();
					if (op.getLeft() instanceof ReferencePointcut) {
						label += op.getLeft().toString() + "..";	
					} else {
						label += POINTCUT_ANONYMOUS + "..";
					}
				} else {
					label += POINTCUT_ANONYMOUS;
				}
			} else {
				label += POINTCUT_ABSTRACT;
			}
			node.setName(label);

		} else if (methodDeclaration instanceof PointcutDeclaration) { 
			PointcutDeclaration pd = (PointcutDeclaration)methodDeclaration;
			node.setKind( ProgramElementNode.Kind.POINTCUT);
			String label = translatePointcutName(new String(methodDeclaration.selector));
			label += "(" + genArguments(pd) + ")";
			node.setName(label); 
			
		} else if (methodDeclaration instanceof DeclareDeclaration) { 
			DeclareDeclaration declare = (DeclareDeclaration)methodDeclaration;
			String label = DEC_LABEL + " ";
			if (declare.declare instanceof DeclareErrorOrWarning) {
				DeclareErrorOrWarning deow = (DeclareErrorOrWarning)declare.declare;
				
				if (deow.isError()) {
					node.setKind( ProgramElementNode.Kind.DECLARE_ERROR);
					label += DECLARE_ERROR;
				} else {
					node.setKind( ProgramElementNode.Kind.DECLARE_WARNING);
					label += DECLARE_WARNING;
				}
				node.setName(label + "\"" + genDeclareMessage(deow.getMessage()) + "\"") ;
				 
			} else if (declare.declare instanceof DeclareParents) {
				node.setKind( ProgramElementNode.Kind.DECLARE_PARENTS);
				DeclareParents dp = (DeclareParents)declare.declare;
				node.setName(label + DECLARE_PARENTS + genTypePatternLabel(dp.getChild()));	
				
			} else if (declare.declare instanceof DeclareSoft) {
				node.setKind( ProgramElementNode.Kind.DECLARE_SOFT);
				DeclareSoft ds = (DeclareSoft)declare.declare;
				node.setName(label + DECLARE_SOFT + genTypePatternLabel(ds.getException()));
			} else if (declare.declare instanceof DeclarePrecedence) {
				node.setKind( ProgramElementNode.Kind.DECLARE_PRECEDENCE);
				DeclarePrecedence ds = (DeclarePrecedence)declare.declare;
				node.setName(label + DECLARE_PRECEDENCE + genPrecedenceListLabel(ds.getPatterns()));
			} else {
				node.setKind( ProgramElementNode.Kind.ERROR);
				node.setName(DECLARE_UNKNONWN);
			}
			
		} else if (methodDeclaration instanceof InterTypeDeclaration) {
			InterTypeDeclaration itd = (InterTypeDeclaration)methodDeclaration;
			String label = itd.onType.toString() + "." + new String(itd.getDeclaredSelector()); 
			if (methodDeclaration instanceof InterTypeFieldDeclaration) {
				node.setKind(ProgramElementNode.Kind.INTER_TYPE_FIELD);				
			} else if (methodDeclaration instanceof InterTypeMethodDeclaration) {
				node.setKind(ProgramElementNode.Kind.INTER_TYPE_METHOD);
				InterTypeMethodDeclaration itmd = (InterTypeMethodDeclaration)methodDeclaration;			
				label += "(" + genArguments(itd) + ")";
			} else if (methodDeclaration instanceof InterTypeConstructorDeclaration) {
				node.setKind(ProgramElementNode.Kind.INTER_TYPE_CONSTRUCTOR);
				InterTypeConstructorDeclaration itcd = (InterTypeConstructorDeclaration)methodDeclaration;				
			} else {
				node.setKind(ProgramElementNode.Kind.ERROR);
			}
			node.setName(label);
			node.setReturnType(itd.returnType.toString());
			
		} else {
			node.setKind(ProgramElementNode.Kind.METHOD);
			node.setName(new String(methodDeclaration.selector));	
		}
	}


	private String genPrecedenceListLabel(TypePatternList list) {
		String tpList = "";
		for (int i = 0; i < list.size(); i++) {
			tpList += genTypePatternLabel(list.get(i));
			if (i < list.size()-1) tpList += ", ";
		} 
		return tpList;
	}
  
	private String genArguments(MethodDeclaration md) {
		String args = "";
		Argument[] argArray = md.arguments;
		if (argArray == null) return args;
		for (int i = 0; i < argArray.length; i++) {
			String argName = new String(argArray[i].name);
			String argType = argArray[i].type.toString();
//			TODO: fix this way of determing ajc-added arguments, make subtype of Argument with extra info
			if (!argName.startsWith("ajc$this_") 
				&& !argType.equals("org.aspectj.lang.JoinPoint.StaticPart")
				&& !argType.equals("org.aspectj.lang.JoinPoint")
				&& !argType.equals("org.aspectj.runtime.internal.AroundClosure")) {   
				args += argType + ", ";
			}  
		}
		int lastSepIndex = args.lastIndexOf(',');
		if (lastSepIndex != -1 && args.endsWith(", ")) args = args.substring(0, lastSepIndex);
		return args;
	}

	public String genTypePatternLabel(TypePattern tp) {
		final String TYPE_PATTERN_LITERAL = "<type pattern>";
		String label;
		TypeX typeX = tp.getExactType();
		
		if (typeX != ResolvedTypeX.MISSING) {
			label = typeX.getName();
			if (tp.isIncludeSubtypes()) label += "+";
		} else {
			label = TYPE_PATTERN_LITERAL;
		}
		return label;
		
	}

	public String genDeclareMessage(String message) {
		int length = message.length();
		if (length < MAX_MESSAGE_LENGTH) {
			return message;
		} else {
			return message.substring(0, MAX_MESSAGE_LENGTH-1) + "..";
		}
	}
	
//	// TODO: 
//	private String translateAdviceName(String label) {
//		if (label.indexOf("before") != -1) return "before";
//		if (label.indexOf("returning") != -1) return "after returning";
//		if (label.indexOf("after") != -1) return "after";
//		if (label.indexOf("around") != -1) return "around";
//		else return "<advice>";
//	}
	
//	// !!! move or replace
//	private String translateDeclareName(String name) {
//		int colonIndex = name.indexOf(":");
//		if (colonIndex != -1) {
//			return name.substring(0, colonIndex);
//		} else { 
//			return name;
//		}
//	}

	// !!! move or replace
//	private String translateInterTypeDecName(String name) {
//		int index = name.lastIndexOf('$');
//		if (index != -1) {
//			return name.substring(index+1);
//		} else { 
//			return name;
//		}
//	}

	// !!! move or replace
	private String translatePointcutName(String name) {
		int index = name.indexOf("$$")+2;
		int endIndex = name.lastIndexOf('$');
		if (index != -1 && endIndex != -1) {
			return name.substring(index, endIndex);
		} else { 
			return name;
		}
	}


}
