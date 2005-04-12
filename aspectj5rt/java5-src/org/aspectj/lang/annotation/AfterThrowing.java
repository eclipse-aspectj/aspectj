/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.lang.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * After throwing advice
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Target(ElementType.METHOD)
public @interface AfterThrowing {

    /**
     * The pointcut expression where to bind the advice
     */
    String value() default "";

    /**
     * The pointcut expression where to bind the advice, overrides "value" when specified
     */
    String pointcut() default "";

    /**
     * The name of the argument in the advice signature to bind the thrown exception to
     */
    String throwing() default "";
}
