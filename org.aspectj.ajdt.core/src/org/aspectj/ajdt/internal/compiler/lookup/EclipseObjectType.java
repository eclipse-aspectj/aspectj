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


package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.*;

import org.aspectj.ajdt.internal.compiler.ast.*;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.*;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class EclipseObjectType extends ResolvedTypeX.Name {
	private ReferenceBinding binding;

	private ResolvedMember[] declaredPointcuts = null;
	private ResolvedMember[] declaredMethods = null;
	private ResolvedMember[] declaredFields = null;
	

	public EclipseObjectType(String signature, EclipseWorld world, ReferenceBinding binding) {
		super(signature, world);
		this.binding = binding;
	}
	
	private EclipseWorld eclipseWorld() {
		return (EclipseWorld)world;
	}


	public boolean isAspect() {
		if (!(binding instanceof SourceTypeBinding)) return false;
		//XXX assume SourceBinding throughout
		return ((SourceTypeBinding)binding).scope.referenceContext instanceof AspectDeclaration;

	}
	
	public ResolvedTypeX getSuperclass() {
		//XXX what about java.lang.Object
		return eclipseWorld().fromEclipse(binding.superclass());
	}
	
	public ResolvedTypeX[] getDeclaredInterfaces() {
		return eclipseWorld().fromEclipse(binding.superInterfaces());
	}

	public ResolvedMember[] getDeclaredFields() {
		if (declaredFields == null) fillDeclaredMembers();
		return declaredFields;
	}

	public ResolvedMember[] getDeclaredMethods() {
		if (declaredMethods == null) fillDeclaredMembers();
		return declaredMethods;
	}

	public ResolvedMember[] getDeclaredPointcuts() {
		if (declaredPointcuts == null) fillDeclaredMembers();
		return declaredPointcuts;
	}

	private void fillDeclaredMembers() {
		List declaredPointcuts = new ArrayList();
		List declaredMethods = new ArrayList();
		List declaredFields = new ArrayList();
		
		MethodBinding[] methods = binding.methods();
		for (int i=0, len=methods.length; i < len; i++) {
			MethodBinding m = methods[i];
			AbstractMethodDeclaration amd = m.sourceMethod();
			if (amd == null) continue; //???
			if (amd instanceof PointcutDeclaration) {
				PointcutDeclaration d = (PointcutDeclaration)amd;
				ResolvedPointcutDefinition df = d.makeResolvedPointcutDefinition();
				declaredPointcuts.add(df);
			} else {
				//XXX this doesn't handle advice quite right
				declaredMethods.add(eclipseWorld().makeResolvedMember(m));
			}
		}
		
		FieldBinding[] fields = binding.fields();
		for (int i=0, len=fields.length; i < len; i++) {
			FieldBinding f = fields[i];
			declaredFields.add(eclipseWorld().makeResolvedMember(f));
		}
			
		this.declaredPointcuts = (ResolvedMember[])
			declaredPointcuts.toArray(new ResolvedMember[declaredPointcuts.size()]);
		this.declaredMethods = (ResolvedMember[])
			declaredMethods.toArray(new ResolvedMember[declaredMethods.size()]);
		this.declaredFields = (ResolvedMember[])
			declaredFields.toArray(new ResolvedMember[declaredFields.size()]);
	}

	
	public int getModifiers() {
		// only return the real Java modifiers, not the extra eclipse ones
		return binding.modifiers & CompilerModifiers.AccJustFlag;
	}
	
	ReferenceBinding getBinding() {
		return binding;
	}
	
	public String toString() {
		return "EclipseObjectType(" + getClassName() + ")";
	}
	public CrosscuttingMembers collectCrosscuttingMembers() {
		return crosscuttingMembers;
	}


	//XXX make sure this is applied to classes and interfaces
	public void checkPointcutDeclarations() {
		ResolvedMember[] pointcuts = getDeclaredPointcuts();
		for (int i=0, len=pointcuts.length; i < len; i++) {
			if (pointcuts[i].isAbstract()) {
				if (!this.isAspect()) {
					MessageUtil.error(
							"abstract pointcut only allowed in aspect" + pointcuts[i].getName(),
							pointcuts[i].getSourceLocation());
				} else if (!this.isAbstract()) {
					MessageUtil.error(
							"abstract pointcut in concrete aspect" + pointcuts[i].getName(),
							pointcuts[i].getSourceLocation());
				}
			}
				
			for (int j=i+1; j < len; j++) {
				if (pointcuts[i].getName().equals(pointcuts[j].getName())) {
					eclipseWorld().getMessageHandler().handleMessage(
						MessageUtil.error(
							"duplicate pointcut name: " + pointcuts[j].getName(),
							pointcuts[j].getSourceLocation()));
				}
			}
		}
		
		//now check all inherited pointcuts to be sure that they're handled reasonably
		if (!isAspect()) return;
		
//		for (Iterator i = getSuperclass().getFields(); )
//		XXX
		
		
	}

}
