/* *******************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement (IBM)     initial implementation 
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile.tests;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Utility;

/**
 * Generics introduces more complex signature possibilities, they are no longer just
 * made up of primitives or big 'L' types. The addition of 'anglies' due to
 * parameterization and the ability to specify wildcards (possibly bounded)
 * when talking about parameterized types means we need to be much more sophisticated.
 *
 *
 * Notes:
 * Signatures are used to encode Java programming language type informaiton
 * that is not part of the JVM type system, such as generic type and method
 * declarations and parameterized types.  This kind of information is
 * needed to support reflection and debugging, and by the Java compiler.
 * 
 * ============================================= 
 * 
 * ClassTypeSignature =      LPackageSpecifier* SimpleClassTypeSignature ClassTypeSignatureSuffix*;
 * 
 * PackageSpecifier =        Identifier/PackageSpecifier*
 * SimpleClassTypeSignature= Identifier TypeArguments(opt)
 * ClassTypeSignatureSuffix= .SimpleClassTypeSignature
 * TypeVariableSignature =   TIdentifier;
 * TypeArguments =           <TypeArgument+>
 * TypeArgument =            WildcardIndiciator(opt) FieldTypeSignature
 *                           *
 * WildcardIndicator =       +
 *                           -
 * ArrayTypeSignature =      [TypeSignature
 * TypeSignature =           [FieldTypeSignature
 *                           [BaseType
 *                           
 *                           <not sure those [ should be prefixing fts and bt>
 * Examples:
 * 	Ljava/util/List;                      ==   java.util.List
 *  Ljava/util/List<Ljava/lang/String;>;  ==   java.util.List<java.lang.String>
 *  Ljava/util/List<Ljava/lang/Double;>;  ==   java.util.List<java.lang.Double>
 *  Ljava/util/List<+Ljava/lang/Number;>; ==   java.util.List<? extends java.lang.Number>
 *  Ljava/util/List<-Ljava/lang/Number;>; ==   java.util.List<? super java.lang.Number>
 *  Ljava/util/List<*>;                   ==   java.util.List<?>
 *  Ljava/util/Map<*-Ljava/lang/Number;>; ==   java.util.Map<?,? super java.lang.Number>
 *                           
 * ============================================= 
 * 
 * ClassSignature =          FormalTypeParameters(opt) SuperclassSignature SuperinterfaceSignatures*
 *   
 *   optional formal type parameters then a superclass signature then a superinterface signature
 * 
 * FormalTypeParameters =    <FormalTypeParameter+>
 * FormalTypeParameter  =    Identifier ClassBound InterfaceBound*  
 * ClassBound =              :FieldTypeSignature(opt)
 * InterfaceBound =          :FieldTypeSignature
 *    
 *   If it exists, a set of formal type parameters are contained in anglies and consist of an identifier a classbound (assumed to be
 *   object if not specified) and then an optional list of InterfaceBounds
 *   
 * SuperclassSignature =     ClassTypeSignature
 * SuperinterfaceSignature = ClassTypeSignature
 * FieldTypeSignature =      ClassTypeSignature
 *                           ArrayTypeSignature
 *                           TypeVariableSignature
 *                           
 *                           
 * MethodTypeSignature =     FormalTypeParameters(opt) ( TypeSignature* ) ReturnType ThrowsSignature*
 * ReturnType =              TypeSignature
 *                           VoidDescriptor
 * ThrowsSignature =         ^ClassTypeSignature
 *                           ^TypeVariableSignature
 *                           
 *  Examples: 
 * 
 * <T::Ljava/lang/Comparable<-Ljava/lang/Number;>;>
 * 
 * ClassBound not supplied, Object assumed.  Interface bound is Comparable<? super Number>
 * 
 * "T:Ljava/lang/Object;:Ljava/lang/Comparable<-TT;>;","T extends java.lang.Object & java.lang.Comparable<? super T>"
 *
 */
public class GenericSignatureParsingTest extends BcelTestCase {
	
	
	/** 
	 * Throw some generic format signatures at the BCEL signature 
	 * parsing code and see what it does.
	 */
	public void testParsingGenericSignatures_ClassTypeSignature() {
		// trivial
		checkClassTypeSignature("Ljava/util/List;","java.util.List");
		
		// basics
		checkClassTypeSignature("Ljava/util/List<Ljava/lang/String;>;","java.util.List<java.lang.String>");
		checkClassTypeSignature("Ljava/util/List<Ljava/lang/Double;>;","java.util.List<java.lang.Double>");

		// madness
		checkClassTypeSignature("Ljava/util/List<+Ljava/lang/Number;>;","java.util.List<? extends java.lang.Number>");
		checkClassTypeSignature("Ljava/util/List<-Ljava/lang/Number;>;","java.util.List<? super java.lang.Number>");
		checkClassTypeSignature("Ljava/util/List<*>;",                  "java.util.List<?>");
		checkClassTypeSignature("Ljava/util/Map<*-Ljava/lang/Number;>;","java.util.Map<?,? super java.lang.Number>");
		
		// with type params
		checkClassTypeSignature("Ljava/util/Collection<TT;>;","java.util.Collection<T>");
		
		// arrays
		checkClassTypeSignature("Ljava/util/List<[Ljava/lang/String;>;","java.util.List<java.lang.String[]>");
		checkClassTypeSignature("[Ljava/util/List<Ljava/lang/String;>;","java.util.List<java.lang.String>[]");

	}
	
	
	public void testMethodTypeToSignature() {
	  checkMethodTypeToSignature("void",new String[]{"java.lang.String[]","boolean"},"([Ljava/lang/String;Z)V");
	  checkMethodTypeToSignature("void",new String[]{"java.util.List<java/lang/String>"},"(Ljava/util/List<java/lang/String>;)V");
	}
	
	public void testMethodSignatureToArgumentTypes() {
	  checkMethodSignatureArgumentTypes("([Ljava/lang/String;Z)V",new String[]{"java.lang.String[]","boolean"});
//	  checkMethodSignatureArgumentTypes("(Ljava/util/List<java/lang/String>;)V",new String[]{"java.util.List<java/lang/String>"});
	}
	
	public void testMethodSignatureReturnType() {
	  checkMethodSignatureReturnType("([Ljava/lang/String;)Z","boolean");
	}
	
	public void testLoadingGenerics() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("PossibleGenericsSigs");
		// J5TODO asc fill this bit in...
	}
	
	
	// helper methods below

	// These routines call BCEL to determine if it can correctly translate from one form to the other.
	private void checkClassTypeSignature(String sig, String expected) {
		StringBuffer result = new StringBuffer();
		int p = Utility.readClassTypeSignatureFrom(sig,0,result,false);
		assertTrue("Only swallowed "+p+" chars of this sig "+sig+" (len="+sig.length()+")",p==sig.length());
		assertTrue("Expected '"+expected+"' but got '"+result.toString()+"'",result.toString().equals(expected));
	}
	
	private void checkMethodTypeToSignature(String ret,String[] args,String expected) {
		String res = Utility.methodTypeToSignature(ret,args);
		if (!res.equals(expected)) {
			fail("Should match.  Got: "+res+"  Expected:"+expected);
		}
	}
	
	private void checkMethodSignatureReturnType(String sig,String expected) {
		String result = Utility.methodSignatureReturnType(sig,false);
		if (!result.equals(expected)) {
			fail("Should match.  Got: "+result+"  Expected:"+expected);
		}
	}
	
	private void checkMethodSignatureArgumentTypes(String in,String[] expected) {
		String[] result = Utility.methodSignatureArgumentTypes(in,false);
		if (result.length!=expected.length) {
			fail("Expected "+expected.length+" entries to be returned but only got "+result.length);
		}
		for (int i = 0; i < expected.length; i++) {
			String string = result[i];
			if (!string.equals(expected[i]))
				fail("Argument: "+i+" should have been "+expected[i]+" but was "+string);
		}
	}
	
	public Signature getSignatureAttribute(JavaClass clazz,String name) {
		Method m = getMethod(clazz,name);
		Attribute[] as = m.getAttributes();
		for (int i = 0; i < as.length; i++) {
			Attribute attribute = as[i];
			if (attribute.getName().equals("Signature")) {
				return (Signature)attribute;
			}
		}
		return null;
	}
	
}
