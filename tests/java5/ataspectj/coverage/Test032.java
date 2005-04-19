// "calling @Before advice explicitly as a method"

import org.aspectj.lang.annotation.*;

@Aspect
class A{
  @Before("call(* *.*(..))")
  public void someCall(){
  }
}
class B{
  public static void main(String[] args){
    A a = new A();
    a.someCall();
  }
}
