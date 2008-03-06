/********************************************************************
 * Copyright (c) 2005 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement          initial implementation
 *     Helen Hawkins         Converted to new interface (bug 148190)
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IElementHandleProvider;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.JDTLikeHandleProvider;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.Ajc;
import org.aspectj.util.FileUtil;

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
public class MultiProjectIncrementalTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	/*
	A.aj
	package pack;
	public aspect A {
	        pointcut p() : call(* C.method
	        before() : p() { // line 7
	        }
	}

	C.java
	package pack;
	public class C {
	        public void method1() {
	          method2(); // line 6
	        }
	        public void method2() {   }
	        public void method3() { 
	          method2();  // line 13
	        }

	}*/
	public void testDontLoseAdviceMarkers_pr134471() {
		try {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=false;
			initialiseProject("P4");
			build("P4");
			Ajc.dumpAJDEStructureModel("after full build where advice is applying");
			// should be 4 relationship entries
	
			// In inc1 the first advised line is 'commented out'
			alter("P4","inc1");
			build("P4");
			checkWasntFullBuild();
			Ajc.dumpAJDEStructureModel("after inc build where first advised line is gone");
			// should now be 2 relationship entries
			
			// This will be the line 6 entry in C.java
			IProgramElement codeElement = findCode(checkForNode("pack","C",true));
			
			// This will be the line 7 entry in A.java
			IProgramElement advice = findAdvice(checkForNode("pack","A",true));
			
			IRelationshipMap asmRelMap = AsmManager.getDefault().getRelationshipMap();
			assertEquals("There should be two relationships in the relationship map",
					2,asmRelMap.getEntries().size());
	
			for (Iterator iter = asmRelMap.getEntries().iterator(); iter.hasNext();) {
				String sourceOfRelationship = (String) iter.next();
				IProgramElement ipe = AsmManager.getDefault().getHierarchy()
										.findElementForHandle(sourceOfRelationship);
				assertNotNull("expected to find IProgramElement with handle " 
						+ sourceOfRelationship + " but didn't",ipe);
				if (ipe.getKind().equals(IProgramElement.Kind.ADVICE)) {
					assertEquals("expected source of relationship to be " +
							advice.toString() + " but found " +
							ipe.toString(),advice,ipe);
				} else if (ipe.getKind().equals(IProgramElement.Kind.CODE)) {
					assertEquals("expected source of relationship to be " +
							codeElement.toString() + " but found " +
							ipe.toString(),codeElement,ipe);
				} else {
					fail("found unexpected relationship source " + ipe 
							+ " with kind " + ipe.getKind()+" when looking up handle: "+sourceOfRelationship);
				}
				List relationships = asmRelMap.get(ipe);
				assertNotNull("expected " + ipe.getName() +" to have some " +
						"relationships",relationships);
				for (Iterator iterator = relationships.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					List targets = rel.getTargets();
					for (Iterator iterator2 = targets.iterator(); iterator2.hasNext();) {
						String t = (String) iterator2.next();
						IProgramElement link = AsmManager.getDefault().getHierarchy().findElementForHandle(t);
						if (ipe.getKind().equals(IProgramElement.Kind.ADVICE)) {
							assertEquals("expected target of relationship to be " +
									codeElement.toString() + " but found " +
									link.toString(),codeElement,link);
						} else if (ipe.getKind().equals(IProgramElement.Kind.CODE)) {
							assertEquals("expected target of relationship to be " +
									advice.toString() + " but found " +
									link.toString(),advice,link);	
						} else {
							fail("found unexpected relationship source " + ipe.getName() 
									+ " with kind " + ipe.getKind());
						}
					}				
				}
			}
			
		} finally {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=true;
			//configureBuildStructureModel(false);
		}
	}
	
	public void testIncrementalItdsWithMultipleAspects_pr173729() {
		initialiseProject("PR173729");
		build("PR173729");
		checkWasFullBuild();
		alter("PR173729","inc1");
		build("PR173729");
		checkWasntFullBuild();
	}
	
	// Compile a single simple project
	public void testTheBasics() {
		initialiseProject("P1");
		build("P1"); // This first build will be batch
		build("P1");
		checkWasntFullBuild();
		checkCompileWeaveCount("P1",0,0);
	}

	// source code doesnt matter, we are checking invalid path handling
	public void testInvalidAspectpath_pr121395() {
		initialiseProject("P1");
		File f = new File("foo.jar");
		Set s = new HashSet();
		s.add(f);
		configureAspectPath("P1",s);
		build("P1"); // This first build will be batch
		checkForError("P1","invalid aspectpath entry");
	}
	
	
	/**
	 * Build a project containing a resource - then mark the resource readOnly(), then
	 * do an inc-compile, it will report an error about write access to the resource
	 * in the output folder being denied
	 */
	/*public void testProblemCopyingResources_pr138171() {
		initialiseProject("PR138171");
		
		File f=getProjectRelativePath("PR138171","res.txt");
		Map m = new HashMap();
		m.put("res.txt",f);
		AjdeInteractionTestbed.MyProjectPropertiesAdapter.getInstance().setSourcePathResources(m);
		build("PR138171");
		File f2 = getProjectOutputRelativePath("PR138171","res.txt");
		boolean successful = f2.setReadOnly();
		
		alter("PR138171","inc1");
		AjdeInteractionTestbed.MyProjectPropertiesAdapter.getInstance().setSourcePathResources(m);
		build("PR138171");
		List msgs = MyTaskListManager.getErrorMessages();
		assertTrue("there should be one message but there are "+(msgs==null?0:msgs.size())+":\n"+msgs,msgs!=null && msgs.size()==1);
		IMessage msg = (IMessage)msgs.get(0);
		String exp = "unable to copy resource to output folder: 'res.txt'";
		assertTrue("Expected message to include this text ["+exp+"] but it does not: "+msg,msg.toString().indexOf(exp)!=-1);
	}*/
	
	
	// Make simple changes to a project, adding a class
	public void testSimpleChanges() {
		initialiseProject("P1");
		build("P1"); // This first build will be batch
		alter("P1","inc1"); // adds a single class
		build("P1");
		checkCompileWeaveCount("P1",1,-1);
		build("P1"); 
		checkCompileWeaveCount("P1",0,-1);
	}
	
	
	// Make simple changes to a project, adding a class and an aspect
	public void testAddingAnAspect() {
		initialiseProject("P1");
		build("P1");							// build 1, weave 1
		alter("P1","inc1"); // adds a class
		alter("P1","inc2"); // adds an aspect
		build("P1");                            // build 1,
		long timeTakenForFullBuildAndWeave = getTimeTakenForBuild("P1");
		checkWasFullBuild();  // it *will* be a full build under the new 
		                      // "back-to-the-source strategy
		checkCompileWeaveCount("P1",5,3); // we compile X and A (the delta) find out that
		                             // an aspect has changed, go back to the source
									 // and compile X,A,C, then weave them all.
		build("P1");
		long timeTakenForSimpleIncBuild = getTimeTakenForBuild("P1");
		// I don't think this test will have timing issues as the times should be *RADICALLY* different
		// On my config, first build time is 2093ms  and the second is 30ms
		assertTrue("Should not take longer for the trivial incremental build!  first="+timeTakenForFullBuildAndWeave+
				   "ms  second="+timeTakenForSimpleIncBuild+"ms",
				   timeTakenForSimpleIncBuild<timeTakenForFullBuildAndWeave);
	}
	
	
	public void testBuildingTwoProjectsInTurns() {
		initialiseProject("P1");
		initialiseProject("P2");
		build("P1");
		build("P2");
		build("P1");
		checkWasntFullBuild();
		build("P2");
		checkWasntFullBuild();
	}
	
//	public void testDeclareAtType_pr149293() {
//		configureBuildStructureModel(true);
//		initialiseProject("PR149293_1");
//		build("PR149293_1");
//		checkCompileWeaveCount(4,5);
//		assertNoErrors();
//		alter("PR149293_1","inc1");
//		build("PR149293_1");
//		assertNoErrors();
//	}
	
/*
	public void testRefactoring_pr148285() {
		configureBuildStructureModel(true);
		initialiseProject("PR148285");
		build("PR148285");
		System.err.println("xxx");
		alter("PR148285","inc1");
		build("PR148285");
	}
*/
	
	
	/**
	 * In order for this next test to run, I had to move the weaver/world pair we keep in the
	 * AjBuildManager instance down into the state object - this makes perfect sense - otherwise
	 * when reusing the state for another project we'd not be switching to the right weaver/world
	 * for that project.
	 */
	public void testBuildingTwoProjectsMakingSmallChanges() {
		
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

	public void testPr134371() {
		initialiseProject("PR134371");
		build("PR134371");
		alter("PR134371","inc1");
		build("PR134371");
		assertTrue("There should be no exceptions handled:\n"
				+getErrorMessages("PR134371"),getErrorMessages("PR134371").isEmpty());		

	}
	
	/** 
	 * Setup up two simple projects and build them in turn - check the
	 * structure model is right after each build 
	 */
	public void testBuildingTwoProjectsAndVerifyingModel() {
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
		checkCompileWeaveCount("PR125405",1,1);
		alter("PR125405","inc1");
		build("PR125405");
		// "only abstract aspects can have type parameters"
		checkForError("PR125405","only abstract aspects can have type parameters");
		alter("PR125405","inc2");
		build("PR125405");
		checkCompileWeaveCount("PR125405",1,1);
		assertTrue("Should be no errors, but got "+getErrorMessages("PR125405"),
				getErrorMessages("PR125405").size()==0);		
	}
	
	public void testPr128618() {
		initialiseProject("PR128618_1");
		initialiseProject("PR128618_2");
		configureNewProjectDependency("PR128618_2","PR128618_1");
		assertTrue("there should be no warning messages before we start",
				getWarningMessages("PR128618_1").isEmpty());
		assertTrue("there should be no warning messages before we start",
				getWarningMessages("PR128618_2").isEmpty());
		
		build("PR128618_1");
		build("PR128618_2");
		List l = getWarningMessages("PR128618_2");
		
		// there should be one warning against "PR128618_2"
		List warnings = getWarningMessages("PR128618_2");
		assertTrue("Should be one warning, but there are #"+warnings.size(),warnings.size()==1);
		IMessage msg = (IMessage)(getWarningMessages("PR128618_2").get(0));
		assertEquals("warning should be against the FFDC.aj resource","FFDC.aj",msg.getSourceLocation().getSourceFile().getName());

		
		alter("PR128618_2","inc1");
		build("PR128618_2");
		
		checkWasntFullBuild();
		IMessage msg2 = (IMessage)(getWarningMessages("PR128618_2").get(0));
		assertEquals("warning should be against the FFDC.aj resource","FFDC.aj",msg2.getSourceLocation().getSourceFile().getName());
		assertFalse("a new warning message should have been generated", msg.equals(msg2));
	}
	
	public void testPr92837() {
		initialiseProject("PR92837");
		build("PR92837");
		alter("PR92837","inc1");
		build("PR92837");
	}

// See open generic itd bug mentioning 119570
//	public void testPr119570() {
//		initialiseProject("PR119570");
//		build("PR119570");
//		assertNoErrors("PR119570");
//	}
	
	public void testPr119570_2() {
		initialiseProject("PR119570_2");
		build("PR119570_2");
		List l = getWarningMessages("PR119570_2");
		assertTrue("Should be no warnings, but got "+l,l.size()==0);
	}
	
	// If you fiddle with the compiler options - you must manually reset the options at the end of the test
	public void testPr117209() {
		try {
			initialiseProject("pr117209");
			configureNonStandardCompileOptions("pr117209","-proceedOnError");
			build("pr117209");
			checkCompileWeaveCount("pr117209",6,6);
		} finally {
			//MyBuildOptionsAdapter.reset();
		}
	}
	
	public void testPr114875() {
		// temporary problem with this on linux, think it is a filesystem lastmodtime issue
		if (System.getProperty("os.name","").toLowerCase().equals("linux")) return;
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
	

	public void testPr220255_InfiniteBuildHasMember() {
		AjdeInteractionTestbed.VERBOSE=true;
		initialiseProject("pr220255");
		configureNonStandardCompileOptions("pr220255","-XhasMember");
		build("pr220255");
		checkWasFullBuild();
		alter("pr220255","inc1");
		build("pr220255");
		checkWasntFullBuild(); 
	}

	public void testPr157054() {
		initialiseProject("PR157054");
		configureNonStandardCompileOptions("PR157054","-showWeaveInfo");
		configureShowWeaveInfoMessages("PR157054",true);
		build("PR157054");
		checkWasFullBuild();
		List weaveMessages = getWeavingMessages("PR157054");
		assertTrue("Should be two weaving messages but there are "+weaveMessages.size(),weaveMessages.size()==2);
		alter("PR157054","inc1");
		build("PR157054");
		weaveMessages = getWeavingMessages("PR157054");
		assertTrue("Should be three weaving messages but there are "+weaveMessages.size(),weaveMessages.size()==3);
		checkWasntFullBuild();
		fullBuild("PR157054");
		weaveMessages = getWeavingMessages("PR157054");
		assertTrue("Should be three weaving messages but there are "+weaveMessages.size(),weaveMessages.size()==3);
	}
	
	

	/**
	 * Checks we aren't leaking mungers across compiles (accumulating multiple instances of the same one that
	 * all do the same thing).  On the first compile the munger is added late on - so at the time we set
	 * the count it is still zero.  On the subsequent compiles we know about this extra one.
	 */
	public void testPr141956_IncrementallyCompilingAtAj() {
		initialiseProject("PR141956");
		build("PR141956");
		assertTrue("Should be zero but reports "+EclipseFactory.debug_mungerCount,EclipseFactory.debug_mungerCount==0);
		alter("PR141956","inc1");
		build("PR141956");
		assertTrue("Should be two but reports "+EclipseFactory.debug_mungerCount,EclipseFactory.debug_mungerCount==2);
		alter("PR141956","inc1");
		build("PR141956");
		assertTrue("Should be two but reports "+EclipseFactory.debug_mungerCount,EclipseFactory.debug_mungerCount==2);
		alter("PR141956","inc1");
		build("PR141956");
		assertTrue("Should be two but reports "+EclipseFactory.debug_mungerCount,EclipseFactory.debug_mungerCount==2);
		alter("PR141956","inc1");
		build("PR141956");
		assertTrue("Should be two but reports "+EclipseFactory.debug_mungerCount,EclipseFactory.debug_mungerCount==2);
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
		initialiseProject("pr121384");
		configureNonStandardCompileOptions("pr121384","-showWeaveInfo");
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
		assertTrue("build should have compiled ok",
				getErrorMessages("PR113531").isEmpty());
		alter("PR113531","inc1");
		build("PR113531");
		assertEquals("error message should be 'foo cannot be resolved' ",
				"foo cannot be resolved",
				((IMessage)getErrorMessages("PR113531").get(0))
					.getMessage());
		alter("PR113531","inc2");
		build("PR113531");
		assertTrue("There should be no exceptions handled:\n"
				+getCompilerErrorMessages("PR113531"),
				getCompilerErrorMessages("PR113531").isEmpty());		
		assertEquals("error message should be 'foo cannot be resolved' ",
				"foo cannot be resolved",
				((IMessage)getErrorMessages("PR113531").get(0))
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
		assertTrue("build should have compiled ok",getErrorMessages("PR119882").isEmpty());
		alter("PR119882","inc1");
		build("PR119882");
		//fullBuild("PR119882");
		List errors = getErrorMessages("PR119882");
		assertTrue("Should be at least one error, but got none",errors.size()==1);
		assertEquals("error message should be 'i cannot be resolved' ",
				"i cannot be resolved",
				((IMessage)errors.get(0))
					.getMessage());
		alter("PR119882","inc2");
		build("PR119882");
		assertTrue("There should be no exceptions handled:\n"
				+getCompilerErrorMessages("PR119882"),
				getCompilerErrorMessages("PR119882").isEmpty());	
		assertEquals("error message should be 'i cannot be resolved' ",
				"i cannot be resolved",
				((IMessage)errors.get(0))
					.getMessage());

	}
	
	public void testPr112736() {
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
	
    //Bugzilla Bug 152257 - Incremental compiler doesn't handle exception declaration correctly
	public void testPr152257() {
		initialiseProject("PR152257");
		configureNonStandardCompileOptions("PR152257","-XnoInline");
		build("PR152257");
		List errors = getErrorMessages("PR152257");
		assertTrue("Should be no warnings, but there are #"+errors.size(),errors.size()==0);
		checkWasFullBuild();
		alter("PR152257","inc1");
		build("PR152257");
		errors = getErrorMessages("PR152257");
		assertTrue("Should be no warnings, but there are #"+errors.size(),errors.size()==0);
		checkWasntFullBuild();
	}


	public void testPr128655() {
		initialiseProject("pr128655");
		configureNonStandardCompileOptions("pr128655","-showWeaveInfo");
		configureShowWeaveInfoMessages("pr128655",true);
		build("pr128655");
		List firstBuildMessages = getWeavingMessages("pr128655");
		assertTrue("Should be at least one message about the dec @type, but there were none",firstBuildMessages.size()>0);
		alter("pr128655","inc1");
		build("pr128655");
		checkWasntFullBuild(); // back to the source
		List secondBuildMessages = getWeavingMessages("pr128655");
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
		initialiseProject("pr128655_2");
		configureNonStandardCompileOptions("pr128655_2","-showWeaveInfo");
		configureShowWeaveInfoMessages("pr128655_2", true);
		build("pr128655_2");
		List firstBuildMessages = getWeavingMessages("pr128655_2");
		assertTrue("Should be at least one message about the dec @type, but there were none",firstBuildMessages.size()>0);
		alter("pr128655_2","inc1");
		build("pr128655_2");
		checkWasntFullBuild(); // back to the source
		List secondBuildMessages = getWeavingMessages("pr128655_2");
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
		initialiseProject("PR129613");
		build("PR129613");
		alter("PR129613","inc1");
		build("PR129613");
		assertTrue("There should be no exceptions handled:\n"+getCompilerErrorMessages("PR129613"),
				getCompilerErrorMessages("PR129613").isEmpty());
		assertEquals("warning message should be 'no match for this type name: File [Xlint:invalidAbsoluteTypeName]' ",
				"no match for this type name: File [Xlint:invalidAbsoluteTypeName]",
				((IMessage)getWarningMessages("PR129613").get(0))
					.getMessage());
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
		initialiseProject("pr129163_2");
		build("pr129163_2");
		checkWasFullBuild();
		alter("pr129163_2","inc1");
		build("pr129163_2");
		checkWasntFullBuild(); // shouldn't be a full build because the 
		                       // aspect hasn't changed
	}
	
	// Case001: renaming a private field in a type
/*	public void testPrReducingDependentBuilds_001_221427() {
		AjdeInteractionTestbed.VERBOSE=true;
		IncrementalStateManager.debugIncrementalStates=true;
		initialiseProject("P221427_1");
		initialiseProject("P221427_2");
		configureNewProjectDependency("P221427_2","P221427_1");
		
		build("P221427_1");
		build("P221427_2"); 
		alter("P221427_1","inc1"); // rename private class in super project
		MyStateListener.reset();
		build("P221427_1");
		build("P221427_2");

		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("P221427_1","bin")));
		assertTrue("There should be state for project P221427_1",ajs!=null);
		//System.out.println(MyStateListener.getInstance().getDecisions());
		checkWasntFullBuild();
		assertTrue("Should be one structural change but there were "+
				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==1);
		
	}
	
	// Case002: changing a class to final that is extended in a dependent project
	public void testPrReducingDependentBuilds_002_221427() {
		AjdeInteractionTestbed.VERBOSE=true;
		IncrementalStateManager.debugIncrementalStates=true;
		initialiseProject("P221427_3");
		initialiseProject("P221427_4");
		configureNewProjectDependency("P221427_4","P221427_3");
		
		build("P221427_3");
		build("P221427_4"); // build OK, type in super project is non-final
		alter("P221427_3","inc1"); // change class declaration in super-project to final
		MyStateListener.reset();
		build("P221427_3");
		build("P221427_4"); // build FAIL, type in super project is now final

		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("P221427_3","bin")));
		assertTrue("There should be state for project P221427_3",ajs!=null);
		System.out.println(MyStateListener.getInstance().getDecisions());

		List errors = getErrorMessages("P221427_4");
		if (errors.size()!=1) {
			if (errors.size()==0) fail("Expected error about not being able to extend final class");
			for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
				System.out.println(object);
			}
			fail("Expected 1 error but got "+errors.size());
		}
//		assertTrue("Shouldn't be one structural change but there were "+
//				ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
//				ajs.getNumberOfStructuralChangesSinceLastFullBuild()==1);
		
	}*/
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
	}
	

	public void testPr133117() {
//		System.gc();
//		System.exit();
		initialiseProject("PR133117");
		configureNonStandardCompileOptions("PR133117","-Xlint:warning");
		build("PR133117");
		assertTrue("There should only be one xlint warning message reported:\n"
				+getWarningMessages("PR133117"),
				getWarningMessages("PR133117").size()==1);	
		alter("PR133117","inc1");
		build("PR133117");
		List warnings = getWarningMessages("PR133117");
		List noGuardWarnings = new ArrayList();
		for (Iterator iter = warnings.iterator(); iter.hasNext();) {
			IMessage element = (IMessage) iter.next();
			if (element.getMessage().indexOf("Xlint:noGuardForLazyTjp") != -1) {
				noGuardWarnings.add(element);
			}
		}
		assertTrue("There should only be two Xlint:noGuardForLazyTjp warning message reported:\n"
				+noGuardWarnings,noGuardWarnings.size() == 2);
	}
	
	public void testPr131505() {
		initialiseProject("PR131505");
		configureNonStandardCompileOptions("PR131505","-outxml");
		build("PR131505");
		checkWasFullBuild();
		String outputDir = getWorkingDir().getAbsolutePath() + File.separatorChar 
			+ "PR131505" + File.separatorChar + "bin";
		// aop.xml file shouldn't contain any aspects
		checkXMLAspectCount("PR131505","",0, outputDir);
		// add a new aspect A which should be included in the aop.xml file
		alter("PR131505","inc1");
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505","",1, outputDir);
		checkXMLAspectCount("PR131505","A",1, outputDir);
		// make changes to the class file which shouldn't affect the contents
		// of the aop.xml file
		alter("PR131505","inc2");
		build("PR131505");
		checkWasntFullBuild();
		checkXMLAspectCount("PR131505","",1, outputDir);
		checkXMLAspectCount("PR131505","A",1, outputDir);		
		// add another new aspect A1 which should also be included in the aop.xml file
		// ...there should be no duplicate entries in the file
		alter("PR131505","inc3");
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505","",2, outputDir);
		checkXMLAspectCount("PR131505","A1",1, outputDir);
		checkXMLAspectCount("PR131505","A",1, outputDir);
		// delete aspect A1 which meanss that aop.xml file should only contain A
		File a1 = new File(getWorkingDir().getAbsolutePath() 
				+ File.separatorChar + "PR131505" + File.separatorChar + "A1.aj");
		a1.delete();
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505","",1, outputDir);
		checkXMLAspectCount("PR131505","A1",0, outputDir);
		checkXMLAspectCount("PR131505","A",1, outputDir);	
		// add another aspect called A which is in a different package, both A
		// and pkg.A should be included in the aop.xml file
		alter("PR131505","inc4");
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505","",2, outputDir);
		checkXMLAspectCount("PR131505","A",1, outputDir);
		checkXMLAspectCount("PR131505","pkg.A",1, outputDir);
	}

	public void testPr136585() {
		initialiseProject("PR136585");
		build("PR136585");
		alter("PR136585","inc1");
		build("PR136585");
		assertTrue("There should be no errors reported:\n"+getErrorMessages("PR136585"),
				getErrorMessages("PR136585").isEmpty());	
	}
	
	public void testPr133532() {
		initialiseProject("PR133532");
		build("PR133532");
		alter("PR133532","inc1");
		build("PR133532");
		alter("PR133532","inc2");
		build("PR133532");
		assertTrue("There should be no errors reported:\n"+getErrorMessages("PR133532"),
				getErrorMessages("PR133532").isEmpty());	
	}
	
	public void testPr133532_2() {
		initialiseProject("pr133532_2");
		build("pr133532_2");
		alter("pr133532_2","inc2");
		build("pr133532_2");
		assertTrue("There should be no errors reported:\n"+getErrorMessages("pr133532_2"),
				getErrorMessages("pr133532_2").isEmpty());	
		String decisions = AjdeInteractionTestbed.MyStateListener.getDecisions();
		String expect="Need to recompile 'A.aj'";
		assertTrue("Couldn't find build decision: '"+expect+"' in the list of decisions made:\n"+decisions,
				  decisions.indexOf(expect)!=-1);
	}
	
	public void testPr133532_3() {
		initialiseProject("PR133532_3");
		build("PR133532_3");
		alter("PR133532_3","inc1");
		build("PR133532_3");
		assertTrue("There should be no errors reported:\n"+getErrorMessages("PR133532_3"),
				getErrorMessages("PR133532_3").isEmpty());			
	}
	
	public void testPr134541() {
		initialiseProject("PR134541");
		build("PR134541");
		assertEquals("[Xlint:adviceDidNotMatch] should be associated with line 5",5,
				((IMessage)getWarningMessages("PR134541").get(0)).getSourceLocation().getLine());
		alter("PR134541","inc1");
		build("PR134541");
		if (AsmManager.getDefault().getHandleProvider().dependsOnLocation())
		  checkWasFullBuild(); // the line number has changed... but nothing structural about the code
		else 
		  checkWasntFullBuild(); // the line number has changed... but nothing structural about the code
		assertEquals("[Xlint:adviceDidNotMatch] should now be associated with line 7",7,
				((IMessage)getWarningMessages("PR134541").get(0)).getSourceLocation().getLine());
	}
	
	public void testJDTLikeHandleProviderWithLstFile_pr141730() {
		IElementHandleProvider handleProvider = AsmManager.getDefault().getHandleProvider();
		AsmManager.getDefault().setHandleProvider(new JDTLikeHandleProvider());
		try {
			// The JDTLike-handles should start with the name
			// of the buildconfig file
			initialiseProject("JDTLikeHandleProvider");
			build("JDTLikeHandleProvider");
			IHierarchy top = AsmManager.getDefault().getHierarchy();
		  	IProgramElement pe = top.findElementForType("pkg","A");
		  	String expectedHandle = "JDTLikeHandleProvider<pkg*A.aj}A";
		  	assertEquals("expected handle to be " + expectedHandle + ", but found "
		  			+ pe.getHandleIdentifier(),expectedHandle,pe.getHandleIdentifier());	
		} finally {
			AsmManager.getDefault().setHandleProvider(handleProvider);
		}
	}
	
	public void testMovingAdviceDoesntChangeHandles_pr141730() {
		IElementHandleProvider handleProvider = AsmManager.getDefault().getHandleProvider();
		AsmManager.getDefault().setHandleProvider(new JDTLikeHandleProvider());
		try {
			initialiseProject("JDTLikeHandleProvider");
			build("JDTLikeHandleProvider");
			checkWasFullBuild();
			IHierarchy top = AsmManager.getDefault().getHierarchy();
			IProgramElement pe = top.findElementForLabel(top.getRoot(),
					IProgramElement.Kind.ADVICE,"before(): <anonymous pointcut>");
		  	// add a line which shouldn't change the handle
			alter("JDTLikeHandleProvider","inc1");
			build("JDTLikeHandleProvider");
			checkWasntFullBuild();
			IHierarchy top2 = AsmManager.getDefault().getHierarchy();
			IProgramElement pe2 = top.findElementForLabel(top2.getRoot(),
					IProgramElement.Kind.ADVICE,"before(): <anonymous pointcut>");
			assertEquals("expected advice to be on line " + pe.getSourceLocation().getLine() + 1 
					+ " but was on " + pe2.getSourceLocation().getLine(),
					pe.getSourceLocation().getLine()+1,pe2.getSourceLocation().getLine());
			assertEquals("expected advice to have handle " + pe.getHandleIdentifier()
					+ " but found handle " + pe2.getHandleIdentifier(),
					pe.getHandleIdentifier(),pe2.getHandleIdentifier());		
		} finally {
			AsmManager.getDefault().setHandleProvider(handleProvider);
		}
	}
	
	public void testSwappingAdviceAndHandles_pr141730() {
		IElementHandleProvider handleProvider = AsmManager.getDefault().getHandleProvider();
		AsmManager.getDefault().setHandleProvider(new JDTLikeHandleProvider());
		try {
			initialiseProject("JDTLikeHandleProvider");
			build("JDTLikeHandleProvider");
			IHierarchy top = AsmManager.getDefault().getHierarchy();

			IProgramElement call = top.findElementForLabel(top.getRoot(),
					IProgramElement.Kind.ADVICE, "after(): callPCD..");
			IProgramElement exec = top.findElementForLabel(top.getRoot(),
					IProgramElement.Kind.ADVICE, "after(): execPCD..");
		  	// swap the two after advice statements over. This forces
			// a full build which means 'after(): callPCD..' will now
			// be the second after advice in the file and have the same
			// handle as 'after(): execPCD..' originally did.
			alter("JDTLikeHandleProvider","inc2");
			build("JDTLikeHandleProvider");
			checkWasFullBuild();
			
			IHierarchy top2 = AsmManager.getDefault().getHierarchy();
			IProgramElement newCall = top2.findElementForLabel(top2.getRoot(),
					IProgramElement.Kind.ADVICE, "after(): callPCD..");
			IProgramElement newExec = top2.findElementForLabel(top2.getRoot(),
					IProgramElement.Kind.ADVICE, "after(): execPCD..");

			assertEquals("after swapping places, expected 'after(): callPCD..' " +
					"to be on line " + newExec.getSourceLocation().getLine() +
					" but was on line " + call.getSourceLocation().getLine(),
					newExec.getSourceLocation().getLine(),
					call.getSourceLocation().getLine());
			assertEquals("after swapping places, expected 'after(): callPCD..' " +
					"to have handle " + exec.getHandleIdentifier() +
					" (because was full build) but had " + newCall.getHandleIdentifier(),
					exec.getHandleIdentifier(), newCall.getHandleIdentifier());
		} finally {
			AsmManager.getDefault().setHandleProvider(handleProvider);
		}
	}
	
	public void testInitializerCountForJDTLikeHandleProvider_pr141730() {
		IElementHandleProvider handleProvider = AsmManager.getDefault().getHandleProvider();
		AsmManager.getDefault().setHandleProvider(new JDTLikeHandleProvider());
		try {
			initialiseProject("JDTLikeHandleProvider");
			build("JDTLikeHandleProvider");
			String expected = "JDTLikeHandleProvider<pkg*A.aj[C|1";

			IHierarchy top = AsmManager.getDefault().getHierarchy();
			IProgramElement init = top.findElementForLabel(top.getRoot(),
					IProgramElement.Kind.INITIALIZER, "...");
			assertEquals("expected initializers handle to be " + expected + "," +
					" but found " + init.getHandleIdentifier(true),
					expected,init.getHandleIdentifier(true));
			
			alter("JDTLikeHandleProvider","inc2");
			build("JDTLikeHandleProvider");
			checkWasFullBuild();
			
			IHierarchy top2 = AsmManager.getDefault().getHierarchy();
			IProgramElement init2 = top2.findElementForLabel(top2.getRoot(),
					IProgramElement.Kind.INITIALIZER, "...");
			assertEquals("expected initializers handle to still be " + expected + "," +
					" but found " + init2.getHandleIdentifier(true),
					expected,init2.getHandleIdentifier(true));

		
		} finally {
			AsmManager.getDefault().setHandleProvider(handleProvider);
		}
	}
	
	// 134471 related tests perform incremental compilation and verify features of the structure model post compile
	public void testPr134471_IncrementalCompilationAndModelUpdates() {
		try {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=false;
		
		// Step1.  Build the code, simple advice from aspect A onto class C
		initialiseProject("PR134471");
		configureNonStandardCompileOptions("PR134471","-showWeaveInfo -emacssym");
		configureShowWeaveInfoMessages("PR134471",true);
		build("PR134471");
		
		// Step2. Quick check that the advice points to something...
		IProgramElement nodeForTypeA = checkForNode("pkg","A",true);
		IProgramElement nodeForAdvice = findAdvice(nodeForTypeA);
		List relatedElements = getRelatedElements(nodeForAdvice,1);
		
		// Step3. Check the advice applying at the first 'code' join point in pkg.C is from aspect pkg.A, line 7
		IProgramElement programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true)));
		int line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 7 - but is at line "+line,line==7);
		
		// Step4. Simulate the aspect being saved but with no change at all in it
		alter("PR134471","inc1");
		build("PR134471");

		// Step5. Quick check that the advice points to something...
		nodeForTypeA = checkForNode("pkg","A",true);
		nodeForAdvice = findAdvice(nodeForTypeA);
		relatedElements = getRelatedElements(nodeForAdvice,1);

		// Step6. Check the advice applying at the first 'code' join point in pkg.C is from aspect pkg.A, line 7
		programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true)));
		line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 7 - but is at line "+line,line==7);
		} finally {
		// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=true;
		}
	}
	
	// now the advice moves down a few lines - hopefully the model will notice... see discussion in 134471
	public void testPr134471_MovingAdvice() {
		
		// Step1. build the project
		initialiseProject("PR134471_2");
		configureNonStandardCompileOptions("PR134471_2","-showWeaveInfo -emacssym");
		configureShowWeaveInfoMessages("PR134471_2",true);
		build("PR134471_2");
		
		// Step2. confirm advice is from correct location
		IProgramElement programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true)));
		int line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 7 - but is at line "+line,line==7);
		
		// Step3. No structural change to the aspect but the advice has moved down a few lines... (change in source location)
		alter("PR134471_2","inc1");
		build("PR134471_2");
		if (AsmManager.getDefault().getHandleProvider().dependsOnLocation())
			  checkWasFullBuild(); // the line number has changed... but nothing structural about the code
			else 
			  checkWasntFullBuild(); // the line number has changed... but nothing structural about the code

		//checkWasFullBuild(); // this is true whilst we consider sourcelocation in the type/shadow munger equals() method - have to until the handles are independent of location
		
		// Step4. Check we have correctly realised the advice moved to line 11
		programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true)));
		line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 11 - but is at line "+line,line==11);
	}
	

	public void testAddingAndRemovingDecwWithStructureModel() {
		initialiseProject("P3");
		build("P3");
		alter("P3","inc1");
		build("P3");
		assertTrue("There should be no exceptions handled:\n"+getCompilerErrorMessages("P3"),
				getCompilerErrorMessages("P3").isEmpty());		
		alter("P3","inc2");
		build("P3");
		assertTrue("There should be no exceptions handled:\n"+getCompilerErrorMessages("P3"),
				getCompilerErrorMessages("P3").isEmpty());		
	}
		
	
	// same as first test with an extra stage that asks for C to be recompiled, it should still be advised...
	public void testPr134471_IncrementallyRecompilingTheAffectedClass() {
		try {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=false;
			// Step1. build the project
			initialiseProject("PR134471");
			configureNonStandardCompileOptions("PR134471","-showWeaveInfo -emacssym");	
			configureShowWeaveInfoMessages("PR134471",true);
			build("PR134471");
			
			// Step2. confirm advice is from correct location
			IProgramElement programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true)));
			int line = programElement.getSourceLocation().getLine();
			assertTrue("advice should be at line 7 - but is at line "+line,line==7);
	
			// Step3. No change to the aspect at all
			alter("PR134471","inc1");
			build("PR134471");
			
			// Step4. Quick check that the advice points to something...
			IProgramElement nodeForTypeA = checkForNode("pkg","A",true);
			IProgramElement nodeForAdvice = findAdvice(nodeForTypeA);
			List relatedElements = getRelatedElements(nodeForAdvice,1);
			
		    // Step5. No change to the file C but it should still be advised afterwards
			alter("PR134471","inc2");
			build("PR134471");
			checkWasntFullBuild();
		
			// Step6. confirm advice is from correct location
			programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true)));
			line = programElement.getSourceLocation().getLine();
			assertTrue("advice should be at line 7 - but is at line "+line,line==7);		
		} finally {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=true;
		}

	}

	// similar to previous test but with 'declare warning' as well as advice
	public void testPr134471_IncrementallyRecompilingAspectContainingDeclare() {
		
		// Step1. build the project
		initialiseProject("PR134471_3");
		configureNonStandardCompileOptions("PR134471_3","-showWeaveInfo -emacssym");
		configureShowWeaveInfoMessages("PR134471_3",true);
		build("PR134471_3");
		checkWasFullBuild();
		
		// Step2. confirm declare warning is from correct location, decw matches line 7 in pkg.C
		IProgramElement programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true),7));
		int line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 10 - but is at line "+line,line==10);
		
		// Step3. confirm advice is from correct location, advice matches line 6 in pkg.C
		programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true),6));
		line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 7 - but is at line "+line,line==7);

		// Step4. Move declare warning in the aspect
		alter("PR134471_3","inc1");
		build("PR134471_3");
		if (AsmManager.getDefault().getHandleProvider().dependsOnLocation())
			  checkWasFullBuild(); // the line number has changed... but nothing structural about the code
			else 
			  checkWasntFullBuild(); // the line number has changed... but nothing structural about the code

		//checkWasFullBuild();

		// Step5. confirm declare warning is from correct location, decw (now at line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true),7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line "+line,line==12);

		// Step6. Now just simulate 'resave' of the aspect, nothing has changed
		alter("PR134471_3","inc2");
		build("PR134471_3");
		checkWasntFullBuild();

		// Step7. confirm declare warning is from correct location, decw (now at line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true),7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line "+line,line==12);
	}
	
	// similar to previous test but with 'declare warning' as well as advice
	public void testPr134471_IncrementallyRecompilingTheClassAffectedByDeclare() {
		
		// Step1. build the project
		initialiseProject("PR134471_3");
		configureNonStandardCompileOptions("PR134471_3","-showWeaveInfo -emacssym");
		configureShowWeaveInfoMessages("PR134471_3",true);
		build("PR134471_3");
		checkWasFullBuild();
		
		// Step2. confirm declare warning is from correct location, decw matches line 7 in pkg.C
		IProgramElement programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true),7));
		int line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 10 - but is at line "+line,line==10);
		
		// Step3. confirm advice is from correct location, advice matches line 6 in pkg.C
		programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true),6));
		line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 7 - but is at line "+line,line==7);

		// Step4. Move declare warning in the aspect
		alter("PR134471_3","inc1");
		build("PR134471_3");
		if (AsmManager.getDefault().getHandleProvider().dependsOnLocation())
			  checkWasFullBuild(); // the line number has changed... but nothing structural about the code
			else 
			  checkWasntFullBuild(); // the line number has changed... but nothing structural about the code

		//checkWasFullBuild();

		// Step5. confirm declare warning is from correct location, decw (now at line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true),7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line "+line,line==12);

		// Step6. Now just simulate 'resave' of the aspect, nothing has changed
		alter("PR134471_3","inc2");
		build("PR134471_3");
		checkWasntFullBuild();

		// Step7. confirm declare warning is from correct location, decw (now at line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true),7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line "+line,line==12);

		// Step8. Now just simulate resave of the pkg.C type - no change at all... are relationships gonna be repaired OK?
		alter("PR134471_3","inc3");
		build("PR134471_3");
		checkWasntFullBuild();

		// Step9. confirm declare warning is from correct location, decw (now at line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(findCode(checkForNode("pkg","C",true),7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line "+line,line==12);
	}
	
	public void testDontLoseXlintWarnings_pr141556() {
		initialiseProject("PR141556");
		configureNonStandardCompileOptions("PR141556","-Xlint:warning");
		build("PR141556");
		checkWasFullBuild();
		String warningMessage = "can not build thisJoinPoint " +
				"lazily for this advice since it has no suitable guard " +
				"[Xlint:noGuardForLazyTjp]";
		assertEquals("warning message should be '" + warningMessage + "'",
				warningMessage,
				((IMessage)getWarningMessages("PR141556").get(0))
					.getMessage());

		// add a space to the Aspect but dont do a build
		alter("PR141556","inc1");
		// remove the space so that the Aspect is exactly as it was
		alter("PR141556","inc2");
		// build the project and we should not have lost the xlint warning
		build("PR141556");
		checkWasntFullBuild();
		assertTrue("there should still be a warning message ",
				!getWarningMessages("PR141556").isEmpty());
		assertEquals("warning message should be '" + warningMessage + "'",
				warningMessage,
				((IMessage)getWarningMessages("PR141556").get(0))
					.getMessage());
	}
	
	public void testAdviceDidNotMatch_pr152589() {
		initialiseProject("PR152589");
		build("PR152589");
		List warnings = getWarningMessages("PR152589");
		assertTrue("There should be no warnings:\n"+warnings,
				warnings.isEmpty());
		alter("PR152589","inc1");
		build("PR152589");
		if (AsmManager.getDefault().getHandleProvider().dependsOnLocation())
			  checkWasFullBuild(); // the line number has changed... but nothing structural about the code
			else 
			  checkWasntFullBuild(); // the line number has changed... but nothing structural about the code

//		checkWasFullBuild();
		warnings = getWarningMessages("PR152589");
		assertTrue("There should be no warnings after adding a whitespace:\n"
				+warnings,warnings.isEmpty());	
	}
	
	// see comment #11 of bug 154054
	public void testNoFullBuildOnChangeInSysOutInAdviceBody_pr154054() {
		initialiseProject("PR154054");
		build("PR154054");
		alter("PR154054","inc1");
		build("PR154054");
		checkWasntFullBuild();
	}

	// change exception type in around advice, does it notice?
	public void testShouldFullBuildOnExceptionChange_pr154054() {
		initialiseProject("PR154054_2");
		build("PR154054_2");
		alter("PR154054_2","inc1");
		build("PR154054_2");
		checkWasFullBuild();
	}
	
	public void testPR158573() {
		IElementHandleProvider handleProvider = AsmManager.getDefault().getHandleProvider();
		AsmManager.getDefault().setHandleProvider(new JDTLikeHandleProvider());
		initialiseProject("PR158573");
		build("PR158573");
		List warnings = getWarningMessages("PR158573");
		assertTrue("There should be no warnings:\n"+warnings,warnings.isEmpty());
		alter("PR158573","inc1");
		build("PR158573");

		checkWasntFullBuild();
		warnings = getWarningMessages("PR158573");
		assertTrue("There should be no warnings after changing the value of a " +
				"variable:\n"+warnings,warnings.isEmpty());	
		AsmManager.getDefault().setHandleProvider(handleProvider);
	}
	
	/**
	 * If the user has specified that they want Java 6 compliance
	 * and kept the default classfile and source file level settings
	 * (also 6.0) then expect an error saying that we don't support 
	 * java 6.
	 */
	public void testPR164384_1() {
		initialiseProject("PR164384");

		Hashtable javaOptions = new Hashtable();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance","1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform","1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.source","1.6");
		configureJavaOptionsMap("PR164384",javaOptions);
		
		build("PR164384");
		List errors = getErrorMessages("PR164384");
		
		if (getCompilerForProjectWithName("PR164384").isJava6Compatible()) {
			assertTrue("There should be no errors:\n"+errors,errors.isEmpty());	
		} else {
			String expectedError = "Java 6.0 compliance level is unsupported";
			String found = ((IMessage)errors.get(0)).getMessage();
			assertEquals("Expected 'Java 6.0 compliance level is unsupported'" +
					" error message but found " + found,expectedError,found);
			// This is because the 'Java 6.0 compliance' error is an 'error'
			// rather than an 'abort'. Aborts are really for compiler exceptions.
			assertTrue("expected there to be more than the one compliance level" +
					" error but only found that one",errors.size() > 1);
		}
		
	}

	/**
	 * If the user has specified that they want Java 6 compliance
	 * and selected classfile and source file level settings to be
	 * 5.0 then expect an error saying that we don't support java 6.
	 */
	public void testPR164384_2() {
		initialiseProject("PR164384");

		Hashtable javaOptions = new Hashtable();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance","1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform","1.5");
		javaOptions.put("org.eclipse.jdt.core.compiler.source","1.5");
		configureJavaOptionsMap("PR164384",javaOptions);
		
		build("PR164384");
		List errors = getErrorMessages("PR164384");
		if (getCompilerForProjectWithName("PR164384").isJava6Compatible()) {
			assertTrue("There should be no errors:\n"+errors,errors.isEmpty());	
		} else {
			String expectedError = "Java 6.0 compliance level is unsupported";
			String found = ((IMessage)errors.get(0)).getMessage();
			assertEquals("Expected 'Java 6.0 compliance level is unsupported'" +
					" error message but found " + found,expectedError,found);			
			// This is because the 'Java 6.0 compliance' error is an 'error'
			// rather than an 'abort'. Aborts are really for compiler exceptions.
			assertTrue("expected there to be more than the one compliance level" +
					" error but only found that one",errors.size() > 1);
		}
	}
	
	/**
	 * If the user has specified that they want Java 6 compliance
	 * and set the classfile level to be 6.0 and source file level 
	 * to be 5.0 then expect an error saying that we don't support 
	 * java 6.
	 */
	public void testPR164384_3() {
		initialiseProject("PR164384");

		Hashtable javaOptions = new Hashtable();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance","1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform","1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.source","1.5");
		configureJavaOptionsMap("PR164384",javaOptions);
		
		build("PR164384");
		List errors = getErrorMessages("PR164384");
		
		if (getCompilerForProjectWithName("PR164384").isJava6Compatible()) {
			assertTrue("There should be no errros:\n"+errors,errors.isEmpty());	
		} else {
			String expectedError = "Java 6.0 compliance level is unsupported";
			String found = ((IMessage)errors.get(0)).getMessage();
			assertEquals("Expected 'Java 6.0 compliance level is unsupported'" +
					" error message but found " + found,expectedError,found);			
			// This is because the 'Java 6.0 compliance' error is an 'error'
			// rather than an 'abort'. Aborts are really for compiler exceptions.
			assertTrue("expected there to be more than the one compliance level" +
					" error but only found that one",errors.size() > 1);
		}
	}
		
	public void testPr168840() throws Exception {
		initialiseProject("inpathTesting");
		
		String inpathTestingDir = getWorkingDir() + File.separator + "inpathTesting";
		String inpathDir  = inpathTestingDir + File.separator + "injarBin" + File.separator + "pkg";
		String expectedOutputDir = inpathTestingDir + File.separator + "bin";
		
		// set up the inpath to have the directory on it's path
		File f = new File(inpathDir);
		Set s = new HashSet();
		s.add(f);
		configureInPath("inpathTesting",s);
		build("inpathTesting");
		// the declare warning matches one place so expect one warning message
		List warnings = getWarningMessages("inpathTesting");
		assertTrue("Expected there to be one warning message but found "
				+ warnings.size() + ": " + warnings, warnings.size() == 1);
		
		// copy over the updated version of the inpath class file
		File from = new File(testdataSrcDir+File.separatorChar+"inpathTesting"
				+File.separatorChar+"newInpathClass" + File.separatorChar + "InpathClass.class");
		File destination = new File(inpathDir + File.separatorChar + "InpathClass.class");
		FileUtil.copyFile(from,destination);
		
		build("inpathTesting");
		checkWasntFullBuild();
		// the newly copied inpath class means the declare warning now matches two
		// places, therefore expect two warning messages
		warnings = getWarningMessages("inpathTesting");
		assertTrue("Expected there to be two warning message but found "
				+ warnings.size() + ": " + warnings, warnings.size() == 2);
	}
	
	// --- helper code ---
	
	/**
	 * Retrieve program elements related to this one regardless of the relationship.  A JUnit assertion is
	 * made that the number that the 'expected' number are found.
	 * 
	 * @param programElement Program element whose related elements are to be found
	 * @param expected the number of expected related elements
	 */
	private List/*IProgramElement*/ getRelatedElements(IProgramElement programElement,int expected) {
		List relatedElements = getRelatedElements(programElement);
		StringBuffer debugString = new StringBuffer();
		if (relatedElements!=null) {
			for (Iterator iter = relatedElements.iterator(); iter.hasNext();) {	
				String element = (String) iter.next();
				debugString.append(AsmManager.getDefault().getHierarchy().findElementForHandle(element).toLabelString()).append("\n");
			}
		}
		assertTrue("Should be "+expected+" element"+(expected>1?"s":"")+" related to this one '"+programElement+
				"' but found :\n "+debugString,relatedElements!=null && relatedElements.size()==1);
		return relatedElements;
	}
	
	private IProgramElement getFirstRelatedElement(IProgramElement programElement) {
		List rels = getRelatedElements(programElement,1);
		return AsmManager.getDefault().getHierarchy().findElementForHandle((String)rels.get(0));
	}

	
	
	private List/*IProgramElement*/ getRelatedElements(IProgramElement advice) {
		List output = null;
		IRelationshipMap map = AsmManager.getDefault().getRelationshipMap();
		List/*IRelationship*/ rels = (List)map.get(advice);
		if (rels==null) fail("Did not find any related elements!");
		for (Iterator iter = rels.iterator(); iter.hasNext();) {
			IRelationship element = (IRelationship) iter.next();
			List/*String*/ targets = element.getTargets();
			if (output==null) output = new ArrayList();
			output.addAll(targets);
		}
		return output;
	}
	
	private IProgramElement findAdvice(IProgramElement ipe) {
		return findAdvice(ipe,1);
	}
	
	private IProgramElement findAdvice(IProgramElement ipe,int whichOne) {
		if (ipe.getKind()==IProgramElement.Kind.ADVICE) {
			whichOne=whichOne-1;
			if (whichOne==0) return ipe;
		}
		List kids = ipe.getChildren();
		for (Iterator iter = kids.iterator(); iter.hasNext();) {
			IProgramElement kid = (IProgramElement) iter.next();
			IProgramElement found = findAdvice(kid,whichOne);
			if (found!=null) return found;
		}
		return null;
	}
	
	/**
	 * Finds the first 'code' program element below the element supplied - will return null if there aren't any
	 */
	private IProgramElement findCode(IProgramElement ipe) {
		return findCode(ipe,-1);
	}
	
	/**
	 * Searches a hierarchy of program elements for a 'code' element at the specified line number, a line number
	 * of -1 means just return the first one you find
	 */
	private IProgramElement findCode(IProgramElement ipe,int linenumber) {
		if (ipe.getKind()==IProgramElement.Kind.CODE) {
			if (linenumber==-1 || ipe.getSourceLocation().getLine()==linenumber) return ipe;
		}
		List kids = ipe.getChildren();
		for (Iterator iter = kids.iterator(); iter.hasNext();) {
			IProgramElement kid = (IProgramElement) iter.next();
			IProgramElement found = findCode(kid,linenumber);
			if (found!=null) return found;
		}
		return null;
	}
	
	
	// other possible tests:
	// - memory usage (freemem calls?)
	// - relationship map

	// ---------------------------------------------------------------------------------------------------

	private IProgramElement checkForNode(String packageName,String typeName,boolean shouldBeFound) {
		IProgramElement ipe = AsmManager.getDefault().getHierarchy().findElementForType(packageName,typeName);
		if (shouldBeFound) {
           if (ipe==null) printModel();
		   assertTrue("Should have been able to find '"+packageName+"."+typeName+"' in the asm",ipe!=null);
		} else {
		   if (ipe!=null) printModel();
		   assertTrue("Should have NOT been able to find '"+packageName+"."+typeName+"' in the asm",ipe==null);
		}
		return ipe;	
	}


	private void printModel() {
		try {
			AsmManager.dumptree(AsmManager.getDefault().getHierarchy().getRoot(),0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void log(String msg) {
		if (VERBOSE) System.out.println(msg);
	}

	private File getProjectRelativePath(String p,String filename) {
		File projDir = new File(getWorkingDir(),p);
		return new File(projDir,filename);
	}

	private File getProjectOutputRelativePath(String p,String filename) {
		File projDir = new File(getWorkingDir(),p);
		return new File(projDir,"bin"+File.separator+filename);
	}

	private void assertNoErrors(String projectName) {
		assertTrue("Should be no errors, but got "+getErrorMessages(projectName),getErrorMessages(projectName).size()==0);				
	}
	
}