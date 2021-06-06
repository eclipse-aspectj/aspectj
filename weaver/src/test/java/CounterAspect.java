/* *******************************************************************
 * Copyright (c) 2008 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Andy Clement         initial implementation
 * ******************************************************************/
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created to enable PointcutDesignatorHandlerTests.testParsingBeanInReferencePointcut01 and 02 to run
 *
 * @author Andy Clement
 */
@Aspect
public class CounterAspect {

    int count;

    @Before("execution(* set*(..)) && bean(testBean1)")
    public void increment1ForAnonymousPointcut() {
        count++;
    }

    @Pointcut("execution(* toString(..)) && bean(testBean1)")
    public void testBean1toString() {
    }

    @Pointcut("execution(* setAge(..)) && bean(testBean1)")
    public void testBean1SetAge() {
    }

    @Pointcut("execution(* setAge(..)) && bean(testBean2)")
    public void testBean2SetAge() {
    }

    @Before("testBean1SetAge()")
    public void increment1() {
        count++;
    }

    @Before("testBean2SetAge()")
    public void increment2() {
        count++;
    }
}
