/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseTypeMunger;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedFieldBinding;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedHandler;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;

/**
 * An inter-type field declaration.
 *
 * returnType encodes the type of the field selector encodes the name statements is null until resolution when it is filled in from
 * the initializer
 *
 * @author Jim Hugunin
 */
public class InterTypeFieldDeclaration extends InterTypeDeclaration {
	public Expression initialization;
	private TypeBinding realFieldType;

	// public InterTypeFieldBinding interBinding;

	public InterTypeFieldDeclaration(CompilationResult result, TypeReference onType) {
		super(result, onType);
	}

	public TypeBinding getRealFieldType() {
		return realFieldType;
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		// we don't have a body to parse
	}

	protected char[] getPrefix() {
		return (NameMangler.ITD_PREFIX + "interField$").toCharArray();
	}

	public void resolveOnType(ClassScope classScope) {
		super.resolveOnType(classScope);
		if (ignoreFurtherInvestigation) {
			return;
		}
		if (Modifier.isStatic(declaredModifiers) && onTypeBinding.isInterface()) {
			scope.problemReporter().signalError(sourceStart, sourceEnd, "static inter-type field on interface not supported");
			ignoreFurtherInvestigation = true;
		}
		if (Modifier.isStatic(declaredModifiers) && typeVariableAliases != null && typeVariableAliases.size() > 0
				&& onTypeBinding.isGenericType()) {
			scope.problemReporter().signalError(sourceStart, sourceEnd,
					"static intertype field declarations cannot refer to type variables from the target generic type");
		}

	}

	public void resolve(ClassScope upperScope) {
		if (munger == null) {
			ignoreFurtherInvestigation = true;
		}
		if (ignoreFurtherInvestigation) {
			return;
		}

		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(upperScope);
		ResolvedMember sig = munger.getSignature();
		UnresolvedType aspectType = world.fromBinding(upperScope.referenceContext.binding);

		if (sig.getReturnType().equals(UnresolvedType.VOID)
				|| (sig.getReturnType().isArray() && (sig.getReturnType().getComponentType().equals(UnresolvedType.VOID)))) {
			upperScope.problemReporter().signalError(sourceStart, sourceEnd, "field type can not be void");
		}

		//
		// System.err.println("sig: " + sig);
		// System.err.println("field: " + world.makeFieldBinding(
		// AjcMemberMaker.interFieldClassField(sig, aspectType)));

		if (initialization != null && initialization instanceof ArrayInitializer) {
			// System.err.println("got initializer: " + initialization);
			ArrayAllocationExpression aae = new ArrayAllocationExpression();
			aae.initializer = (ArrayInitializer) initialization;
			ArrayBinding arrayType = (ArrayBinding) world.makeTypeBinding(sig.getReturnType());
			aae.type = AstUtil.makeTypeReference(arrayType.leafComponentType());
			aae.sourceStart = initialization.sourceStart;
			aae.sourceEnd = initialization.sourceEnd;
			aae.dimensions = new Expression[arrayType.dimensions];
			initialization = aae;
		} /*
		 * else if (initialization!=null) { MethodScope initializationScope = this.scope; TypeBinding fieldType = realFieldType;
		 * TypeBinding initializationType; this.initialization.setExpectedType(fieldType); // needed in case of generic method
		 * invocation if (this.initialization instanceof ArrayInitializer) {
		 *
		 * if ((initializationType = this.initialization.resolveTypeExpecting(initializationScope, fieldType)) != null) {
		 * ((ArrayInitializer) this.initialization).binding = (ArrayBinding) initializationType;
		 * this.initialization.computeConversion(initializationScope, fieldType, initializationType); } } //
		 * System.err.println("i=>"+initialization); // System.err.println("sasuages=>"+initialization.resolvedType); //
		 * //initializationType = initialization.resolveType(initializationScope); //
		 * System.err.println("scope=>"+initializationScope);
		 *
		 * else if ((initializationType = this.initialization.resolveType(initializationScope)) != null) {
		 *
		 * if (fieldType != initializationType) // must call before computeConversion() and typeMismatchError()
		 * initializationScope.compilationUnitScope().recordTypeConversion(fieldType, initializationType); if
		 * (this.initialization.isConstantValueOfTypeAssignableToType(initializationType, fieldType) || (fieldType.isBaseType() &&
		 * BaseTypeBinding.isWidening(fieldType.id, initializationType.id)) || initializationType.isCompatibleWith(fieldType)) {
		 * initialization.computeConversion(initializationScope, fieldType, initializationType); if
		 * (initializationType.needsUncheckedConversion(fieldType)) {
		 * initializationScope.problemReporter().unsafeTypeConversion(this.initialization, initializationType, fieldType); } } else
		 * if (initializationScope.isBoxingCompatibleWith(initializationType, fieldType) || (initializationType.isBaseType() //
		 * narrowing then boxing ? && initializationScope.compilerOptions().sourceLevel >= JDK1_5 // autoboxing &&
		 * !fieldType.isBaseType() && initialization.isConstantValueOfTypeAssignableToType(initializationType,
		 * initializationScope.environment().computeBoxingType(fieldType)))) {
		 * this.initialization.computeConversion(initializationScope, fieldType, initializationType); } else {
		 * initializationScope.problemReporter().typeMismatchError(initializationType, fieldType, this); } // if
		 * (this.binding.isFinal()){ // cast from constant actual type to variable type //
		 * this.binding.setConstant(this.initialization.constant.castTo((this.binding.returnType.id << 4) +
		 * this.initialization.constant.typeID())); // } // } else { // this.binding.setConstant(NotAConstant); } // }
		 */

		// ////////////////////

		if (initialization == null) {
			this.statements = new Statement[] { new ReturnStatement(null, 0, 0), };
		} else if (!onTypeBinding.isInterface()) {
			MethodBinding writeMethod = world.makeMethodBinding(AjcMemberMaker.interFieldSetDispatcher(sig, aspectType),
					munger.getTypeVariableAliases());
			// For the body of an intertype field initalizer, generate a call to the inter field set dispatcher
			// method as that casts the shadow of a field set join point.
			if (Modifier.isStatic(declaredModifiers)) {
				this.statements = new Statement[] { new KnownMessageSend(writeMethod,
						AstUtil.makeNameReference(writeMethod.declaringClass), new Expression[] { initialization }), };
			} else {
				this.statements = new Statement[] { new KnownMessageSend(writeMethod,
						AstUtil.makeNameReference(writeMethod.declaringClass), new Expression[] {
								AstUtil.makeLocalVariableReference(arguments[0].binding), initialization }), };
			}
		} else {
			// XXX something is broken about this logic. Can we write to static interface fields?
			MethodBinding writeMethod = world.makeMethodBinding(
					AjcMemberMaker.interFieldInterfaceSetter(sig, sig.getDeclaringType().resolve(world.getWorld()), aspectType),
					munger.getTypeVariableAliases());
			if (Modifier.isStatic(declaredModifiers)) {
				this.statements = new Statement[] { new KnownMessageSend(writeMethod,
						AstUtil.makeNameReference(writeMethod.declaringClass), new Expression[] { initialization }), };
			} else {
				this.statements = new Statement[] { new KnownMessageSend(writeMethod,
						AstUtil.makeLocalVariableReference(arguments[0].binding), new Expression[] { initialization }), };
			}
		}

		super.resolve(upperScope);
	}

	public void setInitialization(Expression initialization) {
		this.initialization = initialization;

	}

	/*
	 * public void resolveStatements() { super.resolveStatements();
	 *
	 * // if (initialization!=null) { // MethodScope initializationScope = this.scope; // TypeBinding fieldType = realFieldType; //
	 * TypeBinding initializationType; // this.initialization.setExpectedType(fieldType); // needed in case of generic method
	 * invocation // if (this.initialization instanceof ArrayInitializer) { // // if ((initializationType =
	 * this.initialization.resolveTypeExpecting(initializationScope, fieldType)) != null) { // ((ArrayInitializer)
	 * this.initialization).binding = (ArrayBinding) initializationType; //
	 * this.initialization.computeConversion(initializationScope, fieldType, initializationType); // } // } ////
	 * System.err.println("i=>"+initialization); //// System.err.println("sasuages=>"+initialization.resolvedType); ////
	 * //initializationType = initialization.resolveType(initializationScope); ////
	 * System.err.println("scope=>"+initializationScope); // // else if ((initializationType =
	 * this.initialization.resolveType(initializationScope)) != null) { // // if (fieldType != initializationType) // must call
	 * before computeConversion() and typeMismatchError() //
	 * initializationScope.compilationUnitScope().recordTypeConversion(fieldType, initializationType); // if
	 * (this.initialization.isConstantValueOfTypeAssignableToType(initializationType, fieldType) // || (fieldType.isBaseType() &&
	 * BaseTypeBinding.isWidening(fieldType.id, initializationType.id)) // || initializationType.isCompatibleWith(fieldType)) { //
	 * initialization.computeConversion(initializationScope, fieldType, initializationType); // if
	 * (initializationType.needsUncheckedConversion(fieldType)) { //
	 * initializationScope.problemReporter().unsafeTypeConversion(this.initialization, initializationType, fieldType); // } // }
	 * else if (initializationScope.isBoxingCompatibleWith(initializationType, fieldType) // || (initializationType.isBaseType() //
	 * narrowing then boxing ? // && initializationScope.compilerOptions().sourceLevel >= JDK1_5 // autoboxing // &&
	 * !fieldType.isBaseType() // && initialization.isConstantValueOfTypeAssignableToType(initializationType,
	 * initializationScope.environment().computeBoxingType(fieldType)))) { //
	 * this.initialization.computeConversion(initializationScope, fieldType, initializationType); // } else { //
	 * initializationScope.problemReporter().typeMismatchError(initializationType, fieldType, this); // } // // if
	 * (this.binding.isFinal()){ // cast from constant actual type to variable type // //
	 * this.binding.setConstant(this.initialization.constant.castTo((this.binding.returnType.id << 4) +
	 * this.initialization.constant.typeID())); // // } // // } else { // // this.binding.setConstant(NotAConstant); // }}
	 *
	 * }
	 */
	public EclipseTypeMunger build(ClassScope classScope) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);
		resolveOnType(classScope);

		if (ignoreFurtherInvestigation) {
			return null;
		}

		binding = classScope.referenceContext.binding.resolveTypesFor(binding);
		if (ignoreFurtherInvestigation) {
			return null;
		}

		if (isTargetAnnotation(classScope, "field")) {
			return null; // Error message output in isTargetAnnotation
		}
		if (isTargetEnum(classScope, "field")) {
			return null; // Error message output in isTargetEnum
		}

		if (!Modifier.isStatic(declaredModifiers)) {
			super.binding.parameters = new TypeBinding[] { onTypeBinding, };
			this.arguments = new Argument[] { AstUtil.makeFinalArgument("ajc$this_".toCharArray(), onTypeBinding), };
		}

		// System.err.println("type: " + binding.returnType + ", " + returnType);
		ResolvedType declaringType = world.fromBinding(onTypeBinding).resolve(world.getWorld());
		if (declaringType.isRawType() || declaringType.isParameterizedType()) {
			declaringType = declaringType.getGenericType();
		}

		if (interTypeScope == null) {
			return null; // We encountered a problem building the scope, don't continue - error already reported
		}

		// Build a half correct resolvedmember (makeResolvedMember understands tvars) then build a fully correct sig from it
		ResolvedMember sigtemp = world.makeResolvedMemberForITD(binding, onTypeBinding, interTypeScope.getRecoveryAliases());
		UnresolvedType returnType = sigtemp.getReturnType();
		// if (returnType.isParameterizedType() || returnType.isGenericType()) returnType = returnType.getRawType();
		ResolvedMember sig = new ResolvedMemberImpl(Member.FIELD, declaringType, declaredModifiers, returnType, new String(
				declaredSelector), UnresolvedType.NONE);
		sig.setTypeVariables(sigtemp.getTypeVariables());

		NewFieldTypeMunger myMunger = new NewFieldTypeMunger(sig, null, typeVariableAliases);
		if (world.getItdVersion() == 1) {
			myMunger.version = NewFieldTypeMunger.VersionOne;
		}
		setMunger(myMunger);
		ResolvedType aspectType = world.fromEclipse(classScope.referenceContext.binding);
		ResolvedMember me = myMunger.getInitMethod(aspectType);
		this.selector = binding.selector = me.getName().toCharArray();
		this.realFieldType = this.binding.returnType;
		this.binding.returnType = TypeBinding.VOID;
		// ??? all other pieces should already match

		return new EclipseTypeMunger(world, myMunger, aspectType, this);
	}

	private AjAttribute makeAttribute() {
		return new AjAttribute.TypeMunger(munger);
	}

	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (ignoreFurtherInvestigation) {
			return;
		}

		classFile.extraAttributes.add(new EclipseAttributeAdapter(makeAttribute()));
		super.generateCode(classScope, classFile);
		generateDispatchMethods(classScope, classFile);
		// interBinding.reader.generateMethod(this, classScope, classFile);
		// interBinding.writer.generateMethod(this, classScope, classFile);
	}

	private void generateDispatchMethods(ClassScope classScope, ClassFile classFile) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);
		ResolvedMember sig = munger.getSignature();
		UnresolvedType aspectType = world.fromBinding(classScope.referenceContext.binding);
		generateDispatchMethod(world, sig, aspectType, classScope, classFile, true);
		generateDispatchMethod(world, sig, aspectType, classScope, classFile, false);
	}

	private void generateDispatchMethod(EclipseFactory world, ResolvedMember sig, UnresolvedType aspectType, ClassScope classScope,
			ClassFile classFile, boolean isGetter) {
		MethodBinding binding;
		if (isGetter) {
			binding = world.makeMethodBinding(AjcMemberMaker.interFieldGetDispatcher(sig, aspectType),
					munger.getTypeVariableAliases(), munger.getSignature().getDeclaringType());
		} else {
			binding = world.makeMethodBinding(AjcMemberMaker.interFieldSetDispatcher(sig, aspectType),
					munger.getTypeVariableAliases(), munger.getSignature().getDeclaringType());
		}
		classFile.generateMethodInfoHeader(binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttributes(binding, makeEffectiveSignatureAttribute(sig, isGetter ? Shadow.FieldGet : Shadow.FieldSet, false));
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);

		NewFieldTypeMunger fieldMunger = (NewFieldTypeMunger) munger;

		// Force use of version 1 if there is a field with that name on the type already
		if (world.getItdVersion() == 1) {
			fieldMunger.version = NewFieldTypeMunger.VersionOne;
		} else {
			if (!onTypeBinding.isInterface()) {
				FieldBinding[] existingFields = onTypeBinding.fields();
				for (FieldBinding fieldBinding : existingFields) {
					if (CharOperation.equals(fieldBinding.name, sig.getName().toCharArray())) {
						fieldMunger.version = NewFieldTypeMunger.VersionOne;
					}
				}
			}
		}

		FieldBinding classField = world.makeFieldBinding(
				AjcMemberMaker.interFieldClassField(sig, aspectType, fieldMunger.version == NewFieldTypeMunger.VersionTwo),
				munger.getTypeVariableAliases());

		codeStream.initializeMaxLocals(binding);
		if (isGetter) {
			if (onTypeBinding.isInterface()) {
				UnresolvedType declaringTX = sig.getDeclaringType();
				ResolvedType declaringRTX = world.getWorld().resolve(declaringTX, munger.getSourceLocation());
				MethodBinding readMethod = world.makeMethodBinding(
						AjcMemberMaker.interFieldInterfaceGetter(sig, declaringRTX, aspectType), munger.getTypeVariableAliases());
				generateInterfaceReadBody(binding, readMethod, codeStream);
			} else {
				generateClassReadBody(binding, classField, codeStream);
			}
		} else {
			if (onTypeBinding.isInterface()) {
				MethodBinding writeMethod = world.makeMethodBinding(
						AjcMemberMaker.interFieldInterfaceSetter(sig,
								world.getWorld().resolve(sig.getDeclaringType(), munger.getSourceLocation()), aspectType),
						munger.getTypeVariableAliases());
				generateInterfaceWriteBody(binding, writeMethod, codeStream);
			} else {
				generateClassWriteBody(binding, classField, codeStream);
			}
		}
		AstUtil.generateReturn(binding.returnType, codeStream);

		classFile.completeCodeAttribute(codeAttributeOffset,scope);
		attributeNumber++;
		classFile.completeMethodInfo(binding,methodAttributeOffset, attributeNumber);
	}

	private void generateInterfaceReadBody(MethodBinding binding, MethodBinding readMethod, CodeStream codeStream) {
		codeStream.aload_0();
		codeStream.invoke(Opcodes.OPC_invokeinterface,readMethod,null);
	}

	private void generateInterfaceWriteBody(MethodBinding binding, MethodBinding writeMethod, CodeStream codeStream) {
		codeStream.aload_0();
		codeStream.load(writeMethod.parameters[0], 1);
		codeStream.invoke(Opcodes.OPC_invokeinterface, writeMethod, null);
	}

	private void generateClassReadBody(MethodBinding binding, FieldBinding field, CodeStream codeStream) {
		if (field.isPrivate() || !field.canBeSeenBy(binding.declaringClass.fPackage)) {

			PrivilegedHandler handler = (PrivilegedHandler) Scope.findPrivilegedHandler(binding.declaringClass);
			if (handler == null) {
				// one is now required!
				ReferenceBinding typebinding = binding.declaringClass;
				if (typebinding instanceof ReferenceBinding) {
					SourceTypeBinding sourceBinding = (SourceTypeBinding) typebinding;
					handler = new PrivilegedHandler((AspectDeclaration) sourceBinding.scope.referenceContext);
					sourceBinding.privilegedHandler = handler;
				}
			}
			PrivilegedFieldBinding fBinding = (PrivilegedFieldBinding) handler.getPrivilegedAccessField(field, null);

			if (field.isStatic()) {
				codeStream.invoke(Opcodes.OPC_invokestatic,fBinding.reader,null);
			} else {
				codeStream.aload_0();
				codeStream.invoke(Opcodes.OPC_invokestatic,fBinding.reader,null);
			}
			return;
		}
		if (field.isStatic()) {
			codeStream.fieldAccess(Opcodes.OPC_getstatic,field,null);
		} else {
			codeStream.aload_0();
			codeStream.fieldAccess(Opcodes.OPC_getfield,field,null);
		}
	}

	private void generateClassWriteBody(MethodBinding binding, FieldBinding field, CodeStream codeStream) {
		if (field.isPrivate() || !field.canBeSeenBy(binding.declaringClass.fPackage)) {
			PrivilegedFieldBinding fBinding = (PrivilegedFieldBinding) Scope.findPrivilegedHandler(binding.declaringClass)
					.getPrivilegedAccessField(field, null);
			if (field.isStatic()) {
				codeStream.load(field.type, 0);
				codeStream.invoke(Opcodes.OPC_invokestatic,fBinding.writer,null);
			} else {
				codeStream.aload_0();
				codeStream.load(field.type, 1);
				codeStream.invoke(Opcodes.OPC_invokestatic,fBinding.writer,null);
			}
			return;
		}
		if (field.isStatic()) {
			codeStream.load(field.type, 0);
			codeStream.fieldAccess(Opcodes.OPC_putstatic,field,null);
		} else {
			codeStream.aload_0();
			codeStream.load(field.type, 1);
			codeStream.fieldAccess(Opcodes.OPC_putfield,field,null);
		}
	}

	protected Shadow.Kind getShadowKindForBody() {
		return null;
	}

}
