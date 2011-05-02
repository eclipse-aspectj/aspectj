package foo.bar;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class BarAspect {
    @SuppressWarnings("unused")
    @Pointcut("execution(* foo.bar.Foo.bar())")
    private void pointcut() {
    }

    @Around("pointcut()")
    public Object applyAdvice(ProceedingJoinPoint pjp) throws Throwable {
        System.out.print("pre...");
        Object retVal = pjp.proceed();
        System.out.println("...post");
        return retVal;
    }

}
