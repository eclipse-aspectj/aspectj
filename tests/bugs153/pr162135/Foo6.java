import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect public class Foo6 {

  public void m() {
    new RuntimeException("hello");
  }
	
  public static void main(String[] argv) {
	  try {
	   new Foo6().m();
	  } catch (Throwable t) {}
  }
		
  @Pointcut("call(Throwable+.new(String, ..)) && this(caller) && if()")
  public static boolean exceptionInitializer(Object caller) {
      return true;
  }

  @Around("exceptionInitializer(caller)")
  public Object annotateException(ProceedingJoinPoint jp, Object caller) {
      System.out.println("ProceedingJoinPoint is "+jp);
      System.out.println("caller is "+(caller==null?"null":"notnull"));
      return null;
  }
}
