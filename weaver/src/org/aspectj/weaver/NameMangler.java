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

import org.aspectj.weaver.bcel.BcelObjectType;
import org.aspectj.weaver.bcel.LazyClassGen;

public class NameMangler {
	private NameMangler() {
		throw new RuntimeException("static");
	}
	
	public static final String PREFIX = "ajc$";
	
	
	public static final String CFLOW_STACK_TYPE = "org.aspectj.runtime.internal.CFlowStack";
	public static final String SOFT_EXCEPTION_TYPE = "org.aspectj.lang.SoftException";

	public static final String PERSINGLETON_FIELD_NAME =  PREFIX + "perSingletonInstance";
	public static final String PERCFLOW_FIELD_NAME =  PREFIX + "perCflowStack";
	//public static final String PERTHIS_FIELD_NAME =  PREFIX + "perSingletonInstance";
	
	// -----
	public static final String PERCFLOW_PUSH_METHOD = PREFIX + "perCflowPush";

	public static final String PEROBJECT_BIND_METHOD = PREFIX + "perObjectBind";

	public static final String AJC_PRE_CLINIT_NAME = PREFIX + "preClinit";

	public static final String AJC_POST_CLINIT_NAME = PREFIX + "postClinit";



	public static String perObjectInterfaceGet(TypeX aspectType) {
		return makeName(aspectType.getNameAsIdentifier(), "perObjectGet");
	}

	public static String perObjectInterfaceSet(TypeX aspectType) {
		return makeName(aspectType.getNameAsIdentifier(), "perObjectSet");
	}

	public static String perObjectInterfaceField(TypeX aspectType) {
		return makeName(aspectType.getNameAsIdentifier(), "perObjectField");
	}

	
	
	public static String privilegedAccessMethodForMethod(String name, TypeX objectType, TypeX aspectType) {
		return makeName("privMethod", aspectType.getNameAsIdentifier(),
					objectType.getNameAsIdentifier(), name);
	}
	
	public static String privilegedAccessMethodForFieldGet(String name, TypeX objectType, TypeX aspectType) {
		return makeName("privFieldGet", aspectType.getNameAsIdentifier(),
					objectType.getNameAsIdentifier(), name);
	}
	
	public static String privilegedAccessMethodForFieldSet(String name, TypeX objectType, TypeX aspectType) {
		return makeName("privFieldSet", aspectType.getNameAsIdentifier(),
					objectType.getNameAsIdentifier(), name);
	}
	
	
	
	public static String inlineAccessMethodForMethod(String name, TypeX objectType, TypeX aspectType) {
		return makeName("inlineAccessMethod", aspectType.getNameAsIdentifier(),
					objectType.getNameAsIdentifier(), name);
	}
	
	public static String inlineAccessMethodForFieldGet(String name, TypeX objectType, TypeX aspectType) {
		return makeName("inlineAccessFieldGet", aspectType.getNameAsIdentifier(),
					objectType.getNameAsIdentifier(), name);
	}
	
	public static String inlineAccessMethodForFieldSet(String name, TypeX objectType, TypeX aspectType) {
		return makeName("inlineAccessFieldSet", aspectType.getNameAsIdentifier(),
					objectType.getNameAsIdentifier(), name);
	}
	
	
	
	/**
	 * The name of methods corresponding to advice declarations
	 */
	public static String adviceName(TypeX aspectType, AdviceKind kind, int position) {
		return makeName(kind.getName(), aspectType.getNameAsIdentifier(),
					Integer.toHexString(position));
	}
	
	/**
	 * This field goes on top-most implementers of the interface the field
	 * is declared onto
	 */
	public static String interFieldInterfaceField(TypeX aspectType, TypeX interfaceType, String name) {
		return makeName("interField", aspectType.getNameAsIdentifier(),
					interfaceType.getNameAsIdentifier(), name);
	}
	
	/**
	 * This instance method goes on the interface the field is declared onto
	 * as well as its top-most implementors
	 */
	public static String interFieldInterfaceSetter(TypeX aspectType, TypeX interfaceType, String name) {
		return makeName("interFieldSet", aspectType.getNameAsIdentifier(),
					interfaceType.getNameAsIdentifier(), name);
	}
	
	
	/**
	 * This instance method goes on the interface the field is declared onto
	 * as well as its top-most implementors
	 */
	public static String interFieldInterfaceGetter(TypeX aspectType, TypeX interfaceType, String name) {
		return makeName("interFieldGet", aspectType.getNameAsIdentifier(),
					interfaceType.getNameAsIdentifier(), name);
	}

	
	/**
	 * This static method goes on the aspect that declares the inter-type field
	 */
	public static String interFieldSetDispatcher(TypeX aspectType, TypeX onType, String name) {
		return makeName("interFieldSetDispatch", aspectType.getNameAsIdentifier(),
					onType.getNameAsIdentifier(), name);
	}

	/**
	 * This static method goes on the aspect that declares the inter-type field
	 */
	public static String interFieldGetDispatcher(TypeX aspectType, TypeX onType, String name) {
		return makeName("interFieldGetDispatch", aspectType.getNameAsIdentifier(),
					onType.getNameAsIdentifier(), name);
	}


	/**
	 * This field goes on the class the field
	 * is declared onto
	 */
	public static String interFieldClassField(int modifiers, TypeX aspectType, TypeX classType, String name) {
		if (Modifier.isPublic(modifiers)) return name;
		//??? might want to handle case where aspect and class are in same package similar to public
		return makeName("interField", makeVisibilityName(modifiers, aspectType), name);
	}
	
//	/**
//	 * This static method goes on the aspect that declares the inter-type field
//	 */
//	public static String classFieldSetDispatcher(TypeX aspectType, TypeX classType, String name) {
//		return makeName("interFieldSetDispatch", aspectType.getNameAsIdentifier(),
//					classType.getNameAsIdentifier(), name);
//	}
//
//	/**
//	 * This static method goes on the aspect that declares the inter-type field
//	 */
//	public static String classFieldGetDispatcher(TypeX aspectType, TypeX classType, String name) 
//	{
//		return makeName(
//			"interFieldGetDispatch",
//			aspectType.getNameAsIdentifier(),
//			classType.getNameAsIdentifier(),
//			name);
//	}

	/**
	 * This static void method goes on the aspect that declares the inter-type field and is called
	 * from the appropriate place (target's initializer, or clinit, or topmost implementer's inits), 
	 * to initialize the field;
	 */

	public static String interFieldInitializer(TypeX aspectType, TypeX classType, String name) 
	{
		return makeName(
			"interFieldInit",
			aspectType.getNameAsIdentifier(),
			classType.getNameAsIdentifier(),
			name);
	}


	// ----

	/**
	 * This method goes on the target type of the inter-type method. (and possibly the topmost-implemeters,
	 * if the target type is an interface) 
	 */
	public static String interMethod(int modifiers, TypeX aspectType, TypeX classType, String name) 
	{
		if (Modifier.isPublic(modifiers)) return name;
		//??? might want to handle case where aspect and class are in same package similar to public
		return makeName("interMethodDispatch2", makeVisibilityName(modifiers, aspectType), name);	
	}

	/**
	 * This static method goes on the declaring aspect of the inter-type method.
	 */
	public static String interMethodDispatcher(TypeX aspectType, TypeX classType, String name) 
	{
		return makeName("interMethodDispatch1", aspectType.getNameAsIdentifier(), 
					classType.getNameAsIdentifier(), name);
	}

	/**
	 * This static method goes on the declaring aspect of the inter-type method.
	 */
	public static String interMethodBody(TypeX aspectType, TypeX classType, String name) 
	{
		return makeName("interMethod", aspectType.getNameAsIdentifier(), 
					classType.getNameAsIdentifier(), name);
	}

	// ----
	
	/**
	 * This static method goes on the declaring aspect of the inter-type constructor.
	 */
	public static String preIntroducedConstructor(
		TypeX aspectType,
		TypeX targetType) 
	{
		return makeName("preInterConstructor", aspectType.getNameAsIdentifier(),
			targetType.getNameAsIdentifier());
	}
	
	/**
	 * This static method goes on the declaring aspect of the inter-type constructor.
	 */
	public static String postIntroducedConstructor(
		TypeX aspectType,
		TypeX targetType) 
	{
		return makeName("postInterConstructor", aspectType.getNameAsIdentifier(),
			targetType.getNameAsIdentifier());
	}
	// ----

	/**
	 * This static method goes on the target class of the inter-type method.
	 */
	public static String superDispatchMethod(TypeX classType, String name) 
	{
		return makeName("superDispatch",
					classType.getNameAsIdentifier(), name);
	}

	/**
	 * This static method goes on the target class of the inter-type method.
	 */
	public static String protectedDispatchMethod(TypeX classType, String name) 
	{
		return makeName("protectedDispatch",
					classType.getNameAsIdentifier(), name);
	}

	// ----

	private static String makeVisibilityName(int modifiers, TypeX aspectType) {
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
		return "ajc$" + s1 + "$"  + s2;
	}
	public static String makeName(String s1, String s2, String s3) {
		return "ajc$" + s1 + "$"  + s2 + "$" + s3;
	}
	public static String makeName(String s1, String s2, String s3, String s4) {
		return "ajc$" + s1 + "$"  + s2 + "$" + s3 + "$" + s4;
	}
	public static String cflowStack(CrosscuttingMembers xcut) {
		return makeName("cflowStack", Integer.toHexString(xcut.getCflowEntries().size()));
	}



	public static String makeClosureClassName(
		TypeX enclosingType,
		int index) 
	{
			return enclosingType.getName() + "$AjcClosure"  + index;
	}

	public static String aroundCallbackMethodName(
		Member shadowSig,
		LazyClassGen enclosingType) 
	{
		String ret =
			shadowSig.getExtractableName()
				+ "_aroundBody"
				+ enclosingType.getNewGeneratedNameTag();
		return ret;
	}

	public static String proceedMethodName(String adviceMethodName) {
		return adviceMethodName + "proceed";
	}

}
