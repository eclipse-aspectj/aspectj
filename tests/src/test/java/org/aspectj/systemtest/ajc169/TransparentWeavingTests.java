/*******************************************************************************
 * Copyright (c) 2008 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc169;

import java.lang.reflect.Modifier;

import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * What would a completely transparent weave be? Is there a minimal subset that makes sense? What is the roadmap to get there? What
 * needs testing
 * 
 * --- 'Transparent' here is meaning that the resultant bytecode is more representative of the original declarations, so that it
 * looks like the intertype declaration and associated constructs have been seamless added to the affected targets.
 * 
 * 
 * Fully transparent weaving, what would we like to have: - ITDs appear exactly as declared: 'private int A.i' will create 'private
 * int i' in A
 * 
 * - What is the benefit? - although this isn't really in keeping with the AspectJ definition of what an ITD represents, having the
 * end result look like the declaration does make it easier for users simply looking at the resultant class file or attempting
 * reflection to access what they just ITD'd in place
 * 
 * 
 * testing For transparent weaving of ITD fields - annotations on new fields - AJDT model - AjType support - what happens to it? -
 * advice on within() how does that get affected? - visibility - accessors created when required? - handling clashes with existing
 * fields - handling clashes with other aspects - generic declarations - interface declarations - initializers - static and
 * non-static - accessibility from advice, for read and write
 * 
 * Design<br>
 * The intention will be 'new code' uses the new style whilst old code continues to cause the old code to be built. Whether the code
 * wants to use the old or new naming should be apparent from the
 * 
 * @author Andy Clement
 */
public class TransparentWeavingTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// Simple private ITD onto a target
	public void testSimplePrivate() throws Exception {
		runTest("one - private");
		checkForField("OnePrivate", Modifier.PRIVATE, "x");
	}

	// Default visibility ITD field
	public void testSimpleDefault() throws Exception {
		runTest("one - default");
		checkForField("OneDefault", 0, "x");
	}

	// annotated private ITD
	public void testSimplePrivateAnnotated() throws Exception {
		runTest("one - private - annotated");
		Field f = checkForField("OnePrivateAnnotated", Modifier.PRIVATE, "x");
		AnnotationGen[] annos = f.getAnnotations();
		assertTrue(annos.length > 0); // 0==Anno 1==ajcITD
		assertEquals("LAnno;", annos[0].getTypeSignature());
	}

	// annotated default ITD
	public void testSimpleDefaultAnnotated() throws Exception {
		runTest("one - default - annotated");
		Field f = checkForField("OneDefaultAnnotated", 0, "x");
		AnnotationGen[] annos = f.getAnnotations();
		assertTrue(annos.length > 0); // 0==Anno 1==ajcITD
		assertEquals("LAnno;", annos[0].getTypeSignature());
	}

	// Simple private ITD with getter/setter usage
	public void testSimplePrivateWithAccessors() throws Exception {
		runTest("one - private - accessors");
	}

	// check initializer runs OK
	public void testSimplePrivateInitializer() throws Exception {
		runTest("one - private - initializer");
	}

	public void testDeclareAtOnPrivateItd() throws Exception {
		runTest("declare at on private itd");
		Field f = checkForField("OneDeclareAt", Modifier.PRIVATE, "x");
		AnnotationGen[] annos = f.getAnnotations();
		assertTrue(annos.length > 0); // 1==Anno 0==ajcITD
		assertEquals("LAnno;", annos[1].getTypeSignature());
	}

	// declare @field on a field that already has one
	public void testDeclareAtTwo() throws Exception {
		runTest("declare at two");
		Field f = checkForField("DeclareAtTwo", Modifier.PRIVATE, "x");
		AnnotationGen[] annos = f.getAnnotations();
		assertTrue(annos.length > 2); // 1==Anno 0==ajcITD
		assertEquals("LAnno;", annos[0].getTypeSignature());
		assertEquals("LAnno2;", annos[2].getTypeSignature());
	}

	public void testTwoItdsOnTarget() throws Exception {
		runTest("two itds on target");
		// Aspect X gets the field, aspect Y gets a mangled one
		if (hasField("TwoItdsOnTarget", "ajc$interField$Y$x")) {
			checkForField("TwoItdsOnTarget", Modifier.PRIVATE, "x");
			checkForField("TwoItdsOnTarget", Modifier.PUBLIC, "ajc$interField$Y$x");
		} else {
			checkForField("TwoItdsOnTarget", Modifier.PRIVATE, "x");
			checkForField("TwoItdsOnTarget", Modifier.PUBLIC, "ajc$interField$X$x");
		}
	}

	public void testTwoItdsOnTargetThatAlreadyHasIt() throws Exception {
		runTest("two itds on target that already has it");
		// Aspect X gets the field, aspect Y gets a mangled one
		checkForField("TwoItdsOnTargetHasAlready", Modifier.PUBLIC, "ajc$interField$X$x");
		checkForField("TwoItdsOnTargetHasAlready", Modifier.PUBLIC, "ajc$interField$Y$x");
	}

	public void testInteractingOldAndNew() throws Exception {
		runTest("interacting old and new");
		int SYNTHETIC = 0x00001000; // 4096
		if (hasField("InteractingOldAndNew", "ajc$interField$Y$i")) {
			checkForField("InteractingOldAndNew", Modifier.PRIVATE, "i");
			checkForField("InteractingOldAndNew", Modifier.PUBLIC, "ajc$interField$Y$i");
		} else {
			checkForField("InteractingOldAndNew", Modifier.PRIVATE, "i");
			checkForField("InteractingOldAndNew", Modifier.PUBLIC, "ajc$interField$X$i");
		}
		checkForMethod("InteractingOldAndNew", Modifier.PUBLIC | Modifier.STATIC, "main");
		checkForMethod("InteractingOldAndNew", Modifier.PUBLIC | Modifier.STATIC | SYNTHETIC, "ajc$get$i");
		checkForMethod("InteractingOldAndNew", Modifier.PUBLIC | Modifier.STATIC | SYNTHETIC, "ajc$set$i");
		checkForMethod("InteractingOldAndNew", Modifier.PUBLIC, "getI1");
		checkForMethod("InteractingOldAndNew", Modifier.PUBLIC, "getI2");
		checkForMethod("InteractingOldAndNew", Modifier.PUBLIC, "setI1");
		checkForMethod("InteractingOldAndNew", Modifier.PUBLIC, "setI2");
	}

	public void testPrivateGenerics() throws Exception {
		runTest("generics - private");
		Field f = checkForField("Generics", Modifier.PRIVATE, "listOfString");
		assertEquals("Ljava/util/List<Ljava/lang/String;>;", f.getGenericSignature());
		f = checkForField("Generics", Modifier.PRIVATE, "thing");
		assertEquals("TX;", f.getGenericSignature());
	}

	// ---

	private boolean hasField(String clazzname, String name) {
		try {
			JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), clazzname);
			Field[] fs = jc.getFields();
			StringBuffer fields = new StringBuffer();
			for (Field f : fs) {
				fields.append(f.getName()).append(" ");
				if (f.getName().equals(name)) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	private Field checkForField(String clazzname, int modifiers, String name) throws Exception {
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), clazzname);
		Field[] fs = jc.getFields();
		StringBuffer fields = new StringBuffer();
		for (Field f : fs) {
			fields.append(f.getName()).append(" ");
			if (f.getName().equals(name)) {
				if (f.getModifiers() != modifiers) {
					fail("Found field " + name + " in " + clazzname + " but modifiers were wrong, they were " + f.getModifiers());
				}
				return f;
			}
		}
		fail("Did not find field " + name + " in class " + clazzname + ".  Found fields: " + fields.toString());
		return null;
	}

	private Method checkForMethod(String clazzname, int modifiers, String name) throws Exception {
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), clazzname);
		Method[] fs = jc.getMethods();
		StringBuffer methods = new StringBuffer();
		methods.append("\n");
		for (Method f : fs) {
			methods.append(f.getName()).append("\n");
			if (f.getName().equals(name)) {
				if (f.getModifiers() != modifiers) {
					fail("Found method " + name + " in " + clazzname + " but modifiers were wrong, they were " + f.getModifiers());
				}
				return f;
			}
			System.out.println(f.getGenericSignature());
		}
		fail("Did not find method " + name + " in class " + clazzname + ".  Found methods: " + methods.toString());
		return null;
	}

	// public itd onto a target that already has a field of that name
	// just to check what goes wrong and who checks it
	public void testPublicClash() throws Exception {
		runTest("two");
	}

	public void testPrivateClash() throws Exception {
		runTest("three");

		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "Three");
		Field[] fs = jc.getFields();
		for (Field f : fs) {
			System.out.println(f);
		}

		// public int ajc$interField$X$xPrivate [RuntimeVisibleAnnotations]
		// public Integer ajc$interField$$yDefault [RuntimeVisibleAnnotations]
		// public String zPublic [RuntimeVisibleAnnotations]

	}

	// --

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(TransparentWeavingTests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("transparentweaving.xml");
	}

}