// "@Pointcut on @Aspect class constructor"

import org.aspectj.lang.annotation.*;

@Aspect
class A{
  @Pointcut("call(* *.*(..))")
  A(){
  }
}
