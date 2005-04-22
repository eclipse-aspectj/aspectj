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
package org.aspectj.lang;

import org.aspectj.runtime.internal.AroundClosure;

/**
 * ProceedingJoinPoint exposes the proceed(..) method in order to support around advice in @AJ aspects
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public interface ProceedingJoinPoint extends JoinPoint {

    /**
     * The joinpoint needs to know about its closure so that proceed can delegate to closure.run()
     * <p/>
     * This internal method should not be called directly, and won't be visible to the end-user when
     * packed in a jar (synthetic method)
     *
     * @param arc
     */
    void set$AroundClosure(AroundClosure arc);

    /**
     * Proceed with the next advice or target method invocation
     *
     * @return
     * @throws Throwable
     */
    public Object proceed() throws Throwable;

    /**
     * Proceed with the next advice or target method invocation
     * <p/>
     * The given args Object[] must be in the same order and size as the advice signature but
     * without the actual joinpoint instance
     *
     * @param args
     * @return
     * @throws Throwable
     */
    public Object proceed(Object[] args) throws Throwable;

}


