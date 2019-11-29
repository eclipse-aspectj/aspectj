/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.knownlimitations;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class KnownLimitationsTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(KnownLimitationsTests.class);
  }

  protected java.net.URL getSpecFile() {
	  return getClassResource("knownlimitations.xml");
//    return new File("../tests/src/org/aspectj/systemtest/knownlimitations/knownlimitations.xml");
  }


  public void test001(){
    runTest("DEPRECATED: introduce of variables");
  }

  public void test002(){
    runTest("checking the contra-variant errors for typing of proceed");
  }

  public void test003(){
    runTest("introduction of static methods and fields on classes and interfaces");
  }

  public void test004(){
    runTest("advice on catch clauses");
  }

  public void test005(){
    runTest("holding onto proceed calls in a closure-like way");
  }

  public void test006(){
    runTest("PR#458 Compiler was incorrectly flagging error in advice on initialization and static initialization");
  }

  public void test007(){
    runTest("Introduced type unavailable to instanceof expressions in introduced methods");
  }

  public void test008(){
    runTest("enclosing join point not exported properly in pre-initialization join point");
  }

  public void test009(){
    runTest("cyclic pointcut definitions");
  }

  public void test010(){
    runTest("package typepattern with no packages (in default package)");
  }

  public void test011(){
    runTest("flag errors when binding args with indeterminate prefix and suffix");
  }

  public void test012(){
    runTest("around and return types + inlining optimizations");
  }

  public void test013(){
    runTest("source locations within expressions (hard case of constructor start)");
  }

  public void test014(){
    runTest("declaring method on superclass and subclass");
  }

  public void test015(){
    runTest("illegal name binding in around cflow");
  }

  public void test016(){
    runTest("incrementally change string size and wire in injar classes");
  }

  public void test017(){
    runTest("before():execution(new(..)) does not throw NoAspectBoundException");
  }

  public void test018(){
    runTest("declare error on handler/method execution with no code on binary ajc 1.1 classes");
  }

  public void test019(){
    runTest("declare error on handler/method execution with no code on binary javac 1.4 classes");
  }

  public void test020(){
    runTest("CLE: -help usage");
  }

  public void test021(){
    runTest("declare warnings on main - constructor execution");
  }

  public void test022(){
    runTest("declare warnings on binary javac 1.4 main - constructor execution");
  }

  public void test023(){
    runTest("declare warnings on binary ajc 1.1 main - constructor execution");
  }

  public void test024(){
    runTest("advice on handler join points should not throw unpermitted checked exceptions");
  }

  public void test025(){
    runTest("-nowarn suppresses XLint warnings");
  }

  public void test026(){
    runTest("warn:none suppresses XLint warnings");
  }

  public void test027(){
    runTest("-nowarn suppresses declare warnings");
  }

  public void test028(){
    runTest("-warn:none suppresses declare warnings");
  }

  public void test029(){
    runTest("insertion of lots of advice code can make branch offset for if too large");
  }

}

