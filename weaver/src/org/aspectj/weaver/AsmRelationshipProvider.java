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


package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.Iterator;

import org.aspectj.asm.*;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;

public class AsmRelationshipProvider {
	
	public static final String ADVISES = "advises";
	public static final String ADVISED_BY = "advised by";
	public static final String DECLARES_ON = "declares on";
	public static final String DECLAREDY_BY = "declared by";
	public static final String MATCHED_BY = "matched by";
	public static final String MATCHES_DECLARE = "matches declare";

	public static void checkerMunger(IHierarchy model, Shadow shadow, Checker checker) {
		if (shadow.getSourceLocation() == null || checker == null) return;
		
		String sourceHandle = ProgramElement.createHandleIdentifier(
			checker.getSourceLocation().getSourceFile(),
			checker.getSourceLocation().getLine(),
			checker.getSourceLocation().getColumn());
			
		String targetHandle = ProgramElement.createHandleIdentifier(
			shadow.getSourceLocation().getSourceFile(),
			shadow.getSourceLocation().getLine(),
			shadow.getSourceLocation().getColumn());

		IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
		if (sourceHandle != null && targetHandle != null) {
			IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE, MATCHED_BY);
			foreward.getTargets().add(targetHandle);
				
			IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE, MATCHES_DECLARE);
			back.getTargets().add(sourceHandle);  
		}
	}
	
	public static void adviceMunger(IHierarchy model, Shadow shadow, ShadowMunger munger) {
		if (munger instanceof Advice) {
			Advice advice = (Advice)munger;
			if (advice.getKind().isPerEntry() || advice.getKind().isCflow()) {
				// TODO: might want to show these in the future
				return;
			}
			IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
			IProgramElement targetNode = getNode(AsmManager.getDefault().getHierarchy(), shadow);
			if (advice.getSourceLocation() != null && targetNode != null) {
				String adviceHandle = ProgramElement.createHandleIdentifier(
					advice.getSourceLocation().getSourceFile(),
					advice.getSourceLocation().getLine(),
					advice.getSourceLocation().getColumn());
		
				if (targetNode != null) {
					String targetHandle = targetNode.getHandleIdentifier();	
				
					IRelationship foreward = mapper.get(adviceHandle, IRelationship.Kind.ADVICE, ADVISES);
					if (foreward != null) foreward.getTargets().add(targetHandle);
					
					IRelationship back = mapper.get(targetHandle, IRelationship.Kind.ADVICE, ADVISED_BY);
					if (back != null) back.getTargets().add(adviceHandle);
				}
			}

		}
	}

	private static IProgramElement getNode(IHierarchy model, Shadow shadow) {
		Member enclosingMember = shadow.getEnclosingCodeSignature();
		
		IProgramElement enclosingNode = lookupMember(model, enclosingMember);
		if (enclosingNode == null) {
			Lint.Kind err = shadow.getIWorld().getLint().shadowNotInStructure;
			if (err.isEnabled()) {
				err.signal(shadow.toString(), shadow.getSourceLocation());
			}
			return null;
		}
		
		Member shadowSig = shadow.getSignature();
		if (!shadowSig.equals(enclosingMember)) {
			IProgramElement bodyNode = findOrCreateCodeNode(enclosingNode, shadowSig, shadow);
			return bodyNode;
		} else {
			return enclosingNode;
		}
	}
	
	private static IProgramElement findOrCreateCodeNode(IProgramElement enclosingNode, Member shadowSig, Shadow shadow)
	{
		for (Iterator it = enclosingNode.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement node = (IProgramElement)it.next();
			if (shadowSig.getName().equals(node.getBytecodeName()) &&
				shadowSig.getSignature().equals(node.getBytecodeSignature()))
			{
				return node;
			}
		}
		
		ISourceLocation sl = shadow.getSourceLocation();
		
		IProgramElement peNode = new ProgramElement(
			shadow.toString(),
			IProgramElement.Kind.CODE,
		//XXX why not use shadow file? new SourceLocation(sl.getSourceFile(), sl.getLine()),
			new SourceLocation(enclosingNode.getSourceLocation().getSourceFile(), sl.getLine()),
			0,
			"",
			new ArrayList());
				
		peNode.setBytecodeName(shadowSig.getName());
		peNode.setBytecodeSignature(shadowSig.getSignature());
		enclosingNode.addChild(peNode);
		return peNode;
	}
	
	private static IProgramElement lookupMember(IHierarchy model, Member member) {
		TypeX declaringType = member.getDeclaringType();
		IProgramElement classNode =
			model.findElementForType(declaringType.getPackageName(), declaringType.getClassName());
		return findMemberInClass(classNode, member);
	}

	private static IProgramElement findMemberInClass(
		IProgramElement classNode,
		Member member)
	{
		if (classNode == null) return null; // XXX remove this check
		for (Iterator it = classNode.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement node = (IProgramElement)it.next();
			//System.err.println("checking: " + member.getName() + " with " + node.getBytecodeName() + ", " + node.getBytecodeSignature());
			if (member.getName().equals(node.getBytecodeName()) &&
				member.getSignature().equals(node.getBytecodeSignature()))
			{
				return node;
			}
		}
	 	// if we can't find the member, we'll just put it in the class
		return classNode;
	}
	
//	private static IProgramElement.Kind genShadowKind(Shadow shadow) {
//		IProgramElement.Kind shadowKind;
//		if (shadow.getKind() == Shadow.MethodCall
//			|| shadow.getKind() == Shadow.ConstructorCall
//			|| shadow.getKind() == Shadow.FieldGet
//			|| shadow.getKind() == Shadow.FieldSet
//			|| shadow.getKind() == Shadow.ExceptionHandler) {
//			return IProgramElement.Kind.CODE;
//			
//		} else if (shadow.getKind() == Shadow.MethodExecution) {
//			return IProgramElement.Kind.METHOD;
//			
//		} else if (shadow.getKind() == Shadow.ConstructorExecution) {
//			return IProgramElement.Kind.CONSTRUCTOR;
//			
//		} else if (shadow.getKind() == Shadow.PreInitialization
//			|| shadow.getKind() == Shadow.Initialization) {
//			return IProgramElement.Kind.CLASS;
//			
//		} else if (shadow.getKind() == Shadow.AdviceExecution) {
//			return IProgramElement.Kind.ADVICE;
//			
//		} else {
//			return IProgramElement.Kind.ERROR;
//		}
//	}

}
