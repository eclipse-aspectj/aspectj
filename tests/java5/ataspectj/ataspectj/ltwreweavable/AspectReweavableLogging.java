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
package ataspectj.ltwreweavable;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Contributed by David Knibb
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Aspect
public class AspectReweavableLogging {

        @Before("execution(void ataspectj.ltwreweavable.MainReweavableLogging.test1()) && this(a)")
        public void before(Advisable a, JoinPoint thisJoinPoint) {
            System.err.println(thisJoinPoint);
            a.addJoinPoint(thisJoinPoint.getSignature().getName());
    }
}
    