/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 *     Matthew Webster  move Java 5 tests           
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.weaver.AbstractWorldTestCase;
import org.aspectj.weaver.BcweaverTests;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;


/**
 * This is a test case for the nameType parts of worlds.
 */
public class AsmDelegateTests5 extends AbstractWorldTestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(AsmDelegateTests5.class.getName());
		suite.addTestSuite(AsmDelegateTests5.class);
		return suite;
	}

    private final BcelWorld world = new BcelWorld(BcweaverTests.TESTDATA_PATH+"/forAsmDelegateTesting/stuff.jar");

    public AsmDelegateTests5(String name) {
        super(name);
    }

	protected World getWorld() {
		return world;
	}
	
	// --- testcode

    /**
     * Methods are transformed according to generic signatures - this checks 
     * that some of the generic methods in java.lang.Class appear the same 
     * whether viewed through an ASM or a BCEL delegate.
     */
    public void testCompareGenericMethods() {
        BcelWorld slowWorld = new BcelWorld();
        slowWorld.setFastDelegateSupport(false);
        slowWorld.setBehaveInJava5Way(true);
        
        BcelWorld fastWorld = new BcelWorld();
        fastWorld.setBehaveInJava5Way(true);

        ResolvedType bcelJavaLangClass = slowWorld.resolve(UnresolvedType.forName("java.lang.Class"));
        ResolvedType  asmJavaLangClass = fastWorld.resolve(UnresolvedType.forName("java.lang.Class"));
        
        bcelJavaLangClass = bcelJavaLangClass.getGenericType();
        asmJavaLangClass  = asmJavaLangClass.getGenericType();
    	
    	ResolvedMember[] bcelMethods = bcelJavaLangClass.getDeclaredMethods();
    	ResolvedMember[]  asmMethods = asmJavaLangClass.getDeclaredMethods();
    	
    	for (int i = 0; i < bcelMethods.length; i++) {
    		bcelMethods[i].setParameterNames(null); // forget them, asm delegates dont currently know them
    		String one = bcelMethods[i].toDebugString();
    		String two = asmMethods[i].toDebugString();
    		if (!one.equals(two)) {
    			fail("These methods look different when viewed through ASM or BCEL\nBCEL='"+bcelMethods[i].toDebugString()+
    				 "'\n ASM='"+asmMethods[i].toDebugString()+"'");
    		}
    		// If one is parameterized, check the other is...
    		if (bcelMethods[i].canBeParameterized()) {
    			assertTrue("ASM method '"+asmMethods[i].toDebugString()+"' can't be parameterized whereas its' BCEL variant could",
    				       asmMethods[i].canBeParameterized());
    		}
			
		}
    	
    	// Let's take a special look at:
    	//   public <U> Class<? extends U> asSubclass(Class<U> clazz)
    	ResolvedMember bcelSubclassMethod = null;
    	for (int i = 0; i < bcelMethods.length; i++) {
			if (bcelMethods[i].getName().equals("asSubclass")) { bcelSubclassMethod = bcelMethods[i]; break; }
		}
    	ResolvedMember asmSubclassMethod = null;
    	for (int i = 0; i < asmMethods.length; i++) {
			if (asmMethods[i].getName().equals("asSubclass")) { asmSubclassMethod = asmMethods[i];break;	}
		}
    	
    	TypeVariable[] tvs = bcelSubclassMethod.getTypeVariables();
    	assertTrue("should have one type variable on the bcel version but found: "+format(tvs),tvs!=null && tvs.length==1);
        tvs = asmSubclassMethod.getTypeVariables();
    	assertTrue("should have one type variable on the asm version but found: "+format(tvs),tvs!=null && tvs.length==1);

    }
    
    private String format(TypeVariable[] tvs) {
    	if (tvs==null) return "null";
    	StringBuffer s = new StringBuffer();
    	s.append("[");
    	for (int i = 0; i < tvs.length; i++) {
			s.append(tvs[i]);
			if ((i+1)<tvs.length) s.append(",");
		}
    	s.append("]");
    	return s.toString();
    }
    
    public void testCompareGenericFields() {
        BcelWorld slowWorld = new BcelWorld();
        slowWorld.setFastDelegateSupport(false);
        slowWorld.setBehaveInJava5Way(true);
        
        BcelWorld fastWorld = new BcelWorld();
        fastWorld.setBehaveInJava5Way(true);

        ResolvedType bcelJavaLangClass = slowWorld.resolve(UnresolvedType.forName("java.lang.Class"));
        ResolvedType  asmJavaLangClass = fastWorld.resolve(UnresolvedType.forName("java.lang.Class"));
        
        bcelJavaLangClass = bcelJavaLangClass.getGenericType();
        asmJavaLangClass = asmJavaLangClass.getGenericType();
    	
    	ResolvedMember[] bcelFields = bcelJavaLangClass.getDeclaredFields();
    	ResolvedMember[]  asmFields = asmJavaLangClass.getDeclaredFields();
    	
    	for (int i = 0; i < bcelFields.length; i++) {
    		UnresolvedType bcelFieldType = bcelFields[i].getGenericReturnType();
    		UnresolvedType asmFieldType = asmFields[i].getGenericReturnType();
    		if (!bcelFields[i].getGenericReturnType().toDebugString().equals(asmFields[i].getGenericReturnType().toDebugString())) {
    			fail("These fields look different when viewed through ASM or BCEL\nBCEL='"+bcelFieldType.toDebugString()+
    				 "'\n ASM='"+asmFieldType.toDebugString()+"'");
    		}
		}
    }
    
}
