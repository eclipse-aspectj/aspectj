package a.b.c;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;


class Foo {
  public void m() {
	  throw new RuntimeException("Hello World");
  }


}
@Aspect
public class Real {

  public static void main(String []argv) {
	  try {
	  new Foo().m();
	  } catch (Throwable t) {
		 System.out.println(t.getMessage());
	  }
  }

    @Pointcut("call(Throwable+.new(String, ..)) && this(caller) && args(exceptionMessage) && if()")
    public static boolean exceptionInitializer(Object caller, String exceptionMessage) {
        return isNdcEmpty();
    }

    @Around("exceptionInitializer(caller, exceptionMessage)")
    public Object annotateException(ProceedingJoinPoint jp, Object caller, String exceptionMessage) {
	System.out.println("advice running");
        return jp.proceed(new Object[]{caller, "newmessage"});
    }

    private static boolean isNdcEmpty() {
        return NDC.getDepth() == 0;
    }

}


class NDC {
  public static int getDepth() { return 0; }
}
