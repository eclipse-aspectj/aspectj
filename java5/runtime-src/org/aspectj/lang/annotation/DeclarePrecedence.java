/*******************************************************************************
 * Copyright (c) 2005 Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.lang.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Aspect precedence declaration
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Target(ElementType.TYPE)//TODO should be inner @ of Aspect
public @interface DeclarePrecedence {

    /**
     * The precedence list
     */
    Class[] value();//TODO change to support type pattern.

    public static final class ANY {};//TODO remove

}
