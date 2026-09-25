package pkg;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AnnotationStyleModelCoverage {

    @Pointcut("call(* pkg.InPackage.*(..))")
    public void p() { }

    @Before("p()")
    public void before() { }

    @After("p()")
    public void after() { }

    @Around("p()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed();
    }

    static <T> T generics(T t) {
        return t;
    }

    enum Bool { TRUE, FALSE }

    public static void main(String[] args) {
        System.out.println(Bool.TRUE);
    }
}