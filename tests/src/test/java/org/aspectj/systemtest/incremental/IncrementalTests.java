/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.incremental;

import java.io.File;
import java.util.List;

import org.aspectj.ajdt.internal.core.builder.AbstractStateListener;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class IncrementalTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(IncrementalTests.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("incremental.xml");
  }

	protected void setUp() throws Exception {
		super.setUp();
		AjState.FORCE_INCREMENTAL_DURING_TESTING = true;
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		AjState.FORCE_INCREMENTAL_DURING_TESTING = false;
	}
  
  public void test001() throws Exception {
    runTest("expect class added in initial incremental tests");
    nextIncrement(false);
    copyFileAndDoIncrementalBuild("src.20/main/Main.java","src/main/Main.java");
    assertAdded("main/Target.class");
    run("main.Main");
  }

  public void test002() throws Exception {
    runTest("expect class removed in initial incremental tests");
    nextIncrement(false);
    assertAdded("main/Target.class");
    copyFileAndDoIncrementalBuild("src.20/main/Main.java","src/main/Main.java");
    assertDeleted("main/Target.class");
    run("main.Main");
  }

  public void test003() throws Exception {
    runTest("expect class updated in initial incremental tests");
    long lastTime = nextIncrement(true);
    copyFileAndDoIncrementalBuild("src.20/main/Main.java","src/main/Main.java");
    assertUpdated("main/Main.class",lastTime);
    run("main.Main");
  }

  public void test004() throws Exception {
    runTest("add file with class");
    nextIncrement(false);
    copyFileAndDoIncrementalBuild("src.20/main/Target.java","src/main/Target.java");
    assertAdded("main/Target.class");
    long lastTime = nextIncrement(true);
    copyFileAndDoIncrementalBuild("src.30/main/Main.java","src/main/Main.java");
    assertUpdated("main/Main.class",lastTime);
    run("main.Main");
  }

  public void test005()throws Exception {
    runTest("delete source file before incremental compile");
    nextIncrement(false);
    MessageSpec messageSpec = new MessageSpec(null,newMessageList(new Message(6,"delete/Target.java",null,null)));
    deleteFileAndDoIncrementalBuild("src/delete/DeleteMe.java",messageSpec);
    nextIncrement(false);
    copyFileAndDoIncrementalBuild("src.30/delete/Target.java","src/delete/Target.java");
    run("delete.Main");
  }

  public void test006() throws Exception {
    runTest("do everything in default package (sourceroots)");
    nextIncrement(false);
    copyFileAndDoIncrementalBuild("changes/Target.20.java","src/Target.java");
    run("Target");
    long lastTime = nextIncrement(true);
    copyFileAndDoIncrementalBuild("changes/Main.30.java","src/Main.java");
    assertUpdated("Main.class",lastTime);
    nextIncrement(false);
    MessageSpec messageSpec = new MessageSpec(null,newMessageList(new Message(6,"Main.java",null,null)));
    deleteFileAndDoIncrementalBuild("src/Target.java",messageSpec);
    nextIncrement(false);
    copyFileAndDoIncrementalBuild("changes/Main.50.java","src/Main.java");
    run("Main");    
  }

  public void test007() throws Exception {
    runTest("change sources in default package");
    nextIncrement(false);
    copyFileAndDoIncrementalBuild("changes/Main.20.java","src/Main.java");
    run("Main");
  }

  public void test008() throws Exception {
    runTest("change source");
    nextIncrement(false);
    copyFileAndDoIncrementalBuild("changes/Main.20.java","src/app/Main.java");
    run("app.Main");
  }
  
  /**
   * See bug report 85297.  We plugged a hole so that we check whether the contents of
   * directories on the classpath have changed when deciding whether we can do an
   * incremental build or not - the implementation didn't allow for the output location
   * being on the classpath.  This test verifies the fix is OK
   */
  public void testIncrementalOKWithOutputPathOnClasspath() throws Exception {
	  class MyStateListener extends AbstractStateListener {
  	    public boolean pathChange = false;
		public void pathChangeDetected() {pathChange = true;}
		public void aboutToCompareClasspaths(List oldClasspath, List newClasspath) {}
		public void detectedClassChangeInThisDir(File f) {}
		public void buildSuccessful(boolean wasFullBuild) {}
	  };
	  MyStateListener sl = new MyStateListener();
	  try {
	       AjState.stateListener = sl;
		  runTest("change source");
		  nextIncrement(false);
		  copyFileAndDoIncrementalBuild("changes/Main.20.java","src/app/Main.java");
		  assertTrue("Did not expect a path change to be detected ",!sl.pathChange);
		  run("app.Main");
	  } finally {
		  AjState.stateListener=null;
	  }
  }

  public void test009() throws Exception {
    runTest("incrementally change only string literal, still expect advice");
    long lastTime = nextIncrement(true);
    copyFileAndDoIncrementalBuild("changes/Main.20.java","src/packageOne/Main.java");
    assertUpdated("packageOne/Main.class",lastTime);
    run("packageOne.Main",new String[] {"in longer packageOne.Main.main(..)",
			                            "before main packageOne.Main"},
		null);
  }

  public void test010() throws Exception {
    runTest("add aspect source file and check world is rewoven");
    nextIncrement(false);
    copyFileAndDoIncrementalBuild("changes/Detour.20.java","src/Detour.java");
    assertAdded("Detour.class");
    run("Main");
  }

  public void test011() throws Exception {
    runTest("make sure additional classes generated during weave are deleted with src class file");
    nextIncrement(false);
    assertTrue("AdviceOnIntroduced$AjcClosure1.class exists",
    		new File(ajc.getSandboxDirectory(),"AdviceOnIntroduced$AjcClosure1.class").exists());
    deleteFileAndDoIncrementalBuild("src/AdviceOnIntroduced.java");
    assertDeleted("AdviceOnIntroduced$AjcClosure1.class");
  }

  public void test012() throws Exception {
    runTest("incremental with aspect-driven full rebuild");
    nextIncrement(false);
    MessageSpec messageSpec = new MessageSpec(newMessageList(new Message(3,"Main.java",null,null)),null);
    copyFileAndDoIncrementalBuild("changes/Aspect.20.java","src/Aspect.java",messageSpec);
    run("Main");
  }
  
  public void testIncrementalResourceAdditionToInPath() throws Exception {
      runTest("incremental with addition of resource to inpath directory");
      RunResult result = run("Hello");
      assertTrue("Should have been advised", result.getStdOut().contains("World"));
      nextIncrement(false);
      assertFalse("Resource file should not exist yet",new File(ajc.getSandboxDirectory(),"AResourceFile.txt").exists());
      copyFileAndDoIncrementalBuild("changes/AResourceFile.txt", "indir/AResourceFile.txt");
      // resources are *NOT* copied from inpath directories
      assertFalse("Resource file should not exist yet",new File(ajc.getSandboxDirectory(),"AResourceFile.txt").exists());
  }
  
  public void testAdditionOfResourceToInJar() throws Exception {
      runTest("incremental with addition of resource to inpath jar");
      nextIncrement(true);
      assertFalse("Resource file should not exist yet",new File(ajc.getSandboxDirectory(),"AResourceFile.txt").exists());
      copyFileAndDoIncrementalBuild("changes/MyJar.20.jar", "MyJar.jar");
      // resources *are* copied from inpath jars
      assertAdded("AResourceFile.txt");      
  }

  public void testRemovalOfResourceFromInJar() throws Exception {
      runTest("incremental with removal of resource from inpath jar");
      nextIncrement(true);
      assertAdded("AResourceFile.txt");
      copyFileAndDoIncrementalBuild("changes/MyJar.20.jar", "MyJar.jar");
      // resources *are* copied from inpath jars
      assertDeleted("AResourceFile.txt");      
  }
  
  public void testAdditionOfClassToInPathJar() throws Exception {
      runTest("incremental with addition of class to inpath jar");
      nextIncrement(true);
      assertFalse("Hello2.class should not exist yet",new File(ajc.getSandboxDirectory(),"Hello2.class").exists());
      copyFileAndDoIncrementalBuild("changes/MyJar.20.jar", "MyJar.jar");
      assertAdded("Hello2.class");      
  }

  public void testRemovalOfClassFromInPathJar() throws Exception {
      runTest("incremental with removal of class from inpath jar");
      nextIncrement(true);
      assertAdded("Hello2.class");
      copyFileAndDoIncrementalBuild("changes/MyJar.20.jar", "MyJar.jar");
      assertDeleted("Hello2.class");      
  }
  
  public void testAdditionOfClassToInJarJar() throws Exception {
      runTest("incremental with addition of class to injar jar");
      nextIncrement(true);
      assertFalse("Hello2.class should not exist yet",new File(ajc.getSandboxDirectory(),"Hello2.class").exists());
      copyFileAndDoIncrementalBuild("changes/MyJar.20.jar", "MyJar.jar");
      assertAdded("Hello2.class");      
  }

  public void testRemovalOfClassFromInJarJar() throws Exception {
      runTest("incremental with removal of class from injar jar");
      nextIncrement(true);
      assertAdded("Hello2.class");
      copyFileAndDoIncrementalBuild("changes/MyJar.20.jar", "MyJar.jar");
      assertDeleted("Hello2.class");      
  }
  
  public void testAdditionOfClassToInPathDir() throws Exception {
      runTest("incremental with addition of class to inpath dir");
      nextIncrement(true);
      assertFalse("Hello2.class should not exist yet",new File(ajc.getSandboxDirectory(),"Hello2.class").exists());
      copyFileAndDoIncrementalBuild("changes/Hello2.20.class", "indir/Hello2.class");
      assertAdded("Hello2.class");      
  }

  public void testRemovalOfClassFromInPathDir() throws Exception {
      runTest("incremental with removal of class from inpath dir");
      nextIncrement(true);
      assertAdded("Hello2.class");
      deleteFileAndDoIncrementalBuild("indir/Hello2.class");
      assertDeleted("Hello2.class");      
  }
   
  public void testUpdateOfClassInInPathDir() throws Exception {
      runTest("incremental with update of class in inpath dir");
      nextIncrement(true);
      RunResult before = run("Hello");
      assertTrue("Should say hello",before.getStdOut().startsWith("hello"));
      copyFileAndDoIncrementalBuild("changes/Hello.20.class", "indir/Hello.class");
      RunResult after = run("Hello");
      assertTrue("Should say updated hello",after.getStdOut().startsWith("updated hello"));
  }
  
  public void testUsesPointcutRelsWhenReferringToPCTIn2ndFile_pr90806() throws Exception {
	    runTest("NPE in genHandleIdentifier");
	    nextIncrement(true);
	    copyFileAndDoIncrementalBuild("changes/X.20.aj","src/X.aj");
  }
  
  public void testPersistingDeow_pr84033() throws Exception {
	  runTest("incremental declare error persists after fix");
	  copyFileAndDoIncrementalBuild("changes/Aspect.20.java", "src/pack/Aspect.java");
      nextIncrement(true);
	  RunResult before = run("pack.Main");
  }
  
  public void testIncrementalUpdateOfBodyInAroundAdvice_pr154054() throws Exception {
	  runTest("incremental update of body in around advice");
	  nextIncrement(true);
	  RunResult before = run("MyClass");
	  assertTrue("value should be 13 but was " + before.getStdOut(),
			  before.getStdOut().startsWith("13"));
	  // update value added to proceed
	  copyFileAndDoIncrementalBuild("changes/MyAspect.20.aj","src/MyAspect.aj");
	  RunResult after = run("MyClass");
	  assertTrue("value should be 14 but was " + after.getStdOut(),
			  after.getStdOut().startsWith("14"));
  }
  
  public void testIncrementalUpdateOfBodyInAroundAdviceWithString_pr154054() throws Exception {
	  runTest("incremental update of body in around advice with string");
	  nextIncrement(true);
	  RunResult before = run("MyClass");
	  assertTrue("expected 'Fred and George' in output but found " + before.getStdOut(),
			  before.getStdOut().startsWith("Fred and George"));
	  // update value added to proceed
	  copyFileAndDoIncrementalBuild("changes/MyAspect.30.aj","src/MyAspect.aj");
	  RunResult after = run("MyClass");
	  assertTrue("expected 'Fred and Harry' in output but found " + after.getStdOut(),
			  after.getStdOut().startsWith("Fred and Harry"));
  }
}

