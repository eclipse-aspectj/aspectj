// "@Around given an extension of ProceedingJoinPoint"

import org.aspectj.lang.annotation.*;
import org.aspectj.lang.ProceedingJoinPoint;


interface B extends ProceedingJoinPoint{
}
aspect A{
  @Around("call(* *.*(..))")
  public Object doNothing(B thisJoinPoint) {
    return thisJoinPoint.proceed();                             
  }
}
