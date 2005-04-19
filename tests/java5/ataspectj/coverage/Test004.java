// "@Pointcut declared on codestyle advice"

// Should have got a message about not allowing @Pointcut on advice

import org.aspectj.lang.annotation.*;

aspect A{
  @Pointcut("call(* *.*(..))")
    before(): call(* *(..)) {
  }
}
