// "@Before on non-public method"

import org.aspectj.lang.annotation.*;

aspect A{
	  @Before("call(* org..*(..))")
	  private void someCall(){
	  }
}
