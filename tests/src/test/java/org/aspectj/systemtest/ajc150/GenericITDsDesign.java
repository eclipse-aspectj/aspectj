package org.aspectj.systemtest.ajc150;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.tools.ajc.Ajc;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.CrosscuttingMembers;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.TypeVariableReference;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelTypeMunger;
import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.Test;

public class GenericITDsDesign extends XMLBasedAjcTestCase {

	private World recentWorld;

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(GenericITDsDesign.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc150.xml");
	}

	private void verifyDebugString(ResolvedMember theMember, String string) {
		assertTrue("Expected '" + string + "' but found " + theMember.toDebugString(), theMember.toDebugString().equals(string));
	}

	public static JavaClass getClassFromDisk(Ajc ajc, String classname) {
		try {
			ClassPath cp = new ClassPath(ajc.getSandboxDirectory() + File.pathSeparator + System.getProperty("java.class.path"));
			SyntheticRepository sRepos = SyntheticRepository.getInstance(cp);
			return sRepos.loadClass(classname);
		} catch (ClassNotFoundException e) {
			fail("Couldn't find class " + classname + " in the sandbox directory.");
		}
		return null;
	}

	public static Signature getClassSignature(Ajc ajc, String classname) {
		JavaClass clazz = getClassFromDisk(ajc, classname);
		Signature sigAttr = null;
		Attribute[] attrs = clazz.getAttributes();
		for (Attribute attribute : attrs) {
			if (attribute.getName().equals("Signature")) {
				sigAttr = (Signature) attribute;
			}
		}
		return sigAttr;
	}

	// Check the signature attribute on a class is correct
	public static void verifyClassSignature(Ajc ajc, String classname, String sig) {
		Signature sigAttr = getClassSignature(ajc, classname);
		assertTrue("Failed to find signature attribute for class " + classname, sigAttr != null);
		assertTrue("Expected signature to be '" + sig + "' but was '" + sigAttr.getSignature() + "'", sigAttr.getSignature()
				.equals(sig));
	}

	public List<ConcreteTypeMunger> getTypeMunger(String classname) {
		ClassPath cp = new ClassPath(ajc.getSandboxDirectory() + File.pathSeparator + System.getProperty("java.class.path"));
		recentWorld = new BcelWorld(cp.toString());
		ReferenceType resolvedType = (ReferenceType) recentWorld.resolve(classname);
		CrosscuttingMembers cmembers = resolvedType.collectCrosscuttingMembers(true);
		List<ConcreteTypeMunger> tmungers = cmembers.getTypeMungers();
		return tmungers;
	}

	private BcelTypeMunger getMungerFromLine(String classname, int linenumber) {
		List allMungers = getTypeMunger(classname);
		for (Object munger : allMungers) {
			BcelTypeMunger element = (BcelTypeMunger) munger;
			if (element.getMunger().getSourceLocation().getLine() == linenumber) {
				return element;
			}
		}
		for (Object allMunger : allMungers) {
			BcelTypeMunger element = (BcelTypeMunger) allMunger;
			System.err.println("Line: " + element.getMunger().getSourceLocation().getLine() + "  > " + element);
		}
		fail("Couldn't find a type munger from line " + linenumber + " in class " + classname);
		return null;
	}

	public Hashtable<String,Field> getMeTheFields(String classname) {
		JavaClass theClass = getClassFromDisk(ajc, classname);
		Hashtable<String,Field> retval = new Hashtable<>();
		org.aspectj.apache.bcel.classfile.Field[] fs = theClass.getFields();
		for (Field field : fs) {
			retval.put(field.getName(), field);
		}
		return retval;
	}

	/*
	 * test plan: 1. Serializing and recovering 'default bounds' type variable info: a. methods b. fields c. ctors 2. Serializing
	 * and recovering 'extends' with a class bounded type variable info: a. methods b. fields c. ctors 3. Serializing and recovering
	 * 'extends' with an interface bounded type variable info: a. methods b. fields c. ctors 4. Multiple interface bounds a. methods
	 * b. fields c. ctors 5. wildcard bounds '? extends/? super' a. methods b. fields c. ctors 6. using type variables in an ITD
	 * from the containing aspect, no bounds a. methods b. fields c. ctors
	 */

	// Verify: a) After storing it in a class file and recovering it (through deserialization), we can see the type
	// variable and that the parameter refers to the type variable.
	public void testDesignA() {
		runTest("generic itds - design A");
		BcelTypeMunger theBcelMunger = getMungerFromLine("X", 5);
		ResolvedType typeC = recentWorld.resolve("C");
		ResolvedTypeMunger rtMunger = theBcelMunger.getMunger();
		ResolvedMember theMember = rtMunger.getSignature();
		// Let's check all parts of the member
		assertTrue("Declaring type should be C: " + theMember, theMember.getDeclaringType().equals(typeC));

		TypeVariable tVar = theMember.getTypeVariables()[0];
		TypeVariableReference tvrt = (TypeVariableReference) theMember.getParameterTypes()[0];

		theMember.resolve(recentWorld); // resolution will join the type variables together (i.e. make them refer to the same
		// instance)

		tVar = theMember.getTypeVariables()[0];
		tvrt = (TypeVariableReference) theMember.getParameterTypes()[0];

		assertTrue(
				"Post resolution, the type variable in the parameter should be identical to the type variable declared on the member",
				tVar == tvrt.getTypeVariable());
	}

	// Verify: bounds are preserved and accessible after serialization
	public void testDesignB() {
		runTest("generic itds - design B");
		BcelTypeMunger theBcelMunger = getMungerFromLine("X", 7);
		ResolvedTypeMunger rtMunger = theBcelMunger.getMunger();
		ResolvedMember theMember = rtMunger.getSignature();
		verifyDebugString(theMember, "<T extends java.lang.Number> void C.m0(T)");

		theBcelMunger = getMungerFromLine("X", 9);
		rtMunger = theBcelMunger.getMunger();
		theMember = rtMunger.getSignature();
		verifyDebugString(theMember, "<Q extends I> void C.m1(Q)");

		theBcelMunger = getMungerFromLine("X", 11);
		rtMunger = theBcelMunger.getMunger();
		theMember = rtMunger.getSignature();
		verifyDebugString(theMember, "<R extends java.lang.Number,I> void C.m2(R)");
	}

	// Verify: a) multiple type variables work.
	// b) type variables below the 'top level' (e.g. List<A>) are preserved.
	public void testDesignC() {
		runTest("generic itds - design C");
		BcelTypeMunger theBcelMunger = getMungerFromLine("X", 9);
		// System.err.println(theBcelMunger.getMunger().getSignature().toDebugString());
		verifyDebugString(theBcelMunger.getMunger().getSignature(), "<T extends java.lang.Number,Q extends I> void C.m0(T, Q)");

		theBcelMunger = getMungerFromLine("X", 11);
		// System.err.println(theBcelMunger.getMunger().getSignature().toDebugString());
		verifyDebugString(theBcelMunger.getMunger().getSignature(), "<A,B,C> java.util.List<A> C.m1(B, java.util.Collection<C>)");
	}

	// Verify: a) sharing type vars with some target type results in the correct variable names in the serialized form
	public void testDesignD() {
		runTest("generic itds - design D");
		BcelTypeMunger theBcelMunger = getMungerFromLine("X", 9);
		// System.err.println(theBcelMunger.getMunger().getSignature().toDebugString());
		verifyDebugString(theBcelMunger.getMunger().getSignature(), "void C.m0(R)");

		theBcelMunger = getMungerFromLine("X", 11);
		// System.err.println(theBcelMunger.getMunger().getSignature().toDebugString());
		verifyDebugString(theBcelMunger.getMunger().getSignature(),
				"java.util.List<Q> C.m0(Q, int, java.util.List<java.util.List<Q>>)");
	}

	// Verify: a) for fields, sharing type vars with some target type results in the correct entries in the class file
	public void testDesignE() {
		runTest("generic itds - design E");
		BcelTypeMunger theBcelMunger = getMungerFromLine("X", 9);
		verifyDebugString(theBcelMunger.getMunger().getSignature(), "java.util.List<Z> C.ln");
		assertTrue("Expected to find \"Z\": " + theBcelMunger.getTypeVariableAliases(), theBcelMunger.getTypeVariableAliases()
				.contains("Z"));

		theBcelMunger = getMungerFromLine("X", 11);
		verifyDebugString(theBcelMunger.getMunger().getSignature(), "Q C.n");
		assertTrue("Expected to find \"Q\": " + theBcelMunger.getTypeVariableAliases(), theBcelMunger.getTypeVariableAliases()
				.contains("Q"));
	}

	// Verifying what gets into a class targetted with a field ITD
	public void testDesignF() {
		runTest("generic itds - design F");
		Hashtable<String,Field> fields = getMeTheFields("C");

		// Declared in src as: List C.list1; and List<Z> C<Z>.list2;
		Field list1 = (Field) fields.get("list1");// ajc$interField$$list1");
		assertTrue("Field list1 should be of type 'Ljava/util/List;' but is " + list1.getSignature(), list1.getSignature().equals(
				"Ljava/util/List;"));
		Field list2 = (Field) fields.get("list2");// ajc$interField$$list1");
		assertTrue("Field list2 should be of type 'Ljava/util/List;' but is " + list2.getSignature(), list2.getSignature().equals(
				"Ljava/util/List;"));

		// Declared in src as: String C.field1; and Q C<Q>.field2;
		// bound for second field collapses to Object
		Field field1 = (Field) fields.get("field1");// ajc$interField$$field1");
		assertTrue("Field list1 should be of type 'Ljava/lang/String;' but is " + field1.getSignature(), field1.getSignature()
				.equals("Ljava/lang/String;"));
		Field field2 = (Field) fields.get("field2");// ajc$interField$$field2");
		assertTrue("Field list2 should be of type 'Ljava/lang/Object;' but is " + field2.getSignature(), field2.getSignature()
				.equals("Ljava/lang/Object;"));
	}

	// Verifying what gets into a class when an interface it implements was targetted with a field ITD
	public void testDesignG() {
		runTest("generic itds - design G");
		Hashtable<String,Field> fields = getMeTheFields("C");

		// The ITDs are targetting an interface. That interface is generic and is parameterized with
		// 'String' when implemented in the class C. This means the fields that make it into C should
		// be parameterized with String also.

		// List<Z> I<Z>.ln; and Q I<Q>.n;
		// Field field1 = (Field)fields.get("ajc$interField$X$I$ln");
		// assertTrue("Field list1 should be of type 'Ljava/util/List;' but is "+field1.getSignature(),
		// field1.getSignature().equals("Ljava/util/List;"));
		// Field field2 = (Field)fields.get("ajc$interField$X$I$n");
		// assertTrue("Field list2 should be of type 'Ljava/lang/String;' but is "+field2.getSignature(),
		// field2.getSignature().equals("Ljava/lang/String;"));
	}

	// // Verify: a) sharing type vars with some target type results in the correct variable names in the serialized form
	// public void testDesignE() {
	// runTest("generic itds - design E");
	//		
	// }

}
