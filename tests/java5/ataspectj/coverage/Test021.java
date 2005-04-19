// "@Before on method not returning void"

import org.aspectj.lang.annotation.*;

aspect A{
	  @Before("call(* org..*(..))")
	  public int someCall(){
	    return 42;
	  }
}
