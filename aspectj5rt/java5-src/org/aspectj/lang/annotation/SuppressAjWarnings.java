/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg       initial implementation 
 *   Andy Clement       fleshed out to match SuppressWarnings
 * ******************************************************************/


package org.aspectj.lang.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate members to avoid AspectJ error messages.
 * Currently supported:
 * <ul>
 * <li>advice that might not run (-Xlint TODO message id)</li>
 * </ul>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SuppressAjWarnings {
  String[] value() default "";
}
