package org.aspectj.systemtest.ajc150;

import java.io.File;

import junit.framework.Test;

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
	
//	public void testItdNonStaticMethod() {
//		runTest("Parsing generic ITDs - 1");
//	}
//	public void testItdStaticMethod() {
//		runTest("Parsing generic ITDs - 2");
//	}
//	public void testItdCtor() {
//		runTest("Parsing generic ITDs - 3");
//	}
//	public void testItdComplexMethod() {
//		runTest("Parsing generic ITDs - 4");
//	}
	
//	public void testItdOnGenericType() {
//		runTest("ITDs on generic type");
//	}
//	
//	public void testItdNonStaticMember() {
//		runTest("itd of non static member");
//	}
//	
//	public void testItdStaticMember() {
//		runTest("itd of static member");
//	}
//	
//	public void testItdUsingTypeParameter() {
//		runTest("itd using type parameter");
//	}
//	
//	public void testItdIncorrectlyUsingTypeParameter() {
//		runTest("itd incorrectly using type parameter");
//	}

	// generic declare parents
//	public void testPR96220_GenericDecp() {
//		runTest("generic decp");
//	}
//
//	public void testIllegalGenericDecp() {
//		runTest("illegal generic decp");
//	}
//
//	public void testPR95992_TypeResolvingProblemWithGenerics() {
//		runTest("Problems resolving type name inside generic class");
//	}
	
}
