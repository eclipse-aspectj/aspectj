/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.incremental;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class IncrementalTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(IncrementalTests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/incremental/incremental.xml");
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
      assertTrue("Should have been advised",result.getStdOut().indexOf("World") != -1);
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
  
}

