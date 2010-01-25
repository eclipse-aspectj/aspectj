/**
 * Copyright (c) 2009 Collaborative Development Group, C.S. Dept., University of Bari
 *
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0  which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package it.uniba.di.cdg.penelope.ui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks views to be injected.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
@Inherited
public @interface View {
}
