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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

	public static boolean VERBOSE = false;
	
	protected void setUp() throws Exception {
		super.setUp();
		AjdeInteractionTestbed.VERBOSE = VERBOSE;
		AjState.FORCE_INCREMENTAL_DURING_TESTING = true;
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		AjState.FORCE_INCREMENTAL_DURING_TESTING = false;
	}
	
	
	// Compile a single simple project
	public void testTheBasics() {
		initialiseProject("P1");
		build("P1"); // This first build will be batch
		build("P1");
		checkWasntFullBuild();
		checkCompileWeaveCount(0,0);
	}

	// source code doesnt matter, we are checking invalid path handling
	public void testInvalidAspectpath_pr121395() {
		initialiseProject("P1");
		File f = new File("foo.jar");
		Set s = new HashSet();
		s.add(f);
		configureAspectPath(s);
		build("P1"); // This first build will be batch
		checkForError("invalid aspectpath entry");
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
		build("P1");							// build 1, weave 1
		alter("P1","inc1"); // adds a class
		alter("P1","inc2"); // adds an aspect
		build("P1");                            // build 1,
		long timeTakenForFullBuildAndWeave = getTimeTakenForBuild();
		checkWasFullBuild();  // it *will* be a full build under the new 
		                      // "back-to-the-source strategy
		checkCompileWeaveCount(5,3); // we compile X and A (the delta) find out that
		                             // an aspect has changed, go back to the source
									 // and compile X,A,C, then weave them all.
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
		checkWasFullBuild();  // adding an aspect makes us go back to the source
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
	 * The C.java file modified in this test has an inner class - this means the inner class
	 * has a this$0 field and <init>(C) ctor to watch out for when checking for structural changes
	 *
	 */
	public void testStateManagement3() {		
		File binDirForInterproject1 = new File(getFile("interprojectdeps1","bin"));
		
		initialiseProject("interprojectdeps1");
		build("interprojectdeps1"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirForInterproject1);
		assertTrue("There should be state for project P1",ajs!=null);
		assertTrue("Should be no struc changes as its a full build: "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==0);
		
		
		alter("interprojectdeps1","inc1"); // adds a space to C.java
		build("interprojectdeps1");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("interprojectdeps1","bin")));
		assertTrue("There should be state for project interprojectdeps1",ajs!=null);
		checkWasntFullBuild();
		assertTrue("Shouldn't be any structural changes but there were "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==0);
	}
	
	/**
	 * The C.java file modified in this test has an inner class - which has two ctors - this checks
	 * how they are mangled with an instance of C.
	 *
	 */
	public void testStateManagement4() {		
		File binDirForInterproject2 = new File(getFile("interprojectdeps2","bin"));
		
		initialiseProject("interprojectdeps2");
		build("interprojectdeps2"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirForInterproject2);
		assertTrue("There should be state for project interprojectdeps2",ajs!=null);
		assertTrue("Should be no struc changes as its a full build: "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==0);
		
		
		alter("interprojectdeps2","inc1"); // minor change to C.java
		build("interprojectdeps2");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("interprojectdeps2","bin")));
		assertTrue("There should be state for project interprojectdeps1",ajs!=null);
		checkWasntFullBuild();
		assertTrue("Shouldn't be any structural changes but there were "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==0);
	}
	
	/**
	 * The C.java file modified in this test has an inner class - it has two ctors but
	 * also a reference to C.this in it - which will give rise to an accessor being
	 * created in C
	 *
	 */
	public void testStateManagement5() {		
		File binDirForInterproject3 = new File(getFile("interprojectdeps3","bin"));
		
		initialiseProject("interprojectdeps3");
		build("interprojectdeps3"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirForInterproject3);
		assertTrue("There should be state for project interprojectdeps3",ajs!=null);
		assertTrue("Should be no struc changes as its a full build: "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==0);
		
		
		alter("interprojectdeps3","inc1"); // minor change to C.java
		build("interprojectdeps3");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("interprojectdeps3","bin")));
		assertTrue("There should be state for project interprojectdeps1",ajs!=null);
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

	// parameterization of generic aspects
	public void testPr125405() {
		initialiseProject("PR125405");
		build("PR125405");
		checkCompileWeaveCount(1,1);
		alter("PR125405","inc1");
		build("PR125405");
		// "only abstract aspects can have type parameters"
		checkForError("only abstract aspects can have type parameters");
		alter("PR125405","inc2");
		build("PR125405");
		checkCompileWeaveCount(1,1);
		assertTrue("Should be no errors, but got "+MyTaskListManager.getErrorMessages(),MyTaskListManager.getErrorMessages().size()==0);		
	}
	
	public void testPr128618() {
		initialiseProject("PR128618_1");
		initialiseProject("PR128618_2");
		configureNewProjectDependency("PR128618_2","PR128618_1");
		assertTrue("there should be no warning messages before we start",
				MyTaskListManager.getWarningMessages().isEmpty());
		build("PR128618_1");
		build("PR128618_2");
		List warnings = MyTaskListManager.getWarningMessages();
		assertTrue("Should be one warning, but there are #"+warnings.size(),warnings.size()==1);
		IMessage msg = (IMessage)(MyTaskListManager.getWarningMessages().get(0));
		assertEquals("warning should be against the FFDC.aj resource","FFDC.aj",msg.getSourceLocation().getSourceFile().getName());
		alter("PR128618_2","inc1");
		build("PR128618_2");
		checkWasntFullBuild();
		IMessage msg2 = (IMessage)(MyTaskListManager.getWarningMessages().get(0));
		assertEquals("warning should be against the FFDC.aj resource","FFDC.aj",msg2.getSourceLocation().getSourceFile().getName());
		assertFalse("a new warning message should have been generated", msg.equals(msg2));
	}
	
	public void testPr92837() {
		initialiseProject("PR92837");
		build("PR92837");
		alter("PR92837","inc1");
		build("PR92837");
	}
	
	public void testPr119570() {
		initialiseProject("PR119570");
		build("PR119570");
		assertTrue("Should be no errors, but got "+MyTaskListManager.getErrorMessages(),MyTaskListManager.getErrorMessages().size()==0);		
	}
	
	public void testPr119570_2() {
		initialiseProject("PR119570_2");
		build("PR119570_2");
		List l = MyTaskListManager.getWarningMessages();
		assertTrue("Should be no warnings, but got "+l,l.size()==0);
	}
	
	// If you fiddle with the compiler options - you must manually reset the options at the end of the test
	public void testPr117209() {
		try {
			initialiseProject("pr117209");
			configureNonStandardCompileOptions("-proceedOnError");
			build("pr117209");
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
		checkWasFullBuild();
		alter("pr114875","inc2");
		build("pr114875");
		checkWasFullBuild();  // back to the source for an aspect change
	}
	
	public void testPr117882() {
//		AjdeInteractionTestbed.VERBOSE=true;
//		AjdeInteractionTestbed.configureBuildStructureModel(true);
		initialiseProject("PR117882");
		build("PR117882");
		checkWasFullBuild();
		alter("PR117882","inc1");
		build("PR117882");
		checkWasFullBuild();  // back to the source for an aspect
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
		checkWasFullBuild();  // back to the source...
		//checkCompileWeaveCount(1,4);
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
		checkWasFullBuild();  // back to the source
	}
	

//	public void testPr124399() {
//		AjdeInteractionTestbed.VERBOSE=true;
//		configureBuildStructureModel(true);
//		initialiseProject("PR124399");
//		build("PR124399");
//		checkWasFullBuild();
//		alter("PR124399","inc1");
//		build("PR124399");
//		checkWasntFullBuild();
//	}
	
	public void testPr121384() {
//		AjdeInteractionTestbed.VERBOSE=true;
//		AsmManager.setReporting("c:/foo.txt",true,true,true,false);
		MyBuildOptionsAdapter.setNonStandardOptions("-showWeaveInfo");
		configureBuildStructureModel(true);
		initialiseProject("pr121384");
		build("pr121384"); 
		checkWasFullBuild();
		alter("pr121384","inc1");
		build("pr121384");
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

	// Stage 1: Compile the 4 files, pack.A2 extends pack.A1 (aspects) where
	//          A2 uses a protected field in A1 and pack.C2 extends pack.C1 (classes)
	//          where C2 uses a protected field in C1
	// Stage 2: make the field private in class C1 ==> compile errors in C2
	// Stage 3: make the field private in aspect A1 whilst there's the compile
	//          error. 
	// There shouldn't be a BCExcpetion saying can't find delegate for pack.C2
	public void testPr119882() {
		initialiseProject("PR119882");
		build("PR119882");
		assertFalse("build should have compiled ok",MyTaskListManager.hasErrorMessages());
		alter("PR119882","inc1");
		build("PR119882");
		//fullBuild("PR119882");
		List errors = MyTaskListManager.getErrorMessages();
		assertTrue("Should be at least one error, but got none",errors.size()==1);
		assertEquals("error message should be 'i cannot be resolved' ",
				"i cannot be resolved",
				((IMessage)errors.get(0))
					.getMessage());
		alter("PR119882","inc2");
		build("PR119882");
		assertTrue("There should be no exceptions handled:\n"+MyErrorHandler.getErrorMessages(),
				MyErrorHandler.getErrorMessages().isEmpty());	
		assertEquals("error message should be 'i cannot be resolved' ",
				"i cannot be resolved",
				((IMessage)errors.get(0))
					.getMessage());

	}
	
	public void testPr112736() {
	//	AjdeInteractionTestbed.VERBOSE = true;
		initialiseProject("PR112736");
		build("PR112736");
		checkWasFullBuild();
		String fileC2 = getWorkingDir().getAbsolutePath() + File.separatorChar + "PR112736" + File.separatorChar + "src" + File.separatorChar + "pack" + File.separatorChar + "A.java";
		(new File(fileC2)).delete();
		alter("PR112736","inc1");
		build("PR112736");
		checkWasFullBuild();
	}
	
	/**
	 * We have problems with multiple rewrites of a pointcut across incremental builds.
	 */
	public void testPr113257() {
		initialiseProject("PR113257");
		build("PR113257");
		alter("PR113257","inc1");
		build("PR113257");
		checkWasFullBuild();  // back to the source
		alter("PR113257","inc1");
		build("PR113257");
	}

	public void testPr123612() {
		initialiseProject("PR123612");
		build("PR123612");
		alter("PR123612","inc1");
		build("PR123612");
		checkWasFullBuild(); // back to the source
	}
	

	public void testPr128655() {
		configureNonStandardCompileOptions("-showWeaveInfo");
		initialiseProject("pr128655");
		build("pr128655");
		List firstBuildMessages = MyTaskListManager.getWeavingMessages();
		assertTrue("Should be at least one message about the dec @type, but there were none",firstBuildMessages.size()>0);
		alter("pr128655","inc1");
		build("pr128655");
		checkWasntFullBuild(); // back to the source
		List secondBuildMessages = MyTaskListManager.getWeavingMessages();
		// check they are the same
		for (int i = 0; i < firstBuildMessages.size(); i++) {
			IMessage m1 = (IMessage)firstBuildMessages.get(i);
			IMessage m2 = (IMessage)secondBuildMessages.get(i);
			if (!m1.toString().equals(m2.toString())) {
				System.err.println("Message during first build was: "+m1);
				System.err.println("Message during second build was: "+m1);
				fail("The two messages should be the same, but are not: \n"+m1+"!="+m2);
			}
		}
	}
	
	// Similar to above, but now the annotation is in the default package
	public void testPr128655_2() {
		configureNonStandardCompileOptions("-showWeaveInfo");
		initialiseProject("pr128655_2");
		build("pr128655_2");
		List firstBuildMessages = MyTaskListManager.getWeavingMessages();
		assertTrue("Should be at least one message about the dec @type, but there were none",firstBuildMessages.size()>0);
		alter("pr128655_2","inc1");
		build("pr128655_2");
		checkWasntFullBuild(); // back to the source
		List secondBuildMessages = MyTaskListManager.getWeavingMessages();
		// check they are the same
		for (int i = 0; i < firstBuildMessages.size(); i++) {
			IMessage m1 = (IMessage)firstBuildMessages.get(i);
			IMessage m2 = (IMessage)secondBuildMessages.get(i);
			if (!m1.toString().equals(m2.toString())) {
				System.err.println("Message during first build was: "+m1);
				System.err.println("Message during second build was: "+m1);
				fail("The two messages should be the same, but are not: \n"+m1+"!="+m2);
			}
		}
	}
	
	// test for comment #31 - NPE
	public void testPr129163() {
		configureBuildStructureModel(true);
		initialiseProject("PR129613");
		build("PR129613");
		alter("PR129613","inc1");
		build("PR129613");
		assertTrue("There should be no exceptions handled:\n"+MyErrorHandler.getErrorMessages(),
				MyErrorHandler.getErrorMessages().isEmpty());
		assertEquals("warning message should be 'no match for this type name: File [Xlint:invalidAbsoluteTypeName]' ",
				"no match for this type name: File [Xlint:invalidAbsoluteTypeName]",
				((IMessage)MyTaskListManager.getWarningMessages().get(0))
					.getMessage());
		configureBuildStructureModel(false);
	}
	
	// test for comment #0 - adding a comment to a class file shouldn't
	// cause us to go back to source and recompile everything. To force this
	// to behave like AJDT we need to include the aspect in 'inc1' so that
	// when AjState looks at its timestamp it thinks the aspect has been modified. 
	// The logic within CrosscuttingMembers should then work out correctly 
	// that there haven't really been any changes within the aspect and so 
	// we shouldn't go back to source.
	public void testPr129163_2() {
		// want to behave like AJDT
		configureBuildStructureModel(true);
		initialiseProject("pr129163_2");
		build("pr129163_2");
		checkWasFullBuild();
		alter("pr129163_2","inc1");
		build("pr129163_2");
		checkWasntFullBuild(); // shouldn't be a full build because the 
		                       // aspect hasn't changed
		configureBuildStructureModel(false);
	}
	
	// test for comment #6 - simulates AJDT core builder test testBug99133a -
	// changing the contents of a method within a class shouldn't force a 
	// full build of a dependant project. To force this to behave like AJDT
	// 'inc1' of the dependant project should just be a copy of 'base' so that
	// AjState thinks somethings changed within the dependant project and 
	// we do a build. Similarly, 'inc1' of the project depended on should 
	// include the aspect even though nothing's changed within it. This causes
	// AjState to think that the aspect has changed. Together its then up to 
	// logic within CrosscuttingMembers and various equals methods to decide
	// correctly that we don't have to go back to source.
	public void testPr129163_3() {
		configureBuildStructureModel(true);
		initialiseProject("PR129163_4");
		build("PR129163_4");
		checkWasFullBuild(); // should be a full build because initializing project
		initialiseProject("PR129163_3");
		configureNewProjectDependency("PR129163_3","PR129163_4");
		build("PR129163_3");
		checkWasFullBuild(); // should be a full build because initializing project
		alter("PR129163_4","inc1");
		build("PR129163_4");
		checkWasntFullBuild(); // should be an incremental build because although
		                       // "inc1" includes the aspect A1.aj, it actually hasn't
							   // changed so we shouldn't go back to source
		alter("PR129163_3","inc1");
		build("PR129163_3");
		checkWasntFullBuild(); // should be an incremental build because nothing has
			                   // changed within the class and no aspects have changed
		                       // within the running of the test
		configureBuildStructureModel(false);
	}
	
	public void testPr131505() {
		configureNonStandardCompileOptions("-outxml");
		initialiseProject("PR131505");
		build("PR131505");
		checkWasFullBuild();
		// aop.xml file shouldn't contain any aspects
		checkXMLAspectCount("PR131505","",0);
		// add a new aspect A which should be included in the aop.xml file
		alter("PR131505","inc1");
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505","",1);
		checkXMLAspectCount("PR131505","A",1);
		// make changes to the class file which shouldn't affect the contents
		// of the aop.xml file
		alter("PR131505","inc2");
		build("PR131505");
		checkWasntFullBuild();
		checkXMLAspectCount("PR131505","",1);
		checkXMLAspectCount("PR131505","A",1);		
		// add another new aspect A1 which should also be included in the aop.xml file
		// ...there should be no duplicate entries in the file
		alter("PR131505","inc3");
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505","",2);
		checkXMLAspectCount("PR131505","A1",1);
		checkXMLAspectCount("PR131505","A",1);
		// delete aspect A1 which meanss that aop.xml file should only contain A
		File a1 = new File(getWorkingDir().getAbsolutePath() 
				+ File.separatorChar + "PR131505" + File.separatorChar + "A1.aj");
		a1.delete();
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505","",1);
		checkXMLAspectCount("PR131505","A1",0);
		checkXMLAspectCount("PR131505","A",1);	
		// add another aspect called A which is in a different package, both A
		// and pkg.A should be included in the aop.xml file
		alter("PR131505","inc4");
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505","",2);
		checkXMLAspectCount("PR131505","A",1);
		checkXMLAspectCount("PR131505","pkg.A",1);
	}
	
	public void testPr133532() {
		initialiseProject("PR133532");
		build("PR133532");
		alter("PR133532","inc1");
		build("PR133532");
		alter("PR133532","inc2");
		build("PR133532");
		assertTrue("There should be no errors reported:\n"+MyTaskListManager.getErrorMessages(),
				MyTaskListManager.getErrorMessages().isEmpty());	
	}
	
	public void testPr133532_2() {
		initialiseProject("pr133532_2");
		build("pr133532_2");
		alter("pr133532_2","inc2");
		build("pr133532_2");
		assertTrue("There should be no errors reported:\n"+MyTaskListManager.getErrorMessages(),
				MyTaskListManager.getErrorMessages().isEmpty());	
		String decisions = AjdeInteractionTestbed.MyStateListener.getDecisions();
		String expect="Need to recompile 'A.aj'";
		assertTrue("Couldn't find build decision: '"+expect+"' in the list of decisions made:\n"+decisions,
				  decisions.indexOf(expect)!=-1);
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
	
	public void checkForError(String anError) {
		List messages = MyTaskListManager.getErrorMessages();
		for (Iterator iter = messages.iterator(); iter.hasNext();) {
			IMessage element = (IMessage) iter.next();
			if (element.getMessage().indexOf(anError)!=-1) return;
		}
		fail("Didn't find the error message:\n'"+anError+"'.\nErrors that occurred:\n"+MyTaskListManager.getErrorMessages());
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
	protected void initialiseProject(String p) {
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
	
	/**
	 * Count the number of times a specified aspectName appears in the default
	 * aop.xml file and compare with the expected number of occurrences. If just 
	 * want to count the number of aspects mentioned within the file then 
	 * pass "" for the aspectName, otherwise, specify the name of the 
	 * aspect interested in.
	 */
	private void checkXMLAspectCount(String projectName, String aspectName, int expectedOccurrences) {
		int aspectCount = 0;
		File aopXML = new File(getWorkingDir().getAbsolutePath() 
				+ File.separatorChar + projectName + File.separatorChar 
				+ "bin" + File.separatorChar + "META-INF" + File.separatorChar + "aop.xml");

		if (!aopXML.exists()) {
			fail("Expected file " + aopXML.getAbsolutePath() + " to exist but it doesn't");
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(aopXML));
			String line = reader.readLine();
			while (line != null) {
				if (aspectName.equals("") && line.indexOf("aspect name=\"") != -1) {
					aspectCount++;
				} else if (line.indexOf("aspect name=\""+aspectName+"\"") != -1) {
					aspectCount++;
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		if (aspectCount != expectedOccurrences) {
			fail("Expected aspect " + aspectName + " to appear " + expectedOccurrences + " times" +
					" in the aop.xml file but found " + aspectCount + " occurrences");
		}
	}
	
	
}
