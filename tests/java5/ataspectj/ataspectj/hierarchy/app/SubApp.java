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
package ataspectj.hierarchy.app;

import ataspectj.hierarchy.AppContainerTest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Leaves in child classloader in two forms, like 2 webapp
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class SubApp implements AppContainerTest.IApp {

    // simple echo. May be advised or not depending on the aspect deployed there
    public String invoke(String input) {
        return input;
    }

    // this child aspect will be LTW for only one variation of the SubApp
    @Aspect
    public static class SubAspect {

        @Around("execution(* ataspectj.hierarchy.app.SubApp.invoke(..))")
        public Object around(ProceedingJoinPoint jp) throws Throwable {
            String out = (String) jp.proceed();
            return "localAspect[" + out + "]";
        }
    }

}
