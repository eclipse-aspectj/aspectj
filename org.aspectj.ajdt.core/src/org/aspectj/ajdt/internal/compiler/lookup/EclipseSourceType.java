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


package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.*;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.*;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class EclipseSourceType extends ResolvedTypeX.ConcreteName {
	protected ResolvedPointcutDefinition[] declaredPointcuts = null;
	protected ResolvedMember[] declaredMethods = null;
	protected ResolvedMember[] declaredFields = null;
	
	public List declares = new ArrayList();
	public List typeMungers = new ArrayList();
	
	private EclipseFactory factory;
	
	private SourceTypeBinding binding;
	private TypeDeclaration declaration;
	
	protected EclipseFactory eclipseWorld() {
		return factory;
	}

	public EclipseSourceType(ResolvedTypeX.Name resolvedTypeX, EclipseFactory factory,
								SourceTypeBinding binding, TypeDeclaration declaration)
	{
		super(resolvedTypeX, true);
		this.factory = factory;
		this.binding = binding;
		this.declaration = declaration;
	}


	public boolean isAspect() {
		return binding.scope.referenceContext instanceof AspectDeclaration;

	}
	
	public ResolvedTypeX getSuperclass() {
		if (binding.isInterface()) return getResolvedTypeX().getWorld().resolve(TypeX.OBJECT);
		//XXX what about java.lang.Object
		return eclipseWorld().fromEclipse(binding.superclass());
	}
	
	public ResolvedTypeX[] getDeclaredInterfaces() {
		return eclipseWorld().fromEclipse(binding.superInterfaces());
	}


	protected void fillDeclaredMembers() {
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
			
		this.declaredPointcuts = (ResolvedPointcutDefinition[])
			declaredPointcuts.toArray(new ResolvedPointcutDefinition[declaredPointcuts.size()]);
		this.declaredMethods = (ResolvedMember[])
			declaredMethods.toArray(new ResolvedMember[declaredMethods.size()]);
		this.declaredFields = (ResolvedMember[])
			declaredFields.toArray(new ResolvedMember[declaredFields.size()]);
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

	
	public int getModifiers() {
		// only return the real Java modifiers, not the extra eclipse ones
		return binding.modifiers & CompilerModifiers.AccJustFlag;
	}
	
	public String toString() {
		return "EclipseSourceType(" + new String(binding.sourceName()) + ")";
	}


	//XXX make sure this is applied to classes and interfaces
	public void checkPointcutDeclarations() {
		ResolvedMember[] pointcuts = getDeclaredPointcuts();
		boolean sawError = false;
		for (int i=0, len=pointcuts.length; i < len; i++) {
			if (pointcuts[i].isAbstract()) {
				if (!this.isAspect()) {
					eclipseWorld().showMessage(IMessage.ERROR,
						"abstract pointcut only allowed in aspect" + pointcuts[i].getName(),
						pointcuts[i].getSourceLocation(), null);
					sawError = true;
				} else if (!binding.isAbstract()) {
					eclipseWorld().showMessage(IMessage.ERROR,
						"abstract pointcut in concrete aspect" + pointcuts[i],
						pointcuts[i].getSourceLocation(), null);
					sawError = true;
				}
			}
				
			for (int j=i+1; j < len; j++) {
				if (pointcuts[i].getName().equals(pointcuts[j].getName())) {
					eclipseWorld().showMessage(IMessage.ERROR,
						"duplicate pointcut name: " + pointcuts[j].getName(),
						pointcuts[i].getSourceLocation(), pointcuts[j].getSourceLocation());
					sawError = true;
				}
			}
		}
		
		//now check all inherited pointcuts to be sure that they're handled reasonably
		if (sawError || !isAspect()) return;
		

		
		// find all pointcuts that override ones from super and check override is legal
		//    i.e. same signatures and greater or equal visibility
		// find all inherited abstract pointcuts and make sure they're concretized if I'm concrete
		// find all inherited pointcuts and make sure they don't conflict
		getResolvedTypeX().getExposedPointcuts();  //??? this is an odd construction

	}
	
	//???
//	public CrosscuttingMembers collectCrosscuttingMembers() {
//		return crosscuttingMembers;
//	}

	public ISourceLocation getSourceLocation() {
		TypeDeclaration dec = binding.scope.referenceContext;
		return new EclipseSourceLocation(dec.compilationResult, dec.sourceStart, dec.sourceEnd);
	}

	public boolean isInterface() {
		return binding.isInterface();
	}

	public PerClause getPerClause() {
		//should probably be: ((AspectDeclaration)declaration).perClause;
		// but we don't need this level of detail, and working with real per clauses
		// at this stage of compilation is not worth the trouble
		return new PerSingleton(); 
	}
	
	protected Collection getDeclares() {
		return declares;
	}

	protected Collection getPrivilegedAccesses() {
		return Collections.EMPTY_LIST;
	}

	protected Collection getTypeMungers() {
		return typeMungers;
	}

}
