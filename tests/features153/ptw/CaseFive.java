import java.lang.reflect.Method;

import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

class AClass {}
class BClass {}
class CClass {}

@Aspect("pertypewithin(*Class)")
public class CaseFive {
  public static void main(String []argv) throws Exception {
    new Runner().run();
  }
}

class Runner {
  public void run() throws Exception {
    if (Aspects14.hasAspect(CaseFive.class,AClass.class)) {
      System.out.println("AClass has an aspect instance");
      CaseFive instance = (CaseFive)Aspects14.aspectOf(CaseFive.class,AClass.class);
      Method m = CaseFive.class.getDeclaredMethod("getWithinTypeName",null);
      String s = (String)m.invoke(instance,null);
      System.out.println("The aspect instance thinks it is for type name "+s);
    }
  }
}
