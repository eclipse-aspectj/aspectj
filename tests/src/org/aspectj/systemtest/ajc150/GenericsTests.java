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
	 * withincode  PASS
	 *    - no generic or parameterized declaring type patterns  PASS
	 *    - no parameterized throws patterns  PASS
	 *    - return type as type variable PASS
	 *    - return type as parameterized type PASS
	 *    - parameter as type variable  PASS
	 *    - parameter as parameterized type PASS
	 *    - no join points within bridge methods PASS
	 * execution PASS
	 *    - no generic or parameterized declaring type patterns PASS
	 *    - no parameterized throws patterns PASS
	 *    - return type as type variable  PASS
	 *    - return type as parameterized type  PASS
	 *    - parameter as type variable   PASS
	 *    - parameter as parameterized type  PASS
	 *    - no join points for bridge methods  PASS
	 * call
	 *    - no generic or parameterized declaring type patterns 
	 *    - no parameterized throws patterns 
	 *    - return type as type variable  
	 *    - return type as parameterized type  
	 *    - parameter as type variable   
	 *    - parameter as parameterized type  
	 *    - a call to a bridge method is really a call to the method being bridged...	 (1.4/1.5 differences here?)
	 */
	
	/* ==========================================
	 * Generics test plan for ITDs.
	 * 
	 * think about:
	 * - 'visibility' default/private/public
	 * - static/nonstatic
	 * - parameterized ITDs (methods/ctors/fields)
	 * - ITD target: interface/class/aspect
	 * - multiple type variables
	 * - constructor ITDs, method ITDs
	 * - ITDs sharing type variables with generic types
	 * -  relating to above point, this makes generic ITD fields possible
	 * - signature attributes for generic ITDs (required? required only for public ITDs?)
	 * - binary weaving when target type changes over time (might start out 'simple' then sometime later be 'generic')
	 * - bridge methods - when to create them
	 * - multiple 'separate' ITDs in a file that share a type variable by 'name'
	 * - wildcards '?' 'extends' 'super' '&'
	 * - do type variables assigned to members need to persist across serialization
	 * - recursive type variable definitions eg. <R extends Comparable<? super R>>
	 * - super/extends with parameterized types <? extends List<String>>
	 * - multiple ITDs defined in one type that reuse type variable letters, specifying different bounds
	 * - generic aspects
	 * 
	 * PASS parsing generic ITDs
	 * PASS generic methods
	 * PASS generic constructors
	 * PASS ITD visibility
	 * PASS static/nonstatic
	 * PASS parameterizedITDs
	 * PASS differing targets (interface/class/aspect)
	 * PASS multiple type variables in an ITD
     * TODO using type variables from the target type in your ITD (no type vars of your own)
	 * TODO parsing ITDs that share type variables with target type
	 * TODO sharing type variables (methods)
	 * TODO sharing type variables (fields)
	 * TODO sharing type variables (constructors)
	 * TODO sharing type variables and having your own type variables (methods/constructors)
	 * TODO signature attributes for generic ITDs (public only?)
	 * TODO binary weaving with changing types (moving between generic and simple)
	 * TODO bridge method creation
	 * TODO reusing type variable letter but differing spec across multiple ITDs in one aspect
	 * PASS wildcards
	 * TODO exotic class/interface bounds ('? extends List<String>')
	 * PASS recursive type variable definitions
	 * TODO generic aspects
	 * TODO parameterizing ITDs with type variables
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
	public void testPR96220_GenericAspects1() {runTest("generic aspects - 1");}
	public void testPR96220_GenericAspects2() {runTest("generic aspects - 2");}
	public void testPR96220_GenericAspects3() {runTest("generic aspects - 3");}
//	public void testGenericAspects4()         {runTest("generic aspects - 4");}
//	public void testGenericAspects5()         {runTest("generic aspects - 5 (ajdk)");}
	
    //////////////////////////////////////////////////////////////////////////////
    // Generic/Parameterized ITDs - includes scenarios from developers notebook //
    //////////////////////////////////////////////////////////////////////////////
	
	
	// parsing of generic ITD members
	public void testParseItdNonStaticMethod() {runTest("Parsing generic ITDs - 1");}
	public void testParseItdStaticMethod()    {runTest("Parsing generic ITDs - 2");}
	public void testParseItdCtor()            {runTest("Parsing generic ITDs - 3");}
	public void testParseItdComplexMethod()   {runTest("Parsing generic ITDs - 4");}
//	public void testParseItdSharingVars1()    {runTest("Parsing generic ITDs - 5");}
//	public void testParseItdSharingVars2()    {runTest("Parsing generic ITDs - 6");}
	
	
	// non static
	public void testGenericMethodITD1()  {runTest("generic method itd - 1");}   // <E> ... (List<? extends E>)
	public void testGenericMethodITD2()  {runTest("generic method itd - 2");}   // <E extends Number> ... (List<? extends E>) called incorrectly
	public void testGenericMethodITD3()  {runTest("generic method itd - 3");}   // <E> ... (List<E>,List<E>)
	public void testGenericMethodITD4()  {runTest("generic method itd - 4");}   // <A,B> ... (List<A>,List<B>)
	public void testGenericMethodITD5()  {runTest("generic method itd - 5");}   // <E> ... (List<E>,List<E>) called incorrectly
	public void testGenericMethodITD6()  {runTest("generic method itd - 6");}   // <E extends Number> ... (List<? extends E>)
	public void testGenericMethodITD7()  {runTest("generic method itd - 7"); }  // <E> ... (List<E>,List<? extends E>)
	public void testGenericMethodITD8()  {runTest("generic method itd - 8"); }  // <E> ... (List<E>,List<? extends E>) called incorrectly
	public void testGenericMethodITD9()  {runTest("generic method itd - 9"); }  // <R extends Comparable<? super R>> ... (List<R>)
	public void testGenericMethodITD10() {runTest("generic method itd - 10");}  // <R extends Comparable<? super R>> ... (List<R>) called incorrectly
	public void testGenericMethodITD11() {runTest("generic method itd - 11");}  // <R extends Comparable<? extends R>> ... (List<R>)
	public void testGenericMethodITD12() {runTest("generic method itd - 12");}  // <R extends Comparable<? extends R>> ... (List<R>) called incorrectly
	public void testGenericMethodITD13() {runTest("generic method itd - 13");}  // <R extends Comparable<? extends R>> ... (List<R>) called correctly in a clever way ;)
	public void testGenericMethodITD14() {runTest("generic method itd - 14");}  // <R extends Comparable<? super R>> ... (List<R>) called incorrectly in a clever way
	public void testGenericMethodITD15() {runTest("generic method itd - 15");}  // <R extends Comparable<? super R>> ... (List<R>) called correctly in a clever way
	


	// generic ctors
	public void testGenericCtorITD1() {runTest("generic ctor itd - 1");} // <T> new(List<T>)
	public void testGenericCtorITD2() {runTest("generic ctor itd - 2");} // <T> new(List<T>,List<? extends T>)
	public void testGenericCtorITD3() {runTest("generic ctor itd - 3");} // <T> new(List<T>,Comparator<? super T>)

	
	// parameterized ITDs
	public void testParameterizedMethodITD1() {runTest("parameterized method itd - 1");} // (List<? extends Super>)
	public void testParameterizedMethodITD2() {runTest("parameterized method itd - 2");} // (List<? extends Number>) called incorrectly
	public void testParameterizedMethodITD3() {runTest("parameterized method itd - 3");} // (List<? super A>) called incorrectly
	public void testParameterizedMethodITD4() {runTest("parameterized method itd - 4");} // (List<? super B>)
	
	
	// differing visibilities
	public void testPublicITDs()       {runTest("public itds");}
	public void testPublicITDsErrors() {runTest("public itds with errors");}
	public void testPrivateITDs()      {runTest("private itds");}
	public void testPackageITDs()      {runTest("package itds");}
	
	
	// targetting different types (interface/class/aspect)
	public void testTargettingInterface() {runTest("targetting interface");}
	public void testTargettingAspect()    {runTest("targetting aspect");}
	public void testTargettingClass()     {runTest("targetting class");}
	
	
	
	// sharing a type variable between the ITD and the target generic type	
//	public void testMethodITDsSharingTvar() {runTest("method itd sharing type variable with generic type");}
//	public void testFieldITDsSharingTvar()  {runTest("field itd sharing type variable with generic type");}

	
	// general tests ... usually just more complex scenarios
	public void testReusingTypeVariableLetters()   {runTest("reusing type variable letters");}
    public void testMultipleGenericITDsInOneFile() {runTest("multiple generic itds in one file");}
	public void testItdNonStaticMember()           {runTest("itd of non static member");}
	public void testItdStaticMember()              {runTest("itd of static member");}
	public void testStaticGenericMethodITD()       {runTest("static generic method itd");}
	
	

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
	
	public void testExecutionPointcutErrors() {
		runTest("execution with various parameterizations and generic types - errors");
	}
	
	public void testExecutionMatching() {
		runTest("execution with various parameterizations and generic types - matching");
	}
	
	public void testExecutionOverrideMatchingWithGenericMembers() {
		runTest("execution with overriding of inherited generic members");
	}

	public void testCallPointcutErrors() {
		runTest("call with various parameterizations and generic types - errors");
	}
	
	public void testCallMatching() {
		runTest("call with various parameterizations and generic types - matching");
	}
	
	public void testCallOverrideMatchingWithGenericMembers() {
		runTest("call with overriding of inherited generic members");
	}
	
	public void testCallWithBridgeMethods() {
		runTest("call with bridge methods");
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
