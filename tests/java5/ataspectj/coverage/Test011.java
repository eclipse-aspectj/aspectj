// "@Pointcut on non-aspect class method"

// I dont think this test is valid, you can have pointcuts in classes

import org.aspectj.lang.annotation.*;

class A{
	  @Pointcut("call(* *.*(..))")
	  void someCall(){
	  }
}
