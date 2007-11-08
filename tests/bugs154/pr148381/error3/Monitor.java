package test;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

class Foo {}

@Aspect
class Monitor {
  @Around(value="execution(* *(..))",argNames="")
  public void b(Foo aa) {}
  @Pointcut(value="execution(* *(..))",argNames="")
  public void a(Foo aa) {}
  @Before(value="execution(* *(..))",argNames="")
  public void c(Foo aa) {}
  @After(value="execution(* *(..))",argNames="a,b,c")
  public void d(Foo aa) {}
  @AfterThrowing(value="execution(* *(..))",argNames="")
  public void e(Foo aa) {}
  @AfterReturning(value="execution(* *(..))",argNames="")
  public void f(Foo aa) {}
}

