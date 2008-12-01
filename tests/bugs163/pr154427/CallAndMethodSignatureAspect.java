import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;

public aspect CallAndMethodSignatureAspect {

      pointcut callAnyPublicMethodInAuthorization() : call(public * Authorization+.*(..) );

      Object around() : callAnyPublicMethodInAuthorization() {

            MethodSignature methodSignature = (MethodSignature) thisJoinPoint.getSignature();

            // returns NULL when calling a method defined in the top interface "Authorization"
            Method method = methodSignature.getMethod();

            System.out.println(method);
            System.out.println(methodSignature.toLongString());

            return proceed();
      }
}

