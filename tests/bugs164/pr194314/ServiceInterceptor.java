
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ServiceInterceptor {

    @Around("execution(void Service.method(long))")
    public void method(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        long id = (Long) args[0];
        System.out.println("in advice, arg = " + id + " (before proceed)");
        pjp.proceed(pjp.getArgs());
        System.out.println("in advice (after proceed)");
    }
}
