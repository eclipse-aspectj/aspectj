/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj.bugs;

import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Aspect
public abstract class AbstractAspectNPEParent {

    @Pointcut
    abstract void method();//NPE at AJC time

    public static void main(String args[]){
    }
}
