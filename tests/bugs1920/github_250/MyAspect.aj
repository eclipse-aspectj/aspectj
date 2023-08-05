import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Random;

/**
 * Reproduces <a href="https://github.com/eclipse-aspectj/aspectj/issues/250">Github bug 250</a>
 */
@Aspect
public class MyAspect {
  @Around("execution(* Application.*(..))")
  public Object myAdvice1(ProceedingJoinPoint joinPoint) {
    System.out.println(joinPoint);
    a(1, "one");
    return joinPoint.proceed();
  }

  @Around("execution(* Application.*(..))")
  public Object myAdvice2(ProceedingJoinPoint joinPoint) throws Throwable {
    System.out.println(joinPoint);
    a(2);
    return new Random().nextBoolean() ? joinPoint.proceed() : null;
  }

  private void a(int i) {
    System.out.println(i);
  }
  private void a(int i, String s) {
    System.out.println(i + " / " + s);
  }

  public static void main(String[] args) {
    new Application().doSomething();
  }
}

class Application {
  public void doSomething() {}
}
