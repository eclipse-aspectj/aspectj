import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class TheAspect {
    @Around("execution(* *(@SomeAnnotation (*), ..))")
    public Object aroundContext(ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed();
    }
}
