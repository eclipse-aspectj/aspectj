package c.d;

import org.aspectj.lang.annotation.*;

@Aspect
public class AtDurationMethod extends AbstractDurationMethod {
  @Pointcut("within(a.b.*) && call(public * a..*(..))")
  public void methods()  {}
}
