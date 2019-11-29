/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * ******************************************************************/
package org.aspectj.systemtest.ajc120;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc120Tests extends XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc120Tests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("ajc120.xml");
  }

  public void test001(){
    runTest("NPE in concretization error path");
  }

  public void test002(){
    runTest("priviledged aspects calling methods from advice");
  }

  public void test003(){
    runTest("No error on overloaded pointcuts in class");
  }

  public void test004(){
    runTest("No error on overloaded pointcuts unless binding variables");
  }

  public void test005(){
    runTest("Declare soft softening other exception types");
  }

  public void test006(){
    runTest("static method introduction on interfaces, should not be allowed");
  }

  public void test007(){
    runTest("External pointcut refs not resolved if named pointcut used by declare");
  }

  public void test008(){
    runTest("Appropriate message for 'after() thowing(Throwable th)' syntax error");
  }

  public void test009(){
    runTest("Ensure we don't look for source on the classpath when binary not found");
  }

  public void test010(){
    runTest("inner aspect containing declare soft");
  }

  public void test011(){
    runTest("Bad parser error recovery in advice");
  }

  public void test012(){
    runTest("Bad parser error recovery in java source");
  }

  public void test013(){
    runTest("compiler issues error on inner aspects when privilieged");
  }

  public void test014(){
    runTest("After throwing advice on ctors doesn't execute for inter-type decl field inits");
  }

  public void test015(){
    runTest("Introduced abstract method on abstract class not implemented by subtype (single source file)");
  }

  public void test016(){
    runTest("Introduced abstract method on abstract class with introduced concrete method (single source file)");
  }

  public void test017(){
    runTest("Introduced abstract method on abstract class with existing concrete method (single source file)");
  }

  public void test018(){
    runTest("aspect declares interface method (no modifiers)");
  }

  public void test019(){
    runTest("aspect declares interface method (abstract)");
  }

  public void test020(){
    runTest("aspect declares interface method (public abstract)");
  }

  public void test021(){
    runTest("Use class implementing interface via aspect (not woven together)");
  }

  public void test022(){
    runTest("Use class implementing interface via aspect (weave all together)");
  }

  public void test023(){
    runTest("Use class implementing interface via aspect (only one implementer)");
  }

  public void test024(){
    runTest("Erroneous exception conversion");
  }

  public void test025(){
    runTest("before():execution(new(..)) does not throw NoAspectBoundException");
  }

  public void test026(){
    runTest("Anomalous handling of inter-type declarations to abstract base classes in aspectj 1.1");
  }

  public void test027(){
    runTest("NPE When compiling intertype declaration");
  }

  public void test028(){
    runTest("declare warning on subtype constructor");
  }

  public void test029(){
    runTest("CatchClauseSignature has broken operation");
  }

  public void test030(){
    runTest("after returning with parameter: matching rules");
  }

  public void test031(){
    runTest("binary compatibility of advice method names - expect no error");
  }

  public void test032(){
    runTest("binary compatibility of advice method names - expect error");
  }

  public void test033(){
    runTest("binary compatibility of advice method names - expect no error");
  }

  public void test034(){
    runTest("mail list VerifyError with protected access");
  }

  public void test035(){
    runTest("Polymorphic ITD fails in CVS HEAD (From ajdt 1.1.6)");
  }

  public void test036(){
    runTest("ClasscastException on concretization of if(false)");
  }

  public void test037(){
    runTest("ClasscastException on concretization of if(false)");
  }

  public void test038(){
    runTest("Introduced abstract method on interface not implemented by subtype (weave altogether)");
  }

  public void test039(){
    runTest("declare String field on interface");
  }

  public void test040(){
    runTest("declare int field on interface");
  }

  public void test041(){
    runTest("declare Object field on interface");
  }

  public void test042(){
    runTest("fail in compiling aspect with overriding method introduction with different throws clause ");
  }

  public void test043(){
    runTest("super call in anonymous class created in around advice");
  }

  public void test044(){
    runTest("retitle warning to circular {advice} dependency at ...");
  }

  public void test045(){
    runTest("Introduce Unknown Type to class causes Null pointer exception");
  }

  public void test046(){
    runTest("Private members introduced via an interface are visible to the class");
  }

  public void test047(){
    runTest("declare precedence on a class should be a compile-time error");
  }

  public void test048(){
    runTest("declare precedence on a class should be a compile-time error");
  }

  public void test049(){
    runTest("NPE when binary weaving a ctor ITD");
  }

  public void test050(){
    runTest("NPE in compiler when using (an unusual) declare warning against a ctor ITD");
  }

  public void test051(){
    runTest("InterTypeMethodDeclaration.java:104");
  }

  public void test052(){
    runTest("nested uses of this() inside constructors not handled properly for initialization and preinitialization pointcuts");
  }

  public void test053(){
    runTest("wrong variable binding in || pointcuts");
  }

  public void test054(){
	runTest("error message for constructor-execution pcd");
  }

	public void test055(){
	  runTest("weaving using an empty jar in -injars");
	}

	public void test056(){
	  runTest("weaving using an empty jar in -aspectpath");
	}

}

