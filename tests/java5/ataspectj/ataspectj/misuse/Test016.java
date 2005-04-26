// "@Pointcut with throws clause"
package ataspectj.misuse;

import org.aspectj.lang.annotation.*;

@Aspect
public class Test016{
  @Pointcut("call(* *.*(..))")
  void someCall() throws Exception {}
}
