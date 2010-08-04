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

import java.util.Set;

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
 * Incremental compilation tests. MultiProjectIncrementalTests was getting unwieldy - started this new test class for 1.6.10.
 * 
 * @author Andy Clement
 * @since 1.6.10
 */
public class IncrementalCompilationTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

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
		// Here is the model without deletion.  The node for 'Code.java' can safely be deleted as it contains
		// no types that are the target of relationships.
		//		PR278496_1  [build configuration file]  hid:=PR278496_1
		//		  a.b.c  [package]  hid:=PR278496_1<a.b.c
		//		    Azpect.java  [java source file] 1 hid:=PR278496_1<a.b.c{Azpect.java
		//		      a.b.c  [package declaration] 1 hid:=PR278496_1<a.b.c{Azpect.java%a.b.c
		//		        [import reference]  hid:=PR278496_1<a.b.c{Azpect.java#
		//		      Azpect  [aspect] 3 hid:=PR278496_1<a.b.c{Azpect.java}Azpect
		//		        before(): <anonymous pointcut>  [advice] 4 hid:=PR278496_1<a.b.c{Azpect.java}Azpect&before
		//		    Code.java  [java source file] 1 hid:=PR278496_1<a.b.c{Code.java
		//		      a.b.c  [package declaration] 1 hid:=PR278496_1<a.b.c{Code.java%a.b.c
		//		        [import reference]  hid:=PR278496_1<a.b.c{Code.java#
		//		        java.util.ArrayList  [import reference] 3 hid:=PR278496_1<a.b.c{Code.java#java.util.ArrayList
		//		        java.util.List  [import reference] 2 hid:=PR278496_1<a.b.c{Code.java#java.util.List
		//		      Code  [class] 5 hid:=PR278496_1<a.b.c{Code.java[Code
		//		        m()  [method] 6 hid:=PR278496_1<a.b.c{Code.java[Code~m
		//		    Code2.java  [java source file] 1 hid:=PR278496_1<a.b.c{Code2.java
		//		      a.b.c  [package declaration] 1 hid:=PR278496_1<a.b.c{Code2.java%a.b.c
		//		        [import reference]  hid:=PR278496_1<a.b.c{Code2.java#
		//		        java.util.ArrayList  [import reference] 3 hid:=PR278496_1<a.b.c{Code2.java#java.util.ArrayList
		//		        java.util.List  [import reference] 2 hid:=PR278496_1<a.b.c{Code2.java#java.util.List
		//		      Code2  [class] 5 hid:=PR278496_1<a.b.c{Code2.java[Code2
		//		        m()  [method] 6 hid:=PR278496_1<a.b.c{Code2.java[Code2~m
		//		Hid:1:(targets=1) =PR278496_1<a.b.c{Azpect.java}Azpect&before (advises) =PR278496_1<a.b.c{Code2.java[Code2
		//		Hid:2:(targets=1) =PR278496_1<a.b.c{Code2.java[Code2 (advised by) =PR278496_1<a.b.c{Azpect.java}Azpect&before

		// deletion turned on:
		//		PR278496_1  [build configuration file]  hid:=PR278496_1
		//		  a.b.c  [package]  hid:<a.b.c
		//		    Azpect.java  [java source file] 1 hid:<a.b.c{Azpect.java
		//		      a.b.c  [package declaration] 1 hid:<a.b.c{Azpect.java%a.b.c
		//		        [import reference]  hid:<a.b.c{Azpect.java#
		//		      Azpect  [aspect] 3 hid:<a.b.c{Azpect.java}Azpect
		//		        before(): <anonymous pointcut>  [advice] 4 hid:<a.b.c{Azpect.java}Azpect&before
		//		    Code2.java  [java source file] 1 hid:<a.b.c{Code2.java
		//		      a.b.c  [package declaration] 1 hid:<a.b.c{Code2.java%a.b.c
		//		        [import reference]  hid:<a.b.c{Code2.java#
		//		        java.util.ArrayList  [import reference] 3 hid:<a.b.c{Code2.java#java.util.ArrayList
		//		        java.util.List  [import reference] 2 hid:<a.b.c{Code2.java#java.util.List
		//		      Code2  [class] 5 hid:<a.b.c{Code2.java[Code2
		//		        m()  [method] 6 hid:<a.b.c{Code2.java[Code2~m
		//		Hid:1:(targets=1) <a.b.c{Azpect.java}Azpect&before (advises) <a.b.c{Code2.java[Code2
		//		Hid:2:(targets=1) <a.b.c{Code2.java[Code2 (advised by) <a.b.c{Azpect.java}Azpect&before

		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		// Node for "Code.java" should not be there:
		IProgramElement ipe = model.findElementForHandleOrCreate("=PR278496_1<a.b.c{Code.java",false);
		assertNull(ipe);
	}

	// deleting unaffected model entries
	public void testDeletion_278496_2() throws Exception {
		String p = "PR278496_2";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=true");
		build(p);
		checkWasFullBuild();
		//		printModel(p);
		// Here is the model without deletion.
		//		PR278496_2  [build configuration file]  hid:=PR278496_2
		//	    [package]  hid:=PR278496_2<
		//	    Azpect.java  [java source file] 1 hid:=PR278496_2<{Azpect.java
		//	        [import reference]  hid:=PR278496_2<{Azpect.java#
		//	      Azpect  [aspect] 1 hid:=PR278496_2<{Azpect.java}Azpect
		//	        Code.m()  [inter-type method] 2 hid:=PR278496_2<{Azpect.java}Azpect)Code.m
		//	    Code.java  [java source file] 1 hid:=PR278496_2<{Code.java
		//	        [import reference]  hid:=PR278496_2<{Code.java#
		//	      Code  [class] 1 hid:=PR278496_2<{Code.java[Code
		//	Hid:1:(targets=1) =PR278496_2<{Azpect.java}Azpect)Code.m (declared on) =PR278496_2<{Code.java[Code
		//	Hid:2:(targets=1) =PR278496_2<{Code.java[Code (aspect declarations) =PR278496_2<{Azpect.java}Azpect)Code.m


		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		// Node for "Code.java" should be there since it is the target of a relationship
		IProgramElement ipe = model.findElementForHandleOrCreate("=PR278496_2<{Code.java",false);
		assertNotNull(ipe);
	}

	public void testWorldDemotion_278496_5() throws Exception {
		String p = "PR278496_5";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true");
		build(p);
		checkWasFullBuild();
		alter(p,"inc1");
		build(p);
		checkWasntFullBuild();
		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();
		ReferenceTypeDelegate delegate = null;
		delegate = w.resolveToReferenceType("com.Foo").getDelegate();
		ResolvedMember[] fields = delegate.getDeclaredFields();
		assertEquals("int com.Foo.i",fields[0].toString());
		assertEquals("java.lang.String com.Foo.s",fields[1].toString());
		assertEquals("java.util.List com.Foo.ls",fields[2].toString());

		assertEquals("[Anno[Lcom/Anno; rVis]]", stringify(fields[2].getAnnotations()));
		assertNotNull(fields[2].getAnnotationOfType(UnresolvedType.forSignature("Lcom/Anno;")));
		assertNull(fields[2].getAnnotationOfType(UnresolvedType.forSignature("Lcom/Anno2;")));
		assertTrue(fields[2].hasAnnotation(UnresolvedType.forSignature("Lcom/Anno;")));
		assertFalse(fields[2].hasAnnotation(UnresolvedType.forSignature("Ljava/lang/String;")));
		assertEquals(0,fields[1].getAnnotations().length);
		assertEquals("[com.Anno2 com.Anno]",stringify(fields[3].getAnnotationTypes()));
		assertEquals("[]",stringify(fields[1].getAnnotationTypes()));
		assertEquals("[Anno[Lcom/Anno2; rVis a=(int)42] Anno[Lcom/Anno; rVis]]",stringify(fields[3].getAnnotations()));
		assertEquals("[]",stringify(fields[1].getAnnotations()));

		assertEquals("I",fields[0].getSignature());
		assertEquals("Ljava/lang/String;",fields[1].getSignature());
		assertEquals("Ljava/util/List;",fields[2].getSignature());
		assertEquals("Pjava/util/List<Ljava/lang/String;>;",fields[2].getGenericReturnType().getSignature());
		assertEquals("Ljava/util/List;",fields[3].getSignature());
		assertEquals("Pjava/util/List<Ljava/lang/Integer;>;",fields[3].getGenericReturnType().getSignature());
	}

	public void testWorldDemotion_278496_6() throws Exception {
		String p = "PR278496_6";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true");
		build(p);
		checkWasFullBuild();
		alter(p,"inc1");
		build(p);
		checkWasntFullBuild();
		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();
		ReferenceTypeDelegate delegate = null;
		delegate = w.resolveToReferenceType("com.Meths").getDelegate();
		//		assertTrue(delegate instanceof CompactTypeStructureDelegate);
		ResolvedMember[] methods = delegate.getDeclaredMethods();
		assertEquals("void com.Meths.<init>()",methods[0].toString());
		assertEquals("void com.Meths.m()",methods[1].toString());
		assertEquals("java.util.List com.Meths.n(int, long, java.util.List)",methods[2].toString());


		System.out.println(stringify(methods[0].getAnnotations()));
		System.out.println(stringify(methods[1].getAnnotations()));
		System.out.println(stringify(methods[2].getAnnotations()));
		assertEquals("[Anno[Lcom/Anno; rVis]]", stringify(methods[1].getAnnotations()));
		//		assertNotNull(fields[2].getAnnotationOfType(UnresolvedType.forSignature("Lcom/Anno;")));
		//		assertNull(fields[2].getAnnotationOfType(UnresolvedType.forSignature("Lcom/Anno2;")));
		//		assertTrue(fields[2].hasAnnotation(UnresolvedType.forSignature("Lcom/Anno;")));
		//		assertFalse(fields[2].hasAnnotation(UnresolvedType.forSignature("Ljava/lang/String;")));
		//		assertEquals(0,fields[1].getAnnotations().length);
		//		assertEquals("[com.Anno2 com.Anno]",stringify(fields[3].getAnnotationTypes()));
		//		assertEquals("[]",stringify(fields[1].getAnnotationTypes()));
		//		assertEquals("[Anno[Lcom/Anno2; rVis a=(int)42] Anno[Lcom/Anno; rVis]]",stringify(fields[3].getAnnotations()));
		//		assertEquals("[]",stringify(fields[1].getAnnotations()));
		//
		//		assertEquals("I",fields[0].getSignature());
		//		assertEquals("Ljava/lang/String;",fields[1].getSignature());
		//		assertEquals("Ljava/util/List;",fields[2].getSignature());
		//		assertEquals("Pjava/util/List<Ljava/lang/String;>;",fields[2].getGenericReturnType().getSignature());
		//		assertEquals("Ljava/util/List;",fields[3].getSignature());
		//		assertEquals("Pjava/util/List<Ljava/lang/Integer;>;",fields[3].getGenericReturnType().getSignature());
	}

	//	public void testWorldDemotion_278496_7() throws Exception {
	//		boolean demotion = true;
	//		AjdeInteractionTestbed.VERBOSE=true;
	//		String p = "PR278496_7";
	//		TypeMap.useExpendableMap=false;
	//		initialiseProject(p);
	//		if (demotion) {
	//			configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true,typeDemotionDebug=true");
	//		}
	//		build(p);
	//		checkWasFullBuild();
	//		assertNoErrors(p);
	//		alter(p,"inc1");
	//		build(p);
	//		checkWasntFullBuild();
	//		assertNoErrors(p);
	//
	//		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
	//		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
	//		World w = ajBuildManager.getWorld();
	//	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		TypeMap.useExpendableMap=true;
	}

	public void testWorldDemotion_278496_4() throws Exception {
		String p = "PR278496_4";
		// Setting this ensures types are forced out when demoted - we are not at the mercy of weak reference GC
		TypeMap.useExpendableMap=false;
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:typeDemotion=true,typeDemotionDebug=true");
		build(p);
		checkWasFullBuild();
		alter(p,"inc1");
		build(p);
		checkWasntFullBuild();

		AjdeCoreBuildManager buildManager = getCompilerForProjectWithName(p).getBuildManager();
		AjBuildManager ajBuildManager = buildManager.getAjBuildManager();
		World w = ajBuildManager.getWorld();

		// Confirm demoted:
		assertNotInTypeMap(w,"Lcom/foo/Bar;");

		ReferenceType rt =null;
		ReferenceTypeDelegate delegate = null;
		rt = w.resolveToReferenceType("com.foo.Bar");
		delegate = rt.getDelegate();
		// Should have been demoted to a CTSD
		assertEquals(0,delegate.getAnnotations().length);
		assertEquals(0,delegate.getAnnotationTypes().length);
		assertEquals(0,delegate.getDeclaredInterfaces().length);
		assertEquals("java.lang.Object",delegate.getSuperclass().toString());
		assertNull(delegate.getRetentionPolicy());
		assertFalse(delegate.isInterface());
		assertTrue(delegate.isClass());
		assertFalse(delegate.isEnum());
		//		assertFalse(rtd.isWeavable());
		//		try {
		//			assertTrue(delegate.hasBeenWoven());
		//			fail("expected exception");
		//		} catch (IllegalStateException ise) {
		//			// success
		//		}

		// Confirm demoted:
		assertNull(w.getTypeMap().get("Lcom/foo/Color;"));
		rt = w.resolveToReferenceType("com.foo.Color");
		delegate = rt.getDelegate();
		assertFalse(delegate.isInterface());
		assertTrue(delegate.isEnum());

		// Aspects are never demoted and so never have a per clause, declares or type mungers
		assertNull(delegate.getPerClause());
		assertEquals(0,delegate.getDeclares().size());
		assertEquals(0,delegate.getTypeMungers().size());
		assertFalse(delegate.isAspect());
		assertEquals(0,delegate.getPrivilegedAccesses().size());
		assertEquals(0,delegate.getDeclaredPointcuts().length);
		assertFalse(delegate.isAnnotationStyleAspect());
		assertFalse(delegate.isAnnotationWithRuntimeRetention());

		// Confirm demoted:
		assertNull(w.getTypeMap().get("Lcom/foo/Extender;"));
		rt = w.resolveToReferenceType("com.foo.Extender");
		delegate = rt.getDelegate();
		assertEquals("[com.foo.Marker]",stringify(delegate.getDeclaredInterfaces()));
		assertEquals("com.foo.Super",delegate.getSuperclass().toString());

		// this has one fixed annotation that is a well known one
		// Confirm demoted:
		ResolvedType annoType = w.getTypeMap().get("Lcom/foo/Anno;");
		assertNull(annoType);
		rt = w.resolveToReferenceType("com.foo.Anno");
		delegate = rt.getDelegate();
		assertEquals("[Anno[Ljava/lang/annotation/Retention; rVis value=E(Ljava/lang/annotation/RetentionPolicy; RUNTIME)]]",stringify(delegate.getAnnotations()));
		assertEquals("[java.lang.annotation.Retention]",stringify(delegate.getAnnotationTypes()));
		assertTrue(delegate.isAnnotationWithRuntimeRetention());
		assertEquals("RUNTIME",delegate.getRetentionPolicy());

		// this has a bunch of well known ones
		rt = w.resolveToReferenceType("com.foo.Anno2");
		delegate = rt.getDelegate();
		assertEquals("[Anno[Ljava/lang/Deprecated; rVis] Anno[Ljava/lang/annotation/Inherited; rVis] Anno[Ljava/lang/annotation/Retention; rVis value=E(Ljava/lang/annotation/RetentionPolicy; CLASS)]]",
				stringify(delegate.getAnnotations()));
		assertEquals("[java.lang.Deprecated java.lang.annotation.Inherited java.lang.annotation.Retention]",stringify(delegate.getAnnotationTypes()));
		assertFalse(delegate.isAnnotationWithRuntimeRetention());
		assertEquals("CLASS",delegate.getRetentionPolicy());
		assertTrue(delegate.hasAnnotation(UnresolvedType.forSignature("Ljava/lang/annotation/Inherited;")));
		assertTrue(delegate.hasAnnotation(UnresolvedType.forSignature("Ljava/lang/annotation/Retention;")));
		assertFalse(delegate.hasAnnotation(UnresolvedType.forSignature("Lcom/foo/Anno;")));

		// this has a well known one and a non-well known one
		rt = w.resolveToReferenceType("com.foo.Anno3");
		delegate = rt.getDelegate();
		System.out.println(stringify(delegate.getAnnotations()));
		assertEquals("[Anno[Lcom/foo/Anno; rVis] Anno[Ljava/lang/annotation/Retention; rVis value=E(Ljava/lang/annotation/RetentionPolicy; SOURCE)]]",stringify(delegate.getAnnotations()));
		assertEquals("[com.foo.Anno java.lang.annotation.Retention]",stringify(delegate.getAnnotationTypes()));
		assertFalse(delegate.isAnnotationWithRuntimeRetention());
		assertEquals("SOURCE",delegate.getRetentionPolicy());

		// this has two non-well known ones
		rt = w.resolveToReferenceType("com.foo.Anno4");
		delegate = rt.getDelegate();
		assertEquals("[Anno[Lcom/foo/Anno2; rInvis] Anno[Lcom/foo/Anno; rVis]]",stringify(delegate.getAnnotations()));
		assertEquals("[com.foo.Anno2 com.foo.Anno]",stringify(delegate.getAnnotationTypes()));
		assertFalse(delegate.isAnnotationWithRuntimeRetention());
		assertNull(delegate.getRetentionPolicy());
		assertTrue(delegate.hasAnnotation(UnresolvedType.forSignature("Lcom/foo/Anno;")));


		rt = w.resolveToReferenceType("com.foo.Colored");
		delegate = rt.getDelegate();
		AnnotationAJ annotation = delegate.getAnnotations()[0]; // should be ColorAnno(c=Color.G)
		assertTrue(annotation.hasNamedValue("c"));
		assertFalse(annotation.hasNamedValue("value"));
		assertTrue(annotation.hasNameValuePair("c","Lcom/foo/Color;G"));
		assertFalse(annotation.hasNameValuePair("c","Lcom/foo/Color;B"));
		assertFalse(annotation.hasNameValuePair("d","xxx"));
		assertNull(annotation.getStringFormOfValue("d"));
		assertEquals("Lcom/foo/Color;G",annotation.getStringFormOfValue("c"));
		assertEquals(0,annotation.getTargets().size());
		assertTrue(delegate.isCacheable());

		assertFalse(delegate.isExposedToWeaver());

		//		assertEquals(w.resolve(UnresolvedType.forSignature("Lcom/foo/Colored;")),delegate.getResolvedTypeX());

		assertEquals("com/foo/Colored.java",delegate.getSourcefilename());

		// Anno5 has an @Target annotation
		rt = w.resolveToReferenceType("com.foo.Anno5");
		delegate = rt.getDelegate();
		annotation = delegate.getAnnotations()[0]; // should be @Target(TYPE,FIELD)
		Set<String> ss = annotation.getTargets();
		assertEquals(2,ss.size());
		assertTrue(ss.contains("FIELD"));
		assertTrue(ss.contains("TYPE"));
		//		AnnotationTargetKind[] kinds = delegate.getAnnotationTargetKinds();
		//		assertEquals("FIELD",kinds[0].getName());
		//		assertEquals("TYPE",kinds[1].getName());

		rt = w.resolveToReferenceType("com.foo.Inners$Inner");
		delegate = rt.getDelegate();
		assertTrue(delegate.isNested());
		assertEquals("com.foo.Inners",delegate.getOuterClass().getName());

		rt = w.resolveToReferenceType("com.foo.Inners$1");
		delegate = rt.getDelegate();
		assertTrue(delegate.isAnonymous());
		assertTrue(delegate.isNested());

		//		delegate = w.resolveToReferenceType("com.foo.Anno6").getDelegate();
		//		kinds = delegate.getAnnotationTargetKinds();
		//		assertEquals(6,kinds.length);
		//		String s = stringify(kinds);
		//		assertTrue(s.contains("ANNOTATION_TYPE"));
		//		assertTrue(s.contains("LOCAL_VARIABLE"));
		//		assertTrue(s.contains("METHOD"));
		//		assertTrue(s.contains("PARAMETER"));
		//		assertTrue(s.contains("PACKAGE"));
		//		assertTrue(s.contains("CONSTRUCTOR"));

		delegate = w.resolveToReferenceType("com.foo.Marker").getDelegate();
		assertTrue(delegate.isInterface());

	}

	private void assertNotInTypeMap(World w, String string) {
		assertNull(w.getTypeMap().get(string));
	}

	private String stringify(Object[] arr) {
		StringBuilder s = new StringBuilder();
		for (int i=0;i<arr.length;i++) {
			s.append(arr[i]);
			s.append(" ");
		}
		return "["+s.toString().trim()+"]";
	}

	public void testDeletionInnerAspects_278496_4() throws Exception {
		String p = "PR278496_4";
		initialiseProject(p);
		configureNonStandardCompileOptions(p, "-Xset:minimalModel=true");
		build(p);
		checkWasFullBuild();
		printModel(p);
		// Here is the model without deletion.
		//		PR278496_4  [build configuration file]  hid:=PR278496_4
		//		  foo  [package]  hid:=PR278496_4<foo
		//		    MyOtherClass.java  [java source file] 1 hid:=PR278496_4<foo{MyOtherClass.java
		//		      foo  [package declaration] 1 hid:=PR278496_4<foo{MyOtherClass.java%foo
		//		        [import reference]  hid:=PR278496_4<foo{MyOtherClass.java#
		//		      MyOtherClass  [class] 2 hid:=PR278496_4<foo{MyOtherClass.java[MyOtherClass
		//		        MyInnerClass  [class] 4 hid:=PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass
		//		          MyInnerInnerAspect  [aspect] 6 hid:=PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass}MyInnerInnerAspect
		//		            before(): <anonymous pointcut>  [advice] 8 hid:=PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass}MyInnerInnerAspect&before
		//		    MyClass.java  [java source file] 1 hid:=PR278496_4<foo{MyClass.java
		//		      foo  [package declaration] 1 hid:=PR278496_4<foo{MyClass.java%foo
		//		        [import reference]  hid:=PR278496_4<foo{MyClass.java#
		//		      MyClass  [class] 9 hid:=PR278496_4<foo{MyClass.java[MyClass
		//		        main(java.lang.String[])  [method] 12 hid:=PR278496_4<foo{MyClass.java[MyClass~main~\[QString;
		//		        method1()  [method] 16 hid:=PR278496_4<foo{MyClass.java[MyClass~method1
		//		        method2()  [method] 18 hid:=PR278496_4<foo{MyClass.java[MyClass~method2
		//		Hid:1:(targets=1) =PR278496_4<foo{MyClass.java[MyClass~method1 (advised by) =PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass}MyInnerInnerAspect&before
		//		Hid:2:(targets=1) =PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass}MyInnerInnerAspect&before (advises) =PR278496_4<foo{MyClass.java[MyClass~method1

		AspectJElementHierarchy model = (AspectJElementHierarchy) getModelFor(p).getHierarchy();
		IProgramElement ipe = model.findElementForHandleOrCreate(
				"=PR278496_4<foo{MyOtherClass.java[MyOtherClass[MyInnerClass'MyInnerInnerAspect", false);
		assertNotNull(ipe);
	}
}
