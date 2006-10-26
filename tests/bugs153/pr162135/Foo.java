import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect public class Foo {

  public void m() {
    new RuntimeException("hello");
  }
	
  public static void main(String[] argv) {
  }
		
  @Pointcut("call(Throwable+.new(String, ..)) && this(caller) && if()")
  public static boolean exceptionInitializer(Object caller) {
      return true;
  }

  @Around("exceptionInitializer(caller)")
  public Object annotateException(ProceedingJoinPoint jp, Object caller) {
      return null;
  }
}
