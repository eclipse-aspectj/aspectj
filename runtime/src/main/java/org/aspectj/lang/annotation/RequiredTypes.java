/* *******************************************************************
 * Copyright (c) 2014 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be specified on an aspect to ensure that particular types must be accessible before
 * the aspect will be 'activated'. The array value should be a list of fully qualified
 * type names as strings, for example "com.foo.Bar". Useful in an aspect library that
 * includes a number of aspects, only a few of which should ever be active depending
 * upon what is on the classpath.
 * 
 * @author Andy Clement
 * @since 1.8.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiredTypes {
  String[] value();
}
