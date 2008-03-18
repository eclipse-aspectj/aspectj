import java.util.*;
import org.aspectj.lang.annotation.*;

@Aspect
public class Broken {

	@Pointcut("call(* someMethod(..)) && args(arg1) && if()")
    public static boolean someMethod2if(int arg1) {
      return true;
    }

    @Pointcut("cflow(execution(* doProcess(..) ) && args(*, args)) && this(SomeClass+) ")
    public void inSomeClass2(Map args) {}

    @After( "inSomeClass2(args) && someMethod2if(arg1) ")
    public void deleteManagerInSomeClass2(Map args,int arg1) { }
    
    public static void main(String[] args) {
		new SomeClass().doProcess("a",new HashMap());
	}
}

class SomeClass {

  public void doProcess(Object o, Map m) {
    someMethod(1);
  }

  public void someMethod(int a) { }
}
