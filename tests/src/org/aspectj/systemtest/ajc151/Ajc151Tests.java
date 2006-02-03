/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc151;

import java.io.File;

import junit.framework.Test;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.systemtest.ajc150.GenericsTests;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc151Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	  
  public void testMemberTypesInGenericTypes_pr122458()    { runTest("member types in generic types");}
  public void testMemberTypesInGenericTypes_pr122458_2()  { runTest("member types in generic types - 2");}
  public void testNPEOnDeclareAnnotation_pr123695() { runTest("Internal nullptr exception with complex declare annotation");}
  public void testHasMemberPackageProblem_pr124105() { runTest("hasMember problems with packages");}
  public void testDifferentNumbersofTVars_pr124803() { runTest("generics and different numbers of type variables");}
  public void testDifferentNumbersofTVars_pr124803_2() { runTest("generics and different numbers of type variables - classes");}
  public void testParameterizedCollectionFieldMatching_pr124808() { runTest("parameterized collection fields matched via pointcut");}
  public void testGenericAspectsAndAnnotations_pr124654() { runTest("generic aspects and annotations");}
  public void testCallInheritedGenericMethod_pr124999() { runTest("calling inherited generic method from around advice");}
  public void testIncorrectlyReferencingPointcuts_pr122452()    { runTest("incorrectly referencing pointcuts");}
  public void testIncorrectlyReferencingPointcuts_pr122452_2()    { runTest("incorrectly referencing pointcuts - 2");}
  public void testInlinevisitorNPE_pr123901() { runTest("inlinevisitor NPE");}
  //public void testExposingWithintype_enh123423() { runTest("exposing withintype");}
  
  public void testMixingNumbersOfTypeParameters_pr125080()   { 
	  runTest("mixing numbers of type parameters");    
	  GenericsTests.verifyClassSignature(ajc,"AspectInterface","<T:Ljava/lang/Object;S:Ljava/lang/Object;>Ljava/lang/Object;");
	  GenericsTests.verifyClassSignature(ajc,"AbstractAspect","<T:Ljava/lang/Object;>Ljava/lang/Object;LAspectInterface<TT;Ljava/lang/Integer;>;");
	  GenericsTests.verifyClassSignature(ajc,"ConcreteAspect","LAbstractAspect<Ljava/lang/String;>;");
  }
  
  public void testMixingNumbersOfTypeParameters_pr125080_2() { 
	  runTest("mixing numbers of type parameters - 2"); 
	  GenericsTests.verifyClassSignature(ajc,"AspectInterface","<T:Ljava/lang/Object;S:Ljava/lang/Number;>Ljava/lang/Object;");
	  GenericsTests.verifyClassSignature(ajc,"AbstractAspect","<T:Ljava/lang/Object;>Ljava/lang/Object;LAspectInterface<TT;Ljava/lang/Integer;>;");
	  GenericsTests.verifyClassSignature(ajc,"ConcreteAspect","LAbstractAspect<LStudent;>;");
  }
  
  public void testIProgramElementMethods_pr125295() {
	  runTest("new IProgramElement methods");  
  	  IHierarchy top = AsmManager.getDefault().getHierarchy();

  	  IProgramElement pe = top.findElementForType("pkg","foo");
  	  assertNotNull("Couldn't find 'foo' element in the tree",pe);
  	  // check that the defaults return the fully qualified arg
  	  assertEquals("foo(int, java.lang.Object)",pe.toLabelString());
  	  assertEquals("C.foo(int, java.lang.Object)",pe.toLinkLabelString());
  	  assertEquals("foo(int, java.lang.Object)",pe.toSignatureString());
  	  // check that can get hold of the non qualified args
  	  assertEquals("foo(int, Object)",pe.toLabelString(false));
  	  assertEquals("C.foo(int, Object)",pe.toLinkLabelString(false));
  	  assertEquals("foo(int, Object)",pe.toSignatureString(false));

  	  IProgramElement pe2 = top.findElementForType("pkg","printParameters");
  	  assertNotNull("Couldn't find 'printParameters' element in the tree",pe2);
  	  // the argument is org.aspectj.lang.JoinPoint, check that this is added
  	  assertFalse("printParameters method should have arguments",pe2.getParameterTypes().isEmpty());	  
  }

  public void testParameterizedEnum_pr126316() {
	  runTest("E extends Enum(E) again");
  }
  
  /*
   * @AspectJ bugs and enhancements
   */
//  public void testAtAspectInheritsAdviceWithTJPAndThis_pr125699 () {
//	  runTest("inherit adivce with this() and thisJoinPoint"); 
//  }
  
  public void testAtAspectInheritsAbstractPointcut_pr125810 () {
	  runTest("warning when inherited pointcut not made concrete"); 
  }
  
  /*
   * Load-time weaving bugs and enhancements
   */
  public void testEmptyPointcutInAtAspectJ_pr125475 () {
	  runTest("define empty pointcut using an annotation"); 
  }

  public void testEmptyPointcutInAtAspectJ_pr125475_2() {
	  runTest("define empty pointcut using an annotation - 2"); 
  }
  
  public void testEmptyPointcutInAtAspectJWithLTW_pr125475 () {
	  runTest("define empty pointcut using aop.xml"); 
  }
  
  public void testLTWGeneratedAspectWithAbstractMethod_pr125480 () {
	  runTest("aop.xml aspect inherits abstract method that has concrete implementation in parent"); 
  }
  
  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc151Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc151/ajc151.xml");
  }
  
}