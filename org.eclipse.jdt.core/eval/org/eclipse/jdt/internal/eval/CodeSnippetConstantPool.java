/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * This constant pool is used to manage well known methods and fields related specifically to the
 * code snippet code generation (java.lang.reflect classes).
 */
public class CodeSnippetConstantPool extends ConstantPool implements TypeConstants {

	// predefined type constant names
	final static char[][] JAVA_LANG_REFLECT_FIELD = new char[][] {JAVA, LANG, REFLECT, "Field".toCharArray()}; //$NON-NLS-1$
	final static char[][] JAVA_LANG_REFLECT_ACCESSIBLEOBJECT = new char[][] {JAVA, LANG, REFLECT, "AccessibleObject".toCharArray()}; //$NON-NLS-1$
	final static char[][] JAVA_LANG_REFLECT_METHOD = new char[][] {JAVA, LANG, REFLECT, "Method".toCharArray()}; //$NON-NLS-1$
	final static char[][] JAVA_LANG_REFLECT_ARRAY = new char[][] {JAVA, LANG, REFLECT, "Array".toCharArray()}; //$NON-NLS-1$

	// predefined methods constant names
	final static char[] GETDECLAREDFIELD_NAME = "getDeclaredField".toCharArray(); //$NON-NLS-1$
	final static char[] GETDECLAREDFIELD_SIGNATURE = "(Ljava/lang/String;)Ljava/lang/reflect/Field;".toCharArray(); //$NON-NLS-1$
	final static char[] SETACCESSIBLE_NAME = "setAccessible".toCharArray(); //$NON-NLS-1$
	final static char[] SETACCESSIBLE_SIGNATURE = "(Z)V".toCharArray(); //$NON-NLS-1$
	final static char[] JAVALANGREFLECTFIELD_CONSTANTPOOLNAME = "java/lang/reflect/Field".toCharArray(); //$NON-NLS-1$
	final static char[] JAVALANGREFLECTACCESSIBLEOBJECT_CONSTANTPOOLNAME = "java/lang/reflect/AccessibleObject".toCharArray(); //$NON-NLS-1$
	final static char[] JAVALANGREFLECTARRAY_CONSTANTPOOLNAME = "java/lang/reflect/Array".toCharArray(); //$NON-NLS-1$
	final static char[] JAVALANGREFLECTMETHOD_CONSTANTPOOLNAME = "java/lang/reflect/Method".toCharArray(); //$NON-NLS-1$
	final static char[] GET_INT_METHOD_NAME = "getInt".toCharArray(); //$NON-NLS-1$
	final static char[] GET_LONG_METHOD_NAME = "getLong".toCharArray(); //$NON-NLS-1$
	final static char[] GET_DOUBLE_METHOD_NAME = "getDouble".toCharArray(); //$NON-NLS-1$
	final static char[] GET_FLOAT_METHOD_NAME = "getFloat".toCharArray(); //$NON-NLS-1$
	final static char[] GET_BYTE_METHOD_NAME = "getByte".toCharArray(); //$NON-NLS-1$
	final static char[] GET_CHAR_METHOD_NAME = "getChar".toCharArray(); //$NON-NLS-1$
	final static char[] GET_BOOLEAN_METHOD_NAME = "getBoolean".toCharArray(); //$NON-NLS-1$
	final static char[] GET_OBJECT_METHOD_NAME = "get".toCharArray(); //$NON-NLS-1$
	final static char[] GET_SHORT_METHOD_NAME = "getShort".toCharArray(); //$NON-NLS-1$
	final static char[] ARRAY_NEWINSTANCE_NAME = "newInstance".toCharArray(); //$NON-NLS-1$
	final static char[] GET_INT_METHOD_SIGNATURE = "(Ljava/lang/Object;)I".toCharArray(); //$NON-NLS-1$
	final static char[] GET_LONG_METHOD_SIGNATURE = "(Ljava/lang/Object;)J".toCharArray(); //$NON-NLS-1$
	final static char[] GET_DOUBLE_METHOD_SIGNATURE = "(Ljava/lang/Object;)D".toCharArray(); //$NON-NLS-1$
	final static char[] GET_FLOAT_METHOD_SIGNATURE = "(Ljava/lang/Object;)F".toCharArray(); //$NON-NLS-1$
	final static char[] GET_BYTE_METHOD_SIGNATURE = "(Ljava/lang/Object;)B".toCharArray(); //$NON-NLS-1$
	final static char[] GET_CHAR_METHOD_SIGNATURE = "(Ljava/lang/Object;)C".toCharArray(); //$NON-NLS-1$
	final static char[] GET_BOOLEAN_METHOD_SIGNATURE = "(Ljava/lang/Object;)Z".toCharArray(); //$NON-NLS-1$
	final static char[] GET_OBJECT_METHOD_SIGNATURE = "(Ljava/lang/Object;)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	final static char[] GET_SHORT_METHOD_SIGNATURE = "(Ljava/lang/Object;)S".toCharArray(); //$NON-NLS-1$
	final static char[] SET_INT_METHOD_NAME = "setInt".toCharArray(); //$NON-NLS-1$
	final static char[] SET_LONG_METHOD_NAME = "setLong".toCharArray(); //$NON-NLS-1$
	final static char[] SET_DOUBLE_METHOD_NAME = "setDouble".toCharArray(); //$NON-NLS-1$
	final static char[] SET_FLOAT_METHOD_NAME = "setFloat".toCharArray(); //$NON-NLS-1$
	final static char[] SET_BYTE_METHOD_NAME = "setByte".toCharArray(); //$NON-NLS-1$
	final static char[] SET_CHAR_METHOD_NAME = "setChar".toCharArray(); //$NON-NLS-1$
	final static char[] SET_BOOLEAN_METHOD_NAME = "setBoolean".toCharArray(); //$NON-NLS-1$
	final static char[] SET_OBJECT_METHOD_NAME = "set".toCharArray(); //$NON-NLS-1$
	final static char[] SET_SHORT_METHOD_NAME = "setShort".toCharArray(); //$NON-NLS-1$
	final static char[] SET_INT_METHOD_SIGNATURE = "(Ljava/lang/Object;I)V".toCharArray(); //$NON-NLS-1$
	final static char[] SET_LONG_METHOD_SIGNATURE = "(Ljava/lang/Object;J)V".toCharArray(); //$NON-NLS-1$
	final static char[] SET_DOUBLE_METHOD_SIGNATURE = "(Ljava/lang/Object;D)V".toCharArray(); //$NON-NLS-1$
	final static char[] SET_FLOAT_METHOD_SIGNATURE = "(Ljava/lang/Object;F)V".toCharArray(); //$NON-NLS-1$
	final static char[] SET_BYTE_METHOD_SIGNATURE = "(Ljava/lang/Object;B)V".toCharArray(); //$NON-NLS-1$
	final static char[] SET_CHAR_METHOD_SIGNATURE = "(Ljava/lang/Object;C)V".toCharArray(); //$NON-NLS-1$
	final static char[] SET_BOOLEAN_METHOD_SIGNATURE = "(Ljava/lang/Object;Z)V".toCharArray(); //$NON-NLS-1$
	final static char[] SET_OBJECT_METHOD_SIGNATURE = "(Ljava/lang/Object;Ljava/lang/Object;)V".toCharArray(); //$NON-NLS-1$
	final static char[] SET_SHORT_METHOD_SIGNATURE = "(Ljava/lang/Object;S)V".toCharArray(); //$NON-NLS-1$
	final static char[] GETDECLAREDMETHOD_NAME = "getDeclaredMethod".toCharArray(); //$NON-NLS-1$
	final static char[] GETDECLAREDMETHOD_SIGNATURE = "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;".toCharArray(); //$NON-NLS-1$
	final static char[] ARRAY_NEWINSTANCE_SIGNATURE = "(Ljava/lang/Class;[I)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	final static char[] INVOKE_METHOD_METHOD_NAME = "invoke".toCharArray(); //$NON-NLS-1$
	final static char[] INVOKE_METHOD_METHOD_SIGNATURE = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	final static char[] BYTEVALUE_BYTE_METHOD_NAME = "byteValue".toCharArray(); //$NON-NLS-1$
	final static char[] BYTEVALUE_BYTE_METHOD_SIGNATURE = "()B".toCharArray(); //$NON-NLS-1$
	final static char[] SHORTVALUE_SHORT_METHOD_NAME = "shortValue".toCharArray(); //$NON-NLS-1$
	final static char[] DOUBLEVALUE_DOUBLE_METHOD_NAME = "doubleValue".toCharArray(); //$NON-NLS-1$
	final static char[] FLOATVALUE_FLOAT_METHOD_NAME = "floatValue".toCharArray(); //$NON-NLS-1$
	final static char[] INTVALUE_INTEGER_METHOD_NAME = "intValue".toCharArray(); //$NON-NLS-1$
	final static char[] CHARVALUE_CHARACTER_METHOD_NAME = "charValue".toCharArray(); //$NON-NLS-1$
	final static char[] BOOLEANVALUE_BOOLEAN_METHOD_NAME = "booleanValue".toCharArray(); //$NON-NLS-1$
	final static char[] LONGVALUE_LONG_METHOD_NAME = "longValue".toCharArray(); //$NON-NLS-1$
	final static char[] SHORTVALUE_SHORT_METHOD_SIGNATURE = "()S".toCharArray(); //$NON-NLS-1$
	final static char[] DOUBLEVALUE_DOUBLE_METHOD_SIGNATURE = "()D".toCharArray(); //$NON-NLS-1$
	final static char[] FLOATVALUE_FLOAT_METHOD_SIGNATURE = "()F".toCharArray(); //$NON-NLS-1$
	final static char[] INTVALUE_INTEGER_METHOD_SIGNATURE = "()I".toCharArray(); //$NON-NLS-1$
	final static char[] CHARVALUE_CHARACTER_METHOD_SIGNATURE = "()C".toCharArray(); //$NON-NLS-1$
	final static char[] BOOLEANVALUE_BOOLEAN_METHOD_SIGNATURE = "()Z".toCharArray(); //$NON-NLS-1$
	final static char[] LONGVALUE_LONG_METHOD_SIGNATURE = "()J".toCharArray(); //$NON-NLS-1$
	final static char[] GETDECLAREDCONSTRUCTOR_NAME = "getDeclaredConstructor".toCharArray(); //$NON-NLS-1$
	final static char[] GETDECLAREDCONSTRUCTOR_SIGNATURE = "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;".toCharArray(); //$NON-NLS-1$
	
	// predefined constant index for well known types
	final static int JAVA_LANG_REFLECT_FIELD_TYPE = 0;
	final static int JAVA_LANG_REFLECT_METHOD_TYPE = 1;
	final static int JAVA_LANG_REFLECT_ACCESSIBLEOBJECT_TYPE = 2;
	final static int JAVA_LANG_REFLECT_ARRAY_TYPE = 3;

	// predefined constant index for well known methods
	final static int GETDECLAREDFIELD_CLASS_METHOD = 0;
	final static int SETACCESSIBLE_ACCESSIBLEOBJECT_METHOD = 1;
	final static int GET_INT_METHOD = 2;
	final static int GET_LONG_METHOD = 3;
	final static int GET_DOUBLE_METHOD = 4;
	final static int GET_FLOAT_METHOD = 5;
	final static int GET_BYTE_METHOD = 6;
	final static int GET_CHAR_METHOD = 7;
	final static int GET_BOOLEAN_METHOD = 8;
	final static int GET_OBJECT_METHOD = 9;
	final static int GET_SHORT_METHOD = 10;
	final static int SET_INT_METHOD = 11;
	final static int SET_LONG_METHOD = 12;
	final static int SET_DOUBLE_METHOD = 13;
	final static int SET_FLOAT_METHOD = 14;
	final static int SET_BYTE_METHOD = 15;
	final static int SET_CHAR_METHOD = 16;
	final static int SET_BOOLEAN_METHOD = 17;
	final static int SET_OBJECT_METHOD = 18;
	final static int SET_SHORT_METHOD = 19;
	final static int GETDECLAREDMETHOD_CLASS_METHOD = 20;
	final static int NEWINSTANCE_ARRAY_METHOD = 21;
	final static int INVOKE_METHOD_METHOD = 22;
	final static int BYTEVALUE_BYTE_METHOD = 23;
	final static int SHORTVALUE_SHORT_METHOD = 24;
	final static int DOUBLEVALUE_DOUBLE_METHOD = 25;
	final static int FLOATVALUE_FLOAT_METHOD = 26;
	final static int INTVALUE_INTEGER_METHOD = 27;
	final static int CHARVALUE_CHARACTER_METHOD = 28;
	final static int BOOLEANVALUE_BOOLEAN_METHOD = 29;
	final static int LONGVALUE_LONG_METHOD = 30;
	final static int GETDECLAREDCONSTRUCTOR_CLASS_METHOD = 31;
	
	// predefined constant index for well known name and type for methods
	final static int GETDECLAREDFIELD_CLASS_METHOD_NAME_AND_TYPE = 0;
	final static int SETACCESSIBLE_ACCESSIBLEOBJECT_METHOD_NAME_AND_TYPE = 1;
	final static int GET_INT_METHOD_NAME_AND_TYPE = 2;
	final static int GET_LONG_METHOD_NAME_AND_TYPE = 3;
	final static int GET_DOUBLE_METHOD_NAME_AND_TYPE = 4;
	final static int GET_FLOAT_METHOD_NAME_AND_TYPE = 5;
	final static int GET_BYTE_METHOD_NAME_AND_TYPE = 6;
	final static int GET_CHAR_METHOD_NAME_AND_TYPE = 7;
	final static int GET_BOOLEAN_METHOD_NAME_AND_TYPE = 8;
	final static int GET_OBJECT_METHOD_NAME_AND_TYPE = 9;
	final static int GET_SHORT_METHOD_NAME_AND_TYPE = 10;
	final static int SET_INT_METHOD_NAME_AND_TYPE = 11;
	final static int SET_LONG_METHOD_NAME_AND_TYPE = 12;
	final static int SET_DOUBLE_METHOD_NAME_AND_TYPE = 13;
	final static int SET_FLOAT_METHOD_NAME_AND_TYPE = 14;
	final static int SET_BYTE_METHOD_NAME_AND_TYPE = 15;
	final static int SET_CHAR_METHOD_NAME_AND_TYPE = 16;
	final static int SET_BOOLEAN_METHOD_NAME_AND_TYPE = 17;
	final static int SET_OBJECT_METHOD_NAME_AND_TYPE = 18;
	final static int SET_SHORT_METHOD_NAME_AND_TYPE = 19;
	final static int GETDECLAREDMETHOD_CLASS_METHOD_NAME_AND_TYPE = 20;
	final static int ARRAY_NEWINSTANCE_METHOD_NAME_AND_TYPE = 21;
	final static int INVOKE_METHOD_METHOD_NAME_AND_TYPE = 22;
	final static int BYTEVALUE_BYTE_METHOD_NAME_AND_TYPE = 23;
	final static int SHORTVALUE_SHORT_METHOD_NAME_AND_TYPE = 24;
	final static int DOUBLEVALUE_DOUBLE_METHOD_NAME_AND_TYPE = 25;
	final static int FLOATVALUE_FLOAT_METHOD_NAME_AND_TYPE = 26;
	final static int INTVALUE_INTEGER_METHOD_NAME_AND_TYPE = 27;
	final static int CHARVALUE_CHARACTER_METHOD_NAME_AND_TYPE = 28;
	final static int BOOLEANVALUE_BOOLEAN_METHOD_NAME_AND_TYPE = 29;
	final static int LONGVALUE_LONG_METHOD_NAME_AND_TYPE = 30;
	final static int GETDECLAREDCONSTRUCTOR_CLASS_METHOD_NAME_AND_TYPE = 31;
	
	int[] wellKnownTypes = new int[4];
	int[] wellKnownMethods = new int[32];
	int[] wellKnownMethodNameAndTypes = new int[32];	
/**
 * CodeSnippetConstantPool constructor comment.
 * @param classFile org.eclipse.jdt.internal.compiler.ClassFile
 */
public CodeSnippetConstantPool(org.eclipse.jdt.internal.compiler.ClassFile classFile) {
	super(classFile);
}
/**
 * Return the index of the @methodBinding.
 *
 * Returns -1 if the @methodBinding is not a predefined methodBinding, 
 * the right index otherwise.
 *
 * @param methodBinding org.eclipse.jdt.internal.compiler.lookup.MethodBinding
 * @return <CODE>int</CODE>
 */
public int indexOfWellKnownMethodNameAndType(MethodBinding methodBinding) {
	int index = super.indexOfWellKnownMethodNameAndType(methodBinding);
	if (index == -1) {
		char firstChar = methodBinding.selector[0];
		switch(firstChar) {
			case 'g':
				if (methodBinding.parameters.length == 1
					&& methodBinding.parameters[0].id == T_JavaLangString
					&& CharOperation.equals(methodBinding.selector, GETDECLAREDFIELD_NAME)
					&& methodBinding.returnType instanceof ReferenceBinding
					&& CharOperation.equals(((ReferenceBinding) methodBinding.returnType).compoundName,JAVA_LANG_REFLECT_FIELD)) {
						return GETDECLAREDFIELD_CLASS_METHOD_NAME_AND_TYPE;
				}
				if (methodBinding.parameters.length == 2
					&& methodBinding.parameters[0].id == T_JavaLangString
					&& methodBinding.parameters[1].isArrayType()
					&& ((ArrayBinding) methodBinding.parameters[1]).leafComponentType.id == T_JavaLangClass
					&& CharOperation.equals(methodBinding.selector, GETDECLAREDMETHOD_NAME)
					&& methodBinding.returnType instanceof ReferenceBinding
					&& CharOperation.equals(((ReferenceBinding) methodBinding.returnType).compoundName,JAVA_LANG_REFLECT_METHOD)) {
						return GETDECLAREDMETHOD_CLASS_METHOD_NAME_AND_TYPE;
				}
				if (methodBinding.parameters.length == 1
					&& methodBinding.parameters[0].isArrayType()
					&& ((ArrayBinding) methodBinding.parameters[0]).leafComponentType.id == T_JavaLangClass
					&& CharOperation.equals(methodBinding.selector, GETDECLAREDCONSTRUCTOR_NAME)
					&& methodBinding.returnType instanceof ReferenceBinding
					&& CharOperation.equals(((ReferenceBinding) methodBinding.returnType).compoundName,JAVA_LANG_REFLECT_CONSTRUCTOR)) {
						return GETDECLAREDCONSTRUCTOR_CLASS_METHOD_NAME_AND_TYPE;
				}
				if (methodBinding.parameters.length == 1
					&& methodBinding.parameters[0].id == T_Object) {
						switch(methodBinding.returnType.id) {
							case T_int :
								if (CharOperation.equals(methodBinding.selector, GET_INT_METHOD_NAME)
									&& methodBinding.returnType.id == T_int) {
									return GET_INT_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_byte :
								if (CharOperation.equals(methodBinding.selector, GET_BYTE_METHOD_NAME)
									&& methodBinding.returnType.id == T_byte) {
									return GET_BYTE_METHOD_NAME_AND_TYPE;
								}
								break;					
							case T_short :
								if (CharOperation.equals(methodBinding.selector, GET_SHORT_METHOD_NAME)
									&& methodBinding.returnType.id == T_short) {
									return GET_SHORT_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_char :
								if (CharOperation.equals(methodBinding.selector, GET_CHAR_METHOD_NAME)
									&& methodBinding.returnType.id == T_char) {
									return GET_CHAR_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_double :
								if (CharOperation.equals(methodBinding.selector, GET_DOUBLE_METHOD_NAME)
									&& methodBinding.returnType.id == T_double) {
									return GET_DOUBLE_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_float :
								if (CharOperation.equals(methodBinding.selector, GET_FLOAT_METHOD_NAME)
									&& methodBinding.returnType.id == T_float) {
									return GET_FLOAT_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_long :
								if (CharOperation.equals(methodBinding.selector, GET_LONG_METHOD_NAME)
									&& methodBinding.returnType.id == T_long) {
									return GET_LONG_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_boolean :
								if (CharOperation.equals(methodBinding.selector, GET_BOOLEAN_METHOD_NAME)
									&& methodBinding.returnType.id == T_boolean) {
									return GET_BOOLEAN_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_Object :
								if (CharOperation.equals(methodBinding.selector, GET_OBJECT_METHOD_NAME)
									&& methodBinding.returnType.id == T_JavaLangObject) {
									return GET_OBJECT_METHOD_NAME_AND_TYPE;
								}
						}
				}
				break;
			case 'i':
				if (methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, INTVALUE_INTEGER_METHOD_NAME)
					&& methodBinding.returnType.id == T_int) {
						return INTVALUE_INTEGER_METHOD_NAME_AND_TYPE;
				}
				if (methodBinding.parameters.length == 2
					&& methodBinding.parameters[0].id == T_JavaLangObject
					&& methodBinding.parameters[1].isArrayType()
					&& ((ArrayBinding) methodBinding.parameters[1]).leafComponentType.id == T_JavaLangObject
					&& CharOperation.equals(methodBinding.selector, INVOKE_METHOD_METHOD_NAME)
					&& methodBinding.returnType.id == T_JavaLangObject) {
						return INVOKE_METHOD_METHOD_NAME_AND_TYPE;
				}			
				break;
			case 's':
				if (methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, SHORTVALUE_SHORT_METHOD_NAME)
					&& methodBinding.returnType.id == T_short) {
						return SHORTVALUE_SHORT_METHOD_NAME_AND_TYPE;
				}
				if (methodBinding.parameters.length == 1
					&& methodBinding.parameters[0].id == T_boolean
					&& methodBinding.selector.length == 13
					&& CharOperation.equals(methodBinding.selector, SETACCESSIBLE_NAME)
					&& methodBinding.returnType.id == T_void) {
						return SETACCESSIBLE_ACCESSIBLEOBJECT_METHOD_NAME_AND_TYPE;
				}
				if (methodBinding.returnType.id == T_void
					&& methodBinding.parameters.length == 2
					&& methodBinding.parameters[0].id == T_Object) {
						switch(methodBinding.returnType.id) {
							case T_int :
								if (methodBinding.parameters[1].id == T_int && CharOperation.equals(methodBinding.selector, SET_INT_METHOD_NAME)
									&& methodBinding.returnType.id == T_void) {
									return SET_INT_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_byte :
								if (methodBinding.parameters[1].id == T_byte && CharOperation.equals(methodBinding.selector, SET_BYTE_METHOD_NAME)
									&& methodBinding.returnType.id == T_void) {
									return SET_BYTE_METHOD_NAME_AND_TYPE;
								}
								break;					
							case T_short :
								if (methodBinding.parameters[1].id == T_short && CharOperation.equals(methodBinding.selector, SET_SHORT_METHOD_NAME)
									&& methodBinding.returnType.id == T_void) {
									return SET_SHORT_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_char :
								if (methodBinding.parameters[1].id == T_char && CharOperation.equals(methodBinding.selector, SET_CHAR_METHOD_NAME)
									&& methodBinding.returnType.id == T_void) {
									return SET_CHAR_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_double :
								if (methodBinding.parameters[1].id == T_double && CharOperation.equals(methodBinding.selector, SET_DOUBLE_METHOD_NAME)
									&& methodBinding.returnType.id == T_void) {
									return SET_DOUBLE_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_float :
								if (methodBinding.parameters[1].id == T_float && CharOperation.equals(methodBinding.selector, SET_FLOAT_METHOD_NAME)
									&& methodBinding.returnType.id == T_void) {
									return SET_FLOAT_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_long :
								if (methodBinding.parameters[1].id == T_long && CharOperation.equals(methodBinding.selector, SET_LONG_METHOD_NAME)
									&& methodBinding.returnType.id == T_void) {
									return SET_LONG_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_boolean :
								if (methodBinding.parameters[1].id == T_boolean && CharOperation.equals(methodBinding.selector, SET_BOOLEAN_METHOD_NAME)
									&& methodBinding.returnType.id == T_void) {
									return SET_BOOLEAN_METHOD_NAME_AND_TYPE;
								}
								break;
							case T_Object :
								if (methodBinding.parameters[1].id == T_Object && CharOperation.equals(methodBinding.selector, SET_OBJECT_METHOD_NAME)
									&& methodBinding.returnType.id == T_void) {
									return SET_OBJECT_METHOD_NAME_AND_TYPE;
								}
						}
				}			
				break;
			case 'f':
				if (methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, FLOATVALUE_FLOAT_METHOD_NAME)
					&& methodBinding.returnType.id == T_float) {
						return FLOATVALUE_FLOAT_METHOD_NAME_AND_TYPE;
				}
				break;
			case 'd':
				if (methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, DOUBLEVALUE_DOUBLE_METHOD_NAME)
					&& methodBinding.returnType.id == T_double) {
						return DOUBLEVALUE_DOUBLE_METHOD_NAME_AND_TYPE;
				}
				break;
			case 'c':
				if (methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, CHARVALUE_CHARACTER_METHOD_NAME)
					&& methodBinding.returnType.id == T_char) {
						return CHARVALUE_CHARACTER_METHOD_NAME_AND_TYPE;
				}
				break;
			case 'b':
				if (methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, BOOLEANVALUE_BOOLEAN_METHOD_NAME)
					&& methodBinding.returnType.id == T_boolean) {
						return BOOLEANVALUE_BOOLEAN_METHOD_NAME_AND_TYPE;
				}
				if (methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, BYTEVALUE_BYTE_METHOD_NAME)
					&& methodBinding.returnType.id == T_byte) {
						return BYTEVALUE_BYTE_METHOD_NAME_AND_TYPE;
				}
				break;
			case 'l':
				if (methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, LONGVALUE_LONG_METHOD_NAME)
					&& methodBinding.returnType.id == T_long) {
						return LONGVALUE_LONG_METHOD_NAME_AND_TYPE;
				}
				break;
			case 'n':
				if (methodBinding.parameters.length == 2
					&& methodBinding.parameters[0].id == T_JavaLangClass
					&& methodBinding.parameters[1].isArrayType()
					&& ((ArrayBinding) methodBinding.parameters[1]).leafComponentType.id == T_int
					&& CharOperation.equals(methodBinding.selector, ARRAY_NEWINSTANCE_NAME)
					&& methodBinding.returnType instanceof ReferenceBinding
					&& CharOperation.equals(((ReferenceBinding) methodBinding.returnType).compoundName,JAVA_LANG_REFLECT_ARRAY)) {
						return ARRAY_NEWINSTANCE_METHOD_NAME_AND_TYPE;
				}
		}

	}
	return index;
}
/**
 * Return the index of the @methodBinding.
 *
 * Returns -1 if the @methodBinding is not a predefined methodBinding, 
 * the right index otherwise.
 *
 * @param methodBindingorg.eclipse.jdt.internal.compiler.lookup.MethodBinding
 * @return <CODE>int</CODE>
 */
public int indexOfWellKnownMethods(MethodBinding methodBinding) {
	int index = super.indexOfWellKnownMethods(methodBinding);
	if (index == -1) {
		char firstChar = methodBinding.selector[0];
		switch(firstChar) {
			case 'g':
				if (methodBinding.declaringClass.id == T_JavaLangClass
					&& methodBinding.parameters.length == 1
					&& methodBinding.parameters[0].id == T_JavaLangString
					&& CharOperation.equals(methodBinding.selector, GETDECLAREDFIELD_NAME)) {
						return GETDECLAREDFIELD_CLASS_METHOD;
				}
				if (methodBinding.declaringClass.id == T_JavaLangClass
					&& methodBinding.parameters.length == 2
					&& methodBinding.parameters[0].id == T_JavaLangString
					&& methodBinding.parameters[1].isArrayType()
					&& ((ArrayBinding) methodBinding.parameters[1]).leafComponentType.id == T_JavaLangClass
					&& CharOperation.equals(methodBinding.selector, GETDECLAREDMETHOD_NAME)) {
						return GETDECLAREDMETHOD_CLASS_METHOD;
				}
				if (methodBinding.declaringClass.id == T_JavaLangClass
					&& methodBinding.parameters.length == 1
					&& methodBinding.parameters[0].isArrayType()
					&& ((ArrayBinding) methodBinding.parameters[0]).leafComponentType.id == T_JavaLangClass
					&& CharOperation.equals(methodBinding.selector, GETDECLAREDCONSTRUCTOR_NAME)) {
						return GETDECLAREDCONSTRUCTOR_CLASS_METHOD;
				}
				if (CharOperation.equals(methodBinding.declaringClass.compoundName, JAVA_LANG_REFLECT_FIELD)
					&& methodBinding.parameters.length == 1
					&& methodBinding.parameters[0].id == T_Object) {
						switch(methodBinding.returnType.id) {
							case T_int :
								if (CharOperation.equals(methodBinding.selector, GET_INT_METHOD_NAME)) {
									return GET_INT_METHOD;
								}
								break;
							case T_byte :
								if (CharOperation.equals(methodBinding.selector, GET_BYTE_METHOD_NAME)) {
									return GET_BYTE_METHOD;
								}
								break;					
							case T_short :
								if (CharOperation.equals(methodBinding.selector, GET_SHORT_METHOD_NAME)) {
									return GET_SHORT_METHOD;
								}
								break;
							case T_char :
								if (CharOperation.equals(methodBinding.selector, GET_CHAR_METHOD_NAME)) {
									return GET_CHAR_METHOD;
								}
								break;
							case T_double :
								if (CharOperation.equals(methodBinding.selector, GET_DOUBLE_METHOD_NAME)) {
									return GET_DOUBLE_METHOD;
								}
								break;
							case T_float :
								if (CharOperation.equals(methodBinding.selector, GET_FLOAT_METHOD_NAME)) {
									return GET_FLOAT_METHOD;
								}
								break;
							case T_long :
								if (CharOperation.equals(methodBinding.selector, GET_LONG_METHOD_NAME)) {
									return GET_LONG_METHOD;
								}
								break;
							case T_boolean :
								if (CharOperation.equals(methodBinding.selector, GET_BOOLEAN_METHOD_NAME)) {
									return GET_BOOLEAN_METHOD;
								}
								break;
							case T_Object :
								if (CharOperation.equals(methodBinding.selector, GET_OBJECT_METHOD_NAME)) {
									return GET_OBJECT_METHOD;
								}
						}
				}
				break;
			case 'i':
				if (methodBinding.declaringClass.id == T_JavaLangInteger
					&& methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, INTVALUE_INTEGER_METHOD_NAME)) {
						return INTVALUE_INTEGER_METHOD;
				}
				if (CharOperation.equals(methodBinding.declaringClass.compoundName, JAVA_LANG_REFLECT_METHOD)
					&& methodBinding.parameters.length == 2
					&& methodBinding.parameters[0].id == T_JavaLangObject
					&& methodBinding.parameters[1].isArrayType()
					&& ((ArrayBinding) methodBinding.parameters[1]).leafComponentType.id == T_JavaLangObject
					&& CharOperation.equals(methodBinding.selector, INVOKE_METHOD_METHOD_NAME)) {
						return INVOKE_METHOD_METHOD;
				}			
				break;
			case 'b':
				if (methodBinding.declaringClass.id == T_JavaLangByte
					&& methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, BYTEVALUE_BYTE_METHOD_NAME)) {
						return BYTEVALUE_BYTE_METHOD;
				}
				if (methodBinding.declaringClass.id == T_JavaLangBoolean
					&& methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, BOOLEANVALUE_BOOLEAN_METHOD_NAME)) {
						return BOOLEANVALUE_BOOLEAN_METHOD;
				}
				break;
			case 's': 				
				if (methodBinding.declaringClass.id == T_JavaLangShort
					&& methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, SHORTVALUE_SHORT_METHOD_NAME)) {
						return SHORTVALUE_SHORT_METHOD;
				}
				if (CharOperation.equals(methodBinding.declaringClass.compoundName, JAVA_LANG_REFLECT_ACCESSIBLEOBJECT)
					&& methodBinding.parameters.length == 1
					&& methodBinding.parameters[0].id == T_boolean
					&& methodBinding.selector.length == 13
					&& CharOperation.equals(methodBinding.selector, SETACCESSIBLE_NAME)) {
						return SETACCESSIBLE_ACCESSIBLEOBJECT_METHOD;
				}
				if (CharOperation.equals(methodBinding.declaringClass.compoundName, JAVA_LANG_REFLECT_FIELD)
					&& methodBinding.returnType.id == T_void
					&& methodBinding.parameters.length == 2
					&& methodBinding.parameters[0].id == T_Object) {
						switch(methodBinding.returnType.id) {
							case T_int :
								if (methodBinding.parameters[1].id == T_int && CharOperation.equals(methodBinding.selector, SET_INT_METHOD_NAME)) {
									return SET_INT_METHOD;
								}
								break;
							case T_byte :
								if (methodBinding.parameters[1].id == T_byte && CharOperation.equals(methodBinding.selector, SET_BYTE_METHOD_NAME)) {
									return SET_BYTE_METHOD;
								}
								break;					
							case T_short :
								if (methodBinding.parameters[1].id == T_short && CharOperation.equals(methodBinding.selector, SET_SHORT_METHOD_NAME)) {
									return SET_SHORT_METHOD;
								}
								break;
							case T_char :
								if (methodBinding.parameters[1].id == T_char && CharOperation.equals(methodBinding.selector, SET_CHAR_METHOD_NAME)) {
									return SET_CHAR_METHOD;
								}
								break;
							case T_double :
								if (methodBinding.parameters[1].id == T_double && CharOperation.equals(methodBinding.selector, SET_DOUBLE_METHOD_NAME)) {
									return SET_DOUBLE_METHOD;
								}
								break;
							case T_float :
								if (methodBinding.parameters[1].id == T_float && CharOperation.equals(methodBinding.selector, SET_FLOAT_METHOD_NAME)) {
									return SET_FLOAT_METHOD;
								}
								break;
							case T_long :
								if (methodBinding.parameters[1].id == T_long && CharOperation.equals(methodBinding.selector, SET_LONG_METHOD_NAME)) {
									return SET_LONG_METHOD;
								}
								break;
							case T_boolean :
								if (methodBinding.parameters[1].id == T_boolean && CharOperation.equals(methodBinding.selector, SET_BOOLEAN_METHOD_NAME)) {
									return SET_BOOLEAN_METHOD;
								}
								break;
							case T_Object :
								if (methodBinding.parameters[1].id == T_Object && CharOperation.equals(methodBinding.selector, SET_OBJECT_METHOD_NAME)) {
									return SET_OBJECT_METHOD;
								}
						}
				}			
				break;
			case 'f':
				if (methodBinding.declaringClass.id == T_JavaLangFloat
					&& methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, FLOATVALUE_FLOAT_METHOD_NAME)) {
						return FLOATVALUE_FLOAT_METHOD;
				}
				break;
			case 'd':
				if (methodBinding.declaringClass.id == T_JavaLangDouble
					&& methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, DOUBLEVALUE_DOUBLE_METHOD_NAME)) {
						return DOUBLEVALUE_DOUBLE_METHOD;
				}
				break;
			case 'c':
				if (methodBinding.declaringClass.id == T_JavaLangCharacter
					&& methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, CHARVALUE_CHARACTER_METHOD_NAME)) {
						return CHARVALUE_CHARACTER_METHOD;
				}
				break;
			case 'l':
				if (methodBinding.declaringClass.id == T_JavaLangLong
					&& methodBinding.parameters.length == 0
					&& CharOperation.equals(methodBinding.selector, LONGVALUE_LONG_METHOD_NAME)) {
						return LONGVALUE_LONG_METHOD;
				}
				break;
			case 'n':
				if (CharOperation.equals(methodBinding.declaringClass.compoundName, JAVA_LANG_REFLECT_ARRAY)
					&& methodBinding.parameters.length == 2
					&& methodBinding.parameters[0].id == T_JavaLangClass
					&& methodBinding.parameters[1].isArrayType()
					&& ((ArrayBinding) methodBinding.parameters[1]).leafComponentType.id == T_int
					&& CharOperation.equals(methodBinding.selector, ARRAY_NEWINSTANCE_NAME)) {
						return NEWINSTANCE_ARRAY_METHOD;
				}
				break;
		}
	}
	return index;
}
/**
 * Return the index of the @typeBinding
 *
 * Returns -1 if the @typeBinding is not a predefined binding, the right index 
 * otherwise.
 *
 * @param typeBinding org.eclipse.jdt.internal.compiler.lookup.TypeBinding
 * @return <CODE>int</CODE>
 */
public int indexOfWellKnownTypes(TypeBinding typeBinding) {
	int index = super.indexOfWellKnownTypes(typeBinding);
	if (index == -1) {
		if (!typeBinding.isBaseType() && !typeBinding.isArrayType()) {
			ReferenceBinding type = (ReferenceBinding) typeBinding;
			if (type.compoundName.length == 4) {
				if (CharOperation.equals(JAVA_LANG_REFLECT_FIELD, type.compoundName)) {
					return JAVA_LANG_REFLECT_FIELD_TYPE;
				}
				if (CharOperation.equals(JAVA_LANG_REFLECT_METHOD, type.compoundName)) {
					return JAVA_LANG_REFLECT_METHOD_TYPE;
				}
				if (CharOperation.equals(JAVA_LANG_REFLECT_ARRAY, type.compoundName)) {
					return JAVA_LANG_REFLECT_ARRAY_TYPE;
				}
				if (CharOperation.equals(JAVA_LANG_REFLECT_ACCESSIBLEOBJECT, type.compoundName)) {
					return JAVA_LANG_REFLECT_ACCESSIBLEOBJECT_TYPE;
				}
			}
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @param MethodBinding aMethodBinding
 * @return <CODE>int</CODE>
 */
public int literalIndex(MethodBinding aMethodBinding) {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	int indexWellKnownMethod;
	if ((indexWellKnownMethod = super.indexOfWellKnownMethods(aMethodBinding)) == -1) {
		if ((indexWellKnownMethod = indexOfWellKnownMethods(aMethodBinding)) == -1) {
			if (aMethodBinding.declaringClass.isInterface()) {
				// Lookinf into the interface method ref table
				if ((index = interfaceMethodCache.get(aMethodBinding)) < 0) {
					classIndex = literalIndex(aMethodBinding.declaringClass);
					nameAndTypeIndex =
						literalIndexForMethods(
							literalIndex(aMethodBinding.constantPoolName()),
							literalIndex(aMethodBinding.signature()),
							aMethodBinding);
					index = interfaceMethodCache.put(aMethodBinding, currentIndex++);
					// Write the interface method ref constant into the constant pool
					// First add the tag
					writeU1(InterfaceMethodRefTag);
					// Then write the class index
					writeU2(classIndex);
					// The write the nameAndType index
					writeU2(nameAndTypeIndex);
				}
			} else {
				// Lookinf into the method ref table
				if ((index = methodCache.get(aMethodBinding)) < 0) {
					classIndex = literalIndex(aMethodBinding.declaringClass);
					nameAndTypeIndex =
						literalIndexForMethods(
							literalIndex(aMethodBinding.constantPoolName()),
							literalIndex(aMethodBinding.signature()),
							aMethodBinding);
					index = methodCache.put(aMethodBinding, currentIndex++);
					// Write the method ref constant into the constant pool
					// First add the tag
					writeU1(MethodRefTag);
					// Then write the class index
					writeU2(classIndex);
					// The write the nameAndType index
					writeU2(nameAndTypeIndex);
				}
			}
		} else {
			// This is a well known method
			if ((index = wellKnownMethods[indexWellKnownMethod]) == 0) {
				// this methods was not inserted yet
				if (aMethodBinding.declaringClass.isInterface()) {
					// Lookinf into the interface method ref table
					classIndex = literalIndex(aMethodBinding.declaringClass);
					nameAndTypeIndex =
						literalIndexForMethods(
							literalIndex(aMethodBinding.constantPoolName()),
							literalIndex(aMethodBinding.signature()),
							aMethodBinding);
					index = wellKnownMethods[indexWellKnownMethod] = currentIndex++;
					// Write the interface method ref constant into the constant pool
					// First add the tag
					writeU1(InterfaceMethodRefTag);
					// Then write the class index
					writeU2(classIndex);
					// The write the nameAndType index
					writeU2(nameAndTypeIndex);
				} else {
					// Lookinf into the method ref table
					classIndex = literalIndex(aMethodBinding.declaringClass);
					nameAndTypeIndex =
						literalIndexForMethods(
							literalIndex(aMethodBinding.constantPoolName()),
							literalIndex(aMethodBinding.signature()),
							aMethodBinding);
					index = wellKnownMethods[indexWellKnownMethod] = currentIndex++;
					// Write the method ref constant into the constant pool
					// First add the tag
					writeU1(MethodRefTag);
					// Then write the class index
					writeU2(classIndex);
					// The write the nameAndType index
					writeU2(nameAndTypeIndex);
				}
			}
		}
	} else {
		index = super.literalIndex(aMethodBinding);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndex(TypeBinding aTypeBinding) {
	int index;
	int nameIndex;
	int indexWellKnownType;
	if ((indexWellKnownType = super.indexOfWellKnownTypes(aTypeBinding)) == -1) {
		if ((indexWellKnownType = indexOfWellKnownTypes(aTypeBinding)) == -1) {
			if ((index = classCache.get(aTypeBinding)) < 0) {
				// The entry doesn't exit yet
				nameIndex = literalIndex(aTypeBinding.constantPoolName());
				index = classCache.put(aTypeBinding, currentIndex++);
				writeU1(ClassTag);
				// Then add the 8 bytes representing the long
				writeU2(nameIndex);
			}
		} else {
			if ((index = wellKnownTypes[indexWellKnownType]) == 0) {
				// Need to insert that binding
				nameIndex = literalIndex(aTypeBinding.constantPoolName());
				index = wellKnownTypes[indexWellKnownType] = currentIndex++;
				writeU1(ClassTag);
				// Then add the 8 bytes representing the long
				writeU2(nameIndex);
			}
		}
	} else {
		index = super.literalIndex(aTypeBinding);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangBooleanBooleanValue() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[BOOLEANVALUE_BOOLEAN_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangBoolean();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[BOOLEANVALUE_BOOLEAN_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(BOOLEANVALUE_BOOLEAN_METHOD_NAME);
			int typeIndex = literalIndex(BOOLEANVALUE_BOOLEAN_METHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[BOOLEANVALUE_BOOLEAN_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[BOOLEANVALUE_BOOLEAN_METHOD_NAME_AND_TYPE] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangByteByteValue() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[BYTEVALUE_BYTE_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangByte();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[BYTEVALUE_BYTE_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(BYTEVALUE_BYTE_METHOD_NAME);
			int typeIndex = literalIndex(BYTEVALUE_BYTE_METHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[BYTEVALUE_BYTE_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[BYTEVALUE_BYTE_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangCharacterCharValue() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[CHARVALUE_CHARACTER_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangCharacter();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[CHARVALUE_CHARACTER_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(CHARVALUE_CHARACTER_METHOD_NAME);
			int typeIndex = literalIndex(CHARVALUE_CHARACTER_METHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[CHARVALUE_CHARACTER_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[CHARVALUE_CHARACTER_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangClassGetDeclaredConstructor() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[GETDECLAREDCONSTRUCTOR_CLASS_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangClass();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[GETDECLAREDCONSTRUCTOR_CLASS_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(GETDECLAREDCONSTRUCTOR_NAME);
			int typeIndex = literalIndex(GETDECLAREDCONSTRUCTOR_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[GETDECLAREDCONSTRUCTOR_CLASS_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[GETDECLAREDCONSTRUCTOR_CLASS_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangClassGetDeclaredField() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[GETDECLAREDFIELD_CLASS_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangClass();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[GETDECLAREDFIELD_CLASS_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(GETDECLAREDFIELD_NAME);
			int typeIndex = literalIndex(GETDECLAREDFIELD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[GETDECLAREDFIELD_CLASS_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[GETDECLAREDFIELD_CLASS_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangClassGetDeclaredMethod() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[GETDECLAREDMETHOD_CLASS_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangClass();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[GETDECLAREDMETHOD_CLASS_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(GETDECLAREDMETHOD_NAME);
			int typeIndex = literalIndex(GETDECLAREDMETHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[GETDECLAREDMETHOD_CLASS_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[GETDECLAREDMETHOD_CLASS_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangDoubleDoubleValue() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[DOUBLEVALUE_DOUBLE_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangDouble();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[DOUBLEVALUE_DOUBLE_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(DOUBLEVALUE_DOUBLE_METHOD_NAME);
			int typeIndex = literalIndex(DOUBLEVALUE_DOUBLE_METHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[DOUBLEVALUE_DOUBLE_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[DOUBLEVALUE_DOUBLE_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangFloatFloatValue() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[FLOATVALUE_FLOAT_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangFloat();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[FLOATVALUE_FLOAT_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(FLOATVALUE_FLOAT_METHOD_NAME);
			int typeIndex = literalIndex(FLOATVALUE_FLOAT_METHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[FLOATVALUE_FLOAT_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[FLOATVALUE_FLOAT_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangIntegerIntValue() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[INTVALUE_INTEGER_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangInteger();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[INTVALUE_INTEGER_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(INTVALUE_INTEGER_METHOD_NAME);
			int typeIndex = literalIndex(INTVALUE_INTEGER_METHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[INTVALUE_INTEGER_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[INTVALUE_INTEGER_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangLongLongValue() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[LONGVALUE_LONG_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangLong();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[LONGVALUE_LONG_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(LONGVALUE_LONG_METHOD_NAME);
			int typeIndex = literalIndex(LONGVALUE_LONG_METHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[LONGVALUE_LONG_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[LONGVALUE_LONG_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangReflectAccessibleObject() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_REFLECT_ACCESSIBLEOBJECT_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(JAVALANGREFLECTACCESSIBLEOBJECT_CONSTANTPOOLNAME);
		index = wellKnownTypes[JAVA_LANG_REFLECT_ACCESSIBLEOBJECT_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangReflectAccessibleObjectSetAccessible() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[SETACCESSIBLE_ACCESSIBLEOBJECT_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangReflectAccessibleObject();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[SETACCESSIBLE_ACCESSIBLEOBJECT_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(SETACCESSIBLE_NAME);
			int typeIndex = literalIndex(SETACCESSIBLE_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[SETACCESSIBLE_ACCESSIBLEOBJECT_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[SETACCESSIBLE_ACCESSIBLEOBJECT_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangReflectArray() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_REFLECT_ARRAY_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(JAVALANGREFLECTARRAY_CONSTANTPOOLNAME);
		index = wellKnownTypes[JAVA_LANG_REFLECT_ARRAY_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangReflectArrayNewInstance() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[NEWINSTANCE_ARRAY_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangReflectArray();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[ARRAY_NEWINSTANCE_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(ARRAY_NEWINSTANCE_NAME);
			int typeIndex = literalIndex(ARRAY_NEWINSTANCE_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[ARRAY_NEWINSTANCE_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[NEWINSTANCE_ARRAY_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangReflectField() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_REFLECT_FIELD_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(JAVALANGREFLECTFIELD_CONSTANTPOOLNAME);
		index = wellKnownTypes[JAVA_LANG_REFLECT_FIELD_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangReflectMethod() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_REFLECT_METHOD_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(JAVALANGREFLECTMETHOD_CONSTANTPOOLNAME);
		index = wellKnownTypes[JAVA_LANG_REFLECT_METHOD_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangReflectMethodInvoke() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[INVOKE_METHOD_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangReflectMethod();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[INVOKE_METHOD_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(INVOKE_METHOD_METHOD_NAME);
			int typeIndex = literalIndex(INVOKE_METHOD_METHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[INVOKE_METHOD_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[INVOKE_METHOD_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangShortShortValue() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[SHORTVALUE_SHORT_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangShort();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[SHORTVALUE_SHORT_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(SHORTVALUE_SHORT_METHOD_NAME);
			int typeIndex = literalIndex(SHORTVALUE_SHORT_METHOD_SIGNATURE);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[SHORTVALUE_SHORT_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[SHORTVALUE_SHORT_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding 
 * nameAndType constant with nameIndex, typeIndex.
 *
 * @param int nameIndex
 * @param int nameIndex
 * @param org.eclipse.jdt.internal.compiler.lookup.MethodBinding a methodBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForMethods(int nameIndex, int typeIndex, MethodBinding key) {
	int index;
	int indexOfWellKnownMethodNameAndType;
	if ((indexOfWellKnownMethodNameAndType = super.indexOfWellKnownMethodNameAndType(key)) == -1) {
		if ((indexOfWellKnownMethodNameAndType = indexOfWellKnownMethodNameAndType(key)) == -1) {
			// check if the entry exists
			if ((index = nameAndTypeCacheForMethods.get(key)) == -1) {
				// The entry doesn't exit yet
				index = nameAndTypeCacheForMethods.put(key, currentIndex++);
				writeU1(NameAndTypeTag);
				writeU2(nameIndex);
				writeU2(typeIndex);
			}
		} else {
			if ((index = wellKnownMethodNameAndTypes[indexOfWellKnownMethodNameAndType]) == 0) {
				index = wellKnownMethodNameAndTypes[indexOfWellKnownMethodNameAndType] = currentIndex++;
				writeU1(NameAndTypeTag);
				writeU2(nameIndex);
				writeU2(typeIndex);
			}
		}
	} else {
		index = super.literalIndexForMethods(nameIndex,typeIndex,key);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexJavaLangReflectFieldGetter(int typeID) {
	int index = 0;
	int nameAndTypeIndex = 0;
	int classIndex = 0;
	switch (typeID) {
		case T_int :
			if ((index = wellKnownMethods[GET_INT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[GET_INT_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(GET_INT_METHOD_NAME);
					int typeIndex = literalIndex(GET_INT_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[GET_INT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[GET_INT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_byte :
			if ((index = wellKnownMethods[GET_BYTE_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[GET_BYTE_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(GET_BYTE_METHOD_NAME);
					int typeIndex = literalIndex(GET_BYTE_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[GET_BYTE_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[GET_BYTE_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_short :
			if ((index = wellKnownMethods[GET_SHORT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[GET_SHORT_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(GET_SHORT_METHOD_NAME);
					int typeIndex = literalIndex(GET_SHORT_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[GET_SHORT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[GET_SHORT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_long :
			if ((index = wellKnownMethods[GET_LONG_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[GET_LONG_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(GET_LONG_METHOD_NAME);
					int typeIndex = literalIndex(GET_LONG_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[GET_LONG_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[GET_LONG_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_float :
			if ((index = wellKnownMethods[GET_FLOAT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[GET_FLOAT_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(GET_FLOAT_METHOD_NAME);
					int typeIndex = literalIndex(GET_FLOAT_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[GET_FLOAT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[GET_FLOAT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_double :
			if ((index = wellKnownMethods[GET_DOUBLE_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[GET_DOUBLE_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(GET_DOUBLE_METHOD_NAME);
					int typeIndex = literalIndex(GET_DOUBLE_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[GET_DOUBLE_METHOD_NAME_AND_TYPE] =
							currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[GET_DOUBLE_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_char :
			if ((index = wellKnownMethods[GET_CHAR_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[GET_CHAR_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(GET_CHAR_METHOD_NAME);
					int typeIndex = literalIndex(GET_CHAR_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[GET_CHAR_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[GET_CHAR_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_boolean :
			if ((index = wellKnownMethods[GET_BOOLEAN_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[GET_BOOLEAN_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(GET_BOOLEAN_METHOD_NAME);
					int typeIndex = literalIndex(GET_BOOLEAN_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[GET_BOOLEAN_METHOD_NAME_AND_TYPE] =
							currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[GET_BOOLEAN_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		default :
			if ((index = wellKnownMethods[GET_OBJECT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[GET_OBJECT_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(GET_OBJECT_METHOD_NAME);
					int typeIndex = literalIndex(GET_OBJECT_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[GET_OBJECT_METHOD_NAME_AND_TYPE] =
							currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[GET_OBJECT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
		}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexJavaLangReflectFieldSetter(int typeID) {
	int index = 0;
	int nameAndTypeIndex = 0;
	int classIndex = 0;
	switch (typeID) {
		case T_int :
			if ((index = wellKnownMethods[SET_INT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[SET_INT_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(SET_INT_METHOD_NAME);
					int typeIndex = literalIndex(SET_INT_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[SET_INT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[SET_INT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_byte :
			if ((index = wellKnownMethods[SET_BYTE_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[SET_BYTE_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(SET_BYTE_METHOD_NAME);
					int typeIndex = literalIndex(SET_BYTE_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[SET_BYTE_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[SET_BYTE_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_short :
			if ((index = wellKnownMethods[SET_SHORT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[SET_SHORT_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(SET_SHORT_METHOD_NAME);
					int typeIndex = literalIndex(SET_SHORT_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[SET_SHORT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[SET_SHORT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_long :
			if ((index = wellKnownMethods[SET_LONG_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[SET_LONG_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(SET_LONG_METHOD_NAME);
					int typeIndex = literalIndex(SET_LONG_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[SET_LONG_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[SET_LONG_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_float :
			if ((index = wellKnownMethods[SET_FLOAT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[SET_FLOAT_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(SET_FLOAT_METHOD_NAME);
					int typeIndex = literalIndex(SET_FLOAT_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[SET_FLOAT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[SET_FLOAT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_double :
			if ((index = wellKnownMethods[SET_DOUBLE_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[SET_DOUBLE_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(SET_DOUBLE_METHOD_NAME);
					int typeIndex = literalIndex(SET_DOUBLE_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[SET_DOUBLE_METHOD_NAME_AND_TYPE] =
							currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[SET_DOUBLE_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_char :
			if ((index = wellKnownMethods[SET_CHAR_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[SET_CHAR_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(SET_CHAR_METHOD_NAME);
					int typeIndex = literalIndex(SET_CHAR_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[SET_CHAR_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[SET_CHAR_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_boolean :
			if ((index = wellKnownMethods[SET_BOOLEAN_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[SET_BOOLEAN_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(SET_BOOLEAN_METHOD_NAME);
					int typeIndex = literalIndex(SET_BOOLEAN_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[SET_BOOLEAN_METHOD_NAME_AND_TYPE] =
							currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[SET_BOOLEAN_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		default :
			if ((index = wellKnownMethods[SET_OBJECT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangReflectField();
				if ((nameAndTypeIndex =
					wellKnownMethodNameAndTypes[SET_OBJECT_METHOD_NAME_AND_TYPE])
					== 0) {
					int nameIndex = literalIndex(SET_OBJECT_METHOD_NAME);
					int typeIndex = literalIndex(SET_OBJECT_METHOD_SIGNATURE);
					nameAndTypeIndex =
						wellKnownMethodNameAndTypes[SET_OBJECT_METHOD_NAME_AND_TYPE] =
							currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[SET_OBJECT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
		}
	return index;
}
}
