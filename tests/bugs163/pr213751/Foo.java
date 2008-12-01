import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

public abstract aspect Foo {
	    public abstract pointcut targetPointcut();

	        @Around("targetPointcut()")
			    public Object someAdvice(ProceedingJoinPoint thisJoinPoint)
			        {
					        return thisJoinPoint.proceed();
						    }
}


