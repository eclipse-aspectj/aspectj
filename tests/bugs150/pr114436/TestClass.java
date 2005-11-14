public class TestClass
{
  public void doSomething(String stuff)
  {
    System.out.println("TestClass.doSomething(\""+stuff+"\")");
  }
  
  public static void main(String[] args)
  {
    TestClass test = new TestClass();
    test.doSomething("withThis");
  }
}
