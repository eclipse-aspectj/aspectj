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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.ajde.core.ICompilerConfiguration;
import org.aspectj.ajde.core.TestOutputLocationManager;
import org.aspectj.ajde.core.internal.AjdeCoreBuildManager;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Kind;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.tools.ajc.Ajc;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;

/**
 * The superclass knows all about talking through Ajde to the compiler. The superclass isn't in charge of knowing how to simulate
 * overlays for incremental builds, that is in here. As is the ability to generate valid build configs based on a directory
 * structure. To support this we just need access to a sandbox directory - this sandbox is managed by the superclass (it only
 * assumes all builds occur in <sandboxDir>/<projectName>/ )
 *
 * The idea is you can initialize multiple projects in the sandbox and they can all be built independently, hopefully exploiting
 * incremental compilation. Between builds you can alter the contents of a project using the alter() method that overlays some set
 * of new files onto the current set (adding new files/changing existing ones) - you can then drive a new build and check it behaves
 * as expected.
 */
public class MultiProjectIncrementalTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	public void testIncremental_344326() throws Exception {
		String p = "pr344326";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 3, 4);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		checkCompileWeaveCount(p, 1, 1);
	}

	public void testMissingRel_328121() throws Exception {
		String p = "pr328121";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		// Check the annotations:
		runMethod(p, "TestRequirements.TestRequirements", "foo");
		assertEquals(4, getRelationshipCount(p));
	}

	public void testEncoding_pr290741() throws Exception {
		String p = "pr290741";
		initialiseProject(p);
		setProjectEncoding(p, "UTF-8");
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		runMethod(p, "demo.ConverterTest", "run");
	}

	public void testRogueConstantReference() throws Exception {
		String p = "pr404345";
		initialiseProject(p);
		setProjectEncoding(p, "UTF-8");
		build(p);
		checkWasFullBuild();
		// Should both indicate that Location cannot be resolved
		assertEquals(2,getErrorMessages(p).size());
	}

	public void testIncrementalITDInners4() throws Exception {
		String p = "prInner4";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		// touch the aspect making the ITD member type
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertNoErrors(p);
	}

	public void testIncrementalITDInners3() throws Exception {
		String p = "prInner3";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		// touch the aspect making the ITD member type
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		// touch the aspect making the ITD that depends on the member type
		alter(p, "inc2");
		build(p);
		checkWasntFullBuild();
		// touch the type affected by the ITDs
		alter(p, "inc3");
		build(p);
		checkWasntFullBuild();
	}

	// mixing ITDs with inner type intertypes
	public void testIncrementalITDInners2() throws Exception {
		String p = "prInner2";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		// touch the aspect making the ITD member type
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		// touch the aspect making the ITD that depends on the member type
		alter(p, "inc2");
		build(p);
		checkWasntFullBuild();
		// touch the type affected by the ITDs
		alter(p, "inc3");
		build(p);
		checkWasntFullBuild();
	}

	public void testIncrementalITDInners() throws Exception {
		String p = "prInner";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
	}

	/*
	 * public void testIncrementalAspectWhitespace() throws Exception { AjdeInteractionTestbed.VERBOSE = true; String p = "xxx";
	 * initialiseProject(p); configureNonStandardCompileOptions(p, "-showWeaveInfo"); configureShowWeaveInfoMessages(p, true);
	 * build(p);
	 *
	 * List weaveMessages = getWeavingMessages(p); if (weaveMessages.size() != 0) { for (Iterator iterator =
	 * weaveMessages.iterator(); iterator.hasNext();) { Object object = iterator.next(); System.out.println(object); } }
	 * checkWasFullBuild(); assertNoErrors(p); alter(p, "inc1"); build(p); checkWasntFullBuild(); assertNoErrors(p); }
	 */

	public void testIncrementalGenericItds_pr280676() throws Exception {
		String p = "pr280676";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		alter(p, "inc1"); // remove type variables from ITD field
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		alter(p, "inc2"); // remove type variables from ITD method
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		alter(p, "inc3"); // readded type variables on ITD method
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
	}

	public void testIncrementalGenericItds_pr280676_2() throws Exception {
		String p = "pr280676_2";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		alter(p, "inc1"); // remove type variables from target type
		build(p);
		List<IMessage> errors = getErrorMessages(p);
		// Build errors:
		// error at N:\temp\ajcSandbox\aspectj16_3\ajcTest60379.tmp\pr280676_2\src\p\A.java:8:0::0 a.ls cannot be resolved or is not
		// a field
		// error at N:\temp\ajcSandbox\aspectj16_3\ajcTest60379.tmp\pr280676_2\src\p\Foo.aj:8:0::0 Type parameters can not be
		// specified in the ITD target type - the target type p.A is not generic.
		// error at N:\temp\ajcSandbox\aspectj16_3\ajcTest60379.tmp\pr280676_2\src\p\Foo.aj:12:0::0 Type parameters can not be
		// specified in the ITD target type - the target type p.A is not generic.
		// error at N:\temp\ajcSandbox\aspectj16_3\ajcTest60379.tmp\pr280676_2\src\p\Foo.aj:8:0::0 Type parameters can not be
		// specified in the ITD target type - the target type p.A is not generic.
		// error at N:\temp\ajcSandbox\aspectj16_3\ajcTest60379.tmp\pr280676_2\src\p\Foo.aj:12:0::0 Type parameters can not be
		// specified in the ITD target type - the target type p.A is not generic.
		assertEquals(5, errors.size());
	}

	public void testAdviceHandles_pr284771() throws Exception {
		String p = "pr284771";
		initialiseProject(p);
		build(p);
		IRelationshipMap irm = getModelFor(p).getRelationshipMap();
		List<IRelationship> rels = irm.get("=pr284771<test*AspectTrace.aj'AspectTrace&before");
		assertNotNull(rels);
		assertEquals(2, ((Relationship) rels.get(0)).getTargets().size());
		rels = irm.get("=pr284771<test*AspectTrace.aj'AspectTrace&before!2");
		assertNotNull(rels);
		assertEquals(2, ((Relationship) rels.get(0)).getTargets().size());
	}

	public void testDeclareSoftHandles_329111() throws Exception {
		String p = "pr329111";
		initialiseProject(p);
		build(p);
		printModel(p);
		IRelationshipMap irm = getModelFor(p).getRelationshipMap();
		List<IRelationship> rels = irm.get("=pr329111<{AJ.java'AJ`declare soft");
		assertNotNull(rels);
		rels = irm.get("=pr329111<{AJ2.java'AJ2`declare soft");
		assertNotNull(rels);
		rels = irm.get("=pr329111<{AJ2.java'AJ2`declare soft!2");
		assertNotNull(rels);
		rels = irm.get("=pr329111<{AJ2.java'AJ2`declare soft!3");
		assertNotNull(rels);
		rels = irm.get("=pr329111<{AJ3.java'AJ3`declare warning");
		assertNotNull(rels);
		rels = irm.get("=pr329111<{AJ3.java'AJ3`declare warning!2");
		assertNotNull(rels);
		rels = irm.get("=pr329111<{AJ3.java'AJ3`declare error");
		assertNotNull(rels);
		rels = irm.get("=pr329111<{AJ3.java'AJ3`declare error!2");
		assertNotNull(rels);
	}

	/**
	 * Test that the declare parents in the super aspect gets a relationship from the type declaring it.
	 */
	public void testAspectInheritance_322446() throws Exception {
		String p = "pr322446";
		initialiseProject(p);
		build(p);
		IRelationshipMap irm = getModelFor(p).getRelationshipMap();
		// Hid:1:(targets=1) =pr322446<{Class.java[Class (aspect declarations) =pr322446<{AbstractAspect.java'AbstractAspect`declare
		// parents
		// Hid:2:(targets=1) =pr322446<{AbstractAspect.java'AbstractAspect`declare parents (declared on) =pr322446<{Class.java[Class
		List<IRelationship> rels = irm.get("=pr322446<{AbstractAspect.java'AbstractAspect`declare parents");
		assertNotNull(rels);
	}

	public void testAspectInheritance_322446_2() throws Exception {
		String p = "pr322446_2";
		initialiseProject(p);
		build(p);
		IProgramElement thisAspectNode = getModelFor(p).getHierarchy().findElementForType("", "Sub");
		assertEquals("{Code=[I]}", thisAspectNode.getDeclareParentsMap().toString());
	}

	public void testBinaryAspectsAndTheModel_343001() throws Exception {
		String lib = "pr343001_lib";
		initialiseProject(lib);
		build(lib);

		// Check the 'standard build' - the library also has a type affected by the decp so we can check what happens on an 'all
		// source' build
		IProgramElement theAspect = getModelFor(lib).getHierarchy().findElementForHandleOrCreate("=pr343001_lib<{Super.java'Super",
				false);
		assertNotNull(theAspect);
		IProgramElement sourcelevelDecp = getModelFor(lib).getHierarchy().findElementForHandleOrCreate(
				"=pr343001_lib<{Super.java'Super`declare parents", false);
		assertNotNull(sourcelevelDecp);
		assertEquals("[java.io.Serializable]", sourcelevelDecp.getParentTypes().toString());

		String p = "pr343001";
		initialiseProject(p);
		configureAspectPath(p, getProjectRelativePath(lib, "bin"));
		build(p);

		IProgramElement theBinaryAspect = getModelFor(p).getHierarchy().findElementForHandleOrCreate(
				"=pr343001/binaries<(Super.class'Super", false);
		assertNotNull(theBinaryAspect);
		IProgramElement binaryDecp = getModelFor(p).getHierarchy().findElementForHandleOrCreate(
				"=pr343001/binaries<(Super.class'Super`declare parents", false);
		assertNotNull(binaryDecp);
		assertEquals("[java.io.Serializable]", (binaryDecp.getParentTypes() == null ? "" : binaryDecp.getParentTypes().toString()));
	}

	// found whilst looking at 322446 hence that is the testdata name
	public void testAspectInheritance_322664() throws Exception {
		String p = "pr322446_3";
		initialiseProject(p);
		build(p);
		assertNoErrors(p);
		alter(p, "inc1");
		build(p);
		// should be some errors:
		// error at N:\temp\ajcSandbox\aspectj16_1\ajcTest3209787521625191676.tmp\pr322446_3\src\AbstractAspect.java:5:0::0 can't
		// bind type name 'T'
		// error at N:\temp\ajcSandbox\aspectj16_1\ajcTest3209787521625191676.tmp\pr322446_3\src\AbstractAspect.java:8:0::0
		// Incorrect number of arguments for type AbstractAspect<S>; it cannot be parameterized with arguments <X, Y>
		List<IMessage> errors = getErrorMessages(p);
		assertTrue(errors != null && errors.size() > 0);
		alter(p, "inc2");
		build(p);
		// that build would contain an exception if the bug were around
		assertNoErrors(p);
	}

	// TODO (asc) these tests don't actually verify anything!
	// public void testAtDeclareParents_280658() throws Exception {
	// AjdeInteractionTestbed.VERBOSE = true;
	// String lib = "pr280658_decp";
	// initialiseProject(lib);
	// build(lib);
	// checkWasFullBuild();
	//
	// String cli = "pr280658_target";
	// initialiseProject(cli);
	//
	// configureAspectPath(cli, getProjectRelativePath(lib, "bin"));
	// build(cli);
	// checkWasFullBuild();
	// printModel(cli);
	// }
	//
	// public void testAtDeclareMixin_280651() throws Exception {
	// AjdeInteractionTestbed.VERBOSE = true;
	// String lib = "pr280651_decmix";
	// initialiseProject(lib);
	// build(lib);
	// checkWasFullBuild();
	//
	// String cli = "pr280658_target";
	// initialiseProject(cli);
	//
	// configureAspectPath(cli, getProjectRelativePath(lib, "bin"));
	// build(cli);
	// checkWasFullBuild();
	// printModel(cli);
	// }

	// Testing that declare annotation model entries preserve the fully qualified type of the annotation
	public void testDecAnnoState_pr286539() throws Exception {
		String p = "pr286539";
		initialiseProject(p);
		build(p);
		printModel(p);
		IProgramElement decpPE = getModelFor(p).getHierarchy().findElementForHandle(
				"=pr286539<p.q.r{Aspect.java'Asp`declare parents");
		assertNotNull(decpPE);
		String s = ((decpPE.getParentTypes()).get(0));
		assertEquals("p.q.r.Int", s);

		decpPE = getModelFor(p).getHierarchy().findElementForHandle("=pr286539<p.q.r{Aspect.java'Asp`declare parents!2");
		assertNotNull(decpPE);
		s = ((decpPE.getParentTypes()).get(0));
		assertEquals("p.q.r.Int", s);

		IProgramElement decaPE = getModelFor(p).getHierarchy().findElementForHandle(
				"=pr286539<p.q.r{Aspect.java'Asp`declare \\@type");
		assertNotNull(decaPE);
		assertEquals("p.q.r.Foo", decaPE.getAnnotationType());

		decaPE = getModelFor(p).getHierarchy().findElementForHandle("=pr286539<p.q.r{Aspect.java'Asp`declare \\@type!2");
		assertNotNull(decaPE);
		assertEquals("p.q.r.Goo", decaPE.getAnnotationType());

		decaPE = getModelFor(p).getHierarchy().findElementForHandle("=pr286539<p.q.r{Aspect.java'Asp`declare \\@field");
		assertNotNull(decaPE);
		assertEquals("p.q.r.Foo", decaPE.getAnnotationType());

		decaPE = getModelFor(p).getHierarchy().findElementForHandle("=pr286539<p.q.r{Aspect.java'Asp`declare \\@method");
		assertNotNull(decaPE);
		assertEquals("p.q.r.Foo", decaPE.getAnnotationType());

		decaPE = getModelFor(p).getHierarchy().findElementForHandle("=pr286539<p.q.r{Aspect.java'Asp`declare \\@constructor");
		assertNotNull(decaPE);
		assertEquals("p.q.r.Foo", decaPE.getAnnotationType());
	}

	public void testQualifiedInnerTypeRefs_269082() throws Exception {
		String p = "pr269082";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=false");

		build(p);
		printModel(p);

		IProgramElement root = getModelFor(p).getHierarchy().getRoot();

		IProgramElement ipe = findElementAtLine(root, 7);
		assertEquals("=pr269082<a{ClassUsingInner.java[ClassUsingInner~foo~QMyInner;~QObject;~QString;", ipe.getHandleIdentifier());

		ipe = findElementAtLine(root, 9);
		assertEquals("=pr269082<a{ClassUsingInner.java[ClassUsingInner~goo~QClassUsingInner.MyInner;~QObject;~QString;",
				ipe.getHandleIdentifier());

		ipe = findElementAtLine(root, 11);
		assertEquals("=pr269082<a{ClassUsingInner.java[ClassUsingInner~hoo~Qa.ClassUsingInner.MyInner;~QObject;~QString;",
				ipe.getHandleIdentifier());
	}

	// just simple incremental build - no code change, just the aspect touched
	public void testIncrementalFqItds_280380() throws Exception {

		String p = "pr280380";
		initialiseProject(p);
		build(p);
		// printModel(p);
		alter(p, "inc1");
		build(p);
		// should not be an error about f.AClass not being found
		assertNoErrors(p);
		// printModel(p);
	}

	public void testIncrementalAdvisingItdJoinpointsAccessingPrivFields_307120() throws Exception {
		String p = "pr307120";
		initialiseProject(p);
		build(p);
		// Hid:1:(targets=1) =pr307120<{Test.java}Test)A.getFoo?field-get(int A.foo) (advised by) =pr307120<{Test.java}Test&before
		// Hid:2:(targets=1) =pr307120<{A.java[A (aspect declarations) =pr307120<{Test.java}Test)A.getFoo
		// Hid:3:(targets=1) =pr307120<{Test.java}Test&before (advises) =pr307120<{Test.java}Test)A.getFoo?field-get(int A.foo)
		// Hid:4:(targets=1) =pr307120<{Test.java}Test)A.getFoo (declared on) =pr307120<{A.java[A
		alter(p, "inc1");
		assertEquals(4, getRelationshipCount(p));
		build(p);
		// Hid:1:(targets=1) =pr307120<{A.java[A (aspect declarations) =pr307120<{Test.java}Test)A.getFoo
		// Hid:2:(targets=1) =pr307120<{Test.java}Test)A.getFoo (declared on) =pr307120<{A.java[A
		// These two are missing without the fix:
		// Hid:1:(targets=1) =pr307120<{Test.java}Test)A.getFoo?field-get(int A.foo) (advised by) =pr307120<{Test.java}Test&before
		// Hid:7:(targets=1) =pr307120<{Test.java}Test&before (advises) =pr307120<{Test.java}Test)A.getFoo?field-get(int A.foo)
		assertNoErrors(p);
		assertEquals(4, getRelationshipCount(p));
	}

	public void testIncrementalAdvisingItdJoinpointsAccessingPrivFields_307120_pipelineOff() throws Exception {
		String p = "pr307120";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:pipelineCompilation=false");
		build(p);
		// Hid:1:(targets=1) =pr307120<{Test.java}Test)A.getFoo?field-get(int A.foo) (advised by) =pr307120<{Test.java}Test&before
		// Hid:2:(targets=1) =pr307120<{A.java[A (aspect declarations) =pr307120<{Test.java}Test)A.getFoo
		// Hid:3:(targets=1) =pr307120<{Test.java}Test&before (advises) =pr307120<{Test.java}Test)A.getFoo?field-get(int A.foo)
		// Hid:4:(targets=1) =pr307120<{Test.java}Test)A.getFoo (declared on) =pr307120<{A.java[A
		alter(p, "inc1");
		assertEquals(4, getRelationshipCount(p));
		build(p);
		// Hid:1:(targets=1) =pr307120<{A.java[A (aspect declarations) =pr307120<{Test.java}Test)A.getFoo
		// Hid:2:(targets=1) =pr307120<{Test.java}Test)A.getFoo (declared on) =pr307120<{A.java[A
		// These two are missing without the fix:
		// Hid:1:(targets=1) =pr307120<{Test.java}Test)A.getFoo?field-get(int A.foo) (advised by) =pr307120<{Test.java}Test&before
		// Hid:7:(targets=1) =pr307120<{Test.java}Test&before (advises) =pr307120<{Test.java}Test)A.getFoo?field-get(int A.foo)
		assertNoErrors(p);
		assertEquals(4, getRelationshipCount(p));
	}

	// More sophisticated variant of above.
	public void testIncrementalAdvisingItdJoinpointsAccessingPrivFields_307120_2_pipelineOff() throws Exception {
		String p = "pr307120_3";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:pipelineCompilation=false");
		build(p);
		assertNoErrors(p);
		// Hid:1:(targets=1) =pr307120_3<{TargetAugmenter.java}TargetAugmenter)Target.setIt)QString; (declared on)
		// =pr307120_3<{Target.java[Target

		// Hid:2:(targets=1) =pr307120_3<{Target.java[Target (aspect declarations)
		// =pr307120_3<{TargetAugmenter.java}TargetAugmenter)Target.setIt)QString;

		// these are missing under this bug:

		// Hid:3:(targets=1) =pr307120_3<{Advisor.java}Advisor&around&QObject;&QObject; (advises)
		// =pr307120_3<{TargetAugmenter.java}TargetAugmenter)Target.setIt)QString;?field-set(java.lang.String Target.it)

		// Hid:4:(targets=1) =pr307120_3<{TargetAugmenter.java}TargetAugmenter)Target.setIt)QString;?field-set(java.lang.String
		// Target.it) (advised by) =pr307120_3<{Advisor.java}Advisor&around&QObject;&QObject;

		assertEquals(4, getRelationshipCount(p));
		alter(p, "inc1");
		build(p);

		assertEquals(4, getRelationshipCount(p));
		assertNoErrors(p);
	}

	// More sophisticated variant of above.
	public void testIncrementalAdvisingItdJoinpointsAccessingPrivFields_307120_2() throws Exception {
		String p = "pr307120_2";
		initialiseProject(p);
		build(p);
		assertNoErrors(p);
		// Hid:2:(targets=1) =pr307120_2<{TargetAugmenter.java}TargetAugmenter)Target.setIt)QString; (declared on)
		// =pr307120_2<{Target.java[Target
		// Hid:8:(targets=1) =pr307120_2<{TargetAugmenter.java}TargetAugmenter)Target.getIt (declared on)
		// =pr307120_2<{Target.java[Target
		// Hid:5:(targets=2) =pr307120_2<{Target.java[Target (aspect declarations)
		// =pr307120_2<{TargetAugmenter.java}TargetAugmenter)Target.getIt
		// Hid:6:(targets=2) =pr307120_2<{Target.java[Target (aspect declarations)
		// =pr307120_2<{TargetAugmenter.java}TargetAugmenter)Target.setIt)QString;
		// Hid:1:(targets=1) =pr307120_2<{TargetAugmenter.java}TargetAugmenter)Target.setIt)QString;?field-set(java.lang.String
		// Target.it) (advised by) =pr307120_2<{Advisor.java}Advisor&around&QObject;&QObject;
		// Hid:3:(targets=1) =pr307120_2<{TargetAugmenter.java}TargetAugmenter)Target.getIt?field-get(java.lang.String Target.it)
		// (advised by) =pr307120_2<{Advisor.java}Advisor&around&QObject;
		// Hid:4:(targets=1) =pr307120_2<{Advisor.java}Advisor&around&QObject; (advises)
		// =pr307120_2<{TargetAugmenter.java}TargetAugmenter)Target.getIt?field-get(java.lang.String Target.it)
		// Hid:7:(targets=1) =pr307120_2<{Advisor.java}Advisor&around&QObject;&QObject; (advises)
		// =pr307120_2<{TargetAugmenter.java}TargetAugmenter)Target.setIt)QString;?field-set(java.lang.String Target.it)
		assertEquals(8, getRelationshipCount(p));
		alter(p, "inc1");
		build(p);
		assertEquals(8, getRelationshipCount(p));
		assertNoErrors(p);
	}

	// // More sophisticated variant of above.
	// public void testIncrementalAdvisingItdJoinpointsAccessingPrivFields_307120_4_pipelineOff() throws Exception {
	// String p = "pr307120_4";
	// initialiseProject(p);
	// configureNonStandardCompileOptions(p, "-Xset:pipelineCompilation=false");
	// build(p);
	// assertNoErrors(p);
	//
	// printModel(p);
	// assertEquals(4, getRelationshipCount(p));
	// alter(p, "inc1");
	// build(p);
	//
	// assertEquals(4, getRelationshipCount(p));
	// assertNoErrors(p);
	// }

	// modified aspect so target is fully qualified on the incremental change
	public void testIncrementalFqItds_280380_2() throws Exception {
		String p = "pr280380";
		initialiseProject(p);
		build(p);
		// printModel(p);
		assertEquals(4, getModelFor(p).getRelationshipMap().getEntries().size());
		// Hid:1:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.xxxx
		// Hid:2:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.y
		// Hid:3:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.AClass_new
		// Hid:4:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.y (declared on) =pr280380<f{AClass.java[AClass
		// Hid:5:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.AClass_new (declared on) =pr280380<f{AClass.java[AClass
		// Hid:6:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.xxxx (declared on) =pr280380<f{AClass.java[AClass

		alter(p, "inc2");
		build(p);
		// should not be an error about f.AClass not being found
		assertNoErrors(p);
		// printModel(p);
		assertEquals(4, getModelFor(p).getRelationshipMap().getEntries().size());
		// Hid:1:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.xxxx
		// Hid:2:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.y
		// Hid:3:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.AClass_new
		// Hid:4:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.y (declared on) =pr280380<f{AClass.java[AClass
		// Hid:5:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.AClass_new (declared on) =pr280380<f{AClass.java[AClass
		// Hid:6:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.xxxx (declared on) =pr280380<f{AClass.java[AClass
	}

	public void testIncrementalFqItds_280380_3() throws Exception {
		String p = "pr280380";
		initialiseProject(p);
		build(p);
		// printModel(p);
		assertEquals(4, getModelFor(p).getRelationshipMap().getEntries().size());
		// Hid:1:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.xxxx
		// Hid:2:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.y
		// Hid:3:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.AClass_new
		// Hid:4:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.y (declared on) =pr280380<f{AClass.java[AClass
		// Hid:5:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.AClass_new (declared on) =pr280380<f{AClass.java[AClass
		// Hid:6:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.xxxx (declared on) =pr280380<f{AClass.java[AClass
		printModel(p);
		assertNotNull(getModelFor(p).getRelationshipMap().get("=pr280380<g*AnAspect.aj'AnAspect,AClass.xxxx"));
		alter(p, "inc2");
		build(p);
		assertNoErrors(p);
		printModel(p);
		// On this build the relationship should have changed to include the fully qualified target
		assertEquals(4, getModelFor(p).getRelationshipMap().getEntries().size());
		assertNotNull(getModelFor(p).getRelationshipMap().get("=pr280380<g*AnAspect.aj'AnAspect,AClass.xxxx"));
		// Hid:1:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.xxxx
		// Hid:2:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.y
		// Hid:3:(targets=3) =pr280380<f{AClass.java[AClass (aspect declarations) =pr280380<g*AnAspect.aj}AnAspect)AClass.AClass_new
		// Hid:4:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.y (declared on) =pr280380<f{AClass.java[AClass
		// Hid:5:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.AClass_new (declared on) =pr280380<f{AClass.java[AClass
		// Hid:6:(targets=1) =pr280380<g*AnAspect.aj}AnAspect)AClass.xxxx (declared on) =pr280380<f{AClass.java[AClass
	}

	public void testFQItds_322039() throws Exception {
		String p = "pr322039";
		initialiseProject(p);
		build(p);
		printModel(p);
		IRelationshipMap irm = getModelFor(p).getRelationshipMap();
		List<IRelationship> rels = irm.get("=pr322039<p{Azpect.java'Azpect)q2.Code.something2");
		assertNotNull(rels);
	}

	public void testIncrementalCtorItdHandle_280383() throws Exception {
		String p = "pr280383";
		initialiseProject(p);
		build(p);
		printModel(p);
		IRelationshipMap irm = getModelFor(p).getRelationshipMap();
		List<IRelationship> rels = irm.get("=pr280383<f{AnAspect.java'AnAspect)f.AClass.f_AClass_new");
		assertNotNull(rels);
	}

	// public void testArraysGenerics() throws Exception {
	// String p = "pr283864";
	// initialiseProject(p);
	// build(p);
	// printModel(p);
	// // IRelationshipMap irm = getModelFor(p).getRelationshipMap();
	// // List rels = irm.get("=pr280383<f{AnAspect.java}AnAspect)f.AClass.f_AClass_new");
	// // assertNotNull(rels);
	// }

	public void testSimilarITDS() throws Exception {
		String p = "pr283657";
		initialiseProject(p);
		build(p);
		printModel(p);
		// Hid:1:(targets=1) =pr283657<{Aspect.java}Aspect)Target.foo (declared on) =pr283657<{Aspect.java[Target
		// Hid:2:(targets=1) =pr283657<{Aspect.java}Aspect)Target.foo!2 (declared on) =pr283657<{Aspect.java[Target
		// Hid:3:(targets=2) =pr283657<{Aspect.java[Target (aspect declarations) =pr283657<{Aspect.java}Aspect)Target.foo
		// Hid:4:(targets=2) =pr283657<{Aspect.java[Target (aspect declarations) =pr283657<{Aspect.java}Aspect)Target.foo!2
		IRelationshipMap irm = getModelFor(p).getRelationshipMap();
		List<IRelationship> rels = irm.get("=pr283657<{Aspect.java'Aspect,Target.foo");
		assertNotNull(rels);
		rels = irm.get("=pr283657<{Aspect.java'Aspect)Target.foo!2");
		assertNotNull(rels);
	}

	public void testIncrementalAnnotationMatched_276399() throws Exception {
		String p = "pr276399";
		initialiseProject(p);
		addSourceFolderForSourceFile(p, getProjectRelativePath(p, "src/X.aj"), "src");
		addSourceFolderForSourceFile(p, getProjectRelativePath(p, "src/C.java"), "src");
		build(p);
		IRelationshipMap irm = getModelFor(p).getRelationshipMap();
		IRelationship ir = irm.get("=pr276399/src<*X.aj'X&after").get(0);
		assertNotNull(ir);
		alter(p, "inc1");
		build(p);
		printModel(p);
		irm = getModelFor(p).getRelationshipMap();
		List<IRelationship> rels = irm.get("=pr276399/src<*X.aj'X&after"); // should be gone after the inc build
		assertNull(rels);
	}

	public void testHandleCountDecA_pr278255() throws Exception {
		String p = "pr278255";
		initialiseProject(p);
		build(p);
		if (AjdeInteractionTestbed.VERBOSE) {
			printModelAndRelationships(p);
		}
		IRelationshipMap irm = getModelFor(p).getRelationshipMap();
		List<IRelationship> l = irm.get("=pr278255<{A.java'X`declare \\@type");
		assertNotNull(l);
		IRelationship ir = l.get(0);
		assertNotNull(ir);
	}

	public void testIncrementalItdDefaultCtor() {
		String p = "pr275032";
		initialiseProject(p);
		build(p);
		assertEquals(0, getErrorMessages(p).size());
		alter(p, "inc1");
		build(p);
		// error is: inter-type declaration from X conflicts with existing member: void A.<init>()
		// List ms =
		getErrorMessages(p);
		assertEquals(4, getErrorMessages(p).size());
		// Why 4 errors? I believe the problem is:
		// 2 errors are reported when there is a clash - one against the aspect, one against the affected target type.
		// each of the two errors are recorded against the compilation result for the aspect and the target
		// So it comes out as 4 - but for now I am tempted to leave it because at least it shows there is a problem...
		assertTrue("Was:" + getErrorMessages(p).get(0), getErrorMessages(p).get(0).toString().contains("conflicts"));
	}

	public void testOutputLocationCallbacks2() {
		String p = "pr268827_ol_res";
		initialiseProject(p);
		Map<String,File> m = new HashMap<>();
		m.put("a.txt", new File(getFile(p, "src/a.txt")));
		configureResourceMap(p, m);
		CustomOLM olm = new CustomOLM(getProjectRelativePath(p, ".").toString());
		configureOutputLocationManager(p, olm);
		build(p);
		checkCompileWeaveCount(p, 2, 2);
		assertEquals(3, olm.writeCount);
		alter(p, "inc1"); // this contains a new B.java that doesn't have the aspect inside it
		build(p);
		checkCompileWeaveCount(p, 3, 1);
		assertEquals(1, olm.removeCount); // B.class removed
	}

	public void testOutputLocationCallbacks() {
		String p = "pr268827_ol";
		initialiseProject(p);
		CustomOLM olm = new CustomOLM(getProjectRelativePath(p, ".").toString());
		configureOutputLocationManager(p, olm);
		build(p);
		checkCompileWeaveCount(p, 2, 3);
		alter(p, "inc1"); // this contains a new Foo.java that no longer has Extra class in it
		build(p);
		checkCompileWeaveCount(p, 1, 1);
		assertEquals(1, olm.removeCount);
	}

	public void testOutputLocationCallbacksFileAdd() {
		String p = "pr268827_ol2";
		initialiseProject(p);
		CustomOLM olm = new CustomOLM(getProjectRelativePath(p, ".").toString());
		configureOutputLocationManager(p, olm);
		build(p);
		assertEquals(3, olm.writeCount);
		olm.writeCount = 0;
		checkCompileWeaveCount(p, 2, 3);
		alter(p, "inc1"); // this contains a new file Boo.java
		build(p);
		assertEquals(1, olm.writeCount);
		checkCompileWeaveCount(p, 1, 1);
		// assertEquals(1, olm.removeCount);
	}

	static class CustomOLM extends TestOutputLocationManager {

		public int writeCount = 0;
		public int removeCount = 0;

		public CustomOLM(String testProjectPath) {
			super(testProjectPath);
		}

		@Override
		public void reportFileWrite(String outputfile, int filetype) {
			super.reportFileWrite(outputfile, filetype);
			writeCount++;
			System.out.println("Written " + outputfile);
			// System.out.println("Written " + outputfile + " " + filetype);
		}

		@Override
		public void reportFileRemove(String outputfile, int filetype) {
			super.reportFileRemove(outputfile, filetype);
			removeCount++;
			System.out.println("Removed " + outputfile);
			// System.out.println("Removed " + outputfile + "  " + filetype);
		}

	}

	public void testBrokenCodeDeca_268611() {
		String p = "pr268611";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertTrue(((Message) getErrorMessages(p).get(0)).getMessage().contains("Syntax error on token \")\", \"name pattern\" expected"));
	}

	public void testIncrementalMixin() {
		String p = "mixin";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertEquals(0, getErrorMessages(p).size());
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(0, getErrorMessages(p).size());
	}

	public void testUnusedPrivates_pr266420() {
		String p = "pr266420";
		initialiseProject(p);

		Map<String,String> javaOptions = new Hashtable<>();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.source", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.problem.unusedPrivateMember", "warning");
		configureJavaOptionsMap(p, javaOptions);

		build(p);
		checkWasFullBuild();
		List<IMessage> warnings = getWarningMessages(p);
		assertEquals(0, warnings.size());
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		warnings = getWarningMessages(p);
		assertEquals(0, warnings.size());
	}

	public void testExtendingITDAspectOnClasspath_PR298704() throws Exception {
		String base = "pr298704_baseaspects";
		String test = "pr298704_testaspects";
		initialiseProject(base);
		initialiseProject(test);
		configureNewProjectDependency(test, base);

		build(base);
		build(test);
		checkWasFullBuild();
		assertNoErrors(test);
		IRelationshipMap irm = getModelFor(test).getRelationshipMap();
		assertEquals(7, irm.getEntries().size());
	}

	public void testPR265729() {
		String lib = "pr265729_lib";
		initialiseProject(lib);
		// addClasspathEntryChanged(lib, getProjectRelativePath(p1,
		// "bin").toString());
		build(lib);
		checkWasFullBuild();

		String cli = "pr265729_client";
		initialiseProject(cli);

		// addClasspathEntry(cli, new File("../lib/junit/junit.jar"));
		configureAspectPath(cli, getProjectRelativePath(lib, "bin"));
		build(cli);
		checkWasFullBuild();

		IProgramElement root = getModelFor(cli).getHierarchy().getRoot();

		// dumptree(root, 0);
		// PrintWriter pw = new PrintWriter(System.out);
		// try {
		// getModelFor(cli).dumprels(pw);
		// pw.flush();
		// } catch (Exception e) {
		// }
		IRelationshipMap irm = getModelFor(cli).getRelationshipMap();
		IRelationship ir = irm.get("=pr265729_client<be.cronos.aop{App.java[App").get(0);
		// This type should be affected by an ITD and a declare parents
		// could be either way round
		String h1 = ir.getTargets().get(0);
		String h2 = ir.getTargets().get(1);

		// For some ITD: public void I.g(String s) {}
		// Node in tree: I.g(java.lang.String) [inter-type method]
		// Handle: =pr265729_client<be.cronos.aop{App.java}X)I.g)QString;

		if (!h1.endsWith("parents")) {
			String h3 = h1;
			h1 = h2;
			h2 = h3;
		}
		// ITD from the test program:
		// public String InterTypeAspectInterface.foo(int i,List list,App a) {
		assertEquals("=pr265729_client/binaries<be.cronos.aop.aspects(InterTypeAspect.class'InterTypeAspect`declare parents", h1);
		assertEquals(
				"=pr265729_client/binaries<be.cronos.aop.aspects(InterTypeAspect.class'InterTypeAspect)InterTypeAspectInterface.foo)I)QList;)QSerializable;",
				h2);
		IProgramElement binaryDecp = getModelFor(cli).getHierarchy().getElement(h1);
		assertNotNull(binaryDecp);
		IProgramElement binaryITDM = getModelFor(cli).getHierarchy().getElement(h2);
		assertNotNull(binaryITDM);

		// @see AsmRelationshipProvider.createIntertypeDeclaredChild()
		List<char[]> ptypes = binaryITDM.getParameterTypes();
		assertEquals("int", new String(ptypes.get(0)));
		assertEquals("java.util.List", new String(ptypes.get(1)));
		assertEquals("java.io.Serializable", new String(ptypes.get(2)));

		// param names not set
		// List pnames = binaryITDM.getParameterNames();
		// assertEquals("i", new String((char[]) pnames.get(0)));
		// assertEquals("list", new String((char[]) pnames.get(1)));
		// assertEquals("b", new String((char[]) pnames.get(2)));

		assertEquals("java.lang.String", binaryITDM.getCorrespondingType(true));
	}

	public void testXmlConfiguredProject() {
		String p = "xmlone";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-showWeaveInfo");// -xmlConfigured");
		configureShowWeaveInfoMessages(p, true);
		addXmlConfigFile(p, getProjectRelativePath(p, "p/aop.xml").toString());
		build(p);
		checkWasFullBuild();
		List<IMessage> weaveMessages = getWeavingMessages(p);
		if (weaveMessages.size() != 1) {
			for (Object object : weaveMessages) {
				System.out.println(object);
			}
			fail("Expected just one weave message.  The aop.xml should have limited the weaving");
		}

	}

	public void testDeclareParentsInModel() {
		String p = "decps";
		initialiseProject(p);
		build(p);
		IProgramElement decp = getModelFor(p).getHierarchy().findElementForHandle("=decps<a{A.java'A`declare parents");
		List<String> ps = decp.getParentTypes();
		assertNotNull(ps);
		assertEquals(2, ps.size());
		int count = 0;
		for (String type : ps) {
			if (type.equals("java.io.Serializable")) {
				count++;
			}
			if (type.equals("a.Goo")) {
				count++;
			}
		}
		assertEquals("Should have found the two types in: " + ps, 2, count);
	}

	public void testConstructorAdvice_pr261380() throws Exception {
		String p = "261380";
		initialiseProject(p);
		build(p);
		IRelationshipMap irm = getModelFor(p).getRelationshipMap();
		IRelationship ir = irm.get("=261380<test{C.java'X&before").get(0);
		List<String> targets = ir.getTargets();
		assertEquals(1, targets.size());
		System.out.println(targets.get(0));
		String handle = targets.get(0);
		assertEquals("Expected the handle for the code node inside the constructor decl",
				"=261380<test{C.java[C~C?constructor-call(void test.C.<init>())", handle);
	}

	/*
	 * A.aj package pack; public aspect A { pointcut p() : call( C.method before() : p() { // line 7 } }
	 *
	 * C.java package pack; public class C { public void method1() { method2(); // line 6 } public void method2() { } public void
	 * method3() { method2(); // line 13 }
	 *
	 * }
	 */
	public void testDontLoseAdviceMarkers_pr134471() {
		try {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=false;
			initialiseProject("P4");
			build("P4");
			if (AjdeInteractionTestbed.VERBOSE) {
				Ajc.dumpAJDEStructureModel(getModelFor("P4"), "after full build where advice is applying");
			}
			// should be 4 relationship entries

			// In inc1 the first advised line is 'commented out'
			alter("P4", "inc1");
			build("P4");
			checkWasntFullBuild();
			if (AjdeInteractionTestbed.VERBOSE) {
				Ajc.dumpAJDEStructureModel(getModelFor("P4"), "after inc build where first advised line is gone");
			}
			// should now be 2 relationship entries

			// This will be the line 6 entry in C.java
			IProgramElement codeElement = findCode(checkForNode(getModelFor("P4"), "pack", "C", true));

			// This will be the line 7 entry in A.java
			IProgramElement advice = findAdvice(checkForNode(getModelFor("P4"), "pack", "A", true));

			IRelationshipMap asmRelMap = getModelFor("P4").getRelationshipMap();
			assertEquals("There should be two relationships in the relationship map", 2, asmRelMap.getEntries().size());

			for (String sourceOfRelationship : asmRelMap.getEntries()) {
				IProgramElement ipe = getModelFor("P4").getHierarchy().findElementForHandle(sourceOfRelationship);
				assertNotNull("expected to find IProgramElement with handle " + sourceOfRelationship + " but didn't", ipe);
				if (ipe.getKind().equals(Kind.ADVICE)) {
					assertEquals("expected source of relationship to be " + advice.toString() + " but found " + ipe.toString(),
							advice, ipe);
				} else if (ipe.getKind().equals(Kind.CODE)) {
					assertEquals(
							"expected source of relationship to be " + codeElement.toString() + " but found " + ipe.toString(),
							codeElement, ipe);
				} else {
					fail("found unexpected relationship source " + ipe + " with kind " + ipe.getKind()
							+ " when looking up handle: " + sourceOfRelationship);
				}
				List<IRelationship> relationships = asmRelMap.get(ipe);
				assertNotNull("expected " + ipe.getName() + " to have some " + "relationships", relationships);
				for (IRelationship relationship : relationships) {
					Relationship rel = (Relationship) relationship;
					List<String> targets = rel.getTargets();
					for (String t : targets) {
						IProgramElement link = getModelFor("P4").getHierarchy().findElementForHandle(t);
						if (ipe.getKind().equals(Kind.ADVICE)) {
							assertEquals(
									"expected target of relationship to be " + codeElement.toString() + " but found "
											+ link.toString(), codeElement, link);
						} else if (ipe.getKind().equals(Kind.CODE)) {
							assertEquals(
									"expected target of relationship to be " + advice.toString() + " but found " + link.toString(),
									advice, link);
						} else {
							fail("found unexpected relationship source " + ipe.getName() + " with kind " + ipe.getKind());
						}
					}
				}
			}

		} finally {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=true;
			// configureBuildStructureModel(false);
		}
	}

	public void testPr148285() {
		String p = "PR148285_2";
		initialiseProject(p); // Single source file A.aj defines A and C
		build(p);
		checkWasFullBuild();
		alter(p, "inc1"); // Second source introduced C.java, defines C
		build(p);
		checkWasntFullBuild();
		List<IMessage> msgs = getErrorMessages(p);
		assertEquals("error message should be 'The type C is already defined' ", "The type C is already defined",
				msgs.get(0).getMessage());
		alter("PR148285_2", "inc2"); // type C in A.aj is commented out
		build("PR148285_2");
		checkWasntFullBuild();
		msgs = getErrorMessages(p);
		assertTrue("There should be no errors reported:\n" + getErrorMessages(p), msgs.isEmpty());
	}

	public void testIncrementalAndAnnotations() {
		initialiseProject("Annos");
		build("Annos");
		checkWasFullBuild();
		checkCompileWeaveCount("Annos", 4, 4);
		AsmManager model = getModelFor("Annos");
		assertEquals("Should be 3 relationships ", 3, model.getRelationshipMap().getEntries().size());

		alter("Annos", "inc1"); // Comment out the annotation on Parent
		build("Annos");
		checkWasntFullBuild();
		assertEquals("Should be no relationships ", 0, model.getRelationshipMap().getEntries().size());
		checkCompileWeaveCount("Annos", 3, 3);

		alter("Annos", "inc2"); // Add the annotation back onto Parent
		build("Annos");
		checkWasntFullBuild();
		assertEquals("Should be 3 relationships ", 3, model.getRelationshipMap().getEntries().size());
		checkCompileWeaveCount("Annos", 3, 3);
	}

	// package a.b.c;
	//
	// public class A {
	// }
	//
	// aspect X {
	// B A.foo(C c) { return null; }
	// declare parents: A implements java.io.Serializable;
	// }
	//
	// class B {}
	// class C {}
	public void testITDFQNames_pr252702() {
		String p = "itdfq";
		initialiseProject(p);
		build(p);
		AsmManager model = getModelFor(p);
		dumptree(model.getHierarchy().getRoot(), 0);
		IProgramElement root = model.getHierarchy().getRoot();
		ProgramElement theITD = (ProgramElement) findElementAtLine(root, 7);
		Map<String, Object> m = theITD.kvpairs;
		for (String type : m.keySet()) {
			System.out.println(type + " = " + m.get(type));
		}
		// return type of the ITD
		assertEquals("a.b.c.B", theITD.getCorrespondingType(true));
		List<char[]> ptypes = theITD.getParameterTypes();
		for (char[] object : ptypes) {
			System.out.println("p = " + new String(object));
		}
		ProgramElement decp = (ProgramElement) findElementAtLine(root, 8);
		m = decp.kvpairs;
		for (String type : m.keySet()) {
			System.out.println(type + " = " + m.get(type));
		}
		List<String> l = decp.getParentTypes();
		assertEquals("java.io.Serializable", l.get(0));
		ProgramElement ctorDecp = (ProgramElement) findElementAtLine(root, 16);
		String ctordecphandle = ctorDecp.getHandleIdentifier();
		assertEquals("=itdfq<a.b.c{A.java'XX)B.B_new)QString;", ctordecphandle); // 252702
		// ,
		// comment
		// 7
	}

	public void testBrokenHandles_pr247742() {
		String p = "BrokenHandles";
		initialiseProject(p);
		// alter(p, "inc1");
		build(p);
		// alter(p, "inc2");
		// build(p);
		AsmManager model = getModelFor(p);
		dumptree(model.getHierarchy().getRoot(), 0);

		IProgramElement root = model.getHierarchy().getRoot();
		IProgramElement ipe = findElementAtLine(root, 4);
		assertEquals("=BrokenHandles<p{GetInfo.java'GetInfo`declare warning", ipe.getHandleIdentifier());
		ipe = findElementAtLine(root, 5);
		assertEquals("=BrokenHandles<p{GetInfo.java'GetInfo`declare warning!2", ipe.getHandleIdentifier());
		ipe = findElementAtLine(root, 6);
		assertEquals("=BrokenHandles<p{GetInfo.java'GetInfo`declare parents", ipe.getHandleIdentifier());
	}

	public void testNPEIncremental_pr262218() {
		String p = "pr262218";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		List<String> l = getCompilerErrorMessages(p);
		assertEquals("Unexpected compiler error", 0, l.size());
	}

	public void testDeclareAnnotationNPE_298504() {
		String p = "pr298504";
		initialiseProject(p);
		build(p);
		List<IMessage> l = getErrorMessages(p);
		assertTrue(l.toString().contains("ManagedResource cannot be resolved to a type"));
		// checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		// checkWasntFullBuild();
		List<String> compilerErrors = getCompilerErrorMessages(p);
		assertTrue(!compilerErrors.toString().contains("NullPointerException"));
		l = getErrorMessages(p);
		assertTrue(l.toString().contains("ManagedResource cannot be resolved to a type"));
	}

	public void testIncrementalAnnoStyle_pr286341() {
		String base = "pr286341_base";
		initialiseProject(base);
		build(base);
		checkWasFullBuild();
		String p = "pr286341";
		initialiseProject(p);
		configureAspectPath(p, getProjectRelativePath(base, "bin"));
		addClasspathEntry(p, getProjectRelativePath(base, "bin"));
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertNoErrors(p);
	}

	public void testImports_pr263487() {
		String p2 = "importProb2";
		initialiseProject(p2);
		build(p2);
		checkWasFullBuild();

		String p = "importProb";
		initialiseProject(p);
		build(p);
		configureAspectPath(p, getProjectRelativePath(p2, "bin"));
		checkWasFullBuild();
		build(p);
		build(p);
		build(p);
		alter(p, "inc1");
		addProjectSourceFileChanged(p, getProjectRelativePath(p, "src/p/Code.java"));
		// addProjectSourceFileChanged(p, getProjectRelativePath(p,
		// "src/q/Asp.java"));
		build(p);
		checkWasntFullBuild();
		List<String> l = getCompilerErrorMessages(p);
		assertEquals("Unexpected compiler error", 0, l.size());
	}

	public void testBuildingBrokenCode_pr263323() {
		String p = "brokenCode";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		alter(p, "inc1"); // break the aspect
		build(p);
		checkWasntFullBuild();
		alter(p, "inc2"); // whitespace change on affected file
		build(p);
		checkWasntFullBuild();
		List<String> l = getCompilerErrorMessages(p);
		assertEquals("Unexpected compiler error", 0, l.size());
	}

	/*
	 * public void testNPEGenericCtor_pr260944() { AjdeInteractionTestbed.VERBOSE = true; String p = "pr260944";
	 * initialiseProject(p); build(p); checkWasFullBuild(); alter(p, "inc1"); build(p); checkWasntFullBuild(); List l =
	 * getCompilerErrorMessages(p); assertEquals("Unexpected compiler error", 0, l.size()); }
	 */

	public void testItdProb() {
		String p = "itdprob";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		List<String> l = getCompilerErrorMessages(p);
		assertEquals("Unexpected compiler error", 0, l.size());
	}

	/*
	 * public void testGenericITD_pr262257() throws IOException { String p = "pr262257"; initialiseProject(p); build(p);
	 * checkWasFullBuild();
	 *
	 * dumptree(getModelFor(p).getHierarchy().getRoot(), 0); PrintWriter pw = new PrintWriter(System.out);
	 * getModelFor(p).dumprels(pw); pw.flush(); }
	 */
	public void testAnnotations_pr262154() {
		String p = "pr262154";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		List<String> l = getCompilerErrorMessages(p);
		assertEquals("Unexpected compiler error", 0, l.size());
	}

	public void testAnnotations_pr255555() {
		String p = "pr255555";
		initialiseProject(p);
		build(p);
		checkCompileWeaveCount(p, 2, 1);
	}

	public void testSpacewarHandles() {
		// String p = "SpaceWar";
		String p = "Simpler";
		initialiseProject(p);
		build(p);
		dumptree(getModelFor(p).getHierarchy().getRoot(), 0);
		// incomplete
	}

	/**
	 * Test what is in the model for package declarations and import statements. Package Declaration nodes are new in AspectJ 1.6.4.
	 * Import statements are contained with an 'import references' node.
	 */
	public void testImportHandles() {
		String p = "Imports";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=false");
		build(p);

		IProgramElement root = getModelFor(p).getHierarchy().getRoot();

		// Looking for 'package p.q'
		IProgramElement ipe = findFile(root, "Example.aj");// findElementAtLine(root, 1);
		ipe = ipe.getChildren().get(0); // package decl is first entry in the type
		assertEquals(IProgramElement.Kind.PACKAGE_DECLARATION, ipe.getKind());
		assertEquals("package p.q;", ipe.getSourceSignature());
		assertEquals("=Imports<p.q*Example.aj%p.q", ipe.getHandleIdentifier());
		assertEquals(ipe.getSourceLocation().getOffset(), 8); // "package p.q" - location of p.q

		// Looking for import containing containing string and integer
		ipe = findElementAtLine(root, 3); // first import
		ipe = ipe.getParent(); // imports container
		assertEquals("=Imports<p.q*Example.aj#", ipe.getHandleIdentifier());
	}

	public void testAdvisingCallJoinpointsInITDS_pr253067() {
		String p = "pr253067";
		initialiseProject(p);
		build(p);
		// Check for a code node at line 5 - if there is one then we created it
		// correctly when building
		// the advice relationship
		IProgramElement root = getModelFor(p).getHierarchy().getRoot();
		IProgramElement code = findElementAtLine(root, 5);
		assertEquals("=pr253067<aa*AdvisesC.aj'AdvisesC)C.nothing?method-call(int aa.C.nothing())", code.getHandleIdentifier());
		// dumptree(getModelFor(p).getHierarchy().getRoot(), 0);
		// Ajc.dumpAJDEStructureModel(getModelFor("pr253067"),
		// "after inc build where first advised line is gone");
	}

	public void testHandles_DeclareAnno_pr249216_c9() {
		String p = "pr249216";
		initialiseProject(p);
		build(p);
		IProgramElement root = getModelFor(p).getHierarchy().getRoot();
		IProgramElement code = findElementAtLine(root, 4);
		// the @ should be escapified
		assertEquals("=pr249216<{Deca.java'X`declare \\@type", code.getHandleIdentifier());
		// dumptree(getModelFor(p).getHierarchy().getRoot(), 0);
		// Ajc.dumpAJDEStructureModel(getModelFor(p),
		// "after inc build where first advised line is gone");
	}

	public void testNullDelegateBrokenCode_pr251940() {
		String p = "pr251940";
		initialiseProject(p);
		build(p);
		checkForError(p, "The type F must implement the inherited");
	}

	public void testBeanExample() throws Exception {
		String p = "BeanExample";
		initialiseProject(p);
		build(p);
		dumptree(getModelFor(p).getHierarchy().getRoot(), 0);
		PrintWriter pw = new PrintWriter(System.out);
		getModelFor(p).dumprels(pw);
		pw.flush();
		// incomplete
	}

	// private void checkIfContainsFile(Set s, String filename, boolean shouldBeFound) {
	// StringBuffer sb = new StringBuffer("Set of files\n");
	// for (Iterator iterator = s.iterator(); iterator.hasNext();) {
	// Object object = iterator.next();
	// sb.append(object).append("\n");
	// }
	// for (Iterator iterator = s.iterator(); iterator.hasNext();) {
	// File fname = (File) iterator.next();
	// if (fname.getName().endsWith(filename)) {
	// if (!shouldBeFound) {
	// System.out.println(sb.toString());
	// fail("Unexpectedly found file " + filename);
	// } else {
	// return;
	// }
	// }
	// }
	// if (shouldBeFound) {
	// System.out.println(sb.toString());
	// fail("Did not find filename " + filename);
	// }
	// }

	// /**
	// * Checking return values of the AsmManager API calls that can be invoked
	// post incremental build that tell the caller which
	// * files had their relationships altered. As well as the affected (woven)
	// files, it is possible to query the aspects that wove
	// * those files.
	// */
	// public void testChangesOnBuild() throws Exception {
	// String p = "ChangesOnBuild";
	// initialiseProject(p);
	// build(p);
	// // Not incremental
	// checkIfContainsFile(AsmManager.getDefault().getModelChangesOnLastBuild(),
	// "A.java", false);
	// alter(p, "inc1");
	// build(p);
	// // Incremental
	// checkIfContainsFile(AsmManager.getDefault().getModelChangesOnLastBuild(),
	// "A.java", true);
	// checkIfContainsFile(AsmManager.getDefault().
	// getAspectsWeavingFilesOnLastBuild(), "X.java", true);
	// checkIfContainsFile(AsmManager.getDefault().
	// getAspectsWeavingFilesOnLastBuild(), "Y.java", false);
	// }

	public void testITDIncremental_pr192877() {
		String p = "PR192877";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
	}

	public void testIncrementalBuildsWithItds_pr259528() {
		String p = "pr259528";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
	}

	public void testAdviceHandlesAreJDTCompatible() {
		String p = "AdviceHandles";
		initialiseProject(p);
		addSourceFolderForSourceFile(p, getProjectRelativePath(p, "src/Handles.aj"), "src");
		build(p);
		IProgramElement root = getModelFor(p).getHierarchy().getRoot();
		IProgramElement typeDecl = findElementAtLine(root, 4);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles", typeDecl.getHandleIdentifier());

		IProgramElement advice1 = findElementAtLine(root, 7);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles&before", advice1.getHandleIdentifier());

		IProgramElement advice2 = findElementAtLine(root, 11);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles&before!2", advice2.getHandleIdentifier());

		IProgramElement advice3 = findElementAtLine(root, 15);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles&before&I", advice3.getHandleIdentifier());

		IProgramElement advice4 = findElementAtLine(root, 20);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles&before&I!2", advice4.getHandleIdentifier());

		IProgramElement advice5 = findElementAtLine(root, 25);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles&after", advice5.getHandleIdentifier());

		IProgramElement advice6 = findElementAtLine(root, 30);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles&afterReturning", advice6.getHandleIdentifier());

		IProgramElement advice7 = findElementAtLine(root, 35);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles&afterThrowing", advice7.getHandleIdentifier());

		IProgramElement advice8 = findElementAtLine(root, 40);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles&afterThrowing&I", advice8.getHandleIdentifier());

		IProgramElement namedInnerClass = findElementAtLine(root, 46);
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles~x[NamedClass", namedInnerClass.getHandleIdentifier());

		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles~foo[", findElementAtLine(root, 55).getHandleIdentifier());
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles~foo[!2", findElementAtLine(root, 56).getHandleIdentifier());

		// From 247742: comment 3: two anon class declarations
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles~b~QString;[", findElementAtLine(root, 62)
				.getHandleIdentifier());
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles~b~QString;[!2", findElementAtLine(root, 63)
				.getHandleIdentifier());

		// From 247742: comment 6: two diff anon class declarations
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles~c~QString;[", findElementAtLine(root, 66)
				.getHandleIdentifier());
		assertEquals("=AdviceHandles/src<spacewar*Handles.aj'Handles~c~QString;[!2", findElementAtLine(root, 67)
				.getHandleIdentifier());

		// // From 247742: comment 4
		// assertEquals(
		// "=AdviceHandles/src<spacewar*Handles.aj}Foo&afterReturning&QString;",
		// findElementAtLine(root,
		// 72).getHandleIdentifier());
		// assertEquals(
		// "=AdviceHandles/src<spacewar*Handles.aj}Foo&afterReturning&QString;!2"
		// , findElementAtLine(root,
		// 73).getHandleIdentifier());

	}

	// Testing code handles - should they included positional information? seems
	// to be what AJDT wants but we
	// only have the declaration start position in the programelement
	// public void testHandlesForCodeElements() {
	// String p = "CodeHandles";
	// initialiseProject(p);
	// addSourceFolderForSourceFile(p, getProjectRelativePath(p,
	// "src/Handles.aj"), "src");
	// build(p);
	// IProgramElement root = AsmManager.getDefault().getHierarchy().getRoot();
	// IProgramElement typeDecl = findElementAtLine(root, 3);
	// assertEquals("=CodeHandles/src<spacewar*Handles.aj[C",
	// typeDecl.getHandleIdentifier());
	//
	// IProgramElement code = findElementAtLine(root, 6);
	// assertEquals(
	// "=CodeHandles/src<spacewar*Handles.aj[C~m?method-call(void spacewar.C.foo(int))"
	// , code.getHandleIdentifier());
	// code = findElementAtLine(root, 7);
	// assertEquals(
	// "=CodeHandles/src<spacewar*Handles.aj[C~m?method-call(void spacewar.C.foo(int))!2"
	// , code.getHandleIdentifier());
	//
	// }

	private IProgramElement findFile(IProgramElement whereToLook, String filesubstring) {
		if (whereToLook.getSourceLocation() != null && whereToLook.getKind().isSourceFile()
				&& whereToLook.getSourceLocation().getSourceFile().toString().contains(filesubstring)) {
			return whereToLook;
		}
		for (IProgramElement element : whereToLook.getChildren()) {
			Kind k = element.getKind();
			ISourceLocation sloc = element.getSourceLocation();
			if (sloc != null && k.isSourceFile() && sloc.getSourceFile().toString().contains(filesubstring)) {
				return element;
			}
			if (k.isSourceFile()) {
				continue; // no need to look further down
			}
			IProgramElement gotSomething = findFile(element, filesubstring);
			if (gotSomething != null) {
				return gotSomething;
			}
		}
		return null;
	}

	private IProgramElement findElementAtLine(IProgramElement whereToLook, int line) {
		if (whereToLook == null) {
			return null;
		}
		if (whereToLook.getSourceLocation() != null && whereToLook.getSourceLocation().getLine() == line) {
			return whereToLook;
		}
		for (IProgramElement object : whereToLook.getChildren()) {
			if (object.getSourceLocation() != null && object.getSourceLocation().getLine() == line) {
				return object;
			}
			IProgramElement gotSomething = findElementAtLine(object, line);
			if (gotSomething != null) {
				return gotSomething;
			}
		}
		return null;
	}

	public void testModelWithMultipleSourceFolders() {
		initialiseProject("MultiSource");
		// File sourceFolderOne = getProjectRelativePath("MultiSource", "src1");
		// File sourceFolderTwo = getProjectRelativePath("MultiSource", "src2");
		// File sourceFolderThree = getProjectRelativePath("MultiSource",
		// "src3");
		// src1 source folder slashed as per 264563
		addSourceFolderForSourceFile("MultiSource", getProjectRelativePath("MultiSource", "src1/CodeOne.java"), "src1/");
		addSourceFolderForSourceFile("MultiSource", getProjectRelativePath("MultiSource", "src2/CodeTwo.java"), "src2");
		addSourceFolderForSourceFile("MultiSource", getProjectRelativePath("MultiSource", "src3/pkg/CodeThree.java"), "src3");
		build("MultiSource");
		IProgramElement srcOne = getModelFor("MultiSource").getHierarchy().findElementForHandle("=MultiSource/src1");
		IProgramElement CodeOneClass = getModelFor("MultiSource").getHierarchy().findElementForHandle(
				"=MultiSource/src1{CodeOne.java[CodeOne");
		IProgramElement srcTwoPackage = getModelFor("MultiSource").getHierarchy().findElementForHandle("=MultiSource/src2<pkg");
		IProgramElement srcThreePackage = getModelFor("MultiSource").getHierarchy().findElementForHandle("=MultiSource/src3<pkg");
		assertNotNull(srcOne);
		assertNotNull(CodeOneClass);
		assertNotNull(srcTwoPackage);
		assertNotNull(srcThreePackage);
		if (srcTwoPackage.equals(srcThreePackage)) {
			throw new RuntimeException(
					"Should not have found these package nodes to be the same, they are in different source folders");
		}
		// dumptree(AsmManager.getDefault().getHierarchy().getRoot(), 0);
	}

	// Now the source folders are more complex 'src/java/main' and
	// 'src/java/tests'
	public void testModelWithMultipleSourceFolders2() {
		initialiseProject("MultiSource");
		// File sourceFolderOne = getProjectRelativePath("MultiSource",
		// "src/java/main");
		// File sourceFolderTwo = getProjectRelativePath("MultiSource", "src2");
		// File sourceFolderThree = getProjectRelativePath("MultiSource",
		// "src3");
		addSourceFolderForSourceFile("MultiSource", getProjectRelativePath("MultiSource", "src1/CodeOne.java"), "src/java/main");
		addSourceFolderForSourceFile("MultiSource", getProjectRelativePath("MultiSource", "src2/CodeTwo.java"), "src/java/main");
		addSourceFolderForSourceFile("MultiSource", getProjectRelativePath("MultiSource", "src3/pkg/CodeThree.java"),
				"src/java/tests");
		build("MultiSource");

		IProgramElement srcOne = getModelFor("MultiSource").getHierarchy().findElementForHandleOrCreate(
				"=MultiSource/src\\/java\\/main", false);
		IProgramElement CodeOneClass = getModelFor("MultiSource").getHierarchy().findElementForHandle(
				"=MultiSource/src\\/java\\/main{CodeOne.java[CodeOne");
		IProgramElement srcTwoPackage = getModelFor("MultiSource").getHierarchy().findElementForHandle(
				"=MultiSource/src\\/java\\/tests<pkg");
		IProgramElement srcThreePackage = getModelFor("MultiSource").getHierarchy().findElementForHandle(
				"=MultiSource/src\\/java\\/testssrc3<pkg");
		assertNotNull(srcOne);
		assertNotNull(CodeOneClass);
		assertNotNull(srcTwoPackage);
		assertNotNull(srcThreePackage);
		if (srcTwoPackage.equals(srcThreePackage)) {
			throw new RuntimeException(
					"Should not have found these package nodes to be the same, they are in different source folders");
		}
		// dumptree(AsmManager.getDefault().getHierarchy().getRoot(), 0);
	}

	public void testIncrementalItdsWithMultipleAspects_pr173729() {
		initialiseProject("PR173729");
		build("PR173729");
		checkWasFullBuild();
		alter("PR173729", "inc1");
		build("PR173729");
		checkWasntFullBuild();
	}

	// Compile a single simple project
	public void testTheBasics() {
		initialiseProject("P1");
		build("P1"); // This first build will be batch
		build("P1");
		checkWasntFullBuild();
		checkCompileWeaveCount("P1", 0, 0);
	}

	// source code doesnt matter, we are checking invalid path handling
	public void testInvalidAspectpath_pr121395() {
		initialiseProject("P1");
		File f = new File("foo.jar");
		Set<File> s = new HashSet<>();
		s.add(f);
		configureAspectPath("P1", s);
		build("P1"); // This first build will be batch
		checkForError("P1", "invalid aspectpath entry");
	}

	// incorrect use of '?' when it should be '*'
	public void testAspectPath_pr242797_c46() {
		String bug = "pr242797_1";
		String bug2 = "pr242797_2";
		initialiseProject(bug);
		initialiseProject(bug2);
		configureAspectPath(bug2, getProjectRelativePath(bug, "bin"));
		build(bug);
		build(bug2);
	}

	public void testAspectPath_pr247742_c16() throws IOException {
		String bug = "AspectPathOne";
		String bug2 = "AspectPathTwo";
		addSourceFolderForSourceFile(bug2, getProjectRelativePath(bug2, "src/C.java"), "src");
		initialiseProject(bug);
		initialiseProject(bug2);
		configureAspectPath(bug2, getProjectRelativePath(bug, "bin"));
		build(bug);
		build(bug2);
		dumptree(getModelFor(bug2).getHierarchy().getRoot(), 0);
		PrintWriter pw = new PrintWriter(System.out);
		getModelFor(bug2).dumprels(pw);
		pw.flush();
		IProgramElement root = getModelFor(bug2).getHierarchy().getRoot();
		assertEquals("=AspectPathTwo/binaries<pkg(Asp.class'Asp&before", findElementAtLine(root, 5).getHandleIdentifier());
		assertEquals("=AspectPathTwo/binaries<(Asp2.class'Asp2&before", findElementAtLine(root, 16).getHandleIdentifier());
	}

	public void testAspectPath_pr274558() throws Exception {
		String base = "bug274558depending";
		String depending = "bug274558base";
		// addSourceFolderForSourceFile(bug2, getProjectRelativePath(bug2, "src/C.java"), "src");
		initialiseProject(base);
		initialiseProject(depending);
		configureAspectPath(depending, getProjectRelativePath(base, "bin"));
		build(base);
		build(depending);
		printModel(depending);
		IProgramElement root = getModelFor(depending).getHierarchy().getRoot();
		assertEquals("=bug274558base/binaries<r(DeclaresITD.class'DeclaresITD,InterfaceForITD.x", findElementAtLine(root, 5)
				.getHandleIdentifier());
		// assertEquals("=AspectPathTwo/binaries<(Asp2.class}Asp2&before", findElementAtLine(root, 16).getHandleIdentifier());
	}

	public void testAspectPath_pr265693() throws IOException {
		String bug = "AspectPath3";
		String bug2 = "AspectPath4";
		addSourceFolderForSourceFile(bug2, getProjectRelativePath(bug2, "src/C.java"), "src");
		initialiseProject(bug);
		initialiseProject(bug2);
		configureAspectPath(bug2, getProjectRelativePath(bug, "bin"));
		build(bug);
		build(bug2);
		// dumptree(getModelFor(bug2).getHierarchy().getRoot(), 0);
		// PrintWriter pw = new PrintWriter(System.out);
		// getModelFor(bug2).dumprels(pw);
		// pw.flush();
		IProgramElement root = getModelFor(bug2).getHierarchy().getRoot();
		IProgramElement binariesNode = getChild(root, "binaries");
		assertNotNull(binariesNode);
		IProgramElement packageNode = binariesNode.getChildren().get(0);
		assertEquals("a.b.c", packageNode.getName());
		IProgramElement fileNode = packageNode.getChildren().get(0);
		assertEquals(IProgramElement.Kind.FILE, fileNode.getKind());
	}

	private IProgramElement getChild(IProgramElement start, String name) {
		if (start.getName().equals(name)) {
			return start;
		}
		List<IProgramElement> kids = start.getChildren();
		if (kids != null) {
			for (IProgramElement kid : kids) {
				IProgramElement found = getChild(kid, name);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	public void testHandleQualification_pr265993() throws IOException {
		String p = "pr265993";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=false");
		build(p);
		IProgramElement root = getModelFor(p).getHierarchy().getRoot();
		// dumptree(getModelFor(p).getHierarchy().getRoot(), 0);
		// PrintWriter pw = new PrintWriter(System.out);
		// getModelFor(p).dumprels(pw);
		// pw.flush();
		assertEquals("=pr265993<{A.java[A~m~QString;~Qjava.lang.String;", findElementAtLine(root, 3).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m2~QList;", findElementAtLine(root, 5).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m3~Qjava.util.ArrayList;", findElementAtLine(root, 6).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m4~QMap\\<Qjava.lang.String;QList;>;", findElementAtLine(root, 8).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m5~Qjava.util.Map\\<Qjava.lang.String;QList;>;", findElementAtLine(root, 9)
				.getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m6~QMap\\<\\[IQList;>;", findElementAtLine(root, 10).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m7~\\[I", findElementAtLine(root, 11).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m8~\\[Qjava.lang.String;", findElementAtLine(root, 12).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m9~\\[QString;", findElementAtLine(root, 13).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m10~\\[\\[QList\\<QString;>;", findElementAtLine(root, 14).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m11~Qjava.util.List\\<QT;>;", findElementAtLine(root, 15).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m12~\\[QT;", findElementAtLine(root, 16).getHandleIdentifier());
		assertEquals("=pr265993<{A.java[A~m13~QClass\\<QT;>;~QObject;~QString;", findElementAtLine(root, 17).getHandleIdentifier());
	}

	public void testHandlesForAnnotationStyle_pr269286() throws IOException {
		String p = "pr269286";
		initialiseProject(p);
		build(p);
		IProgramElement root = getModelFor(p).getHierarchy().getRoot();
		dumptree(getModelFor(p).getHierarchy().getRoot(), 0);
		PrintWriter pw = new PrintWriter(System.out);
		getModelFor(p).dumprels(pw);
		pw.flush();
		assertEquals("=pr269286<{Logger.java[Logger", findElementAtLine(root, 4).getHandleIdentifier()); // type
		assertEquals("=pr269286<{Logger.java[Logger~boo", findElementAtLine(root, 7).getHandleIdentifier()); // before
		assertEquals("=pr269286<{Logger.java[Logger~aoo", findElementAtLine(root, 11).getHandleIdentifier()); // after
		assertEquals("=pr269286<{Logger.java[Logger~aroo", findElementAtLine(root, 15).getHandleIdentifier()); // around

		// pointcuts are not fixed - seems to buggy handling of them internally
		assertEquals("=pr269286<{Logger.java[Logger\"ooo", findElementAtLine(root, 20).getHandleIdentifier());

		// DeclareWarning
		assertEquals("=pr269286<{Logger.java[Logger^message", findElementAtLine(root, 24).getHandleIdentifier());

		// DeclareError
		assertEquals("=pr269286<{Logger.java[Logger^message2", findElementAtLine(root, 27).getHandleIdentifier());
	}

	public void testHandleCountersForAdvice() throws IOException {
		String p = "prx";
		initialiseProject(p);
		build(p);
		// System.out.println("Handle Counters For Advice Output");
		IProgramElement root = getModelFor(p).getHierarchy().getRoot();
		// dumptree(getModelFor(p).getHierarchy().getRoot(), 0);
		// PrintWriter pw = new PrintWriter(System.out);
		// getModelFor(p).dumprels(pw);
		// pw.flush();
		IProgramElement ff = findFile(root, "ProcessAspect.aj");
		assertEquals("=prx<com.kronos.aspects*ProcessAspect.aj'ProcessAspect&after&QMyProcessor;", findElementAtLine(root, 22)
				.getHandleIdentifier());
		assertEquals("=prx<com.kronos.aspects*ProcessAspect.aj'ProcessAspect&after&QMyProcessor;!2", findElementAtLine(root, 68)
				.getHandleIdentifier());
	}

	/**
	 * A change is made to an aspect on the aspectpath (staticinitialization() advice is added) for another project.
	 * <p>
	 * Managing the aspectpath is hard. We want to do a minimal build of this project which means recognizing what kind of changes
	 * have occurred on the aspectpath. Was it a regular class or an aspect? Was it a structural change to that aspect?
	 * <p>
	 * The filenames for .class files created that contain aspects is stored in the AjState.aspectClassFiles field. When a change is
	 * detected we can see who was managing the location where the change occurred and ask them if the .class file contained an
	 * aspect. Right now a change detected like this will cause a full build. We might improve the detection logic here but it isn't
	 * trivial:
	 * <ul>
	 * <li>Around advice is inlined. Changing the body of an around advice would not normally be thought of as a structural change
	 * (as it does not change the signature of the class) but due to inlining it is a change we would need to pay attention to as it
	 * will affect types previously woven with that advice.
	 * <li>Annotation style aspects include pointcuts in strings. Changes to these are considered non-structural but clearly they do
	 * affect what might be woven.
	 * </ul>
	 */
	public void testAspectPath_pr249212_c1() throws IOException {
		String p1 = "AspectPathOne";
		String p2 = "AspectPathTwo";
		addSourceFolderForSourceFile(p2, getProjectRelativePath(p2, "src/C.java"), "src");
		initialiseProject(p1);
		initialiseProject(p2);
		configureAspectPath(p2, getProjectRelativePath(p1, "bin"));
		build(p1);
		build(p2);

		alter(p1, "inc1");
		build(p1); // Modify the aspect Asp2 to include staticinitialization()
		// advice
		checkWasFullBuild();
		Set<File> s = getModelFor(p1).getModelChangesOnLastBuild();
		assertTrue("Should be empty as was full build:" + s, s.isEmpty());

		// prod the build of the second project with some extra info to tell it
		// more precisely about the change:
		addClasspathEntryChanged(p2, getProjectRelativePath(p1, "bin").toString());
		configureAspectPath(p2, getProjectRelativePath(p1, "bin"));
		build(p2);
		checkWasFullBuild();

		// dumptree(AsmManager.getDefault().getHierarchy().getRoot(), 0);
		// PrintWriter pw = new PrintWriter(System.out);
		// AsmManager.getDefault().dumprels(pw);
		// pw.flush();

		// Not incremental
		assertTrue("Should be empty as was full build:" + s, s.isEmpty());
		// Set s = AsmManager.getDefault().getModelChangesOnLastBuild();
		// checkIfContainsFile(AsmManager.getDefault().getModelChangesOnLastBuild
		// (), "C.java", true);
	}

	// public void testAspectPath_pr242797_c41() {
	// String bug = "pr242797_3";
	// String bug2 = "pr242797_4";
	// initialiseProject(bug);
	// initialiseProject(bug2);
	// configureAspectPath(bug2, getProjectRelativePath(bug, "bin"));
	// build(bug);
	// build(bug2);
	// }

	/**
	 * Build a project containing a resource - then mark the resource readOnly(), then do an inc-compile, it will report an error
	 * about write access to the resource in the output folder being denied
	 */
	/*
	 * public void testProblemCopyingResources_pr138171() { initialiseProject("PR138171");
	 *
	 * File f=getProjectRelativePath("PR138171","res.txt"); Map m = new HashMap(); m.put("res.txt",f);
	 * AjdeInteractionTestbed.MyProjectPropertiesAdapter .getInstance().setSourcePathResources(m); build("PR138171"); File f2 =
	 * getProjectOutputRelativePath("PR138171","res.txt"); boolean successful = f2.setReadOnly();
	 *
	 * alter("PR138171","inc1"); AjdeInteractionTestbed.MyProjectPropertiesAdapter .getInstance().setSourcePathResources(m);
	 * build("PR138171"); List msgs = MyTaskListManager.getErrorMessages(); assertTrue("there should be one message but there are "
	 * +(msgs==null?0:msgs.size())+":\n"+msgs,msgs!=null && msgs.size()==1); IMessage msg = (IMessage)msgs.get(0); String exp =
	 * "unable to copy resource to output folder: 'res.txt'"; assertTrue("Expected message to include this text ["
	 * +exp+"] but it does not: "+msg,msg.toString().indexOf(exp)!=-1); }
	 */

	// Make simple changes to a project, adding a class
	public void testSimpleChanges() {
		initialiseProject("P1");
		build("P1"); // This first build will be batch
		alter("P1", "inc1"); // adds a single class
		build("P1");
		checkCompileWeaveCount("P1", 1, -1);
		build("P1");
		checkCompileWeaveCount("P1", 0, -1);
	}

	// Make simple changes to a project, adding a class and an aspect
	public void testAddingAnAspect() {
		initialiseProject("P1");
		build("P1"); // build 1, weave 1
		alter("P1", "inc1"); // adds a class
		alter("P1", "inc2"); // adds an aspect
		build("P1"); // build 1,
		long timeTakenForFullBuildAndWeave = getTimeTakenForBuild("P1");
		checkWasFullBuild(); // it *will* be a full build under the new
		// "back-to-the-source strategy
		checkCompileWeaveCount("P1", 5, 3); // we compile X and A (the delta)
		// find out that
		// an aspect has changed, go back to the source
		// and compile X,A,C, then weave the all.
		build("P1");
		long timeTakenForSimpleIncBuild = getTimeTakenForBuild("P1");
		// I don't think this test will have timing issues as the times should
		// be *RADICALLY* different
		// On my config, first build time is 2093ms and the second is 30ms
		assertTrue("Should not take longer for the trivial incremental build!  first=" + timeTakenForFullBuildAndWeave
				+ "ms  second=" + timeTakenForSimpleIncBuild + "ms", timeTakenForSimpleIncBuild < timeTakenForFullBuildAndWeave);
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

	public void testBuildingBrokenCode_pr240360() {
		initialiseProject("pr240360");
		// configureNonStandardCompileOptions("pr240360","-proceedOnError");
		build("pr240360");
		checkWasFullBuild();
		checkCompileWeaveCount("pr240360", 5, 4);
		assertTrue("There should be an error:\n" + getErrorMessages("pr240360"), !getErrorMessages("pr240360").isEmpty());

		Set s = getModelFor("pr240360").getRelationshipMap().getEntries();
		int relmapLength = s.size();

		// Delete the erroneous type
		String f = getWorkingDir().getAbsolutePath() + File.separatorChar + "pr240360" + File.separatorChar + "src"
				+ File.separatorChar + "test" + File.separatorChar + "Error.java";
		(new File(f)).delete();
		build("pr240360");
		checkWasntFullBuild();
		checkCompileWeaveCount("pr240360", 0, 0);
		assertEquals(relmapLength, getModelFor("pr240360").getRelationshipMap().getEntries().size());

		// Readd the erroneous type
		alter("pr240360", "inc1");
		build("pr240360");
		checkWasntFullBuild();
		checkCompileWeaveCount("pr240360", 1, 0);
		assertEquals(relmapLength, getModelFor("pr240360").getRelationshipMap().getEntries().size());

		// Change the advice
		alter("pr240360", "inc2");
		build("pr240360");
		checkWasFullBuild();
		checkCompileWeaveCount("pr240360", 6, 4);
		assertEquals(relmapLength, getModelFor("pr240360").getRelationshipMap().getEntries().size());

	}

	public void testBrokenCodeCompilation() {
		initialiseProject("pr102733_1");
		// configureNonStandardCompileOptions("pr102733_1","-proceedOnError");
		build("pr102733_1");
		checkWasFullBuild();
		checkCompileWeaveCount("pr102733_1", 1, 0);
		assertTrue("There should be an error:\n" + getErrorMessages("pr102733_1"), !getErrorMessages("pr102733_1").isEmpty());
		build("pr102733_1"); // incremental
		checkCompileWeaveCount("pr102733_1", 0, 0);
		checkWasntFullBuild();
		alter("pr102733_1", "inc1"); // fix the error
		build("pr102733_1");
		checkWasntFullBuild();
		checkCompileWeaveCount("pr102733_1", 1, 1);
		assertTrue("There should be no errors:\n" + getErrorMessages("pr102733_1"), getErrorMessages("pr102733_1").isEmpty());
		alter("pr102733_1", "inc2"); // break it again
		build("pr102733_1");
		checkWasntFullBuild();
		checkCompileWeaveCount("pr102733_1", 1, 0);
		assertTrue("There should be an error:\n" + getErrorMessages("pr102733_1"), !getErrorMessages("pr102733_1").isEmpty());
	}

	// public void testDeclareAtType_pr149293() {
	// configureBuildStructureModel(true);
	// initialiseProject("PR149293_1");
	// build("PR149293_1");
	// checkCompileWeaveCount(4,5);
	// assertNoErrors();
	// alter("PR149293_1","inc1");
	// build("PR149293_1");
	// assertNoErrors();
	// }

	public void testRefactoring_pr148285() {
		// configureBuildStructureModel(true);

		initialiseProject("PR148285");
		build("PR148285");
		alter("PR148285", "inc1");
		build("PR148285");
	}

	/**
	 * In order for this next test to run, I had to move the weaver/world pair we keep in the AjBuildManager instance down into the
	 * state object - this makes perfect sense - otherwise when reusing the state for another project we'd not be switching to the
	 * right weaver/world for that project.
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

		alter("P1", "inc1"); // adds a class
		alter("P1", "inc2"); // adds an aspect
		build("P1");
		checkWasFullBuild(); // adding an aspect makes us go back to the source
	}

	public void testPr134371() {
		initialiseProject("PR134371");
		build("PR134371");
		alter("PR134371", "inc1");
		build("PR134371");
		assertTrue("There should be no exceptions handled:\n" + getErrorMessages("PR134371"), getErrorMessages("PR134371")
				.isEmpty());

	}

	/**
	 * This test is verifying the behaviour of the code that iterates through the type hierarchy for some type. There are two ways
	 * to do it - an approach that grabs all the information up front or an approach that works through iterators and only processes
	 * as much data as necessary to satisfy the caller. The latter approach could be much faster - especially if the matching
	 * process typically looks for a method in the declaring type.
	 */
	public void xtestOptimizedMemberLookup() {
		String p = "oml";
		initialiseProject(p);
		build(p);

		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();
		// Type A has no hierarchy (well, Object) and defines 3 methods
		checkType(w, "com.foo.A");
		// Type B extends B2. Two methods in B2, three in B
		checkType(w, "com.foo.B");
		// Type C implements an interface
		checkType(w, "com.foo.C");
		// Type CC extends a class that implements an interface
		checkType(w, "com.foo.CC");
		// Type CCC implements an interface that extends another interface
		checkType(w, "com.foo.CCC");
		// Type CCC implements an interface that extends another interface
		checkType(w, "com.foo.CCC");
		checkType(w, "GenericMethodInterface");
		checkType(w, "GenericInterfaceChain");

		// Some random classes from rt.jar that did reveal some problems:
		checkType(w, "java.lang.StringBuffer");
		checkType(w, "com.sun.corba.se.impl.encoding.CDRInputObject");
		checkTypeHierarchy(w, "com.sun.corba.se.impl.interceptors.PIHandlerImpl$RequestInfoStack", true);
		checkType(w, "com.sun.corba.se.impl.interceptors.PIHandlerImpl$RequestInfoStack");
		checkType(w, "DeclareWarningAndInterfaceMethodCW");
		checkType(w, "ICanGetSomething");
		checkType(w, "B");
		checkType(w, "C");

		// checkRtJar(w); // only works if the JDK path is setup ok in checkRtJar

		// speedCheck(w);
	}

	// private void checkRtJar(World w) {
	// System.out.println("Processing everything in rt.jar: ~16000 classes");
	// try {
	// ZipFile zf = new ZipFile("c:/jvms/jdk1.6.0_06/jre/lib/rt.jar");
	// Enumeration e = zf.entries();
	// int count = 1;
	// while (e.hasMoreElements()) {
	// ZipEntry ze = (ZipEntry) e.nextElement();
	// String n = ze.getName();
	// if (n.endsWith(".class")) {
	// n = n.replace('/', '.');
	// n = n.substring(0, n.length() - 6);
	// if ((count % 100) == 0) {
	// System.out.print(count + " ");
	// }
	// if ((count % 1000) == 0) {
	// System.out.println();
	// }
	// checkType(w, n);
	// count++;
	// }
	// }
	// zf.close();
	// } catch (IOException t) {
	// t.printStackTrace();
	// fail(t.toString());
	// }
	// System.out.println();
	// }

	/**
	 * Compare time taken to grab them all and look at them and iterator through them all.
	 */
	private void speedCheck(World w) {
		long stime = System.currentTimeMillis();
		try {
			ZipFile zf = new ZipFile("c:/jvms/jdk1.6.0_06/jre/lib/rt.jar");
			Enumeration<? extends ZipEntry> e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = e.nextElement();
				String n = ze.getName();
				if (n.endsWith(".class")) {
					n = n.replace('/', '.');
					n = n.substring(0, n.length() - 6);
					ResolvedType typeA = w.resolve(n);
					assertFalse(typeA.isMissing());
					List<ResolvedMember> viaIteratorList = getThemAll(typeA.getMethods(true, true));
					viaIteratorList = getThemAll(typeA.getMethods(false, true));
				}
			}
			zf.close();
		} catch (IOException t) {
			t.printStackTrace();
			fail(t.toString());
		}
		long etime = System.currentTimeMillis();
		System.out.println("Time taken for 'iterator' approach: " + (etime - stime) + "ms");
		stime = System.currentTimeMillis();
		try {
			ZipFile zf = new ZipFile("c:/jvms/jdk1.6.0_06/jre/lib/rt.jar");
			Enumeration e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();
				String n = ze.getName();
				if (n.endsWith(".class")) {
					n = n.replace('/', '.');
					n = n.substring(0, n.length() - 6);
					ResolvedType typeA = w.resolve(n);
					assertFalse(typeA.isMissing());
					List<ResolvedMember> viaIteratorList = typeA.getMethodsWithoutIterator(false, true, true);
					viaIteratorList = typeA.getMethodsWithoutIterator(false, true, false);
				}
			}
			zf.close();
		} catch (IOException t) {
			t.printStackTrace();
			fail(t.toString());
		}
		etime = System.currentTimeMillis();
		System.out.println("Time taken for 'grab all up front' approach: " + (etime - stime) + "ms");

	}

	private void checkType(World w, String name) {
		checkTypeHierarchy(w, name, true);
		checkTypeHierarchy(w, name, false);
		checkMethods(w, name, true);
		checkMethods(w, name, false);
	}

	private void checkMethods(World w, String name, boolean wantGenerics) {
		ResolvedType typeA = w.resolve(name);
		assertFalse(typeA.isMissing());
		List<ResolvedMember> viaIteratorList = getThemAll(typeA.getMethods(wantGenerics, true));
		List<ResolvedMember> directlyList = typeA.getMethodsWithoutIterator(true, true, wantGenerics);
		viaIteratorList.sort(new ResolvedMemberComparator());
		directlyList.sort(new ResolvedMemberComparator());
		compare(viaIteratorList, directlyList, name);
		// System.out.println(toString(viaIteratorList, directlyList, genericsAware));
	}

	private static class ResolvedMemberComparator implements Comparator<ResolvedMember> {
		public int compare(ResolvedMember o1, ResolvedMember o2) {
			return o1.toString().compareTo(o2.toString());
		}
	}

	private void checkTypeHierarchy(World w, String name, boolean wantGenerics) {
		ResolvedType typeA = w.resolve(name);
		assertFalse(typeA.isMissing());
		List<String> viaIteratorList = exhaustTypeIterator(typeA.getHierarchy(wantGenerics, false));
		List<ResolvedType> typeDirectlyList = typeA.getHierarchyWithoutIterator(true, true, wantGenerics);
		assertFalse(viaIteratorList.isEmpty());
		List<String> directlyList = new ArrayList<>();
		for (ResolvedType type : typeDirectlyList) {
			String n = type.getName();
			if (!directlyList.contains(n)) {
				directlyList.add(n);
			}
		}
		Collections.sort(viaIteratorList);
		Collections.sort(directlyList);
		compareTypeLists(viaIteratorList, directlyList);
		// System.out.println("ShouldBeGenerics?" + wantGenerics + "\n" + typeListsToString(viaIteratorList, directlyList));
	}

	private void compare(List<ResolvedMember> viaIteratorList, List<ResolvedMember> directlyList, String typename) {
		assertEquals(typename + "\n" + toString(directlyList), typename + "\n" + toString(viaIteratorList));
	}

	private void compareTypeLists(List<String> viaIteratorList, List<String> directlyList) {
		assertEquals(typeListToString(directlyList), typeListToString(viaIteratorList));
	}

	private String toString(List<ResolvedMember> list) {
		StringBuffer sb = new StringBuffer();
		for (ResolvedMember m : list) {
			sb.append(m).append("\n");
		}
		return sb.toString();
	}

	private String typeListToString(List<String> list) {
		StringBuffer sb = new StringBuffer();
		for (String m : list) {
			sb.append(m).append("\n");
		}
		return sb.toString();
	}

	private String toString(List<ResolvedMember> one, List<ResolvedMember> two, boolean shouldIncludeGenerics) {
		StringBuffer sb = new StringBuffer();
		sb.append("Through iterator\n");
		for (ResolvedMember m : one) {
			sb.append(m).append("\n");
		}
		sb.append("Directly retrieved\n");
		for (ResolvedMember m : one) {
			sb.append(m).append("\n");
		}
		return sb.toString();
	}

	private String typeListsToString(List<String> one, List<String> two) {
		StringBuffer sb = new StringBuffer();
		sb.append("Through iterator\n");
		for (String m : one) {
			sb.append(">" + m).append("\n");
		}
		sb.append("Directly retrieved\n");
		for (String m : one) {
			sb.append(">" + m).append("\n");
		}
		return sb.toString();
	}

	private List<ResolvedMember> getThemAll(Iterator<ResolvedMember> methods) {
		List<ResolvedMember> allOfThem = new ArrayList<>();
		while (methods.hasNext()) {
			allOfThem.add(methods.next());
		}
		return allOfThem;
	}

	private List<String> exhaustTypeIterator(Iterator<ResolvedType> types) {
		List<String> allOfThem = new ArrayList<>();
		while (types.hasNext()) {
			allOfThem.add(types.next().getName());
		}
		return allOfThem;
	}

	/**
	 * Setup up two simple projects and build them in turn - check the structure model is right after each build
	 */
	public void testBuildingTwoProjectsAndVerifyingModel() {
		initialiseProject("P1");
		initialiseProject("P2");
		configureNonStandardCompileOptions("P1", "-Xset:minimalModel=false");
		configureNonStandardCompileOptions("P2", "-Xset:minimalModel=false");

		build("P1");
		checkForNode(getModelFor("P1"), "pkg", "C", true);

		build("P2");
		checkForNode(getModelFor("P2"), "pkg", "C", false);

		build("P1");
		checkForNode(getModelFor("P1"), "pkg", "C", true);

		build("P2");
		checkForNode(getModelFor("P2"), "pkg", "C", false);
	}

	// Setup up two simple projects and build them in turn - check the
	// structure model is right after each build
	public void testBuildingTwoProjectsAndVerifyingStuff() {
		initialiseProject("P1");
		initialiseProject("P2");
		configureNonStandardCompileOptions("P1", "-Xset:minimalModel=false");
		configureNonStandardCompileOptions("P2", "-Xset:minimalModel=false");

		build("P1");
		checkForNode(getModelFor("P1"), "pkg", "C", true);

		build("P2");
		checkForNode(getModelFor("P2"), "pkg", "C", false);

		build("P1");
		checkForNode(getModelFor("P1"), "pkg", "C", true);

		build("P2");
		checkForNode(getModelFor("P2"), "pkg", "C", false);
	}

	/**
	 * Complex. Here we are testing that a state object records structural changes since the last full build correctly. We build a
	 * simple project from scratch - this will be a full build and so the structural changes since last build count should be 0. We
	 * then alter a class, adding a new method and check structural changes is 1.
	 */
	public void testStateManagement1() {

		File binDirectoryForP1 = new File(getFile("P1", "bin"));

		initialiseProject("P1");
		build("P1"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirectoryForP1);
		assertTrue("There should be a state object for project P1", ajs != null);
		assertTrue(
				"Should be no structural changes as it was a full build but found: "
						+ ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 0);

		alter("P1", "inc3"); // adds a method to the class C.java
		build("P1");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("P1", "bin")));
		assertTrue("There should be state for project P1", ajs != null);
		checkWasntFullBuild();
		assertTrue(
				"Should be one structural changes as it was a full build but found: "
						+ ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 1);

	}

	/**
	 * Complex. Here we are testing that a state object records structural changes since the last full build correctly. We build a
	 * simple project from scratch - this will be a full build and so the structural changes since last build count should be 0. We
	 * then alter a class, changing body of a method, not the structure and check struc changes is still 0.
	 */
	public void testStateManagement2() {
		File binDirectoryForP1 = new File(getFile("P1", "bin"));

		initialiseProject("P1");
		alter("P1", "inc3"); // need this change in here so 'inc4' can be
		// applied without making
		// it a structural change
		build("P1"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirectoryForP1);
		assertTrue("There should be state for project P1", ajs != null);
		assertTrue("Should be no struc changes as its a full build: " + ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 0);

		alter("P1", "inc4"); // changes body of main() method but does *not*
		// change the structure of C.java
		build("P1");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("P1", "bin")));
		assertTrue("There should be state for project P1", ajs != null);
		checkWasntFullBuild();
		assertTrue("Shouldn't be any structural changes but there were " + ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 0);
	}

	/**
	 * The C.java file modified in this test has an inner class - this means the inner class has a this$0 field and <init>(C) ctor
	 * to watch out for when checking for structural changes
	 *
	 */
	public void testStateManagement3() {
		File binDirForInterproject1 = new File(getFile("interprojectdeps1", "bin"));

		initialiseProject("interprojectdeps1");
		build("interprojectdeps1"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirForInterproject1);
		assertTrue("There should be state for project P1", ajs != null);
		assertTrue("Should be no struc changes as its a full build: " + ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 0);

		alter("interprojectdeps1", "inc1"); // adds a space to C.java
		build("interprojectdeps1");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("interprojectdeps1", "bin")));
		assertTrue("There should be state for project interprojectdeps1", ajs != null);
		checkWasntFullBuild();
		assertTrue("Shouldn't be any structural changes but there were " + ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 0);
	}

	/**
	 * The C.java file modified in this test has an inner class - which has two ctors - this checks how they are mangled with an
	 * instance of C.
	 *
	 */
	public void testStateManagement4() {
		File binDirForInterproject2 = new File(getFile("interprojectdeps2", "bin"));

		initialiseProject("interprojectdeps2");
		build("interprojectdeps2"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirForInterproject2);
		assertTrue("There should be state for project interprojectdeps2", ajs != null);
		assertTrue("Should be no struc changes as its a full build: " + ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 0);

		alter("interprojectdeps2", "inc1"); // minor change to C.java
		build("interprojectdeps2");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("interprojectdeps2", "bin")));
		assertTrue("There should be state for project interprojectdeps1", ajs != null);
		checkWasntFullBuild();
		assertTrue("Shouldn't be any structural changes but there were " + ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 0);
	}

	/**
	 * The C.java file modified in this test has an inner class - it has two ctors but also a reference to C.this in it - which will
	 * give rise to an accessor being created in C
	 *
	 */
	public void testStateManagement5() {
		File binDirForInterproject3 = new File(getFile("interprojectdeps3", "bin"));

		initialiseProject("interprojectdeps3");
		build("interprojectdeps3"); // full build
		AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(binDirForInterproject3);
		assertTrue("There should be state for project interprojectdeps3", ajs != null);
		assertTrue("Should be no struc changes as its a full build: " + ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 0);

		alter("interprojectdeps3", "inc1"); // minor change to C.java
		build("interprojectdeps3");
		checkWasntFullBuild();
		ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("interprojectdeps3", "bin")));
		assertTrue("There should be state for project interprojectdeps1", ajs != null);
		checkWasntFullBuild();
		assertTrue("Shouldn't be any structural changes but there were " + ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
				ajs.getNumberOfStructuralChangesSinceLastFullBuild() == 0);
	}

	/**
	 * Now the most complex test. Create a dependancy between two projects. Building one may affect whether the other does an
	 * incremental or full build. The structural information recorded in the state object should be getting used to control whether
	 * a full build is necessary...
	 */
	public void testBuildingDependantProjects() {
		initialiseProject("P1");
		initialiseProject("P2");
		configureNewProjectDependency("P2", "P1");

		build("P1");
		build("P2"); // now everything is consistent and compiled
		alter("P1", "inc1"); // adds a second class
		build("P1");
		build("P2"); // although a second class was added - P2 can't be using
		// it, so we don't full build here :)
		checkWasntFullBuild();
		alter("P1", "inc3"); // structurally changes one of the classes
		build("P1");
		build("P2"); // build notices the structural change, but is incremental
		// of I and J as they depend on C
		checkWasntFullBuild();
		alter("P1", "inc4");
		build("P1");
		build("P2"); // build sees a change but works out its not structural
		checkWasntFullBuild();
	}

	public void testPr85132() {
		initialiseProject("PR85132");
		build("PR85132");
		alter("PR85132", "inc1");
		build("PR85132");
	}

	// parameterization of generic aspects
	public void testPr125405() {
		initialiseProject("PR125405");
		build("PR125405");
		checkCompileWeaveCount("PR125405", 1, 1);
		alter("PR125405", "inc1");
		build("PR125405");
		// "only abstract aspects can have type parameters"
		checkForError("PR125405", "only abstract aspects can have type parameters");
		alter("PR125405", "inc2");
		build("PR125405");
		checkCompileWeaveCount("PR125405", 1, 1);
		assertTrue("Should be no errors, but got " + getErrorMessages("PR125405"), getErrorMessages("PR125405").size() == 0);
	}

	public void testPr128618() {
		initialiseProject("PR128618_1");
		initialiseProject("PR128618_2");
		configureNewProjectDependency("PR128618_2", "PR128618_1");
		assertTrue("there should be no warning messages before we start", getWarningMessages("PR128618_1").isEmpty());
		assertTrue("there should be no warning messages before we start", getWarningMessages("PR128618_2").isEmpty());

		build("PR128618_1");
		build("PR128618_2");
		List<IMessage> l = getWarningMessages("PR128618_2");

		// there should be one warning against "PR128618_2"
		List<IMessage> warnings = getWarningMessages("PR128618_2");
		assertTrue("Should be one warning, but there are #" + warnings.size(), warnings.size() == 1);
		IMessage msg = (getWarningMessages("PR128618_2").get(0));
		assertEquals("warning should be against the FFDC.aj resource", "FFDC.aj", msg.getSourceLocation().getSourceFile().getName());

		alter("PR128618_2", "inc1");
		build("PR128618_2");

		checkWasntFullBuild();
		IMessage msg2 = (getWarningMessages("PR128618_2").get(0));
		assertEquals("warning should be against the FFDC.aj resource", "FFDC.aj", msg2.getSourceLocation().getSourceFile()
				.getName());
		assertFalse("a new warning message should have been generated", msg.equals(msg2));
	}

	public void testPr92837() {
		initialiseProject("PR92837");
		build("PR92837");
		alter("PR92837", "inc1");
		build("PR92837");
	}

	// See open generic itd bug mentioning 119570
	// public void testPr119570() {
	// initialiseProject("PR119570");
	// build("PR119570");
	// assertNoErrors("PR119570");
	// }

	// public void testPr119570_212783_2() {
	// initialiseProject("PR119570_2");
	// build("PR119570_2");
	// List l = getWarningMessages("PR119570_2");
	// assertTrue("Should be no warnings, but got "+l,l.size()==0);
	// assertNoErrors("PR119570_2");
	// }
	//
	// public void testPr119570_212783_3() {
	// initialiseProject("pr119570_3");
	// build("pr119570_3");
	// List l = getWarningMessages("pr119570_3");
	// assertTrue("Should be no warnings, but got "+l,l.size()==0);
	// assertNoErrors("pr119570_3");
	// }

	// If you fiddle with the compiler options - you must manually reset the
	// options at the end of the test
	public void testPr117209() {
		try {
			initialiseProject("pr117209");
			configureNonStandardCompileOptions("pr117209", "-proceedOnError");
			build("pr117209");
			checkCompileWeaveCount("pr117209", 6, 5);
		} finally {
			// MyBuildOptionsAdapter.reset();
		}
	}

	public void testPr114875() {
		// temporary problem with this on linux, think it is a filesystem
		// lastmodtime issue
		if (System.getProperty("os.name", "").toLowerCase().equals("linux")) {
			return;
		}
		initialiseProject("pr114875");
		build("pr114875");
		alter("pr114875", "inc1");
		build("pr114875");
		checkWasFullBuild();
		alter("pr114875", "inc2");
		build("pr114875");
		checkWasFullBuild(); // back to the source for an aspect change
	}

	public void testPr117882() {
		// AjdeInteractionTestbed.VERBOSE=true;
		// AjdeInteractionTestbed.configureBuildStructureModel(true);
		initialiseProject("PR117882");
		build("PR117882");
		checkWasFullBuild();
		alter("PR117882", "inc1");
		build("PR117882");
		// This should be an incremental build now - because of the changes
		// under 259649
		checkWasntFullBuild(); // back to the source for an aspect
		// AjdeInteractionTestbed.VERBOSE=false;
		// AjdeInteractionTestbed.configureBuildStructureModel(false);
	}

	public void testPr117882_2() {
		// AjdeInteractionTestbed.VERBOSE=true;
		// AjdeInteractionTestbed.configureBuildStructureModel(true);
		initialiseProject("PR117882_2");
		build("PR117882_2");
		checkWasFullBuild();
		alter("PR117882_2", "inc1");
		build("PR117882_2");
		checkWasFullBuild(); // back to the source...
		// checkCompileWeaveCount(1,4);
		// fullBuild("PR117882_2");
		// checkWasFullBuild();
		// AjdeInteractionTestbed.VERBOSE=false;
		// AjdeInteractionTestbed.configureBuildStructureModel(false);
	}

	public void testPr115251() {
		// AjdeInteractionTestbed.VERBOSE=true;
		initialiseProject("PR115251");
		build("PR115251");
		checkWasFullBuild();
		alter("PR115251", "inc1");
		build("PR115251");
		checkWasFullBuild(); // back to the source
	}

	public void testPr220255_InfiniteBuildHasMember() {
		initialiseProject("pr220255");
		configureNonStandardCompileOptions("pr220255", "-XhasMember");
		build("pr220255");
		checkWasFullBuild();
		alter("pr220255", "inc1");
		build("pr220255");
		checkWasntFullBuild();
	}

	public void testPr157054() {
		initialiseProject("PR157054");
		configureNonStandardCompileOptions("PR157054", "-showWeaveInfo");
		configureShowWeaveInfoMessages("PR157054", true);
		build("PR157054");
		checkWasFullBuild();
		List<IMessage> weaveMessages = getWeavingMessages("PR157054");
		assertTrue("Should be two weaving messages but there are " + weaveMessages.size(), weaveMessages.size() == 2);
		alter("PR157054", "inc1");
		build("PR157054");
		weaveMessages = getWeavingMessages("PR157054");
		assertTrue("Should be three weaving messages but there are " + weaveMessages.size(), weaveMessages.size() == 3);
		checkWasntFullBuild();
		fullBuild("PR157054");
		weaveMessages = getWeavingMessages("PR157054");
		assertTrue("Should be three weaving messages but there are " + weaveMessages.size(), weaveMessages.size() == 3);
	}

	/**
	 * Checks we aren't leaking mungers across compiles (accumulating multiple instances of the same one that all do the same
	 * thing). On the first compile the munger is added late on - so at the time we set the count it is still zero. On the
	 * subsequent compiles we know about this extra one.
	 */
	public void testPr141956_IncrementallyCompilingAtAj() {
		initialiseProject("PR141956");
		build("PR141956");
		assertTrue("Should be zero but reports " + EclipseFactory.debug_mungerCount, EclipseFactory.debug_mungerCount == 0);
		alter("PR141956", "inc1");
		build("PR141956");
		assertTrue("Should be two but reports " + EclipseFactory.debug_mungerCount, EclipseFactory.debug_mungerCount == 2);
		alter("PR141956", "inc1");
		build("PR141956");
		assertTrue("Should be two but reports " + EclipseFactory.debug_mungerCount, EclipseFactory.debug_mungerCount == 2);
		alter("PR141956", "inc1");
		build("PR141956");
		assertTrue("Should be two but reports " + EclipseFactory.debug_mungerCount, EclipseFactory.debug_mungerCount == 2);
		alter("PR141956", "inc1");
		build("PR141956");
		assertTrue("Should be two but reports " + EclipseFactory.debug_mungerCount, EclipseFactory.debug_mungerCount == 2);
	}

	// public void testPr124399() {
	// AjdeInteractionTestbed.VERBOSE=true;
	// configureBuildStructureModel(true);
	// initialiseProject("PR124399");
	// build("PR124399");
	// checkWasFullBuild();
	// alter("PR124399","inc1");
	// build("PR124399");
	// checkWasntFullBuild();
	// }

	public void testPr121384() {
		// AjdeInteractionTestbed.VERBOSE=true;
		// AsmManager.setReporting("c:/foo.txt",true,true,true,false);
		initialiseProject("pr121384");
		configureNonStandardCompileOptions("pr121384", "-showWeaveInfo");
		build("pr121384");
		checkWasFullBuild();
		alter("pr121384", "inc1");
		build("pr121384");
		checkWasntFullBuild();
	}

	/*
	 * public void testPr111779() { super.VERBOSE=true; initialiseProject("PR111779"); build("PR111779"); alter("PR111779","inc1");
	 * build("PR111779"); }
	 */

	public void testPr93310_1() {
		initialiseProject("PR93310_1");
		build("PR93310_1");
		checkWasFullBuild();
		String fileC2 = getWorkingDir().getAbsolutePath() + File.separatorChar + "PR93310_1" + File.separatorChar + "src"
				+ File.separatorChar + "pack" + File.separatorChar + "C2.java";
		(new File(fileC2)).delete();
		alter("PR93310_1", "inc1");
		build("PR93310_1");
		checkWasFullBuild();
		int l = AjdeInteractionTestbed.MyStateListener.detectedDeletions.size();
		assertTrue("Expected one deleted file to be noticed, but detected: " + l, l == 1);
		String name = AjdeInteractionTestbed.MyStateListener.detectedDeletions.get(0);
		assertTrue("Should end with C2.java but is " + name, name.endsWith("C2.java"));
	}

	public void testPr93310_2() {
		initialiseProject("PR93310_2");
		build("PR93310_2");
		checkWasFullBuild();
		String fileC2 = getWorkingDir().getAbsolutePath() + File.separatorChar + "PR93310_2" + File.separatorChar + "src"
				+ File.separatorChar + "pack" + File.separatorChar + "C2.java";
		(new File(fileC2)).delete();
		alter("PR93310_2", "inc1");
		build("PR93310_2");
		checkWasFullBuild();
		int l = AjdeInteractionTestbed.MyStateListener.detectedDeletions.size();
		assertTrue("Expected one deleted file to be noticed, but detected: " + l, l == 1);
		String name = AjdeInteractionTestbed.MyStateListener.detectedDeletions.get(0);
		assertTrue("Should end with C2.java but is " + name, name.endsWith("C2.java"));
	}

	// Stage1: Compile two files, pack.A and pack.A1 - A1 sets a protected field
	// in A.
	// Stage2: make the field private in class A > gives compile error
	// Stage3: Add a new aspect whilst there is a compile error !
	public void testPr113531() {
		initialiseProject("PR113531");
		build("PR113531");
		assertTrue("build should have compiled ok", getErrorMessages("PR113531").isEmpty());
		alter("PR113531", "inc1");
		build("PR113531");
		assertEquals("error message should be 'foo cannot be resolved to a variable' ", "foo cannot be resolved to a variable",
				(getErrorMessages("PR113531").get(0)).getMessage());
		alter("PR113531", "inc2");
		build("PR113531");
		assertTrue("There should be no exceptions handled:\n" + getCompilerErrorMessages("PR113531"),
				getCompilerErrorMessages("PR113531").isEmpty());
		assertEquals("error message should be 'foo cannot be resolved to a variable' ", "foo cannot be resolved to a variable",
				(getErrorMessages("PR113531").get(0)).getMessage());
	}

	// Stage 1: Compile the 4 files, pack.A2 extends pack.A1 (aspects) where
	// A2 uses a protected field in A1 and pack.C2 extends pack.C1 (classes)
	// where C2 uses a protected field in C1
	// Stage 2: make the field private in class C1 ==> compile errors in C2
	// Stage 3: make the field private in aspect A1 whilst there's the compile
	// error.
	// There shouldn't be a BCException saying can't find delegate for pack.C2
	public void testPr119882() {
		initialiseProject("PR119882");
		build("PR119882");
		assertTrue("build should have compiled ok", getErrorMessages("PR119882").isEmpty());
		alter("PR119882", "inc1");
		build("PR119882");
		// fullBuild("PR119882");
		List<IMessage> errors = getErrorMessages("PR119882");
		assertTrue("Should be at least one error, but got none", errors.size() == 1);
		assertEquals("error message should be 'i cannot be resolved to a variable' ", "i cannot be resolved to a variable",
				errors.get(0).getMessage());
		alter("PR119882", "inc2");
		build("PR119882");
		assertTrue("There should be no exceptions handled:\n" + getCompilerErrorMessages("PR119882"),
				getCompilerErrorMessages("PR119882").isEmpty());
		assertEquals("error message should be 'i cannot be resolved to a variable' ", "i cannot be resolved to a variable",
				errors.get(0).getMessage());

	}

	public void testPr112736() {
		initialiseProject("PR112736");
		build("PR112736");
		checkWasFullBuild();
		String fileC2 = getWorkingDir().getAbsolutePath() + File.separatorChar + "PR112736" + File.separatorChar + "src"
				+ File.separatorChar + "pack" + File.separatorChar + "A.java";
		(new File(fileC2)).delete();
		alter("PR112736", "inc1");
		build("PR112736");
		checkWasFullBuild();
	}

	/**
	 * We have problems with multiple rewrites of a pointcut across incremental builds.
	 */
	public void testPr113257() {
		initialiseProject("PR113257");
		build("PR113257");
		alter("PR113257", "inc1");
		build("PR113257");
		checkWasFullBuild(); // back to the source
		alter("PR113257", "inc1");
		build("PR113257");
	}

	public void testPr123612() {
		initialiseProject("PR123612");
		build("PR123612");
		alter("PR123612", "inc1");
		build("PR123612");
		checkWasFullBuild(); // back to the source
	}

	// Bugzilla Bug 152257 - Incremental compiler doesn't handle exception
	// declaration correctly
	public void testPr152257() {
		initialiseProject("PR152257");
		configureNonStandardCompileOptions("PR152257", "-XnoInline");
		build("PR152257");
		List<IMessage> errors = getErrorMessages("PR152257");
		assertTrue("Should be no warnings, but there are #" + errors.size(), errors.size() == 0);
		checkWasFullBuild();
		alter("PR152257", "inc1");
		build("PR152257");
		errors = getErrorMessages("PR152257");
		assertTrue("Should be no warnings, but there are #" + errors.size(), errors.size() == 0);
		checkWasntFullBuild();
	}

	public void testPr128655() {
		initialiseProject("pr128655");
		configureNonStandardCompileOptions("pr128655", "-showWeaveInfo");
		configureShowWeaveInfoMessages("pr128655", true);
		build("pr128655");
		List<IMessage> firstBuildMessages = getWeavingMessages("pr128655");
		assertTrue("Should be at least one message about the dec @type, but there were none", firstBuildMessages.size() > 0);
		alter("pr128655", "inc1");
		build("pr128655");
		checkWasntFullBuild(); // back to the source
		List<IMessage> secondBuildMessages = getWeavingMessages("pr128655");
		// check they are the same
		for (int i = 0; i < firstBuildMessages.size(); i++) {
			IMessage m1 = firstBuildMessages.get(i);
			IMessage m2 = secondBuildMessages.get(i);
			if (!m1.toString().equals(m2.toString())) {
				System.err.println("Message during first build was: " + m1);
				System.err.println("Message during second build was: " + m1);
				fail("The two messages should be the same, but are not: \n" + m1 + "!=" + m2);
			}
		}
	}

	// Similar to above, but now the annotation is in the default package
	public void testPr128655_2() {
		initialiseProject("pr128655_2");
		configureNonStandardCompileOptions("pr128655_2", "-showWeaveInfo");
		configureShowWeaveInfoMessages("pr128655_2", true);
		build("pr128655_2");
		List<IMessage> firstBuildMessages = getWeavingMessages("pr128655_2");
		assertTrue("Should be at least one message about the dec @type, but there were none", firstBuildMessages.size() > 0);
		alter("pr128655_2", "inc1");
		build("pr128655_2");
		checkWasntFullBuild(); // back to the source
		List<IMessage> secondBuildMessages = getWeavingMessages("pr128655_2");
		// check they are the same
		for (int i = 0; i < firstBuildMessages.size(); i++) {
			IMessage m1 = firstBuildMessages.get(i);
			IMessage m2 = secondBuildMessages.get(i);
			if (!m1.toString().equals(m2.toString())) {
				System.err.println("Message during first build was: " + m1);
				System.err.println("Message during second build was: " + m1);
				fail("The two messages should be the same, but are not: \n" + m1 + "!=" + m2);
			}
		}
	}

	// test for comment #31 - NPE
	public void testPr129163() {
		initialiseProject("PR129613");
		build("PR129613");
		alter("PR129613", "inc1");
		build("PR129613");
		assertTrue("There should be no exceptions handled:\n" + getCompilerErrorMessages("PR129613"),
				getCompilerErrorMessages("PR129613").isEmpty());
		assertEquals("warning message should be 'no match for this type name: File [Xlint:invalidAbsoluteTypeName]' ",
				"no match for this type name: File [Xlint:invalidAbsoluteTypeName]",
				(getWarningMessages("PR129613").get(0)).getMessage());
	}

	// test for comment #0 - adding a comment to a class file shouldn't
	// cause us to go back to source and recompile everything. To force this
	// to behave like AJDT we need to include the aspect in 'inc1' so that
	// when AjState looks at its timestamp it thinks the aspect has been
	// modified.
	// The logic within CrosscuttingMembers should then work out correctly
	// that there haven't really been any changes within the aspect and so
	// we shouldn't go back to source.
	public void testPr129163_2() {
		// want to behave like AJDT
		initialiseProject("pr129163_2");
		build("pr129163_2");
		checkWasFullBuild();
		alter("pr129163_2", "inc1");
		build("pr129163_2");
		checkWasntFullBuild(); // shouldn't be a full build because the
		// aspect hasn't changed
	}

	public void testIncrementalIntelligence_Scenario01() {
		initialiseProject("Project1");
		initialiseProject("Project2");
		configureNewProjectDependency("Project2", "Project1");
		build("Project1");
		build("Project2");

		alter("Project1", "inc1"); // white space change to ClassA - no impact
		build("Project1");
		build("Project2");
		checkWasntFullBuild(); // not a structural change so ignored

		alter("Project1", "inc2"); // structural change to ClassB - new method!
		build("Project1");
		build("Project2");
		checkWasntFullBuild(); // not a type that Project2 depends on so ignored

		alter("Project1", "inc3"); // structural change to ClassA
		build("Project1");
		setNextChangeResponse("Project2", ICompilerConfiguration.EVERYTHING); // See
		// pr245566
		// comment
		// 3
		build("Project2");
		checkWasntFullBuild(); // Just need to recompile ClassAExtender
		checkCompileWeaveCount("Project2", 1, 1);
		checkCompiled("Project2", "ClassAExtender");

		alter("Project2", "inc1"); // New type that depends on ClassAExtender
		build("Project1");
		build("Project2");
		checkWasntFullBuild(); // Just build ClassAExtenderExtender

		alter("Project1", "inc4"); // another structural change to ClassA
		build("Project1");
		setNextChangeResponse("Project2", ICompilerConfiguration.EVERYTHING); // See
		// pr245566
		// comment
		// 3
		build("Project2");
		checkWasntFullBuild(); // Should rebuild ClassAExtender and
		// ClassAExtenderExtender
		checkCompileWeaveCount("Project2", 2, 2);
		checkCompiled("Project2", "ClassAExtenderExtender");

	}

	private void checkCompiled(String projectName, String typeNameSubstring) {
		List<String> files = getCompiledFiles(projectName);
		boolean found = false;
		for (String object: files) {
			if (object.contains(typeNameSubstring)) {
				found = true;
			}
		}
		assertTrue("Did not find '" + typeNameSubstring + "' in list of compiled files", found);
	}

	// Case001: renaming a private field in a type
	/*
	 * public void testPrReducingDependentBuilds_001_221427() { AjdeInteractionTestbed.VERBOSE=true;
	 * IncrementalStateManager.debugIncrementalStates=true; initialiseProject("P221427_1"); initialiseProject("P221427_2");
	 * configureNewProjectDependency("P221427_2","P221427_1");
	 *
	 * build("P221427_1"); build("P221427_2"); alter("P221427_1","inc1"); // rename private class in super project
	 * MyStateListener.reset(); build("P221427_1"); build("P221427_2");
	 *
	 * AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("P221427_1","bin")));
	 * assertTrue("There should be state for project P221427_1",ajs!=null);
	 * //System.out.println(MyStateListener.getInstance().getDecisions()); checkWasntFullBuild();
	 * assertTrue("Should be one structural change but there were "+ ajs.getNumberOfStructuralChangesSinceLastFullBuild(),
	 * ajs.getNumberOfStructuralChangesSinceLastFullBuild()==1);
	 *
	 * }
	 *
	 * // Case002: changing a class to final that is extended in a dependent project public void
	 * testPrReducingDependentBuilds_002_221427() { AjdeInteractionTestbed.VERBOSE=true;
	 * IncrementalStateManager.debugIncrementalStates=true; initialiseProject("P221427_3"); initialiseProject("P221427_4");
	 * configureNewProjectDependency("P221427_4","P221427_3");
	 *
	 * build("P221427_3"); build("P221427_4"); // build OK, type in super project is non-final alter("P221427_3","inc1"); // change
	 * class declaration in super-project to final MyStateListener.reset(); build("P221427_3"); build("P221427_4"); // build FAIL,
	 * type in super project is now final
	 *
	 * AjState ajs = IncrementalStateManager.findStateManagingOutputLocation(new File(getFile("P221427_3","bin")));
	 * assertTrue("There should be state for project P221427_3",ajs!=null);
	 * System.out.println(MyStateListener.getInstance().getDecisions());
	 *
	 * List errors = getErrorMessages("P221427_4"); if (errors.size()!=1) { if (errors.size()==0)
	 * fail("Expected error about not being able to extend final class"); for (Iterator iterator = errors.iterator();
	 * iterator.hasNext();) { Object object = (Object) iterator.next(); System.out.println(object); }
	 * fail("Expected 1 error but got "+errors.size()); } // assertTrue("Shouldn't be one structural change but there were "+ //
	 * ajs.getNumberOfStructuralChangesSinceLastFullBuild(), // ajs.getNumberOfStructuralChangesSinceLastFullBuild()==1);
	 *
	 * }
	 */
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
		checkWasFullBuild(); // should be a full build because initializing
		// project
		initialiseProject("PR129163_3");
		configureNewProjectDependency("PR129163_3", "PR129163_4");
		build("PR129163_3");
		checkWasFullBuild(); // should be a full build because initializing
		// project
		alter("PR129163_4", "inc1");
		build("PR129163_4");
		checkWasntFullBuild(); // should be an incremental build because
		// although
		// "inc1" includes the aspect A1.aj, it actually hasn't
		// changed so we shouldn't go back to source
		alter("PR129163_3", "inc1");
		build("PR129163_3");
		checkWasntFullBuild(); // should be an incremental build because nothing
		// has
		// changed within the class and no aspects have changed
		// within the running of the test
	}

	public void testPr133117() {
		// System.gc();
		// System.exit();
		initialiseProject("PR133117");
		configureNonStandardCompileOptions("PR133117", "-Xlint:warning");
		build("PR133117");
		assertTrue("There should only be one xlint warning message reported:\n" + getWarningMessages("PR133117"),
				getWarningMessages("PR133117").size() == 1);
		alter("PR133117", "inc1");
		build("PR133117");
		List<IMessage> warnings = getWarningMessages("PR133117");
		List<IMessage> noGuardWarnings = new ArrayList<>();
		for (IMessage warning: warnings) {
			if (warning.getMessage().contains("Xlint:noGuardForLazyTjp")) {
				noGuardWarnings.add(warning);
			}
		}
		assertTrue("There should only be two Xlint:noGuardForLazyTjp warning message reported:\n" + noGuardWarnings,
				noGuardWarnings.size() == 2);
	}

	public void testPr131505() {
		initialiseProject("PR131505");
		configureNonStandardCompileOptions("PR131505", "-outxml");
		build("PR131505");
		checkWasFullBuild();
		String outputDir = getWorkingDir().getAbsolutePath() + File.separatorChar + "PR131505" + File.separatorChar + "bin";
		// aop.xml file shouldn't contain any aspects
		checkXMLAspectCount("PR131505", "", 0, outputDir);
		// add a new aspect A which should be included in the aop.xml file
		alter("PR131505", "inc1");
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505", "", 1, outputDir);
		checkXMLAspectCount("PR131505", "A", 1, outputDir);
		// make changes to the class file which shouldn't affect the contents
		// of the aop.xml file
		alter("PR131505", "inc2");
		build("PR131505");
		checkWasntFullBuild();
		checkXMLAspectCount("PR131505", "", 1, outputDir);
		checkXMLAspectCount("PR131505", "A", 1, outputDir);
		// add another new aspect A1 which should also be included in the
		// aop.xml file
		// ...there should be no duplicate entries in the file
		alter("PR131505", "inc3");
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505", "", 2, outputDir);
		checkXMLAspectCount("PR131505", "A1", 1, outputDir);
		checkXMLAspectCount("PR131505", "A", 1, outputDir);
		// delete aspect A1 which meanss that aop.xml file should only contain A
		File a1 = new File(getWorkingDir().getAbsolutePath() + File.separatorChar + "PR131505" + File.separatorChar + "A1.aj");
		a1.delete();
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505", "", 1, outputDir);
		checkXMLAspectCount("PR131505", "A1", 0, outputDir);
		checkXMLAspectCount("PR131505", "A", 1, outputDir);
		// add another aspect called A which is in a different package, both A
		// and pkg.A should be included in the aop.xml file
		alter("PR131505", "inc4");
		build("PR131505");
		checkWasFullBuild();
		checkXMLAspectCount("PR131505", "", 2, outputDir);
		checkXMLAspectCount("PR131505", "A", 1, outputDir);
		checkXMLAspectCount("PR131505", "pkg.A", 1, outputDir);
	}

	public void testPr136585() {
		initialiseProject("PR136585");
		build("PR136585");
		alter("PR136585", "inc1");
		build("PR136585");
		assertTrue("There should be no errors reported:\n" + getErrorMessages("PR136585"), getErrorMessages("PR136585").isEmpty());
	}

	public void testPr133532() {
		initialiseProject("PR133532");
		build("PR133532");
		alter("PR133532", "inc1");
		build("PR133532");
		alter("PR133532", "inc2");
		build("PR133532");
		assertTrue("There should be no errors reported:\n" + getErrorMessages("PR133532"), getErrorMessages("PR133532").isEmpty());
	}

	public void testPr133532_2() {
		initialiseProject("pr133532_2");
		build("pr133532_2");
		alter("pr133532_2", "inc2");
		build("pr133532_2");
		assertTrue("There should be no errors reported:\n" + getErrorMessages("pr133532_2"), getErrorMessages("pr133532_2")
				.isEmpty());
		String decisions = AjdeInteractionTestbed.MyStateListener.getDecisions();
		String expect = "Need to recompile 'A.aj'";
		assertTrue("Couldn't find build decision: '" + expect + "' in the list of decisions made:\n" + decisions,
				decisions.contains(expect));
	}

	public void testPr133532_3() {
		initialiseProject("PR133532_3");
		build("PR133532_3");
		alter("PR133532_3", "inc1");
		build("PR133532_3");
		assertTrue("There should be no errors reported:\n" + getErrorMessages("PR133532_3"), getErrorMessages("PR133532_3")
				.isEmpty());
	}

	public void testPr134541() {
		initialiseProject("PR134541");
		build("PR134541");
		assertEquals("[Xlint:adviceDidNotMatch] should be associated with line 5", 5, (getWarningMessages("PR134541").get(0))
				.getSourceLocation().getLine());
		alter("PR134541", "inc1");
		build("PR134541");
		// if (getModelFor("PR134541").getHandleProvider().dependsOnLocation())
		// checkWasFullBuild(); // the line number has changed... but nothing
		// // structural about the code
		// else
		checkWasntFullBuild(); // the line number has changed... but nothing
		// structural about the code
		assertEquals("[Xlint:adviceDidNotMatch] should now be associated with line 7", 7, (getWarningMessages("PR134541").get(0))
				.getSourceLocation().getLine());
	}

	public void testJDTLikeHandleProviderWithLstFile_pr141730() {
		// IElementHandleProvider handleProvider =
		// AsmManager.getDefault().getHandleProvider();
		// AsmManager.getDefault().setHandleProvider(new
		// JDTLikeHandleProvider());
		// try {
		// The JDTLike-handles should start with the name
		// of the buildconfig file
		initialiseProject("JDTLikeHandleProvider");
		build("JDTLikeHandleProvider");
		IHierarchy top = getModelFor("JDTLikeHandleProvider").getHierarchy();
		IProgramElement pe = top.findElementForType("pkg", "A");
		String expectedHandle = "=JDTLikeHandleProvider<pkg*A.aj'A";
		assertEquals("expected handle to be " + expectedHandle + ", but found " + pe.getHandleIdentifier(), expectedHandle,
				pe.getHandleIdentifier());
		// } finally {
		// AsmManager.getDefault().setHandleProvider(handleProvider);
		// }
	}

	public void testMovingAdviceDoesntChangeHandles_pr141730() {
		// IElementHandleProvider handleProvider =
		// AsmManager.getDefault().getHandleProvider();
		// AsmManager.getDefault().setHandleProvider(new
		// JDTLikeHandleProvider());
		// try {
		initialiseProject("JDTLikeHandleProvider");
		build("JDTLikeHandleProvider");
		checkWasFullBuild();
		IHierarchy top = getModelFor("JDTLikeHandleProvider").getHierarchy();
		IProgramElement pe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.ADVICE, "before(): <anonymous pointcut>");
		// add a line which shouldn't change the handle
		alter("JDTLikeHandleProvider", "inc1");
		build("JDTLikeHandleProvider");
		checkWasntFullBuild();
		IHierarchy top2 = getModelFor("JDTLikeHandleProvider").getHierarchy();
		IProgramElement pe2 = top
				.findElementForLabel(top2.getRoot(), IProgramElement.Kind.ADVICE, "before(): <anonymous pointcut>");
		assertEquals("expected advice to be on line " + pe.getSourceLocation().getLine() + 1 + " but was on "
				+ pe2.getSourceLocation().getLine(), pe.getSourceLocation().getLine() + 1, pe2.getSourceLocation().getLine());
		assertEquals(
				"expected advice to have handle " + pe.getHandleIdentifier() + " but found handle " + pe2.getHandleIdentifier(),
				pe.getHandleIdentifier(), pe2.getHandleIdentifier());
		// } finally {
		// AsmManager.getDefault().setHandleProvider(handleProvider);
		// }
	}

	public void testSwappingAdviceAndHandles_pr141730() {
		// IElementHandleProvider handleProvider =
		// AsmManager.getDefault().getHandleProvider();
		// AsmManager.getDefault().setHandleProvider(new
		// JDTLikeHandleProvider());
		// try {
		initialiseProject("JDTLikeHandleProvider");
		build("JDTLikeHandleProvider");
		IHierarchy top = getModelFor("JDTLikeHandleProvider").getHierarchy();

		IProgramElement call = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.ADVICE, "after(): callPCD..");
		IProgramElement exec = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.ADVICE, "after(): execPCD..");
		// swap the two after advice statements over. This forces
		// a full build which means 'after(): callPCD..' will now
		// be the second after advice in the file and have the same
		// handle as 'after(): execPCD..' originally did.
		alter("JDTLikeHandleProvider", "inc2");
		build("JDTLikeHandleProvider");
		checkWasFullBuild();

		IHierarchy top2 = getModelFor("JDTLikeHandleProvider").getHierarchy();
		IProgramElement newCall = top2.findElementForLabel(top2.getRoot(), IProgramElement.Kind.ADVICE, "after(): callPCD..");
		IProgramElement newExec = top2.findElementForLabel(top2.getRoot(), IProgramElement.Kind.ADVICE, "after(): execPCD..");

		assertEquals("after swapping places, expected 'after(): callPCD..' " + "to be on line "
				+ newExec.getSourceLocation().getLine() + " but was on line " + call.getSourceLocation().getLine(), newExec
				.getSourceLocation().getLine(), call.getSourceLocation().getLine());
		assertEquals("after swapping places, expected 'after(): callPCD..' " + "to have handle " + exec.getHandleIdentifier()
				+ " (because was full build) but had " + newCall.getHandleIdentifier(), exec.getHandleIdentifier(),
				newCall.getHandleIdentifier());
		// } finally {
		// AsmManager.getDefault().setHandleProvider(handleProvider);
		// }
	}

	public void testInitializerCountForJDTLikeHandleProvider_pr141730() {
		// IElementHandleProvider handleProvider =
		// AsmManager.getDefault().getHandleProvider();
		// AsmManager.getDefault().setHandleProvider(new
		// JDTLikeHandleProvider());
		// try {
		initialiseProject("JDTLikeHandleProvider");
		build("JDTLikeHandleProvider");
		String expected = "=JDTLikeHandleProvider<pkg*A.aj[C|1";

		IHierarchy top = getModelFor("JDTLikeHandleProvider").getHierarchy();
		IProgramElement init = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.INITIALIZER, "...");
		assertEquals("expected initializers handle to be " + expected + "," + " but found " + init.getHandleIdentifier(true),
				expected, init.getHandleIdentifier(true));

		alter("JDTLikeHandleProvider", "inc2");
		build("JDTLikeHandleProvider");
		checkWasFullBuild();

		IHierarchy top2 = getModelFor("JDTLikeHandleProvider").getHierarchy();
		IProgramElement init2 = top2.findElementForLabel(top2.getRoot(), IProgramElement.Kind.INITIALIZER, "...");
		assertEquals(
				"expected initializers handle to still be " + expected + "," + " but found " + init2.getHandleIdentifier(true),
				expected, init2.getHandleIdentifier(true));

		// } finally {
		// AsmManager.getDefault().setHandleProvider(handleProvider);
		// }
	}

	// 134471 related tests perform incremental compilation and verify features
	// of the structure model post compile
	public void testPr134471_IncrementalCompilationAndModelUpdates() {
		try {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=false;

			// Step1. Build the code, simple advice from aspect A onto class C
			initialiseProject("PR134471");
			configureNonStandardCompileOptions("PR134471", "-showWeaveInfo -emacssym");
			configureShowWeaveInfoMessages("PR134471", true);
			build("PR134471");
			AsmManager model = getModelFor("PR134471");
			// Step2. Quick check that the advice points to something...
			IProgramElement nodeForTypeA = checkForNode(model, "pkg", "A", true);
			IProgramElement nodeForAdvice = findAdvice(nodeForTypeA);
			List<String> relatedElements = getRelatedElements(model, nodeForAdvice, 1);

			// Step3. Check the advice applying at the first 'code' join point
			// in pkg.C is from aspect pkg.A, line 7
			IProgramElement programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true)));
			int line = programElement.getSourceLocation().getLine();
			assertTrue("advice should be at line 7 - but is at line " + line, line == 7);

			// Step4. Simulate the aspect being saved but with no change at all
			// in it
			alter("PR134471", "inc1");
			build("PR134471");
			model = getModelFor("PR134471");

			// Step5. Quick check that the advice points to something...
			nodeForTypeA = checkForNode(model, "pkg", "A", true);
			nodeForAdvice = findAdvice(nodeForTypeA);
			relatedElements = getRelatedElements(model, nodeForAdvice, 1);

			// Step6. Check the advice applying at the first 'code' join point
			// in pkg.C is from aspect pkg.A, line 7
			programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true)));
			line = programElement.getSourceLocation().getLine();
			assertTrue("advice should be at line 7 - but is at line " + line, line == 7);
		} finally {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=true;
		}
	}

	// now the advice moves down a few lines - hopefully the model will
	// notice... see discussion in 134471
	public void testPr134471_MovingAdvice() {

		// Step1. build the project
		initialiseProject("PR134471_2");
		configureNonStandardCompileOptions("PR134471_2", "-showWeaveInfo -emacssym");
		configureShowWeaveInfoMessages("PR134471_2", true);
		build("PR134471_2");
		AsmManager model = getModelFor("PR134471_2");
		// Step2. confirm advice is from correct location
		IProgramElement programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true)));
		int line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 7 - but is at line " + line, line == 7);

		// Step3. No structural change to the aspect but the advice has moved
		// down a few lines... (change in source location)
		alter("PR134471_2", "inc1");
		build("PR134471_2");
		model = getModelFor("PR134471_2");
		checkWasntFullBuild(); // the line number has changed... but nothing
		// structural about the code

		// checkWasFullBuild(); // this is true whilst we consider
		// sourcelocation in the type/shadow munger equals() method - have
		// to until the handles are independent of location

		// Step4. Check we have correctly realised the advice moved to line 11
		programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true)));
		line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 11 - but is at line " + line, line == 11);
	}

	public void testAddingAndRemovingDecwWithStructureModel() {
		initialiseProject("P3");
		build("P3");
		alter("P3", "inc1");
		build("P3");
		assertTrue("There should be no exceptions handled:\n" + getCompilerErrorMessages("P3"), getCompilerErrorMessages("P3")
				.isEmpty());
		alter("P3", "inc2");
		build("P3");
		assertTrue("There should be no exceptions handled:\n" + getCompilerErrorMessages("P3"), getCompilerErrorMessages("P3")
				.isEmpty());
	}

	// same as first test with an extra stage that asks for C to be recompiled,
	// it should still be advised...
	public void testPr134471_IncrementallyRecompilingTheAffectedClass() {
		try {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=false;
			// Step1. build the project
			initialiseProject("PR134471");
			configureNonStandardCompileOptions("PR134471", "-showWeaveInfo -emacssym");
			configureShowWeaveInfoMessages("PR134471", true);
			build("PR134471");
			AsmManager model = getModelFor("PR134471");
			// Step2. confirm advice is from correct location
			IProgramElement programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true)));
			int line = programElement.getSourceLocation().getLine();
			assertTrue("advice should be at line 7 - but is at line " + line, line == 7);

			// Step3. No change to the aspect at all
			alter("PR134471", "inc1");
			build("PR134471");
			model = getModelFor("PR134471");
			// Step4. Quick check that the advice points to something...
			IProgramElement nodeForTypeA = checkForNode(model, "pkg", "A", true);
			IProgramElement nodeForAdvice = findAdvice(nodeForTypeA);
			List<String> relatedElements = getRelatedElements(model, nodeForAdvice, 1);

			// Step5. No change to the file C but it should still be advised
			// afterwards
			alter("PR134471", "inc2");
			build("PR134471");
			checkWasntFullBuild();
			model = getModelFor("PR134471");

			// Step6. confirm advice is from correct location
			programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true)));
			line = programElement.getSourceLocation().getLine();
			assertTrue("advice should be at line 7 - but is at line " + line, line == 7);
		} finally {
			// see pr148027 AsmHierarchyBuilder.shouldAddUsesPointcut=true;
		}

	}

	// similar to previous test but with 'declare warning' as well as advice
	public void testPr134471_IncrementallyRecompilingAspectContainingDeclare() {

		// Step1. build the project
		initialiseProject("PR134471_3");
		configureNonStandardCompileOptions("PR134471_3", "-showWeaveInfo -emacssym");
		configureShowWeaveInfoMessages("PR134471_3", true);
		build("PR134471_3");
		checkWasFullBuild();

		AsmManager model = getModelFor("PR134471_3");
		// Step2. confirm declare warning is from correct location, decw matches
		// line 7 in pkg.C
		IProgramElement programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true), 7));
		int line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 10 - but is at line " + line, line == 10);

		// Step3. confirm advice is from correct location, advice matches line 6
		// in pkg.C
		programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true), 6));
		line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 7 - but is at line " + line, line == 7);

		// Step4. Move declare warning in the aspect
		alter("PR134471_3", "inc1");
		build("PR134471_3");
		model = getModelFor("PR134471_3");
		checkWasntFullBuild(); // the line number has changed... but nothing
		// structural about the code

		// checkWasFullBuild();

		// Step5. confirm declare warning is from correct location, decw (now at
		// line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true), 7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line " + line, line == 12);

		// Step6. Now just simulate 'resave' of the aspect, nothing has changed
		alter("PR134471_3", "inc2");
		build("PR134471_3");
		checkWasntFullBuild();
		model = getModelFor("PR134471_3");
		// Step7. confirm declare warning is from correct location, decw (now at
		// line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true), 7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line " + line, line == 12);
	}

	// similar to previous test but with 'declare warning' as well as advice
	public void testPr134471_IncrementallyRecompilingTheClassAffectedByDeclare() {

		// Step1. build the project
		initialiseProject("PR134471_3");
		configureNonStandardCompileOptions("PR134471_3", "-showWeaveInfo -emacssym");
		configureShowWeaveInfoMessages("PR134471_3", true);
		build("PR134471_3");
		checkWasFullBuild();
		AsmManager model = getModelFor("PR134471_3");
		// Step2. confirm declare warning is from correct location, decw matches
		// line 7 in pkg.C
		IProgramElement programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true), 7));
		int line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 10 - but is at line " + line, line == 10);

		// Step3. confirm advice is from correct location, advice matches line 6
		// in pkg.C
		programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true), 6));
		line = programElement.getSourceLocation().getLine();
		assertTrue("advice should be at line 7 - but is at line " + line, line == 7);

		// Step4. Move declare warning in the aspect
		alter("PR134471_3", "inc1");
		build("PR134471_3");
		model = getModelFor("PR134471_3");
		checkWasntFullBuild(); // the line number has changed... but nothing
		// structural about the code

		// checkWasFullBuild();

		// Step5. confirm declare warning is from correct location, decw (now at
		// line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true), 7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line " + line, line == 12);

		// Step6. Now just simulate 'resave' of the aspect, nothing has changed
		alter("PR134471_3", "inc2");
		build("PR134471_3");
		checkWasntFullBuild();
		model = getModelFor("PR134471_3");
		// Step7. confirm declare warning is from correct location, decw (now at
		// line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true), 7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line " + line, line == 12);

		// Step8. Now just simulate resave of the pkg.C type - no change at
		// all... are relationships gonna be repaired OK?
		alter("PR134471_3", "inc3");
		build("PR134471_3");
		checkWasntFullBuild();

		// Step9. confirm declare warning is from correct location, decw (now at
		// line 12) in pkg.A matches line 7 in pkg.C
		programElement = getFirstRelatedElement(model, findCode(checkForNode(model, "pkg", "C", true), 7));
		line = programElement.getSourceLocation().getLine();
		assertTrue("declare warning should be at line 12 - but is at line " + line, line == 12);
	}

	public void testDontLoseXlintWarnings_pr141556() {
		initialiseProject("PR141556");
		configureNonStandardCompileOptions("PR141556", "-Xlint:warning");
		build("PR141556");
		checkWasFullBuild();
		String warningMessage = "can not build thisJoinPoint " + "lazily for this advice since it has no suitable guard "
				+ "[Xlint:noGuardForLazyTjp]";
		assertEquals("warning message should be '" + warningMessage + "'", warningMessage,
				(getWarningMessages("PR141556").get(0)).getMessage());

		// add a space to the Aspect but dont do a build
		alter("PR141556", "inc1");
		// remove the space so that the Aspect is exactly as it was
		alter("PR141556", "inc2");
		// build the project and we should not have lost the xlint warning
		build("PR141556");
		checkWasntFullBuild();
		assertTrue("there should still be a warning message ", !getWarningMessages("PR141556").isEmpty());
		assertEquals("warning message should be '" + warningMessage + "'", warningMessage,
				(getWarningMessages("PR141556").get(0)).getMessage());
	}

	public void testAdviceDidNotMatch_pr152589() {
		initialiseProject("PR152589");
		build("PR152589");
		List<IMessage> warnings = getWarningMessages("PR152589");
		assertTrue("There should be no warnings:\n" + warnings, warnings.isEmpty());
		alter("PR152589", "inc1");
		build("PR152589");
		checkWasntFullBuild(); // the line number has changed... but nothing
		// structural about the code

		// checkWasFullBuild();
		warnings = getWarningMessages("PR152589");
		assertTrue("There should be no warnings after adding a whitespace:\n" + warnings, warnings.isEmpty());
	}

	// see comment #11 of bug 154054
	public void testNoFullBuildOnChangeInSysOutInAdviceBody_pr154054() {
		initialiseProject("PR154054");
		build("PR154054");
		alter("PR154054", "inc1");
		build("PR154054");
		checkWasntFullBuild();
	}

	public void testIncrementalBuildAdviceChange_456801() throws Exception {
		initialiseProject("456801");
		build("456801");
		String output = runMethod("456801", "Code", "run");
		assertEquals("advice runnning\nrun() running\n",output.replace("\r",""));
		alter("456801", "inc1");
		build("456801");
		output = runMethod("456801", "Code", "run");
		assertEquals("advice running\nrun() running\n",output.replace("\r",""));
		checkCompileWeaveCount("456801", 1, 1);
		checkWasntFullBuild();
	}

	// change exception type in around advice, does it notice?
	public void testShouldFullBuildOnExceptionChange_pr154054() {
		initialiseProject("PR154054_2");
		build("PR154054_2");
		alter("PR154054_2", "inc1");
		build("PR154054_2");
		checkWasFullBuild();
	}

	public void testPR158573() {
		// IElementHandleProvider handleProvider =
		// AsmManager.getDefault().getHandleProvider();
		// AsmManager.getDefault().setHandleProvider(new
		// JDTLikeHandleProvider());
		initialiseProject("PR158573");
		build("PR158573");
		List warnings = getWarningMessages("PR158573");
		assertTrue("There should be no warnings:\n" + warnings, warnings.isEmpty());
		alter("PR158573", "inc1");
		build("PR158573");

		checkWasntFullBuild();
		warnings = getWarningMessages("PR158573");
		assertTrue("There should be no warnings after changing the value of a " + "variable:\n" + warnings, warnings.isEmpty());
		// AsmManager.getDefault().setHandleProvider(handleProvider);
	}

	/**
	 * If the user has specified that they want Java 6 compliance and kept the default classfile and source file level settings
	 * (also 6.0) then expect an error saying that we don't support java 6.
	 */
	public void testPR164384_1() {
		initialiseProject("PR164384");

		Map<String, String> javaOptions = new Hashtable<>();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.source", "1.6");
		configureJavaOptionsMap("PR164384", javaOptions);

		build("PR164384");
		List<IMessage> errors = getErrorMessages("PR164384");

		if (getCompilerForProjectWithName("PR164384").isJava6Compatible()) {
			assertTrue("There should be no errors:\n" + errors, errors.isEmpty());
		} else {
			String expectedError = "Java 6.0 compliance level is unsupported";
			String found = errors.get(0).getMessage();
			assertEquals("Expected 'Java 6.0 compliance level is unsupported'" + " error message but found " + found,
					expectedError, found);
			// This is because the 'Java 6.0 compliance' error is an 'error'
			// rather than an 'abort'. Aborts are really for compiler
			// exceptions.
			assertTrue("expected there to be more than the one compliance level" + " error but only found that one",
					errors.size() > 1);
		}

	}

	/**
	 * If the user has specified that they want Java 6 compliance and selected classfile and source file level settings to be 5.0
	 * then expect an error saying that we don't support java 6.
	 */
	public void testPR164384_2() {
		initialiseProject("PR164384");

		Map<String, String> javaOptions = new Hashtable<>();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.5");
		javaOptions.put("org.eclipse.jdt.core.compiler.source", "1.5");
		configureJavaOptionsMap("PR164384", javaOptions);

		build("PR164384");
		List<IMessage> errors = getErrorMessages("PR164384");
		if (getCompilerForProjectWithName("PR164384").isJava6Compatible()) {
			assertTrue("There should be no errors:\n" + errors, errors.isEmpty());
		} else {
			String expectedError = "Java 6.0 compliance level is unsupported";
			String found = errors.get(0).getMessage();
			assertEquals("Expected 'Java 6.0 compliance level is unsupported'" + " error message but found " + found,
					expectedError, found);
			// This is because the 'Java 6.0 compliance' error is an 'error'
			// rather than an 'abort'. Aborts are really for compiler
			// exceptions.
			assertTrue("expected there to be more than the one compliance level" + " error but only found that one",
					errors.size() > 1);
		}
	}

	/**
	 * If the user has specified that they want Java 6 compliance and set the classfile level to be 6.0 and source file level to be
	 * 5.0 then expect an error saying that we don't support java 6.
	 */
	public void testPR164384_3() {
		initialiseProject("PR164384");

		Map<String, String> javaOptions = new Hashtable<>();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.source", "1.5");
		configureJavaOptionsMap("PR164384", javaOptions);

		build("PR164384");
		List<IMessage> errors = getErrorMessages("PR164384");

		if (getCompilerForProjectWithName("PR164384").isJava6Compatible()) {
			assertTrue("There should be no errros:\n" + errors, errors.isEmpty());
		} else {
			String expectedError = "Java 6.0 compliance level is unsupported";
			String found = errors.get(0).getMessage();
			assertEquals("Expected 'Java 6.0 compliance level is unsupported'" + " error message but found " + found,
					expectedError, found);
			// This is because the 'Java 6.0 compliance' error is an 'error'
			// rather than an 'abort'. Aborts are really for compiler
			// exceptions.
			assertTrue("expected there to be more than the one compliance level" + " error but only found that one",
					errors.size() > 1);
		}
	}

	public void testPr168840() throws Exception {
		initialiseProject("inpathTesting");

		String inpathTestingDir = getWorkingDir() + File.separator + "inpathTesting";
		String inpathDir = inpathTestingDir + File.separator + "injarBin" + File.separator + "pkg";
		String expectedOutputDir = inpathTestingDir + File.separator + "bin";

		// set up the inpath to have the directory on it's path
		File f = new File(inpathDir);
		Set<File> s = new HashSet<>();
		s.add(f);
		configureInPath("inpathTesting", s);
		build("inpathTesting");
		// the declare warning matches one place so expect one warning message
		List<IMessage> warnings = getWarningMessages("inpathTesting");
		assertTrue("Expected there to be one warning message but found " + warnings.size() + ": " + warnings, warnings.size() == 1);

		// copy over the updated version of the inpath class file
		File from = new File(testdataSrcDir + File.separatorChar + "inpathTesting" + File.separatorChar + "newInpathClass"
				+ File.separatorChar + "InpathClass.class");
		File destination = new File(inpathDir + File.separatorChar + "InpathClass.class");
		FileUtil.copyFile(from, destination);

		build("inpathTesting");
		checkWasntFullBuild();
		// the newly copied inpath class means the declare warning now matches
		// two
		// places, therefore expect two warning messages
		warnings = getWarningMessages("inpathTesting");
		assertTrue("Expected there to be two warning message but found " + warnings.size() + ": " + warnings, warnings.size() == 2);
	}

	// warning about cant change parents of Object is fine
	public void testInpathHandles_271201() throws Exception {
		String p = "inpathHandles";
		initialiseProject(p);

		String inpathTestingDir = getWorkingDir() + File.separator + "inpathHandles";
		String inpathDir = inpathTestingDir + File.separator + "binpath";

		// set up the inpath to have the directory on it's path
		System.out.println(inpathDir);
		File f = new File(inpathDir);
		Set<File> s = new HashSet<>();
		s.add(f);
		configureInPath(p, s);
		build(p);

		IProgramElement root = getModelFor(p).getHierarchy().getRoot();

		// alter(p,"inc1");
		// build(p);
		dumptree(root, 0);
		PrintWriter pw = new PrintWriter(System.out);
		try {
			getModelFor(p).dumprels(pw);
			pw.flush();
		} catch (Exception e) {
		}
		List<IRelationship> l = getModelFor(p).getRelationshipMap().get("=inpathHandles/;<codep(Code.class[Code");
		assertNotNull(l);
	}

	// warning about cant change parents of Object is fine
	public void testInpathHandles_IncrementalCompilation_271201() throws Exception {
		String p = "inpathHandles";
		initialiseProject(p);

		String inpathTestingDir = getWorkingDir() + File.separator + "inpathHandles";
		String inpathDir = inpathTestingDir + File.separator + "binpath";

		// set up the inpath to have the directory on it's path
		File f = new File(inpathDir);
		Set<File> s = new HashSet<>();
		s.add(f);
		configureInPath(p, s);

		// This build will weave a declare parents into the inpath class codep.Code
		build(p);
		assertNotNull(getModelFor(p).getRelationshipMap().get("=inpathHandles/;<codep(Code.class[Code"));

		IProgramElement root = getModelFor(p).getHierarchy().getRoot();

		// This alteration introduces a new source file B.java, the build should not
		// damage phantom handle based relationships
		alter(p, "inc1");
		build(p);
		assertNotNull(getModelFor(p).getRelationshipMap().get("=inpathHandles/;<codep(Code.class[Code"));
		assertNotNull(getModelFor(p).getRelationshipMap().get("=inpathHandles<p{B.java[B"));

		// This alteration removes B.java, the build should not damage phantom handle based relationships
		String fileB = getWorkingDir().getAbsolutePath() + File.separatorChar + "inpathHandles" + File.separatorChar + "src"
				+ File.separatorChar + "p" + File.separatorChar + "B.java";
		(new File(fileB)).delete();
		build(p);
		assertNotNull(getModelFor(p).getRelationshipMap().get("=inpathHandles/;<codep(Code.class[Code"));
		assertNull(getModelFor(p).getRelationshipMap().get("=inpathHandles<p{B.java[B"));
	}

	public void testInpathHandles_WithInpathMap_271201() throws Exception {
		String p = "inpathHandles";
		initialiseProject(p);

		String inpathTestingDir = getWorkingDir() + File.separator + "inpathHandles";
		String inpathDir = inpathTestingDir + File.separator + "binpath";// + File.separator+ "codep";
		// String expectedOutputDir = inpathTestingDir + File.separator + "bin";

		// set up the inpath to have the directory on it's path
		System.out.println(inpathDir);
		File f = new File(inpathDir);
		Set<File> s = new HashSet<>();
		s.add(f);
		Map<File, String> m = new HashMap<>();
		m.put(f, "wibble");
		configureOutputLocationManager(p, new TestOutputLocationManager(getProjectRelativePath(p, ".").toString(), m));

		configureInPath(p, s);
		build(p);

		IProgramElement root = getModelFor(p).getHierarchy().getRoot();

		// alter(p,"inc1");
		// build(p);
		dumptree(root, 0);
		PrintWriter pw = new PrintWriter(System.out);
		try {
			getModelFor(p).dumprels(pw);
			pw.flush();
		} catch (Exception e) {
		}
		List<IRelationship> l = getModelFor(p).getRelationshipMap().get("=inpathHandles/;wibble<codep(Code.class[Code");
		assertNotNull(l);
	}

	private void printModelAndRelationships(String p) {
		IProgramElement root = getModelFor(p).getHierarchy().getRoot();

		dumptree(root, 0);
		PrintWriter pw = new PrintWriter(System.out);
		try {
			getModelFor(p).dumprels(pw);
			pw.flush();
		} catch (Exception e) {
		}
	}

	public void testInpathHandles_IncrementalCompilation_RemovingInpathEntries_271201() throws Exception {
		String p = "inpathHandles2";
		initialiseProject(p);

		String inpathDir = getWorkingDir() + File.separator + "inpathHandles2" + File.separator + "binpath";

		// set up the inpath to have the directory on it's path
		File f = new File(inpathDir);
		configureInPath(p, f);

		// This build will weave a declare parents into the inpath class codep.A and codep.B
		build(p);
		assertNotNull(getModelFor(p).getRelationshipMap().get("=inpathHandles2/;<codep(A.class[A"));

		// Not let us delete one of the inpath .class files
		assertTrue(new File(inpathDir, "codep" + File.separator + "A.class").delete());
		setNextChangeResponse(p, ICompilerConfiguration.EVERYTHING);
		build(p);
		// printModelAndRelationships(p);
	}

	// warning about cant change parents of Object is fine
	// public void testInpathJars_271201() throws Exception {
	// AjdeInteractionTestbed.VERBOSE = true;
	// String p = "inpathJars";
	// initialiseProject(p);
	//
	// String inpathTestingDir = getWorkingDir() + File.separator + "inpathJars";
	// String inpathDir = inpathTestingDir + File.separator + "code.jar";
	// // String expectedOutputDir = inpathTestingDir + File.separator + "bin";
	//
	// // set up the inpath to have the directory on it's path
	// File f = new File(inpathDir);
	// Set s = new HashSet();
	// s.add(f);
	// Map m = new HashMap();
	// m.put(f, "Gibble");
	// configureOutputLocationManager(p, new TestOutputLocationManager(getProjectRelativePath(p, ".").toString(), m));
	// configureInPath(p, s);
	// build(p);
	//
	// // alter(p,"inc1");
	// // build(p);
	// List l = getModelFor(p).getRelationshipMap().get("=inpathJars/,Gibble<codep(Code.class[Code");
	// assertNotNull(l);
	// }

	// --- helper code ---

	/**
	 * Retrieve program elements related to this one regardless of the relationship. A JUnit assertion is made that the number that
	 * the 'expected' number are found.
	 *
	 * @param programElement Program element whose related elements are to be found
	 * @param expected the number of expected related elements
	 */
	private List<String> getRelatedElements(AsmManager model, IProgramElement programElement, int expected) {
		List<String> relatedElements = getRelatedElements(model, programElement);
		StringBuffer debugString = new StringBuffer();
		if (relatedElements != null) {
			for (String element : relatedElements) {
				debugString.append(model.getHierarchy().findElementForHandle(element).toLabelString()).append("\n");
			}
		}
		assertTrue("Should be " + expected + " element" + (expected > 1 ? "s" : "") + " related to this one '" + programElement
				+ "' but found :\n " + debugString, relatedElements != null && relatedElements.size() == 1);
		return relatedElements;
	}

	private IProgramElement getFirstRelatedElement(AsmManager model, IProgramElement programElement) {
		List<String> rels = getRelatedElements(model, programElement, 1);
		return model.getHierarchy().findElementForHandle(rels.get(0));
	}

	private List<String> getRelatedElements(AsmManager model, IProgramElement advice) {
		List<String> output = null;
		IRelationshipMap map = model.getRelationshipMap();
		List<IRelationship> rels = map.get(advice);
		if (rels == null) {
			fail("Did not find any related elements!");
		}
		for (IRelationship element : rels) {
			List<String> targets = element.getTargets();
			if (output == null) {
				output = new ArrayList<>();
			}
			output.addAll(targets);
		}
		return output;
	}

	private IProgramElement findAdvice(IProgramElement ipe) {
		return findAdvice(ipe, 1);
	}

	private IProgramElement findAdvice(IProgramElement ipe, int whichOne) {
		if (ipe.getKind() == IProgramElement.Kind.ADVICE) {
			whichOne = whichOne - 1;
			if (whichOne == 0) {
				return ipe;
			}
		}
		List<IProgramElement> kids = ipe.getChildren();
		for (IProgramElement kid: kids) {
			IProgramElement found = findAdvice(kid, whichOne);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Finds the first 'code' program element below the element supplied - will return null if there aren't any
	 */
	private IProgramElement findCode(IProgramElement ipe) {
		return findCode(ipe, -1);
	}

	/**
	 * Searches a hierarchy of program elements for a 'code' element at the specified line number, a line number of -1 means just
	 * return the first one you find
	 */
	private IProgramElement findCode(IProgramElement ipe, int linenumber) {
		if (ipe.getKind() == IProgramElement.Kind.CODE) {
			if (linenumber == -1 || ipe.getSourceLocation().getLine() == linenumber) {
				return ipe;
			}
		}
		List<IProgramElement> kids = ipe.getChildren();
		for (IProgramElement kid: kids) {
			IProgramElement found = findCode(kid, linenumber);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	// other possible tests:
	// - memory usage (freemem calls?)
	// - relationship map

	// --------------------------------------------------------------------------
	// -------------------------

	private IProgramElement checkForNode(AsmManager model, String packageName, String typeName, boolean shouldBeFound) {
		IProgramElement ipe = model.getHierarchy().findElementForType(packageName, typeName);
		if (shouldBeFound) {
			if (ipe == null) {
				printModel(model);
			}
			assertTrue("Should have been able to find '" + packageName + "." + typeName + "' in the asm", ipe != null);
		} else {
			if (ipe != null) {
				printModel(model);
			}
			assertTrue("Should have NOT been able to find '" + packageName + "." + typeName + "' in the asm", ipe == null);
		}
		return ipe;
	}

	private void printModel(AsmManager model) {
		try {
			AsmManager.dumptree(model.getHierarchy().getRoot(), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void log(String msg) {
		if (VERBOSE) {
			System.out.println(msg);
		}
	}

}