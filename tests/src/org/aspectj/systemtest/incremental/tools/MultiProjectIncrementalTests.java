/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 * Andy Clement          initial implementation
* ******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.testing.util.FileUtil;

/**
 * The superclass knows all about talking through Ajde to the compiler.
 * The superclass isn't in charge of knowing how to simulate overlays
 * for incremental builds, that is in here.  As is the ability to
 * generate valid build configs based on a directory structure.  To
 * support this we just need access to a sandbox directory - this
 * sandbox is managed by the superclass (it only assumes all builds occur
 * in <sandboxDir>/<projectName>/ )
 * 
 * The idea is you can initialize multiple projects in the sandbox and
 * they can all be built independently, hopefully exploiting
 * incremental compilation.  Between builds you can alter the contents
 * of a project using the alter() method that overlays some set of 
 * new files onto the current set (adding new files/changing existing
 * ones) - you can then drive a new build and check it behaves as
 * expected.
 */
public class MultiProjectIncrementalTests extends AjdeInteractionTestbed {

	private static boolean VERBOSE = false;
	
	protected void setUp() throws Exception {
		super.setUp();
		AjdeInteractionTestbed.VERBOSE = VERBOSE;
	}
	
	
	// Compile a single simple project
	public void testTheBasics() {
		initialiseProject("P1");
		build("P1"); // This first build will be batch
		build("P1");
		checkWasntFullBuild();
		checkCompileWeaveCount(0,0);
	}
	
	
	// Make simple changes to a project, adding a class
	public void testSimpleChanges() {
		initialiseProject("P1");
		build("P1"); // This first build will be batch
		alter("P1","inc1"); // adds a single class
		build("P1");
		checkCompileWeaveCount(1,-1);
		build("P1"); 
		checkCompileWeaveCount(0,-1);
	}
	
	
	// Make simple changes to a project, adding a class and an aspect
	public void testAddingAnAspect() {
		initialiseProject("P1");
		build("P1");
		alter("P1","inc1"); // adds a class
		alter("P1","inc2"); // adds an aspect
		build("P1");
		long timeTakenForFullBuildAndWeave = getTimeTakenForBuild();
		checkWasntFullBuild();
		checkCompileWeaveCount(2,3);
		build("P1");
		long timeTakenForSimpleIncBuild = getTimeTakenForBuild();
		// I don't think this test will have timing issues as the times should be *RADICALLY* different
		// On my config, first build time is 2093ms  and the second is 30ms
		assertTrue("Should not take longer for the trivial incremental build!  first="+timeTakenForFullBuildAndWeave+
				   "ms  second="+timeTakenForSimpleIncBuild+"ms",
				   timeTakenForSimpleIncBuild<timeTakenForFullBuildAndWeave);
	}
	
	
	public void testBuildingTwoProjectsInTurns() {
		configureBuildStructureModel(true);
		initialiseProject("P1");
		initialiseProject("P2");
		build("P1");
		build("P2");
		build("P1");
		checkWasntFullBuild();
		build("P2");
		checkWasntFullBuild();
	}
	
	
	/**
	 * In order for this next test to run, I had to move the weaver/world pair we keep in the
	 * AjBuildManager instance down into the state object - this makes perfect sense - otherwise
	 * when reusing the state for another project we'd not be switching to the right weaver/world
	 * for that project.
	 */
	public void testBuildingTwoProjectsMakingSmallChanges() {
		
		configureBuildStructureModel(true);
		
		initialiseProject("P1");
		initialiseProject("P2");

		build("P1");
		build("P2");
		build("P1");
		checkWasntFullBuild();
		
		build("P2");
		checkWasntFullBuild();
		
		alter("P1","inc1"); // adds a class
		alter("P1","inc2"); // adds an aspect
		build("P1");
		checkWasntFullBuild();
	}
	
	
	/** 
	 * Setup up two simple projects and build them in turn - check the
	 * structure model is right after each build 
	 */
	public void testBuildingTwoProjectsAndVerifyingModel() {
		
		configureBuildStructureModel(true);
		
		initialiseProject("P1");
		initialiseProject("P2");

		build("P1");	
		checkForNode("pkg","C",true);

		build("P2");
		checkForNode("pkg","C",false);

		build("P1");
		checkForNode("pkg","C",true);
		
		build("P2");
		checkForNode("pkg","C",false);
	}


	// Setup up two simple projects and build them in turn - check the
	// structure model is right after each build
	public void testBuildingTwoProjectsAndVerifyingStuff() {
		configureBuildStructureModel(true);
		
		initialiseProject("P1");
		initialiseProject("P2");

		build("P1");	
		checkForNode("pkg","C",true);

		build("P2");
		checkForNode("pkg","C",false);

		build("P1");
		checkForNode("pkg","C",true);
		
		build("P2");
		checkForNode("pkg","C",false);
	}
	

	/**
	 * Complex.  Here we are testing that a state object records structural changes since
	 * the last full build correctly.  We build a simple project from scratch - this will
	 * be a full build and so the structural changes since last build count should be 0.
	 * We then alter a class, adding a new method and check structural changes is 1.
	 */
	public void testStateManagement1() {
		
		File binDirectoryForP1 = new File(getFile("P1","bin"));
		
		initialiseProject("P1");
		build("P1"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirectoryForP1);
		assertTrue("There should be a state object for project P1",ajs!=null);
		assertTrue("Should be no structural changes as it was a full build but found: "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==0);
		
		
		alter("P1","inc3"); // adds a method to the class C.java
		build("P1");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("P1","bin")));
		assertTrue("There should be state for project P1",ajs!=null);
		checkWasntFullBuild();
		assertTrue("Should be one structural changes as it was a full build but found: "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==1);

	}
	
	
	/**
	 * Complex.  Here we are testing that a state object records structural changes since
	 * the last full build correctly.  We build a simple project from scratch - this will
	 * be a full build and so the structural changes since last build count should be 0.
	 * We then alter a class, changing body of a method, not the structure and
	 * check struc changes is still 0.
	 */
	public void testStateManagement2() {		
		File binDirectoryForP1 = new File(getFile("P1","bin"));
		
		initialiseProject("P1");
		alter("P1","inc3"); // need this change in here so 'inc4' can be applied without making
		                    // it a structural change
		build("P1"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirectoryForP1);
		assertTrue("There should be state for project P1",ajs!=null);
		assertTrue("Should be no struc changes as its a full build: "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==0);
		
		
		alter("P1","inc4"); // changes body of main() method but does *not* change the structure of C.java
		build("P1");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("P1","bin")));
		assertTrue("There should be state for project P1",ajs!=null);
		checkWasntFullBuild();
		assertTrue("Shouldn't be any structural changes but there were "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==0);
	}
	
	
	/**
	 * Now the most complex test.  Create a dependancy between two projects.  Building
	 * one may affect whether the other does an incremental or full build.  The
	 * structural information recorded in the state object should be getting used
	 * to control whether a full build is necessary...
	 */
	public void testBuildingDependantProjects() {
		initialiseProject("P1");
		initialiseProject("P2");
		configureNewProjectDependency("P2","P1");
		
		build("P1");
		build("P2"); // now everything is consistent and compiled
		alter("P1","inc1"); // adds a second class
		build("P1");
		build("P2"); // although a second class was added - P2 can't be using it, so we don't full build here :)
		checkWasntFullBuild();
		alter("P1","inc3"); // structurally changes one of the classes
		build("P1");
		build("P2"); // build notices the structural change
		checkWasFullBuild();
		alter("P1","inc4");
		build("P1");
		build("P2"); // build sees a change but works out its not structural
		checkWasntFullBuild();
	}
	
	
	public void testPr85132() {
		initialiseProject("PR85132");
		build("PR85132");
		alter("PR85132","inc1");
		build("PR85132");
	}
	
	public void testPr92837() {
		initialiseProject("PR92837");
		build("PR92837");
		alter("PR92837","inc1");
		build("PR92837");
	}
	
	// If you fiddle with the compiler options - you must manually reset the options at the end of the test
	public void testPr117209() {
		try {
			initialiseProject("PR117209");
			configureNonStandardCompileOptions("-proceedOnError");
			build("PR117209");
			checkCompileWeaveCount(6,6);
		} finally {
			MyBuildOptionsAdapter.reset();
		}
	}
	
	public void testPr114875() {
		initialiseProject("pr114875");
		build("pr114875");
		alter("pr114875","inc1");
		build("pr114875");
		checkWasntFullBuild();
		alter("pr114875","inc2");
		build("pr114875");
		checkWasntFullBuild();
	}
	
	public void testPr117882() {
//		AjdeInteractionTestbed.VERBOSE=true;
//		AjdeInteractionTestbed.configureBuildStructureModel(true);
		initialiseProject("PR117882");
		build("PR117882");
		checkWasFullBuild();
		alter("PR117882","inc1");
		build("PR117882");
		checkWasntFullBuild();
//		AjdeInteractionTestbed.VERBOSE=false;
//		AjdeInteractionTestbed.configureBuildStructureModel(false);
	}
	
	public void testPr117882_2() {
//		AjdeInteractionTestbed.VERBOSE=true;
//		AjdeInteractionTestbed.configureBuildStructureModel(true);
		initialiseProject("PR117882_2");
		build("PR117882_2");
		checkWasFullBuild();
		alter("PR117882_2","inc1");
		build("PR117882_2");
		checkWasntFullBuild();
		checkCompileWeaveCount(1,4);
		//fullBuild("PR117882_2");
		//checkWasFullBuild();
//		AjdeInteractionTestbed.VERBOSE=false;
//		AjdeInteractionTestbed.configureBuildStructureModel(false);
	}
	
	public void testPr115251() {
		//AjdeInteractionTestbed.VERBOSE=true;
		initialiseProject("PR115251");
		build("PR115251");
		checkWasFullBuild();
		alter("PR115251","inc1");
		build("PR115251");
		checkWasntFullBuild();
	}

	
/*	public void testPr111779() {
		super.VERBOSE=true;
		initialiseProject("PR111779");
		build("PR111779");
		alter("PR111779","inc1");
		build("PR111779");
	}
*/

	public void testPr93310_1() {
		initialiseProject("PR93310_1");
		build("PR93310_1");
		checkWasFullBuild();
		String fileC2 = getWorkingDir().getAbsolutePath() + File.separatorChar + "PR93310_1" + File.separatorChar + "src" + File.separatorChar + "pack" + File.separatorChar + "C2.java";
		(new File(fileC2)).delete();
		alter("PR93310_1","inc1");
		build("PR93310_1");
		checkWasFullBuild();
		int l =  AjdeInteractionTestbed.MyStateListener.detectedDeletions.size();
		assertTrue("Expected one deleted file to be noticed, but detected: "+l,l==1);
		String name = (String)AjdeInteractionTestbed.MyStateListener.detectedDeletions.get(0);
		assertTrue("Should end with C2.java but is "+name,name.endsWith("C2.java"));
	}
	
	public void testPr93310_2() {
		initialiseProject("PR93310_2");
		build("PR93310_2");
		checkWasFullBuild();
		String fileC2 = getWorkingDir().getAbsolutePath() + File.separatorChar + "PR93310_2" + File.separatorChar + "src" + File.separatorChar + "pack" + File.separatorChar + "C2.java";
		(new File(fileC2)).delete();
		alter("PR93310_2","inc1");
		build("PR93310_2");
		checkWasFullBuild();
		int l =  AjdeInteractionTestbed.MyStateListener.detectedDeletions.size();
		assertTrue("Expected one deleted file to be noticed, but detected: "+l,l==1);
		String name = (String)AjdeInteractionTestbed.MyStateListener.detectedDeletions.get(0);
		assertTrue("Should end with C2.java but is "+name,name.endsWith("C2.java"));
	}
	
	// Stage1: Compile two files, pack.A and pack.A1 - A1 sets a protected field in A. 
	// Stage2: make the field private in class A > gives compile error
	// Stage3: Add a new aspect whilst there is a compile error !
	public void testPr113531() {
		initialiseProject("PR113531");
		build("PR113531");
		assertFalse("build should have compiled ok",
				MyTaskListManager.hasErrorMessages());
		alter("PR113531","inc1");
		build("PR113531");
		assertEquals("error message should be 'foo cannot be resolved' ",
				"foo cannot be resolved",
				((IMessage)MyTaskListManager.getErrorMessages().get(0))
					.getMessage());
		alter("PR113531","inc2");
		build("PR113531");
		assertTrue("There should be no exceptions handled:\n"+MyErrorHandler.getErrorMessages(),
				MyErrorHandler.getErrorMessages().isEmpty());		
		assertEquals("error message should be 'foo cannot be resolved' ",
				"foo cannot be resolved",
				((IMessage)MyTaskListManager.getErrorMessages().get(0))
					.getMessage());
	}

	public void testPr112736() {
		AjdeInteractionTestbed.VERBOSE = true;
		initialiseProject("PR112736");
		build("PR112736");
		checkWasFullBuild();
		String fileC2 = getWorkingDir().getAbsolutePath() + File.separatorChar + "PR112736" + File.separatorChar + "src" + File.separatorChar + "pack" + File.separatorChar + "A.java";
		(new File(fileC2)).delete();
		alter("PR112736","inc1");
		build("PR112736");
		checkWasFullBuild();
	}
	
	
	// other possible tests:
	// - memory usage (freemem calls?)
	// - relationship map

	// ---------------------------------------------------------------------------------------------------

	/**
	 * Check we compiled/wove the right number of files, passing '-1' indicates you don't care about
	 * that number.
	 */
	private void checkCompileWeaveCount(int expCompile,int expWoven) {
		if (expCompile!=-1 && getCompiledFiles().size()!=expCompile)
			fail("Expected compilation of "+expCompile+" files but compiled "+getCompiledFiles().size()+
					"\n"+printCompiledAndWovenFiles());
		if (expWoven!=-1 && getWovenClasses().size()!=expWoven)
			fail("Expected weaving of "+expWoven+" files but wove "+getWovenClasses().size()+
					"\n"+printCompiledAndWovenFiles());
	}
	
	private void checkWasntFullBuild() {
		assertTrue("Shouldn't have been a full (batch) build",!wasFullBuild());
	}
	
	private void checkWasFullBuild() {
		assertTrue("Should have been a full (batch) build",wasFullBuild());
	}
	
	private void checkForNode(String packageName,String typeName,boolean shouldBeFound) {
		IProgramElement ipe = AsmManager.getDefault().getHierarchy().findElementForType(packageName,typeName);
		if (shouldBeFound) {
           if (ipe==null) printModel();
		   assertTrue("Should have been able to find '"+packageName+"."+typeName+"' in the asm",ipe!=null);
		} else {
		   if (ipe!=null) printModel();
		   assertTrue("Should have NOT been able to find '"+packageName+"."+typeName+"' in the asm",ipe==null);
		}
	}


	private void printModel() {
		try {
			AsmManager.dumptree(AsmManager.getDefault().getHierarchy().getRoot(),0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public void build(String projectName) {
		constructUpToDateLstFile(projectName,"build.lst");
		build(projectName,"build.lst");
		if (AjdeInteractionTestbed.VERBOSE) printBuildReport();
	}
	
	public void fullBuild(String projectName) {
		constructUpToDateLstFile(projectName,"build.lst");
		fullBuild(projectName,"build.lst");
		if (AjdeInteractionTestbed.VERBOSE) printBuildReport();
	}

	private void constructUpToDateLstFile(String pname,String configname) {
		File projectBase = new File(sandboxDir,pname);
		File toConstruct = new File(projectBase,configname);
		List filesForCompilation = new ArrayList();
		collectUpFiles(projectBase,projectBase,filesForCompilation);

		try {
			FileOutputStream fos = new FileOutputStream(toConstruct);
			DataOutputStream dos = new DataOutputStream(fos);
			for (Iterator iter = filesForCompilation.iterator(); iter.hasNext();) {
				String file = (String) iter.next();
				dos.writeBytes(file+"\n");
			}
			dos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private void collectUpFiles(File location,File base,List collectionPoint) {
		String contents[] = location.list();
		if (contents==null) return;
		for (int i = 0; i < contents.length; i++) {
			String string = contents[i];
			File f = new File(location,string);
			if (f.isDirectory()) {
				collectUpFiles(f,base,collectionPoint);
			} else if (f.isFile() && (f.getName().endsWith(".aj") || f.getName().endsWith(".java"))) {
				String fileFound;
				try {
					fileFound = f.getCanonicalPath();
					String toRemove  = base.getCanonicalPath();
					if (!fileFound.startsWith(toRemove)) throw new RuntimeException("eh? "+fileFound+"   "+toRemove);
					collectionPoint.add(fileFound.substring(toRemove.length()+1));//+1 captures extra separator
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Fill in the working directory with the project base files,
	 * from the 'base' folder.
	 */
	private void initialiseProject(String p) {
		File projectSrc=new File(testdataSrcDir+File.separatorChar+p+File.separatorChar+"base");
		File destination=new File(getWorkingDir(),p);
		if (!destination.exists()) {destination.mkdir();}
		copy(projectSrc,destination);//,false);
	}

	/*
	 * Applies an overlay onto the project being tested - copying
	 * the contents of the specified overlay directory.
	 */
	private void alter(String projectName,String overlayDirectory) {
		File projectSrc =new File(testdataSrcDir+File.separatorChar+projectName+
				                  File.separatorChar+overlayDirectory);
		File destination=new File(getWorkingDir(),projectName);
		copy(projectSrc,destination);
	}
	
	/**
	 * Copy the contents of some directory to another location - the
	 * copy is recursive.
	 */
	private void copy(File from, File to) {
		String contents[] = from.list();
		if (contents==null) return;
		for (int i = 0; i < contents.length; i++) {
			String string = contents[i];
			File f = new File(from,string);
			File t = new File(to,string);
			
			if (f.isDirectory() && !f.getName().startsWith("inc")) {
				t.mkdir();
				copy(f,t);
			} else if (f.isFile()) {
				StringBuffer sb = new StringBuffer();
				//if (VERBOSE) System.err.println("Copying "+f+" to "+t);
				FileUtil.copyFile(f,t,sb);
				if (sb.length()!=0) { System.err.println(sb.toString());}
			} 
		}
	}
	

	private static void log(String msg) {
		if (VERBOSE) System.out.println(msg);
	}
}
