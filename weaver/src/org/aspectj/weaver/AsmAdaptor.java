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


package org.aspectj.weaver;

import java.util.*;
import java.util.Iterator;

import org.aspectj.asm.*;
import org.aspectj.asm.StructureModel;
import org.aspectj.bridge.*;
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
//			System.out.println("> target: " + targetNode + ", advice: " + adviceNode);
			createAppropriateLinks(targetNode, adviceNode);
		}
	}

	private static void createAppropriateLinks(
		ProgramElementNode target,
		ProgramElementNode advice)
	{
		if (target == null || advice == null) return;
		addLink(target, new LinkNode(advice),  org.aspectj.asm.AdviceAssociation.METHOD_RELATION, true);
		addLink(advice, new LinkNode(target),  org.aspectj.asm.AdviceAssociation.METHOD_RELATION, false);
//		System.out.println(">> added target: " + target + ", advice: " + advice);
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
		return lookupMember(model, member);
	}
	
	private static ProgramElementNode getNode(StructureModel model, Shadow shadow) {
		Member enclosingMember = shadow.getEnclosingCodeSignature();
		
		ProgramElementNode enclosingNode = lookupMember(model, enclosingMember);
		
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
			new SourceLocation(enclosingNode.getSourceLocation().getSourceFile(), sl.getLine()),
			0,
			"",
			new ArrayList());
			
		System.err.println(peNode.getSourceLocation());
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
			if (member.getName().equals(node.getBytecodeName()) &&
				member.getSignature().equals(node.getBytecodeSignature()))
			{
				return node;
			}
		}
		return null;
	}




}
