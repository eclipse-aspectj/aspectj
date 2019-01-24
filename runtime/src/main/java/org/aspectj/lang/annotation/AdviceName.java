/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Adrian Colyer									initial implementation
 *******************************************************************************/
package org.aspectj.lang.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Used to annotated code-style advice to name it
 * Name is used by reflection api if present, may in future be used in adviceexecution() pcd.
 * It is an error to use the @AdviceName annotation on an annotation-style advice declaration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AdviceName {

    /**
     * The name of the advice
     */
    String value();
}
