/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.IOException;

import org.aspectj.weaver.ShadowMunger;

public class AfterReturningWeaveTestCase extends WeaveTestCase {
    {
        regenerate = false;
    }

    public AfterReturningWeaveTestCase(String name) {
        super(name);
    }

    public void testAfterReturning() throws IOException {
        weaveTest(
            getStandardTargets(),
            "AfterReturning",
            makeAdviceAll("afterReturning"));
    }

    public void testAfterReturningParam() throws IOException {
        weaveTest(
            getStandardTargets(),
            "AfterReturningParam",
            makeAdviceField("afterReturning", "java.lang.Object"));
    }
    public void testAfterReturningCheckcastParam() throws IOException {
        weaveTest(
            getStandardTargets(),
            "AfterReturningCheckcastParam",
            makeAdviceField("afterReturning", "java.rmi.server.LogStream"));
    }

    public void testAfterReturningConversionParam() throws IOException {
        String mungerString =
            "afterReturning(): call(int *.*(..)) -> "
                + "static void Aspect.ajc_afterReturning_field_get(java.lang.Object)";
        ShadowMunger cm = makeConcreteAdvice(mungerString, 1);

        weaveTest("FancyHelloWorld", "AfterReturningConversionParam", cm);
    }

}
