/* *******************************************************************
 * Copyright (c) 2004 IBM
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationElementValue;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ClassElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.EnumElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.util.SyntheticRepository;


public class RuntimeVisibleAnnotationAttributeTest extends BcelTestCase {
	

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSeeAnnotationsAsAttribute() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("SimpleAnnotatedClass");
		ConstantPool pool = clazz.getConstantPool();
		Attribute[] rvaAttr = findAttribute("RuntimeVisibleAnnotations",clazz);
		assertTrue("Expected a RuntimeVisibleAnnotations attribute but found none",
				rvaAttr.length==1);
	}
	
	public void testAnnotationsAttributeContainsRightData() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("SimpleAnnotatedClass");
		ConstantPool pool = clazz.getConstantPool();
		Attribute[] rvaAttr = findAttribute("RuntimeVisibleAnnotations",clazz);
		RuntimeVisAnnos rva = (RuntimeVisAnnos) rvaAttr[0];
		List<AnnotationGen> anns = rva.getAnnotations();
		assertTrue("Should be one annotation but found "+anns.size(),
				   anns.size()==1);
		AnnotationGen ann = anns.get(0);
		assertTrue("Should be called 'SimpleAnnotation' but was called "+ann.getTypeName(),
				ann.getTypeName().equals("SimpleAnnotation"));
		List<NameValuePair> l = ann.getValues();
		assertTrue("Should be one value for annotation 'SimpleAnnotation' but found "+l.size(),
				l.size()==1);
		NameValuePair envp = l.get(0);
		assertTrue("Name of element in SimpleAnnotation should be 'id' but it is "+envp.getNameString(),
				envp.getNameString().equals("id"));
		SimpleElementValue evalue = (SimpleElementValue)envp.getValue();
		assertTrue("'id' should be of type int, but it is "+evalue.getElementValueType(),evalue.getElementValueType()==SimpleElementValue.PRIMITIVE_INT);
		assertTrue("'id' should have value 4 but it is "+evalue.getValueInt(),
				evalue.getValueInt()==4);
	}
	
	public void testAccessingAnnotationsOnClazz() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("SimpleAnnotatedClass");
		ConstantPool pool = clazz.getConstantPool();
		AnnotationGen[] anns = clazz.getAnnotations();
		assertTrue("Expected one annotation on SimpleAnnotatedClass class but got "+anns.length,
				anns.length==1);
	}
	
	public void testReadingWritingAnnotations() throws ClassNotFoundException, IOException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("SimpleAnnotatedClass");
		ConstantPool pool = clazz.getConstantPool();
		AnnotationGen[] anns = clazz.getAnnotations();
		assertTrue("Expected one annotation on SimpleAnnotatedClass class but got "+anns.length,
				anns.length==1);
		
		//	 Write it out
		File tfile = createTestdataFile("SimpleAnnotatedClass.class");
		clazz.dump(tfile);
	
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos.loadClass("SimpleAnnotatedClass");
		ConstantPool pool2 = clazz2.getConstantPool();
		AnnotationGen[] anns2 = clazz2.getAnnotations();
		assertTrue("Expected one annotation on SimpleAnnotatedClass class but got "+anns2.length,
				anns2.length==1);
		
		assertTrue(tfile.delete());
	}
	
	
	
	////
	// Test for annotations containing string elements
	
	public void testAnnotationStringElement() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("AnnotatedClass");
		verifyAnnotationStringElement(clazz);
	}
	

	public void testAnnotationStringElementReadWrite() throws ClassNotFoundException, IOException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("AnnotatedClass");
		verifyAnnotationStringElement(clazz);
		
		//	 Write it out
		File tfile = createTestdataFile("AnnotatedClass.class");
		clazz.dump(tfile);
		
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos2.loadClass("AnnotatedClass");
		verifyAnnotationStringElement(clazz2);	

		assertTrue(tfile.delete());
	}

	private void verifyAnnotationStringElement(JavaClass clazz) {
		AnnotationGen[] anns = clazz.getAnnotations();
		assertTrue("should be one annotation but found "+anns.length,anns.length==1);
		AnnotationGen ann = anns[0];
		assertTrue("should be called 'AnnotationStringElement' but was called "+ann.getTypeName(),
				ann.getTypeName().equals("AnnotationStringElement"));
		List<NameValuePair> l = ann.getValues();
		assertTrue("Should be one value but there were "+l.size(),l.size()==1);
		NameValuePair nvp = l.get(0);
		assertTrue("Name of element should be 'stringval' but was "+nvp.getNameString(),
				nvp.getNameString().equals("stringval"));
		SimpleElementValue ev = (SimpleElementValue)nvp.getValue();
		assertTrue("String value should be 'hello' but was '"+ev.getValueString()+"'",
				ev.getValueString().equals("hello"));
	}
	
	////
	// Test for complex annotation that includes all primitives
	
	public void testComplexAnnotation() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("ComplexAnnotatedClass");
		verifyComplexAnnotation(clazz);
	}
	

	public void testComplexAnnotationsReadWrite() throws ClassNotFoundException, IOException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("ComplexAnnotatedClass");
		verifyComplexAnnotation(clazz);

		//	 Write it out
		File tfile = createTestdataFile("ComplexAnnotatedClass.class");
		clazz.dump(tfile);
		
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos.loadClass("ComplexAnnotatedClass");
		verifyComplexAnnotation(clazz2);
		
		assertTrue(tfile.delete());
		
	}
	
	private void verifyComplexAnnotation(JavaClass clazz) {
		AnnotationGen[] anns = clazz.getAnnotations();
		assertTrue("Should be one annotation but found "+anns.length,anns.length==1);
		AnnotationGen ann = anns[0];
		assertTrue("Should be called 'ComplexAnnotation' but was called "+ann.getTypeName(),
				ann.getTypeName().equals("ComplexAnnotation"));
		List<NameValuePair> l = ann.getValues();
		assertTrue("Should be eight values for annotation 'ComplexAnnotation' but found "+l.size(),
				l.size()==8);
		List<String> names = RuntimeVisibleAnnotationAttributeTest.getListOfAnnotationNames(ann);
		assertTrue("Cant find expected element ",names.contains("ival"));
		assertTrue("Cant find expected element ",names.contains("dval"));
		assertTrue("Cant find expected element ",names.contains("zval"));
		assertTrue("Cant find expected element ",names.contains("fval"));
		assertTrue("Cant find expected element ",names.contains("jval"));
		assertTrue("Cant find expected element ",names.contains("sval"));
		assertTrue("Cant find expected element ",names.contains("bval"));
		assertTrue("Cant find expected element ",names.contains("cval"));
		
		checkValue(ann,"ival","4");
		checkValue(ann,"jval","56");
		checkValue(ann,"fval","3.0");
		checkValue(ann,"dval","33.4");
		checkValue(ann,"sval","99");
		checkValue(ann,"bval","2");
		checkValue(ann,"cval",new Character('5').toString());
		checkValue(ann,"zval","false");
		
	}

	private void checkValue(AnnotationGen a,String name,String tostring) {
		for (NameValuePair element : a.getValues()) {
			if (element.getNameString().equals(name)) {
				if (!element.getValue().stringifyValue().equals(tostring)) {
					fail("Expected element " + name + " to have value " + tostring + " but it had value " + element.getValue().stringifyValue());
				}
				return;
			}
		}
		fail("Didnt find named element "+name);
	}

	////
	// Test an annotation containing a 'Class' element
	
	public void testAnnotationClassElement() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("AnnotatedWithClassClass");
		verifyClassAnnotation(clazz);
	}
	
	public void testAnnotationClassElementCopying() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("AnnotatedWithClassClass");
		AnnotationGen[] anns = clazz.getAnnotations();
		ClassGen cg = new ClassGen(clazz);
		// Checks we can copy class values in an annotation
		new AnnotationGen(anns[0],cg.getConstantPool(),true);
		new AnnotationGen(anns[0],cg.getConstantPool(),false);
	}
	
	public void testAnnotationClassElementReadWrite() throws ClassNotFoundException,IOException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("AnnotatedWithClassClass");
		verifyClassAnnotation(clazz);

		//	 Write it out
		File tfile = createTestdataFile("AnnotatedWithClassClass.class");
		clazz.dump(tfile);
		
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos2.loadClass("AnnotatedWithClassClass");
		verifyClassAnnotation(clazz2);
		
		assertTrue(wipe("AnnotatedWithClassClass.class"));
	}
	
	private void verifyClassAnnotation(JavaClass clazz) {
		AnnotationGen[] anns = clazz.getAnnotations();
		assertTrue("should be one annotation but found "+anns.length,anns.length==1);
		AnnotationGen ann = anns[0];
		assertTrue("should be called 'AnnotationClassElement' but was called "+ann.getTypeName(),
				ann.getTypeName().equals("AnnotationClassElement"));
		List<NameValuePair> l = ann.getValues();
		assertTrue("Should be one value but there were "+l.size(),l.size()==1);
		NameValuePair nvp = l.get(0);
		assertTrue("Name of element should be 'clz' but was "+nvp.getNameString(),
				nvp.getNameString().equals("clz"));
		ClassElementValue ev = (ClassElementValue)nvp.getValue();
		assertTrue("String value should be 'Ljava/lang/Integer;' but was '"+ev.getClassString()+"'",
				ev.getClassString().equals("Ljava/lang/Integer;"));
		
	}
	
	////
	// Test an annotation containing an enum element
	
	public void testAnnotationEnumElement() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("AnnotatedWithEnumClass");
		verifyAnnotationEnumElement(clazz);
	}
		
	public void testAnnotationEnumElementReadWrite() throws ClassNotFoundException, IOException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("AnnotatedWithEnumClass");
		verifyAnnotationEnumElement(clazz);

		//	 Write it out
		File tfile = createTestdataFile("AnnotatedWithEnumClass.class");
		clazz.dump(tfile);
		
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos2.loadClass("AnnotatedWithEnumClass");
		verifyAnnotationEnumElement(clazz2);
		
		assertTrue(tfile.delete());
	}
	
	public void verifyAnnotationEnumElement(JavaClass clazz) {
		AnnotationGen[] anns = clazz.getAnnotations();
		assertTrue("should be one annotation but found "+anns.length,anns.length==1);
		AnnotationGen ann = anns[0];
		assertTrue("should be called 'AnnotationEnumElement' but was called "+ann.getTypeName(),
				ann.getTypeName().equals("AnnotationEnumElement"));
		List<NameValuePair> l = ann.getValues();
		assertTrue("Should be one value but there were "+l.size(),l.size()==1);
		NameValuePair nvp = l.get(0);
		assertTrue("Name of element should be 'enumval' but was "+nvp.getNameString(),
				nvp.getNameString().equals("enumval"));
		ElementValue ev = nvp.getValue();
		assertTrue("Should be of type EnumElementValue but is "+ev,ev instanceof EnumElementValue);
		EnumElementValue eev = (EnumElementValue)ev;
		assertTrue("Should be an enum type value but is "+eev.getElementValueType(),eev.getElementValueType()==SimpleElementValue.ENUM_CONSTANT);
		assertTrue("Enum type for annotation should be 'SimpleEnum' but is "+Utility.signatureToString(eev.getEnumTypeString()),Utility.signatureToString(eev.getEnumTypeString()).equals("SimpleEnum"));
		assertTrue("String value should be 'Red' but was '"+eev.getEnumValueString()+"'",
				eev.getEnumValueString().equals("Red"));
	}
	
	////
	// Test an annotation with an array element
	
	public void testAnnotationArraysOfAnnotations() throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("AnnotatedWithCombinedAnnotation");
		AnnotationGen[] anns = clazz.getAnnotations();
		assertTrue("should be one annotation but found "+anns.length,anns.length==1);
		checkCombinedAnnotation(anns[0]);
	}
	
	public void testAnnotationArraysOfAnnotationsReadWrite() throws ClassNotFoundException, IOException {
		SyntheticRepository repos = createRepos("testcode.jar");
		JavaClass           clazz = repos.loadClass("AnnotatedWithCombinedAnnotation");
		AnnotationGen[] anns = clazz.getAnnotations();
		assertTrue("should be one annotation but found "+anns.length,anns.length==1);
		checkCombinedAnnotation(anns[0]);
		
		//	 Write it out
		File tfile = createTestdataFile("AnnotatedWithCombinedAnnotation.class");
		clazz.dump(tfile);
		
		SyntheticRepository repos2 = createRepos(".");
		JavaClass           clazz2 = repos2.loadClass("AnnotatedWithCombinedAnnotation");
		AnnotationGen[] anns2 = clazz2.getAnnotations();
		assertTrue("should be one annotation but found "+anns2.length,anns2.length==1);
		checkCombinedAnnotation(anns2[0]);

		assertTrue(tfile.delete());
	}
	
	
	private void checkCombinedAnnotation(AnnotationGen ann) {
		assertTrue("should be called 'CombinedAnnotation' but was called "+ann.getTypeName(),
				ann.getTypeName().equals("CombinedAnnotation"));
		List<NameValuePair> l = ann.getValues();
		assertTrue("Should be one value but there were "+l.size(),l.size()==1);
		NameValuePair nvp = l.get(0);
		assertTrue("Name of element should be 'value' but was "+nvp.getNameString(),
				nvp.getNameString().equals("value"));
		ElementValue ev = nvp.getValue();
		assertTrue("Should be of type ArrayElementValue but is "+ev,ev instanceof ArrayElementValue);
		ArrayElementValue aev = (ArrayElementValue)ev;
		
		assertTrue("Array element value should be of size 1 but is "+aev.getElementValuesArraySize(),
				aev.getElementValuesArraySize()==1);
		ElementValue[] evs = aev.getElementValuesArray();
		assertTrue("Entry in the array should be AnnotationElementValue but is "+evs[0],
				evs[0] instanceof AnnotationElementValue);
		AnnotationElementValue inner_ev = (AnnotationElementValue)evs[0];
		AnnotationGen a = inner_ev.getAnnotation();
		assertTrue("Should be SimpleAnnotation but is "+a.getTypeName(),a.getTypeName().equals("SimpleAnnotation"));
		List<NameValuePair> envps = a.getValues();
		assertTrue("Should be one name value pair but found "+envps.size(),envps.size()==1);
		NameValuePair envp = envps.get(0);
		assertTrue("Name should be 'id' but it is "+envp.getNameString(),envp.getNameString().equals("id"));
		assertTrue("Value of 'id' should be 4 but it is "+envp.getValue().stringifyValue(),
				envp.getValue().stringifyValue().equals("4"));
	}
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static List<String> getListOfAnnotationNames(AnnotationGen a) {
	  	List<NameValuePair> l = a.getValues();
	    List<String> names = new ArrayList<>();
		for (NameValuePair element : l) {
			names.add(element.getNameString());
		}
	    return names;
	  }
	
}
