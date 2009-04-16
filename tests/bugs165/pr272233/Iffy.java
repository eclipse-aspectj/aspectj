import java.util.*;
import org.aspectj.lang.annotation.*;

@Aspect 
class A {
@Pointcut("execution(!void<?>[] *(..))") 
void pointCutError() {}

  @Before("pointCutError()")
  public void m() {}
}


public class Iffy {


public Collection<?>[] getCollectionArray() {
        return null;
}

}
