package org.aspectj.systemtest.ajc150;

import java.io.File;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.tools.ajc.Ajc;

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
	 * TODO generic aspect binary weaving (or at least multi source file weaving)
	 * TODO binary weaving with changing types (moving between generic and simple)
	 * TODO bridge method creation (also relates to covariance overrides..)
	 * TODO exotic class/interface bounds ('? extends List<String>','? super anything')
	 * TODO signature attributes for generic ITDs (public only?)
	 * 
	 * 
	 * strangeness:
	 * 
	 *   adding declare precedence into the itds/binaryweaving A2.aj, A3.aj causes a bizarre classfile inconsistent message
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
	public void testGenericAspects4()         {runTest("generic aspects - 4");} 
    // TODO FREAKYGENERICASPECTPROBLEM why does everything have to be in one source file?
	// public void testGenericAspects5()         {runTest("generic aspects - 5 (ajdk)");} 
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
//	public void testSophisticatedAspectsG() {runTest("uberaspects - G");}
	public void testSophisticatedAspectsH() {runTest("uberaspects - H");}
	public void testSophisticatedAspectsI() {runTest("uberaspects - I");}
	public void testSophisticatedAspectsJ() {runTest("uberaspects - J");}
	// next test commented out, error message is less than ideal - see 
	// comment in test program as to what should be expected
    //public void testSophisticatedAspectsK() {runTest("uberaspects - K");}
	public void testSophisticatedAspectsL() {runTest("uberaspects - L");}
    public void testSophisticatedAspectsM() {runTest("uberaspects - M");}
	public void testSophisticatedAspectsN() {runTest("uberaspects - N");}
    public void testSophisticatedAspectsO() {runTest("uberaspects - O");}
	public void testSophisticatedAspectsP() {runTest("uberaspects - P");}
	public void testSophisticatedAspectsQ() {runTest("uberaspects - Q");}
	public void testSophisticatedAspectsR() {runTest("uberaspects - R");}
	public void testSophisticatedAspectsS() {runTest("uberaspects - S");}
	public void testSophisticatedAspectsT() {runTest("uberaspects - T");}
	public void testSophisticatedAspectsU() {runTest("uberaspects - U");} //  includes nasty casts
	
	// FIXME asc these two tests have peculiar error messages - generic aspect related
//	public void testItdUsingTypeParameter() {runTest("itd using type parameter");}
//	public void testItdIncorrectlyUsingTypeParameter() {runTest("itd incorrectly using type parameter");}
	

	public void testBinaryWeavingITDsA() {runTest("binary weaving ITDs - A");}
	public void testBinaryWeavingITDsB() {runTest("binary weaving ITDs - B");}
	public void testBinaryWeavingITDs1() {runTest("binary weaving ITDs - 1");}
	public void testBinaryWeavingITDs2() {runTest("binary weaving ITDs - 2");}
	public void testBinaryWeavingITDs3() {runTest("binary weaving ITDs - 3");}
	public void testGenericITFSharingTypeVariable() {runTest("generic intertype field declaration, sharing type variable");}	
	
	
	// general tests ... usually just more complex scenarios
	public void testReusingTypeVariableLetters()   {runTest("reusing type variable letters");}
    public void testMultipleGenericITDsInOneFile() {runTest("multiple generic itds in one file");}
//	public void testItdNonStaticMember()           {runTest("itd of non static member");}
//	public void testItdStaticMember()              {runTest("itd of static member");}
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
//	public void testITDBridgeMethodsCovariance1() {runTest("bridging with covariance 1 normal");}
//	public void testITDBridgeMethodsCovariance2() {runTest("bridging with covariance 1 itd");}
//	public void testITDBridgeMethodsCovariance3() {runTest("bridging with covariance 1 itd binary weaving");}
//	public void testITDBridgeMethods1Normal() {runTest("basic bridging with type vars - 1 - normal");}
//	public void testITDBridgeMethods1Itd()    {runTest("basic bridging with type vars - 1 - itd");}
//	public void testITDBridgeMethods2() {runTest("basic bridging with type vars - 2");}
//	public void testITDBridgeMethodsPr91381() {runTest("Abstract intertype method and covariant returns");}
	
    public void testGenericITDsBridgeMethods1()        {runTest("bridge methods -1");}
//	public void testGenericITDsBridgeMethods1binary()  {runTest("bridge methods -1binary");}
	public void testGenericITDsBridgeMethods2()        {runTest("bridge methods -2");}
//	public void testGenericITDsBridgeMethods2binary()  {runTest("bridge methods -2binary");}
	public void testGenericITDsBridgeMethods3()        {runTest("bridge methods -3");}
//	public void testGenericITDsBridgeMethods3binary()  {runTest("bridge methods -3binary");}
	
	public void testGenericITDsBridgeMethodsPR91381()  {runTest("abstract intertype methods and covariant returns");}
	public void testGenericITDsBridgeMethodsPR91381_2()  {runTest("abstract intertype methods and covariant returns - error");}

	// ----------------------------------------------------------------------------------------
	// generic declare parents tests
	// ----------------------------------------------------------------------------------------
	
	public void testPR96220_GenericDecp() {
		runTest("generic decp - simple");
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
	
	public void testAJDKBridgeMethodExamples() {
		runTest("ajdk notebook: bridge method examples");
	}
	
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
		
	public static Signature getClassSignature(Ajc ajc,String classname) {
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
			return sigAttr;
		} catch (ClassNotFoundException e) {
			fail("Couldn't find class "+classname+" in the sandbox directory.");
		}
		return null;
	}
	// Check the signature attribute on a class is correct
	public static void verifyClassSignature(Ajc ajc,String classname,String sig) {
		Signature sigAttr = getClassSignature(ajc,classname);
		assertTrue("Failed to find signature attribute for class "+classname,sigAttr!=null);
		assertTrue("Expected signature to be '"+sig+"' but was '"+sigAttr.getSignature()+"'",
				sigAttr.getSignature().equals(sig));		
	}
		

}
