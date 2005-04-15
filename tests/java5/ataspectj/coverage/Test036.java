//"@Before twice on one method"

import org.aspectj.lang.annotation.*;

aspect A{
	  @Before("call(* *.*(..))")
      @Before("call(* *.*(..))")
	  public void someCall(){
	  }
}
