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

import java.util.*;

import org.aspectj.asm.*;
import org.aspectj.asm.internal.*;
import org.aspectj.bridge.*;

public class AsmAdaptor {
	
	public static void nodeMunger(StructureModel model, Shadow shadow, ShadowMunger munger) {
		if (munger instanceof Advice) {
			Advice a = (Advice)munger;
//			if (a.getKind().isPerEntry() || a.getKind().isCflow()) {
				// ??? might want to show these in the future
//				return;
//			}

//			System.out.println("--------------------------");
			IProgramElement targetNode = getNode(model, shadow);
			IProgramElement adviceNode = getNode(model, a);  
			
			if (adviceNode != null && targetNode != null) {
//				mapper.putRelationshipForElement(
//					adviceNode, 
//					ADVICE, 
//					targetNode);
			}
				
//			System.out.println("> target: " + targetNode + ", advice: " + adviceNode);
//			throw new RuntimeException("unimplemented");
//			IRelationship relation = new Relationship();
//			if (shadow.getKind().equals(Shadow.FieldGet) || shadow.getKind().equals(Shadow.FieldSet)) {
//				relation = AdviceAssociation.FIELD_ACCESS_RELATION;
//			} else if (shadow.getKind().equals(Shadow.Initialization) || shadow.getKind().equals(Shadow.StaticInitialization)) {
//				relation = AdviceAssociation.INITIALIZER_RELATION;
//			} else if (shadow.getKind().equals(Shadow.ExceptionHandler)) {
//				relation = AdviceAssociation.HANDLER_RELATION;
//			} else if (shadow.getKind().equals(Shadow.MethodCall)) {
//				relation = AdviceAssociation.METHOD_CALL_SITE_RELATION;
//			} else if (shadow.getKind().equals(Shadow.ConstructorCall)) {
//				relation = AdviceAssociation.CONSTRUCTOR_CALL_SITE_RELATION;
//			} else if (shadow.getKind().equals(Shadow.MethodExecution) || shadow.getKind().equals(Shadow.AdviceExecution)) {
//				relation = AdviceAssociation.METHOD_RELATION;
//			} else if (shadow.getKind().equals(Shadow.ConstructorExecution)) {
//				relation = AdviceAssociation.CONSTRUCTOR_RELATION;
//			} else if (shadow.getKind().equals(Shadow.PreInitialization)) {
//				// TODO: someone should check that this behaves reasonably in the IDEs
//				relation = AdviceAssociation.INITIALIZER_RELATION;
//			} else {
//				System.err.println("> unmatched relation: " + shadow.getKind());
//				relation = AdviceAssociation.METHOD_RELATION;
//			}
//			createAppropriateLinks(targetNode, adviceNode, relation);
		}
	}

	private static void createAppropriateLinks(
		IProgramElement target,
		IProgramElement advice,
		IRelationship relation)
	{
		if (target == null || advice == null) return;
		
		
//		addLink(target, new LinkNode(advice),  relation, true);
//		addLink(advice, new LinkNode(target),  relation, false);
	}

	private static void addLink(
		IProgramElement onNode,
//		LinkNode linkNode,
		IRelationship relation,
		boolean isBack)
	{
		IRelationship node = null;
		String relationName = relation.getName();
//		isBack ? relation() : relation.getForwardNavigationName();
		
		//System.err.println("on: " + onNode + " relationName: " + relationName + " existin: " + onNode.getRelations());
		
		for (Iterator i = onNode.getRelations().iterator(); i.hasNext();) {
			IRelationship relationNode = (IRelationship) i.next();
			if (relationName.equals(relationNode.getName())) {
				node = relationNode;
				break;
			}
		}	
		if (node == null) {
			throw new RuntimeException("unimplemented");
//			node = new Relationship(relation,  relationName, new ArrayList());
//			onNode.getRelations().add(node);
		}
//		node.getTargets().add(linkNode);
		
	}

	private static IProgramElement getNode(StructureModel model, Advice a) {
		//ResolvedTypeX inAspect = a.getConcreteAspect();
		Member member = a.getSignature();
		if (a.getSignature() == null) return null;
		return lookupMember(model, member);
	}
	
	private static IProgramElement getNode(StructureModel model, Shadow shadow) {
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
			IProgramElement bodyNode = findOrCreateBodyNode(enclosingNode, shadowSig, shadow);
			return bodyNode;
		} else {
			return enclosingNode;
		}
	}

	private static IProgramElement findOrCreateBodyNode(
		IProgramElement enclosingNode,
		Member shadowSig, Shadow shadow)
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


	
	
	
	public static IProgramElement lookupMember(StructureModel model, Member member) {
		TypeX declaringType = member.getDeclaringType();
		IProgramElement classNode =
			model.findNodeForClass(declaringType.getPackageName(), declaringType.getClassName());
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
}
