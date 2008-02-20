
public class DeclareParentsPrecedenceTest {
  public static void main(String[]argv) {
    TestClass t = new TestClass();
    if (!(t instanceof Interface1)) throw new RuntimeException("t not instanceof Interface1");
    if (!(t instanceof Interface1TestClass)) throw new RuntimeException("t not instanceof Interface1TestClass");
  }
}
