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

import java.lang.reflect.Modifier;


public class AjcMemberMaker {
	private static final int PUBLIC_STATIC_FINAL =
		Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

	private static final int PRIVATE_STATIC =
		Modifier.PRIVATE | Modifier.STATIC;

	private static final int PUBLIC_STATIC =
		Modifier.PUBLIC | Modifier.STATIC;

	private static final int VISIBILITY =
		Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;

	public static final TypeX CFLOW_STACK_TYPE = 
		TypeX.forName(NameMangler.CFLOW_STACK_TYPE);
	public static final TypeX AROUND_CLOSURE_TYPE = 
		TypeX.forName("org.aspectj.runtime.internal.AroundClosure");
		
	public static final TypeX CONVERSIONS_TYPE =
		TypeX.forName("org.aspectj.runtime.internal.Conversions");
		
	public static final TypeX NO_ASPECT_BOUND_EXCEPTION =
		TypeX.forName("org.aspectj.lang.NoAspectBoundException");

	public static ResolvedMember ajcPreClinitMethod(TypeX declaringType) {
		return new ResolvedMember(
			Member.METHOD, 
			declaringType,
			PRIVATE_STATIC,
			NameMangler.AJC_PRE_CLINIT_NAME,
			"()V");
	}

	public static ResolvedMember ajcPostClinitMethod(TypeX declaringType) {
		return new ResolvedMember(
			Member.METHOD, 
			declaringType,
			PRIVATE_STATIC,
			NameMangler.AJC_POST_CLINIT_NAME,
			"()V");
	}

	public static Member noAspectBoundExceptionInit() {
		return new ResolvedMember(
			Member.METHOD,
			NO_ASPECT_BOUND_EXCEPTION,
			Modifier.PUBLIC,
			"<init>",
			"()V");
	}


	public static ResolvedMember perCflowPush(TypeX declaringType) {
		return new ResolvedMember(
			Member.METHOD, 
			declaringType,
			PUBLIC_STATIC,
			NameMangler.PERCFLOW_PUSH_METHOD,
			"()V");
	}
	
	public static ResolvedMember perCflowField(TypeX declaringType) {
		return new ResolvedMember(
			Member.FIELD, 
			declaringType,
			PUBLIC_STATIC_FINAL,
			NameMangler.PERCFLOW_FIELD_NAME,
			CFLOW_STACK_TYPE.getSignature());
	}

	public static ResolvedMember perSingletonField(TypeX declaringType) {
		return new ResolvedMember(
			Member.FIELD, 
			declaringType,
			PUBLIC_STATIC_FINAL,
			NameMangler.PERSINGLETON_FIELD_NAME,
			declaringType.getSignature());
	}
	
	
	public static ResolvedMember perObjectField(TypeX declaringType, ResolvedTypeX aspectType) {
		int modifiers = Modifier.PRIVATE;
		if (!TypeX.SERIALIZABLE.isAssignableFrom(aspectType, aspectType.getWorld())) {
			modifiers |= Modifier.TRANSIENT;
		}
		return new ResolvedMember(
			Member.FIELD, 
			declaringType,
			modifiers,
			aspectType,
			NameMangler.perObjectInterfaceField(aspectType),
			TypeX.NONE);
	}

	
	public static ResolvedMember perObjectBind(TypeX declaringType) {
		return new ResolvedMember(
			Member.METHOD, 
			declaringType,
			PUBLIC_STATIC,
			NameMangler.PEROBJECT_BIND_METHOD,
			"(Ljava/lang/Object;)V");
	}


	public static TypeX perObjectInterfaceType(TypeX aspectType) {
		return TypeX.forName(aspectType.getName()+"$ajcMightHaveAspect");
	}

	public static ResolvedMember perObjectInterfaceGet(TypeX aspectType) {
		return new ResolvedMember(
			Member.METHOD, 
			perObjectInterfaceType(aspectType),
			Modifier.PUBLIC | Modifier.ABSTRACT,
			NameMangler.perObjectInterfaceGet(aspectType),
			"()" + aspectType.getSignature());
	}

	public static ResolvedMember perObjectInterfaceSet(TypeX aspectType) {
		return new ResolvedMember(
			Member.METHOD, 
			perObjectInterfaceType(aspectType),
			Modifier.PUBLIC | Modifier.ABSTRACT,
			NameMangler.perObjectInterfaceSet(aspectType),
			"(" + aspectType.getSignature() + ")V");
	}
	
	
	
	
	public static ResolvedMember perSingletonAspectOfMethod(TypeX declaringType) {
		return new ResolvedMember(Member.METHOD,
			declaringType, PUBLIC_STATIC, "aspectOf", 
			"()" + declaringType.getSignature());		
	}
	
	public static ResolvedMember perSingletonHasAspectMethod(TypeX declaringType) {
		return new ResolvedMember(Member.METHOD,
			declaringType, PUBLIC_STATIC, "hasAspect", 
			"()Z");		
	};
	
	public static ResolvedMember perCflowAspectOfMethod(TypeX declaringType) {
		return perSingletonAspectOfMethod(declaringType);
	}
	
	public static ResolvedMember perCflowHasAspectMethod(TypeX declaringType) {
		return perSingletonHasAspectMethod(declaringType);
	};
	
	public static ResolvedMember perObjectAspectOfMethod(TypeX declaringType) {
		return new ResolvedMember(Member.METHOD,
			declaringType, PUBLIC_STATIC, "aspectOf", 
			"(Ljava/lang/Object;)" + declaringType.getSignature());		
	}
	
	public static ResolvedMember perObjectHasAspectMethod(TypeX declaringType) {
		return new ResolvedMember(Member.METHOD,
			declaringType, PUBLIC_STATIC, "hasAspect", 
			"(Ljava/lang/Object;)Z");		
	};
	
	// -- privileged accessors
	
	public static ResolvedMember privilegedAccessMethodForMethod(TypeX aspectType, ResolvedMember method) {
		String sig;
		sig = method.getSignature();
		return new ResolvedMember(Member.METHOD,
			method.getDeclaringType(),
			Modifier.PUBLIC | (method.isStatic() ? Modifier.STATIC : 0),
			NameMangler.privilegedAccessMethodForMethod(method.getName(),
												method.getDeclaringType(), aspectType),
			sig);
			//XXX needs thrown exceptions to be correct
	}
	
	public static ResolvedMember privilegedAccessMethodForFieldGet(TypeX aspectType, Member field) {
		String sig;
		if (field.isStatic()) {
			sig = "()" + field.getReturnType().getSignature();
		} else {
			sig = "(" + field.getDeclaringType().getSignature() + ")" + field.getReturnType().getSignature();
		}
		
		return new ResolvedMember(Member.METHOD,
			field.getDeclaringType(),
			PUBLIC_STATIC, //Modifier.PUBLIC | (field.isStatic() ? Modifier.STATIC : 0),
			NameMangler.privilegedAccessMethodForFieldGet(field.getName(),
												field.getDeclaringType(), aspectType), 
			sig);
	}
	
	public static ResolvedMember privilegedAccessMethodForFieldSet(TypeX aspectType, Member field) {
		String sig;
		if (field.isStatic()) {
			sig = "(" + field.getReturnType().getSignature() + ")V";
		} else {
			sig = "(" + field.getDeclaringType().getSignature() + field.getReturnType().getSignature() + ")V";
		}
		
		return new ResolvedMember(Member.METHOD,
			field.getDeclaringType(),
			PUBLIC_STATIC, //Modifier.PUBLIC | (field.isStatic() ? Modifier.STATIC : 0),
			NameMangler.privilegedAccessMethodForFieldSet(field.getName(),
												field.getDeclaringType(), aspectType), 
			sig);
	}
	
	// --- inline accessors
	//??? can eclipse handle a transform this weird without putting synthetics into the mix
	public static ResolvedMember superAccessMethod(TypeX baseType, ResolvedMember method) {
		return new ResolvedMember(Member.METHOD,
			baseType,
			Modifier.PUBLIC,
			method.getReturnType(),
			NameMangler.superDispatchMethod(baseType, method.getName()),
			method.getParameterTypes());
			//XXX needs thrown exceptions to be correct
	}
	
	public static ResolvedMember inlineAccessMethodForMethod(TypeX aspectType, ResolvedMember method) {
		TypeX[] paramTypes = method.getParameterTypes();
		if (!method.isStatic()) {
			paramTypes = TypeX.insert(method.getDeclaringType(), paramTypes);
		}
		return new ResolvedMember(Member.METHOD,
			aspectType,
			PUBLIC_STATIC, //??? what about privileged and super access
						   //???Modifier.PUBLIC | (method.isStatic() ? Modifier.STATIC : 0),
			method.getReturnType(),
			
			NameMangler.inlineAccessMethodForMethod(method.getName(),
												method.getDeclaringType(), aspectType),
			paramTypes);
			//XXX needs thrown exceptions to be correct
	}
	
	public static ResolvedMember inlineAccessMethodForFieldGet(TypeX aspectType, Member field) {
		String sig;
		if (field.isStatic()) {
			sig = "()" + field.getReturnType().getSignature();
		} else {
			sig = "(" + field.getDeclaringType().getSignature() + ")" + field.getReturnType().getSignature();
		}
		
		return new ResolvedMember(Member.METHOD,
			aspectType,
			PUBLIC_STATIC, //Modifier.PUBLIC | (field.isStatic() ? Modifier.STATIC : 0),
			NameMangler.inlineAccessMethodForFieldGet(field.getName(),
												field.getDeclaringType(), aspectType), 
			sig);
	}
	
	public static ResolvedMember inlineAccessMethodForFieldSet(TypeX aspectType, Member field) {
		String sig;
		if (field.isStatic()) {
			sig = "(" + field.getReturnType().getSignature() + ")V";
		} else {
			sig = "(" + field.getDeclaringType().getSignature() + field.getReturnType().getSignature() + ")V";
		}
		
		return new ResolvedMember(Member.METHOD,
			aspectType,
			PUBLIC_STATIC, //Modifier.PUBLIC | (field.isStatic() ? Modifier.STATIC : 0),
			NameMangler.inlineAccessMethodForFieldSet(field.getName(),
												field.getDeclaringType(), aspectType), 
			sig);
	}
	


	// --- runtimeLibrary api stuff

	public static Member cflowStackPeekInstance() {
		return new Member(
			Member.METHOD,
			CFLOW_STACK_TYPE,
			0,
			"peekInstance",
			"()Ljava/lang/Object;");
	}

	public static Member cflowStackPushInstance() {
		return new Member(
			Member.METHOD,
			CFLOW_STACK_TYPE,
			0,
			"pushInstance",
			"(Ljava/lang/Object;)V");
	}

	public static Member cflowStackIsValid() {
		return new Member(
			Member.METHOD,
			CFLOW_STACK_TYPE,
			0,
			"isValid",
			"()Z");
	}
	public static Member cflowStackInit() {
		return new Member(
			Member.CONSTRUCTOR,
			CFLOW_STACK_TYPE,
			0,
			"<init>",
			"()V");
	}
	public static Member aroundClosurePreInitializationField() {
		return new Member(
			Member.FIELD,
			AROUND_CLOSURE_TYPE,
			0,
			"preInitializationState",
			"[Ljava/lang/Object;");
	}
	public static Member aroundClosurePreInitializationGetter() {
		return new Member(
			Member.METHOD,
			AROUND_CLOSURE_TYPE,
			0,
			"getPreInitializationState",
			"()[Ljava/lang/Object;");
	}


	public static ResolvedMember preIntroducedConstructor(
		TypeX aspectType,
		TypeX targetType,
		TypeX[] paramTypes) 
	{
		return new ResolvedMember(
			Member.METHOD,
			aspectType,
			PUBLIC_STATIC_FINAL,
			TypeX.OBJECTARRAY,
			NameMangler.preIntroducedConstructor(aspectType, targetType),
			paramTypes);
	}
		
	public static ResolvedMember postIntroducedConstructor(
		TypeX aspectType,
		TypeX targetType,
		TypeX[] paramTypes) 
	{
		return new ResolvedMember(
			Member.METHOD,
			aspectType,
			PUBLIC_STATIC_FINAL,
			ResolvedTypeX.VOID,
			NameMangler.postIntroducedConstructor(aspectType, targetType),
			TypeX.insert(targetType, paramTypes));
	}
	
	public static ResolvedMember interConstructor(ResolvedTypeX targetType, ResolvedMember constructor, TypeX aspectType) {
//		
//		ResolvedTypeX targetType,
//		TypeX[] argTypes,
//		int modifiers) 
//	{
		ResolvedMember ret =
			new ResolvedMember(
				Member.CONSTRUCTOR,
				targetType,
				Modifier.PUBLIC,
				ResolvedTypeX.VOID,
				"<init>",
				constructor.getParameterTypes());
		//System.out.println("ret: " + ret + " mods: " + Modifier.toString(modifiers));
		if (Modifier.isPublic(constructor.getModifiers()))
			return ret;
		int i = 0;
		while (true) {
			ret = addCookieTo(ret, aspectType);
			if (targetType.lookupMemberNoSupers(ret) == null)
				return ret;
		}
	}
	
	public static ResolvedMember interFieldInitializer(ResolvedMember field, TypeX aspectType) {
		return new ResolvedMember(Member.METHOD, aspectType, PUBLIC_STATIC,
			NameMangler.interFieldInitializer(aspectType, field.getDeclaringType(), field.getName()),
			field.isStatic() ? "()V" : "(" + field.getDeclaringType().getSignature() + ")V"
			);
	}
	

	/**
	 * Makes public and non-final
	 */
	private static int makePublicNonFinal(int modifiers) {
		return (modifiers & ~VISIBILITY & ~Modifier.FINAL) | Modifier.PUBLIC;
	}
	
	
	/**
	 * This static method goes on the aspect that declares the inter-type field
	 */
	public static ResolvedMember interFieldSetDispatcher(ResolvedMember field, TypeX aspectType) {
		return new ResolvedMember(Member.METHOD, aspectType, PUBLIC_STATIC,
			ResolvedTypeX.VOID,
			NameMangler.interFieldSetDispatcher(aspectType, field.getDeclaringType(), field.getName()),
			field.isStatic() ? new TypeX[] {field.getReturnType()} 
			                 : new TypeX[] {field.getDeclaringType(), field.getReturnType()} 
			);
	}
	
	/**
	 * This static method goes on the aspect that declares the inter-type field
	 */
	public static ResolvedMember interFieldGetDispatcher(ResolvedMember field, TypeX aspectType) {
		return new ResolvedMember(Member.METHOD, aspectType, PUBLIC_STATIC,
			field.getReturnType(),
			NameMangler.interFieldGetDispatcher(aspectType, field.getDeclaringType(), field.getName()),
			field.isStatic() ? TypeX.NONE : new TypeX[] {field.getDeclaringType()}
			);
	}
	
	
//	private static int makeFieldModifiers(int declaredModifiers) {
//		int ret = Modifier.PUBLIC;
//		if (Modifier.isTransient(declaredModifiers)) ret |= Modifier.TRANSIENT;
//		if (Modifier.isVolatile(declaredModifiers)) ret |= Modifier.VOLATILE;
//		return ret;
//	}
	
	
	/**
	 * This field goes on the class the field
	 * is declared onto
	 */
	public static ResolvedMember interFieldClassField(ResolvedMember field, TypeX aspectType) {
		return new ResolvedMember(Member.FIELD, field.getDeclaringType(), 
			makePublicNonFinal(field.getModifiers()),
			field.getReturnType(), 
			NameMangler.interFieldClassField(field.getModifiers(), aspectType, field.getDeclaringType(), field.getName()),
			TypeX.NONE
			);
	}
	
	
	/**
	 * This field goes on top-most implementers of the interface the field
	 * is declared onto
	 */
	public static ResolvedMember interFieldInterfaceField(ResolvedMember field, TypeX onClass, TypeX aspectType) {
		return new ResolvedMember(Member.FIELD, onClass, makePublicNonFinal(field.getModifiers()),
			field.getReturnType(), 
			NameMangler.interFieldInterfaceField(aspectType, field.getDeclaringType(), field.getName()),
			TypeX.NONE
			);
	}
	
	/**
	 * This instance method goes on the interface the field is declared onto
	 * as well as its top-most implementors
	 */
	public static ResolvedMember interFieldInterfaceSetter(ResolvedMember field, ResolvedTypeX onType, TypeX aspectType) {
		int modifiers = Modifier.PUBLIC;
		if (onType.isInterface()) modifiers |= Modifier.ABSTRACT;
		return new ResolvedMember(Member.METHOD, onType, modifiers,
			ResolvedTypeX.VOID,
			NameMangler.interFieldInterfaceSetter(aspectType, field.getDeclaringType(), field.getName()),
			new TypeX[] {field.getReturnType()}
			);
	}
	
	/**
	 * This instance method goes on the interface the field is declared onto
	 * as well as its top-most implementors
	 */
	public static ResolvedMember interFieldInterfaceGetter(ResolvedMember field, ResolvedTypeX onType, TypeX aspectType) {
		int modifiers = Modifier.PUBLIC;
		if (onType.isInterface()) modifiers |= Modifier.ABSTRACT;
		return new ResolvedMember(Member.METHOD, onType, modifiers,
			field.getReturnType(), 
			NameMangler.interFieldInterfaceGetter(aspectType, field.getDeclaringType(), field.getName()),
			TypeX.NONE
			);
	}
	
	
	

	/**
	 * This method goes on the target type of the inter-type method. (and possibly the topmost-implemeters,
	 * if the target type is an interface) 
	 */
	public static ResolvedMember interMethod(ResolvedMember meth, TypeX aspectType, boolean onInterface) 
	{
		if (Modifier.isPublic(meth.getModifiers()) && !onInterface) return meth;
		
		int modifiers = makePublicNonFinal(meth.getModifiers());
		if (onInterface) modifiers |= Modifier.ABSTRACT;
		
		return new ResolvedMember(Member.METHOD, meth.getDeclaringType(),
			modifiers,
			meth.getReturnType(), 
			NameMangler.interMethod(meth.getModifiers(), aspectType, meth.getDeclaringType(), meth.getName()),
			meth.getParameterTypes());	
	}

	/**
	 * This static method goes on the declaring aspect of the inter-type method.
	 */
	public static ResolvedMember interMethodDispatcher(ResolvedMember meth, TypeX aspectType) 
	{
		TypeX[] paramTypes = meth.getParameterTypes();
		if (!meth.isStatic()) {
			paramTypes = TypeX.insert(meth.getDeclaringType(), paramTypes);
		}
		
		return new ResolvedMember(Member.METHOD, aspectType, PUBLIC_STATIC,
			meth.getReturnType(), 
			NameMangler.interMethodDispatcher(aspectType, meth.getDeclaringType(), meth.getName()),
			paramTypes);
	}

	/**
	 * This static method goes on the declaring aspect of the inter-type method.
	 */
	public static ResolvedMember interMethodBody(ResolvedMember meth, TypeX aspectType) 
	{
		TypeX[] paramTypes = meth.getParameterTypes();
		if (!meth.isStatic()) {
			paramTypes = TypeX.insert(meth.getDeclaringType(), paramTypes);
		}
		
		int modifiers = PUBLIC_STATIC;
		if (Modifier.isStrict(meth.getModifiers())) {
			modifiers |= Modifier.STRICT;
		}
		
		
		return new ResolvedMember(Member.METHOD, aspectType, modifiers,
			meth.getReturnType(), 
			NameMangler.interMethodBody(aspectType, meth.getDeclaringType(), meth.getName()),
			paramTypes);
	}
	
	
	

	private static ResolvedMember addCookieTo(ResolvedMember ret, TypeX aspectType) {
		TypeX[] params = ret.getParameterTypes();
		
		TypeX[] freshParams = TypeX.add(params, aspectType);
		return new ResolvedMember(
			ret.getKind(),
			ret.getDeclaringType(),
			ret.getModifiers(),
			ret.getReturnType(),
			ret.getName(),
			freshParams);
	}

	public static ResolvedMember toObjectConversionMethod(TypeX fromType) {
		if (fromType.isPrimitive()) {
			String name = fromType.toString() + "Object";
			return new ResolvedMember(
				Member.METHOD,
				CONVERSIONS_TYPE,
				PUBLIC_STATIC,
				TypeX.OBJECT,
				name,
				new TypeX[] { fromType });
		} else {
			return null;
		}
	}
	public static Member interfaceConstructor(ResolvedTypeX resolvedTypeX) {
		return new ResolvedMember(
			Member.CONSTRUCTOR,
			resolvedTypeX,
			Modifier.PUBLIC,
			"<init>",
			"()V");
	}
}
