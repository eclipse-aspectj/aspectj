// "@Pointcut not returning void"
package ataspectj.misuse;


import org.aspectj.lang.annotation.*;


@Aspect
class Test008{
  @Pointcut("call(* *.*(..))")
  int someCall(){
	  return 42;
  }
}
