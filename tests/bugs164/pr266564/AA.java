package business;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

public class AA {

        public void foo(long docId, String userid) {
        }

        public static void main(String[] args) {
                new AA().foo(12, "hello");
        }
}

@Aspect
class Asp {

        @Around("execution(* foo(..))")
        public Object around(ProceedingJoinPoint pjp) {
                return pjp.proceed();
        }
}

