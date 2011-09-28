import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

@Aspect 
public class C {
  @Around("execution(* say2(..)) && args(w)") 
  public int m(ProceedingJoinPoint pjp,String w) {
    pjp.proceed(new Object[]{"abc"});
    return 1;
  }
}
