// "@Before on method not returning void"
package ataspectj.misuse;

import org.aspectj.lang.annotation.*;

@Aspect
public class Test021 {
	  @Before("call(* org..*(..))")
	  public int someCall(){
	    return 42;
	  }
}
