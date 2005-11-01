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

  public static void main(String []argv) {
    ReturnTypeTester rtt = new ReturnTypeTester();
    System.err.println("xxx");
    rtt.hashCode();
    System.err.println("yyy");
    rtt.getId();
    System.err.println("zzz");
  }
}
