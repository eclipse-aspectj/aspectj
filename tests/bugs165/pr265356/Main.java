import org.aspectj.lang.annotation.*;

import java.util.Date;

@Aspect
public class Main {
  @Pointcut("execution(java.util.Date foo())") 
  public void pc() {}

  @Before("pc()")
  public void log() {}

  @Before("execution(List goo())")
  public void log2() {} 
}

