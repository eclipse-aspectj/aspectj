// "class with @Before extending @Aspect class"

// shouldn't allow advice in a non-aspect type

import org.aspectj.lang.annotation.*;

@Aspect
class A{
}
class B extends A{
  @Before("call(* org..*(..))")
  public void someCall(){
  }
}
