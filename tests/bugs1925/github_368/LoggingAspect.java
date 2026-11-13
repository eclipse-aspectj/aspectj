import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class LoggingAspect {

  @Before("handler(Exception+) && args(ex)")
  public void handle(JoinPoint jp, Exception ex) {
    System.out.println("advice fired for " + ex);
  }
}
