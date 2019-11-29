/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.base;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class BaseTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(BaseTests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("baseTests.xml");
  }


  public void test001(){
    runTest("static and non-static before methods -- one file");
  }

  public void test002(){
    runTest("static and non-static before methods -- many files");
  }

  public void test003(){
    runTest("this redirection in non-static before methods");
  }

  public void test004(){
    runTest("DEPRECATED: introductions");
  }

  public void test005(){
    runTest("before constructors -- one file");
  }

  public void test006(){
    runTest("advise weaves find methods typed to builtins or non-woven classes");
  }

  public void test007(){
    runTest("make sure new weaves work inside of packages");
  }

  public void test008(){
    runTest("make sure new weaves work inside of packages (again)");
  }

  public void test009(){
    runTest("Inheritance of class and aspect vars in weaves");
  }

  public void test010(){
    runTest("Accessibility of class and aspect members from inside weaves");
  }

  public void test011(){
    runTest("Packaged aspects referring to packaged classes");
  }

  public void test012(){
    runTest("Inheritance of methods advised by aspects");
  }

  public void test013(){
    runTest("Inherited weaves on constructor");
  }

  public void test014(){
    runTest("Initializers in Aspect and Class Bodies");
  }

  public void test015(){
    runTest("Weaver Resolution of method names in method calls passed as args");
  }

  public void test016(){
    runTest("DEPRECATED: Introduce constructor with class inheritance");
  }

  public void test017(){
    runTest("empty and singular patterns on modifiers and throws");
  }

  public void test018(){
    runTest("DEPRECATED: Introduce of constructors");
  }

  public void test019(){
    runTest("Local declarations in advise bodies");
  }

  public void test020(){
    runTest("advises on introduced methods and constructors");
  }

  public void test021(){
    runTest("DEPRECATED: Method introduction into interface implemented by abstract class");
  }

  public void test022(){
    runTest("Crossing super calls in constructors");
  }

  public void test023(){
    runTest("empty modifier pattern");
  }

  public void test024(){
    runTest("Alpha conversion of argument names in designators");
  }

  public void test025(){
    runTest("advice uses its own formals to get actuals");
  }

  public void test026(){
    runTest("DEPRECATED:  introduce weaves can use this");
  }

  public void test027(){
    runTest("DEPRECATED: introduce of abstract methods works");
  }

  public void test028(){
    runTest("multiple arounds successfully intercept and return own values");
  }

  public void test029(){
    runTest("proper matching of overloaded constructors");
  }

  public void test030(){
    runTest("proper handling of formals in catch advice");
  }

  public void test031(){
    runTest("proper values for thisJoinPoint attributes");
  }

  public void test032(){
    runTest("supers, supers, supers");
  }

  public void test033(){
    runTest("operations on private and protected aspect members (++, -- in partciular)");
  }

  public void test034(){
    runTest("only register things once");
  }

  public void test035(){
    runTest("inner aspects and around");
  }

  public void test036(){
    runTest("aspect inheritance and advice, introduction");
  }

  public void test037(){
    runTest("thisResultObject for primitives");
  }

  public void test038(){
    runTest("introductions calling super.");
  }

  public void test039(){
    runTest("allow one argument calls even when there's a comma in the arglist");
  }

  public void test040(){
    runTest("advice on calls to static methods even works when called on super");
  }

}

