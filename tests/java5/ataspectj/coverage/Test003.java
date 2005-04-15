// "Codestyle Aspect with @Pointcut"

import org.aspectj.lang.annotation.*;

aspect A{
	  @Pointcut("call(* *.*(..))")
	  void someCall(int aNumber){
	  }
}
