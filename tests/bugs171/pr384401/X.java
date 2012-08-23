import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class X {
//	@AfterThrowing(pointcut="execution(* *(..))",throwing = "e")
	@AfterThrowing(throwing = "e")
    public void bizLoggerWithException(JoinPoint thisJoinPoint,Throwable e) {
  //  .....// do some stuff
    }

}

class BizLoggable {}
