package example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import junit.framework.TestCase;

import org.aspectj.lang.Aspects;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

public class Bug extends TestCase {

        public void testAdviceMatch() {
                TestImpl impl = new TestImpl();
                impl.method();

                assertEquals(0, Aspects.aspectOf(TestAtAspect.class).count);
//                assertEquals(0, TestAspect.aspectOf().count);
        }


        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @Inherited
        static @interface TestAnnotation {
        }

        @TestAnnotation
        static interface TestInterface {
                void method();
        }

        static class TestImpl implements TestInterface {
//                @Override
                public void method() {
                }
        }

//        static aspect TestAspect {
//                int count = 0;
//
//                before() : @target(example.Bug.TestAnnotation)+ && execution(* *(..)) {
//                        count++;
//                }
//        }

        @Aspect
        static class TestAtAspect {
                int count = 0;

                @Before("@target(example.Bug.TestAnnotation)+ && execution(* *(..))") 
                public void increment() {
                        count++;
                }
        }
}

