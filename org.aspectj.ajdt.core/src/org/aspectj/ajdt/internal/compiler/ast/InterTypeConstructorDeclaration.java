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

import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.weaver.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;


public class InterTypeConstructorDeclaration extends InterTypeDeclaration {	
	private MethodDeclaration preMethod;
	private ExplicitConstructorCall explicitConstructorCall = null;
	
	public InterTypeConstructorDeclaration(CompilationResult result, TypeReference onType) {
		super(result, onType);
	}
	
	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		if (ignoreFurtherInvestigation)
			return;
	    parser.parseAsConstructor(this, unit);
	}

	public void resolve(ClassScope upperScope) {
		if (munger == null || binding == null) ignoreFurtherInvestigation = true;
		if (ignoreFurtherInvestigation) return;

		explicitConstructorCall = null;
		if (statements != null && statements.length > 0 && 
			statements[0] instanceof ExplicitConstructorCall)
		{
			explicitConstructorCall = (ExplicitConstructorCall) statements[0];
			statements = AstUtil.remove(0, statements);
		}
		
		preMethod = makePreMethod(upperScope, explicitConstructorCall);
		
		binding.parameters  = AstUtil.insert(onTypeBinding, binding.parameters);
		this.arguments = AstUtil.insert(
			AstUtil.makeFinalArgument("ajc$this_".toCharArray(), onTypeBinding),
			this.arguments);
			
		super.resolve(upperScope);
	}

	private MethodDeclaration makePreMethod(ClassScope scope, 
											ExplicitConstructorCall explicitConstructorCall)
	{
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(scope);
		
		TypeX aspectTypeX = EclipseWorld.fromBinding(binding.declaringClass);
		TypeX targetTypeX = EclipseWorld.fromBinding(onTypeBinding);
		
		ArrayBinding objectArrayBinding = scope.createArray(scope.getJavaLangObject(), 1);
		
		
		MethodDeclaration pre = new MethodDeclaration(compilationResult);
		pre.modifiers = AccPublic | AccStatic;
		pre.returnType = AstUtil.makeTypeReference(objectArrayBinding);
		pre.selector = NameMangler.postIntroducedConstructor(aspectTypeX, targetTypeX).toCharArray();
		
		
		pre.arguments = AstUtil.copyArguments(this.arguments);
		
		//XXX should do exceptions
		
		pre.scope = new MethodScope(scope, pre, true);
		//??? do we need to do anything with scope???
		
		pre.binding = world.makeMethodBinding(
			AjcMemberMaker.preIntroducedConstructor(aspectTypeX, targetTypeX, 
					world.fromBindings(binding.parameters)));
		
		pre.bindArguments();
		pre.bindThrownExceptions();
		
		
		if (explicitConstructorCall == null) {
			pre.statements = new Statement[] {};
		} else {
			pre.statements = new Statement[] {
				explicitConstructorCall
			};
		}
		
		InterTypeScope newParent =
			new InterTypeScope(scope, onTypeBinding);
		pre.scope.parent = newParent;

		pre.resolveStatements(newParent);
		
		
		
		int nParams = pre.arguments.length;
		MethodBinding explicitConstructor = null;
		if (explicitConstructorCall != null) {
			explicitConstructor = explicitConstructorCall.binding;
			if (explicitConstructor.alwaysNeedsAccessMethod()) {
				explicitConstructor = explicitConstructor.getAccessMethod();
			}
		}
		
		int nExprs;
		if (explicitConstructor == null) nExprs = 0;
		else nExprs = explicitConstructor.parameters.length;
		
		
		ArrayInitializer init = new ArrayInitializer();
		init.expressions = new Expression[nExprs + nParams];
		int index = 0;
		for (int i=0; i < nExprs; i++) {
			if (i >= explicitConstructorCall.arguments.length) {
				init.expressions[index++] = new NullLiteral(0, 0);
				continue;
			}
			
			
			Expression arg = explicitConstructorCall.arguments[i];
			ResolvedMember conversionMethod = 
				AjcMemberMaker.toObjectConversionMethod(world.fromBinding(explicitConstructorCall.binding.parameters[i]));
			if (conversionMethod != null) {
				arg = new KnownMessageSend(world.makeMethodBindingForCall(conversionMethod),
					new CastExpression(new NullLiteral(0, 0), 
						AstUtil.makeTypeReference(world.makeTypeBinding(AjcMemberMaker.CONVERSIONS_TYPE))),
				    new Expression[] {arg });
			}
			init.expressions[index++] = arg;
		}
		
		for (int i=0; i < nParams; i++) {
			LocalVariableBinding binding = pre.arguments[i].binding;
			Expression arg = AstUtil.makeResolvedLocalVariableReference(binding);
			ResolvedMember conversionMethod = 
				AjcMemberMaker.toObjectConversionMethod(world.fromBinding(binding.type));
			if (conversionMethod != null) {
				arg = new KnownMessageSend(world.makeMethodBindingForCall(conversionMethod),
					new CastExpression(new NullLiteral(0, 0), 
						AstUtil.makeTypeReference(world.makeTypeBinding(AjcMemberMaker.CONVERSIONS_TYPE))),
				    new Expression[] {arg });
			}
			init.expressions[index++] = arg;
		}
		
		init.binding =objectArrayBinding;
		
		ArrayAllocationExpression newArray = new ArrayAllocationExpression();
		newArray.initializer = init;
		newArray.type = AstUtil.makeTypeReference(scope.getJavaLangObject());
		newArray.dimensions = new Expression[1];
		newArray.constant = NotAConstant;
		

		
		
		pre.statements = new Statement[] {
			new ReturnStatement(newArray, 0, 0),
		};
		return pre;
	}




	public void build(ClassScope classScope, CrosscuttingMembers xcut) {
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);

		binding = classScope.referenceContext.binding.resolveTypesFor(binding);
		
		resolveOnType(classScope);
		if (ignoreFurtherInvestigation) return;
		
		
		if (onTypeBinding.isInterface()) {
			ignoreFurtherInvestigation = true;
			return;
		}
		
		if (onTypeBinding.isNestedType()) {
			classScope.problemReporter().signalError(sourceStart, sourceEnd,
				"can't define constructors on nested types (compiler limitation)");
			ignoreFurtherInvestigation = true;
			return;
		}	
		
		ResolvedTypeX declaringTypeX = world.fromEclipse(onTypeBinding);
		ResolvedTypeX aspectType = world.fromEclipse(classScope.referenceContext.binding);
		
		ResolvedMember bindingAsMember = world.makeResolvedMember(binding);
		
		ResolvedMember signature =
			new ResolvedMember(Member.CONSTRUCTOR, declaringTypeX, declaredModifiers, 
					ResolvedTypeX.VOID, "<init>", bindingAsMember.getParameterTypes());
					
		ResolvedMember syntheticInterMember =
			AjcMemberMaker.interConstructor(declaringTypeX,  signature, aspectType);
		
		NewConstructorTypeMunger myMunger = 
			new NewConstructorTypeMunger(signature, syntheticInterMember, null, null);
		this.munger = myMunger;
		
		this.selector = binding.selector =
			NameMangler.postIntroducedConstructor(
				EclipseWorld.fromBinding(binding.declaringClass),
				declaringTypeX).toCharArray();
		
		xcut.addTypeMunger(new EclipseTypeMunger(myMunger, aspectType, this));
	}
	
	
	private AjAttribute makeAttribute(EclipseWorld world) {
		if (explicitConstructorCall != null && !(explicitConstructorCall.binding instanceof ProblemMethodBinding)) {
			MethodBinding explicitConstructor = explicitConstructorCall.binding;
			if (explicitConstructor.alwaysNeedsAccessMethod()) {
				explicitConstructor = explicitConstructor.getAccessMethod();
			}
			
			
			((NewConstructorTypeMunger)munger).setExplicitConstructor(
				world.makeResolvedMember(explicitConstructor));
		} else {
			((NewConstructorTypeMunger)munger).setExplicitConstructor(
				new ResolvedMember(Member.CONSTRUCTOR, 
					EclipseWorld.fromBinding(onTypeBinding.superclass()),
					0, ResolvedTypeX.VOID, "<init>", TypeX.NONE));
		}
		return new AjAttribute.TypeMunger(munger);
	}
	
	
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (ignoreFurtherInvestigation) return;
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);
		classFile.extraAttributes.add(new EclipseAttributeAdapter(makeAttribute(world)));
		super.generateCode(classScope, classFile);
		
		preMethod.generateCode(classScope, classFile);
	}
	protected Shadow.Kind getShadowKindForBody() {
		return Shadow.ConstructorExecution;
	}

}
