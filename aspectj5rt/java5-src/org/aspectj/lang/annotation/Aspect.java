/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *   Jonas Bonér, Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.lang.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Aspect declaration
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Target(ElementType.TYPE)
public @interface Aspect {

    /**
     * Per clause expression, defaults to singleton aspect
     * <p/>
     * Valid values are "" (singleton), "perthis(...)", etc
     */
    public String value() default "";
}
