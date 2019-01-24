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

public class NameMangler {

	// public static final char[] AJC_DOLLAR_PREFIX = { 'a', 'j', 'c', '$' };
	// public static final char[] CLINIT = { '<', 'c', 'l', 'i', 'n', 'i', 't', '>' };
	public static final String PREFIX = "ajc$";
	public static final char[] PREFIX_CHARS = "ajc$".toCharArray();
	// public static final char[] INIT = { '<', 'i', 'n', 'i', 't', '>' };
	public static final String ITD_PREFIX = PREFIX + "interType$";
	// public static final char[] METHOD_ASPECTOF = {'a', 's', 'p','e','c','t','O','f'};
	// public static final char[] METHOD_HASASPECT = { 'h', 'a', 's', 'A', 's', 'p', 'e', 'c', 't' };
	// public static final char[] STATIC_INITIALIZER = { '<', 'c', 'l', 'i', 'n', 'i', 't', '>' };

	public static final String CFLOW_STACK_TYPE = "org.aspectj.runtime.internal.CFlowStack";
	public static final String CFLOW_COUNTER_TYPE = "org.aspectj.runtime.internal.CFlowCounter";

	public static final UnresolvedType CFLOW_STACK_UNRESOLVEDTYPE = UnresolvedType
			.forSignature("Lorg/aspectj/runtime/internal/CFlowStack;");

	public static final UnresolvedType CFLOW_COUNTER_UNRESOLVEDTYPE = UnresolvedType
			.forSignature("Lorg/aspectj/runtime/internal/CFlowCounter;");

	public static final String SOFT_EXCEPTION_TYPE = "org.aspectj.lang.SoftException";

	public static final String PERSINGLETON_FIELD_NAME = PREFIX + "perSingletonInstance";
	public static final String PERCFLOW_FIELD_NAME = PREFIX + "perCflowStack";
	// public static final String PERTHIS_FIELD_NAME = PREFIX + "perSingletonInstance";

	// -----
	public static final String PERCFLOW_PUSH_METHOD = PREFIX + "perCflowPush";

	public static final String PEROBJECT_BIND_METHOD = PREFIX + "perObjectBind";

	// PTWIMPL Method and field names
	public static final String PERTYPEWITHIN_GETINSTANCE_METHOD = PREFIX + "getInstance";
	public static final String PERTYPEWITHIN_CREATEASPECTINSTANCE_METHOD = PREFIX + "createAspectInstance";
	public static final String PERTYPEWITHIN_WITHINTYPEFIELD = PREFIX + "withinType";
	public static final String PERTYPEWITHIN_GETWITHINTYPENAME_METHOD = "getWithinTypeName";

	public static final String AJC_PRE_CLINIT_NAME = PREFIX + "preClinit";

	public static final String AJC_POST_CLINIT_NAME = PREFIX + "postClinit";

	public static final String INITFAILURECAUSE_FIELD_NAME = PREFIX + "initFailureCause";

	public static final String ANNOTATION_CACHE_FIELD_NAME = PREFIX + "anno$";

	public static boolean isSyntheticMethod(String methodName, boolean declaredInAspect) {
		if (methodName.startsWith(PREFIX)) {
			// it's synthetic unless it is an advice method
			if (methodName.startsWith("ajc$before") || methodName.startsWith("ajc$after")) {
				return false;
			} else if (methodName.startsWith("ajc$around")) {
				// around advice method is not synthetic, but generated proceed is...
				return (methodName.endsWith("proceed"));
			} else if (methodName.startsWith("ajc$interMethod$")) {
				return false; // body of an itd-m
			}
			return true;
		} else if (methodName.indexOf("_aroundBody") != -1) {
			return true;
		}
		// these aren't the droids you're looking for...move along...... pr148727
		// else if (declaredInAspect) {
		// if (methodName.equals("aspectOf") || methodName.equals("hasAspect")) {
		// return true;
		// }
		// }
		return false;
	}

	public static String perObjectInterfaceGet(UnresolvedType aspectType) {
		return makeName(aspectType.getNameAsIdentifier(), "perObjectGet");
	}

	public static String perObjectInterfaceSet(UnresolvedType aspectType) {
		return makeName(aspectType.getNameAsIdentifier(), "perObjectSet");
	}

	public static String perObjectInterfaceField(UnresolvedType aspectType) {
		return makeName(aspectType.getNameAsIdentifier(), "perObjectField");
	}

	// PTWIMPL method names that must include aspect type
	public static String perTypeWithinFieldForTarget(UnresolvedType aspectType) {
		return makeName(aspectType.getNameAsIdentifier(), "ptwAspectInstance");
	}

	public static String perTypeWithinLocalAspectOf(UnresolvedType aspectType) {
		return makeName(aspectType.getNameAsIdentifier(), "localAspectOf");
	}

	public static String itdAtDeclareParentsField(UnresolvedType aspectType, UnresolvedType itdType) {
		return makeName("instance", aspectType.getNameAsIdentifier(), itdType.getNameAsIdentifier());
	}

	public static String privilegedAccessMethodForMethod(String name, UnresolvedType objectType, UnresolvedType aspectType) {
		return makeName("privMethod", aspectType.getNameAsIdentifier(), objectType.getNameAsIdentifier(), name);
	}

	/**
	 * Create the old style (<1.6.9) format getter name which includes the aspect requesting access and the type containing the
	 * field in the name of the type. At 1.6.9 and above the name is simply ajc$get$<fieldname>
	 */
	public static String privilegedAccessMethodForFieldGet(String name, UnresolvedType objectType, UnresolvedType aspectType) {
		StringBuilder nameBuilder = new StringBuilder();
		nameBuilder.append(makeName("privFieldGet", aspectType.getNameAsIdentifier(), objectType.getNameAsIdentifier(), name));
		return nameBuilder.toString();
	}

	/**
	 * Create the old style (<1.6.9) format setter name which includes the aspect requesting access and the type containing the
	 * field in the name of the type. At 1.6.9 and above the name is simply ajc$set$<fieldname>
	 */
	public static String privilegedAccessMethodForFieldSet(String name, UnresolvedType objectType, UnresolvedType aspectType) {
		return makeName("privFieldSet", aspectType.getNameAsIdentifier(), objectType.getNameAsIdentifier(), name);
	}

	public static String inlineAccessMethodForMethod(String name, UnresolvedType objectType, UnresolvedType aspectType) {
		return makeName("inlineAccessMethod", aspectType.getNameAsIdentifier(), objectType.getNameAsIdentifier(), name);
	}

	public static String inlineAccessMethodForFieldGet(String name, UnresolvedType objectType, UnresolvedType aspectType) {
		return makeName("inlineAccessFieldGet", aspectType.getNameAsIdentifier(), objectType.getNameAsIdentifier(), name);
	}

	public static String inlineAccessMethodForFieldSet(String name, UnresolvedType objectType, UnresolvedType aspectType) {
		return makeName("inlineAccessFieldSet", aspectType.getNameAsIdentifier(), objectType.getNameAsIdentifier(), name);
	}

	/**
	 * The name of methods corresponding to advice declarations Of the form:
	 * "ajc$[AdviceKind]$[AspectName]$[NumberOfAdviceInAspect]$[PointcutHash]"
	 */
	public static String adviceName(String nameAsIdentifier, AdviceKind kind, int adviceSeqNumber, int pcdHash) {
		String newname = makeName(kind.getName(), nameAsIdentifier, Integer.toString(adviceSeqNumber), Integer.toHexString(pcdHash));
		return newname;
	}

	/**
	 * This field goes on top-most implementers of the interface the field is declared onto
	 */
	public static String interFieldInterfaceField(UnresolvedType aspectType, UnresolvedType interfaceType, String name) {
		return makeName("interField", aspectType.getNameAsIdentifier(), interfaceType.getNameAsIdentifier(), name);
	}

	/**
	 * This instance method goes on the interface the field is declared onto as well as its top-most implementors
	 */
	public static String interFieldInterfaceSetter(UnresolvedType aspectType, UnresolvedType interfaceType, String name) {
		return makeName("interFieldSet", aspectType.getNameAsIdentifier(), interfaceType.getNameAsIdentifier(), name);
	}

	/**
	 * This instance method goes on the interface the field is declared onto as well as its top-most implementors
	 */
	public static String interFieldInterfaceGetter(UnresolvedType aspectType, UnresolvedType interfaceType, String name) {
		return makeName("interFieldGet", aspectType.getNameAsIdentifier(), interfaceType.getNameAsIdentifier(), name);
	}

	/**
	 * This static method goes on the aspect that declares the inter-type field
	 */
	public static String interFieldSetDispatcher(UnresolvedType aspectType, UnresolvedType onType, String name) {
		return makeName("interFieldSetDispatch", aspectType.getNameAsIdentifier(), onType.getNameAsIdentifier(), name);
	}

	/**
	 * This static method goes on the aspect that declares the inter-type field
	 */
	public static String interFieldGetDispatcher(UnresolvedType aspectType, UnresolvedType onType, String name) {
		return makeName("interFieldGetDispatch", aspectType.getNameAsIdentifier(), onType.getNameAsIdentifier(), name);
	}

	/**
	 * This field goes on the class the field is declared onto
	 */
	public static String interFieldClassField(int modifiers, UnresolvedType aspectType, UnresolvedType classType, String name) {
		// return name;
		if (Modifier.isPublic(modifiers)) {
			return name;
		}
		// // ??? might want to handle case where aspect and class are in same package similar to public
		return makeName("interField", makeVisibilityName(modifiers, aspectType), name);
	}

	// /**
	// * This static method goes on the aspect that declares the inter-type field
	// */
	// public static String classFieldSetDispatcher(UnresolvedType aspectType, UnresolvedType classType, String name) {
	// return makeName("interFieldSetDispatch", aspectType.getNameAsIdentifier(),
	// classType.getNameAsIdentifier(), name);
	// }
	//
	// /**
	// * This static method goes on the aspect that declares the inter-type field
	// */
	// public static String classFieldGetDispatcher(UnresolvedType aspectType, UnresolvedType classType, String name)
	// {
	// return makeName(
	// "interFieldGetDispatch",
	// aspectType.getNameAsIdentifier(),
	// classType.getNameAsIdentifier(),
	// name);
	// }

	/**
	 * This static void method goes on the aspect that declares the inter-type field and is called from the appropriate place
	 * (target's initializer, or clinit, or topmost implementer's inits), to initialize the field;
	 */

	public static String interFieldInitializer(UnresolvedType aspectType, UnresolvedType classType, String name) {
		return makeName("interFieldInit", aspectType.getNameAsIdentifier(), classType.getNameAsIdentifier(), name);
	}

	// ----

	/**
	 * This method goes on the target type of the inter-type method. (and possibly the topmost-implemeters, if the target type is an
	 * interface)
	 */
	public static String interMethod(int modifiers, UnresolvedType aspectType, UnresolvedType classType, String name) {
		if (Modifier.isPublic(modifiers)) {
			return name;
		}
		// ??? might want to handle case where aspect and class are in same package similar to public
		return makeName("interMethodDispatch2", makeVisibilityName(modifiers, aspectType), name);
	}

	/**
	 * This static method goes on the declaring aspect of the inter-type method.
	 */
	public static String interMethodDispatcher(UnresolvedType aspectType, UnresolvedType classType, String name) {
		return makeName("interMethodDispatch1", aspectType.getNameAsIdentifier(), classType.getNameAsIdentifier(), name);
	}

	/**
	 * This static method goes on the declaring aspect of the inter-type method.
	 */
	public static String interMethodBody(UnresolvedType aspectType, UnresolvedType classType, String name) {
		return makeName("interMethod", aspectType.getNameAsIdentifier(), classType.getNameAsIdentifier(), name);
	}

	// ----

	/**
	 * This static method goes on the declaring aspect of the inter-type constructor.
	 */
	public static String preIntroducedConstructor(UnresolvedType aspectType, UnresolvedType targetType) {
		return makeName("preInterConstructor", aspectType.getNameAsIdentifier(), targetType.getNameAsIdentifier());
	}

	/**
	 * This static method goes on the declaring aspect of the inter-type constructor.
	 */
	public static String postIntroducedConstructor(UnresolvedType aspectType, UnresolvedType targetType) {
		return makeName("postInterConstructor", aspectType.getNameAsIdentifier(), targetType.getNameAsIdentifier());
	}

	// ----

	/**
	 * This static method goes on the target class of the inter-type method.
	 */
	public static String superDispatchMethod(UnresolvedType classType, String name) {
		return makeName("superDispatch", classType.getNameAsIdentifier(), name);
	}

	/**
	 * This static method goes on the target class of the inter-type method.
	 */
	public static String protectedDispatchMethod(UnresolvedType classType, String name) {
		return makeName("protectedDispatch", classType.getNameAsIdentifier(), name);
	}

	// ----

	private static String makeVisibilityName(int modifiers, UnresolvedType aspectType) {
		if (Modifier.isPrivate(modifiers)) {
			return aspectType.getOutermostType().getNameAsIdentifier();
		} else if (Modifier.isProtected(modifiers)) {
			throw new RuntimeException("protected inter-types not allowed");
		} else if (Modifier.isPublic(modifiers)) {
			return "";
		} else {
			return aspectType.getPackageNameAsIdentifier();
		}
	}

	private static String makeName(String s1, String s2) {
		return "ajc$" + s1 + "$" + s2;
	}

	public static String makeName(String s1, String s2, String s3) {
		return "ajc$" + s1 + "$" + s2 + "$" + s3;
	}

	public static String makeName(String s1, String s2, String s3, String s4) {
		return "ajc$" + s1 + "$" + s2 + "$" + s3 + "$" + s4;
	}

	public static String cflowStack(CrosscuttingMembers xcut) {
		return makeName("cflowStack", Integer.toHexString(xcut.getCflowEntries().size()));
	}

	public static String cflowCounter(CrosscuttingMembers xcut) {
		return makeName("cflowCounter", Integer.toHexString(xcut.getCflowEntries().size()));
	}

	public static String makeClosureClassName(UnresolvedType enclosingType, String suffix) {
		return enclosingType.getName() + "$AjcClosure" + suffix;
	}

	public static String aroundShadowMethodName(Member shadowSig, String suffixTag) {
		StringBuffer ret = new StringBuffer();
		ret.append(getExtractableName(shadowSig)).append("_aroundBody").append(suffixTag);
		return ret.toString();
	}

	public static String aroundAdviceMethodName(Member shadowSig, String suffixTag) {
		StringBuffer ret = new StringBuffer();
		ret.append(getExtractableName(shadowSig)).append("_aroundBody").append(suffixTag).append("$advice");
		return ret.toString();
	}

	public static String getExtractableName(Member shadowSignature) {
		String name = shadowSignature.getName();
		MemberKind kind = shadowSignature.getKind();
		if (kind == Member.CONSTRUCTOR) {
			return "init$";
		} else if (kind == Member.STATIC_INITIALIZATION) {
			return "clinit$";
		} else {
			return name;
		}
	}

	public static String proceedMethodName(String adviceMethodName) {
		return adviceMethodName + "proceed";
	}

}
