// "@Pointcut not returning void"


import org.aspectj.lang.annotation.*;


@Aspect
class A{
  @Pointcut("call(* *.*(..))")
  int someCall(){
	  return 42;
  }
}
