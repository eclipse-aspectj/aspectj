// "@Pointcut with wrong number of args"

import org.aspectj.lang.annotation.*;

@Aspect
class A{
  @Pointcut("call(* *.*(..))")
  void someCall(int x) {}
}
