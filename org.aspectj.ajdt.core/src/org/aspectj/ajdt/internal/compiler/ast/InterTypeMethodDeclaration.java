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


package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseTypeMunger;
import org.aspectj.weaver.*;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
//import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/**
 * An inter-type method declaration.
 *
 * @author Jim Hugunin
 */
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
		InitializationFlowContext flowContext,
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
	public void resolveStatements() {
        if ((modifiers & AccSemicolonBody) != 0) {
            if ((declaredModifiers & AccAbstract) == 0)
                scope.problemReporter().methodNeedingAbstractModifier(this);
        } else {
            // the method HAS a body --> abstract native modifiers are forbiden
            if (((declaredModifiers & AccAbstract) != 0))
                scope.problemReporter().methodNeedingNoBody(this);
        }        
        
        
		if (!Modifier.isAbstract(declaredModifiers)) super.resolveStatements();
		if (Modifier.isStatic(declaredModifiers)) {
			// Check the target for ITD is not an interface
			if (onTypeBinding.isInterface()) {
				scope.problemReporter().signalError(sourceStart, sourceEnd,
					"methods in interfaces cannot be declared static");
			}
		}
	}
	
	

	public EclipseTypeMunger build(ClassScope classScope) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);
		
		resolveOnType(classScope);
		if (ignoreFurtherInvestigation) return null;
		
		binding = classScope.referenceContext.binding.resolveTypesFor(binding);
		if (binding == null) {
			// if binding is null, we failed to find a type used in the method params, this error
			// has already been reported. 
			throw new AbortCompilation();
		}
		ResolvedMember sig = new ResolvedMember(Member.METHOD, EclipseFactory.fromBinding(onTypeBinding),
			declaredModifiers, EclipseFactory.fromBinding(binding.returnType), new String(declaredSelector),
			EclipseFactory.fromBindings(binding.parameters),
			world.fromEclipse(binding.thrownExceptions));
		
		NewMethodTypeMunger myMunger = new NewMethodTypeMunger(sig, null);
		setMunger(myMunger);
		ResolvedTypeX aspectType = world.fromEclipse(classScope.referenceContext.binding);
		ResolvedMember me =
			myMunger.getDispatchMethod(aspectType);
		this.selector = binding.selector = me.getName().toCharArray();
		
		return new EclipseTypeMunger(world, myMunger, aspectType, this);
	}
	
	
	private AjAttribute makeAttribute() {
		return new AjAttribute.TypeMunger(munger);
	}
	
	
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (ignoreFurtherInvestigation) {
			//System.err.println("no code for " + this);
			return;
		}
		
		classFile.extraAttributes.add(new EclipseAttributeAdapter(makeAttribute()));
		
		if (!Modifier.isAbstract(declaredModifiers)) {
			super.generateCode(classScope, classFile);
		}
		
		generateDispatchMethod(classScope, classFile);
	}
	
	public void generateDispatchMethod(ClassScope classScope, ClassFile classFile) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);
		
		TypeX aspectType = EclipseFactory.fromBinding(classScope.referenceContext.binding);
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
//		TypeBinding type;
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
