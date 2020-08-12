/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.lang.reflect;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.aspectj.internal.lang.reflect.AjTypeImpl;

/**
 * This is the anchor for the AspectJ runtime type system. 
 * Typical usage to get the AjType representation of a given type
 * at runtime is to call <code>AjType&lt;Foo&gt; fooType = AjTypeSystem.getAjType(Foo.class);</code>
 */
public class AjTypeSystem {
	
		private static Map<Class, WeakReference<AjType>> ajTypes = 
			Collections.synchronizedMap(new WeakHashMap<>());

		/**
		 * Return the AspectJ runtime type representation of the given Java type.
		 * Unlike java.lang.Class, AjType understands pointcuts, advice, declare statements,
		 * and other AspectJ type members. AjType is the recommended reflection API for
		 * AspectJ programs as it offers everything that java.lang.reflect does, with 
		 * AspectJ-awareness on top.
		 * @param <T> the expected type associated with the returned AjType
		 * @param fromClass the class for which to discover the AjType
		 * @return the AjType corresponding to the input class
		 */
		public static <T> AjType<T> getAjType(Class<T> fromClass) {
			WeakReference<AjType> weakRefToAjType =  ajTypes.get(fromClass);
			if (weakRefToAjType!=null) {
				AjType<T> theAjType = weakRefToAjType.get();
				if (theAjType != null) {
					return theAjType;
				} else {
					theAjType = new AjTypeImpl<>(fromClass);
					ajTypes.put(fromClass, new WeakReference<>(theAjType));
					return theAjType;
				}
			}
			// neither key nor value was found
			AjType<T> theAjType = new AjTypeImpl<>(fromClass);
			ajTypes.put(fromClass, new WeakReference<>(theAjType));
			return theAjType;
		}
}
