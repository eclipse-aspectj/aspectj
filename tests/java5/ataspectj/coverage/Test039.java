// "@Pointcut with an empty string"

import org.aspectj.lang.annotation.*;

@Aspect
class Foo {

  @Pointcut("call(* java.util.List.*(..))") // must qualify
  void listOperation() {}
 
  @Pointcut("") // should compile, I think - just matches no joinPoints
  void anyUtilityCall() {}
      
}
