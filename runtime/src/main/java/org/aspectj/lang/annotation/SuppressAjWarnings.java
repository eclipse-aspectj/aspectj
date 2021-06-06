/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
