//"@AdviceName used on @Before advice"

import org.aspectj.lang.annotation.*;

aspect A{
  @AdviceName("callFromFoo")
  @Before("call(* org.aspectprogrammer..*(..))")
  public void callFromFoo() {
    System.out.println("Call from Foo");
  }
}
