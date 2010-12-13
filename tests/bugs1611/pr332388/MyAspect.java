import java.lang.annotation.*;
import org.aspectj.lang.annotation.*;

@Aspect
public class MyAspect {
  @Before("call(* *.*(..))")
  public void myAdvice() {
    System.out.println();
  }
}
