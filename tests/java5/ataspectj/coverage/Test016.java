// "@Pointcut with throws clause"

import org.aspectj.lang.annotation.*;

@Aspect
class A{
  @Pointcut("call(* *.*(..))")
  void someCall() throws Exception {}
}
