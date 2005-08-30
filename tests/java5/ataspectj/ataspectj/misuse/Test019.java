// "@AfterReturning with wrong number of args"
package ataspectj.misuse;

import org.aspectj.lang.annotation.*;

@Aspect
public class Test019 {
  @AfterReturning(value="call(* *..*(..))",returning="f")
  public void itsAFoo(int x, Object f) {
  }        
}
