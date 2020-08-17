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
 * @author Alexandre Vasseur
 */
public interface ProceedingJoinPoint extends JoinPoint {

    /**
     * The joinpoint needs to know about its closure so that proceed can delegate to closure.run().
     * This internal method should not be called directly, and won't be visible to the end-user when
     * packed in a jar (synthetic method).
     *
     * @param arc the around closure to associate with this joinpoint
     */
    void set$AroundClosure(AroundClosure arc);

    /**
     * The joinpoint needs to know about its closure so that proceed can delegate to closure.run().
     * This internal method should not be called directly, and won't be visible to the end-user when
     * packed in a jar (synthetic method). This should maintain a stack of closures as multiple around
     * advice with proceed are targeting a joinpoint and the stack will need to be unwound when
     * exiting nested advice. Passing a non null arc indicates a push, passing null indicates a pop.
     *
     * @param arc the around closure to associate with this joinpoint
     */
     default void stack$AroundClosure(AroundClosure arc) {
    	 throw new UnsupportedOperationException();
     }

    /**
     * Proceed with the next advice or target method invocation
     *
     * @return the result of proceeding
     * @throws Throwable if the invoked proceed throws anything
     */
	Object proceed() throws Throwable;

    /**
     * Proceed with the next advice or target method invocation.
     *
     * Unlike code style, proceed(..) in annotation style places different requirements on the
     * parameters passed to it.  The proceed(..) call takes, in this order:
     * <ul>
     * <li> If 'this()' was used in the pointcut for binding, it must be passed first in proceed(..).
     * <li> If 'target()' was used in the pointcut for binding, it must be passed next in proceed(..) -
     * it will be the first argument to proceed(..) if this() was not used for binding.
     * <li> Finally come all the arguments expected at the join point, in the order they are supplied
     * at the join point. Effectively the advice signature is ignored - it doesn't matter
     * if a subset of arguments were bound or the ordering was changed in the advice signature,
     * the proceed(..) calls takes all of them in the right order for the join point.
     * </ul>
     * Since proceed(..) in this case takes an Object array, AspectJ cannot do as much
     * compile time checking as it can for code style. If the rules above aren't obeyed
     * then it will unfortunately manifest as a runtime error.
     *
     * @param args the arguments to proceed with
     * @return the result of proceeding
     * @throws Throwable if the invoked proceed throws anything
     */
	Object proceed(Object[] args) throws Throwable;

}


