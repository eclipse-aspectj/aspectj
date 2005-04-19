// "@Before and @After on one method"

import org.aspectj.lang.annotation.*;

aspect A{
	  @Before("call(* *.*(..))")
      @After("call(* *.*(..))")
	  public void someCall(){
	  }
}
