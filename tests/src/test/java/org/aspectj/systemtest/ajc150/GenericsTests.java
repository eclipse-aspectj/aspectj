package org.aspectj.systemtest.ajc150;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.tools.ajc.Ajc;
import org.aspectj.util.LangUtil;

import junit.framework.Test;

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
	 * args
	 *   - args(List) matches List, List<T>, List<String>   PASS
	 *   - args(List<T>) -> invalid absolute type T
	 *   - args(List<String>) matches List<String> but not List<Number> PASS
	 *   - args(List<String>) matches List with unchecked warning PASS
	 *   - args(List<String>) matches List<?> with unchecked warning PASS
	 *   - args(List<Double>) matches List, List<?>, List<? extends Number> with unchecked warning PASS
	 *                        matches List<Double> PASS, List<? extends Double> PASS(with warning)
	 *   - args(List<?>) matches List, List<String>, List<?>, ...  PASS
	 *   - args(List<? extends Number) matches List<Number>, List<Double>, not List<String>  PASS
	 *                                 matches List, List<?> with unchecked warning  PASS
	 *   - args(List<? super Number>) matches List<Object>, List<Number>
	 *                                does not match List<Double>
	 *                                matches List, List<?> with unchecked warning
	 *                                matches List<? super Number>
	 *                                matches List<? extends Object> with unchecked warning
	 *                                matches List<? extends Number> with unchecked warning
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
	 * call PASS
	 *    - no generic or parameterized declaring type patterns PASS 
	 *    - no parameterized throws patterns PASS
	 *    - return type as type variable  PASS
	 *    - return type as parameterized type PASS 
	 *    - parameter as type variable   PASS
	 *    - parameter as parameterized type  PASS
	 *    - calls to a bridge methods PASS
	 * after throwing - can't use parameterized type pattern
	 * after returning - same as for args
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
	 * PASS parsing ITDs that share type variables with target type
     * PASS using type variables from the target type in your field ITD
     * PASS using type variables from the target type in your method ITD (but no type vars of your own)
     * PASS using type variables from the target type in your ctor ITD (but no type vars of your own)
	 * PASS using type variables from the target type and having your own too (methods)
	 * PASS using type variables from the target type and having your own too (ctors)
	 * PASS reusing type variable letter but differing spec across multiple ITDs in one aspect
	 * PASS wildcards
	 * PASS recursive type variable definitions
	 * PASS generic aspects
	 * PASS parameterizing ITDs with type variables
     * PASS using type variables from the target type in your *STATIC* ITD (field/method/ctor) (error scenario)
     * PASS basic binary weaving of generic itds
     * 
	 * TODO generic aspect binary weaving (or at least multi source file weaving)
	 * TODO binary weaving with changing types (moving between generic and simple)
	 * TODO bridge method creation (also relates to covariance overrides..)
	 * TODO exotic class/interface bounds ('? extends List<String>','? super anything')
	 * TODO signature attributes for generic ITDs (public only?)
	 * 
	 */
	
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(GenericsTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc150.xml");
	}
	
	public void testITDReturningParameterizedType() {
		runTest("ITD with parameterized type");
	}
	
	public void testPR91267_1() {
		runTest("NPE using generic methods in aspects 1");
	}
	
	public void testParameterizedTypeAndAroundAdvice_PR115250() {
		runTest("parameterized type and around advice");
	}

	public void testParameterizedTypeAndAroundAdvice_PR115250_2() {
		runTest("parameterized type and around advice - 2");
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
	
	public void testGenericsOverrides_1() { runTest("generics and ITD overrides - 1"); }
	public void testGenericsOverrides_2() { runTest("generics and ITD overrides - 2"); }
	public void testGenericsOverrides_3() { runTest("generics and ITD overrides - 3"); }
	public void testGenericsOverrides_4() { runTest("generics and ITD overrides - 4"); }
	

    public void testSelfBoundGenerics_pr117296() { 
	    runTest("self bounding generic types");
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
	public void testGenericAspects4()         {runTest("generic aspects - 4");} 
	public void testGenericAspects5()         {runTest("generic aspects - 5 (ajdk)");}  // in separate files
	public void testGenericAspects6()         {runTest("generic aspects - 6 (ajdk)");}  // all in one file
	public void testTypeVariablesInDeclareWarning() { runTest("generic aspect with declare warning using type vars");}
	public void testTypeVariablesInExecutionAdvice() { runTest("generic aspect with execution advice using type vars");}
	public void testTypeVariablesInAnonymousPointcut() { runTest("generic aspect with anonymous pointcut");}
	public void testDeclareParentWithParameterizedInterface() {
		runTest("generic aspect declare parents");
	}
	public void testDeclareSoftInGenericAspect() {
		runTest("generic aspect declare soft");
	}
	
	//////////////////////////////////////////////////////////////////////////////
    // Generic/Parameterized ITDs - includes scenarios from developers notebook //
    //////////////////////////////////////////////////////////////////////////////
	
	
	// parsing of generic ITD members
	public void testParseItdNonStaticMethod() {runTest("Parsing generic ITDs - 1");}
	public void testParseItdStaticMethod()    {runTest("Parsing generic ITDs - 2");}
	public void testParseItdCtor()            {runTest("Parsing generic ITDs - 3");}
	public void testParseItdComplexMethod()   {runTest("Parsing generic ITDs - 4");}
	public void testParseItdSharingVars1()    {runTest("Parsing generic ITDs - 5");}
	public void testParseItdSharingVars2()    {runTest("Parsing generic ITDs - 6");}
	
	
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
	
	
	
	// using a type variable from the target generic type in your ITD	
	public void testFieldITDsUsingTargetTypeVars1() {runTest("field itd using type variable from target type - 1");}
	public void testFieldITDsUsingTargetTypeVars2() {runTest("field itd using type variable from target type - 2");}
	public void testFieldITDsUsingTargetTypeVars3() {runTest("field itd using type variable from target type - 3");}
	public void testFieldITDsUsingTargetTypeVars4() {runTest("field itd using type variable from target type - 4");}
	public void testFieldITDsUsingTargetTypeVars5() {runTest("field itd using type variable from target type - 5");}
	public void testFieldITDsUsingTargetTypeVars6() {runTest("field itd using type variable from target type - 6");}
	public void testFieldITDsUsingTargetTypeVars7() {runTest("field itd using type variable from target type - 7");}
	public void testFieldITDsUsingTargetTypeVars8() {runTest("field itd using type variable from target type - 8");}
	public void testFieldITDsUsingTargetTypeVars9() {runTest("field itd using type variable from target type - 9");}
	public void testFieldITDsUsingTargetTypeVars10(){runTest("field itd using type variable from target type -10");}
	public void testFieldITDsUsingTargetTypeVars11(){runTest("field itd using type variable from target type -11");}
	public void testFieldITDsUsingTargetTypeVars12(){runTest("field itd using type variable from target type -12");}
	public void testFieldITDsUsingTargetTypeVars13(){runTest("field itd using type variable from target type -13");}
	public void testFieldITDsUsingTargetTypeVars14(){runTest("field itd using type variable from target type -14");}
	public void testFieldITDsUsingTargetTypeVars15(){runTest("field itd using type variable from target type -15");}
	public void testFieldITDsUsingTargetTypeVars16(){runTest("field itd using type variable from target type -16");}
	public void testFieldITDsUsingTargetTypeVars17(){runTest("field itd using type variable from target type -17");}
	

	public void testMethodITDsUsingTargetTypeVarsA1() {runTest("method itd using type variable from target type - A1");}
	public void testMethodITDsUsingTargetTypeVarsA2() {runTest("method itd using type variable from target type - A2");}
	public void testMethodITDsUsingTargetTypeVarsA3() {runTest("method itd using type variable from target type - A3");}
	public void testMethodITDsUsingTargetTypeVarsA4() {runTest("method itd using type variable from target type - A4");}
	public void testMethodITDsUsingTargetTypeVarsB1() {runTest("method itd using type variable from target type - B1");}
	public void testMethodITDsUsingTargetTypeVarsC1() {runTest("method itd using type variable from target type - C1");}
	public void testMethodITDsUsingTargetTypeVarsD1() {runTest("method itd using type variable from target type - D1");}
	public void testMethodITDsUsingTargetTypeVarsE1() {runTest("method itd using type variable from target type - E1");}
	public void testMethodITDsUsingTargetTypeVarsF1() {runTest("method itd using type variable from target type - F1");}
	public void testMethodITDsUsingTargetTypeVarsG1() {runTest("method itd using type variable from target type - G1");}
	public void testMethodITDsUsingTargetTypeVarsH1() {runTest("method itd using type variable from target type - H1");}
	public void testMethodITDsUsingTargetTypeVarsI1() {runTest("method itd using type variable from target type - I1");}
	public void testMethodITDsUsingTargetTypeVarsI2() {runTest("method itd using type variable from target type - I2");}
	public void testMethodITDsUsingTargetTypeVarsJ1() {runTest("method itd using type variable from target type - J1");}
	public void testMethodITDsUsingTargetTypeVarsK1() {runTest("method itd using type variable from target type - K1");}
	public void testMethodITDsUsingTargetTypeVarsL1() {runTest("method itd using type variable from target type - L1");}
	public void testMethodITDsUsingTargetTypeVarsM1() {runTest("method itd using type variable from target type - M1");}
	public void testMethodITDsUsingTargetTypeVarsM2() {runTest("method itd using type variable from target type - M2");}
	public void testMethodITDsUsingTargetTypeVarsN1() {runTest("method itd using type variable from target type - N1");}
	public void testMethodITDsUsingTargetTypeVarsO1() {runTest("method itd using type variable from target type - O1");}
	public void testMethodITDsUsingTargetTypeVarsO2() {runTest("method itd using type variable from target type - O2");}
	public void testMethodITDsUsingTargetTypeVarsP1() {runTest("method itd using type variable from target type - P1");}
	public void testMethodITDsUsingTargetTypeVarsQ1() {runTest("method itd using type variable from target type - Q1");}
	
	public void testCtorITDsUsingTargetTypeVarsA1() {runTest("ctor itd using type variable from target type - A1");}
	public void testCtorITDsUsingTargetTypeVarsB1() {runTest("ctor itd using type variable from target type - B1");}
	public void testCtorITDsUsingTargetTypeVarsC1() {runTest("ctor itd using type variable from target type - C1");}
	public void testCtorITDsUsingTargetTypeVarsD1() {runTest("ctor itd using type variable from target type - D1");}
	public void testCtorITDsUsingTargetTypeVarsE1() {runTest("ctor itd using type variable from target type - E1");}
	public void testCtorITDsUsingTargetTypeVarsF1() {runTest("ctor itd using type variable from target type - F1");}
	public void testCtorITDsUsingTargetTypeVarsG1() {runTest("ctor itd using type variable from target type - G1");}
	public void testCtorITDsUsingTargetTypeVarsH1() {runTest("ctor itd using type variable from target type - H1");}
	public void testCtorITDsUsingTargetTypeVarsI1() {runTest("ctor itd using type variable from target type - I1");}
	
	public void testSophisticatedAspectsA() {runTest("uberaspects - A");}
	public void testSophisticatedAspectsB() {runTest("uberaspects - B");}
	public void testSophisticatedAspectsC() {runTest("uberaspects - C");}
	public void testSophisticatedAspectsD() {runTest("uberaspects - D");}
	public void testSophisticatedAspectsE() {runTest("uberaspects - E");}
	public void testSophisticatedAspectsF() {runTest("uberaspects - F");}
	public void testSophisticatedAspectsG() {runTest("uberaspects - G");}
	public void testSophisticatedAspectsH() {runTest("uberaspects - H");}
	public void testSophisticatedAspectsI() {runTest("uberaspects - I");}
	public void testSophisticatedAspectsJ() {runTest("uberaspects - J");}
    //public void testSophisticatedAspectsK() {runTest("uberaspects - K");} // FIXME asc bounds testing is tough!
    public void testSophisticatedAspectsK2(){runTest("uberaspects - K2");}
	public void testSophisticatedAspectsL() {runTest("uberaspects - L");}
    public void testSophisticatedAspectsM() {runTest("uberaspects - M");}
	public void testSophisticatedAspectsN() {runTest("uberaspects - N");}
    public void testSophisticatedAspectsO() {runTest("uberaspects - O");}
	public void testSophisticatedAspectsP() {runTest("uberaspects - P");}
	public void testSophisticatedAspectsQ() {runTest("uberaspects - Q");}
	public void testSophisticatedAspectsR() {runTest("uberaspects - R");}
	public void testSophisticatedAspectsS() {runTest("uberaspects - S");}
	public void testSophisticatedAspectsT() {runTest("uberaspects - T");}
	public void testSophisticatedAspectsU() {runTest("uberaspects - U");} // includes nasty casts
	public void testSophisticatedAspectsV() {runTest("uberaspects - V");} // casts are gone
	public void testSophisticatedAspectsW() {runTest("uberaspects - W");}
	public void testSophisticatedAspectsX() {runTest("uberaspects - X");} // from the AJDK
	public void testSophisticatedAspectsY() {runTest("uberaspects - Y");} // pointcut matching
	public void testSophisticatedAspectsZ() {runTest("uberaspects - Z");} 
	
	// FIXME asc these two tests have peculiar error messages - generic aspect related
//	public void testItdUsingTypeParameter() {runTest("itd using type parameter");}
//	public void testItdIncorrectlyUsingTypeParameter() {runTest("itd incorrectly using type parameter");}
	
	
	public void testUsingSameTypeVariable() {runTest("using same type variable in ITD");}

	public void testBinaryWeavingITDsA() {runTest("binary weaving ITDs - A");}
	public void testBinaryWeavingITDsB() {runTest("binary weaving ITDs - B");}
	public void testBinaryWeavingITDs1() {runTest("binary weaving ITDs - 1");}
	public void testBinaryWeavingITDs2() {runTest("binary weaving ITDs - 2");}
	public void testBinaryWeavingITDs3() {runTest("binary weaving ITDs - 3");}
	public void testGenericITFSharingTypeVariable() {runTest("generic intertype field declaration, sharing type variable");}	
	
	
	// general tests ... usually just more complex scenarios
	public void testReusingTypeVariableLetters()   {runTest("reusing type variable letters");}
    public void testMultipleGenericITDsInOneFile() {runTest("multiple generic itds in one file");}
	public void testItdNonStaticMember()           {runTest("itd of non static member");}
	public void testItdStaticMember()              {runTest("itd of static member");}
	public void testStaticGenericMethodITD()       {runTest("static generic method itd");}
	
	
	public void testAtOverride0()  {runTest("atOverride used with ITDs");}
	public void testAtOverride1()  {runTest("atOverride used with ITDs - 1");}
	public void testAtOverride2()  {runTest("atOverride used with ITDs - 2");}
	public void testAtOverride3()  {runTest("atOverride used with ITDs - 3");}
	public void testAtOverride4()  {runTest("atOverride used with ITDs - 4");}
	public void testAtOverride5()  {runTest("atOverride used with ITDs - 5");}
	public void testAtOverride6()  {runTest("atOverride used with ITDs - 6");}
	public void testAtOverride7()  {runTest("atOverride used with ITDs - 7");}
	
	
	// bridge methods
	public void testITDBridgeMethodsCovariance1() {runTest("bridging with covariance 1 - normal");}
	public void testITDBridgeMethodsCovariance2() {runTest("bridging with covariance 1 - itd");}
	public void testITDBridgeMethods1Normal()     {runTest("basic bridging with type vars - 1 - normal");}
	public void testITDBridgeMethods1Itd()        {runTest("basic bridging with type vars - 1 - itd");}
	public void testITDBridgeMethods2Normal()     {runTest("basic bridging with type vars - 2 - normal");}
	public void testITDBridgeMethods2Itd()        {runTest("basic bridging with type vars - 2 - itd");}
	public void testITDBridgeMethodsPr91381() {runTest("Abstract intertype method and covariant returns");}
	
	
	// Just normal source compile of two types with a method override between them
    public void testGenericITDsBridgeMethods1() {
    	runTest("bridge methods - 1");
    	checkMethodsExist("Sub1",new String[]{
    			"java.lang.Integer Sub1.m()",
    			"java.lang.Object Sub1.m() [BridgeMethod]"});
    }
    // Now the same thing but the aspect (which doesn't do much!) is binary woven in.
	public void testGenericITDsBridgeMethods1binary()  {
		runTest("bridge methods - 1 - binary");
		checkMethodsExist("Sub1",new String[]{ 
				"java.lang.Integer Sub1.m()",
				"java.lang.Object Sub1.m() [BridgeMethod]"});
	}
	// Now the method is put into the superclass via ITD - there should be a bridge method in the subclass
	public void testGenericITDsBridgeMethods2()        {
		runTest("bridge methods - 2");
		checkMethodsExist("Sub2",new String[]{
    			"java.lang.Integer Sub2.m()",
    			"java.lang.Object Sub2.m() [BridgeMethod]"});
	}
	// Now the superclass ITD is done with binary weaving so the weaver (rather than compiler) has to create the bridge method
	public void testGenericITDsBridgeMethods2binary()  {
		runTest("bridge methods - 2 - binary");
		checkMethodsExist("Sub2",new String[]{
    			"java.lang.Integer Sub2.m()",
    			"java.lang.Object Sub2.m() [BridgeMethod]"});
	}
	// Now the method is put into the subclass via ITD - there should be a bridge method alongside it in the subclass
	public void testGenericITDsBridgeMethods3()        {
		runTest("bridge methods - 3");
		checkMethodsExist("Sub3",new String[]{
    			"java.lang.Integer Sub3.m()",
    			"java.lang.Object Sub3.m() [BridgeMethod]"});
	}
	// Now the subclass ITD is done with binary weaving - the weaver should create the necessary bridge method
	public void testGenericITDsBridgeMethods3binary()  {
		runTest("bridge methods - 3 - binary");
		checkMethodsExist("Sub3",new String[]{
    			"java.lang.Integer Sub3.m()",
    			"java.lang.Object Sub3.m() [BridgeMethod]"});
	}
	// Now the two types are disconnected until the aspect supplies a declare parents relationship - 
	// the bridge method should still be created in the subtype
	public void testGenericITDSBridgeMethods4() {
		runTest("bridge methods - 4");
		checkMethodsExist("Sub4",new String[]{
    			"java.lang.Integer Sub4.m()",
				"java.lang.Object Sub4.m() [BridgeMethod]"});
	}
	// now the aspect doing the decp between the types is applied via binary weaving - weaver should create the bridge method
	public void testGenericITDSBridgeMethods4binary() {
		runTest("bridge methods - 4 - binary");
		checkMethodsExist("Sub4",new String[]{
    			"java.lang.Integer Sub4.m()",
				"java.lang.Object Sub4.m() [BridgeMethod]"});
	}
	
	public void testBinaryBridgeMethodsOne() {
		runTest("binary bridge methods - one");
		checkMethodsExist("OneB",new String[]{
				"java.lang.Number OneB.firstMethod() [BridgeMethod]",
				"java.lang.Integer OneB.firstMethod()",
				"void OneB.secondMethod(java.lang.Number) [BridgeMethod]",
				"void OneB.secondMethod(java.lang.Integer)",
				"void OneB.thirdMethod(java.lang.Number,java.lang.Number) [BridgeMethod]",
				"void OneB.thirdMethod(java.lang.Integer,java.lang.Integer)",
				"void OneB.fourthMethod(java.util.List)",
				"java.lang.Number OneB.fifthMethod(java.lang.Number,java.util.List) [BridgeMethod]",
				"java.lang.Integer OneB.fifthMethod(java.lang.Integer,java.util.List)"
		});
	}
	public void testBinaryBridgeMethodsTwo() {
		runTest("binary bridge methods - two");
		checkMethodsExist("TwoB",new String[]{
				"java.lang.Number TwoB.firstMethod(java.io.Serializable) [BridgeMethod]",
				"java.lang.Integer TwoB.firstMethod(java.lang.String)"
		});
	}
	public void testBinaryBridgeMethodsThree() {
		runTest("binary bridge methods - three");
		checkMethodsExist("ThreeB",new String[]{
				"java.lang.Number ThreeB.m() [BridgeMethod]",
				"java.lang.Double ThreeB.m()"
		});
	}
	
	
	public void testGenericITDsBridgeMethodsPR91381()  {runTest("abstract intertype methods and covariant returns");}
	public void testGenericITDsBridgeMethodsPR91381_2()  {runTest("abstract intertype methods and covariant returns - error");}

	// ----------------------------------------------------------------------------------------
	// generic declare parents tests
	// ----------------------------------------------------------------------------------------
	
	public void testPR96220_GenericDecp() {
		runTest("generic decp - simple");
		checkOneSignatureAttribute(ajc,"Basic");
		verifyClassSignature(ajc,"Basic","Ljava/lang/Object;LJ<Ljava/lang/Double;>;LI<Ljava/lang/Double;>;");
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
		checkOneSignatureAttribute(ajc,"Basic6");
		verifyClassSignature(ajc,"Basic6","<J:Ljava/lang/Object;>Ljava/lang/Object;LI<TJ;>;LK<Ljava/lang/Integer;>;");
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
	
	public void testArgsWithRawType() {
		runTest("args with raw type and generic / parameterized sigs");
	}
	
	public void testArgsParameterizedType() {
		runTest("args with parameterized type and generic / parameterized sigs");
	}
	
	public void testArgsParameterizedAndWildcards() {
		runTest("args with parameterized type and wildcards");
	}
	
	public void testArgsWithWildcardVar() {
		runTest("args with generic wildcard");
	}
	
	public void testArgsWithWildcardExtendsVar() {
		runTest("args with generic wildcard extends");
	}
	
	public void testArgsWithWildcardSuperVar() {
		runTest("args with generic wildcard super");
	}
	
	public void testGenericMethodMatching() {
		runTest("generic method matching");
	}
	
	public void testGenericWildcardsInSignatureMatching() {
		runTest("generic wildcards in signature matching");
	}
	
	public void testAfterThrowing() {
		runTest("after throwing with parameterized throw type");
	}

	public void testAfterReturningWithRawType() {
		runTest("after returning with raw type and generic / parameterized sigs");
	}
	
	public void testAfterReturningParameterizedType() {
		runTest("after returning with parameterized type and generic / parameterized sigs");
	}
	
	public void testAfterReturningParameterizedAndWildcards() {
		runTest("after returning with parameterized type and wildcards");
	}

	public void testAfterReturningWithWildcardVar() {
		if (LangUtil.is19VMOrGreater()) {
			// See ReferenceType.isCoerceableFrom comments
			return;
		}
		// Something to investigate here. The implementation of isCoerceable
		runTest("after returning with generic wildcard");
	}
	
	public void testAfterReturningWithWildcardExtendsVar() {
		runTest("after returning with generic wildcard extends");
	}
	
	public void testAfterReturningWithWildcardSuperVar() {
		runTest("after returning with generic wildcard super");
	}
	
	public void testAJDKErasureMatchingExamples() {
		runTest("ajdk notebook: erasure matching examples");
	}
	
	public void testAJDKParameterizedMatchingSimpleExamples() {
		runTest("ajdk notebook: simple parameterized type matching examples");
	}
	
	public void testAJDKMixedTypeVarsAndParametersExample() {
		runTest("ajdk notebook: mixed parameterized types and generic methods");
	}
	
	public void testAJDKSignatureAndWildcardExamples() {
		runTest("ajdk notebook: signature matching with generic wildcards");
	}
	
	// had to remove at e37 level - although pointcuts are likely to work, we can't compile the code
	// that invokes the bridge methods - seems the compiler is too smart and won't let them through.
//	public void testAJDKBridgeMethodExamples() {
//		runTest("ajdk notebook: bridge method examples");
//	}
	
	public void testAJDKArgsExamples() {
		runTest("ajdk notebook: args examples");
	}
	
	public void testAJDKArgsAndWildcardsExamples() {
		runTest("ajdk notebook: args and wildcards examples");
	}
	
	public void testAJDKAfterReturningExamples() {
		runTest("ajdk notebook: after returning examples");
	}
	
	public void testAJDKPointcutInGenericClassExample() {
		runTest("ajdk notebook: pointcut in generic class example");
	}
	
	// TESTS for generic abstract aspects that get extended and parameterized...
	
	public void testStaticPointcutParameterization() {
		runTest("static pointcut parameterization suite");
	}
	
	public void testDynamicPointcutParameterization() {
		runTest("dynamic pointcut parameterization suite");
	}
	
	public void testReferenceToPointcutInGenericClass() {
		runTest("reference to pointcut in generic class");
	}
	
	public void testReferenceToPointcutInGenericClass2() {
		runTest("reference to non-parameterized pointcut in generic class");
	}
	
	public void testDeclareParentsParameterized() {
		runTest("declare parents parameterized");
	}
	
	public void testDeclarePrecedenceParameterized() { 
		runTest("declare precedence parameterized");
	}
	
	public void testDeclareAnnotationParameterized() {
		runTest("declare annotation parameterized");
	}
	
	public void testMultiLevelGenericAspects() {
		runTest("multi-level generic abstract aspects");
	}
	
	// --- helpers
		
		/**
	 * When a class has been written to the sandbox directory, you can ask this method to 
	 * verify it contains a particular set of methods.  Typically this is used to verify that
	 * bridge methods have been created.
	 */
	public void checkMethodsExist(String classname,String[] methods) {
		Set<String> methodsFound = new HashSet<>();
		StringBuffer debugString = new StringBuffer();
		try {
			ClassLoader cl = new URLClassLoader(new URL[]{ajc.getSandboxDirectory().toURL()});
			Class<?> clz = Class.forName(classname,false,cl);
			java.lang.reflect.Method[] ms = clz.getDeclaredMethods();
			if (ms!=null) {
				for (java.lang.reflect.Method m : ms) {
					String methodString = m.getReturnType().getName() + " " + m.getDeclaringClass().getName() + "." +
							m.getName() + "(" + stringify(m.getParameterTypes()) + ")" +
							(isBridge(m) ? " [BridgeMethod]" : "");
					methodsFound.add(methodString);
					debugString.append("\n[").append(methodString).append("]");
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		// check the methods specified do exist
		for (String string : methods) {
			if (!methodsFound.remove(string)) {
				fail("Couldn't find [" + string + "] in the set of methods in " + classname + " => " + debugString);
			}
		}
		StringBuffer unexpectedMethods = new StringBuffer();
		if (!methodsFound.isEmpty()) {
			for (String element: methodsFound) {
				unexpectedMethods.append("[").append(element).append("]");
			}
			fail("These methods weren't expected: "+unexpectedMethods);
		}
		
	}
    
    /**
     * Use 1.5 API isBridge if available.
     * See JLS3 15.12.4.5 Create Frame, Synchronize, Transfer Control
     */
	public static boolean isBridge(java.lang.reflect.Method m) {
        // why not importing java.lang.reflect.Method? No BCEL clash?
		try {
            final Class<?>[] noparms = new Class[0];
            java.lang.reflect.Method isBridge 
                = java.lang.reflect.Method.class.getMethod("isBridge", noparms);
            Boolean result = (Boolean) isBridge.invoke(m, new Object[0]);
            return result;
        } catch (Throwable t) {
            return false;
        }
    }
	public static JavaClass getClass(Ajc ajc, String classname) {
		try {
			ClassPath cp = 
				new ClassPath(ajc.getSandboxDirectory() + File.pathSeparator + System.getProperty("java.class.path"));
		    SyntheticRepository sRepos =  SyntheticRepository.getInstance(cp);
			JavaClass clazz = sRepos.loadClass(classname);
			return clazz;
		} catch (ClassNotFoundException e) {
			fail("Couldn't find class "+classname+" in the sandbox directory.");
		}
		return null;
	}
	
	public static Signature getClassSignature(Ajc ajc,String classname) {
	    JavaClass clazz = getClass(ajc,classname);
		Signature sigAttr = null;
		Attribute[] attrs = clazz.getAttributes();
		for (Attribute attribute : attrs) {
			if (attribute.getName().equals("Signature")) sigAttr = (Signature) attribute;
		}
		return sigAttr;
	}
	
	public static void checkOneSignatureAttribute(Ajc ajc,String classname) {
		JavaClass clazz = getClass(ajc,classname);
		Attribute[] attrs = clazz.getAttributes();
		int signatureCount = 0;
		StringBuffer sb = new StringBuffer();
		for (Attribute attribute : attrs) {
			if (attribute.getName().equals("Signature")) {
				signatureCount++;
				sb.append("\n" + ((Signature) attribute).getSignature());
			}
		}
		if (signatureCount>1) fail("Should be only one signature attribute but found "+signatureCount+sb.toString());
	}
	
	// Check the signature attribute on a class is correct
	public static void verifyClassSignature(Ajc ajc,String classname,String sig) {
		Signature sigAttr = getClassSignature(ajc,classname);
		assertTrue("Failed to find signature attribute for class "+classname,sigAttr!=null);
		assertTrue("Expected signature to be '"+sig+"' but was '"+sigAttr.getSignature()+"'",
				sigAttr.getSignature().equals(sig));		
	}
		
	private static String stringify(Class<?>[] clazzes) {
		if (clazzes==null) return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < clazzes.length; i++) {
			if (i>0) sb.append(",");
			sb.append(clazzes[i].getName());
		}
		return sb.toString();
	}

}
