import java.util.HashSet;
import java.util.Set;

aspect ReturnTypeTest {
  private interface Test {
    Object getId();
    int hashCode();
  }
 
  public int Test.hashCode() {
	System.out.println("in Test.hashCode()");
    return getId().hashCode();
  }
 
  declare parents : ReturnTypeTester implements Test;
}

class ReturnTypeTester {
  static Set<ReturnTypeTester> set = new HashSet<ReturnTypeTester>();
  static {
    ReturnTypeTester tester = new ReturnTypeTester();
    set.add(tester);
  }
 
  public String getId() {
    return "id";
  }
}

public class pr105479part2 {
	
	public static void main(String[] args) {
		ReturnTypeTester rtt = new ReturnTypeTester();
		rtt.hashCode();
		System.out.println(rtt.getId());
		if (rtt.hashCode() != "id".hashCode()) throw new RuntimeException("dispatch failure");
	}
	
}