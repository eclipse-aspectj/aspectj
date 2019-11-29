/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc151;

import java.io.IOException;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.systemtest.ajc150.GenericsTests;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc151Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// Some @DeclareParents testing
	public void testAtDecp_1() {
		runTest("atDecp - simple");
	}

	public void testAtDecp_2() {
		runTest("atDecp - annotation");
	}

	public void testAtDecp_3() {
		runTest("atDecp - binary interface");
	}

	public void testAtDecp_4() {
		runTest("atDecp - binary interface - 2");
	}

	public void testAnnotationsAndItds_pr98901() {
		runTest("annotations and itds");
	}

	public void testAnnotationsAndItds_pr98901_2() {
		runTest("annotations and itds - 2");
	}

	public void testCircularGenerics_pr133307() {
		runTest("circular generics");
	}

	public void testDeca() {
		runTest("doubly annotating a method with declare");
	}

	public void testDeca2() {
		runTest("doubly annotating a method with declare - 2");
	}

	public void testCrashingWithASM_pr132926_1() {
		runTest("crashing on annotation type resolving with asm - 1");
	}

	public void testCrashingWithASM_pr132926_2() {
		runTest("crashing on annotation type resolving with asm - 2");
	}

	public void testCrashingWithASM_pr132926_3() {
		runTest("crashing on annotation type resolving with asm - 3");
	}

	public void testGenericAdviceParameters_pr123553() {
		runTest("generic advice parameters");
	}

	public void testMemberTypesInGenericTypes_pr122458() {
		runTest("member types in generic types");
	}

	public void testMemberTypesInGenericTypes_pr122458_2() {
		runTest("member types in generic types - 2");
	}

	public void testNPEOnDeclareAnnotation_pr123695() {
		runTest("Internal nullptr exception with complex declare annotation");
	}

	public void testHasMemberPackageProblem_pr124105() {
		runTest("hasMember problems with packages");
	}

	public void testDifferentNumbersofTVars_pr124803() {
		runTest("generics and different numbers of type variables");
	}

	public void testDifferentNumbersofTVars_pr124803_2() {
		runTest("generics and different numbers of type variables - classes");
	}

	public void testParameterizedCollectionFieldMatching_pr124808() {
		runTest("parameterized collection fields matched via pointcut");
	}

	public void testGenericAspectsAndAnnotations_pr124654() {
		runTest("generic aspects and annotations");
	}

	public void testCallInheritedGenericMethod_pr124999() {
		runTest("calling inherited generic method from around advice");
	}

	public void testIncorrectlyReferencingPointcuts_pr122452() {
		runTest("incorrectly referencing pointcuts");
	}

	public void testIncorrectlyReferencingPointcuts_pr122452_2() {
		runTest("incorrectly referencing pointcuts - 2");
	}

	public void testInlinevisitorNPE_pr123901() {
		runTest("inlinevisitor NPE");
	}

	// public void testExposingWithintype_enh123423() { runTest("exposing withintype");}
	// public void testMissingImport_pr127299() { runTest("missing import gives funny message");}
	public void testUnusedInterfaceMessage_pr120527() {
		runTest("incorrect unused interface message");
	}

	public void testAtAspectInheritsAdviceWithTJPAndThis_pr125699() {
		runTest("inherit advice with this() and thisJoinPoint");
	}

	public void testAtAspectInheritsAdviceWithTJPAndThis_pr125699_2() {
		runTest("inherit advice with this() and thisJoinPoint - 2");
	}

	public void testBrokenLTW_pr128744() {
		runTest("broken ltw");
	}

	public void testAtAspectNoInvalidAbsoluteTypeName_pr126560() {
		runTest("@AJ deow doesn't throw an invalidAbsoluteTypeName when specify type in the same package");
	}

	public void testAtAspectNoInvalidAbsoluteTypeName_pr126560_2() {
		runTest("@AJ deow doesn't throw an invalidAbsoluteTypeName when specify type in the same file");
	}

	public void testArrayindexoutofbounds_pr129566() {
		runTest("arrayindexoutofbounds");
		// public class SkipList<T extends Comparable> extends Object implements Set<T>, Iterable<T>
		GenericsTests.verifyClassSignature(ajc, "common.SkipList",
				"<T::Ljava/lang/Comparable;>Ljava/lang/Object;Ljava/util/Set<TT;>;Ljava/lang/Iterable<TT;>;");
		// protected class SkipListElement<E> extends Object
		GenericsTests.verifyClassSignature(ajc, "common.SkipList$SkipListElement", "<E:Ljava/lang/Object;>Ljava/lang/Object;");
		// protected class SkipListIterator<E> implements Iterator<T>
		GenericsTests.verifyClassSignature(ajc, "common.SkipList$SkipListIterator",
				"<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Iterator<TT;>;");
	}

	public void testMixingNumbersOfTypeParameters_pr125080() {
		runTest("mixing numbers of type parameters");
		GenericsTests.verifyClassSignature(ajc, "AspectInterface", "<T:Ljava/lang/Object;S:Ljava/lang/Object;>Ljava/lang/Object;");
		GenericsTests.verifyClassSignature(ajc, "AbstractAspect",
				"<T:Ljava/lang/Object;>Ljava/lang/Object;LAspectInterface<TT;Ljava/lang/Integer;>;");
		GenericsTests.verifyClassSignature(ajc, "ConcreteAspect", "LAbstractAspect<Ljava/lang/String;>;");
	}

	public void testMixingNumbersOfTypeParameters_pr125080_2() {
		runTest("mixing numbers of type parameters - 2");
		GenericsTests.verifyClassSignature(ajc, "AspectInterface", "<T:Ljava/lang/Object;S:Ljava/lang/Number;>Ljava/lang/Object;");
		GenericsTests.verifyClassSignature(ajc, "AbstractAspect",
				"<T:Ljava/lang/Object;>Ljava/lang/Object;LAspectInterface<TT;Ljava/lang/Integer;>;");
		GenericsTests.verifyClassSignature(ajc, "ConcreteAspect", "LAbstractAspect<LStudent;>;");
	}

	public void testIProgramElementMethods_pr125295() throws IOException {
		runTest("new IProgramElement methods");
		IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();

		IProgramElement typeC = top.findElementForType("pkg", "C");
		IProgramElement pe = top.findElementForSignature(typeC, IProgramElement.Kind.METHOD, "foo(int,java.lang.Object)");
		assertNotNull("Couldn't find 'foo' element in the tree", pe);
		// check that the defaults return the fully qualified arg
		assertEquals("foo(int,java.lang.Object)", pe.toLabelString());
		assertEquals("C.foo(int,java.lang.Object)", pe.toLinkLabelString());
		assertEquals("foo(int,java.lang.Object)", pe.toSignatureString());
		// check that can get hold of the non qualified args
		assertEquals("foo(int,Object)", pe.toLabelString(false));
		assertEquals("C.foo(int,Object)", pe.toLinkLabelString(false));
		assertEquals("foo(int,Object)", pe.toSignatureString(false));

		IProgramElement typeA = top.findElementForType("pkg", "A");
		IProgramElement pe2 = top.findElementForSignature(typeA,IProgramElement.Kind.METHOD,"printParameters(org.aspectj.lang.JoinPoint)");
		assertNotNull("Couldn't find 'printParameters' element in the tree", pe2);
		// the argument is org.aspectj.lang.JoinPoint, check that this is added
		assertFalse("printParameters method should have arguments", pe2.getParameterSignatures().isEmpty());
	}

	public void testParameterizedEnum_pr126316() {
		runTest("E extends Enum(E) again");
	}

	public void testSwallowedException() {
		runTest("swallowed exceptions");
	}

	public void testAtAspectVerifyErrorWithAfterThrowingAndthisJoinPoint_pr122742() {
		runTest("@AJ VerifyError with @AfterThrowing and thisJoinPoint argument");
	}

	public void testAtAspectVerifyErrorWithAfterReturningAndthisJoinPoint_pr122742() {
		runTest("@AJ VerifyError with @AfterReturning and thisJoinPoint argument");
	}

	public void testSwallowedExceptionIgnored() {
		runTest("swallowed exceptions with xlint");
	}

	public void testGenericAspectWithUnknownType_pr131933() {
		runTest("no ClassCastException with generic aspect and unknown type");
	}

	/*
	 * @AspectJ bugs and enhancements
	 */
	// public void testAtAspectInheritsAdviceWithTJPAndThis_pr125699 () {
	// runTest("inherit adivce with this() and thisJoinPoint");
	// }
	public void testAtAspectInheritsAbstractPointcut_pr125810() {
		runTest("warning when inherited pointcut not made concrete");
	}

	public void testAtAspectWithoutJoinPointImport_pr121616() {
		runTest("@AJ without JoinPoint import");
	}

	public void testAtAspectDeclareParentsRetainsFieldState_pr122370() {
		runTest("@AJ declare parents retains field state");
	}

	public void testAtAspectNoNPEWithPcdContainingOrThisAndWildcard_pr128237() {
		runTest("@AJ no npe with pointcut containing or, this and a wildcard");
	}

	/*
	 * Load-time weaving bugs and enhancements
	 */
	public void testEmptyPointcutInAtAspectJ_pr125475() {
		runTest("define empty pointcut using an annotation");
	}

	public void testEmptyPointcutInAtAspectJ_pr125475_2() {
		runTest("define empty pointcut using an annotation - 2");
	}

	public void testEmptyPointcutInAtAspectJWithLTW_pr125475() {
		runTest("define empty pointcut using aop.xml");
	}

	public void testGenericAspectsWithAnnotationTypeParameters() {
		runTest("Generic aspects with annotation type parameters");
	}

	public void testPointcutInterfaces_pr130869() {
		runTest("Pointcut interfaces");
	}

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc151Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc151.xml");
	}

}