import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

public class Code {
  public static void main(String []argv) {
try {
    foo();
} catch (Throwable t) {
   System.out.println("Caught "+t);
}
  }

  public static void foo() {
    print1("abc");
    print2("def");
    print1("ghi");
  }

  public static void print1(String s) {
    System.out.println(s);
  }  

  public static void print2(String s) {
    System.out.println(s);
  }  
}

@Aspect
class Azpect {
  @Around("call(* print2(..))")
  public Object one(ProceedingJoinPoint pjp) {
    return pjp.proceed();
  }
  @Around("call(* print2(..))")
  public Object two(ProceedingJoinPoint pjp) {
    //return pjp.proceed();
    throw new IllegalStateException("");
  }
}
