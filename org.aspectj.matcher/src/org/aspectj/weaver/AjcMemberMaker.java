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

package org.aspectj.weaver;

import java.lang.reflect.Modifier;

//import org.aspectj.weaver.ResolvedType.Name;

/**
 * The AjcMemberMaker is responsible for creating the representations of methods/fields/etc that are placed in both aspects and
 * affected target types. It uses the NameMangler class to create the actual names that will be used.
 */
public class AjcMemberMaker {

	private static final int PUBLIC_STATIC_FINAL = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

	private static final int PRIVATE_STATIC = Modifier.PRIVATE | Modifier.STATIC;

	private static final int PUBLIC_STATIC = Modifier.PUBLIC | Modifier.STATIC;

	private static final int BRIDGE = 0x0040;

	private static final int VISIBILITY = Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;

	public static final UnresolvedType CFLOW_STACK_TYPE = UnresolvedType.forName(NameMangler.CFLOW_STACK_TYPE);

	public static final UnresolvedType AROUND_CLOSURE_TYPE = UnresolvedType
			.forSignature("Lorg/aspectj/runtime/internal/AroundClosure;");

	public static final UnresolvedType CONVERSIONS_TYPE = UnresolvedType.forSignature("Lorg/aspectj/runtime/internal/Conversions;");

	public static final UnresolvedType NO_ASPECT_BOUND_EXCEPTION = UnresolvedType
			.forSignature("Lorg/aspectj/lang/NoAspectBoundException;");

	public static ResolvedMember ajcPreClinitMethod(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PRIVATE_STATIC, NameMangler.AJC_PRE_CLINIT_NAME, "()V");
	}

	public static ResolvedMember ajcPostClinitMethod(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PRIVATE_STATIC, NameMangler.AJC_POST_CLINIT_NAME, "()V");
	}

	public static Member noAspectBoundExceptionInit() {
		return new ResolvedMemberImpl(Member.METHOD, NO_ASPECT_BOUND_EXCEPTION, Modifier.PUBLIC, "<init>", "()V");
	}

	public static Member noAspectBoundExceptionInit2() {
		return new ResolvedMemberImpl(Member.METHOD, NO_ASPECT_BOUND_EXCEPTION, Modifier.PUBLIC, "<init>",
				"(Ljava/lang/String;Ljava/lang/Throwable;)V");
	}

	public static Member noAspectBoundExceptionInitWithCause() {
		return new ResolvedMemberImpl(Member.METHOD, NO_ASPECT_BOUND_EXCEPTION, Modifier.PUBLIC, "<init>",
				"(Ljava/lang/String;Ljava/lang/Throwable;)V");
	}

	public static ResolvedMember perCflowPush(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PUBLIC_STATIC, NameMangler.PERCFLOW_PUSH_METHOD, "()V");
	}

	public static ResolvedMember perCflowField(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.FIELD, declaringType, PUBLIC_STATIC_FINAL, NameMangler.PERCFLOW_FIELD_NAME,
				CFLOW_STACK_TYPE.getSignature());
	}

	public static ResolvedMember perSingletonField(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.FIELD, declaringType, PUBLIC_STATIC_FINAL, NameMangler.PERSINGLETON_FIELD_NAME,
				declaringType.getSignature());
	}

	public static ResolvedMember initFailureCauseField(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.FIELD, declaringType, PRIVATE_STATIC, NameMangler.INITFAILURECAUSE_FIELD_NAME,
				UnresolvedType.THROWABLE.getSignature());
	}

	public static ResolvedMember perObjectField(UnresolvedType declaringType, ResolvedType aspectType) {
		int modifiers = Modifier.PRIVATE;
		if (!UnresolvedType.SERIALIZABLE.resolve(aspectType.getWorld()).isAssignableFrom(aspectType)) {
			modifiers |= Modifier.TRANSIENT;
		}
		return new ResolvedMemberImpl(Member.FIELD, declaringType, modifiers, aspectType,
				NameMangler.perObjectInterfaceField(aspectType), UnresolvedType.NONE);
	}

	// PTWIMPL ResolvedMember for aspect instance field, declared in matched type
	public static ResolvedMember perTypeWithinField(UnresolvedType declaringType, ResolvedType aspectType) {
		int modifiers = Modifier.PRIVATE | Modifier.STATIC;
		if (!isSerializableAspect(aspectType)) {
			modifiers |= Modifier.TRANSIENT;
		}
		return new ResolvedMemberImpl(Member.FIELD, declaringType, modifiers, aspectType,
				NameMangler.perTypeWithinFieldForTarget(aspectType), UnresolvedType.NONE);
	}

	// PTWIMPL ResolvedMember for type instance field, declared in aspect
	// (holds typename for which aspect instance exists)
	public static ResolvedMember perTypeWithinWithinTypeField(UnresolvedType declaringType, ResolvedType aspectType) {
		int modifiers = Modifier.PRIVATE;
		if (!isSerializableAspect(aspectType)) {
			modifiers |= Modifier.TRANSIENT;
		}
		return new ResolvedMemberImpl(Member.FIELD, declaringType, modifiers, UnresolvedType.JL_STRING,
				NameMangler.PERTYPEWITHIN_WITHINTYPEFIELD, UnresolvedType.NONE);
	}

	private static boolean isSerializableAspect(ResolvedType aspectType) {
		return UnresolvedType.SERIALIZABLE.resolve(aspectType.getWorld()).isAssignableFrom(aspectType);
	}

	public static ResolvedMember perObjectBind(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PUBLIC_STATIC | Modifier.SYNCHRONIZED, NameMangler.PEROBJECT_BIND_METHOD,
				"(Ljava/lang/Object;)V");
	}

	// PTWIMPL ResolvedMember for getInstance() method, declared in aspect
	public static ResolvedMember perTypeWithinGetInstance(UnresolvedType declaringType) {
		// private static a.X ajc$getInstance(java.lang.Class)
		ResolvedMemberImpl rm = new ResolvedMemberImpl(Member.METHOD, declaringType, PRIVATE_STATIC, declaringType, // return value
				NameMangler.PERTYPEWITHIN_GETINSTANCE_METHOD, new UnresolvedType[] { UnresolvedType.JL_CLASS });
		return rm;
	}

	// PTWIMPL ResolvedMember for getWithinTypeName() method
	public static ResolvedMember perTypeWithinGetWithinTypeNameMethod(UnresolvedType declaringType, boolean inJava5Mode) {
		// public String getWithinTypeName()
		ResolvedMemberImpl rm = new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC, UnresolvedType.JL_STRING, // return
																																// value
				NameMangler.PERTYPEWITHIN_GETWITHINTYPENAME_METHOD, UnresolvedType.NONE);
		return rm;
	}

	public static ResolvedMember perTypeWithinCreateAspectInstance(UnresolvedType declaringType) {
		// public static a.X ajc$createAspectInstance(java.lang.String)
		ResolvedMemberImpl rm = new ResolvedMemberImpl(Member.METHOD, declaringType, PUBLIC_STATIC, declaringType, // return value
				NameMangler.PERTYPEWITHIN_CREATEASPECTINSTANCE_METHOD,
				new UnresolvedType[] { UnresolvedType.forSignature("Ljava/lang/String;") }, new UnresolvedType[] {});
		return rm;
	}

	public static UnresolvedType perObjectInterfaceType(UnresolvedType aspectType) {
		return UnresolvedType.forName(aspectType.getName() + "$ajcMightHaveAspect");
	}

	public static ResolvedMember perObjectInterfaceGet(UnresolvedType aspectType) {
		return new ResolvedMemberImpl(Member.METHOD, perObjectInterfaceType(aspectType), Modifier.PUBLIC | Modifier.ABSTRACT,
				NameMangler.perObjectInterfaceGet(aspectType), "()" + aspectType.getSignature());
	}

	public static ResolvedMember perObjectInterfaceSet(UnresolvedType aspectType) {
		return new ResolvedMemberImpl(Member.METHOD, perObjectInterfaceType(aspectType), Modifier.PUBLIC | Modifier.ABSTRACT,
				NameMangler.perObjectInterfaceSet(aspectType), "(" + aspectType.getSignature() + ")V");
	}

	// PTWIMPL ResolvedMember for localAspectOf() method, declared in matched type
	public static ResolvedMember perTypeWithinLocalAspectOf(UnresolvedType shadowType, UnresolvedType aspectType) {
		return new ResolvedMemberImpl(Member.METHOD, shadowType,// perTypeWithinInterfaceType(aspectType),
				Modifier.PUBLIC | Modifier.STATIC, NameMangler.perTypeWithinLocalAspectOf(aspectType), "()"
						+ aspectType.getSignature());
	}

	public static ResolvedMember perSingletonAspectOfMethod(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PUBLIC_STATIC, "aspectOf", "()" + declaringType.getSignature());
	}

	public static ResolvedMember perSingletonHasAspectMethod(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PUBLIC_STATIC, "hasAspect", "()Z");
	}

	public static ResolvedMember perCflowAspectOfMethod(UnresolvedType declaringType) {
		return perSingletonAspectOfMethod(declaringType);
	}

	public static ResolvedMember perCflowHasAspectMethod(UnresolvedType declaringType) {
		return perSingletonHasAspectMethod(declaringType);
	}

	public static ResolvedMember perObjectAspectOfMethod(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PUBLIC_STATIC, "aspectOf", "(Ljava/lang/Object;)"
				+ declaringType.getSignature());
	}

	public static ResolvedMember perObjectHasAspectMethod(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PUBLIC_STATIC, "hasAspect", "(Ljava/lang/Object;)Z");
	}

	// PTWIMPL ResolvedMember for aspectOf(), declared in aspect
	public static ResolvedMember perTypeWithinAspectOfMethod(UnresolvedType declaringType, boolean inJava5Mode) {
		UnresolvedType parameterType = null;
		if (inJava5Mode) {
			parameterType = UnresolvedType.forRawTypeName("java.lang.Class");
		} else {
			parameterType = UnresolvedType.forSignature("Ljava/lang/Class;");
		}
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PUBLIC_STATIC, declaringType, "aspectOf",
				new UnresolvedType[] { parameterType });
		// return new ResolvedMemberImpl(Member.METHOD,
		// declaringType, PUBLIC_STATIC, "aspectOf",
		// "(Ljava/lang/Class;)" + declaringType.getSignature());
	}

	/*
	 * public static ResolvedMember perTypeWithinGetWithinTypeMethod(UnresolvedType declaringType, boolean inJava5Mode) {
	 * UnresolvedType returnType = null; if (inJava5Mode) { returnType = UnresolvedType.forRawTypeName("java.lang.Class"); } else {
	 * returnType = UnresolvedType.forSignature("Ljava/lang/Class;"); } return new
	 * ResolvedMemberImpl(Member.METHOD,declaringType,Modifier.PUBLIC,ResolvedType.JAVA_LANG_STRING,"getWithinType",new
	 * UnresolvedType[]{}); }
	 */

	// PTWIMPL ResolvedMember for hasAspect(), declared in aspect
	public static ResolvedMember perTypeWithinHasAspectMethod(UnresolvedType declaringType, boolean inJava5Mode) {
		UnresolvedType parameterType = null;
		if (inJava5Mode) {
			parameterType = UnresolvedType.forRawTypeName("java.lang.Class");
		} else {
			parameterType = UnresolvedType.forSignature("Ljava/lang/Class;");
		}
		return new ResolvedMemberImpl(Member.METHOD, declaringType, PUBLIC_STATIC, UnresolvedType.BOOLEAN, "hasAspect",
				new UnresolvedType[] { parameterType });
		// return new ResolvedMemberImpl(Member.METHOD,
		// declaringType, PUBLIC_STATIC, "hasAspect",
		// "(Ljava/lang/Class;)Z");
	}

	// -- privileged accessors

	public static ResolvedMember privilegedAccessMethodForMethod(UnresolvedType aspectType, ResolvedMember method) {
		return new ResolvedMemberImpl(Member.METHOD, method.getDeclaringType(), Modifier.PUBLIC
				| (Modifier.isStatic(method.getModifiers()) ? Modifier.STATIC : 0), method.getReturnType(),
				NameMangler.privilegedAccessMethodForMethod(method.getName(), method.getDeclaringType(), aspectType),
				method.getParameterTypes(), method.getExceptions());
	}

	/**
	 * Return a resolvedmember representing the synthetic getter for the field. The old style (<1.6.9) is a heavyweight static
	 * method with a long name. The new style (1.6.9 and later) is short, and reusable across aspects.
	 * 
	 * @param aspectType the aspect attempting the access
	 * @param field the field to be accessed
	 * @param shortSyntax is the old (long) or new (short) style format being used
	 * @return a resolvedmember representing the synthetic getter
	 */
	public static ResolvedMember privilegedAccessMethodForFieldGet(UnresolvedType aspectType, Member field, boolean shortSyntax) {
		UnresolvedType fieldDeclaringType = field.getDeclaringType();
		if (shortSyntax) {
			UnresolvedType[] args = null;
			if (Modifier.isStatic(field.getModifiers())) {
				args = ResolvedType.NONE;
			} else {
				args = new UnresolvedType[] { fieldDeclaringType };
			}
			StringBuffer name = new StringBuffer("ajc$get$");
			name.append(field.getName());
			return new ResolvedMemberImpl(Member.METHOD, fieldDeclaringType, PUBLIC_STATIC, field.getReturnType(), name.toString(),
					args);
		} else {
			String getterName = NameMangler.privilegedAccessMethodForFieldGet(field.getName(), fieldDeclaringType, aspectType);
			String sig;
			if (Modifier.isStatic(field.getModifiers())) {
				sig = "()" + field.getReturnType().getSignature();
			} else {
				sig = "(" + fieldDeclaringType.getSignature() + ")" + field.getReturnType().getSignature();
			}
			return new ResolvedMemberImpl(Member.METHOD, fieldDeclaringType, PUBLIC_STATIC, getterName, sig);
		}
	}

	/**
	 * Return a resolvedmember representing the synthetic setter for the field. The old style (<1.6.9) is a heavyweight static
	 * method with a long name. The new style (1.6.9 and later) is short, not always static, and reusable across aspects.
	 * 
	 * @param aspectType the aspect attempting the access
	 * @param field the field to be accessed
	 * @param shortSyntax is the old or new style format being used
	 * @return a resolvedmember representing the synthetic setter
	 */
	public static ResolvedMember privilegedAccessMethodForFieldSet(UnresolvedType aspectType, Member field, boolean shortSyntax) {
		UnresolvedType fieldDeclaringType = field.getDeclaringType();
		if (shortSyntax) {
			UnresolvedType[] args = null;
			if (Modifier.isStatic(field.getModifiers())) {
				args = new UnresolvedType[] { field.getType() };
			} else {
				args = new UnresolvedType[] { fieldDeclaringType, field.getType() };
			}
			StringBuffer name = new StringBuffer("ajc$set$");
			name.append(field.getName());
			return new ResolvedMemberImpl(Member.METHOD, fieldDeclaringType, PUBLIC_STATIC, UnresolvedType.VOID, name.toString(),
					args);
		} else {
			String setterName = NameMangler.privilegedAccessMethodForFieldSet(field.getName(), fieldDeclaringType, aspectType);
			String sig;
			if (Modifier.isStatic(field.getModifiers())) {
				sig = "(" + field.getReturnType().getSignature() + ")V";
			} else {
				sig = "(" + fieldDeclaringType.getSignature() + field.getReturnType().getSignature() + ")V";
			}
			return new ResolvedMemberImpl(Member.METHOD, fieldDeclaringType, PUBLIC_STATIC, setterName, sig);
		}
	}

	// --- inline accessors
	// ??? can eclipse handle a transform this weird without putting synthetics into the mix
	public static ResolvedMember superAccessMethod(UnresolvedType baseType, ResolvedMember method) {
		UnresolvedType[] paramTypes = method.getParameterTypes();
		// if (!method.isStatic()) {
		// paramTypes = UnresolvedType.insert(method.getDeclaringType(), paramTypes);
		// }
		return new ResolvedMemberImpl(Member.METHOD, baseType, Modifier.PUBLIC, method.getReturnType(),
				NameMangler.superDispatchMethod(baseType, method.getName()), paramTypes, method.getExceptions());
	}

	public static ResolvedMember inlineAccessMethodForMethod(UnresolvedType aspectType, ResolvedMember method) {
		UnresolvedType[] paramTypes = method.getParameterTypes();
		if (!Modifier.isStatic(method.getModifiers())) {
			paramTypes = UnresolvedType.insert(method.getDeclaringType(), paramTypes);
		}
		return new ResolvedMemberImpl(Member.METHOD, aspectType,
				PUBLIC_STATIC, // ??? what about privileged and super access
				// ???Modifier.PUBLIC | (method.isStatic() ? Modifier.STATIC : 0),
				method.getReturnType(),

				NameMangler.inlineAccessMethodForMethod(method.getName(), method.getDeclaringType(), aspectType), paramTypes,
				method.getExceptions());
	}

	public static ResolvedMember inlineAccessMethodForFieldGet(UnresolvedType aspectType, Member field) {
		String sig;
		if (Modifier.isStatic(field.getModifiers())) {
			sig = "()" + field.getReturnType().getSignature();
		} else {
			sig = "(" + field.getDeclaringType().getSignature() + ")" + field.getReturnType().getSignature();
		}

		return new ResolvedMemberImpl(Member.METHOD, aspectType, PUBLIC_STATIC, // Modifier.PUBLIC | (field.isStatic() ?
				// Modifier.STATIC : 0),
				NameMangler.inlineAccessMethodForFieldGet(field.getName(), field.getDeclaringType(), aspectType), sig);
	}

	public static ResolvedMember inlineAccessMethodForFieldSet(UnresolvedType aspectType, Member field) {
		String sig;
		if (Modifier.isStatic(field.getModifiers())) {
			sig = "(" + field.getReturnType().getSignature() + ")V";
		} else {
			sig = "(" + field.getDeclaringType().getSignature() + field.getReturnType().getSignature() + ")V";
		}

		return new ResolvedMemberImpl(Member.METHOD, aspectType, PUBLIC_STATIC, // Modifier.PUBLIC | (field.isStatic() ?
				// Modifier.STATIC : 0),
				NameMangler.inlineAccessMethodForFieldSet(field.getName(), field.getDeclaringType(), aspectType), sig);
	}

	// --- runtimeLibrary api stuff

	public static Member cflowStackPeekInstance() {
		return new MemberImpl(Member.METHOD, CFLOW_STACK_TYPE, 0, "peekInstance", "()Ljava/lang/Object;");
	}

	public static Member cflowStackPushInstance() {
		return new MemberImpl(Member.METHOD, CFLOW_STACK_TYPE, 0, "pushInstance", "(Ljava/lang/Object;)V");
	}

	public static Member cflowStackIsValid() {
		return new MemberImpl(Member.METHOD, CFLOW_STACK_TYPE, 0, "isValid", "()Z");
	}

	public static Member cflowStackInit() {
		return new MemberImpl(Member.CONSTRUCTOR, CFLOW_STACK_TYPE, 0, "<init>", "()V");
	}

	public static Member aroundClosurePreInitializationField() {
		return new MemberImpl(Member.FIELD, AROUND_CLOSURE_TYPE, 0, "preInitializationState", "[Ljava/lang/Object;");
	}

	public static Member aroundClosurePreInitializationGetter() {
		return new MemberImpl(Member.METHOD, AROUND_CLOSURE_TYPE, 0, "getPreInitializationState", "()[Ljava/lang/Object;");
	}

	public static ResolvedMember preIntroducedConstructor(UnresolvedType aspectType, UnresolvedType targetType,
			UnresolvedType[] paramTypes) {
		return new ResolvedMemberImpl(Member.METHOD, aspectType, PUBLIC_STATIC_FINAL, UnresolvedType.OBJECTARRAY,
				NameMangler.preIntroducedConstructor(aspectType, targetType), paramTypes);
	}

	public static ResolvedMember postIntroducedConstructor(UnresolvedType aspectType, UnresolvedType targetType,
			UnresolvedType[] paramTypes) {
		return new ResolvedMemberImpl(Member.METHOD, aspectType, PUBLIC_STATIC_FINAL, UnresolvedType.VOID,
				NameMangler.postIntroducedConstructor(aspectType, targetType), UnresolvedType.insert(targetType, paramTypes));
	}

	public static ResolvedMember itdAtDeclareParentsField(ResolvedType targetType, UnresolvedType itdType, UnresolvedType aspectType) {
		return new ResolvedMemberImpl(Member.FIELD, targetType, Modifier.PRIVATE, itdType, NameMangler.itdAtDeclareParentsField(
				aspectType, itdType), null);
	}

	public static ResolvedMember interConstructor(ResolvedType targetType, ResolvedMember constructor, UnresolvedType aspectType) {
		//
		// ResolvedType targetType,
		// UnresolvedType[] argTypes,
		// int modifiers)
		// {
		ResolvedMember ret = new ResolvedMemberImpl(Member.CONSTRUCTOR, targetType, Modifier.PUBLIC, UnresolvedType.VOID, "<init>",
				constructor.getParameterTypes(), constructor.getExceptions());
		// System.out.println("ret: " + ret + " mods: " + Modifier.toString(modifiers));
		if (Modifier.isPublic(constructor.getModifiers())) {
			return ret;
		}
		while (true) {
			ret = addCookieTo(ret, aspectType);
			if (targetType.lookupMemberNoSupers(ret) == null) {
				return ret;
			}
		}
	}

	public static ResolvedMember interFieldInitializer(ResolvedMember field, UnresolvedType aspectType) {
		return new ResolvedMemberImpl(Member.METHOD, aspectType, PUBLIC_STATIC, NameMangler.interFieldInitializer(aspectType,
				field.getDeclaringType(), field.getName()), Modifier.isStatic(field.getModifiers()) ? "()V" : "("
				+ field.getDeclaringType().getSignature() + ")V");
	}

	private static int makePublicNonFinal(int modifiers) {
		return (modifiers & ~VISIBILITY & ~Modifier.FINAL) | Modifier.PUBLIC;
	}

	private static int makeNonFinal(int modifiers) {
		return (modifiers & ~Modifier.FINAL);
	}

	/**
	 * This static method goes on the aspect that declares the inter-type field
	 */
	public static ResolvedMember interFieldSetDispatcher(ResolvedMember field, UnresolvedType aspectType) {
		ResolvedMember rm = new ResolvedMemberImpl(Member.METHOD, aspectType, PUBLIC_STATIC, UnresolvedType.VOID,
				NameMangler.interFieldSetDispatcher(aspectType, field.getDeclaringType(), field.getName()), Modifier.isStatic(field
						.getModifiers()) ? new UnresolvedType[] { field.getReturnType() } : new UnresolvedType[] {
						field.getDeclaringType(), field.getReturnType() });
		rm.setTypeVariables(field.getTypeVariables());
		return rm;
	}

	/**
	 * This static method goes on the aspect that declares the inter-type field
	 */
	public static ResolvedMember interFieldGetDispatcher(ResolvedMember field, UnresolvedType aspectType) {
		ResolvedMember rm = new ResolvedMemberImpl(Member.METHOD, aspectType, PUBLIC_STATIC, field.getReturnType(),
				NameMangler.interFieldGetDispatcher(aspectType, field.getDeclaringType(), field.getName()), Modifier.isStatic(field
						.getModifiers()) ? UnresolvedType.NONE : new UnresolvedType[] { field.getDeclaringType() },
				UnresolvedType.NONE);
		rm.setTypeVariables(field.getTypeVariables());
		return rm;
	}

	// private static int makeFieldModifiers(int declaredModifiers) {
	// int ret = Modifier.PUBLIC;
	// if (Modifier.isTransient(declaredModifiers)) ret |= Modifier.TRANSIENT;
	// if (Modifier.isVolatile(declaredModifiers)) ret |= Modifier.VOLATILE;
	// return ret;
	// }

	/**
	 * This field goes on the class the field is declared onto. Field names for ITDs onto interfaces are handled below.
	 */
	public static ResolvedMember interFieldClassField(ResolvedMember field, UnresolvedType aspectType, boolean newStyle) {
		int modifiers = (newStyle ? makeNonFinal(field.getModifiers()) : makePublicNonFinal(field.getModifiers()));
		String name = null;
		if (newStyle) {
			name = field.getName();
		} else {
			name = NameMangler.interFieldClassField(field.getModifiers(), aspectType, field.getDeclaringType(), field.getName());
		}
		return new ResolvedMemberImpl(Member.FIELD, field.getDeclaringType(), modifiers, field.getReturnType(), name,
				UnresolvedType.NONE, UnresolvedType.NONE);
	}

	/**
	 * This field goes on top-most implementers of the interface the field is declared onto
	 */
	public static ResolvedMember interFieldInterfaceField(ResolvedMember field, UnresolvedType onClass, UnresolvedType aspectType, boolean newStyle) {
		String name = null;
		if (newStyle) {
			name = field.getName();
		} else {
			name = NameMangler.interFieldInterfaceField(aspectType, field.getDeclaringType(), field.getName());
		}
		return new ResolvedMemberImpl(Member.FIELD, onClass, makePublicNonFinal(field.getModifiers()), field.getReturnType(),
				name, UnresolvedType.NONE, UnresolvedType.NONE);
	}

	/**
	 * This instance method goes on the interface the field is declared onto as well as its top-most implementors
	 */
	public static ResolvedMember interFieldInterfaceSetter(ResolvedMember field, ResolvedType onType, UnresolvedType aspectType) {
		int modifiers = Modifier.PUBLIC;
		if (onType.isInterface()) {
			modifiers |= Modifier.ABSTRACT;
		}
		ResolvedMember rm = new ResolvedMemberImpl(Member.METHOD, onType, modifiers, UnresolvedType.VOID,
				NameMangler.interFieldInterfaceSetter(aspectType, field.getDeclaringType(), field.getName()),
				new UnresolvedType[] { field.getReturnType() }, UnresolvedType.NONE);
		rm.setTypeVariables(field.getTypeVariables());
		return rm;
	}

	/**
	 * This instance method goes on the interface the field is declared onto as well as its top-most implementors
	 */
	public static ResolvedMember interFieldInterfaceGetter(ResolvedMember field, ResolvedType onType, UnresolvedType aspectType) {
		int modifiers = Modifier.PUBLIC;
		if (onType.isInterface()) {
			modifiers |= Modifier.ABSTRACT;
		}
		ResolvedMember rm = new ResolvedMemberImpl(Member.METHOD, onType, modifiers, field.getReturnType(),
				NameMangler.interFieldInterfaceGetter(aspectType, field.getDeclaringType(), field.getName()), UnresolvedType.NONE,
				UnresolvedType.NONE);
		rm.setTypeVariables(field.getTypeVariables());
		return rm;
	}

	/**
	 * This method goes on the target type of the inter-type method. (and possibly the topmost-implementors, if the target type is
	 * an interface). The implementation will call the interMethodDispatch method on the aspect.
	 */
	public static ResolvedMember interMethod(ResolvedMember meth, UnresolvedType aspectType, boolean onInterface) {
		if (Modifier.isPublic(meth.getModifiers()) && !onInterface) {
			return meth;
		}

		int modifiers = makePublicNonFinal(meth.getModifiers());
		if (onInterface) {
			modifiers |= Modifier.ABSTRACT;
		}

		ResolvedMemberImpl rmi = new ResolvedMemberImpl(Member.METHOD, meth.getDeclaringType(), modifiers, meth.getReturnType(),
				NameMangler.interMethod(meth.getModifiers(), aspectType, meth.getDeclaringType(), meth.getName()),
				meth.getParameterTypes(), meth.getExceptions());
		rmi.setParameterNames(meth.getParameterNames());
		rmi.setTypeVariables(meth.getTypeVariables());
		return rmi;
	}

	/**
	 * This method goes on the target type of the inter-type method. (and possibly the topmost-implementors, if the target type is
	 * an interface). The implementation will call the interMethodDispatch method on the aspect.
	 */
	public static ResolvedMember interMethodBridger(ResolvedMember meth, UnresolvedType aspectType, boolean onInterface) {
		// if (Modifier.isPublic(meth.getModifiers()) && !onInterface)
		// return meth;

		int modifiers = makePublicNonFinal(meth.getModifiers()) | BRIDGE;
		if (onInterface) {
			modifiers |= Modifier.ABSTRACT;
		}

		ResolvedMemberImpl rmi = new ResolvedMemberImpl(Member.METHOD, meth.getDeclaringType(), modifiers, meth.getReturnType(),
				NameMangler.interMethod(meth.getModifiers(), aspectType, meth.getDeclaringType(), meth.getName()),
				meth.getParameterTypes(), meth.getExceptions());
		rmi.setTypeVariables(meth.getTypeVariables());
		return rmi;
	}

	/**
	 * Sometimes the intertyped method requires a bridge method alongside it. For example if the method 'N SomeI<N>.m()' is put onto
	 * an interface 'interface I<N extends Number>' and then a concrete implementation is 'class C implements I<Float>' then the ITD
	 * on the interface will be 'Number m()', whereas the ITD on the 'topmostimplementor' will be 'Float m()'. A bridge method needs
	 * to be created in the topmostimplementor 'Number m()' that delegates to 'Float m()'
	 */
	public static ResolvedMember bridgerToInterMethod(ResolvedMember meth, UnresolvedType aspectType) {

		int modifiers = makePublicNonFinal(meth.getModifiers());

		ResolvedMemberImpl rmi = new ResolvedMemberImpl(Member.METHOD, aspectType, modifiers, meth.getReturnType(),
				NameMangler.interMethod(meth.getModifiers(), aspectType, meth.getDeclaringType(), meth.getName()),
				meth.getParameterTypes(), meth.getExceptions());
		rmi.setTypeVariables(meth.getTypeVariables());
		return rmi;
	}

	/**
	 * This static method goes on the declaring aspect of the inter-type method. The implementation calls the interMethodBody()
	 * method on the aspect.
	 */
	public static ResolvedMember interMethodDispatcher(ResolvedMember meth, UnresolvedType aspectType) {
		UnresolvedType[] paramTypes = meth.getParameterTypes();
		if (!Modifier.isStatic(meth.getModifiers())) {
			paramTypes = UnresolvedType.insert(meth.getDeclaringType(), paramTypes);
		}

		ResolvedMemberImpl rmi = new ResolvedMemberImpl(Member.METHOD, aspectType, PUBLIC_STATIC, meth.getReturnType(),
				NameMangler.interMethodDispatcher(aspectType, meth.getDeclaringType(), meth.getName()), paramTypes,
				meth.getExceptions());
		rmi.setParameterNames(meth.getParameterNames());
		rmi.setTypeVariables(meth.getTypeVariables());

		return rmi;
	}

	/**
	 * This method goes on the declaring aspect of the inter-type method. It contains the real body of the ITD method.
	 */
	public static ResolvedMember interMethodBody(ResolvedMember meth, UnresolvedType aspectType) {
		UnresolvedType[] paramTypes = meth.getParameterTypes();
		if (!Modifier.isStatic(meth.getModifiers())) {
			paramTypes = UnresolvedType.insert(meth.getDeclaringType(), paramTypes);
		}

		int modifiers = PUBLIC_STATIC;
		if (Modifier.isStrict(meth.getModifiers())) {
			modifiers |= Modifier.STRICT;
		}

		ResolvedMemberImpl rmi = new ResolvedMemberImpl(Member.METHOD, aspectType, modifiers, meth.getReturnType(),
				NameMangler.interMethodBody(aspectType, meth.getDeclaringType(), meth.getName()), paramTypes, meth.getExceptions());
		rmi.setParameterNames(meth.getParameterNames());
		rmi.setTypeVariables(meth.getTypeVariables());
		return rmi;
	}

	private static ResolvedMember addCookieTo(ResolvedMember ret, UnresolvedType aspectType) {
		UnresolvedType[] params = ret.getParameterTypes();

		UnresolvedType[] freshParams = UnresolvedType.add(params, aspectType);
		return new ResolvedMemberImpl(ret.getKind(), ret.getDeclaringType(), ret.getModifiers(), ret.getReturnType(),
				ret.getName(), freshParams, ret.getExceptions());
	}

	public static ResolvedMember toObjectConversionMethod(UnresolvedType fromType) {
		if (fromType.isPrimitiveType()) {
			String name = fromType.toString() + "Object";
			return new ResolvedMemberImpl(Member.METHOD, CONVERSIONS_TYPE, PUBLIC_STATIC, UnresolvedType.OBJECT, name,
					new UnresolvedType[] { fromType }, UnresolvedType.NONE);
		} else {
			return null;
		}
	}

	public static Member interfaceConstructor(ResolvedType resolvedTypeX) {
		// AMC next two lines should not be needed when sig for generic type is changed
		ResolvedType declaringType = resolvedTypeX;
		if (declaringType.isRawType()) {
			declaringType = declaringType.getGenericType();
		}
		return new ResolvedMemberImpl(Member.CONSTRUCTOR, declaringType, Modifier.PUBLIC, "<init>", "()V");
	}

	// -- common types we use. Note: Java 5 dependand types are refered to as String
	public final static UnresolvedType ASPECT_ANNOTATION = UnresolvedType.forSignature("Lorg/aspectj/lang/annotation/Aspect;");

	public final static UnresolvedType BEFORE_ANNOTATION = UnresolvedType.forSignature("Lorg/aspectj/lang/annotation/Before;");

	public final static UnresolvedType AROUND_ANNOTATION = UnresolvedType.forSignature("Lorg/aspectj/lang/annotation/Around;");

	public final static UnresolvedType AFTERRETURNING_ANNOTATION = UnresolvedType
			.forSignature("Lorg/aspectj/lang/annotation/AfterReturning;");

	public final static UnresolvedType AFTERTHROWING_ANNOTATION = UnresolvedType
			.forSignature("Lorg/aspectj/lang/annotation/AfterThrowing;");

	public final static UnresolvedType AFTER_ANNOTATION = UnresolvedType.forSignature("Lorg/aspectj/lang/annotation/After;");

	public final static UnresolvedType POINTCUT_ANNOTATION = UnresolvedType.forSignature("Lorg/aspectj/lang/annotation/Pointcut;");

	public final static UnresolvedType DECLAREERROR_ANNOTATION = UnresolvedType
			.forSignature("Lorg/aspectj/lang/annotation/DeclareError;");

	public final static UnresolvedType DECLAREWARNING_ANNOTATION = UnresolvedType
			.forSignature("Lorg/aspectj/lang/annotation/DeclareWarning;");

	public final static UnresolvedType DECLAREPRECEDENCE_ANNOTATION = UnresolvedType
			.forSignature("Lorg/aspectj/lang/annotation/DeclarePrecedence;");

	// public final static UnresolvedType DECLAREIMPLEMENTS_ANNOTATION =
	// UnresolvedType.forSignature("Lorg/aspectj/lang/annotation/DeclareImplements;");

	public final static UnresolvedType DECLAREPARENTS_ANNOTATION = UnresolvedType
			.forSignature("Lorg/aspectj/lang/annotation/DeclareParents;");

	public final static UnresolvedType DECLAREMIXIN_ANNOTATION = UnresolvedType
			.forSignature("Lorg/aspectj/lang/annotation/DeclareMixin;");

	public final static UnresolvedType TYPEX_JOINPOINT = UnresolvedType.forSignature("Lorg/aspectj/lang/JoinPoint;");

	public final static UnresolvedType TYPEX_PROCEEDINGJOINPOINT = UnresolvedType
			.forSignature("Lorg/aspectj/lang/ProceedingJoinPoint;");

	public final static UnresolvedType TYPEX_STATICJOINPOINT = UnresolvedType
			.forSignature("Lorg/aspectj/lang/JoinPoint$StaticPart;");

	public final static UnresolvedType TYPEX_ENCLOSINGSTATICJOINPOINT = UnresolvedType
			.forSignature("Lorg/aspectj/lang/JoinPoint$EnclosingStaticPart;");

}
