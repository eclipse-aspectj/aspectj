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


package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.weaver.*;
import org.aspectj.weaver.Shadow.Kind;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;


public class InterTypeMethodDeclaration extends InterTypeDeclaration {
	public InterTypeMethodDeclaration(CompilationResult result, TypeReference onType) {
		super(result, onType);
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		if (ignoreFurtherInvestigation)
			return;
		if (!Modifier.isAbstract(declaredModifiers)) {
			parser.parse(this, unit);
		}
	}
	

	public void analyseCode(
		ClassScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo)
	{
		if (Modifier.isAbstract(declaredModifiers)) return;
		
		super.analyseCode(currentScope, flowContext, flowInfo);
	}
	
	public void resolve(ClassScope upperScope) {
		if (munger == null) ignoreFurtherInvestigation = true;
		if (ignoreFurtherInvestigation) return;
		
		if (!Modifier.isStatic(declaredModifiers)) {
			this.arguments = AstUtil.insert(
				AstUtil.makeFinalArgument("ajc$this_".toCharArray(), onTypeBinding),
				this.arguments);
			binding.parameters  = AstUtil.insert(onTypeBinding, binding.parameters);
		}
			
		super.resolve(upperScope);
	}
	public void resolveStatements(ClassScope upperScope) {
		if (!Modifier.isAbstract(declaredModifiers)) super.resolveStatements(upperScope);
	}
	
	

	public void build(ClassScope classScope, CrosscuttingMembers xcut) {
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);
		
		resolveOnType(classScope);
		if (ignoreFurtherInvestigation) return;
		
		binding = classScope.referenceContext.binding.resolveTypesFor(binding);
		ResolvedMember sig = new ResolvedMember(Member.METHOD, EclipseWorld.fromBinding(onTypeBinding),
			declaredModifiers, EclipseWorld.fromBinding(binding.returnType), new String(declaredSelector),
			EclipseWorld.fromBindings(binding.parameters));
		
		NewMethodTypeMunger myMunger = new NewMethodTypeMunger(sig, null);
		this.munger = myMunger;
		ResolvedTypeX aspectType = world.fromEclipse(classScope.referenceContext.binding);
		ResolvedMember me =
			myMunger.getDispatchMethod(aspectType);
		this.selector = binding.selector = me.getName().toCharArray();
		
		xcut.addTypeMunger(new EclipseTypeMunger(myMunger, aspectType, this));
	}
	
	
	private AjAttribute makeAttribute() {
		return new AjAttribute.TypeMunger(munger);
	}
	
	
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (ignoreFurtherInvestigation) {
			System.err.println("no code for " + this);
			return;
		}
		
		classFile.extraAttributes.add(new EclipseAttributeAdapter(makeAttribute()));
		
		if (!Modifier.isAbstract(declaredModifiers)) {
			super.generateCode(classScope, classFile);
		}
		
		generateDispatchMethod(classScope, classFile);
	}
	
	public void generateDispatchMethod(ClassScope classScope, ClassFile classFile) {
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);
		
		TypeX aspectType = EclipseWorld.fromBinding(classScope.referenceContext.binding);
		ResolvedMember signature = munger.getSignature();
		
		ResolvedMember dispatchMember = 
			AjcMemberMaker.interMethodDispatcher(signature, aspectType);
		MethodBinding dispatchBinding = world.makeMethodBinding(dispatchMember);
		MethodBinding introducedMethod = 
			world.makeMethodBinding(AjcMemberMaker.interMethod(signature, aspectType, onTypeBinding.isInterface()));
		
		classFile.generateMethodInfoHeader(dispatchBinding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttribute(dispatchBinding, 
				makeEffectiveSignatureAttribute(signature, Shadow.MethodCall, false));
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);
		
		codeStream.initializeMaxLocals(dispatchBinding);
		
		MethodBinding methodBinding = introducedMethod;
		TypeBinding[] parameters = methodBinding.parameters;
		int length = parameters.length;
		int resolvedPosition;
		if (methodBinding.isStatic())
			resolvedPosition = 0;
		else {
			codeStream.aload_0();
			resolvedPosition = 1;
		}
		for (int i = 0; i < length; i++) {
			codeStream.load(parameters[i], resolvedPosition);
			if ((parameters[i] == DoubleBinding) || (parameters[i] == LongBinding))
				resolvedPosition += 2;
			else
				resolvedPosition++;
		}
		TypeBinding type;
		if (methodBinding.isStatic())
			codeStream.invokestatic(methodBinding);
		else {
			if (methodBinding.declaringClass.isInterface()){
				codeStream.invokeinterface(methodBinding);
			} else {
				codeStream.invokevirtual(methodBinding);
			}
		}
		AstUtil.generateReturn(dispatchBinding.returnType, codeStream);

		classFile.completeCodeAttribute(codeAttributeOffset);
		attributeNumber++;
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	
	protected Shadow.Kind getShadowKindForBody() {
		return Shadow.MethodExecution;
	}
}
