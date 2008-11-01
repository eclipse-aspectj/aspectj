
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

public aspect ServiceInterceptorCodeStyle {

    void around(): execution(void Service.method(long)) {
        Object[] args = thisJoinPoint.getArgs();
        long id = (Long) args[0];
        System.out.println("in advice, arg = " + id + " (before proceed)");
        proceed();
        System.out.println("in advice (after proceed)");
    }
}
