/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import junit.framework.TestCase;

import org.aspectj.testing.util.TestUtil;

/**
 * This is a test case for all the portions of TypeX that don't require a world.
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
        arrayTest(TypeX.forNames(testNames), componentNames);
        arrayTest(TypeX.forSignatures(testSigs), componentNames);

        sizeTest(TypeX.forNames(testNames), sizes);
        sizeTest(TypeX.forSignatures(testSigs), sizes);
        
        isPrimitiveTest(TypeX.forSignatures(testSigs), isPrimitive);        
    }
    
    public void testNameAndSigWithInners() {
    	TypeX t = TypeX.forName("java.util.Map$Entry");
    	assertEquals(t.getName(), "java.util.Map$Entry");
    	assertEquals(t.getSignature(), "Ljava/util/Map$Entry;");
    	assertEquals(t.getDeclaringType(), TypeX.forName("java.util.Map"));
    	assertNull(TypeX.forName("java.util.Map").getDeclaringType());
    }

    private void isPrimitiveTest(TypeX[] types, boolean[] isPrimitives) {
        for (int i = 0, len = types.length; i < len; i++) {
            TypeX type = types[i];
            boolean b = isPrimitives[i];
            assertEquals(type + " is primitive: ", b, type.isPrimitive());
        }           
    }

    private void sizeTest(TypeX[] types, int[] sizes) {
        for (int i = 0, len = types.length; i < len; i++) {
            TypeX type = types[i];
            int size = sizes[i];
            assertEquals("size of " + type + ": ", size, type.getSize());
        }           
    }

    private void arrayTest(TypeX[] types, String[] components) {
        for (int i = 0, len = types.length; i < len; i++) {
            TypeX type = types[i];
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
            TypeX tn = TypeX.forName(n);
            TypeX ts = TypeX.forSignature(s);

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
                TypeX tn1 = TypeX.forName(ns[j]);
                TypeX ts1 = TypeX.forSignature(ss[j]); 
                TestUtil.assertCommutativeEquals(tn, tn1, false);
                TestUtil.assertCommutativeEquals(ts, tn1, false);
                TestUtil.assertCommutativeEquals(tn, ts1, false);
                TestUtil.assertCommutativeEquals(ts, ts1, false);
            }                               
        }
    }
    
}
