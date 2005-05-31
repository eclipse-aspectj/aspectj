// "class with @Before extending @Aspect class"

// shouldn't allow advice in a non-aspect type
package ataspectj.misuse;

import org.aspectj.lang.annotation.*;

@Aspect
public abstract class Test006{
}
class Test006B extends Test006{
  @Before("call(* org..*(..))")
  public void someCall(){
  }
}
