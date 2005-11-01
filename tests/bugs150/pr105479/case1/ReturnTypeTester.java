import java.util.HashSet;
import java.util.Set;

public class ReturnTypeTester {
  static Set<ReturnTypeTester> set = new HashSet<ReturnTypeTester>();
  static {
    ReturnTypeTester tester = new ReturnTypeTester();
    set.add(tester);
  }
 
  public String getId() {
    return "id";
  }
}