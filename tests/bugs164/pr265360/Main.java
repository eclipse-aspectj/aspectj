package example;

import org.aspectj.lang.annotation.*;

import java.util.Date;

@Aspect
public class Main {
  @Pointcut("execution(Date foo())") 
  public void pc() {}
  
  @Before("pc()")
  public void log() {}
  
}
