/* *******************************************************************
 * Copyright (c) 2013 VMware
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation 
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile.tests;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeTypeAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisTypeAnnos;
import org.aspectj.apache.bcel.classfile.annotation.TypeAnnotationGen;

public class TypeAnnotationsTest extends BcelTestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public Attribute getAttribute(Attribute[] attrs, byte tag) {
		for (Attribute attr: attrs) {
			if (attr.getTag() == tag) {
				return attr;
			}
		}
		return null;
	}
	
	public void testClassTypeParameter() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnClassTypeParameter");
		RuntimeVisTypeAnnos rvta = (RuntimeVisTypeAnnos)getAttribute(jc.getAttributes(), Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS);
		assertTrue(rvta.areVisible());
		TypeAnnotationGen[] tas = rvta.getTypeAnnotations();
		assertEquals(2,tas.length);
		checkTypeAnnotationClassTypeParameter(tas[0],0,"@Anno");
		checkTypeAnnotationClassTypeParameter(tas[1],1,"@Anno(value=2)");
	}
	
	public void testMethodTypeParameter() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnMethodTypeParameter");
		Method m = getMethod(jc, "m");
		RuntimeVisTypeAnnos rvta = (RuntimeVisTypeAnnos)getAttribute(m.getAttributes(), Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS);
		assertTrue(rvta.areVisible());
		TypeAnnotationGen[] tas = rvta.getTypeAnnotations();
		assertEquals(1,tas.length);
		checkTypeAnnotationMethodTypeParameter(tas[0],0,"@Anno");
	}
	
	public void testSuperinterface() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnSuperinterface1");
		RuntimeVisTypeAnnos rvta = (RuntimeVisTypeAnnos)getAttribute(jc.getAttributes(), Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS);
		assertTrue(rvta.areVisible());
		TypeAnnotationGen[] tas = rvta.getTypeAnnotations();
		assertEquals(1,tas.length);
		TypeAnnotationGen ta = tas[0];
		checkTypeAnnotationClassExtends(ta, 0, "@Anno");
	}
	
	public void testSupertypes() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnSupertypes");
		RuntimeVisTypeAnnos rvta = (RuntimeVisTypeAnnos)getAttribute(jc.getAttributes(), Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS);
		assertTrue(rvta.areVisible());
		TypeAnnotationGen[] tas = rvta.getTypeAnnotations();
		assertEquals(3,tas.length);
		checkTypeAnnotationClassExtends(tas[0],-1,"@Anno(value=1)");
		checkTypeAnnotationClassExtends(tas[1],0,"@Anno");
		checkTypeAnnotationClassExtends(tas[2],1,"@Anno(value=2)");
	}

	public void testClassTypeParameterBound() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnClassTypeParameterBound");
		RuntimeVisTypeAnnos rvta = (RuntimeVisTypeAnnos)getAttribute(jc.getAttributes(), Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS);
		assertTrue(rvta.areVisible());
		TypeAnnotationGen[] tas = rvta.getTypeAnnotations();
		assertEquals(3,tas.length);
		checkTypeAnnotationClassTypeParameterBound(tas[0],0,0,"@Anno");
		checkTypePath(tas[0],TypeAnnotationGen.NO_TYPE_PATH);
		checkTypeAnnotationClassTypeParameterBound(tas[1],0,1,"@Anno(value=2)");
		checkTypePath(tas[1],TypeAnnotationGen.NO_TYPE_PATH);
		checkTypeAnnotationClassTypeParameterBound(tas[2],0,1,"@Anno(value=3)");
		checkTypePath(tas[2],new int[]{TypeAnnotationGen.TYPE_PATH_ENTRY_KIND_TYPE_ARGUMENT,0});
	}

	public void testMethodTypeParameterBound() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnMethodTypeParameterBound");
		Method m = getMethod(jc, "m");
		RuntimeVisTypeAnnos rvta = (RuntimeVisTypeAnnos)getAttribute(m.getAttributes(), Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS);
		assertTrue(rvta.areVisible());
		TypeAnnotationGen[] tas = rvta.getTypeAnnotations();
		assertEquals(3,tas.length);
		checkTypeAnnotationMethodTypeParameterBound(tas[0],0,0,"@Anno");
		checkTypePath(tas[0],TypeAnnotationGen.NO_TYPE_PATH);
		checkTypeAnnotationMethodTypeParameterBound(tas[1],0,1,"@Anno(value=2)");
		checkTypePath(tas[1],TypeAnnotationGen.NO_TYPE_PATH);
		checkTypeAnnotationMethodTypeParameterBound(tas[2],0,1,"@Anno(value=3)");
		checkTypePath(tas[2],new int[]{TypeAnnotationGen.TYPE_PATH_ENTRY_KIND_TYPE_ARGUMENT,1});
	}

	public void testField() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnField");
		Field f = getField(jc,"f1");
		RuntimeVisTypeAnnos rvta = (RuntimeVisTypeAnnos)getAttribute(f.getAttributes(), Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS);
		assertTrue(rvta.areVisible());
		TypeAnnotationGen[] tas = rvta.getTypeAnnotations();
		assertEquals(1,tas.length);
		checkTypeAnnotationField(tas[0],"@Anno");
		checkTypePath(tas[0],TypeAnnotationGen.NO_TYPE_PATH);
		
		tas = getTypeAnnotations(getField(jc,"f2"),true);
		checkTypeAnnotationField(tas[0],"@Anno");
		checkTypePath(tas[0],new int[]{TypeAnnotationGen.TYPE_PATH_ENTRY_KIND_TYPE_ARGUMENT,0});
		
		tas = getTypeAnnotations(getField(jc,"f3"),true);
		checkTypeAnnotationField(tas[0],"@Anno");
		checkTypePath(tas[0],new int[]{TypeAnnotationGen.TYPE_PATH_ENTRY_KIND_ARRAY,0});
		
		tas = getTypeAnnotations(getField(jc,"f4"),true);
		checkTypeAnnotationField(tas[0],"@Anno");
		checkTypePath(tas[0],new int[]{
				TypeAnnotationGen.TYPE_PATH_ENTRY_KIND_ARRAY,0,
				TypeAnnotationGen.TYPE_PATH_ENTRY_KIND_TYPE_ARGUMENT,0
				});
	}

	public void testMethodReturn() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnMethodReturn");
		TypeAnnotationGen[] tas = getTypeAnnotations(getMethod(jc,"m"), true);
		checkTypeAnnotationMethodReturn(tas[0],"@Anno");
		checkTypePath(tas[0],TypeAnnotationGen.NO_TYPE_PATH);
	}

	public void testMethodReceiver() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnMethodReceiver");
		TypeAnnotationGen[] tas = getTypeAnnotations(getMethod(jc,"m"), true);
		checkTypeAnnotationMethodReceiver(tas[0],"@Anno");
		checkTypePath(tas[0],TypeAnnotationGen.NO_TYPE_PATH);
	}

	public void testMethodFormalParameter() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnMethodFormalParameter");
		TypeAnnotationGen[] tas = getTypeAnnotations(getMethod(jc,"m"), true);
		checkTypeAnnotationMethodFormalParameter(tas[0],0, "@Anno");
		checkTypePath(tas[0],TypeAnnotationGen.NO_TYPE_PATH);
	}

	public void testThrows() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnThrows");
		TypeAnnotationGen[] tas = getTypeAnnotations(getMethod(jc,"m"), true);
		checkTypeAnnotationThrows(tas[0],0, "@Anno");
		checkTypePath(tas[0],TypeAnnotationGen.NO_TYPE_PATH);
		checkTypeAnnotationThrows(tas[1],1, "@Anno(value=2)");
		checkTypePath(tas[1],TypeAnnotationGen.NO_TYPE_PATH);
	}
	
	public void testLocalVariable() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnLocalVariable");
		// TODO I think the attribute should be on the code for the method, not the method
		TypeAnnotationGen[] tas = getTypeAnnotations(getMethod(jc,"m").getAttributes(), true);
		assertEquals(1,tas.length);
		checkTypeAnnotationLocalVariable(tas[0],new int[]{11,8,1}, "@Anno");
	}

	public void testResourceVariable() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnResourceVariable");
		// TODO I think the attribute should be on the code for the method, not the method
		TypeAnnotationGen[] tas = getTypeAnnotations(getMethod(jc,"m").getAttributes(), true);
		assertEquals(2,tas.length);
		checkTypeAnnotationResourceVariable(tas[0],new int[]{17,204,1}, "@Anno");
		checkTypeAnnotationResourceVariable(tas[1],new int[]{36,114,3}, "@Anno(value=99)");
	}
	
	public void testExceptionParameter() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnExceptionParameter");
		// TODO I think the attribute should be on the code for the method, not the method
		TypeAnnotationGen[] tas = getTypeAnnotations(getMethod(jc,"m").getAttributes(), true);
		assertEquals(2,tas.length);
		checkTypeAnnotationExceptionParameter(tas[0],0, "@Anno(value=99)");
		checkTypeAnnotationExceptionParameter(tas[1],0, "@Anno");
	}

	public void testInstanceOf() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnInstanceOf");
		// TODO I think the attribute should be on the code for the method, not the method
		TypeAnnotationGen[] tas = getTypeAnnotations(getMethod(jc,"m").getAttributes(), true);
		assertEquals(2,tas.length);
		checkTypeAnnotationInstanceOf(tas[0],3, "@Anno(value=1)");
		checkTypeAnnotationInstanceOf(tas[1],18, "@Anno(value=1)");
	}

	public void testNew() throws Exception {
		JavaClass jc = getClassFromJava8Jar("TypeAnnoOnNew");
		// TODO I think the attribute should be on the code for the method, not the method
		TypeAnnotationGen[] tas = getTypeAnnotations(getMethod(jc,"m").getAttributes(), true);
		assertEquals(4,tas.length);
		checkTypeAnnotationNew(tas[0],0, "@Anno");
		checkTypePath(tas[0],TypeAnnotationGen.NO_TYPE_PATH);
		
		// TODO type path bugs in javac b90 according to the spec
//		checkTypeAnnotationNew(tas[1],8, "@Anno(value=2)");
//		checkTypePath(tas[1],new int[]{
//				TypeAnnotationGen.TYPE_PATH_ENTRY_KIND_ARRAY,0
//				});
//		checkTypeAnnotationNew(tas[2],13, "@Anno(value=4)");
//		checkTypePath(tas[2],TypeAnnotationGen.NO_TYPE_PATH);
//		checkTypeAnnotationNew(tas[3],13, "@Anno(value=3)");
//		checkTypePath(tas[3],new int[]{
//				TypeAnnotationGen.TYPE_PATH_ENTRY_KIND_ARRAY,0,
//				TypeAnnotationGen.TYPE_PATH_ENTRY_KIND_ARRAY,0
//				});
	}


	// ---
	private TypeAnnotationGen[] getTypeAnnotations(Attribute[] attrs, boolean visible) {
		RuntimeTypeAnnos rvta = (RuntimeTypeAnnos)getAttribute(attrs, visible?Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS:Constants.ATTR_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS);
		return rvta.getTypeAnnotations();
	}
	
	private TypeAnnotationGen[] getTypeAnnotations(Method m, boolean visible) {
		RuntimeTypeAnnos rvta = (RuntimeTypeAnnos)getAttribute(m.getAttributes(), visible?Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS:Constants.ATTR_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS);
		return rvta.getTypeAnnotations();
	}
	
	private TypeAnnotationGen[] getTypeAnnotations(Field f, boolean visible) {
		RuntimeTypeAnnos rvta = (RuntimeTypeAnnos)getAttribute(f.getAttributes(), visible?Constants.ATTR_RUNTIME_VISIBLE_TYPE_ANNOTATIONS:Constants.ATTR_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS);
		return rvta.getTypeAnnotations();
	}
	
	private void checkTypePath(TypeAnnotationGen ta, int[] expectedTypePath) {
		int[] typepath = ta.getTypePath();
		if (expectedTypePath==TypeAnnotationGen.NO_TYPE_PATH || expectedTypePath==null) {
			if (typepath!=TypeAnnotationGen.NO_TYPE_PATH) {
				fail("Expected no type path but was "+ta.getTypePathString());
			}
		} else {
			assertEquals(expectedTypePath.length, typepath.length);
			for (int i=0;i<expectedTypePath.length;i++) {
				if (expectedTypePath[i]!=typepath[i]) {
					fail("Expected type path: "+TypeAnnotationGen.toTypePathString(expectedTypePath)+" does not match actual type path "+ta.getTypePathString());
				}
			}
		}
	}

	private void checkLocalVarTarget(TypeAnnotationGen ta, int[] expectedLocalVarTarget) {
		int[] localVarTarget = ta.getLocalVarTarget();
		assertEquals(expectedLocalVarTarget.length, localVarTarget.length);
		for (int i=0;i<expectedLocalVarTarget.length;i++) {
			if (expectedLocalVarTarget[i]!=localVarTarget[i]) {
				fail("Expected local var target: "+toLocalVarTargetString(expectedLocalVarTarget)+" does not match actual type path "+toLocalVarTargetString(localVarTarget));
			}
		}
	}
	
	public static String toLocalVarTargetString(int[] localVarTarget) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		while (count < localVarTarget.length) {
			sb.append("{start_pc="+localVarTarget[count++]+",length="+localVarTarget[count++]+",index="+localVarTarget[count++]+"}");
		}
		return sb.toString();
	}

	private void checkTypeAnnotationLocalVariable(TypeAnnotationGen ta, int[] expectedLocalVarTarget, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.LOCAL_VARIABLE,ta.getTargetType());
		checkLocalVarTarget(ta,expectedLocalVarTarget);
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationResourceVariable(TypeAnnotationGen ta, int[] expectedLocalVarTarget, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.RESOURCE_VARIABLE,ta.getTargetType());
		checkLocalVarTarget(ta,expectedLocalVarTarget);
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationExceptionParameter(TypeAnnotationGen ta, int expectedExceptionTableIndex, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.EXCEPTION_PARAMETER,ta.getTargetType());
		assertEquals(expectedExceptionTableIndex,ta.getExceptionTableIndex());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationInstanceOf(TypeAnnotationGen ta, int expectedOffset, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.INSTANCEOF,ta.getTargetType());
		assertEquals(expectedOffset,ta.getOffset());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationNew(TypeAnnotationGen ta, int expectedOffset, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.NEW,ta.getTargetType());
		assertEquals(expectedOffset,ta.getOffset());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationConstructorReference(TypeAnnotationGen ta, int expectedOffset, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.CONSTRUCTOR_REFERENCE,ta.getTargetType());
		assertEquals(expectedOffset,ta.getOffset());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationMethodReference(TypeAnnotationGen ta, int expectedOffset, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.METHOD_REFERENCE,ta.getTargetType());
		assertEquals(expectedOffset,ta.getOffset());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationField(TypeAnnotationGen ta, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.FIELD,ta.getTargetType());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationMethodReturn(TypeAnnotationGen ta, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.METHOD_RETURN,ta.getTargetType());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationMethodFormalParameter(TypeAnnotationGen ta, int expectedFormalParameterIndex, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.METHOD_FORMAL_PARAMETER,ta.getTargetType());
		assertEquals(expectedFormalParameterIndex,ta.getMethodFormalParameterIndex());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}
	
	private void checkTypeAnnotationThrows(TypeAnnotationGen ta, int expectedThrowsTypeIndex, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.THROWS,ta.getTargetType());
		assertEquals(expectedThrowsTypeIndex,ta.getThrowsTypeIndex());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}
	
	private void checkTypeAnnotationMethodReceiver(TypeAnnotationGen ta, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.METHOD_RECEIVER,ta.getTargetType());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}
	
	private void checkTypeAnnotationClassExtends(TypeAnnotationGen ta, int expectedSupertypeIndex, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.CLASS_EXTENDS,ta.getTargetType());
		assertEquals(expectedSupertypeIndex,ta.getSupertypeIndex());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationClassTypeParameter(TypeAnnotationGen ta, int expectedTypeParameterIndex, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.CLASS_TYPE_PARAMETER,ta.getTargetType());
		assertEquals(expectedTypeParameterIndex,ta.getTypeParameterIndex());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}
	
	private void checkTypeAnnotationClassTypeParameterBound(TypeAnnotationGen ta, int expectedTypeParameterIndex, int expectedBoundIndex, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.CLASS_TYPE_PARAMETER_BOUND,ta.getTargetType());
		assertEquals(expectedTypeParameterIndex,ta.getTypeParameterIndex());
		assertEquals(expectedBoundIndex,ta.getBoundIndex());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationMethodTypeParameterBound(TypeAnnotationGen ta, int expectedTypeParameterIndex, int expectedBoundIndex, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.METHOD_TYPE_PARAMETER_BOUND,ta.getTargetType());
		assertEquals(expectedTypeParameterIndex,ta.getTypeParameterIndex());
		assertEquals(expectedBoundIndex,ta.getBoundIndex());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}

	private void checkTypeAnnotationMethodTypeParameter(TypeAnnotationGen ta, int expectedTypeParameterIndex, String expectedAnnotationText) {
		assertEquals(TypeAnnotationGen.METHOD_TYPE_PARAMETER,ta.getTargetType());
		assertEquals(expectedTypeParameterIndex,ta.getTypeParameterIndex());
		assertEquals(expectedAnnotationText,ta.getAnnotation().toShortString());
	}
	
}
