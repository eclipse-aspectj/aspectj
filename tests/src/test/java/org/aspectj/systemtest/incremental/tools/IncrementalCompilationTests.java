/********************************************************************
 * Copyright (c) 2010 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement (SpringSource)         initial implementation
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.lang.ref.Reference;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.ajde.core.internal.AjdeCoreBuildManager;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.AspectJElementHierarchy;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.World.TypeMap;

/**
 * Incremental compilation tests. MultiProjectIncrementalTests was getting
 * unwieldy - started this new test class for 1.6.10.
 *
 * @author Andy Clement
 * @author Joseph MacFarlane
 * @since 1.6.10
 */
public class IncrementalCompilationTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		TypeMap.useExpendableMap = true;
	}

	public void testAdditionalDependencies328649_1() throws Exception {
		String p = "pr328649_1";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		assertEquals(0, getErrorMessages(p).size());
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		checkCompileWeaveCount(p, 1, 1);
		assertEquals(0, getErrorMessages(p).size());
		alter(p, "inc2");
		AjCompiler compiler = getCompilerForProjectWithName(p);
		String s = getFile(p, "src/B.java");
		assertNotNull(s);
		// add in a dependency where there really isn't one...
		boolean b = compiler.addDependencies(new File(s), new String[] { "A" });
		assertTrue(b);
		build(p);
		checkWasntFullBuild();
		// now A rebuilds, then A and B rebuild due to that extra dependency
		checkCompileWeaveCount(p, 3, 3);
		assertEquals(0, getErrorMessages(p).size());
		alter(p, "inc2");
		compiler = getCompilerForProjectWithName(p);
		s = getFile(p, "src/B.java");
		assertNotNull(s);
	}

	/**
	 * Build a pair of files, then change the throws clause in the first one (add a throws clause where there wasnt one). The second
	 * file should now have a 'unhandled exception' error on it.
	 */
	public void testModifiedThrowsClauseShouldTriggerError_318884() throws Exception {
		String p = "pr318884_1";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("B.java:4:0::0 Unhandled exception type IOException", getErrorMessages(p).get(0));
	}

	public void testITIT_336158() throws Exception {
		String p = "pr336158";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		checkCompileWeaveCount(p, 3, 4);
	}

	public void testITIT_336147() throws Exception {
		String p = "pr336147";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		checkCompileWeaveCount(p, 1, 3);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertNoErrors(p);
		checkCompileWeaveCount(p, 1, 1);
	}

	public void testITIT_336147_2() throws Exception {
		String p = "pr336147_2";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		checkCompileWeaveCount(p, 2, 3);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertNoErrors(p);
		checkCompileWeaveCount(p, 1, 1);
	}

	public void testITIT_336147_3() throws Exception {
		String p = "pr336147_3";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		assertNoErrors(p);
		checkCompileWeaveCount(p, 3, 4);
	}

	public void testDeclareFieldMinus() throws Exception {
		String p = "annoRemoval";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		IProgramElement ipe = null;
		ipe = model.findElementForHandleOrCreate("=annoRemoval<a{Code.java'Remover`declare \\@field", false);
		System.out.println(ipe);
		assertTrue(ipe.isAnnotationRemover());
		String[] annos = ipe.getRemovedAnnotationTypes();
		assertEquals(1, annos.length);
		assertEquals("a.Anno", annos[0]);
		assertNull(ipe.getAnnotationType());
		ipe = model.findElementForHandleOrCreate("=annoRemoval<a{Code.java'Remover`declare \\@field!2", false);
		System.out.println(ipe);
		assertFalse(ipe.isAnnotationRemover());
		assertEquals("a.Anno", ipe.getAnnotationType());
		assertNull(ipe.getRemovedAnnotationTypes());
	}

	/**
	 * Build a pair of files, then change the throws clause in the first one (change the type of the thrown exception). The second
	 * file should now have a 'unhandled exception' error on it.
	 */
	public void testModifiedThrowsClauseShouldTriggerError_318884_2() throws Exception {
		String p = "pr318884_2";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("B.java:4:0::0 Unhandled exception type Exception", getErrorMessages(p).get(0));
	}

	/**
	 * Checking if we have the right information on the member nodes.
	 */
	public void testModelStructure_333123() throws Exception {
		String p = "pr333123";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=false");
		build(p);
		checkWasFullBuild();
		printModel(p);

		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();

		IProgramElement ipe = null;
		// fieldInt [field] 10 hid:=pr333123<a.b{Code.java[Code^fieldInt
		// fieldString [field] 12 hid:=pr333123<a.b{Code.java[Code^fieldString
		// fieldCode [field] 14 hid:=pr333123<a.b{Code.java[Code^fieldCode
		ipe = model.findElementForHandle("=pr333123<a.b{Code.java[Code^fieldInt");
		assertEquals("I", ipe.getCorrespondingTypeSignature());
		assertEquals("int", ipe.getCorrespondingType(true));
		ipe = model.findElementForHandle("=pr333123<a.b{Code.java[Code^fieldString");
		assertEquals("Ljava/lang/String;", ipe.getCorrespondingTypeSignature());
		assertEquals("java.lang.String", ipe.getCorrespondingType(true));
		// assertEquals("Ljava/lang/String;", ipe.getBytecodeSignature());
		ipe = model.findElementForHandle("=pr333123<a.b{Code.java[Code^fieldCode");
		assertEquals("La/b/Code;", ipe.getCorrespondingTypeSignature());
		assertEquals("a.b.Code", ipe.getCorrespondingType(true));
		ipe = model.findElementForHandle("=pr333123<a.b{Code.java[Code^fieldList");
		// assertEquals("La/b/Code;", ipe.getBytecodeSignature());
		assertEquals("Ljava/util/List<La/b/Code;>;", ipe.getCorrespondingTypeSignature());
		assertEquals("java.util.List<a.b.Code>", ipe.getCorrespondingType(true));

		// method(java.lang.String) [method] 4 hid:=pr333123<a.b{Code.java[Code~method~QString;
		// getInt() [method] 6 hid:=pr333123<a.b{Code.java[Code~getInt
		// transform(a.b.Code,java.lang.String,long) [method] 8 hid:=pr333123<a.b{Code.java[Code~transform~QCode;~QString;~J
		ipe = model.findElementForHandle("=pr333123<a.b{Code.java[Code~method~QString;");
		assertEquals("(Ljava/lang/String;)V", ipe.getBytecodeSignature());
		ipe = model.findElementForHandle("=pr333123<a.b{Code.java[Code~getInt");
		assertEquals("()I", ipe.getBytecodeSignature());

		ipe = model.findElementForHandle("=pr333123<a.b{Code.java[Code~transform~QCode;~QString;~J");
		assertEquals("(La/b/Code;Ljava/lang/String;J)La/b/Code;", ipe.getBytecodeSignature());

		List<char[]> paramSigs = ipe.getParameterSignatures();
		assertEquals("La/b/Code;", new String(paramSigs.get(0)));
		assertEquals("Ljava/lang/String;", new String(paramSigs.get(1)));
		assertEquals("J", new String(paramSigs.get(2)));

		assertEquals("a.b.Code", ipe.getCorrespondingType(true));
		assertEquals("La/b/Code;", ipe.getCorrespondingTypeSignature());

		ipe = model.findElementForHandle("=pr333123<a.b{Code.java[Code~transform2~QList\\<QString;>;");
		assertEquals("(Ljava/util/List;)Ljava/util/List;", ipe.getBytecodeSignature());
		paramSigs = ipe.getParameterSignatures();
		assertEquals("Ljava/util/List<Ljava/lang/String;>;", new String(paramSigs.get(0)));
		assertEquals("java.util.List<a.b.Code>", ipe.getCorrespondingType(true));
		assertEquals("Ljava/util/List<La/b/Code;>;", ipe.getCorrespondingTypeSignature());

	}

	// changing method return type parameterization
	public void testModifiedMethodReturnTypeGenericTypeParameterShouldTriggerError_318884_3() throws Exception {
		String p = "pr318884_3";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("The return type is incompatible with B.foo()", getErrorMessages(p).get(0));
	}

	// changing method parameter type parameterization
	public void testModifiedMethodParameterGenericTypeParameterShouldTriggerError_318884_4() throws Exception {
		String p = "pr318884_4";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains(
				"Name clash: The method foo(List<String>) of type A has the same erasure as foo(List<Integer>) of type B but does not override it",
				getErrorMessages(p).get(0));
	}

	// changing constructor parameter type parameterization
	public void testModifiedConstructorParameterGenericTypeParameterShouldTriggerError_318884_5() throws Exception {
		String p = "pr318884_5";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("The constructor B(List<String>) is undefined", getErrorMessages(p).get(0));
	}

	// changing field type parameterization
	public void testModifiedFieldTypeGenericTypeParameterShouldTriggerError_318884_6() throws Exception {
		String p = "pr318884_6";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("Type mismatch: cannot convert from element type Integer to String", getErrorMessages(p).get(0));
	}

	// removing static inner class
	public void testInnerClassChanges_318884_7() throws Exception {
		String p = "pr318884_7";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 3);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("B.C cannot be resolved to a type", getErrorMessages(p).get(0));
	}

	// removing constructor from a static inner class
	public void testInnerClassChanges_318884_9() throws Exception {
		String p = "pr318884_9";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 3);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(1, getErrorMessages(p).size());
		assertContains("The constructor B.C(String) is undefined", getErrorMessages(p).get(0));
	}

	// removing class
	public void testInnerClassChanges_318884_10() throws Exception {
		String p = "pr318884_10";
		initialiseProject(p);
		build(p);
		checkWasFullBuild();
		checkCompileWeaveCount(p, 2, 2);
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		assertEquals(2, getErrorMessages(p).size());
		assertContains("B cannot be resolved to a type", getErrorMessages(p).get(0));
	}

	// deleting unaffected model entries
	public void testDeletion_278496() throws Exception {
		String p = "PR278496_1";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=true");
		build(p);
		checkWasFullBuild();
		// Here is the model without deletion. The node for 'Code.java' can safely be deleted as it contains
		// no types that are the target of relationships.
		// PR278496_1 [build configuration file] hid:=PR278496_1
		// a.b.c [package] hid:=PR278496_1<a.b.c
		// Azpect.java [java source file] 1 hid:=PR278496_1<a.b.c{Azpect.java
		// a.b.c [package declaration] 1 hid:=PR278496_1<a.b.c{Azpect.java%a.b.c
		// [import reference] hid:=PR278496_1<a.b.c{Azpect.java#
		// Azpect [aspect] 3 hid:=PR278496_1<a.b.c{Azpect.java}Azpect
		// before(): <anonymous pointcut> [advice] 4 hid:=PR278496_1<a.b.c{Azpect.java}Azpect&before
		// Code.java [java source file] 1 hid:=PR278496_1<a.b.c{Code.java
		// a.b.c [package declaration] 1 hid:=PR278496_1<a.b.c{Code.java%a.b.c
		// [import reference] hid:=PR278496_1<a.b.c{Code.java#
		// java.util.ArrayList [import reference] 3 hid:=PR278496_1<a.b.c{Code.java#java.util.ArrayList
		// java.util.List [import reference] 2 hid:=PR278496_1<a.b.c{Code.java#java.util.List
		// Code [class] 5 hid:=PR278496_1<a.b.c{Code.java[Code
		// m() [method] 6 hid:=PR278496_1<a.b.c{Code.java[Code~m
		// Code2.java [java source file] 1 hid:=PR278496_1<a.b.c{Code2.java
		// a.b.c [package declaration] 1 hid:=PR278496_1<a.b.c{Code2.java%a.b.c
		// [import reference] hid:=PR278496_1<a.b.c{Code2.java#
		// java.util.ArrayList [import reference] 3 hid:=PR278496_1<a.b.c{Code2.java#java.util.ArrayList
		// java.util.List [import reference] 2 hid:=PR278496_1<a.b.c{Code2.java#java.util.List
		// Code2 [class] 5 hid:=PR278496_1<a.b.c{Code2.java[Code2
		// m() [method] 6 hid:=PR278496_1<a.b.c{Code2.java[Code2~m
		// Hid:1:(targets=1) =PR278496_1<a.b.c{Azpect.java}Azpect&before (advises) =PR278496_1<a.b.c{Code2.java[Code2
		// Hid:2:(targets=1) =PR278496_1<a.b.c{Code2.java[Code2 (advised by) =PR278496_1<a.b.c{Azpect.java}Azpect&before

		// deletion turned on:
		// PR278496_1 [build configuration file] hid:=PR278496_1
		// a.b.c [package] hid:<a.b.c
		// Azpect.java [java source file] 1 hid:<a.b.c{Azpect.java
		// a.b.c [package declaration] 1 hid:<a.b.c{Azpect.java%a.b.c
		// [import reference] hid:<a.b.c{Azpect.java#
		// Azpect [aspect] 3 hid:<a.b.c{Azpect.java}Azpect
		// before(): <anonymous pointcut> [advice] 4 hid:<a.b.c{Azpect.java}Azpect&before
		// Code2.java [java source file] 1 hid:<a.b.c{Code2.java
		// a.b.c [package declaration] 1 hid:<a.b.c{Code2.java%a.b.c
		// [import reference] hid:<a.b.c{Code2.java#
		// java.util.ArrayList [import reference] 3 hid:<a.b.c{Code2.java#java.util.ArrayList
		// java.util.List [import reference] 2 hid:<a.b.c{Code2.java#java.util.List
		// Code2 [class] 5 hid:<a.b.c{Code2.java[Code2
		// m() [method] 6 hid:<a.b.c{Code2.java[Code2~m
		// Hid:1:(targets=1) <a.b.c{Azpect.java}Azpect&before (advises) <a.b.c{Code2.java[Code2
		// Hid:2:(targets=1) <a.b.c{Code2.java[Code2 (advised by) <a.b.c{Azpect.java}Azpect&before

		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		// Node for "Code.java" should not be there:
		IProgramElement ipe = model.findElementForHandleOrCreate("=PR278496_1<a.b.c{Code.java", false);
		assertNull(ipe);
	}

	// inner classes
	public void testDeletion_278496_9() throws Exception {
		String p = "PR278496_9";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=true");
		build(p);
		checkWasFullBuild();
		printModel(p);

		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		// Node for "Code.java" should not be there:
		IProgramElement ipe = model.findElementForHandleOrCreate("=PR278496_9<a.b.c{Code.java", false);
		assertNull(ipe);
	}

	// deleting unaffected model entries
	public void testDeletion_278496_2() throws Exception {
		String p = "PR278496_2";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=true");
		build(p);
		checkWasFullBuild();
		// printModel(p);
		// Here is the model without deletion.
		// PR278496_2 [build configuration file] hid:=PR278496_2
		// [package] hid:=PR278496_2<
		// Azpect.java [java source file] 1 hid:=PR278496_2<{Azpect.java
		// [import reference] hid:=PR278496_2<{Azpect.java#
		// Azpect [aspect] 1 hid:=PR278496_2<{Azpect.java}Azpect
		// Code.m() [inter-type method] 2 hid:=PR278496_2<{Azpect.java}Azpect)Code.m
		// Code.java [java source file] 1 hid:=PR278496_2<{Code.java
		// [import reference] hid:=PR278496_2<{Code.java#
		// Code [class] 1 hid:=PR278496_2<{Code.java[Code
		// Hid:1:(targets=1) =PR278496_2<{Azpect.java}Azpect)Code.m (declared on) =PR278496_2<{Code.java[Code
		// Hid:2:(targets=1) =PR278496_2<{Code.java[Code (aspect declarations) =PR278496_2<{Azpect.java}Azpect)Code.m

		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		// Node for "Code.java" should be there since it is the target of a relationship
		IProgramElement ipe = model.findElementForHandleOrCreate("=PR278496_2<{Code.java", false);
		assertNotNull(ipe);
	}

	public void testWorldDemotion_278496_5() throws Exception {
		String p = "PR278496_5";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true");
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();
		ReferenceTypeDelegate delegate = null;
		delegate = w.resolveToReferenceType("com.Foo").getDelegate();
		ResolvedMember[] fields = delegate.getDeclaredFields();
		assertEquals("int com.Foo.i", fields[0].toString());
		assertEquals("java.lang.String com.Foo.s", fields[1].toString());
		assertEquals("java.util.List com.Foo.ls", fields[2].toString());

		assertEquals("[Anno[Lcom/Anno; rVis]]", stringify(fields[2].getAnnotations()));
		assertNotNull(fields[2].getAnnotationOfType(UnresolvedType.forSignature("Lcom/Anno;")));
		assertNull(fields[2].getAnnotationOfType(UnresolvedType.forSignature("Lcom/Anno2;")));
		assertTrue(fields[2].hasAnnotation(UnresolvedType.forSignature("Lcom/Anno;")));
		assertFalse(fields[2].hasAnnotation(UnresolvedType.forSignature("Ljava/lang/String;")));
		assertEquals(0, fields[1].getAnnotations().length);
		assertEquals("[com.Anno2 com.Anno]", stringify(fields[3].getAnnotationTypes()));
		assertEquals("[]", stringify(fields[1].getAnnotationTypes()));
		assertEquals("[Anno[Lcom/Anno2; rVis a=(int)42] Anno[Lcom/Anno; rVis]]", stringify(fields[3].getAnnotations()));
		assertEquals("[]", stringify(fields[1].getAnnotations()));

		assertEquals("I", fields[0].getSignature());
		assertEquals("Ljava/lang/String;", fields[1].getSignature());
		assertEquals("Ljava/util/List;", fields[2].getSignature());
		assertEquals("Pjava/util/List<Ljava/lang/String;>;", fields[2].getGenericReturnType().getSignature());
		assertEquals("Ljava/util/List;", fields[3].getSignature());
		assertEquals("Pjava/util/List<Ljava/lang/Integer;>;", fields[3].getGenericReturnType().getSignature());
	}

	public void testExpendableMapEntryReplacement() throws Exception {
		String p = "PR278496_5";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true");
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();

		// Hold onto the signature but GC the type...
		String signature = w.resolveToReferenceType("com.Foo").getSignature();
		System.gc();
		assertTrue("Map entry still present", w.getTypeMap().getExpendableMap().containsKey(signature));
		assertNull("Type has been GC'd", w.getTypeMap().getExpendableMap().get(signature).get());

		// Re-resolve the type and check that it has a new instance of the signature
		// String
		ReferenceType referenceType = w.resolveToReferenceType("com.Foo");
		assertNotSame("New type has a new signature.", System.identityHashCode(signature),
				System.identityHashCode(referenceType.getSignature()));

		Map.Entry<String, Reference<ResolvedType>> entry = null;
		for (Map.Entry<String, Reference<ResolvedType>> e : w.getTypeMap().getExpendableMap().entrySet()) {
			if (referenceType.getSignature().equals(e.getKey())) {
				entry = e;
			}
		}
		assertEquals(
				"Map is keyed by the same String instance that is the re-resolved type's signature, not by the previous instance.",
				System.identityHashCode(referenceType.getSignature()), System.identityHashCode(entry.getKey()));
	}

	/**
	 * This test is verifying the treatment of array types (here, String[]). These should be expendable but because the
	 * ArrayReferenceType wasnt overriding isExposedToWeaver() an array had an apparent null delegate - this caused the isExpendable
	 * check to fail when choosing whether to put something in the permanent or expendable map. Leaving something in the permanent
	 * map that would never be cleared out.
	 */
	public void testWorldDemotion_278496_10() throws Exception {
		String p = "PR278496_10";
		TypeMap.useExpendableMap = false;
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true,typeDemotionDebug=true");
		build(p);
		checkWasFullBuild();
		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();

		assertNotInTypeMap(w, "Lcom/Foo;");
		assertNotInTypeMap(w, "[Ljava/lang/String;");
		assertInTypeMap(w, "Lcom/Asp;");
		assertInTypeMap(w, "[I");
		assertInTypeMap(w, "[[F");
	}

	public void testWorldDemotion_278496_11() throws Exception {
		String asp = "PR278496_11_a";
		initialiseProject(asp);
		build(asp);

		String p = "PR278496_11";
		TypeMap.useExpendableMap = false;
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true,typeDemotionDebug=true");
		configureAspectPath(p, getProjectRelativePath(asp, "bin"));
		build(p);
		checkWasFullBuild();
		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();

		assertNotInTypeMap(w, "Lcom/Foo;");
		assertInTypeMap(w, "Lcom/Asp;");
		assertNotInTypeMap(w, "Lcom/Dibble;");
	}

	public void testWorldDemotion_278496_6() throws Exception {
		String p = "PR278496_6";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true");
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();
		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();
		ReferenceTypeDelegate delegate = null;
		delegate = w.resolveToReferenceType("com.Meths").getDelegate();
		// assertTrue(delegate instanceof CompactTypeStructureDelegate);
		ResolvedMember[] methods = delegate.getDeclaredMethods();
		assertEquals("void com.Meths.<init>()", methods[0].toString());
		assertEquals("void com.Meths.m()", methods[1].toString());
		assertEquals("java.util.List com.Meths.n(int, long, java.util.List)", methods[2].toString());

		System.out.println(stringify(methods[0].getAnnotations()));
		System.out.println(stringify(methods[1].getAnnotations()));
		System.out.println(stringify(methods[2].getAnnotations()));
		assertEquals("[Anno[Lcom/Anno; rVis]]", stringify(methods[1].getAnnotations()));
		// assertNotNull(fields[2].getAnnotationOfType(UnresolvedType.forSignature("Lcom/Anno;")));
		// assertNull(fields[2].getAnnotationOfType(UnresolvedType.forSignature("Lcom/Anno2;")));
		// assertTrue(fields[2].hasAnnotation(UnresolvedType.forSignature("Lcom/Anno;")));
		// assertFalse(fields[2].hasAnnotation(UnresolvedType.forSignature("Ljava/lang/String;")));
		// assertEquals(0,fields[1].getAnnotations().length);
		// assertEquals("[com.Anno2 com.Anno]",stringify(fields[3].getAnnotationTypes()));
		// assertEquals("[]",stringify(fields[1].getAnnotationTypes()));
		// assertEquals("[Anno[Lcom/Anno2; rVis a=(int)42] Anno[Lcom/Anno; rVis]]",stringify(fields[3].getAnnotations()));
		// assertEquals("[]",stringify(fields[1].getAnnotations()));
		//
		// assertEquals("I",fields[0].getSignature());
		// assertEquals("Ljava/lang/String;",fields[1].getSignature());
		// assertEquals("Ljava/util/List;",fields[2].getSignature());
		// assertEquals("Pjava/util/List<Ljava/lang/String;>;",fields[2].getGenericReturnType().getSignature());
		// assertEquals("Ljava/util/List;",fields[3].getSignature());
		// assertEquals("Pjava/util/List<Ljava/lang/Integer;>;",fields[3].getGenericReturnType().getSignature());
	}

	// public void testWorldDemotion_278496_7() throws Exception {
	// boolean demotion = true;
	// AjdeInteractionTestbed.VERBOSE=true;
	// String p = "PR278496_7";
	// TypeMap.useExpendableMap=false;
	// initialiseProject(p);
	// if (demotion) {
	// configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true,typeDemotionDebug=true");
	// }
	// build(p);
	// checkWasFullBuild();
	// assertNoErrors(p);
	// alter(p,"inc1");
	// build(p);
	// checkWasntFullBuild();
	// assertNoErrors(p);
	//
	// AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
	// AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
	// World w = ajBuildManager.getWorld();
	// }

	public void testWorldDemotion_278496_4() throws Exception {
		String p = "PR278496_4";
		// Setting this ensures types are forced out when demoted - we are not at the mercy of weak reference GC
		TypeMap.useExpendableMap = false;
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true,typeDemotionDebug=true");
		build(p);
		checkWasFullBuild();
		alter(p, "inc1");
		build(p);
		checkWasntFullBuild();

		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();

		// Confirm demoted:
		assertNotInTypeMap(w, "Lcom/foo/Bar;");

		ReferenceType rt = null;
		ReferenceTypeDelegate delegate = null;
		rt = w.resolveToReferenceType("com.foo.Bar");
		delegate = rt.getDelegate();
		// Should have been demoted to a CTSD
		assertEquals(0, delegate.getAnnotations().length);
		assertEquals(0, delegate.getAnnotationTypes().length);
		assertEquals(0, delegate.getDeclaredInterfaces().length);
		assertEquals("java.lang.Object", delegate.getSuperclass().toString());
		assertNull(delegate.getRetentionPolicy());
		assertFalse(delegate.isInterface());
		assertTrue(delegate.isClass());
		assertFalse(delegate.isEnum());
		// assertFalse(rtd.isWeavable());
		// try {
		// assertTrue(delegate.hasBeenWoven());
		// fail("expected exception");
		// } catch (IllegalStateException ise) {
		// // success
		// }

		// Confirm demoted:
		assertNull(w.getTypeMap().get("Lcom/foo/Color;"));
		rt = w.resolveToReferenceType("com.foo.Color");
		delegate = rt.getDelegate();
		assertFalse(delegate.isInterface());
		assertTrue(delegate.isEnum());

		// Aspects are never demoted and so never have a per clause, declares or type mungers
		assertNull(delegate.getPerClause());
		assertEquals(0, delegate.getDeclares().size());
		assertEquals(0, delegate.getTypeMungers().size());
		assertFalse(delegate.isAspect());
		assertEquals(0, delegate.getPrivilegedAccesses().size());
		assertEquals(0, delegate.getDeclaredPointcuts().length);
		assertFalse(delegate.isAnnotationStyleAspect());
		assertFalse(delegate.isAnnotationWithRuntimeRetention());

		// Confirm demoted:
		assertNull(w.getTypeMap().get("Lcom/foo/Extender;"));
		rt = w.resolveToReferenceType("com.foo.Extender");
		delegate = rt.getDelegate();
		assertEquals("[com.foo.Marker]", stringify(delegate.getDeclaredInterfaces()));
		assertEquals("com.foo.Super", delegate.getSuperclass().toString());

		// this has one fixed annotation that is a well known one
		// Confirm demoted:
		ResolvedType annoType = w.getTypeMap().get("Lcom/foo/Anno;");
		assertNull(annoType);
		rt = w.resolveToReferenceType("com.foo.Anno");
		delegate = rt.getDelegate();
		assertEquals("[Anno[Ljava/lang/annotation/Retention; rVis value=E(Ljava/lang/annotation/RetentionPolicy; RUNTIME)]]",
				stringify(delegate.getAnnotations()));
		assertEquals("[java.lang.annotation.Retention]", stringify(delegate.getAnnotationTypes()));
		assertTrue(delegate.isAnnotationWithRuntimeRetention());
		assertEquals("RUNTIME", delegate.getRetentionPolicy());

		// this has a bunch of well known ones
		rt = w.resolveToReferenceType("com.foo.Anno2");
		delegate = rt.getDelegate();
		assertEquals(
				"[Anno[Ljava/lang/Deprecated; rVis] Anno[Ljava/lang/annotation/Inherited; rVis] Anno[Ljava/lang/annotation/Retention; rVis value=E(Ljava/lang/annotation/RetentionPolicy; CLASS)]]",
				stringify(delegate.getAnnotations()));
		assertEquals("[java.lang.Deprecated java.lang.annotation.Inherited java.lang.annotation.Retention]",
				stringify(delegate.getAnnotationTypes()));
		assertFalse(delegate.isAnnotationWithRuntimeRetention());
		assertEquals("CLASS", delegate.getRetentionPolicy());
		assertTrue(delegate.hasAnnotation(UnresolvedType.forSignature("Ljava/lang/annotation/Inherited;")));
		assertTrue(delegate.hasAnnotation(UnresolvedType.forSignature("Ljava/lang/annotation/Retention;")));
		assertFalse(delegate.hasAnnotation(UnresolvedType.forSignature("Lcom/foo/Anno;")));

		// this has a well known one and a non-well known one
		rt = w.resolveToReferenceType("com.foo.Anno3");
		delegate = rt.getDelegate();
		System.out.println(stringify(delegate.getAnnotations()));
		assertEquals(
				"[Anno[Lcom/foo/Anno; rVis] Anno[Ljava/lang/annotation/Retention; rVis value=E(Ljava/lang/annotation/RetentionPolicy; SOURCE)]]",
				stringify(delegate.getAnnotations()));
		assertEquals("[com.foo.Anno java.lang.annotation.Retention]", stringify(delegate.getAnnotationTypes()));
		assertFalse(delegate.isAnnotationWithRuntimeRetention());
		assertEquals("SOURCE", delegate.getRetentionPolicy());

		// this has two non-well known ones
		rt = w.resolveToReferenceType("com.foo.Anno4");
		delegate = rt.getDelegate();
		assertEquals("[Anno[Lcom/foo/Anno2; rInvis] Anno[Lcom/foo/Anno; rVis]]", stringify(delegate.getAnnotations()));
		assertEquals("[com.foo.Anno2 com.foo.Anno]", stringify(delegate.getAnnotationTypes()));
		assertFalse(delegate.isAnnotationWithRuntimeRetention());
		assertNull(delegate.getRetentionPolicy());
		assertTrue(delegate.hasAnnotation(UnresolvedType.forSignature("Lcom/foo/Anno;")));

		rt = w.resolveToReferenceType("com.foo.Colored");
		delegate = rt.getDelegate();
		AnnotationAJ annotation = delegate.getAnnotations()[0]; // should be ColorAnno(c=Color.G)
		assertTrue(annotation.hasNamedValue("c"));
		assertFalse(annotation.hasNamedValue("value"));
		assertTrue(annotation.hasNameValuePair("c", "Lcom/foo/Color;G"));
		assertFalse(annotation.hasNameValuePair("c", "Lcom/foo/Color;B"));
		assertFalse(annotation.hasNameValuePair("d", "xxx"));
		assertNull(annotation.getStringFormOfValue("d"));
		assertEquals("Lcom/foo/Color;G", annotation.getStringFormOfValue("c"));
		assertEquals(0, annotation.getTargets().size());
		assertTrue(delegate.isCacheable());

		assertFalse(delegate.isExposedToWeaver());

		// assertEquals(w.resolve(UnresolvedType.forSignature("Lcom/foo/Colored;")),delegate.getResolvedTypeX());

		assertEquals("com/foo/Colored.java", delegate.getSourcefilename());

		// Anno5 has an @Target annotation
		rt = w.resolveToReferenceType("com.foo.Anno5");
		delegate = rt.getDelegate();
		annotation = delegate.getAnnotations()[0]; // should be @Target(TYPE,FIELD)
		Set<String> ss = annotation.getTargets();
		assertEquals(2, ss.size());
		assertTrue(ss.contains("FIELD"));
		assertTrue(ss.contains("TYPE"));
		// AnnotationTargetKind[] kinds = delegate.getAnnotationTargetKinds();
		// assertEquals("FIELD",kinds[0].getName());
		// assertEquals("TYPE",kinds[1].getName());

		rt = w.resolveToReferenceType("com.foo.Inners$Inner");
		delegate = rt.getDelegate();
		assertTrue(delegate.isNested());
		assertEquals("com.foo.Inners", delegate.getOuterClass().getName());

		rt = w.resolveToReferenceType("com.foo.Inners$1");
		delegate = rt.getDelegate();
		assertTrue(delegate.isAnonymous());
		assertTrue(delegate.isNested());

		// delegate = w.resolveToReferenceType("com.foo.Anno6").getDelegate();
		// kinds = delegate.getAnnotationTargetKinds();
		// assertEquals(6,kinds.length);
		// String s = stringify(kinds);
		// assertTrue(s.contains("ANNOTATION_TYPE"));
		// assertTrue(s.contains("LOCAL_VARIABLE"));
		// assertTrue(s.contains("METHOD"));
		// assertTrue(s.contains("PARAMETER"));
		// assertTrue(s.contains("PACKAGE"));
		// assertTrue(s.contains("CONSTRUCTOR"));

		delegate = w.resolveToReferenceType("com.foo.Marker").getDelegate();
		assertTrue(delegate.isInterface());

	}

	private void assertNotInTypeMap(World w, String string) {
		assertNull(w.getTypeMap().get(string));
	}

	private void assertInTypeMap(World w, String string) {
		assertNotNull(w.getTypeMap().get(string));
	}

	private String stringify(Object[] arr) {
		StringBuilder s = new StringBuilder();
		for (Object element : arr) {
			s.append(element);
			s.append(" ");
		}
		return "[" + s.toString().trim() + "]";
	}

	public void testDeletionInnerAspects_278496_4() throws Exception {
		String p = "PR278496_4";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=true");
		build(p);
		checkWasFullBuild();
		// printModel(p);
		// Here is the model without deletion.
		// PR278496_4 [build configuration file] hid:=PR278496_4
		// foo [package] hid:=PR278496_4<foo
		// MyOtherClass.java [java source file] 1 hid:=PR278496_4<foo{MyOtherClass.java
		// foo [package declaration] 1 hid:=PR278496_4<foo{MyOtherClass.java%foo
		// [import reference] hid:=PR278496_4<foo{MyOtherClass.java#
		// MyOtherClass [class] 2 hid:=PR278496_4<foo{MyOtherClass.java[MyOtherClass
		// MyInnerClass [class] 4 hid:=PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass
		// MyInnerInnerAspect [aspect] 6 hid:=PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass}MyInnerInnerAspect
		// before(): <anonymous pointcut> [advice] 8
		// hid:=PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass}MyInnerInnerAspect&before
		// MyClass.java [java source file] 1 hid:=PR278496_4<foo{MyClass.java
		// foo [package declaration] 1 hid:=PR278496_4<foo{MyClass.java%foo
		// [import reference] hid:=PR278496_4<foo{MyClass.java#
		// MyClass [class] 9 hid:=PR278496_4<foo{MyClass.java[MyClass
		// main(java.lang.String[]) [method] 12 hid:=PR278496_4<foo{MyClass.java[MyClass~main~\[QString;
		// method1() [method] 16 hid:=PR278496_4<foo{MyClass.java[MyClass~method1
		// method2() [method] 18 hid:=PR278496_4<foo{MyClass.java[MyClass~method2
		// Hid:1:(targets=1) =PR278496_4<foo{MyClass.java[MyClass~method1 (advised by)
		// =PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass}MyInnerInnerAspect&before
		// Hid:2:(targets=1) =PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass}MyInnerInnerAspect&before (advises)
		// =PR278496_4<foo{MyClass.java[MyClass~method1

		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		IProgramElement ipe = model.findElementForHandleOrCreate(
				"=PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass'MyInnerInnerAspect", false);
		assertNotNull(ipe);
	}

	public void testDeletionAnonInnerType_278496_8() throws Exception {
		String p = "pr278496_8";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=true");
		build(p);
		checkWasFullBuild();
		// printModel(p);
		// Here is the model without deletion.
		// PR278496_8 [build configuration file] hid:=PR278496_8
		// generics [package] hid:=PR278496_8<generics
		// DeleteActionAspect.aj [java source file] 1 hid:=PR278496_8<generics*DeleteActionAspect.aj
		// generics [package declaration] 1 hid:=PR278496_8<generics*DeleteActionAspect.aj%generics
		// [import reference] hid:=PR278496_8<generics*DeleteActionAspect.aj#
		// java.util.List [import reference] 3 hid:=PR278496_8<generics*DeleteActionAspect.aj#java.util.List
		// DeleteActionAspect [aspect] 6 hid:=PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect
		// DeleteAction.delete() [inter-type method] 8
		// hid:=PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect)DeleteAction.delete
		// DeleteAction.delete2 [inter-type field] 14
		// hid:=PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete2
		// DeleteAction.delete3 [inter-type field] 16
		// hid:=PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete3
		// main(java.lang.String[]) [method] 19 hid:=PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\[QString;
		// new DeleteAction<String>() {..} [class] 20
		// hid:=PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\[QString;[
		// getSelected() [method] 21 hid:=PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\[QString;[~getSelected
		// ActionExecutor.java [java source file] 1 hid:=PR278496_8<generics{ActionExecutor.java
		// generics [package declaration] 1 hid:=PR278496_8<generics{ActionExecutor.java%generics
		// [import reference] hid:=PR278496_8<generics{ActionExecutor.java#
		// ActionExecutor [class] 3 hid:=PR278496_8<generics{ActionExecutor.java[ActionExecutor
		// main(java.lang.String[]) [method] 4 hid:=PR278496_8<generics{ActionExecutor.java[ActionExecutor~main~\[QString;
		// new DeleteAction<String>() {..} [class] 5 hid:=PR278496_8<generics{ActionExecutor.java[ActionExecutor~main~\[QString;[
		// getSelected() [method] 6 hid:=PR278496_8<generics{ActionExecutor.java[ActionExecutor~main~\[QString;[~getSelected
		// nothing2(generics.DeleteAction<java.lang.String>) [method] 15
		// hid:=PR278496_8<generics{ActionExecutor.java[ActionExecutor~nothing2~QDeleteAction\<QString;>;
		// DeleteAction.java [java source file] 1 hid:=PR278496_8<generics{DeleteAction.java
		// generics [package declaration] 1 hid:=PR278496_8<generics{DeleteAction.java%generics
		// [import reference] hid:=PR278496_8<generics{DeleteAction.java#
		// java.util.List [import reference] 2 hid:=PR278496_8<generics{DeleteAction.java#java.util.List
		// DeleteAction [interface] 5 hid:=PR278496_8<generics{DeleteAction.java[DeleteAction
		// delete() [method] 7 hid:=PR278496_8<generics{DeleteAction.java[DeleteAction~delete
		// getSelected() [method] 9 hid:=PR278496_8<generics{DeleteAction.java[DeleteAction~getSelected
		// test [package] hid:=PR278496_8<test
		// MyAspect.aj [java source file] 1 hid:=PR278496_8<test*MyAspect.aj
		// test [package declaration] 1 hid:=PR278496_8<test*MyAspect.aj%test
		// [import reference] hid:=PR278496_8<test*MyAspect.aj#
		// java.util.List [import reference] 2 hid:=PR278496_8<test*MyAspect.aj#java.util.List
		// MyAspect [aspect] 4 hid:=PR278496_8<test*MyAspect.aj'MyAspect
		// MyAnnotation [annotation] 47 hid:=PR278496_8<test*MyAspect.aj'MyAspect[MyAnnotation
		// Abstract [class] 60 hid:=PR278496_8<test*MyAspect.aj'MyAspect[Abstract
		// Demo.version [inter-type field] 7 hid:=PR278496_8<test*MyAspect.aj'MyAspect,Demo.version
		// Demo.list [inter-type field] 10 hid:=PR278496_8<test*MyAspect.aj'MyAspect,Demo.list
		// Demo.x [inter-type field] 11 hid:=PR278496_8<test*MyAspect.aj'MyAspect,Demo.x
		// Demo.foo(java.util.List<java.lang.String>) [inter-type method] 13
		// hid:=PR278496_8<test*MyAspect.aj'MyAspect)Demo.foo)QList\<QString;>;
		// Demo.Demo(int) [inter-type constructor] 17 hid:=PR278496_8<test*MyAspect.aj'MyAspect)Demo.Demo_new)I
		// declare warning: "blah" [declare warning] 21 hid:=PR278496_8<test*MyAspect.aj'MyAspect`declare warning
		// declare error: "blah" [declare error] 23 hid:=PR278496_8<test*MyAspect.aj'MyAspect`declare error!2
		// declare soft: java.lang.Exception [declare soft] 25 hid:=PR278496_8<test*MyAspect.aj'MyAspect`declare soft!3
		// s() [pointcut] 28 hid:=PR278496_8<test*MyAspect.aj'MyAspect"s
		// before(): s.. [advice] 31 hid:=PR278496_8<test*MyAspect.aj'MyAspect&before
		// after(): s.. [advice] 33 hid:=PR278496_8<test*MyAspect.aj'MyAspect&after
		// around(): s.. [advice] 35 hid:=PR278496_8<test*MyAspect.aj'MyAspect&around
		// afterReturning(): s.. [advice] 39 hid:=PR278496_8<test*MyAspect.aj'MyAspect&afterReturning
		// afterThrowing(): s.. [advice] 41 hid:=PR278496_8<test*MyAspect.aj'MyAspect&afterThrowing
		// declare @type: test.Demo : @MyAnnotation [declare @type] 52 hid:=PR278496_8<test*MyAspect.aj'MyAspect`declare \@type
		// declare @field: int test.Demo.x : @MyAnnotation [declare @field] 53 hid:=PR278496_8<test*MyAspect.aj'MyAspect`declare
		// \@field
		// declare @method: void test.Demo.foo(..) : @MyAnnotation [declare @method] 54
		// hid:=PR278496_8<test*MyAspect.aj'MyAspect`declare \@method
		// declare @constructor: public test.Demo.new(int) : @MyAnnotation [declare @constructor] 55
		// hid:=PR278496_8<test*MyAspect.aj'MyAspect`declare \@constructor
		// Abstract.nothing() [inter-type method] 58 hid:=PR278496_8<test*MyAspect.aj'MyAspect)Abstract.nothing
		// Demo.aj [java source file] 1 hid:=PR278496_8<test*Demo.aj
		// test [package declaration] 1 hid:=PR278496_8<test*Demo.aj%test
		// [import reference] hid:=PR278496_8<test*Demo.aj#
		// test.MyAspect$MyAnnotation [import reference] 1 hid:=PR278496_8<test*Demo.aj#test.MyAspect$MyAnnotation
		// java.util.List [import reference] 3 hid:=PR278496_8<test*Demo.aj#java.util.List
		// Demo [class] 5 hid:=PR278496_8<test*Demo.aj[Demo
		// g() [method] 7 hid:=PR278496_8<test*Demo.aj[Demo~g
		// OtherClass.aj [java source file] 1 hid:=PR278496_8<test*OtherClass.aj
		// test [package declaration] 1 hid:=PR278496_8<test*OtherClass.aj%test
		// [import reference] hid:=PR278496_8<test*OtherClass.aj#
		// OtherClass [class] 4 hid:=PR278496_8<test*OtherClass.aj[OtherClass
		// x() [method] 5 hid:=PR278496_8<test*OtherClass.aj[OtherClass~x
		// test2 [package] hid:=PR278496_8<test2
		// MyAspect2.aj [java source file] 1 hid:=PR278496_8<test2*MyAspect2.aj
		// test2 [package declaration] 4 hid:=PR278496_8<test2*MyAspect2.aj%test2
		// [import reference] hid:=PR278496_8<test2*MyAspect2.aj#
		// test.Demo [import reference] 3 hid:=PR278496_8<test2*MyAspect2.aj#test.Demo
		// MyAspect2 [aspect] 6 hid:=PR278496_8<test2*MyAspect2.aj'MyAspect2
		// Bar [interface] 8 hid:=PR278496_8<test2*MyAspect2.aj'MyAspect2[Bar
		// Foo [class] 18 hid:=PR278496_8<test2*MyAspect2.aj'MyAspect2[Foo
		// Foo() [constructor] 19 hid:=PR278496_8<test2*MyAspect2.aj'MyAspect2[Foo~Foo
		// declare parents: implements MyAspect2$Bar,Cloneable [declare parents] 11
		// hid:=PR278496_8<test2*MyAspect2.aj'MyAspect2`declare parents
		// Bar.bar() [inter-type method] 13 hid:=PR278496_8<test2*MyAspect2.aj'MyAspect2)Bar.bar
		// declare parents: extends MyAspect2$Foo [declare parents] 23 hid:=PR278496_8<test2*MyAspect2.aj'MyAspect2`declare
		// parents!2
		// Foo.baz() [inter-type method] 25 hid:=PR278496_8<test2*MyAspect2.aj'MyAspect2)Foo.baz
		// OtherClass2.aj [java source file] 1 hid:=PR278496_8<test2*OtherClass2.aj
		// test2 [package declaration] 1 hid:=PR278496_8<test2*OtherClass2.aj%test2
		// [import reference] hid:=PR278496_8<test2*OtherClass2.aj#
		// test.Demo [import reference] 3 hid:=PR278496_8<test2*OtherClass2.aj#test.Demo
		// OtherClass2 [class] 5 hid:=PR278496_8<test2*OtherClass2.aj[OtherClass2
		// x() [method] 6 hid:=PR278496_8<test2*OtherClass2.aj[OtherClass2~x
		// Hid:1:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect,Demo.version (declared on) =PR278496_8<test*Demo.aj[Demo
		// Hid:2:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect)Abstract.nothing (declared on)
		// =PR278496_8<test*MyAspect.aj'MyAspect[Abstract
		// Hid:3:(targets=1) =PR278496_8<test*Demo.aj[Demo (annotated by) =PR278496_8<test*MyAspect.aj'MyAspect`declare \@type
		// Hid:4:(targets=8) =PR278496_8<test*Demo.aj[Demo (aspect declarations) =PR278496_8<test2*MyAspect2.aj'MyAspect2`declare
		// parents!2
		// Hid:5:(targets=8) =PR278496_8<test*Demo.aj[Demo (aspect declarations) =PR278496_8<test2*MyAspect2.aj'MyAspect2`declare
		// parents
		// Hid:6:(targets=8) =PR278496_8<test*Demo.aj[Demo (aspect declarations) =PR278496_8<test2*MyAspect2.aj'MyAspect2)Bar.bar
		// Hid:7:(targets=8) =PR278496_8<test*Demo.aj[Demo (aspect declarations) =PR278496_8<test*MyAspect.aj'MyAspect,Demo.version
		// Hid:8:(targets=8) =PR278496_8<test*Demo.aj[Demo (aspect declarations) =PR278496_8<test*MyAspect.aj'MyAspect,Demo.list
		// Hid:9:(targets=8) =PR278496_8<test*Demo.aj[Demo (aspect declarations) =PR278496_8<test*MyAspect.aj'MyAspect,Demo.x
		// Hid:10:(targets=8) =PR278496_8<test*Demo.aj[Demo (aspect declarations)
		// =PR278496_8<test*MyAspect.aj'MyAspect)Demo.foo)QList\<QString;>;
		// Hid:11:(targets=8) =PR278496_8<test*Demo.aj[Demo (aspect declarations)
		// =PR278496_8<test*MyAspect.aj'MyAspect)Demo.Demo_new)I
		// Hid:12:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect`declare \@type (annotates) =PR278496_8<test*Demo.aj[Demo
		// Hid:13:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect,Demo.list (declared on) =PR278496_8<test*Demo.aj[Demo
		// Hid:14:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect)Demo.foo)QList\<QString;>; (declared on)
		// =PR278496_8<test*Demo.aj[Demo
		// Hid:15:(targets=1) =PR278496_8<test2*MyAspect2.aj'MyAspect2`declare parents!2 (declared on) =PR278496_8<test*Demo.aj[Demo
		// Hid:16:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete2 (declared on)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\[QString;[
		// Hid:17:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete2 (declared on)
		// =PR278496_8<generics{ActionExecutor.java[ActionExecutor~main~\[QString;[
		// Hid:18:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete2 (declared on)
		// =PR278496_8<generics{DeleteAction.java[DeleteAction
		// Hid:19:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect`declare \@constructor (annotates)
		// =PR278496_8<test*MyAspect.aj'MyAspect)Demo.Demo_new)I
		// Hid:20:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete3 (declared on)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\[QString;[
		// Hid:21:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete3 (declared on)
		// =PR278496_8<generics{ActionExecutor.java[ActionExecutor~main~\[QString;[
		// Hid:22:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete3 (declared on)
		// =PR278496_8<generics{DeleteAction.java[DeleteAction
		// Hid:23:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect[Abstract (aspect declarations)
		// =PR278496_8<test*MyAspect.aj'MyAspect)Abstract.nothing
		// Hid:24:(targets=1) =PR278496_8<test2*MyAspect2.aj'MyAspect2`declare parents (declared on) =PR278496_8<test*Demo.aj[Demo
		// Hid:25:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect)DeleteAction.delete (declared on)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\[QString;[
		// Hid:26:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect)DeleteAction.delete (declared on)
		// =PR278496_8<generics{ActionExecutor.java[ActionExecutor~main~\[QString;[
		// Hid:27:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect)DeleteAction.delete (declared on)
		// =PR278496_8<generics{DeleteAction.java[DeleteAction
		// Hid:28:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect`declare \@field (annotates)
		// =PR278496_8<test*MyAspect.aj'MyAspect,Demo.x
		// Hid:29:(targets=3) =PR278496_8<generics{ActionExecutor.java[ActionExecutor~main~\[QString;[ (aspect declarations)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect)DeleteAction.delete
		// Hid:30:(targets=3) =PR278496_8<generics{ActionExecutor.java[ActionExecutor~main~\[QString;[ (aspect declarations)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete2
		// Hid:31:(targets=3) =PR278496_8<generics{ActionExecutor.java[ActionExecutor~main~\[QString;[ (aspect declarations)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete3
		// Hid:32:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\[QString;[ (aspect declarations)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect)DeleteAction.delete
		// Hid:33:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\[QString;[ (aspect declarations)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete2
		// Hid:34:(targets=3) =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\[QString;[ (aspect declarations)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete3
		// Hid:35:(targets=3) =PR278496_8<generics{DeleteAction.java[DeleteAction (aspect declarations)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect)DeleteAction.delete
		// Hid:36:(targets=3) =PR278496_8<generics{DeleteAction.java[DeleteAction (aspect declarations)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete2
		// Hid:37:(targets=3) =PR278496_8<generics{DeleteAction.java[DeleteAction (aspect declarations)
		// =PR278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect,DeleteAction.delete3
		// Hid:38:(targets=1) =PR278496_8<test2*MyAspect2.aj'MyAspect2[Foo (aspect declarations)
		// =PR278496_8<test2*MyAspect2.aj'MyAspect2)Foo.baz
		// Hid:39:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect)Demo.Demo_new)I (annotated by)
		// =PR278496_8<test*MyAspect.aj'MyAspect`declare \@constructor
		// Hid:40:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect)Demo.Demo_new)I (declared on) =PR278496_8<test*Demo.aj[Demo
		// Hid:41:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect,Demo.x (annotated by)
		// =PR278496_8<test*MyAspect.aj'MyAspect`declare \@field
		// Hid:42:(targets=1) =PR278496_8<test*MyAspect.aj'MyAspect,Demo.x (declared on) =PR278496_8<test*Demo.aj[Demo
		// Hid:43:(targets=2) =PR278496_8<test2*MyAspect2.aj'MyAspect2)Bar.bar (declared on)
		// =PR278496_8<test2*MyAspect2.aj'MyAspect2[Bar
		// Hid:44:(targets=2) =PR278496_8<test2*MyAspect2.aj'MyAspect2)Bar.bar (declared on) =PR278496_8<test*Demo.aj[Demo
		// Hid:45:(targets=1) =PR278496_8<test2*MyAspect2.aj'MyAspect2[Bar (aspect declarations)
		// =PR278496_8<test2*MyAspect2.aj'MyAspect2)Bar.bar
		// Hid:46:(targets=1) =PR278496_8<test2*MyAspect2.aj'MyAspect2)Foo.baz (declared on)
		// =PR278496_8<test2*MyAspect2.aj'MyAspect2[Foo
		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		// check handle to anonymous inner:
		IProgramElement ipe = model.findElementForHandleOrCreate(
				"=pr278496_8<generics*DeleteActionAspect.aj'DeleteActionAspect~main~\\[QString;[", false);
		assertNotNull(ipe);
	}
}
