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

import java.lang.reflect.Modifier;
import java.util.*;

import junit.framework.TestCase;

import org.aspectj.testing.util.TestUtil;

/** This isn't a test case, it's extended by test cases, and includes tests that
 * should be true in all worlds.
 */

public abstract class AbstractWorldTestCase extends TestCase {

    public AbstractWorldTestCase(String name) {
        super(name);
    }
	
	protected abstract World getWorld();

    private final TypeX[] primitiveTypeXs =
            TypeX.forSignatures(
                new String[] {"B", "S", "C", "I", "J", "F", "D", "V"});

    public void testPrimitiveTypes() {
    	ResolvedTypeX[] primitives = getWorld().resolve(primitiveTypeXs);
        for(int i = 0, len = primitives.length; i < len; i++) {
            ResolvedTypeX ty = primitives[i];
            modifiersTest(ty, Modifier.PUBLIC | Modifier.FINAL);
            fieldsTest(ty, ResolvedMember.NONE);
            methodsTest(ty, ResolvedMember.NONE);
            interfacesTest(ty, ResolvedTypeX.NONE);
            superclassTest(ty, null);
            pointcutsTest(ty, ResolvedMember.NONE);
            isInterfaceTest(ty, false);
            isClassTest(ty, false);
            isAspectTest(ty, false);
            for (int j = 0; j < len; j++) {
                ResolvedTypeX ty1 = primitives[j];
                if (ty.equals(ty1)) {
                    isCoerceableFromTest(ty, ty1, true);
                } else if (ty == ResolvedTypeX.BOOLEAN || ty1 == ResolvedTypeX.BOOLEAN ||
                            ty == ResolvedTypeX.VOID || ty1 == ResolvedTypeX.VOID) {
                    isCoerceableFromTest(ty, ty1, false);
                } else {
                    isCoerceableFromTest(ty, ty1, true);
                }
            }
            isCoerceableFromTest(ty, TypeX.OBJECT, false);
                
            primAssignTest("B", new String[] {});
            primAssignTest("S", new String[] {"B"});
            primAssignTest("C", new String[] {"B"});
            primAssignTest("I", new String[] {"B", "S", "C"});
            primAssignTest("J", new String[] {"B", "S", "C", "I"});
            primAssignTest("F", new String[] {"B", "S", "C", "I", "J"});
            primAssignTest("D", new String[] {"B", "S", "C", "I", "J", "F"});
            primAssignTest("Z", new String[] {});
            primAssignTest("V", new String[] {});
        
        }
    }
    private void primAssignTest(String sig, String[] lowers) {
    	ResolvedTypeX[] primitives = getWorld().resolve(primitiveTypeXs);
    	TypeX tx = TypeX.forSignature(sig);
        ResolvedTypeX ty = getWorld().resolve(tx,true);
        assertTrue("Couldnt find type "+tx,ty!=ResolvedTypeX.MISSING);
        ResolvedTypeX[] lowerTyArray = 
            getWorld().resolve(TypeX.forSignatures(lowers));
        List lowerTys = new ArrayList(Arrays.asList(lowerTyArray));
        lowerTys.add(ty);
        Set allLowerTys = new HashSet(lowerTys);
        Set allUpperTys = new HashSet(Arrays.asList(primitives));
        allUpperTys.removeAll(allLowerTys);
        
        for (Iterator i = allLowerTys.iterator(); i.hasNext(); ) {
            ResolvedTypeX other = (ResolvedTypeX) i.next();
            isAssignableFromTest(ty, other, true);
        }
        for (Iterator i = allUpperTys.iterator(); i.hasNext(); ) {
            ResolvedTypeX other = (ResolvedTypeX) i.next();
            isAssignableFromTest(ty, other, false);
        }
    }  

    public void testPrimitiveArrays() {
    	ResolvedTypeX[] primitives = getWorld().resolve(primitiveTypeXs);
        for(int i = 0, len = primitives.length; i < len; i++) {
            ResolvedTypeX ty = primitives[i];
            TypeX tx = TypeX.forSignature("["+ty.getSignature());
            ResolvedTypeX aty = getWorld().resolve(tx,true);
            assertTrue("Couldnt find type "+tx,aty!=ResolvedTypeX.MISSING);
            modifiersTest(aty, Modifier.PUBLIC | Modifier.FINAL);
            fieldsTest(aty, ResolvedMember.NONE);
            methodsTest(aty, ResolvedMember.NONE);
            interfacesTest(aty, new ResolvedTypeX[] {
                                    getWorld().getCoreType(TypeX.CLONEABLE),
                                    getWorld().getCoreType(TypeX.SERIALIZABLE) });
            superclassTest(aty, TypeX.OBJECT);

            pointcutsTest(aty, ResolvedMember.NONE);
            isInterfaceTest(aty, false);
            isClassTest(aty, false);
            isAspectTest(aty, false);
            for (int j = 0; j < len; j++) {
                ResolvedTypeX ty1 = primitives[j];
                isCoerceableFromTest(aty, ty1, false);
                tx = TypeX.forSignature("[" + ty1.getSignature());
                ResolvedTypeX aty1 = getWorld().resolve(tx,true);
                assertTrue("Couldnt find type "+tx,aty1!=ResolvedTypeX.MISSING);
                if (ty.equals(ty1)) {
                    isCoerceableFromTest(aty, aty1, true);
                    isAssignableFromTest(aty, aty1, true);
                } else {
                    isCoerceableFromTest(aty, aty1, false);
                    isAssignableFromTest(aty, aty1, false);
                }             
            }
        }
    } 

    // ---- tests for parts of ResolvedTypeX objects
    
    protected void modifiersTest(ResolvedTypeX ty, int mods) {
        assertEquals(ty + " modifiers:", Modifier.toString(mods), Modifier.toString(ty.getModifiers()));
    }
    protected void fieldsTest(ResolvedTypeX ty, Member[] x) {
        TestUtil.assertSetEquals(ty + " fields:", x, ty.getDeclaredJavaFields());
    }
    protected void methodsTest(ResolvedTypeX ty, Member[] x) {
        TestUtil.assertSetEquals(ty + " methods:", x, ty.getDeclaredJavaMethods());
    }
	protected void mungersTest(ResolvedTypeX ty, ShadowMunger[] x) {
		TestUtil.assertSetEquals(ty + " mungers:", x, ty.getDeclaredShadowMungersArray());
	}
    protected void interfacesTest(ResolvedTypeX ty, ResolvedTypeX[] x) {
        TestUtil.assertArrayEquals(ty + " interfaces:", x, ty.getDeclaredInterfaces());
    }
    protected void superclassTest(ResolvedTypeX ty, TypeX x) {
        assertEquals(ty + " superclass:", x, ty.getSuperclass());
    }
    protected void pointcutsTest(ResolvedTypeX ty, Member[] x) {
        TestUtil.assertSetEquals(ty + " pointcuts:", x, ty.getDeclaredPointcuts());
    }
    protected void isInterfaceTest(ResolvedTypeX ty, boolean x) {
        assertEquals(ty + " is interface:", x, ty.isInterface());
    }
    protected void isAspectTest(ResolvedTypeX ty, boolean x) {
        assertEquals(ty + " is aspect:", x, ty.isAspect());
    }
    protected void isClassTest(ResolvedTypeX ty, boolean x) {
        assertEquals(ty + " is class:", x, ty.isClass());
    }
    protected void isCoerceableFromTest(TypeX ty0, TypeX ty1, boolean x) {
        assertEquals(ty0 + " is coerceable from " + ty1, x, ty0.isCoerceableFrom(ty1, getWorld()));
        assertEquals(ty1 + " is coerceable from " + ty0, x, ty1.isCoerceableFrom(ty0, getWorld()));
    }
    protected void isAssignableFromTest(TypeX ty0, TypeX ty1, boolean x) {
        assertEquals(ty0 + " is assignable from " + ty1, x, ty0.isAssignableFrom(ty1, getWorld()));
    }

    // ---- tests for parts of ResolvedMethod objects
    
    protected void modifiersTest(ResolvedMember m, int mods) {
        assertEquals(m + " modifiers:", Modifier.toString(mods), Modifier.toString(m.getModifiers()));
    }
    protected void exceptionsTest(ResolvedMember m, TypeX[] exns) {
        TestUtil.assertSetEquals(m + " exceptions:", exns, m.getExceptions());
    }
      
}
