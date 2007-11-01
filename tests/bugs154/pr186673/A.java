import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

@interface I {}

@I
public class A {
  public void m() {}
}

@Aspect
class X {
  @Around("execution(* (@I *).*(..))") 
  public Object foo(ProceedingJoinPoint pjp) {
return null;
  }
}


