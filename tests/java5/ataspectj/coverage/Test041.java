// "@AdviceName given an empty string"

import org.aspectj.lang.annotation.*;

aspect A{
 @SuppressAjWarnings
  @AdviceName("")
  before() : call(* org.aspectprogrammer..*(..)) {
    System.out.println("Call from Foo");
  }
}
