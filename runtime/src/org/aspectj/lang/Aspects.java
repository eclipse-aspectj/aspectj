/*******************************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * initial implementation              Jonas Bonér, Alexandre Vasseur
 *******************************************************************************/
package org.aspectj.lang;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Handles generic aspectOf method when those are not available in the aspects but added later on
 * thru load time weaving.
 * <p/>
 * Aspects.aspectOf(..) is doing reflective calls to the aspect aspectOf, so for better performance
 * consider using preparation of the aspects.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Aspects {

    private final static Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private final static Class[] PEROBJECT_CLASS_ARRAY = new Class[]{Object.class};
    private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final static String ASPECTOF = "aspectOf";

    /**
     * Returns the singleton aspect
     *
     * @param aspectClass
     * @return
     * @throws NoAspectBoundException if no such aspect
     */
    public static Object aspectOf(Class aspectClass) throws NoAspectBoundException {
        try {
            return getSingletonAspectOf(aspectClass).invoke(null, EMPTY_OBJECT_ARRAY);
        } catch (Exception e) {
            throw new NoAspectBoundException(aspectClass.getName(), e);
        }
    }

    /**
     * Returns the perthis / pertarget aspect
     * @param aspectClass
     * @param perObject
     * @return
     * @throws NoAspectBoundException if no such aspect, or no aspect bound
     */
    public static Object aspectOf(Class aspectClass, Object perObject) throws NoAspectBoundException {
        try {
            return getPerObjectAspectOf(aspectClass).invoke(null, new Object[]{perObject});
        } catch (Exception e) {
            throw new NoAspectBoundException(aspectClass.getName(), e);
        }
    }

    public static Object aspectOf(Class aspectClass, Thread perThread) throws NoAspectBoundException {
        //TODO - how to know it s a real per Thread ?
        // if it is actually a singleton one, we will have it as well...
        try {
            return getSingletonAspectOf(aspectClass).invoke(null, EMPTY_OBJECT_ARRAY);
        } catch (Exception e) {
            throw new NoAspectBoundException(aspectClass.getName(), e);
        }
    }

    private static Method getSingletonAspectOf(Class aspectClass) throws NoSuchMethodException {
        Method method = aspectClass.getDeclaredMethod(ASPECTOF, EMPTY_CLASS_ARRAY);
        return checkAspectOf(method, aspectClass);
    }

    private static Method getPerObjectAspectOf(Class aspectClass) throws NoSuchMethodException {
        Method method = aspectClass.getDeclaredMethod(ASPECTOF, PEROBJECT_CLASS_ARRAY);
        return checkAspectOf(method, aspectClass);
    }

    private static Method checkAspectOf(Method method, Class aspectClass) 
        throws NoSuchMethodException {
        method.setAccessible(true);
        if (!method.isAccessible()
            || !Modifier.isPublic(method.getModifiers())
            || !Modifier.isStatic(method.getModifiers())) {            
            new NoSuchMethodException(aspectClass.getName() + ".aspectOf(..) is not accessible public static");
        }
        return method;
    }
}
