// "@Before with && in string"

import org.aspectj.lang.annotation.*;

class Foo{
}
aspect A{
  @SuppressAjWarnings
  @Before("call(* org.aspectprogrammer..*(..)) && this(Foo)")
  public void callFromFoo() {
    System.out.println("Call from Foo");
  }
}
