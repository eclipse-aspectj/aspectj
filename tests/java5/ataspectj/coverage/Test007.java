// "@Before declared on advice"

// should be an error, check attr is on an ajc$ method and barf

import org.aspectj.lang.annotation.*;

aspect A{
  @Before("call(* org..*(..))")
    before(): call(* *(..)) {
  }
}
