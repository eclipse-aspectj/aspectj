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
import java.util.ArrayList;
import java.util.Iterator;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.bcel.BcelAdvice;

public class AsmRelationshipProvider {
	
    protected static AsmRelationshipProvider INSTANCE = new AsmRelationshipProvider();
    
	public static final String ADVISES = "advises";
	public static final String ADVISED_BY = "advised by";
	public static final String DECLARES_ON = "declares on";
	public static final String DECLAREDY_BY = "declared by";
	public static final String MATCHED_BY = "matched by";
	public static final String MATCHES_DECLARE = "matches declare";
	public static final String INTER_TYPE_DECLARES = "declared on";
	public static final String INTER_TYPE_DECLARED_BY = "aspect declarations";
	
	public void checkerMunger(IHierarchy model, Shadow shadow, Checker checker) {
		if (shadow.getSourceLocation() == null || checker.getSourceLocation() == null) return;
		
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
			IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE, MATCHED_BY,false,true);
			foreward.addTarget(targetHandle);
//			foreward.getTargets().add(targetHandle);
				
			IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE, MATCHES_DECLARE,false,true);
			if (back != null && back.getTargets() != null) {
				back.addTarget(sourceHandle);
				//back.getTargets().add(sourceHandle);   
			}
		}
	}

    // For ITDs
	public void addRelationship(
		ResolvedTypeX onType,
		ResolvedTypeMunger munger,
		ResolvedTypeX originatingAspect) {

		String sourceHandle = "";
		if (munger.getSourceLocation()!=null) {
			sourceHandle = ProgramElement.createHandleIdentifier(
										munger.getSourceLocation().getSourceFile(),
										munger.getSourceLocation().getLine(),
										munger.getSourceLocation().getColumn());
		} else {
			sourceHandle = ProgramElement.createHandleIdentifier(
							originatingAspect.getSourceLocation().getSourceFile(),
							originatingAspect.getSourceLocation().getLine(),
							originatingAspect.getSourceLocation().getColumn());
		}
		if (originatingAspect.getSourceLocation() != null) {
				
			String targetHandle = ProgramElement.createHandleIdentifier(
				onType.getSourceLocation().getSourceFile(),
				onType.getSourceLocation().getLine(),
				onType.getSourceLocation().getColumn());
				
			IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
			if (sourceHandle != null && targetHandle != null) {
				IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARES,false,true);
				foreward.addTarget(targetHandle);
//				foreward.getTargets().add(targetHandle);
				
				IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARED_BY,false,true);
				back.addTarget(sourceHandle);
//				back.getTargets().add(sourceHandle);  
			}
		}
	}
	
	public void addDeclareParentsRelationship(ISourceLocation decp,ResolvedTypeX targetType, List newParents) {

		String sourceHandle = ProgramElement.createHandleIdentifier(decp.getSourceFile(),decp.getLine(),decp.getColumn());
		
		IProgramElement ipe = AsmManager.getDefault().getHierarchy().findElementForHandle(sourceHandle);
		
	
		String targetHandle = ProgramElement.createHandleIdentifier(
				targetType.getSourceLocation().getSourceFile(),
				targetType.getSourceLocation().getLine(),
				targetType.getSourceLocation().getColumn());
				
		IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
		if (sourceHandle != null && targetHandle != null) {
			IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARES,false,true);
			foreward.addTarget(targetHandle);
				
			IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARED_BY,false,true);
			back.addTarget(sourceHandle);
		}
		
	}
	
	public void adviceMunger(IHierarchy model, Shadow shadow, ShadowMunger munger) {
		if (munger instanceof Advice) {
			Advice advice = (Advice)munger;
			
			if (advice.getKind().isPerEntry() || advice.getKind().isCflow()) {
				// TODO: might want to show these in the future
				return;
			}
			

			IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
			IProgramElement targetNode = getNode(AsmManager.getDefault().getHierarchy(), shadow);
			boolean runtimeTest = ((BcelAdvice)munger).hasDynamicTests();
			
			// Work out extra info to inform interested UIs !
			IProgramElement.ExtraInformation ai = new IProgramElement.ExtraInformation();

			String adviceHandle = advice.getHandle(); 
			
			// What kind of advice is it?
			// TODO: Prob a better way to do this but I just want to
			// get it into CVS !!!
			AdviceKind ak = ((Advice)munger).getKind();
			ai.setExtraAdviceInformation(ak.getName());
			IProgramElement adviceElement = AsmManager.getDefault().getHierarchy().findElementForHandle(adviceHandle);
			adviceElement.setExtraInfo(ai);		
			
			if (adviceHandle != null && targetNode != null) {
		
				if (targetNode != null) {
					String targetHandle = targetNode.getHandleIdentifier();	
				
					IRelationship foreward = mapper.get(adviceHandle, IRelationship.Kind.ADVICE, ADVISES,runtimeTest,true);
					if (foreward != null) foreward.addTarget(targetHandle);//foreward.getTargets().add(targetHandle);
					
					IRelationship back = mapper.get(targetHandle, IRelationship.Kind.ADVICE, ADVISED_BY,runtimeTest,true);
					if (back != null)     back.addTarget(adviceHandle);//back.getTargets().add(adviceHandle);
				}
			}

		}
	}

	private IProgramElement getNode(IHierarchy model, Shadow shadow) {
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
	
	private boolean sourceLinesMatch(ISourceLocation loc1,ISourceLocation loc2) {
		if (loc1.getLine()!=loc2.getLine()) return false;
		return true;
	}
	
	
	private IProgramElement findOrCreateCodeNode(IProgramElement enclosingNode, Member shadowSig, Shadow shadow)
	{
		for (Iterator it = enclosingNode.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement node = (IProgramElement)it.next();
			if (shadowSig.getName().equals(node.getBytecodeName()) &&
				shadowSig.getSignature().equals(node.getBytecodeSignature()) &&
				sourceLinesMatch(node.getSourceLocation(),shadow.getSourceLocation()))
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
	
	protected IProgramElement lookupMember(IHierarchy model, Member member) {
		TypeX declaringType = member.getDeclaringType();
		IProgramElement classNode =
			model.findElementForType(declaringType.getPackageName(), declaringType.getClassName());
		return findMemberInClass(classNode, member);
	}
 
	protected IProgramElement findMemberInClass(
		IProgramElement classNode,
		Member member)
	{
		if (classNode == null) return null; // XXX remove this check
		for (Iterator it = classNode.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement node = (IProgramElement)it.next();
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

    public static AsmRelationshipProvider getDefault() {
        return INSTANCE;
    }
    
    /**
     * Reset the instance of this class, intended for extensibility.
     * This enables a subclass to become used as the default instance.
     */
    public static void setDefault(AsmRelationshipProvider instance) {
        INSTANCE = instance;
    }
	
}
