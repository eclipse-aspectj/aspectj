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
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;

/**
 * returnType encodes the type of the field
 * selector encodes the name
 */
public class InterTypeFieldDeclaration extends InterTypeDeclaration {
	public Expression initialization;
	//public InterTypeFieldBinding interBinding;
	
	public InterTypeFieldDeclaration(CompilationResult result, TypeReference onType) {
		super(result, onType);
	}
	
	
	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		//we don't have a body to parse
	}
	
	
	public void resolveOnType(ClassScope classScope) {
		super.resolveOnType(classScope);
		if (ignoreFurtherInvestigation) return;
		if (Modifier.isStatic(declaredModifiers) && onTypeBinding.isInterface()) {
			scope.problemReporter().signalError(sourceStart, sourceEnd,
				"static inter-type field on interface not supported");
			ignoreFurtherInvestigation = true;
		}
	}
		
	
	public void resolve(ClassScope upperScope) {
		if (munger == null) ignoreFurtherInvestigation = true;
		if (ignoreFurtherInvestigation) return;
		
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(upperScope);
		ResolvedMember sig = munger.getSignature();
		TypeX aspectType = EclipseWorld.fromBinding(upperScope.referenceContext.binding);
		
		if (initialization == null) {
			this.statements = new Statement[] {
				new ReturnStatement(null, 0, 0),
			};
		} else if (!onTypeBinding.isInterface()) {
			
			FieldBinding interField = world.makeFieldBinding(
				AjcMemberMaker.interFieldClassField(sig, aspectType));
			Reference ref = new KnownFieldReference(interField, 0);
			this.statements = new Statement[] {
				new Assignment(ref, initialization, initialization.sourceEnd),
			};
		} else {
			//XXX something is broken about this logic.  Can we write to static interface fields?
			MethodBinding writeMethod = world.makeMethodBinding(
				AjcMemberMaker.interFieldInterfaceSetter(sig, sig.getDeclaringType().resolve(world), aspectType));
			if (Modifier.isStatic(declaredModifiers)) {
				this.statements = new Statement[] {
					new KnownMessageSend(writeMethod, 
							AstUtil.makeNameReference(writeMethod.declaringClass),
							new Expression[] {initialization}),
				};
			} else {				
				this.statements = new Statement[] {
					new KnownMessageSend(writeMethod, 
							AstUtil.makeLocalVariableReference(arguments[0].binding),
							new Expression[] {initialization}),
				};
			}
		}
		
		super.resolve(upperScope);
	}
	
	
	public void setInitialization(Expression initialization) {
		this.initialization = initialization;

	}
	
	public void build(ClassScope classScope, CrosscuttingMembers xcut) {
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);
		resolveOnType(classScope);
		
		binding = classScope.referenceContext.binding.resolveTypesFor(binding);
		if (ignoreFurtherInvestigation) return;
		
		if (!Modifier.isStatic(declaredModifiers)) {
			super.binding.parameters = new TypeBinding[] {
				onTypeBinding,
			};
			this.arguments = new Argument[] {
				AstUtil.makeFinalArgument("ajc$this_".toCharArray(), onTypeBinding),
			};
		}
		
		ResolvedMember sig =
			new ResolvedMember(Member.FIELD, EclipseWorld.fromBinding(onTypeBinding),
					declaredModifiers, EclipseWorld.fromBinding(binding.returnType),
					new String(declaredSelector), TypeX.NONE);
		
		NewFieldTypeMunger myMunger = new NewFieldTypeMunger(sig, null);
		this.munger = myMunger;
		ResolvedTypeX aspectType = world.fromEclipse(classScope.referenceContext.binding);
		ResolvedMember me = 
			myMunger.getInitMethod(aspectType);
		this.selector = binding.selector = me.getName().toCharArray();
		this.binding.returnType = TypeBinding.VoidBinding;
		//??? all other pieces should already match
		
		xcut.addTypeMunger(new EclipseTypeMunger(myMunger, aspectType, this));
		
//		interBinding = new InterTypeFieldBinding(name, type, declaredModifiers,
//		    declaringClass, constant, withinType);	
		
//		//XXX handle problem bindings
//		TypeBinding type = returnType.getTypeBinding(classScope);
//		char[] name = selector;
//		super.binding.returnType = type;
//		
//		if (ignoreFurtherInvestigation) return;
//		
//		
//		ReferenceBinding withinType = classScope.referenceContext.binding;
//		
//		this.returnType.binding = super.binding.returnType = TypeBinding.VoidBinding;
//		//this.modifiers = super.binding.modifiers = AccStatic | AccPublic; 
//		
//		TypeX aspectTypeX = EclipseWorld.fromBinding(withinType);
//		TypeX onTypeX = EclipseWorld.fromBinding(declaringClass);
//		
//		this.selector =
//			NameMangler.interFieldInitializer(aspectTypeX, onTypeX, new String(selector)).toCharArray();
//		
//		super.binding.selector = this.selector;
//		Constant constant = Constant.NotAConstant;
//		//XXX this is not completely correct, it will miss all constants
////		if (initialization instanceof Literal) {
////			((Literal)initialization).computeConstant();
////			constant = initialization.constant;
////		}
		
	
		
		
		

	}	
	
	
	private AjAttribute makeAttribute() {
		return new AjAttribute.TypeMunger(munger);
	}
	
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (ignoreFurtherInvestigation) return;
		
		classFile.extraAttributes.add(new EclipseAttributeAdapter(makeAttribute()));
		super.generateCode(classScope, classFile);
		generateDispatchMethods(classScope, classFile);
//		interBinding.reader.generateMethod(this, classScope, classFile);
//		interBinding.writer.generateMethod(this, classScope, classFile);
	}

	private void generateDispatchMethods(ClassScope classScope, ClassFile classFile) {
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);
		ResolvedMember sig = munger.getSignature();
		TypeX aspectType = EclipseWorld.fromBinding(classScope.referenceContext.binding);
		generateDispatchMethod(world, sig, aspectType, classScope, classFile, true);
		generateDispatchMethod(world, sig, aspectType, classScope, classFile, false);
	}

	private void generateDispatchMethod(
		EclipseWorld world,
		ResolvedMember sig,
		TypeX aspectType,
		ClassScope classScope,
		ClassFile classFile,
		boolean isGetter)
	{
		MethodBinding binding;
		if (isGetter) {
			binding = world.makeMethodBinding(AjcMemberMaker.interFieldGetDispatcher(sig, aspectType));
		} else {
			binding = world.makeMethodBinding(AjcMemberMaker.interFieldSetDispatcher(sig, aspectType));
		}
		classFile.generateMethodInfoHeader(binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttribute(binding, 
				makeEffectiveSignatureAttribute(sig, isGetter ? Shadow.FieldGet : Shadow.FieldSet, false));
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);
		
		FieldBinding classField = world.makeFieldBinding(
			AjcMemberMaker.interFieldClassField(sig, aspectType));
		
		codeStream.initializeMaxLocals(binding);
		if (isGetter) {
			if (onTypeBinding.isInterface()) {
				MethodBinding readMethod = world.makeMethodBinding(
					AjcMemberMaker.interFieldInterfaceGetter(
						sig, world.resolve(sig.getDeclaringType()), aspectType));
				generateInterfaceReadBody(binding, readMethod, codeStream);
			} else {
				generateClassReadBody(binding, classField, codeStream);
			}
		} else {
			if (onTypeBinding.isInterface()) {
				MethodBinding writeMethod = world.makeMethodBinding(
					AjcMemberMaker.interFieldInterfaceSetter(
						sig, world.resolve(sig.getDeclaringType()), aspectType));
				generateInterfaceWriteBody(binding, writeMethod, codeStream);
			} else {
				generateClassWriteBody(binding, classField, codeStream);
			}
		}
		AstUtil.generateReturn(binding.returnType, codeStream);

		classFile.completeCodeAttribute(codeAttributeOffset);
		attributeNumber++;
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	private void generateInterfaceReadBody(MethodBinding binding, MethodBinding readMethod, CodeStream codeStream) {
		codeStream.aload_0();
		codeStream.invokeinterface(readMethod);
	}
	
	
	private void generateInterfaceWriteBody(MethodBinding binding, MethodBinding writeMethod, CodeStream codeStream) {
		codeStream.aload_0();
		codeStream.load(writeMethod.parameters[0], 1);
		codeStream.invokeinterface(writeMethod);
	}



	private void generateClassReadBody(MethodBinding binding, FieldBinding field, CodeStream codeStream) {
		if (field.isStatic()) {
			codeStream.getstatic(field);
		} else {
			codeStream.aload_0();
			codeStream.getfield(field);
		}
	}

	private void generateClassWriteBody(MethodBinding binding, FieldBinding field, CodeStream codeStream) {
		if (field.isStatic()) {
			codeStream.load(field.type, 0);
			codeStream.putstatic(field);
		} else {
			codeStream.aload_0();
			codeStream.load(field.type, 1);
			codeStream.putfield(field);
		}
	}
	
	protected Shadow.Kind getShadowKindForBody() {
		return null;
	}
}
