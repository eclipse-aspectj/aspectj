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
 *******************************************************************************/
package org.aspectj.lang.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Pointcut declaration
 *
 * @author Alexandre Vasseur
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Pointcut {

    /**
     * @return the pointcut expression
     * We allow "" as default for abstract pointcut
     */
    String value() default "";
    
    /**
     * When compiling without debug info, or when interpreting pointcuts at runtime,
     * the names of any arguments used in the pointcut are not available.
     * Under these circumstances only, it is necessary to provide the arg names in 
     * the annotation - these MUST duplicate the names used in the annotated method.
     * Format is a simple comma-separated list.
     * 
     * @return argNames the argument names (should match those in the annotated method)
     */
    String argNames() default "";
}
