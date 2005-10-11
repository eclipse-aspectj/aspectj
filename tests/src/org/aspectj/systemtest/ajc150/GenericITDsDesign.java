package org.aspectj.systemtest.ajc150;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.tools.ajc.Ajc;
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

public class GenericITDsDesign extends XMLBasedAjcTestCase {

	private World recentWorld;
	
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(GenericITDsDesign.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc150/ajc150.xml");
	}

	public static Signature getClassSignature(Ajc ajc,String classname) {
		try {
			ClassPath cp = 
				new ClassPath(ajc.getSandboxDirectory() + File.pathSeparator + System.getProperty("java.class.path"));
		    SyntheticRepository sRepos =  SyntheticRepository.getInstance(cp);
			JavaClass clazz = sRepos.loadClass(classname);
			Signature sigAttr = null;
			Attribute[] attrs = clazz.getAttributes();
			for (int i = 0; i < attrs.length; i++) {
				Attribute attribute = attrs[i];
				if (attribute.getName().equals("Signature")) sigAttr = (Signature)attribute;
			}
			return sigAttr;
		} catch (ClassNotFoundException e) {
			fail("Couldn't find class "+classname+" in the sandbox directory.");
		}
		return null;
	}
	// Check the signature attribute on a class is correct
	public static void verifyClassSignature(Ajc ajc,String classname,String sig) {
		Signature sigAttr = getClassSignature(ajc,classname);
		assertTrue("Failed to find signature attribute for class "+classname,sigAttr!=null);
		assertTrue("Expected signature to be '"+sig+"' but was '"+sigAttr.getSignature()+"'",
				sigAttr.getSignature().equals(sig));		
	}
		
	public List /*BcelTypeMunger*/ getTypeMunger(String classname) {
		ClassPath cp = 
			new ClassPath(ajc.getSandboxDirectory() + File.pathSeparator + 
					      System.getProperty("java.class.path"));
		recentWorld = new BcelWorld(cp.toString());
		ReferenceType resolvedType = (ReferenceType)recentWorld.resolve(classname);
		CrosscuttingMembers cmembers = resolvedType.collectCrosscuttingMembers();
		List tmungers = cmembers.getTypeMungers();
		return tmungers;
	}
	
	private BcelTypeMunger getMungerFromLine(String classname,int linenumber) {
		List allMungers = getTypeMunger(classname);
		for (Iterator iter = allMungers.iterator(); iter.hasNext();) {
			BcelTypeMunger element = (BcelTypeMunger) iter.next();
			if (element.getMunger().getSourceLocation().getLine()==linenumber) return element;
		}
		for (Iterator iter = allMungers.iterator(); iter.hasNext();) {
			BcelTypeMunger element = (BcelTypeMunger) iter.next();
			System.err.println("Line: "+element.getMunger().getSourceLocation().getLine()+"  > "+element);
		}
		fail("Couldn't find a type munger from line "+linenumber+" in class "+classname);
		return null;
	}
	/* 
		test plan:
		  1. Serializing and recovering 'default bounds' type variable info:
		     a. methods
		     b. fields
		     c. ctors
		  2. Serializing and recovering 'extends' with a class bounded type variable info: 
		     a. methods
		     b. fields
		     c. ctors
		  3. Serializing and recovering 'extends' with an interface bounded type variable info:
		     a. methods
		     b. fields
		     c. ctors
		  4. Multiple interface bounds
		     a. methods
		     b. fields
		     c. ctors
		  5. wildcard bounds '? extends/? super'
		     a. methods
		     b. fields
		     c. ctors
		  6. using type variables in an ITD from the containing aspect, no bounds
		     a. methods
		     b. fields
		     c. ctors
		  
	
	*/ 
	
	
	// Verify: a) After storing it in a class file and recovering it (through deserialization), we can see the type
	//            variable and that the parameter refers to the type variable.
	public void testDesignA() {
		runTest("generic itds - design A"); 
		BcelTypeMunger theBcelMunger = getMungerFromLine("X",5);
		ResolvedType typeC = recentWorld.resolve("C");
		ResolvedTypeMunger rtMunger = theBcelMunger.getMunger();
		ResolvedMember theMember = rtMunger.getSignature();
		// Let's check all parts of the member
		assertTrue("Declaring type should be C: "+theMember,
				theMember.getDeclaringType().equals(typeC));
		
		TypeVariable tVar = theMember.getTypeVariables()[0];
		TypeVariableReference tvrt = (TypeVariableReference)theMember.getParameterTypes()[0];
		
		theMember.resolve(recentWorld); // resolution will join the type variables together (i.e. make them refer to the same instance)
		
		tVar = theMember.getTypeVariables()[0];
		tvrt = (TypeVariableReference)theMember.getParameterTypes()[0];
		
		assertTrue("Post resolution, the type variable in the parameter should be identical to the type variable declared on the member",
				tVar==tvrt.getTypeVariable());
	}
	
	// Verify: bounds are preserved and accessible after serialization
	public void xtestDesignB() {
		runTest("generic itds - design B"); 
		BcelTypeMunger theBcelMunger = getMungerFromLine("X",7);
	}
	
	// Verify: a) multiple type variables work. 
	//         b) type variables below the 'top level' (e.g. List<A>) are preserved.
	public void xtestDesignC() {
		runTest("generic itds - design B"); 
		BcelTypeMunger theBcelMunger = getMungerFromLine("X",7);		
	}
	
	
	/*
	 * broken stuff:
	 * 
	 * When generic signatures are unpacked from members, the typevariables attached to the bcelmethod/field won't
	 * be the same instances as those held in the TypeVariableReferenceTypes for anything that occurs in the
	 * return type or parameterset - we should perhaps fix that up.
	 */
		

}
