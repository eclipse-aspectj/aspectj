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
package ataspectj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.annotation.DeclareError;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DeowTest {

    public void hello() {}

    public void hi() {}

    public void target() {
        hello();
        hi();
    }

    @Aspect
    public static class DeowAspect {

        @DeclareWarning("call(* hello()) && within(ataspectj.DeowTest)")
        final static String onHello = "call hello";

        @DeclareError("call(* hi()) && within(ataspectj.DeowTest)")
        final static String onHi = "call hi";
    }
}
