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

	/*==========================================
	 * Generics test plan for pointcuts.
	 * 
	 * handler  PASS
	 *   - does not permit type var spec
	 *   - does not permit generic type (fail with type not found)
	 *   - does not permit parameterized types
	 * if PASS
	 *   - does not permit type vars
	 * cflow PASS
	 *   - does not permit type vars
	 * cflowbelow PASS
	 *   - does not permit type vars
	 * @this PASS
	 *   - does not permit type vars PASS
	 *   - does not permit parameterized type PASS
	 * @target PASS
	 *   - does not permit type vars PASS
	 *   - does not permit parameterized type PASS
	 * @args PASS
     *   - does not permit type vars PASS
	 *   - does not permit parameterized type PASS
	 * @annotation PASS
     *   - does not permit type vars PASS
	 *   - does not permit parameterized type PASS
	 * @within, @within code - as above PASS
	 * annotation type pattern with generic and parameterized types  PASS
	 *   - just make sure that annotation interfaces can never be generic first! VERIFIED
	 * 	  - @Foo<T>  should fail  PASS
	 *   - @Foo<String> should fail PASS
	 *   - @(Foo || Bar<T>) should fail  DEFERRED (not critical)
	 * staticinitialization PASS
	 *   - error on parameterized type PASS N/A
	 *   - permit parameterized type + PASS N/A
	 *   - matching with parameterized type + N/A 
	 *   - wrong number of parameters in parameterized type  PASS N/A
	 *   - generic type with one type parameter N/A
	 *   - generic type with n type parameters N/A
	 *   - generic type with bounds [extends, extends + i/f's] N/A
	 *   - generic type with wrong number of type params N/A
	 *   - wildcards in bounds N/A
	 * within  PASS
	 *   - as above, but allows parameterized type  (disallowed in simplified plan)
	 *   - wildcards in type parameters  N/A
	 * this  PASS 
	 *   - no type vars
	 *   - parameterized types  - disallowed in simplification plan
	 *        - implements
	 *        - instanceof
	 * target PASS
	 *   - as this
	 * args  TODO
	 *   - as this/target, plus...
	 *   - known static match
	 *   - known static match fail
	 *   - maybe match with unchecked warning
	 * get & set PASS
	 *   - parameterized declaring type PASS
	 *   - generic declaring type  PASS
	 *   - field type is type variable  PASS
	 *   - field type is parameterized  PASS
	 * initialization, preinitialization PASS
	 *   - generic declaring type  PASS
	 *   - type variables as params PASS
	 *   - parameterized types as params PASS
	 *   - no join points for init, preinit of parameterized types (as per staticinit) PASS
	 * withincode
	 *    - no generic or parameterized declaring type patterns  PASS
	 *    - no parameterized throws patterns  PASS
	 *    - return type as type variable
	 *    - return type as parameterized type
	 *    - parameter as type variable
	 *    - parameter as parameterized type
	 *    - no join points within bridge methods
	 * execution
	 *    - wait till we get there!
	 * call
	 *   - wait till we get there!
	 */
	
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

	public void testStaticGenericMethodITD() {
		runTest("static generic method itd");
	}
	
	// non static

	public void testGenericMethodITD1() {runTest("generic method itd - 1");} // <E> ... (List<? extends E>)
	public void testGenericMethodITD2() {runTest("generic method itd - 2");} // <E extends Number> ... (List<? extends E>) called incorrectly
	public void testGenericMethodITD3() {runTest("generic method itd - 3");} // <E> ... (List<E>,List<E>)
	public void testGenericMethodITD4() {runTest("generic method itd - 4");} // <A,B> ... (List<A>,List<B>)
	public void testGenericMethodITD5() {runTest("generic method itd - 5");} // <E> ... (List<E>,List<E>) called incorrectly
	public void testGenericMethodITD6() {runTest("generic method itd - 6");} // <E extends Number> ... (List<? extends E>)
	

	public void testParameterizedMethodITD1() {runTest("parameterized method itd - 1");} // (List<? extends Super>)
	public void testParameterizedMethodITD2() {runTest("parameterized method itd - 2");} // (List<? extends Number>) called incorrectly
	public void testParameterizedMethodITD3() {runTest("parameterized method itd - 3");} // (List<? super A>) called incorrectly
	public void testParameterizedMethodITD4() {runTest("parameterized method itd - 4");} // (List<? super B>)
	

//	public void testNonStaticGenericCtorITD1() {runTest("generic ctor itd - 1");}
//	public void testGenericITFSharingTypeVariable() {
//		runTest("generic intertype field declaration, sharing type variable");
//	}

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

	public void testGenericDecpParameterized() {
		runTest("generic decp - with parameterized on the target");
		verifyClassSignature("Basic6","<J:Ljava/lang/Object;>Ljava/lang/Object;LI<TJ;>;LK<Ljava/lang/Integer;>;");
	}
	
	public void testGenericDecpIncorrectNumberOfTypeParams() {
		runTest("generic decp - incorrect number of type parameters");
	}
	
	public void testGenericDecpSpecifyingBounds() {
		runTest("generic decp - specifying bounds");
	}
	
	public void testGenericDecpViolatingBounds() {
		runTest("generic decp - specifying bounds but breaking them");
	}
	
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

	public void testHandlerWithGenerics() {
		runTest("handler pcd and generics / type vars");
	}
	
	public void testPointcutsThatDontAllowTypeVars() {
		runTest("pointcuts that dont allow type vars");
	}
	
	public void testParameterizedTypesInAtPCDs() {
		runTest("annotation pcds with parameterized types");
	}

	public void testAnnotationPatternsWithParameterizedTypes() {
		runTest("annotation patterns with parameterized types");
	}
	
	public void testStaticInitializationWithParameterizedTypes() {
		runTest("staticinitialization and parameterized types");
	}

	// no longer a valid test with generics simplication
//	public void testStaticInitializationMatchingWithParameterizedTypes() {
//		runTest("staticinitialization and parameterized type matching");
//	}

// no longer a valid test in simplified design
//	public void testStaticInitializationWithGenericTypes() {
//		runTest("staticinitialization with generic types");
//	}

// no longer a valid test in simplified design
//	public void testStaticInitializationWithGenericTypesAdvanced() {
//		runTest("staticinitialization with generic types - advanced");		
//	}
	
	public void testWithinPointcutErrors() {
		runTest("within pcd with various parameterizations and generic types - errors");
	}

	public void testWithinPointcutWarnings() {
		runTest("within pcd with various parameterizations and generic types - warnings");
	}
	
	public void testThisTargetPointcutErrors() {
		runTest("this and target with various parameterizations and generic types - errors");
	}

	public void testThisTargetPointcutRuntime() {
		runTest("this and target with various parameterizations and generic types - runtime");
	}
	
	public void testInitAndPreInitPointcutErrors() {
		runTest("init and preinit with parameterized declaring types");
	}
	
	public void testInitAndPreInitPointcutMatchingWithGenericDeclaringTypes() {
		runTest("init and preinit with raw declaring type pattern");
	}
	
	public void testInitAndPreInitPointcutMatchingWithParameterizedParameterTypes() {
		runTest("init and preinit with parameterized parameter types");
	}
	
	public void testWithinCodePointcutErrors() {
		runTest("withincode with various parameterizations and generic types - errors");
	}
	
	public void testWithinCodeMatching() {
		runTest("withincode with various parameterizations and generic types - matching");
	}
	
	public void testWithinCodeOverrideMatchingWithGenericMembers() {
		runTest("withincode with overriding of inherited generic members");
	}
	
	public void testExecutionWithRawType() {
		runTest("execution pcd with raw type matching");
	}
	
	public void testExecutionWithRawSignature() {
		runTest("execution pcd with raw signature matching");
	}
	
	public void testGetAndSetPointcutErrors() {
		runTest("get and set with various parameterizations and generic types - errors");
	}
	
	public void testGetAndSetPointcutMatchingWithGenericAndParameterizedTypes() {
		runTest("get and set with various parameterizations and generic declaring types");
	}
	
	public void testGetAndSetPointcutMatchingWithGenericAndParameterizedFieldTypes() {
		runTest("get and set with various parameterizations and generic field types");
	}
	
//	public void testExecutionWithGenericDeclaringTypeAndErasedParameterTypes() {
//		runTest("execution pcd with generic declaring type and erased parameter types");
//	}
	
// not passing yet...
//	public void testExecutionWithGenericSignature() {
//		runTest("execution pcd with generic signature matching");
//	}
	
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
