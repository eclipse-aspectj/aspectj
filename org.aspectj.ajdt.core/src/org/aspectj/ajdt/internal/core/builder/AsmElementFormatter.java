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

import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeConstructorDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeFieldDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeMethodDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.compiler.lookup.AjLookupEnvironment;
import org.aspectj.asm.IProgramElement;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.ReferencePointcut;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.TypePatternList;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;

/**
 * @author Mik Kersten
 */
public class AsmElementFormatter {

	public static final String DECLARE_PRECEDENCE = "precedence";
	public static final String DECLARE_SOFT = "soft";
	public static final String DECLARE_PARENTS = "parents";
	public static final String DECLARE_WARNING = "warning";
	public static final String DECLARE_ERROR = "error";
	public static final String DECLARE_UNKNONWN = "<unknown declare>";
	public static final String POINTCUT_ABSTRACT = "<abstract pointcut>";
	public static final String POINTCUT_ANONYMOUS = "<anonymous pointcut>";
	public static final int MAX_MESSAGE_LENGTH = 18;
	public static final String DEC_LABEL = "declare";

	public void genLabelAndKind(MethodDeclaration methodDeclaration, IProgramElement node) {
		
		if (methodDeclaration instanceof AdviceDeclaration) { 
			AdviceDeclaration ad = (AdviceDeclaration)methodDeclaration;
			node.setKind(IProgramElement.Kind.ADVICE);

			if (ad.kind == AdviceKind.Around) {
				node.setCorrespondingType(ad.returnType.toString()); //returnTypeToString(0));
			}
	
			String details = "";
			if (ad.pointcutDesignator != null) {	
				if (ad.pointcutDesignator.getPointcut() instanceof ReferencePointcut) {
					ReferencePointcut rp = (ReferencePointcut)ad.pointcutDesignator.getPointcut();
					details += rp.name + "..";
				} else if (ad.pointcutDesignator.getPointcut() instanceof AndPointcut) {
					AndPointcut ap = (AndPointcut)ad.pointcutDesignator.getPointcut();
					if (ap.getLeft() instanceof ReferencePointcut) {
						details += ap.getLeft().toString() + "..";	
					} else {
						details += POINTCUT_ANONYMOUS + "..";
					}
				} else if (ad.pointcutDesignator.getPointcut() instanceof OrPointcut) {
					OrPointcut op = (OrPointcut)ad.pointcutDesignator.getPointcut();
					if (op.getLeft() instanceof ReferencePointcut) {
						details += op.getLeft().toString() + "..";	
					} else {
						details += POINTCUT_ANONYMOUS + "..";
					}
				} else {
					details += POINTCUT_ANONYMOUS;
				}
			} else {
				details += POINTCUT_ABSTRACT;
			} 
			node.setName(ad.kind.toString());
			node.setDetails(details);
			setParameters(methodDeclaration, node);

		} else if (methodDeclaration instanceof PointcutDeclaration) { 
//			PointcutDeclaration pd = (PointcutDeclaration)methodDeclaration;
			node.setKind(IProgramElement.Kind.POINTCUT);
			node.setName(translatePointcutName(new String(methodDeclaration.selector)));
			setParameters(methodDeclaration, node);
			
		} else if (methodDeclaration instanceof DeclareDeclaration) { 
			DeclareDeclaration declare = (DeclareDeclaration)methodDeclaration;
			String name = DEC_LABEL + " ";
			if (declare.declareDecl instanceof DeclareErrorOrWarning) {
				DeclareErrorOrWarning deow = (DeclareErrorOrWarning)declare.declareDecl;
				
				if (deow.isError()) {
					node.setKind( IProgramElement.Kind.DECLARE_ERROR);
					name += DECLARE_ERROR;
				} else {
					node.setKind( IProgramElement.Kind.DECLARE_WARNING);
					name += DECLARE_WARNING;
				}
				node.setName(name) ;
				node.setDetails("\"" + genDeclareMessage(deow.getMessage()) + "\"");
				
			} else if (declare.declareDecl instanceof DeclareParents) {

				node.setKind( IProgramElement.Kind.DECLARE_PARENTS);
				DeclareParents dp = (DeclareParents)declare.declareDecl;
				node.setName(name + DECLARE_PARENTS);
				
				String kindOfDP = null;
				StringBuffer details = new StringBuffer("");
				TypePattern[] newParents = dp.getParents().getTypePatterns();
				for (int i = 0; i < newParents.length; i++) {
					TypePattern tp = newParents[i];
					TypeX tx = tp.getExactType();
					if (kindOfDP == null) {
					  kindOfDP = "implements ";
					  try {
					  	ResolvedTypeX rtx = tx.resolve(((AjLookupEnvironment)declare.scope.environment()).factory.getWorld());
						if (!rtx.isInterface()) kindOfDP = "extends ";
					  } catch (Throwable t) {
					  	// What can go wrong???? who knows!
					  }
					  
					}
					String typename= tp.toString();
					if (typename.lastIndexOf(".")!=-1) {
						typename=typename.substring(typename.lastIndexOf(".")+1);
					}
					details.append(typename);
					if ((i+1)<newParents.length) details.append(",");
				}
				node.setDetails(kindOfDP+details.toString());

			} else if (declare.declareDecl instanceof DeclareSoft) {
				node.setKind( IProgramElement.Kind.DECLARE_SOFT);
				DeclareSoft ds = (DeclareSoft)declare.declareDecl;
				node.setName(name + DECLARE_SOFT);
				node.setDetails(genTypePatternLabel(ds.getException()));
				
			} else if (declare.declareDecl instanceof DeclarePrecedence) {
				node.setKind( IProgramElement.Kind.DECLARE_PRECEDENCE);
				DeclarePrecedence ds = (DeclarePrecedence)declare.declareDecl;
				node.setName(name + DECLARE_PRECEDENCE);
				node.setDetails(genPrecedenceListLabel(ds.getPatterns()));
				
				
			} else {
				node.setKind(IProgramElement.Kind.ERROR);
				node.setName(DECLARE_UNKNONWN);
			}
			
		} else if (methodDeclaration instanceof InterTypeDeclaration) {
			InterTypeDeclaration itd = (InterTypeDeclaration)methodDeclaration;
			String name = itd.onType.toString() + "." + new String(itd.getDeclaredSelector()); 
			if (methodDeclaration instanceof InterTypeFieldDeclaration) {
				node.setKind(IProgramElement.Kind.INTER_TYPE_FIELD);
				node.setName(name);
			} else if (methodDeclaration instanceof InterTypeMethodDeclaration) {
				node.setKind(IProgramElement.Kind.INTER_TYPE_METHOD);
				node.setName(name);
			} else if (methodDeclaration instanceof InterTypeConstructorDeclaration) {
				node.setKind(IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR);
				
	//			StringBuffer argumentsSignature = new StringBuffer("fubar");
//				argumentsSignature.append("(");
//				if (methodDeclaration.arguments!=null && methodDeclaration.arguments.length>1) {
//		
//				for (int i = 1;i<methodDeclaration.arguments.length;i++) {
//					argumentsSignature.append(methodDeclaration.arguments[i]);
//					if (i+1<methodDeclaration.arguments.length) argumentsSignature.append(",");
//				}
//				}
//				argumentsSignature.append(")");
//				InterTypeConstructorDeclaration itcd = (InterTypeConstructorDeclaration)methodDeclaration;				
				node.setName(itd.onType.toString() + "." + itd.onType.toString()/*+argumentsSignature.toString()*/);
			} else {
				node.setKind(IProgramElement.Kind.ERROR);
				node.setName(name);
			}
			node.setCorrespondingType(itd.returnType.toString());
			if (node.getKind() != IProgramElement.Kind.INTER_TYPE_FIELD) {
				setParameters(methodDeclaration, node);
			}		
		} else {			
			if (methodDeclaration.isConstructor()) {
				node.setKind(IProgramElement.Kind.CONSTRUCTOR);
			} else {
				node.setKind(IProgramElement.Kind.METHOD);
			} 
			String label = new String(methodDeclaration.selector);
			node.setName(label); 
			setParameters(methodDeclaration, node);
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
  
//	private String genArguments(MethodDeclaration md) {
//		String args = "";
//		Argument[] argArray = md.arguments;
//		if (argArray == null) return args;
//		for (int i = 0; i < argArray.length; i++) {
//			String argName = new String(argArray[i].name);
//			String argType = argArray[i].type.toString();
//			if (acceptArgument(argName, argType)) {   
//				args += argType + ", ";
//			}  
//		}
//		int lastSepIndex = args.lastIndexOf(',');
//		if (lastSepIndex != -1 && args.endsWith(", ")) args = args.substring(0, lastSepIndex);
//		return args;
//	}

	private void setParameters(MethodDeclaration md, IProgramElement pe) {
		Argument[] argArray = md.arguments;
		List names = new ArrayList();
		List types = new ArrayList();
		pe.setParameterNames(names);
		pe.setParameterTypes(types);
		
		if (argArray == null) return;
		for (int i = 0; i < argArray.length; i++) {
			String argName = new String(argArray[i].name);
			String argType = argArray[i].type.toString();
			if (acceptArgument(argName, argType)) { 
				names.add(argName);
				types.add(argType);
			}   
		}
	}

	// TODO: fix this way of determing ajc-added arguments, make subtype of Argument with extra info
	private boolean acceptArgument(String name, String type) {
		return !name.startsWith("ajc$this_") 
			&& !type.equals("org.aspectj.lang.JoinPoint.StaticPart")
			&& !type.equals("org.aspectj.lang.JoinPoint")
			&& !type.equals("org.aspectj.runtime.internal.AroundClosure");
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
