/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import org.aspectj.testing.util.TestUtil;
import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

/**
 * This is a test case for all the portions of UnresolvedType that don't require a world.
 */
public class TypeXTestCase extends TestCase {

    public TypeXTestCase(String name) {
        super(name);
    }

    public void testUnresolvedTypes() {
        // basic equality
        String[] testNames = 
            new String[] {"int", "long", "int[]", "boolean[][]", 
                           "java.lang.String", "java.lang.String[]", "void" };
        String[] testSigs = 
            new String[] {"I", "J", "[I", "[[Z", 
                            "Ljava/lang/String;", "[Ljava/lang/String;", "V" };
        
        String[] componentNames = 
            new String[] {null, null, "int", "boolean[]",
                            null, "java.lang.String", null };

        int[] sizes = new int[] {1, 2, 1, 1, 1, 1, 0};
        
        boolean[] isPrimitive = 
            new boolean[] { true, true, false, false, false, false, true };
                                  
        nameSignatureTest(testNames, testSigs);
        arrayTest(UnresolvedType.forNames(testNames), componentNames);
        arrayTest(UnresolvedType.forSignatures(testSigs), componentNames);

        sizeTest(UnresolvedType.forNames(testNames), sizes);
        sizeTest(UnresolvedType.forSignatures(testSigs), sizes);
        
        isPrimitiveTest(UnresolvedType.forSignatures(testSigs), isPrimitive);        
    }
    
    public void testNameAndSigWithInners() {
    	UnresolvedType t = UnresolvedType.forName("java.util.Map$Entry");
    	assertEquals(t.getName(), "java.util.Map$Entry");
    	assertEquals(t.getSignature(), "Ljava/util/Map$Entry;");
    	assertEquals(t.getOutermostType(), UnresolvedType.forName("java.util.Map"));
    	assertEquals(UnresolvedType.forName("java.util.Map").getOutermostType(), UnresolvedType.forName("java.util.Map"));
    }
	
	public void testNameAndSigWithParameters() {
		UnresolvedType t = UnresolvedType.forName("java.util.List<java.lang.String>");
		assertEquals(t.getName(),"java.util.List<java.lang.String>");
		assertEquals(t.getSignature(),"Pjava/util/List<Ljava/lang/String;>;");
		t = UnresolvedType.forSignature("Pjava/util/List<Ljava/lang/String;>;");
		assertEquals(t.getName(),"java.util.List<java.lang.String>");
		assertEquals(t.getSignature(),"Pjava/util/List<Ljava/lang/String;>;");
		t = UnresolvedType.forName("java.util.Map<java.util.String,java.util.List<java.lang.Integer>>");
		assertEquals(t.getName(),"java.util.Map<java.util.String,java.util.List<java.lang.Integer>>");
		assertEquals(t.getSignature(),"Pjava/util/Map<Ljava/util/String;Pjava/util/List<Ljava/lang/Integer;>;>;");
		t = UnresolvedType.forSignature("Pjava/util/Map<Ljava/util/String;Pjava/util/List<Ljava/lang/Integer;>;>;");
		assertEquals(t.getName(),"java.util.Map<java.util.String,java.util.List<java.lang.Integer>>");
		assertEquals(t.getSignature(),"Pjava/util/Map<Ljava/util/String;Pjava/util/List<Ljava/lang/Integer;>;>;");
	}
	
	/**
	 * Verify UnresolvedType signature processing creates the right kind of UnresolvedType's from a signature.
	 * 
	 * For example, calling UnresolvedType.dump() for 
	 *   "Ljava/util/Map<Ljava/util/List<Ljava/lang/String;>;Ljava/lang/String;>;"
	 * results in:
	 *   UnresolvedType:  signature=Ljava/util/Map<Ljava/util/List<Ljava/lang/String;>;Ljava/lang/String;>; parameterized=true #params=2
     *     UnresolvedType:  signature=Ljava/util/List<Ljava/lang/String;>; parameterized=true #params=1
     *       UnresolvedType:  signature=Ljava/lang/String; parameterized=false #params=0
     *     UnresolvedType:  signature=Ljava/lang/String; parameterized=false #params=0
	 */
	public void testTypexGenericSignatureProcessing() {
		UnresolvedType tx = null;
		
		tx = UnresolvedType.forSignature("Pjava/util/Set<Ljava/lang/String;>;");
		checkTX(tx,true,1);
		
		tx = UnresolvedType.forSignature("Pjava/util/Set<Pjava/util/List<Ljava/lang/String;>;>;");
		checkTX(tx,true,1);
		
		tx = UnresolvedType.forSignature("Pjava/util/Map<Pjava/util/List<Ljava/lang/String;>;Ljava/lang/String;>;");
		checkTX(tx,true,2);
		checkTX(tx.getTypeParameters()[0],true,1);
		checkTX(tx.getTypeParameters()[1],false,0);
//		System.err.println(tx.dump());
	}
	
	public void testTypeXForParameterizedTypes() {
		World world = new BcelWorld();
		UnresolvedType stringType = UnresolvedType.forName("java/lang/String");
		ResolvedType listOfStringType =
			TypeFactory.createParameterizedType(
							UnresolvedType.forName("java/util/List").resolve(world),
							new UnresolvedType[] {stringType},
							world);
		assertEquals("1 type param",1,listOfStringType.typeParameters.length);
		assertEquals(stringType,listOfStringType.typeParameters[0]);
		assertTrue(listOfStringType.isParameterizedType());
		assertFalse(listOfStringType.isGenericType());
	}
	
	public void testTypeFactoryForParameterizedTypes() {
		UnresolvedType enumOfSimpleType =
			TypeFactory.createTypeFromSignature("Pjava/lang/Enum<Ljava/lang/String;>;");
		assertEquals(1, enumOfSimpleType.getTypeParameters().length);

		UnresolvedType enumOfNestedType =
			TypeFactory.createTypeFromSignature("Pjava/lang/Enum<Ljavax/jws/soap/SOAPBinding$ParameterStyle;>;");
		assertEquals(1, enumOfNestedType.getTypeParameters().length);

		// is this signature right?
		UnresolvedType nestedTypeOfParameterized =
			TypeFactory.createTypeFromSignature("PMyInterface<Ljava/lang/String;>$MyOtherType;");
		assertEquals(0, nestedTypeOfParameterized.getTypeParameters().length);

		// how about this one? is this valid?
		UnresolvedType doublyNestedTypeSignatures =
			TypeFactory.createTypeFromSignature("PMyInterface<Ljava/lang/String;Ljava/lang/String;>$MyOtherType<Ljava/lang/Object;>;");
		assertEquals(1, doublyNestedTypeSignatures.getTypeParameters().length);

	}
	
	private void checkTX(UnresolvedType tx,boolean shouldBeParameterized,int numberOfTypeParameters) {
		assertTrue("Expected parameterization flag to be "+shouldBeParameterized,tx.isParameterizedType()==shouldBeParameterized);
		if (numberOfTypeParameters==0) {
			UnresolvedType[] params = tx.getTypeParameters();
			assertTrue("Expected 0 type parameters but found "+params.length, params.length==0);
	    } else {
				assertTrue("Expected #type parameters to be "+numberOfTypeParameters,tx.getTypeParameters().length==numberOfTypeParameters);
	    }
	}
	

    private void isPrimitiveTest(UnresolvedType[] types, boolean[] isPrimitives) {
        for (int i = 0, len = types.length; i < len; i++) {
            UnresolvedType type = types[i];
            boolean b = isPrimitives[i];
            assertEquals(type + " is primitive: ", b, type.isPrimitiveType());
        }           
    }

    private void sizeTest(UnresolvedType[] types, int[] sizes) {
        for (int i = 0, len = types.length; i < len; i++) {
            UnresolvedType type = types[i];
            int size = sizes[i];
            assertEquals("size of " + type + ": ", size, type.getSize());
        }           
    }

    private void arrayTest(UnresolvedType[] types, String[] components) {
        for (int i = 0, len = types.length; i < len; i++) {
            UnresolvedType type = types[i];
            String component = components[i];
            assertEquals(type + " is array: ", component != null, type.isArray());
            if (component != null) 
                assertEquals(type + " componentType: ", component, 
                    type.getComponentType().getName());
        }                
    }

    private void nameSignatureTest(String[] ns, String[] ss) {
        for (int i = 0, len = ns.length; i < len; i++) {
            String n = ns[i];
            String s = ss[i];
            UnresolvedType tn = UnresolvedType.forName(n);
            UnresolvedType ts = UnresolvedType.forSignature(s);

            assertEquals("forName(n).getName()", n, 
                tn.getName());
            assertEquals("forSignature(s).getSignature()", s, 
                ts.getSignature());
            assertEquals("forName(n).getSignature()", s, 
                tn.getSignature());
            assertEquals("forSignature(n).getName()", n, 
                ts.getName());
                
            TestUtil.assertCommutativeEquals(tn, tn, true);
            TestUtil.assertCommutativeEquals(ts, ts, true);
            TestUtil.assertCommutativeEquals(tn, ts, true);
            
            for (int j = 0; j < len; j++) {
                if (i == j) continue;
                UnresolvedType tn1 = UnresolvedType.forName(ns[j]);
                UnresolvedType ts1 = UnresolvedType.forSignature(ss[j]); 
                TestUtil.assertCommutativeEquals(tn, tn1, false);
                TestUtil.assertCommutativeEquals(ts, tn1, false);
                TestUtil.assertCommutativeEquals(tn, ts1, false);
                TestUtil.assertCommutativeEquals(ts, ts1, false);
            }                               
        }
    }
    
    
}
