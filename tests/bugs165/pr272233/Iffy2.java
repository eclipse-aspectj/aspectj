import java.util.*;
import org.aspectj.lang.annotation.*;

@Aspect 
class Iffy2 {

  @Before("execution(!void[] *(..))") 
  public void advice1() {}

  @Before("execution(!void[] *(..))") 
  public void advice2() {}

  public Collection<?>[] getCollectionArray() {
        return null;
  }
}
