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

    private final UnresolvedType[] primitiveTypeXs =
            UnresolvedType.forSignatures(
                new String[] {"B", "S", "C", "I", "J", "F", "D", "V"});

    public void testPrimitiveTypes() {
    	ResolvedType[] primitives = getWorld().resolve(primitiveTypeXs);
        for(int i = 0, len = primitives.length; i < len; i++) {
            ResolvedType ty = primitives[i];
            modifiersTest(ty, Modifier.PUBLIC | Modifier.FINAL);
            fieldsTest(ty, ResolvedMember.NONE);
            methodsTest(ty, ResolvedMember.NONE);
            interfacesTest(ty, ResolvedType.NONE);
            superclassTest(ty, null);
            pointcutsTest(ty, ResolvedMember.NONE);
            isInterfaceTest(ty, false);
            isClassTest(ty, false);
            isAspectTest(ty, false);
            for (int j = 0; j < len; j++) {
                ResolvedType ty1 = primitives[j];
                if (ty.equals(ty1)) {
                    isCoerceableFromTest(ty, ty1, true);
                } else if (ty == ResolvedType.BOOLEAN || ty1 == ResolvedType.BOOLEAN ||
                            ty == ResolvedType.VOID || ty1 == ResolvedType.VOID) {
                    isCoerceableFromTest(ty, ty1, false);
                } else {
                    isCoerceableFromTest(ty, ty1, true);
                }
            }
            isCoerceableFromTest(ty, UnresolvedType.OBJECT, false);
                
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
    	ResolvedType[] primitives = getWorld().resolve(primitiveTypeXs);
    	UnresolvedType tx = UnresolvedType.forSignature(sig);
        ResolvedType ty = getWorld().resolve(tx,true);
        assertTrue("Couldnt find type "+tx,!ty.isMissing());
        ResolvedType[] lowerTyArray = 
            getWorld().resolve(UnresolvedType.forSignatures(lowers));
        List lowerTys = new ArrayList(Arrays.asList(lowerTyArray));
        lowerTys.add(ty);
        Set allLowerTys = new HashSet(lowerTys);
        Set allUpperTys = new HashSet(Arrays.asList(primitives));
        allUpperTys.removeAll(allLowerTys);
        
        for (Iterator i = allLowerTys.iterator(); i.hasNext(); ) {
            ResolvedType other = (ResolvedType) i.next();
            isAssignableFromTest(ty, other, true);
        }
        for (Iterator i = allUpperTys.iterator(); i.hasNext(); ) {
            ResolvedType other = (ResolvedType) i.next();
            isAssignableFromTest(ty, other, false);
        }
    }  

    public void testPrimitiveArrays() {
    	ResolvedType[] primitives = getWorld().resolve(primitiveTypeXs);
        for(int i = 0, len = primitives.length; i < len; i++) {
            ResolvedType ty = primitives[i];
            UnresolvedType tx = UnresolvedType.forSignature("["+ty.getSignature());
            ResolvedType aty = getWorld().resolve(tx,true);
            assertTrue("Couldnt find type "+tx,!aty.isMissing());
            modifiersTest(aty, Modifier.PUBLIC | Modifier.FINAL);
            fieldsTest(aty, ResolvedMember.NONE);
            methodsTest(aty, ResolvedMember.NONE);
            interfacesTest(aty, new ResolvedType[] {
                                    getWorld().getCoreType(UnresolvedType.CLONEABLE),
                                    getWorld().getCoreType(UnresolvedType.SERIALIZABLE) });
            superclassTest(aty, UnresolvedType.OBJECT);

            pointcutsTest(aty, ResolvedMember.NONE);
            isInterfaceTest(aty, false);
            isClassTest(aty, false);
            isAspectTest(aty, false);
            for (int j = 0; j < len; j++) {
                ResolvedType ty1 = primitives[j];
                isCoerceableFromTest(aty, ty1, false);
                tx = UnresolvedType.forSignature("[" + ty1.getSignature());
                ResolvedType aty1 = getWorld().resolve(tx,true);
                assertTrue("Couldnt find type "+tx,!aty1.isMissing());
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

    // ---- tests for parts of ResolvedType objects
    
    protected void modifiersTest(ResolvedType ty, int mods) {
        assertEquals(ty + " modifiers:", Modifier.toString(mods), Modifier.toString(ty.getModifiers()));
    }
    protected void fieldsTest(ResolvedType ty, Member[] x) {
        TestUtil.assertSetEquals(ty + " fields:", x, ty.getDeclaredJavaFields());
    }
    protected void methodsTest(ResolvedType ty, Member[] x) {
        TestUtil.assertSetEquals(ty + " methods:", x, ty.getDeclaredJavaMethods());
    }
	protected void mungersTest(ResolvedType ty, ShadowMunger[] x) {
		TestUtil.assertSetEquals(ty + " mungers:", x, ty.getDeclaredShadowMungersArray());
	}
    protected void interfacesTest(ResolvedType ty, ResolvedType[] x) {
        TestUtil.assertArrayEquals(ty + " interfaces:", x, ty.getDeclaredInterfaces());
    }
    protected void superclassTest(ResolvedType ty, UnresolvedType x) {
        assertEquals(ty + " superclass:", x, ty.getSuperclass());
    }
    protected void pointcutsTest(ResolvedType ty, Member[] x) {
        TestUtil.assertSetEquals(ty + " pointcuts:", x, ty.getDeclaredPointcuts());
    }
    protected void isInterfaceTest(ResolvedType ty, boolean x) {
        assertEquals(ty + " is interface:", x, ty.isInterface());
    }
    protected void isAspectTest(ResolvedType ty, boolean x) {
        assertEquals(ty + " is aspect:", x, ty.isAspect());
    }
    protected void isClassTest(ResolvedType ty, boolean x) {
        assertEquals(ty + " is class:", x, ty.isClass());
    }
    protected void isCoerceableFromTest(UnresolvedType ty0, UnresolvedType ty1, boolean x) {
        assertEquals(ty0 + " is coerceable from " + ty1, x, ty0.resolve(getWorld()).isCoerceableFrom(ty1.resolve(getWorld())));
        assertEquals(ty1 + " is coerceable from " + ty0, x, ty1.resolve(getWorld()).isCoerceableFrom(ty0.resolve(getWorld())));
    }
    protected void isAssignableFromTest(UnresolvedType ty0, UnresolvedType ty1, boolean x) {
        assertEquals(ty0 + " is assignable from " + ty1, x, ty0.resolve(getWorld()).isAssignableFrom(ty1.resolve(getWorld())));
    }

    // ---- tests for parts of ResolvedMethod objects
    
    protected void modifiersTest(ResolvedMember m, int mods) {
        assertEquals(m + " modifiers:", Modifier.toString(mods), Modifier.toString(m.getModifiers()));
    }
    protected void exceptionsTest(ResolvedMember m, UnresolvedType[] exns) {
        TestUtil.assertSetEquals(m + " exceptions:", exns, m.getExceptions());
    }
      
}
