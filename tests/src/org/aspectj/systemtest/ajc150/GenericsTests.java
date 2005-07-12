package org.aspectj.systemtest.ajc150;

import java.io.File;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class GenericsTests extends XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(GenericsTests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc150/ajc150.xml");
	}
	
	public void testITDReturningParameterizedType() {
		runTest("ITD with parameterized type");
	}
	
	public void testPR91267_1() {
		runTest("NPE using generic methods in aspects 1");
	}

	public void testPR91267_2() {
		runTest("NPE using generic methods in aspects 2");
	}
	
	public void testPR91053() {
		runTest("Generics problem with Set");
	}
	
	public void testPR87282() {
		runTest("Compilation error on generic member introduction");
	}
	
	public void testPR88606() {
		runTest("Parameterized types on introduced fields not correctly recognized");
	}

    public void testPR97763() {
	    runTest("ITD method with generic arg");
    }

    public void testGenericsBang_pr95993() {
	    runTest("NPE at ClassScope.java:660 when compiling generic class");
    }    
	
	// generic aspects
	public void testPR96220_GenericAspects1() {
		runTest("generic aspects - 1");
	}
	
	public void testPR96220_GenericAspects2() {
		runTest("generic aspects - 2");
	}
	
	public void testPR96220_GenericAspects3() {
		runTest("generic aspects - 3");
	}
	
	// Developers notebook
	// ITD of generic members
	
	public void testItdNonStaticMethod() {
		runTest("Parsing generic ITDs - 1");
	}
	public void testItdStaticMethod() {
		runTest("Parsing generic ITDs - 2");
	}
	public void testItdCtor() {
		runTest("Parsing generic ITDs - 3");
	}
	public void testItdComplexMethod() {
		runTest("Parsing generic ITDs - 4");
	}
		
	public void testItdNonStaticMember() {
		runTest("itd of non static member");
	}
	
	public void testItdStaticMember() {
		runTest("itd of static member");
	}

//	public void testItdOnGenericType() {
//		runTest("ITDs on generic type");
//	}
//	
//	public void testItdUsingTypeParameter() {
//		runTest("itd using type parameter");
//	}
//	
//	public void testItdIncorrectlyUsingTypeParameter() {
//		runTest("itd incorrectly using type parameter");
//	}

	// ----------------------------------------------------------------------------------------
	// generic declare parents tests
	// ----------------------------------------------------------------------------------------
	
	public void testPR96220_GenericDecp() {
		runTest("generic decp - simple");
		verifyClassSignature("Basic","Ljava/lang/Object;LJ<Ljava/lang/Double;>;LI<Ljava/lang/Double;>;");
	}
	
	// Both the existing type decl and the one adding via decp are parameterized
	public void testGenericDecpMultipleVariantsOfAParameterizedType1() {
		runTest("generic decp - implementing two variants #1");
	}

	// Existing type decl is raw and the one added via decp is parameterized
	public void testGenericDecpMultipleVariantsOfAParameterizedType2() {
		runTest("generic decp - implementing two variants #2");
	}

	// Existing type decl is parameterized and the one added via decp is raw
	public void testGenericDecpMultipleVariantsOfAParameterizedType3() {
		runTest("generic decp - implementing two variants #3");
	}

	// decp is parameterized but it does match the one already on the type
	public void testGenericDecpMultipleVariantsOfAParameterizedType4() {
		runTest("generic decp - implementing two variants #4");
	}
	
	// same as above four tests for binary weaving
	public void testGenericDecpMultipleVariantsOfAParameterizedType1_binaryWeaving() {
		runTest("generic decp binary - implementing two variants #1");
	}
	
	public void testGenericDecpMultipleVariantsOfAParameterizedType2_binaryWeaving() {
		runTest("generic decp binary - implementing two variants #2");
	}

	// Existing type decl is parameterized and the one added via decp is raw
	public void testGenericDecpMultipleVariantsOfAParameterizedType3_binaryWeaving() {
		runTest("generic decp binary - implementing two variants #3");
	}

	// decp is parameterized but it does match the one already on the type
	public void testGenericDecpMultipleVariantsOfAParameterizedType4_binaryWeaving() {
		runTest("generic decp binary - implementing two variants #4");
	}
	
//	
//	public void testGenericDecpIncorrectNumberOfTypeParams() {
//		runTest("generic decp - incorrect number of type parameters");
//	}
//	
//	public void testGenericDecpSpecifyingBounds() {
//		runTest("generic decp - specifying bounds");
//	}
//	
//	public void testGenericDecpViolatingBounds() {
//		runTest("generic decp - specifying bounds but breaking them");
//	}
	
	// need separate compilation test to verify signatures are ok
//
//	public void testIllegalGenericDecp() {
//		runTest("illegal generic decp");
//	}
//
//	public void testPR95992_TypeResolvingProblemWithGenerics() {
//		runTest("Problems resolving type name inside generic class");
//	}
	
	// missing tests in here:
	
	// 1. public ITDs and separate compilation - are the signatures correct for the new public members?
	// 2. ITDF

	// -- Pointcut tests...
	
	public void testExecutionWithRawType() {
		runTest("execution pcd with raw type matching");
	}
	
	public void testExecutionWithRawSignature() {
		runTest("execution pcd with raw signature matching");
	}
	
	// --- helpers
		
	// Check the signature attribute on a class is correct
	private void verifyClassSignature(String classname,String sig) {
		try {
			ClassPath cp = 
				new ClassPath(ajc.getSandboxDirectory() + File.pathSeparator + System.getProperty("java.class.path"));
		    SyntheticRepository sRepos =  SyntheticRepository.getInstance(cp);
			JavaClass clazz = sRepos.loadClass(classname);
			Signature sigAttr = null;
			Attribute[] attrs = clazz.getAttributes();
			for (int i = 0; i < attrs.length; i++) {
				Attribute attribute = attrs[i];
				if (attribute.getName().equals("Signature")) sigAttr = (Signature)attribute;
			}
			assertTrue("Failed to find signature attribute for class "+classname,sigAttr!=null);
			assertTrue("Expected signature to be '"+sig+"' but was '"+sigAttr.getSignature()+"'",
					sigAttr.getSignature().equals(sig));
		} catch (ClassNotFoundException e) {
			fail("Couldn't find class "+classname+" in the sandbox directory.");
		}
	}
		

}
