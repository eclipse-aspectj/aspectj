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

import junit.framework.TestCase;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Around;
import org.aspectj.runtime.internal.AroundClosure;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class MultipleBindingTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(MultipleBindingTest.class);
    }

    public void testMultipleJoinPoint() {
        s_log = new StringBuffer();
        Target.dump(new JoinPoint() {
            public String toShortString() {
                return "jpFromApp";
            }

            public String toLongString() {
                return null;
            }

            public Object getThis() {
                return null;
            }

            public Object getTarget() {
                return null;
            }

            public Object[] getArgs() {
                return new Object[0];
            }

            public Signature getSignature() {
                return null;
            }

            public SourceLocation getSourceLocation() {
                return null;
            }

            public String getKind() {
                return null;
            }

            public StaticPart getStaticPart() {
                return null;
            }
        });
        assertEquals("jpFromApp execution(MultipleBindingTest.Target.dump(..)) execution(MultipleBindingTest.Target.dump(..)) jpFromApp ", s_log.toString());
    }

    public void testMultipleProceedingJoinPoint() {
        s_log = new StringBuffer();
        Target.dump2(new ProceedingJoinPoint() {
            public void set$AroundClosure(AroundClosure arc) {

            }

            public Object proceed() throws Throwable {
                return null;
            }

            public Object proceed(Object[] args) throws Throwable {
                return null;
            }

            public String toShortString() {
                return "pjpFromApp";
            }

            public String toLongString() {
                return null;
            }

            public Object getThis() {
                return null;
            }

            public Object getTarget() {
                return null;
            }

            public Object[] getArgs() {
                return new Object[0];
            }

            public Signature getSignature() {
                return null;
            }

            public SourceLocation getSourceLocation() {
                return null;
            }

            public String getKind() {
                return null;
            }

            public StaticPart getStaticPart() {
                return null;
            }

        });
        assertEquals("pjpFromApp execution(MultipleBindingTest.Target.dump2(..)) execution(MultipleBindingTest.Target.dump2(..)) pjpFromApp ", s_log.toString());
    }

    static class Target {
        static void dump(JoinPoint jp) {
            log(jp.toShortString());
        }
        static void dump2(ProceedingJoinPoint pjp) {
            log(pjp.toShortString());
        }
    }

    @Aspect
    public static class TestAspect {

        @Before("execution(* ataspectj.MultipleBindingTest.Target.dump(..)) && args(ajp)")
        public void before(JoinPoint ajp, JoinPoint jp, JoinPoint jpbis) {
            log(ajp.toShortString());
            log(jp.toShortString());
            log(jpbis.toShortString());
        }

        @Around("execution(* ataspectj.MultipleBindingTest.Target.dump2(..)) && args(apjp)")
        public Object around(ProceedingJoinPoint apjp, ProceedingJoinPoint pjp, ProceedingJoinPoint pjpbis) throws Throwable {
            log(apjp.toShortString());
            log(pjp.toShortString());
            log(pjpbis.toShortString());
            return pjp.proceed();
        }
    }

}
