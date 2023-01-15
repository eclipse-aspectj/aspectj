import java.util.*;
import org.aspectj.lang.annotation.*;

@Aspect
class Iffy2 {

  @Before("execution(!void *(..))")
  public void advice1() {}

  @Before("execution(!void[] *(..))")
  public void advice2() {}

  @Before("execution(!void *(..))")
  public void advice3() {}

  @Before("execution(*..Collection[] *(..))")
  public void advice4() {}

  @Before("execution(java.util.Collection<?>[] *(..))")
  public void advice5() {}

  public Collection<?>[] getCollectionArray() {
        return null;
  }
}
