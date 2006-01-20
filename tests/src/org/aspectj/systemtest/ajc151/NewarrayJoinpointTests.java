/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc151;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/*
 * The design:
 * 
 * There are 3 instructions that create arrays:
 * 
 * - NEWARRAY for primitive arrays
 * - ANEWARRAY for object arrays
 * - MULTIANEWARRAY for multidimensional arrays
 * 
 * The changes to expose the new joinpoint are in:
 *   BcelClassWeaver.match(LazyMethodGen mg,InstructionHandle ih,BcelShadow enclosingShadow,List shadowAccumulator)
 *   
 * Determining the type of the array is easy.  Determining the size of the array is not easy statically, it is on the stack.
 * 
 * 
 * What still needs testing:
 * - structure model
 * 
 */ 


public class NewarrayJoinpointTests extends XMLBasedAjcTestCase {

  // when its the creation of a new 'object' (not a primitive) single dimension array
  public void testTheBasics_1() { runTest("basics"); }
  public void testTheBasics_2() { runTest("basics - 2"); }
  public void testWhatShouldntMatch()    { runTest("shouldnt match"); }
  public void testThisJoinPoint()        { runTest("thisjoinpoint"); }
  public void testDifferentAdviceKinds() { runTest("different advice kinds");}
  public void testArgs() { runTest("args");}
  
  // when it is the creation of a new array of primitives
  public void testBasicWithAPrimitiveArray() { runTest("basic primitive array creation");}
  
  
  // when it is the creation of a new multi-dimensional array
  public void testBasicWithAMultiDimensionalArray() { runTest("multi dimensional array creation"); }
  public void testArgsWithAMultiDimensionalArray() { runTest("multi dimensional array args");}

  // complicated
  public void testUsingTargetAndAfterReturningAdvice() { runTest("using target and after returning");}
  public void testUsingItForReal() { runTest("using it for real");}
  public void testDifferentiatingArrayTypes() { runTest("differentiating array types");}
  public void testStructureModel() { runTest("structure model");}

  //
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(NewarrayJoinpointTests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc151/newarray_joinpoint.xml");
  }
	
}
