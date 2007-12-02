import java.lang.annotation.*;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

@Aspect class A {

  @Pointcut("execution(@Tracing * *(..)) && @annotation(tracing)")
        void annotatedMethods(Tracing tracing) { }
       
        @AfterThrowing(pointcut = "annotatedMethods(tracing)", throwing = "t")
        public void logException(JoinPoint thisJoinPoint, Throwable t,Tracing tracing) {
        }

}

@Retention(RetentionPolicy.RUNTIME)
@interface Tracing { }

public class Test2 {

  @Tracing
  public void m() {}

  public static void main(String []argv) {}

}

