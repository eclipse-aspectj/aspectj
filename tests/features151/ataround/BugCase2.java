import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;

@Aspect
public class BugCase2 {

  @Pointcut("execution(* setAge(..)) && args(i)")
  void setAge(int i) {}

 @Around("setAge(i)")
 public Object twiceAsOld(ProceedingJoinPoint thisJoinPoint, int i) {
   System.err.println("advice running");
   return thisJoinPoint.proceed(new Object[]{i*2});
 }
  public static void main(String []argv) {
    Foo.main(argv);
  }
}


 class Foo {
  int a;
  public void setAge(int i) {
     System.err.println("Setting age to "+i);
     a=i;
  }

  public static void main(String[]argv) {
    new Foo().setAge(5);
  }
}
