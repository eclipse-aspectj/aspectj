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
import org.aspectj.tools.ajc.CompilationResult;
import org.aspectj.util.FileUtil;

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
    MessageSpec messageSpec = new MessageSpec(newMessageList(new Message(4,"Main.java",null,null)),null);
    copyFileAndDoIncrementalBuild("changes/Aspect.20.java","src/Aspect.java",messageSpec);
    run("Main");
  }

  private long nextIncrement(boolean doWait) {
  	long time = System.currentTimeMillis();
  	if (doWait) {
  		try {
  	  		Thread.sleep(1000);
  		} catch (InterruptedException intEx) {}
  	}
  	return time;
  }
  
  private void copyFileAndDoIncrementalBuild(String from, String to) throws Exception {
  	String dir = getCurrentTest().getDir();
  	FileUtil.copyFile(new File(dir + File.separator + from),
  			          new File(ajc.getSandboxDirectory(),to));
  	CompilationResult result = ajc.doIncrementalCompile();
  	assertNoMessages(result,"Expected clean compile from test '" + getCurrentTest().getTitle() + "'");
  }
  
  private void copyFileAndDoIncrementalBuild(String from, String to, MessageSpec expectedResults) throws Exception {
  	String dir = getCurrentTest().getDir();
  	FileUtil.copyFile(new File(dir + File.separator + from),
  			          new File(ajc.getSandboxDirectory(),to));
  	CompilationResult result = ajc.doIncrementalCompile();
  	assertMessages(result,"Test '" + getCurrentTest().getTitle() + "' did not produce expected messages",expectedResults);
  }
  
  
  private void deleteFileAndDoIncrementalBuild(String file, MessageSpec expectedResult) throws Exception {
  	new File(ajc.getSandboxDirectory(),file).delete();
  	CompilationResult result = ajc.doIncrementalCompile();
  	assertMessages(result,"Test '" + getCurrentTest().getTitle() + "' did not produce expected messages",expectedResult);
  }
  
  private void deleteFileAndDoIncrementalBuild(String file) throws Exception {
  	deleteFileAndDoIncrementalBuild(file,MessageSpec.EMPTY_MESSAGE_SET);
  }
  
  private void assertAdded(String file) {
  	assertTrue("File " + file + " should have been added",
  			new File(ajc.getSandboxDirectory(),file).exists());
  }

  private void assertDeleted(String file) {
  	assertFalse("File " + file + " should have been deleted",
  			new File(ajc.getSandboxDirectory(),file).exists());
  }

  private void assertUpdated(String file, long sinceTime) {
  	File f = new File(ajc.getSandboxDirectory(),file);
  	assertTrue("File " + file + " should have been updated",f.lastModified() > sinceTime);
  }
}

