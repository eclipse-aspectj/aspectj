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

public class AsmAdapter {
	
	public static final String ADVISES = "advises";
	public static final String ADVISED_BY = "advised by";
	public static final String DECLARES_ON = "declares on";
	public static final String DECLAREDY_BY = "declared by";

	public static void checkerMunger(StructureModel model, Shadow shadow) {
//		System.err.println("> " + shadow.getThisVar() + " to " + shadow.getTargetVar());
	}
	
	public static void nodeMunger(StructureModel model, Shadow shadow, ShadowMunger munger) {
		if (munger instanceof Advice) {
			Advice a = (Advice)munger;
			if (a.getKind().isPerEntry() || a.getKind().isCflow()) {
				// TODO: might want to show these in the future
				return;
			}
			IRelationshipMapper mapper = StructureModelManager.getDefault().getMapper();

			IProgramElement targetNode = getNode(model, shadow);
			IProgramElement adviceNode = getNode(model, a);  
			
			if (adviceNode != null && targetNode != null) {
				IRelationship foreward = mapper.get(adviceNode);
				if (foreward == null) {
					foreward = new Relationship(
						ADVISES,
						IRelationship.Kind.ADVICE,
						adviceNode, 
						new ArrayList()
					);
					mapper.put(adviceNode, foreward);
				}
				foreward.getTargets().add(targetNode);

				IRelationship back = mapper.get(targetNode);
				if (back == null) {
					back = new Relationship(
						ADVISED_BY,
						IRelationship.Kind.ADVICE,
						targetNode, 
						new ArrayList()
					);
					mapper.put(targetNode, back);
				}
				back.getTargets().add(adviceNode);
			}
		}
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
			model.findNodeForType(declaringType.getPackageName(), declaringType.getClassName());
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
