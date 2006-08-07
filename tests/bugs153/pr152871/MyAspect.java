package a;

import org.aspectj.lang.annotation.*;

@Aspect
public class MyAspect {
  //before(): call(* print(..)) {
  @Before("call(* print(..))")
  public void m() {
    System.out.println("advice running");
  }
}
