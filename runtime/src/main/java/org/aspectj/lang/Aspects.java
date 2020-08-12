/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Alexandre Vasseur
 * generic signature update            Adrian Colyer
 *******************************************************************************/
package org.aspectj.lang;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

/**
 * Handles generic aspectOf method when those are not available in the aspects but added later on
 * thru load time weaving.
 * Aspects.aspectOf(..) is doing reflective calls to the aspect aspectOf, so for better performance
 * consider using ajc compilation of the aspects and using them as a binary dependancies in your project.
 *
 * @author Alexandre Vasseur
 */
public class Aspects {

    private final static Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private final static Class[] PEROBJECT_CLASS_ARRAY = new Class[]{Object.class};
    private final static Class[] PERTYPEWITHIN_CLASS_ARRAY = new Class[]{Class.class};
    private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final static String ASPECTOF = "aspectOf";
    private final static String HASASPECT = "hasAspect";

    /**
     * @param <T> the expected class of the aspect
     * @param aspectClass the aspect class 
     * @return the singleton aspect or the percflow / percflowbelow associated with the current thread
     * @throws NoAspectBoundException if no such aspect
     */
    public static <T> T aspectOf(Class<T> aspectClass) throws NoAspectBoundException {
        try {
            return (T) getSingletonOrThreadAspectOf(aspectClass).invoke(null, EMPTY_OBJECT_ARRAY);
        } catch (InvocationTargetException e) {
        	//FIXME asc Highly temporary change to see what the build makes of it - dont use 1.4 APIs
            throw new NoAspectBoundException(aspectClass.getName(), e);//e.getCause());
        } catch (Exception e) {
            throw new NoAspectBoundException(aspectClass.getName(), e);
        }
    }

    /**
     * @param <T> the expected class of the aspect
     * @param aspectClass the aspect class 
     * @param perObject the this/target object for which to look for an aspect instance
     * @return the associated perthis / pertarget aspect instance
     * @throws NoAspectBoundException if no such aspect, or no aspect bound
     */
    public static <T> T aspectOf(Class<T> aspectClass, Object perObject) throws NoAspectBoundException {
        try {
            return (T) getPerObjectAspectOf(aspectClass).invoke(null, new Object[]{perObject});
        } catch (InvocationTargetException e) {
        	//FIXME asc Highly temporary change to see what the build makes of it - dont use 1.4 APIs
            throw new NoAspectBoundException(aspectClass.getName(), e);//e.getCause());
        } catch (Exception e) {
            throw new NoAspectBoundException(aspectClass.getName(), e);
        }
    }

    /**
     * @param <T> the expected class of the aspect
     * @param aspectClass the aspect class 
     * @param perTypeWithin the class for which to search for an aspect instance
     * @return the associated aspect instance
     * @throws NoAspectBoundException if no such aspect, or no aspect bound
     */
    public static <T> T aspectOf(Class<T> aspectClass, Class<?> perTypeWithin) throws NoAspectBoundException {
        try {
            return (T) getPerTypeWithinAspectOf(aspectClass).invoke(null, new Object[]{perTypeWithin});
        } catch (InvocationTargetException e) {
//        	FIXME asc Highly temporary change to see what the build makes of it - dont use 1.4 APIs
            throw new NoAspectBoundException(aspectClass.getName(), e);//e.getCause());
        } catch (Exception e) {
            throw new NoAspectBoundException(aspectClass.getName(), e);
        }
    }

    /**
     * @param aspectClass the aspect class 
     * @return true if singleton aspect or percflow / percflowbelow aspect is bound
     * @throws NoAspectBoundException if not bound
     */
    public static boolean hasAspect(Class<?> aspectClass) throws NoAspectBoundException {
        try {
            return (Boolean) getSingletonOrThreadHasAspect(aspectClass).invoke(null, EMPTY_OBJECT_ARRAY);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param aspectClass the aspect class 
     * @param perObject the this/target object for which to look for an aspect instance
     * @return true if the perthis / pertarget aspect is bound
     * @throws NoAspectBoundException if not bound
     */
    public static boolean hasAspect(Class<?> aspectClass, Object perObject) throws NoAspectBoundException {
        try {
            return (Boolean) getPerObjectHasAspect(aspectClass).invoke(null, new Object[]{perObject});
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param aspectClass the aspect class 
     * @param perTypeWithin class
     * @return true if the pertypewithin aspect is bound
     * @throws NoAspectBoundException if not bound
     */
    public static boolean hasAspect(Class<?> aspectClass, Class<?> perTypeWithin) throws NoAspectBoundException {
        try {
            return (Boolean) getPerTypeWithinHasAspect(aspectClass).invoke(null, new Object[]{perTypeWithin});
        } catch (Exception e) {
            return false;
        }
    }

    // -- aspectOf

    private static Method getSingletonOrThreadAspectOf(Class<?> aspectClass) throws NoSuchMethodException {
        Method method = aspectClass.getDeclaredMethod(ASPECTOF, EMPTY_CLASS_ARRAY);
        return checkAspectOf(method, aspectClass);
    }

    private static Method getPerObjectAspectOf(Class<?> aspectClass) throws NoSuchMethodException {
        Method method = aspectClass.getDeclaredMethod(ASPECTOF, PEROBJECT_CLASS_ARRAY);
        return checkAspectOf(method, aspectClass);
    }

    private static Method getPerTypeWithinAspectOf(Class<?> aspectClass) throws NoSuchMethodException {
        Method method = aspectClass.getDeclaredMethod(ASPECTOF, PERTYPEWITHIN_CLASS_ARRAY);
        return checkAspectOf(method, aspectClass);
    }

    private static Method checkAspectOf(Method method, Class<?> aspectClass) throws NoSuchMethodException {
        method.setAccessible(true);
        if (!method.isAccessible()
            || !Modifier.isPublic(method.getModifiers())
            || !Modifier.isStatic(method.getModifiers())) {            
            throw new NoSuchMethodException(aspectClass.getName() + ".aspectOf(..) is not accessible public static");
        }
        return method;
    }

    // -- hasAspect

    private static Method getSingletonOrThreadHasAspect(Class aspectClass) throws NoSuchMethodException {
        Method method = aspectClass.getDeclaredMethod(HASASPECT, EMPTY_CLASS_ARRAY);
        return checkHasAspect(method, aspectClass);
    }

    private static Method getPerObjectHasAspect(Class aspectClass) throws NoSuchMethodException {
        Method method = aspectClass.getDeclaredMethod(HASASPECT, PEROBJECT_CLASS_ARRAY);
        return checkHasAspect(method, aspectClass);
    }

    private static Method getPerTypeWithinHasAspect(Class aspectClass) throws NoSuchMethodException {
        Method method = aspectClass.getDeclaredMethod(HASASPECT, PERTYPEWITHIN_CLASS_ARRAY);
        return checkHasAspect(method, aspectClass);
    }

    private static Method checkHasAspect(Method method, Class aspectClass) throws NoSuchMethodException {
        method.setAccessible(true);
        if (!method.isAccessible()
            || !Modifier.isPublic(method.getModifiers())
            || !Modifier.isStatic(method.getModifiers())) {
            throw new NoSuchMethodException(aspectClass.getName() + ".hasAspect(..) is not accessible public static");
        }
        return method;
    }
}
