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

import org.aspectj.asm.AdviceAssociation;
import org.aspectj.asm.LinkNode;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.asm.Relation;
import org.aspectj.asm.RelationNode;
import org.aspectj.asm.StructureModel;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;

public class AsmAdaptor {
	public static void noteMunger(StructureModel model, Shadow shadow, ShadowMunger munger) {
		if (munger instanceof Advice) {
			Advice a = (Advice)munger;
			if (a.getKind().isPerEntry() || a.getKind().isCflow()) {
				// ??? might want to show these in the future
				return;
			}

//			System.out.println("--------------------------");
			ProgramElementNode targetNode = getNode(model, shadow);
			ProgramElementNode adviceNode = getNode(model, a);  
			
			Relation relation;
			if (shadow.getKind().equals(Shadow.FieldGet) || shadow.getKind().equals(Shadow.FieldSet)) {
				relation = AdviceAssociation.FIELD_ACCESS_RELATION;
			} else if (shadow.getKind().equals(Shadow.Initialization) || shadow.getKind().equals(Shadow.StaticInitialization)) {
				relation = AdviceAssociation.INITIALIZER_RELATION;
			} else if (shadow.getKind().equals(Shadow.ExceptionHandler)) {
				relation = AdviceAssociation.HANDLER_RELATION;
			} else if (shadow.getKind().equals(Shadow.MethodCall)) {
				relation = AdviceAssociation.METHOD_CALL_SITE_RELATION;
			} else if (shadow.getKind().equals(Shadow.ConstructorCall)) {
				relation = AdviceAssociation.CONSTRUCTOR_CALL_SITE_RELATION;
			} else if (shadow.getKind().equals(Shadow.MethodExecution) || shadow.getKind().equals(Shadow.AdviceExecution)) {
				relation = AdviceAssociation.METHOD_RELATION;
			} else if (shadow.getKind().equals(Shadow.ConstructorExecution)) {
				relation = AdviceAssociation.CONSTRUCTOR_RELATION;
			} else {
				System.err.println("> unmatched relation: " + shadow.getKind());
				relation = AdviceAssociation.METHOD_RELATION;
			}
			
//			System.out.println("> target: " + targetNode + ", advice: " + adviceNode);
			createAppropriateLinks(targetNode, adviceNode, relation);
		}
	}

	private static void createAppropriateLinks(
		ProgramElementNode target,
		ProgramElementNode advice,
		Relation relation)
	{
		if (target == null || advice == null) return;
		
		
		addLink(target, new LinkNode(advice),  relation, true);
		addLink(advice, new LinkNode(target),  relation, false);
		
//		System.out.println(">> added target: " + target.getProgramElementKind() + ", advice: " + advice);
//		System.out.println(">> target: " + target + ", advice: " + target.getSourceLocation());
	}

	private static void addLink(
		ProgramElementNode onNode,
		LinkNode linkNode,
		Relation relation,
		boolean isBack)
	{
		RelationNode node = null;
		String relationName = isBack ? relation.getBackNavigationName() : relation.getForwardNavigationName();
		
		//System.err.println("on: " + onNode + " relationName: " + relationName + " existin: " + onNode.getRelations());
		
		for (Iterator i = onNode.getRelations().iterator(); i.hasNext();) {
			RelationNode relationNode = (RelationNode) i.next();
			if (relationName.equals(relationNode.getName())) {
				node = relationNode;
				break;
			}
		}	
		if (node == null) {
			node = new RelationNode(relation,  relationName, new ArrayList());
			onNode.getRelations().add(node);
		}
		node.getChildren().add(linkNode);
		
	}

	private static ProgramElementNode getNode(StructureModel model, Advice a) {
		//ResolvedTypeX inAspect = a.getConcreteAspect();
		Member member = a.getSignature();
		if (a.getSignature() == null) return null;
		return lookupMember(model, member);
	}
	
	private static ProgramElementNode getNode(StructureModel model, Shadow shadow) {
		Member enclosingMember = shadow.getEnclosingCodeSignature();
		
		ProgramElementNode enclosingNode = lookupMember(model, enclosingMember);
		if (enclosingNode == null) {
			Lint.Kind err = shadow.getIWorld().getLint().shadowNotInStructure;
			if (err.isEnabled()) {
				err.signal(shadow.toString(), shadow.getSourceLocation());
			}
			return null;
		}
		
		Member shadowSig = shadow.getSignature();
		if (!shadowSig.equals(enclosingMember)) {
			ProgramElementNode bodyNode = findOrCreateBodyNode(enclosingNode, shadowSig, shadow);
			return bodyNode;
		} else {
			return enclosingNode;
		}
	}

	private static ProgramElementNode findOrCreateBodyNode(
		ProgramElementNode enclosingNode,
		Member shadowSig, Shadow shadow)
	{
		for (Iterator it = enclosingNode.getChildren().iterator(); it.hasNext(); ) {
			ProgramElementNode node = (ProgramElementNode)it.next();
			if (shadowSig.getName().equals(node.getBytecodeName()) &&
				shadowSig.getSignature().equals(node.getBytecodeSignature()))
			{
				return node;
			}
		}
		
		ISourceLocation sl = shadow.getSourceLocation();
		
		ProgramElementNode peNode = new ProgramElementNode(
			shadow.toString(),
			ProgramElementNode.Kind.CODE,
//XXX why not use shadow file? new SourceLocation(sl.getSourceFile(), sl.getLine()),
        new SourceLocation(enclosingNode.getSourceLocation().getSourceFile(), sl.getLine()),
//			enclosingNode.getSourceLocation(),
			0,
			"",
			new ArrayList());
			
		//System.err.println(peNode.getSourceLocation());
		peNode.setBytecodeName(shadowSig.getName());
		peNode.setBytecodeSignature(shadowSig.getSignature());
		enclosingNode.addChild(peNode);
		return peNode;
	}


	
	
	
	public static ProgramElementNode lookupMember(StructureModel model, Member member) {
		TypeX declaringType = member.getDeclaringType();
		ProgramElementNode classNode =
			model.findNodeForClass(declaringType.getPackageName(), declaringType.getClassName());
		return findMemberInClass(classNode, member);
	}

	private static ProgramElementNode findMemberInClass(
		ProgramElementNode classNode,
		Member member)
	{
		if (classNode == null) return null; // XXX remove this check
		for (Iterator it = classNode.getChildren().iterator(); it.hasNext(); ) {
			ProgramElementNode node = (ProgramElementNode)it.next();
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




}
