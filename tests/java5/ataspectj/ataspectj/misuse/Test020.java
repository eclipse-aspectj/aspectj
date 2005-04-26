// "@Before on non-public method"
package ataspectj.misuse;

import org.aspectj.lang.annotation.*;

@Aspect
public class Test020 {
	  @Before("call(* org..*(..))")
	  private void someCall(){
	  }
}
