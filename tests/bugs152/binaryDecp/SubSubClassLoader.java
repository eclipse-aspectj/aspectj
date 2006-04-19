import java.util.*;

public class SubSubClassLoader {
  private static String l;

  class Inner2 {
   public void foo() {
     System.err.println(l.toString());
   }
  }

}
