/*******************************************************************************
 * Copyright (c) 2004 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;

import org.aspectj.tools.ajc.CompilationResult;

public class AnnotationBinding extends TestUtils {
	
  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/java5/annotations/binding");
  }
  
  ///////////////////////////////////// @ANNOTATION and CALL
  
  // Very simple annotation binding for 'call() && @annotation()'
  public void testCallAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"CallAnnBinding.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec());
  	RunResult rR = run("CallAnnBinding"); 
  }

  // 'call() && @annotation()' when the called method has multiple arguments
  public void testCallAnnotationBinding2() {
  	CompilationResult cR = ajc(baseDir,new String[]{"CallAnnBinding2.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec());
  	RunResult rR = run("CallAnnBinding2");
  }
  
  // 'call() && @annotation()' when the called method takes primitive arguments (YUCK!)
  public void testCallAnnotationBinding3() {
  	CompilationResult cR = ajc(baseDir,new String[]{"CallAnnBinding3.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("CallAnnBinding3");
  }
  
  // 'call() && @annotation()' when runtime type will exhibit different annotation (due to interface implementing)
  public void testCallAnnotationBinding4() {
  	CompilationResult cR = ajc(baseDir,new String[]{"CallAnnBinding4.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("CallAnnBinding4");
  }
  
  // 'call() && @annotation()' when target doesnt have an annotation !
  public void testCallAnnotationBinding5() {
  	CompilationResult cR = ajc(baseDir,new String[]{"CallAnnBinding5.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("CallAnnBinding5");
  }
  
  // 'call() && @annotation()' when runtime type will exhibit different annotation (due to subclassing)
  public void testCallAnnotationBinding6() {
  	CompilationResult cR = ajc(baseDir,new String[]{"CallAnnBinding6.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("CallAnnBinding6");
  }
  
  
  

  ///////////////////////////////////// @TARGET
  
  // 'call() && @target()'
  public void testAtTargetAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"AtTarget1.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("AtTarget1");
  }
  
  // 'call() && @target() && @target'
  public void testAtTargetAnnotationBinding2() {
  	CompilationResult cR = ajc(baseDir,new String[]{"AtTarget2.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("AtTarget2");
  }
  
  // 'call() && @target()' - using a type hierarchy where some levels are missing annotations
  public void testAtTargetAnnotationBinding3() {
  	CompilationResult cR = ajc(baseDir,new String[]{"AtTarget3.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("AtTarget3");
  }
  
  // 'call() && @target()' - using a type hierarchy where some levels are missing annotations 
  // but the annotation is inherited
  public void testAtTargetAnnotationBinding4() {
  	CompilationResult cR = ajc(baseDir,new String[]{"AtTarget4.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("AtTarget4");
  }
  
  
  // need to instantiate some types but have runtime types being diff

  ///////////////////////////////////// @ANNOTATION and EXECUTION
  
  // 'execution() && @annotation()' 
  public void testExecutionAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"ExecutionAnnBinding1.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("ExecutionAnnBinding1");
  }
  
  ///////////////////////////////////// @ANNOTATION and SET
  
  // 'set() && @annotation()' 
  public void testFieldAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"FieldAnnBinding1.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("FieldAnnBinding1");
  }
  
  // 'get() && @annotation()' 
  public void testFieldAnnotationBinding2() {
  	CompilationResult cR = ajc(baseDir,new String[]{"FieldAnnBinding2.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("FieldAnnBinding2");
  }
  
  // 'get() && @annotation()' when using array fields
  public void testFieldAnnotationBinding3() {
  	CompilationResult cR = ajc(baseDir,new String[]{"FieldAnnBinding3.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("FieldAnnBinding3");
  }
  
  ///////////////////////////////////// @ANNOTATION and CTOR-CALL
  
  // 'ctor-call(new) && @annotation()' 
  public void testCtorCallAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"CtorAnnBinding1.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("CtorAnnBinding1");
  }
  
  ///////////////////////////////////// @ANNOTATION and CTOR-CALL
  
  // 'ctor-execution() && @annotation()' 
  public void testCtorExecAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"CtorAnnBinding2.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("CtorAnnBinding2");
  }
  
  
  ///////////////////////////////////// @ANNOTATION and STATICINITIALIZATION
  
  // 'staticinitialization() && @annotation()' 
  public void testStaticInitAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"StaticInitBinding.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("StaticInitBinding");
  }
  
  ///////////////////////////////////// @ANNOTATION and PREINITIALIZATION
  
  // 'preinitialization() && @annotation()' 
  public void testPreInitAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"PreInitBinding.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("PreInitBinding");
  }
  
  ///////////////////////////////////// @ANNOTATION and INITIALIZATION
  
  // 'initialization() && @annotation()' 
  public void testInitAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"InitBinding.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("InitBinding");
  }
  
  ///////////////////////////////////// @ANNOTATION and ADVICEEXECUTION
  
  // 'adviceexecution() && @annotation()' 
  public void testAdviceExecAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"AdviceExecBinding.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("AdviceExecBinding");
  }
  
  ///////////////////////////////////// @ANNOTATION and HANDLER
  
  // 'handler() && @annotation()' 
  public void testHandlerAnnotationBinding1() {
  	CompilationResult cR = ajc(baseDir,new String[]{"HandlerBinding.aj","-1.5"});
  	assertMessages(cR,new EmptyMessageSpec()); 
  	RunResult rR = run("HandlerBinding");
  }
  

  ///////////////////////////////////// @ANNOTATION complex tests
  
  // Using package names for the types (including the annotation) - NO BINDING
  public void testPackageNamedTypesNoBinding() {
  	CompilationResult cR = ajc(new File(baseDir,"complexExample"),
  			new String[]{"A.java","B.java","Color.java","X.java","-1.5","-d","."});
  	assertMessages(cR,new EmptyMessageSpec());
  	RunResult rR = run("a.b.c.A");
  }
  
  // Using package names for the types (including the annotation) - INCLUDES BINDING
  public void testPackageNamedTypesWithBinding() {
  	CompilationResult cR = ajc(new File(baseDir,"complexExample"),
  			new String[]{"A.java","B.java","Color.java","X2.java","-1.5","-d","."});
  	assertMessages(cR,new EmptyMessageSpec());
  	RunResult rR = run("a.b.c.A");
  }
  
  // Binding with calls/executions of static methods
  public void testCallsAndExecutionsOfStaticMethods() {
  	CompilationResult cR = ajc(baseDir,new String[]{"StaticMethods.java","-1.5","-d","."});
  	assertMessages(cR,new EmptyMessageSpec());
  	RunResult rR = run("StaticMethods");
  }
}